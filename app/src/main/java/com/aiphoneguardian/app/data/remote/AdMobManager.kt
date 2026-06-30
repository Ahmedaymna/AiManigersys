package com.aiphoneguardian.app.data.remote

import android.content.Context
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdMobManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isBannerVisible = MutableStateFlow(false)
    val isBannerVisible: StateFlow<Boolean> = _isBannerVisible

    private val _isInterstitialReady = MutableStateFlow(false)
    val isInterstitialReady: StateFlow<Boolean> = _isInterstitialReady

    private val _interstitialCount = MutableStateFlow(0)
    val interstitialCount: StateFlow<Int> = _interstitialCount

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    companion object {
        const val BANNER_AD_ID = ""
        const val INTERSTITIAL_AD_ID = ""
        const val REWARDED_AD_ID = ""
        const val MAX_INTERSTITIALS_PER_SESSION = 3
        const val INTERSTITIAL_INTERVAL_MS = 30000L // 30 seconds
    }

    fun setBannerVisibility(visible: Boolean) {
        _isBannerVisible.value = visible
    }

    fun loadInterstitialAd() {
        if (INTERSTITIAL_AD_ID.isEmpty()) return
        if (_interstitialCount.value >= MAX_INTERSTITIALS_PER_SESSION) return

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    _isInterstitialReady.value = true
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    _isInterstitialReady.value = false
                }
            }
        )
    }

    fun showInterstitialAd(activity: android.app.Activity, onAdClosed: () -> Unit = {}) {
        interstitialAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    _isInterstitialReady.value = false
                    _interstitialCount.value += 1
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    _isInterstitialReady.value = false
                    onAdClosed()
                }
            }
            ad.show(activity)
        } ?: run {
            onAdClosed()
        }
    }

    fun loadRewardedAd() {
        if (REWARDED_AD_ID.isEmpty()) return

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_ID, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showRewardedAd(activity: android.app.Activity, onRewarded: () -> Unit, onAdClosed: () -> Unit = {}) {
        rewardedAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    onAdClosed()
                }
            }
            ad.show(activity) { _ ->
                onRewarded()
            }
        } ?: run {
            onAdClosed()
        }
    }

    fun canShowInterstitial(): Boolean {
        return _interstitialCount.value < MAX_INTERSTITIALS_PER_SESSION
    }

    fun resetSessionCounters() {
        _interstitialCount.value = 0
    }
}
