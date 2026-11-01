package com.apleq.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apleq.app.ui.components.SplitzyLogo
import com.apleq.app.ui.screens.GuideContent.tr

private val GreenOk = Color(0xFF059669)
private val AmberWarn = Color(0xFFD97706)
private val PinkMock = Color(0xFFDB2777)

/** Ilustración de cada pantalla de la guía (0..5): maqueta del móvil con la interfaz real de Apleq. */
@Composable
fun GuideIllustration(index: Int) {
    // Fondo degradado suave con el «móvil» encima
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 10.dp,
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.width(236.dp).height(218.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                when (index) {
                    0 -> MockHome()
                    1 -> MockCreate()
                    2 -> MockInvite()
                    3 -> MockJoin()
                    4 -> MockAlerts()
                    else -> MockSettings()
                }
            }
        }
    }
}

// ---------- Pantallas de ejemplo ----------

@Composable
private fun MockHome() {
    MockBar(title = "Apleq", showLogo = true)
    MockCard("S", tr("Streaming", "Streaming", "Streaming"), tr("4 miembros", "4 membres", "4 members"), "17,99 €", MaterialTheme.colorScheme.primary)
    MockCard("M", tr("Música", "Música", "Music"), tr("3 miembros", "3 membres", "3 members"), "10,99 €", GreenOk)
    MockCard("C", "Cloud", tr("2 miembros", "2 membres", "2 members"), "2,99 €", AmberWarn)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.horizontalGradient(listOf(MaterialTheme.colorScheme.primary, GreenOk)))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            tr("Total al mes", "Total al mes", "Monthly total"),
            color = Color.White, fontSize = 11.sp, modifier = Modifier.weight(1f)
        )
        Text("31,97 €", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun MockCreate() {
    MockBar(title = tr("Nueva suscripción", "Nova subscripció", "New subscription"))
    MockField(tr("Nombre", "Nom", "Name"), "Streaming")
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
        Box(Modifier.weight(1f)) { MockField(tr("Precio", "Preu", "Price"), "17,99 €") }
        Box(Modifier.weight(1f)) { MockField(tr("Cobro", "Cobrament", "Billing"), tr("Mensual", "Mensual", "Monthly")) }
    }
    MockField(tr("Plazas", "Places", "Slots"), "4")
    MockButton(tr("Guardar", "Desa", "Save"))
}

@Composable
private fun MockInvite() {
    MockBar(title = tr("Invitar", "Convidar", "Invite"))
    Text(
        tr("Código de invitación", "Codi d'invitació", "Invitation code"),
        fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "K7M2QX",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 5.sp,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
        MockPill(tr("Caduca en 7 días", "Caduca en 7 dies", "Expires in 7 days"), AmberWarn.copy(alpha = 0.15f), AmberWarn)
    }
    MockButton(tr("Compartir", "Comparteix", "Share"), icon = true)
}

@Composable
private fun MockJoin() {
    MockBar(title = tr("Unirme a un grupo", "Unir-me a un grup", "Join a group"))
    MockField(tr("Código", "Codi", "Code"), "K7M2QX")
    MockButton(tr("Unirme", "Uneix-me", "Join"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text("Streaming", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(
                tr("Tu cuota al mes", "La teva quota al mes", "Your monthly share"),
                fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("4,50 €", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
    }
    MockPill(tr("Próximo cobro: día 5", "Proper cobrament: dia 5", "Next charge: the 5th"), MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
}

@Composable
private fun MockAlerts() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            tr("Avisos", "Avisos", "Alerts"),
            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
        )
        Box {
            Icon(Icons.Default.Notifications, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Box(
                modifier = Modifier.size(9.dp).clip(CircleShape).background(PinkMock).align(Alignment.TopEnd)
            )
        }
    }
    MockNotice(AmberWarn, tr("Cobro mañana · Streaming", "Cobrament demà · Streaming", "Charge tomorrow · Streaming"))
    MockNotice(PinkMock, tr("Nuevo mensaje de Ana", "Nou missatge d'Ana", "New message from Ana"))
    MockBubble(tr("¿Quién paga este mes?", "Qui paga aquest mes?", "Who pays this month?"), mine = false)
    MockBubble(tr("Ya lo he pagado ✓", "Ja l'he pagat ✓", "Already paid ✓"), mine = true)
}

@Composable
private fun MockSettings() {
    MockBar(title = tr("Ajustes", "Configuració", "Settings"))
    Text(tr("Tema", "Tema", "Theme"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MockChip(tr("Claro", "Clar", "Light"), selected = false)
        MockChip(tr("Oscuro", "Fosc", "Dark"), selected = true)
        MockChip("Auto", selected = false)
    }
    Text(tr("Idioma", "Idioma", "Language"), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        MockChip("ES", selected = false)
        MockChip("CA", selected = true)
        MockChip("EN", selected = false)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            tr("Mi cuenta & sincronización", "El meu compte i sincronització", "My account & sync"),
            fontSize = 10.sp, fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f)
        )
        Icon(Icons.Default.Check, null, tint = GreenOk, modifier = Modifier.size(16.dp))
    }
}

// ---------- Piezas sueltas ----------

@Composable
private fun MockBar(title: String, showLogo: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (showLogo) {
            SplitzyLogo(size = 20.dp)
            Spacer(Modifier.width(6.dp))
        }
        Text(
            title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MockCard(letter: String, title: String, subtitle: String, price: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(letter, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(price, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MockField(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.primary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MockButton(text: String, icon: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(vertical = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon) {
            Icon(Icons.Default.Share, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MockPill(text: String, bg: Color, fg: Color) {
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = fg)
    }
}

@Composable
private fun MockChip(text: String, selected: Boolean) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier.clip(RoundedCornerShape(50)).background(bg).padding(horizontal = 12.dp, vertical = 5.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = fg)
    }
}

@Composable
private fun MockNotice(dot: Color, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dot))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun MockBubble(text: String, mine: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (mine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text, fontSize = 10.sp,
                color = if (mine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
