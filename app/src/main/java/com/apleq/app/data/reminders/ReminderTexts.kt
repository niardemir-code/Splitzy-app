package com.apleq.app.data.reminders

import com.apleq.app.ui.util.I18n

/** Textos de los avisos y de su sección de Ajustes (español, catalán, inglés). */
object ReminderTexts {
    private fun tr(es: String, ca: String, en: String): String = when {
        I18n.isSpanish -> es
        I18n.isCatalan -> ca
        else -> en
    }

    val channelName get() = tr("Avisos de cobro", "Avisos de cobrament", "Payment reminders")
    val channelDescription get() = tr(
        "Recordatorios de los cobros de tus suscripciones",
        "Recordatoris dels cobraments de les teves subscripcions",
        "Reminders for your subscription payments"
    )

    /** Cuerpo de la notificación; [days] = días que faltan para el cobro (0 = hoy). */
    fun body(item: ReminderItem, days: Int): String {
        val extra = if (item.amount.isNotBlank()) " (${item.amount})" else ""
        return when (item.kind) {
            "MEMBER" -> when {
                days <= 0 -> tr("Hoy cobras a ${item.who}$extra", "Avui cobres a ${item.who}$extra", "Today you collect from ${item.who}$extra")
                days == 1 -> tr("Mañana cobras a ${item.who}$extra", "Demà cobres a ${item.who}$extra", "Tomorrow you collect from ${item.who}$extra")
                else -> tr("En $days días cobras a ${item.who}$extra", "D'aquí a $days dies cobres a ${item.who}$extra", "In $days days you collect from ${item.who}$extra")
            }
            "CLIENT" -> when {
                days <= 0 -> tr("Hoy toca pagar tu cuota$extra", "Avui toca pagar la teva quota$extra", "Today your share is due$extra")
                days == 1 -> tr("Mañana toca pagar tu cuota$extra", "Demà toca pagar la teva quota$extra", "Tomorrow your share is due$extra")
                else -> tr("En $days días toca pagar tu cuota$extra", "D'aquí a $days dies toca pagar la teva quota$extra", "In $days days your share is due$extra")
            }
            else -> when {
                days <= 0 -> tr("Hoy se cobra esta suscripción$extra", "Avui es cobra aquesta subscripció$extra", "This subscription is charged today$extra")
                days == 1 -> tr("Mañana se cobra esta suscripción$extra", "Demà es cobra aquesta subscripció$extra", "This subscription is charged tomorrow$extra")
                else -> tr("En $days días se cobra esta suscripción$extra", "D'aquí a $days dies es cobra aquesta subscripció$extra", "This subscription is charged in $days days$extra")
            }
        }
    }

    // ---- Sección de Ajustes ----
    val sectionTitle get() = tr("Avisos en el móvil", "Avisos al mòbil", "Phone notifications")
    val sectionSubtitle get() = tr(
        "Te avisamos de los cobros aunque la app esté cerrada",
        "T'avisem dels cobraments encara que l'app estigui tancada",
        "We remind you of payments even when the app is closed"
    )
    val enabledLabel get() = tr("Avisos de cobro en el móvil", "Avisos de cobrament al mòbil", "Payment notifications on this phone")
    val timeLabel get() = tr("Hora de los avisos", "Hora dels avisos", "Reminder time")
    val timeHelp get() = tr(
        "Todos los avisos saltan a esta hora, con la antelación que hayas elegido en cada suscripción o miembro.",
        "Tots els avisos salten a aquesta hora, amb l'antelació que hagis triat a cada subscripció o membre.",
        "All reminders go off at this time, with the lead time you set on each subscription or member."
    )
    val change get() = tr("Cambiar", "Canvia", "Change")
    val accept get() = tr("Aceptar", "Accepta", "OK")
    val cancel get() = tr("Cancelar", "Cancel·la", "Cancel")
    val notificationsOff get() = tr(
        "Las notificaciones de Apleq están desactivadas en el sistema: no recibirás los avisos.",
        "Les notificacions d'Apleq estan desactivades al sistema: no rebràs els avisos.",
        "Apleq notifications are turned off in the system: you won't receive reminders."
    )
    val notificationsActivate get() = tr("Activar notificaciones", "Activa les notificacions", "Turn on notifications")
    val notificationsOn get() = tr("Notificaciones del sistema activadas", "Notificacions del sistema activades", "System notifications are on")

