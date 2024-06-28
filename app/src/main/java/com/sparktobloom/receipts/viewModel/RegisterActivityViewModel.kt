package com.sparktobloom.receipts.viewModel

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparktobloom.receipts.data.User
import com.sparktobloom.receipts.data.ValidateEmailBody
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.RequestStatus
import kotlinx.coroutines.launch

class RegisterActivityViewModel(
    private val sparkRepo: SparkRepository,
    val application: Application
) :
    ViewModel() {
    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<String> = MutableLiveData()
    private var isUniqueEmail: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var user: MutableLiveData<User> = MutableLiveData()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getErrorMessage(): LiveData<String> = errorMessage
    fun getIsUnique(): LiveData<Boolean> = isUniqueEmail
    fun getUser(): LiveData<User> = user

    init {
        Log.d("RegisterActivityVM", "ViewModel initialized with Repository: $sparkRepo")
        // Initialization code
    }

    fun validateEmailAddress(body: ValidateEmailBody) {
        viewModelScope.launch {

            try {
                sparkRepo.validateEmailAddress(body).collect {
                    when (it) {
                        is RequestStatus.Waiting -> {
                            isLoading.value = true
                        }

                        is RequestStatus.Success -> {
                            isLoading.value = false
                            isUniqueEmail.value = it.data.unique
                        }

                        is RequestStatus.Error -> {
                            isLoading.value = false
                            errorMessage.value = it.message
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("RegisterActivityVM", "Error during validateEmailAddress", e)
                isLoading.value = false
                errorMessage.value = e.message.toString()
            }
        }
    }
}