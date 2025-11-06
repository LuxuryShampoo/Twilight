package xyz.malefic.staticsite.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.Overflow
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.text.SpanText
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.TextArea
import xyz.malefic.staticsite.model.*
import xyz.malefic.staticsite.util.*
import kotlin.js.Date

@Page
@Composable
fun HomePage() {
    var tasks by remember { mutableStateOf(listOf<Task>()) }
    var schedule by remember { mutableStateOf(ScheduleParser.parseDefaultSchedule()) }
    var currentWeekStart by remember { mutableStateOf(getCurrentWeekStart()) }
    var command by remember { mutableStateOf("") }
    val output = remember { mutableStateListOf<String>() }

    // Auto-schedule tasks when they change
    LaunchedEffect(tasks, schedule) {
        if (tasks.isNotEmpty()) {
            AutoScheduler.scheduleTasks(tasks.toList(), schedule, currentWeekStart)
        }
    }

    fun executeCommand(cmd: String) {
        output.add("> $cmd")
        val trimmedCmd = cmd.trim()
        
        // Check if this is a natural language task input (contains " - ")
        if (trimmedCmd.contains(" - ") && !trimmedCmd.startsWith("task ")) {
            val task = TaskParser.parseTask(trimmedCmd)
            if (task != null) {
                tasks = tasks + task
                output.add("✓ Task added: ${task.name}")
                output.add("  Type: ${task.taskType.name.replace("_", " ")}")
                output.add("  Urgency: ${task.urgencyLevel.name}")
                output.add("  Duration: ${task.calculateTotalMinutes()} min")
                output.add("  Due: ${formatDateSimple(task.dueDate)}")
            } else {
                output.add("✗ Failed to parse task. Use format:")
                output.add("  <name> - <details>, due <date>, <urgency>")
                output.add("  Example: Math HW - 50 questions, 1 min each, due Friday, HIGH")
            }
            command = ""
            return
        }
        
        val parts = trimmedCmd.split(" ")
        when (parts.getOrNull(0)) {
            "help" -> {
                output.add("TASK SCHEDULER COMMANDS:")
                output.add("  <task description> - Add task using natural language")
                output.add("    Example: Math HW - 50 questions, 1 min each, due Friday, HIGH")
                output.add("  tasks - List all tasks")
                output.add("  schedule - Show auto-generated schedule")
                output.add("  stats - Show scheduling statistics")
                output.add("  rm <task-id> - Remove a task")
                output.add("  clear - Clear the output")
                output.add("  help - Show this help message")
            }
            "tasks" -> {
                if (tasks.isEmpty()) {
                    output.add("No tasks yet.")
                } else {
                    output.add("TASKS (${tasks.size}):")
                    tasks.forEach { task ->
                        val shortId = task.id.takeLast(8)
                        output.add("  [$shortId] ${task.name}")
                        output.add("    ${task.taskType.name.replace("_", " ")} | ${task.urgencyLevel.name} | ${task.calculateTotalMinutes()}min")
                        output.add("    Due: ${formatDateSimple(task.dueDate)}")
                        if (task.scheduledSessions.isNotEmpty()) {
                            output.add("    Scheduled: ${task.scheduledSessions.size} session(s)")
                        }
                        output.add("")
                    }
                }
            }
            "schedule" -> {
                if (tasks.isEmpty()) {
                    output.add("No tasks to schedule.")
                } else {
                    output.add("AUTO-GENERATED SCHEDULE:")
                    output.add("")
                    
                    // Group sessions by day
                    val sessionsByDay = mutableMapOf<Int, MutableList<Pair<Task, ScheduledSession>>>()
                    tasks.forEach { task ->
                        task.scheduledSessions.forEach { session ->
                            val day = session.startTime.getDay()
                            sessionsByDay.getOrPut(day) { mutableListOf() }.add(Pair(task, session))
                        }
                    }
                    
                    if (sessionsByDay.isEmpty()) {
                        output.add("  No sessions scheduled yet.")
                        output.add("  (Tasks may not fit in available free blocks)")
                    } else {
                        val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                        val sortedDays = sessionsByDay.keys.sorted()
                        sortedDays.forEach { day ->
                            val sessions = sessionsByDay[day] ?: return@forEach
                            output.add("${days[day]}:")
                            sessions.sortedBy { it.second.startTime.getTime() }.forEach { pair ->
                                val task = pair.first
                                val session = pair.second
                                val timeStr = formatTime(session.startTime) + " - " + formatTime(session.endTime)
                                output.add("  $timeStr | ${task.name} (${session.durationMinutes}min)")
                            }
                            output.add("")
                        }
                    }
                }
            }
            "stats" -> {
                if (tasks.isEmpty()) {
                    output.add("No tasks to analyze.")
                } else {
                    val stats = AutoScheduler.calculateStatistics(tasks, schedule)
                    output.add("SCHEDULING STATISTICS:")
                    output.add("  Total Free Time: ${formatHours(stats.totalFreeHours)} hrs")
                    output.add("  Scheduled Time: ${formatHours(stats.scheduledHours)} hrs")
                    output.add("  Remaining Free: ${formatHours(stats.remainingFreeHours)} hrs")
                    output.add("  Utilization: ${formatPercent(stats.utilizationPercent)}%")
                    output.add("  Tasks Scheduled: ${stats.tasksScheduled}")
                    output.add("  Tasks Unscheduled: ${stats.tasksUnscheduled}")
                }
            }
            "rm" -> {
                val id = parts.getOrNull(1)
                if (id == null) {
                    output.add("✗ Missing task ID. Use: rm <task-id>")
                } else {
                    val taskExists = tasks.any { it.id.endsWith(id) }
                    if (taskExists) {
                        tasks = tasks.filterNot { it.id.endsWith(id) }
                        output.add("✓ Task removed.")
                    } else {
                        output.add("✗ Task with id '$id' not found.")
                    }
                }
            }
            "clear" -> {
                output.clear()
            }
            else -> {
                output.add("✗ Unknown command: '${parts[0]}'")
                output.add("  Type 'help' for available commands")
                output.add("  Or add a task: <name> - <details>, due <date>, <urgency>")
            }
        }
        command = ""
    }

    LaunchedEffect(Unit) {
        output.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        output.add("  TWILIGHT - Intelligent Task Scheduler")
        output.add("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        output.add("")
        output.add("Type 'help' for commands or add a task directly:")
        output.add("  Example: Study Physics - 2 hours, due Monday, HIGH")
        output.add("")
    }

    Box(
        Modifier
            .fillMaxSize()
            .backgroundColor(Colors.Black)
            .padding(16.px)
            .fontFamily("monospace")
            .color(Colors.LightGreen),
    ) {
        Column(Modifier.fillMaxSize()) {
            // Output area
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .overflow { y(Overflow.Auto) },
            ) {
                Column(Modifier.fillMaxWidth()) {
                    output.forEach {
                        SpanText(it, Modifier.whiteSpace(WhiteSpace.PreWrap))
                    }
                }
            }

            // Input area
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                SpanText(">", Modifier.margin(right = 8.px))
                TextArea(attrs = {
                    value(command)
                    onInput { e -> command = e.value }
                    onKeyDown { e ->
                        if (e.key == "Enter" && !e.shiftKey) {
                            e.preventDefault()
                            executeCommand(command)
                        }
                    }
                    style {
                        width(100.percent)
                        border(0.px)
                        outline("none")
                        backgroundColor(Color("transparent"))
                        color(Color("lightgreen"))
                        fontFamily("monospace")
                        property("resize", "none")
                        height(20.px)
                        overflow("hidden")
                    }
                })
            }
        }
    }
}

private fun getCurrentWeekStart(): Date {
    val now = Date()
    val dayOfWeek = now.getDay() // 0 = Sunday
    val daysToSubtract = dayOfWeek
    return Date(now.getTime() - daysToSubtract * 24 * 60 * 60 * 1000)
}

private fun formatDateSimple(date: Date): String {
    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return "${days[date.getDay()]} ${months[date.getMonth()]} ${date.getDate()}"
}

private fun formatTime(date: Date): String {
    val hour = date.getHours()
    val minute = date.getMinutes()
    val ampm = if (hour >= 12) "PM" else "AM"
    val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
    val displayMinute = minute.toString().padStart(2, '0')
    return "$displayHour:$displayMinute$ampm"
}

private fun formatHours(hours: Double): String {
    val rounded = kotlin.math.round(hours * 10) / 10
    return rounded.toString()
}

private fun formatPercent(percent: Double): String {
    return kotlin.math.round(percent).toInt().toString()
}
