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
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.css.DisplayStyle
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*
import org.w3c.dom.HTMLElement
import xyz.malefic.staticsite.util.CalendarEvent
import xyz.malefic.staticsite.util.CalendarUtils
import xyz.malefic.staticsite.util.ThemeManager
import kotlin.js.Date
import com.varabyte.kobweb.compose.ui.graphics.Color as Kolor

// Shared drag state so other events can disable pointer-events while dragging
object GlobalDragState {
    var draggingId by mutableStateOf<String?>(null)
}

// Selection state management - Enhanced with click-outside support
object GlobalSelectionState {
    val selectedEventIds = mutableStateListOf<String>()
    
    fun isSelected(eventId: String): Boolean = selectedEventIds.contains(eventId)
    
    fun toggleSelection(eventId: String, isShiftClick: Boolean = false) {
        if (isShiftClick) {
            // For shift-click, add to existing selection
            if (isSelected(eventId)) {
                selectedEventIds.remove(eventId)
            } else {
                selectedEventIds.add(eventId)
            }
        } else {
            // For regular click, clear selection and select only this event
            selectedEventIds.clear()
            selectedEventIds.add(eventId)
        }
    }
    
    fun clearSelection() {
        selectedEventIds.clear()
    }
    
    fun selectAll(eventIds: List<String>) {
        selectedEventIds.clear()
        selectedEventIds.addAll(eventIds)
    }
    
    // Add method to check if we have multiple selections for drag-all functionality
    fun hasMultipleSelections(): Boolean = selectedEventIds.size > 1
    
    // Get all selected event IDs for operations
    fun getAllSelected(): List<String> = selectedEventIds.toList()
}

// Calendar Grid Styles
val CalendarGridStyle =
    CssStyle.base {
        Modifier
            .fillMaxWidth()
            .display(DisplayStyle.Grid)
            .styleModifier {
                property("grid-template-columns", "auto repeat(7, 1fr)")
                property("grid-template-rows", "auto repeat(48, 40px)") // Changed to 48 30-minute intervals, 40px each
                property("gap", "1px")
            }.border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
            .boxShadow(0.px, 2.px, 10.px, color = Kolor.rgba(0f, 0f, 0f, 0.1f))
    }

// Calendar Header Styles
val CalendarHeaderStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color(ThemeManager.Colors.headerBackground))
            .padding(12.px)
            .borderBottom(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .fontSize(14.px)
            .fontWeight(500)
            .textAlign(TextAlign.Center)
            .color(Color(ThemeManager.Colors.text))
    }

// Time Column Styles
val TimeColumnStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color(ThemeManager.Colors.headerBackground))
            .padding(8.px)
            .borderRight(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .fontSize(12.px)
            .color(Color(ThemeManager.Colors.secondaryText))
            .textAlign(TextAlign.Right)
    }

// Calendar Cell Styles
val CalendarCellStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
            .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .minHeight(40.px) // Updated to match new 30-minute interval height
            .position(Position.Relative)
            .overflow(Overflow.Visible) // Allow events to overflow when needed
    }

// Add new style for drop targets
val CalendarCellDropTargetStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#f0f9ff"))
            .border(1.px, LineStyle.Solid, Color("#3b82f6"))
            .minHeight(40.px) // Updated to match new 30-minute interval height
            .position(Position.Relative)
            .overflow(Overflow.Visible)
    }

// Calendar Event Styles - Improved for better aesthetics
val CalendarEventStyle =
    CssStyle {
        base {
            Modifier
                .backgroundColor(Color("#10b981")) // Light green for all events
                .color(Colors.White)
                .borderRadius(8.px) // Rounded corners for modern look
                .padding(6.px, 10.px) // Increased padding for better spacing
                .fontSize(12.px)
                .fontWeight(500)
                .margin(2.px) // Small margin to prevent stacking overlap
                .overflow(Overflow.Hidden)
                .styleModifier {
                    property("text-overflow", "ellipsis")
                    property("white-space", "nowrap")
                    property("user-select", "none")
                    property("cursor", "grab")
                    property("transition", "all 0.2s ease")
                    property("border-left", "3px solid rgba(255,255,255,0.3)")
                    property("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                    property("backdrop-filter", "blur(10px)")
                    property("min-height", "28px") // Ensure consistent height
                    property("display", "flex")
                    property("align-items", "center")
                }
        }
        
        hover {
            Modifier.styleModifier {
                property("transform", "translateY(-1px)")
                property("box-shadow", "0 4px 8px rgba(0,0,0,0.15)")
            }
        }
    }

// Active Event Style - More vibrant red with better contrast
val ActiveEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#ef4444"))
            .styleModifier {
                property("border-left", "3px solid rgba(255,255,255,0.4)")
                property("background", "linear-gradient(135deg, #ef4444 0%, #dc2626 100%)")
            }
    }

