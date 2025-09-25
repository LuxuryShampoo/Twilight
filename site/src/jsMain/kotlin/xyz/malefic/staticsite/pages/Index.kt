package xyz.malefic.staticsite.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.localStorage
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*
import org.w3c.dom.HTMLElement
import xyz.malefic.staticsite.components.calendar.CalendarCell
import xyz.malefic.staticsite.components.WeeklyTaskManager
import xyz.malefic.staticsite.components.WeeklyTask
import xyz.malefic.staticsite.components.TaskPriority
import xyz.malefic.staticsite.util.CalendarEvent
import xyz.malefic.staticsite.util.CalendarUtils
import xyz.malefic.staticsite.util.EventMode
import xyz.malefic.staticsite.util.RecurrenceFrequency
import xyz.malefic.staticsite.util.ThemeManager
import kotlin.js.Date

@Page
@Composable
fun HomePage() {
    // Load saved settings from localStorage
    val savedTitle = localStorage.getItem("calendarTheme") ?: "Twilight Calendar"
    val savedThemeJson = localStorage.getItem("calendarTheme")

    // Default colors
    var primaryColor = "#9c27b0" // Deep Purple
    var secondaryColor = "#e91e63" // Pink
    var backgroundColor = "#ffffff" // White

    // Try to parse saved theme
    if (savedThemeJson != null) {
        try {
            val theme = js("JSON.parse(savedThemeJson)")
            primaryColor = js("theme.primaryColor || primaryColor") as String
            secondaryColor = js("theme.secondaryColor || secondaryColor") as String
            backgroundColor = js("theme.backgroundColor || backgroundColor") as String
        } catch (e: Exception) {
            console.error("Failed to parse saved theme: ${e.message}")
        }
    }

    // State for calendar title
    var calendarTitle by remember { mutableStateOf(savedTitle) }
    var isEditingTitle by remember { mutableStateOf(false) }

    // State for current date
    val currentDate = remember { Date() }
    var displayDate by remember { mutableStateOf(currentDate) }

    // State for events
    val events =
        remember {
            mutableStateListOf<CalendarEvent>().apply {
                // Add a test event for debugging
                val now = Date()
                val testEvent =
                    CalendarEvent(
                        id = "test-event-1",
                        title = "Test Event",
                        description = "This is a test event",
                        startTime = Date(now.getFullYear(), now.getMonth(), now.getDate(), 9), // 9 AM
                        endTime = Date(now.getFullYear(), now.getMonth(), now.getDate(), 10), // 10 AM
                        mode = EventMode.PASSIVE,
                    )
                add(testEvent)
            }
        }

    // State for weekly tasks
    val tasks = remember { mutableStateListOf<WeeklyTask>() }

    // Calendar configuration - removed EventStyleConfig as it doesn't exist
    // LaunchedEffect(Unit) {
    //     // Event style configuration would go here
    // }

    // State for event creation dialog
    var showEventDialog by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventDescription by remember { mutableStateOf("") }
    var newEventStartTime by remember { mutableStateOf<Date?>(null) }
    var newEventEndTime by remember { mutableStateOf<Date?>(null) }
    var newEventMode by remember { mutableStateOf(EventMode.PASSIVE) }
    
    // State for task creation dialog
    var showTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskStartTime by remember { mutableStateOf<Date?>(null) }
    var newTaskEndTime by remember { mutableStateOf<Date?>(null) }
    
    // State for recurring options
    var isRecurring by remember { mutableStateOf(false) }
    var recurrenceFrequency by remember { mutableStateOf(RecurrenceFrequency.DAILY) }
    var customHours by remember { mutableStateOf(24) }

    // Get month and year for display
    val monthNames =
        listOf(
            "January",
            "February",
            "March",
            "April",
            "May",
            "June",
            "July",
            "August",
            "September",
            "October",
            "November",
            "December",
        )
    val currentMonth = remember(displayDate) { monthNames[displayDate.getMonth()] }
    val currentYear = remember(displayDate) { displayDate.getFullYear() }

    // Days of the week
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    // Hours of the day (6 AM to 5:59 AM)
    val hours =
        listOf(
            "6 AM",
            "7 AM",
            "8 AM",
            "9 AM",
            "10 AM",
            "11 AM",
            "12 PM",
            "1 PM",
            "2 PM",
            "3 PM",
            "4 PM",
            "5 PM",
            "6 PM",
            "7 PM",
            "8 PM",
            "9 PM",
            "10 PM",
            "11 PM",
            "12 AM",
            "1 AM",
            "2 AM",
            "3 AM",
            "4 AM",
            "5 AM",
        )

    Column(
        Modifier
            .fillMaxSize()
            .backgroundColor(
                org.jetbrains.compose.web.css
                    .Color(ThemeManager.Colors.background),
            ).padding(16.px),
    ) {
        // Top bar with dark mode toggle
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Button(
                attrs = {
                    onClick { ThemeManager.toggleDarkMode() }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(
                            org.jetbrains.compose.web.css
                                .Color(if (ThemeManager.isDarkMode) "#222" else "#eee"),
                        )
                        color(
                            org.jetbrains.compose.web.css
                                .Color(if (ThemeManager.isDarkMode) "#fff" else "#222"),
                        )
                        border(0.px)
                        borderRadius(6.px)
                        fontSize(14.px)
                        fontWeight(600)
                        cursor(Cursor.Pointer)
                    }
                },
            ) {
                Text(if (ThemeManager.isDarkMode) "☀️ Light Mode" else "🌙 Dark Mode")
            }
        }

        // Calendar Title
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 16.px)
                .onClick { isEditingTitle = true },
        ) {
            if (isEditingTitle) {
                // Editable title
                TextInput(
                    attrs = {
                        value(calendarTitle)
                        onInput { calendarTitle = it.value }
                        onKeyDown { event ->
                            if (event.key == "Enter") {
                                isEditingTitle = false
                                // Save to localStorage
                                js("localStorage.setItem('calendarTitle', calendarTitle)")
                            }
                        }
                        onBlur {
                            isEditingTitle = false
                            // Save to localStorage
                            js("localStorage.setItem('calendarTitle', calendarTitle)")
                        }
                        style {
                            fontSize(32.px)
                            fontWeight(700)
                            color(Color(primaryColor))
                            width(100.percent)
                            border(0.px)
                            outline("none")
                            backgroundColor(Colors.Transparent)
                            margin(0.px)
                            padding(0.px)
                        }
                    },
                )
            } else {
                // Display title
                H1(
                    attrs = {
                        style {
                            color(Color(primaryColor))
                            margin(0.px)
                            cursor(Cursor.Pointer)
                        }
                    },
                ) {
                    Text(calendarTitle)
                }
            }
        }

        // Calendar Description
        Box(Modifier.fillMaxWidth().padding(bottom = 24.px)) {
            P(
                attrs = {
                    style {
                        fontSize(16.px)
                        property("line-height", "1.5")
                        color(Color("#666"))
                        margin(0.px)
                    }
                },
            ) {
                Text(
                    "Welcome to your aesthetic calendar! Create events, customize your schedule, and stay organized with this beautiful calendar interface.",
                )
            }
        }

        // Calendar Navigation
        Box(Modifier.fillMaxWidth().padding(bottom = 16.px)) {
            // Use a table layout for navigation
            Table(
                attrs = {
                    style {
                        width(100.percent)
                    }
                },
            ) {
                Tr {
                    // Month and Year
                    Td(
                        attrs = {
                            style {
                                textAlign(TextAlign.Left)
                            }
                        },
                    ) {
                        H2(
                            attrs = {
                                style {
                                    margin(0.px)
                                    color(Color("#333"))
                                }
                            },
                        ) {
                            Text("$currentMonth $currentYear")
                        }
                    }

                    // Navigation Buttons
                    Td(
                        attrs = {
                            style {
                                textAlign(TextAlign.Right)
                            }
                        },
                    ) {
                        Button(
                            attrs = {
                                onClick {
                                    // Go to previous month by creating a new Date with the previous month
                                    val currentMonth = displayDate.getMonth()
                                    val currentYear = displayDate.getFullYear()

                                    val newMonth = if (currentMonth == 0) 11 else currentMonth - 1
                                    val newYear = if (currentMonth == 0) currentYear - 1 else currentYear

                                    displayDate = Date(newYear, newMonth, 1)
                                }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(Color("#f0f0f0"))
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    marginRight(8.px)
                                }
                            },
                        ) {
                            Text("Previous Month")
                        }

                        Button(
                            attrs = {
                                onClick {
                                    // Reset to current date
                                    displayDate = Date()
                                }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(Color("#f0f0f0"))
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    marginRight(8.px)
                                }
                            },
                        ) {
                            Text("Today")
                        }

                        Button(
                            attrs = {
                                onClick {
                                    // Go to next month by creating a new Date with the next month
                                    val currentMonth = displayDate.getMonth()
                                    val currentYear = displayDate.getFullYear()

                                    val newMonth = if (currentMonth == 11) 0 else currentMonth + 1
                                    val newYear = if (currentMonth == 11) currentYear + 1 else currentYear

                                    displayDate = Date(newYear, newMonth, 1)
                                }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(Color("#f0f0f0"))
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                }
                            },
                        ) {
                            Text("Next Month")
                        }
                    }
                }
            }
        }

        // Calendar Grid
        Box(
            Modifier
                .fillMaxWidth()
                .display(DisplayStyle.Grid)
                .styleModifier {
                    property("grid-template-columns", "60px repeat(7, 1fr)")
                    property("grid-template-rows", "auto auto repeat(24, 40px)")
                    property("gap", "1px")
                }.border(1.px, LineStyle.Solid, Color("#e6e6e6"))
                .borderRadius(8.px)
                .overflow(Overflow.Hidden),
        ) {
            // Empty cell for time column header
            Box(
                Modifier
                    .backgroundColor(Color("#f9fafb"))
                    .padding(8.px)
                    .styleModifier {
                        property("grid-column", "1")
                        property("grid-row", "1")
                    },
            ) {}

            // Day headers with day names and dates
            for (dayOffset in 0..6) {
                val dayDate = Date(displayDate.getTime() + dayOffset * 24 * 60 * 60 * 1000)
                val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                val dayName = dayNames[dayDate.getDay()]

                Box(
                    Modifier
                        .backgroundColor(Color("#f9fafb"))
                        .padding(8.px)
                        .borderBottom(1.px, LineStyle.Solid, Color("#e6e6e6"))
                        .styleModifier {
                            property("grid-column", "${dayOffset + 2}")
                            property("grid-row", "1")
                        },
                ) {
                    Column {
                        SpanText(
                            dayName,
                            Modifier
                                .fontSize(12.px)
                                .fontWeight(500)
                                .textAlign(TextAlign.Center),
                        )
                        SpanText(
                            "${dayDate.getDate()}",
                            Modifier
                                .fontSize(16.px)
                                .fontWeight(700)
                                .textAlign(TextAlign.Center),
                        )
                    }
                }
            }

            // Time column (6 AM to 5 AM)
            for (hour in 0..23) {
                val displayHour = (hour + 6) % 24 // Start from 6 AM
                val formattedHour =
                    when {
                        displayHour == 0 -> "12 AM"
                        displayHour < 12 -> "$displayHour AM"
                        displayHour == 12 -> "12 PM"
                        else -> "${displayHour - 12} PM"
                    }

                Box(
                    Modifier
                        .backgroundColor(Color("#f9fafb"))
                        .padding(4.px)
                        .borderRight(1.px, LineStyle.Solid, Color("#e6e6e6"))
                        .styleModifier {
                            property("grid-column", "1")
                            property("grid-row", "${hour + 3}")
                        },
                ) {
                    SpanText(
                        formattedHour,
                        Modifier
                            .fontSize(10.px)
                            .color(Color("#646464"))
                            .textAlign(TextAlign.Right),
                    )
                }
            }

            // Calendar cells (6 AM to 5 AM)
            for (dayOffset in 0..6) {
                val dayDate = Date(displayDate.getTime() + dayOffset * 24 * 60 * 60 * 1000)

                for (hour in 0..23) {
                    val actualHour = (hour + 6) % 24 // Start from 6 AM
                    val cellDate = Date(dayDate.getFullYear(), dayDate.getMonth(), dayDate.getDate(), actualHour)

                    Box(
                        Modifier
                            .width(100.percent)
                            .backgroundColor(Colors.White)
                            .border(1.px, LineStyle.Solid, Color("#f0f0f0"))
                            .minHeight(40.px)
                            .position(Position.Relative)
                            .id("cell-$dayOffset-$hour")
                            .styleModifier {
                                property("grid-column", "${dayOffset + 2}")
                                property("grid-row", "${hour + 3}")
                            }.attrsModifier {
                                // Add data attributes for drag and drop
                                attr("data-date", "${cellDate.getFullYear()}-${cellDate.getMonth()}-${cellDate.getDate()}")
                                attr("data-hour", "$actualHour")
                                attr("data-cell", "true")

                                // Handle drop events for calendar cells
                                onDragOver { e ->
                                    e.preventDefault()
                                    e.stopPropagation()
                                }

                                onDrop { e ->
                                    e.preventDefault()
                                    e.stopPropagation()

                                    val eventId = e.dataTransfer?.getData("text/plain") ?: return@onDrop
                                    val targetEvent = events.find { it.id == eventId } ?: return@onDrop

                                    // Calculate the offset within the cell (0-1)
                                    val rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
                                    val offsetY = (e.clientY - rect.top) / rect.height

                                    // Create a new date at the drop position
                                    val newDate = Date(cellDate.getFullYear(), cellDate.getMonth(), cellDate.getDate(), actualHour)
                                    val minutes = (offsetY * 60).toInt()
                                    // Use asDynamic() to access JavaScript methods
                                    newDate.asDynamic().setMinutes(minutes)

                                    // Calculate the duration of the event
                                    val duration = targetEvent.endTime.getTime() - targetEvent.startTime.getTime()

                                    // Create updated event with new time
                                    val updatedEvent =
                                        targetEvent.copy(
                                            startTime = newDate,
                                            endTime = Date(newDate.getTime() + duration),
                                        )

                                    // Update the event
                                    val index = events.indexOfFirst { it.id == updatedEvent.id }
                                    if (index >= 0) {
                                        events[index] = updatedEvent
                                    }
                                }
                            },
                    ) {
                        CalendarCell(
                            date = cellDate,
                            hour = actualHour,
                            events =
                                events.filter { event ->
                                    // Only show events that match this specific date and hour
                                    event.startTime.getFullYear() == cellDate.getFullYear() &&
                                        event.startTime.getMonth() == cellDate.getMonth() &&
                                        event.startTime.getDate() == cellDate.getDate() &&
                                        event.startTime.getHours() == actualHour
                                },
                            allEvents = events, // Pass full events list for drop operations
                            onEventClick = { event ->
                                // Event click handled in CalendarCell
                            },
                            onEventUpdate = { updatedEvent ->
                                val index = events.indexOfFirst { it.id == updatedEvent.id }
                                if (index >= 0) {
                                    events[index] = updatedEvent
                                }
                            },
                            onEventDelete = { deletedEvent ->
                                val index = events.indexOfFirst { it.id == deletedEvent.id }
                                if (index >= 0) {
                                    events.removeAt(index)
                                }
                            },
                        )
                    }
                }
            }
        }

        // Calendar Controls
        Box(Modifier.fillMaxWidth().padding(top = 24.px)) {
            // Use a table layout for controls
            Table(
                attrs = {
                    style {
                        width(100.percent)
                    }
                },
            ) {
                Tr {
                    // Add Event and Task buttons
                    Td(
                        attrs = {
                            style {
                                textAlign(TextAlign.Left)
                            }
                        },
                    ) {
                        Button(
                            attrs = {
                                onClick { showEventDialog = true }
                                style {
                                    padding(12.px, 24.px)
                                    backgroundColor(Color(primaryColor))
                                    color(Colors.White)
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    fontSize(16.px)
                                    fontWeight(500)
                                    marginRight(12.px)
                                }
                            },
                        ) {
                            Text("+ Add Event")
                        }
                        
                        Button(
                            attrs = {
                                onClick { showTaskDialog = true }
                                style {
                                    padding(12.px, 24.px)
                                    backgroundColor(Color("#dc2626")) // Red for tasks (active)
                                    color(Colors.White)
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    fontSize(16.px)
                                    fontWeight(500)
                                }
                            },
                        ) {
                            Text("+ Add Task")
                        }
                    }

                    // Today button
                    Td(
                        attrs = {
                            style {
                                textAlign(TextAlign.Right)
                            }
                        },
                    ) {
                        Button(
                            attrs = {
                                onClick { 
                                    displayDate = Date()
                                }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(Color(if (ThemeManager.isDarkMode) "#4A4A4A" else "#f0f0f0"))
                                    color(Color(ThemeManager.Colors.text))
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    fontSize(14.px)
                                }
                            },
                        ) {
                            Text("Today")
                        }
                    }
                }
            }
        }

        // Event Creation Dialog
        if (showEventDialog) {
            Box(
                modifier =
                    Modifier
                        .position(Position.Fixed)
                        .top(0.px)
                        .left(0.px)
                        .right(0.px)
                        .bottom(0.px)
                        .backgroundColor(
                            com.varabyte.kobweb.compose.ui.graphics.Color
                                .rgba(0f, 0f, 0f, 0.5f),
                        ).display(DisplayStyle.Flex)
                        .styleModifier {
                            property("justify-content", "center")
                            property("align-items", "center")
                            property("z-index", "1000")
                        },
            ) {
                Box(
                    modifier =
                        Modifier
                            .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                            .padding(24.px)
                            .borderRadius(8.px)
                            .maxWidth(400.px)
                            .width(100.percent),
                ) {
                    Column {
                        SpanText(
                            "Create New Event",
                            Modifier
                                .fontSize(18.px)
                                .fontWeight(700)
                                .margin(bottom = 16.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        SpanText(
                            "Title",
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(500)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        TextInput(
                            attrs = {
                                value(newEventTitle)
                                onInput { newEventTitle = it.value }
                                style {
                                    width(100.percent)
                                    marginBottom(12.px)
                                    padding(8.px)
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                    color(Color(ThemeManager.Colors.text))
                                }
                            },
                        )

                        SpanText(
                            "Description",
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(500)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        TextArea(
                            attrs = {
                                value(newEventDescription)
                                onInput { newEventDescription = it.value }
                                style {
                                    width(100.percent)
                                    minHeight(60.px)
                                    marginBottom(16.px)
                                    padding(8.px)
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                    color(Color(ThemeManager.Colors.text))
                                }
                            },
                        )

                        // Recurring Options
                        SpanText(
                            "Recurring",
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(500)
                                .margin(bottom = 8.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        // Recurring checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .margin(bottom = 8.px)
                                .styleModifier {
                                    property("align-items", "center")
                                    property("gap", "8px")
                                }
                        ) {
                            Input(
                                type = InputType.Checkbox,
                                attrs = {
                                    checked(isRecurring)
                                    onChange { isRecurring = it.value }
                                    style {
                                        cursor(Cursor.Pointer)
                                    }
                                }
                            )
                            SpanText(
                                "Make this event recurring",
                                Modifier
                                    .fontSize(14.px)
                                    .color(Color(ThemeManager.Colors.text))
                            )
                        }

                        if (isRecurring) {
                            // Frequency selection
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .margin(bottom = 16.px)
                                    .styleModifier {
                                        property("gap", "8px")
                                    }
                            ) {
                                listOf(
                                    RecurrenceFrequency.DAILY to "Daily",
                                    RecurrenceFrequency.WEEKLY to "Weekly",
                                    RecurrenceFrequency.CUSTOM to "Custom"
                                ).forEach { (freq, label) ->
                                    Button(
                                        attrs = {
                                            onClick { recurrenceFrequency = freq }
                                            style {
                                                padding(6.px, 12.px)
                                                backgroundColor(
                                                    if (recurrenceFrequency == freq) {
                                                        Color(ThemeManager.Colors.primaryButton)
                                                    } else {
                                                        Color(ThemeManager.Colors.buttonBackground)
                                                    }
                                                )
                                                color(
                                                    if (recurrenceFrequency == freq) {
                                                        Colors.White
                                                    } else {
                                                        Color(ThemeManager.Colors.buttonText)
                                                    }
                                                )
                                                border(0.px)
                                                borderRadius(4.px)
                                                cursor(Cursor.Pointer)
                                                fontSize(12.px)
                                            }
                                        },
                                    ) {
                                        SpanText(label)
                                    }
                                }
                            }

                            if (recurrenceFrequency == RecurrenceFrequency.CUSTOM) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .margin(bottom = 16.px)
                                        .styleModifier {
                                            property("align-items", "center")
                                            property("gap", "8px")
                                        }
                                ) {
                                    SpanText(
                                        "Every",
                                        Modifier
                                            .fontSize(14.px)
                                            .color(Color(ThemeManager.Colors.text))
                                    )
                                    Input(
                                        type = InputType.Number,
                                        attrs = {
                                            value(customHours.toString())
                                            onInput { 
                                                try {
                                                    customHours = (it.value?.toInt() ?: 24).coerceAtLeast(1)
                                                } catch (e: NumberFormatException) {
                                                    customHours = 24
                                                }
                                            }
                                            style {
                                                width(60.px)
                                                padding(4.px)
                                                border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                                borderRadius(4.px)
                                                backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                                color(Color(ThemeManager.Colors.text))
                                            }
                                        }
                                    )
                                    SpanText(
                                        "hours",
                                        Modifier
                                            .fontSize(14.px)
                                            .color(Color(ThemeManager.Colors.text))
                                    )
                                }
                            }
                        }

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .styleModifier {
                                        property("justify-content", "flex-end")
                                        property("gap", "8px")
                                    },
                        ) {
                            Button(
                                attrs = {
                                    onClick {
                                        showEventDialog = false
                                        newEventTitle = ""
                                        newEventDescription = ""
                                        isRecurring = false
                                        recurrenceFrequency = RecurrenceFrequency.DAILY
                                        customHours = 24
                                    }
                                    style {
                                        padding(8.px, 16.px)
                                        backgroundColor(Color(ThemeManager.Colors.buttonBackground))
                                        color(Color(ThemeManager.Colors.buttonText))
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                    }
                                },
                            ) {
                                SpanText("Cancel")
                            }
                            Button(
                                attrs = {
                                    onClick {
                                        if (newEventTitle.isNotBlank()) {
                                            val now = Date()
                                            val startTime = Date(now.getFullYear(), now.getMonth(), now.getDate(), 9) // 9 AM
                                            val endTime = Date(now.getFullYear(), now.getMonth(), now.getDate(), 10) // 10 AM

                                            val newEvent =
                                                CalendarEvent(
                                                    id = CalendarUtils.createEventId(),
                                                    title = newEventTitle,
                                                    description = newEventDescription,
                                                    startTime = startTime,
                                                    endTime = endTime,
                                                    mode = EventMode.PASSIVE, // Events are always passive
                                                    isRecurring = isRecurring,
                                                    recurrenceFrequency = if (isRecurring) recurrenceFrequency else null,
                                                )

                                            events.add(newEvent)

                                            showEventDialog = false
                                            newEventTitle = ""
                                            newEventDescription = ""
                                            isRecurring = false
                                            recurrenceFrequency = RecurrenceFrequency.DAILY
                                            customHours = 24
                                        }
                                    }
                                    style {
                                        padding(8.px, 16.px)
                                        backgroundColor(Color(ThemeManager.Colors.primaryButton))
                                        color(Colors.White)
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                    }
                                },
                            ) {
                                SpanText("Create Event")
                            }
                        }
                    }
                }
            }
        }

        // Task Creation Dialog
        if (showTaskDialog) {
            Box(
                modifier =
                    Modifier
                        .position(Position.Fixed)
                        .top(0.px)
                        .left(0.px)
                        .right(0.px)
                        .bottom(0.px)
                        .backgroundColor(
                            com.varabyte.kobweb.compose.ui.graphics.Color
                                .rgba(0f, 0f, 0f, 0.5f),
                        ).display(DisplayStyle.Flex)
                        .styleModifier {
                            property("justify-content", "center")
                            property("align-items", "center")
                            property("z-index", "1000")
                        },
            ) {
                Box(
                    modifier =
                        Modifier
                            .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                            .padding(24.px)
                            .borderRadius(8.px)
                            .maxWidth(400.px)
                            .width(100.percent),
                ) {
                    Column {
                        SpanText(
                            "Create New Task",
                            Modifier
                                .fontSize(18.px)
                                .fontWeight(700)
                                .margin(bottom = 16.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        SpanText(
                            "Title",
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(500)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        TextInput(
                            attrs = {
                                value(newTaskTitle)
                                onInput { newTaskTitle = it.value }
                                style {
                                    width(100.percent)
                                    marginBottom(12.px)
                                    padding(8.px)
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                    color(Color(ThemeManager.Colors.text))
                                }
                            },
                        )

                        SpanText(
                            "Description",
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(500)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        TextArea(
                            attrs = {
                                value(newTaskDescription)
                                onInput { newTaskDescription = it.value }
                                style {
                                    width(100.percent)
                                    minHeight(60.px)
                                    marginBottom(16.px)
                                    padding(8.px)
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                    color(Color(ThemeManager.Colors.text))
                                }
                            },
                        )

                        // Recurring Options for Tasks
                        SpanText(
                            "Recurring",
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(500)
                                .margin(bottom = 8.px)
                                .color(Color(ThemeManager.Colors.text)),
                        )

                        // Recurring checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .margin(bottom = 8.px)
                                .styleModifier {
                                    property("align-items", "center")
                                    property("gap", "8px")
                                }
                        ) {
                            Input(
                                type = InputType.Checkbox,
                                attrs = {
                                    checked(isRecurring)
                                    onChange { isRecurring = it.value }
                                    style {
                                        cursor(Cursor.Pointer)
                                    }
                                }
                            )
                            SpanText(
                                "Make this task recurring",
                                Modifier
                                    .fontSize(14.px)
                                    .color(Color(ThemeManager.Colors.text))
                            )
                        }

                        if (isRecurring) {
                            // Frequency selection
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .margin(bottom = 16.px)
                                    .styleModifier {
                                        property("gap", "8px")
                                    }
                            ) {
                                listOf(
                                    RecurrenceFrequency.DAILY to "Daily",
                                    RecurrenceFrequency.WEEKLY to "Weekly",
                                    RecurrenceFrequency.CUSTOM to "Custom"
                                ).forEach { (freq, label) ->
                                    Button(
                                        attrs = {
                                            onClick { recurrenceFrequency = freq }
                                            style {
                                                padding(6.px, 12.px)
                                                backgroundColor(
                                                    if (recurrenceFrequency == freq) {
                                                        Color("#dc2626") // Red for tasks
                                                    } else {
                                                        Color(ThemeManager.Colors.buttonBackground)
                                                    }
                                                )
                                                color(
                                                    if (recurrenceFrequency == freq) {
                                                        Colors.White
                                                    } else {
                                                        Color(ThemeManager.Colors.buttonText)
                                                    }
                                                )
                                                border(0.px)
                                                borderRadius(4.px)
                                                cursor(Cursor.Pointer)
                                                fontSize(12.px)
                                            }
                                        },
                                    ) {
                                        SpanText(label)
                                    }
                                }
                            }

                            if (recurrenceFrequency == RecurrenceFrequency.CUSTOM) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .margin(bottom = 16.px)
                                        .styleModifier {
                                            property("align-items", "center")
                                            property("gap", "8px")
                                        }
                                ) {
                                    SpanText(
                                        "Every",
                                        Modifier
                                            .fontSize(14.px)
                                            .color(Color(ThemeManager.Colors.text))
                                    )
                                    Input(
                                        type = InputType.Number,
                                        attrs = {
                                            value(customHours.toString())
                                            onInput { 
                                                try {
                                                    customHours = (it.value?.toInt() ?: 24).coerceAtLeast(1)
                                                } catch (e: NumberFormatException) {
                                                    customHours = 24
                                                }
                                            }
                                            style {
                                                width(60.px)
                                                padding(4.px)
                                                border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                                borderRadius(4.px)
                                                backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                                color(Color(ThemeManager.Colors.text))
                                            }
                                        }
                                    )
                                    SpanText(
                                        "hours",
                                        Modifier
                                            .fontSize(14.px)
                                            .color(Color(ThemeManager.Colors.text))
                                    )
                                }
                            }
                        }

                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .styleModifier {
                                        property("justify-content", "flex-end")
                                        property("gap", "8px")
                                    },
                        ) {
                            Button(
                                attrs = {
                                    onClick {
                                        showTaskDialog = false
                                        newTaskTitle = ""
                                        newTaskDescription = ""
                                        isRecurring = false
                                        recurrenceFrequency = RecurrenceFrequency.DAILY
                                        customHours = 24
                                    }
                                    style {
                                        padding(8.px, 16.px)
                                        backgroundColor(Color(ThemeManager.Colors.buttonBackground))
                                        color(Color(ThemeManager.Colors.buttonText))
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                    }
                                },
                            ) {
                                SpanText("Cancel")
                            }
                            Button(
                                attrs = {
                                    onClick {
                                        if (newTaskTitle.isNotBlank()) {
                                            val now = Date()
                                            val startTime = Date(now.getFullYear(), now.getMonth(), now.getDate(), 9) // 9 AM
                                            val endTime = Date(now.getFullYear(), now.getMonth(), now.getDate(), 10) // 10 AM

                                            val newTask =
                                                CalendarEvent(
                                                    id = CalendarUtils.createEventId(),
                                                    title = newTaskTitle,
                                                    description = newTaskDescription,
                                                    startTime = startTime,
                                                    endTime = endTime,
                                                    mode = EventMode.ACTIVE, // Tasks are always active
                                                    isRecurring = isRecurring,
                                                    recurrenceFrequency = if (isRecurring) recurrenceFrequency else null,
                                                )

                                            events.add(newTask)

                                            showTaskDialog = false
                                            newTaskTitle = ""
                                            newTaskDescription = ""
                                            isRecurring = false
                                            recurrenceFrequency = RecurrenceFrequency.DAILY
                                            customHours = 24
                                        }
                                    }
                                    style {
                                        padding(8.px, 16.px)
                                        backgroundColor(Color("#dc2626")) // Red for tasks
                                        color(Colors.White)
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                    }
                                },
                            ) {
                                SpanText("Create Task")
                            }
                        }
                    }
                }
            }
        }
        WeeklyTaskManager(
            tasks = tasks,
            onTaskAdd = { task ->
                tasks.add(task)
            },
            onTaskUpdate = { updatedTask ->
                val index = tasks.indexOfFirst { it.id == updatedTask.id }
                if (index >= 0) {
                    tasks[index] = updatedTask
                }
            },
            onTaskDelete = { deletedTask ->
                val index = tasks.indexOfFirst { it.id == deletedTask.id }
                if (index >= 0) {
                    tasks.removeAt(index)
                }
            },
            onAutoSort = { autoSortedEvents ->
                // Add the auto-sorted events to the calendar
                events.addAll(autoSortedEvents)
            }
        )
    }
}
