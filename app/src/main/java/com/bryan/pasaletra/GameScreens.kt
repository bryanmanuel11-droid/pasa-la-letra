package com.bryan.pasaletra

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import java.util.Locale

// ======================= SORTEO =======================

@Composable
fun SorteoScreen(vm: GameViewModel) {
    val letters = SORTEO_LETTERS
    var shownLetter by remember { mutableIntStateOf(0) }
    var shownPlayer by remember { mutableIntStateOf(0) }
    var spinning by remember { mutableStateOf(true) }
    val bounce = remember { Animatable(1f) }
    val jitter by rememberInfiniteTransition(label = "jitter").animateFloat(
        initialValue = 0f, targetValue = -6f,
        animationSpec = infiniteRepeatable(tween(70), RepeatMode.Reverse), label = "j",
    )

    LaunchedEffect(vm.drawId) {
        spinning = true
        val n = vm.table.size.coerceAtLeast(1)
        for (i in 0 until 24) {
            shownLetter = (shownLetter + 1) % letters.length
            shownPlayer = (shownPlayer + 1) % n
            delay(55L + i * 6L)
        }
        shownLetter = letters.indexOf(vm.letter.uppercaseChar()).coerceAtLeast(0)
        shownPlayer = vm.current
        spinning = false
        vm.landed()
        bounce.snapTo(1.15f)
        bounce.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = 300f))
    }

    val player = vm.table.getOrNull(shownPlayer)?.player
    val cur = letters[shownLetter].toString()
    val prev = letters[(shownLetter + letters.length - 1) % letters.length].toString()
    val next = letters[(shownLetter + 1) % letters.length].toString()

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        T("SORTEO", 14.sp, C.Dim, FontWeight.Bold)
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
        ) {
            GhostTile(prev, -6f)
            Tile(
                cur, 170.dp,
                color = C.Yellow, borderColor = C.Cream, shadowColor = C.Red,
                rotation = -3f, border = 3.dp, shadow = 10.dp,
                modifier = Modifier
                    .scale(bounce.value)
                    .offset(y = if (spinning) jitter.dp else 0.dp),
            )
            GhostTile(next, 5f)
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.height(100.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (player != null) Avatar(player, 52.dp, ring = C.Cream)
                T(
                    if (spinning || player == null) "Sorteando…" else "Empieza ${player.name}",
                    34.sp, C.Cream, FontWeight.ExtraBold, display = true, maxLines = 1,
                )
            }
            if (!spinning) {
                TA(
                    buildAnnotatedString {
                        append("con una palabra que empiece por ")
                        withStyle(SpanStyle(color = C.Yellow, fontWeight = FontWeight.Bold)) { append(cur) }
                    },
                    18.sp, C.Dim, align = TextAlign.Center,
                )
            }
        }
        BigButton(
            "¡Vamos!", { vm.beginTurn() }, Modifier.fillMaxWidth(),
            color = C.Yellow, textColor = C.Ink, shadowColor = C.Red, fontSize = 24.sp, enabled = !spinning,
        )
        LinkButton("Volver a sortear", { if (!spinning) vm.draw() }, color = C.Cream)
    }
}

