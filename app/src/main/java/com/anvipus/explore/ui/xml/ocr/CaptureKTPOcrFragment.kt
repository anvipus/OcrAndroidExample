package com.anvipus.explore.ui.xml.ocr

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.anvipus.core.R
import com.anvipus.core.common.ResultErrorType
import com.anvipus.core.utils.*
import com.anvipus.core.utils.BitmapUtils.saveBitmapToFile
import com.anvipus.core.utils.analyzer.*
import com.anvipus.core.utils.model.OCRResultModel
import com.anvipus.explore.base.BaseFragment
import com.anvipus.explore.databinding.CaptureKtpOcrFragmentBinding
import com.anvipus.explore.databinding.PopupBottomsheetScanTimerOcrBinding
import com.codedisruptors.dabestofme.di.Injectable
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject
import kotlin.concurrent.fixedRateTimer

class CaptureKTPOcrFragment : BaseFragment(), CaptureKtpListener, Injectable {

    companion object {
        fun newInstance() = CaptureKTPOcrFragment()
    }

    override val layoutResource: Int
        get() = com.anvipus.explore.R.layout.capture_ktp_ocr_fragment

    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_capture_ocr

    private lateinit var binding: CaptureKtpOcrFragmentBinding

    private var timer: Timer? = null
    private var flashMode = ImageCapture.FLASH_MODE_OFF
    private var camera: Camera? = null

    private var analysisUseCase : ImageAnalysis? = null
    private var lightSensor: LightSensor? = null
    private var croppedBitmap : Bitmap? = null
    private var memoryUsageMonitor: MemoryUsageMonitor? = null
    private var bottomSheetDialog: BottomSheetDialog? = null
    lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CaptureOCRViewModel by activityViewModels { viewModelFactory }

