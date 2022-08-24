package com.hn_ads

import android.content.Context
import android.content.SharedPreferences
import kotlin.random.Random

object AdsSPManager {
    private const val KEY_TIME_ADS = "KEY_TIME_ADS"

    private const val PREMIUM_UPGRADE = "FILE_PREMIUM_UPGRADE"

    private const val KEY_TIME_DISCOUNT = "KEY_TIME_DISCOUNT"
    private const val TIME_FLASH_SALE = 10800 * 1000
    private val dayRestartFlashSale by lazy {
        Random.nextInt(1, 3) * (24 * 60 * 60 * 1000)
    }
    private const val KEY_DELAY_FLASH_DIALOG = "KEY_DELAY_FLASH_DIALOG"
    private const val DAY_DELAY_FLASH_DIALOG = 1 * (24 * 60 * 60 * 1000)

    private fun getSP(c: Context): SharedPreferences {
        return c.getSharedPreferences(c.packageName, Context.MODE_PRIVATE)
    }

    fun updateFlashDialog(context: Context) {
        getSP(context)
            .edit()
            .putLong(KEY_DELAY_FLASH_DIALOG, System.currentTimeMillis() + DAY_DELAY_FLASH_DIALOG)
            .apply()
    }

    private fun isTimeFlashDialogValid(context: Context): Boolean {
        val sp = getSP(context)
        val time = sp.getLong(KEY_DELAY_FLASH_DIALOG, 0L)
        return time < System.currentTimeMillis()
    }

    fun updateInterstitialShowTime(c: Context, timeDelay: Int) {
        val sp = getSP(c)
        sp.edit()
            .putLong(KEY_TIME_ADS, System.currentTimeMillis() + timeDelay.convertToMilliseconds())
            .apply()
    }

    fun isTimeValid(c: Context?): Boolean {
        return c?.let {
            val sp = getSP(it)
            val time = sp.getLong(KEY_TIME_ADS, 0L)
            time < System.currentTimeMillis()
        } ?: false
    }

    fun upgradePremium(c: Context) {
        getSP(c)
            .edit()
            .putBoolean(PREMIUM_UPGRADE, true)
            .apply()
    }

    fun isPremium(context: Context?): Boolean {
        return context?.let {
            getSP(it).getBoolean(PREMIUM_UPGRADE, false)
        } ?: false
    }

    fun initTimeDiscount(context: Context, timeFlashSale: Int = TIME_FLASH_SALE) {
        val sp = getSP(context)
        val timeDiscount = sp.getLong(KEY_TIME_DISCOUNT, 0L)
        val isStart =
            timeDiscount == 0L || (System.currentTimeMillis() > (timeDiscount + dayRestartFlashSale))
        if (isStart) {
            sp.edit()
                .putLong(
                    KEY_TIME_DISCOUNT,
                    System.currentTimeMillis() + timeFlashSale
                )
                .apply()
        }
    }

    fun isFlashSale(context: Context): Boolean {
        val sp = getSP(context)
        val time = sp.getLong(KEY_TIME_DISCOUNT, 0L) - System.currentTimeMillis()

        return time > 0
    }

    fun getTimeDiscount(context: Context): Long {
        val sp = getSP(context)
        return sp.getLong(KEY_TIME_DISCOUNT, 0L) - System.currentTimeMillis()
    }

    fun isValidShowAd(context: Context?): Boolean {
        return context?.let {
            context.isNetworkAvailable() &&
                    isTimeValid(it) &&
                    !isPremium(it)
        } ?: false
    }

    fun isValidShowFlashSale(context: Context): Boolean {
        return isFlashSale(context) && isTimeFlashDialogValid(context)
    }
}