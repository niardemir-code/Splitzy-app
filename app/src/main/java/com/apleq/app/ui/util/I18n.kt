package com.apleq.app.ui.util

import com.apleq.app.data.model.BillingPeriod
import java.text.DateFormatSymbols
import java.util.Locale

/**
 * Sistema de Internacionalización (i18n) que detecta automáticamente
 * el idioma configurado en el sistema del teléfono móvil o tablet.
 */
object I18n {

    val isSpanish: Boolean
        get() {
            val lang = Locale.getDefault().language.lowercase()
            return lang.startsWith("es")
        }

    val isCatalan: Boolean
        get() {
            val lang = Locale.getDefault().language.lowercase()
            return lang.startsWith("ca")
        }

    val currentLanguageCode: String
        get() = when {
            isSpanish -> "es"
            isCatalan -> "ca"
            else -> "en"
        }

    // ==========================================
    // GENERAL & COMMON
    // ==========================================
    val appName: String get() = "Apleq"
    val appSubtitle: String get() = when {
        isSpanish -> "Control inteligente de suscripciones"
        isCatalan -> "Control intel·ligent de subscripcions"
        else -> "Smart subscription tracking"
    }

    val cancel: String get() = when {
        isSpanish -> "Cancelar"
        isCatalan -> "Cancel·lar"
        else -> "Cancel"
    }

    val save: String get() = when {
        isSpanish -> "Guardar"
        isCatalan -> "Desar"
        else -> "Save"
    }

    val edit: String get() = when {
        isSpanish -> "Editar"
        isCatalan -> "Editar"
        else -> "Edit"
    }

    val delete: String get() = when {
        isSpanish -> "Eliminar"
        isCatalan -> "Eliminar"
        else -> "Delete"
    }

    val accept: String get() = when {
        isSpanish -> "Aceptar"
        isCatalan -> "D'acord"
        else -> "OK"
    }

    val close: String get() = when {
        isSpanish -> "Cerrar"
        isCatalan -> "Tancar"
        else -> "Close"
    }

    val back: String get() = when {
        isSpanish -> "Volver"
        isCatalan -> "Enrere"
        else -> "Back"
    }

    val copy: String get() = when {
        isSpanish -> "Copiar"
        isCatalan -> "Copiar"
        else -> "Copy"
    }

    val share: String get() = when {
        isSpanish -> "Compartir"
        isCatalan -> "Compartir"
        else -> "Share"
    }

    val optional: String get() = when {
        isSpanish -> "opcional"
        isCatalan -> "opcional"
        else -> "optional"
    }

    val requiredField: String get() = when {
        isSpanish -> "Campo obligatorio"
        isCatalan -> "Camp obligatori"
        else -> "Required field"
    }

    // ==========================================
    // AUTHENTICATION & LOGIN GATE
    // ==========================================
    val tabCreateAccount: String get() = when {
        isSpanish -> "Crear cuenta"
        isCatalan -> "Crear compte"
        else -> "Create account"
    }

    val tabSignIn: String get() = when {
        isSpanish -> "Iniciar sesión"
        isCatalan -> "Iniciar sessió"
        else -> "Sign in"
    }

    val googleSignUp: String get() = when {
        isSpanish -> "Registrarme con Google"
        isCatalan -> "Registrar-me amb Google"
        else -> "Sign up with Google"
    }

    val googleSignIn: String get() = when {
        isSpanish -> "Continuar con Google"
        isCatalan -> "Continuar amb Google"
        else -> "Continue with Google"
    }

    val orWithEmail: String get() = when {
        isSpanish -> "o con correo electrónico"
        isCatalan -> "o amb correu electrònic"
        else -> "or with email"
    }

    val emailLabel: String get() = when {
        isSpanish -> "Correo electrónico"
        isCatalan -> "Correu electrònic"
        else -> "Email address"
    }

    val passwordLabel: String get() = when {
        isSpanish -> "Contraseña"
        isCatalan -> "Contrasenya"
        else -> "Password"
    }

