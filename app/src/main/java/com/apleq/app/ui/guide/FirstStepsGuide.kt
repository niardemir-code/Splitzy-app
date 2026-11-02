package com.apleq.app.ui.guide

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apleq.app.data.util.GuidePath
import java.util.Locale

enum class GuideStepId {
    // Gestor
    M_CREATE,
    M_INVITE,
    M_SHARE,
    M_JOIN,
    M_MEMBER,
    M_BELL,
    // Cliente
    C_JOIN,
    C_GROUP,
    C_CHAT,
    C_BELL
}

data class GuideStepStatus(
    val id: GuideStepId,
    val done: Boolean
)

object FirstStepsTexts {
    private fun tr(es: String, ca: String, en: String): String {
        val lang = Locale.getDefault().language.lowercase()
        return when {
            lang.startsWith("ca") -> ca
            lang.startsWith("es") -> es
            else -> en
        }
    }

    // Diálogo inicial
    val dialogTitle: String
        get() = tr(
            "¿Qué vienes a hacer?",
            "Què vens a fer?",
            "What do you want to do?"
        )

    val dialogDescription: String
        get() = tr(
            "Elige tu rol para adaptar la ayuda a lo que necesitas. Puedes cambiarlo cuando quieras desde Ajustes.",
            "Tria el teu rol per adaptar l'ajuda al que necessites. Pots canviar-ho quan vulguis des d'Ajustos.",
            "Choose your role so we can tailor the guide to you. You can change it anytime in Settings."
        )

    val managerTitle: String
        get() = tr(
            "Quiero compartir una suscripción",
            "Vull compartir una subscripció",
            "I want to share a subscription"
        )

    val managerSubtitle: String
        get() = tr(
            "Soy el titular del servicio (Netflix, Spotify...) y quiero invitar a amigos o familia y llevar las cuentas.",
            "Sóc el titular del servei (Netflix, Spotify...) i vull convidar amics o família i portar els comptes.",
            "I'm the account owner (Netflix, Spotify...) and want to invite members and track payments."
        )

    val clientTitle: String
        get() = tr(
            "Me han invitado a una suscripción",
            "M'han convidat a una subscripció",
            "I was invited to a subscription"
        )

    val clientSubtitle: String
        get() = tr(
            "Tengo un código de invitación de 6 letras para unirme a un grupo ya creado.",
            "Tinc un codi d'invitació de 6 lletres per unir-me a un grup ja creat.",
            "I have a 6-letter invite code to join an existing group."
        )

    val decideLater: String
        get() = tr(
            "Lo decidiré luego",
            "Ho decidiré després",
            "I'll decide later"
        )

    // Ajustes
    val settingsTitle: String
        get() = tr(
            "Primeros pasos",
            "Primers passos",
            "First steps"
        )

    val settingsSubtitle: String
        get() = tr(
            "Reiniciar la guía y elegir qué quieres hacer",
            "Reiniciar la guia i triar què vols fer",
            "Reset the guide and choose what you want to do"
        )

    // Tarjeta
    val cardTitle: String
        get() = tr(
            "Primeros pasos",
            "Primers passos",
            "First steps"
        )

    val roleManagerTag: String
        get() = tr("Gestor", "Gestor", "Manager")

    val roleClientTag: String
        get() = tr("Miembro", "Membre", "Member")

    val allDone: String
        get() = tr(
            "¡Listo! Has completado todos los pasos.",
            "Fet! Has completat tots els passos.",
            "All done! You've completed all steps."
        )

    val dismiss: String
        get() = tr("Ocultar", "Amagar", "Dismiss")

    val changeRole: String
        get() = tr("Cambiar rol", "Canviar rol", "Change role")

    val inviteNeedsSlotsWarning: String
        get() = tr(
            "Para generar el código necesitas tener al menos un hueco libre en la suscripción.",
            "Per generar el codi necessites tenir almenys un buit lliure a la subscripció.",
            "To generate the code you need at least one free slot in the subscription."
        )

