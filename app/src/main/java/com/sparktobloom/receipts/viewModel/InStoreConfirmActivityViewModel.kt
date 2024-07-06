package com.sparktobloom.receipts.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparktobloom.receipts.data.InStoreItem
import com.sparktobloom.receipts.model.ConfirmationItem
import com.sparktobloom.receipts.model.ReceiptUser
import com.sparktobloom.receipts.model.ReceiptUserSingleton
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.RequestStatus
import kotlinx.coroutines.launch

class InStoreConfirmActivityViewModel(
    private val sparkRepo: SparkRepository,
    val application: Application
) : ViewModel() {
    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var isConfirmed: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<String> = MutableLiveData()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getIsConfirmed(): MutableLiveData<Boolean> = isConfirmed
    fun getErrorMessage(): MutableLiveData<String> = errorMessage

    fun submitConfirmation() {
        viewModelScope.launch {
            try {
                val accessToken = ReceiptUserSingleton.user?.accessToken
                val refreshToken = ReceiptUserSingleton.user?.refreshToken
                val item = ConfirmationItem.inStore
                if (accessToken != null && refreshToken != null && item != null) {
                    sparkRepo.saveInStore(accessToken, refreshToken, item).collect {
                        when (it) {
                            is RequestStatus.Waiting -> {
                                isLoading.value = true
                            }

                            is RequestStatus.Success -> {
                                isLoading.value = false
                                isConfirmed.value = true
                                // might want to clear item here
                            }

                            is RequestStatus.Error -> {
                                isLoading.value = false
                                errorMessage.value = it.message
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("InStoreConfirmActivityVM", "Error during saveInStore", e)
                isLoading.value = false
                errorMessage.value = e.message.toString()
            }
        }
    }
}