package com.bryan.pasaletra

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ======================= INICIO =======================

@Composable
fun InicioScreen(vm: GameViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
        ) {
            Logo()
            Spacer(Modifier.height(28.dp))
            TA(
                buildAnnotatedString {
                    append("Di una palabra. El siguiente jugador empieza por su ")
                    withStyle(SpanStyle(color = C.Red, fontWeight = FontWeight.Bold)) { append("última letra") }
                    append(".")
                },
                19.sp, lineHeight = 26.sp,
            )
            Spacer(Modifier.height(20.dp))
            TA(
                buildAnnotatedString {
                    val words = listOf("casa", "árbol", "limón", "nube")
                    words.forEachIndexed { i, w ->
                        if (i > 0) {
                            withStyle(SpanStyle(color = C.Ink, fontWeight = FontWeight.Bold)) { append(w.first()) }
                            append(w.substring(1, w.length - 1))
                        } else append(w.dropLast(1))
                        withStyle(SpanStyle(color = C.Red, fontWeight = FontWeight.Bold)) { append(w.last()) }
                        append("  →  ")
                    }
                    append("…")
                },
                15.sp, color = C.Muted,
            )
        }
        BigButton("Jugar", { vm.go(Screen.JUGADORES) }, Modifier.fillMaxWidth(), icon = Ic.Play, fontSize = 24.sp)
        OutlineButton("Cómo se juega", { vm.go(Screen.COMO) }, Modifier.fillMaxWidth())
        T(
            "De 2 a 8 jugadores · un solo teléfono", 14.sp, C.Muted,
            modifier = Modifier.fillMaxWidth(), align = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}

@Composable
private fun Logo() {
    val transition = rememberInfiniteTransition(label = "logo")
    val hop by transition.animateFloat(
        initialValue = 0f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            keyframes {
                durationMillis = 2400
                0f at 0
                0f at 1400
                1f at 1800
                0f at 2150
            }
        ),
        label = "hop",
    )
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val k = (maxWidth.value / 340f).coerceAtMost(1.15f)
        val big = (64 * k).dp
        val small = (52 * k).dp
        val gap = (8 * k).dp
        Column(verticalArrangement = Arrangement.spacedBy(gap + 2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(gap)) {
                Tile("P", big)
                Tile("A", big, rotation = -3f)
                Tile("S", big)
                Tile("A", big, rotation = 2f)
            }
            Row(Modifier.padding(start = (36 * k).dp), horizontalArrangement = Arrangement.spacedBy(gap)) {
                Tile("L", big, color = C.Paper)
                Tile("A", big, color = C.Paper, rotation = -2f)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                Tile("L", small)
                Tile("E", small)
                Tile("T", small, rotation = 3f)
                Tile("R", small)
                Tile(
                    "A", small,
                    color = C.Red, textColor = C.White,
                    rotation = 8f + 10f * hop,
                    modifier = Modifier.offset(x = (6f * hop).dp, y = (-6f - 10f * hop).dp),
                )
                Icon(
                    Ic.Back, contentDescription = null, tint = C.Red,
                    modifier = Modifier
                        .size((30 * k).dp)
                        .offset(x = (4f * hop).dp)
                        .rotate(180f),
                )
            }
        }
    }
}

// ======================= CÓMO SE JUEGA =======================

@Composable
fun ComoScreen(vm: GameViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(onBack = { vm.go(Screen.INICIO) })
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            T("Cómo se juega", 38.sp, weight = FontWeight.ExtraBold, display = true, lineHeight = 40.sp)
            Step("1", "Di una palabra", "Cuando sea tu turno, toca el micrófono y di una palabra que empiece por la letra de la ficha.")
            Step("2", "Pasa la letra", "Su última letra es la del siguiente jugador: manzana → abeja.")
            Step("3", "El grupo decide", "La app revisa la letra, las repetidas y el tiempo. Ustedes votan si la palabra existe.")
            Step("4", "Si fallas…", "En Eliminación pierdes una vida. En Liga no sumas ese turno. La letra pasa igual al siguiente.", red = true)
            Card(Modifier.fillMaxWidth(), dashed = true) {
                T("Letras con truco", 15.sp, weight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                T("Las tildes no cuentan: café termina en E.", 14.sp, C.Body, lineHeight = 20.sp)
                T("Si una palabra acaba en X o W, pasa la letra anterior: tórax → A.", 14.sp, C.Body, lineHeight = 20.sp)
                T("No se puede repetir una palabra en la misma partida.", 14.sp, C.Body, lineHeight = 20.sp)
            }
        }
        BigButton("Entendido", { vm.go(Screen.INICIO) }, Modifier.fillMaxWidth())
    }
}

