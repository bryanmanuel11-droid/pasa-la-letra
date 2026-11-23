package com.bryan.pasaletra

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit

/** Paleta del diseño "fichas de juego de mesa". */
object C {
    val Cream = Color(0xFFF6EFE3)
    val Paper = Color(0xFFFFFBF4)
    val Ink = Color(0xFF1B1A20)
    val Ink2 = Color(0xFF3A3742)
    val Ink3 = Color(0xFF4F4B57)
    val Muted = Color(0xFF6B6459)
    val Body = Color(0xFF4A443C)
    val Line = Color(0xFFE3D9C8)
    val Line2 = Color(0xFFC9BDA8)
    val Dim = Color(0xFFA79F92)
    val Dim2 = Color(0xFFCFC6B8)
    val Red = Color(0xFFD93A22)
    val Yellow = Color(0xFFFFC93C)
    val Green = Color(0xFF1F7A4F)
    val White = Color(0xFFFFFFFF)
}

class AppFonts(val display: FontFamily, val body: FontFamily)

val LocalFonts = staticCompositionLocalOf { AppFonts(FontFamily.SansSerif, FontFamily.SansSerif) }

/**
 * Carga Bricolage Grotesque (títulos) y DM Sans (texto) desde assets/fonts.
 * GitHub las descarga al compilar; si no están, se usa la letra del sistema.
 */
@OptIn(ExperimentalTextApi::class)
fun loadFonts(context: Context): AppFonts {
    val files = try {
        context.assets.list("fonts")?.toSet() ?: emptySet()
    } catch (e: Exception) {
        emptySet()
    }
    val display = if ("bricolage.ttf" in files) {
        try {
            FontFamily(
                Font("fonts/bricolage.ttf", context.assets, FontWeight.Medium),
                Font("fonts/bricolage.ttf", context.assets, FontWeight.Bold),
                Font("fonts/bricolage.ttf", context.assets, FontWeight.ExtraBold),
            )
        } catch (e: Exception) {
            FontFamily.SansSerif
        }
    } else FontFamily.SansSerif
    val body = if ("dmsans.ttf" in files) {
        try {
            FontFamily(
                Font("fonts/dmsans.ttf", context.assets, FontWeight.Normal),
                Font("fonts/dmsans.ttf", context.assets, FontWeight.Medium),
                Font("fonts/dmsans.ttf", context.assets, FontWeight.Bold),
            )
        } catch (e: Exception) {
            FontFamily.SansSerif
        }
    } else FontFamily.SansSerif
    return AppFonts(display, body)
}

/** Texto con las fuentes de la app. display = true usa la de títulos. */
@Composable
fun T(
    text: String,
    size: TextUnit,
    color: Color = C.Ink,
    weight: FontWeight = FontWeight.Normal,
    display: Boolean = false,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    strike: Boolean = false,
    underline: Boolean = false,
) {
    val f = LocalFonts.current
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = if (display) f.display else f.body,
        textAlign = align ?: TextAlign.Unspecified,
        lineHeight = lineHeight,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        textDecoration = when {
            strike -> TextDecoration.LineThrough
            underline -> TextDecoration.Underline
            else -> null
        },
    )
}

@Composable
fun TA(
    text: AnnotatedString,
    size: TextUnit,
    color: Color = C.Ink,
    weight: FontWeight = FontWeight.Normal,
    display: Boolean = false,
    modifier: Modifier = Modifier,
    align: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
) {
    val f = LocalFonts.current
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = size,
        fontWeight = weight,
        fontFamily = if (display) f.display else f.body,
        textAlign = align ?: TextAlign.Unspecified,
        lineHeight = lineHeight,
    )
}
