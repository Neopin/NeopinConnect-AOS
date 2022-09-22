package io.neoply.dappsample.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import io.neoply.dappsample.R
import io.neoply.dappsample.databinding.DialogQrCodeBinding
import java.lang.Exception

class QrCodeDialog(
    private val uri: String
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogQrCodeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.dialog_qr_code, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            val multiFormatWriter = MultiFormatWriter()
            try {
                val bitMatrix: BitMatrix = multiFormatWriter.encode(
                    uri,
                    BarcodeFormat.QR_CODE,
                    180,
                    180
                )
                val barcodeEncoder = BarcodeEncoder()
                val bitmap = barcodeEncoder.createBitmap(bitMatrix)
                ivQr.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            viewOutside.setOnClickListener {
                dismiss()
            }
            btnClose.setOnClickListener {
                dismiss()
            }
            tvCopy.setOnClickListener {
                val clipboardManager = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboardManager.setPrimaryClip(ClipData.newPlainText("code", uri))
                Toast.makeText(requireContext(), "Copied!", Toast.LENGTH_LONG).show()
            }
        }
    }
}