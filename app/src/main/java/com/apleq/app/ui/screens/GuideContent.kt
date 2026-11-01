package com.apleq.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import com.apleq.app.ui.util.I18n

data class GuideRow(val icon: ImageVector, val title: String, val detail: String)

data class GuidePage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val rows: List<GuideRow>
)

/** Textos de la guía de inicio en español, catalán e inglés. */
object GuideContent {

    internal fun tr(es: String, ca: String, en: String): String = when {
        I18n.isSpanish -> es
        I18n.isCatalan -> ca
        else -> en
    }

    val skip: String get() = tr("Saltar", "Omet", "Skip")
    val next: String get() = tr("Siguiente", "Següent", "Next")
    val previous: String get() = tr("Anterior", "Anterior", "Back")
    val start: String get() = tr("Empezar", "Comença", "Get started")

    val settingsTitle: String get() = tr("Guía de inicio", "Guia d'inici", "Quick start guide")
    val settingsSubtitle: String get() = tr(
        "Repasa cómo funciona Apleq paso a paso",
        "Repassa com funciona Apleq pas a pas",
        "Review how Apleq works, step by step"
    )

    fun pages(): List<GuidePage> = listOf(
        GuidePage(
            icon = Icons.Default.Star,
            title = tr("Bienvenido a Apleq", "Et donem la benvinguda a Apleq", "Welcome to Apleq"),
            subtitle = tr(
                "Controla tus suscripciones compartidas sin líos.",
                "Controla les teves subscripcions compartides sense embolics.",
                "Keep your shared subscriptions under control."
            ),
            rows = listOf(
                GuideRow(
                    Icons.Default.Edit,
                    tr("Tus suscripciones", "Les teves subscripcions", "Your subscriptions"),
                    tr("Apunta cada servicio con su precio y su fecha de cobro.",
                        "Apunta cada servei amb el seu preu i la data de cobrament.",
                        "Add each service with its price and billing date.")
                ),
                GuideRow(
                    Icons.Default.Group,
                    tr("Compártelas", "Comparteix-les", "Share them"),
                    tr("Reparte las plazas con otras personas y sabed quién paga qué.",
                        "Reparteix les places amb altres persones i sabeu qui paga què.",
                        "Split the slots with other people and know who pays what.")
                ),
                GuideRow(
                    Icons.Default.Notifications,
                    tr("Sin sorpresas", "Sense sorpreses", "No surprises"),
                    tr("Recibe avisos antes de cada cobro.",
                        "Rep avisos abans de cada cobrament.",
                        "Get reminders before every charge.")
                )
            )
        ),
        GuidePage(
            icon = Icons.Default.Edit,
            title = tr("Crea tu suscripción", "Crea la teva subscripció", "Create your subscription"),
            subtitle = tr(
                "Si eres quien paga el servicio, tú eres el gestor.",
                "Si ets qui paga el servei, tu ets el gestor.",
                "If you pay for the service, you're the manager."
            ),
            rows = listOf(
                GuideRow(
                    Icons.Default.Info,
                    tr("Datos del servicio", "Dades del servei", "Service details"),
                    tr("Nombre, precio y cada cuánto se cobra.",
                        "Nom, preu i cada quan es cobra.",
                        "Name, price and how often it's charged.")
                ),
                GuideRow(
                    Icons.Default.Group,
                    tr("Plazas", "Places", "Slots"),
                    tr("Indica cuántas personas pueden compartirla.",
                        "Indica quantes persones la poden compartir.",
                        "Set how many people can share it.")
                ),
                GuideRow(
                    Icons.Default.Share,
                    tr("Plataforma", "Plataforma", "Platform"),
                    tr("Elige dónde la compartís, o añade la tuya en Ajustes.",
                        "Tria on la compartiu, o afegeix la teva a Configuració.",
                        "Pick where you share it, or add your own in Settings.")
                )
            )
        ),
        GuidePage(
            icon = Icons.Default.Share,
            title = tr("Invita con un código", "Convida amb un codi", "Invite with a code"),
            subtitle = tr(
                "Sin buscar a nadie: genera un código y compártelo.",
                "Sense buscar ningú: genera un codi i comparteix-lo.",
                "No searching for anyone: generate a code and share it."
            ),
            rows = listOf(
                GuideRow(
                    Icons.Default.Lock,
                    tr("Código de 6 caracteres", "Codi de 6 caràcters", "6-character code"),
                    tr("Cada invitación tiene su propio código.",
                        "Cada invitació té el seu propi codi.",
                        "Every invitation has its own code.")
                ),
                GuideRow(
                    Icons.Default.DateRange,
                    tr("Caduca a los 7 días", "Caduca als 7 dies", "Expires in 7 days"),
                    tr("Si no se usa, deja de valer. Puedes crear otra.",
                        "Si no s'utilitza, deixa de valer. Pots crear-ne una altra.",
                        "If it isn't used, it stops working. You can create another.")
                ),
                GuideRow(
                    Icons.Default.Share,
                    tr("Envíalo como quieras", "Envia'l com vulguis", "Send it any way you like"),
                    tr("Por mensaje, correo o en persona.",
                        "Per missatge, correu o en persona.",
                        "By message, email or in person.")
                )
            )
        ),
        GuidePage(
            icon = Icons.Default.Person,
            title = tr("Únete a un grupo", "Uneix-te a un grup", "Join a group"),
            subtitle = tr(
                "Si te han invitado, tú eres cliente.",
                "Si t'han convidat, tu ets client.",
                "If you've been invited, you're a client."
            ),
            rows = listOf(
                GuideRow(
                    Icons.Default.Edit,
                    tr("Introduce el código", "Introdueix el codi", "Enter the code"),
                    tr("Te lo pasa quien gestiona la suscripción.",
                        "Te'l passa qui gestiona la subscripció.",
                        "The subscription manager gives it to you.")
                ),
                GuideRow(
                    Icons.Default.DateRange,
                    tr("Tu cuota y tus fechas", "La teva quota i les teves dates", "Your share and dates"),
                    tr("Verás cuánto pagas y cuándo.",
                        "Veuràs quant pagues i quan.",
                        "You'll see how much you pay and when.")
                ),
                GuideRow(
                    Icons.Default.Favorite,
                    tr("Libertad para salir", "Llibertat per sortir", "Free to leave"),
                    tr("Puedes abandonar el grupo cuando quieras; el gestor recibe un aviso.",
                        "Pots abandonar el grup quan vulguis; el gestor rep un avís.",
                        "You can leave the group any time; the manager gets a notice.")
                )
            )
        ),
        GuidePage(
            icon = Icons.Default.Notifications,
            title = tr("Avisos y chat", "Avisos i xat", "Alerts and chat"),
            subtitle = tr(
                "Todo lo importante, en un solo sitio.",
                "Tot el que és important, en un sol lloc.",
                "Everything important, in one place."
            ),
            rows = listOf(
                GuideRow(
                    Icons.Default.Notifications,
                    tr("La campana", "La campana", "The bell"),
                    tr("Cobros próximos, mensajes nuevos y bajas del grupo.",
                        "Cobraments propers, missatges nous i baixes del grup.",
                        "Upcoming charges, new messages and members leaving.")
                ),
                GuideRow(
                    Icons.Default.Email,
                    tr("Chat del grupo", "Xat del grup", "Group chat"),
                    tr("Habla con el gestor o con los clientes sin salir de la app.",
                        "Parla amb el gestor o amb els clients sense sortir de l'app.",
                        "Talk to the manager or clients without leaving the app.")
                ),
                GuideRow(
                    Icons.Default.DateRange,
                    tr("Recordatorios de cobro", "Recordatoris de cobrament", "Payment reminders"),
                    tr("Te avisamos antes de cada renovación.",
                        "T'avisem abans de cada renovació.",
                        "We remind you before each renewal.")
                )
            )
        ),
        GuidePage(
            icon = Icons.Default.Settings,
            title = tr("Hazla tuya", "Fes-la teva", "Make it yours"),
            subtitle = tr(
                "Ajusta la app a tu gusto.",
                "Ajusta l'app al teu gust.",
                "Adjust the app to your taste."
            ),
            rows = listOf(
                GuideRow(
                    Icons.Default.Settings,
                    tr("Ajustes", "Configuració", "Settings"),
                    tr("Tema claro u oscuro e idioma: español, catalán o inglés.",
                        "Tema clar o fosc i idioma: castellà, català o anglès.",
                        "Light or dark theme and language: Spanish, Catalan or English.")
                ),
                GuideRow(
                    Icons.Default.Person,
                    tr("Mi cuenta & sincronización", "El meu compte i sincronització", "My account & sync"),
                    tr("Tus datos se guardan en tu cuenta y los recuperas en otro móvil.",
                        "Les teves dades es guarden al teu compte i les recuperes en un altre mòbil.",
                        "Your data is saved to your account and follows you to another phone.")
                ),
                GuideRow(
                    Icons.Default.Info,
                    tr("Esta guía, cuando quieras", "Aquesta guia, quan vulguis", "This guide, any time"),
                    tr("La encuentras en Ajustes → Guía de inicio.",
                        "La trobes a Configuració → Guia d'inici.",
                        "Find it in Settings → Quick start guide.")
                )
            )
        )
    )
}
