package com.sparktobloom.receipts.utils

import org.json.JSONException
import org.json.JSONObject

object SimplifiedMessage {
    fun get(message: String): HashMap<String, String> {
        val messages = HashMap<String, String>()
        val jsonObject = JSONObject(message)

        try {
            if (jsonObject.has("message")) {
                val jsonMessages = jsonObject.getJSONObject("message")
                jsonMessages.keys().forEach {
                    messages[it] = jsonMessages.getString(it)
                }
            } else if (jsonObject.has("msg")) {
                messages["error"] = jsonObject.getString("msg")
            } else {
                messages["error"] = "Unknown error occurred"
            }
        } catch (e: JSONException) {
            messages["error"] = "Error parsing response"
        }

        return messages
    }
}

object ServerMessage {
    fun get(responseBody: String): String {
        return try {
            val cleanedResponseBody = responseBody.replace("\n", "").replace("\r", "")
            val jsonObject = JSONObject(cleanedResponseBody)
            when {
                jsonObject.has("message") -> jsonObject.getString("message")
                jsonObject.has("error") -> jsonObject.getString("error")
                else -> "Unknown error"
            }
        } catch (e: JSONException) {
            // Handle the case where responseBody is not a valid JSON
            e.message.toString()
        }
    }
}