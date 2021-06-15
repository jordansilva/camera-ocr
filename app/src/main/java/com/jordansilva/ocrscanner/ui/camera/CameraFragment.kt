package com.jordansilva.ocrscanner.ui.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.core.impl.ImageOutputConfig.RotationValue
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jordansilva.ocrscanner.databinding.FragmentCameraBinding
import com.jordansilva.ocrscanner.utils.checkPermissionGranted
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private var imageAnalyzer: ImageAnalysis? = null

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                TODO("Implement cameraUnavailable()")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraExecutor = Executors.newSingleThreadExecutor()
        checkCameraPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        cameraExecutor.shutdown()
    }

    private fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA
        when {
            requireContext().checkPermissionGranted(permission) -> startCamera()
            shouldShowRequestPermissionRationale(permission) -> TODO("Implement showUiRationale()")
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            bindCameraUseCases(cameraProvider)

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    /** Declare and bind preview, capture and analysis use cases */
    private fun bindCameraUseCases(cameraProvider: ProcessCameraProvider) {
        val rotation = binding.cameraPreviewView.display.rotation

        val cameraSelector = buildCameraSelector(cameraProvider)
        val preview = buildPreview(rotation)
        imageCapture = buildImageCapture(rotation)
        imageAnalyzer = buildImageAnalyzer(rotation)

        // Unbind use cases before rebinding
        cameraProvider.unbindAll()

        try {
            preview.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)

            // Bind use cases to camera
            cameraProvider.bindToLifecycle(
                viewLifecycleOwner,
                cameraSelector,
                preview,
                imageCapture,
                imageAnalyzer
            )
        } catch (exception: Exception) {
            Log.e(TAG, "Use case binding failed", exception)
        }
    }

    private fun buildCameraSelector(cameraProvider: ProcessCameraProvider): CameraSelector {
        // Select back camera as a default
        return when {
            cameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) -> CameraSelector.DEFAULT_BACK_CAMERA
            cameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA) -> CameraSelector.DEFAULT_FRONT_CAMERA
            else -> throw IllegalStateException("Camera is not available!")
        }
    }

    private fun buildPreview(@RotationValue rotation: Int): Preview {
        return Preview.Builder()
            .setTargetRotation(rotation)
            .build()
    }

    private fun buildImageCapture(@RotationValue rotation: Int): ImageCapture {
        return ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .setTargetRotation(rotation)
            .build()
    }

    private fun buildImageAnalyzer(@RotationValue rotation: Int): ImageAnalysis {
        return ImageAnalysis.Builder()
            .setTargetRotation(rotation)
            .build()
            .apply { setAnalyzer(cameraExecutor, ::analyze) }
    }

    private fun analyze(imageProxy: ImageProxy) {
        val buffer = imageProxy.planes[0].buffer
        val data = ByteArray(buffer.limit())
        buffer.get(data) // Copy the buffer into a byte array

        val pixels = data.map { it.toInt() and 0xFF }
        val luma = pixels.average()
        Log.d(TAG, "Average luminosity: $luma")
        imageProxy.close()
    }

    fun takePhoto() {
        val imageCapture: ImageCapture = imageCapture ?: return

        imageCapture.takePicture(
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    image.close()
                }

                override fun onError(exception: ImageCaptureException) {
                    Log.d(TAG, "onError: ${exception.message}", exception)
                }
            })
    }

    companion object {
        private val TAG = CameraFragment::class.simpleName

        /**
         * Use this factory method to create a new instance of
         * [CameraFragment] using the provided parameters.
         *
         * @return A new instance of fragment CameraFragment.
         */
        fun newInstance() = CameraFragment()
    }
}
