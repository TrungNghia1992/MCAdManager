package com.hn_ads.banner

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.hn_ads.AdsSPManager
import com.hn_ads.isNetworkAvailable


object BannerUtils {

    fun getBanner(context: Context, adUnit: String, layout: LinearLayout) {
        if (AdsSPManager.isPremium(context)) {
            layout.visibility = View.GONE
            return
        }

        val adView = AdView(context)
        adView.adUnitId = adUnit
        if (context.isNetworkAvailable()) {
            adView.adSize = AdSize.BANNER
            // Căn chỉnh layout.
            val adViewParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )
            adViewParams.addRule(RelativeLayout.CENTER_HORIZONTAL or RelativeLayout.CENTER_VERTICAL)

            // Set khoảng cách left, top, right, bottom.
            // adViewParams.setMargins(0, 4, 0, 0);
            // Căn giữa layout.
            adViewParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE)

            // Gán quảng cáo admod vào layout.
            layout.addView(adView, adViewParams)

            val adRequest = AdRequest.Builder().build()
            adView.loadAd(adRequest)
        } else {
            layout.visibility = View.GONE
        }
    }
}