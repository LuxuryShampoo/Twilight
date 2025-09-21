package xyz.malefic.staticsite.components.calendar

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.silk.components.text.SpanText
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.toModifier
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event
import org.w3c.dom.events.MouseEvent
import xyz.malefic.staticsite.util.*
import kotlin.js.Date
import kotlin.math.max
import kotlin.math.min
import com.varabyte.kobweb.compose.ui.graphics.Color as Kolor

// Shared drag state so other events can disable pointer-events while dragging
object DragState {
    var draggingId by mutableStateOf<String?>(null)
}

// Calendar Grid Styles
val CalendarGridStyle =
    CssStyle.base {
        Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Grid)
            .styleModifier {
                property("grid-template-columns", "auto repeat(7, 1fr)")
                property("grid-template-rows", "auto repeat(24, 60px)")
                property("gap", "1px")
            }.border(1.px, LineStyle.Solid, Color("#e6e6e6"))
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
            .boxShadow(0.px, 2.px, 10.px, color = Kolor.rgba(0f, 0f, 0f, 0.1f))
    }

// Calendar Header Styles
val CalendarHeaderStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#f9fafb"))
            .padding(12.px)
            .borderBottom(1.px, LineStyle.Solid, Color("#e6e6e6"))
            .fontSize(14.px)
            .fontWeight(500)
            .textAlign(TextAlign.Center)
    }

// Time Column Styles
val TimeColumnStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#f9fafb"))
            .padding(8.px)
            .borderRight(1.px, LineStyle.Solid, Color("#e6e6e6"))
            .fontSize(12.px)
            .color(Color("#646464"))
            .textAlign(TextAlign.Right)
    }

// Calendar Cell Styles
val CalendarCellStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Colors.White)
            .border(1.px, LineStyle.Solid, Color("#f0f0f0"))
            .minHeight(60.px)
            .position(Position.Relative)
            .overflow(Overflow.Visible) // Allow events to overflow when needed
    }

// Add new style for drop targets
val CalendarCellDropTargetStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#f0f9ff"))
            .border(1.px, LineStyle.Solid, Color("#3b82f6"))
            .minHeight(60.px)
            .position(Position.Relative)
            .overflow(Overflow.Visible)
    }

// Calendar Event Styles
val CalendarEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#3b82f6"))
            .color(Colors.White)
            .borderRadius(4.px)
            .padding(4.px, 8.px)
            .fontSize(12.px)
            .fontWeight(500)
            .margin(2.px)
            .overflow(Overflow.Hidden)
            .styleModifier {
                property("text-overflow", "ellipsis")
                property("white-space", "nowrap")
                property("user-select", "none") // Prevent text selection during drag
                property("cursor", "grab") // Show grab cursor
                property("transition", "box-shadow 0.2s ease")
            }.transition(Transition.of("background-color", 0.2.s))
    }

// Active Event Style
val ActiveEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#dc2626"))
    }

// Passive Event Style
val PassiveEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#3b82f6"))
    }

// Custom Event Style
val CustomEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#8b5cf6"))
    }

// Holiday Event Style
val HolidayEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#10b981"))
    }

// Calendar Navigation Styles
val CalendarNavStyle =
    CssStyle.base {
        Modifier
            .display(DisplayStyle.Flex)
            .styleModifier {
                property("justify-content", "space-between")
                property("align-items", "center")
            }.padding(16.px)
            .styleModifier {
                property("margin-bottom", "16px")
            }
    }

val NavButtonStyle =
    Modifier
        .padding(8.px, 12.px)
        .backgroundColor(Color("#f9fafb"))
        .border(1.px, LineStyle.Solid, Color("#e6e6e6"))
        .borderRadius(4.px)
        .cursor(Cursor.Pointer)
        .fontSize(14.px)
        .transition(Transition.of("background-color", 0.2.s))

val NavButtonHoverStyle =
    CssStyle {
        base {
            NavButtonStyle
        }

        hover {
            Modifier.backgroundColor(Color("#f0f0f0"))
        }
    }

val TitleStyle =
    CssStyle.base {
        Modifier
            .fontSize(20.px)
            .fontWeight(700)
    }

