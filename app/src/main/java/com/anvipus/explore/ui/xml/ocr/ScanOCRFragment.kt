package com.anvipus.explore.ui.xml.ocr

import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.anvipus.core.R
import com.anvipus.explore.base.BaseFragment
import com.anvipus.explore.databinding.ScanOcrFragmentBinding
import com.codedisruptors.dabestofme.di.Injectable
import com.anvipus.core.utils.BitmapUtils.saveBitmapToFile
import com.anvipus.core.utils.analyzer.ScanKtpListener
import com.anvipus.core.utils.analyzer.ScanOCRAnalyzer
import com.anvipus.core.utils.analyzer.Status
import com.anvipus.core.utils.model.KTPModel
import com.anvipus.core.utils.model.OCRResultModel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

class ScanOCRFragment : BaseFragment(), ScanKtpListener, Injectable {

    companion object {
        fun newInstance() = ScanOCRFragment()
    }

    override val layoutResource: Int
        get() = com.anvipus.explore.R.layout.scan_ocr_fragment

    override val statusBarColor: Int
        get() = R.color.colorAccent

    override val showToolbar: Boolean get() = true

    override val headTitle: Int
        get() = com.anvipus.explore.R.string.title_toolbar_scan_ocr

    private lateinit var binding: ScanOcrFragmentBinding

    private lateinit var cameraExecutor: ExecutorService
    private var cameraProvider: ProcessCameraProvider? = null

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val viewModel: CaptureOCRViewModel by activityViewModels { viewModelFactory }

    override fun initView(view: View) {
        super.initView(view)
        binding = ScanOcrFragmentBinding.bind(view)
        with(binding) {
            cameraExecutor = Executors.newSingleThreadExecutor()
            viewFinder.post {
                // Set up the camera and its use cases
                setUpCamera()
            }
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
        val cameraSelector =
            CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()

        // Preview
        val previewUseCase = Preview.Builder().build()


        val analysisUseCase = ImageAnalysis.Builder().build().also {
            it.setAnalyzer(cameraExecutor, ScanOCRAnalyzer(this))
        }

        // Must unbind the use-cases before rebinding them
        cameraProvider.unbindAll()

        try {
            // A variable number of use-cases can be passed here -
            // camera provides access to CameraControl & CameraInfo
            cameraProvider.bindToLifecycle(
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

        }
    }

    override fun onStatusChanged(status: Status) {
        binding.progressBar.visibility = View.INVISIBLE
        when (status) {
            Status.NOT_FOUND -> {
                binding.tvInfo.text = "KTP Tidak Ditemukan"
            }
            Status.NOT_READY -> {
                binding.tvInfo.text = "Posisikan KTP anda pada kotak yg telah disediakan"
            }
            Status.SCANNING -> {
                binding.tvInfo.text = "Scanning..."
                binding.progressBar.visibility = View.VISIBLE
            }
            else -> {
                binding.tvInfo.text = "Scan Complete"
            }
        }
    }

    override fun onProgress(progress: Int) {
        binding.progressBar.progress = progress
    }

    override fun onScanComplete(ktpModel: KTPModel) {
        ktpModel.bitmap?.let {
            try {
                val bitmapuri = saveBitmapToFile(it,requireContext().filesDir.absolutePath, "scanktp.jpg",
                    onError = { message, errorType ->
                        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
                    })

                val scanResult =
                    OCRResultModel(
                        true,
                        "Success",
                        errorType = null,
                        bitmapuri.path,
                        ktpModel.apply { bitmap = it })
                navigate(ScanOCRFragmentDirections.actionToScanOcrResult(scanResult))
            }catch (e:Exception){
                e.printStackTrace()
            }


        }
    }

    override fun onScanFailed(exception: Exception) {
        Toast.makeText(requireContext(),exception.message,Toast.LENGTH_LONG).show()
    }


}