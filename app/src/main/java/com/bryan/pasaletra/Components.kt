package com.bryan.pasaletra

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Caja con la sombra dura del diseño (la "ficha"). Al pulsarla, la cara baja hacia la sombra.
 */
@Composable
fun Chunky(
    modifier: Modifier = Modifier,
    color: Color = C.Paper,
    shape: Shape = RoundedCornerShape(16.dp),
    borderColor: Color? = C.Ink,
    borderWidth: Dp = 2.dp,
    shadowColor: Color? = C.Ink,
    shadowOffset: Dp = 5.dp,
    pressed: Boolean = false,
    rotation: Float = 0f,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    val off = if (shadowColor != null) shadowOffset else 0.dp
    Box(
        modifier
            .graphicsLayer { rotationZ = rotation }
            .padding(bottom = off),
        propagateMinConstraints = true,
    ) {
        if (shadowColor != null) {
            Box(
                Modifier
                    .matchParentSize()
                    .offset(y = off)
                    .background(shadowColor, shape)
            )
        }
        var face = Modifier
            .offset(y = if (pressed) off * 0.7f else 0.dp)
            .background(color, shape)
        if (borderColor != null) face = face.border(borderWidth, borderColor, shape)
        Box(face.clip(shape), contentAlignment = contentAlignment, content = content)
    }
}

@Composable
fun Tile(
    letter: String,
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = C.Yellow,
    textColor: Color = C.Ink,
    borderColor: Color = C.Ink,
    shadowColor: Color = C.Ink,
    rotation: Float = 0f,
    border: Dp = 2.5.dp,
    shadow: Dp = 5.dp,
    fontScale: Float = 0.62f,
) {
    Chunky(
        modifier = modifier
            .width(size)
            .height(size + shadow),
        color = color,
        shape = RoundedCornerShape(size * 0.2f),
        borderColor = borderColor,
        borderWidth = border,
        shadowColor = shadowColor,
        shadowOffset = shadow,
        rotation = rotation,
    ) {
        T(letter, (size.value * fontScale).sp, textColor, FontWeight.ExtraBold, display = true)
    }
}

@Composable
fun Avatar(player: Player, size: Dp, ring: Color? = null, alpha: Float = 1f) {
    var m = Modifier
        .size(size)
        .alpha(alpha)
        .clip(CircleShape)
        .background(player.color)
    if (ring != null) m = m.border(3.dp, ring, CircleShape)
    Box(m, contentAlignment = Alignment.Center) {
        T(player.initial, (size.value * 0.46f).sp, C.White, FontWeight.ExtraBold, display = true)
    }
}

/** Botón principal con sombra de color. */
@Composable
fun BigButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = C.Ink,
    textColor: Color = C.Cream,
    shadowColor: Color = C.Red,
    borderColor: Color? = null,
    height: Dp = 64.dp,
    fontSize: TextUnit = 22.sp,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconColor: Color = textColor,
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    Chunky(
        modifier = modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.4f)
            .clickable(
                interactionSource = interaction,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            ),
        color = color,
        shape = RoundedCornerShape(16.dp),
        borderColor = borderColor,
        borderWidth = 2.5.dp,
        shadowColor = shadowColor,
        shadowOffset = 5.dp,
        pressed = pressed,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            modifier = Modifier.padding(horizontal = 12.dp),
        ) {
            if (icon != null) Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            T(text, fontSize, textColor, FontWeight.ExtraBold, display = true, maxLines = 1)
        }
    }
}

/** Botón de borde, sin relleno. */
@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = C.Ink,
    height: Dp = 56.dp,
    fontSize: TextUnit = 17.sp,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconColor: Color = color,
) {
    Box(
        modifier
            .height(height)
            .alpha(if (enabled) 1f else 0.4f)
            .clip(RoundedCornerShape(16.dp))
            .border(2.dp, color, RoundedCornerShape(16.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (icon != null) Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            T(text, fontSize, color, FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun LinkButton(text: String, onClick: () -> Unit, color: Color = C.Ink, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        T(text, 16.sp, color, FontWeight.Bold, underline = true)
    }
}

/** Botón cuadrado de icono (volver, pausa…). */
@Composable
fun IconSquare(icon: ImageVector, description: String, onClick: () -> Unit, dark: Boolean = false) {
    val fg = if (dark) C.Cream else C.Ink
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (dark) C.Ink else C.Paper)
            .border(2.dp, fg, RoundedCornerShape(12.dp))
            .clickable(role = Role.Button, onClickLabel = description, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = description, tint = fg, modifier = Modifier.size(22.dp))
    }
}

@Composable
fun TopBar(onBack: (() -> Unit)?, label: String? = null) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        if (onBack != null) IconSquare(Ic.Back, "Volver", onBack) else Box(Modifier.size(44.dp))
        if (label != null) T(label, 14.sp, C.Muted, FontWeight.Bold)
    }
}

/** Interruptor visual (el estado lo lleva quien lo usa). */
@Composable
fun SwitchKnob(on: Boolean, width: Dp = 44.dp, height: Dp = 26.dp) {
    val knob = height - 8.dp
    val x by animateDpAsState(if (on) width - knob - 4.dp else 4.dp, label = "knob")
    Box(
        Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(if (on) C.Ink else C.Line2)
    ) {
        Box(
            Modifier
                .offset(x = x, y = 4.dp)
                .size(knob)
                .clip(CircleShape)
                .background(if (on) C.Yellow else C.Paper)
        )
    }
}

/** Opción pequeña seleccionable (vidas, rondas). */
@Composable
fun Pill(label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) C.Ink else Color.Transparent)
            .border(1.5.dp, C.Ink, RoundedCornerShape(10.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        T(label, 15.sp, if (selected) C.Cream else C.Ink, FontWeight.Bold)
    }
}

@Composable
fun Card(
    modifier: Modifier = Modifier,
    dashed: Boolean = false,
    content: @Composable () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (dashed) Color.Transparent else C.Paper)
            .border(2.dp, if (dashed) C.Line2 else C.Line, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        content()
    }
}