@Composable
private fun Step(n: String, title: String, text: String, red: Boolean = false) {
    Card(Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Tile(
                n, 40.dp,
                color = if (red) C.Red else C.Yellow,
                textColor = if (red) C.White else C.Ink,
                border = 2.dp, shadow = 3.dp, fontScale = 0.55f,
            )
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                T(title, 17.sp, weight = FontWeight.Bold)
                T(text, 15.sp, C.Body, lineHeight = 21.sp)
            }
        }
    }
}

// ======================= JUGADORES =======================

@Composable
fun JugadoresScreen(vm: GameViewModel) {
    var draft by remember { mutableStateOf("") }
    val fonts = LocalFonts.current
    val full = vm.players.size >= 8
    fun add() {
        if (vm.addPlayer(draft)) draft = ""
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(onBack = { vm.go(Screen.INICIO) }, label = "Paso 1 de 2")
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            T("¿Quién juega?", 40.sp, weight = FontWeight.ExtraBold, display = true, lineHeight = 42.sp)
            T("De 2 a 8 jugadores. Usa las flechas para cambiar el orden.", 16.sp, C.Muted, lineHeight = 22.sp)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(C.Paper)
                    .border(2.dp, C.Ink, RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                if (draft.isEmpty()) T(if (full) "Máximo 8 jugadores" else "Escribe un nombre", 17.sp, C.Muted)
                BasicTextField(
                    value = draft,
                    onValueChange = { if (it.length <= 14) draft = it },
                    enabled = !full,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 17.sp, color = C.Ink, fontFamily = fonts.body),
                    cursorBrush = SolidColor(C.Red),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { add() }),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            val canAdd = draft.isNotBlank() && !full
            Chunky(
                modifier = Modifier
                    .size(width = 56.dp, height = 60.dp)
                    .clickable(enabled = canAdd, role = Role.Button, onClickLabel = "Añadir jugador") { add() },
                color = if (canAdd) C.Ink else C.Ink3,
                borderColor = null,
                shadowColor = C.Red,
                shadowOffset = 4.dp,
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(Ic.Plus, contentDescription = "Añadir jugador", tint = C.Cream, modifier = Modifier.size(24.dp))
            }
        }

        LazyColumn(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(vm.players, key = { _, p -> p.id }) { i, p ->
                PlayerRow(
                    player = p,
                    canUp = i > 0,
                    canDown = i < vm.players.size - 1,
                    onUp = { vm.movePlayer(p.id, -1) },
                    onDown = { vm.movePlayer(p.id, 1) },
                    onRemove = { vm.removePlayer(p.id) },
                )
            }
            if (vm.players.isEmpty()) {
                item {
                    T("Todavía no hay jugadores. Escribe un nombre y pulsa +.", 15.sp, C.Muted, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(2.dp, C.Ink, RoundedCornerShape(16.dp))
                .clickable(role = Role.Switch) { vm.shuffle = !vm.shuffle }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Ic.Shuffle, contentDescription = null, tint = C.Ink, modifier = Modifier.size(22.dp))
            T("Orden aleatorio", 16.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
            SwitchKnob(vm.shuffle, 48.dp, 28.dp)
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                T("${vm.players.size}", 20.sp, weight = FontWeight.ExtraBold, display = true)
                T(if (vm.players.size == 1) "jugador" else "jugadores", 15.sp, C.Muted)
            }
            BigButton(
                "Continuar", { vm.go(Screen.MODO) },
                Modifier.weight(1f), enabled = vm.players.size >= 2,
            )
        }
    }
}

@Composable
private fun PlayerRow(
    player: Player,
    canUp: Boolean,
    canDown: Boolean,
    onUp: () -> Unit,
    onDown: () -> Unit,
    onRemove: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(C.Paper)
            .border(2.dp, C.Line, RoundedCornerShape(16.dp))
            .padding(start = 10.dp, end = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Avatar(player, 36.dp)
        T(player.name, 18.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f), maxLines = 1)
        SmallIcon(Ic.Up, "Subir a ${player.name}", canUp, onUp)
        SmallIcon(Ic.Down, "Bajar a ${player.name}", canDown, onDown)
        SmallIcon(Ic.Close, "Quitar a ${player.name}", true, onRemove)
    }
}