    val passwordCreateLabel: String get() = when {
        isSpanish -> "Crear contraseña (mínimo 6 caracteres)"
        isCatalan -> "Crear contrasenya (mínim 6 caràcters)"
        else -> "Create password (min 6 characters)"
    }

    val btnCreateAccount: String get() = when {
        isSpanish -> "Crear mi Cuenta"
        isCatalan -> "Crear el meu Compte"
        else -> "Create Account"
    }

    val btnSignIn: String get() = when {
        isSpanish -> "Iniciar Sesión"
        isCatalan -> "Iniciar Sessió"
        else -> "Sign In"
    }

    val verifying: String get() = when {
        isSpanish -> "Verificando..."
        isCatalan -> "Verificant..."
        else -> "Verifying..."
    }

    val cloudSyncTitle: String get() = when {
        isSpanish -> "Sincronización en la Nube"
        isCatalan -> "Sincronització al Núvol"
        else -> "Cloud Synchronization"
    }

    val cloudSyncSubtitle: String get() = when {
        isSpanish -> "Tus suscripciones y miembros seguros y accesibles en cualquier dispositivo."
        isCatalan -> "Les teves subscripcions i membres segurs i accessibles en qualsevol dispositiu."
        else -> "Your subscriptions and members secure and accessible on any device."
    }

    val privacyTitle: String get() = when {
        isSpanish -> "Privacidad y Control"
        isCatalan -> "Privadesa i Control"
        else -> "Privacy & Control"
    }

    val privacySubtitle: String get() = when {
        isSpanish -> "Tus datos viajan encriptados y asociados únicamente a tu cuenta."
        isCatalan -> "Les teves dades viatgen encriptades i associades únicament al teu compte."
        else -> "Your data is encrypted and linked only to your account."
    }

    // ==========================================
    // HOME & SEARCH & FILTERS
    // ==========================================
    val mySubscriptions: String get() = when {
        isSpanish -> "Mis Suscripciones"
        isCatalan -> "Les Meves Subscripcions"
        else -> "My Subscriptions"
    }

    val noFilteredSubscriptions: String get() = when {
        isSpanish -> "No se encontraron suscripciones"
        isCatalan -> "No s'han trobat subscripcions"
        else -> "No subscriptions found"
    }

    val noSubscriptionsYet: String get() = when {
        isSpanish -> "Aún no tienes suscripciones"
        isCatalan -> "Encara no tens subscripcions"
        else -> "No subscriptions yet"
    }

    val noSubscriptionsDesc: String get() = when {
        isSpanish -> "Comienza añadiendo una plataforma o servicio compartido."
        isCatalan -> "Comença afegint una plataforma o servei compartit."
        else -> "Start by adding a shared platform or service."
    }

    val addSubscription: String get() = when {
        isSpanish -> "Añadir Suscripción"
        isCatalan -> "Afegir Subscripció"
        else -> "Add Subscription"
    }

    val deleteSubscriptionConfirmTitle: String get() = when {
        isSpanish -> "Eliminar suscripción"
        isCatalan -> "Eliminar subscripció"
        else -> "Delete subscription"
    }

    fun deleteSubscriptionConfirmMessage(platformName: String): String = when {
        isSpanish -> "¿Estás seguro de que quieres eliminar la suscripción a $platformName y todos sus usuarios vinculados? Esta acción no se puede deshacer."
        isCatalan -> "Segur que vols eliminar la subscripció a $platformName i tots els seus usuaris vinculats? Aquesta acció no es pot desfer."
        else -> "Are you sure you want to delete the subscription to $platformName and all associated users? This action cannot be undone."
    }

    val searchPlaceholder: String get() = when {
        isSpanish -> "Buscar suscripción o miembro..."
        isCatalan -> "Cercar subscripció o membre..."
        else -> "Search subscription or member..."
    }

    val filterAll: String get() = when {
        isSpanish -> "Todas"
        isCatalan -> "Totes"
        else -> "All"
    }

    val sortBy: String get() = when {
        isSpanish -> "Ordenar por"
        isCatalan -> "Ordenar per"
        else -> "Sort by"
    }