@Composable
private fun GhostTile(letter: String, rotation: Float) {
    Box(
        Modifier
            .size(86.dp)
            .graphicsLayer { rotationZ = rotation }
            .border(2.5.dp, C.Ink2, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center,
    ) {
        T(letter, 52.sp, C.Ink3, FontWeight.ExtraBold, display = true)
    }
}

// ======================= TURNO =======================

@Composable
fun TurnoScreen(vm: GameViewModel) {
    val context = LocalContext.current
    val ps = vm.table.getOrNull(vm.current) ?: return
    var listening by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var typing by remember { mutableStateOf(false) }
    var typed by remember { mutableStateOf("") }
    val speech = remember { SpeechHelper(context) }

    speech.onListening = { listening = it }
    speech.onResults = { message = null; vm.submit(it) }
    speech.onMessage = { message = it }

    DisposableEffect(Unit) {
        onDispose { speech.destroy() }
    }

    // Reloj del turno
    LaunchedEffect(vm.turnId) {
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            vm.tick((now - last) / 1_000_000_000f)
            last = now
        }
    }

    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            speech.start()
        } else {
            message = "Sin permiso de micrófono. Puedes escribir la palabra."
            typing = true
        }
    }

    fun onMic() {
        message = null
        if (!speech.available) {
            message = "Este teléfono no tiene reconocimiento de voz. Escribe la palabra."
            typing = true
            return
        }
        if (listening) {
            speech.stop()
            return
        }
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) speech.start() else permission.launch(Manifest.permission.RECORD_AUDIO)
    }

    fun sendTyped() {
        if (typed.isNotBlank()) {
            val t = typed
            typed = ""
            vm.submit(listOf(t))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // Barra superior
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (vm.mode == Mode.LIGA) {
                    T("Liga · Ronda ${vm.round} de ${vm.rounds}", 14.sp, weight = FontWeight.Bold)
                    RoundBar(vm.round, vm.rounds)
                } else {
                    T("Eliminación · Turno ${vm.turn}", 14.sp, weight = FontWeight.Bold)
                }
            }
            IconSquare(Ic.Pause, "Pausar partida", { vm.pause() })
        }

        PlayerStrip(vm)

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            T("Le toca a", 15.sp, C.Muted, FontWeight.Bold)
            T(ps.player.name, 38.sp, weight = FontWeight.ExtraBold, display = true, maxLines = 1)
        }

        BoxWithConstraints(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            val d = min(min(maxWidth, maxHeight), 240.dp)
            TimerRing(vm.timeLeft, vm.seconds.toFloat(), vm.letter.up(), d)
        }

        LastWordCard(vm)

        if (typing) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                val fonts = LocalFonts.current
                Box(
                    Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(C.Paper)
                        .border(2.dp, C.Ink, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    if (typed.isEmpty()) T("Palabra con ${vm.letter.up()}…", 16.sp, C.Muted)
                    BasicTextField(
                        value = typed,
                        onValueChange = { if (it.length <= 30) typed = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 17.sp, color = C.Ink, fontFamily = fonts.body),
                        cursorBrush = SolidColor(C.Red),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { sendTyped() }),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                BigButton("OK", { sendTyped() }, Modifier.width(72.dp), height = 56.dp, fontSize = 18.sp, enabled = typed.isNotBlank())
            }
        }

        if (message != null) {
            T(message ?: "", 14.sp, C.Red, FontWeight.Bold, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }

        // Botones inferiores
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            OutlineButton("Paso", { vm.fail(FailReason.PASO) }, Modifier.width(92.dp), height = 52.dp, fontSize = 16.sp)
            MicButton(listening) { onMic() }
            OutlineButton(
                if (typing) "Voz" else "Escribir",
                { typing = !typing },
                Modifier.width(92.dp), height = 52.dp, fontSize = 15.sp,
                icon = if (typing) null else Ic.Keyboard,
            )
        }
        T(
            if (listening) "Escuchando… di tu palabra" else "Toca el micrófono y di tu palabra",
            13.sp, if (listening) C.Red else C.Muted, FontWeight.Bold,
            align = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun RoundBar(round: Int, total: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        for (i in 1..total) {
            Box(
                Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        when {
                            i < round -> C.Ink
                            i == round -> C.Red
                            else -> C.Line
                        }
                    )
            )
        }
    }
}

@Composable
private fun PlayerStrip(vm: GameViewModel) {
    val many = vm.table.size > 4
    val rowModifier = if (many) Modifier.horizontalScroll(rememberScrollState()) else Modifier.fillMaxWidth()
    Row(rowModifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        vm.table.forEachIndexed { i, ps ->
            val cellModifier = if (many) Modifier.width(76.dp) else Modifier.weight(1f)
            PlayerCell(vm, ps, i == vm.current, cellModifier)
        }
    }
}

