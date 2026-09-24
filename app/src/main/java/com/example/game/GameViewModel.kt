package com.example.game

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.GameDatabase
import com.example.data.GameProfile
import com.example.data.GameRepository
import com.example.model.DinoSkin
import com.example.model.GameMode
import com.example.model.SkinCatalog
import com.example.model.UpgradeCatalog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class PlayState(
    val isRunning: Boolean = false,
    val isGameOver: Boolean = false,
    val isPaused: Boolean = false,
    val score: Float = 0f,
    val coinsThisRun: Int = 0,
    val obstaclesDodged: Int = 0,
    val currentMode: GameMode = GameMode.COLOR,
    val activeSkin: DinoSkin = SkinCatalog.skins.first(),

    // Player physics
    val dinoY: Float = 0f, // 0 = ground, positive = in air
    val dinoVy: Float = 0f,
    val isDucking: Boolean = false,
    val jumpsLeft: Int = 1,
    val maxJumps: Int = 1,
    val runFrame: Int = 0, // 0 or 1 for leg animation

    // Powerups & buffs
    val hasShield: Boolean = false,
    val revivesLeft: Int = 0,
    val magnetTimer: Float = 0f,
    val slowMoTimer: Float = 0f,
    val multiplierTimer: Float = 0f,

    // Obstacles & Items
    val obstacles: List<Obstacle> = emptyList(),
    val collectibles: List<Collectible> = emptyList(),
    val particles: List<GameParticle> = emptyList(),
    val clouds: List<CloudItem> = emptyList(),

    // Visual world
    val groundOffset: Float = 0f,
    val dayNightPhase: Float = 0f, // 0..1
    val milestoneTrigger: Boolean = false,
    val gameSpeed: Float = 420f,
    val nextSpawnDistance: Float = 400f
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    val soundManager = SoundManager(application.applicationContext)

    val profileFlow: StateFlow<GameProfile>
    private val _playState = MutableStateFlow(PlayState())
    val playState: StateFlow<PlayState> = _playState.asStateFlow()

    private var gameLoopJob: Job? = null
    private var nextObstacleId = 1L
    private var nextCollectibleId = 1L
    private var nextCloudId = 1L
    private var distanceSinceLastSpawn = 0f
    private var lastMilestoneScore = 0

    init {
        val db = GameDatabase.getInstance(application)
        repository = GameRepository(db.gameDao())

        val initialProfile = MutableStateFlow(GameProfile())
        profileFlow = initialProfile

        viewModelScope.launch {
            repository.profileFlow.collect { profile ->
                initialProfile.value = profile
                val mode = GameMode.fromId(profile.activeGameMode)
                val skin = SkinCatalog.find(profile.selectedSkinId)
                _playState.update { it.copy(currentMode = mode, activeSkin = skin) }
            }
        }

        initEnvironment()
    }

    private fun initEnvironment() {
        val clouds = mutableListOf<CloudItem>()
        for (i in 0 until 5) {
            clouds.add(
                CloudItem(
                    id = nextCloudId++,
                    x = Random.nextFloat() * 1200f,
                    y = Random.nextFloat() * 140f + 20f,
                    width = Random.nextFloat() * 40f + 55f,
                    speed = Random.nextFloat() * 25f + 15f
                )
            )
        }
        _playState.update { it.copy(clouds = clouds) }
    }

    fun startGame() {
        val profile = profileFlow.value
        val mode = GameMode.fromId(profile.activeGameMode)
        val skin = SkinCatalog.find(profile.selectedSkinId)
        val maxJumps = if (profile.doubleJumpLevel > 0) 2 else 1
        val initialShield = profile.startShieldLevel > 0
        val initialRevives = profile.reviveLevel

        distanceSinceLastSpawn = 0f
        lastMilestoneScore = 0

        _playState.value = PlayState(
            isRunning = true,
            isGameOver = false,
            isPaused = false,
            score = 0f,
            coinsThisRun = 0,
            obstaclesDodged = 0,
            currentMode = mode,
            activeSkin = skin,
            dinoY = 0f,
            dinoVy = 0f,
            isDucking = false,
            jumpsLeft = maxJumps,
            maxJumps = maxJumps,
            hasShield = initialShield,
            revivesLeft = initialRevives,
            magnetTimer = 0f,
            slowMoTimer = 0f,
            gameSpeed = when (mode) {
                GameMode.CLASSIC -> 430f
                GameMode.COLOR -> 460f
                GameMode.REALISTIC -> 480f
            },
            clouds = _playState.value.clouds
        )

        startGameLoop()
    }

    fun pauseGame() {
        _playState.update { it.copy(isPaused = !it.isPaused) }
    }

    fun resumeGame() {
        _playState.update { it.copy(isPaused = false) }
    }

    fun jump() {
        val state = _playState.value
        if (!state.isRunning) {
            startGame()
            return
        }
        if (state.isGameOver || state.isPaused) return

        if (state.jumpsLeft > 0) {
            val isDoubleJump = state.jumpsLeft < state.maxJumps
            val profile = profileFlow.value
            val jumpPower = if (isDoubleJump) {
                when (profile.doubleJumpLevel) {
                    1 -> 580f
                    2 -> 640f
                    else -> 680f
                }
            } else {
                620f
            }

            if (isDoubleJump) {
                soundManager.playDoubleJump(profile.soundEnabled, profile.vibrationEnabled)
            } else {
                soundManager.playJump(profile.soundEnabled, profile.vibrationEnabled)
            }

            // Spawn jump dust particles
            val newParticles = state.particles.toMutableList()
            for (i in 0 until 5) {
                newParticles.add(
                    GameParticle(
                        x = 100f + Random.nextFloat() * 20f,
                        y = 10f,
                        vx = -Random.nextFloat() * 80f - 20f,
                        vy = Random.nextFloat() * 40f + 10f,
                        alpha = 0.8f,
                        color = state.currentMode.accentColor,
                        size = Random.nextFloat() * 4f + 3f,
                        life = 1f
                    )
                )
            }

            _playState.update {
                it.copy(
                    dinoVy = jumpPower,
                    jumpsLeft = it.jumpsLeft - 1,
                    isDucking = false,
                    particles = newParticles
                )
            }
        }
    }

    fun setDucking(ducking: Boolean) {
        val state = _playState.value
        if (!state.isRunning || state.isGameOver || state.isPaused) return
        _playState.update { it.copy(isDucking = ducking) }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastTime = System.nanoTime()
            while (isActive) {
                val currentTime = System.nanoTime()
                val delta = ((currentTime - lastTime) / 1_000_000_000.0).toFloat().coerceIn(0.005f, 0.05f)
                lastTime = currentTime

                val currentState = _playState.value
                if (currentState.isRunning && !currentState.isGameOver && !currentState.isPaused) {
                    updatePhysics(delta)
                }

                delay(16) // ~60 fps
            }
        }
    }

    private fun updatePhysics(dt: Float) {
        val state = _playState.value
        val profile = profileFlow.value

        // Speed multiplier calculations (slow-mo powerup or slow-mo upgrade)
        val slowMoFactor = if (state.slowMoTimer > 0f) 0.65f else {
            if (profile.slowReflexLevel > 0) 0.92f else 1.0f
        }
        val effectiveSpeed = state.gameSpeed * slowMoFactor

        // Gravity & Jump
        val gravity = -1450f
        var newDinoY = state.dinoY + state.dinoVy * dt
        var newDinoVy = state.dinoVy + gravity * dt
        var newJumpsLeft = state.jumpsLeft

        if (newDinoY <= 0f) {
            newDinoY = 0f
            newDinoVy = 0f
            newJumpsLeft = state.maxJumps
        }

        // Score increment
        val scoreInc = dt * (effectiveSpeed / 40f)
        val newScore = state.score + scoreInc
        val currentIntScore = newScore.toInt()

        // 100 points milestone chime
        var milestoneTrigger = false
        if (currentIntScore >= lastMilestoneScore + 100 && currentIntScore > 0) {
            lastMilestoneScore = (currentIntScore / 100) * 100
            milestoneTrigger = true
            soundManager.playPowerup(profile.soundEnabled, profile.vibrationEnabled)
        }

        // Speed ramp up slowly with score
        val newSpeed = (420f + (newScore * 0.18f)).coerceAtMost(950f)

        // Day/night progress (one full cycle every 500 points)
        val newDayNight = (newScore % 500f) / 500f

        // Leg animation frame switch
        val runFrame = if (newDinoY > 0f) 0 else ((newScore * 0.12f).toInt() % 2)

        // Ground scrolling offset
        val newGroundOffset = (state.groundOffset + effectiveSpeed * dt) % 800f

        // Timers update
        val newMagnetTimer = (state.magnetTimer - dt).coerceAtLeast(0f)
        val newSlowMoTimer = (state.slowMoTimer - dt).coerceAtLeast(0f)
        val newMultiplierTimer = (state.multiplierTimer - dt).coerceAtLeast(0f)

        // Clouds movement
        val updatedClouds = state.clouds.map { cloud ->
            var newX = cloud.x - cloud.speed * dt
            if (newX < -150f) {
                newX = 1200f + Random.nextFloat() * 100f
            }
            cloud.copy(x = newX)
        }

        // Obstacles movement & dodging
        val updatedObstacles = mutableListOf<Obstacle>()
        var obstaclesDodged = state.obstaclesDodged
        var collidedObstacle: Obstacle? = null

        // Dino hitbox dimensions
        val dinoX = 80f
        val dinoWidth = 46f
        val dinoHeight = if (state.isDucking) 28f else 54f
        val dinoBox = RectF(dinoX, newDinoY, dinoX + dinoWidth, newDinoY + dinoHeight)

        for (obs in state.obstacles) {
            val newX = obs.x - effectiveSpeed * dt
            if (newX + obs.width < 0f) {
                obstaclesDodged++
                continue
            }

            // Check collision with dino
            val obsBox = RectF(newX, obs.y, newX + obs.width, obs.y + obs.height)
            if (dinoBox.intersects(obsBox)) {
                collidedObstacle = obs
            }

            // Wing flapping for pterodactyls
            val wing = if (obs.type == ObstacleType.PTERODACTYL_HIGH || obs.type == ObstacleType.PTERODACTYL_LOW) {
                (newScore * 0.25f).toInt() % 2 == 0
            } else false

            updatedObstacles.add(obs.copy(x = newX, wingState = wing))
        }

        // Collectibles movement and magnet attraction
        val updatedCollectibles = mutableListOf<Collectible>()
        var coinsCollected = state.coinsThisRun
        var hasShield = state.hasShield
        var magnetTime = newMagnetTimer
        var slowMoTime = newSlowMoTimer
        val magnetRadius = if (profile.magnetLevel > 0) 180f + (profile.magnetLevel * 80f) else 100f
        val isMagnetPulling = newMagnetTimer > 0f || profile.magnetLevel >= 3

        for (item in state.collectibles) {
            var itemX = item.x - effectiveSpeed * dt
            var itemY = item.y

            // Magnet effect: pull toward dino
            if (isMagnetPulling) {
                val dx = dinoX - itemX
                val dy = (newDinoY + 20f) - itemY
                val dist = kotlin.math.hypot(dx, dy)
                if (dist < magnetRadius) {
                    itemX += (dx / dist) * 450f * dt
                    itemY += (dy / dist) * 450f * dt
                }
            }

            if (itemX + item.size < 0f) continue

            // Hit test with dino
            val itemBox = RectF(itemX, itemY, itemX + item.size, itemY + item.size)
            if (dinoBox.intersects(itemBox)) {
                // Collect!
                when (item.type) {
                    CollectibleType.COIN -> {
                        val baseVal = 1
                        val mult = when (profile.coinMultiplierLevel) {
                            1 -> 2
                            2 -> 3
                            3 -> 4
                            else -> 1
                        }
                        coinsCollected += baseVal * mult
                        soundManager.playCoin(profile.soundEnabled, profile.vibrationEnabled)
                    }
                    CollectibleType.FOSSIL_AMBER -> {
                        coinsCollected += 5
                        soundManager.playCoin(profile.soundEnabled, profile.vibrationEnabled)
                    }
                    CollectibleType.POWER_SHIELD -> {
                        hasShield = true
                        soundManager.playPowerup(profile.soundEnabled, profile.vibrationEnabled)
                    }
                    CollectibleType.POWER_MAGNET -> {
                        magnetTime = 8f + (profile.magnetLevel * 2f)
                        soundManager.playPowerup(profile.soundEnabled, profile.vibrationEnabled)
                    }
                    CollectibleType.POWER_SLOW_MO -> {
                        slowMoTime = 6f
                        soundManager.playPowerup(profile.soundEnabled, profile.vibrationEnabled)
                    }
                }
            } else {
                updatedCollectibles.add(item.copy(x = itemX, y = itemY, pulsePhase = item.pulsePhase + dt * 4f))
            }
        }

        // Particle system update
        val updatedParticles = state.particles.mapNotNull { p ->
            val newLife = p.life - dt * 2.2f
            if (newLife <= 0f) null
            else p.copy(
                x = p.x + p.vx * dt,
                y = p.y + p.vy * dt,
                alpha = newLife.coerceIn(0f, 1f),
                life = newLife
            )
        }.toMutableList()

        // Distance accumulator for spawner
        distanceSinceLastSpawn += effectiveSpeed * dt
        if (distanceSinceLastSpawn >= state.nextSpawnDistance) {
            distanceSinceLastSpawn = 0f
            spawnObstaclesAndCollectibles(
                state.currentMode,
                newScore,
                updatedObstacles,
                updatedCollectibles
            )
        }

        // Collision handling
        if (collidedObstacle != null) {
            if (hasShield) {
                // Shield absorbs collision!
                hasShield = false
                soundManager.playHit(profile.soundEnabled, profile.vibrationEnabled)
                updatedObstacles.remove(collidedObstacle)

                // Spawn shield break particles
                for (i in 0 until 12) {
                    val angle = (i * 30.0 * Math.PI / 180.0)
                    updatedParticles.add(
                        GameParticle(
                            x = dinoX + dinoWidth / 2f,
                            y = newDinoY + dinoHeight / 2f,
                            vx = (cos(angle) * 160.0).toFloat(),
                            vy = (sin(angle) * 160.0).toFloat(),
                            alpha = 1f,
                            color = Color(0xFF38BDF8),
                            size = 6f,
                            life = 1f
                        )
                    )
                }
            } else if (state.revivesLeft > 0) {
                // Auto-revive triggered!
                val revivesLeft = state.revivesLeft - 1
                soundManager.playHit(profile.soundEnabled, profile.vibrationEnabled)
                updatedObstacles.clear() // clear obstacles near player

                _playState.update {
                    it.copy(
                        revivesLeft = revivesLeft,
                        hasShield = true, // give brief shield after revive
                        particles = updatedParticles
                    )
                }
                return
            } else {
                // Game Over!
                triggerGameOver(
                    finalScore = newScore.toInt(),
                    coinsEarned = coinsCollected,
                    obstaclesDodged = obstaclesDodged,
                    mode = state.currentMode
                )
                return
            }
        }

        _playState.update {
            it.copy(
                score = newScore,
                coinsThisRun = coinsCollected,
                obstaclesDodged = obstaclesDodged,
                dinoY = newDinoY,
                dinoVy = newDinoVy,
                jumpsLeft = newJumpsLeft,
                hasShield = hasShield,
                magnetTimer = magnetTime,
                slowMoTimer = slowMoTime,
                multiplierTimer = newMultiplierTimer,
                runFrame = runFrame,
                groundOffset = newGroundOffset,
                dayNightPhase = newDayNight,
                milestoneTrigger = milestoneTrigger,
                gameSpeed = newSpeed,
                obstacles = updatedObstacles,
                collectibles = updatedCollectibles,
                particles = updatedParticles,
                clouds = updatedClouds
            )
        }
    }

    private fun spawnObstaclesAndCollectibles(
        mode: GameMode,
        score: Float,
        obstacles: MutableList<Obstacle>,
        collectibles: MutableList<Collectible>
    ) {
        val spawnX = 920f
        val roll = Random.nextFloat()

        // Choose obstacle based on game mode & score
        val obstacleType = when (mode) {
            GameMode.CLASSIC -> {
                if (score > 120f && roll > 0.65f) {
                    if (roll > 0.82f) ObstacleType.PTERODACTYL_HIGH else ObstacleType.PTERODACTYL_LOW
                } else if (roll > 0.35f) {
                    ObstacleType.CACTUS_LARGE
                } else {
                    ObstacleType.CACTUS_SMALL
                }
            }
            GameMode.COLOR -> {
                if (score > 100f && roll > 0.6f) {
                    if (roll > 0.8f) ObstacleType.PTERODACTYL_HIGH else ObstacleType.PTERODACTYL_LOW
                } else if (roll > 0.3f) {
                    ObstacleType.CACTUS_DOUBLE
                } else {
                    ObstacleType.CACTUS_LARGE
                }
            }
            GameMode.REALISTIC -> {
                if (roll > 0.7f) {
                    ObstacleType.PTERODACTYL_LOW
                } else if (roll > 0.4f) {
                    ObstacleType.VOLCANIC_BOULDER
                } else {
                    ObstacleType.PREHISTORIC_SPIRE
                }
            }
        }

        val (width, height, yPos) = when (obstacleType) {
            ObstacleType.CACTUS_SMALL -> Triple(24f, 42f, 0f)
            ObstacleType.CACTUS_LARGE -> Triple(32f, 58f, 0f)
            ObstacleType.CACTUS_DOUBLE -> Triple(52f, 55f, 0f)
            ObstacleType.PTERODACTYL_LOW -> Triple(44f, 32f, 25f) // jump over
            ObstacleType.PTERODACTYL_HIGH -> Triple(46f, 34f, 65f) // duck under or small jump
            ObstacleType.VOLCANIC_BOULDER -> Triple(42f, 44f, 0f)
            ObstacleType.PREHISTORIC_SPIRE -> Triple(36f, 62f, 0f)
        }

        obstacles.add(
            Obstacle(
                id = nextObstacleId++,
                x = spawnX,
                y = yPos,
                width = width,
                height = height,
                type = obstacleType
            )
        )

        // Spawn collectibles around obstacles (over cactus or following arc)
        val coinRoll = Random.nextFloat()
        if (coinRoll > 0.3f) {
            val coinType = when {
                coinRoll > 0.95f -> CollectibleType.POWER_SHIELD
                coinRoll > 0.90f -> CollectibleType.POWER_MAGNET
                coinRoll > 0.85f -> CollectibleType.POWER_SLOW_MO
                coinRoll > 0.70f -> CollectibleType.FOSSIL_AMBER
                else -> CollectibleType.COIN
            }

            val coinY = if (yPos > 0) 0f else (height + 25f + Random.nextFloat() * 30f)
            collectibles.add(
                Collectible(
                    id = nextCollectibleId++,
                    x = spawnX + width / 2f,
                    y = coinY,
                    size = if (coinType == CollectibleType.COIN) 20f else 24f,
                    type = coinType
                )
            )
        }

        // Randomize next spawn interval
        val minGap = 320f.coerceAtLeast(420f - (score * 0.1f))
        val maxGap = 580f
        _playState.update { it.copy(nextSpawnDistance = Random.nextFloat() * (maxGap - minGap) + minGap) }
    }

    private fun triggerGameOver(finalScore: Int, coinsEarned: Int, obstaclesDodged: Int, mode: GameMode) {
        val profile = profileFlow.value
        soundManager.playGameOver(profile.soundEnabled, profile.vibrationEnabled)

        _playState.update {
            it.copy(
                isRunning = false,
                isGameOver = true
            )
        }

        viewModelScope.launch {
            repository.recordGameRun(
                score = finalScore,
                coinsEarned = coinsEarned,
                obstaclesDojed = obstaclesDodged,
                mode = mode.id
            )
        }
    }

    fun setGameMode(mode: GameMode) {
        viewModelScope.launch {
            repository.setActiveGameMode(mode.id)
            _playState.update { it.copy(currentMode = mode) }
        }
    }

    fun buyUpgrade(upgradeKey: String, cost: Int, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.purchaseUpgrade(upgradeKey, cost)
            if (success) {
                soundManager.playPurchase(profileFlow.value.soundEnabled, profileFlow.value.vibrationEnabled)
                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    fun buySkin(skinId: String, cost: Int, onSuccess: () -> Unit = {}, onFailure: () -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.purchaseSkin(skinId, cost)
            if (success) {
                soundManager.playPurchase(profileFlow.value.soundEnabled, profileFlow.value.vibrationEnabled)
                onSuccess()
            } else {
                onFailure()
            }
        }
    }

    fun equipSkin(skinId: String) {
        viewModelScope.launch {
            repository.equipSkin(skinId)
            val skin = SkinCatalog.find(skinId)
            _playState.update { it.copy(activeSkin = skin) }
        }
    }

    fun claimBonusCoins(amount: Int = 100) {
        viewModelScope.launch {
            repository.addCoins(amount)
            soundManager.playCoin(profileFlow.value.soundEnabled, profileFlow.value.vibrationEnabled)
        }
    }

    fun toggleSound() {
        viewModelScope.launch {
            repository.toggleSound(!profileFlow.value.soundEnabled)
        }
    }

    fun toggleVibration() {
        viewModelScope.launch {
            repository.toggleVibration(!profileFlow.value.vibrationEnabled)
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        soundManager.release()
    }
}
