package com.iie.thethreeburnouts.mineyourmoney

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.common.util.concurrent.ListenableFuture
import com.iie.thethreeburnouts.mineyourmoney.databinding.FragmentAddExpenseBinding
import java.util.Calendar
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class AddExpenseFragment : Fragment() {
    private var _binding: FragmentAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedWallet: Wallet? = null
    private var selectedRecurrence: String? = null
    private var selectedDate: Calendar = Calendar.getInstance()
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var cameraSelector: CameraSelector
    private var imageCapture: ImageCapture? = null
    private lateinit var imgCaptureExecutor: ExecutorService



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ref the module manual for this
        binding.topAppBar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnWalletDropdown.setOnClickListener {
            WalletSelectorBottomSheet { wallet ->
                selectedWallet = wallet
                binding.tvSelectedWallet.apply {
                    text = wallet.name
                    visibility = View.VISIBLE
                }
            }.show(childFragmentManager, "WalletSelector")
        }

        binding.btnRecurrenceDropdown.setOnClickListener {
            RecurrenceSelectorBottomSheet { recurrence ->
                selectedRecurrence = recurrence
                binding.tvSelectedRecurrence.apply {
                    text = recurrence
                    visibility = View.VISIBLE
                }
            }.show(childFragmentManager, "RecurrenceSelector")
        }

        binding.btnDateDropdown.setOnClickListener {
            DatePickerBottomSheet(selectedDate) { year, month, day ->
                selectedDate.set(year, month, day)
                binding.tvSelectedDate.apply {
                    text = "$day/${month + 1}/$year"
                    visibility = View.VISIBLE
                }
            }.show(childFragmentManager, "DatePicker")
        }

        binding.btnUploadPhoto.setOnClickListener {
            startCamera()
        }
        // ref the module manual for this

        binding.btnConfirm.setOnClickListener {
            // Handle saving the expense
            val amount = binding.etExpenseAmount.text.toString()
            val note = binding.etInputNote.text.toString()
        }
        imgCaptureExecutor = Executors.newSingleThreadExecutor()

        // Request camera permission and start camera
        val cameraProviderResult = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { permissionGranted ->
            if (permissionGranted) {
                startCamera()
            } else {
                Toast.makeText(context,
                    "Camera permission is required to use the camera",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    //private lateinit var imageCapture: ImageCapture

    private fun startCamera(){
        cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also{
                //it.setSurfaceProvider(binding.imgCameraImage.surfaceProvider)
            }
            imageCapture = ImageCapture.Builder().build()
            try{
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            }catch (e: Exception){
                Log.d("AddExpenseFragment", "Use case binding failed ")
            }
        }, ContextCompat.getMainExecutor(requireContext()))

    }
}
