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
import xyz.malefic.staticsite.util.CalendarEvent
import xyz.malefic.staticsite.util.EventMode
import xyz.malefic.staticsite.util.CalendarUtils
import xyz.malefic.staticsite.util.ThemeManager
import kotlin.js.Date

data class WeeklyTask(
    val id: String,
    val title: String,
    val description: String,
    val estimatedHours: Double,
    val priority: TaskPriority,
    val dueDate: Date?,
    val isCompleted: Boolean = false
)

enum class TaskPriority {
    LOW, MEDIUM, HIGH, URGENT, END
}

@Composable
fun WeeklyTaskManager(
    tasks: List<WeeklyTask>,
    onTaskAdd: (WeeklyTask) -> Unit,
    onTaskUpdate: (WeeklyTask) -> Unit,
    onTaskDelete: (WeeklyTask) -> Unit,
    onAutoSort: (List<CalendarEvent>) -> Unit
) {
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskDescription by remember { mutableStateOf("") }
    var newTaskHours by remember { mutableStateOf(1.0) }
    var newTaskPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.px)
            .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
            .borderRadius(8.px)
            .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
    ) {
        // Header
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.px),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SpanText(
                "Weekly Task Manager",
                Modifier
                    .fontSize(18.px)
                    .fontWeight(600)
                    .color(Color(ThemeManager.Colors.text))
            )
            
            Spacer()
            
            Button(
                attrs = {
                    onClick { showAddTaskDialog = true }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(Color(ThemeManager.Colors.primaryButton))
                        color(Colors.White)
                        border(0.px)
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                        marginRight(8.px)
                    }
                }
            ) {
                SpanText("+ Add Task")
            }
            
            Button(
                attrs = {
                    onClick { 
                        val events = autoSortTasks(tasks)
                        onAutoSort(events)
                    }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(Color("#10b981"))
                        color(Colors.White)
                        border(0.px)
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                    }
                }
            ) {
                SpanText("Auto Sort")
            }
        }

        // Task List
        if (tasks.isEmpty()) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(32.px)
                    .textAlign(TextAlign.Center)
            ) {
                SpanText(
                    "No tasks yet. Add some tasks to get started!",
                    Modifier.color(Color(ThemeManager.Colors.secondaryText))
                )
            }
        } else {
            tasks.forEach { task ->
                TaskItem(
                    task = task,
                    onUpdate = onTaskUpdate,
                    onDelete = onTaskDelete
                )
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            Box(
                Modifier
                    .position(Position.Fixed)
                    .top(0.px)
                    .left(0.px)
                    .right(0.px)
                    .bottom(0.px)
                    .backgroundColor(
                        com.varabyte.kobweb.compose.ui.graphics.Color.rgba(0f, 0f, 0f, 0.5f)
                    )
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
                        .maxWidth(400.px)
                        .width(100.percent)
                ) {
                    Column {
                        SpanText(
                            "Add New Task",
                            Modifier
                                .fontSize(18.px)
                                .fontWeight(600)
                                .color(Color(ThemeManager.Colors.text))
                                .margin(bottom = 16.px)
                        )

                        // Title
                        TextInput(
                            attrs = {
                                attr("placeholder", "Task title...")
                                value(newTaskTitle)
                                onInput { newTaskTitle = it.value }
                                style {
                                    width(100.percent)
                                    padding(8.px)
                                    marginBottom(12.px)
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                    color(Color(ThemeManager.Colors.text))
                                }
                            }
                        )

                        // Description
                        TextArea(
                            attrs = {
                                attr("placeholder", "Task description...")
                                value(newTaskDescription)
                                onInput { newTaskDescription = it.value }
                                style {
                                    width(100.percent)
                                    padding(8.px)
                                    marginBottom(12.px)
                                    minHeight(80.px)
                                    border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
                                    borderRadius(4.px)
                                    backgroundColor(Color(ThemeManager.Colors.calendarBackground))
                                    color(Color(ThemeManager.Colors.text))
                                }
                            }
                        )

                        // Estimated Hours
                        SpanText(
                            "Estimated Hours: ${if (newTaskHours % 1 == 0.0) newTaskHours.toInt().toString() else newTaskHours.toString()}",
                            Modifier
                                .color(Color(ThemeManager.Colors.text))
                                .margin(bottom = 8.px)
                        )
                        
                        Input(
                            type = InputType.Range,
                            attrs = {
                                attr("min", "0")
                                attr("max", "12") // 0 to 3 hours in 0.25 increments = 12 steps  
                                attr("step", "1")
                                value((newTaskHours * 4).toInt().toString()) // Convert to slider steps
                                onInput { event -> 
                                    val target = event.target as HTMLInputElement
                                    val steps = target.value.toIntOrNull() ?: 4 // Default to 1 hour (4 steps)
                                    newTaskHours = steps / 4.0 // Convert steps back to hours
                                }
                                style {
                                    width(100.percent)
                                    marginBottom(16.px)
                                }
                            }
                        )

                        // Priority Selection
                        SpanText(
                            "Priority:",
                            Modifier
                                .color(Color(ThemeManager.Colors.text))
                                .margin(bottom = 8.px)
                        )
                        
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .margin(bottom = 16.px)
                                .styleModifier { property("gap", "8px") }
                        ) {
                            TaskPriority.values().forEach { priority ->
                                Button(
                                    attrs = {
                                        onClick { newTaskPriority = priority }
                                        style {
                                            padding(6.px, 12.px)
                                            backgroundColor(
                                                Color(
                                                    if (newTaskPriority == priority) {
                                                        ThemeManager.Colors.primaryButton
                                                    } else {
                                                        ThemeManager.Colors.buttonBackground
                                                    }
                                                )
                                            )
                                            color(
                                                Color(
                                                    if (newTaskPriority == priority) {
                                                        "#ffffff"
                                                    } else {
                                                        ThemeManager.Colors.text
                                                    }
                                                )
                                            )
                                            border(0.px)
                                            borderRadius(4.px)
                                            cursor(Cursor.Pointer)
                                            fontSize(12.px)
                                        }
                                    }
                                ) {
                                    SpanText(priority.name)
                                }
                            }
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
                                    onClick {
                                        showAddTaskDialog = false
                                        newTaskTitle = ""
                                        newTaskDescription = ""
                                        newTaskHours = 1.0
                                        newTaskPriority = TaskPriority.MEDIUM
                                    }
                                    style {
                                        padding(8.px, 16.px)
                                        backgroundColor(Color(ThemeManager.Colors.buttonBackground))
                                        color(Color(ThemeManager.Colors.buttonText))
                                        border(0.px)
                                        borderRadius(4.px)
                                        cursor(Cursor.Pointer)
                                    }
                                }
                            ) {
                                SpanText("Cancel")
                            }
                            
                            Button(
                                attrs = {
                                    onClick {
                                        if (newTaskTitle.isNotBlank()) {
                                            val task = WeeklyTask(
                                                id = CalendarUtils.createEventId(),
                                                title = newTaskTitle,
                                                description = newTaskDescription,
                                                estimatedHours = newTaskHours,
                                                priority = newTaskPriority,
                                                dueDate = null
                                            )
                                            onTaskAdd(task)
                                            showAddTaskDialog = false
                                            newTaskTitle = ""
                                            newTaskDescription = ""
                                            newTaskHours = 1.0
                                            newTaskPriority = TaskPriority.MEDIUM
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
                                }
                            ) {
                                SpanText("Add Task")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: WeeklyTask,
    onUpdate: (WeeklyTask) -> Unit,
    onDelete: (WeeklyTask) -> Unit
) {
    val priorityColor = when (task.priority) {
        TaskPriority.LOW -> "#10b981"
        TaskPriority.MEDIUM -> "#3b82f6"
        TaskPriority.HIGH -> "#f59e0b"
        TaskPriority.URGENT -> "#ef4444"
        TaskPriority.END -> "#8b5cf6"
    }

    Box(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 8.px)
            .backgroundColor(Color(ThemeManager.Colors.calendarBackground))
            .border(1.px, LineStyle.Solid, Color(ThemeManager.Colors.border))
            .borderRadius(6.px)
            .padding(12.px)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Priority indicator
            Box(
                Modifier
                    .width(4.px)
                    .height(40.px)
                    .backgroundColor(Color(priorityColor))
                    .borderRadius(2.px)
                    .margin(right = 12.px)
            )
            
            Column(Modifier.weight(1f)) {
                SpanText(
                    task.title,
                    Modifier
                        .fontSize(14.px)
                        .fontWeight(600)
                        .color(Color(ThemeManager.Colors.text))
                        .margin(bottom = 4.px)
                )
                
                if (task.description.isNotBlank()) {
                    SpanText(
                        task.description,
                        Modifier
                            .fontSize(12.px)
                            .color(Color(ThemeManager.Colors.secondaryText))
                            .margin(bottom = 4.px)
                    )
                }
                
                SpanText(
                    "${if (task.estimatedHours % 1 == 0.0) task.estimatedHours.toInt().toString() else task.estimatedHours.toString()}h • ${task.priority.name}",
                    Modifier
                        .fontSize(11.px)
                        .color(Color(ThemeManager.Colors.secondaryText))
                )
            }
            
            Button(
                attrs = {
                    onClick { onDelete(task) }
                    style {
                        padding(4.px, 8.px)
                        backgroundColor(Color("#ef4444"))
                        color(Colors.White)
                        border(0.px)
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                        fontSize(12.px)
                    }
                }
            ) {
                SpanText("×")
            }
        }
    }
}

// Auto-sorting algorithm
fun autoSortTasks(tasks: List<WeeklyTask>): List<CalendarEvent> {
    val now = Date()
    val events = mutableListOf<CalendarEvent>()
    
    // Separate END tasks from regular tasks
    val completedTasks = tasks.filter { !it.isCompleted }
    val endTasks = completedTasks.filter { it.priority == TaskPriority.END }
    val regularTasks = completedTasks.filter { it.priority != TaskPriority.END }
        .sortedWith(compareByDescending<WeeklyTask> { it.priority.ordinal }
            .thenBy { it.estimatedHours })
    
    var currentHour = 9.0 // Start at 9 AM
    var currentDay = 0
    val workDayEnd = 17.0 // End at 5 PM
    
    // Schedule regular tasks first
    regularTasks.forEach { task ->
        // Skip weekends (simple implementation)
        if (currentDay >= 5) {
            currentDay = 0
            currentHour = 9.0
        }
        
        // Check if task fits in current day
        if (currentHour + task.estimatedHours > workDayEnd) {
            currentDay++
            currentHour = 9.0
            // Skip weekends again
            if (currentDay >= 5) {
                currentDay = 0
            }
        }
        
        // Create event for the task
        val startHour = currentHour.toInt()
        val startMinute = ((currentHour % 1) * 60).toInt()
        val endTime = currentHour + task.estimatedHours
        val endHour = endTime.toInt()
        val endMinute = ((endTime % 1) * 60).toInt()
        
        val startTime = Date(
            now.getFullYear(),
            now.getMonth(),
            now.getDate() + currentDay,
            startHour,
            startMinute
        )
        val endTimeDate = Date(
            now.getFullYear(),
            now.getMonth(),
            now.getDate() + currentDay,
            endHour,
            endMinute
        )
        
        val event = CalendarEvent(
            id = CalendarUtils.createEventId(),
            title = task.title,
            description = task.description,
            startTime = startTime,
            endTime = endTimeDate,
            mode = if (task.priority == TaskPriority.HIGH || task.priority == TaskPriority.URGENT) {
                EventMode.ACTIVE
            } else {
                EventMode.PASSIVE
            },
            color = when (task.priority) {
                TaskPriority.LOW -> "#10b981"    // Green
                TaskPriority.MEDIUM -> "#3b82f6" // Blue  
                TaskPriority.HIGH -> "#f59e0b"   // Orange
                TaskPriority.URGENT -> "#ef4444" // Red
                TaskPriority.END -> "#8b5cf6"    // Purple
            }
        )
        
        events.add(event)
        
        // Move to next time slot
        currentHour += task.estimatedHours
    }
    
    // Handle END tasks - schedule them to fill remaining time each day
    if (endTasks.isNotEmpty()) {
        val endTask = endTasks.first() // Use the first END task
        
        // For each work day, find the earliest available time and fill to end of day
        for (day in 0 until 5) {
            val dayStartHour = if (day == currentDay && currentHour > 9.0) currentHour else 9.0
            
            if (dayStartHour < workDayEnd) {
                val startTime = Date(
                    now.getFullYear(),
                    now.getMonth(),
                    now.getDate() + day,
                    dayStartHour.toInt(),
                    ((dayStartHour % 1) * 60).toInt()
                )
                val endTime = Date(
                    now.getFullYear(),
                    now.getMonth(),
                    now.getDate() + day,
                    workDayEnd.toInt(),
                    0
                )
                
                val endEvent = CalendarEvent(
                    id = CalendarUtils.createEventId(),
                    title = endTask.title,
                    description = endTask.description + " (END - fills remaining day)",
                    startTime = startTime,
                    endTime = endTime,
                    mode = EventMode.PASSIVE,
                    color = "#8b5cf6" // Purple for END tasks
                )
                
                events.add(endEvent)
            }
        }
    }
    
    return events
}