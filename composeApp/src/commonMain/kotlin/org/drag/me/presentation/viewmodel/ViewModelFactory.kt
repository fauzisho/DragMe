package org.drag.me.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.drag.me.data.repository.ApiBuildingBlockRepository
import org.drag.me.data.repository.BuildingBlockRepository
import org.drag.me.data.repository.InMemoryBuildingBlockRepository
import kotlin.reflect.KClass

class ViewModelFactory(
    private val useApi: Boolean = true
) {
    
    @Suppress("UNCHECKED_CAST")
    fun <T : ViewModel> create(modelClass: KClass<T>): T {
        if (modelClass == DragAndDropViewModel::class) {
            val repository: BuildingBlockRepository = if (useApi) {
                ApiBuildingBlockRepository()
            } else {
                InMemoryBuildingBlockRepository()
            }
            
            return DragAndDropViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