/**
 * Calendar header component that displays the days of the week.
 */
@Composable
fun CalendarHeader() {
    Row(CalendarHeaderStyle.toModifier()) {
        // Empty cell for the time column
        Box(Modifier.width(60.px)) {}

        // Day headers
        CalendarUtils.getShortDayNames().forEach { day ->
            Box(Modifier.fillMaxWidth()) {
                SpanText(day)
            }
        }
    }
}

/**
 * Time column component that displays the hours of the day.
 */
@Composable
fun TimeColumn() {
    Column(TimeColumnStyle.toModifier()) {
        // Empty cell for the header row
        Box(Modifier.height(40.px)) {}

        // Hour labels
        for (hour in 0..23) {
            Box(Modifier.height(60.px).padding(top = 4.px)) {
                val formattedHour =
                    if (hour ==
                        0
                    ) {
                        "12 AM"
                    } else if (hour < 12) {
                        "$hour AM"
                    } else if (hour == 12) {
                        "12 PM"
                    } else {
                        "${hour - 12} PM"
                    }
                SpanText(formattedHour)
            }
        }
    }
}

/**
 * Calendar cell component that displays events for a specific day and hour.
 */
@Composable
fun CalendarCell(
    date: Date,
    hour: Int,
    events: List<CalendarEvent>,
    allEvents: List<CalendarEvent> = events,
    onEventClick: (CalendarEvent) -> Unit,
    onEventUpdate: (CalendarEvent) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    // Drop highlight state
    var isDropTarget by remember { mutableStateOf(false) }

    // For debugging - assign a unique ID to help track this cell
    val cellId = "cell-${date.getFullYear()}-${date.getMonth()}-${date.getDate()}-$hour"

    // Use different style based on drop target state
    val cellStyle = if (isDropTarget) CalendarCellDropTargetStyle else CalendarCellStyle

    Box(
        cellStyle
            .toModifier()
            .attrsModifier {
                // Add data attributes for drag and drop
                id(cellId)
                attr("data-date", "${date.getFullYear()}-${date.getMonth()}-${date.getDate()}")
                attr("data-hour", "$hour")
                attr("data-cell", "true")

                // Simple drag event handlers with improved handling
                onDragOver { e ->
                    e.preventDefault()
                    e.dataTransfer?.dropEffect = "move"
                    isDropTarget = true
                }

                onDragEnter { e ->
                    e.preventDefault()
                    isDropTarget = true
                }

                onDragLeave { e ->
                    e.preventDefault()
                    isDropTarget = false
                }

                onDrop { e ->
                    e.preventDefault()
                    e.stopPropagation()
                    isDropTarget = false

                    try {
                        // Determine the element directly under the pointer to snap to the closest cell
                        val elemAtPoint = document.elementFromPoint(e.clientX.toDouble(), e.clientY.toDouble()) as? HTMLElement

                        // Traverse up to find an ancestor with data-cell attribute
                        var targetCell: HTMLElement? = null
                        var cur = elemAtPoint
                        while (cur != null) {
                            if (cur.getAttribute("data-cell") == "true") {
                                targetCell = cur
                                break
                            }
                            cur = cur.parentElement as? HTMLElement
                        }

                        // Fallback to the currentTarget if no cell found
                        val cellElement = targetCell ?: (e.currentTarget as HTMLElement)

                        val eventId = e.dataTransfer?.getData("text/plain")
                        if (!eventId.isNullOrEmpty()) {
                            var targetEvent = events.find { it.id == eventId }
                            if (targetEvent == null) targetEvent = allEvents.find { it.id == eventId }

                            if (targetEvent != null) {
                                // Read cell attributes for date and hour
                                val cellDateAttr = cellElement.getAttribute("data-date")
                                val cellHourAttr = cellElement.getAttribute("data-hour")

                                // Parse cell date
                                val cellDateObj: Date =
                                    if (!cellDateAttr.isNullOrEmpty()) {
                                        val parts = cellDateAttr.split("-").mapNotNull { it.toIntOrNull() }
                                        if (parts.size >= 3) Date(parts[0], parts[1], parts[2]) else Date()
                                    } else {
                                        Date(date.getFullYear(), date.getMonth(), date.getDate())
                                    }

                                val cellHourInt = cellHourAttr?.toIntOrNull() ?: hour

                                // Compute relative Y inside the chosen cell element
                                val rect = cellElement.getBoundingClientRect()
                                val relY = e.clientY - rect.top
                                val cellHeight = rect.height
                                val minutesInHour = 60

                                // Minute offset within hour
                                val minuteOffset = ((relY / cellHeight) * minutesInHour).toInt().coerceIn(0, 59)

                                // Create new start and end
                                val newStartDate =
                                    Date(cellDateObj.getFullYear(), cellDateObj.getMonth(), cellDateObj.getDate(), cellHourInt)
                                newStartDate.asDynamic().setMinutes(minuteOffset)
                                val duration = targetEvent.endTime.getTime() - targetEvent.startTime.getTime()
                                val newEndDate = Date(newStartDate.getTime() + duration)

                                val updatedEvent = targetEvent.copy(startTime = newStartDate, endTime = newEndDate)
                                onEventUpdate(updatedEvent)
                            }
                        }
                    } catch (ex: Exception) {
                        console.error("Error in drop handler: ${ex.message}")
                    }
                }
            },
    ) {
        // Filter events for this cell
        val cellEvents =
            events.filter { event ->
                val eventStart = event.startTime
                val eventEnd = event.endTime
                val cellStart = Date(date.getFullYear(), date.getMonth(), date.getDate(), hour)
                val cellEnd = Date(date.getFullYear(), date.getMonth(), date.getDate(), hour + 1)

                // Event overlaps with this cell
                eventStart.getTime() < cellEnd.getTime() && eventEnd.getTime() > cellStart.getTime()
            }

        // Display events
        Column {
            cellEvents.forEach { event ->
                key(event.id) {
                    CalendarEventItem(
                        event = event,
                        onEventClick = {
                            editingEvent = it
                        },
                        onEventUpdate = onEventUpdate,
                        cellDate = date,
                        cellHour = hour,
                    )
                }
            }
        }

        // Show edit dialog
        if (editingEvent != null) {
            EditEventDialog(
                event = editingEvent!!,
                onClose = { editingEvent = null },
                onSave = { updatedEvent ->
                    onEventUpdate(updatedEvent)
                    editingEvent = null
                },
            )
        }
    }
}

