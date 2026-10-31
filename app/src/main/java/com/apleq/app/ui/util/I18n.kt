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

    // ==========================================
    // DEBT & PAYMENT CYCLES (manager)
    // ==========================================
    fun debtSinceOneCycle(date: String): String = when {
        isSpanish -> "Debe 1 cuota desde el $date"
        isCatalan -> "Deu 1 quota des del $date"
        else -> "Owes 1 installment since $date"
    }

    fun debtSinceMultipleCycles(count: Int, date: String): String = when {
        isSpanish -> "Debe $count cuotas desde el $date"
        isCatalan -> "Deu $count quotes des del $date"
        else -> "Owes $count installments since $date"
    }

    val subscriptionFallbackName: String get() = when {
        isSpanish -> "Suscripción"
        isCatalan -> "Subscripció"
        else -> "Subscription"
    }

    val coSubscriberFallback: String get() = when {
        isSpanish -> "Co-suscriptor"
        isCatalan -> "Co-subscriptor"
        else -> "Co-subscriber"
    }

    val paymentPendingShort: String get() = when {
        isSpanish -> "Pago pendiente"
        isCatalan -> "Pagament pendent"
        else -> "Payment pending"
    }

    fun paymentsPendingCount(count: Int): String = when {
        isSpanish -> "$count pagos pendientes"
        isCatalan -> "$count pagaments pendents"
        else -> "$count payments pending"
    }

    val overdueYesterday: String get() = when {
        isSpanish -> "Vencido ayer"
        isCatalan -> "Vençut ahir"
        else -> "Overdue yesterday"
    }

    fun overdueDays(days: Int): String = when {
        isSpanish -> "Vencido hace $days días"
        isCatalan -> "Vençut fa $days dies"
        else -> "Overdue by $days days"
    }

    val dueTodayExclaim: String get() = when {
        isSpanish -> "¡Vence hoy!"
        isCatalan -> "Venç avui!"
        else -> "Due today!"
    }

    val dueTomorrowShort: String get() = when {
        isSpanish -> "Vence mañana"
        isCatalan -> "Venç demà"
        else -> "Due tomorrow"
    }

    fun dueInDaysShort(days: Int): String = when {
        isSpanish -> "Vence en $days días"
        isCatalan -> "Venç en $days dies"
        else -> "Due in $days days"
    }

    val alarmSetSameDay: String get() = when {
        isSpanish -> "Alarma configurada: El mismo día"
        isCatalan -> "Alarma configurada: El mateix dia"
        else -> "Alarm set: same day"
    }

    fun alarmSetLeadTime(value: Int, unitLabel: String): String = when {
        isSpanish -> "Alarma configurada: $value $unitLabel antes"
        isCatalan -> "Alarma configurada: $value $unitLabel abans"
        else -> "Alarm set: $value $unitLabel before"
    }

    fun timeUnitLabel(unit: String, value: Int): String {
        val plural = value != 1
        return when (unit) {
            "weeks" -> when {
                isSpanish -> if (plural) "semanas" else "semana"
                isCatalan -> if (plural) "setmanes" else "setmana"
                else -> if (plural) "weeks" else "week"
            }
            "months" -> when {
                isSpanish -> if (plural) "meses" else "mes"
                isCatalan -> if (plural) "mesos" else "mes"
                else -> if (plural) "months" else "month"
            }
            else -> when {
                isSpanish -> if (plural) "días" else "día"
                isCatalan -> if (plural) "dies" else "dia"
                else -> if (plural) "days" else "day"
            }
        }
    }

    // ==========================================
    // DEBT & PAYMENT (client's own reminders)
    // ==========================================
    val youHavePaymentDue: String get() = when {
        isSpanish -> "Tienes un pago pendiente"
        isCatalan -> "Tens un pagament pendent"
        else -> "You have a payment due"
    }

    fun youHavePaymentsDue(count: Int): String = when {
        isSpanish -> "Tienes $count pagos pendientes"
        isCatalan -> "Tens $count pagaments pendents"
        else -> "You have $count payments due"
    }

    fun yourPaymentOverdueDays(days: Int): String = when {
        isSpanish -> "Tu pago venció hace $days días"
        isCatalan -> "El teu pagament ha vençut fa $days dies"
        else -> "Your payment was due $days days ago"
    }

    val yourPaymentDueTomorrow: String get() = when {
        isSpanish -> "Tu pago vence mañana"
        isCatalan -> "El teu pagament venç demà"
        else -> "Your payment is due tomorrow"
    }

    fun yourPaymentDueInDays(days: Int): String = when {
        isSpanish -> "Tu pago vence en $days días"
        isCatalan -> "El teu pagament venç en $days dies"
        else -> "Your payment is due in $days days"
    }

    val yourPaymentLabel: String get() = when {
        isSpanish -> "Tu pago"
        isCatalan -> "El teu pagament"
        else -> "Your payment"
    }

    // ==========================================
    // NOTIFICATIONS PANEL
    // ==========================================
    val notificationsTitle: String get() = when {
        isSpanish -> "Notificaciones"
        isCatalan -> "Notificacions"
        else -> "Notifications"
    }

    val markAllRead: String get() = when {
        isSpanish -> "Marcar leídas"
        isCatalan -> "Marcar com llegides"
        else -> "Mark as read"
    }

    val deleteAllNotifications: String get() = when {
        isSpanish -> "Borrar todas"
        isCatalan -> "Esborrar totes"
        else -> "Delete all"
    }

    val noNotificationsPending: String get() = when {
        isSpanish -> "No tienes avisos pendientes."
        isCatalan -> "No tens avisos pendents."
        else -> "You have no pending alerts."
    }

    val dismissNotification: String get() = when {
        isSpanish -> "Descartar aviso"
        isCatalan -> "Descartar avís"
        else -> "Dismiss alert"
    }

    val markAsUnread: String get() = when {
        isSpanish -> "Marcar como no leída"
        isCatalan -> "Marcar com no llegit"
        else -> "Mark as unread"
    }

    val markAsRead: String get() = when {
        isSpanish -> "Marcar como leída"
        isCatalan -> "Marcar com llegit"
        else -> "Mark as read"
    }

    // ==========================================
    // CHAT
    // ==========================================
    val chatBackToList: String get() = when {
        isSpanish -> "Volver a la lista"
        isCatalan -> "Tornar a la llista"
        else -> "Back to list"
    }

    val chatNoMessagesYet: String get() = when {
        isSpanish -> "Aún no hay mensajes. Escribe el primero."
        isCatalan -> "Encara no hi ha missatges. Escriu el primer."
        else -> "No messages yet. Write the first one."
    }

    val chatMessagePlaceholder: String get() = when {
        isSpanish -> "Escribe un mensaje..."
        isCatalan -> "Escriu un missatge..."
        else -> "Type a message..."
    }

    val chatSend: String get() = when {
        isSpanish -> "Enviar"
        isCatalan -> "Enviar"
        else -> "Send"
    }

    fun chatListTitle(subscriptionName: String): String = when {
        isSpanish -> "Chats · $subscriptionName"
        isCatalan -> "Xats · $subscriptionName"
        else -> "Chats · $subscriptionName"
    }

    val chatWithMembers: String get() = when {
        isSpanish -> "Chats con miembros"
        isCatalan -> "Xats amb membres"
        else -> "Chats with members"
    }

    val chatNoLinkedMembers: String get() = when {
        isSpanish -> "Ningún miembro tiene cuenta enlazada todavía."
        isCatalan -> "Cap membre té un compte enllaçat encara."
        else -> "No members have a linked account yet."
    }

    val chatDefaultClientName: String get() = when {
        isSpanish -> "Cliente"
        isCatalan -> "Client"
        else -> "Client"
    }

    val chatUnreadMessage: String get() = when {
        isSpanish -> "Mensaje sin leer"
        isCatalan -> "Missatge sense llegir"
        else -> "Unread message"
    }

    // ==========================================
    // CLIENT ALARM PANEL
    // ==========================================
    val yourAlarmTitle: String get() = when {
        isSpanish -> "Tu alarma"
        isCatalan -> "La teva alarma"
        else -> "Your alarm"
    }

    val remindMeLabel: String get() = when {
        isSpanish -> "Avisarme"
        isCatalan -> "Avisa'm"
        else -> "Remind me"
    }

    val beforeLabel: String get() = when {
        isSpanish -> "antes"
        isCatalan -> "abans"
        else -> "before"
    }

    val noAlarmForThisPayment: String get() = when {
        isSpanish -> "No recibirás avisos de este pago."
        isCatalan -> "No rebràs avisos d'aquest pagament."
        else -> "You won't receive alerts for this payment."
    }

    // ==========================================
    // MESSAGES PANEL (client) & LEAVE GROUP
    // ==========================================
    val messagesWithManagerTitle: String get() = when {
        isSpanish -> "Mensajes con el gestor"
        isCatalan -> "Missatges amb el gestor"
        else -> "Messages with the manager"
    }

    val chatManagerName: String get() = when {
        isSpanish -> "El gestor"
        isCatalan -> "El gestor"
        else -> "The manager"
    }

    val tapToOpenConversation: String get() = when {
        isSpanish -> "Toca para abrir la conversación"
        isCatalan -> "Toca per obrir la conversa"
        else -> "Tap to open the conversation"
    }

    val youHaveNewMessage: String get() = when {
        isSpanish -> "Tienes un mensaje nuevo"
        isCatalan -> "Tens un missatge nou"
        else -> "You have a new message"
    }

    val leaveThisGroup: String get() = when {
        isSpanish -> "Salir de este grupo"
        isCatalan -> "Sortir d'aquest grup"
        else -> "Leave this group"
    }

    fun leaveGroupConfirmTitle(groupName: String): String = when {
        isSpanish -> "¿Salir de $groupName?"
        isCatalan -> "Vols sortir de $groupName?"
        else -> "Leave $groupName?"
    }

    val leaveGroupConfirmMsg: String get() = when {
        isSpanish -> "Dejarás de participar en este grupo."
        isCatalan -> "Deixaràs de participar en aquest grup."
        else -> "You will stop participating in this group."
    }

    val leaveButtonLabel: String get() = when {
        isSpanish -> "Salir"
        isCatalan -> "Sortir"
        else -> "Leave"
    }

    val couldNotLeaveGroup: String get() = when {
        isSpanish -> "No se pudo salir del grupo."
        isCatalan -> "No s'ha pogut sortir del grup."
        else -> "Could not leave the group."
    }

    // ==========================================
    // DEBT PANEL (member edit dialog)
    // ==========================================
    val debtPanelOneCycle: String get() = when {
        isSpanish -> "Debe 1 cuota"
        isCatalan -> "Deu 1 quota"
        else -> "Owes 1 installment"
    }

    fun debtPanelMultipleCycles(count: Int): String = when {
        isSpanish -> "Debe $count cuotas"
        isCatalan -> "Deu $count quotes"
        else -> "Owes $count installments"
    }

    fun debtPanelSinceDate(date: String): String = when {
        isSpanish -> "Desde el $date"
        isCatalan -> "Des del $date"
        else -> "Since $date"
    }

    val debtPanelInstruction: String get() = when {
        isSpanish -> "Apaga el interruptor cuando te haya pagado: la deuda se saldará por completo."
        isCatalan -> "Apaga l'interruptor quan t'hagi pagat: el deute es liquidarà per complet."
        else -> "Turn off the switch once they've paid you: the debt will be fully settled."
    }

    // ==========================================
    // SUBSCRIPTION CARD: unpaid cycles badge
    // ==========================================
    val oneCyclePendingBadge: String get() = when {
        isSpanish -> "1 cuota pendiente"
        isCatalan -> "1 quota pendent"
        else -> "1 payment pending"
    }

    fun cyclesPendingBadge(count: Int): String = when {
        isSpanish -> "$count cuotas pendientes"
        isCatalan -> "$count quotes pendents"
        else -> "$count payments pending"
    }

    // ==========================================
    // PASSWORD REQUIREMENTS
    // ==========================================
    val passwordMinChars: String get() = when {
        isSpanish -> "Mínimo 6 caracteres"
        isCatalan -> "Mínim 6 caràcters"
        else -> "Minimum 6 characters"
    }

    val passwordTooShortWarning: String get() = when {
        isSpanish -> "Demasiado corta: mínimo 6 caracteres"
        isCatalan -> "Massa curta: mínim 6 caràcters"
        else -> "Too short: minimum 6 characters"
    }

    // ==========================================
    // AUTH GATE (login/register screen) & JOIN GROUP DIALOG
    // ==========================================
    val authGateErrorIconDescription: String get() = when {
        isSpanish -> "Error"
        isCatalan -> "Error"
        else -> "Error"
    }

    val continueWithGoogle: String get() = when {
        isSpanish -> "Continuar con Google"
        isCatalan -> "Continua amb Google"
        else -> "Continue with Google"
    }

    val registerWithEmail: String get() = when {
        isSpanish -> "Registrarme con correo"
        isCatalan -> "Registra'm amb correu"
        else -> "Sign up with email"
    }

    val alreadyHaveAccountLoginWithEmail: String get() = when {
        isSpanish -> "¿Ya tienes cuenta? Iniciar sesión con correo"
        isCatalan -> "Ja tens compte? Inicia sessió amb correu"
        else -> "Already have an account? Sign in with email"
    }

    val cloudSyncBenefitTitle: String get() = when {
        isSpanish -> "Sincronización en la Nube"
        isCatalan -> "Sincronització al núvol"
        else -> "Cloud Sync"
    }

    val cloudSyncBenefitSubtitle: String get() = when {
        isSpanish -> "Tus suscripciones y miembros seguros y accesibles en cualquier dispositivo."
        isCatalan -> "Les teves subscripcions i membres, segurs i accessibles des de qualsevol dispositiu."
        else -> "Your subscriptions and members, safe and accessible on any device."
    }

    val privacyBenefitTitle: String get() = when {
        isSpanish -> "Privacidad y Control"
        isCatalan -> "Privadesa i control"
        else -> "Privacy & Control"
    }

    val privacyBenefitSubtitle: String get() = when {
        isSpanish -> "Tus datos viajan encriptados y asociados únicamente a tu cuenta."
        isCatalan -> "Les teves dades viatgen xifrades i associades únicament al teu compte."
        else -> "Your data travels encrypted and is linked only to your account."
    }

    val joinGroupDialogTitle: String get() = when {
        isSpanish -> "Unirse a un grupo"
        isCatalan -> "Afegir-se a un grup"
        else -> "Join a group"
    }

    val joinGroupDialogDescription: String get() = when {
        isSpanish -> "Introduce el código de invitación que te han compartido."
        isCatalan -> "Introdueix el codi d'invitació que t'han compartit."
        else -> "Enter the invitation code that was shared with you."
    }

    val joinGroupCodePlaceholder: String get() = when {
        isSpanish -> "Ej. 7K3-P9M"
        isCatalan -> "Ex. 7K3-P9M"
        else -> "E.g. 7K3-P9M"
    }

    val joinGroupJoiningInProgress: String get() = when {
        isSpanish -> "Uniéndote..."
        isCatalan -> "Afegint-te..."
        else -> "Joining..."
    }

    val joinGroupAction: String get() = when {
        isSpanish -> "Unirse"
        isCatalan -> "Afegir-me"
        else -> "Join"
    }

    // ==========================================
    // APP MENU SHEET & SETTINGS (platforms section)
    // ==========================================
    val menuDefaultSubtitle: String get() = when {
        isSpanish -> "Gestión de suscripciones compartidas"
        isCatalan -> "Gestió de subscripcions compartides"
        else -> "Shared subscription management"
    }

    val menuSettingsSubtitle: String get() = when {
        isSpanish -> "Tema, apariencia y plataformas de compartición"
        isCatalan -> "Tema, aparença i plataformes de compartició"
        else -> "Theme, appearance and sharing platforms"
    }

    val menuBackupTitle: String get() = when {
        isSpanish -> "Copia de Seguridad y Restauración"
        isCatalan -> "Còpia de seguretat i restauració"
        else -> "Backup & Restore"
    }

    val menuBackupSubtitle: String get() = when {
        isSpanish -> "Exportar o importar tus suscripciones en formato JSON"
        isCatalan -> "Exporta o importa les teves subscripcions en format JSON"
        else -> "Export or import your subscriptions as JSON"
    }

    fun menuCloudSyncConnectedAs(email: String): String = when {
        isSpanish -> "Conectado como $email"
        isCatalan -> "Connectat com a $email"
        else -> "Connected as $email"
    }

    val menuCloudSyncSignInPrompt: String get() = when {
        isSpanish -> "Inicia sesión con Google para sincronizar"
        isCatalan -> "Inicia la sessió amb Google per sincronitzar"
        else -> "Sign in with Google to sync"
    }

    val menuFooterNote: String get() = when {
        isSpanish -> "Apleq • Privacidad local y sincronización segura"
        isCatalan -> "Apleq • Privadesa local i sincronització segura"
        else -> "Apleq • Local privacy and secure sync"
    }

    val settingsPlatformsIntro: String get() = when {
        isSpanish -> "Personaliza al 100% las plataformas que usas para compartir gastos. Modifica sus nombres, colores o añade nuevas."
        isCatalan -> "Personalitza al 100% les plataformes que fas servir per compartir despeses. Modifica'n els noms, els colors o afegeix-ne de noves."
        else -> "Fully customize the platforms you use to share costs. Edit their names and colors, or add new ones."
    }

    val settingsNoPlatformsTitle: String get() = when {
        isSpanish -> "No hay plataformas creadas"
        isCatalan -> "No hi ha plataformes creades"
        else -> "No platforms created"
    }

    val settingsNoPlatformsDesc: String get() = when {
        isSpanish -> "Puedes crear una plataforma personalizada o restaurar las 6 plataformas iniciales."
        isCatalan -> "Pots crear una plataforma personalitzada o restaurar les 6 plataformes inicials."
        else -> "You can create a custom platform or restore the 6 default platforms."
    }

    val settingsResetPlatformsButton: String get() = when {
        isSpanish -> "Restablecer plataformas iniciales"
        isCatalan -> "Restableix les plataformes inicials"
        else -> "Reset default platforms"
    }

    val settingsAddCustomPlatform: String get() = when {
        isSpanish -> "Añadir nueva plataforma personalizada"
        isCatalan -> "Afegir una nova plataforma personalitzada"
        else -> "Add new custom platform"
    }

    val settingsRestoreSixPlatforms: String get() = when {
        isSpanish -> "Restaurar las 6 plataformas iniciales"
        isCatalan -> "Restaurar les 6 plataformes inicials"
        else -> "Restore the 6 default platforms"
    }

    fun platformPreviewPill(name: String): String = when {
        isSpanish -> "Vista: $name"
        isCatalan -> "Vista: $name"
        else -> "Preview: $name"
    }

    fun editPlatformDescription(name: String): String = when {
        isSpanish -> "Editar $name"
        isCatalan -> "Editar $name"
        else -> "Edit $name"
    }

    fun deletePlatformDescription(name: String): String = when {
        isSpanish -> "Eliminar $name"
        isCatalan -> "Eliminar $name"
        else -> "Delete $name"
    }

    // ==========================================
    // ADD / EDIT SUBSCRIPTION DIALOG (main form)
    // ==========================================
    val editSubscriptionDialogTitle: String get() = when {
        isSpanish -> "Editar Suscripción"
        isCatalan -> "Editar Subscripció"
        else -> "Edit Subscription"
    }

    val changeIconDescription: String get() = when {
        isSpanish -> "Cambiar icono"
        isCatalan -> "Canviar icona"
        else -> "Change icon"
    }

    val subscriptionNameHeaderPlaceholder: String get() = when {
        isSpanish -> "Nombre del servicio / suscripción"
        isCatalan -> "Nom del servei / subscripció"
        else -> "Service / subscription name"
    }

    val tapToCustomizeIcon: String get() = when {
        isSpanish -> "Toca aquí para personalizar icono o imagen"
        isCatalan -> "Toca aquí per personalitzar la icona o la imatge"
        else -> "Tap here to customize icon or image"
    }

    val subscriptionNameFieldLabel: String get() = when {
        isSpanish -> "Nombre del servicio / suscripción *"
        isCatalan -> "Nom del servei / subscripció *"
        else -> "Service / subscription name *"
    }

    val subscriptionNameFieldPlaceholder: String get() = when {
        isSpanish -> "Ej: Netflix, Spotify, ChatGPT, Gimnasio, Canal+..."
        isCatalan -> "Ex: Netflix, Spotify, ChatGPT, Gimnàs, Canal+..."
        else -> "E.g. Netflix, Spotify, ChatGPT, Gym, Canal+..."
    }

    val subscriptionNameRequired: String get() = when {
        isSpanish -> "El nombre del servicio es obligatorio"
        isCatalan -> "El nom del servei és obligatori"
        else -> "The service name is required"
    }

    val subscriptionCategoryFieldLabel: String get() = when {
        isSpanish -> "Categoría de la suscripción *"
        isCatalan -> "Categoria de la subscripció *"
        else -> "Subscription category *"
    }

    fun getSubscriptionCategoryLabel(category: String): String = when (category) {
        "Streaming" -> "Streaming"
        "Música" -> when {
            isSpanish -> "Música"
            isCatalan -> "Música"
            else -> "Music"
        }
        "Productividad" -> when {
            isSpanish -> "Productividad"
            isCatalan -> "Productivitat"
            else -> "Productivity"
        }
        "Gaming" -> when {
            isSpanish -> "Gaming"
            isCatalan -> "Jocs"
            else -> "Gaming"
        }
        "Educación" -> when {
            isSpanish -> "Educación"
            isCatalan -> "Educació"
            else -> "Education"
        }
        "Salud" -> when {
            isSpanish -> "Salud"
            isCatalan -> "Salut"
            else -> "Health"
        }
        "Estilo de vida" -> when {
            isSpanish -> "Estilo de vida"
            isCatalan -> "Estil de vida"
            else -> "Lifestyle"
        }
        "Seguridad" -> when {
            isSpanish -> "Seguridad"
            isCatalan -> "Seguretat"
            else -> "Security"
        }
        "Finanzas" -> when {
            isSpanish -> "Finanzas"
            isCatalan -> "Finances"
            else -> "Finance"
        }
        "General" -> "General"
        else -> category
    }

    val mainUserFieldLabel: String get() = when {
        isSpanish -> "Titular / Usuario Principal *"
        isCatalan -> "Titular / Usuari Principal *"
        else -> "Account Holder / Main User *"
    }

    val mainUserFieldPlaceholder: String get() = when {
        isSpanish -> "Ej: Carlos (Yo)"
        isCatalan -> "Ex: Carles (Jo)"
        else -> "E.g. Charlie (Me)"
    }

    val mainUserRequired: String get() = when {
        isSpanish -> "El titular es obligatorio"
        isCatalan -> "El titular és obligatori"
        else -> "The account holder is required"
    }

    val priceFieldLabelPlain: String get() = when {
        isSpanish -> "Precio"
        isCatalan -> "Preu"
        else -> "Price"
    }

    fun conversionToEuros(amount: String): String = when {
        isSpanish -> "Conversión a Euros: ≈ $amount"
        isCatalan -> "Conversió a Euros: ≈ $amount"
        else -> "Conversion to Euros: ≈ $amount"
    }

    fun exchangeRateLine(currencyCode: String, rate: String): String = when {
        isSpanish -> "Tipo de cambio: 1 $currencyCode ≈ $rate €"
        isCatalan -> "Tipus de canvi: 1 $currencyCode ≈ $rate €"
        else -> "Exchange rate: 1 $currencyCode ≈ $rate €"
    }

    fun monthlyEquivalentLine(monthlyAmount: String): String = when {
        isSpanish -> "Equivalente mensual: ≈ $monthlyAmount / mes"
        isCatalan -> "Equivalent mensual: ≈ $monthlyAmount / mes"
        else -> "Monthly equivalent: ≈ $monthlyAmount / month"
    }

    val billingFrequencyFieldLabel: String get() = when {
        isSpanish -> "Frecuencia / Periodo de cobro *"
        isCatalan -> "Freqüència / Període de cobrament *"
        else -> "Billing frequency / period *"
    }

    val billingDayFieldLabelMonthly: String get() = when {
        isSpanish -> "Día de cobro (1-31)"
        isCatalan -> "Dia de cobrament (1-31)"
        else -> "Billing day (1-31)"
    }

    val billingDayPlaceholderMonthly: String get() = when {
        isSpanish -> "Día (1-31)"
        isCatalan -> "Dia (1-31)"
        else -> "Day (1-31)"
    }

    val billingDayFieldLabelShort: String get() = when {
        isSpanish -> "Día (1-31)"
        isCatalan -> "Dia (1-31)"
        else -> "Day (1-31)"
    }

    val billingDayPlaceholderShort: String get() = when {
        isSpanish -> "Día"
        isCatalan -> "Dia"
        else -> "Day"
    }

    fun billingMonthFieldLabel(period: BillingPeriod): String = when {
        isSpanish -> "Mes de cobro (${period.label.lowercase()})"
        isCatalan -> "Mes de cobrament (${getBillingPeriodLabel(period).lowercase()})"
        else -> "Billing month (${getBillingPeriodLabel(period).lowercase()})"
    }

    val selectMonthPlaceholder: String get() = when {
        isSpanish -> "Seleccionar mes"
        isCatalan -> "Seleccionar mes"
        else -> "Select month"
    }

    val freeSlotsFieldLabel: String get() = when {
        isSpanish -> "Huecos libres (plazas para otros)"
        isCatalan -> "Llocs lliures (places per a altres)"
        else -> "Free slots (seats for others)"
    }

    val notesFieldPlaceholder: String get() = when {
        isSpanish -> "Ej: Acceso de administrador, Bizum..."
        isCatalan -> "Ex: Accés d'administrador, Bizum..."
        else -> "E.g. Admin access, Bizum..."
    }

    val deleteSubscriptionIconDescription: String get() = when {
        isSpanish -> "Eliminar Suscripción"
        isCatalan -> "Eliminar Subscripció"
        else -> "Delete Subscription"
    }

    val saveChangesButton: String get() = when {
        isSpanish -> "Guardar Cambios"
        isCatalan -> "Desar Canvis"
        else -> "Save Changes"
    }

    val createSubscriptionButton: String get() = when {
        isSpanish -> "Crear Suscripción"
        isCatalan -> "Crear Subscripció"
        else -> "Create Subscription"
    }

    // ==========================================
    // ADD / EDIT SUBSCRIPTION DIALOG: alarm, sharing platforms, icon picker
    // ==========================================
    val subscriptionAlarmTitle: String get() = when {
        isSpanish -> "Alarma de cobro de suscripción"
        isCatalan -> "Alarma de cobrament de la subscripció"
        else -> "Subscription billing alarm"
    }

    val subscriptionAlarmSubtitle: String get() = when {
        isSpanish -> "Notificación antes de la fecha de cobro"
        isCatalan -> "Notificació abans de la data de cobrament"
        else -> "Notification before the billing date"
    }

    val subscriptionAlarmAmountHeader: String get() = when {
        isSpanish -> "CANTIDAD"
        isCatalan -> "QUANTITAT"
        else -> "AMOUNT"
    }

    val subscriptionAlarmUnitHeader: String get() = when {
        isSpanish -> "UNIDAD DE ANTELACIÓN"
        isCatalan -> "UNITAT D'ANTELACIÓ"
        else -> "LEAD TIME UNIT"
    }

    fun subscriptionAlarmUnitLabel(unitKey: String): String = when (unitKey.lowercase()) {
        "same_day", "mismo_dia" -> when {
            isSpanish -> "El mismo día"
            isCatalan -> "El mateix dia"
            else -> "Same day"
        }
        "hours", "horas" -> when {
            isSpanish -> "Hora(s) antes"
            isCatalan -> "Hora/es abans"
            else -> "Hour(s) before"
        }
        "weeks", "semanas" -> when {
            isSpanish -> "Semana(s) antes"
            isCatalan -> "Setmana/es abans"
            else -> "Week(s) before"
        }
        "months", "meses" -> when {
            isSpanish -> "Mes(es) antes"
            isCatalan -> "Mes/os abans"
            else -> "Month(s) before"
        }
        else -> when {
            isSpanish -> "Día(s) antes"
            isCatalan -> "Dia/es abans"
            else -> "Day(s) before"
        }
    }

    val subscriptionAlarmSummarySameDay: String get() = when {
        isSpanish -> "🔔 Avisar el mismo día del cobro"
        isCatalan -> "🔔 Avisar el mateix dia del cobrament"
        else -> "🔔 Notify on the billing day"
    }

    fun subscriptionAlarmSummaryLead(value: Int, unitLabelLowercase: String): String = when {
        isSpanish -> "🔔 Avisar $value $unitLabelLowercase"
        isCatalan -> "🔔 Avisar $value $unitLabelLowercase"
        else -> "🔔 Notify $value $unitLabelLowercase"
    }

    val sharingPlatformsCardSubtitle: String get() = when {
        isSpanish -> "Configura hasta 3 plataformas con su precio, divisa y periodicidad"
        isCatalan -> "Configura fins a 3 plataformes amb el seu preu, divisa i periodicitat"
        else -> "Set up to 3 platforms with their price, currency and billing period"
    }

    val addSharingPlatformButton: String get() = when {
        isSpanish -> "Añadir plataforma de compartición"
        isCatalan -> "Afegir plataforma de compartició"
        else -> "Add sharing platform"
    }

    val noSharingPlatformsConfigured: String get() = when {
        isSpanish -> "No has configurado plataformas de compartición aún. Pulsa el botón de arriba para añadir una plataforma con su precio, divisa y periodicidad."
        isCatalan -> "Encara no has configurat cap plataforma de compartició. Prem el botó de dalt per afegir una plataforma amb el seu preu, divisa i periodicitat."
        else -> "You haven't set up any sharing platforms yet. Tap the button above to add a platform with its price, currency and billing period."
    }

    val editPlatformIconDescription: String get() = when {
        isSpanish -> "Editar plataforma"
        isCatalan -> "Editar plataforma"
        else -> "Edit platform"
    }

    val deletePlatformIconDescription: String get() = when {
        isSpanish -> "Eliminar plataforma"
        isCatalan -> "Eliminar plataforma"
        else -> "Delete platform"
    }

    val sharingPlatformModalAddTitle: String get() = when {
        isSpanish -> "Añadir Plataforma de Compartición"
        isCatalan -> "Afegir plataforma de compartició"
        else -> "Add Sharing Platform"
    }

    val sharingPlatformModalEditTitle: String get() = when {
        isSpanish -> "Editar Plataforma de Compartición"
        isCatalan -> "Editar plataforma de compartició"
        else -> "Edit Sharing Platform"
    }

    val sharingPlatformFieldLabel: String get() = when {
        isSpanish -> "Plataforma de compartición *"
        isCatalan -> "Plataforma de compartició *"
        else -> "Sharing platform *"
    }

    val sharingPlatformNamePlaceholder: String get() = when {
        isSpanish -> "Nombre de la plataforma..."
        isCatalan -> "Nom de la plataforma..."
        else -> "Platform name..."
    }

    val sharingPlatformListButton: String get() = when {
        isSpanish -> "Lista"
        isCatalan -> "Llista"
        else -> "List"
    }

    val sharingPlatformSelectPlaceholder: String get() = when {
        isSpanish -> "Seleccionar plataforma..."
        isCatalan -> "Selecciona una plataforma..."
        else -> "Select platform..."
    }

    val sharingPlatformOtherCustom: String get() = when {
        isSpanish -> "+ Otra personalizada..."
        isCatalan -> "+ Una altra de personalitzada..."
        else -> "+ Other (custom)..."
    }

    val sharingPlatformPricePerSlot: String get() = when {
        isSpanish -> "Precio / slot *"
        isCatalan -> "Preu / slot *"
        else -> "Price / slot *"
    }

    val sharingPlatformCurrencyRequired: String get() = when {
        isSpanish -> "Moneda *"
        isCatalan -> "Moneda *"
        else -> "Currency *"
    }

    val sharingPlatformPaymentFrequency: String get() = when {
        isSpanish -> "Frecuencia de pago de la plataforma *"
        isCatalan -> "Freqüència de pagament de la plataforma *"
        else -> "Platform payment frequency *"
    }

    val sharingPlatformDefaultMethodLabel: String get() = when {
        isSpanish -> "Método habitual por defecto (opcional)"
        isCatalan -> "Mètode habitual per defecte (opcional)"
        else -> "Usual payment method (optional)"
    }

    val sharingPlatformDefaultMethodPlaceholder: String get() = when {
        isSpanish -> "Ej. Bizum, Sharesub Wallet, Joiin..."
        isCatalan -> "Ex. Bizum, Sharesub Wallet, Joiin..."
        else -> "E.g. Bizum, Sharesub Wallet, Joiin..."
    }

    val sharingPlatformFallbackName: String get() = when {
        isSpanish -> "Plataforma"
        isCatalan -> "Plataforma"
        else -> "Platform"
    }

    fun sharingPlatformRateSummary(platformName: String, amount: String, symbol: String, suffix: String): String = when {
        isSpanish -> "Tarifa: $platformName • $amount $symbol$suffix"
        isCatalan -> "Tarifa: $platformName • $amount $symbol$suffix"
        else -> "Rate: $platformName • $amount $symbol$suffix"
    }

    val sharingPlatformAddToListButton: String get() = when {
        isSpanish -> "Añadir a la lista"
        isCatalan -> "Afegir a la llista"
        else -> "Add to list"
    }

    val sharingPlatformUpdateButton: String get() = when {
        isSpanish -> "Actualizar"
        isCatalan -> "Actualitzar"
        else -> "Update"
    }

    val iconPickerTitle: String get() = when {
        isSpanish -> "Elegir Icono para la Suscripción"
        isCatalan -> "Triar una icona per a la subscripció"
        else -> "Choose an Icon for the Subscription"
    }

    val iconPickerFallbackName: String get() = when {
        isSpanish -> "Icono"
        isCatalan -> "Icona"
        else -> "Icon"
    }

    val iconPickerPreview: String get() = when {
        isSpanish -> "Vista Previa"
        isCatalan -> "Vista prèvia"
        else -> "Preview"
    }

    val iconPickerGalleryPhoto: String get() = when {
        isSpanish -> "Foto de la galería"
        isCatalan -> "Foto de la galeria"
        else -> "Gallery photo"
    }

    fun iconPickerSymbolPrefix(key: String): String = when {
        isSpanish -> "Símbolo: $key"
        isCatalan -> "Símbol: $key"
        else -> "Symbol: $key"
    }

    val iconPickerLibraryTab: String get() = when {
        isSpanish -> "Biblioteca de Iconos"
        isCatalan -> "Biblioteca d'icones"
        else -> "Icon Library"
    }

    val iconPickerGalleryTab: String get() = when {
        isSpanish -> "Subir de la Galería"
        isCatalan -> "Pujar de la galeria"
        else -> "Upload from Gallery"
    }

    val iconPickerBackgroundColor: String get() = when {
        isSpanish -> "Color de fondo"
        isCatalan -> "Color de fons"
        else -> "Background color"
    }

    val iconPickerSelectedDescription: String get() = when {
        isSpanish -> "Seleccionado"
        isCatalan -> "Seleccionat"
        else -> "Selected"
    }

    val iconPickerSelectSymbol: String get() = when {
        isSpanish -> "Seleccionar símbolo"
        isCatalan -> "Selecciona un símbol"
        else -> "Select symbol"
    }

    val iconPickerChange: String get() = when {
        isSpanish -> "Cambiar"
        isCatalan -> "Canviar"
        else -> "Change"
    }

    val iconPickerRemoveImage: String get() = when {
        isSpanish -> "Quitar imagen"
        isCatalan -> "Treure la imatge"
        else -> "Remove image"
    }

    val iconPickerChoosePhotoHint: String get() = when {
        isSpanish -> "Elige una foto o logo desde tu dispositivo"
        isCatalan -> "Tria una foto o un logo des del teu dispositiu"
        else -> "Choose a photo or logo from your device"
    }

    val iconPickerSelectFromGallery: String get() = when {
        isSpanish -> "Seleccionar de la galería"
        isCatalan -> "Selecciona de la galeria"
        else -> "Select from gallery"
    }

    val iconPickerApply: String get() = when {
        isSpanish -> "Aplicar Icono"
        isCatalan -> "Aplicar la icona"
        else -> "Apply Icon"
    }

    /** Nombre mostrado de cada icono de la biblioteca. Si la clave no se conoce, se usa el texto original. */
    fun getIconLabel(key: String, fallback: String): String = when (key.lowercase()) {
        "movie" -> when { isSpanish -> "Películas"; isCatalan -> "Pel·lícules"; else -> "Movies" }
        "tv" -> when { isSpanish -> "Series / TV"; isCatalan -> "Sèries / TV"; else -> "Series / TV" }
        "videocam" -> when { isSpanish -> "Video"; isCatalan -> "Vídeo"; else -> "Video" }
        "headphones" -> when { isSpanish -> "Música"; isCatalan -> "Música"; else -> "Music" }
        "mic" -> when { isSpanish -> "Podcast / Audio"; isCatalan -> "Pòdcast / Àudio"; else -> "Podcast / Audio" }
        "radio" -> "Radio"
        "chat" -> when { isSpanish -> "Chat / IA"; isCatalan -> "Xat / IA"; else -> "Chat / AI" }
        "auto_awesome" -> when { isSpanish -> "IA / Gemini"; isCatalan -> "IA / Gemini"; else -> "AI / Gemini" }
        "code" -> when { isSpanish -> "Desarrollo"; isCatalan -> "Desenvolupament"; else -> "Development" }
        "cloud" -> when { isSpanish -> "Nube / Almacenamiento"; isCatalan -> "Núvol / Emmagatzematge"; else -> "Cloud / Storage" }
        "work" -> when { isSpanish -> "Trabajo"; isCatalan -> "Feina"; else -> "Work" }
        "laptop" -> "Software"
        "games" -> "Gaming"
        "sports_esports" -> when { isSpanish -> "Consola"; isCatalan -> "Consola"; else -> "Console" }
        "school" -> when { isSpanish -> "Educación"; isCatalan -> "Educació"; else -> "Education" }
        "book" -> when { isSpanish -> "Libros / Lectura"; isCatalan -> "Llibres / Lectura"; else -> "Books / Reading" }
        "fitness" -> when { isSpanish -> "Gimnasio / Deporte"; isCatalan -> "Gimnàs / Esport"; else -> "Gym / Sports" }
        "soccer" -> when { isSpanish -> "Deportes"; isCatalan -> "Esports"; else -> "Sports" }
        "shopping" -> when { isSpanish -> "Compras / Tienda"; isCatalan -> "Compres / Botiga"; else -> "Shopping / Store" }
        "palette" -> when { isSpanish -> "Diseño"; isCatalan -> "Disseny"; else -> "Design" }
        "newspaper" -> when { isSpanish -> "Noticias / Prensa"; isCatalan -> "Notícies / Premsa"; else -> "News / Press" }
        "lock" -> when { isSpanish -> "Seguridad / VPN"; isCatalan -> "Seguretat / VPN"; else -> "Security / VPN" }
        "key" -> when { isSpanish -> "Contraseñas / Vault"; isCatalan -> "Contrasenyes / Vault"; else -> "Passwords / Vault" }
        "euro" -> when { isSpanish -> "Finanzas / Banco"; isCatalan -> "Finances / Banc"; else -> "Finance / Bank" }
        "star" -> when { isSpanish -> "Favorito / Premium"; isCatalan -> "Preferit / Premium"; else -> "Favorite / Premium" }
        "heart" -> when { isSpanish -> "Salud / Bienestar"; isCatalan -> "Salut / Benestar"; else -> "Health / Wellness" }
        "subscriptions" -> when { isSpanish -> "Suscripción"; isCatalan -> "Subscripció"; else -> "Subscription" }
        "widgets" -> when { isSpanish -> "Servicios"; isCatalan -> "Serveis"; else -> "Services" }
        else -> fallback
    }

    // ==========================================
    // ADD / EDIT PLATFORM DIALOG & DELETE PLATFORM CONFIRM
    // ==========================================
    val platformDialogNewTitle: String get() = when {
        isSpanish -> "Nueva plataforma"
        isCatalan -> "Nova plataforma"
        else -> "New platform"
    }

    val platformDialogEditTitle: String get() = when {
        isSpanish -> "Editar plataforma"
        isCatalan -> "Editar plataforma"
        else -> "Edit platform"
    }

    val platformDialogSubtitle: String get() = when {
        isSpanish -> "Personaliza el nombre y color de la plataforma"
        isCatalan -> "Personalitza el nom i el color de la plataforma"
        else -> "Customize the platform's name and color"
    }

    val platformNamePlaceholder: String get() = when {
        isSpanish -> "Ej. Price Together, Sharingful..."
        isCatalan -> "Ex. Price Together, Sharingful..."
        else -> "E.g. Price Together, Sharingful..."
    }

    val platformNameRequiredError: String get() = when {
        isSpanish -> "Introduce un nombre para la plataforma"
        isCatalan -> "Introdueix un nom per a la plataforma"
        else -> "Enter a name for the platform"
    }

    val platformColorLabel: String get() = when {
        isSpanish -> "Color identificativo"
        isCatalan -> "Color identificatiu"
        else -> "Identifying color"
    }

    val platformHexLabel: String get() = when {
        isSpanish -> "Código de color Hex personalizado"
        isCatalan -> "Codi de color Hex personalitzat"
        else -> "Custom Hex color code"
    }

    val platformPreviewHeader: String get() = when {
        isSpanish -> "VISTA PREVIA"
        isCatalan -> "VISTA PRÈVIA"
        else -> "PREVIEW"
    }

    val platformPreviewIntro: String get() = when {
        isSpanish -> "Así se verá en la lista de usuarios y filtros:"
        isCatalan -> "Així es veurà a la llista d'usuaris i filtres:"
        else -> "This is how it will look in the user list and filters:"
    }

    val platformPreviewFallbackName: String get() = when {
        isSpanish -> "Nombre plataforma"
        isCatalan -> "Nom de la plataforma"
        else -> "Platform name"
    }

    val platformDialogAddButton: String get() = when {
        isSpanish -> "Añadir plataforma"
        isCatalan -> "Afegir plataforma"
        else -> "Add platform"
    }

    val platformDialogSaveButton: String get() = when {
        isSpanish -> "Guardar cambios"
        isCatalan -> "Desar els canvis"
        else -> "Save changes"
    }

    val deletePlatformConfirmTitle: String get() = when {
        isSpanish -> "¿Eliminar plataforma?"
        isCatalan -> "Eliminar la plataforma?"
        else -> "Delete platform?"
    }

    fun deletePlatformConfirmMessage(platformName: String): String = when {
        isSpanish -> "¿Estás seguro de que deseas eliminar la plataforma \"$platformName\"? Desaparecerá de los selectores y listas de la aplicación."
        isCatalan -> "Segur que vols eliminar la plataforma \"$platformName\"? Desapareixerà dels selectors i de les llistes de l'aplicació."
        else -> "Are you sure you want to delete the platform \"$platformName\"? It will disappear from the app's selectors and lists."
    }

    // ==========================================
    // AUTH / ACCOUNT & SYNC DIALOG
    // ==========================================
    val accountTitleAuthenticated: String get() = when {
        isSpanish -> "Mi Cuenta & Sincronización"
        isCatalan -> "El meu compte i sincronització"
        else -> "My Account & Sync"
    }

    val accountCreateAccount: String get() = when {
        isSpanish -> "Crear cuenta"
        isCatalan -> "Crear compte"
        else -> "Create account"
    }

    val accountTitleLogin: String get() = when {
        isSpanish -> "Acceso Multidispositivo"
        isCatalan -> "Accés multidispositiu"
        else -> "Multi-device access"
    }

    val accountSubtitleAuthenticated: String get() = when {
        isSpanish -> "Sincronizado con la nube (Firestore)"
        isCatalan -> "Sincronitzat amb el núvol (Firestore)"
        else -> "Synced with the cloud (Firestore)"
    }

    val accountSubtitleLogin: String get() = when {
        isSpanish -> "Usa tu cuenta en Android y Web"
        isCatalan -> "Fes servir el teu compte a Android i a la web"
        else -> "Use your account on Android and Web"
    }

    val accountConnectingToCloud: String get() = when {
        isSpanish -> "Conectando con la nube..."
        isCatalan -> "Connectant amb el núvol..."
        else -> "Connecting to the cloud..."
    }

    val accountRetry: String get() = when {
        isSpanish -> "Reintentar"
        isCatalan -> "Torna-ho a provar"
        else -> "Retry"
    }

    val accountOpeningAccountPicker: String get() = when {
        isSpanish -> "Abriendo selector de cuentas..."
        isCatalan -> "Obrint el selector de comptes..."
        else -> "Opening account picker..."
    }

    val accountProfilePhotoDescription: String get() = when {
        isSpanish -> "Foto de perfil"
        isCatalan -> "Foto de perfil"
        else -> "Profile picture"
    }

    val accountRegisteredUserFallback: String get() = when {
        isSpanish -> "Usuario registrado"
        isCatalan -> "Usuari registrat"
        else -> "Registered user"
    }

    val accountSyncSectionTitle: String get() = when {
        isSpanish -> "Sincronización en la nube"
        isCatalan -> "Sincronització al núvol"
        else -> "Cloud sync"
    }

    val accountSyncSectionDescription: String get() = when {
        isSpanish -> "Tus datos se guardan de forma segura y se sincronizan en tiempo real con el esquema unificado users/{userId}/subscriptions."
        isCatalan -> "Les teves dades es desen de manera segura i se sincronitzen en temps real amb l'esquema unificat users/{userId}/subscriptions."
        else -> "Your data is stored securely and synced in real time using the unified users/{userId}/subscriptions schema."
    }

    val accountUploadToCloud: String get() = when {
        isSpanish -> "Subir a la nube"
        isCatalan -> "Puja al núvol"
        else -> "Upload to cloud"
    }

    val accountDownload: String get() = when {
        isSpanish -> "Descargar"
        isCatalan -> "Baixa"
        else -> "Download"
    }

    val accountCleanDatabase: String get() = when {
        isSpanish -> "🧹 Limpiar y Depurar Base de Datos en la Nube"
        isCatalan -> "🧹 Netejar i depurar la base de dades al núvol"
        else -> "🧹 Clean & Tidy Cloud Database"
    }

    val accountSignOut: String get() = when {
        isSpanish -> "Cerrar sesión"
        isCatalan -> "Tanca la sessió"
        else -> "Sign out"
    }

    val accountDeleteMyAccount: String get() = when {
        isSpanish -> "Eliminar mi cuenta"
        isCatalan -> "Elimina el meu compte"
        else -> "Delete my account"
    }

    val accountDeleteConfirmTitle: String get() = when {
        isSpanish -> "¿Eliminar tu cuenta?"
        isCatalan -> "Eliminar el teu compte?"
        else -> "Delete your account?"
    }

    val accountDeleteConfirmBody: String get() = when {
        isSpanish -> "Esta acción es irreversible. Se borrarán todas tus suscripciones. En los grupos de otras personas donde participas, tu plaza quedará como «pendiente de eliminar» y quien gestiona el grupo podrá conservar tu nombre y tus pagos hasta que la retire. No podrás recuperar tu cuenta ni tus datos."
        isCatalan -> "Aquesta acció és irreversible. S'esborraran totes les teves subscripcions. Als grups d'altres persones on participes, la teva plaça quedarà com a «pendent d'eliminar» i qui gestiona el grup podrà conservar el teu nom i els teus pagaments fins que la retiri. No podràs recuperar el teu compte ni les teves dades."
        else -> "This action is irreversible. All your subscriptions will be deleted. In other people's groups you take part in, your seat will be marked as “pending removal” and the group's organiser may keep your name and payments until they remove it. You won't be able to recover your account or your data."
    }

    val accountDeleting: String get() = when {
        isSpanish -> "Eliminando..."
        isCatalan -> "Eliminant..."
        else -> "Deleting..."
    }

    val accountDeleteConfirmButton: String get() = when {
        isSpanish -> "Sí, eliminar mi cuenta"
        isCatalan -> "Sí, elimina el meu compte"
        else -> "Yes, delete my account"
    }

    val accountTabLogin: String get() = when {
        isSpanish -> "Iniciar sesión"
        isCatalan -> "Inicia la sessió"
        else -> "Sign in"
    }

    val accountNameLabel: String get() = when {
        isSpanish -> "Tu nombre"
        isCatalan -> "El teu nom"
        else -> "Your name"
    }

    val accountNamePlaceholder: String get() = when {
        isSpanish -> "Ej. Víctor Oliver"
        isCatalan -> "Ex. Víctor Oliver"
        else -> "E.g. Victor Oliver"
    }

    val accountEmailLabel: String get() = when {
        isSpanish -> "Tu Correo electrónico"
        isCatalan -> "El teu correu electrònic"
        else -> "Your email"
    }

    val accountEmailPlaceholder: String get() = when {
        isSpanish -> "ejemplo@gmail.com"
        isCatalan -> "exemple@gmail.com"
        else -> "example@gmail.com"
    }

    val accountPasswordLabelRegister: String get() = when {
        isSpanish -> "Crear contraseña (mínimo 6 caracteres)"
        isCatalan -> "Crea una contrasenya (mínim 6 caràcters)"
        else -> "Create password (minimum 6 characters)"
    }

    val accountPasswordLabelLogin: String get() = when {
        isSpanish -> "Tu Contraseña"
        isCatalan -> "La teva contrasenya"
        else -> "Your password"
    }

    val accountShowPasswordDescription: String get() = when {
        isSpanish -> "Ver contraseña"
        isCatalan -> "Mostra la contrasenya"
        else -> "Show password"
    }

    val accountSubmitRegister: String get() = when {
        isSpanish -> "Registrarme con Correo"
        isCatalan -> "Registra'm amb correu"
        else -> "Sign up with Email"
    }

    val accountOrAlsoSeparator: String get() = when {
        isSpanish -> " o también "
        isCatalan -> " o també "
        else -> " or also "
    }

    // ==========================================
    // BACKUP & RESTORE DIALOG
    // ==========================================
    val backupDialogTitle: String get() = when {
        isSpanish -> "Copia y Restauración"
        isCatalan -> "Còpia i restauració"
        else -> "Backup & Restore"
    }

    val backupInfoText: String get() = when {
        isSpanish -> "Puedes guardar y restaurar tus copias en tu almacenamiento local, Google Drive o Microsoft OneDrive mediante el selector del sistema o compartirlas a la nube."
        isCatalan -> "Pots desar i restaurar les teves còpies a l'emmagatzematge local, a Google Drive o a Microsoft OneDrive mitjançant el selector del sistema, o compartir-les al núvol."
        else -> "You can save and restore your backups on local storage, Google Drive or Microsoft OneDrive using the system picker, or share them to the cloud."
    }

    val backupCreateSection: String get() = when {
        isSpanish -> "Crear copia de seguridad"
        isCatalan -> "Crear còpia de seguretat"
        else -> "Create backup"
    }

    val backupSaveFileTitle: String get() = when {
        isSpanish -> "Guardar archivo de copia"
        isCatalan -> "Desar el fitxer de còpia"
        else -> "Save backup file"
    }

    val backupSaveFileSubtitle: String get() = when {
        isSpanish -> "Elige guardar en Local, Google Drive o OneDrive"
        isCatalan -> "Tria si vols desar-lo en local, a Google Drive o a OneDrive"
        else -> "Choose to save to Local, Google Drive or OneDrive"
    }

    val backupShareTitle: String get() = when {
        isSpanish -> "Enviar o Subir a la nube"
        isCatalan -> "Enviar o pujar al núvol"
        else -> "Send or Upload to the cloud"
    }

    val backupShareSubtitle: String get() = when {
        isSpanish -> "Sube a Google Drive, OneDrive, Gmail, etc."
        isCatalan -> "Puja a Google Drive, OneDrive, Gmail, etc."
        else -> "Upload to Google Drive, OneDrive, Gmail, etc."
    }

    val backupRestoreSection: String get() = when {
        isSpanish -> "Restauración de datos"
        isCatalan -> "Restauració de dades"
        else -> "Data restore"
    }

    val backupRestoreFileTitle: String get() = when {
        isSpanish -> "Restaurar desde archivo"
        isCatalan -> "Restaurar des d'un fitxer"
        else -> "Restore from file"
    }

    val backupRestoreFileSubtitle: String get() = when {
        isSpanish -> "Selecciona desde Local, Google Drive o OneDrive"
        isCatalan -> "Selecciona des de local, Google Drive o OneDrive"
        else -> "Select from Local, Google Drive or OneDrive"
    }

    val backupShareEmailSubject: String get() = when {
        isSpanish -> "Copia de Seguridad - Gestor de Suscripciones"
        isCatalan -> "Còpia de seguretat - Gestor de subscripcions"
        else -> "Backup - Subscription Manager"
    }

    val backupShareChooserTitle: String get() = when {
        isSpanish -> "Guardar en Google Drive, OneDrive o Enviar..."
        isCatalan -> "Desar a Google Drive, OneDrive o enviar..."
        else -> "Save to Google Drive, OneDrive or Send..."
    }

    val backupToastSaved: String get() = when {
        isSpanish -> "✅ Copia de seguridad guardada correctamente"
        isCatalan -> "✅ Còpia de seguretat desada correctament"
        else -> "✅ Backup saved successfully"
    }

    val backupToastSaveError: String get() = when {
        isSpanish -> "❌ Error al guardar el archivo"
        isCatalan -> "❌ Error en desar el fitxer"
        else -> "❌ Error saving the file"
    }

    val backupToastReadError: String get() = when {
        isSpanish -> "❌ No se pudo leer el archivo seleccionado"
        isCatalan -> "❌ No s'ha pogut llegir el fitxer seleccionat"
        else -> "❌ Could not read the selected file"
    }

    val backupToastShareError: String get() = when {
        isSpanish -> "Error al preparar el archivo para compartir"
        isCatalan -> "Error en preparar el fitxer per compartir"
        else -> "Error preparing the file to share"
    }

    val backupPreviewTitleValid: String get() = when {
        isSpanish -> "Restaurar copia de seguridad"
        isCatalan -> "Restaurar la còpia de seguretat"
        else -> "Restore backup"
    }

    val backupPreviewTitleInvalid: String get() = when {
        isSpanish -> "Copia no válida"
        isCatalan -> "Còpia no vàlida"
        else -> "Invalid backup"
    }

    val backupPreviewReadOk: String get() = when {
        isSpanish -> "Se ha leído correctamente el archivo de copia:"
        isCatalan -> "S'ha llegit correctament el fitxer de còpia:"
        else -> "The backup file was read successfully:"
    }

    fun backupPreviewDate(date: String): String = when {
        isSpanish -> "📅 Fecha: $date"
        isCatalan -> "📅 Data: $date"
        else -> "📅 Date: $date"
    }

    fun backupPreviewSubscriptions(count: Int): String = when {
        isSpanish -> "💳 Suscripciones: $count"
        isCatalan -> "💳 Subscripcions: $count"
        else -> "💳 Subscriptions: $count"
    }

    fun backupPreviewMembers(count: Int): String = when {
        isSpanish -> "👥 Miembros/Usuarios: $count"
        isCatalan -> "👥 Membres/Usuaris: $count"
        else -> "👥 Members/Users: $count"
    }

    fun backupPreviewIncludes(list: String): String = when {
        isSpanish -> "Incluye: $list..."
        isCatalan -> "Inclou: $list..."
        else -> "Includes: $list..."
    }

    val backupReplaceAll: String get() = when {
        isSpanish -> "Reemplazar todo"
        isCatalan -> "Substitueix-ho tot"
        else -> "Replace all"
    }

    val backupMerge: String get() = when {
        isSpanish -> "Fusionar datos"
        isCatalan -> "Fusiona les dades"
        else -> "Merge data"
    }

    val backupReplaceWarning: String get() = when {
        isSpanish -> "⚠️ 'Reemplazar todo' eliminará los datos actuales y dejará exactamente el contenido de la copia."
        isCatalan -> "⚠️ «Substitueix-ho tot» eliminarà les dades actuals i deixarà exactament el contingut de la còpia."
        else -> "⚠️ 'Replace all' will delete the current data and leave exactly the contents of the backup."
    }

    val backupMergeInfo: String get() = when {
        isSpanish -> "ℹ️ 'Fusionar datos' mantendrá tus suscripciones actuales y añadirá las de la copia de seguridad."
        isCatalan -> "ℹ️ «Fusiona les dades» mantindrà les teves subscripcions actuals i hi afegirà les de la còpia de seguretat."
        else -> "ℹ️ 'Merge data' will keep your current subscriptions and add those from the backup."
    }

    fun backupInvalidFormat(error: String?): String = when {
        isSpanish -> "El archivo no contiene un formato de copia de seguridad válido:\n$error"
        isCatalan -> "El fitxer no conté un format de còpia de seguretat vàlid:\n$error"
        else -> "The file does not contain a valid backup format:\n$error"
    }

    val backupRestoreNow: String get() = when {
        isSpanish -> "Restaurar ahora"
        isCatalan -> "Restaurar ara"
        else -> "Restore now"
    }

    // ==========================================
    // BATCH 6: RESTORE MODE, REMINDER DIALOG, EMAIL AUTH DIALOGS, SUBSCRIPTION CARD
    // ==========================================
    val restoreHowToApply: String get() = when {
        isSpanish -> "¿Cómo deseas aplicar la restauración?"
        isCatalan -> "Com vols aplicar la restauració?"
        else -> "How do you want to apply the restore?"
    }

    fun reminderMessageReadyFor(memberName: String): String = when {
        isSpanish -> "Mensaje listo para enviar a $memberName:"
        isCatalan -> "Missatge a punt per enviar a $memberName:"
        else -> "Message ready to send to $memberName:"
    }

    val reminderClipLabel: String get() = when {
        isSpanish -> "Recordatorio"
        isCatalan -> "Recordatori"
        else -> "Reminder"
    }

    val reminderShareChooser: String get() = when {
        isSpanish -> "Enviar recordatorio por..."
        isCatalan -> "Enviar recordatori per..."
        else -> "Send reminder via..."
    }

    val reminderDefaultPlanName: String get() = when {
        isSpanish -> "Cuenta Compartida"
        isCatalan -> "Compte Compartit"
        else -> "Shared Account"
    }

    fun reminderPeriodLabel(unit: String, value: Int, fallback: String): String {
        val freqVal = if (value > 0) value else 1
        return when (unit) {
            "months" -> when {
                isSpanish -> if (freqVal == 1) "mensual" else "cada $freqVal meses"
                isCatalan -> if (freqVal == 1) "mensual" else "cada $freqVal mesos"
                else -> if (freqVal == 1) "monthly" else "every $freqVal months"
            }
            "years" -> when {
                isSpanish -> if (freqVal == 1) "anual" else "cada $freqVal años"
                isCatalan -> if (freqVal == 1) "anual" else "cada $freqVal anys"
                else -> if (freqVal == 1) "yearly" else "every $freqVal years"
            }
            "weeks" -> when {
                isSpanish -> if (freqVal == 1) "semanal" else "cada $freqVal semanas"
                isCatalan -> if (freqVal == 1) "setmanal" else "cada $freqVal setmanes"
                else -> if (freqVal == 1) "weekly" else "every $freqVal weeks"
            }
            "days" -> when {
                isSpanish -> if (freqVal == 1) "diaria" else "cada $freqVal días"
                isCatalan -> if (freqVal == 1) "diària" else "cada $freqVal dies"
                else -> if (freqVal == 1) "daily" else "every $freqVal days"
            }
            else -> fallback.lowercase()
        }
    }

    val emailRegisterTitle: String get() = when {
        isSpanish -> "Crear cuenta con correo"
        isCatalan -> "Crear compte amb correu"
        else -> "Create account with email"
    }

    val confirmPasswordLabel: String get() = when {
        isSpanish -> "Repite la contraseña"
        isCatalan -> "Repeteix la contrasenya"
        else -> "Repeat password"
    }

    val passwordsDoNotMatch: String get() = when {
        isSpanish -> "Las contraseñas no coinciden"
        isCatalan -> "Les contrasenyes no coincideixen"
        else -> "Passwords do not match"
    }

    val emailCreating: String get() = when {
        isSpanish -> "Creando..."
        isCatalan -> "Creant..."
        else -> "Creating..."
    }

    val emailCreateMyAccount: String get() = when {
        isSpanish -> "Crear mi cuenta"
        isCatalan -> "Crear el meu compte"
        else -> "Create my account"
    }

    val emailLoginTitle: String get() = when {
        isSpanish -> "Iniciar sesión con correo"
        isCatalan -> "Iniciar sessió amb correu"
        else -> "Sign in with email"
    }

    val emailLoggingIn: String get() = when {
        isSpanish -> "Entrando..."
        isCatalan -> "Entrant..."
        else -> "Signing in..."
    }

    val alarmActiveDescription: String get() = when {
        isSpanish -> "Alarma activada"
        isCatalan -> "Alarma activada"
        else -> "Alarm active"
    }

    val unreadMessageDescription: String get() = when {
        isSpanish -> "Mensaje sin leer"
        isCatalan -> "Missatge sense llegir"
        else -> "Unread message"
    }

    val paymentAlarmActiveDescription: String get() = when {
        isSpanish -> "Alarma de pago activada"
        isCatalan -> "Alarma de pagament activada"
        else -> "Payment alarm active"
    }

    val shareInviteDescription: String get() = when {
        isSpanish -> "Compartir invitación"
        isCatalan -> "Compartir invitació"
        else -> "Share invite"
    }

    val inviteToSlot: String get() = when {
        isSpanish -> "Invitar a un hueco"
        isCatalan -> "Convidar a un buit"
        else -> "Invite to a slot"
    }

    fun slotsFullHint(capacity: Int): String = when {
        isSpanish -> "Has alcanzado los $capacity huecos. Aumenta los huecos para añadir más."
        isCatalan -> "Has assolit els $capacity buits. Augmenta els buits per afegir-ne més."
        else -> "You have reached $capacity slots. Increase slots to add more."
    }

    // ==========================================
    // BATCH 7: HOME SCREEN & VIEWMODEL MESSAGES
    // ==========================================
    val homeMenuDescription: String get() = when {
        isSpanish -> "Menú"
        isCatalan -> "Menú"
        else -> "Menu"
    }

    val unreadManagerMessageDescription: String get() = when {
        isSpanish -> "Mensaje sin leer del gestor"
        isCatalan -> "Missatge sense llegir del gestor"
        else -> "Unread manager message"
    }

    fun participatingInTitle(count: Int): String = when {
        isSpanish -> "Participo en ($count)"
        isCatalan -> "Participo en ($count)"
        else -> "Participating in ($count)"
    }

    val nextPaymentLabel: String get() = when {
        isSpanish -> "Próximo pago:"
        isCatalan -> "Pròxim pagament:"
        else -> "Next payment:"
    }

    val paymentStatusLabel: String get() = when {
        isSpanish -> "Estado:"
        isCatalan -> "Estat:"
        else -> "Status:"
    }

    val paymentStatusPaid: String get() = when {
        isSpanish -> "Pagado"
        isCatalan -> "Pagat"
        else -> "Paid"
    }

    val paymentStatusPending: String get() = when {
        isSpanish -> "Pendiente"
        isCatalan -> "Pendent"
        else -> "Pending"
    }

    val fewerDaysDescription: String get() = when {
        isSpanish -> "Menos días"
        isCatalan -> "Menys dies"
        else -> "Fewer days"
    }

    val moreDaysDescription: String get() = when {
        isSpanish -> "Más días"
        isCatalan -> "Més dies"
        else -> "More days"
    }

    val claimInviteInvalidCode: String get() = when {
        isSpanish -> "El código no es válido."
        isCatalan -> "El codi no és vàlid."
        else -> "Invalid code."
    }

    val claimInviteSuccess: String get() = when {
        isSpanish -> "¡Te has unido al grupo!"
        isCatalan -> "T'has unit al grup!"
        else -> "You have joined the group!"
    }

    val claimInviteDefaultError: String get() = when {
        isSpanish -> "No se pudo unir. Revisa el código."
        isCatalan -> "No s'ha pogut unir. Revisa el codi."
        else -> "Could not join. Check the code."
    }

    val deleteAccountSuccess: String get() = when {
        isSpanish -> "Cuenta eliminada correctamente."
        isCatalan -> "Compte eliminat correctament."
        else -> "Account deleted successfully."
    }

    val deleteAccountDefaultError: String get() = when {
        isSpanish -> "No se pudo eliminar la cuenta."
        isCatalan -> "No s'ha pogut eliminar el compte."
        else -> "Could not delete account."
    }

    // ==========================================
    // BATCH 8: FIREBASE AUTH SERVICE MESSAGES (auth errors, cloud sync status, DB cleanup)
    // ==========================================
    val authCredentialManagerUnavailable: String get() = when {
        isSpanish -> "El gestor de credenciales no está disponible en este dispositivo"
        isCatalan -> "El gestor de credencials no està disponible en aquest dispositiu"
        else -> "The credential manager is not available on this device"
    }

    val authServiceNotInitialized: String get() = when {
        isSpanish -> "Servicio de autenticación no inicializado"
        isCatalan -> "Servei d'autenticació no inicialitzat"
        else -> "Authentication service not initialized"
    }

    val authUserUnavailable: String get() = when {
        isSpanish -> "Usuario no disponible"
        isCatalan -> "Usuari no disponible"
        else -> "User not available"
    }

    val authGoogleTokenMissing: String get() = when {
        isSpanish -> "No se obtuvo el token de Google"
        isCatalan -> "No s'ha obtingut el token de Google"
        else -> "Couldn't get the Google token"
    }

    fun authUnsupportedCredentialType(type: String): String = when {
        isSpanish -> "Tipo de credencial no soportado: $type"
        isCatalan -> "Tipus de credencial no suportat: $type"
        else -> "Unsupported credential type: $type"
    }

    val authUnknownCredentialType: String get() = when {
        isSpanish -> "Tipo de credencial desconocido"
        isCatalan -> "Tipus de credencial desconegut"
        else -> "Unknown credential type"
    }

    fun authGoogleError(detail: String): String = when {
        isSpanish -> "Error de Google: $detail"
        isCatalan -> "Error de Google: $detail"
        else -> "Google error: $detail"
    }

    val authDefaultError: String get() = when {
        isSpanish -> "Error de autenticación"
        isCatalan -> "Error d'autenticació"
        else -> "Authentication error"
    }

    val authGoogleDefaultError: String get() = when {
        isSpanish -> "Error de autenticación con Google"
        isCatalan -> "Error d'autenticació amb Google"
        else -> "Google authentication error"
    }

    val authEmailAlreadyInUse: String get() = when {
        isSpanish -> "Este correo electrónico ya está registrado. Inicia sesión directamente."
        isCatalan -> "Aquest correu electrònic ja està registrat. Inicia la sessió directament."
        else -> "This email address is already registered. Please sign in instead."
    }

    val authEmailBadFormat: String get() = when {
        isSpanish -> "El formato del correo electrónico no es válido."
        isCatalan -> "El format del correu electrònic no és vàlid."
        else -> "The email address format is not valid."
    }

    val authWrongCredentials: String get() = when {
        isSpanish -> "Correo o contraseña incorrectos."
        isCatalan -> "Correu o contrasenya incorrectes."
        else -> "Incorrect email or password."
    }

    val authPasswordMinLengthError: String get() = when {
        isSpanish -> "La contraseña debe tener al menos 6 caracteres."
        isCatalan -> "La contrasenya ha de tenir almenys 6 caràcters."
        else -> "The password must be at least 6 characters long."
    }

    val authNetworkError: String get() = when {
        isSpanish -> "Error de red. Comprueba tu conexión a Internet."
        isCatalan -> "Error de xarxa. Comprova la teva connexió a Internet."
        else -> "Network error. Check your Internet connection."
    }

    val authGenericError: String get() = when {
        isSpanish -> "Ha ocurrido un error en la autenticación."
        isCatalan -> "S'ha produït un error en l'autenticació."
        else -> "An authentication error occurred."
    }

    // ---- Eliminar cuenta / salir del grupo (mensajes por defecto si el servidor no envía uno) ----
    val deleteAccountRetryError: String get() = when {
        isSpanish -> "No se pudo eliminar la cuenta. Inténtalo de nuevo."
        isCatalan -> "No s'ha pogut eliminar el compte. Torna-ho a provar."
        else -> "Couldn't delete the account. Please try again."
    }

    val leaveGroupRetryError: String get() = when {
        isSpanish -> "No se pudo salir del grupo. Inténtalo de nuevo."
        isCatalan -> "No s'ha pogut sortir del grup. Torna-ho a provar."
        else -> "Couldn't leave the group. Please try again."
    }

    // ---- Estado de sincronización ----
    val syncUnknownError: String get() = when {
        isSpanish -> "Error desconocido"
        isCatalan -> "Error desconegut"
        else -> "Unknown error"
    }

    val syncLoginRequiredDownload: String get() = when {
        isSpanish -> "⚠️ Debes iniciar sesión con tu cuenta de Google para descargar datos de la nube."
        isCatalan -> "⚠️ Has d'iniciar la sessió amb el teu compte de Google per descarregar dades del núvol."
        else -> "⚠️ You must sign in with your Google account to download data from the cloud."
    }

    val syncFirestoreUnavailable: String get() = when {
        isSpanish -> "⚠️ Firestore no está disponible en este momento."
        isCatalan -> "⚠️ Firestore no està disponible en aquest moment."
        else -> "⚠️ Firestore is not available right now."
    }

    val syncDownloading: String get() = when {
        isSpanish -> "☁️ Descargando datos desde la nube..."
        isCatalan -> "☁️ Descarregant dades del núvol..."
        else -> "☁️ Downloading data from the cloud..."
    }

    val syncDownloadedOk: String get() = when {
        isSpanish -> "✅ Suscripciones sincronizadas desde la nube"
        isCatalan -> "✅ Subscripcions sincronitzades des del núvol"
        else -> "✅ Subscriptions synced from the cloud"
    }

    val syncNoChanges: String get() = when {
        isSpanish -> "ℹ️ Sincronización completada. No hay cambios pendientes."
        isCatalan -> "ℹ️ Sincronització completada. No hi ha canvis pendents."
        else -> "ℹ️ Sync complete. No pending changes."
    }

    val syncPermissionDeniedDownload: String get() = when {
        isSpanish -> "⚠️ Permiso denegado en Firestore: Revisa las Reglas en Firebase Console."
        isCatalan -> "⚠️ Permís denegat a Firestore: Revisa les regles a Firebase Console."
        else -> "⚠️ Permission denied in Firestore: Check the Rules in Firebase Console."
    }

    fun syncDownloadError(detail: String): String = when {
        isSpanish -> "⚠️ Error al descargar de la nube: $detail"
        isCatalan -> "⚠️ Error en descarregar del núvol: $detail"
        else -> "⚠️ Error downloading from the cloud: $detail"
    }

    val syncPermissionDeniedUpload: String get() = when {
        isSpanish -> "⚠️ Permiso denegado en Firestore: Configura las Reglas (Rules) en tu consola de Firebase."
        isCatalan -> "⚠️ Permís denegat a Firestore: Configura les regles (Rules) a la teva consola de Firebase."
        else -> "⚠️ Permission denied in Firestore: Set up the Rules in your Firebase console."
    }

    fun syncUploadError(detail: String): String = when {
        isSpanish -> "⚠️ Error al sincronizar con la nube: $detail"
        isCatalan -> "⚠️ Error en sincronitzar amb el núvol: $detail"
        else -> "⚠️ Error syncing with the cloud: $detail"
    }

    // ---- Depuración de la base de datos en la nube ----
    val cleanupLoginRequired: String get() = when {
        isSpanish -> "Debes iniciar sesión para depurar la base de datos"
        isCatalan -> "Has d'iniciar la sessió per depurar la base de dades"
        else -> "You must sign in to clean up the database"
    }

    val cleanupFirestoreUnavailable: String get() = when {
        isSpanish -> "Firestore no disponible"
        isCatalan -> "Firestore no disponible"
        else -> "Firestore not available"
    }

    val cleanupRunning: String get() = when {
        isSpanish -> "🧹 Depurando y limpiando base de datos en la nube..."
        isCatalan -> "🧹 Depurant i netejant la base de dades al núvol..."
        else -> "🧹 Cleaning up the cloud database..."
    }

    fun cleanupSummary(subscriptions: Int, deletedDocs: Int): String = when {
        isSpanish -> "✨ Base de datos depurada: $subscriptions suscripciones consolidadas, $deletedDocs documentos duplicados eliminados."
        isCatalan -> "✨ Base de dades depurada: $subscriptions subscripcions consolidades, $deletedDocs documents duplicats eliminats."
        else -> "✨ Database cleaned up: $subscriptions subscriptions consolidated, $deletedDocs duplicate documents removed."
    }

    val cleanupUnknownError: String get() = when {
        isSpanish -> "Error desconocido al depurar"
        isCatalan -> "Error desconegut en depurar"
        else -> "Unknown error while cleaning up"
    }

    fun cleanupError(detail: String): String = when {
        isSpanish -> "⚠️ Error al depurar: $detail"
        isCatalan -> "⚠️ Error en depurar: $detail"
        else -> "⚠️ Error cleaning up: $detail"
    }

    // ==========================================
    // BATCH 9: ADD / EDIT MEMBER DIALOG
    // ==========================================
    val memberFormNameHeader: String get() = when {
        isSpanish -> "NOMBRE O APODO *"
        isCatalan -> "NOM O SOBRENOM *"
        else -> "NAME OR NICKNAME *"
    }

    val memberFormNamePlaceholder: String get() = when {
        isSpanish -> "Ej. Daniel Álvarez (danielalvarez)"
        isCatalan -> "Ex. Daniel Álvarez (danielalvarez)"
        else -> "E.g. Daniel Álvarez (danielalvarez)"
    }

    val memberFormNameRequired: String get() = when {
        isSpanish -> "El nombre es obligatorio"
        isCatalan -> "El nom és obligatori"
        else -> "Name is required"
    }

    val memberFormPlatformHeader: String get() = when {
        isSpanish -> "PLATAFORMA DE COMPARTICIÓN"
        isCatalan -> "PLATAFORMA DE COMPARTICIÓ"
        else -> "SHARING PLATFORM"
    }

    val memberFormSelectPlatform: String get() = when {
        isSpanish -> "Seleccionar plataforma configurada..."
        isCatalan -> "Seleccionar plataforma configurada..."
        else -> "Select configured platform..."
    }

    val memberFormNoPlatformsConfigured: String get() = when {
        isSpanish -> "Sin plataformas configuradas en la suscripción"
        isCatalan -> "Sense plataformes configurades a la subscripció"
        else -> "No platforms configured in subscription"
    }

    val memberFormAddPlatformRatesHint: String get() = when {
        isSpanish -> "Añade tarifas de plataforma editando la suscripción para asignarlas aquí."
        isCatalan -> "Afegeix tarifes de plataforma editant la subscripció per assignar-les aquí."
        else -> "Add platform rates by editing the subscription to assign them here."
    }

    val memberFormNoPlatform: String get() = when {
        isSpanish -> "Sin plataforma"
        isCatalan -> "Sense plataforma"
        else -> "No platform"
    }

    val memberFormContributionHeader: String get() = when {
        isSpanish -> "IMPORTE APORTADO /mes"
        isCatalan -> "IMPORT APORTAT /mes"
        else -> "CONTRIBUTION AMOUNT /mo"
    }

    val memberFormMethodHeader: String get() = when {
        isSpanish -> "MÉTODO HABITUAL"
        isCatalan -> "MÈTODE HABITUAL"
        else -> "USUAL METHOD"
    }

    val memberFormMethodPlaceholder: String get() = when {
        isSpanish -> "Ej. Transferencia Sharesub a Revolut, Bizum..."
        isCatalan -> "Ex. Transferència Sharesub a Revolut, Bizum..."
        else -> "E.g. Sharesub transfer to Revolut, Bizum..."
    }

    val memberFormJoinDate: String get() = when {
        isSpanish -> "Fecha de unión"
        isCatalan -> "Data d'unió"
        else -> "Join date"
    }

    val memberFormChangeDate: String get() = when {
        isSpanish -> "Cambiar fecha"
        isCatalan -> "Canviar data"
        else -> "Change date"
    }

    val memberFormNextPaymentDate: String get() = when {
        isSpanish -> "Fecha próximo pago"
        isCatalan -> "Data proper pagament"
        else -> "Next payment date"
    }

    val memberFormNextPaymentHint: String get() = when {
        isSpanish -> "Calculada según la periodicidad o editable a mano."
        isCatalan -> "Calculada segons la periodicitat o editable a mà."
        else -> "Calculated based on frequency or editable manually."
    }

    val memberFormFrequencyTitle: String get() = when {
        isSpanish -> "Frecuencia del pago"
        isCatalan -> "Freqüència del pagament"
        else -> "Payment frequency"
    }

    val memberFormFrequencyAuto: String get() = when {
        isSpanish -> "Automático"
        isCatalan -> "Automàtic"
        else -> "Automatic"
    }

    val memberFormEveryHeader: String get() = when {
        isSpanish -> "CADA"
        isCatalan -> "CADA"
        else -> "EVERY"
    }

    val memberFormIncrease: String get() = when {
        isSpanish -> "Incrementar"
        isCatalan -> "Incrementar"
        else -> "Increase"
    }

    val memberFormDecrease: String get() = when {
        isSpanish -> "Decrementar"
        isCatalan -> "Decrementar"
        else -> "Decrease"
    }

    val memberFormUnitHeader: String get() = when {
        isSpanish -> "UNIDAD"
        isCatalan -> "UNITAT"
        else -> "UNIT"
    }

    fun memberFrequencyUnitLabel(unitKey: String): String = when (unitKey.lowercase()) {
        "days", "dias", "días" -> when {
            isSpanish -> "Día(s)"
            isCatalan -> "Dia(es)"
            else -> "Day(s)"
        }
        "weeks", "semanas" -> when {
            isSpanish -> "Semana(s)"
            isCatalan -> "Setmana(es)"
            else -> "Week(s)"
        }
        "years", "anos", "años" -> when {
            isSpanish -> "Año(s)"
            isCatalan -> "Any(s)"
            else -> "Year(s)"
        }
        else -> when {
            isSpanish -> "Mes(es)"
            isCatalan -> "Mes(os)"
            else -> "Month(s)"
        }
    }

    fun memberFormRepeatsEvery(count: Int, unitLabel: String): String = when {
        isSpanish -> "• Se repite cada $count $unitLabel."
        isCatalan -> "• Es repeteix cada $count $unitLabel."
        else -> "• Repeats every $count $unitLabel."
    }

    val memberFormAlarmTitle: String get() = when {
        isSpanish -> "Alarma de aviso para el gestor"
        isCatalan -> "Alarma d'avís per al gestor"
        else -> "Reminder alarm for manager"
    }

    val memberFormAlarmSubtitle: String get() = when {
        isSpanish -> "Notificación antes del vencimiento"
        isCatalan -> "Notificació abans del venciment"
        else -> "Notification before due date"
    }

    val memberFormAlarmSummarySameDay: String get() = when {
        isSpanish -> "🔔 Avisar el mismo día del cobro"
        isCatalan -> "🔔 Avisar el mateix dia del cobrament"
        else -> "🔔 Remind on the day of charge"
    }

    fun memberFormAlarmSummary(value: Int, unitLabel: String): String = when {
        isSpanish -> "🔔 Avisar $value $unitLabel"
        isCatalan -> "🔔 Avisar $value $unitLabel"
        else -> "🔔 Remind $value $unitLabel"
    }

    val memberFormContactHeader: String get() = when {
        isSpanish -> "TELÉFONO O CORREO (OPCIONAL)"
        isCatalan -> "TELÈFON O CORREU (OPCIONAL)"
        else -> "PHONE OR EMAIL (OPTIONAL)"
    }

    val memberFormContactPlaceholder: String get() = when {
        isSpanish -> "Ej. +34 600 000 000 o email@ejemplo.com"
        isCatalan -> "Ex. +34 600 000 000 o email@exemple.com"
        else -> "E.g. +34 600 000 000 or email@example.com"
    }

    val memberFormNotesHeader: String get() = when {
        isSpanish -> "NOTAS O PERFIL ASIGNADO (OPCIONAL)"
        isCatalan -> "NOTES O PERFIL ASSIGNAT (OPCIONAL)"
        else -> "NOTES OR ASSIGNED PROFILE (OPTIONAL)"
    }

    val memberFormNotesPlaceholder: String get() = when {
        isSpanish -> "Ej. Perfil 3 (PIN: 1234)"
        isCatalan -> "Ex. Perfil 3 (PIN: 1234)"
        else -> "E.g. Profile 3 (PIN: 1234)"
    }

    val memberFormStatusHeader: String get() = when {
        isSpanish -> "ESTADOS Y ALERTAS DEL USUARIO"
        isCatalan -> "ESTATS I ALERTES DE L'USUARI"
        else -> "USER STATUS AND ALERTS"
    }

    val memberFormPendingPayment: String get() = when {
        isSpanish -> "Pendiente de pago"
        isCatalan -> "Pendent de pagament"
        else -> "Pending payment"
    }

    val memberFormPendingPaymentDesc: String get() = when {
        isSpanish -> "Marca al usuario si aún no ha realizado su pago"
        isCatalan -> "Marca l'usuari si encara no ha fet el seu pagament"
        else -> "Marks the user if they have not yet made their payment"
    }

    val memberFormPendingRemoval: String get() = when {
        isSpanish -> "Pendiente de eliminar"
        isCatalan -> "Pendent d'eliminar"
        else -> "Pending removal"
    }

    val memberFormPendingRemovalDesc: String get() = when {
        isSpanish -> "Indica que este usuario va a dejar la suscripción"
        isCatalan -> "Indica que aquest usuari deixarà la subscripció"
        else -> "Indicates that this user is going to leave the subscription"
    }

    val memberFormPendingActivation: String get() = when {
        isSpanish -> "Pendiente dar de alta"
        isCatalan -> "Pendent de donar d'alta"
        else -> "Pending activation"
    }

    val memberFormPendingActivationDesc: String get() = when {
        isSpanish -> "Para miembros que aún no están activos en la cuenta"
        isCatalan -> "Per a membres que encara no estan actius al compte"
        else -> "For members who are not yet active on the account"
    }

    val deleteMemberConfirmTitle: String get() = when {
        isSpanish -> "¿Eliminar usuario?"
        isCatalan -> "Eliminar usuari?"
        else -> "Delete user?"
    }

    fun deleteMemberConfirmMessage(memberName: String): String = when {
        isSpanish -> "¿Estás seguro de que deseas eliminar a '$memberName' de esta suscripción? Esta acción no se puede deshacer."
        isCatalan -> "Segur que vols eliminar a '$memberName' d'aquesta subscripció? Aquesta acció no es pot desfer."
        else -> "Are you sure you want to delete '$memberName' from this subscription? This action cannot be undone."
    }

    // ==========================================
    // BATCH 10: INVITE SHARE MESSAGE, RESTORE RESULT & BACKUP ERRORS
    // ==========================================
    val inviteShareIntro: String get() = when {
        isSpanish -> "Te invito a compartir gastos en Apleq."
        isCatalan -> "Et convido a compartir despeses a Apleq."
        else -> "I'm inviting you to share expenses on Apleq."
    }

    fun inviteShareCode(code: String): String = when {
        isSpanish -> "Únete con este código: $code"
        isCatalan -> "Uneix-t'hi amb aquest codi: $code"
        else -> "Join with this code: $code"
    }

    fun inviteShareDownload(url: String): String = when {
        isSpanish -> "Descarga la app: $url"
        isCatalan -> "Descarrega l'app: $url"
        else -> "Download the app: $url"
    }

    fun inviteShareWeb(url: String): String = when {
        isSpanish -> "O regístrate en la web: $url"
        isCatalan -> "O registra't al web: $url"
        else -> "Or sign up on the web: $url"
    }

    val inviteShareSubject: String get() = when {
        isSpanish -> "Invitación a Apleq"
        isCatalan -> "Invitació a Apleq"
        else -> "Invitation to Apleq"
    }

    fun restoreSuccessMessage(subscriptions: Int, members: Int): String = when {
        isSpanish -> "¡Restauración completada con éxito! ($subscriptions suscripciones, $members miembros)"
        isCatalan -> "Restauració completada amb èxit! ($subscriptions subscripcions, $members membres)"
        else -> "Restore completed successfully! ($subscriptions subscriptions, $members members)"
    }

    fun restoreErrorMessage(detail: String?): String = when {
        isSpanish -> "Error al restaurar: $detail"
        isCatalan -> "Error en restaurar: $detail"
        else -> "Restore error: $detail"
    }

    val backupUnknownDate: String get() = when {
        isSpanish -> "Fecha desconocida"
        isCatalan -> "Data desconeguda"
        else -> "Unknown date"
    }

    val backupFileInvalidOrCorrupt: String get() = when {
        isSpanish -> "Archivo de copia de seguridad no válido o dañado"
        isCatalan -> "Arxiu de còpia de seguretat no vàlid o malmès"
        else -> "Invalid or corrupted backup file"
    }

    val backupInvalidFileFormat: String get() = when {
        isSpanish -> "Formato de archivo inválido (sin suscripciones)"
        isCatalan -> "Format d'arxiu invàlid (sense subscripcions)"
        else -> "Invalid file format (no subscriptions)"
    }

    val backupProcessingError: String get() = when {
        isSpanish -> "Error al procesar la copia de seguridad"
        isCatalan -> "Error en processar la còpia de seguretat"
        else -> "Error processing the backup"
    }
}



