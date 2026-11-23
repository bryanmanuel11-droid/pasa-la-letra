package com.bryan.pasaletra

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

enum class Mode { ELIMINACION, LIGA }

enum class Screen { INICIO, COMO, JUGADORES, MODO, SORTEO, TURNO, VALIDAR, FALLO, PAUSA, RESULTADOS }

enum class FailReason { TIEMPO, PASO, NO_VALE, REPETIDA }

/** Colores de los jugadores, en orden de asignación. Todos con buen contraste para texto blanco. */
val PLAYER_COLORS = listOf(
    0xFFD93A22, 0xFF2F4BD8, 0xFF1F7A4F, 0xFF6A3FA0,
    0xFFB5560B, 0xFF0E7C86, 0xFF9C2C6B, 0xFF3D3A45,
)

/** Letras posibles para el sorteo inicial (se dejan fuera las difíciles para empezar). */
const val SORTEO_LETTERS = "ABCDEFGHIJLMNOPRSTUV"

data class Player(val id: Int, val name: String, val colorIndex: Int) {
    val color: Color get() = Color(PLAYER_COLORS[colorIndex % PLAYER_COLORS.size])
    val initial: String get() = name.take(1).uppercase()
}

class PlayerState(val player: Player, startLives: Int) {
    var lives by mutableIntStateOf(startLives)
    var points by mutableIntStateOf(0)
    var words by mutableIntStateOf(0)

    /** Turno en el que quedó eliminado; 0 si sigue en juego. */
    var eliminatedAtTurn by mutableIntStateOf(0)
    val eliminated: Boolean get() = eliminatedAtTurn > 0
}

/** Palabra escuchada, a la espera de que el grupo la valide. */
data class Pending(
    val word: String,
    val display: String,
    val startsOk: Boolean,
    val repeated: Boolean,
    val seconds: Float,
)

/** Palabra aceptada en la cadena. */
data class ChainItem(val display: String, val word: String, val player: Int, val seconds: Float)

object Words {
    /** Primera palabra, en minúsculas, sin tildes ni signos. La ñ se conserva. */
    fun normalize(raw: String): String {
        val first = raw.trim().split(Regex("\\s+")).firstOrNull() ?: ""
        val sb = StringBuilder()
        for (ch in first.lowercase()) {
            val c = when (ch) {
                'á', 'à', 'ä', 'â' -> 'a'
                'é', 'è', 'ë', 'ê' -> 'e'
                'í', 'ì', 'ï', 'î' -> 'i'
                'ó', 'ò', 'ö', 'ô' -> 'o'
                'ú', 'ù', 'ü', 'û' -> 'u'
                else -> ch
            }
            if (c in 'a'..'z' || c == 'ñ') sb.append(c)
        }
        return sb.toString()
    }

    /** Letra que se pasa: la última, salvo X o W, que pasan la anterior. */
    fun lastLetter(word: String): Char {
        val w = normalize(word)
        for (i in w.indices.reversed()) {
            if (w[i] != 'x' && w[i] != 'w') return w[i]
        }
        return w.lastOrNull() ?: 'a'
    }

    /** Lo que se muestra en pantalla: la primera palabra en mayúsculas, con sus tildes. */
    fun display(raw: String): String {
        val first = raw.trim().split(Regex("\\s+")).firstOrNull() ?: ""
        return first.filter { it.isLetter() }.uppercase()
    }
}

fun Char.up(): String = this.uppercaseChar().toString()