    val batteryOk get() = tr(
        "Sin restricciones de batería: los avisos llegarán con la app cerrada.",
        "Sense restriccions de bateria: els avisos arribaran amb l'app tancada.",
        "No battery restrictions: reminders will arrive with the app closed."
    )
    val batteryBad get() = tr(
        "El sistema puede cerrar Apleq en segundo plano y hacer que algún aviso no llegue.",
        "El sistema pot tancar Apleq en segon pla i fer que algun avís no arribi.",
        "The system may stop Apleq in the background and some reminders may not arrive."
    )
    val batteryRemove get() = tr("Quitar restricciones", "Treu les restriccions", "Remove restrictions")
    val autostartHint get() = tr(
        "En Xiaomi, Redmi y POCO hay que activar también el «Autoarranque» de Apleq; si no, los avisos se pierden al reiniciar el móvil.",
        "A Xiaomi, Redmi i POCO cal activar també l'«Inici automàtic» d'Apleq; si no, els avisos es perden en reiniciar el mòbil.",
        "On Xiaomi, Redmi and POCO you also need to turn on Apleq's \"Autostart\"; otherwise reminders are lost when the phone restarts."
    )
    val autostartButton get() = tr("Abrir Autoarranque", "Obre l'Inici automàtic", "Open Autostart")
    val moreInfo get() = tr("Más información", "Més informació", "More info")
    val later get() = tr("Ahora no", "Ara no", "Not now")
    val close get() = tr("Cerrar", "Tanca", "Close")

    val batteryPromptTitle get() = tr(
        "Para que los avisos lleguen siempre",
        "Perquè els avisos arribin sempre",
        "So reminders always arrive"
    )
    val batteryPromptText get() = tr(
        "Algunos móviles cierran las apps en segundo plano para ahorrar batería. Si quitas la restricción a Apleq, los avisos de cobro llegarán aunque la app esté cerrada.",
        "Alguns mòbils tanquen les apps en segon pla per estalviar bateria. Si treus la restricció a Apleq, els avisos de cobrament arribaran encara que l'app estigui tancada.",
        "Some phones close apps in the background to save battery. If you remove the restriction for Apleq, payment reminders will arrive even when the app is closed."
    )
    val batteryInfoTitle get() = tr("Ahorro de batería por marca", "Estalvi de bateria per marca", "Battery saving by brand")
    val batteryInfoIntro get() = tr(
        "Además del ajuste de Android, algunas marcas tienen su propio ahorro de batería. Los nombres de los menús pueden variar según el modelo:",
        "A més de l'ajust d'Android, algunes marques tenen el seu propi estalvi de bateria. Els noms dels menús poden variar segons el model:",
        "Besides the Android setting, some brands have their own battery saver. Menu names may vary by model:"
    )
    val batteryXiaomi get() = tr(
        "Ajustes → Aplicaciones → Apleq → Ahorro de batería → «Sin restricciones», y activa «Inicio automático».",
        "Configuració → Aplicacions → Apleq → Estalvi de bateria → «Sense restriccions», i activa «Inici automàtic».",
        "Settings → Apps → Apleq → Battery saver → \"No restrictions\", and turn on \"Autostart\"."
    )
    val batteryOppo get() = tr(
        "Ajustes → Batería → Apleq → permitir actividad en segundo plano y inicio automático.",
        "Configuració → Bateria → Apleq → permet activitat en segon pla i inici automàtic.",
        "Settings → Battery → Apleq → allow background activity and autostart."
    )
    val batterySamsung get() = tr(
        "Ajustes → Aplicaciones → Apleq → Batería → «Sin restricciones».",
        "Configuració → Aplicacions → Apleq → Bateria → «Sense restriccions».",
        "Settings → Apps → Apleq → Battery → \"Unrestricted\"."
    )
    val batteryHuawei get() = tr(
        "Ajustes → Batería → Inicio de aplicaciones → Apleq → «Gestionar manualmente» y activa todo.",
        "Configuració → Bateria → Inici d'aplicacions → Apleq → «Gestiona manualment» i activa-ho tot.",
        "Settings → Battery → App launch → Apleq → \"Manage manually\" and turn everything on."
    )
    val batteryVivo get() = tr(
        "Ajustes → Batería → Consumo en segundo plano → Apleq → «Permitir».",
        "Configuració → Bateria → Consum en segon pla → Apleq → «Permet».",
        "Settings → Battery → Background power use → Apleq → \"Allow\"."
    )
    val batteryGuide get() = tr("Ver guía completa (dontkillmyapp.com)", "Veure la guia completa (dontkillmyapp.com)", "See the full guide (dontkillmyapp.com)")
}
