package com.sparktobloom.receipts.viewModel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparktobloom.receipts.model.ReceiptUserSingleton
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.RequestStatus
import kotlinx.coroutines.launch

class ChatActivityViewModel(
    private val sparkRepo: SparkRepository,
    val application: Application
) : ViewModel() {
    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<String> = MutableLiveData()
    private var serverMessage: MutableLiveData<String> = MutableLiveData()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getErrorMessage(): MutableLiveData<String> = errorMessage
    fun getServerMessage(): MutableLiveData<String> = serverMessage

    fun chat(message: String) {
        val user = ReceiptUserSingleton.user

        if (user != null) {
            viewModelScope.launch {
                try {
                    sparkRepo.chat(user.accessToken, user.refreshToken, message).collect {
                        when (it) {
                            is RequestStatus.Waiting -> {
                                isLoading.value = true
                            }

                            is RequestStatus.Success<*> -> {
                                isLoading.value = false
                                serverMessage.value = it.data as String
                            }

                            is RequestStatus.Error -> {
                                isLoading.value = false
                                errorMessage.value = it.message
                            }
                        }
                    }
                } catch (e: Exception) {
                    isLoading.value = false
                    errorMessage.value = e.message
                }
            }
        }
    }
}