package com.hn_ads.inapp

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.hn_ads.AdsSPManager
import com.hn_ads.MILLISECONDS
import com.hn_ads.databinding.ViewTimeFlashBinding
import com.hn_ads.getTimeFlash
import com.hn_ads.getTimeString


class TimeFlashView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private var countDownTimer: CountDownTimer? = null

    private val binding: ViewTimeFlashBinding =
        ViewTimeFlashBinding.inflate(LayoutInflater.from(context), this)

    fun startTimeDiscount() {
        val timeSecondsDiscount = AdsSPManager.getTimeDiscount(context)
        if (timeSecondsDiscount > 0) {
            countDownTimer =
                object : CountDownTimer(timeSecondsDiscount, MILLISECONDS) {
                    override fun onTick(time: Long) {
                        bindView(time.getTimeFlash())

                    }

                    override fun onFinish() {
                    }
                }

            countDownTimer?.start()
        }
    }

    private fun bindView(timeFlash: Triple<Int, Int, Int>) {
        binding.lblHour.text = timeFlash.first.getTimeString()
        binding.lblMinus.text = timeFlash.second.getTimeString()
        binding.lblSeconds.text = timeFlash.third.getTimeString()
    }

    override fun onDetachedFromWindow() {
        countDownTimer?.cancel()
        countDownTimer = null
        super.onDetachedFromWindow()
    }

}