package xyz.malefic.staticsite.pages

import androidx.compose.runtime.*
import com.varabyte.kobweb.compose.css.*
import com.varabyte.kobweb.compose.foundation.layout.*
import com.varabyte.kobweb.compose.ui.*
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.core.Page
import com.varabyte.kobweb.silk.components.navigation.Link
import kotlinx.browser.document
import kotlinx.browser.localStorage
import kotlinx.browser.window
import org.jetbrains.compose.web.attributes.*
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import xyz.malefic.staticsite.util.*
import kotlin.js.Date
import kotlin.js.JSON
import kotlin.js.json

data class CalendarTheme(
    val name: String,
    val primaryColor: String,
    val secondaryColor: String,
    val backgroundColor: String,
)

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
            CalendarTheme("Dark", "#5c6bc0", "#7986cb", "#121212"),
            CalendarTheme("Minimal", "#212121", "#757575", "#fafafa"),
        )

    // Load saved settings from localStorage
    val savedTitle = localStorage.getItem("calendarTitle") ?: "Twilight Calendar"
    val savedThemeJson = localStorage.getItem("calendarTheme")
    val savedMode = localStorage.getItem("calendarMode") ?: "Passive"

    // Default values
    var selectedThemeIndex by remember { mutableStateOf(0) }
    var customPrimaryColor by remember { mutableStateOf("#9c27b0") }
    var customSecondaryColor by remember { mutableStateOf("#e91e63") }
    var customBackgroundColor by remember { mutableStateOf("#ffffff") }

    // Try to parse saved theme
    if (savedThemeJson != null) {
        try {
            val themeObj = JSON.parse<dynamic>(savedThemeJson)
            customPrimaryColor = themeObj.primaryColor as? String ?: customPrimaryColor
            customSecondaryColor = themeObj.secondaryColor as? String ?: customSecondaryColor
            customBackgroundColor = themeObj.backgroundColor as? String ?: customBackgroundColor

            // Find matching theme if any
            val matchIndex =
                themes.indexOfFirst {
                    it.primaryColor == customPrimaryColor &&
                        it.secondaryColor == customSecondaryColor &&
                        it.backgroundColor == customBackgroundColor
                }
            if (matchIndex >= 0) {
                selectedThemeIndex = matchIndex
            }
        } catch (e: Exception) {
            console.error("Failed to parse saved theme: ${e.message}")
        }
    }

    // State for calendar title
    var calendarTitle by remember { mutableStateOf(savedTitle) }

    // State for selected mode
    var selectedMode by remember { mutableStateOf(savedMode) }
    
    // State for calendar view
    var selectedCalendarView by remember { mutableStateOf(localStorage.getItem("calendarView") ?: "week") }

    // State for settings tab
    var activeTab by remember { mutableStateOf("appearance") }

    // State for save notification
    var showSaveNotification by remember { mutableStateOf(false) }
    var notificationType by remember { mutableStateOf("success") }
    var notificationMessage by remember { mutableStateOf("") }

    // Active theme (selected or custom)
    val activeTheme =
        remember(selectedThemeIndex, customPrimaryColor, customSecondaryColor, customBackgroundColor) {
            if (selectedThemeIndex < themes.size) {
                themes[selectedThemeIndex]
            } else {
                CalendarTheme("Custom", customPrimaryColor, customSecondaryColor, customBackgroundColor)
            }
        }

    // Function to show notification
    fun showNotification(
        type: String,
        message: String,
    ) {
        notificationType = type
        notificationMessage = message
        showSaveNotification = true

        // Auto-hide notification after 3 seconds
        window.setTimeout({
            showSaveNotification = false
        }, 3000)
    }

    // Function to save settings
    fun saveSettings() {
        try {
            // Save theme
            val themeToSave =
                if (selectedThemeIndex < themes.size) {
                    themes[selectedThemeIndex]
                } else {
                    CalendarTheme("Custom", customPrimaryColor, customSecondaryColor, customBackgroundColor)
                }

            // Create a JSON object to save theme
            val themeObj = js("{}")
            themeObj["name"] = themeToSave.name
            themeObj["primaryColor"] = themeToSave.primaryColor
            themeObj["secondaryColor"] = themeToSave.secondaryColor
            themeObj["backgroundColor"] = themeToSave.backgroundColor

            // Save to localStorage
            localStorage.setItem("calendarTheme", JSON.stringify(themeObj))
            localStorage.setItem("calendarTitle", calendarTitle)
            localStorage.setItem("calendarMode", selectedMode)
            localStorage.setItem("calendarView", selectedCalendarView)

            showNotification("success", "Settings saved successfully!")
        } catch (e: Exception) {
            showNotification("error", "Failed to save settings: ${e.message}")
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .backgroundColor(
                org.jetbrains.compose.web.css
                    .Color(ThemeManager.Colors.background),
            ).padding(16.px),
    ) {
        // Top bar with dark mode toggle
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
            Button(
                attrs = {
                    onClick { ThemeManager.toggleDarkMode() }
                    style {
                        padding(8.px, 16.px)
                        backgroundColor(
                            org.jetbrains.compose.web.css.Color(
                                if (ThemeManager.isDarkMode) "#222" else "#eee",
                            ),
                        )
                        color(
                            org.jetbrains.compose.web.css.Color(
                                if (ThemeManager.isDarkMode) "#fff" else "#222",
                            ),
                        )
                        border(0.px)
                        borderRadius(6.px)
                        fontSize(14.px)
                        fontWeight(600)
                        cursor(Cursor.Pointer)
                    }
                },
            ) {
                Text(if (ThemeManager.isDarkMode) "☀️ Light Mode" else "🌙 Dark Mode")
            }
        }

        // Page Title
        Box(Modifier.fillMaxWidth().padding(bottom = 24.px)) {
            H1(
                attrs = {
                    style {
                        color(
                            org.jetbrains.compose.web.css
                                .Color(ThemeManager.Colors.text),
                        )
                        margin(0.px)
                    }
                },
            ) {
                Text("Settings")
            }
        }

        // Tabs for settings categories
        Box(Modifier.fillMaxWidth().padding(bottom = 16.px)) {
            Div(
                attrs = {
                    style {
                        display(DisplayStyle.Flex)
                        borderBottom(
                            1.px,
                            LineStyle.Solid,
                            org.jetbrains.compose.web.css
                                .Color(ThemeManager.Colors.border),
                        )
                    }
                },
            ) {
                listOf(
                    Pair("appearance", "Appearance"),
                    Pair("calendar", "Calendar"),
                    Pair("events", "Events"),
                ).forEach { (id, label) ->
                    Div(
                        attrs = {
                            onClick { activeTab = id }
                            style {
                                padding(12.px, 24.px)
                                cursor(Cursor.Pointer)
                                color(
                                    org.jetbrains.compose.web.css.Color(
                                        if (activeTab == id) activeTheme.primaryColor else ThemeManager.Colors.text,
                                    ),
                                )
                                fontWeight(if (activeTab == id) 600 else 400)
                                borderBottom(
                                    3.px,
                                    LineStyle.Solid,
                                    org.jetbrains.compose.web.css.Color(
                                        if (activeTab == id) activeTheme.primaryColor else "transparent",
                                    ),
                                )
                                property("transition", "all 0.2s ease")
                            }
                        },
                    ) {
                        Text(label)
                    }
                }
            }
        }

        // Settings content area
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.px)
                .backgroundColor(
                    org.jetbrains.compose.web.css
                        .Color(ThemeManager.Colors.calendarBackground),
                ).border(
                    1.px,
                    LineStyle.Solid,
                    org.jetbrains.compose.web.css
                        .Color(ThemeManager.Colors.border),
                ).borderRadius(8.px)
                .padding(24.px),
        ) {
            when (activeTab) {
                "appearance" -> {
                    // Theme Settings
                    Column(Modifier.fillMaxWidth().gap(24.px)) {
                        // Theme Selection
                        Div {
                            H3(
                                attrs = {
                                    style {
                                        margin(0.px, 0.px, 16.px, 0.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            ) {
                                Text("Theme Selection")
                            }

                            // Theme Grid - More compact with color previews
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Grid)
                                        property("grid-template-columns", "repeat(auto-fill, minmax(140px, 1fr))")
                                        gap(12.px)
                                        marginBottom(24.px)
                                    }
                                },
                            ) {
                                themes.forEachIndexed { index, theme ->
                                    Div(
                                        attrs = {
                                            onClick { 
                                                selectedThemeIndex = index
                                                // Apply theme immediately
                                                val themeObj = js("{}")
                                                themeObj["name"] = theme.name
                                                themeObj["primaryColor"] = theme.primaryColor
                                                themeObj["secondaryColor"] = theme.secondaryColor
                                                themeObj["backgroundColor"] = theme.backgroundColor
                                                localStorage.setItem("calendarTheme", JSON.stringify(themeObj))
                                                // Force refresh to apply theme
                                                window.location.reload()
                                            }
                                            style {
                                                border(
                                                    2.px,
                                                    LineStyle.Solid,
                                                    org.jetbrains.compose.web.css.Color(
                                                        if (selectedThemeIndex == index) theme.primaryColor else ThemeManager.Colors.border,
                                                    ),
                                                )
                                                borderRadius(8.px)
                                                padding(12.px)
                                                cursor(Cursor.Pointer)
                                                backgroundColor(
                                                    org.jetbrains.compose.web.css.Color(
                                                        if (selectedThemeIndex == index) {
                                                            if (ThemeManager.isDarkMode) "#2C2C2C" else "#f5f5f5"
                                                        } else {
                                                            ThemeManager.Colors.calendarBackground
                                                        },
                                                    ),
                                                )
                                                property("transition", "all 0.2s ease")
                                                property("min-height", "80px")
                                                display(DisplayStyle.Flex)
                                                flexDirection(FlexDirection.Column)
                                                property("align-items", "center")
                                                property("justify-content", "center")
                                            }
                                        },
                                    ) {
                                        // Compact color preview circles
                                        Div(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    property("justify-content", "center")
                                                    property("align-items", "center")
                                                    gap(8.px)
                                                    marginBottom(8.px)
                                                }
                                            }
                                        ) {
                                            // Primary color circle
                                            Div(
                                                attrs = {
                                                    style {
                                                        width(20.px)
                                                        height(20.px)
                                                        backgroundColor(
                                                            org.jetbrains.compose.web.css.Color(theme.primaryColor)
                                                        )
                                                        borderRadius(50.percent)
                                                        border(2.px, LineStyle.Solid, 
                                                            org.jetbrains.compose.web.css.Color(ThemeManager.Colors.border)
                                                        )
                                                    }
                                                }
                                            ) {}
                                            
                                            // Secondary color circle
                                            Div(
                                                attrs = {
                                                    style {
                                                        width(16.px)
                                                        height(16.px)
                                                        backgroundColor(
                                                            org.jetbrains.compose.web.css.Color(theme.secondaryColor)
                                                        )
                                                        borderRadius(50.percent)
                                                        border(2.px, LineStyle.Solid, 
                                                            org.jetbrains.compose.web.css.Color(ThemeManager.Colors.border)
                                                        )
                                                    }
                                                }
                                            ) {}
                                        }

                                        // Theme Name
                                        P(
                                            attrs = {
                                                style {
                                                    fontWeight(600)
                                                    fontSize(14.px)
                                                    color(
                                                        org.jetbrains.compose.web.css
                                                            .Color(ThemeManager.Colors.text),
                                                    )
                                                    margin(0.px)
                                                    textAlign(TextAlign.Center)
                                                }
                                            },
                                        ) {
                                            Text(theme.name)
                                        }
                                    }
                                }

                                // Custom Theme Option
                                Div(
                                    attrs = {
                                        onClick { selectedThemeIndex = themes.size }
                                        style {
                                            border(
                                                2.px,
                                                LineStyle.Solid,
                                                org.jetbrains.compose.web.css.Color(
                                                    if (selectedThemeIndex ==
                                                        themes.size
                                                    ) {
                                                        customPrimaryColor
                                                    } else {
                                                        ThemeManager.Colors.border
                                                    },
                                                ),
                                            )
                                            borderRadius(8.px)
                                            padding(16.px)
                                            cursor(Cursor.Pointer)
                                            backgroundColor(
                                                org.jetbrains.compose.web.css.Color(
                                                    if (selectedThemeIndex == themes.size) {
                                                        if (ThemeManager.isDarkMode) "#2C2C2C" else "#f5f5f5"
                                                    } else {
                                                        ThemeManager.Colors.calendarBackground
                                                    },
                                                ),
                                            )
                                            property("transition", "all 0.2s ease")
                                        }
                                    },
                                ) {
                                    // Custom Theme Preview
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(80.px)
                                            .backgroundColor(
                                                org.jetbrains.compose.web.css
                                                    .Color(customBackgroundColor),
                                            ).borderRadius(4.px)
                                            .margin(bottom = 12.px),
                                    ) {
                                        // Color bars representing the custom theme
                                        Div(
                                            attrs = {
                                                style {
                                                    width(100.percent)
                                                    height(24.px)
                                                    backgroundColor(
                                                        org.jetbrains.compose.web.css
                                                            .Color(customPrimaryColor),
                                                    )
                                                    marginBottom(8.px)
                                                    borderRadius(2.px)
                                                }
                                            },
                                        ) {}

                                        Div(
                                            attrs = {
                                                style {
                                                    width(75.percent)
                                                    height(16.px)
                                                    backgroundColor(
                                                        org.jetbrains.compose.web.css
                                                            .Color(customSecondaryColor),
                                                    )
                                                    borderRadius(2.px)
                                                }
                                            },
                                        ) {}
                                    }

                                    // Theme Name
                                    P(
                                        attrs = {
                                            style {
                                                fontWeight(600)
                                                color(
                                                    org.jetbrains.compose.web.css
                                                        .Color(ThemeManager.Colors.text),
                                                )
                                                margin(0.px)
                                                textAlign(TextAlign.Center)
                                            }
                                        },
                                    ) {
                                        Text("Custom")
                                    }
                                }
                            }
                        }

                        // Custom Colors (only shown if Custom is selected)
                        if (selectedThemeIndex == themes.size) {
                            Div {
                                H3(
                                    attrs = {
                                        style {
                                            margin(0.px, 0.px, 16.px, 0.px)
                                            color(
                                                org.jetbrains.compose.web.css
                                                    .Color(ThemeManager.Colors.text),
                                            )
                                        }
                                    },
                                ) {
                                    Text("Customize Colors")
                                }

                                // Color Picker Grid
                                Div(
                                    attrs = {
                                        style {
                                            display(DisplayStyle.Grid)
                                            property("grid-template-columns", "repeat(auto-fit, minmax(250px, 1fr))")
                                            gap(24.px)
                                        }
                                    },
                                ) {
                                    // Primary Color
                                    Div {
                                        Label(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Block)
                                                    marginBottom(8.px)
                                                    color(
                                                        org.jetbrains.compose.web.css
                                                            .Color(ThemeManager.Colors.text),
                                                    )
                                                }
                                            },
                                        ) {
                                            Text("Primary Color")
                                        }

                                        Div(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    gap(12.px)
                                                    alignItems("center")
                                                }
                                            },
                                        ) {
                                            // Color Preview
                                            Div(
                                                attrs = {
                                                    style {
                                                        width(36.px)
                                                        height(36.px)
                                                        backgroundColor(
                                                            org.jetbrains.compose.web.css
                                                                .Color(customPrimaryColor),
                                                        )
                                                        borderRadius(4.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                    }
                                                },
                                            ) {}

                                            // Color Input
                                            Input(
                                                type = InputType.Color,
                                                attrs = {
                                                    value(customPrimaryColor)
                                                    onInput { customPrimaryColor = it.value }
                                                    style {
                                                        width(40.px)
                                                        height(40.px)
                                                        padding(0.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                        borderRadius(4.px)
                                                        cursor(Cursor.Pointer)
                                                    }
                                                },
                                            )

                                            // Color Text Input
                                            Input(
                                                type = InputType.Text,
                                                attrs = {
                                                    value(customPrimaryColor)
                                                    onInput { customPrimaryColor = it.value }
                                                    style {
                                                        width(100.px)
                                                        padding(8.px, 12.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                        borderRadius(4.px)
                                                        fontSize(14.px)
                                                    }
                                                },
                                            )
                                        }
                                    }

                                    // Secondary Color
                                    Div {
                                        Label(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Block)
                                                    marginBottom(8.px)
                                                    color(
                                                        org.jetbrains.compose.web.css
                                                            .Color(ThemeManager.Colors.text),
                                                    )
                                                }
                                            },
                                        ) {
                                            Text("Secondary Color")
                                        }

                                        Div(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    gap(12.px)
                                                    alignItems("center")
                                                }
                                            },
                                        ) {
                                            // Color Preview
                                            Div(
                                                attrs = {
                                                    style {
                                                        width(36.px)
                                                        height(36.px)
                                                        backgroundColor(
                                                            org.jetbrains.compose.web.css
                                                                .Color(customSecondaryColor),
                                                        )
                                                        borderRadius(4.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                    }
                                                },
                                            ) {}

                                            // Color Input
                                            Input(
                                                type = InputType.Color,
                                                attrs = {
                                                    value(customSecondaryColor)
                                                    onInput { customSecondaryColor = it.value }
                                                    style {
                                                        width(40.px)
                                                        height(40.px)
                                                        padding(0.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                        borderRadius(4.px)
                                                        cursor(Cursor.Pointer)
                                                    }
                                                },
                                            )

                                            // Color Text Input
                                            Input(
                                                type = InputType.Text,
                                                attrs = {
                                                    value(customSecondaryColor)
                                                    onInput { customSecondaryColor = it.value }
                                                    style {
                                                        width(100.px)
                                                        padding(8.px, 12.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                        borderRadius(4.px)
                                                        fontSize(14.px)
                                                    }
                                                },
                                            )
                                        }
                                    }

                                    // Background Color
                                    Div {
                                        Label(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Block)
                                                    marginBottom(8.px)
                                                    color(
                                                        org.jetbrains.compose.web.css
                                                            .Color(ThemeManager.Colors.text),
                                                    )
                                                }
                                            },
                                        ) {
                                            Text("Background Color")
                                        }

                                        Div(
                                            attrs = {
                                                style {
                                                    display(DisplayStyle.Flex)
                                                    gap(12.px)
                                                    alignItems("center")
                                                }
                                            },
                                        ) {
                                            // Color Preview
                                            Div(
                                                attrs = {
                                                    style {
                                                        width(36.px)
                                                        height(36.px)
                                                        backgroundColor(
                                                            org.jetbrains.compose.web.css
                                                                .Color(customBackgroundColor),
                                                        )
                                                        borderRadius(4.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                    }
                                                },
                                            ) {}

                                            // Color Input
                                            Input(
                                                type = InputType.Color,
                                                attrs = {
                                                    value(customBackgroundColor)
                                                    onInput { customBackgroundColor = it.value }
                                                    style {
                                                        width(40.px)
                                                        height(40.px)
                                                        padding(0.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                        borderRadius(4.px)
                                                        cursor(Cursor.Pointer)
                                                    }
                                                },
                                            )

                                            // Color Text Input
                                            Input(
                                                type = InputType.Text,
                                                attrs = {
                                                    value(customBackgroundColor)
                                                    onInput { customBackgroundColor = it.value }
                                                    style {
                                                        width(100.px)
                                                        padding(8.px, 12.px)
                                                        border(
                                                            1.px,
                                                            LineStyle.Solid,
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.border),
                                                        )
                                                        borderRadius(4.px)
                                                        fontSize(14.px)
                                                    }
                                                },
                                            )
                                        }
                                    }
                                }

                                // Theme preview
                                Div(
                                    attrs = {
                                        style {
                                            marginTop(24.px)
                                            padding(16.px)
                                            backgroundColor(
                                                org.jetbrains.compose.web.css
                                                    .Color(customBackgroundColor),
                                            )
                                            borderRadius(8.px)
                                            border(
                                                1.px,
                                                LineStyle.Solid,
                                                org.jetbrains.compose.web.css
                                                    .Color(ThemeManager.Colors.border),
                                            )
                                        }
                                    },
                                ) {
                                    H4(
                                        attrs = {
                                            style {
                                                margin(0.px, 0.px, 8.px, 0.px)
                                                color(
                                                    org.jetbrains.compose.web.css
                                                        .Color(customPrimaryColor),
                                                )
                                            }
                                        },
                                    ) {
                                        Text("Theme Preview")
                                    }

                                    P(
                                        attrs = {
                                            style {
                                                margin(0.px, 0.px, 16.px, 0.px)
                                                color(
                                                    org.jetbrains.compose.web.css
                                                        .Color("#333"),
                                                )
                                            }
                                        },
                                    ) {
                                        Text("This is how your custom theme will look.")
                                    }

                                    Button(
                                        attrs = {
                                            style {
                                                backgroundColor(
                                                    org.jetbrains.compose.web.css
                                                        .Color(customPrimaryColor),
                                                )
                                                color(Colors.White)
                                                border(0.px)
                                                borderRadius(4.px)
                                                padding(8.px, 16.px)
                                                marginRight(8.px)
                                            }
                                        },
                                    ) {
                                        Text("Primary Button")
                                    }

                                    Button(
                                        attrs = {
                                            style {
                                                backgroundColor(
                                                    org.jetbrains.compose.web.css
                                                        .Color(customSecondaryColor),
                                                )
                                                color(Colors.White)
                                                border(0.px)
                                                borderRadius(4.px)
                                                padding(8.px, 16.px)
                                            }
                                        },
                                    ) {
                                        Text("Secondary Button")
                                    }
                                }
                            }
                        }
                    }
                }

                "calendar" -> {
                    // Calendar Settings
                    Column(Modifier.fillMaxWidth().gap(24.px)) {
                        // Calendar Title Setting
                        Div {
                            H3(
                                attrs = {
                                    style {
                                        margin(0.px, 0.px, 16.px, 0.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            ) {
                                Text("Calendar Title")
                            }

                            Input(
                                type = InputType.Text,
                                attrs = {
                                    value(calendarTitle)
                                    onInput { calendarTitle = it.value }
                                    style {
                                        width(100.percent)
                                        maxWidth(400.px)
                                        padding(12.px)
                                        border(
                                            1.px,
                                            LineStyle.Solid,
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.border),
                                        )
                                        borderRadius(4.px)
                                        fontSize(16.px)
                                        backgroundColor(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.calendarBackground),
                                        )
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            )

                            P(
                                attrs = {
                                    style {
                                        fontSize(14.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.secondaryText),
                                        )
                                        margin(8.px, 0.px, 0.px, 0.px)
                                    }
                                },
                            ) {
                                Text("This title will be displayed at the top of your calendar.")
                            }
                        }

                        // Time Format Setting
                        Div {
                            H3(
                                attrs = {
                                    style {
                                        margin(0.px, 0.px, 16.px, 0.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            ) {
                                Text("Calendar View Options")
                            }

                            // View Options as radio buttons
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexDirection(FlexDirection.Column)
                                        gap(12.px)
                                    }
                                },
                            ) {
                                // These are placeholders since we don't have this functionality yet
                                listOf(
                                    Pair("week", "Week View (7 days)"),
                                    Pair("month", "Month View (Calendar grid)"),
                                ).forEach { (value, label) ->
                                    Div(
                                        attrs = {
                                            style {
                                                display(DisplayStyle.Flex)
                                                alignItems("center")
                                                gap(8.px)
                                            }
                                        },
                                    ) {
                                        Input(
                                            type = InputType.Radio,
                                            attrs = {
                                                name("calendarView")
                                                value(value)
                                                checked(value == selectedCalendarView)
                                                onChange { 
                                                    selectedCalendarView = value
                                                    saveSettings()
                                                }
                                                style {
                                                    cursor(Cursor.Pointer)
                                                }
                                            },
                                        )

                                        Label(
                                            attrs = {
                                                style {
                                                    color(
                                                        org.jetbrains.compose.web.css
                                                            .Color(ThemeManager.Colors.text),
                                                    )
                                                    cursor(Cursor.Pointer)
                                                }
                                            },
                                        ) {
                                            Text(label)
                                        }
                                    }
                                }
                            }

                            P(
                                attrs = {
                                    style {
                                        fontSize(14.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.secondaryText),
                                        )
                                        margin(8.px, 0.px, 0.px, 0.px)
                                    }
                                },
                            ) {
                                Text("Select your preferred calendar view. Changes are saved automatically.")
                            }
                        }

                        // Time Display Setting
                        Div {
                            H3(
                                attrs = {
                                    style {
                                        margin(0.px, 0.px, 16.px, 0.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            ) {
                                Text("Time Format")
                            }

                            // Time format options
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Flex)
                                        flexDirection(FlexDirection.Column)
                                        gap(12.px)
                                    }
                                },
                            ) {
                                listOf(
                                    Pair("12h", "12-hour (1:00 PM)"),
                                    Pair("24h", "24-hour (13:00)"),
                                ).forEach { (value, label) ->
                                    Div(
                                        attrs = {
                                            style {
                                                display(DisplayStyle.Flex)
                                                alignItems("center")
                                                gap(8.px)
                                            }
                                        },
                                    ) {
                                        Input(
                                            type = InputType.Radio,
                                            attrs = {
                                                name("timeFormat")
                                                value(value)
                                                checked(value == "12h") // Default to 12h format
                                                style {
                                                    cursor(Cursor.Pointer)
                                                }
                                            },
                                        )

                                        Label(
                                            attrs = {
                                                style {
                                                    color(
                                                        org.jetbrains.compose.web.css
                                                            .Color(ThemeManager.Colors.text),
                                                    )
                                                    cursor(Cursor.Pointer)
                                                }
                                            },
                                        ) {
                                            Text(label)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                "events" -> {
                    // Event Settings
                    Column(Modifier.fillMaxWidth().gap(24.px)) {
                        // Default Event Mode
                        Div {
                            H3(
                                attrs = {
                                    style {
                                        margin(0.px, 0.px, 16.px, 0.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            ) {
                                Text("Default Event Mode")
                            }

                            // Mode options
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Flex)
                                        gap(12.px)
                                        flexWrap(FlexWrap.Wrap)
                                    }
                                },
                            ) {
                                listOf("Passive", "Active", "Custom").forEach { mode ->
                                    Button(
                                        attrs = {
                                            onClick { selectedMode = mode }
                                            style {
                                                padding(12.px, 20.px)
                                                backgroundColor(
                                                    org.jetbrains.compose.web.css.Color(
                                                        if (selectedMode ==
                                                            mode
                                                        ) {
                                                            activeTheme.primaryColor
                                                        } else {
                                                            ThemeManager.Colors.buttonBackground
                                                        },
                                                    ),
                                                )
                                                color(
                                                    org.jetbrains.compose.web.css.Color(
                                                        if (selectedMode == mode) "#fff" else ThemeManager.Colors.text,
                                                    ),
                                                )
                                                border(0.px)
                                                borderRadius(6.px)
                                                cursor(Cursor.Pointer)
                                                property("transition", "all 0.2s ease")
                                            }
                                        },
                                    ) {
                                        Text(mode)
                                    }
                                }
                            }

                            // Mode descriptions
                            Box(Modifier.padding(top = 16.px)) {
                                Div(
                                    attrs = {
                                        style {
                                            padding(16.px)
                                            backgroundColor(
                                                org.jetbrains.compose.web.css.Color(
                                                    if (ThemeManager.isDarkMode) "#2c2c2c" else "#f5f5f5",
                                                ),
                                            )
                                            borderRadius(6.px)
                                        }
                                    },
                                ) {
                                    when (selectedMode) {
                                        "Passive" -> {
                                            P(
                                                attrs = {
                                                    style {
                                                        margin(0.px)
                                                        color(
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.text),
                                                        )
                                                    }
                                                },
                                            ) {
                                                Text(
                                                    "Passive Mode: Events are for reference only. The calendar will not send reminders or enforce schedules.",
                                                )
                                            }
                                        }
                                        "Active" -> {
                                            P(
                                                attrs = {
                                                    style {
                                                        margin(0.px)
                                                        color(
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.text),
                                                        )
                                                    }
                                                },
                                            ) {
                                                Text("Active Mode: The calendar will send reminders and help you stick to your schedule.")
                                            }
                                        }
                                        else -> {
                                            P(
                                                attrs = {
                                                    style {
                                                        margin(0.px)
                                                        color(
                                                            org.jetbrains.compose.web.css
                                                                .Color(ThemeManager.Colors.text),
                                                        )
                                                    }
                                                },
                                            ) {
                                                Text(
                                                    "Custom Mode: You'll choose whether each event is passive or active when you create it.",
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Event Default Duration
                        Div {
                            H3(
                                attrs = {
                                    style {
                                        margin(0.px, 0.px, 16.px, 0.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.text),
                                        )
                                    }
                                },
                            ) {
                                Text("Default Event Duration")
                            }

                            // Duration slider (placeholder)
                            Div(
                                attrs = {
                                    style {
                                        display(DisplayStyle.Flex)
                                        alignItems("center")
                                        gap(16.px)
                                    }
                                },
                            ) {
                                Input(
                                    type = InputType.Range,
                                    attrs = {
                                        attr("min", "0")
                                        attr("max", "180")
                                        attr("step", "15")
                                        attr("value", "60")
                                        style {
                                            width(200.px)
                                        }
                                    },
                                )

                                Span(
                                    attrs = {
                                        style {
                                            color(
                                                org.jetbrains.compose.web.css
                                                    .Color(ThemeManager.Colors.text),
                                            )
                                        }
                                    },
                                ) {
                                    Text("60 minutes")
                                }
                            }

                            P(
                                attrs = {
                                    style {
                                        fontSize(14.px)
                                        color(
                                            org.jetbrains.compose.web.css
                                                .Color(ThemeManager.Colors.secondaryText),
                                        )
                                        margin(8.px, 0.px, 0.px, 0.px)
                                    }
                                },
                            ) {
                                Text("Set the default duration for newly created events.")
                            }
                        }
                    }
                }
            }
        }

        // Save Button Area
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = 16.px)
                .display(DisplayStyle.Flex)
                .styleModifier {
                    property("justify-content", "space-between")
                    property("align-items", "center")
                },
        ) {
            // Back to Calendar button
            Link(
                path = "/",
                modifier =
                    Modifier
                        .padding(12.px, 24.px)
                        .styleModifier {
                            backgroundColor(
                                org.jetbrains.compose.web.css
                                    .Color(ThemeManager.Colors.buttonBackground),
                            )
                            color(
                                org.jetbrains.compose.web.css
                                    .Color(ThemeManager.Colors.text),
                            )
                        }.border(0.px)
                        .borderRadius(6.px)
                        .fontSize(16.px)
                        .fontWeight(FontWeight.Medium)
                        .textDecorationLine(TextDecorationLine.None)
                        .cursor(Cursor.Pointer),
            ) {
                Text("← Back to Calendar")
            }

            // Save Button
            Button(
                attrs = {
                    onClick { saveSettings() }
                    style {
                        padding(12.px, 24.px)
                        backgroundColor(
                            org.jetbrains.compose.web.css
                                .Color(activeTheme.primaryColor),
                        )
                        color(Colors.White)
                        border(0.px)
                        borderRadius(6.px)
                        fontSize(16.px)
                        fontWeight(500)
                        cursor(Cursor.Pointer)
                    }
                },
            ) {
                Text("Save Settings")
            }
        }

        // Notification banner
        if (showSaveNotification) {
            Box(
                Modifier
                    .position(Position.Fixed)
                    .bottom(20.px)
                    .right(20.px)
                    .padding(16.px)
                    .backgroundColor(
                        org.jetbrains.compose.web.css.Color(
                            if (notificationType == "success") "#4caf50" else "#f44336",
                        ),
                    ).borderRadius(6.px)
                    .zIndex(1000),
            ) {
                Text(notificationMessage)
            }
        }
    }
}
