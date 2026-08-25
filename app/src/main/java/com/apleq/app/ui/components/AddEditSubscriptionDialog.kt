package com.apleq.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apleq.app.data.local.SharingPlatformEntity
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.data.model.BillingPeriod
import com.apleq.app.data.model.CurrencyItem
import com.apleq.app.data.model.CurrencyManager
import com.apleq.app.data.model.IconLibrary
import com.apleq.app.data.model.PlatformPriceItem
import com.apleq.app.data.model.PlatformPricingHelper
import com.apleq.app.data.model.SharingPlatforms
import com.apleq.app.ui.util.ImageStorageHelper
import kotlinx.coroutines.delay
import java.util.Locale

data class ConfiguredPlatformUi(
    val platformName: String,
    val price: String,
    val currency: String = "EUR",
    val billingPeriod: BillingPeriod = BillingPeriod.MONTHLY
)

val availableCategories = listOf(
    "Streaming",
    "Música",
    "Productividad",
    "Gaming",
    "Educación",
    "Salud",
    "Estilo de vida",
    "Seguridad",
    "Finanzas",
    "General"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionDialog(
    subscriptionToEdit: SubscriptionEntity?,
    onDismiss: () -> Unit,
    onSave: (SubscriptionEntity) -> Unit,
    onDelete: ((SubscriptionEntity) -> Unit)? = null,
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEditing = subscriptionToEdit != null

    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    // Campos completamente vacíos para nueva suscripción
    var platformName by remember {
        mutableStateOf(subscriptionToEdit?.platformName ?: "")
    }
    var mainUserName by remember {
        mutableStateOf(subscriptionToEdit?.mainUserName ?: "")
    }
    var mainUserContact by remember {
        mutableStateOf(subscriptionToEdit?.mainUserContact ?: "")
    }
    var costText by remember {
        mutableStateOf(
            if (subscriptionToEdit != null) {
                String.format(Locale.US, "%.2f", subscriptionToEdit.cost)
            } else {
                ""
            }
        )
    }

    // Plataformas de compartición con precios, divisas y frecuencias independientes
    var configuredPlatforms by remember {
        mutableStateOf<List<ConfiguredPlatformUi>>(
            if (subscriptionToEdit != null) {
                val parsed = PlatformPricingHelper.parse(subscriptionToEdit.platformPricing)
                if (parsed.isNotEmpty()) {
                    parsed.map {
                        ConfiguredPlatformUi(
                            platformName = it.platformName,
                            price = if (it.pricePerUser > 0) String.format(Locale.US, "%.2f", it.pricePerUser) else "",
                            currency = it.currency,
                            billingPeriod = BillingPeriod.fromKey(it.billingPeriod)
                        )
                    }
                } else if (subscriptionToEdit.defaultContributionPerUser > 0) {
                    listOf(
                        ConfiguredPlatformUi(
                            platformName = "Sharesub",
                            price = String.format(Locale.US, "%.2f", subscriptionToEdit.defaultContributionPerUser),
                            currency = "EUR",
                            billingPeriod = BillingPeriod.MONTHLY
                        )
                    )
                } else {
                    emptyList()
                }
            } else {
                emptyList()
            }
        )
    }

    // Estado del diálogo para Añadir / Editar una plataforma de compartición
    var editingPlatformIndex by remember { mutableStateOf<Int?>(null) } // null = cerrado, -1 = nueva plataforma, >=0 = editar existente
    var editModalPlatformName by remember { mutableStateOf("") }
    var editModalPriceText by remember { mutableStateOf("") }
    var editModalCurrency by remember { mutableStateOf(CurrencyManager.findCurrency("EUR")) }
    var editModalBillingPeriod by remember { mutableStateOf(BillingPeriod.MONTHLY) }
    var editModalPlatformDropdownExpanded by remember { mutableStateOf(false) }
    var editModalCurrencyDropdownExpanded by remember { mutableStateOf(false) }
    var editModalPeriodDropdownExpanded by remember { mutableStateOf(false) }
    var editModalCustomPlatformInput by remember { mutableStateOf(false) }
    var editModalCustomPlatformName by remember { mutableStateOf("") }
    var editModalError by remember { mutableStateOf(false) }

    var billingDayText by remember {
        mutableStateOf(
            if (subscriptionToEdit != null) {
                subscriptionToEdit.billingDay.toString()
            } else {
                ""
            }
        )
    }
    var category by remember {
        mutableStateOf(subscriptionToEdit?.category ?: "Streaming")
    }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    var billingPeriod by remember {
        mutableStateOf(BillingPeriod.fromKey(subscriptionToEdit?.billingPeriod))
    }
    var billingPeriodDropdownExpanded by remember { mutableStateOf(false) }

    var billingMonth by remember {
        mutableStateOf<Int?>(subscriptionToEdit?.billingMonth)
    }
    var monthDropdownExpanded by remember { mutableStateOf(false) }

    var selectedCurrency by remember {
        mutableStateOf(CurrencyManager.findCurrency(subscriptionToEdit?.currency ?: "EUR"))
    }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    // Alarma para el cobro de la suscripción
    var enableAlarm by remember {
        mutableStateOf(subscriptionToEdit?.enableAlarm ?: false)
    }
    var alarmValue by remember {
        mutableIntStateOf(subscriptionToEdit?.alarmValue ?: 0)
    }
    var alarmUnit by remember {
        mutableStateOf(subscriptionToEdit?.alarmUnit ?: "same_day")
    }
    var alarmUnitDropdownExpanded by remember { mutableStateOf(false) }

    var notes by remember {
        mutableStateOf(subscriptionToEdit?.notes ?: "")
    }

    // Icon Selection States
    var iconType by remember {
        mutableStateOf(subscriptionToEdit?.iconType ?: "VECTOR")
    }
    var iconKey by remember {
        mutableStateOf(subscriptionToEdit?.iconKey ?: "subscriptions")
    }
    var customImageUri by remember {
        mutableStateOf(subscriptionToEdit?.customImageUri ?: "")
    }
    var iconColorHex by remember {
        mutableStateOf(subscriptionToEdit?.iconColorHex ?: "#6366F1")
    }

    // Modal / Sub-diálogo para personalizar el icono cuando se pulsa sobre él
    var showIconPickerModal by remember { mutableStateOf(false) }

    var costError by remember { mutableStateOf(false) }
    var mainUserError by remember { mutableStateOf(false) }
    var platformNameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Subscriptions,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEditing) "Editar Suscripción" else "Nueva Suscripción",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp)
            ) {
                // Header: Icono genérico interactivo + Nombre de la suscripción
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showIconPickerModal = true }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            PlatformIconBadge(
                                platformName = platformName.ifBlank { "Suscripción" },
                                iconType = iconType,
                                iconKey = iconKey,
                                customImageUri = customImageUri,
                                iconColorHex = iconColorHex,
                                size = 56.dp,
                                iconSize = 30.dp
                            )
                            // Pequeño indicador de edición sobre el icono
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Cambiar icono",
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = platformName.ifBlank { "Nombre del servicio / suscripción" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (platformName.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Toca aquí para personalizar icono o imagen",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Nombre del servicio / suscripción
                OutlinedTextField(
                    value = platformName,
                    onValueChange = {
                        platformName = it
                        platformNameError = it.isBlank()
                    },
                    label = { Text("Nombre del servicio / suscripción *") },
                    placeholder = { Text("Ej: Netflix, Spotify, ChatGPT, Gimnasio, Canal+...") },
                    isError = platformNameError,
                    supportingText = if (platformNameError) {
                        { Text("El nombre del servicio es obligatorio") }
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sub_platform_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Desplegable para elegir la categoría de la suscripción
                ExposedDropdownMenuBox(
                    expanded = categoryDropdownExpanded,
                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría de la suscripción *") },
                        leadingIcon = { Icon(Icons.Default.Category, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("input_sub_category_dropdown"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = categoryDropdownExpanded,
                        onDismissRequest = { categoryDropdownExpanded = false }
                    ) {
                        availableCategories.forEach { catOption ->
                            DropdownMenuItem(
                                text = { Text(catOption) },
                                onClick = {
                                    category = catOption
                                    categoryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Titular / Usuario Principal (vacío por defecto)
                OutlinedTextField(
                    value = mainUserName,
                    onValueChange = {
                        mainUserName = it
                        mainUserError = it.isBlank()
                    },
                    label = { Text("Titular / Usuario Principal *") },
                    placeholder = { Text("Ej: Carlos (Yo)") },
                    isError = mainUserError,
                    supportingText = if (mainUserError) {
                        { Text("El titular es obligatorio") }
                    } else null,
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_main_user_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 4. Dinero que me cuesta a mí (con selector de divisas y conversión a euros en la misma línea)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OutlinedTextField(
                        value = costText,
                        onValueChange = {
                            costText = it
                            costError = it.replace(',', '.').toDoubleOrNull() == null
                        },
                        label = { Text("Precio (${selectedCurrency.symbol}${billingPeriod.suffix}) *") },
                        placeholder = { Text(if (billingPeriod == BillingPeriod.YEARLY) "119.99" else "17.99") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = costError,
                        supportingText = if (costError) {
                            { Text("Introduce un importe numérico") }
                        } else null,
                        modifier = Modifier
                            .weight(0.50f)
                            .testTag("input_sub_cost"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Selector de moneda
                    ExposedDropdownMenuBox(
                        expanded = currencyDropdownExpanded,
                        onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                        modifier = Modifier.weight(0.50f)
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.flag} ${selectedCurrency.code} (${selectedCurrency.symbol})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Moneda") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("dropdown_currency_selector"),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = currencyDropdownExpanded,
                            onDismissRequest = { currencyDropdownExpanded = false },
                            modifier = Modifier.widthIn(min = 180.dp)
                        ) {
                            CurrencyManager.currencies.forEach { curr ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${curr.flag} ${curr.code} (${curr.symbol})",
                                            maxLines = 1,
                                            softWrap = false,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (curr.code == selectedCurrency.code) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    onClick = {
                                        selectedCurrency = curr
                                        currencyDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Conversión actualizada en todo momento a Euros (€)
                val parsedCost = costText.replace(',', '.').toDoubleOrNull()
                if (parsedCost != null && parsedCost > 0.0) {
                    val costInEur = CurrencyManager.convertToEur(parsedCost, selectedCurrency.code)
                    val monthlyCostInEur = billingPeriod.toMonthlyCost(costInEur)
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedCurrency.code != "EUR") MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            if (selectedCurrency.code != "EUR") {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CurrencyExchange,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Conversión a Euros: ≈ ${CurrencyManager.formatInEur(costInEur)}",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                                Text(
                                    text = "Tipo de cambio: 1 ${selectedCurrency.code} ≈ ${String.format(Locale.getDefault(), "%.4f", CurrencyManager.getRateToEur(selectedCurrency.code))} €",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                            if (billingPeriod != BillingPeriod.MONTHLY) {
                                if (selectedCurrency.code != "EUR") Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Equivalente mensual: ≈ ${CurrencyManager.formatInEur(monthlyCostInEur)} / mes",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Frecuencia / Periodo de cobro (Desplegable: Mensual, Trimestral, Semestral, Anual)
                ExposedDropdownMenuBox(
                    expanded = billingPeriodDropdownExpanded,
                    onExpandedChange = { billingPeriodDropdownExpanded = !billingPeriodDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = billingPeriod.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Frecuencia / Periodo de cobro *") },
                        leadingIcon = { Icon(Icons.Default.Timelapse, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = billingPeriodDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .testTag("dropdown_billing_period"),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = billingPeriodDropdownExpanded,
                        onDismissRequest = { billingPeriodDropdownExpanded = false }
                    ) {
                        BillingPeriod.list.forEach { periodOption ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = periodOption.label,
                                            fontWeight = if (billingPeriod == periodOption) FontWeight.Bold else FontWeight.Normal
                                        )
                                        if (billingPeriod == periodOption) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    billingPeriod = periodOption
                                    billingPeriodDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 6. Día y mes de cobro / ciclo de facturación
                if (billingPeriod == BillingPeriod.MONTHLY) {
                    OutlinedTextField(
                        value = billingDayText,
                        onValueChange = {
                            billingDayText = it.filter { ch -> ch.isDigit() }.take(2)
                        },
                        label = { Text("Día de cobro (1-31)") },
                        placeholder = { Text("Día (1-31)") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_billing_day"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = billingDayText,
                            onValueChange = {
                                billingDayText = it.filter { ch -> ch.isDigit() }.take(2)
                            },
                            label = { Text("Día (1-31)") },
                            placeholder = { Text("Día") },
                            leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(0.40f)
                                .testTag("input_billing_day"),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = monthDropdownExpanded,
                            onExpandedChange = { monthDropdownExpanded = !monthDropdownExpanded },
                            modifier = Modifier.weight(0.60f)
                        ) {
                            OutlinedTextField(
                                value = if (billingMonth != null) BillingPeriod.getMonthName(billingMonth!!) else "",
                                onValueChange = {},
                                readOnly = true,
                                label = {
                                    Text(
                                        when (billingPeriod) {
                                            BillingPeriod.YEARLY -> "Mes renovación"
                                            BillingPeriod.SEMI_ANNUAL -> "Mes inicio"
                                            BillingPeriod.QUARTERLY -> "Mes inicio"
                                            else -> "Mes cobro"
                                        }
                                    )
                                },
                                placeholder = { Text("Seleccionar mes") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = monthDropdownExpanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_billing_month"),
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = monthDropdownExpanded,
                                onDismissRequest = { monthDropdownExpanded = false }
                            ) {
                                BillingPeriod.months.forEach { monthItem ->
                                    DropdownMenuItem(
                                        text = { Text("${monthItem.number}. ${monthItem.name}") },
                                        onClick = {
                                            billingMonth = monthItem.number
                                            monthDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (billingMonth != null || billingDayText.isNotBlank()) {
                        val currentDayVal = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1
                        val currentMonthVal = billingMonth ?: 1
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "📅 ${BillingPeriod.formatSchedule(currentDayVal, currentMonthVal, billingPeriod)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 6.5. Alarma de aviso para el cobro de la suscripción
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = if (enableAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Alarma de cobro de suscripción",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Notificación antes de la fecha de cobro",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Switch(
                                checked = enableAlarm,
                                onCheckedChange = { enableAlarm = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("switch_subscription_alarm")
                            )
                        }

                        if (enableAlarm) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (alarmUnit != "same_day") {
                                    Column(modifier = Modifier.weight(0.8f)) {
                                        Text(
                                            text = "CANTIDAD",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        OutlinedTextField(
                                            value = alarmValue.toString(),
                                            onValueChange = { str ->
                                                val num = str.filter { it.isDigit() }.toIntOrNull()
                                                if (num != null) alarmValue = num
                                                else if (str.isBlank()) alarmValue = 0
                                            },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("input_subscription_alarm_value"),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = "UNIDAD DE ANTELACIÓN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    ExposedDropdownMenuBox(
                                        expanded = alarmUnitDropdownExpanded,
                                        onExpandedChange = { alarmUnitDropdownExpanded = !alarmUnitDropdownExpanded },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        OutlinedTextField(
                                            value = getSubscriptionAlarmUnitLabel(alarmUnit),
                                            onValueChange = {},
                                            readOnly = true,
                                            trailingIcon = {
                                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = alarmUnitDropdownExpanded)
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .menuAnchor()
                                                .testTag("dropdown_subscription_alarm_unit"),
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp)
                                        )
                                        ExposedDropdownMenu(
                                            expanded = alarmUnitDropdownExpanded,
                                            onDismissRequest = { alarmUnitDropdownExpanded = false }
                                        ) {
                                            listOf("same_day", "hours", "days", "weeks", "months").forEach { aKey ->
                                                DropdownMenuItem(
                                                    text = { Text(getSubscriptionAlarmUnitLabel(aKey)) },
                                                    onClick = {
                                                        alarmUnit = aKey
                                                        if (aKey == "same_day") alarmValue = 0
                                                        else if (alarmValue == 0) alarmValue = 1
                                                        alarmUnitDropdownExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (alarmUnit == "same_day") "🔔 Avisar el mismo día del cobro"
                                else "🔔 Avisar $alarmValue ${getSubscriptionAlarmUnitLabel(alarmUnit).lowercase()}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7. Plataformas de compartición y precio por usuario independiente (Hasta 3)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "Plataformas de compartición",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Configura hasta 3 plataformas con su precio, divisa y periodicidad",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (configuredPlatforms.isNotEmpty()) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = "${configuredPlatforms.size}/3",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (configuredPlatforms.isNotEmpty()) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Botón para Añadir plataforma de compartición
                        if (configuredPlatforms.size < 3) {
                            OutlinedButton(
                                onClick = {
                                    editModalPlatformName = ""
                                    editModalPriceText = ""
                                    editModalCurrency = CurrencyManager.findCurrency("EUR")
                                    editModalBillingPeriod = BillingPeriod.MONTHLY
                                    editModalCustomPlatformInput = false
                                    editModalCustomPlatformName = ""
                                    editModalError = false
                                    editingPlatformIndex = -1
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_add_platform_modal"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Añadir plataforma de compartición", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lista de tarjetas para ver y editar las plataformas configuradas
                        if (configuredPlatforms.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "No has configurado plataformas de compartición aún. Pulsa el botón de arriba para añadir una plataforma con su precio, divisa y periodicidad.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            configuredPlatforms.forEachIndexed { index, item ->
                                val pInfo = SharingPlatforms.getInfo(item.platformName, availablePlatforms)
                                val itemCurrency = CurrencyManager.findCurrency(item.currency)
                                val itemPeriod = item.billingPeriod
                                val formattedDisplayPrice = if (item.price.isNotBlank()) {
                                    val numericP = item.price.replace(',', '.').toDoubleOrNull() ?: 0.0
                                    "${String.format(Locale.getDefault(), "%.2f", numericP)} ${itemCurrency.symbol}${itemPeriod.suffix}"
                                } else {
                                    "0.00 ${itemCurrency.symbol}${itemPeriod.suffix}"
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            editModalPlatformName = item.platformName
                                            editModalPriceText = item.price
                                            editModalCurrency = itemCurrency
                                            editModalBillingPeriod = itemPeriod
                                            editModalCustomPlatformInput = false
                                            editModalCustomPlatformName = ""
                                            editModalError = false
                                            editingPlatformIndex = index
                                        }
                                        .testTag("platform_item_row_$index")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(pInfo.baseColor)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.platformName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                                modifier = Modifier.padding(top = 2.dp)
                                            ) {
                                                Text(
                                                    text = formattedDisplayPrice,
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Botón Editar plataforma
                                        IconButton(
                                            onClick = {
                                                editModalPlatformName = item.platformName
                                                editModalPriceText = item.price
                                                editModalCurrency = itemCurrency
                                                editModalBillingPeriod = itemPeriod
                                                editModalCustomPlatformInput = false
                                                editModalCustomPlatformName = ""
                                                editModalError = false
                                                editingPlatformIndex = index
                                            },
                                            modifier = Modifier.testTag("btn_edit_platform_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar plataforma",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        // Botón Eliminar plataforma
                                        IconButton(
                                            onClick = {
                                                configuredPlatforms = configuredPlatforms.filterIndexed { i, _ -> i != index }
                                            },
                                            modifier = Modifier.testTag("btn_delete_platform_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar plataforma",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 8. Notas adicionales
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas adicionales (opcional)") },
                    placeholder = { Text("Ej: Acceso de administrador, Bizum...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_sub_notes"),
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isEditing && onDelete != null) {
                    IconButton(
                        onClick = { showConfirmDeleteDialog = true },
                        modifier = Modifier.testTag("delete_sub_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar Suscripción",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("cancel_sub_btn")
                    ) {
                        Text("Cancelar")
                    }

                    Button(
                        onClick = {
                            val cost = costText.replace(',', '.').toDoubleOrNull()
                            if (platformName.isBlank()) {
                                platformNameError = true
                                return@Button
                            }
                            if (mainUserName.isBlank()) {
                                mainUserError = true
                                return@Button
                            }
                            if (cost == null || cost <= 0.0) {
                                costError = true
                                return@Button
                            }

                            val finalPlatformItems = configuredPlatforms.mapNotNull { item ->
                                val p = item.price.replace(',', '.').toDoubleOrNull() ?: 0.0
                                if (item.platformName.isNotBlank()) {
                                    PlatformPriceItem(
                                        platformName = item.platformName.trim(),
                                        pricePerUser = p,
                                        currency = item.currency.trim().uppercase(),
                                        billingPeriod = item.billingPeriod.key
                                    )
                                } else null
                            }
                            val platformPricingString = PlatformPricingHelper.serialize(finalPlatformItems)
                            val defaultContribution = finalPlatformItems.firstOrNull()?.pricePerUser ?: 0.0
                            val billingDay = billingDayText.toIntOrNull()?.coerceIn(1, 31) ?: 1

                            val finalIconType = if (iconType == "CUSTOM_IMAGE" && customImageUri.isNotBlank()) {
                                "CUSTOM_IMAGE"
                            } else {
                                "VECTOR"
                            }

                            val entity = SubscriptionEntity(
                                id = subscriptionToEdit?.id ?: 0L,
                                platformName = platformName.trim(),
                                customPlanName = "",
                                mainUserName = mainUserName.trim(),
                                mainUserContact = mainUserContact.trim(),
                                cost = cost,
                                billingPeriod = billingPeriod.key,
                                billingDay = billingDay,
                                billingMonth = billingMonth ?: 1,
                                currency = selectedCurrency.code,
                                defaultContributionPerUser = defaultContribution,
                                platformPricing = platformPricingString,
                                category = category,
                                notes = notes.trim(),
                                iconType = finalIconType,
                                iconKey = iconKey,
                                customImageUri = customImageUri,
                                iconColorHex = iconColorHex,
                                enableAlarm = enableAlarm,
                                alarmValue = alarmValue,
                                alarmUnit = alarmUnit,
                                alarmDaysBefore = if (alarmUnit == "days") alarmValue else 0,
                                createdAt = subscriptionToEdit?.createdAt ?: System.currentTimeMillis()
                            )
                            onSave(entity)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_sub_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isEditing) "Guardar Cambios" else "Crear Suscripción")
                    }
                }
            }
        },
        dismissButton = null,
        modifier = modifier
    )

    // Diálogo de confirmación para eliminar la suscripción
    if (showConfirmDeleteDialog && subscriptionToEdit != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("Eliminar Suscripción") },
            text = {
                Text("¿Estás seguro de que deseas eliminar la suscripción a \"${subscriptionToEdit.platformName}\"? Esta acción no se puede deshacer.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        onDelete?.invoke(subscriptionToEdit)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Modal / Diálogo para Añadir o Editar una plataforma de compartición
    if (editingPlatformIndex != null) {
        val isNew = editingPlatformIndex == -1
        val availableQuickPlatforms = if (availablePlatforms.isNotEmpty()) {
            availablePlatforms.map { it.name }
        } else {
            listOf("Sharesub", "Together Price", "Sharingful", "Spliiit", "GamsGo", "Directo / Familia")
        }

        AlertDialog(
            onDismissRequest = { editingPlatformIndex = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isNew) Icons.Default.Add else Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNew) "Añadir Plataforma de Compartición" else "Editar Plataforma de Compartición",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Selector de Nombre de Plataforma
                    Column {
                        Text(
                            text = "Plataforma de compartición *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        if (editModalCustomPlatformInput) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = editModalCustomPlatformName,
                                    onValueChange = {
                                        editModalCustomPlatformName = it
                                        editModalPlatformName = it
                                        editModalError = false
                                    },
                                    placeholder = { Text("Nombre de la plataforma...") },
                                    isError = editModalError && editModalPlatformName.isBlank(),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("input_modal_custom_platform_name"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(
                                    onClick = { editModalCustomPlatformInput = false }
                                ) {
                                    Text("Lista")
                                }
                            }
                        } else {
                            ExposedDropdownMenuBox(
                                expanded = editModalPlatformDropdownExpanded,
                                onExpandedChange = { editModalPlatformDropdownExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = editModalPlatformName.ifBlank { "Seleccionar plataforma..." },
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editModalPlatformDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("dropdown_modal_platform_select"),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                ExposedDropdownMenu(
                                    expanded = editModalPlatformDropdownExpanded,
                                    onDismissRequest = { editModalPlatformDropdownExpanded = false }
                                ) {
                                    availableQuickPlatforms.forEach { pName ->
                                        val pInfo = SharingPlatforms.getInfo(pName, availablePlatforms)
                                        DropdownMenuItem(
                                            text = { Text(pName) },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(12.dp)
                                                        .clip(CircleShape)
                                                        .background(pInfo.baseColor)
                                                )
                                            },
                                            onClick = {
                                                editModalPlatformName = pName
                                                editModalError = false
                                                editModalPlatformDropdownExpanded = false
                                            }
                                        )
                                    }
                                    HorizontalDivider()
                                    DropdownMenuItem(
                                        text = { Text("+ Otra personalizada...") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        },
                                        onClick = {
                                            editModalCustomPlatformInput = true
                                            editModalCustomPlatformName = ""
                                            editModalPlatformName = ""
                                            editModalPlatformDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 2. Precio y Moneda en la misma línea
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(0.50f)) {
                            Text(
                                text = "Precio / slot *",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = editModalPriceText,
                                onValueChange = { editModalPriceText = it },
                                placeholder = { Text("0.00") },
                                leadingIcon = {
                                    Text(
                                        text = editModalCurrency.symbol,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(start = 12.dp)
                                    )
                                },
                                trailingIcon = {
                                    Text(
                                        text = editModalBillingPeriod.suffix,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_modal_platform_price")
                            )
                        }

                        Column(modifier = Modifier.weight(0.50f)) {
                            Text(
                                text = "Moneda *",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))

                            ExposedDropdownMenuBox(
                                expanded = editModalCurrencyDropdownExpanded,
                                onExpandedChange = { editModalCurrencyDropdownExpanded = it },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = "${editModalCurrency.flag} ${editModalCurrency.code} (${editModalCurrency.symbol})",
                                    onValueChange = {},
                                    readOnly = true,
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editModalCurrencyDropdownExpanded) },
                                    modifier = Modifier
                                        .menuAnchor()
                                        .fillMaxWidth()
                                        .testTag("dropdown_modal_platform_currency"),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true
                                )

                                ExposedDropdownMenu(
                                    expanded = editModalCurrencyDropdownExpanded,
                                    onDismissRequest = { editModalCurrencyDropdownExpanded = false }
                                ) {
                                    CurrencyManager.currencies.forEach { currencyItem ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = "${currencyItem.flag} ${currencyItem.code} (${currencyItem.symbol})",
                                                    maxLines = 1,
                                                    softWrap = false,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = if (currencyItem.code == editModalCurrency.code) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                editModalCurrency = currencyItem
                                                editModalCurrencyDropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Periodicidad de cobro / pago de la plataforma
                    Column {
                        Text(
                            text = "Frecuencia de pago de la plataforma *",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        ExposedDropdownMenuBox(
                            expanded = editModalPeriodDropdownExpanded,
                            onExpandedChange = { editModalPeriodDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = editModalBillingPeriod.label,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = editModalPeriodDropdownExpanded) },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                                    .testTag("dropdown_modal_platform_period"),
                                shape = RoundedCornerShape(10.dp)
                            )

                            ExposedDropdownMenu(
                                expanded = editModalPeriodDropdownExpanded,
                                onDismissRequest = { editModalPeriodDropdownExpanded = false }
                            ) {
                                BillingPeriod.entries.forEach { periodItem ->
                                    DropdownMenuItem(
                                        text = { Text("${periodItem.label} (${periodItem.suffix})") },
                                        onClick = {
                                            editModalBillingPeriod = periodItem
                                            editModalPeriodDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 5. Tarjeta de vista previa
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val displayNum = editModalPriceText.replace(',', '.').toDoubleOrNull() ?: 0.0
                            Text(
                                text = "Tarifa: ${editModalPlatformName.ifBlank { "Plataforma" }} • ${String.format(Locale.getDefault(), "%.2f", displayNum)} ${editModalCurrency.symbol}${editModalBillingPeriod.suffix}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editModalPlatformName.isBlank()) {
                            editModalError = true
                            return@Button
                        }
                        val newItem = ConfiguredPlatformUi(
                            platformName = editModalPlatformName.trim(),
                            price = editModalPriceText.trim(),
                            currency = editModalCurrency.code,
                            billingPeriod = editModalBillingPeriod
                        )
                        if (isNew) {
                            if (configuredPlatforms.size < 3) {
                                configuredPlatforms = configuredPlatforms + newItem
                            }
                        } else {
                            val targetIdx = editingPlatformIndex!!
                            if (targetIdx in configuredPlatforms.indices) {
                                configuredPlatforms = configuredPlatforms.toMutableList().also {
                                    it[targetIdx] = newItem
                                }
                            }
                        }
                        editingPlatformIndex = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_save_modal_platform")
                ) {
                    Text(if (isNew) "Añadir a la lista" else "Actualizar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { editingPlatformIndex = null },
                    modifier = Modifier.testTag("btn_cancel_modal_platform")
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo emergente al hacer clic en el icono para elegir Biblioteca de Iconos o Subir Imagen
    if (showIconPickerModal) {
        IconPickerDialog(
            initialIconType = iconType,
            initialIconKey = iconKey,
            initialCustomImageUri = customImageUri,
            initialColorHex = iconColorHex,
            platformName = platformName,
            onDismiss = { showIconPickerModal = false },
            onIconSelected = { selectedType, selectedKey, selectedUri, selectedColor ->
                iconType = selectedType
                iconKey = selectedKey
                customImageUri = selectedUri
                iconColorHex = selectedColor
                showIconPickerModal = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun IconPickerDialog(
    initialIconType: String,
    initialIconKey: String,
    initialCustomImageUri: String,
    initialColorHex: String,
    platformName: String,
    onDismiss: () -> Unit,
    onIconSelected: (type: String, key: String, uri: String, colorHex: String) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember {
        mutableIntStateOf(if (initialIconType == "CUSTOM_IMAGE" && initialCustomImageUri.isNotBlank()) 1 else 0)
    }
    var tempIconType by remember { mutableStateOf(initialIconType) }
    var tempIconKey by remember { mutableStateOf(initialIconKey) }
    var tempCustomImageUri by remember { mutableStateOf(initialCustomImageUri) }
    var tempColorHex by remember { mutableStateOf(initialColorHex) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = ImageStorageHelper.saveImageFromUri(context, uri)
            if (savedPath != null) {
                tempCustomImageUri = savedPath
                tempIconType = "CUSTOM_IMAGE"
                selectedTab = 1
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Elegir Icono para la Suscripción",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Live preview
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        PlatformIconBadge(
                            platformName = platformName.ifBlank { "Icono" },
                            iconType = tempIconType,
                            iconKey = tempIconKey,
                            customImageUri = tempCustomImageUri,
                            iconColorHex = tempColorHex,
                            size = 50.dp,
                            iconSize = 26.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Vista Previa",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = when (tempIconType) {
                                    "CUSTOM_IMAGE" -> "Foto de la galería"
                                    else -> "Símbolo: $tempIconKey"
                                },
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = {
                            selectedTab = 0
                            tempIconType = "VECTOR"
                        },
                        text = { Text("Biblioteca de Iconos", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = {
                            selectedTab = 1
                            if (tempCustomImageUri.isNotBlank()) {
                                tempIconType = "CUSTOM_IMAGE"
                            }
                        },
                        text = { Text("Subir de la Galería", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // TAB 0: Biblioteca de Iconos y Colores
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Color de fondo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconLibrary.availableColors.forEach { colorOption ->
                                val isColorSelected = tempColorHex.equals(colorOption.hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(colorOption.color)
                                        .clickable {
                                            tempColorHex = colorOption.hex
                                            tempIconType = "VECTOR"
                                        }
                                        .then(
                                            if (isColorSelected) {
                                                Modifier.border(2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                            } else {
                                                Modifier
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isColorSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Seleccionado",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Seleccionar símbolo",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconLibrary.availableIcons.forEach { iconOption ->
                                val isIconSelected = tempIconType == "VECTOR" && tempIconKey.equals(iconOption.key, ignoreCase = true)
                                FilterChip(
                                    selected = isIconSelected,
                                    onClick = {
                                        tempIconKey = iconOption.key
                                        tempIconType = "VECTOR"
                                    },
                                    label = { Text(iconOption.label, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = iconOption.icon,
                                            contentDescription = iconOption.label,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                    }
                } else {
                    // TAB 1: Subir imagen de la galería
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (tempCustomImageUri.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PlatformIconBadge(
                                        platformName = platformName.ifBlank { "Icono" },
                                        iconType = "CUSTOM_IMAGE",
                                        customImageUri = tempCustomImageUri,
                                        size = 64.dp,
                                        cornerRadius = 16.dp
                                    )

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column {
                                        Button(
                                            onClick = { galleryLauncher.launch("image/*") },
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.testTag("change_gallery_image_modal_btn")
                                        ) {
                                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Cambiar")
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        TextButton(
                                            onClick = {
                                                tempCustomImageUri = ""
                                                tempIconType = "VECTOR"
                                                selectedTab = 0
                                            }
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Quitar imagen", color = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Elige una foto o logo desde tu dispositivo",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("upload_gallery_image_modal_btn")
                                ) {
                                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Seleccionar de la galería")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalType = if (tempIconType == "CUSTOM_IMAGE" && tempCustomImageUri.isNotBlank()) "CUSTOM_IMAGE" else "VECTOR"
                    onIconSelected(finalType, tempIconKey, tempCustomImageUri, tempColorHex)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Aplicar Icono")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun getSubscriptionAlarmUnitLabel(unitKey: String): String {
    return when (unitKey.lowercase()) {
        "same_day", "mismo_dia" -> "El mismo día"
        "hours", "horas" -> "Hora(s) antes"
        "weeks", "semanas" -> "Semana(s) antes"
        "months", "meses" -> "Mes(es) antes"
        else -> "Día(s) antes"
    }
}

