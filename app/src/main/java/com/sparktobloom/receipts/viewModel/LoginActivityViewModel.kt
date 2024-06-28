package com.sparktobloom.receipts.viewModel

import android.app.Application
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sparktobloom.receipts.data.UserRequestDto
import com.sparktobloom.receipts.data.UserResponseDto
import com.sparktobloom.receipts.model.ReceiptUser
import com.sparktobloom.receipts.model.ReceiptUserSingleton
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.RequestStatus
import kotlinx.coroutines.launch

class LoginActivityViewModel(
    private val sparkRepo: SparkRepository,
    val application: Application
) :
    ViewModel() {
    private var isLoading: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { value = false }
    private var errorMessage: MutableLiveData<String> = MutableLiveData()
    private var user: MutableLiveData<ReceiptUser?> = MutableLiveData()

    fun getIsLoading(): LiveData<Boolean> = isLoading
    fun getErrorMessage(): MutableLiveData<String> = errorMessage
    fun getUser(): MutableLiveData<ReceiptUser?> = user

    fun login(userDto: UserRequestDto) {
        viewModelScope.launch {
            try {
                sparkRepo.login(userDto).collect {
                    when (it) {
                        is RequestStatus.Waiting -> {
                            isLoading.value = true
                        }

                        is RequestStatus.Success -> {
                            isLoading.value = false
                            //user.value = it.data
                            val user = ReceiptUser(
                                userName = it.data.user.username,
                                emailAddress = it.data.user.email,
                                accessToken = it.data.accessToken,
                                refreshToken = it.data.refreshToken
                            )

                            // Setting the singleton user
                            ReceiptUserSingleton.user = user
                            this@LoginActivityViewModel.user.value = user
                        }

                        is RequestStatus.Error -> {
                            isLoading.value = false
                            errorMessage.value = it.message
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("LoginActivityVM", "Error during login", e)
                isLoading.value = false
                errorMessage.value = e.message.toString()
            }
        }
    }
}