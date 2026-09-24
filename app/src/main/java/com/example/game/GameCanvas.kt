package com.example.game

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.model.DinoSkin
import com.example.model.GameMode
import com.example.model.SkinHat
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    state: PlayState,
    onJump: () -> Unit,
    onDuckingChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Preload realistic assets safely
    val realisticBgBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_bg_realistic)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }
    val realisticDinoBitmap = remember {
        try {
            BitmapFactory.decodeResource(context.resources, R.drawable.img_dino_realistic)?.asImageBitmap()
        } catch (_: Exception) {
            null
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(state.isRunning, state.isGameOver) {
                detectTapGestures(
                    onTap = {
                        onJump()
                    },
                    onPress = {
                        // User can tap/hold or swipe
                        tryAwaitRelease()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        if (dragAmount.y > 15f) {
                            onDuckingChange(true)
                        } else if (dragAmount.y < -15f) {
                            onJump()
                            onDuckingChange(false)
                        }
                        change.consume()
                    },
                    onDragEnd = {
                        onDuckingChange(false)
                    },
                    onDragCancel = {
                        onDuckingChange(false)
                    }
                )
            }
    ) {
        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()
        val groundY = screenHeight * 0.72f

        Canvas(modifier = Modifier.fillMaxSize()) {
            when (state.currentMode) {
                GameMode.CLASSIC -> drawClassicWorld(state, groundY, screenWidth, screenHeight)
                GameMode.COLOR -> drawColorWorld(state, groundY, screenWidth, screenHeight)
                GameMode.REALISTIC -> drawRealisticWorld(
                    state,
                    groundY,
                    screenWidth,
                    screenHeight,
                    realisticBgBitmap,
                    realisticDinoBitmap
                )
            }

            // Draw Dino
            drawDino(
                mode = state.currentMode,
                skin = state.activeSkin,
                x = 80f,
                y = groundY - state.dinoY,
                isDucking = state.isDucking,
                runFrame = state.runFrame,
                inAir = state.dinoY > 0f,
                hasShield = state.hasShield,
                hasMagnet = state.magnetTimer > 0f,
                realisticBitmap = realisticDinoBitmap
            )

            // Draw Obstacles
            for (obs in state.obstacles) {
                drawObstacle(obs, groundY, state.currentMode)
            }

            // Draw Collectibles & Powerups
            for (col in state.collectibles) {
                drawCollectible(col, groundY, state.currentMode)
            }

            // Draw Particles
            for (p in state.particles) {
                drawCircle(
                    color = p.color.copy(alpha = p.alpha),
                    radius = p.size,
                    center = Offset(p.x, groundY - p.y)
                )
            }
        }
    }
}

private fun DrawScope.drawClassicWorld(
    state: PlayState,
    groundY: Float,
    width: Float,
    height: Float
) {
    // Classic pure crisp retro monochrome background
    drawRect(Color(0xFFF7F7F7))

    // Classic clouds
    val cloudColor = Color(0xFFC4C4C4)
    for (cloud in state.clouds) {
        val cx = cloud.x % (width + 200f) - 50f
        drawClassicCloud(cx, cloud.y, cloudColor)
    }

    // Classic ground line
    drawLine(
        color = Color(0xFF535353),
        start = Offset(0f, groundY),
        end = Offset(width, groundY),
        strokeWidth = 3f
    )

    // Classic ground dots / gravel pattern
    val dotColor = Color(0xFF757575)
    var dotX = -(state.groundOffset % 60f)
    var seed = 0
    while (dotX < width) {
        seed++
        if (seed % 3 == 0) {
            drawRect(dotColor, Offset(dotX, groundY + 6f), Size(12f, 2f))
        } else if (seed % 5 == 0) {
            drawRect(dotColor, Offset(dotX + 5f, groundY + 14f), Size(6f, 2f))
            drawRect(dotColor, Offset(dotX + 18f, groundY + 22f), Size(8f, 2f))
        }
        dotX += 28f
    }
}

private fun DrawScope.drawClassicCloud(x: Float, y: Float, color: Color) {
    // Stepped pixel cloud
    drawRect(color, Offset(x + 14f, y), Size(24f, 6f))
    drawRect(color, Offset(x + 6f, y + 6f), Size(40f, 6f))
    drawRect(color, Offset(x, y + 12f), Size(52f, 6f))
}

