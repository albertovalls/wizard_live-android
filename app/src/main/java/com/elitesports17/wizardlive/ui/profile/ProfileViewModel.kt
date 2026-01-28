package com.elitesports17.wizardlive.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elitesports17.wizardlive.data.remote.ApiService

import com.elitesports17.wizardlive.data.model.FollowersResponse
import com.elitesports17.wizardlive.data.model.MyChannelResponse
import com.elitesports17.wizardlive.data.model.ProfileMeResponse
import com.elitesports17.wizardlive.ui.util.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class FollowersUiState {
    object Loading : FollowersUiState()
    data class Success(val data: FollowersResponse) : FollowersUiState()
    data class Error(val message: String) : FollowersUiState()
}

sealed class SubscriptionsUiState {
    object Loading : SubscriptionsUiState()
    data class Success(val count: Int) : SubscriptionsUiState()
    data class Error(val message: String) : SubscriptionsUiState()
}


sealed class ChannelUiState {
    object Loading : ChannelUiState()
    data class Success(val data: MyChannelResponse) : ChannelUiState()
    data class Error(val message: String) : ChannelUiState()
}

sealed class ViewerProfileUiState {
    object Idle : ViewerProfileUiState()
    object Loading : ViewerProfileUiState()
    data class Success(val data: ProfileMeResponse) : ViewerProfileUiState()
    data class Error(val message: String) : ViewerProfileUiState()
}


class ProfileViewModel(
    private val api: ApiService
) : ViewModel() {

    private val _followersState =
        MutableStateFlow<FollowersUiState>(FollowersUiState.Loading)

    val followersState: StateFlow<FollowersUiState> = _followersState

    private val _subscriptionsState =
        MutableStateFlow<SubscriptionsUiState>(SubscriptionsUiState.Loading)

    val subscriptionsState: StateFlow<SubscriptionsUiState> = _subscriptionsState


    private val _channelState =
        MutableStateFlow<ChannelUiState>(ChannelUiState.Loading)

    val channelState: StateFlow<ChannelUiState> = _channelState

    private val _viewerProfileState =
        MutableStateFlow<ViewerProfileUiState>(ViewerProfileUiState.Idle)

    val viewerProfileState = _viewerProfileState.asStateFlow()

    fun loadFollowers(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getMyFollowers("Bearer $token")
                _followersState.value = FollowersUiState.Success(response)
            } catch (e: Exception) {
                _followersState.value =
                    FollowersUiState.Error("Failed to load followers")
            }
        }
    }


    fun loadSubscriptions(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getMySubscriptions("Bearer $token")
                _subscriptionsState.value =
                    SubscriptionsUiState.Success(response.count)
            } catch (e: Exception) {
                _subscriptionsState.value =
                    SubscriptionsUiState.Error("Failed to load subscriptions")
            }
        }
    }

    fun loadMyChannel(token: String) {
        viewModelScope.launch {
            try {
                val response = api.getMyChannel("Bearer $token")
                _channelState.value = ChannelUiState.Success(response)
            } catch (e: Exception) {
                _channelState.value = ChannelUiState.Error("Failed to load channel")
            }
        }
    }

    fun loadViewerProfile(token: String) {
        viewModelScope.launch {
            _viewerProfileState.value = ViewerProfileUiState.Loading
            try {
                val response = api.getMyProfile("Bearer $token")
                _viewerProfileState.value = ViewerProfileUiState.Success(response)
            } catch (e: Exception) {
                _viewerProfileState.value =
                    ViewerProfileUiState.Error(e.message ?: "Unknown error")
            }
        }
    }



}
