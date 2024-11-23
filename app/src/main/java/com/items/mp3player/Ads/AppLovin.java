package com.items.mp3player.Ads;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.applovin.mediation.MaxAd;
import com.applovin.mediation.MaxAdViewAdListener;
import com.applovin.mediation.MaxError;
import com.applovin.mediation.ads.MaxAdView;
import com.applovin.mediation.ads.MaxInterstitialAd;
import com.applovin.mediation.nativeAds.MaxNativeAdListener;
import com.applovin.mediation.nativeAds.MaxNativeAdLoader;
import com.applovin.mediation.nativeAds.MaxNativeAdView;
import com.items.mp3player.Constants;
import com.items.mp3player.R;
import com.items.mp3player.utils.SharedPrefData;

import java.util.concurrent.TimeUnit;


@SuppressLint("ResourceAsColor")
public class AppLovin {
    Activity context;
    private MaxAdView adView2;
    private MaxInterstitialAd interstitialAd;
    ActionShowAds ActionShowAds;
    private int retryAttempt;
    private MaxNativeAdLoader nativeAdLoader;
    private MaxAd nativeAd;
    SharedPrefData sharedPrefData;
    public AppLovin(Activity cont) {
        this.context = cont;
        sharedPrefData = new SharedPrefData(cont);
        interstitialAd = new MaxInterstitialAd(sharedPrefData.LoadString("admob_banner"), context);

    }
    public void Showbanner(FrameLayout layout) {
        try {
            adView2 = new MaxAdView(sharedPrefData.LoadString("applovin_banner"), context);
            // Stretch to the width of the screen for banners to be fully functional
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            // Banner height on phones and tablets is 50 and 90, respectively
            int heightPx = context.getResources().getDimensionPixelSize(R.dimen.banner_height);
            adView2.setLayoutParams(new FrameLayout.LayoutParams(width, heightPx));
            adView2.loadAd();
            ((ViewGroup) layout.getParent()).removeView(adView2);
            layout.addView(adView2);
            adView2.setListener(new MaxAdViewAdListener() {
                @Override
                public void onAdExpanded(MaxAd ad) {

                }

                @Override
                public void onAdCollapsed(MaxAd ad) {

                }

                @Override
                public void onAdLoaded(MaxAd ad) {
                    layout.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAdDisplayed(MaxAd ad) {

                }

                @Override
                public void onAdHidden(MaxAd ad) {

                }

                @Override
                public void onAdClicked(MaxAd ad) {

                }

                @Override
                public void onAdLoadFailed(String adUnitId, MaxError error) {

                }

                @Override
                public void onAdDisplayFailed(MaxAd ad, MaxError error) {

                }
            });
        } catch (Exception e) {
            Log.d("Applovin", "Crash LoadBanner");
        }


        Log.d("Applovin", "LoadBanner");
    }

    public void NativeMediumAd(LinearLayout nativeAdLayout) {
        try {
            nativeAdLoader = new MaxNativeAdLoader(sharedPrefData.LoadString("applovin_native"), context);
            nativeAdLoader.setNativeAdListener(new MaxNativeAdListener() {
                @Override
                public void onNativeAdLoaded(final MaxNativeAdView nativeAdView, final MaxAd ad) {
                    // logAnonymousCallback();
                    // Cleanup any pre-existing native ad to prevent memory leaks.
                    if (nativeAd != null) {
                        nativeAdLoader.destroy(nativeAd);
                    }
                    // Save ad for cleanup.
                    nativeAd = ad;
                    // Add ad view to view.
                    nativeAdLayout.removeAllViews();
                    context.findViewById(R.id.shimmer).setVisibility(View.GONE);
                    nativeAdLayout.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params = nativeAdLayout.getLayoutParams();
                    params.height = dpToPx(300,context);
                    nativeAdLayout.setLayoutParams(params);
                    nativeAdLayout.addView(nativeAdView);
                    nativeAdView.setBackgroundColor(Color.WHITE);
                }

                @Override
                public void onNativeAdLoadFailed(final String adUnitId, final MaxError error) {
                    //   logAnonymousCallback();
                    Log.d("Error native", " detain error" + error.toString());
                }

                @Override
                public void onNativeAdClicked(final MaxAd ad) {
                    //  logAnonymousCallback();
                }
            });
            nativeAdLoader.loadAd();
        } catch (Exception e) {
            Log.d("Applovin", "Crash Native Medium");

        }


    }

    public static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
    public void LoadInterstitial() {
        try {
            interstitialAd = new MaxInterstitialAd(sharedPrefData.LoadString("applovin_inter"), context);
            interstitialAd.setListener(new MaxAdViewAdListener() {
                @Override
                public void onAdLoaded(final MaxAd maxAd) {
                    // Interstitial ad is ready to be shown. interstitialAd.isReady() will now return 'true'
                    Log.d("Applovin", "Load INTER is ready");
                    // Reset retry attempt
                    retryAttempt = 0;
                }

                @Override
                public void onAdLoadFailed(final String adUnitId, final MaxError error) {
                    // Interstitial ad failed to load
                    // We recommend retrying with exponentially higher delays up to a maximum delay (in this case 64 seconds)
                    Log.d("Applovin", "failed INTER" + error.getMessage());
                    retryAttempt++;
                    long delayMillis = TimeUnit.SECONDS.toMillis((long) Math.pow(2, Math.min(6, retryAttempt)));

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            interstitialAd.loadAd();
                        }
                    }, delayMillis);
                }

                @Override
                public void onAdDisplayFailed(final MaxAd maxAd, final MaxError error) {
                    // Interstitial ad failed to display. We recommend loading the next ad
                    interstitialAd.loadAd();
                }

                @Override
                public void onAdDisplayed(final MaxAd maxAd) {
                }

                @Override
                public void onAdClicked(final MaxAd maxAd) {
                }

                @Override
                public void onAdHidden(final MaxAd maxAd) {
                    // Interstitial ad is hidden. Pre-load the next ad
                     interstitialAd.loadAd();
                    if (ActionShowAds != null)
                        ActionShowAds.onDone();
                }

                @Override
                public void onAdExpanded(MaxAd ad) {

                }

                @Override
                public void onAdCollapsed(MaxAd ad) {

                }
            });
            // Load the first ad
            interstitialAd.loadAd();
        }catch (Exception e){
            Log.d("Applovin", " Crash INTER");
        }

    }

    public void ShowInter(ActionShowAds li) {
        ActionShowAds = li;
        try {
            if (interstitialAd.isReady()) {
                Log.d("Applovin", "Show INTER");
                interstitialAd.showAd();
            } else if (li != null)
                li.onDone();
        }catch (Exception e){
            if (li != null)
                li.onDone();
            Log.d("Applovin", " Crash INTER");
        }


    }

    public boolean isLoad() {
        return interstitialAd.isReady();
    }

}

