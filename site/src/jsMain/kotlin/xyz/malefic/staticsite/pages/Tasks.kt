package xyz.malefic.staticsite.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import xyz.malefic.staticsite.model.*
import xyz.malefic.staticsite.util.*
import kotlin.js.Date

@Page("/tasks")
@Composable
fun TasksPage() {
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var taskInput by remember { mutableStateOf("") }
    var schedule by remember { mutableStateOf(ScheduleParser.parseDefaultSchedule()) }
    var currentWeekStart by remember { mutableStateOf(getCurrentWeekStart()) }
    var showSchedule by remember { mutableStateOf(false) }
    var stats by remember { mutableStateOf<SchedulingStatistics?>(null) }
    
    // Auto-schedule tasks when they change
    LaunchedEffect(tasks, schedule) {
        if (tasks.isNotEmpty()) {
            AutoScheduler.scheduleTasks(tasks.toList(), schedule, currentWeekStart)
            stats = AutoScheduler.calculateStatistics(tasks, schedule)
        }
    }
    
    Column(
        Modifier
            .fillMaxSize()
            .backgroundColor(Color("#f5f5f5"))
            .padding(24.px)
    ) {
        // Header
        Box(Modifier.fillMaxWidth().padding(bottom = 24.px)) {
            H1(
                attrs = {
                    style {
                        color(Color("#1f2937"))
                        margin(0.px)
                    }
                }
            ) {
                Text("Smart Task Scheduler")
            }
        }
        
        // Input Section
        Box(
            Modifier
                .fillMaxWidth()
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .padding(24.px)
                .margin(bottom = 24.px)
                .boxShadow(0.px, 2.px, 4.px, 0.px, Color("#00000010"))
        ) {
            Column(Modifier.fillMaxWidth()) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#374151"))
                            fontSize(16.px)
                        }
                    }
                ) {
                    Text("Add Task (Natural Language)")
                }
                
                // Examples
                P(
                    attrs = {
                        style {
                            fontSize(14.px)
                            color(Color("#6b7280"))
                            margin(0.px, 0.px, 12.px, 0.px)
                        }
                    }
                ) {
                    Text("Examples:")
                    Br()
                    Text("• SAT Math Practice - 50 questions, 1 min each, due Friday, HIGH urgency")
                    Br()
                    Text("• History Essay - 3 hours total, due next Monday, MEDIUM urgency")
                    Br()
                    Text("• Read Chapter 5 - 45 minutes, due tomorrow, LOW urgency")
                }
                
                Row(
                    Modifier.fillMaxWidth().gap(12.px),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextArea(attrs = {
                        value(taskInput)
                        onInput { e -> taskInput = e.value }
                        style {
                            flex("1")
                            padding(12.px)
                            border(1.px, LineStyle.Solid, Color("#d1d5db"))
                            borderRadius(4.px)
                            fontSize(14.px)
                            property("resize", "vertical")
                            minHeight(60.px)
                        }
                    })
                    
                    Button(
                        attrs = {
                            onClick {
                                val task = TaskParser.parseTask(taskInput)
                                if (task != null) {
                                    tasks = tasks + task
                                    taskInput = ""
                                } else {
                                    console.log("Failed to parse task")
                                }
                            }
                            style {
                                padding(12.px, 24.px)
                                backgroundColor(Color("#3b82f6"))
                                color(Colors.White)
                                border(0.px)
                                borderRadius(4.px)
                                cursor(Cursor.Pointer)
                                fontSize(14.px)
                                fontWeight(600)
                                property("white-space", "nowrap")
                            }
                        }
                    ) {
                        Text("Add Task")
                    }
                    
                    Button(
                        attrs = {
                            onClick {
                                showSchedule = !showSchedule
                            }
                            style {
                                padding(12.px, 24.px)
                                backgroundColor(Color("#10b981"))
                                color(Colors.White)
                                border(0.px)
                                borderRadius(4.px)
                                cursor(Cursor.Pointer)
                                fontSize(14.px)
                                fontWeight(600)
                                property("white-space", "nowrap")
                            }
                        }
                    ) {
                        Text(if (showSchedule) "Hide Schedule" else "Show Schedule")
                    }
                }
            }
        }
        
        // Statistics Section
        stats?.let { statistics ->
            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Colors.White)
                    .borderRadius(8.px)
                    .padding(20.px)
                    .margin(bottom = 24.px)
                    .boxShadow(0.px, 2.px, 4.px, 0.px, Color("#00000010"))
            ) {
                Column(Modifier.fillMaxWidth()) {
                    H2(
                        attrs = {
                            style {
                                fontSize(20.px)
                                fontWeight(600)
                                margin(0.px, 0.px, 16.px, 0.px)
                                color(Color("#1f2937"))
                            }
                        }
                    ) {
                        Text("Schedule Statistics")
                    }
                    
                    Row(
                        Modifier.fillMaxWidth().gap(24.px),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StatCard("Total Free Time", "${statistics.totalFreeHours.format(1)} hrs", "#3b82f6")
                        StatCard("Scheduled Time", "${statistics.scheduledHours.format(1)} hrs", "#10b981")
                        StatCard("Remaining Free", "${statistics.remainingFreeHours.format(1)} hrs", "#6366f1")
                        StatCard("Utilization", "${statistics.utilizationPercent.format(1)}%", "#f59e0b")
                    }
                }
            }
        }
        
        // Tasks List
        Box(
            Modifier
                .fillMaxWidth()
                .backgroundColor(Colors.White)
                .borderRadius(8.px)
                .padding(20.px)
                .margin(bottom = 24.px)
                .boxShadow(0.px, 2.px, 4.px, 0.px, Color("#00000010"))
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    Modifier.fillMaxWidth().margin(bottom = 16.px),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    H2(
                        attrs = {
                            style {
                                fontSize(20.px)
                                fontWeight(600)
                                margin(0.px)
                                color(Color("#1f2937"))
                            }
                        }
                    ) {
                        Text("Your Tasks (${tasks.size})")
                    }
                    
                    if (tasks.isNotEmpty()) {
                        Button(
                            attrs = {
                                onClick {
                                    tasks = emptyList()
                                    stats = null
                                }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(Color("#ef4444"))
                                    color(Colors.White)
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    fontSize(14.px)
                                }
                            }
                        ) {
                            Text("Clear All")
                        }
                    }
                }
                
                if (tasks.isEmpty()) {
                    P(
                        attrs = {
                            style {
                                color(Color("#9ca3af"))
                                fontSize(14.px)
                                textAlign(TextAlign.Center)
                                padding(40.px)
                            }
                        }
                    ) {
                        Text("No tasks yet. Add a task above using natural language!")
                    }
                } else {
                    Column(Modifier.fillMaxWidth().gap(12.px)) {
                        tasks.forEach { task ->
                            TaskCard(task) {
                                tasks = tasks.filter { it.id != task.id }
                            }
                        }
                    }
                }
            }
        }
        
        // Schedule View
        if (showSchedule) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .backgroundColor(Colors.White)
                    .borderRadius(8.px)
                    .padding(20.px)
                    .boxShadow(0.px, 2.px, 4.px, 0.px, Color("#00000010"))
            ) {
                Column(Modifier.fillMaxWidth()) {
                    H2(
                        attrs = {
                            style {
                                fontSize(20.px)
                                fontWeight(600)
                                margin(0.px, 0.px, 16.px, 0.px)
                                color(Color("#1f2937"))
                            }
                        }
                    ) {
                        Text("Weekly Schedule")
                    }
                    
                    ScheduleCalendar(schedule, tasks)
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: String) {
    Box(
        Modifier
            .border(1.px, LineStyle.Solid, Color("#e5e7eb"))
            .borderRadius(6.px)
            .padding(16.px)
            .flex(1)
    ) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            SpanText(
                label,
                Modifier
                    .fontSize(14.px)
                    .color(Color("#6b7280"))
                    .margin(bottom = 8.px)
            )
            SpanText(
                value,
                Modifier
                    .fontSize(24.px)
                    .fontWeight(700)
                    .color(Color(color))
            )
        }
    }
}

