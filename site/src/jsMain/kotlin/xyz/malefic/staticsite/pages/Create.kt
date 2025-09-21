package xyz.malefic.staticsite.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import xyz.malefic.staticsite.util.*

@Page
@Composable
fun CreatePage() {
    // Available themes
    val themes = listOf(
        CalendarTheme("Default", "#9c27b0", "#e91e63"),
        CalendarTheme("Ocean", "#1976d2", "#00bcd4"),
        CalendarTheme("Forest", "#388e3c", "#8bc34a"),
        CalendarTheme("Sunset", "#f57c00", "#ffeb3b")
    )

    // State for selected theme
    var selectedTheme by remember { mutableStateOf(themes[0]) }

    // State for calendar title
    var calendarTitle by remember { mutableStateOf("") }

    // State for selected mode
    var selectedMode by remember { mutableStateOf("Passive") }

    Column(Modifier.fillMaxSize().padding(16.px)) {
        // Page Title
        Box(Modifier.fillMaxWidth().padding(bottom = 24.px)) {
            H1(
                attrs = {
                    style {
                        color(Color(selectedTheme.primaryColor))
                        margin(0.px)
                    }
                }
            ) {
                Text("Create New Calendar")
            }
        }

        // Create Calendar Form
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.px)
                .border(1.px, LineStyle.Solid, Color("#e0e0e0"))
                .borderRadius(8.px)
                .padding(24.px)
        ) {
            // Calendar Title Setting
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                }
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    }
                ) {
                    Text("Calendar Title")
                }

                TextInput(
                    attrs = {
                        value(calendarTitle)
                        onInput { calendarTitle = it.value }
                        style {
                            width(100.percent)
                            padding(8.px, 12.px)
                            border(1.px, LineStyle.Solid, Color("#ccc"))
                            borderRadius(4.px)
                            fontSize(16.px)
                        }
                    }
                )

                // Helper text
                P(
                    attrs = {
                        style {
                            fontSize(14.px)
                            color(Color("#666"))
                            margin(4.px, 0.px, 0.px, 0.px)
                        }
                    }
                ) {
                    Text("Enter a name for your new calendar")
                }
            }

            // Theme Selection
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                }
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    }
                ) {
                    Text("Choose a Theme")
                }

                // Theme Options
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Grid)
                            property("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
                            gap(16.px)
                        }
                    }
                ) {
                    themes.forEach { theme ->
                        Div(
                            attrs = {
                                onClick { selectedTheme = theme }
                                style {
                                    border(2.px, LineStyle.Solid, 
                                        if (selectedTheme == theme) Color(theme.primaryColor) else Color("#ccc"))
                                    borderRadius(8.px)
                                    padding(16.px)
                                    cursor(Cursor.Pointer)
                                    backgroundColor(if (selectedTheme == theme) Color("#f5f5f5") else Colors.White)
                                }
                            }
                        ) {
                            // Theme Preview
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Flex)
                                        marginBottom(8.px)
                                    }
                                }
                            ) {
                                // Primary Color
                                Div(
                                    attrs = {
                                        style {
                                            width(24.px)
                                            height(24.px)
                                            backgroundColor(Color(theme.primaryColor))
                                            borderRadius(4.px)
                                            marginRight(8.px)
                                        }
                                    }
                                ) {}

                                // Secondary Color
                                Div(
                                    attrs = {
                                        style {
                                            width(24.px)
                                            height(24.px)
                                            backgroundColor(Color(theme.secondaryColor))
                                            borderRadius(4.px)
                                        }
                                    }
                                ) {}
                            }

                            // Theme Name
                            Div(
                                attrs = {
                                    style {
                                        fontWeight(600)
                                        color(Color("#333"))
                                    }
                                }
                            ) {
                                Text(theme.name)
                            }
                        }
                    }
                }
            }

            // Default Event Mode
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                }
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    }
                ) {
                    Text("Calendar Mode")
                }

                // Mode Options as buttons
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            marginBottom(8.px)
                        }
                    }
                ) {
                    // Mode buttons
                    listOf("Passive", "Active", "Custom").forEach { mode ->
                        Button(
                            attrs = {
                                onClick { selectedMode = mode }
                                style {
                                    padding(8.px, 16.px)
                                    backgroundColor(if (selectedMode == mode) Color(selectedTheme.primaryColor) else Color("#f0f0f0"))
                                    color(if (selectedMode == mode) Colors.White else Color("#333"))
                                    border(0.px)
                                    borderRadius(4.px)
                                    cursor(Cursor.Pointer)
                                    marginRight(8.px)
                                }
                            }
                        ) {
                            Text(mode)
                        }
                    }
                }

                P(
                    attrs = {
                        style {
                            fontSize(14.px)
                            color(Color("#666"))
                            margin(4.px, 0.px, 0.px, 0.px)
                        }
                    }
                ) {
                    Text("Passive: Just for reference. Active: Holds you to the schedule. Custom: Choose per event.")
                }
            }

            // Calendar Preview
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                }
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    }
                ) {
                    Text("Calendar Preview")
                }

                // Preview Box
                Div(
                    attrs = {
                        style {
                            border(1.px, LineStyle.Solid, Color("#e0e0e0"))
                            borderRadius(8.px)
                            padding(16.px)
                            backgroundColor(Colors.White)
                        }
                    }
                ) {
                    // Calendar Title Preview
                    H3(
                        attrs = {
                            style {
                                color(Color(selectedTheme.primaryColor))
                                margin(0.px, 0.px, 16.px, 0.px)
                            }
                        }
                    ) {
                        Text(if (calendarTitle.isNotEmpty()) calendarTitle else "My Calendar")
                    }

                    // Mini Calendar Grid Preview
                    Table(
                        attrs = {
                            style {
                                width(100.percent)
                                borderCollapse(BorderCollapse.Collapse)
                            }
                        }
                    ) {
                        // Days of the week
                        Tr {
                            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                                Th(
                                    attrs = {
                                        style {
                                            padding(8.px)
                                            backgroundColor(Color(selectedTheme.primaryColor))
                                            color(Colors.White)
                                            textAlign(TextAlign.Center)
                                        }
                                    }
                                ) {
                                    Text(day)
                                }
                            }
                        }

                        // Calendar days (just a sample week)
                        for (week in 1..2) {
                            Tr {
                                for (day in 1..7) {
                                    Td(
                                        attrs = {
                                            style {
                                                padding(8.px)
                                                border(1.px, LineStyle.Solid, Color("#e0e0e0"))
                                                height(40.px)
                                                textAlign(TextAlign.Center)
                                                verticalAlign(VerticalAlign.Top)
                                            }
                                        }
                                    ) {
                                        Text("${day + (week - 1) * 7}")

                                        // Sample event (only on a couple of cells)
                                        if ((week == 1 && day == 3) || (week == 2 && day == 5)) {
                                            Div(
                                                attrs = {
                                                    style {
                                                        backgroundColor(Color(selectedTheme.primaryColor))
                                                        color(Colors.White)
                                                        fontSize(10.px)
                                                        padding(2.px, 4.px)
                                                        borderRadius(2.px)
                                                        marginTop(2.px)
                                                    }
                                                }
                                            ) {
                                                Text("Event")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Create Button
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        property("justify-content", "flex-end")
                    }
                }
            ) {
                Button(
                    attrs = {
                        style {
                            padding(12.px, 24.px)
                            backgroundColor(Color(selectedTheme.primaryColor))
                            color(Colors.White)
                            border(0.px)
                            borderRadius(4.px)
                            cursor(Cursor.Pointer)
                            fontSize(16.px)
                            fontWeight(500)
                        }
                    }
                ) {
                    Text("Create Calendar")
                }
            }
        }
    }
}
