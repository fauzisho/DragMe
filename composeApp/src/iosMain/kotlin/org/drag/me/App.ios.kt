package org.drag.me

import androidx.compose.runtime.Composable
import org.drag.me.presentation.viewmodel.ViewModelFactory

@Composable
actual fun AppWithViewModel() {
    val viewModelFactory = ViewModelFactory()
    AppContent(viewModelFactory)
}
