package pl.org.seva.events.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import pl.org.seva.events.R
import pl.org.seva.events.about.AboutRoute
import pl.org.seva.events.comm.AdminAddScreen
import pl.org.seva.events.comm.COMM_DELETE_NAME_PLACEHOLDER
import pl.org.seva.events.comm.COMM_EDIT_NAME_PLACEHOLDER
import pl.org.seva.events.comm.CommAddRoute
import pl.org.seva.events.comm.CommAddViewModel
import pl.org.seva.events.comm.CommDeleteRoute
import pl.org.seva.events.comm.CommDetailsRoute
import pl.org.seva.events.comm.CommEditRoute
import pl.org.seva.events.comm.CommListRoute
import pl.org.seva.events.comm.CommViewModel
import pl.org.seva.events.event.Event
import pl.org.seva.events.event.EventCreateRoute
import pl.org.seva.events.event.EventCreateViewModel
import pl.org.seva.events.event.EventDetailsRoute
import pl.org.seva.events.event.EventListRoute
import pl.org.seva.events.event.EventViewModel
import pl.org.seva.events.event.Events
import pl.org.seva.events.event.LocationPickerRoute
import pl.org.seva.events.login.LogOutConfirmationRoute
import pl.org.seva.events.login.Login
import pl.org.seva.events.login.LoginConfirmationRoute
import pl.org.seva.events.main.data.firestore.FsWriter
import pl.org.seva.events.main.extension.bold
import pl.org.seva.events.main.extension.inBrowser
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.message.MessagesRoute
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val commAddViewModel by viewModels<CommAddViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            EventsTheme {
                EventsApp(commAddViewModel = commAddViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            intent.getStringExtra(SearchManager.QUERY)?.let { query ->
                commAddViewModel.query(query)
            }
        }
    }
}

@Serializable data object EventsNavKey : NavKey
@Serializable data object EventDetailsNavKey : NavKey
@Serializable data object EventCreateNavKey : NavKey
@Serializable data object LocationPickerNavKey : NavKey
@Serializable data object LoginConfirmationNavKey : NavKey
@Serializable data object LogOutConfirmationNavKey : NavKey
@Serializable data object SystemMessagesNavKey : NavKey
@Serializable data object AboutNavKey : NavKey
@Serializable data object CommListNavKey : NavKey
@Serializable data object AddCommNavKey : NavKey
@Serializable data object CommDetailsNavKey : NavKey
@Serializable data object CommEditNavKey : NavKey
@Serializable data object CommDeleteNavKey : NavKey
@Serializable data object AdminAddNavKey : NavKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsApp(commAddViewModel: CommAddViewModel) {
    val context = LocalContext.current
    val navigationState = rememberNavigationState(
            startRoute = EventsNavKey,
            topLevelRoutes = setOf(EventsNavKey),
    )
    val navigator = remember { Navigator(navigationState) }
    val currentRoute = navigationState.backStacks[navigationState.topLevelRoute]?.lastOrNull() ?: EventsNavKey

    val events by instance<Events>()
    val login by instance<Login>()
    val fsWriter by instance<FsWriter>()
    val scope = rememberCoroutineScope()

    val eventViewModel: EventViewModel = viewModel()
    val eventCreateViewModel: EventCreateViewModel = viewModel()
    val commViewModel: CommViewModel = viewModel()

    val isLoggedIn by login.isLoggedInFlow.collectAsStateWithLifecycle()
    val commName by commViewModel.name.collectAsStateWithLifecycle()
    val isCommAdmin by commViewModel.isAdmin.collectAsStateWithLifecycle()

    var adminAddress by rememberSaveable { mutableStateOf("") }
    var searchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var overflowExpanded by remember { mutableStateOf(false) }

