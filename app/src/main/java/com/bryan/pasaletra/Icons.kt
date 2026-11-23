package com.bryan.pasaletra

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Iconos de trazo, los mismos del diseño. */
object Ic {
    private fun line(name: String, width: Float, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        for (d in paths) {
            b.addPath(
                pathData = addPathNodes(d),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = width,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
        return b.build()
    }

    private fun solid(name: String, vararg paths: String): ImageVector {
        val b = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        for (d in paths) b.addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black))
        return b.build()
    }

    val Back = line("back", 2.2f, "M15,5L8,12L15,19")
    val Plus = line("plus", 2.5f, "M12,5V19M5,12H19")
    val Close = line("close", 2.2f, "M6,6L18,18M18,6L6,18")
    val Check = line("check", 3f, "M5,12.5L9.5,17L19,7")
    val Pause = line("pause", 2.6f, "M9,5V19M15,5V19")
    val Mic = line(
        "mic", 2.2f,
        "M9,6a3,3 0 0 1 6,0v5a3,3 0 0 1 -6,0z",
        "M5,11a7,7 0 0 0 14,0",
        "M12,18v3",
    )
    val Shuffle = line(
        "shuffle", 2f,
        "M4,7h3l10,10h3", "M4,17h3l3,-3", "M14,10l3,-3h3", "M17,4l3,3l-3,3", "M17,14l3,3l-3,3",
    )
    val Clock = line("clock", 1.8f, "M4,13a8,8 0 1 0 16,0a8,8 0 1 0 -16,0", "M12,9v4l2.5,1.5", "M9.5,2.5h5")
    val Trophy = line(
        "trophy", 2.2f,
        "M8,4h8v5a4,4 0 0 1 -8,0z", "M8,6H5a3,3 0 0 0 3,4", "M16,6h3a3,3 0 0 1 -3,4", "M12,13v4", "M8,20h8", "M9,17h6",
    )
    val Keyboard = line(
        "keyboard", 2f,
        "M4,6h16a1,1 0 0 1 1,1v10a1,1 0 0 1 -1,1h-16a1,1 0 0 1 -1,-1v-10a1,1 0 0 1 1,-1z",
        "M7,10h.01M11,10h.01M15,10h.01M7,14h10",
    )
    val Up = line("up", 2.2f, "M6,15l6,-6l6,6")
    val Down = line("down", 2.2f, "M6,9l6,6l6,-6")
    val Play = solid("play", "M7,4.5v15l12,-7.5z")
    val Heart = solid("heart", "M12,21c0,0 -8,-5 -8,-11a4.5,4.5 0 0 1 8,-2.8a4.5,4.5 0 0 1 8,2.8c0,6 -8,11 -8,11z")
    val HeartOff = line("heartOff", 2.5f, "M12,21c0,0 -8,-5 -8,-11a4.5,4.5 0 0 1 8,-2.8a4.5,4.5 0 0 1 8,2.8c0,6 -8,11 -8,11z")
}