@Composable
fun CalendarEventItem(
    event: CalendarEvent,
    onEventClick: (CalendarEvent) -> Unit,
    onEventUpdate: (CalendarEvent) -> Unit,
    cellDate: Date,
    cellHour: Int,
) {
    val coroutineScope = rememberCoroutineScope()

    // State for tracking drag and resize operations
    var isDragging by remember { mutableStateOf(false) }
    var isResizing by remember { mutableStateOf(false) }

    // Calculate the position and height of the event in this cell
    val cellStart = Date(cellDate.getFullYear(), cellDate.getMonth(), cellDate.getDate(), cellHour)
    val cellEnd = Date(cellDate.getFullYear(), cellDate.getMonth(), cellDate.getDate(), cellHour + 1)
    val eventStart = event.startTime
    val eventEnd = event.endTime

    // Calculate vertical position in the cell
    val hourMillis = 60 * 60 * 1000.0
    val offsetTop =
        if (eventStart.getTime() > cellStart.getTime()) {
            val millisFromCellStart = eventStart.getTime() - cellStart.getTime()
            (millisFromCellStart / hourMillis) * 60.0
        } else {
            0.0
        }

    // Calculate event height
    val overlapStart = max(eventStart.getTime(), cellStart.getTime())
    val overlapEnd = min(eventEnd.getTime(), cellEnd.getTime())
    val durationInCell = overlapEnd - overlapStart
    val heightPercent = durationInCell / hourMillis
    val eventHeight = max(heightPercent * 60.0, 15.0) // Minimum height of 15px

    // Style based on event type
    val baseStyle =
        when {
            event.isHoliday -> HolidayEventStyle
            event.mode == EventMode.ACTIVE -> ActiveEventStyle
            event.mode == EventMode.PASSIVE -> PassiveEventStyle
            else -> CustomEventStyle
        }

    // For debugging - unique ID for this event instance
    val eventInstanceId = "event-${event.id}-${cellDate.getDate()}-$cellHour"

    Box(
        baseStyle
            .toModifier()
            .styleModifier {
                property("position", "absolute")
                property("top", "${offsetTop}px")
                property("height", "${eventHeight}px")
                property("left", "4px")
                property("width", "calc(100% - 8px)")
                property("z-index", if (isDragging || isResizing) "100" else "1")

                if (isDragging) {
                    property("opacity", "0.7")
                    property("cursor", "grabbing")
                    property("box-shadow", "0 4px 8px rgba(0,0,0,0.3)")
                }

                if (isResizing) {
                    property("border", "2px dashed #888")
                }

                // If some other event is being dragged, allow drops through this element
                if (DragState.draggingId != null && DragState.draggingId != event.id) {
                    property("pointer-events", "none")
                }
            }.onClick { e ->
                e.stopPropagation()
                if (!isDragging && !isResizing) {
                    onEventClick(event)
                }
            }.attrsModifier {
                // Add ids and data for easier debugging
                id(eventInstanceId)
                attr("data-event-id", event.id)
                attr("data-event-title", event.title)
                attr("draggable", "true")

                // Use improved drag handlers
                onDragStart { e ->
                    // Set this first - order matters for some browsers
                    e.dataTransfer?.setData("text/plain", event.id)
                    e.dataTransfer?.effectAllowed = "move"

                    // set global drag id so other event items can ignore pointer events
                    DragState.draggingId = event.id

                    // Create custom drag image with clearer visibility and stable id
                    try {
                        val dragImage = document.createElement("div") as HTMLElement
                        val dragId = "drag-image-${event.id}"
                        dragImage.id = dragId
                        dragImage.style.position = "absolute"
                        dragImage.style.top = "0"
                        dragImage.style.left = "0"
                        dragImage.style.backgroundColor = "#3b82f6"
                        dragImage.style.color = "white"
                        dragImage.style.padding = "8px 12px"
                        dragImage.style.borderRadius = "4px"
                        dragImage.style.fontSize = "14px"
                        dragImage.style.fontWeight = "bold"
                        dragImage.style.asDynamic().pointerEvents = "none"
                        dragImage.style.opacity = "0.8"
                        dragImage.style.boxShadow = "0 2px 10px rgba(0,0,0,0.2)"
                        dragImage.style.zIndex = "1000"
                        dragImage.textContent = event.title

                        // Position offscreen
                        dragImage.style.position = "absolute"
                        dragImage.style.top = "-1000px"
                        document.body?.appendChild(dragImage)

                        // Use the custom drag image
                        e.dataTransfer?.setDragImage(dragImage, 20, 20)
                    } catch (ex: Exception) {
                        console.error("Error setting drag image: ${ex.message}")
                    }

                    isDragging = true
                }

                onDrag { e ->
                    // Additional tracking during drag
                }

                onDragEnd { e ->
                    // Remove drag image if still present
                    try {
                        val dragId = "drag-image-${event.id}"
                        val img = document.getElementById(dragId)
                        if (img != null) document.body?.removeChild(img)
                    } catch (ex: Exception) {
                        console.error("Error removing drag image: ${ex.message}")
                    }
                    isDragging = false
                    // clear global drag id
                    DragState.draggingId = null
                }

                // Custom resize handler on mousedown
                onMouseDown { e ->
                    val element = e.currentTarget as HTMLElement
                    val rect = element.getBoundingClientRect()
                    val isNearBottom = e.clientY > (rect.top + rect.height - 10)

                    if (isNearBottom) {
                        e.preventDefault()
                        e.stopPropagation()
                        isResizing = true

                        // Initial y position and original end time
                        val initialY = e.clientY
                        val originalEndTime = event.endTime.getTime()

                        // Fix: Store handler references in variables first
                        var mouseMoveHandler: ((Event) -> Unit)? = null
                        var mouseUpHandler: ((Event) -> Unit)? = null

                        mouseMoveHandler = { moveEvent: Event ->
                            if (moveEvent is MouseEvent) {
                                val deltaY = moveEvent.clientY - initialY

                                // Calculate time change (15-minute increments)
                                val timeChange = (deltaY / 15.0).toInt() * 15 * 60 * 1000
                                val newEndTime = Date(originalEndTime + timeChange)

                                // Ensure end time is after start time
                                if (newEndTime.getTime() > event.startTime.getTime()) {
                                    onEventUpdate(event.copy(endTime = newEndTime))
                                }
                            }
                        }

                        mouseUpHandler = { _: Event ->
                            isResizing = false
                            mouseMoveHandler?.let { document.removeEventListener("mousemove", it) }
                            mouseUpHandler?.let { document.removeEventListener("mouseup", it) }
                        }

                        // Add the event listeners
                        mouseMoveHandler?.let { document.addEventListener("mousemove", it) }
                        mouseUpHandler?.let { document.addEventListener("mouseup", it) }
                    }
                }
            },
    ) {
        // Event content - render a visible card with title and time
        Column(
            Modifier
                .fillMaxWidth()
                .padding(4.px)
                .styleModifier {
                    property("display", "flex")
                    property("flex-direction", "column")
                    property("gap", "2px")
                },
        ) {
            SpanText(
                event.title,
                Modifier
                    .fontSize(12.px)
                    .fontWeight(FontWeight.SemiBold),
            )

            // show time range
            val sh =
                event.startTime
                    .getHours()
                    .toString()
                    .padStart(2, '0')
            val sm =
                event.startTime
                    .getMinutes()
                    .toString()
                    .padStart(2, '0')
            val eh =
                event.endTime
                    .getHours()
                    .toString()
                    .padStart(2, '0')
            val em =
                event.endTime
                    .getMinutes()
                    .toString()
                    .padStart(2, '0')
            SpanText("$sh:$sm - $eh:$em", Modifier.fontSize(10.px).color(Color("#e6eefc")))

            if (eventHeight > 30 && !event.description.isNullOrBlank()) {
                SpanText(
                    event.description ?: "",
                    Modifier.fontSize(10.px).color(Color("#e0e0e0")),
                )
            }
        }

        // Resize handle
        Box(
            Modifier
                .styleModifier {
                    property("position", "absolute")
                    property("bottom", "0")
                    property("left", "0")
                    property("right", "0")
                    property("height", "6px")
                    property("cursor", "ns-resize")
                    property("background", "rgba(255,255,255,0.2)")
                }.classNames("resize-handle"),
        )
    }
}

