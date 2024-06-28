package com.sparktobloom.receipts.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sparktobloom.receipts.R
import com.sparktobloom.receipts.databinding.ActivityHomeBinding
import com.sparktobloom.receipts.databinding.ActivityLoginBinding
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.DevApiService
import com.sparktobloom.receipts.viewModel.HomeActivityViewModel
import com.sparktobloom.receipts.viewModel.LoginActivityViewModel
import com.sparktobloom.receipts.viewModel.ViewModelFactory

class HomeActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: HomeActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        binding = ActivityHomeBinding.inflate(LayoutInflater.from(this))
        binding.chatBtn.setOnClickListener(this)
        binding.scanBtn.setOnClickListener(this)

        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        try {
            val sparkRepo = SparkRepository(DevApiService.getService(this))
            val factory =
                ViewModelFactory(HomeActivityViewModel::class.java, sparkRepo, application)
            viewModel = ViewModelProvider(this, factory).get(HomeActivityViewModel::class.java)
            viewModel.checkAuth()
        } catch (e: Exception) {
            Toast.makeText(
                this@HomeActivity,
                e.message,
                Toast.LENGTH_LONG
            ).show()
        }

        /*viewModel.getAuthenticatedStatus().observe(this) { response ->
            Log.d("HomeActivity", "Auth status changed: $response")
            if (response == true) {
                Toast.makeText(
                    this@HomeActivity,
                    "Authenticated",
                    Toast.LENGTH_LONG
                ).show()
            } else if (response == false) {
                Toast.makeText(
                    this@HomeActivity,
                    "Not Authenticated",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }*/
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.chatBtn -> {
                    startActivity(Intent(this, ChatActivity::class.java))
                }

                R.id.scanBtn -> {
                    startActivity(Intent(this, MainActivity::class.java))
                }
            }
        }
    }
}