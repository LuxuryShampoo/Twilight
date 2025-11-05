package xyz.malefic.staticsite.components.layouts

import androidx.compose.runtime.Composable
import com.varabyte.kobweb.compose.foundation.layout.Box
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.modifiers.fillMaxSize
import com.varabyte.kobweb.core.layout.Layout

@Layout
@Composable
fun NavBarLayout(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
        content()
    }
}