    val sortName: String get() = when {
        isSpanish -> "Nombre"
        isCatalan -> "Nom"
        else -> "Name"
    }

    val sortCost: String get() = when {
        isSpanish -> "Coste"
        isCatalan -> "Cost"
        else -> "Cost"
    }

    val sortNextPayment: String get() = when {
        isSpanish -> "Próximo cobro"
        isCatalan -> "Proper cobrament"
        else -> "Next billing"
    }

    val sortContribution: String get() = when {
        isSpanish -> "Aporte"
        isCatalan -> "Aportació"
        else -> "Contribution"
    }

    val emptySubscriptionsTitle: String get() = when {
        isSpanish -> "No hay suscripciones registradas"
        isCatalan -> "No hi ha subscripcions registrades"
        else -> "No subscriptions yet"
    }

    val emptySubscriptionsSubtitle: String get() = when {
        isSpanish -> "Pulsa el botón + para añadir tu primera suscripción compartida"
        isCatalan -> "Prem el botó + per afegir la teva primera subscripció compartida"
        else -> "Tap the + button to add your first shared subscription"
    }

    val newSubscription: String get() = when {
        isSpanish -> "Nueva Suscripción"
        isCatalan -> "Nova Subscripció"
        else -> "New Subscription"
    }

    // ==========================================
    // FINANCIAL SUMMARY CARD
    // ==========================================
    val monthlyNetProfit: String get() = when {
        isSpanish -> "GANANCIA NETA MENSUAL"
        isCatalan -> "GUANY NET MENSUAL"
        else -> "MONTHLY NET PROFIT"
    }

    val monthlyNetBalance: String get() = when {
        isSpanish -> "BALANCE NETO MENSUAL"
        isCatalan -> "BALANÇ NET MENSUAL"
        else -> "MONTHLY NET BALANCE"
    }

    val perMonthSuffix: String get() = when {
        isSpanish -> "/mes"
        isCatalan -> "/mes"
        else -> "/mo"
    }

    val costsYou: String get() = when {
        isSpanish -> "Te cuesta a ti"
        isCatalan -> "Et costa a tu"
        else -> "Costs you"
    }

    val monthlyExpense: String get() = when {
        isSpanish -> "Gasto mensual"
        isCatalan -> "Despesa mensual"
        else -> "Monthly expense"
    }

    val contributionsToYou: String get() = when {
        isSpanish -> "Te aportan"
        isCatalan -> "T'aporten"
        else -> "Contributions"
    }

    val monthlyContribution: String get() = when {
        isSpanish -> "Aporte mensual"
        isCatalan -> "Aportació mensual"
        else -> "Monthly contribution"
    }

    val costCoverage: String get() = when {
        isSpanish -> "Cobertura de Costes"
        isCatalan -> "Cobertura de Costos"
        else -> "Cost Coverage"
    }

    val subscriptionsUnit: String get() = when {
        isSpanish -> "Suscripciones"
        isCatalan -> "Subscripcions"
        else -> "Subscriptions"
    }

    val membersUnit: String get() = when {
        isSpanish -> "Miembros activos"
        isCatalan -> "Membres actius"
        else -> "Active members"
    }

    val totalCostLabel: String get() = when {
        isSpanish -> "Coste Total"
        isCatalan -> "Cost Total"
        else -> "Total Cost"
    }

    val memberContributionsLabel: String get() = when {
        isSpanish -> "Aportes Miembros"
        isCatalan -> "Aportacions Membres"
        else -> "Member Shares"
    }

    val netCostLabel: String get() = when {
        isSpanish -> "Tu Coste Neto"
        isCatalan -> "El Teu Cost Net"
        else -> "Your Net Cost"
    }

    val monthlySuffix: String get() = when {
        isSpanish -> "mensual"
        isCatalan -> "mensual"
        else -> "monthly"
    }

    fun savePercentageText(percent: Int): String = when {
        isSpanish -> "Ahorras un $percent% del coste total"
        isCatalan -> "Estalvies un $percent% del cost total"
        else -> "You save $percent% of total cost"
    }

