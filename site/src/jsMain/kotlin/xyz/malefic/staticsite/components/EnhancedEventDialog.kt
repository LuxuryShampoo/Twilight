package xyz.malefic.staticsite.components

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.attributes.*
import org.w3c.dom.HTMLInputElement
import xyz.malefic.staticsite.util.*
import kotlin.js.Date

/**
 * Enhanced event creation dialog with simplified input for different task types
 */
@Composable
fun EnhancedEventDialog(
    onDismiss: () -> Unit,
    onCreate: (CalendarEvent) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var taskType by remember { mutableStateOf(TaskType.ASSIGNMENT) }
    var numQuestions by remember { mutableStateOf(10) }
    var timePerQuestion by remember { mutableStateOf(3.0) } // minutes
    var urgencyLevel by remember { mutableStateOf(UrgencyLevel.MEDIUM) }
    var useQuestionMode by remember { mutableStateOf(true) }
    var manualHours by remember { mutableStateOf(1.0) }

    // Calculate total time
    val totalTimeInMinutes = if (useQuestionMode) {
        numQuestions * timePerQuestion
    } else {
        manualHours * 60
    }
    val totalTimeInHours = totalTimeInMinutes / 60.0

    Box(
        Modifier
            .position(Position.Fixed)
            .top(0.px)
            .left(0.px)
            .right(0.px)
            .bottom(0.px)
            .backgroundColor(com.varabyte.kobweb.compose.ui.graphics.Color.rgba(0f, 0f, 0f, 0.5f))
            .styleModifier {
                property("z-index", "1000")
                property("display", "flex")
                property("justify-content", "center")
                property("align-items", "center")
            }
    ) {
        Box(
            Modifier
                .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                .padding(24.px)
                .borderRadius(8.px)
                .maxWidth(500.px)
                .width(90.percent)
                .maxHeight(90.percent)
                .overflow { y(Overflow.Auto) }
        ) {
            Column(Modifier.styleModifier { property("gap", "16px") }) {
                SpanText(
                    "Create Task",
                    Modifier
                        .fontSize(20.px)
                        .fontWeight(600)
                        .color(Color(ThemeManager.Colors.text))
                )

                // Title
                Column(Modifier.fillMaxWidth()) {
                    SpanText(
                        "Task Name",
                        Modifier
                            .fontSize(14.px)
                            .fontWeight(500)
                            .margin(bottom = 4.px)
                            .color(Color(ThemeManager.Colors.text))
                    )
                    TextInput(
                        attrs = {
                            attr("placeholder", "e.g., Math homework, SAT practice...")
                            value(title)
                            onInput { title = it.value }
                            style {
                                width(100.percent)
                                padding(10.px)
                                border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                borderRadius(4.px)
                                backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                color(Color(ThemeManager.Colors.text))
                                fontSize(14.px)
                            }
                        }
                    )
                }

                // Task Type
                Column(Modifier.fillMaxWidth()) {
                    SpanText(
                        "Task Type",
                        Modifier
                            .fontSize(14.px)
                            .fontWeight(500)
                            .margin(bottom = 8.px)
                            .color(Color(ThemeManager.Colors.text))
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .styleModifier { 
                                property("gap", "8px")
                                property("flex-wrap", "wrap")
                            }
                    ) {
                        listOf(
                            TaskType.ASSIGNMENT to "Assignment",
                            TaskType.HOMEWORK to "Homework",
                            TaskType.PROJECT to "Project",
                            TaskType.SAT_STUDY to "SAT Study",
                            TaskType.EXAM_PREP to "Exam Prep",
                            TaskType.READING to "Reading",
                            TaskType.PRACTICE to "Practice"
                        ).forEach { (type, label) ->
                            Button(
                                attrs = {
                                    onClick { taskType = type }
                                    style {
                                        padding(8.px, 12.px)
                                        backgroundColor(
                                            Color(
                                                if (taskType == type) ThemeManager.Colors.primaryButton
                                                else ThemeManager.Colors.buttonBackground
                                            )
                                        )
                                        color(
                                            if (taskType == type) Colors.White
                                            else Color(ThemeManager.Colors.text)
                                        )
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                        fontSize(13.px)
                                    }
                                }
                            ) {
                                SpanText(label)
                            }
                        }
                    }
                }

                // Time Input Mode Toggle
                Column(Modifier.fillMaxWidth()) {
                    SpanText(
                        "Time Estimation Method",
                        Modifier
                            .fontSize(14.px)
                            .fontWeight(500)
                            .margin(bottom = 8.px)
                            .color(Color(ThemeManager.Colors.text))
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .styleModifier { property("gap", "8px") }
                    ) {
                        Button(
                            attrs = {
                                onClick { useQuestionMode = true }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(
                                        Color(
                                            if (useQuestionMode) ThemeManager.Colors.primaryButton
                                            else ThemeManager.Colors.buttonBackground
                                        )
                                    )
                                    color(
                                        if (useQuestionMode) Colors.White
                                        else Color(ThemeManager.Colors.text)
                                    )
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    fontSize(13.px)
                                }
                            }
                        ) {
                            SpanText("# of Questions")
                        }
                        Button(
                            attrs = {
                                onClick { useQuestionMode = false }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(
                                        Color(
                                            if (!useQuestionMode) ThemeManager.Colors.primaryButton
                                            else ThemeManager.Colors.buttonBackground
                                        )
                                    )
                                    color(
                                        if (!useQuestionMode) Colors.White
                                        else Color(ThemeManager.Colors.text)
                                    )
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    fontSize(13.px)
                                }
                            }
                        ) {
                            SpanText("Manual Time")
                        }
                    }
                }

                // Question-based or Manual time input
                if (useQuestionMode) {
                    Column(Modifier.fillMaxWidth()) {
                        SpanText(
                            "Number of Questions: $numQuestions",
                            Modifier
                                .fontSize(14.px)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text))
                        )
                        Input(
                            type = InputType.Range,
                            attrs = {
                                attr("min", "1")
                                attr("max", "100")
                                attr("step", "1")
                                value(numQuestions.toString())
                                onInput { event ->
                                    val target = event.target as HTMLInputElement
                                    numQuestions = target.value.toIntOrNull() ?: 10
                                }
                                style {
                                    width(100.percent)
                                }
                            }
                        )
                    }

                    Column(Modifier.fillMaxWidth()) {
                        SpanText(
                            "Time per Question: ${timePerQuestion.toInt()} min",
                            Modifier
                                .fontSize(14.px)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text))
                        )
                        Input(
                            type = InputType.Range,
                            attrs = {
                                attr("min", "1")
                                attr("max", "30")
                                attr("step", "1")
                                value(timePerQuestion.toInt().toString())
                                onInput { event ->
                                    val target = event.target as HTMLInputElement
                                    timePerQuestion = target.value.toDoubleOrNull() ?: 3.0
                                }
                                style {
                                    width(100.percent)
                                }
                            }
                        )
                    }
                } else {
                    Column(Modifier.fillMaxWidth()) {
                        SpanText(
                            "Estimated Hours: ${if (manualHours % 1 == 0.0) manualHours.toInt() else manualHours}",
                            Modifier
                                .fontSize(14.px)
                                .margin(bottom = 4.px)
                                .color(Color(ThemeManager.Colors.text))
                        )
                        Input(
                            type = InputType.Range,
                            attrs = {
                                attr("min", "0.25")
                                attr("max", "8")
                                attr("step", "0.25")
                                value(manualHours.toString())
                                onInput { event ->
                                    val target = event.target as HTMLInputElement
                                    manualHours = target.value.toDoubleOrNull() ?: 1.0
                                }
                                style {
                                    width(100.percent)
                                }
                            }
                        )
                    }
                }

                // Total time display
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.px)
                        .backgroundColor(Color("#f3f4f6"))
                        .borderRadius(6.px)
                ) {
                    val formattedHours = (totalTimeInHours * 100).toInt() / 100.0
                    SpanText(
                        "Total Time: $formattedHours hours (${totalTimeInMinutes.toInt()} minutes)",
                        Modifier
                            .fontSize(14.px)
                            .fontWeight(600)
                            .color(Color("#1f2937"))
                    )
                }

                // Urgency
                Column(Modifier.fillMaxWidth()) {
                    SpanText(
                        "Urgency Level",
                        Modifier
                            .fontSize(14.px)
                            .fontWeight(500)
                            .margin(bottom = 8.px)
                            .color(Color(ThemeManager.Colors.text))
                    )
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .styleModifier { property("gap", "8px") }
                    ) {
                        listOf(
                            UrgencyLevel.LOW to ("Low" to "#10b981"),
                            UrgencyLevel.MEDIUM to ("Medium" to "#3b82f6"),
                            UrgencyLevel.HIGH to ("High" to "#f59e0b"),
                            UrgencyLevel.CRITICAL to ("Critical" to "#dc2626")
                        ).forEach { (level, labelColor) ->
                            val (label, color) = labelColor
                            Button(
                                attrs = {
                                    onClick { urgencyLevel = level }
                                    style {
                                        padding(8.px, 16.px)
                                        backgroundColor(
                                            Color(
                                                if (urgencyLevel == level) color
                                                else ThemeManager.Colors.buttonBackground
                                            )
                                        )
                                        color(
                                            if (urgencyLevel == level) Colors.White
                                            else Color(ThemeManager.Colors.text)
                                        )
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                        fontSize(13.px)
                                    }
                                }
                            ) {
                                SpanText(label)
                            }
                        }
                    }
                }

                // Description (optional)
                Column(Modifier.fillMaxWidth()) {
                    SpanText(
                        "Notes (optional)",
                        Modifier
                            .fontSize(14.px)
                            .fontWeight(500)
                            .margin(bottom = 4.px)
                            .color(Color(ThemeManager.Colors.text))
                    )
                    TextArea(
                        attrs = {
                            attr("placeholder", "Any additional notes...")
                            value(description)
                            onInput { description = it.value }
                            style {
                                width(100.percent)
                                padding(10.px)
                                minHeight(60.px)
                                border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                borderRadius(4.px)
                                backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                color(Color(ThemeManager.Colors.text))
                                fontSize(14.px)
                                resize(Resize.Vertical)
                            }
                        }
                    )
                }

                // Buttons
                Row(
                    Modifier
                        .fillMaxWidth()
                        .styleModifier {
                            property("justify-content", "flex-end")
                            property("gap", "8px")
                        }
                ) {
                    Button(
                        attrs = {
                            onClick { onDismiss() }
                            style {
                                padding(10.px, 20.px)
                                backgroundColor(Color(ThemeManager.Colors.buttonBackground))
                                color(Color(ThemeManager.Colors.buttonText))
                                border(0.px)
                                borderRadius(4.px)
                                cursor(Cursor.Pointer)
                                fontSize(14.px)
                            }
                        }
                    ) {
                        SpanText("Cancel")
                    }

                    Button(
                        attrs = {
                            onClick {
                                if (title.isNotBlank()) {
                                    // Create a placeholder event (will be scheduled later)
                                    val now = Date()
                                    val event = CalendarEvent(
                                        id = CalendarUtils.createEventId(),
                                        title = title,
                                        description = description,
                                        startTime = now,
                                        endTime = Date(now.getTime() + (totalTimeInHours * 60 * 60 * 1000).toLong()),
                                        taskType = taskType,
                                        numQuestions = if (useQuestionMode) numQuestions else null,
                                        timePerQuestion = if (useQuestionMode) timePerQuestion else null,
                                        urgencyLevel = urgencyLevel,
                                        mode = when (urgencyLevel) {
                                            UrgencyLevel.CRITICAL, UrgencyLevel.HIGH -> EventMode.ACTIVE
                                            else -> EventMode.PASSIVE
                                        },
                                        color = when (urgencyLevel) {
                                            UrgencyLevel.CRITICAL -> "#dc2626"
                                            UrgencyLevel.HIGH -> "#f59e0b"
                                            UrgencyLevel.MEDIUM -> "#3b82f6"
                                            UrgencyLevel.LOW -> "#10b981"
                                        }
                                    )
                                    onCreate(event)
                                    onDismiss()
                                }
                            }
                            style {
                                padding(10.px, 20.px)
                                backgroundColor(Color(ThemeManager.Colors.primaryButton))
                                color(Colors.White)
                                border(0.px)
                                borderRadius(4.px)
                                cursor(Cursor.Pointer)
                                fontSize(14.px)
                                fontWeight(500)
                            }
                        }
                    ) {
                        SpanText("Create Task")
                    }
                }
            }
        }
    }
}