@Composable
private fun PlayerCell(vm: GameViewModel, ps: PlayerState, active: Boolean, modifier: Modifier) {
    val inner: @Composable () -> Unit = {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Avatar(ps.player, 34.dp, alpha = if (ps.eliminated) 0.35f else 1f)
            T(ps.player.name, 13.sp, if (ps.eliminated) C.Muted else C.Ink, FontWeight.Bold, maxLines = 1, strike = ps.eliminated)
            if (vm.mode == Mode.LIGA) {
                T("${ps.points} pts", 14.sp, weight = FontWeight.ExtraBold, display = true)
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    for (k in 1..vm.lives) {
                        val full = k <= ps.lives
                        Icon(
                            if (full) Ic.Heart else Ic.HeartOff,
                            contentDescription = null,
                            tint = if (full) C.Red else C.Line2,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
    if (active) {
        Chunky(modifier, color = C.Paper, shape = RoundedCornerShape(14.dp), borderWidth = 2.5.dp, shadowOffset = 4.dp) { inner() }
    } else {
        Box(modifier.padding(bottom = 4.dp)) { inner() }
    }
}

@Composable
private fun TimerRing(timeLeft: Float, total: Float, letter: String, d: Dp) {
    val frac = (timeLeft / total).coerceIn(0f, 1f)
    val urgent = timeLeft > 0f && timeLeft <= 3f
    val shake by rememberInfiniteTransition(label = "shake").animateFloat(
        initialValue = -4f, targetValue = 4f,
        animationSpec = infiniteRepeatable(tween(170), RepeatMode.Reverse), label = "s",
    )
    val secs = kotlin.math.ceil(timeLeft).toInt()
    Box(Modifier.size(d), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val sw = 12.dp.toPx()
            val tl = Offset(sw / 2, sw / 2)
            val sz = Size(size.width - sw, size.height - sw)
            drawArc(C.Line, 0f, 360f, false, topLeft = tl, size = sz, style = Stroke(sw))
            drawArc(C.Red, -90f, 360f * frac, false, topLeft = tl, size = sz, style = Stroke(sw, cap = StrokeCap.Round))
        }
        Tile(
            letter, d * 0.55f,
            color = if (urgent || timeLeft <= 0f) C.Red else C.Yellow,
            textColor = if (urgent || timeLeft <= 0f) C.White else C.Ink,
            rotation = if (urgent) shake else 0f,
            border = 3.dp, shadow = 7.dp, fontScale = 0.68f,
            modifier = Modifier.offset(y = -(d * 0.04f)),
        )
        T(
            if (timeLeft <= 0f) "¡Tiempo!" else "$secs s",
            22.sp, C.Red, FontWeight.ExtraBold, display = true,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = d * 0.06f),
        )
    }
}

@Composable
private fun LastWordCard(vm: GameViewModel) {
    val last = vm.chain.lastOrNull()
    Card(Modifier.fillMaxWidth()) {
        if (last == null) {
            T("Primera palabra de la partida", 13.sp, C.Muted, FontWeight.Bold)
            T("Empieza por ${vm.letter.up()}", 22.sp, weight = FontWeight.ExtraBold, display = true)
        } else {
            val who = vm.table.getOrNull(last.player)?.player?.name ?: ""
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    T("Última palabra · $who", 13.sp, C.Muted, FontWeight.Bold, maxLines = 1)
                    TA(
                        buildAnnotatedString {
                            val shown = last.display
                            if (shown.length > 1) append(shown.dropLast(1))
                            withStyle(SpanStyle(color = C.Red)) { append(shown.takeLast(1)) }
                        },
                        24.sp, weight = FontWeight.ExtraBold, display = true,
                    )
                }
                val prev = vm.chain.takeLast(3).dropLast(1).map { it.display.lowercase(Locale("es")) }
                if (prev.isNotEmpty()) {
                    T(prev.joinToString(" → ") + " →", 13.sp, C.Muted, align = TextAlign.End, maxLines = 2, modifier = Modifier.width(120.dp))
                }
            }
        }
    }
}

@Composable
private fun MicButton(listening: Boolean, onClick: () -> Unit) {
    val t = rememberInfiniteTransition(label = "halo")
    val p by t.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(if (listening) 900 else 1600)), label = "p",
    )
    Box(Modifier.size(104.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(104.dp)
                .graphicsLayer {
                    scaleX = 1f + 0.55f * p
                    scaleY = 1f + 0.55f * p
                    alpha = 0.55f * (1f - p)
                }
                .background(C.Red, CircleShape)
        )
        Box(
            Modifier
                .size(104.dp)
                .offset(y = 6.dp)
                .background(C.Ink, CircleShape)
        )
        Box(
            Modifier
                .size(104.dp)
                .clip(CircleShape)
                .background(if (listening) C.Ink else C.Red)
                .border(3.dp, C.Ink, CircleShape)
                .clickable(
                    role = Role.Button,
                    onClickLabel = if (listening) "Dejar de escuchar" else "Hablar",
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Ic.Mic, contentDescription = "Micrófono", tint = C.White, modifier = Modifier.size(44.dp))
        }
    }
}