    // ==========================================
    // SUBSCRIPTION CARD & STATUS
    // ==========================================
    val addUser: String get() = when {
        isSpanish -> "Añadir Usuario"
        isCatalan -> "Afegir Usuari"
        else -> "Add User"
    }

    val editSubscription: String get() = when {
        isSpanish -> "Editar suscripción"
        isCatalan -> "Editar subscripció"
        else -> "Edit subscription"
    }

    val deleteSubscription: String get() = when {
        isSpanish -> "Eliminar suscripción"
        isCatalan -> "Eliminar subscripció"
        else -> "Delete subscription"
    }

    val mainUserOwner: String get() = when {
        isSpanish -> "Titular"
        isCatalan -> "Titular"
        else -> "Owner"
    }

    val renewalDate: String get() = when {
        isSpanish -> "Renovación"
        isCatalan -> "Renovació"
        else -> "Renewal"
    }

    val ownCost: String get() = when {
        isSpanish -> "Coste propio"
        isCatalan -> "Cost propi"
        else -> "Own cost"
    }

    val contributeLabel: String get() = when {
        isSpanish -> "Aportan"
        isCatalan -> "Aporten"
        else -> "Contribute"
    }

    val netProfit: String get() = when {
        isSpanish -> "Ganancia neta"
        isCatalan -> "Guany net"
        else -> "Net profit"
    }

    val netBalance: String get() = when {
        isSpanish -> "Balance neto"
        isCatalan -> "Balanç net"
        else -> "Net balance"
    }

    val currentUsers: String get() = when {
        isSpanish -> "Usuarios en este momento"
        isCatalan -> "Usuaris en aquest moment"
        else -> "Current users"
    }

    val noMembersInSub: String get() = when {
        isSpanish -> "Aún no has añadido miembros a esta suscripción."
        isCatalan -> "Encara no has afegit membres a aquesta subscripció."
        else -> "You haven't added members to this subscription yet."
    }

    val viewDetail: String get() = when {
        isSpanish -> "Ver Detalle"
        isCatalan -> "Veure Detall"
        else -> "View Detail"
    }

    // ==========================================
    // SUBSCRIPTION CARD & STATUS
    // ==========================================
    val yourShare: String get() = when {
        isSpanish -> "Tu parte:"
        isCatalan -> "La teva part:"
        else -> "Your share:"
    }

    val membersLabel: String get() = when {
        isSpanish -> "Miembros"
        isCatalan -> "Membres"
        else -> "Members"
    }

    val theyContribute: String get() = when {
        isSpanish -> "Aportan:"
        isCatalan -> "Aporten:"
        else -> "Contribute:"
    }

    val paused: String get() = when {
        isSpanish -> "Pausado"
        isCatalan -> "Pausat"
        else -> "Paused"
    }

    fun formatDaysRemaining(days: Int): String {
        return when {
            days == 0 -> when {
                isSpanish -> "¡Hoy!"
                isCatalan -> "Avui!"
                else -> "Today!"
            }
            days == 1 -> when {
                isSpanish -> "Mañana"
                isCatalan -> "Demà"
                else -> "Tomorrow"
            }
            days in 2..30 -> when {
                isSpanish -> "En $days días"
                isCatalan -> "En $days dies"
                else -> "In $days days"
            }
            days < 0 -> when {
                isSpanish -> "Vencido hace ${-days} d"
                isCatalan -> "Vençut fa ${-days} d"
                else -> "Overdue by ${-days} d"
            }
            else -> when {
                isSpanish -> "En $days días"
                isCatalan -> "En $days dies"
                else -> "In $days days"
            }
        }
    }

    fun formatMembersCount(count: Int, totalSlots: Int = 0): String {
        val base = when {
            count == 1 -> if (isSpanish) "1 miembro" else if (isCatalan) "1 membre" else "1 member"
            else -> if (isSpanish) "$count miembros" else if (isCatalan) "$count membres" else "$count members"
        }
        return if (totalSlots > 0) "$base / $totalSlots" else base
    }

