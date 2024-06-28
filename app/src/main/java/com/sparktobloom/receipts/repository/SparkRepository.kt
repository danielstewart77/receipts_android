package com.sparktobloom.receipts.repository

import com.sparktobloom.receipts.data.UserRequestDto
import com.sparktobloom.receipts.data.ValidateEmailBody
import com.sparktobloom.receipts.utils.ApiConsumer
import com.sparktobloom.receipts.utils.RequestStatus
import com.sparktobloom.receipts.utils.ServerMessage
import com.sparktobloom.receipts.utils.SimplifiedMessage
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody

class SparkRepository(private val consumer: ApiConsumer) {
    fun validateEmailAddress(body: ValidateEmailBody) = flow {
        emit(RequestStatus.Waiting)
        val response = consumer.validateUniqueEmail(body)
        if (response.isSuccessful) {
            emit(RequestStatus.Success(response.body()!!))
        } else {
            emit(
                RequestStatus.Error(
                    ServerMessage.get(
                        response.errorBody()!!.toString()
                    )
                )
            )
        }
    }

    fun login(userDto: UserRequestDto) = flow {
        emit(RequestStatus.Waiting)
        val response = consumer.login(userDto)
        if (response.isSuccessful) {
            emit(RequestStatus.Success(response.body()!!))
        } else {
            val errorBody = response.errorBody()?.string().takeIf { it?.isNotEmpty() == true }
                ?: "{\"error\": \"Unknown error\"}"
            emit(
                RequestStatus.Error(
                    ServerMessage.get(errorBody)
                )
            )
        }
    }

    fun authenticated(accessToken: String, refreshToken: String) = flow {
        emit(RequestStatus.Waiting)
        val cookie = "refresh_token=$refreshToken"
        val response = consumer.authenticated("Bearer $accessToken", cookie)
        if (response.isSuccessful) {
            emit(RequestStatus.Success(true))
        } else {
            emit(RequestStatus.Success(false))
        }
    }

    suspend fun uploadReceipt(accessToken: String, refreshToken: String, file: MultipartBody.Part) = flow {
        emit(RequestStatus.Waiting)
        val cookie = "refresh_token=$refreshToken"
        val response = consumer.uploadReceipt("Bearer $accessToken", cookie, file)
        if (response.isSuccessful) {
            emit(RequestStatus.Success(response.body()!!))
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                ServerMessage.get(errorBody)
            } else {
                "Unknown error occurred"
            }
            emit(RequestStatus.Error(errorMessage))
        }
    }

    suspend fun uploadInStore(accessToken: String, refreshToken: String, file: MultipartBody.Part) = flow {
        emit(RequestStatus.Waiting)
        val cookie = "refresh_token=$refreshToken"
        val response = consumer.uploadinstore("Bearer $accessToken", cookie, file)
        if (response.isSuccessful) {
            emit(RequestStatus.Success(response.body()!!))
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                ServerMessage.get(errorBody)
            } else {
                "Unknown error occurred"
            }
            emit(RequestStatus.Error(errorMessage))
        }
    }

    suspend fun chat(accessToken: String, refreshToken: String, message: String) = flow {
        emit(RequestStatus.Waiting)
        val cookie = "refresh_token=$refreshToken"
        val messageBody = mapOf("query" to message)
        val response = consumer.chat("Bearer $accessToken", cookie, messageBody)
        if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null) {
                val message = responseBody["message"] ?: "No message"
                emit(RequestStatus.Success(message))
            } else {
                emit(RequestStatus.Error("Empty response"))
            }
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMessage = if (errorBody != null) {
                ServerMessage.get(errorBody)
            } else {
                "Unknown error occurred"
            }
            emit(RequestStatus.Error(errorMessage))
        }
    }

}