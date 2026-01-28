package com.elitesports17.wizardlive.ui.chat

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ViewersViewModel : ViewModel() {

    private val _viewers =
        MutableStateFlow<Map<String, Int>>(emptyMap())

    val viewers = _viewers.asStateFlow()

    fun update(room: String, count: Int) {
        _viewers.value = _viewers.value.toMutableMap().apply {
            put(room, count)
        }
    }
}