    // ==========================================
    // BILLING PERIODS & SCHEDULE
    // ==========================================
    fun getBillingPeriodLabel(period: BillingPeriod): String = when (period) {
        BillingPeriod.MONTHLY -> when {
            isSpanish -> "Mensual"
            isCatalan -> "Mensual"
            else -> "Monthly"
        }
        BillingPeriod.QUARTERLY -> when {
            isSpanish -> "Trimestral"
            isCatalan -> "Trimestral"
            else -> "Quarterly"
        }
        BillingPeriod.SEMI_ANNUAL -> when {
            isSpanish -> "Semestral"
            isCatalan -> "Semestral"
            else -> "Semi-annual"
        }
        BillingPeriod.YEARLY -> when {
            isSpanish -> "Anual"
            isCatalan -> "Anual"
            else -> "Annual"
        }
    }

    fun getBillingPeriodSuffix(period: BillingPeriod): String = when (period) {
        BillingPeriod.MONTHLY -> when {
            isSpanish -> "/mes"
            isCatalan -> "/mes"
            else -> "/mo"
        }
        BillingPeriod.QUARTERLY -> when {
            isSpanish -> "/trimestre"
            isCatalan -> "/trimestre"
            else -> "/quarter"
        }
        BillingPeriod.SEMI_ANNUAL -> when {
            isSpanish -> "/semestre"
            isCatalan -> "/semestre"
            else -> "/semester"
        }
        BillingPeriod.YEARLY -> when {
            isSpanish -> "/año"
            isCatalan -> "/any"
            else -> "/yr"
        }
    }

    fun getMonthName(month: Int): String {
        val clamped = month.coerceIn(1, 12)
        return try {
            val symbols = DateFormatSymbols(Locale.getDefault())
            symbols.months[clamped - 1].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } catch (_: Exception) {
            BillingPeriod.getMonthName(clamped)
        }
    }

    fun getMonthShort(month: Int): String {
        val clamped = month.coerceIn(1, 12)
        return try {
            val symbols = DateFormatSymbols(Locale.getDefault())
            symbols.shortMonths[clamped - 1].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        } catch (_: Exception) {
            BillingPeriod.getMonthShort(clamped)
        }
    }

    fun formatSchedule(day: Int, month: Int, period: BillingPeriod): String {
        val validDay = day.coerceIn(1, 31)
        val mName = getMonthName(month)
        val pLabel = getBillingPeriodLabel(period)
        return when (period) {
            BillingPeriod.MONTHLY -> when {
                isSpanish -> "Día $validDay de cada mes"
                isCatalan -> "Dia $validDay de cada mes"
                else -> "Day $validDay of each month"
            }
            BillingPeriod.YEARLY -> when {
                isSpanish -> "Día $validDay de $mName ($pLabel)"
                isCatalan -> "Dia $validDay de $mName ($pLabel)"
                else -> "$mName $validDay ($pLabel)"
            }
            BillingPeriod.SEMI_ANNUAL -> {
                val secondMonth = ((month - 1 + 6) % 12) + 1
                val secondMonthName = getMonthName(secondMonth)
                when {
                    isSpanish -> "Día $validDay de $mName y $secondMonthName ($pLabel)"
                    isCatalan -> "Dia $validDay de $mName i $secondMonthName ($pLabel)"
                    else -> "$mName & $secondMonthName $validDay ($pLabel)"
                }
            }
            BillingPeriod.QUARTERLY -> when {
                isSpanish -> "Día $validDay de $mName (Ciclo cada 3 meses)"
                isCatalan -> "Dia $validDay de $mName (Cicle cada 3 mesos)"
                else -> "$mName $validDay (Every 3 months)"
            }
        }
    }

    // ==========================================
    // SUBSCRIPTION DETAIL SHEET
    // ==========================================
    val subscriptionDetailTitle: String get() = when {
        isSpanish -> "Detalle de Suscripción"
        isCatalan -> "Detall de Subscripció"
        else -> "Subscription Details"
    }

    val billingDayLabel: String get() = when {
        isSpanish -> "Día de cobro"
        isCatalan -> "Dia de cobrament"
        else -> "Billing day"
    }

