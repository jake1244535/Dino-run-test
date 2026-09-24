package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class GameRepository(private val gameDao: GameDao) {

    val profileFlow: Flow<GameProfile> = gameDao.getGameProfile()
        .onStart {
            ensureProfileInitialized()
        }
        .map { it ?: GameProfile() }

    private suspend fun ensureProfileInitialized() {
        val existing = gameDao.getGameProfileOnce()
        if (existing == null) {
            gameDao.insertOrUpdateProfile(GameProfile())
        }
    }

    suspend fun getProfile(): GameProfile {
        return gameDao.getGameProfileOnce() ?: GameProfile().also {
            gameDao.insertOrUpdateProfile(it)
        }
    }

    suspend fun updateProfile(profile: GameProfile) {
        gameDao.insertOrUpdateProfile(profile)
    }

    suspend fun recordGameRun(
        score: Int,
        coinsEarned: Int,
        obstaclesDojed: Int,
        mode: String
    ): GameProfile {
        val current = getProfile()
        val updatedHighScoreClassic = if (mode == "CLASSIC") maxOf(current.highScoreClassic, score) else current.highScoreClassic
        val updatedHighScoreColor = if (mode == "COLOR") maxOf(current.highScoreColor, score) else current.highScoreColor
        val updatedHighScoreRealistic = if (mode == "REALISTIC") maxOf(current.highScoreRealistic, score) else current.highScoreRealistic

        val updated = current.copy(
            coins = current.coins + coinsEarned,
            totalCoinsEarned = current.totalCoinsEarned + coinsEarned,
            totalGamesPlayed = current.totalGamesPlayed + 1,
            totalObstaclesDojed = current.totalObstaclesDojed + obstaclesDojed,
            highScoreClassic = updatedHighScoreClassic,
            highScoreColor = updatedHighScoreColor,
            highScoreRealistic = updatedHighScoreRealistic
        )
        gameDao.insertOrUpdateProfile(updated)
        return updated
    }

    suspend fun purchaseUpgrade(upgradeKey: String, cost: Int): Boolean {
        val current = getProfile()
        if (current.coins < cost) return false

        val updated = when (upgradeKey) {
            "double_jump" -> current.copy(coins = current.coins - cost, doubleJumpLevel = current.doubleJumpLevel + 1)
            "shield" -> current.copy(coins = current.coins - cost, startShieldLevel = current.startShieldLevel + 1)
            "magnet" -> current.copy(coins = current.coins - cost, magnetLevel = current.magnetLevel + 1)
            "multiplier" -> current.copy(coins = current.coins - cost, coinMultiplierLevel = current.coinMultiplierLevel + 1)
            "revive" -> current.copy(coins = current.coins - cost, reviveLevel = current.reviveLevel + 1)
            "slow_mo" -> current.copy(coins = current.coins - cost, slowReflexLevel = current.slowReflexLevel + 1)
            else -> return false
        }
        gameDao.insertOrUpdateProfile(updated)
        return true
    }

    suspend fun purchaseSkin(skinId: String, cost: Int): Boolean {
        val current = getProfile()
        if (current.coins < cost || current.isSkinUnlocked(skinId)) return false

        val updated = current.withUnlockedSkin(skinId).copy(
            coins = current.coins - cost,
            selectedSkinId = skinId
        )
        gameDao.insertOrUpdateProfile(updated)
        return true
    }

    suspend fun equipSkin(skinId: String): Boolean {
        val current = getProfile()
        if (!current.isSkinUnlocked(skinId)) return false
        val updated = current.copy(selectedSkinId = skinId)
        gameDao.insertOrUpdateProfile(updated)
        return true
    }

    suspend fun setActiveGameMode(mode: String) {
        val current = getProfile()
        val updated = current.copy(activeGameMode = mode)
        gameDao.insertOrUpdateProfile(updated)
    }

    suspend fun addCoins(amount: Int) {
        val current = getProfile()
        val updated = current.copy(
            coins = current.coins + amount,
            totalCoinsEarned = current.totalCoinsEarned + amount
        )
        gameDao.insertOrUpdateProfile(updated)
    }

    suspend fun toggleSound(enabled: Boolean) {
        val current = getProfile()
        gameDao.insertOrUpdateProfile(current.copy(soundEnabled = enabled))
    }

    suspend fun toggleVibration(enabled: Boolean) {
        val current = getProfile()
        gameDao.insertOrUpdateProfile(current.copy(vibrationEnabled = enabled))
    }
}