    fun stepTitle(id: GuideStepId): String = when (id) {
        GuideStepId.M_CREATE -> tr(
            "Crea tu primera suscripción",
            "Crea la teva primera subscripció",
            "Create your first subscription"
        )
        GuideStepId.M_INVITE -> tr(
            "Activa el código de invitación",
            "Activa el codi d'invitació",
            "Activate invite code"
        )
        GuideStepId.M_SHARE -> tr(
            "Comparte el código con tus miembros",
            "Comparteix el codi amb els teus membres",
            "Share the code with your members"
        )
        GuideStepId.M_JOIN -> tr(
            "Espera a que se una el primer miembro",
            "Espera que s'uneixi el primer membre",
            "Wait for the first member to join"
        )
        GuideStepId.M_MEMBER -> tr(
            "Revisa los pagos del miembro",
            "Revisa els pagaments del membre",
            "Review member payments"
        )
        GuideStepId.M_BELL -> tr(
            "Descubre el centro de avisos",
            "Descobreix el centre d'avisos",
            "Explore notification center"
        )
        GuideStepId.C_JOIN -> tr(
            "Únete con tu código de invitación",
            "Uneix-te amb el teu codi d'invitació",
            "Join with your invite code"
        )
        GuideStepId.C_GROUP -> tr(
            "Abre la suscripción",
            "Obre la subscripció",
            "Open the subscription"
        )
        GuideStepId.C_CHAT -> tr(
            "Habla con el titular por el chat",
            "Parla amb el titular pel xat",
            "Chat with the owner"
        )
        GuideStepId.C_BELL -> tr(
            "Revisa la campana de avisos",
            "Revisa la campana d'avisos",
            "Check notification bell"
        )
    }

    fun stepDetail(id: GuideStepId): String = when (id) {
        GuideStepId.M_CREATE -> tr(
            "Pulsa el botón «+» para añadir el servicio que compartes (Netflix, Spotify, YouTube...) con su precio y fecha de cobro.",
            "Toca el botó «+» per afegir el servei que comparteixes (Netflix, Spotify, YouTube...) amb el preu i data de cobrament.",
            "Tap the \"+\" button to add the service you share (Netflix, Spotify, YouTube...) with its cost and billing date."
        )
        GuideStepId.M_INVITE -> tr(
            "Dentro de la tarjeta de la suscripción, pulsa «Invitar a hueco» para generar un código de 6 letras que compartir.",
            "A la targeta de la subscripció, toca «Convidar a buit» per tenir un codi de 6 lletres per compartir.",
            "Inside the subscription card, tap \"Invite to slot\" to get a 6-letter invite code to share."
        )
        GuideStepId.M_SHARE -> tr(
            "Pulsa el botón de compartir al lado del código para enviarlo por WhatsApp, Telegram o copiarlo.",
            "Toca el botó de compartir al costat del codi per enviar-lo per WhatsApp, Telegram o copiar-lo.",
            "Tap the share button next to the code to send it via WhatsApp, Telegram, or copy it."
        )
        GuideStepId.M_JOIN -> tr(
            "Cuando alguien introduzca tu código con «Unirme», verás su nombre en la tarjeta y recibirás un aviso.",
            "Quan algú introdueixi el teu codi amb «Unir-me», veuràs el seu nom a la targeta i rebràs un avís.",
            "When someone enters your code with \"Join\", you will see their name on the card and receive an alert."
        )
        GuideStepId.M_MEMBER -> tr(
            "Toca el nombre del miembro para ver su historial, marcarlo como pagado o enviar un recordatorio.",
            "Toca el nom del membre per veure el seu historial, marcar-lo com a pagat o enviar un recordatori.",
            "Tap the member's name to view history, mark as paid, or send a reminder."
        )
        GuideStepId.M_BELL -> tr(
            "La campana de arriba te avisa de nuevos miembros, bajas y próximos cobros.",
            "La campana de dalt t'avisa de nous membres, baixes i propers cobraments.",
            "The bell at the top notifies you of new members, departures, and upcoming billings."
        )
        GuideStepId.C_JOIN -> tr(
            "Pulsa «Unirme» arriba e introduce el código de 6 letras que te ha dado el titular.",
            "Toca «Unir-me» a dalt i introdueix el codi de 6 lletres que t'ha donat el titular.",
            "Tap \"Join\" at top and enter the 6-letter code given to you by the owner."
        )
        GuideStepId.C_GROUP -> tr(
            "Toca la tarjeta de la suscripción para ver los detalles del servicio, tu cuota y cuándo se paga.",
            "Toca la targeta de la subscripció per veure els detalls del servei, la teva quota i quan es paga.",
            "Tap the subscription card to view service details, your share, and billing dates."
        )
        GuideStepId.C_CHAT -> tr(
            "Pulsa el icono de chat en la tarjeta para hablar directamente con el titular de la cuenta.",
            "Toca la icona de xat a la targeta per parlar directament amb el titular del compte.",
            "Tap the chat icon on the card to talk directly with the subscription owner."
        )
        GuideStepId.C_BELL -> tr(
            "Aquí recibirás recordatorios de pago y avisos importantes del titular.",
            "Aquí rebràs recordatoris de pagament i avisos importants del titular.",
            "Here you will receive payment reminders and important updates from the owner."
        )
    }