// Passive Event Style - Calming blue gradient
val PassiveEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#10b981")) // Light green for events
            .styleModifier {
                property("border-left", "3px solid rgba(255,255,255,0.4)")
                property("background", "linear-gradient(135deg, #10b981 0%, #059669 100%)") // Light green gradient
            }
    }

// Custom Event Style - Purple gradient for distinction
val CustomEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#8b5cf6"))
            .styleModifier {
                property("border-left", "3px solid rgba(255,255,255,0.4)")
                property("background", "linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%)")
            }
    }

// Holiday Event Style - Green gradient for special occasions
val HolidayEventStyle =
    CssStyle.base {
        Modifier
            .backgroundColor(Color("#10b981"))
            .styleModifier {
                property("border-left", "3px solid rgba(255,255,255,0.4)")
                property("background", "linear-gradient(135deg, #10b981 0%, #059669 100%)")
            }
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
        .backgroundColor(Color(ThemeManager.Colors.buttonBackground))
        .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
        .borderRadius(4.px)
        .cursor(Cursor.Pointer)
        .fontSize(14.px)
        .color(Color(ThemeManager.Colors.buttonText))
        .transition(Transition.of("background-color", 0.2.s))

val NavButtonHoverStyle =
    CssStyle {
        base {
            NavButtonStyle
        }

        hover {
            Modifier.backgroundColor(Color(if (ThemeManager.isDarkMode) "#4A4A4A" else "#f0f0f0"))
        }
    }

val TitleStyle =
    CssStyle.base {
        Modifier
            .fontSize(20.px)
            .fontWeight(700)
            .color(Color(ThemeManager.Colors.text))
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
 * Time column component that displays 30-minute intervals throughout the day.
 */
@Composable
fun TimeColumn() {
    Column(TimeColumnStyle.toModifier()) {
        // Empty cell for the header row
        Box(Modifier.height(40.px)) {}

        // 30-minute interval labels - 48 intervals total (24 hours × 2)
        for (halfHour in 0..47) {
            val hour = halfHour / 2
            val isHalfHour = halfHour % 2 == 1
            
            Box(Modifier.height(40.px).padding(top = 4.px)) {
                if (!isHalfHour) { // Only show hour labels on the hour, not half-hour
                    val formattedHour = when {
                        hour == 0 -> "12 AM"
                        hour < 12 -> "$hour AM"  
                        hour == 12 -> "12 PM"
                        else -> "${hour - 12} PM"
                    }
                    SpanText(formattedHour)
                } else {
                    // Show :30 for half-hour marks
                    val baseHour = when {
                        hour == 0 -> "12"
                        hour <= 12 -> "$hour"
                        else -> "${hour - 12}"
                    }
                    SpanText("$baseHour:30", Modifier.fontSize(9.px).color(Color(ThemeManager.Colors.secondaryText)))
                }
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
    onEventDelete: (CalendarEvent) -> Unit, // Add delete callback
) {
    var editingEvent by remember { mutableStateOf<CalendarEvent?>(null) }

    // Drop highlight state
    var isDropTarget by remember { mutableStateOf(false) }

    // For debugging - assign a unique ID to help track this cell
    val cellId = "cell-${date.getFullYear()}-${date.getMonth()}-${date.getDate()}-$hour"

    Box(
        Modifier
            .backgroundColor(
                if (isDropTarget) {
                    Color(if (ThemeManager.isDarkMode) "#1a3a52" else "#f0f9ff")
                } else {
                    Color(ThemeManager.Colors.calendarBackground)
                }
            )
            .border(
                1.px, 
                LineStyle.Solid, 
                Color(if (isDropTarget) {
                    if (ThemeManager.isDarkMode) "#3b82f6" else "#3b82f6"
                } else {
                    ThemeManager.Colors.border
                })
            )
            .minHeight(60.px)
            .position(Position.Relative)
            .overflow(Overflow.Visible)
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
                        // Simple drag and drop handling
                        val eventId = GlobalDragState.draggingId ?: return@onDrop
                        
                        // Create new date at the drop position
                        val newDate = Date(
                            date.getFullYear(), 
                            date.getMonth(), 
                            date.getDate(), 
                            hour
                        )
                        
                        // Find the event and update it
                        val targetEvent = allEvents.find { it.id == eventId }
                        if (targetEvent != null) {
                            val updatedEvent = targetEvent.copy(hour = hour)
                            onEventUpdate(updatedEvent)
                        }
                        
                        GlobalDragState.draggingId = null
                    } catch (e: Exception) {
                        console.error("Drop error: ${e.message}")
                    }
                }
            },
    ) {
        // Time slot indicator (optional) - theme aware
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.px)
                .backgroundColor(
                    Color(if (ThemeManager.isDarkMode) "#404040" else "#f0f0f0")
                )
                .align(Alignment.TopStart),
        )

        // Separate events by mode for positioning
        val activeEvents = events.filter { !it.isPassive }
        val passiveEvents = events.filter { it.isPassive }

        // Active events on the left side
        if (activeEvents.isNotEmpty()) {
            Box(
                Modifier
                    .width(if (passiveEvents.isEmpty()) 100.percent else 60.percent)
                    .align(Alignment.CenterStart)
                    .padding(2.px)
            ) {
                Column {
                    activeEvents.forEachIndexed { index, event ->
                        val isActive = event == editingEvent
                        val isSelected = GlobalSelectionState.isSelected(event.id)
                        var isHovered by remember { mutableStateOf(false) }
                        val isDragging = GlobalDragState.draggingId == event.id

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .margin(bottom = if (index < activeEvents.size - 1) 1.px else 0.px)
                                .onClick { clickEvent ->
                                    // Access shiftKey from the DOM event
                                    val domEvent = clickEvent.nativeEvent
                                    val isShiftClick = domEvent.asDynamic().shiftKey as Boolean
                                    if (isShiftClick) {
                                        GlobalSelectionState.toggleSelection(event.id, true)
                                    } else {
                                        // Call the onEventClick callback to open edit dialog
                                        onEventClick(event)
                                        GlobalSelectionState.toggleSelection(event.id, false)
                                    }
                                }
                                .onMouseEnter { 
                                    if (!isDragging) isHovered = true 
                                }
                                .onMouseLeave { 
                                    if (!isDragging) isHovered = false 
                                }
                                .padding(6.px, 8.px)
                                .backgroundColor(
                                    Color(
                                        event.color ?: if (isSelected) {
                                            if (ThemeManager.isDarkMode) "#059669" else "#10b981"
                                        } else {
                                            if (ThemeManager.isDarkMode) "#10b981" else "#10b981"
                                        }
                                    )
                                ) // Use event color if available, otherwise light green, darker when selected
                                .borderRadius(4.px)
                                .border(
                                    if (isSelected) 2.px else 0.px,
                                    LineStyle.Solid,
                                    Color("#fbbf24")
                                ) // Yellow border for selected events
                                .zIndex(if (isHovered && !isDragging) 10 else 1)
                                .styleModifier {
                                    if (!isDragging) {
                                        property("transition", "all 0.2s ease-in-out")
                                        if (isHovered) {
                                            property("transform", "scale(1.02)")
                                            property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                                        }
                                    }
                                    property("cursor", if (isDragging) "grabbing" else "grab")
                                    property("user-select", "none")
                                    property("min-height", "24px") // Ensure minimum readable height
                                }
                                .position(Position.Relative)
                                .attrsModifier {
                                    attr("draggable", "true")
                                    onDragStart { e ->
                                        GlobalDragState.draggingId = event.id
                                        e.dataTransfer?.effectAllowed = "move"
                                        e.dataTransfer?.setData("text/plain", event.id)
                                        isHovered = false
                                    }
                                    onDragEnd { e ->
                                        GlobalDragState.draggingId = null
                                    }
                                },
                        ) {
                            SpanText(
                                text = event.title,
                                modifier = Modifier
                                    .color(Colors.White)
                                    .fontSize(13.px)
                                    .fontWeight(FontWeight.Bold)
                                    .styleModifier {
                                        property("white-space", "nowrap")
                                        property("overflow", "hidden")
                                        property("text-overflow", "ellipsis")
                                        property("max-width", "100%")
                                        property("pointer-events", "none")
                                        property("line-height", "1.2")
                                        if (!isHovered && !isActive && !isDragging) {
                                            property("opacity", "0.85")
                                            property("transition", "opacity 0.3s ease-in-out")
                                        } else {
                                            property("opacity", "1")
                                        }
                                    }
                            )

                            // Tooltip for active events
                            if (isHovered && !isDragging) {
                                Box(
                                    Modifier
                                        .position(Position.Absolute)
                                        .top((-60).px) // Moved further up to avoid overlap
                                        .left(0.px)
                                        .backgroundColor(
                                            Color(if (ThemeManager.isDarkMode) "#2d3748" else "#1f2937")
                                        )
                                        .color(
                                            Color(if (ThemeManager.isDarkMode) "#e2e8f0" else "#ffffff")
                                        )
                                        .padding(8.px)
                                        .borderRadius(4.px)
                                        .fontSize(12.px)
                                        .zIndex(1000) // Higher z-index to ensure visibility
                                        .styleModifier {
                                            property("box-shadow", "0 2px 8px rgba(0,0,0,0.3)")
                                            property("max-width", "200px")
                                            property("word-wrap", "break-word")
                                            property("pointer-events", "none")
                                        }
                                ) {
                                    SpanText(
                                        if (event.description.isNotBlank()) {
                                            "${event.title} (${CalendarUtils.formatTime(event.startTime)} - ${CalendarUtils.formatTime(event.endTime)})\n${event.description}"
                                        } else {
                                            "${event.title} (${CalendarUtils.formatTime(event.startTime)} - ${CalendarUtils.formatTime(event.endTime)})"
                                        }
                                    )
                                }
                            }

                            // Delete button for active events
                            if (isActive) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.px)
                                        .backgroundColor(Color("#dc2626"))
                                        .borderRadius(2.px)
                                        .padding(4.px)
                                        .cursor(Cursor.Pointer)
                                        .onClick {
                                            editingEvent?.let { event ->
                                                onEventDelete(event)
                                                editingEvent = null
                                            }
                                        }
                                        .styleModifier {
                                            property("transition", "all 0.2s ease")
                                        }
                                ) {
                                    SpanText(
                                        "×",
                                        Modifier
                                            .color(Colors.White)
                                            .fontSize(12.px)
                                            .fontWeight(FontWeight.Bold)
                                            .styleModifier {
                                                property("line-height", "1")
                                                property("user-select", "none")
                                            }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Passive events on the right side with transparency for stacking
        if (passiveEvents.isNotEmpty()) {
            Box(
                Modifier
                    .width(if (activeEvents.isEmpty()) 100.percent else 60.percent)
                    .align(Alignment.CenterEnd)
                    .padding(2.px)
            ) {
                Column {
                    passiveEvents.forEachIndexed { index, event ->
                        val isActive = event == editingEvent
                        val isSelected = GlobalSelectionState.isSelected(event.id)
                        var isHovered by remember { mutableStateOf(false) }
                        val isDragging = GlobalDragState.draggingId == event.id
                        // Improved stacking - reduce overlap more gradually
                        val stackOffset = if (passiveEvents.size > 1) index * -3 else 0
                        val alpha = if (index == 0 && passiveEvents.size > 1) 0.9f else 1f

                        Box(
                            Modifier
                                .fillMaxWidth()
                                .margin(bottom = if (index < passiveEvents.size - 1) 1.px else 0.px)
                                .onClick { clickEvent ->
                                    // Access shiftKey from the DOM event
                                    val domEvent = clickEvent.nativeEvent
                                    val isShiftClick = domEvent.asDynamic().shiftKey as Boolean
                                    if (isShiftClick) {
                                        GlobalSelectionState.toggleSelection(event.id, true)
                                    } else {
                                        // Call the onEventClick callback to open edit dialog
                                        onEventClick(event)
                                        GlobalSelectionState.toggleSelection(event.id, false)
                                    }
                                }
                                .onMouseEnter { 
                                    if (!isDragging) isHovered = true 
                                }
                                .onMouseLeave { 
                                    if (!isDragging) isHovered = false 
                                }
                                .padding(6.px, 8.px)
                                .backgroundColor(
                                    Color(
                                        event.color ?: if (isSelected) {
                                            if (ThemeManager.isDarkMode) "#059669" else "#10b981"
                                        } else {
                                            if (ThemeManager.isDarkMode) "#10b981" else "#10b981"
                                        }
                                    )
                                ) // Use event color if available, otherwise light green, darker when selected
                                .borderRadius(4.px)
                                .border(
                                    if (isSelected) 2.px else 0.px,
                                    LineStyle.Solid,
                                    Color("#fbbf24")
                                ) // Yellow border for selected events
                                .zIndex(if (isHovered && !isDragging) 10 else (passiveEvents.size - index))
                                .attrsModifier {
                                    // Add event-element class for click-outside detection
                                    classes("event-element")
                                }
                                .styleModifier {
                                    property("opacity", alpha.toString())
                                    property("margin-top", "${stackOffset}px")
                                    if (!isDragging) {
                                        property("transition", "all 0.2s ease-in-out")
                                        if (isHovered) {
                                            property("transform", "scale(1.02)")
                                            property("box-shadow", "0 4px 12px rgba(0,0,0,0.15)")
                                        }
                                    }
                                    property("cursor", if (isDragging) "grabbing" else "grab")
                                    property("user-select", "none")
                                    property("min-height", "24px") // Ensure minimum readable height
                                }
                                .position(Position.Relative)
                                .attrsModifier {
                                    attr("draggable", "true")
                                    onDragStart { e ->
                                        GlobalDragState.draggingId = event.id
                                        e.dataTransfer?.effectAllowed = "move"
                                        e.dataTransfer?.setData("text/plain", event.id)
                                        isHovered = false
                                    }
                                    onDragEnd { e ->
                                        GlobalDragState.draggingId = null
                                    }
                                },
                        ) {
                            SpanText(
                                text = event.title,
                                modifier = Modifier
                                    .color(Colors.White)
                                    .fontSize(13.px)
                                    .fontWeight(FontWeight.Bold)
                                    .styleModifier {
                                        property("white-space", "nowrap")
                                        property("overflow", "hidden")
                                        property("text-overflow", "ellipsis")
                                        property("max-width", "100%")
                                        property("pointer-events", "none")
                                        property("line-height", "1.2")
                                        if (!isHovered && !isActive && !isDragging) {
                                            property("opacity", "0.85")
                                            property("transition", "opacity 0.3s ease-in-out")
                                        } else {
                                            property("opacity", "1")
                                        }
                                    }
                            )

                            // Improved Tooltip for passive events - fixes overflow issue
                            if (isHovered && !isDragging) {
                                Box(
                                    Modifier
                                        .position(Position.Absolute)
                                        .top((-65).px) // Moved further up to avoid overlap
                                        .left(0.px) // Changed from right to left for better positioning
                                        .backgroundColor(
                                            Color(if (ThemeManager.isDarkMode) "#2d3748" else "#1f2937")
                                        )
                                        .color(
                                            Color(if (ThemeManager.isDarkMode) "#e2e8f0" else "#ffffff")
                                        )
                                        .padding(10.px, 12.px) // Better padding
                                        .borderRadius(6.px) // Slightly more rounded
                                        .fontSize(11.px)
                                        .zIndex(1000) // Higher z-index to ensure visibility
                                        .attrsModifier {
                                            // Add event-tooltip class for click-outside detection
                                            classes("event-tooltip")
                                        }
                                        .styleModifier {
                                            property("box-shadow", "0 4px 12px rgba(0,0,0,0.25)")
                                            property("max-width", "250px") // Increased max width
                                            property("min-width", "150px") // Minimum width for consistency
                                            property("word-wrap", "break-word")
                                            property("white-space", "normal") // Allow text wrapping
                                            property("pointer-events", "none")
                                            property("border", "1px solid rgba(255,255,255,0.1)")
                                            // Tooltip arrow
                                            property("position", "relative")
                                        }
                                        .attrsModifier {
                                            style {
                                                // CSS-only tooltip arrow
                                                property("--tooltip-arrow", "8px")
                                            }
                                        }
                                ) {
                                    SpanText(
                                        if (event.description.isNotBlank()) {
                                            "${event.title}\n${CalendarUtils.formatTime(event.startTime)} - ${CalendarUtils.formatTime(event.endTime)}\n${event.description}"
                                        } else {
                                            "${event.title}\n${CalendarUtils.formatTime(event.startTime)} - ${CalendarUtils.formatTime(event.endTime)}"
                                        },
                                        Modifier.styleModifier {
                                            property("line-height", "1.4")
                                        }
                                    )
                                }
                            }

                            // Delete button for passive events
                            if (isActive) {
                                Box(
                                    Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(2.px)
                                        .backgroundColor(Color("#dc2626"))
                                        .borderRadius(2.px)
                                        .padding(4.px)
                                        .cursor(Cursor.Pointer)
                                        .onClick {
                                            editingEvent?.let { event ->
                                                onEventDelete(event)
                                                editingEvent = null
                                            }
                                        }
                                        .attrsModifier {
                                            // Add event-element class for click-outside detection
                                            classes("event-element")
                                        }
                                        .styleModifier {
                                            property("transition", "all 0.2s ease")
                                        }
                                ) {
                                    SpanText(
                                        "×",
                                        Modifier
                                            .color(Colors.White)
                                            .fontSize(12.px)
                                            .fontWeight(FontWeight.Bold)
                                            .styleModifier {
                                                property("line-height", "1")
                                                property("user-select", "none")
                                            }
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

/**
 * Month view calendar component that displays a monthly grid with events.
 */
@Composable
fun MonthCalendar(
    displayDate: Date,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onEventUpdate: (CalendarEvent) -> Unit,
    onEventDelete: (CalendarEvent) -> Unit,
) {
    val currentDate = Date()
    val today = Date(currentDate.getFullYear(), currentDate.getMonth(), currentDate.getDate())
    
    // Calculate the first day of the month and the number of days
    val firstDayOfMonth = Date(displayDate.getFullYear(), displayDate.getMonth(), 1)
    val lastDayOfMonth = Date(displayDate.getFullYear(), displayDate.getMonth() + 1, 0)
    val daysInMonth = lastDayOfMonth.getDate()
    val startDayOfWeek = firstDayOfMonth.getDay() // 0 = Sunday, 1 = Monday, etc.
    
    // Calculate total cells needed (including previous/next month days)
    val totalCells = 42 // 6 weeks * 7 days
    
    Box(
        Modifier
            .fillMaxWidth()
            .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
            .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .borderRadius(8.px)
            .overflow(Overflow.Hidden)
    ) {
        Column {
            // Day headers
            Row {
                listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .backgroundColor(Color(ThemeManager.Colors.headerBackground))
                            .padding(12.px)
                            .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                            .styleModifier {
                                property("flex", "1")
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        SpanText(
                            day,
                            Modifier
                                .fontSize(14.px)
                                .fontWeight(FontWeight.Bold)
                                .color(Color(ThemeManager.Colors.text))
                        )
                    }
                }
            }
            
            // Calendar grid
            for (week in 0 until 6) {
                Row {
                    for (day in 0..6) {
                        val cellIndex = week * 7 + day
                        val dayNumber = cellIndex - startDayOfWeek + 1
                        
                        val isCurrentMonth = dayNumber >= 1 && dayNumber <= daysInMonth
                        val cellDate = if (isCurrentMonth) {
                            Date(displayDate.getFullYear(), displayDate.getMonth(), dayNumber)
                        } else if (dayNumber < 1) {
                            // Previous month
                            val prevMonth = if (displayDate.getMonth() == 0) 11 else displayDate.getMonth() - 1
                            val prevYear = if (displayDate.getMonth() == 0) displayDate.getFullYear() - 1 else displayDate.getFullYear()
                            val daysInPrevMonth = Date(prevYear, prevMonth + 1, 0).getDate()
                            Date(prevYear, prevMonth, daysInPrevMonth + dayNumber)
                        } else {
                            // Next month
                            val nextMonth = if (displayDate.getMonth() == 11) 0 else displayDate.getMonth() + 1
                            val nextYear = if (displayDate.getMonth() == 11) displayDate.getFullYear() + 1 else displayDate.getFullYear()
                            Date(nextYear, nextMonth, dayNumber - daysInMonth)
                        }
                        
                        val isToday = CalendarUtils.formatDate(cellDate) == CalendarUtils.formatDate(today)
                        val dayEvents = CalendarUtils.getEventsForDate(
                            xyz.malefic.staticsite.util.Calendar("temp", "temp", 
                                xyz.malefic.staticsite.util.CalendarTheme("temp", "#000", "#000"), 
                                events.toMutableList()
                            ), 
                            cellDate
                        )
                        
                        MonthCalendarCell(
                            date = cellDate,
                            dayNumber = if (isCurrentMonth) dayNumber else if (dayNumber < 1) dayNumber + Date(displayDate.getFullYear(), displayDate.getMonth(), 0).getDate() else dayNumber - daysInMonth,
                            isCurrentMonth = isCurrentMonth,
                            isToday = isToday,
                            events = dayEvents,
                            onEventClick = onEventClick,
                            onEventUpdate = onEventUpdate,
                            onEventDelete = onEventDelete
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual cell in the month calendar view.
 */
@Composable
fun MonthCalendarCell(
    date: Date,
    dayNumber: Int,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    events: List<CalendarEvent>,
    onEventClick: (CalendarEvent) -> Unit,
    onEventUpdate: (CalendarEvent) -> Unit,
    onEventDelete: (CalendarEvent) -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .minHeight(120.px)
            .backgroundColor(
                when {
                    isToday -> Color(ThemeManager.Colors.todayHighlight)
                    !isCurrentMonth -> Color(if (ThemeManager.isDarkMode) "#0f0f0f" else "#f8f9fa")
                    else -> Color(ThemeManager.Colors.calendarBackground)
                }
            )
            .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .padding(4.px)
            .position(Position.Relative)
            .styleModifier {
                property("flex", "1")
            }
    ) {
        Column {
            // Day number
            SpanText(
                dayNumber.toString(),
                Modifier
                    .fontSize(14.px)
                    .fontWeight(if (isToday) FontWeight.Bold else FontWeight.Normal)
                    .color(
                        Color(
                            when {
                                isToday -> if (ThemeManager.isDarkMode) "#ffffff" else "#1f2937"
                                !isCurrentMonth -> ThemeManager.Colors.secondaryText
                                else -> ThemeManager.Colors.text
                            }
                        )
                    )
                    .margin(bottom = 4.px)
            )
            
            // Events
            events.take(3).forEachIndexed { index, event ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .backgroundColor(
                            Color(
                                event.color ?: if (event.isPassive) {
                                    if (ThemeManager.isDarkMode) "#10b981" else "#10b981" // Light green for passive events
                                } else {
                                    if (ThemeManager.isDarkMode) "#10b981" else "#10b981" // Light green for active events too
                                }
                            )
                        )
                        .borderRadius(2.px)
                        .padding(2.px, 4.px)
                        .margin(bottom = 2.px)
                        .cursor(Cursor.Pointer)
                        .onClick { onEventClick(event) }
                        .styleModifier {
                            property("transition", "all 0.2s ease")
                        }
                ) {
                    SpanText(
                        event.title,
                        Modifier
                            .fontSize(10.px)
                            .color(Colors.White)
                            .styleModifier {
                                property("white-space", "nowrap")
                                property("overflow", "hidden")
                                property("text-overflow", "ellipsis")
                            }
                    )
                }
            }
            
            // Show "X more" if there are more than 3 events
            if (events.size > 3) {
                SpanText(
                    "+${events.size - 3} more",
                    Modifier
                        .fontSize(10.px)
                        .color(Color(ThemeManager.Colors.secondaryText))
                        .cursor(Cursor.Pointer)
                )
            }
        }
    }
}