    val slotsLabel: String get() = when {
        isSpanish -> "Plazas / Slots"
        isCatalan -> "Places / Slots"
        else -> "Slots / Seats"
    }

    val notesLabel: String get() = when {
        isSpanish -> "Notas"
        isCatalan -> "Notes"
        else -> "Notes"
    }

    val membersSectionTitle: String get() = when {
        isSpanish -> "Miembros del Grupo"
        isCatalan -> "Membres del Grup"
        else -> "Group Members"
    }

    val addMemberButton: String get() = when {
        isSpanish -> "Añadir Usuario"
        isCatalan -> "Afegir Usuari"
        else -> "Add User"
    }

    val noMembersYet: String get() = when {
        isSpanish -> "Aún no hay miembros en esta suscripción"
        isCatalan -> "Encara no hi ha membres en aquesta subscripció"
        else -> "No members in this subscription yet"
    }

    val deleteSubConfirmTitle: String get() = when {
        isSpanish -> "¿Eliminar suscripción?"
        isCatalan -> "Eliminar subscripció?"
        else -> "Delete subscription?"
    }

    val deleteSubConfirmMsg: String get() = when {
        isSpanish -> "Esta acción eliminará la suscripción y todos los miembros asociados. No se puede deshacer."
        isCatalan -> "Aquesta acció eliminarà la subscripció i tots els membres associats. No es pot desfer."
        else -> "This action will delete the subscription and all associated members. It cannot be undone."
    }

    // ==========================================
    // ADD / EDIT SUBSCRIPTION DIALOG
    // ==========================================
    val addSubTitle: String get() = when {
        isSpanish -> "Nueva Suscripción"
        isCatalan -> "Nova Subscripció"
        else -> "New Subscription"
    }

    val editSubTitle: String get() = when {
        isSpanish -> "Editar Suscripción"
        isCatalan -> "Editar Subscripció"
        else -> "Edit Subscription"
    }

    val platformNameLabel: String get() = when {
        isSpanish -> "Nombre de la plataforma *"
        isCatalan -> "Nom de la plataforma *"
        else -> "Platform name *"
    }

    val customPlanLabel: String get() = when {
        isSpanish -> "Nombre del plan (ej. Premium 4K)"
        isCatalan -> "Nom del pla (ex. Premium 4K)"
        else -> "Plan name (e.g. Premium 4K)"
    }

    val priceLabel: String get() = when {
        isSpanish -> "Precio *"
        isCatalan -> "Preu *"
        else -> "Price *"
    }

    val currencyLabel: String get() = when {
        isSpanish -> "Moneda"
        isCatalan -> "Moneda"
        else -> "Currency"
    }

    val billingPeriodTitle: String get() = when {
        isSpanish -> "Periodo de facturación"
        isCatalan -> "Període de facturació"
        else -> "Billing cycle"
    }

    val billingDayOfMonth: String get() = when {
        isSpanish -> "Día de cobro del mes (1 - 31)"
        isCatalan -> "Dia de cobrament del mes (1 - 31)"
        else -> "Billing day of month (1 - 31)"
    }

    val billingMonthAnnual: String get() = when {
        isSpanish -> "Mes de inicio/renovación"
        isCatalan -> "Mes d'inici/renovació"
        else -> "Renewal month"
    }

    val totalSlotsLabel: String get() = when {
        isSpanish -> "Total de plazas / slots (incluyéndote)"
        isCatalan -> "Total de places / slots (incloent-te)"
        else -> "Total slots / seats (including you)"
    }

    val categoryLabel: String get() = when {
        isSpanish -> "Categoría"
        isCatalan -> "Categoria"
        else -> "Category"
    }

    val iconColorLabel: String get() = when {
        isSpanish -> "Icono y Color"
        isCatalan -> "Icona i Color"
        else -> "Icon & Color"
    }

    val selectFromGallery: String get() = when {
        isSpanish -> "Subir de galería"
        isCatalan -> "Pujar de la galeria"
        else -> "Upload from gallery"
    }

    val optionalNotes: String get() = when {
        isSpanish -> "Notas adicionales (opcional)"
        isCatalan -> "Notes addicionals (opcional)"
        else -> "Additional notes (optional)"
    }

