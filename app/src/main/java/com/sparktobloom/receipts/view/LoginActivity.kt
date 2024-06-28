package com.sparktobloom.receipts.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.sparktobloom.receipts.R
import com.sparktobloom.receipts.data.UserRequestDto
import com.sparktobloom.receipts.databinding.ActivityLoginBinding
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.ApiService
import com.sparktobloom.receipts.utils.DevApiService
import com.sparktobloom.receipts.utils.ServerMessage
import com.sparktobloom.receipts.viewModel.LoginActivityViewModel
import com.sparktobloom.receipts.viewModel.ViewModelFactory

class LoginActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        binding.loginBtn.setOnClickListener(this)
        binding.loginRegisterBtn.setOnClickListener(this)
        binding.emailEt.onFocusChangeListener = this
        binding.passwordEt.onFocusChangeListener = this
        binding.passwordEt.setOnClickListener(this)

        try {
            val sparkRepo = SparkRepository(DevApiService.getService(this))
            val factory =
                ViewModelFactory(LoginActivityViewModel::class.java, sparkRepo, application)
            viewModel = ViewModelProvider(this, factory).get(LoginActivityViewModel::class.java)

            Log.d("RegisterActivity", "ViewModel initialized successfully")

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error initializing ViewModel", e)
        }

        setupObservers()

        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupObservers() {
        viewModel.getIsLoading().observe(this) {
            binding.progressBar.isVisible = it
        }

        viewModel.getUser().observe(this) { user ->
            if (user != null) {
                Log.d("DEBUG", "user: $user")
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }

        viewModel.getErrorMessage().observe(this) { error ->
            Toast.makeText(
                this@LoginActivity,
                error,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun validate(): Boolean {
        return validateEmail() && validatePassword()
    }

    private fun validateEmail(): Boolean {
        var error: String? = null
        val value: String = binding.emailEt.text.toString()
        if (value.isEmpty()) {
            error = "Email is required"
        }

        binding.emailEt.error = error
        return error == null
    }

    private fun validatePassword(): Boolean {
        var error: String? = null
        val value: String = binding.passwordEt.text.toString()
        if (value.isEmpty()) {
            error = "Password is required"
        }

        binding.passwordEt.error = error
        return error == null
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.loginBtn -> {
                    submitForm()
                }

                R.id.loginRegisterBtn -> {
                    startActivity(Intent(this, RegisterActivity::class.java))
                }
            }
        }
    }

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                R.id.emailEt -> {
                    if (hasFocus) {
                        if (binding.emailTil.isErrorEnabled) {
                            binding.emailTil.isErrorEnabled = false
                        }
                    }
                }

                R.id.passwordEt -> {
                    if (hasFocus) {
                        if (binding.passwordTil.isErrorEnabled) {
                            binding.passwordTil.isErrorEnabled = false
                        }
                    } else {
                        validatePassword()
                    }
                }
            }
        }
    }

    private fun submitForm() {
        if (validate()) {
            viewModel.login(
                UserRequestDto(
                    binding.emailEt.text!!.toString(),
                    binding.passwordEt.text!!.toString()
                )
            )
        }
    }

    override fun onKey(v: View?, keyCode: Int, keyEvent: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_ENTER && keyEvent!!.action == KeyEvent.ACTION_UP) {
            submitForm()
        }
        return false
    }
}