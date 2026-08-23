package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.example.data.model.BillingPeriod
import com.example.data.model.CurrencyManager
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SharingPlatformEntity
import com.example.data.local.SubscriptionWithMembers
import com.example.data.model.SharingPlatforms
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.ProfitGreenBg
import com.example.ui.util.HighlightedText
import com.example.ui.util.I18n
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SubscriptionCard(
    subscriptionWithMembers: SubscriptionWithMembers,
    onAddMemberClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onMemberClick: (com.example.data.local.MemberEntity) -> Unit = {},
    searchQuery: String = "",
    availablePlatforms: List<SharingPlatformEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    val sub = subscriptionWithMembers.subscription
    val members = subscriptionWithMembers.members
    val totalContributed = subscriptionWithMembers.totalContributed
    val myCostMonthly = subscriptionWithMembers.myCostMonthly
    val myCostEur = subscriptionWithMembers.myCostEur
    val netBalance = subscriptionWithMembers.netBalance
    val isProfit = subscriptionWithMembers.isNetProfit
    val period = subscriptionWithMembers.billingPeriodObj

    var isExpanded by rememberSaveable(sub.id) { mutableStateOf(false) }

    val arrowRotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        label = "arrow_rotation"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .testTag("subscription_card_${sub.id}"),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 2.5.dp else 1.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Platform Icon, Platform + Plan Name, Expand Chevron, More Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlatformIconBadge(
                    subscription = sub,
                    size = 46.dp,
                    iconSize = 24.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    HighlightedText(
                        text = sub.platformName,
                        query = searchQuery,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 20.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (sub.customPlanName.isNotBlank()) {
                        HighlightedText(
                            text = sub.customPlanName,
                            query = searchQuery,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Expand / Collapse Chevron indicator
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                    modifier = Modifier.testTag("expand_toggle_${sub.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.rotate(arrowRotation)
                    )
                }

                // Edit subscription button
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.testTag("edit_sub_${sub.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = I18n.editSubscription,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Preview Details (Visible only when expanded)
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

            // Main User / Titular on one line, and Renewal Date on the next line
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Line 1: Titular
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = I18n.mainUserOwner,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${I18n.mainUserOwner}: ",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        HighlightedText(
                            text = sub.mainUserName,
                            query = searchQuery,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Line 2: Fecha de renovación
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = I18n.renewalDate,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${I18n.renewalDate}: ${com.example.data.model.BillingPeriod.formatSchedule(sub.billingDay, sub.billingMonth, period)}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (sub.enableAlarm) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Alarma activada",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            val platformPricesList = subscriptionWithMembers.platformPrices
            if (platformPricesList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    platformPricesList.forEach { pItem ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = "${pItem.platformName}: ${String.format(Locale.getDefault(), "%.2f", pItem.pricePerUser)} ${pItem.currencyItem.symbol}${pItem.billingPeriodObj.localizedSuffix}",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Financial Summary Block in 3 independent lines
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val curr = com.example.data.model.CurrencyManager.findCurrency(sub.currency)

                    // Línea 1: Coste propio
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = I18n.ownCost,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.2f", sub.cost)} ${curr.symbol}${period.localizedSuffix}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (curr.code != "EUR") {
                                val eurConvertedText = if (period != com.example.data.model.BillingPeriod.MONTHLY) {
                                    "≈ ${String.format(Locale.getDefault(), "%.2f", myCostEur)} € (${String.format(Locale.getDefault(), "%.2f", myCostMonthly)} €${I18n.perMonthSuffix})"
                                } else {
                                    "≈ ${String.format(Locale.getDefault(), "%.2f", myCostMonthly)} €${I18n.perMonthSuffix}"
                                }
                                Text(
                                    text = eurConvertedText,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else if (period != com.example.data.model.BillingPeriod.MONTHLY) {
                                Text(
                                    text = "(${String.format(Locale.getDefault(), "%.2f", myCostMonthly)} €${I18n.perMonthSuffix})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )

                    // Línea 2: Aportan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${I18n.contributeLabel} (${members.size} ${I18n.membersUnit})",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${String.format(Locale.getDefault(), "%.2f", totalContributed)} €${I18n.perMonthSuffix}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = ProfitGreen
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        thickness = 0.8.dp
                    )

                    // Línea 3: Ganancia neta o Balance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isProfit) I18n.netProfit else I18n.netBalance,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.onSurface
                        )
                        val prefix = if (netBalance > 0.001) "+" else ""
                        Text(
                            text = "$prefix${String.format(Locale.getDefault(), "%.2f", netBalance)} €${I18n.perMonthSuffix}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (isProfit) ProfitGreen else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // List of User / Member Chips
            Text(
                text = "${I18n.currentUsers} (${members.size})",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (members.isEmpty()) {
                Text(
                    text = I18n.noMembersInSub,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    members.forEach { member ->
                        val chipBgColor = when {
                            member.isPendingRemoval -> Color(0xFFFFE4E6)
                            member.isPendingRegistration -> Color(0xFFDBEAFE)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFFEF3C7)
                            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        }
                        val dotColor = when {
                            member.isPendingRemoval -> Color(0xFFE11D48)
                            member.isPendingRegistration -> Color(0xFF2563EB)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFFD97706)
                            else -> ProfitGreen
                        }
                        val memberTextColor = when {
                            member.isPendingRemoval -> Color(0xFF991B1B)
                            member.isPendingRegistration -> Color(0xFF1E40AF)
                            member.isPendingPayment || !member.isPaidThisMonth -> Color(0xFF92400E)
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = chipBgColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onMemberClick(member) }
                                .testTag("member_chip_${member.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(dotColor)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        HighlightedText(
                                            text = member.memberName,
                                            query = searchQuery,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                lineHeight = 14.sp
                                            ),
                                            color = memberTextColor,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )

                                        val memberPaymentDateFormatted = remember(
                                            member.nextPaymentDate,
                                            member.joinedDate,
                                            member.joinedDateStr,
                                            member.paymentFrequencyValue,
                                            member.paymentFrequencyUnit
                                        ) {
                                            getMemberPaymentDateFormatted(member)
                                        }

                                        if (memberPaymentDateFormatted.isNotBlank()) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                                                modifier = Modifier.testTag("member_payment_date_badge_${member.id}")
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CalendarMonth,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Text(
                                                        text = memberPaymentDateFormatted,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 10.5.sp,
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    if (member.enableAlarm) {
                                                        Icon(
                                                            imageVector = Icons.Default.Notifications,
                                                            contentDescription = "Alarma de pago activada",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    if (member.sharingPlatform.isNotBlank() || member.notes.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            if (member.sharingPlatform.isNotBlank()) {
                                                val platformInfo = SharingPlatforms.getInfo(member.sharingPlatform, availablePlatforms)
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = platformInfo.badgeBgColor
                                                ) {
                                                    Text(
                                                        text = platformInfo.name,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = platformInfo.badgeTextColor
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.5.dp)
                                                    )
                                                }
                                            }
                                            if (member.notes.isNotBlank()) {
                                                Text(
                                                    text = "(${member.notes.trim()})",
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Medium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    ),
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                val memberPlatformPricing = platformPricesList.find { it.platformName.equals(member.sharingPlatform, ignoreCase = true) }
                                val memberDisplayAmount = if (member.contributionAmount > 0.0) {
                                    member.contributionAmount
                                } else if (memberPlatformPricing != null && memberPlatformPricing.pricePerUser > 0.0) {
                                    memberPlatformPricing.pricePerUser
                                } else {
                                    0.0
                                }
                                val memberCurrencySymbol = if (member.currency.isNotBlank()) {
                                    CurrencyManager.findCurrency(member.currency).symbol
                                } else {
                                    memberPlatformPricing?.currencyItem?.symbol ?: "€"
                                }
                                val memberPeriodSuffix = if (member.paymentFrequencyUnit.isNotBlank()) {
                                    val freqVal = if (member.paymentFrequencyValue > 0) member.paymentFrequencyValue else 1
                                    when (member.paymentFrequencyUnit) {
                                        "months" -> if (freqVal == 1) I18n.perMonthSuffix else " / ${freqVal}m"
                                        "years" -> if (freqVal == 1) I18n.getBillingPeriodSuffix(BillingPeriod.YEARLY) else " / ${freqVal}a"
                                        "weeks" -> " / ${freqVal}sem"
                                        "days" -> " / ${freqVal}d"
                                        else -> memberPlatformPricing?.billingPeriodObj?.localizedSuffix ?: I18n.perMonthSuffix
                                    }
                                } else {
                                    memberPlatformPricing?.billingPeriodObj?.localizedSuffix ?: I18n.perMonthSuffix
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                                ) {
                                    Text(
                                        text = "+${String.format(Locale.getDefault(), "%.2f", memberDisplayAmount)} $memberCurrencySymbol$memberPeriodSuffix",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (member.isPendingRemoval) Color(0xFFE11D48) else if (member.isPendingPayment || !member.isPaidThisMonth) Color(0xFFD97706) else ProfitGreen
                                        ),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button: + Añadir usuario
            OutlinedButton(
                onClick = onAddMemberClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("add_member_btn_${sub.id}"),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = I18n.addUser,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }

            // Notas de la suscripción (al final, detrás del botón de añadir usuario)
            if (sub.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscription_notes_card_${sub.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = I18n.notesLabel,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${I18n.notesLabel}:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = sub.notes.trim(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
}
}

private fun getMemberPaymentDateFormatted(member: com.example.data.local.MemberEntity): String {
    val targetMillis = if (member.nextPaymentDate.isNotBlank()) {
        parseDateToMillis(member.nextPaymentDate) ?: calculateMemberNextCycle(member)
    } else if (member.joinedDate > 0 || member.joinedDateStr.isNotBlank()) {
        calculateMemberNextCycle(member)
    } else {
        null
    } ?: return ""

    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = targetMillis
    val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
    val month = cal.get(java.util.Calendar.MONTH) + 1
    return "$day/$month"
}

private fun parseDateToMillis(dateStr: String?): Long? {
    if (dateStr.isNullOrBlank()) return null
    val clean = dateStr.trim()
    return try {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).parse(clean)?.time
    } catch (_: Exception) {
        try {
            java.text.SimpleDateFormat("dd/MM/yy", java.util.Locale.getDefault()).parse(clean)?.time
        } catch (_: Exception) {
            try {
                java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).parse(clean)?.time
            } catch (_: Exception) {
                null
            }
        }
    }
}

private fun calculateMemberNextCycle(member: com.example.data.local.MemberEntity): Long {
    val baseMillis = if (member.joinedDate > 0) {
        member.joinedDate
    } else {
        parseDateToMillis(member.joinedDateStr) ?: System.currentTimeMillis()
    }
    val cal = java.util.Calendar.getInstance()
    cal.timeInMillis = baseMillis
    val value = if (member.paymentFrequencyValue > 0) member.paymentFrequencyValue else 1
    when (member.paymentFrequencyUnit.lowercase()) {
        "days", "dias", "día", "días" -> cal.add(java.util.Calendar.DAY_OF_MONTH, value)
        "weeks", "semanas", "semana" -> cal.add(java.util.Calendar.WEEK_OF_YEAR, value)
        "years", "anos", "años", "año", "ano" -> cal.add(java.util.Calendar.YEAR, value)
        else -> cal.add(java.util.Calendar.MONTH, value)
    }
    return cal.timeInMillis
}


