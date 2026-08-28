package com.apleq.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Subscriptions
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.apleq.app.ui.theme.Quicksand
import com.apleq.app.data.local.SubscriptionEntity
import com.apleq.app.ui.components.AddEditMemberDialog
import com.apleq.app.ui.components.AddEditSubscriptionDialog
import com.apleq.app.ui.components.FinancialSummaryCard
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
    val allSubscriptions by viewModel.allSubscriptions.collectAsStateWithLifecycle()
    val filteredSubscriptions by viewModel.filteredSubscriptions.collectAsStateWithLifecycle()
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

    var subscriptionToDelete by remember { mutableStateOf<SubscriptionEntity?>(null) }

    val rawCategories = listOf("Todas", "Streaming", "Música", "Productividad", "Gaming", "Educación", "Salud")

    val showBackupRestoreDialog by viewModel.showBackupRestoreDialog.collectAsStateWithLifecycle()
    val pendingRestorePreview by viewModel.pendingRestorePreview.collectAsStateWithLifecycle()
    val backupStatusMessage by viewModel.backupStatusMessage.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
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
                                    (authState as com.apleq.app.data.remote.AuthState.Authenticated).user.email ?: "Apleq"
                                } else {
                                    I18n.appSubtitle
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
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
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddSubscription() },
                icon = { Icon(Icons.Default.Add, contentDescription = I18n.newSubscription) },
                text = { Text(I18n.newSubscription, fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_subscription")
            )
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
                        onAddMemberClick = { viewModel.openAddMember(item) },
                        onEditClick = { viewModel.openEditSubscription(item.subscription) },
                        onDeleteClick = { subscriptionToDelete = item.subscription },
                        onMemberClick = { member -> viewModel.openEditMember(member, item) }
                    )
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
            userEmail = (authState as? com.apleq.app.data.remote.AuthState.Authenticated)?.user?.email
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
            onSignInWithGoogle = { viewModel.signInWithGoogle() },
            onSignInWithEmail = { email, pass -> viewModel.signInWithEmail(email, pass) },
            onRegisterWithEmail = { email, pass -> viewModel.registerWithEmail(email, pass) },
            onSignOut = { viewModel.signOut() },
            onSyncToCloud = { viewModel.syncToCloud() },
            onSyncFromCloud = { viewModel.syncFromCloud() },
            onCleanAndPruneDatabase = { viewModel.cleanAndPruneFirebaseDatabase() },
            onClearError = { viewModel.clearAuthError() }
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