// ======================= VALIDAR =======================

@Composable
fun ValidarScreen(vm: GameViewModel) {
    val p = vm.pending ?: return
    val ps = vm.table.getOrNull(vm.current) ?: return
    val ok = p.startsOk && !p.repeated
    val nextPs = vm.table.getOrNull(vm.peekNext())
    val nextLetter = Words.lastLetter(p.word).up()

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Avatar(ps.player, 36.dp)
            T("${ps.player.name} ha dicho", 17.sp, weight = FontWeight.Bold)
        }

        WordTiles(p.display)

        if (vm.timeLeft > 0f) LinkButton("¿No era eso? Repetir", { vm.retry() })

        Card(Modifier.fillMaxWidth()) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CheckLine(p.startsOk, if (p.startsOk) "Empieza por ${vm.letter.up()}" else "No empieza por ${vm.letter.up()}")
                CheckLine(!p.repeated, if (p.repeated) "Ya se dijo en esta partida" else "Nadie la ha dicho todavía")
                CheckLine(true, "A tiempo: ${String.format(Locale("es"), "%.1f", p.seconds)} s")
            }
        }

        Spacer(Modifier.weight(1f))

        when {
            ok -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T("¿Vale la palabra?", 30.sp, weight = FontWeight.ExtraBold, display = true)
                    T("Que lo decida el grupo.", 15.sp, C.Muted)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BigButton(
                        "No vale", { vm.fail(FailReason.NO_VALE) }, Modifier.weight(1f),
                        color = C.Paper, textColor = C.Ink, shadowColor = C.Ink, borderColor = C.Ink,
                        height = 76.dp, icon = Ic.Close, iconColor = C.Red,
                    )
                    BigButton(
                        "Vale", { vm.accept() }, Modifier.weight(1f),
                        shadowColor = C.Green, height = 76.dp, icon = Ic.Check, iconColor = C.Yellow,
                    )
                }
                if (nextPs != null) {
                    Card(Modifier.fillMaxWidth(), dashed = true) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TA(
                                buildAnnotatedString {
                                    append("Si vale, sigue ")
                                    withStyle(SpanStyle(color = C.Ink, fontWeight = FontWeight.Bold)) { append(nextPs.player.name) }
                                    append(" con la")
                                },
                                15.sp, C.Muted, modifier = Modifier.weight(1f),
                            )
                            Tile(nextLetter, 42.dp, border = 2.dp, shadow = 3.dp)
                        }
                    }
                }
            }
            !p.startsOk -> {
                T("No empieza por ${vm.letter.up()}", 28.sp, weight = FontWeight.ExtraBold, display = true)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (vm.timeLeft > 0f) {
                        BigButton("Repetir", { vm.retry() }, Modifier.weight(1f), fontSize = 20.sp)
                    }
                    BigButton(
                        "Es un fallo", { vm.fail(FailReason.NO_VALE) }, Modifier.weight(1f),
                        color = C.Red, textColor = C.White, shadowColor = C.Ink, fontSize = 20.sp,
                    )
                }
            }
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    T("¡Repetida!", 30.sp, weight = FontWeight.ExtraBold, display = true)
                    T("Las palabras repetidas se penalizan.", 15.sp, C.Muted)
                }
                BigButton(
                    "Aceptar penalización", { vm.fail(FailReason.REPETIDA) }, Modifier.fillMaxWidth(),
                    color = C.Red, textColor = C.White, shadowColor = C.Ink,
                )
            }
        }
    }
}

@Composable
private fun WordTiles(word: String) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val n = word.length.coerceAtLeast(1)
        val gap = 5.dp
        val size = min(46.dp, (maxWidth - gap * (n - 1)) / n)
        Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
            word.forEachIndexed { i, ch ->
                val first = i == 0
                val last = i == word.length - 1 && word.length > 1
                Tile(
                    ch.toString(), size,
                    color = when {
                        first -> C.Yellow
                        last -> C.Red
                        else -> C.Paper
                    },
                    textColor = if (last) C.White else C.Ink,
                    rotation = if (last) 6f else 0f,
                    border = 2.dp, shadow = 4.dp, fontScale = 0.6f,
                )
            }
        }
    }
}