    // ==========================================
    // ADD / EDIT MEMBER DIALOG
    // ==========================================
    val addMemberTitle: String get() = when {
        isSpanish -> "Añadir Usuario"
        isCatalan -> "Afegir Usuari"
        else -> "Add User"
    }

    val editMemberTitle: String get() = when {
        isSpanish -> "Editar Usuario"
        isCatalan -> "Editar Usuari"
        else -> "Edit User"
    }

    val userNameLabel: String get() = when {
        isSpanish -> "Nombre del usuario *"
        isCatalan -> "Nom de l'usuari *"
        else -> "User name *"
    }

    val userContributionLabel: String get() = when {
        isSpanish -> "Aporte o cuota *"
        isCatalan -> "Aportació o quota *"
        else -> "Contribution / fee *"
    }

    val sharingPlatformSelectLabel: String get() = when {
        isSpanish -> "Plataforma de compartición"
        isCatalan -> "Plataforma de compartició"
        else -> "Sharing platform"
    }

    val userPhoneLabel: String get() = when {
        isSpanish -> "Teléfono / WhatsApp (opcional)"
        isCatalan -> "Telèfon / WhatsApp (opcional)"
        else -> "Phone / WhatsApp (optional)"
    }

    val userEmailLabel: String get() = when {
        isSpanish -> "Correo electrónico (opcional)"
        isCatalan -> "Correu electrònic (opcional)"
        else -> "Email address (optional)"
    }

    val memberStatusLabel: String get() = when {
        isSpanish -> "Estado del usuario"
        isCatalan -> "Estat de l'usuari"
        else -> "User status"
    }

    val statusActive: String get() = when {
        isSpanish -> "Activo"
        isCatalan -> "Actiu"
        else -> "Active"
    }

    val statusPaused: String get() = when {
        isSpanish -> "Pausado"
        isCatalan -> "Pausat"
        else -> "Paused"
    }

    // ==========================================
    // REMINDER & NOTIFICATION
    // ==========================================
    val paymentReminderTitle: String get() = when {
        isSpanish -> "Recordatorio de Pago"
        isCatalan -> "Recordatori de Pagament"
        else -> "Payment Reminder"
    }

    val reminderMsgReady: String get() = when {
        isSpanish -> "Mensaje listo para enviar:"
        isCatalan -> "Missatge a punt per enviar:"
        else -> "Ready to send message:"
    }

    val sendWhatsApp: String get() = when {
        isSpanish -> "WhatsApp"
        isCatalan -> "WhatsApp"
        else -> "WhatsApp"
    }

    val messageCopied: String get() = when {
        isSpanish -> "Mensaje copiado al portapapeles"
        isCatalan -> "Missatge copiat al porta-retalls"
        else -> "Message copied to clipboard"
    }

    fun buildReminderMessage(
        memberName: String,
        periodLabel: String,
        amount: String,
        currencySymbol: String,
        platformName: String,
        planName: String
    ): String {
        val planText = if (planName.isNotBlank()) " ($planName)" else ""
        return when {
            isSpanish -> "¡Hola $memberName! Te recuerdo la cuota $periodLabel de $amount $currencySymbol para la suscripción compartida de $platformName$planText. ¡Muchas gracias!"
            isCatalan -> "Hola $memberName! Et recordo la quota $periodLabel de $amount $currencySymbol per a la subscripció compartida de $platformName$planText. Moltes gràcies!"
            else -> "Hi $memberName! Friendly reminder for your $periodLabel payment of $amount $currencySymbol for the shared $platformName subscription$planText. Thank you!"
        }
    }

    // ==========================================
    // SETTINGS SCREEN
    // ==========================================
    val settingsTitle: String get() = when {
        isSpanish -> "Configuración"
        isCatalan -> "Configuració"
        else -> "Settings"
    }

    val appearanceSection: String get() = when {
        isSpanish -> "Apariencia"
        isCatalan -> "Aparició"
        else -> "Appearance"
    }