// Editable dialog for event title and description
@Composable
fun EditEventDialog(
    event: CalendarEvent,
    onClose: () -> Unit,
    onSave: (CalendarEvent) -> Unit,
) {
    var title by remember { mutableStateOf(event.title) }
    var description by remember { mutableStateOf(event.description ?: "") }

    // Format time for the time input fields
    var startHours by remember { mutableStateOf(event.startTime.getHours()) }
    var startMinutes by remember { mutableStateOf(event.startTime.getMinutes()) }
    var endHours by remember { mutableStateOf(event.endTime.getHours()) }
    var endMinutes by remember { mutableStateOf(event.endTime.getMinutes()) }

    val startTimeFormatted = "${startHours.toString().padStart(2, '0')}:${startMinutes.toString().padStart(2, '0')}"
    val endTimeFormatted = "${endHours.toString().padStart(2, '0')}:${endMinutes.toString().padStart(2, '0')}"

    Box(
        modifier =
            Modifier
                .position(Position.Fixed)
                .top(0.px)
                .left(0.px)
                .right(0.px)
                .bottom(0.px)
                .backgroundColor(Kolor.rgba(0f, 0f, 0f, 0.5f))
                .display(DisplayStyle.Flex)
                .styleModifier {
                    property("justify-content", "center")
                    property("align-items", "center")
                    property("z-index", "1000")
                },
    ) {
        Box(
            modifier =
                Modifier
                    .backgroundColor(Colors.White)
                    .padding(24.px)
                    .borderRadius(8.px)
                    .maxWidth(400.px)
                    .width(100.percent)
                    .onClick { e ->
                        // Prevent closing when clicking inside the dialog
                        e.stopPropagation()
                    },
        ) {
            Column {
                // Title
                SpanText(
                    "Edit Event",
                    Modifier
                        .fontSize(18.px)
                        .fontWeight(700)
                        .margin(bottom = 16.px),
                )

                // Title input
                SpanText(
                    "Title",
                    Modifier
                        .fontSize(14.px)
                        .fontWeight(500)
                        .margin(bottom = 4.px),
                )
                TextInput(
                    attrs = {
                        value(title)
                        onInput { ev -> title = ev.value }
                        style {
                            width(100.percent)
                            marginBottom(12.px)
                            padding(8.px)
                            border(1.px, LineStyle.Solid, Color("#d1d5db"))
                            borderRadius(4.px)
                        }
                    },
                )

                // Time inputs (side by side)
                Row(
                    Modifier.fillMaxWidth().gap(12.px).margin(bottom = 12.px),
                ) {
                    // Start time
                    Column(Modifier.weight(1)) {
                        SpanText(
                            "Start Time",
                            Modifier.fontSize(14.px).fontWeight(500).margin(bottom = 4.px),
                        )
                        TextInput(
                            attrs = {
                                attr("type", "time")
                                value(startTimeFormatted)
                                onInput {
                                    try {
                                        val parts = it.value.split(":")
                                        if (parts.size == 2) {
                                            startHours = parts[0].toInt()
                                            startMinutes = parts[1].toInt()
                                        }
                                    } catch (e: Exception) {
                                        console.error("Invalid time format: ${it.value}")
                                    }
                                }
                                style {
                                    width(100.percent)
                                    padding(8.px)
                                    border(1.px, LineStyle.Solid, Color("#d1d5db"))
                                    borderRadius(4.px)
                                }
                            },
                        )
                    }

                    // End time
                    Column(Modifier.weight(1)) {
                        SpanText(
                            "End Time",
                            Modifier.fontSize(14.px).fontWeight(500).margin(bottom = 4.px),
                        )
                        TextInput(
                            attrs = {
                                attr("type", "time")
                                value(endTimeFormatted)
                                onInput {
                                    try {
                                        val parts = it.value.split(":")
                                        if (parts.size == 2) {
                                            endHours = parts[0].toInt()
                                            endMinutes = parts[1].toInt()
                                        }
                                    } catch (e: Exception) {
                                        console.error("Invalid time format: ${it.value}")
                                    }
                                }
                                style {
                                    width(100.percent)
                                    padding(8.px)
                                    border(1.px, LineStyle.Solid, Color("#d1d5db"))
                                    borderRadius(4.px)
                                }
                            },
                        )
                    }
                }

                // Description input
                SpanText(
                    "Description",
                    Modifier
                        .fontSize(14.px)
                        .fontWeight(500)
                        .margin(bottom = 4.px),
                )
                TextArea(
                    attrs = {
                        value(description)
                        onInput { ev -> description = ev.value }
                        style {
                            width(100.percent)
                            minHeight(60.px)
                            marginBottom(16.px)
                            padding(8.px)
                            border(1.px, LineStyle.Solid, Color("#d1d5db"))
                            borderRadius(4.px)
                        }
                    },
                )

                // Buttons
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
                            onClick { onClose() }
                            style {
                                padding(8.px, 16.px)
                                backgroundColor(Kolor.rgba(229f / 255f, 231f / 255f, 235f / 255f, 1f))
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
                                try {
                                    // Create new date objects with updated times using direct JS interop
                                    val newStart = Date(event.startTime.getTime())
                                    // Use dynamic to access JS Date methods safely
                                    newStart.asDynamic().setHours(startHours)
                                    newStart.asDynamic().setMinutes(startMinutes)

                                    val newEnd = Date(event.endTime.getTime())
                                    newEnd.asDynamic().setHours(endHours)
                                    newEnd.asDynamic().setMinutes(endMinutes)

                                    // Ensure end time is after start time
                                    if (newEnd.getTime() <= newStart.getTime()) {
                                        newEnd.asDynamic().setTime(newStart.getTime() + (60 * 60 * 1000)) // Add 1 hour
                                    }

                                    // Save updated event
                                    onSave(
                                        event.copy(
                                            title = title,
                                            description = description,
                                            startTime = newStart,
                                            endTime = newEnd,
                                        ),
                                    )
                                } catch (e: Exception) {
                                    console.error("Error updating event: ${e.message}")
                                }
                            }
                            style {
                                padding(8.px, 16.px)
                                backgroundColor(Kolor.rgba(59f / 255f, 130f / 255f, 246f / 255f, 1f))
                                color(Colors.White)
                                border(0.px)
                                borderRadius(4.px)
                                cursor(Cursor.Pointer)
                            }
                        },
                    ) {
                        SpanText("Save")
                    }
                }
            }
        }
    }
}

