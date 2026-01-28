package com.elitesports17.wizardlive.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.elitesports17.wizardlive.data.remote.ApiService

class ProfileViewModelFactory(
    private val api: ApiService
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(api) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