    val appearanceSubtitle: String get() = when {
        isSpanish -> "Elige el tema visual de la aplicación"
        isCatalan -> "Tria el tema visual de l'aplicació"
        else -> "Choose the app visual theme"
    }

    val themeLight: String get() = when {
        isSpanish -> "Claro"
        isCatalan -> "Clar"
        else -> "Light"
    }

    val themeLightDesc: String get() = when {
        isSpanish -> "Fondo luminoso con contraste optimizado"
        isCatalan -> "Fons lluminós amb contrast optimitzat"
        else -> "Bright background with optimized contrast"
    }

    val themeDark: String get() = when {
        isSpanish -> "Oscuro"
        isCatalan -> "Fosc"
        else -> "Dark"
    }

    val themeDarkDesc: String get() = when {
        isSpanish -> "Fondo oscuro de alto contraste y ahorro de energía"
        isCatalan -> "Fons fosc d'alt contrast i estalvi d'energia"
        else -> "Dark background with energy saving"
    }

    val themeSystem: String get() = when {
        isSpanish -> "Usar configuración del sistema"
        isCatalan -> "Utilitzar configuració del sistema"
        else -> "Use system settings"
    }

    val themeSystemDesc: String get() = when {
        isSpanish -> "Se adapta automáticamente al tema de tu dispositivo"
        isCatalan -> "S'adapta automàticament al tema del teu dispositiu"
        else -> "Automatically matches your device theme"
    }

    val languageSection: String get() = when {
        isSpanish -> "Idioma"
        isCatalan -> "Idioma"
        else -> "Language"
    }

    val languageAutomaticTitle: String get() = when {
        isSpanish -> "Automático (Idioma del sistema)"
        isCatalan -> "Automàtic (Idioma del sistema)"
        else -> "Automatic (System language)"
    }

    val languageAutomaticDesc: String
        get() {
            val l = Locale.getDefault().displayLanguage.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
            return when {
                isSpanish -> "Sincronizado con el sistema de tu móvil/tablet ($l)"
                isCatalan -> "Sincronitzat amb el sistema del teu mòbil/tauleta ($l)"
                else -> "Synchronized with your phone/tablet system ($l)"
            }
        }

    val platformsSectionTitle: String get() = when {
        isSpanish -> "Plataformas de compartición"
        isCatalan -> "Plataformes de compartició"
        else -> "Sharing platforms"
    }

    fun platformsCountText(count: Int): String = when {
        isSpanish -> "$count plataformas configuradas"
        isCatalan -> "$count plataformes configurades"
        else -> "$count configured platforms"
    }

    val addPlatformBtn: String get() = when {
        isSpanish -> "Añadir"
        isCatalan -> "Afegir"
        else -> "Add"
    }

    val cloudBackupSection: String get() = when {
        isSpanish -> "Cuenta y Copia de Seguridad"
        isCatalan -> "Compte i Còpia de Seguretat"
        else -> "Account & Backup"
    }

    val signOutBtn: String get() = when {
        isSpanish -> "Cerrar sesión"
        isCatalan -> "Tancar sessió"
        else -> "Sign out"
    }

    // ==========================================
    // CATEGORIES
    // ==========================================
    fun getCategoryName(category: String): String {
        return when (category.trim().lowercase()) {
            "streaming" -> "Streaming"
            "música", "musica", "music" -> when {
                isSpanish -> "Música"
                isCatalan -> "Música"
                else -> "Music"
            }
            "software" -> "Software"
            "gaming", "juegos", "videojuegos" -> when {
                isSpanish -> "Juegos"
                isCatalan -> "Jocs"
                else -> "Gaming"
            }
            "productividad", "productivity" -> when {
                isSpanish -> "Productividad"
                isCatalan -> "Productivitat"
                else -> "Productivity"
            }
            "fitness", "gimnasio" -> when {
                isSpanish -> "Fitness / Gimnasio"
                isCatalan -> "Fitness / Gimnàs"
                else -> "Fitness / Gym"
            }
            "otros", "other", "others" -> when {
                isSpanish -> "Otros"
                isCatalan -> "Altres"
                else -> "Others"
            }
            else -> category
        }
    }
}
