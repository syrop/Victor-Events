package pl.org.seva.events.main

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
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
import pl.org.seva.events.main.extension.question
import pl.org.seva.events.main.extension.toast
import pl.org.seva.events.main.init.instance
import pl.org.seva.events.main.ui.EventsTheme
import pl.org.seva.events.message.MessagesRoute
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {

    private val commAddViewModel by viewModels<CommAddViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
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

private object Routes {
    const val EVENTS = "events"
    const val EVENT_DETAILS = "event_details"
    const val EVENT_CREATE = "event_create"
    const val LOCATION_PICKER = "location_picker"
    const val LOGIN_CONFIRMATION = "login_confirmation"
    const val LOG_OUT_CONFIRMATION = "log_out_confirmation"
    const val SYSTEM_MESSAGES = "system_messages"
    const val ABOUT = "about"
    const val COMM_LIST = "comm_list"
    const val ADD_COMM = "add_comm"
    const val COMM_DETAILS = "comm_details"
    const val COMM_EDIT = "comm_edit"
    const val COMM_DELETE = "comm_delete"
    const val ADMIN_ADD = "admin_add"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsApp(commAddViewModel: CommAddViewModel) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.EVENTS

    val events by instance<Events>()
    val login by instance<Login>()
    val fsWriter by instance<FsWriter>()
    val scope = rememberCoroutineScope()

    val eventViewModel: EventViewModel = viewModel()
    val eventCreateViewModel: EventCreateViewModel = viewModel()
    val commViewModel: CommViewModel = viewModel()

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

    LaunchedEffect(currentRoute) {
        if (currentRoute != Routes.ADD_COMM) {
            searchExpanded = false
            searchQuery = ""
        }
        if (currentRoute != Routes.ADMIN_ADD) {
            adminAddress = ""
        }
    }

    val titleText = when (currentRoute) {
        Routes.EVENTS, Routes.EVENT_CREATE -> stringResource(id = R.string.app_name)
        Routes.EVENT_DETAILS -> eventViewModel.event.name
        Routes.LOCATION_PICKER -> stringResource(id = R.string.pick_location_label)
        Routes.LOGIN_CONFIRMATION -> stringResource(id = R.string.login_confirmation_label)
        Routes.LOG_OUT_CONFIRMATION -> stringResource(id = R.string.log_out_confirmation_label)
        Routes.SYSTEM_MESSAGES -> stringResource(id = R.string.system_messages_label)
        Routes.ABOUT -> stringResource(id = R.string.about_label)
        Routes.COMM_LIST -> stringResource(id = R.string.comm_list_label)
        Routes.ADD_COMM -> stringResource(id = R.string.add_comm_label)
        Routes.COMM_DETAILS -> commName ?: commViewModel.comm.name
        Routes.COMM_EDIT -> stringResource(id = R.string.comm_edit_label)
        Routes.COMM_DELETE -> stringResource(id = R.string.comm_delete_title)
                .replace(COMM_DELETE_NAME_PLACEHOLDER, commViewModel.comm.name)
        Routes.ADMIN_ADD -> stringResource(id = R.string.admin_add_label)
        else -> stringResource(id = R.string.app_name)
    }

    fun navigateUp() {
        when (currentRoute) {
            Routes.ADD_COMM -> {
                commAddViewModel.resetQuery()
                navController.popBackStack()
            }
            Routes.COMM_EDIT -> {
                commViewModel.reset()
                navController.popBackStack()
            }
            Routes.EVENT_CREATE -> {
                if (eventCreateViewModel.isFilledIn) {
                    context.question(
                            message = context.getString(R.string.main_activity_dismiss_event),
                            yes = {
                                eventCreateViewModel.clear()
                                navController.popBackStack()
                            },
                    )
                } else {
                    navController.popBackStack()
                }
            }
            else -> navController.popBackStack()
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
                            if ((currentRoute == Routes.ADD_COMM) && searchExpanded) {
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
                            if (currentRoute != Routes.EVENTS) {
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
                                Routes.EVENTS -> {
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
                                        if (!login.isLoggedIn) {
                                            DropdownMenuItem(
                                                    text = { Text(text = stringResource(id = R.string.events_list_action_login)) },
                                                    onClick = {
                                                        overflowExpanded = false
                                                        navController.navigate(Routes.LOGIN_CONFIRMATION)
                                                    },
                                            )
                                        } else {
                                            DropdownMenuItem(
                                                    text = { Text(text = stringResource(id = R.string.events_list_action_log_out)) },
                                                    onClick = {
                                                        overflowExpanded = false
                                                        navController.navigate(Routes.LOG_OUT_CONFIRMATION)
                                                    },
                                            )
                                        }
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_comms)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    navController.navigate(Routes.COMM_LIST)
                                                },
                                        )
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_system_messages)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    navController.navigate(Routes.SYSTEM_MESSAGES)
                                                },
                                        )
                                        DropdownMenuItem(
                                                text = { Text(text = stringResource(id = R.string.events_list_action_about)) },
                                                onClick = {
                                                    overflowExpanded = false
                                                    navController.navigate(Routes.ABOUT)
                                                },
                                        )
                                    }
                                }
                                Routes.ADD_COMM -> {
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
                                Routes.COMM_DETAILS -> {
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
                                                        navController.navigate(Routes.ADMIN_ADD)
                                                    },
                                            )
                                        }
                                    }
                                }
                                Routes.COMM_EDIT -> {
                                    IconButton(
                                            onClick = {
                                                with(commViewModel.comm.copy(desc = commViewModel.desc.value.orEmpty())) {
                                                    update()
                                                    commViewModel.update()
                                                    context.toast(
                                                            context.getString(R.string.comm_edit_updated)
                                                                    .bold(COMM_EDIT_NAME_PLACEHOLDER, name),
                                                    )
                                                    navController.popBackStack()
                                                }
                                            },
                                    ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(id = android.R.string.ok),
                                        )
                                    }
                                }
                                Routes.ADMIN_ADD -> {
                                    IconButton(
                                            onClick = {
                                                fsWriter.grantAdmin(commViewModel.comm, adminAddress)
                                                context.toast(R.string.admin_add_toast)
                                                navController.popBackStack()
                                            },
                                    ) {
                                        Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = stringResource(id = android.R.string.ok),
                                        )
                                    }
                                }
                                Routes.EVENT_CREATE -> {
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
                                                        navController.popBackStack()
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
                                Routes.LOCATION_PICKER -> {
                                    IconButton(onClick = { navController.popBackStack() }) {
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
        NavHost(
                navController = navController,
                startDestination = Routes.EVENTS,
                modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
        ) {
            composable(Routes.EVENTS) {
                EventListRoute(
                        eventViewModel = eventViewModel,
                        onNavigateToEventDetails = { navController.navigate(Routes.EVENT_DETAILS) },
                        onNavigateToCreateEvent = { navController.navigate(Routes.EVENT_CREATE) },
                )
            }
            composable(Routes.EVENT_DETAILS) {
                EventDetailsRoute(eventViewModel = eventViewModel)
            }
            composable(Routes.EVENT_CREATE) {
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
                        onNavigateToLocationPicker = { navController.navigate(Routes.LOCATION_PICKER) },
                        onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.LOCATION_PICKER) {
                LocationPickerRoute(eventCreateViewModel = eventCreateViewModel)
            }
            composable(Routes.LOGIN_CONFIRMATION) {
                LoginConfirmationRoute(onBack = { navController.popBackStack() })
            }
            composable(Routes.LOG_OUT_CONFIRMATION) {
                LogOutConfirmationRoute(onBack = { navController.popBackStack() })
            }
            composable(Routes.SYSTEM_MESSAGES) {
                MessagesRoute()
            }
            composable(Routes.ABOUT) {
                AboutRoute()
            }
            composable(Routes.COMM_LIST) {
                CommListRoute(
                        commViewModel = commViewModel,
                        onNavigateToCommDetails = { navController.navigate(Routes.COMM_DETAILS) },
                        onNavigateToAddComm = { navController.navigate(Routes.ADD_COMM) },
                )
            }
            composable(Routes.ADD_COMM) {
                CommAddRoute(
                        commAddViewModel = commAddViewModel,
                        onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.COMM_DETAILS) {
                CommDetailsRoute(
                        commViewModel = commViewModel,
                        onNavigateToEdit = { navController.navigate(Routes.COMM_EDIT) },
                        onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.COMM_EDIT) {
                CommEditRoute(
                        commViewModel = commViewModel,
                        onNavigateToDelete = { navController.navigate(Routes.COMM_DELETE) },
                        onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.COMM_DELETE) {
                CommDeleteRoute(
                        commViewModel = commViewModel,
                        onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ADMIN_ADD) {
                AdminAddScreen(
                        address = adminAddress,
                        onAddressChange = { adminAddress = it },
                )
            }
        }
    }
}
