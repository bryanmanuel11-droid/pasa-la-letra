package com.bryan.pasaletra

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import kotlin.math.ceil

class GameViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("pasaletra", Context.MODE_PRIVATE)
    private val tone: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 70)
    } catch (e: Exception) {
        null
    }
    private val vibrator: Vibrator? = try {
        app.getSystemService(Vibrator::class.java)
    } catch (e: Exception) {
        null
    }

    // ---------- Navegación ----------
    var screen by mutableStateOf(Screen.INICIO)
        private set

    // ---------- Jugadores y ajustes (se guardan en el teléfono) ----------
    val players = mutableStateListOf<Player>()
    private var nextId = 1

    var mode by mutableStateOf(Mode.ELIMINACION)
    var lives by mutableIntStateOf(2)
    var rounds by mutableIntStateOf(10)
    var seconds by mutableIntStateOf(15)
    var sound by mutableStateOf(true)
    var vibration by mutableStateOf(true)
    var shuffle by mutableStateOf(true)

    // ---------- Partida ----------
    val table = mutableStateListOf<PlayerState>()
    var current by mutableIntStateOf(0)
        private set
    private var starter = 0
    var letter by mutableStateOf('m')
        private set
    var turn by mutableIntStateOf(1)
        private set
    var round by mutableIntStateOf(1)
        private set
    var turnId by mutableIntStateOf(0)
        private set
    var drawId by mutableIntStateOf(0)
        private set
    var timeLeft by mutableFloatStateOf(15f)
        private set
    val chain = mutableStateListOf<ChainItem>()
    private val used = HashSet<String>()
    var pending by mutableStateOf<Pending?>(null)
        private set
    var lastFail by mutableStateOf(FailReason.TIEMPO)
        private set
    var failPlayer by mutableIntStateOf(0)
        private set
    var failEliminated by mutableStateOf(false)
        private set
    var gameOver by mutableStateOf(false)
        private set
    var longest by mutableStateOf<ChainItem?>(null)
        private set
    var fastest by mutableStateOf<ChainItem?>(null)
        private set
    private var lastBeepSecond = -1

    init {
        loadSaved()
    }

    // ---------- Navegación ----------
    fun go(s: Screen) {
        if (s == Screen.MODO || s == Screen.INICIO) saveSettings()
        screen = s
    }

    fun back() {
        screen = when (screen) {
            Screen.COMO, Screen.JUGADORES, Screen.RESULTADOS -> Screen.INICIO
            Screen.MODO -> Screen.JUGADORES
            Screen.SORTEO -> Screen.MODO
            Screen.TURNO -> Screen.PAUSA
            Screen.PAUSA -> Screen.TURNO
            Screen.VALIDAR -> Screen.VALIDAR
            Screen.FALLO -> Screen.FALLO
            Screen.INICIO -> Screen.INICIO
        }
    }

    // ---------- Jugadores ----------
    fun addPlayer(raw: String): Boolean {
        val name = raw.replace("|", "").replace("\n", " ").trim().replace(Regex("\\s+"), " ")
        if (name.isEmpty() || players.size >= 8) return false
        val usedColors = players.map { it.colorIndex }.toSet()
        val color = PLAYER_COLORS.indices.firstOrNull { it !in usedColors } ?: 0
        players.add(Player(nextId++, name.replaceFirstChar { it.uppercaseChar() }, color))
        savePlayers()
        return true
    }

    fun removePlayer(id: Int) {
        players.removeAll { it.id == id }
        savePlayers()
    }

    fun movePlayer(id: Int, delta: Int) {
        val i = players.indexOfFirst { it.id == id }
        val j = i + delta
        if (i < 0 || j < 0 || j >= players.size) return
        val p = players.removeAt(i)
        players.add(j, p)
        savePlayers()
    }

    // ---------- Partida ----------
    fun startGame() {
        if (players.size < 2) return
        saveSettings()
        val order = if (shuffle) players.shuffled() else players.toList()
        table.clear()
        order.forEach { table.add(PlayerState(it, lives)) }
        chain.clear()
        used.clear()
        pending = null
        longest = null
        fastest = null
        turn = 1
        round = 1
        gameOver = false
        draw()
        screen = Screen.SORTEO
    }

    /** Sortea letra y jugador inicial. La pantalla de sorteo anima hasta este resultado. */
    fun draw() {
        letter = SORTEO_LETTERS.random().lowercaseChar()
        current = table.indices.random()
        starter = current
        drawId += 1
    }

    fun landed() {
        beep(ToneGenerator.TONE_PROP_ACK, 120)
        buzz(60)
    }

    fun beginTurn() {
        timeLeft = seconds.toFloat()
        lastBeepSecond = -1
        turnId += 1
        pending = null
        screen = Screen.TURNO
    }

    fun tick(dt: Float) {
        if (screen != Screen.TURNO) return
        timeLeft = (timeLeft - dt).coerceAtLeast(0f)
        val s = ceil(timeLeft).toInt()
        if (s in 1..3 && s != lastBeepSecond) {
            lastBeepSecond = s
            beep(ToneGenerator.TONE_PROP_BEEP, 100)
            buzz(40)
        }
        if (timeLeft <= 0f) fail(FailReason.TIEMPO)
    }

    fun pause() {
        if (screen == Screen.TURNO) screen = Screen.PAUSA
    }

    fun resume() {
        screen = Screen.TURNO
    }

    /** Recibe lo que oyó el micrófono (varias opciones) o lo que se escribió. */
    fun submit(candidates: List<String>) {
        if (screen != Screen.TURNO) return
        val options = candidates.map { it.trim() }.filter { Words.normalize(it).isNotEmpty() }
        if (options.isEmpty()) return
        val best = options.firstOrNull {
            val w = Words.normalize(it)
            w.first() == letter && w !in used
        } ?: options.firstOrNull { Words.normalize(it).first() == letter } ?: options.first()
        val word = Words.normalize(best)
        val shown = Words.display(best)
        pending = Pending(
            word = word,
            display = if (shown.isEmpty()) word.uppercase() else shown,
            startsOk = word.first() == letter,
            repeated = word in used,
            seconds = seconds - timeLeft,
        )
        screen = Screen.VALIDAR
    }

    fun retry() {
        pending = null
        screen = Screen.TURNO
    }

    fun accept() {
        val p = pending ?: return
        val ps = table[current]
        used.add(p.word)
        ps.words += 1
        if (mode == Mode.LIGA) ps.points += 1
        val item = ChainItem(p.display, p.word, current, p.seconds)
        chain.add(item)
        val l = longest
        if (l == null || p.word.length > l.word.length) longest = item
        val f = fastest
        if (f == null || p.seconds < f.seconds) fastest = item
        letter = Words.lastLetter(p.word)
        pending = null
        beep(ToneGenerator.TONE_PROP_ACK, 150)
        advance()
        if (gameOver) screen = Screen.RESULTADOS else beginTurn()
    }

    fun fail(reason: FailReason) {
        val ps = table[current]
        lastFail = reason
        failPlayer = current
        failEliminated = false
        if (mode == Mode.ELIMINACION) {
            ps.lives -= 1
            if (ps.lives <= 0) {
                ps.eliminatedAtTurn = turn
                failEliminated = true
            }
            if (activeCount() <= 1) gameOver = true
        }
        pending = null
        beep(ToneGenerator.TONE_PROP_NACK, 300)
        buzz(250)
        screen = Screen.FALLO
    }

    /** Tras un fallo la letra no cambia: el siguiente jugador intenta con la misma. */
    fun continueAfterFail() {
        if (gameOver) {
            screen = Screen.RESULTADOS
            return
        }
        advance()
        if (gameOver) screen = Screen.RESULTADOS else beginTurn()
    }

    private fun advance() {
        turn += 1
        val next = nextActive(current)
        if (mode == Mode.LIGA && next == starter) {
            if (round >= rounds) gameOver = true else round += 1
        }
        current = next
    }

    private fun nextActive(from: Int): Int {
        val n = table.size
        if (n == 0) return 0
        var i = from
        repeat(n) {
            i = (i + 1) % n
            if (!table[i].eliminated) return i
        }
        return from
    }

    fun peekNext(): Int = nextActive(current)

    fun activeCount(): Int = table.count { !it.eliminated }

    fun ranking(): List<PlayerState> =
        if (mode == Mode.LIGA) {
            table.sortedWith(compareByDescending<PlayerState> { it.points }.thenByDescending { it.words })
        } else {
            table.sortedWith(
                compareByDescending<PlayerState> { if (it.eliminated) it.eliminatedAtTurn else Int.MAX_VALUE }
                    .thenByDescending { it.words }
            )
        }

    fun isTie(): Boolean {
        if (mode != Mode.LIGA) return false
        val r = ranking()
        return r.size > 1 && r[0].points == r[1].points
    }

    // ---------- Sonido y vibración ----------
    private fun beep(type: Int, ms: Int) {
        if (!sound) return
        try {
            tone?.startTone(type, ms)
        } catch (e: Exception) {
        }
    }

    private fun buzz(ms: Long) {
        if (!vibration) return
        try {
            vibrator?.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (e: Exception) {
        }
    }

    override fun onCleared() {
        tone?.release()
        super.onCleared()
    }

    // ---------- Guardado ----------
    private fun savePlayers() {
        val text = players.joinToString("\n") { "${it.id}|${it.name}|${it.colorIndex}" }
        prefs.edit().putString("players", text).apply()
    }

    fun saveSettings() {
        prefs.edit()
            .putString("mode", if (mode == Mode.LIGA) "L" else "E")
            .putInt("lives", lives)
            .putInt("rounds", rounds)
            .putInt("seconds", seconds)
            .putBoolean("sound", sound)
            .putBoolean("vibration", vibration)
            .putBoolean("shuffle", shuffle)
            .apply()
    }

    private fun loadSaved() {
        prefs.getString("players", null)?.split("\n")?.forEach { line ->
            val parts = line.split("|")
            if (parts.size == 3) {
                val id = parts[0].toIntOrNull()
                val color = parts[2].toIntOrNull()
                if (id != null && color != null && parts[1].isNotBlank() && players.size < 8) {
                    players.add(Player(id, parts[1], color))
                    if (id >= nextId) nextId = id + 1
                }
            }
        }
        mode = if (prefs.getString("mode", "E") == "L") Mode.LIGA else Mode.ELIMINACION
        lives = prefs.getInt("lives", 2)
        rounds = prefs.getInt("rounds", 10)
        seconds = prefs.getInt("seconds", 15)
        sound = prefs.getBoolean("sound", true)
        vibration = prefs.getBoolean("vibration", true)
        shuffle = prefs.getBoolean("shuffle", true)
    }
}
