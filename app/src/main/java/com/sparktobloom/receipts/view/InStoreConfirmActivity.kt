package com.sparktobloom.receipts.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.sparktobloom.receipts.data.InStoreItem
import com.sparktobloom.receipts.databinding.ActivityInStoreConfirmBinding
import com.sparktobloom.receipts.databinding.ActivityLoginBinding
import com.sparktobloom.receipts.model.ConfirmationItem
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.DevApiService
import com.sparktobloom.receipts.viewModel.InStoreConfirmActivityViewModel
import com.sparktobloom.receipts.viewModel.LoginActivityViewModel
import com.sparktobloom.receipts.viewModel.ViewModelFactory

class InStoreConfirmActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityInStoreConfirmBinding
    private lateinit var viewModel: InStoreConfirmActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityInStoreConfirmBinding.inflate(LayoutInflater.from(this))
        binding.confirmBtn.setOnClickListener(this)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        try {
            val sparkRepo = SparkRepository(DevApiService.getService(this))
            val factory =
                ViewModelFactory(InStoreConfirmActivityViewModel::class.java, sparkRepo, application)
            viewModel = ViewModelProvider(this, factory).get(InStoreConfirmActivityViewModel::class.java)

            Log.d("RegisterActivity", "ViewModel initialized successfully")

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error initializing ViewModel", e)
        }

        viewModel.getIsLoading().observe(this) {
            binding.progressBar.isVisible = it
        }

        viewModel.getIsConfirmed().observe(this) { confirmed ->
            if (confirmed) {
                Toast.makeText(
                    this@InStoreConfirmActivity,
                    "Success!",
                    Toast.LENGTH_LONG
                ).show()
                startActivity(Intent(this, HomeActivity::class.java))
            }
        }

        viewModel.getErrorMessage().observe(this) { error ->
            Toast.makeText(
                this@InStoreConfirmActivity,
                error,
                Toast.LENGTH_LONG
            ).show()
        }

        loadForm()
    }

    private fun loadForm() {
        try {
            if (ConfirmationItem.inStore != null) {
                binding.itemNameEt.setText(ConfirmationItem.inStore!!.itemName)
                binding.unitEt.setText(ConfirmationItem.inStore!!.units.toString())
                binding.unitPriceEt.setText(ConfirmationItem.inStore!!.unitPrice.toString())
                binding.totalPriceEt.setText(ConfirmationItem.inStore!!.totalPrice.toString())
            }
        } catch (e: Exception){
            Log.e("LoadInStoreConfirm", "Error loading form from ConfirmationItem")
        }
    }

    override fun onClick(v: View?) {
        // do some validation, then:
        viewModel.submitConfirmation()
    }
}