@Composable
private fun TaskCard(task: Task, onDelete: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .border(2.px, LineStyle.Solid, Color(task.urgencyLevel.getColor()))
            .borderRadius(6.px)
            .padding(16.px)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(Modifier.flex(1)) {
                SpanText(
                    task.name,
                    Modifier
                        .fontSize(16.px)
                        .fontWeight(600)
                        .color(Color("#1f2937"))
                        .margin(bottom = 8.px)
                )
                
                Row(Modifier.gap(16.px).margin(bottom = 8.px)) {
                    Tag("${task.taskType.name.replace("_", " ")}", "#6366f1")
                    Tag(task.urgencyLevel.name, task.urgencyLevel.getColor())
                    Tag("${task.calculateTotalMinutes()} min", "#64748b")
                }
                
                SpanText(
                    "Due: ${DateParser.formatDate(task.dueDate)}",
                    Modifier
                        .fontSize(14.px)
                        .color(Color("#6b7280"))
                        .margin(bottom = 4.px)
                )
                
                if (task.scheduledSessions.isNotEmpty()) {
                    SpanText(
                        "Scheduled: ${task.scheduledSessions.size} session(s)",
                        Modifier
                            .fontSize(14.px)
                            .color(Color("#10b981"))
                            .fontWeight(500)
                    )
                }
            }
            
            Button(
                attrs = {
                    onClick { onDelete() }
                    style {
                        padding(6.px, 12.px)
                        backgroundColor(Color("#ef4444"))
                        color(Colors.White)
                        border(0.px)
                        borderRadius(4.px)
                        cursor(Cursor.Pointer)
                        fontSize(12.px)
                    }
                }
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
private fun Tag(text: String, color: String) {
    Box(
        Modifier
            .backgroundColor(Color(color))
            .color(Colors.White)
            .fontSize(12.px)
            .padding(4.px, 8.px)
            .borderRadius(4.px)
            .fontWeight(500)
    ) {
        SpanText(text)
    }
}

@Composable
private fun ScheduleCalendar(schedule: WeeklySchedule, tasks: List<Task>) {
    val days = listOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    
    Column(Modifier.fillMaxWidth().gap(12.px)) {
        days.forEachIndexed { dayIndex, dayName ->
            val dayOfWeek = DayOfWeek.fromIndex(dayIndex)
            val freeBlocks = schedule.getFreeBlocksForDay(dayOfWeek)
            
            if (freeBlocks.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .border(1.px, LineStyle.Solid, Color("#e5e7eb"))
                        .borderRadius(6.px)
                        .padding(12.px)
                ) {
                    SpanText(
                        dayName,
                        Modifier
                            .fontSize(16.px)
                            .fontWeight(600)
                            .color(Color("#1f2937"))
                            .margin(bottom = 8.px)
                    )
                    
                    Column(Modifier.fillMaxWidth().gap(4.px)) {
                        freeBlocks.forEach { block ->
                            val scheduledSessions = tasks.flatMap { it.scheduledSessions }
                                .filter { session ->
                                    val sessionDay = DayOfWeek.fromDate(session.startTime)
                                    sessionDay == dayOfWeek &&
                                    session.startTime.getHours() >= block.startHour &&
                                    session.endTime.getHours() <= block.endHour
                                }
                            
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(8.px)
                                    .backgroundColor(Color("#f9fafb"))
                                    .borderRadius(4.px),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                SpanText(
                                    "${block.name}: ${formatTime(block.startHour, block.startMinute)} - ${formatTime(block.endHour, block.endMinute)}",
                                    Modifier
                                        .fontSize(14.px)
                                        .color(Color("#374151"))
                                )
                                
                                if (scheduledSessions.isNotEmpty()) {
                                    SpanText(
                                        "(${scheduledSessions.size} task(s))",
                                        Modifier
                                            .fontSize(12.px)
                                            .color(Color("#10b981"))
                                            .fontWeight(500)
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

private fun formatTime(hour: Int, minute: Int): String {
    val ampm = if (hour >= 12) "PM" else "AM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val displayMinute = minute.toString().padStart(2, '0')
    return "$displayHour:$displayMinute $ampm"
}

private fun getCurrentWeekStart(): Date {
    val now = Date()
    val dayOfWeek = now.getDay() // 0 = Sunday
    val daysToSubtract = dayOfWeek
    return Date(now.getTime() - daysToSubtract * 24 * 60 * 60 * 1000)
}

private fun Double.format(decimals: Int): String {
    // Use a simple rounding approach without dynamic
    val factor = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        3 -> 1000.0
        else -> 1.0
    }
    val rounded = kotlin.math.round(this * factor) / factor
    
    // Format to string with fixed decimals
    val intPart = rounded.toInt()
    val decPart = ((rounded - intPart) * factor).toInt()
    
    return if (decimals == 0) {
        intPart.toString()
    } else {
        val decStr = decPart.toString().padStart(decimals, '0')
        "$intPart.$decStr"
    }
}
