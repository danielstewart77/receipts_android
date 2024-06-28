package com.sparktobloom.receipts.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sparktobloom.receipts.R
import com.sparktobloom.receipts.databinding.ActivityChatBinding
import com.sparktobloom.receipts.databinding.ActivityLoginBinding
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.DevApiService
import com.sparktobloom.receipts.viewModel.ChatActivityViewModel
import com.sparktobloom.receipts.viewModel.LoginActivityViewModel
import com.sparktobloom.receipts.viewModel.ViewModelFactory

class ChatActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var viewModel: ChatActivityViewModel
    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // binding
        binding = ActivityChatBinding.inflate(LayoutInflater.from(this))
        binding.chatBtn.setOnClickListener(this)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.chat)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // init viewmodel
        try {
            val sparkRepo = SparkRepository(DevApiService.getService(this))
            val factory =
                ViewModelFactory(ChatActivityViewModel::class.java, sparkRepo, application)
            viewModel = ViewModelProvider(this, factory).get(ChatActivityViewModel::class.java)

            Log.d("RegisterActivity", "ViewModel initialized successfully")

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error initializing ViewModel", e)
        }

        // observers
        viewModel.getIsLoading().observe(this) {
            binding.progressBar.isVisible = it
        }
        viewModel.getErrorMessage().observe(this) { error ->
            Toast.makeText(
                this@ChatActivity,
                error,
                Toast.LENGTH_LONG
            ).show()
        }

        // Set up WebView
        binding.serverWebView.apply {
            webViewClient = WebViewClient() // Ensures links open within the WebView
            settings.javaScriptEnabled = true // Enable JavaScript if needed
        }

        viewModel.getServerMessage().observe(this, Observer { message ->
            binding.serverWebView.loadData(message, "text/html", "UTF-8")
        })
    }

    override fun onClick(v: View?) {
        viewModel.chat(binding.chatEt.text.toString())
    }
}