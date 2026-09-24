package com.example.model

import androidx.compose.ui.graphics.Color

enum class GameMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val accentColor: Color,
    val description: String
) {
    CLASSIC(
        id = "CLASSIC",
        title = "Clásico Pixel",
        subtitle = "Monocromo Original",
        primaryColor = Color(0xFF535353),
        accentColor = Color(0xFF222222),
        description = "La experiencia retro auténtica estilo Google Dino con gráficos de píxeles en blanco y negro."
    ),
    COLOR(
        id = "COLOR",
        title = "Modo a Color",
        subtitle = "Vibrante & Neón",
        primaryColor = Color(0xFF10B981),
        accentColor = Color(0xFF3B82F6),
        description = "Paleta moderna y colorida con cielos degradados, día y noche, cactus tropicales y monedas doradas brillantes."
    ),
    REALISTIC(
        id = "REALISTIC",
        title = "Modo Realista",
        subtitle = "Jurásico 4K",
        primaryColor = Color(0xFFE11D48),
        accentColor = Color(0xFFF59E0B),
        description = "Texturas prehistóricas, fondo jurásico cinematográfico, T-Rex detallado y partículas atmosféricas de ceniza volcánica."
    );

    companion object {
        fun fromId(id: String): GameMode {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: COLOR
        }
    }
}