    override fun initView(view: View) {
        super.initView(view)
        binding = CaptureKtpOcrFragmentBinding.bind(view)
        with(binding) {
            cameraExecutor = Executors.newSingleThreadExecutor()
            viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }

            lightSensor = LightSensor(requireContext(), object : LightSensorListener {
                override fun onCurrentLightChanged(value: Int) {
                    val isLowLight = value < 5
                    tvMessage.visibility = isLowLight.toVisibilityOrGone()
                }
            })
            hideProgressDialog()
            ibFlashMode.visibility = (IdentifierOCR.withFlash ?: false).toVisibilityOrGone()
            tvMessage.visibility = View.GONE
            setMessageIsTorchEnable(false)

            memoryUsageMonitor = MemoryUsageMonitor(requireActivity(), requireContext().getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager, lowMemoryThreshold = IdentifierOCR.lowMemoryThreshold)
        }

    }

    override fun initObserver() {
        super.initObserver()
        viewModel.currentState.observe(this) {
            when(it) {
                StateCapture.COMPLETED -> {
                    val extractDataOCR = ExtractDataOCR(requireContext(), object : ExtractDataOCRListener {
                        override fun onStart() {
                            showProgressDialog()
                        }

                        override fun onFinish(result: OCRResultModel) {
                            hideProgressDialog()
                            try {
                                navigate(CaptureKTPOcrFragmentDirections.actionToScanOcrResult(result))
                            }catch (e:Exception){
                                e.printStackTrace()
                            }
                        }

                        override fun onError(message: String?, errorType: ResultErrorType?) {
                            hideProgressDialog()

                            Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
                        }
                    })
                    viewModel.processExtract(extractDataOCR)
                }
                StateCapture.SCANNING -> {
                    viewModel.captureImage(croppedBitmap)
                }
                else -> {
                    stopTimer()
                    hideProgressDialog()
                    dismissPopupScanDialog()
                }
            }
        }
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
    }

    private fun showPopupHoldScanDialog() {
        if (timer != null || requireActivity().isFinishing) return
        val bindingPopup = PopupBottomsheetScanTimerOcrBinding.inflate(LayoutInflater.from(requireContext()))
        var counter = CaptureOCRViewModel.COUNTDOWN_TIME

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogThemeOCR)

        viewModel.captureImage(croppedBitmap)

        timer = fixedRateTimer(initialDelay = 500, period = 1000) {
            requireActivity().runOnUiThread {
                bindingPopup.tvCountdown.text = "$counter"
                if (counter == 0) {
                    dismissPopupScanDialog()
                    stopTimer()
                }
                counter--
            }
        }

        if (bottomSheetDialog?.isShowing == true) {
            bottomSheetDialog?.dismiss()
        }

        bottomSheetDialog?.setContentView(bindingPopup.root)
        bottomSheetDialog?.show()
    }

    private fun dismissPopupScanDialog() {
        if(bottomSheetDialog != null && bottomSheetDialog?.isShowing == true) {
            bottomSheetDialog?.dismiss()
            bottomSheetDialog = null
        }
    }

    private fun setMessageIsTorchEnable(isActive: Boolean) {
        with(binding) {
            tvMessage.text = if (isActive) "Cahaya terlalu gelap, silahkan mencari tempat yang lebih terang" else "Cahaya terlalu gelap, bisa menggunakan flash"
        }

    }

    private fun setUpCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            // CameraProvider
            cameraProvider = cameraProviderFuture.get()

            // Build and bind the camera use cases
            cameraProvider?.let {
                startCamera(it, binding.viewFinder)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun startCamera(
        cameraProvider: ProcessCameraProvider,
        previewView: PreviewView,
    ) {
        // CameraSelector
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview
        val previewUseCase = Preview.Builder().build()

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetResolution(Size(1280,720))
            .build().also {
                it.setAnalyzer(cameraExecutor, CaptureOCRAnalyzer(this))
            }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            camera = cameraProvider.bindToLifecycle(
                this, cameraSelector, previewUseCase, analysisUseCase
            )
            // Attach the viewfinder's surface provider to preview use case
            previewUseCase.setSurfaceProvider(previewView.surfaceProvider)
        } catch (exc: Exception) {
            Toast.makeText(requireContext(),exc.message,Toast.LENGTH_LONG).show()
        }
    }

    override fun setupListener() {
        super.setupListener()
        with(binding){
            ibFlashMode.setOnClickListener {
                flashMode =
                    if (flashMode == ImageCapture.FLASH_MODE_OFF) ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
                val isTorchEnable = flashMode == ImageCapture.FLASH_MODE_ON

                setMessageIsTorchEnable(isTorchEnable)

                ibFlashMode.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        if (isTorchEnable) R.drawable.ic_baseline_flash_off else R.drawable.ic_baseline_flash_on
                    )
                )
                camera?.cameraControl?.enableTorch(isTorchEnable)
            }
        }
    }

    //Listener of CaptureKtpListener
    override fun onStatusChanged(status: Status, croppedBitmap : Bitmap?) {
        if (viewModel.currentState.value == StateCapture.COMPLETED) return
        if (status == Status.SCANNING) {
            this.croppedBitmap = croppedBitmap
            if(viewModel.currentState.value == StateCapture.READY) {
                memoryUsageMonitor?.checkMemoryAndProceed {
                    showPopupHoldScanDialog()
                }
            }
        } else {
            viewModel.clearDataCapture()
        }
    }

    override fun onCaptureFailed(exception: Exception) {
        try {
            Toast.makeText(requireContext(),exception.message,Toast.LENGTH_LONG).show()
        }catch (e:Exception){
            e.printStackTrace()
        }

    }

    private fun showProgressDialog() {
        requireActivity().runOnUiThread {
            with(binding){
                gifLoading.visibility = View.VISIBLE
            }

        }
    }

    private fun hideProgressDialog() {
        with(binding) {
            gifLoading.visibility = View.GONE
        }

    }

    override fun onResume() {
        super.onResume()
        stopTimer()
        lightSensor?.startDetectingSensor()
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
        hideProgressDialog()
        lightSensor?.closeSensor()
    }

    override fun onDestroy() {
        dismissPopupScanDialog()
        super.onDestroy()
    }

}