/**
 * Calendar navigation component.
 */
@Composable
fun CalendarNavigation(
    currentDate: Date,
    onPrevWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onToday: () -> Unit,
) {
    Row(CalendarNavStyle.toModifier()) {
        // Title with month and year
        Box(TitleStyle.toModifier()) {
            val month = CalendarUtils.getMonthNames()[currentDate.getMonth()]
            val year = currentDate.getFullYear()
            SpanText("$month $year")
        }

        // Navigation buttons
        Row(Modifier.gap(8.px)) {
            Button(
                attrs = {
                    onClick { onPrevWeek() }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(Color("#f9fafb"))
                        border(1.px, LineStyle.Solid, Color("#e6e6e6"))
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                        fontSize(14.px)
                        transition(Transition.of("background-color", 0.2.s))
                    }
                },
            ) {
                SpanText("Previous Week")
            }

            Button(
                attrs = {
                    onClick { onToday() }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(Color("#f9fafb"))
                        border(1.px, LineStyle.Solid, Color("#e6e6e6"))
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                        fontSize(14.px)
                        transition(Transition.of("background-color", 0.2.s))
                    }
                },
            ) {
                SpanText("Today")
            }

            Button(
                attrs = {
                    onClick { onNextWeek() }
                    style {
                        padding(8.px, 12.px)
                        backgroundColor(Color("#f9fafb"))
                        border(1.px, LineStyle.Solid, Color("#e6e6e6"))
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                        fontSize(14.px)
                        transition(Transition.of("background-color", 0.2.s))
                    }
                },
            ) {
                SpanText("Next Week")
            }
        }
    }
}

