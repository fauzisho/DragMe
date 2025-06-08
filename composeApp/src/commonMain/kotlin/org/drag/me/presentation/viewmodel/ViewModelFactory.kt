package org.drag.me.presentation.viewmodel

import androidx.lifecycle.ViewModel
import org.drag.me.data.repository.BuildingBlockRepository
import org.drag.me.data.repository.InMemoryBuildingBlockRepository
import kotlin.reflect.KClass

class ViewModelFactory {
    
    @Suppress("UNCHECKED_CAST")
    fun <T : ViewModel> create(modelClass: KClass<T>): T {
        if (modelClass == DragAndDropViewModel::class) {
            // Using in-memory repository for custom blocks
            val repository: BuildingBlockRepository = InMemoryBuildingBlockRepository()
            return DragAndDropViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
