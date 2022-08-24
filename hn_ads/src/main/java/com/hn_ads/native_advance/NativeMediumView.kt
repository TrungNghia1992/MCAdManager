package com.hn_ads.native_advance

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.hn_ads.AdsSPManager
import com.hn_ads.databinding.HnNativeMediumAdBinding
import com.hn_ads.isNetworkAvailable


class NativeMediumView(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val binding: HnNativeMediumAdBinding =
        HnNativeMediumAdBinding.inflate(LayoutInflater.from(context), this)

    private var nativeAd: NativeAd? = null

    private fun showDefault(idImage: Int? = null) {
        if (idImage != null) {
            binding.imgDefault.visibility = View.VISIBLE
            binding.imgDefault.setImageResource(idImage)

            binding.nativeAdView.visibility = GONE
            binding.viewPlaceholder.visibility = View.GONE
        } else {
            this@NativeMediumView.visibility = GONE
        }
    }

    fun loadAds(idUnitAd: String, idImage: Int? = null) {
        this.visibility = VISIBLE
        if (!context.isNetworkAvailable() || AdsSPManager.isPremium(context)) {
            showDefault(idImage)
            return
        }

        val adLoader = AdLoader.Builder(context, idUnitAd)
            .forNativeAd {
                setNativeAd(it)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    // TODO: Show default ads team.
                    showDefault(idImage)
                }
            })
            .withNativeAdOptions(NativeAdOptions.Builder().build())
            .build()

        adLoader.loadAd(AdRequest.Builder().build())
    }

    private fun setNativeAd(nativeAd: NativeAd) {
        this.nativeAd = nativeAd

        with(binding) {
            nativeAdView.visibility = VISIBLE
            nativeAdView.callToActionView = callToActionView
            nativeAdView.headlineView = primaryView
            nativeAdView.mediaView = mediaView
            primaryView.text = nativeAd.headline
            callToActionView.text = nativeAd.callToAction
            tertiaryView.text = nativeAd.body
            nativeAdView.bodyView = tertiaryView

            if (nativeAd.icon != null) {
                iconView.visibility = VISIBLE
                iconView.setImageDrawable(nativeAd.icon.drawable)
            } else {
                iconView.visibility = GONE
            }

            imgDefault.visibility = View.GONE
            if (viewPlaceholder.isShimmerStarted) {
                viewPlaceholder.visibility = View.GONE
                viewPlaceholder.stopShimmer()
            }

            //  Set the secondary view to be the star rating if available.
            //  Set the secondary view to be the star rating if available.
            val starRating = nativeAd.starRating
            if (starRating != null && starRating > 0) {
                secondaryView.visibility = GONE
                ratingBar.visibility = VISIBLE
                ratingBar.rating = starRating.toFloat()
                nativeAdView.starRatingView = ratingBar
            } else {
                secondaryView.text = getSecondaryText(nativeAd)
                secondaryView.visibility = VISIBLE
                ratingBar.visibility = GONE
            }

            nativeAdView.setNativeAd(nativeAd)
        }
    }

    private fun getSecondaryText(nativeAd: NativeAd): String {
        return if (adHasOnlyStore(nativeAd)) {
            binding.nativeAdView.storeView = binding.secondaryView
            nativeAd.store
        } else if (!TextUtils.isEmpty(nativeAd.advertiser)) {
            binding.nativeAdView.advertiserView = binding.secondaryView
            nativeAd.advertiser
        } else {
            ""
        }
    }

    private fun adHasOnlyStore(nativeAd: NativeAd): Boolean {
        return !TextUtils.isEmpty(nativeAd.store) && TextUtils.isEmpty(nativeAd.advertiser)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        binding.viewPlaceholder.visibility = VISIBLE
        binding.viewPlaceholder.startShimmer()
    }
}