/**
 * Unexpected event dialog component.
 */
@Composable
fun UnexpectedEventDialog(
    isOpen: Boolean,
    onClose: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (isOpen) {
        Div(
            attrs = {
                style {
                    position(Position.Fixed)
                    top(0.px)
                    left(0.px)
                    right(0.px)
                    bottom(0.px)
                    backgroundColor(Kolor.rgba(0f, 0f, 0f, 0.5f))
                    display(DisplayStyle.Flex)
                    property("justify-content", "center")
                    property("align-items", "center")
                    zIndex(1000)
                }
            },
        ) {
            Div(
                attrs = {
                    style {
                        backgroundColor(Colors.White)
                        padding(24.px)
                        borderRadius(8.px)
                        maxWidth(400.px)
                        width(100.percent)
                    }
                },
            ) {
                SpanText(
                    "Unexpected Event",
                    Modifier
                        .fontSize(18.px)
                        .fontWeight(700)
                        .margin(bottom = 16.px),
                )
                SpanText(
                    "You are about to mark this event as missed due to an unexpected circumstance. Do you want to continue?",
                    Modifier
                        .fontSize(14.px)
                        .margin(bottom = 16.px),
                )
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            property("justify-content", "flex-end")
                            gap(8.px)
                            marginTop(16.px)
                        }
                    },
                ) {
                    Button(
                        attrs = {
                            onClick { onClose() }
                            style {
                                padding(8.px, 16.px)
                                backgroundColor(Kolor.rgba(229f / 255f, 231f / 255f, 235f / 255f, 1f))
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
                            onClick { onConfirm() }
                            style {
                                padding(8.px, 16.px)
                                backgroundColor(Kolor.rgba(220f / 255f, 38f / 255f, 38f / 255f, 1f))
                                color(Colors.White)
                                border(0.px)
                                borderRadius(4.px)
                                cursor(Cursor.Pointer)
                            }
                        },
                    ) {
                        SpanText("Confirm")
                    }
                }
            }
        }
    }
}
