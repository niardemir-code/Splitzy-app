package com.apleq.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.apleq.app.ui.theme.Quicksand
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.data.model.PlatformPricingHelper
import com.apleq.app.ui.components.AddEditMemberDialog
import com.apleq.app.ui.components.AddEditSubscriptionDialog
import com.apleq.app.ui.components.ChatDialog
import com.apleq.app.ui.components.ChatListDialog
import com.apleq.app.ui.components.FinancialSummaryCard
import com.apleq.app.ui.components.JoinGroupDialog
import com.apleq.app.ui.components.NotificationsDialog
import com.apleq.app.ui.components.PlatformIconBadge
import com.apleq.app.ui.components.ReminderMessageDialog
import com.apleq.app.ui.components.SplitzyLogo
import com.apleq.app.ui.components.SubscriptionCard
import com.apleq.app.ui.viewmodel.SubscriptionViewModel

import com.apleq.app.ui.util.I18n

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SubscriptionViewModel,
    modifier: Modifier = Modifier
) {
    val activityContext = androidx.compose.ui.platform.LocalContext.current
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data)
    }

    val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val filteredSubscriptions by viewModel.filteredSubscriptions.collectAsStateWithLifecycle()
    val participatingGroups by viewModel.participatingGroups.collectAsStateWithLifecycle()
    val financialOverview by viewModel.financialOverview.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val sortOrder by viewModel.sortOrder.collectAsStateWithLifecycle()

    val showAddEditSubDialog by viewModel.showAddEditSubscriptionDialog.collectAsStateWithLifecycle()
    val subscriptionToEdit by viewModel.subscriptionToEdit.collectAsStateWithLifecycle()

    val showAddEditMemberDialog by viewModel.showAddEditMemberDialog.collectAsStateWithLifecycle()
    val memberToEdit by viewModel.memberToEdit.collectAsStateWithLifecycle()
    val targetSubForMember by viewModel.targetSubscriptionForNewMember.collectAsStateWithLifecycle()

    val reminderData by viewModel.reminderMemberData.collectAsStateWithLifecycle()

    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var subscriptionToDelete by remember { mutableStateOf<SubscriptionEntity?>(null) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var selectedClientGroup by remember { mutableStateOf<Map<String, Any>?>(null) }
    var groupPendingLeave by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLeavingGroup by remember { mutableStateOf(false) }
    var leaveGroupError by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    val rawCategories = listOf("Todas", "Streaming", "Música", "Productividad", "Gaming", "Educación", "Salud")

    val showBackupRestoreDialog by viewModel.showBackupRestoreDialog.collectAsStateWithLifecycle()
    val pendingRestorePreview by viewModel.pendingRestorePreview.collectAsStateWithLifecycle()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val currentUid = (authState as? com.apleq.app.data.remote.AuthState.Authenticated)?.user?.uid ?: ""
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
    val clientAlarmPrefs by viewModel.clientAlarmPrefs.collectAsStateWithLifecycle()

    var openChatInfo by remember { mutableStateOf<Triple<String, String, String>?>(null) } // chatId, subscriptionName, otherPersonName
    var openChatIsOwnerSide by remember { mutableStateOf(true) }
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val unreadChats by viewModel.unreadChats.collectAsStateWithLifecycle()
    val unreadChatIdsForOwner = remember(unreadChats) {
        unreadChats.filter { it.isOwnerSide }.map { it.chatId }.toSet()
    }
    val unreadChatIdsForClient = remember(unreadChats) {
        unreadChats.filter { !it.isOwnerSide }.map { it.chatId }.toSet()
    }
    var chatListForSubscription by remember { mutableStateOf<com.apleq.app.data.local.SubscriptionWithMembers?>(null) }

    LaunchedEffect(authState) {
        if (authState is com.apleq.app.data.remote.AuthState.Authenticated) {
            viewModel.closeAuthDialog()
            viewModel.closeAppMenu()
        }
    }

    LaunchedEffect(authState) {
        if (authState is com.apleq.app.data.remote.AuthState.Error && (authState as com.apleq.app.data.remote.AuthState.Error).message == "FALLBACK_GOOGLE_SIGNIN") {
            val intent = viewModel.getGoogleSignInIntent(activityContext)
            googleSignInLauncher.launch(intent)
        }
    }

    val showAppMenu by viewModel.showAppMenu.collectAsStateWithLifecycle()
    val sharingPlatforms by viewModel.sharingPlatforms.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncStatus by viewModel.syncStatus.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.openAppMenu() }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("btn_app_logo_menu")
                    ) {
                        SplitzyLogo(size = 38.dp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Apleq",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontFamily = Quicksand,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Menú",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = if (authState is com.apleq.app.data.remote.AuthState.Authenticated) {
                                    val user = (authState as com.apleq.app.data.remote.AuthState.Authenticated).user
                                    user.displayName?.takeIf { it.isNotBlank() } ?: user.email ?: "Apleq"
                                } else {
                                    I18n.appSubtitle
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNotificationsDialog = true },
                        modifier = Modifier.testTag("btn_top_notifications")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFE11D48),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadNotificationsCount > 0)
                                    Icons.Default.Notifications
                                else
                                    Icons.Default.NotificationsNone,
                                contentDescription = "Notificaciones"
                            )
                        }
                    }

                    // Cloud Download / Sync Button (to the left of Settings)
                    IconButton(
                        onClick = {
                            if (authState !is com.apleq.app.data.remote.AuthState.Authenticated) {
                                viewModel.openAuthDialog()
                            } else {
                                viewModel.syncFromCloud()
                            }
                        },
                        enabled = !isSyncing,
                        modifier = Modifier.testTag("btn_top_sync_from_cloud")
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = I18n.cloudSyncTitle,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Quick Settings Icon Button
                    IconButton(
                        onClick = { viewModel.openSettingsScreen() },
                        modifier = Modifier.testTag("btn_top_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = I18n.settingsTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Account / Cloud Sync Button with Google Avatar or Icon
                    IconButton(
                        onClick = { viewModel.openAuthDialog() },
                        modifier = Modifier.testTag("btn_auth_account")
                    ) {
                        val currentAuthState = authState
                        if (currentAuthState is com.apleq.app.data.remote.AuthState.Authenticated) {
                            val user = currentAuthState.user
                            if (user.photoUrl != null) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = user.displayName ?: user.email,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (user.displayName?.firstOrNull() ?: user.email?.firstOrNull() ?: 'U').uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = I18n.tabSignIn,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ExtendedFloatingActionButton(
                    onClick = { showJoinDialog = true },
                    icon = { Icon(Icons.Default.GroupAdd, contentDescription = "Unirse", modifier = Modifier.size(18.dp)) },
                    text = { Text("Unirse", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = RoundedCornerShape(16.dp)
                )
                ExtendedFloatingActionButton(
                    onClick = { viewModel.openAddSubscription() },
                    icon = { Icon(Icons.Default.Add, contentDescription = I18n.newSubscription, modifier = Modifier.size(18.dp)) },
                    text = { Text(I18n.newSubscription, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.testTag("fab_add_subscription")
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Financial Overview Summary Card
            item {
                FinancialSummaryCard(overview = financialOverview)
            }

            // 2. Search & Category Filters
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = { Text(I18n.searchPlaceholder, style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.setSearchQuery("") },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = I18n.close,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        maxLines = 1,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_bar_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rawCategories.forEach { category ->
                            val isSelected = selectedCategory == category
                            val displayLabel = if (category == "Todas") I18n.filterAll else I18n.getCategoryName(category)
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSelectedCategory(category) },
                                label = { Text(displayLabel) },
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sorting Options Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = I18n.sortBy,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${I18n.sortBy}:",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        com.apleq.app.ui.viewmodel.SubscriptionSortOrder.values().forEach { order ->
                            val isSelected = sortOrder == order
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setSortOrder(order) },
                                label = {
                                    Text(
                                        text = order.localizedChipText,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    )
                                },
                                leadingIcon = if (isSelected) {
                                    {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(12.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // 3. Section Title & Subscriptions List
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${I18n.mySubscriptions} (${filteredSubscriptions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (filteredSubscriptions.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Subscriptions,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank() || selectedCategory != "Todas") {
                                    I18n.noFilteredSubscriptions
                                } else {
                                    I18n.noSubscriptionsYet
                                },
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = I18n.noSubscriptionsDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            androidx.compose.material3.OutlinedButton(
                                onClick = { showJoinDialog = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GroupAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Unirse a un grupo")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.openAddSubscription() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(I18n.addSubscription)
                            }
                        }
                    }
                }
            } else {
                items(filteredSubscriptions, key = { it.subscription.id }) { item ->
                    SubscriptionCard(
                        subscriptionWithMembers = item,
                        searchQuery = searchQuery,
                        availablePlatforms = sharingPlatforms,
                        currentUid = currentUid,
                        unreadChatIdsForOwner = unreadChatIdsForOwner,
                        onAddMemberClick = { viewModel.openAddMember(item) },
                        onGenerateInvite = { viewModel.generateInvite(item) },
                        onEditClick = { viewModel.openEditSubscription(item.subscription) },
                        onDeleteClick = { subscriptionToDelete = item.subscription },
                        onMemberClick = { member -> viewModel.openEditMember(member, item) },
                        onOpenChatList = { chatListForSubscription = item }
                    )
                }
            }

            // --- Sección "Participo en" ---
            if (participatingGroups.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Participo en (${participatingGroups.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                }
                items(participatingGroups, key = { it["_docId"].toString() }) { group ->
                    val groupName = (group["platformName"] ?: group["name"] ?: "Grupo").toString()
                    val members = (group["members"] as? List<*>) ?: emptyList<Any>()
                    val myMember = members.filterIsInstance<Map<String, Any?>>()
                        .find { it["linkedUid"]?.toString() == currentUid || it["linked_uid"]?.toString() == currentUid }
                    val rawAmount = (myMember?.get("contributionAmount")
                        ?: myMember?.get("contribution_amount")
                        ?: myMember?.get("amount")
                        ?: 0).toString().toDoubleOrNull() ?: 0.0

                    // Respaldo: si el miembro no tiene importe propio, usar el precio por
                    // usuario de su plataforma de compartición (igual que hace la vista del gestor).
                    val myPlatformName = (myMember?.get("sharingPlatform")
                        ?: myMember?.get("sharing_platform")
                        ?: myMember?.get("platform")
                        ?: "").toString()

                    val groupPricingRaw = group["platformPricing"]
                        ?: group["platform_pricing"]
                        ?: group["platformPrices"]
                        ?: group["platform_prices"]
                        ?: group["platforms"]
                        ?: group["sharingPlatforms"]
                        ?: ""
                    val groupPricingList = PlatformPricingHelper.parseAny(groupPricingRaw)

                    val matchedPricing = groupPricingList.find {
                        it.platformName.equals(myPlatformName, ignoreCase = true)
                    }

                    val myAmount = when {
                        rawAmount > 0.0 -> rawAmount
                        matchedPricing != null && matchedPricing.pricePerUser > 0.0 -> matchedPricing.pricePerUser
                        groupPricingList.isNotEmpty() && groupPricingList.first().pricePerUser > 0.0 ->
                            groupPricingList.first().pricePerUser
                        else -> 0.0
                    }

                    val myCurrencySymbol = matchedPricing?.currencyItem?.symbol
                        ?: groupPricingList.firstOrNull()?.currencyItem?.symbol
                        ?: "€"

                    val nextPayment = (myMember?.get("nextPaymentDate") ?: myMember?.get("next_payment_date") ?: "").toString()
                    val nextPaymentFormatted = if (nextPayment.length >= 10) {
                        val parts = nextPayment.substring(0, 10).split("-")
                        if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else nextPayment
                    } else nextPayment
                    val isPaid = myMember?.get("isPendingPayment") != true

                    val clientIconColorHex = (group["iconColorHex"] ?: group["icon_color_hex"] ?: "#1285FA").toString()
                    val clientIconType = (group["iconType"] ?: group["icon_type"] ?: "PRESET").toString()
                    val clientIconKey = (group["iconKey"] ?: group["icon_key"] ?: groupName).toString()
                    val clientCustomImageUri = (group["customImageUri"] ?: group["custom_image_uri"] ?: "").toString()
                    val clientAccentColor = runCatching {
                        Color(android.graphics.Color.parseColor(clientIconColorHex))
                    }.getOrDefault(Color(0xFF1285FA))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedClientGroup = group },
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .background(clientAccentColor)
                        )
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PlatformIconBadge(
                                    platformName = groupName,
                                    iconType = clientIconType,
                                    iconKey = clientIconKey,
                                    customImageUri = clientCustomImageUri,
                                    iconColorHex = clientIconColorHex,
                                    size = 46.dp,
                                    iconSize = 24.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = groupName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            lineHeight = 20.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                run {
                                    val myChatId = "${group["_ownerUid"]}_${group["_docId"]}_${currentUid}"
                                    if (unreadChatIdsForClient.contains(myChatId)) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 6.dp)
                                                .size(20.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Email,
                                                contentDescription = "Mensaje sin leer del gestor",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(17.dp)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .align(Alignment.TopEnd)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE11D48))
                                            )
                                        }
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Cliente",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tu parte:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${String.format("%.2f", myAmount)} $myCurrencySymbol", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            if (nextPaymentFormatted.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Próximo pago:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(nextPaymentFormatted, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Estado:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = if (isPaid) "Pagado" else "Pendiente",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPaid) androidx.compose.ui.graphics.Color(0xFF10B981) else androidx.compose.ui.graphics.Color(0xFFF59E0B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Subscription Dialog
    if (showAddEditSubDialog) {
        AddEditSubscriptionDialog(
            subscriptionToEdit = subscriptionToEdit,
            onDismiss = { viewModel.closeAddEditSubscription() },
            onSave = { entity -> viewModel.saveSubscription(entity) },
            onDelete = { entity ->
                viewModel.deleteSubscription(entity)
                viewModel.closeAddEditSubscription()
            },
            availablePlatforms = sharingPlatforms
        )
    }

    // Add / Edit Member Dialog
    if (showAddEditMemberDialog && targetSubForMember != null) {
        AddEditMemberDialog(
            targetSubscription = targetSubForMember!!,
            memberToEdit = memberToEdit,
            onDismiss = { viewModel.closeAddEditMember() },
            onSave = { entity -> viewModel.saveMember(entity) },
            onDelete = { entity -> 
                viewModel.deleteMember(entity)
                viewModel.closeAddEditMember()
            },
            availablePlatforms = sharingPlatforms
        )
    }

    // App Menu Bottom Sheet (Opened from App Logo)
    if (showAppMenu) {
        com.apleq.app.ui.components.AppMenuSheet(
            onDismiss = { viewModel.closeAppMenu() },
            onOpenSettings = { viewModel.openSettingsScreen() },
            onOpenBackupRestore = {
                viewModel.closeAppMenu()
                viewModel.openBackupRestoreDialog()
            },
            onOpenCloudSync = {
                viewModel.closeAppMenu()
                viewModel.openAuthDialog()
            },
            userEmail = (authState as? com.apleq.app.data.remote.AuthState.Authenticated)?.user?.email,
            userName = (authState as? com.apleq.app.data.remote.AuthState.Authenticated)?.user?.displayName
        )
    }


    // Payment Reminder Message Generator Dialog
    reminderData?.let { (member, sub) ->
        ReminderMessageDialog(
            member = member,
            subscription = sub,
            onDismiss = { viewModel.closeReminderGenerator() }
        )
    }

    // Delete Subscription Confirmation Dialog
    subscriptionToDelete?.let { sub ->
        AlertDialog(
            onDismissRequest = { subscriptionToDelete = null },
            title = { Text(I18n.deleteSubscriptionConfirmTitle) },
            text = {
                Text(I18n.deleteSubscriptionConfirmMessage(sub.platformName))
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSubscription(sub)
                        subscriptionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(I18n.delete)
                }
            },
            dismissButton = {
                TextButton(onClick = { subscriptionToDelete = null }) {
                    Text(I18n.cancel)
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        com.apleq.app.ui.components.BackupRestoreDialog(
            onDismissRequest = { viewModel.closeBackupRestoreDialog() },
            onGetBackupJson = { viewModel.getBackupJson() },
            onPreviewBackup = { json -> viewModel.previewBackupContent(json) },
            pendingPreview = pendingRestorePreview,
            onConfirmRestore = { replaceExisting -> viewModel.executeRestore(replaceExisting) },
            onDismissPreview = { viewModel.dismissRestorePreview() }
        )
    }

    // Auth & Cloud Sync Dialog
    if (showAuthDialog) {
        com.apleq.app.ui.components.AuthAccountDialog(
            authState = authState,
            isSyncing = isSyncing,
            onDismissRequest = { viewModel.closeAuthDialog() },
            onSignInWithGoogle = { viewModel.signInWithGoogle(activityContext) },
            onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onRegisterWithEmail = { email, pass, name -> viewModel.registerWithEmail(email, pass, name) },
            onSignOut = { viewModel.signOut() },
            onDeleteAccount = { onResult -> viewModel.deleteAccount(onResult) },
            onSyncToCloud = { viewModel.syncToCloud() },
            onSyncFromCloud = { viewModel.syncFromCloud() },
            onCleanAndPruneDatabase = { viewModel.cleanAndPruneFirebaseDatabase() },
            onClearError = { viewModel.clearAuthError() }
        )
    }

    if (showJoinDialog) {
        JoinGroupDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code, cb -> viewModel.claimInvite(code, cb) }
        )
    }

    selectedClientGroup?.let { group ->
        val groupName = (group["platformName"] ?: group["name"] ?: "Grupo").toString()
        val members = (group["members"] as? List<*>) ?: emptyList<Any>()
        val myMember = members.filterIsInstance<Map<String, Any?>>()
            .find { it["linkedUid"]?.toString() == currentUid || it["linked_uid"]?.toString() == currentUid }
        val rawAmount = (myMember?.get("contributionAmount")
            ?: myMember?.get("contribution_amount")
            ?: myMember?.get("amount")
            ?: 0).toString().toDoubleOrNull() ?: 0.0

        val myPlatformName = (myMember?.get("sharingPlatform")
            ?: myMember?.get("sharing_platform")
            ?: myMember?.get("platform")
            ?: "").toString()

        val groupPricingRaw = group["platformPricing"]
            ?: group["platform_pricing"]
            ?: group["platformPrices"]
            ?: group["platform_prices"]
            ?: group["platforms"]
            ?: group["sharingPlatforms"]
            ?: ""
        val groupPricingList = PlatformPricingHelper.parseAny(groupPricingRaw)

        val matchedPricing = groupPricingList.find {
            it.platformName.equals(myPlatformName, ignoreCase = true)
        }

        val myAmount = when {
            rawAmount > 0.0 -> rawAmount
            matchedPricing != null && matchedPricing.pricePerUser > 0.0 -> matchedPricing.pricePerUser
            groupPricingList.isNotEmpty() && groupPricingList.first().pricePerUser > 0.0 ->
                groupPricingList.first().pricePerUser
            else -> 0.0
        }

        val myCurrencySymbol = matchedPricing?.currencyItem?.symbol
            ?: groupPricingList.firstOrNull()?.currencyItem?.symbol
            ?: "€"

        val nextPayment = (myMember?.get("nextPaymentDate") ?: myMember?.get("next_payment_date") ?: "").toString()
        val nextPaymentFormatted = if (nextPayment.length >= 10) {
            val parts = nextPayment.substring(0, 10).split("-")
            if (parts.size == 3) "${parts[2]}-${parts[1]}-${parts[0]}" else nextPayment
        } else nextPayment
        val isPaid = myMember?.get("isPendingPayment") != true
        val showOwnerName = group["showMainUserToMembers"] == true
        val ownerName = (group["mainUserName"] ?: "").toString()
        val platform = (myMember?.get("sharingPlatform") ?: myMember?.get("sharing_platform") ?: "").toString()

        AlertDialog(
            onDismissRequest = { selectedClientGroup = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(groupName, fontWeight = FontWeight.Black)
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Cliente",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (showOwnerName && ownerName.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Titular:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(ownerName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (platform.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Plataforma:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(platform, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Tu parte:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${String.format("%.2f", myAmount)} $myCurrencySymbol", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    if (nextPaymentFormatted.isNotBlank()) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Próximo pago:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(nextPaymentFormatted, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Estado:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (isPaid) "Pagado" else "Pendiente",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isPaid) androidx.compose.ui.graphics.Color(0xFF10B981) else androidx.compose.ui.graphics.Color(0xFFF59E0B)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            val groupIdForAlarm = group["_docId"].toString()
                            val currentPref = clientAlarmPrefs[groupIdForAlarm] ?: (true to 3)
                            var alarmEnabled by remember(groupIdForAlarm, currentPref) { mutableStateOf(currentPref.first) }
                            var alarmDays by remember(groupIdForAlarm, currentPref) { mutableStateOf(currentPref.second) }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tu alarma", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = alarmEnabled,
                                    onCheckedChange = {
                                        alarmEnabled = it
                                        viewModel.setClientAlarmPreference(groupIdForAlarm, it, alarmDays)
                                    }
                                )
                            }
                            if (alarmEnabled) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Avisarme",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    IconButton(
                                        onClick = {
                                            val newVal = (alarmDays - 1).coerceAtLeast(1)
                                            alarmDays = newVal
                                            viewModel.setClientAlarmPreference(groupIdForAlarm, alarmEnabled, newVal)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, contentDescription = "Menos días", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        text = "$alarmDays ${if (alarmDays == 1) "día" else "días"}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    IconButton(
                                        onClick = {
                                            val newVal = (alarmDays + 1).coerceAtMost(30)
                                            alarmDays = newVal
                                            viewModel.setClientAlarmPreference(groupIdForAlarm, alarmEnabled, newVal)
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Más días", modifier = Modifier.size(16.dp))
                                    }
                                    Text(
                                        "antes",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "No recibirás avisos de este pago.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    run {
                        val ownerUidForChat = group["_ownerUid"].toString()
                        val groupIdForChat = group["_docId"].toString()
                        val myChatId = "${ownerUidForChat}_${groupIdForChat}_${currentUid}"
                        val hasUnread = unreadChatIdsForClient.contains(myChatId)

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (hasUnread)
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            border = BorderStroke(
                                if (hasUnread) 1.5.dp else 1.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = if (hasUnread) 0.9f else 0.35f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val subName = (group["platformName"] ?: group["name"] ?: "Suscripción").toString()
                                    openChatIsOwnerSide = false
                                    openChatInfo = Triple(myChatId, subName, "El gestor")
                                    viewModel.openChat(myChatId)
                                    viewModel.markChatRead(myChatId, asOwner = false)
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BadgedBox(
                                    badge = {
                                        if (hasUnread) {
                                            Badge(containerColor = Color(0xFFE11D48))
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Mensajes con el gestor",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (hasUnread) "Tienes un mensaje nuevo" else "Toca para abrir la conversación",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                                        color = if (hasUnread)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedClientGroup = null }) { Text("Cerrar") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        groupPendingLeave = group
                        selectedClientGroup = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Salir de este grupo")
                }
            }
        )
    }

    groupPendingLeave?.let { group ->
        val groupName = (group["platformName"] ?: group["name"] ?: "este grupo").toString()
        AlertDialog(
            onDismissRequest = { if (!isLeavingGroup) { groupPendingLeave = null; leaveGroupError = null } },
            title = { Text("¿Salir de $groupName?") },
            text = {
                Column {
                    Text("Dejarás de participar en este grupo.")
                    leaveGroupError?.let { err ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isLeavingGroup,
                    onClick = {
                        val ownerUid = group["_ownerUid"].toString()
                        val groupId = group["_docId"].toString()
                        isLeavingGroup = true
                        leaveGroupError = null
                        coroutineScope.launch {
                            val result = viewModel.leaveGroup(ownerUid, groupId)
                            isLeavingGroup = false
                            result.onSuccess {
                                groupPendingLeave = null
                            }.onFailure { e ->
                                leaveGroupError = e.message ?: "No se pudo salir del grupo."
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    if (isLeavingGroup) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Salir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    enabled = !isLeavingGroup,
                    onClick = { groupPendingLeave = null; leaveGroupError = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showNotificationsDialog) {
        NotificationsDialog(
            notifications = notifications,
            onMarkAllRead = { viewModel.markAllNotificationsRead() },
            onDismissNotification = { id -> viewModel.dismissNotification(id) },
            onDismissAll = { viewModel.dismissAllNotifications() },
            onDismiss = { showNotificationsDialog = false },
            onToggleRead = { id ->
                if (notifications.find { it.id == id }?.isRead == true) {
                    viewModel.unmarkNotificationRead(id)
                } else {
                    viewModel.markNotificationRead(id)
                }
            }
        )
    }

    chatListForSubscription?.let { subWithMembers ->
        ChatListDialog(
            subscriptionName = subWithMembers.subscription.platformName,
            members = subWithMembers.members,
            currentUid = currentUid,
            groupId = subWithMembers.subscription.id.toString(),
            unreadChatIdsForOwner = unreadChatIdsForOwner,
            onMemberClick = { chatId, clientUid, clientName ->
                openChatIsOwnerSide = true
                openChatInfo = Triple(chatId, subWithMembers.subscription.platformName, clientName)
                viewModel.openChat(chatId)
                viewModel.markChatRead(chatId, asOwner = true)
                chatListForSubscription = null
            },
            onDismiss = { chatListForSubscription = null }
        )
    }

    openChatInfo?.let { (chatId, subName, otherName) ->
        ChatDialog(
            subscriptionName = subName,
            otherPersonName = otherName,
            currentUid = currentUid,
            messages = chatMessages,
            onSend = { text ->
                val parts = chatId.split("_")
                if (parts.size >= 3) {
                    val ownerUid = parts[0]
                    val groupId = parts[1]
                    val clientUid = parts.drop(2).joinToString("_")
                    viewModel.sendChatMessage(
                        ownerUid = ownerUid,
                        clientUid = clientUid,
                        groupId = groupId,
                        subscriptionName = subName,
                        clientName = if (openChatIsOwnerSide) otherName else "Cliente",
                        text = text
                    )
                }
            },
            onDismiss = {
                viewModel.closeChat()
                openChatInfo = null
            }
        )
    }

    // Sync status toast
    syncStatus?.let { msg ->
        LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_SHORT).show()
            viewModel.clearSyncStatus()
        }
    }

    // Toast feedback message
    backupStatusMessage?.let { msg ->
        LaunchedEffect(msg) {
            android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show()
            viewModel.clearBackupStatusMessage()
        }
    }
}