private fun DrawScope.drawColorWorld(
    state: PlayState,
    groundY: Float,
    width: Float,
    height: Float
) {
    // Dynamic Sky gradient (Day -> Sunset -> Night based on dayNightPhase)
    val phase = state.dayNightPhase
    val skyBrush = when {
        phase < 0.45f -> {
            // Day
            Brush.verticalGradient(
                colors = listOf(Color(0xFF60A5FA), Color(0xFF93C5FD), Color(0xFFE0F2FE)),
                startY = 0f,
                endY = groundY
            )
        }
        phase < 0.75f -> {
            // Sunset / Twilight
            Brush.verticalGradient(
                colors = listOf(Color(0xFF4338CA), Color(0xFFBE185D), Color(0xFFF97316), Color(0xFFFDE68A)),
                startY = 0f,
                endY = groundY
            )
        }
        else -> {
            // Starry Night
            Brush.verticalGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF312E81)),
                startY = 0f,
                endY = groundY
            )
        }
    }
    drawRect(brush = skyBrush, size = Size(width, groundY))

    // Stars at night
    if (phase >= 0.75f) {
        for (i in 0 until 18) {
            val sx = (i * 73f) % width
            val sy = ((i * 37f) % (groundY * 0.6f)) + 15f
            drawCircle(Color.White.copy(alpha = 0.8f), radius = 1.8f, center = Offset(sx, sy))
        }
    }

    // Fluffy Colorful Clouds
    for (cloud in state.clouds) {
        val cx = cloud.x % (width + 200f) - 50f
        val cloudColor = if (phase >= 0.75f) Color(0x66CBD5E1) else Color.White.copy(alpha = 0.85f)
        drawCircle(cloudColor, 16f, Offset(cx + 18f, cloud.y + 12f))
        drawCircle(cloudColor, 22f, Offset(cx + 34f, cloud.y + 10f))
        drawCircle(cloudColor, 18f, Offset(cx + 52f, cloud.y + 14f))
        drawRoundRect(
            cloudColor,
            Offset(cx + 6f, cloud.y + 12f),
            Size(58f, 18f),
            CornerRadius(9f, 9f)
        )
    }

    // Distant Sand Dunes in parallax
    val duneBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBBF24).copy(alpha = 0.4f), Color(0xFFD97706).copy(alpha = 0.5f)),
        startY = groundY - 60f,
        endY = groundY
    )
    val dunePath = Path().apply {
        moveTo(0f, groundY)
        var dx = 0f
        while (dx <= width + 100f) {
            val duneH = 22f + 16f * sin((dx + state.groundOffset * 0.3f) * 0.015f)
            lineTo(dx, groundY - duneH)
            dx += 40f
        }
        lineTo(width, groundY)
        close()
    }
    drawPath(dunePath, duneBrush)

    // Ground: warm vibrant desert floor
    val groundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFF78350F)),
        startY = groundY,
        endY = height
    )
    drawRect(brush = groundBrush, topLeft = Offset(0f, groundY), size = Size(width, height - groundY))

    // Vibrant ground edge line
    drawLine(
        color = Color(0xFFFDE68A),
        start = Offset(0f, groundY),
        end = Offset(width, groundY),
        strokeWidth = 4f
    )

    // Little desert stones
    var stoneX = -(state.groundOffset % 80f)
    while (stoneX < width) {
        drawRoundRect(
            color = Color(0xFF92400E).copy(alpha = 0.6f),
            topLeft = Offset(stoneX + 10f, groundY + 8f),
            size = Size(10f, 4f),
            cornerRadius = CornerRadius(2f, 2f)
        )
        stoneX += 45f
    }
}

