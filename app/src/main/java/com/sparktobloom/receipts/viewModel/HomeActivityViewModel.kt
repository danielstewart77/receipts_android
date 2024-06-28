package com.sparktobloom.receipts.viewModel

import android.app.Application
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparktobloom.receipts.model.ReceiptUserSingleton
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.RequestStatus
import kotlinx.coroutines.launch

class HomeActivityViewModel(
    private val sparkRepo: SparkRepository,
    val application: Application
) : ViewModel() {

    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<String> = MutableLiveData()
    private var authStatus: MutableLiveData<Boolean?> =
        MutableLiveData<Boolean?>().apply { value = null }
    fun getErrorMessage(): MutableLiveData<String> = errorMessage
    fun getAuthenticatedStatus(): MutableLiveData<Boolean?> = authStatus

    fun checkAuth() {
        viewModelScope.launch {
            try {
                val user = ReceiptUserSingleton.user
                if (user != null) {
                    sparkRepo.authenticated(user.accessToken, user.refreshToken).collect {
                        when (it) {
                            is RequestStatus.Waiting -> {
                                isLoading.value = true
                            }

                            is RequestStatus.Success<*> -> {
                                isLoading.value = false
                                authStatus.value = it.data as Boolean
                            }

                            is RequestStatus.Error -> {
                                isLoading.value = false
                                errorMessage.value = it.message
                                authStatus.value = false
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                isLoading.value = false
                errorMessage.value = e.message.toString()
                authStatus.value = false
            }
        }
    }
}