package xyz.malefic.staticsite.components.layouts

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.varabyte.kobweb.compose.css.Background
import com.varabyte.kobweb.compose.css.BackgroundImage
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.css.Transition
import com.varabyte.kobweb.compose.css.WhiteSpace
import com.varabyte.kobweb.compose.css.functions.LinearGradient
import com.varabyte.kobweb.compose.css.functions.linearGradient
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.foundation.layout.Column
import com.varabyte.kobweb.compose.foundation.layout.Row
import com.varabyte.kobweb.compose.ui.Alignment
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.compose.ui.styleModifier
import com.varabyte.kobweb.core.layout.Layout
import com.varabyte.kobweb.core.rememberPageContext
import com.varabyte.kobweb.silk.components.navigation.Link
import com.varabyte.kobweb.silk.style.CssStyle
import com.varabyte.kobweb.silk.style.base
import com.varabyte.kobweb.silk.style.selectors.hover
import com.varabyte.kobweb.silk.style.toModifier
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import xyz.malefic.staticsite.util.Pages
import com.varabyte.kobweb.compose.ui.graphics.Color as Kolor

// Silk-themed styles
val NavBarStyle =
    CssStyle.base {
        Modifier
            .fillMaxWidth()
            .height(60.px)
            .background(
                Background.of(
                    BackgroundImage.of(
                        linearGradient(
                            LinearGradient.Direction.ToRight,
                        ) {
                            add(Color("#f8f9fa"), 0.percent)
                            add(Color("#e9ecef"), 50.percent)
                            add(Color("#dee2e6"), 100.percent)
                        },
                    ),
                ),
            ).boxShadow(0.px, 2.px, 4.px, color = Kolor.rgba(0f, 0f, 0f, 0.1f))
            .borderBottom(1.px, LineStyle.Solid, Color("#dee2e6"))
    }

val NavItemStyle =
    Modifier
        .padding(12.px, 20.px)
        .margin(0.px, 4.px)
        .borderRadius(6.px)
        .styleModifier {
            textDecoration("none")
        }.color(Color("#495057"))
        .fontSize(14.px)
        .fontWeight(500)
        .transition(Transition.all(0.2.s))
        .whiteSpace(WhiteSpace.NoWrap)

val NavItemHoverStyle =
    CssStyle {
        base {
            NavItemStyle
        }

        hover {
            Modifier
                .background(Kolor.rgba(108f, 117f, 125f, 0.1f))
                .color(Color("#212529"))
                .translateY((-1).px)
        }
    }

val ActiveNavItemStyle =
    CssStyle.base {
        NavItemStyle
            .background(Kolor.rgba(13f, 110f, 253f, 0.1f))
            .color(Color("#0d6efd"))
            .fontWeight(600)
    }

val DropdownStyle =
    CssStyle.base {
        Modifier
            .position(Position.Relative)
            .display(DisplayStyle.InlineBlock)
    }

val DropdownContentStyle =
    CssStyle.base {
        Modifier
            .position(Position.Absolute)
            .top(100.percent)
            .right(0.px)
            .background(Colors.White)
            .minWidth(180.px)
            .boxShadow(0.px, 8.px, 16.px, color = Kolor.rgba(0f, 0f, 0f, 0.15f))
            .borderRadius(8.px)
            .border(1.px, LineStyle.Solid, Color("#dee2e6"))
            .zIndex(1000)
            .padding(8.px, 0.px)
    }

val DropdownItemStyle =
    Modifier
        .display(DisplayStyle.Block)
        .padding(10.px, 16.px)
        .styleModifier {
            textDecoration("none")
        }.color(Color("#495057"))
        .fontSize(14.px)
        .transition(Transition.of("background-color", 0.15.s))
        .whiteSpace(WhiteSpace.NoWrap)

val DropdownItemHoverStyle =
    CssStyle {
        base {
            DropdownItemStyle
        }

        hover {
            Modifier.background(Color("#f8f9fa"))
        }
    }