private fun DrawScope.drawRealisticWorld(
    state: PlayState,
    groundY: Float,
    width: Float,
    height: Float,
    bgBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    dinoBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    if (bgBitmap != null) {
        // Draw the generated prehistoric backdrop
        drawImage(
            image = bgBitmap,
            dstOffset = androidx.compose.ui.unit.IntOffset(0, 0),
            dstSize = androidx.compose.ui.unit.IntSize(width.toInt(), groundY.toInt())
        )
    } else {
        // Fallback realistic atmospheric gradient
        val deepJungleBrush = Brush.verticalGradient(
            colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF1C1917)),
            startY = 0f,
            endY = groundY
        )
        drawRect(brush = deepJungleBrush, size = Size(width, groundY))
    }

    // Volcanic ambient ash / ember particles
    for (i in 0 until 12) {
        val emberX = ((i * 89f) - (state.groundOffset * 0.8f)) % width
        val finalX = if (emberX < 0) emberX + width else emberX
        val emberY = (i * 47f) % (groundY - 30f)
        drawCircle(
            color = Color(0xFFF97316).copy(alpha = 0.65f),
            radius = 2.5f,
            center = Offset(finalX, emberY)
        )
    }

    // Prehistoric earth & volcanic rock ground
    val darkEarthBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF292524), Color(0xFF1C1917), Color(0xFF0C0A09)),
        startY = groundY,
        endY = height
    )
    drawRect(brush = darkEarthBrush, topLeft = Offset(0f, groundY), size = Size(width, height - groundY))

    // Cracked magma border line
    drawLine(
        brush = Brush.horizontalGradient(
            listOf(Color(0xFFDC2626), Color(0xFFF59E0B), Color(0xFFEA580C), Color(0xFFDC2626))
        ),
        start = Offset(0f, groundY),
        end = Offset(width, groundY),
        strokeWidth = 4f
    )

    // Realistic silhouette ferns along ground
    var fernX = -(state.groundOffset % 120f)
    while (fernX < width) {
        drawFernLeaf(fernX, groundY)
        fernX += 70f
    }
}

private fun DrawScope.drawFernLeaf(x: Float, groundY: Float) {
    val leafColor = Color(0xFF15803D).copy(alpha = 0.55f)
    val path = Path().apply {
        moveTo(x, groundY)
        quadraticTo(x + 8f, groundY - 18f, x + 16f, groundY - 24f)
        quadraticTo(x + 10f, groundY - 14f, x + 14f, groundY)
        close()
    }
    drawPath(path, leafColor)
}

