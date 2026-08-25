package com.apleq.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apleq.app.data.local.MemberEntity
import com.apleq.app.data.local.SharingPlatformEntity
import com.apleq.app.data.local.SubscriptionWithMembers
import com.apleq.app.data.model.CurrencyManager
import com.apleq.app.data.model.SharingPlatforms
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditMemberDialog(
    targetSubscription: SubscriptionWithMembers,
    memberToEdit: MemberEntity?,
    onDismiss: () -> Unit,
    onSave: (MemberEntity) -> Unit,
    onDelete: ((MemberEntity) -> Unit)? = null,
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val isEditing = memberToEdit != null
    val sub = targetSubscription.subscription
    val platformPrices = targetSubscription.platformPrices

    val displayDateFormat = remember { SimpleDateFormat("dd/MM/yy", Locale.getDefault()) }
    val isoDateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US) }

    // 1. Basic Info
    var memberName by remember {
        mutableStateOf(memberToEdit?.memberName ?: "")
    }
    val initialSharingPlatform = memberToEdit?.sharingPlatform ?: ""
    val initialMatchedPlatformPrice = platformPrices.find { it.platformName.equals(initialSharingPlatform, ignoreCase = true) }

    var sharingPlatform by remember {
        mutableStateOf(initialSharingPlatform)
    }

    // Currency selection
    var selectedCurrencyCode by remember {
        mutableStateOf(
            if (initialMatchedPlatformPrice != null && initialMatchedPlatformPrice.currency.isNotBlank() && (
                memberToEdit == null ||
                memberToEdit.currency.isBlank() ||
                memberToEdit.currency.equals(sub.currency, ignoreCase = true) ||
                (memberToEdit.currency.equals("TRY", ignoreCase = true) && !initialMatchedPlatformPrice.currency.equals("TRY", ignoreCase = true)) ||
                (memberToEdit.currency.equals("€", ignoreCase = true) && initialMatchedPlatformPrice.currency.equals("EUR", ignoreCase = true))
            )) {
                CurrencyManager.findCurrency(initialMatchedPlatformPrice.currency).code
            } else if (memberToEdit != null && memberToEdit.currency.isNotBlank()) {
                CurrencyManager.findCurrency(memberToEdit.currency).code
            } else if (initialMatchedPlatformPrice != null && initialMatchedPlatformPrice.currency.isNotBlank()) {
                CurrencyManager.findCurrency(initialMatchedPlatformPrice.currency).code
            } else {
                CurrencyManager.findCurrency(sub.currency).code
            }
        )
    }

    // Contribution Amount
    var contributionText by remember {
        mutableStateOf(
            if (memberToEdit != null && memberToEdit.contributionAmount > 0.0) {
                String.format(Locale.US, "%.2f", memberToEdit.contributionAmount)
            } else if (initialMatchedPlatformPrice != null && initialMatchedPlatformPrice.pricePerUser > 0.0) {
                String.format(Locale.US, "%.2f", initialMatchedPlatformPrice.pricePerUser)
            } else if (sub.defaultContributionPerUser > 0.0) {
                String.format(Locale.US, "%.2f", sub.defaultContributionPerUser)
            } else {
                val split = targetSubscription.equalSplitPerPerson
                String.format(Locale.US, "%.2f", split)
            }
        )
    }

    // Payment Method
    var paymentMethod by remember {
        mutableStateOf(memberToEdit?.paymentMethod ?: "Bizum")
    }

    // 2. Joined Date
    var joinedDateTimestamp by remember {
        val initialJoined = if (memberToEdit != null) {
            if (memberToEdit.joinedDate > 0) memberToEdit.joinedDate
            else parseDateStrToMillis(memberToEdit.joinedDateStr) ?: System.currentTimeMillis()
        } else {
            System.currentTimeMillis()
        }
        mutableLongStateOf(initialJoined)
    }

    // 3. Payment Frequency
    var paymentFrequencyValue by remember {
        mutableIntStateOf(if (memberToEdit != null && memberToEdit.paymentFrequencyValue > 0) memberToEdit.paymentFrequencyValue else 1)
    }
    var paymentFrequencyUnit by remember {
        mutableStateOf(if (memberToEdit != null && memberToEdit.paymentFrequencyUnit.isNotBlank()) memberToEdit.paymentFrequencyUnit else "months")
    }

    // 4. Next Payment Date
    var nextPaymentDateTimestamp by remember {
        val calculated = if (memberToEdit != null && memberToEdit.nextPaymentDate.isNotBlank()) {
            parseDateStrToMillis(memberToEdit.nextPaymentDate) ?: calculateNextCycleDate(joinedDateTimestamp, paymentFrequencyValue, paymentFrequencyUnit)
        } else {
            calculateNextCycleDate(joinedDateTimestamp, paymentFrequencyValue, paymentFrequencyUnit)
        }
        mutableLongStateOf(calculated)
    }

    // 5. Manager Alarm
    var enableAlarm by remember {
        mutableStateOf(memberToEdit?.enableAlarm ?: false)
    }
    var alarmValue by remember {
        mutableIntStateOf(memberToEdit?.alarmValue ?: 0)
    }
    var alarmUnit by remember {
        mutableStateOf(memberToEdit?.alarmUnit ?: "same_day")
    }

    // 6. Contact & Notes
    var memberContact by remember {
        mutableStateOf(memberToEdit?.memberContact ?: "")
    }
    var notesOrProfile by remember {
        mutableStateOf(memberToEdit?.notes ?: "")
    }

    // 7. Statuses
    var isPendingPayment by remember {
        mutableStateOf(memberToEdit?.isPendingPayment ?: (if (memberToEdit != null) !memberToEdit.isPaidThisMonth else false))
    }
    var isPendingRemoval by remember {
        mutableStateOf(memberToEdit?.isPendingRemoval ?: false)
    }
    var isPendingRegistration by remember {
        mutableStateOf(memberToEdit?.isPendingRegistration ?: false)
    }

    // UI Dropdown States
    var platformDropdownExpanded by remember { mutableStateOf(false) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }
    var freqUnitDropdownExpanded by remember { mutableStateOf(false) }
    var alarmUnitDropdownExpanded by remember { mutableStateOf(false) }

    // Date Picker Dialog states
    var activeDatePickerTarget by remember { mutableStateOf<String?>(null) }
    var datePickerInitialMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    fun autoRecalculateNextPayment() {
        nextPaymentDateTimestamp = calculateNextCycleDate(joinedDateTimestamp, paymentFrequencyValue, paymentFrequencyUnit)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 540.dp)
                .fillMaxHeight(0.90f)
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. DIALOG HEADER (Fixed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isEditing) Icons.Default.Person else Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isEditing) "Editar Usuario" else "Añadir Usuario",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 2. SCROLLABLE FORM CONTENT (takes available space smoothly)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // NOMBRE O APODO
                    Text(
                        text = "NOMBRE O APODO *",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = memberName,
                        onValueChange = {
                            memberName = it
                            nameError = it.isBlank()
                        },
                        placeholder = { Text("Ej. Daniel Álvarez (danielalvarez)") },
                        isError = nameError,
                        supportingText = if (nameError) {
                            { Text("El nombre es obligatorio") }
                        } else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_member_name"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // PLATAFORMA DE COMPARTICIÓN
                    Text(
                        text = "PLATAFORMA DE COMPARTICIÓN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = platformDropdownExpanded,
                        onExpandedChange = { 
                            if (platformPrices.isNotEmpty()) {
                                platformDropdownExpanded = !platformDropdownExpanded 
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val currentPlatformInfo = SharingPlatforms.getInfo(sharingPlatform, availablePlatforms)
                        val displayPlatformText = if (sharingPlatform.isNotBlank()) {
                            sharingPlatform
                        } else if (platformPrices.isNotEmpty()) {
                            "Seleccionar plataforma configurada..."
                        } else {
                            "Sin plataformas configuradas en la suscripción"
                        }

                        OutlinedTextField(
                            value = displayPlatformText,
                            onValueChange = {},
                            readOnly = true,
                            enabled = platformPrices.isNotEmpty(),
                            leadingIcon = if (sharingPlatform.isNotBlank()) {
                                {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(currentPlatformInfo.baseColor)
                                    )
                                }
                            } else null,
                            trailingIcon = {
                                if (platformPrices.isNotEmpty()) {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = platformDropdownExpanded)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("input_member_platform_dropdown"),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp),
                            supportingText = if (platformPrices.isEmpty()) {
                                {
                                    Text(
                                        text = "Añade tarifas de plataforma editando la suscripción para asignarlas aquí.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            } else null
                        )

                        if (platformPrices.isNotEmpty()) {
                            ExposedDropdownMenu(
                                expanded = platformDropdownExpanded,
                                onDismissRequest = { platformDropdownExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Sin plataforma",
                                            fontWeight = if (sharingPlatform.isBlank()) FontWeight.Bold else FontWeight.Normal,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.outlineVariant)
                                        )
                                    },
                                    onClick = {
                                        sharingPlatform = ""
                                        platformDropdownExpanded = false
                                    }
                                )
                                platformPrices.forEach { pItem ->
                                    val pInfo = SharingPlatforms.getInfo(pItem.platformName, availablePlatforms)
                                    val isSelected = sharingPlatform.equals(pItem.platformName, ignoreCase = true)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = pItem.platformName,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                )
                                                if (pItem.pricePerUser > 0) {
                                                    Text(
                                                        text = "${String.format(Locale.US, "%.2f", pItem.pricePerUser)} ${pItem.currencyItem.symbol}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        },
                                        leadingIcon = {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(pInfo.baseColor)
                                            )
                                        },
                                        onClick = {
                                            sharingPlatform = pItem.platformName
                                            if (pItem.pricePerUser > 0) {
                                                contributionText = String.format(Locale.US, "%.2f", pItem.pricePerUser)
                                            }
                                            if (pItem.currency.isNotBlank()) {
                                                selectedCurrencyCode = CurrencyManager.findCurrency(pItem.currency).code
                                            }
                                            platformDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // IMPORTE APORTADO /mes & MONEDA
                    Text(
                        text = "IMPORTE APORTADO /mes",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = contributionText,
                            onValueChange = {
                                contributionText = it
                                amountError = it.replace(',', '.').toDoubleOrNull() == null
                            },
                            placeholder = { Text("2.25") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = amountError,
                            modifier = Modifier
                                .weight(0.50f)
                                .testTag("input_member_contribution"),
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = currencyDropdownExpanded,
                            onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded },
                            modifier = Modifier.weight(0.50f)
                        ) {
                            val currItem = CurrencyManager.findCurrency(selectedCurrencyCode)
                            OutlinedTextField(
                                value = "${currItem.flag} ${currItem.code} (${currItem.symbol})",
                                onValueChange = {},
                                readOnly = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyDropdownExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("input_member_currency"),
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = currencyDropdownExpanded,
                                onDismissRequest = { currencyDropdownExpanded = false },
                                modifier = Modifier.widthIn(min = 220.dp)
                            ) {
                                CurrencyManager.currencies.forEach { item ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = "${item.flag} ${item.code} (${item.symbol})",
                                                maxLines = 1,
                                                softWrap = false,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (item.code == selectedCurrencyCode) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            selectedCurrencyCode = item.code
                                            currencyDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // MÉTODO HABITUAL
                    Text(
                        text = "MÉTODO HABITUAL",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = { paymentMethod = it },
                        placeholder = { Text("Ej. Transferencia Sharesub a Revolut, Bizum...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_member_payment_method"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // CARD: FECHA DE UNIÓN
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fecha de unión",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = displayDateFormat.format(Date(joinedDateTimestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = displayDateFormat.format(Date(joinedDateTimestamp)),
                                onValueChange = {},
                                readOnly = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        datePickerInitialMillis = joinedDateTimestamp
                                        activeDatePickerTarget = "joined"
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Cambiar fecha",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        datePickerInitialMillis = joinedDateTimestamp
                                        activeDatePickerTarget = "joined"
                                    }
                                    .testTag("input_member_joined_date"),
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // CARD: FECHA PRÓXIMO PAGO + FRECUENCIA + ALARMA
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
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f, fill = false)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Fecha próximo pago",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = displayDateFormat.format(Date(nextPaymentDateTimestamp)),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = displayDateFormat.format(Date(nextPaymentDateTimestamp)),
                                onValueChange = {},
                                readOnly = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                                trailingIcon = {
                                    IconButton(onClick = {
                                        datePickerInitialMillis = nextPaymentDateTimestamp
                                        activeDatePickerTarget = "nextPayment"
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Cambiar fecha",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        datePickerInitialMillis = nextPaymentDateTimestamp
                                        activeDatePickerTarget = "nextPayment"
                                    }
                                    .testTag("input_member_next_payment_date"),
                                singleLine = true,
                                maxLines = 1,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Calculada según la periodicidad o editable a mano.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // SUBSECCIÓN: FRECUENCIA DEL PAGO
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Autorenew,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Frecuencia del pago",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Text(
                                            text = "Automático",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // CADA (NÚMERO)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "CADA",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            OutlinedTextField(
                                                value = paymentFrequencyValue.toString(),
                                                onValueChange = { str ->
                                                    val num = str.filter { it.isDigit() }.toIntOrNull()
                                                    if (num != null && num > 0) {
                                                        paymentFrequencyValue = num
                                                        autoRecalculateNextPayment()
                                                    } else if (str.isBlank()) {
                                                        paymentFrequencyValue = 1
                                                        autoRecalculateNextPayment()
                                                    }
                                                },
                                                trailingIcon = {
                                                    Column {
                                                        Icon(
                                                            imageVector = Icons.Default.KeyboardArrowUp,
                                                            contentDescription = "Incrementar",
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clickable {
                                                                    paymentFrequencyValue += 1
                                                                    autoRecalculateNextPayment()
                                                                }
                                                        )
                                                        Icon(
                                                            imageVector = Icons.Default.KeyboardArrowDown,
                                                            contentDescription = "Decrementar",
                                                            modifier = Modifier
                                                                .size(16.dp)
                                                                .clickable {
                                                                    if (paymentFrequencyValue > 1) {
                                                                        paymentFrequencyValue -= 1
                                                                        autoRecalculateNextPayment()
                                                                    }
                                                                }
                                                        )
                                                    }
                                                },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                modifier = Modifier.fillMaxWidth(),
                                                singleLine = true,
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                        }

                                        // UNIDAD
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "UNIDAD",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            ExposedDropdownMenuBox(
                                                expanded = freqUnitDropdownExpanded,
                                                onExpandedChange = { freqUnitDropdownExpanded = !freqUnitDropdownExpanded },
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedTextField(
                                                    value = getFrequencyUnitLabel(paymentFrequencyUnit),
                                                    onValueChange = {},
                                                    readOnly = true,
                                                    trailingIcon = {
                                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = freqUnitDropdownExpanded)
                                                    },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .menuAnchor(),
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(10.dp)
                                                )
                                                ExposedDropdownMenu(
                                                    expanded = freqUnitDropdownExpanded,
                                                    onDismissRequest = { freqUnitDropdownExpanded = false }
                                                ) {
                                                    listOf("days", "weeks", "months", "years").forEach { unitKey ->
                                                        DropdownMenuItem(
                                                            text = { Text(getFrequencyUnitLabel(unitKey)) },
                                                            onClick = {
                                                                paymentFrequencyUnit = unitKey
                                                                freqUnitDropdownExpanded = false
                                                                autoRecalculateNextPayment()
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "• Se repite cada $paymentFrequencyValue ${getFrequencyUnitLabel(paymentFrequencyUnit).lowercase()}.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // SUBSECCIÓN: ALARMA DE AVISO
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
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
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "Alarma de aviso para el gestor",
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "Notificación antes del vencimiento",
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
                                            )
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
                                                        modifier = Modifier.fillMaxWidth(),
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
                                                        value = getAlarmUnitLabel(alarmUnit),
                                                        onValueChange = {},
                                                        readOnly = true,
                                                        trailingIcon = {
                                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = alarmUnitDropdownExpanded)
                                                        },
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .menuAnchor(),
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                                    ExposedDropdownMenu(
                                                        expanded = alarmUnitDropdownExpanded,
                                                        onDismissRequest = { alarmUnitDropdownExpanded = false }
                                                    ) {
                                                        listOf("same_day", "hours", "days", "weeks", "months").forEach { aKey ->
                                                            DropdownMenuItem(
                                                                text = { Text(getAlarmUnitLabel(aKey)) },
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
                                            else "🔔 Avisar $alarmValue ${getAlarmUnitLabel(alarmUnit).lowercase()}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // TELÉFONO O CORREO
                    Text(
                        text = "TELÉFONO O CORREO (OPCIONAL)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = memberContact,
                        onValueChange = { memberContact = it },
                        placeholder = { Text("Ej. +34 600 000 000 o email@ejemplo.com") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_member_contact"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // NOTAS O PERFIL ASIGNADO
                    Text(
                        text = "NOTAS O PERFIL ASIGNADO (OPCIONAL)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = notesOrProfile,
                        onValueChange = { notesOrProfile = it },
                        placeholder = { Text("Ej. Perfil 3 (PIN: 1234)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_member_notes"),
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // ESTADOS Y ALERTAS
                    Text(
                        text = "ESTADOS Y ALERTAS DEL USUARIO",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Switch 1: Pendiente de pago
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPendingPayment) Color(0xFFFFF7ED) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPendingPayment) Color(0xFFF97316).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pendiente de pago",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isPendingPayment) Color(0xFFC2410C) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Marca al usuario si aún no ha realizado su pago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPendingPayment,
                                onCheckedChange = {
                                    isPendingPayment = it
                                    if (it) {
                                        isPendingRemoval = false
                                        isPendingRegistration = false
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Switch 2: Pendiente de eliminar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPendingRemoval) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPendingRemoval) Color(0xFFEF4444).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pendiente de eliminar",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isPendingRemoval) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Indica que este usuario va a dejar la suscripción",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPendingRemoval,
                                onCheckedChange = {
                                    isPendingRemoval = it
                                    if (it) {
                                        isPendingPayment = false
                                        isPendingRegistration = false
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Switch 3: Pendiente dar de alta
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPendingRegistration) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (isPendingRegistration) Color(0xFF3B82F6).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Pendiente dar de alta",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = if (isPendingRegistration) Color(0xFF1D4ED8) else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Para miembros que aún no están activos en la cuenta",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isPendingRegistration,
                                onCheckedChange = {
                                    isPendingRegistration = it
                                    if (it) {
                                        isPendingPayment = false
                                        isPendingRemoval = false
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // 3. DIALOG FOOTER (Fixed, clean bar with explicit buttons)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditing && onDelete != null) {
                        TextButton(
                            onClick = { showConfirmDeleteDialog = true },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Eliminar",
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Button(
                        onClick = {
                            val trimmedName = memberName.trim()
                            if (trimmedName.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val amount = contributionText.replace(',', '.').toDoubleOrNull() ?: 0.0
                            val isoJoined = isoDateFormat.format(Date(joinedDateTimestamp))
                            val isoNextPayment = isoDateFormat.format(Date(nextPaymentDateTimestamp))

                            val updatedEntity = MemberEntity(
                                id = memberToEdit?.id ?: 0,
                                subscriptionId = sub.id,
                                memberName = trimmedName,
                                sharingPlatform = sharingPlatform.trim(),
                                memberContact = memberContact.trim(),
                                joinedDate = joinedDateTimestamp,
                                joinedDateStr = isoJoined,
                                nextPaymentDate = isoNextPayment,
                                paymentFrequencyValue = paymentFrequencyValue,
                                paymentFrequencyUnit = paymentFrequencyUnit,
                                autoRepeatPayment = true,
                                paymentMethod = paymentMethod.trim().ifBlank { "Bizum" },
                                lastPaymentDate = memberToEdit?.lastPaymentDate ?: "",
                                enableAlarm = enableAlarm,
                                alarmValue = alarmValue,
                                alarmUnit = alarmUnit,
                                alarmDaysBefore = if (alarmUnit == "days") alarmValue else 0,
                                contributionAmount = amount,
                                currency = selectedCurrencyCode,
                                isPaidThisMonth = !isPendingPayment,
                                isPendingPayment = isPendingPayment,
                                isPendingRemoval = isPendingRemoval,
                                isPendingRegistration = isPendingRegistration,
                                notes = notesOrProfile.trim()
                            )
                            onSave(updatedEntity)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_member_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Guardar",
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }

    // DATE PICKER DIALOG
    if (activeDatePickerTarget != null) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = datePickerInitialMillis
        )
        DatePickerDialog(
            onDismissRequest = { activeDatePickerTarget = null },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selected ->
                        if (activeDatePickerTarget == "joined") {
                            joinedDateTimestamp = selected
                            autoRecalculateNextPayment()
                        } else if (activeDatePickerTarget == "nextPayment") {
                            nextPaymentDateTimestamp = selected
                        }
                    }
                    activeDatePickerTarget = null
                }) {
                    Text("Aceptar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDatePickerTarget = null }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // DELETE CONFIRMATION DIALOG
    if (showConfirmDeleteDialog && memberToEdit != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("¿Eliminar usuario?") },
            text = { Text("¿Estás seguro de que deseas eliminar a '${memberToEdit.memberName}' de esta suscripción? Esta acción no se puede deshacer.") },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        onDelete(memberToEdit)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun calculateNextCycleDate(fromMillis: Long, freqValue: Int, freqUnit: String): Long {
    val cal = Calendar.getInstance()
    cal.timeInMillis = fromMillis
    val value = if (freqValue > 0) freqValue else 1
    when (freqUnit.lowercase()) {
        "days", "dias", "día", "días" -> cal.add(Calendar.DAY_OF_MONTH, value)
        "weeks", "semanas", "semana" -> cal.add(Calendar.WEEK_OF_YEAR, value)
        "years", "anos", "años", "año", "ano" -> cal.add(Calendar.YEAR, value)
        else -> cal.add(Calendar.MONTH, value)
    }
    return cal.timeInMillis
}

private fun parseDateStrToMillis(dateStr: String?): Long? {
    if (dateStr.isNullOrBlank()) return null
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        isoFormat.parse(dateStr.trim())?.time
    } catch (_: Exception) {
        try {
            val displayFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
            displayFormat.parse(dateStr.trim())?.time
        } catch (_: Exception) {
            try {
                val fullDisplayFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                fullDisplayFormat.parse(dateStr.trim())?.time
            } catch (_: Exception) {
                null
            }
        }
    }
}

private fun getFrequencyUnitLabel(unitKey: String): String {
    return when (unitKey.lowercase()) {
        "days", "dias", "días" -> "Día(s)"
        "weeks", "semanas" -> "Semana(s)"
        "years", "anos", "años" -> "Año(s)"
        else -> "Mes(es)"
    }
}

private fun getAlarmUnitLabel(unitKey: String): String {
    return when (unitKey.lowercase()) {
        "same_day", "mismo_dia" -> "El mismo día"
        "hours", "horas" -> "Hora(s) antes"
        "weeks", "semanas" -> "Semana(s) antes"
        "months", "meses" -> "Mes(es) antes"
        else -> "Día(s) antes"
    }
}
