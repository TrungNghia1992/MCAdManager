package com.hn_ads.inapp

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.widget.LinearLayoutCompat
import com.hn_ads.AdsSPManager
import com.hn_ads.MILLISECONDS
import com.hn_ads.R
import com.hn_ads.databinding.ViewUpgradeBinding
import com.hn_ads.getTimeDiscount


class UpgradeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private var countDownTimer: CountDownTimer? = null

    private val binding: ViewUpgradeBinding =
        ViewUpgradeBinding.inflate(LayoutInflater.from(context), this)

    fun startTimeDiscount() {
        val timeSecondsDiscount = AdsSPManager.getTimeDiscount(context)
        if (timeSecondsDiscount > 0) {
            countDownTimer =
                object : CountDownTimer(timeSecondsDiscount, MILLISECONDS) {
                    override fun onTick(time: Long) {
                        binding.lblTimeDiscount.text = time.getTimeDiscount()

                    }

                    override fun onFinish() {
                        binding.lblTimeDiscount.text = "Upgrade"
                    }
                }

            val animation: Animation =
                AnimationUtils.loadAnimation(context, R.anim.hn_scale_anim)
            binding.imgCrown.startAnimation(animation)

            countDownTimer?.start()
        }
    }

    fun onCleanDiscount() {
        countDownTimer?.cancel()
        countDownTimer = null
        binding.imgCrown.clearAnimation()
    }

}