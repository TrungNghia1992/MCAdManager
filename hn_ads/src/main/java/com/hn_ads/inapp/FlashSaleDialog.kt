package com.hn_ads.inapp

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.DialogFragment
import com.hn_ads.AdsSPManager
import com.hn_ads.R
import com.hn_ads.databinding.DialogFlashSaleBinding

class FlashSaleDialog : DialogFragment() {

    companion object {
        const val TAG = "FlashSaleDialog"
        private const val KEY_APP_NAME = "KEY_APP_NAME"
        fun getInstance(appName: String) =
            FlashSaleDialog().apply {
                arguments = Bundle().apply {
                    putString(KEY_APP_NAME, appName)
                }
            }
    }

    private lateinit var binding: DialogFlashSaleBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = android.app.AlertDialog.Builder(requireContext(), R.style.HNTransparent)
        builder.setView(setupView())
        return builder.create()
    }

    private fun setupView(): View? {
        val activity = activity ?: return null
        val layoutInflater =
            activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = DialogFlashSaleBinding.inflate(layoutInflater, null, false)
        initView()
        initAction()
        return binding.root
    }

    private fun initView() {
        binding.viewTimeFlash.startTimeDiscount()

    }


    private fun initAction() {
        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.btnBuyNow.setOnClickListener {
            openInApp()
        }

    }

    private fun openInApp() {
        val appName = arguments?.getString(KEY_APP_NAME) ?: ""
        val context = context ?: return
        startActivity(InApp2Activity.createIntent(context, appName))
        dismiss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        context?.let { AdsSPManager.updateFlashDialog(it) }
        super.onDismiss(dialog)
    }

}