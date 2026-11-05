package com.stunt.aimpanel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.isActive
import kotlin.math.hypot
import kotlin.math.min

private val Bg = Color(0xFF090909)
private val Panel = Color(0xFF191919)
private val Red = Color(0xFFFF3038)
private val Muted = Color(0xFFAAAAAA)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AimPanelApp() }
    }
}

@Composable
fun AimPanelApp() {
    var enabled by remember { mutableStateOf(true) }
    var strength by remember { mutableFloatStateOf(65f) }
    var fov by remember { mutableFloatStateOf(90f) }
    var target by remember { mutableStateOf(Offset(0.65f, 0.40f)) }
    var aim by remember { mutableStateOf(Offset(0.5f, 0.5f)) }
    var phase by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(enabled, strength, fov) {
        while (isActive) {
            withFrameNanos { }
            phase += 0.025f
            target = Offset(
                0.5f + 0.28f * kotlin.math.cos(phase),
                0.5f + 0.24f * kotlin.math.sin(phase * 1.35f)
            )
            if (enabled) {
                val dx = target.x - aim.x
                val dy = target.y - aim.y
                val distance = hypot(dx, dy)
                val fovRadius = (fov / 180f) * 0.72f
                if (distance < fovRadius && distance > 0.001f) {
                    val pull = (strength / 100f) * 0.075f
                    aim = Offset(aim.x + dx * pull, aim.y + dy * pull)
                }
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme(
        background = Bg, surface = Panel, primary = Red, onBackground = Color.White
    )) {
        Column(
            Modifier.fillMaxSize().background(Bg).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("◉", color = Red, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text("BLOOD STRIKE", color = Color.White, fontWeight = FontWeight.Black, fontSize = 19.sp)
                    Text("AIM PANEL • TREINAMENTO", color = Muted, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                Text("v0.1", color = Muted, fontSize = 11.sp)
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ASSISTÊNCIA DE MIRA", color = Color.White, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        Text(if (enabled) "ATIVA" else "PAUSADA",
                            color = if (enabled) Red else Muted, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { enabled = !enabled },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = if (enabled) Color(0xFF6E171B) else Red)
                    ) { Text(if (enabled) "PAUSAR TREINO" else "INICIAR TREINO", fontWeight = FontWeight.Bold) }
                    Text("Motor de rastreamento demonstrativo; atua somente nesta tela.",
                        color = Muted, fontSize = 11.sp)
                }
            }

            Text("CAMPO DE TREINAMENTO", color = Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Canvas(
                Modifier.fillMaxWidth().weight(1f).background(Color(0xFF111111), RoundedCornerShape(16.dp))
            ) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val radius = min(w, h) * (fov / 180f) * 0.48f
                drawCircle(Red.copy(alpha = 0.22f), radius, center, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                val t = Offset(target.x * w, target.y * h)
                drawCircle(Red, 13.dp.toPx(), t)
                drawCircle(Color.White, 4.dp.toPx(), t)
                val a = Offset(aim.x * w, aim.y * h)
                drawLine(Color.White, Offset(a.x - 13.dp.toPx(), a.y), Offset(a.x + 13.dp.toPx(), a.y), 2.dp.toPx())
                drawLine(Color.White, Offset(a.x, a.y - 13.dp.toPx()), Offset(a.x, a.y + 13.dp.toPx()), 2.dp.toPx())
                drawCircle(Color.White.copy(alpha = 0.6f), 3.dp.toPx(), a)
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel),
                shape = RoundedCornerShape(18.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row { Text("INTENSIDADE", color = Color.White, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("${strength.toInt()}%", color = Red) }
                    Slider(value = strength, onValueChange = { strength = it }, valueRange = 0f..100f,
                        colors = SliderDefaults.colors(thumbColor = Red, activeTrackColor = Red))
                    Row { Text("FOV", color = Color.White, fontWeight = FontWeight.Bold); Spacer(Modifier.weight(1f)); Text("${fov.toInt()}°", color = Red) }
                    Slider(value = fov, onValueChange = { fov = it }, valueRange = 20f..180f,
                        colors = SliderDefaults.colors(thumbColor = Red, activeTrackColor = Red))
                }
            }
            Text("DEMO LOCAL — NÃO CONECTADO AO BLOOD STRIKE",
                color = Muted, fontSize = 9.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }
}