private fun DrawScope.drawDino(
    mode: GameMode,
    skin: DinoSkin,
    x: Float,
    y: Float, // groundY - dinoY (top left of dino base)
    isDucking: Boolean,
    runFrame: Int,
    inAir: Boolean,
    hasShield: Boolean,
    hasMagnet: Boolean,
    realisticBitmap: androidx.compose.ui.graphics.ImageBitmap?
) {
    val dinoColor = when (mode) {
        GameMode.CLASSIC -> Color(0xFF535353)
        GameMode.COLOR -> skin.bodyColor
        GameMode.REALISTIC -> skin.bodyColor
    }
    val eyeColor = when (mode) {
        GameMode.CLASSIC -> Color.White
        GameMode.COLOR -> skin.eyeColor
        GameMode.REALISTIC -> Color(0xFFFBBF24)
    }

    if (mode == GameMode.REALISTIC && realisticBitmap != null) {
        // Photorealistic T-Rex Sprite
        val dinoW = if (isDucking) 68 else 60
        val dinoH = if (isDucking) 40 else 58
        val topPos = (y - dinoH).toInt()

        drawImage(
            image = realisticBitmap,
            dstOffset = androidx.compose.ui.unit.IntOffset(x.toInt(), topPos),
            dstSize = androidx.compose.ui.unit.IntSize(dinoW, dinoH)
        )
    } else {
        // Procedural crisp vector Dino with leg animation
        if (isDucking) {
            // Ducking Dino posture (elongated horizontal body, lowered head)
            val headY = y - 28f
            // Body
            drawRoundRect(
                color = dinoColor,
                topLeft = Offset(x, headY + 6f),
                size = Size(54f, 20f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Head extended forward
            drawRoundRect(
                color = dinoColor,
                topLeft = Offset(x + 36f, headY),
                size = Size(24f, 16f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Eye
            drawRect(eyeColor, Offset(x + 48f, headY + 4f), Size(4f, 4f))
            // Jaw / open mouth
            drawRect(Color.DarkGray, Offset(x + 50f, headY + 12f), Size(10f, 3f))

            // Ducking legs
            val leg1X = if (runFrame == 0) x + 12f else x + 24f
            val leg2X = if (runFrame == 0) x + 28f else x + 16f
            drawLine(dinoColor, Offset(leg1X, headY + 26f), Offset(leg1X + 4f, y), 4f)
            drawLine(dinoColor, Offset(leg2X, headY + 26f), Offset(leg2X + 4f, y), 4f)
        } else {
            // Standing / Running / Jumping Dino
            val dinoH = 54f
            val topY = y - dinoH

            // Tail
            val tailPath = Path().apply {
                moveTo(x, topY + 36f)
                lineTo(x - 14f, topY + 30f)
                lineTo(x, topY + 24f)
                close()
            }
            drawPath(tailPath, dinoColor)

            // Main Body
            drawRoundRect(
                color = dinoColor,
                topLeft = Offset(x, topY + 18f),
                size = Size(32f, 26f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Neck & Head
            drawRoundRect(
                color = dinoColor,
                topLeft = Offset(x + 14f, topY),
                size = Size(28f, 22f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Snout / Teeth
            drawRect(dinoColor, Offset(x + 34f, topY + 6f), Size(10f, 12f))
            drawRect(Color.White, Offset(x + 36f, topY + 15f), Size(6f, 2f)) // Teeth

            // Eye
            drawRect(eyeColor, Offset(x + 28f, topY + 4f), Size(5f, 5f))
            drawRect(Color.Black, Offset(x + 30f, topY + 5f), Size(2f, 2f))

            // Short Arms
            drawRoundRect(
                color = dinoColor,
                topLeft = Offset(x + 28f, topY + 24f),
                size = Size(10f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Legs Animation
            if (inAir) {
                // Tucked jump legs
                drawLine(dinoColor, Offset(x + 10f, topY + 44f), Offset(x + 16f, topY + 50f), 4f)
                drawLine(dinoColor, Offset(x + 20f, topY + 44f), Offset(x + 26f, topY + 50f), 4f)
            } else {
                if (runFrame == 0) {
                    drawLine(dinoColor, Offset(x + 10f, topY + 44f), Offset(x + 6f, y), 4f)
                    drawLine(dinoColor, Offset(x + 22f, topY + 44f), Offset(x + 26f, y - 4f), 4f)
                } else {
                    drawLine(dinoColor, Offset(x + 10f, topY + 44f), Offset(x + 12f, y - 4f), 4f)
                    drawLine(dinoColor, Offset(x + 22f, topY + 44f), Offset(x + 20f, y), 4f)
                }
            }
        }
    }

    // Render Equipped Hat / Accessory
    val hatBaseX = x + 20f
    val hatBaseY = y - (if (isDucking) 28f else 54f)
    when (skin.hatType) {
        SkinHat.CROWN -> {
            val crownPath = Path().apply {
                moveTo(hatBaseX - 4f, hatBaseY)
                lineTo(hatBaseX - 6f, hatBaseY - 12f)
                lineTo(hatBaseX + 2f, hatBaseY - 6f)
                lineTo(hatBaseX + 10f, hatBaseY - 14f)
                lineTo(hatBaseX + 18f, hatBaseY - 6f)
                lineTo(hatBaseX + 26f, hatBaseY - 12f)
                lineTo(hatBaseX + 24f, hatBaseY)
                close()
            }
            drawPath(crownPath, Color(0xFFFBBF24))
            drawCircle(Color(0xFFEF4444), 2f, Offset(hatBaseX + 10f, hatBaseY - 14f))
        }
        SkinHat.SUNGLASSES -> {
            drawRoundRect(
                Color.Black,
                Offset(hatBaseX + 6f, hatBaseY + 6f),
                Size(16f, 7f),
                CornerRadius(2f, 2f)
            )
            drawLine(Color.Black, Offset(hatBaseX + 2f, hatBaseY + 8f), Offset(hatBaseX + 6f, hatBaseY + 8f), 2f)
        }
        SkinHat.ASTRONAUT_HELMET -> {
            drawCircle(
                color = Color(0x6638BDF8),
                radius = 18f,
                center = Offset(hatBaseX + 10f, hatBaseY + 10f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = 18f,
                center = Offset(hatBaseX + 10f, hatBaseY + 10f),
                style = Stroke(3f)
            )
        }
        SkinHat.FIRE_SPIKES -> {
            for (i in 0 until 4) {
                val spikeX = x + (i * 8f)
                val spikePath = Path().apply {
                    moveTo(spikeX, hatBaseY + 16f)
                    lineTo(spikeX + 4f, hatBaseY + 6f)
                    lineTo(spikeX + 8f, hatBaseY + 16f)
                    close()
                }
                drawPath(spikePath, Color(0xFFEF4444))
            }
        }
        SkinHat.BANDANA -> {
            drawRoundRect(
                Color(0xFFDC2626),
                Offset(hatBaseX - 2f, hatBaseY + 2f),
                Size(24f, 5f),
                CornerRadius(2f, 2f)
            )
        }
        SkinHat.NONE -> {}
    }

    // Active Shield Bubble
    if (hasShield) {
        val shieldCenter = Offset(x + 22f, y - (if (isDucking) 16f else 28f))
        drawCircle(
            color = Color(0x3338BDF8),
            radius = 34f,
            center = shieldCenter
        )
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 34f,
            center = shieldCenter,
            style = Stroke(3f)
        )
    }

    // Active Magnet Aura
    if (hasMagnet) {
        drawCircle(
            color = Color(0x22F43F5E),
            radius = 42f,
            center = Offset(x + 22f, y - 28f),
            style = Stroke(2f)
        )
    }
}

private fun DrawScope.drawObstacle(obs: Obstacle, groundY: Float, mode: GameMode) {
    val obsY = groundY - obs.y - obs.height

    when (obs.type) {
        ObstacleType.CACTUS_SMALL -> {
            val cactusColor = if (mode == GameMode.CLASSIC) Color(0xFF535353) else Color(0xFF15803D)
            // Main stem
            drawRoundRect(
                color = cactusColor,
                topLeft = Offset(obs.x + 8f, obsY),
                size = Size(8f, obs.height),
                cornerRadius = CornerRadius(3f, 3f)
            )
            // Left arm
            drawRoundRect(
                color = cactusColor,
                topLeft = Offset(obs.x, obsY + 12f),
                size = Size(8f, 16f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Right arm
            drawRoundRect(
                color = cactusColor,
                topLeft = Offset(obs.x + 14f, obsY + 8f),
                size = Size(8f, 16f),
                cornerRadius = CornerRadius(2f, 2f)
            )
            if (mode == GameMode.COLOR) {
                // Little desert flower on top
                drawCircle(Color(0xFFF43F5E), 3f, Offset(obs.x + 12f, obsY - 2f))
            }
        }
        ObstacleType.CACTUS_LARGE -> {
            val cactusColor = if (mode == GameMode.CLASSIC) Color(0xFF535353) else Color(0xFF166534)
            // Sturdy stem
            drawRoundRect(
                color = cactusColor,
                topLeft = Offset(obs.x + 10f, obsY),
                size = Size(12f, obs.height),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Arms
            drawRoundRect(cactusColor, Offset(obs.x, obsY + 16f), Size(10f, 20f), CornerRadius(2f, 2f))
            drawRoundRect(cactusColor, Offset(obs.x + 20f, obsY + 12f), Size(10f, 22f), CornerRadius(2f, 2f))

            if (mode == GameMode.COLOR) {
                drawCircle(Color(0xFFF59E0B), 3.5f, Offset(obs.x + 16f, obsY - 2f))
            }
        }
        ObstacleType.CACTUS_DOUBLE -> {
            val cactusColor = if (mode == GameMode.CLASSIC) Color(0xFF535353) else Color(0xFF047857)
            // Stem 1
            drawRoundRect(cactusColor, Offset(obs.x + 6f, obsY + 6f), Size(10f, obs.height - 6f), CornerRadius(3f, 3f))
            // Stem 2
            drawRoundRect(cactusColor, Offset(obs.x + 28f, obsY), Size(12f, obs.height), CornerRadius(4f, 4f))
            drawRoundRect(cactusColor, Offset(obs.x + 38f, obsY + 14f), Size(8f, 18f), CornerRadius(2f, 2f))
        }
        ObstacleType.PTERODACTYL_LOW, ObstacleType.PTERODACTYL_HIGH -> {
            val pteroColor = if (mode == GameMode.CLASSIC) Color(0xFF535353) else Color(0xFF7C3AED)
            // Body
            drawRoundRect(
                pteroColor,
                Offset(obs.x + 10f, obsY + 12f),
                Size(22f, 10f),
                CornerRadius(3f, 3f)
            )
            // Sharp Beak & Head
            val beakPath = Path().apply {
                moveTo(obs.x + 8f, obsY + 14f)
                lineTo(obs.x - 6f, obsY + 17f)
                lineTo(obs.x + 8f, obsY + 20f)
                close()
            }
            drawPath(beakPath, pteroColor)

            // Flapping Wings
            val wingPath = Path().apply {
                if (obs.wingState) {
                    // Wings UP
                    moveTo(obs.x + 14f, obsY + 14f)
                    lineTo(obs.x + 24f, obsY - 8f)
                    lineTo(obs.x + 32f, obsY + 12f)
                } else {
                    // Wings DOWN
                    moveTo(obs.x + 14f, obsY + 14f)
                    lineTo(obs.x + 24f, obsY + 28f)
                    lineTo(obs.x + 32f, obsY + 12f)
                }
                close()
            }
            drawPath(wingPath, pteroColor)
            // Eye
            drawRect(Color.White, Offset(obs.x + 4f, obsY + 14f), Size(3f, 3f))
        }
        ObstacleType.VOLCANIC_BOULDER -> {
            // Realistic jagged volcanic rock
            val rockPath = Path().apply {
                moveTo(obs.x + 4f, groundY)
                lineTo(obs.x, obsY + 18f)
                lineTo(obs.x + 14f, obsY)
                lineTo(obs.x + 32f, obsY + 6f)
                lineTo(obs.x + obs.width, obsY + 24f)
                lineTo(obs.x + obs.width - 4f, groundY)
                close()
            }
            drawPath(rockPath, Color(0xFF262626))
            // Magma vein
            drawLine(Color(0xFFEF4444), Offset(obs.x + 10f, obsY + 10f), Offset(obs.x + 24f, obsY + 28f), 2.5f)
        }
        ObstacleType.PREHISTORIC_SPIRE -> {
            // Obsidian Spire
            val spirePath = Path().apply {
                moveTo(obs.x + 4f, groundY)
                lineTo(obs.x + obs.width / 2f, obsY)
                lineTo(obs.x + obs.width - 4f, groundY)
                close()
            }
            drawPath(spirePath, Color(0xFF1E293B))
            drawLine(
                brush = Brush.verticalGradient(listOf(Color(0xFFE2E8F0), Color(0xFF38BDF8))),
                start = Offset(obs.x + obs.width / 2f, obsY),
                end = Offset(obs.x + obs.width / 2f, groundY),
                strokeWidth = 2f
            )
        }
    }
}

private fun DrawScope.drawCollectible(col: Collectible, groundY: Float, mode: GameMode) {
    val cy = groundY - col.y - col.size / 2f
    val cx = col.x + col.size / 2f

    when (col.type) {
        CollectibleType.COIN -> {
            val coinColor = if (mode == GameMode.CLASSIC) Color(0xFF757575) else Color(0xFFF59E0B)
            drawCircle(coinColor, col.size / 2f, Offset(cx, cy))
            drawCircle(Color(0xFFFEF3C7), col.size / 3f, Offset(cx, cy))
            drawCircle(coinColor, col.size / 5f, Offset(cx, cy))
        }
        CollectibleType.FOSSIL_AMBER -> {
            val amberColor = Color(0xFFD97706)
            drawCircle(amberColor, col.size / 2f + 2f, Offset(cx, cy))
            drawCircle(Color(0xFFFDE68A), col.size / 2f - 2f, Offset(cx, cy))
            // Inner bone silhouette
            drawRect(Color(0xFF78350F), Offset(cx - 3f, cy - 3f), Size(6f, 6f))
        }
        CollectibleType.POWER_SHIELD -> {
            drawCircle(Color(0xFF38BDF8), col.size / 2f, Offset(cx, cy))
            drawCircle(Color.White, col.size / 3f, Offset(cx, cy), style = Stroke(2f))
        }
        CollectibleType.POWER_MAGNET -> {
            drawCircle(Color(0xFFEF4444), col.size / 2f, Offset(cx, cy))
            drawCircle(Color(0xFFFEE2E2), col.size / 3.5f, Offset(cx, cy))
        }
        CollectibleType.POWER_SLOW_MO -> {
            drawCircle(Color(0xFF8B5CF6), col.size / 2f, Offset(cx, cy))
            drawCircle(Color.White, col.size / 3.5f, Offset(cx, cy), style = Stroke(2f))
        }
    }
}
