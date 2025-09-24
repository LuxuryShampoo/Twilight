package xyz.malefic.staticsite.components.calendar

import androidx.compose.foundation.clickable
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
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
import org.w3c.dom.HTMLElement
import xyz.malefic.staticsite.components.TimePicker
import xyz.malefic.staticsite.model.CalendarEvent
import xyz.malefic.staticsite.util.CalendarUtils
import kotlin.js.Date
import com.varabyte.kobweb.compose.ui.graphics.Color as Kolor

// Shared drag state so other events can disable pointer-events while dragging
object GlobalDragState {
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
            .borderRadius(0.px) // Fix: Add .px unit
            .padding(4.px, 8.px)
            .fontSize(12.px)
            .fontWeight(500)
            .margin(0.px) // Fix: Add .px unit
            .overflow(Overflow.Hidden)
            .styleModifier {
                property("text-overflow", "ellipsis")
                property("white-space", "nowrap")
                property("user-select", "none")
                property("cursor", "grab")
                property("transition", "box-shadow 0.2s ease, transform 0.1s ease")
                property("border-left", "4px solid rgba(255,255,255,0.5)")
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
    onEventDelete: (CalendarEvent) -> Unit, // Add delete callback
) {
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

                        // Fix nullable handling for HTMLElement
                        val targetCell: HTMLElement = elemAtPoint ?: return@onDrop

                        // Traverse up to find an ancestor with data-cell attribute
                        var cur = targetCell
                        while (cur != null) {
                            if (cur.getAttribute("data-cell") == "true") {
                                targetCell = cur
                                break
                            }
                            cur = cur.parentElement as? HTMLElement
                        }

                        // Find the nearest cell to snap to if no exact cell was found under pointer
                        if (targetCell == null) {
                            // Fallback: use the original date and hour
                            CalendarEvent
                                .DragEnd(
                                    eventId = GlobalDragState.draggingId ?: "",
                                    date = date,
                                    hour = hour,
                                ).also { onEventUpdate(it) }
                            return@onDrop
                        }

                        // Extract date and hour from the target cell's data attributes
                        val targetDateStr = targetCell.getAttribute("data-date") ?: return@onDrop
                        val targetHourStr = targetCell.getAttribute("data-hour") ?: return@onDrop

                        // Parse the target date and hour
                        val targetDate = Date(targetDateStr)
                        val targetHour = targetHourStr.toIntOrNull() ?: return@onDrop

                        // Calculate the new date by setting the hour
                        targetDate.setHours(targetHour)

                        // Dispatch the drag end event with the new date and hour
                        CalendarEvent
                            .DragEnd(
                                eventId = GlobalDragState.draggingId ?: "",
                                date = targetDate,
                                hour = targetHour,
                            ).also { onEventUpdate(it) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            },
    ) {
        // Time slot indicator (optional)
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.px)
                .backgroundColor(Kolor.rgba(240, 240, 240, 0.5f))
                .align(Alignment.TopStart),
        )

        // Event indicators
        events.forEach { event ->
            val isActive = event == editingEvent

            Box(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        // On click, set the event to editing state
                        editingEvent = if (isActive) null else event
                    }.padding(4.px)
                    .backgroundColor(
                        when {
                            event.isHoliday -> HolidayEventStyle.backgroundColor
                            event.isCustom -> CustomEventStyle.backgroundColor
                            event.isPassive -> PassiveEventStyle.backgroundColor
                            else -> ActiveEventStyle.backgroundColor
                        },
                    ).borderRadius(4.px)
                    .zIndex(1), // Ensure events are above the time slot indicator
            ) {
                // Event content
                Text(
                    text = event.title,
                    style =
                        TextStyle(
                            color = Colors.White,
                            fontSize = 14.px,
                            fontWeight = FontWeight.Bold,
                        ),
                )
            }

            // Debug: Log event rendering
            LaunchedEffect(event) {
                console.log("Rendered event: ${event.title} at ${event.date} ${event.hour}")
            }
        }

        // Event editing UI
        if (isActive) {
            // Drag handle
            Box(
                Modifier
                    .fillMaxHeight()
                    .width(8.px)
                    .backgroundColor(Kolor.rgba(255, 255, 255, 0.9f))
                    .align(Alignment.CenterEnd)
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount ->
                            // Calculate new position based on drag
                            val newTop = this@Box.offset.y + dragAmount.y
                            val newHour = (newTop / 60).toInt().coerceIn(0, 23)

                            // Update event hour on drag
                            onEventUpdate(event.copy(hour = newHour))
                        }
                    },
            )

            // Close button
            IconButton(
                onClick = {
                    // On close, remove the event
                    onEventDelete(event)
                    editingEvent = null
                },
                modifier =
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.px),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Colors.White,
                )
            }
        }
    }
}