val DropdownButtonStyle =
    Modifier
        .padding(12.px, 16.px)
        .margin(0.px, 4.px)
        .borderRadius(6.px)
        .background(Colors.Transparent)
        .border(1.px, LineStyle.Solid, Color("#dee2e6"))
        .color(Color("#495057"))
        .fontSize(14.px)
        .fontWeight(500)
        .cursor(Cursor.Pointer)
        .transition(Transition.all(0.2.s))
        .styleModifier {
            property("white-space", "nowrap")
        }

val DropdownButtonHoverStyle =
    CssStyle {
        base {
            DropdownButtonStyle
        }

        hover {
            Modifier
                .background(Kolor.rgba(108f, 117f, 125f, 0.1f))
                .border {
                    color(Color("#adb5bd"))
                }
        }
    }

@Layout
@Composable
fun NavBarLayout(content: @Composable () -> Unit) {
    val ctx = rememberPageContext()
    val currentRoute = ctx.route.path
    var isDropdownOpen by remember { mutableStateOf(false) }

    // Configuration: Maximum number of pages to show before overflow
    val maxVisiblePages = 4
    val allPages = Pages.entries
    val visiblePages = allPages.take(maxVisiblePages)
    val overflowPages = allPages.drop(maxVisiblePages)

    Column(Modifier.fillMaxSize()) {
        // Navigation Bar
        Box(
            NavBarStyle.toModifier(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .maxWidth(1200.px)
                    .padding(0.px, 20.px),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Brand/Logo area (optional)
                Box(Modifier.flexGrow(1)) {
                    // Developers can customize this area
                }

                // Navigation Links
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visible navigation items
                    visiblePages.forEach { page ->
                        val isActive = page.isCurrentPage(currentRoute)
                        val pageRoute = page.route

                        Link(
                            path = pageRoute,
                            modifier =
                                if (isActive) {
                                    ActiveNavItemStyle.toModifier()
                                } else {
                                    NavItemHoverStyle.toModifier()
                                },
                        ) {
                            Text(page.value)
                        }
                    }

                    // Overflow dropdown (only show if there are overflow pages)
                    if (overflowPages.isNotEmpty()) {
                        Box(DropdownStyle.toModifier()) {
                            // Dropdown button
                            Div(
                                attrs = {
                                    onClick { isDropdownOpen = !isDropdownOpen }
                                },
                            ) {
                                Box(
                                    DropdownButtonHoverStyle.toModifier(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("More")
                                        // Simple arrow indicator
                                        Box(
                                            Modifier
                                                .margin(left = 8.px)
                                                .fontSize(10.px),
                                        ) {
                                            Text(if (isDropdownOpen) "▲" else "▼")
                                        }
                                    }
                                }
                            }

                            // Dropdown content
                            if (isDropdownOpen) {
                                Box(DropdownContentStyle.toModifier()) {
                                    Column {
                                        overflowPages.forEach { page ->
                                            val isActive = page.isCurrentPage(currentRoute)
                                            val pageRoute = page.route

                                            Link(
                                                path = pageRoute,
                                                modifier =
                                                    if (isActive) {
                                                        DropdownItemStyle
                                                            .background(Kolor.rgba(13f, 110f, 253f, 0.1f))
                                                            .color(Color("#0d6efd"))
                                                            .fontWeight(600)
                                                    } else {
                                                        DropdownItemHoverStyle.toModifier()
                                                    },
                                            ) {
                                                Text(page.value)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Page content
        Box(Modifier.fillMaxSize()) {
            content()
        }
    }

    // Close dropdown when clicking outside
    LaunchedEffect(isDropdownOpen) {
        if (isDropdownOpen) {
            // You might want to add a click outside listener here
            // For simplicity, we'll just auto-close after a delay
            kotlinx.coroutines.delay(5000)
            isDropdownOpen = false
        }
    }
}

// Helper functions
private fun Pages.isCurrentPage(currentRoute: String): Boolean =
    when (this) {
        Pages.INDEX -> currentRoute == "" || currentRoute == "/"
        else -> currentRoute == route
    }