    fun stepActionLabel(id: GuideStepId): String? = when (id) {
        GuideStepId.M_CREATE -> tr("Crear suscripción", "Crear subscripció", "Create subscription")
        GuideStepId.M_BELL, GuideStepId.C_BELL -> tr("Ver avisos", "Veure avisos", "View notifications")
        GuideStepId.C_JOIN -> tr("Unirme a grupo", "Unir-me a grup", "Join group")
        else -> null
    }
}

@Composable
fun FirstStepsChoiceDialog(
    onChoosePath: (GuidePath) -> Unit,
    onDecideLater: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDecideLater,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = FirstStepsTexts.dialogTitle,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = FirstStepsTexts.dialogDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Opción 1: Gestor
                Surface(
                    onClick = { onChoosePath(GuidePath.MANAGER) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_guide_choose_manager")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = FirstStepsTexts.managerTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FirstStepsTexts.managerSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Opción 2: Cliente
                Surface(
                    onClick = { onChoosePath(GuidePath.CLIENT) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_guide_choose_client")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = FirstStepsTexts.clientTitle,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = FirstStepsTexts.clientSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDecideLater,
                modifier = Modifier.testTag("btn_guide_decide_later")
            ) {
                Text(FirstStepsTexts.decideLater)
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun FirstStepsCard(
    path: GuidePath,
    steps: List<GuideStepStatus>,
    collapsed: Boolean,
    inviteNeedsSlots: Boolean,
    onToggleCollapse: () -> Unit,
    onDismiss: () -> Unit,
    onReopenRole: () -> Unit,
    onCreateSubscription: () -> Unit,
    onJoinGroup: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = steps.size
    val doneCount = steps.count { it.done }
    val allDone = total > 0 && doneCount == total
    val currentStepIndex = steps.indexOfFirst { !it.done }

    val roleTag = if (path == GuidePath.MANAGER) FirstStepsTexts.roleManagerTag else FirstStepsTexts.roleClientTag

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_first_steps"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Header: Icon, Title, Role Tag, Count, Expand/Collapse
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleCollapse() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (allDone) Color(0xFF10B981).copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (allDone) Icons.Default.CheckCircle else Icons.Default.School,
                        contentDescription = null,
                        tint = if (allDone) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = FirstStepsTexts.cardTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = roleTag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "$doneCount/$total",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (allDone) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onToggleCollapse,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = if (collapsed) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { if (total > 0) doneCount.toFloat() / total.toFloat() else 0f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (allDone) Color(0xFF10B981) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            if (!collapsed) {
                Spacer(modifier = Modifier.height(14.dp))

                if (allDone) {
                    // Estado completado
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = FirstStepsTexts.allDone,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onReopenRole,
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text(FirstStepsTexts.changeRole)
                        }
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(FirstStepsTexts.dismiss)
                        }
                    }
                } else {
                    // Lista de pasos
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        steps.forEachIndexed { index, step ->
                            val isCurrent = index == currentStepIndex
                            StepItemView(
                                index = index + 1,
                                step = step,
                                isCurrent = isCurrent,
                                inviteNeedsSlots = inviteNeedsSlots && step.id == GuideStepId.M_INVITE,
                                onActionClick = {
                                    when (step.id) {
                                        GuideStepId.M_CREATE -> onCreateSubscription()
                                        GuideStepId.M_BELL, GuideStepId.C_BELL -> onOpenNotifications()
                                        GuideStepId.C_JOIN -> onJoinGroup()
                                        else -> Unit
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = onReopenRole,
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text(
                                text = FirstStepsTexts.changeRole,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        TextButton(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(horizontal = 6.dp)
                        ) {
                            Text(
                                text = FirstStepsTexts.dismiss,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItemView(
    index: Int,
    step: GuideStepStatus,
    isCurrent: Boolean,
    inviteNeedsSlots: Boolean,
    onActionClick: () -> Unit
) {
    val title = FirstStepsTexts.stepTitle(step.id)
    val detail = FirstStepsTexts.stepDetail(step.id)
    val actionLabel = FirstStepsTexts.stepActionLabel(step.id)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = when {
            step.done -> Color.Transparent
            isCurrent -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else -> Color.Transparent
        },
        border = when {
            isCurrent -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            else -> null
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicador circular (check verde, número destacado o número atenuado)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                step.done -> Color(0xFF10B981)
                                isCurrent -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (step.done) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    } else {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    ),
                    textDecoration = if (step.done) TextDecoration.LineThrough else TextDecoration.None,
                    color = when {
                        step.done -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        isCurrent -> MaterialTheme.colorScheme.onSurface
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Si es el paso actual, mostrar la explicación detallada y su botón de acción si existe
            if (isCurrent && !step.done) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.padding(start = 34.dp)) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (inviteNeedsSlots) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = FirstStepsTexts.inviteNeedsSlotsWarning,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    if (actionLabel != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = onActionClick,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
