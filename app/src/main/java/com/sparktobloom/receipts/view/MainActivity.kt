package com.sparktobloom.receipts.view

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.tabs.TabLayout
import com.sparktobloom.receipts.R
import com.sparktobloom.receipts.data.InStoreItem
import com.sparktobloom.receipts.data.UserResponseDto
import com.sparktobloom.receipts.databinding.ActivityMainBinding
import com.sparktobloom.receipts.model.ConfirmationItem
import com.sparktobloom.receipts.model.ReceiptUser
import com.sparktobloom.receipts.model.ReceiptUserSingleton
import com.sparktobloom.receipts.repository.SparkRepository
import com.sparktobloom.receipts.utils.ApiService
import com.sparktobloom.receipts.utils.DevApiService
import com.sparktobloom.receipts.utils.RequestStatus
import com.sparktobloom.receipts.viewModel.LoginActivityViewModel
import com.sparktobloom.receipts.viewModel.ViewModelFactory
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Locale


class MainActivity : AppCompatActivity() {

    private var user: ReceiptUser? = null
    private lateinit var viewModel: LoginActivityViewModel
    private lateinit var sparkRepo: SparkRepository

    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val multiplePermissionId = 14
    private val multiplePermissionNameList = if (Build.VERSION.SDK_INT >= 34) {
        arrayListOf(
            android.Manifest.permission.CAMERA,
        )
    } else {
        arrayListOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    private var isReceipt = true

    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var camera: Camera
    private lateinit var cameraSelector: CameraSelector
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var aspectRatio = AspectRatio.RATIO_16_9

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        tabLayout.addTab(tabLayout.newTab().setText("Receipt"));
        tabLayout.addTab(tabLayout.newTab().setText("In-store"));

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                when (tab.position) {
                    0 -> {
                        // Receipt tab selected
                        isReceipt = true
                    }

                    1 -> {
                        // In-store tab selected
                        isReceipt = false
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        user = ReceiptUserSingleton.user

        if (checkMultiplePermission()) {
            startCamera()
        }

        binding.captureBtn.setOnClickListener {
            takePhoto()
        }

        try {
            sparkRepo = SparkRepository(DevApiService.getService(this))
            val factory =
                ViewModelFactory(LoginActivityViewModel::class.java, sparkRepo, application)
            viewModel = ViewModelProvider(this, factory).get(LoginActivityViewModel::class.java)

            Log.d("RegisterActivity", "ViewModel initialized successfully")

        } catch (e: Exception) {
            Log.e("RegisterActivity", "Error initializing ViewModel", e)
        }
    }

    private fun checkMultiplePermission(): Boolean {
        val listPermissionNeeded = arrayListOf<String>()
        for (permission in multiplePermissionNameList) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                listPermissionNeeded.add(permission)
            }
        }
        if (listPermissionNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionNeeded.toTypedArray(),
                multiplePermissionId
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == multiplePermissionId) {
            if (grantResults.isNotEmpty()) {
                var isGrant = true
                for (element in grantResults) {
                    if (element == PackageManager.PERMISSION_DENIED) {
                        isGrant = false
                    }
                }
                if (isGrant) {
                    // here all permission granted successfully
                    startCamera()
                } else {
                    var someDenied = false
                    for (permission in permissions) {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                                this,
                                permission
                            )
                        ) {
                            if (ActivityCompat.checkSelfPermission(
                                    this,
                                    permission
                                ) == PackageManager.PERMISSION_DENIED
                            ) {
                                someDenied = true
                            }
                        }
                    }
                    /*if (someDenied) {
                        // here app Setting open because all permission is not granted
                        // and permanent denied
                        appSettingOpen(this)
                    } else {
                        // here warning permission show
                        warningPermissionDialog(this) { _: DialogInterface, which: Int ->
                            when (which) {
                                DialogInterface.BUTTON_POSITIVE ->
                                    checkMultiplePermission()
                            }
                        }
                    }*/
                }
            }
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()
            bindCameraUserCases()
        }, ContextCompat.getMainExecutor(this))
    }

    private fun bindCameraUserCases() {
        val rotation = binding.cameraPreview.display.rotation

        val resolutionSelector = ResolutionSelector.Builder()
            .setAspectRatioStrategy(
                AspectRatioStrategy(
                    aspectRatio,
                    AspectRatioStrategy.FALLBACK_RULE_AUTO
                )
            )
            .build()

        val preview = Preview.Builder()
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()
            .also {
                it.setSurfaceProvider(binding.cameraPreview.surfaceProvider)
            }

        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .setResolutionSelector(resolutionSelector)
            .setTargetRotation(rotation)
            .build()

        cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        try {
            cameraProvider.unbindAll()

            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, preview, imageCapture
            )
            //setUpZoomTapToFocus()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun takePhoto() {
        val imageFolder = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "Images")
        if (!imageFolder.exists()) {
            imageFolder.mkdir()
        }

        val fileName = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(System.currentTimeMillis()) + ".jpg"

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Images")
            }
        }

        val metadata = ImageCapture.Metadata().apply {
            isReversedHorizontal = (lensFacing == CameraSelector.LENS_FACING_FRONT)
        }
        val outputOption =
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                ImageCapture.OutputFileOptions.Builder(
                    contentResolver,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ).setMetadata(metadata).build()
            } else {
                val imageFile = File(imageFolder, fileName)
                ImageCapture.OutputFileOptions.Builder(imageFile)
                    .setMetadata(metadata).build()
            }

        imageCapture.takePicture(
            outputOption,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    val message = "Photo Capture Succeeded: ${outputFileResults.savedUri}"
                    Toast.makeText(
                        this@MainActivity,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                    val imageUri = outputFileResults.savedUri ?: return

                    val inputStream = contentResolver.openInputStream(imageUri)
                    val file = createFileFromInputStream(inputStream, fileName)

                    try {
                        val accessToken = user!!.accessToken
                        val refreshToken = user!!.refreshToken
                        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                        if (isReceipt) uploadReceipt(accessToken, refreshToken, body)
                        else uploadInStore(accessToken, refreshToken, body)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this@MainActivity,
                            e.message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(
                        this@MainActivity,
                        exception.message.toString(),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        )
    }

    private fun uploadReceipt(accessToken: String, refreshToken: String, body: MultipartBody.Part){
        lifecycleScope.launch {
            sparkRepo.uploadReceipt(accessToken, refreshToken, body).collect {
                when (it) {
                    is RequestStatus.Waiting -> {
                        // Show loading
                    }

                    is RequestStatus.Success -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Upload successful",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    is RequestStatus.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Upload failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun uploadInStore(accessToken: String, refreshToken: String, body: MultipartBody.Part){
        lifecycleScope.launch {
            sparkRepo.uploadInStore(accessToken, refreshToken, body).collect {
                when (it) {
                    is RequestStatus.Waiting -> {
                        // Show loading
                    }

                    is RequestStatus.Success -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Upload successful",
                            Toast.LENGTH_LONG
                        ).show()
                        confirmation(it.data)
                    }

                    is RequestStatus.Error -> {
                        Toast.makeText(
                            this@MainActivity,
                            "Upload failed: ${it.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun confirmation(inStoreItem: InStoreItem){
        ConfirmationItem.inStore = InStoreItem(
            inStoreItem.storeName,
            inStoreItem.itemName,
            inStoreItem.units,
            inStoreItem.unitPrice,
            inStoreItem.totalPrice)

        startActivity(Intent(this, InStoreConfirmActivity::class.java))
    }

    private fun createFileFromInputStream(inputStream: InputStream?, fileName: String): File {
        val file = File(cacheDir, fileName)
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    /*private fun setUpZoomTapToFocus(){
        val listener = object : ScaleGestureDetector.SimpleOnScaleGestureListener(){
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val currentZoomRatio = camera.cameraInfo.zoomState.value?.zoomRatio  ?: 1f
                val delta = detector.scaleFactor
                camera.cameraControl.setZoomRatio(currentZoomRatio * delta)
                return true
            }
        }

        val scaleGestureDetector = ScaleGestureDetector(this,listener)

        binding.cameraPreview.setOnTouchListener { view, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (event.action == MotionEvent.ACTION_DOWN){
                val factory = binding.cameraPreview.meteringPointFactory
                val point = factory.createPoint(event.x,event.y)
                val action = FocusMeteringAction.Builder(point,FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(2, TimeUnit.SECONDS)
                    .build()

                val x = event.x
                val y = event.y

                val focusCircle = RectF(x-50,y-50, x+50,y+50)

                binding.focusCircleView.focusCircle = focusCircle
                binding.focusCircleView.invalidate()

                camera.cameraControl.startFocusAndMetering(action)

                view.performClick()
            }
            true
        }
    }*/
}
