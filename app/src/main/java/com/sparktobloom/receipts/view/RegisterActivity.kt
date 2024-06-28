package com.sparktobloom.receipts.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.sparktobloom.receipts.R
import com.sparktobloom.receipts.data.ValidateEmailBody
import com.sparktobloom.receipts.databinding.ActivityRegisterBinding
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.ApiService
import com.sparktobloom.receipts.utils.DevApiService
import com.sparktobloom.receipts.viewModel.RegisterActivityViewModel
import com.sparktobloom.receipts.viewModel.ViewModelFactory

class RegisterActivity : AppCompatActivity(), View.OnClickListener, View.OnFocusChangeListener,
    View.OnKeyListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d("RegisterActivity", "onCreate called")

        binding.userNameEt.onFocusChangeListener = this
        binding.emailEt.onFocusChangeListener = this
        binding.passwordEt.onFocusChangeListener = this
        binding.confirmPasswordEt.onFocusChangeListener = this
        try {
            //val sparkRepo = SparkRepository(ApiService.getService())
            val sparkRepo = SparkRepository(ApiService.getService())
            val factory =
                ViewModelFactory(RegisterActivityViewModel::class.java, sparkRepo, application)
            viewModel = ViewModelProvider(this, factory).get(RegisterActivityViewModel::class.java)

//            viewModel = ViewModelProvider(
//                this,
//                ViewModelFactory(sparkRepo, application)
//            ).get(RegisterActivityViewModel::class.java)

            Log.d("RegisterActivity", "ViewModel initialized successfully")

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error initializing ViewModel", e)
        }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.getIsLoading().observe(this) {
            binding.progressBar.isVisible = it
        }

        viewModel.getIsUnique().observe(this) {
            if (it) {
                binding.emailTil.apply {
                    if (isErrorEnabled) isErrorEnabled = false
                    setStartIconDrawable(R.drawable.check_circle_24)
                    setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                }
            } else {
                binding.emailTil.apply {
                    startIconDrawable = null
                    isErrorEnabled = true
                    error = "That email is taken"
                }
            }
        }

        viewModel.getErrorMessage().observe(this) { errorMessage ->
            binding.progressBar.isVisible = false
            val formErrorKeys = arrayOf("username", "email", "password")
            val message = StringBuilder()

            val errorMap = errorMessage.split(";").associate {
                val (key, value) = it.split(":")
                key to value
            }

            errorMap.map { entry ->
                if (formErrorKeys.contains(entry.key)) {
                    when (entry.key) {
                        "username" -> {
                            binding.userNameTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

                        "email" -> {
                            binding.emailTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

                        "password" -> {
                            binding.passwordTil.apply {
                                isErrorEnabled = true
                                error = entry.value
                            }
                        }

                        else -> {
                            Toast.makeText(this, "something weird happened", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    message.append(entry.value).append("\n")
                }
            }

            if (message.isNotEmpty()) {
                // Handle non-form specific errors here
                Toast.makeText(this, message.toString(), Toast.LENGTH_LONG).show()
            }


            if (message.isNotEmpty()) {
                AlertDialog.Builder(this)
                    .setIcon(R.drawable.info_24)
                    .setTitle("Info")
                    .setMessage(message)
                    .setPositiveButton("OK") { dialog, _ -> dialog!!.dismiss() }
                    .show()
            }

        }

        viewModel.getUser().observe(this) {
            binding.progressBar.isVisible = false
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_register)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun validateUserName(): Boolean {
        var error: String? = null
        val value: String = binding.userNameEt.text.toString()
        if (value.isEmpty()) {
            error = "Username is required"
        }

        binding.userNameEt.error = error
        return error == null
    }

    private fun validateEmail(): Boolean {
        var error: String? = null
        val value: String = binding.emailEt.text.toString()
        if (value.isEmpty()) {
            error = "Email is required"
        } else if (!Patterns.EMAIL_ADDRESS.matcher(value).matches()) {
            error = "Address is invalid"
        }

        binding.emailEt.error = error
        return error == null
    }

    private fun validatePassword(): Boolean {
        var error: String? = null
        val value: String = binding.passwordEt.text.toString()
        if (value.isEmpty()) {
            error = "Password is required"
        } else if (value.length < 8) {
            error = "Password must be at least 8 characters"
        } else if (!value.any { it.isUpperCase() }) {
            error = "Password must contain at least one uppercase letter"
        } else if (!value.any { it.isLowerCase() }) {
            error = "Password must contain at least one lowercase letter"
        } else if (!value.any { it.isDigit() }) {
            error = "Password must contain at least one number"
        } else if (!value.any { it in "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/`~" }) {
            error = "Password must contain at least one special character"
        }

        binding.passwordEt.error = error
        return error == null
    }

    private fun validateConfirmPassword(): Boolean {
        var error: String? = null
        val password: String = binding.passwordEt.text.toString()
        val confirmPassword: String = binding.confirmPasswordEt.text.toString()
        if (confirmPassword.isEmpty()) {
            error = "Please confirm your password"
        } else if (confirmPassword != password) {
            error = "Passwords do not match"
        }

        binding.confirmPasswordEt.error = error
        return error == null
    }

    override fun onClick(v: View?) {}

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (v != null) {
            when (v.id) {
                R.id.userNameEt -> {
                    if (hasFocus) {
                        if (binding.userNameTil.isErrorEnabled) {
                            binding.userNameTil.isErrorEnabled = false
                        }
                    } else {
                        validateUserName()
                    }
                }

                R.id.emailEt -> {
                    if (hasFocus) {
                        if (binding.emailTil.isErrorEnabled) {
                            binding.emailTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateEmail()) {
                            viewModel.validateEmailAddress(ValidateEmailBody(binding.emailEt.text!!.toString()))
                        }
                    }
                }

                R.id.passwordEt -> {
                    if (hasFocus) {
                        if (binding.passwordTil.isErrorEnabled) {
                            binding.passwordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validatePassword() && binding.passwordEt.text!!.isNotEmpty() && validateConfirmPassword()) {
                            if (binding.confirmPasswordTil.isErrorEnabled) {
                                binding.confirmPasswordTil.isErrorEnabled = false
                            }
                            binding.confirmPasswordTil.apply {
                                setStartIconDrawable(R.drawable.check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }

                R.id.confirmPasswordEt -> {
                    if (hasFocus) {
                        if (binding.confirmPasswordTil.isErrorEnabled) {
                            binding.confirmPasswordTil.isErrorEnabled = false
                        }
                    } else {
                        if (validateConfirmPassword() && validatePassword()) {
                            if (binding.passwordTil.isErrorEnabled) {
                                binding.passwordTil.isErrorEnabled = false
                            }
                            binding.passwordTil.apply {
                                setStartIconDrawable(R.drawable.check_circle_24)
                                setStartIconTintList(ColorStateList.valueOf(Color.GREEN))
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        return false
    }
}