@Composable
private fun SmallIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (enabled) C.Muted else C.Line2, modifier = Modifier.size(20.dp))
    }
}

// ======================= MODO Y AJUSTES =======================

@Composable
fun ModoScreen(vm: GameViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TopBar(onBack = { vm.go(Screen.JUGADORES) }, label = "Paso 2 de 2")
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            T("¿Cómo jugamos?", 38.sp, weight = FontWeight.ExtraBold, display = true, lineHeight = 40.sp)

            ModeCard(
                title = "Eliminación",
                text = "Si fallas, pierdes una vida. Gana el último que quede en pie.",
                selected = vm.mode == Mode.ELIMINACION,
                onClick = { vm.mode = Mode.ELIMINACION },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    T("Vidas", 14.sp, weight = FontWeight.Bold)
                    listOf(1, 2, 3).forEach { n -> Pill("$n", vm.lives == n, { vm.lives = n }) }
                }
            }
            ModeCard(
                title = "Liga",
                text = "Nadie queda fuera. Cada palabra válida suma 1 punto. Gana quien más sume.",
                selected = vm.mode == Mode.LIGA,
                onClick = { vm.mode = Mode.LIGA },
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    T("Rondas", 14.sp, weight = FontWeight.Bold)
                    listOf(5, 10, 15).forEach { n -> Pill("$n", vm.rounds == n, { vm.rounds = n }) }
                }
            }

            T("TIEMPO POR TURNO", 14.sp, C.Muted, FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(10, 15, 20, 30).forEach { s ->
                    val on = vm.seconds == s
                    Chunky(
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .clickable(role = Role.RadioButton) { vm.seconds = s },
                        color = if (on) C.Yellow else C.Cream,
                        shape = RoundedCornerShape(14.dp),
                        shadowColor = if (on) C.Ink else null,
                        shadowOffset = 4.dp,
                    ) {
                        T("$s s", 18.sp, weight = FontWeight.ExtraBold, display = true)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ToggleCard("Sonido", vm.sound, { vm.sound = !vm.sound }, Modifier.weight(1f))
                ToggleCard("Vibración", vm.vibration, { vm.vibration = !vm.vibration }, Modifier.weight(1f))
            }
        }
        BigButton(
            "Sortear y empezar", { vm.startGame() },
            Modifier.fillMaxWidth(), enabled = vm.players.size >= 2,
        )
    }
}

@Composable
private fun ModeCard(
    title: String,
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    extra: @Composable () -> Unit,
) {
    Chunky(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton, onClick = onClick),
        color = C.Paper,
        shape = RoundedCornerShape(18.dp),
        borderColor = if (selected) C.Ink else C.Line,
        borderWidth = if (selected) 2.5.dp else 2.dp,
        shadowColor = if (selected) C.Ink else null,
        contentAlignment = Alignment.TopStart,
    ) {
        Column(
            Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                T(title, 22.sp, weight = FontWeight.ExtraBold, display = true, modifier = Modifier.weight(1f))
                if (selected) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(C.Red),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Ic.Check, contentDescription = "Elegido", tint = C.White, modifier = Modifier.size(16.dp))
                    }
                } else {
                    Box(
                        Modifier
                            .size(28.dp)
                            .border(2.dp, C.Line2, CircleShape)
                    )
                }
            }
            T(text, 15.sp, C.Body, lineHeight = 21.sp)
            if (selected) extra()
        }
    }
}

@Composable
private fun ToggleCard(label: String, on: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(C.Paper)
            .border(2.dp, C.Line, RoundedCornerShape(14.dp))
            .clickable(role = Role.Switch, onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        T(label, 15.sp, weight = FontWeight.Bold, modifier = Modifier.weight(1f))
        SwitchKnob(on)
    }
}
