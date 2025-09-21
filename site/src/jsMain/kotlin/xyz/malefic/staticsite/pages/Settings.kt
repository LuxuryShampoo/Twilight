package xyz.malefic.staticsite.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import xyz.malefic.staticsite.util.*

@Page
@Composable
fun SettingsPage() {
    // Available themes
    val themes =
        listOf(
            CalendarTheme("Default", "#9c27b0", "#e91e63", "#ffffff"),
            CalendarTheme("Ocean", "#1976d2", "#00bcd4", "#f0f8ff"),
            CalendarTheme("Forest", "#388e3c", "#8bc34a", "#f1f8e9"),
            CalendarTheme("Sunset", "#f57c00", "#ffeb3b", "#fff8e1"),
        )

    // State for selected theme
    var selectedTheme by remember { mutableStateOf(themes[0]) }

    // State for calendar title
    var calendarTitle by remember { mutableStateOf("My Calendar") }

    // State for background color
    var backgroundColor by remember { mutableStateOf(selectedTheme.backgroundColor) }

    // State for selected mode
    var selectedMode by remember { mutableStateOf("Passive") }
    val coroutineScope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.px)) {
        // Page Title
        Box(Modifier.fillMaxWidth().padding(bottom = 24.px)) {
            H1(
                attrs = {
                    style {
                        color(Color(selectedTheme.primaryColor))
                        margin(0.px)
                    }
                },
            ) {
                Text("Calendar Settings")
            }
        }

        // Settings Form
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.px)
                .border(1.px, LineStyle.Solid, Color("#e0e0e0"))
                .borderRadius(8.px)
                .padding(24.px),
        ) {
            // Calendar Title Setting
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                },
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    },
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
                    },
                )
            }

            // Theme Selection
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                },
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    },
                ) {
                    Text("Theme")
                }

                // Theme Options
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Grid)
                            property("grid-template-columns", "repeat(auto-fill, minmax(200px, 1fr))")
                            gap(16.px)
                        }
                    },
                ) {
                    themes.forEach { theme ->
                        Div(
                            attrs = {
                                onClick { selectedTheme = theme }
                                style {
                                    border(
                                        2.px,
                                        LineStyle.Solid,
                                        if (selectedTheme == theme) Color(theme.primaryColor) else Color("#ccc"),
                                    )
                                    borderRadius(8.px)
                                    padding(16.px)
                                    cursor(Cursor.Pointer)
                                    backgroundColor(if (selectedTheme == theme) Color("#f5f5f5") else Colors.White)
                                }
                            },
                        ) {
                            // Theme Preview
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Flex)
                                        marginBottom(8.px)
                                    }
                                },
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
                                    },
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
                                    },
                                ) {}
                            }

                            // Theme Name
                            Div(
                                attrs = {
                                    style {
                                        fontWeight(600)
                                        color(Color("#333"))
                                    }
                                },
                            ) {
                                Text(theme.name)
                            }
                        }
                    }
                }
            }

            // Background Color
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                },
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    },
                ) {
                    Text("Background Color")
                }

                // Color picker and preview
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            gap(16.px)
                        }
                    },
                ) {
                    // Color preview
                    Div(
                        attrs = {
                            style {
                                width(40.px)
                                height(40.px)
                                backgroundColor(Color(backgroundColor))
                                borderRadius(4.px)
                                border(1.px, LineStyle.Solid, Color("#ccc"))
                            }
                        },
                    ) {}

                    // Color input
                    TextInput(
                        attrs = {
                            value(backgroundColor)
                            onInput { backgroundColor = it.value }
                            style {
                                width(120.px)
                                padding(8.px, 12.px)
                                border(1.px, LineStyle.Solid, Color("#ccc"))
                                borderRadius(4.px)
                                fontSize(16.px)
                            }
                        },
                    )
                }

                P(
                    attrs = {
                        style {
                            fontSize(14.px)
                            color(Color("#666"))
                            margin(4.px, 0.px, 0.px, 0.px)
                        }
                    },
                ) {
                    Text("Enter a color hex code (e.g., #ffffff for white).")
                }
            }

            // Background Image Upload
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                },
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    },
                ) {
                    Text("Background Image (Optional)")
                }

                // File upload button (simplified)
                Button(
                    attrs = {
                        style {
                            padding(8.px, 16.px)
                            backgroundColor(Color("#f0f0f0"))
                            border(1.px, LineStyle.Solid, Color("#ccc"))
                            borderRadius(4.px)
                            cursor(Cursor.Pointer)
                        }
                    },
                ) {
                    Text("Choose File")
                }

                P(
                    attrs = {
                        style {
                            fontSize(14.px)
                            color(Color("#666"))
                            margin(4.px, 0.px, 0.px, 0.px)
                        }
                    },
                ) {
                    Text("Upload an image or GIF to use as a background decoration for your calendar.")
                }
            }

            // Default Event Mode
            Div(
                attrs = {
                    style {
                        marginBottom(24.px)
                    }
                },
            ) {
                Label(
                    attrs = {
                        style {
                            display(DisplayStyle.Block)
                            marginBottom(8.px)
                            fontWeight(600)
                            color(Color("#333"))
                        }
                    },
                ) {
                    Text("Default Event Mode")
                }

                // Mode Options as buttons
                Div(
                    attrs = {
                        style {
                            display(DisplayStyle.Flex)
                            marginBottom(8.px)
                        }
                    },
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
                            },
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
                    },
                ) {
                    Text("Passive: Just for reference. Active: Holds you to the schedule. Custom: Choose per event.")
                }
            }

            // Save Button
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        property("justify-content", "flex-end")
                    }
                },
            ) {
                // Save success message
                var showSaveSuccess by remember { mutableStateOf(false) }

                Button(
                    attrs = {
                        onClick {
                            // Create a custom theme with the current settings
                            val customTheme =
                                CalendarTheme(
                                    name = selectedTheme.name,
                                    primaryColor = selectedTheme.primaryColor,
                                    secondaryColor = selectedTheme.secondaryColor,
                                    backgroundColor = backgroundColor,
                                )

                            // Save settings to localStorage
                            try {
                                // Save theme
                                js(
                                    "localStorage.setItem('calendarTheme', JSON.stringify({" +
                                        "name: customTheme.name," +
                                        "primaryColor: customTheme.primaryColor," +
                                        "secondaryColor: customTheme.secondaryColor," +
                                        "backgroundColor: backgroundColor" +
                                        "}))",
                                )

                                // Save title
                                js("localStorage.setItem('calendarTitle', calendarTitle)")

                                // Save mode
                                js("localStorage.setItem('calendarMode', selectedMode)")

                                // Show success message
                                showSaveSuccess = true

                                // Hide success message after 3 seconds using coroutine
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(3000)
                                    showSaveSuccess = false
                                }
                            } catch (e: Exception) {
                                console.error("Failed to save settings: ${e.message}")
                            }
                        }
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
                    },
                ) {
                    Text("Save Settings")
                }

                // Success message
                if (showSaveSuccess) {
                    Div(
                        attrs = {
                            style {
                                marginTop(16.px)
                                padding(8.px, 16.px)
                                backgroundColor(Color("#4caf50"))
                                color(Colors.White)
                                borderRadius(4.px)
                                fontSize(14.px)
                            }
                        },
                    ) {
                        Text("Settings saved successfully!")
                    }
                }
            }
        }
    }
}
