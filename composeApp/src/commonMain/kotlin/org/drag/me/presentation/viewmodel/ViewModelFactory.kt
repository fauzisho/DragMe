package org.drag.me.presentation.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.drag.me.data.repository.ApiBuildingBlockRepository
import org.drag.me.data.repository.BuildingBlockRepository
import org.drag.me.data.repository.InMemoryBuildingBlockRepository
import kotlin.reflect.KClass

class ViewModelFactory(
    private val useApi: Boolean = true,
    private val coroutineScope: CoroutineScope? = null
) {
    
    @Suppress("UNCHECKED_CAST")
    fun <T : ViewModel> create(modelClass: KClass<T>): T {
        if (modelClass == DragAndDropViewModel::class) {
            val repository: BuildingBlockRepository = if (useApi) {
                val apiRepository = ApiBuildingBlockRepository()
                // Load initial data from API
                coroutineScope?.launch {
                    apiRepository.loadBlocks()
                }
                apiRepository
            } else {
                InMemoryBuildingBlockRepository()
            }
            
            return DragAndDropViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.simpleName}")
    }
}
