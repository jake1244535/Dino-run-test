package com.example.game

import androidx.compose.ui.graphics.Color

enum class ObstacleType {
    CACTUS_SMALL,
    CACTUS_LARGE,
    CACTUS_DOUBLE,
    PTERODACTYL_LOW,
    PTERODACTYL_HIGH,
    VOLCANIC_BOULDER,
    PREHISTORIC_SPIRE
}

data class Obstacle(
    val id: Long,
    val x: Float,
    val y: Float, // distance above ground level (0 = on ground, 70-130 = flying)
    val width: Float,
    val height: Float,
    val type: ObstacleType,
    val wingState: Boolean = false
) {
    fun getBoundingBox(groundY: Float): RectF {
        val top = groundY - y - height
        val bottom = groundY - y
        return RectF(x, top, x + width, bottom)
    }
}

enum class CollectibleType {
    COIN,
    FOSSIL_AMBER,
    POWER_SHIELD,
    POWER_MAGNET,
    POWER_SLOW_MO
}

data class Collectible(
    val id: Long,
    val x: Float,
    val y: Float, // distance above ground level
    val size: Float,
    val type: CollectibleType,
    val collected: Boolean = false,
    val pulsePhase: Float = 0f
) {
    fun getBoundingBox(groundY: Float): RectF {
        val top = groundY - y - size
        val bottom = groundY - y
        return RectF(x, top, x + size, bottom)
    }
}

data class GameParticle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val alpha: Float,
    val color: Color,
    val size: Float,
    val life: Float // 1.0 down to 0.0
)

data class RectF(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun intersects(other: RectF): Boolean {
        return left < other.right && right > other.left && top < other.bottom && bottom > other.top
    }
}

data class CloudItem(
    val id: Long,
    val x: Float,
    val y: Float,
    val width: Float,
    val speed: Float,
    val opacity: Float = 0.8f
)

data class GroundDetail(
    val x: Float,
    val width: Float,
    val height: Float,
    val style: Int
)
