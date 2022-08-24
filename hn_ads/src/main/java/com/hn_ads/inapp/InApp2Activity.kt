package com.hn_ads.inapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import com.hn_ads.AdsSPManager
import com.hn_ads.databinding.ActivityHnInApp2Binding
import com.hn_ads.isNetworkAvailable
import com.hn_ads.toastMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

class InApp2Activity : AppCompatActivity() {
    private var _binding: ActivityHnInApp2Binding? = null
    private val binding get() = _binding!!

    private var skuDetail: SkuDetails? = null

    companion object {
        private const val KEY_APP_NAME = "KEY_APP_NAME"
        private const val KEY_INAPP_SKU = "file_premium_upgrade"
        private const val KEY_INAPP_SKU_FLASH_SALE = "file_premium_upgrade_flash_sale"

        fun createIntent(context: Context, appName: String) =
            Intent(context, InApp2Activity::class.java).apply {
                putExtra(KEY_APP_NAME, appName)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityHnInApp2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        connectBilling()
        initAction()
    }

    private fun initAction() {
        binding.imgClose.setOnClickListener {
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

        binding.prgLoading.visibility = View.VISIBLE
        billingClient?.queryPurchaseHistoryAsync(
            BillingClient.SkuType.INAPP
        ) { billingResult, listPurchaseHistoryRecord ->
            binding.prgLoading.post {
                binding.prgLoading.visibility = View.GONE
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
        binding.prgLoading.visibility = View.VISIBLE

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

                dismissLoading()
            }

            override fun onBillingServiceDisconnected() {
                toastMessage("Please try connect again later")
                Log.e("TAG", "onBillingServiceDisconnected")
                dismissLoading()
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private fun dismissLoading() {
        binding.prgLoading.post {
            binding.prgLoading.visibility = View.GONE
        }
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
        var skuOld: SkuDetails? = null
        when (skuDetailsList.size) {
            1 -> skuDetail = skuDetailsList.first()
            2 -> {
                skuDetail = skuDetailsList.first { it.sku == KEY_INAPP_SKU_FLASH_SALE }
                skuOld = skuDetailsList.first { it.sku == KEY_INAPP_SKU }
            }
            else -> {

            }
        }

        binding.ctlPayment.post { bindView(skuOld) }
    }

    private fun bindView(skuOld: SkuDetails?) {
        // Co flash sale.
        if (skuOld !== null) {
            val percent =
                (skuDetail?.originalPriceAmountMicros
                    ?: 0).toFloat() * 100 / skuOld.originalPriceAmountMicros.toFloat()
            val percentText = if (percent > 0) "${percent.roundToInt()}" else "-"
            binding.ctlNormal.visibility = View.VISIBLE
            binding.lblPercentDiscount.visibility = View.VISIBLE
            binding.lblPercentDiscount.text = "$percentText%"
            binding.lblNormalPrice.text = skuOld.price
            binding.viewTimeFlash.visibility = View.VISIBLE
            binding.viewTimeFlash.startTimeDiscount()
            binding.lblPayment.text = "Flash Sale"
        } else {
            binding.viewTimeFlash.visibility = View.GONE
            binding.ctlNormal.visibility = View.GONE
            binding.lblPercentDiscount.visibility = View.GONE
            binding.lblPayment.text = "Best Offer"
        }

        skuDetail?.let {
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