@Composable
private fun CheckLine(ok: Boolean, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (ok) C.Green else C.Red),
            contentAlignment = Alignment.Center,
        ) {
            Icon(if (ok) Ic.Check else Ic.Close, contentDescription = if (ok) "Bien" else "Mal", tint = C.White, modifier = Modifier.size(14.dp))
        }
        T(text, 16.sp)
    }
}

// ======================= FALLO =======================

@Composable
fun FalloScreen(vm: GameViewModel) {
    val ps = vm.table.getOrNull(vm.failPlayer) ?: return
    val name = ps.player.name
    val headline = when (vm.lastFail) {
        FailReason.TIEMPO -> "¡Tiempo!"
        FailReason.PASO -> "Paso"
        FailReason.NO_VALE -> "No vale"
        FailReason.REPETIDA -> "¡Repetida!"
    }
    val detail = when {
        vm.mode == Mode.LIGA -> "$name no suma en este turno."
        vm.failEliminated -> "$name pierde su última vida y queda fuera."
        ps.lives == 1 -> "$name pierde una vida. Le queda 1."
        else -> "$name pierde una vida. Le quedan ${ps.lives}."
    }
    val winner = if (vm.gameOver && vm.mode == Mode.ELIMINACION) vm.table.firstOrNull { !it.eliminated } else null
    val next = if (!vm.gameOver) vm.table.getOrNull(vm.peekNext()) else null

    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Column(
            Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterVertically),
        ) {
            Icon(
                if (vm.lastFail == FailReason.TIEMPO) Ic.Clock else Ic.Close,
                contentDescription = null, tint = C.White, modifier = Modifier.size(92.dp),
            )
            T(headline, 60.sp, C.White, FontWeight.ExtraBold, display = true, align = TextAlign.Center, lineHeight = 62.sp)
            T(detail, 19.sp, C.White, align = TextAlign.Center, lineHeight = 26.sp)
            Row(
                Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(C.Ink)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Avatar(ps.player, 48.dp, alpha = if (vm.failEliminated) 0.6f else 1f)
                Column {
                    T(name, 20.sp, C.Cream, FontWeight.ExtraBold, display = true, strike = vm.failEliminated)
                    val sub = when {
                        vm.mode == Mode.LIGA -> "${ps.points} puntos"
                        vm.failEliminated -> "Eliminado en el turno ${ps.eliminatedAtTurn}"
                        else -> "Vidas: ${ps.lives}"
                    }
                    T(sub, 14.sp, C.Dim)
                }
            }
        }
        when {
            winner != null -> T("Solo queda ${winner.player.name} en pie.", 17.sp, C.White, FontWeight.Bold)
            next != null -> T("Sigue ${next.player.name} con la misma letra: ${vm.letter.up()}", 17.sp, C.White, FontWeight.Bold, align = TextAlign.Center)
            else -> {}
        }
        BigButton(
            if (vm.gameOver) "Ver resultados" else "Seguir",
            { vm.continueAfterFail() }, Modifier.fillMaxWidth(),
            color = C.Paper, textColor = C.Ink, shadowColor = C.Ink,
        )
    }
}

// ======================= PAUSA =======================

