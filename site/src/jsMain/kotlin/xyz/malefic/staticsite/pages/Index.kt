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
import xyz.malefic.staticsite.util.CalendarEvent
import xyz.malefic.staticsite.util.EventMode
import kotlin.js.Date

@Page
@Composable
fun HomePage() {
    var events by remember { mutableStateOf(listOf<CalendarEvent>()) }
    var command by remember { mutableStateOf("") }
    val output = remember { mutableStateListOf<String>() }

    fun executeCommand(cmd: String) {
        output.add("> $cmd")
        val parts = cmd.split(" ")
        when (parts.getOrNull(0)) {
            "help" -> {
                output.add("Available commands:")
                output.add("  ls - list all events")
                output.add("  add <title> <yyyy-mm-ddThh:mm> <yyyy-mm-ddThh:mm> [description] - add a new event")
                output.add("  rm <id> - remove an event by id")
                output.add("  clear - clear the output")
                output.add("  help - show this help message")
            }
            "ls" -> {
                if (events.isEmpty()) {
                    output.add("No events.")
                } else {
                    output.add("Events:")
                    events.forEach {
                        output.add("  - [${it.id}] ${it.title}: ${it.startTime} to ${it.endTime} (${it.description})")
                    }
                }
            }
            "add" -> {
                try {
                    val title = parts[1]
                    val startTime = Date(parts[2])
                    val endTime = Date(parts[3])
                    val description = parts.drop(4).joinToString(" ")
                    val newEvent =
                        CalendarEvent(
                            id = "evt-${Date.now().toLong()}",
                            title = title,
                            startTime = startTime,
                            endTime = endTime,
                            description = description,
                            mode = EventMode.ACTIVE,
                        )
                    events = events + newEvent
                    output.add("Event added with id: ${newEvent.id}")
                } catch (e: Exception) {
                    output.add("Error: Invalid command format. Use: add <title> <yyyy-mm-ddThh:mm> <yyyy-mm-ddThh:mm> [description]")
                }
            }
            "rm" -> {
                val id = parts.getOrNull(1)
                if (id == null) {
                    output.add("Error: Missing event id. Use: rm <id>")
                } else {
                    val eventExists = events.any { it.id == id }
                    if (eventExists) {
                        events = events.filterNot { it.id == id }
                        output.add("Event with id '$id' removed.")
                    } else {
                        output.add("Error: Event with id '$id' not found.")
                    }
                }
            }
            "clear" -> {
                output.clear()
            }
            else -> {
                output.add("Unknown command: '${parts[0]}'. Type 'help' for a list of commands.")
            }
        }
        command = ""
    }

    LaunchedEffect(Unit) {
        output.add("Welcome to Twilight Terminal.")
        output.add("Type 'help' to see available commands.")
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
