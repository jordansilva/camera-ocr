package com.jordansilva.ocrscanner.ui.camera

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jordansilva.ocrscanner.databinding.FragmentCameraBinding
import com.jordansilva.ocrscanner.utils.checkPermissionGranted

class CameraFragment : Fragment() {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                cameraUnavailable()
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

        checkCameraPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkCameraPermission() {
        val permission = Manifest.permission.CAMERA
        when {
            requireContext().checkPermissionGranted(permission) -> startCamera()
            shouldShowRequestPermissionRationale(permission) -> showUiRationale()
            else -> requestPermissionLauncher.launch(permission)
        }
    }

    private fun showUiRationale() {}
    private fun cameraUnavailable() {}

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            //Preview
            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(binding.cameraPreviewView.surfaceProvider)

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Select back camera as a default
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
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