@Composable
fun PausaScreen(vm: GameViewModel) {
    val ps = vm.table.getOrNull(vm.current)
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(2) {
                    Chunky(
                        Modifier.size(width = 22.dp, height = 82.dp),
                        color = C.Yellow, shape = RoundedCornerShape(8.dp),
                        borderColor = C.Cream, borderWidth = 2.5.dp, shadowColor = C.Red,
                    ) {}
                }
            }
            T("Pausa", 56.sp, C.Cream, FontWeight.ExtraBold, display = true)
            if (ps != null) {
                Row(
                    Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, C.Ink2, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Avatar(ps.player, 36.dp)
                    Column {
                        TA(
                            buildAnnotatedString {
                                append("Turno de ")
                                withStyle(SpanStyle(color = C.Cream, fontWeight = FontWeight.Bold)) { append(ps.player.name) }
                                append(" con la ")
                                withStyle(SpanStyle(color = C.Yellow, fontWeight = FontWeight.Bold)) { append(vm.letter.up()) }
                            },
                            15.sp, C.Dim2,
                        )
                        T("Le quedan ${kotlin.math.ceil(vm.timeLeft).toInt()} s, el reloj está parado", 15.sp, C.Dim2)
                    }
                }
            }
        }
        BigButton(
            "Seguir jugando", { vm.resume() }, Modifier.fillMaxWidth(),
            color = C.Yellow, textColor = C.Ink, shadowColor = C.Red, icon = Ic.Play,
        )
        OutlineButton("Reiniciar partida", { vm.startGame() }, Modifier.fillMaxWidth(), color = C.Cream)
        LinkButton("Salir al inicio", { vm.go(Screen.INICIO) }, color = C.Cream, modifier = Modifier.align(Alignment.CenterHorizontally))
    }
}

// ======================= RESULTADOS =======================

@Composable
fun ResultadosScreen(vm: GameViewModel) {
    val ranking = vm.ranking()
    val top = ranking.firstOrNull() ?: return
    val totalWords = vm.chain.size
    val tie = vm.isTie()
    val subtitle = if (vm.mode == Mode.LIGA) {
        "Liga · ${vm.rounds} rondas · $totalWords palabras"
    } else {
        "Eliminación · ${vm.turn} turnos · $totalWords palabras"
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(120.dp)) {
                Tile(
                    if (tie) "=" else top.player.initial, 96.dp,
                    rotation = -4f, border = 3.dp, shadow = 7.dp,
                    modifier = Modifier.align(Alignment.Center),
                )
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(C.Red)
                        .border(3.dp, C.Ink, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Ic.Trophy, contentDescription = null, tint = C.White, modifier = Modifier.size(24.dp))
                }
            }
            T(
                if (tie) "¡Empate!" else "¡Ganó ${top.player.name}!",
                40.sp, weight = FontWeight.ExtraBold, display = true, align = TextAlign.Center, lineHeight = 42.sp,
            )
            T(subtitle, 15.sp, C.Muted, align = TextAlign.Center)
        }

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(ranking) { i, ps ->
                val first = i == 0
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (first) C.Ink else C.Paper)
                        .border(2.dp, if (first) C.Ink else C.Line, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    T("${i + 1}", 18.sp, if (first) C.Yellow else C.Ink, FontWeight.ExtraBold, display = true, modifier = Modifier.width(22.dp))
                    Avatar(ps.player, 32.dp)
                    T(ps.player.name, 17.sp, if (first) C.Cream else C.Ink, FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
                    val right = if (vm.mode == Mode.LIGA) "${ps.points} pts" else "${ps.words} palabras"
                    T(right, 14.sp, if (first) C.Dim2 else C.Muted)
                }
            }
            item {
                val longest = vm.longest
                val fastest = vm.fastest
                if (longest != null || fastest != null) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (longest != null) {
                            StatCard(
                                "Palabra más larga", longest.display,
                                vm.table.getOrNull(longest.player)?.player?.name ?: "",
                                Modifier.weight(1f),
                            )
                        }
                        if (fastest != null) {
                            StatCard(
                                "Respuesta más rápida",
                                "${String.format(Locale("es"), "%.1f", fastest.seconds)} s",
                                (vm.table.getOrNull(fastest.player)?.player?.name ?: "") + " · «" + fastest.display.lowercase(Locale("es")) + "»",
                                Modifier.weight(1f),
                            )
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlineButton("Inicio", { vm.go(Screen.INICIO) }, Modifier.weight(1f), height = 60.dp)
            BigButton("Revancha", { vm.startGame() }, Modifier.weight(1f), height = 64.dp, fontSize = 20.sp)
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, who: String, modifier: Modifier) {
    Card(modifier, dashed = true) {
        T(label, 13.sp, C.Muted, FontWeight.Bold)
        T(value, 19.sp, weight = FontWeight.ExtraBold, display = true, maxLines = 1)
        T(who, 13.sp, C.Muted, maxLines = 1)
    }
}
