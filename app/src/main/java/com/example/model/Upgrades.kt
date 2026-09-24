package com.example.model

data class UpgradeItem(
    val id: String,
    val name: String,
    val description: String,
    val baseCost: Int,
    val costMultiplier: Float,
    val maxLevel: Int,
    val iconName: String,
    val badgeLabel: String
) {
    fun getCost(currentLevel: Int): Int {
        if (currentLevel >= maxLevel) return 0
        return (baseCost * Math.pow(costMultiplier.toDouble(), currentLevel.toDouble())).toInt()
    }

    fun isMaxLevel(currentLevel: Int): Boolean = currentLevel >= maxLevel

    fun getEffectDescription(currentLevel: Int): String {
        return when (id) {
            "double_jump" -> when (currentLevel) {
                0 -> "Bloqueado (Pulsa para saltar dos veces en el aire)"
                1 -> "Nivel 1: Doble salto desbloqueado"
                2 -> "Nivel 2: +15% de elevación en segundo salto"
                else -> "Nivel MÁX: Doble salto ágil y caída controlada"
            }
            "shield" -> when (currentLevel) {
                0 -> "Sin escudo inicial"
                1 -> "Nivel 1: Escudo inicial de 1 golpe cada partida"
                2 -> "Nivel 2: Escudo + onda de choque que destruye 1 obstáculo"
                else -> "Nivel MÁX: Escudo reforzado de doble impacto"
            }
            "magnet" -> when (currentLevel) {
                0 -> "Atracción estándar básica"
                1 -> "Nivel 1: Atrae fósiles a 150px de distancia"
                2 -> "Nivel 2: Atrae a 250px + duración +5s de imán"
                else -> "Nivel MÁX: Vórtice magnético total de toda la pantalla"
            }
            "multiplier" -> when (currentLevel) {
                0 -> "Ganancia normal (x1.0)"
                1 -> "Nivel 1: +50% monedas ganadas (x1.5)"
                2 -> "Nivel 2: Doble de monedas en cada run (x2.0)"
                else -> "Nivel MÁX: Triple de monedas acumuladas (x3.0)"
            }
            "revive" -> when (currentLevel) {
                0 -> "Sin resurrección"
                1 -> "Nivel 1: 1 resurrección automática por partida"
                else -> "Nivel MÁX: Resurrección con escudo invencible temporal"
            }
            "slow_mo" -> when (currentLevel) {
                0 -> "Velocidad estándar"
                1 -> "Nivel 1: Reflejos rápidos (-10% velocidad máxima de obstáculos)"
                else -> "Nivel MÁX: Tiempo bala en saltos críticos"
            }
            else -> description
        }
    }
}

object UpgradeCatalog {
    val items = listOf(
        UpgradeItem(
            id = "double_jump",
            name = "Doble Salto",
            description = "Salta dos veces en el aire para superar cactus dobles o pterodáctilos altos.",
            baseCost = 60,
            costMultiplier = 1.8f,
            maxLevel = 3,
            iconName = "flight",
            badgeLabel = "Movilidad"
        ),
        UpgradeItem(
            id = "shield",
            name = "Escudo Prehistórico",
            description = "Comienza cada carrera con un campo de energía que absorbe un choque fatal.",
            baseCost = 80,
            costMultiplier = 2.0f,
            maxLevel = 3,
            iconName = "shield",
            badgeLabel = "Defensa"
        ),
        UpgradeItem(
            id = "magnet",
            name = "Imán de Fósiles",
            description = "Atrae automáticamente todas las monedas y fósiles cercanos hacia el dinosaurio.",
            baseCost = 50,
            costMultiplier = 1.7f,
            maxLevel = 3,
            iconName = "filter_tilt_shift",
            badgeLabel = "Recolección"
        ),
        UpgradeItem(
            id = "multiplier",
            name = "Multiplicador de Botín",
            description = "Multiplica permanentemente todos los fósiles y monedas recogidos en el juego.",
            baseCost = 100,
            costMultiplier = 2.2f,
            maxLevel = 3,
            iconName = "monetization_on",
            badgeLabel = "Economía"
        ),
        UpgradeItem(
            id = "revive",
            name = "Segunda Oportunidad",
            description = "Resucita automáticamente al chocar con una ráfaga que limpia la pista.",
            baseCost = 150,
            costMultiplier = 2.5f,
            maxLevel = 2,
            iconName = "favorite",
            badgeLabel = "Supervivencia"
        ),
        UpgradeItem(
            id = "slow_mo",
            name = "Reflejos de Fósil",
            description = "Modera la velocidad vertiginosa para esquivar obstáculos con mayor facilidad.",
            baseCost = 75,
            costMultiplier = 1.9f,
            maxLevel = 2,
            iconName = "speed",
            badgeLabel = "Control"
        )
    )

    fun find(id: String): UpgradeItem? = items.find { it.id == id }
}
