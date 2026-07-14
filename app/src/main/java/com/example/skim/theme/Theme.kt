package com.example.skim.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = NightMoss,
  onPrimary = Night,
  primaryContainer = NightMossContainer,
  onPrimaryContainer = NightMoss,
  secondary = NightStraw,
  onSecondary = Night,
  secondaryContainer = NightStrawContainer,
  onSecondaryContainer = NightStraw,
  tertiary = NightEvidence,
  onTertiary = Color(0xFF542410),
  tertiaryContainer = NightEvidenceContainer,
  onTertiaryContainer = NightEvidence,
  background = Night,
  onBackground = NightText,
  surface = Night,
  onSurface = NightText,
  surfaceVariant = NightSurface,
  onSurfaceVariant = NightMuted,
  outline = NightOutline,
  errorContainer = NightErrorContainer,
  onErrorContainer = Color(0xFFFFDAD4),
)

private val LightColorScheme =
  lightColorScheme(
    primary = Moss,
    onPrimary = Color.White,
    primaryContainer = MossContainer,
    onPrimaryContainer = Color(0xFF1B3925),
    secondary = Straw,
    onSecondary = Color.White,
    secondaryContainer = StrawContainer,
    onSecondaryContainer = Color(0xFF3A3500),
    tertiary = Evidence,
    onTertiary = Color.White,
    tertiaryContainer = EvidenceContainer,
    onTertiaryContainer = Color(0xFF5A1D07),
    background = Paper,
    onBackground = Graphite,
    surface = Paper,
    onSurface = Graphite,
    surfaceVariant = PaperVariant,
    onSurfaceVariant = GraphiteMuted,
    outline = Outline,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF6B1F16),
  )

@Composable
fun SkimTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme =
    if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
