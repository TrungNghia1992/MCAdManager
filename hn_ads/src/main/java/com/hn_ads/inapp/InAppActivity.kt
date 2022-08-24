package com.hn_ads.inapp

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.hn_ads.AdsSPManager
import com.hn_ads.databinding.ActivityHnInAppBinding
import com.hn_ads.isNetworkAvailable
import com.hn_ads.toastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InAppActivity : AppCompatActivity() {
    private var _binding: ActivityHnInAppBinding? = null
    private val binding get() = _binding!!

    private var skuDetail: SkuDetails? = null

    companion object {
        private const val KEY_APP_NAME = "KEY_APP_NAME"
        private const val KEY_INAPP_SKU = "zip_premium_upgrade"
        private const val KEY_INAPP_SKU_FLASH_SALE = "zip_premium_upgrade_flash_sale"

        fun createIntent(context: Context, appName: String) =
            Intent(context, InAppActivity::class.java).apply {
                putExtra(KEY_APP_NAME, appName)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHnInAppBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectBilling()
        initAction()
    }

    private fun initAction() {
        binding.imgHnBack.setOnClickListener {
            finish()
        }

        binding.btnBuyNow.setOnClickListener {
            onLaunchPurchase()
        }

        binding.lblRestore.setOnClickListener {
            onRestore()
        }
    }

    private fun onRestore() {
        if (!isNetworkAvailable()) {
            toastMessage("Please check internet connection")
            return
        }

        binding.prgRestore.visibility = View.VISIBLE
        billingClient?.queryPurchaseHistoryAsync(
            BillingClient.SkuType.INAPP
        ) { billingResult, listPurchaseHistoryRecord ->
            binding.prgRestore.post {
                binding.prgRestore.visibility = View.GONE
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val items = listPurchaseHistoryRecord?.filter {
                        it.skus.contains(KEY_INAPP_SKU) || it.skus.contains(KEY_INAPP_SKU_FLASH_SALE)
                    }
                    if (items.isNullOrEmpty()) {
                        val appName = intent.getStringExtra(KEY_APP_NAME) ?: ""
                        toastMessage("Restore successfully, but you are not an $appName Pro member yet.")
                    } else {
                        toastMessage("Congratulations. Restore successfully.")
                        binding.btnBuyNow.postDelayed({
                            onUpgradeSuccess()
                        }, 200)
                    }
                } else {
                    toastMessage("Error. Try again later")
                }
            }
        }
    }


    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        // To be implemented in a later section.
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken).build()

                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { rs ->
                            val billingResponseCode = rs.responseCode
                            val billingDebugMessage = rs.debugMessage

                            Log.v("TAG_INAPP", "response code: $billingResponseCode")
                            Log.v("TAG_INAPP", "debugMessage : $billingDebugMessage")
                        }
                    }
                }
            }

            binding.btnBuyNow.postDelayed({ onUpgradeSuccess() }, 300)
        }
    }

    private var billingClient: BillingClient? = null

    private fun connectBilling() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.e("TAG", "onBillingSetupFinished: $billingResult")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    GlobalScope.launch {
                        querySkuDetails()
                    }
                    // The BillingClient is ready. You can query purchases here.
                } else toastMessage("Please try connect again later")
            }

            override fun onBillingServiceDisconnected() {
                toastMessage("Please try connect again later")
                Log.e("TAG", "onBillingServiceDisconnected")
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    suspend fun querySkuDetails() {
        val skuList = ArrayList<String>()
        skuList.add(KEY_INAPP_SKU)

        if (AdsSPManager.isFlashSale(this)) {
            skuList.add(KEY_INAPP_SKU_FLASH_SALE)
        }
        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)

        val skuDetailsResult = withContext(Dispatchers.IO) {
            billingClient?.querySkuDetails(params.build())
        }


        val skuDetailsList = skuDetailsResult?.skuDetailsList ?: listOf()
        when (skuDetailsList.size) {
            1 -> skuDetail = skuDetailsList.first()
            2 -> {
                skuDetail = skuDetailsList.first { it.sku == KEY_INAPP_SKU_FLASH_SALE }
                bindViewFlashSale(skuDetailsList.first { it.sku == KEY_INAPP_SKU })
            }
            else -> {

            }
        }

        binding.lblContent.post { bindView() }
    }

    private fun bindViewFlashSale(sku: SkuDetails) {
        binding.lblContent.post {
            binding.lblPriceDiscount.paintFlags =
                binding.lblPriceDiscount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            val percent =
                (skuDetail?.originalPriceAmountMicros ?: 0) * 100 / sku.originalPriceAmountMicros
            binding.lblPercentDiscount.text = "${percent}%"
            binding.lblPercentDiscount.visibility = View.VISIBLE
            binding.lblPriceDiscount.text = sku.price
            binding.lblPriceDiscount.visibility = View.VISIBLE
        }
    }

    private fun bindView() {
        skuDetail?.let {
            binding.lblTitle.text = it.title
            binding.lblContent.text = it.description
            binding.lblPrice.text = it.price
        }
    }

    private fun onUpgradeSuccess() {
        AdsSPManager.upgradePremium(this)
        setResult(RESULT_OK, Intent())
        finish()
    }

    private fun onLaunchPurchase() {
        if (!isNetworkAvailable()) {
            toastMessage("Please check internet connection")
            return
        }

        skuDetail?.let {
            val flowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(it)
                .build()
            billingClient?.launchBillingFlow(this, flowParams)?.responseCode
        } ?: toastMessage("Error. Please try again later ")
    }
}