    var commError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var dateError by remember { mutableStateOf(false) }
    var showDismissEventDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentRoute) {
        if (currentRoute != AddCommNavKey) {
            searchExpanded = false
            searchQuery = ""
        }
        if (currentRoute != AdminAddNavKey) {
            adminAddress = ""
        }
    }

    val titleText = when (currentRoute) {
        EventsNavKey, EventCreateNavKey -> stringResource(id = R.string.app_name)
        EventDetailsNavKey -> eventViewModel.event.name
        LocationPickerNavKey -> stringResource(id = R.string.pick_location_label)
        LoginConfirmationNavKey -> stringResource(id = R.string.login_confirmation_label)
        LogOutConfirmationNavKey -> stringResource(id = R.string.log_out_confirmation_label)
        SystemMessagesNavKey -> stringResource(id = R.string.system_messages_label)
        AboutNavKey -> stringResource(id = R.string.about_label)
        CommListNavKey -> stringResource(id = R.string.comm_list_label)
        AddCommNavKey -> stringResource(id = R.string.add_comm_label)
        CommDetailsNavKey -> commName ?: commViewModel.comm.name
        CommEditNavKey -> stringResource(id = R.string.comm_edit_label)
        CommDeleteNavKey -> stringResource(id = R.string.comm_delete_title)
                .replace(COMM_DELETE_NAME_PLACEHOLDER, commViewModel.comm.name)
        AdminAddNavKey -> stringResource(id = R.string.admin_add_label)
        else -> stringResource(id = R.string.app_name)
    }

    fun navigateUp() {
        when (currentRoute) {
            AddCommNavKey -> {
                commAddViewModel.resetQuery()
                navigator.goBack()
            }
            CommEditNavKey -> {
                commViewModel.reset()
                navigator.goBack()
            }
            EventCreateNavKey -> {
                if (eventCreateViewModel.isFilledIn) {
                    showDismissEventDialog = true
                } else {
                    navigator.goBack()
                }
            }
            else -> navigator.goBack()
        }
    }

    if (showDismissEventDialog) {
        AlertDialog(
                onDismissRequest = { showDismissEventDialog = false },
                text = { Text(text = stringResource(id = R.string.main_activity_dismiss_event)) },
                confirmButton = {
                    TextButton(
                            onClick = {
                                showDismissEventDialog = false
                                eventCreateViewModel.clear()
                                navigator.goBack()
                            },
                    ) {
                        Text(text = stringResource(id = android.R.string.ok))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDismissEventDialog = false }) {
                        Text(text = stringResource(id = android.R.string.cancel))
                    }
                },
        )
    }

    val entryProvider = entryProvider {
        entry<EventsNavKey> {
            EventListRoute(
                    eventViewModel = eventViewModel,
                    onNavigateToEventDetails = { navigator.navigate(EventDetailsNavKey) },
                    onNavigateToCreateEvent = { navigator.navigate(EventCreateNavKey) },
            )
        }
        entry<EventDetailsNavKey> {
            EventDetailsRoute(eventViewModel = eventViewModel)
        }
        entry<EventCreateNavKey> {
            EventCreateRoute(
                    vm = eventCreateViewModel,
                    commError = commError,
                    nameError = nameError,
                    timeError = timeError,
                    dateError = dateError,
                    onClearCommError = { commError = false },
                    onClearNameError = { nameError = false },
                    onClearTimeError = { timeError = false },
                    onClearDateError = { dateError = false },
                    onNavigateToLocationPicker = { navigator.navigate(LocationPickerNavKey) },
                    onBack = { navigator.goBack() },
            )
        }
        entry<LocationPickerNavKey> {
            LocationPickerRoute(eventCreateViewModel = eventCreateViewModel)
        }
        entry<LoginConfirmationNavKey> {
            LoginConfirmationRoute(onBack = { navigator.goBack() })
        }
        entry<LogOutConfirmationNavKey> {
            LogOutConfirmationRoute(onBack = { navigator.goBack() })
        }
        entry<SystemMessagesNavKey> {
            MessagesRoute()
        }
        entry<AboutNavKey> {
            AboutRoute()
        }
        entry<CommListNavKey> {
            CommListRoute(
                    commViewModel = commViewModel,
                    onNavigateToCommDetails = { navigator.navigate(CommDetailsNavKey) },
                    onNavigateToAddComm = { navigator.navigate(AddCommNavKey) },
            )
        }
        entry<AddCommNavKey> {
            CommAddRoute(
                    commAddViewModel = commAddViewModel,
                    onBack = { navigator.goBack() },
            )
        }
        entry<CommDetailsNavKey> {
            CommDetailsRoute(
                    commViewModel = commViewModel,
                    onNavigateToEdit = { navigator.navigate(CommEditNavKey) },
                    onBack = { navigator.goBack() },
            )
        }
        entry<CommEditNavKey> {
            CommEditRoute(
                    commViewModel = commViewModel,
                    onNavigateToDelete = { navigator.navigate(CommDeleteNavKey) },
                    onBack = { navigator.goBack() },
            )
        }
        entry<CommDeleteNavKey> {
            CommDeleteRoute(
                    commViewModel = commViewModel,
                    onBack = { navigator.goBack() },
            )
        }
        entry<AdminAddNavKey> {
            AdminAddScreen(
                    address = adminAddress,
                    onAddressChange = { adminAddress = it },
            )
        }
    }

    Scaffold(
            topBar = {
                TopAppBar(
                        colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        title = {
                            if ((currentRoute == AddCommNavKey) && searchExpanded) {
                                TextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        placeholder = {
                                            Text(
                                                    text = stringResource(id = R.string.searchable_add_community_hint),
                                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                                            )
                                        },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                        keyboardActions = KeyboardActions(
                                                onSearch = {
                                                    if (searchQuery.isNotBlank()) {
                                                        commAddViewModel.query(searchQuery)
                                                    }
                                                },
                                        ),
                                        colors = TextFieldDefaults.colors(
                                                focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                                                focusedContainerColor = Color.Transparent,
                                                unfocusedContainerColor = Color.Transparent,
                                                cursorColor = MaterialTheme.colorScheme.onPrimary,
                                        ),
                                        modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                Text(text = titleText)
                            }
                        },
                        navigationIcon = {
                            if (currentRoute != EventsNavKey) {
                                IconButton(onClick = { navigateUp() }) {
                                    Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                    )
                                }
                            }
                        },
                        actions = {
                            when (currentRoute) {
                                EventsNavKey -> {
                                    IconButton(onClick = { overflowExpanded = true }) {
                                        Icon(
                                                imageVector = Icons.Default.MoreVert,
                                                contentDescription = null,
                                        )
                                    }
                                    DropdownMenu(
                                            expanded = overflowExpanded,
                                            onDismissRequest = { overflowExpanded = false },
                                    ) {
                                        if (!isLoggedIn) {
                                            DropdownMenuItem(
                                                    text = { Text(text = stringResource(id = R.string.events_list_action_login)) },
                                                    onClick = {
                                                        overflowExpanded = false
                                                        navigator.navigate(LoginConfirmationNavKey)
                                                    },
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                    text = { Text(text = stringResource(id = R.string.events_list_action_log_out)) },
                                                    onClick = {
                                                        overflowExpanded = false
                                                        navigator.navigate(LogOutConfirmationNavKey)
                                                    },
                                            )
                                        }
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_comms)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    navigator.navigate(CommListNavKey)
                                                },
                                        )
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_system_messages)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    navigator.navigate(SystemMessagesNavKey)
                                                },
                                        )
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_privacy_policy)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    context.inBrowser(context.getString(R.string.login_confirmation_privacy_uri))
                                                },
                                        )
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_about)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    navigator.navigate(AboutNavKey)
                                                },
                                        )
                                    }
                                }
                                AddCommNavKey -> {
                                    if (searchExpanded) {
                                        IconButton(
                                                onClick = {
                                                    if (searchQuery.isNotBlank()) {
                                                        commAddViewModel.query(searchQuery)
                                                    }
                                                },
                                        ) {
                                            Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = stringResource(id = R.string.add_comm_action_search),
                                            )
                                        }
                                        IconButton(
                                                onClick = {
                                                    searchQuery = ""
                                                    searchExpanded = false
                                                },
                                        ) {
                                            Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = null,
                                            )
                                        }
                                    } else {
                                        IconButton(onClick = { searchExpanded = true }) {
                                            Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = stringResource(id = R.string.add_comm_action_search),
                                            )
                                        }
                                    }
                                }
                                CommDetailsNavKey -> {
                                    if (isCommAdmin == true) {
                                        IconButton(onClick = { overflowExpanded = true }) {
                                            Icon(
                                                    imageVector = Icons.Default.MoreVert,
                                                    contentDescription = null,
                                            )
                                        }
                                        DropdownMenu(
                                                expanded = overflowExpanded,
                                                onDismissRequest = { overflowExpanded = false },
                                        ) {
                                            DropdownMenuItem(
                                                    text = { Text(text = stringResource(id = R.string.comm_details_add_admin)) },
                                                    onClick = {
                                                        overflowExpanded = false
                                                        navigator.navigate(AdminAddNavKey)
                                                    },
                                            )
                                        }
                                    }
                                }
                                CommEditNavKey -> {
                                    IconButton(
                                            onClick = {
                                                with(commViewModel.comm.copy(desc = commViewModel.desc.value.orEmpty())) {
                                                    update()
                                                    commViewModel.update()
                                                    context.toast(
                                                            context.getString(R.string.comm_edit_updated)
                                                                    .bold(COMM_EDIT_NAME_PLACEHOLDER, name),
                                                    )
                                                    navigator.goBack()
                                                }
                                            },
                                    ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(id = android.R.string.ok),
                                        )
                                    }
                                }
                                AdminAddNavKey -> {
                                    IconButton(
                                            onClick = {
                                                fsWriter.grantAdmin(commViewModel.comm, adminAddress)
                                                context.toast(R.string.admin_add_toast)
                                                navigator.goBack()
                                            },
                                    ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(id = android.R.string.ok),
                                        )
                                    }
                                }
                                EventCreateNavKey -> {
                                    IconButton(
                                            onClick = {
                                                commError = eventCreateViewModel.comm.value.isNullOrEmpty()
                                                nameError = eventCreateViewModel.name.value.isNullOrEmpty()
                                                timeError = eventCreateViewModel.time.value == null
                                                dateError = eventCreateViewModel.date.value == null

                                                if (!commError && !nameError && !timeError && !dateError) {
                                                    scope.launch {
                                                        events.add(
                                                                Event(
                                                                        comm = checkNotNull(eventCreateViewModel.comm.value),
                                                                        name = checkNotNull(eventCreateViewModel.name.value),
                                                                        time = LocalDateTime.of(
                                                                                checkNotNull(eventCreateViewModel.date.value),
                                                                                checkNotNull(eventCreateViewModel.time.value),
                                                                        ),
                                                                        location = eventCreateViewModel.location.value?.location,
                                                                        address = eventCreateViewModel.location.value?.address,
                                                                        desc = eventCreateViewModel.desc.value,
                                                                ),
                                                        )
                                                        eventCreateViewModel.clear()
                                                        navigator.goBack()
                                                    }
                                                }
                                            },
                                    ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(id = android.R.string.ok),
                                        )
                                    }
                                }
                                LocationPickerNavKey -> {
                                    IconButton(onClick = { navigator.goBack() }) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(id = android.R.string.ok),
                                        )
                                    }
                                }
                            }
                        },
                )
            },
    ) { innerPadding ->
        NavDisplay(
                entries = navigationState.toEntries(entryProvider),
                onBack = { navigateUp() },
                modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding),
        )
    }
}
