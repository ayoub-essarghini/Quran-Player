package com.items.mp3player.Ads;


import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.NativeAd;
import com.items.mp3player.Constants;
import com.items.mp3player.R;
import com.items.mp3player.utils.SharedPrefData;


public class AdAdmob {
    //ProgressDialog ProgressDialog;
    Activity mActivity;
    private InterstitialAd mInterstitialAd;
    ActionShowAds mAction;
    SharedPrefData sharedPrefData;
    public AdAdmob(Activity activity) {
        mActivity = activity;
        sharedPrefData = new SharedPrefData(activity);
        MobileAds.initialize(activity, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus
                                                         initializationStatus) {
            }
        });


    }


    public void BannerAd( FrameLayout Ad_Layout) {


        AdView mAdView = new AdView(mActivity);
        mAdView.setAdSize(AdSize.BANNER);
        mAdView.setAdUnitId(sharedPrefData.LoadString("admob_banner"));
        AdRequest adore = new AdRequest.Builder().build();
        mAdView.loadAd(adore);
        Ad_Layout.addView(mAdView);


        mAdView.setAdListener(new AdListener() {

            @Override
            public void onAdLoaded() {
                Ad_Layout.setVisibility(View.VISIBLE);
                super.onAdLoaded();

            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();

            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                mAdView.destroy();

            }
        });


    }

    public void LoadFullscreenAd() {
        // TODO Auto-generated method stub
            AdRequest adRequest = new AdRequest.Builder().build();

            InterstitialAd.load(mActivity, sharedPrefData.LoadString("admob_inter"), adRequest,
                    new InterstitialAdLoadCallback() {
                        @Override
                        public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                            mInterstitialAd = interstitialAd;
                            //   ProgressDialog.dismiss();
                                Log.d("admob_inter", "loaded successfully");

                        }

                        @Override
                        public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                            // Handle the error
                            // ProgressDialog.dismiss();
                            Log.d("admob_inter", "loaded failed");

                        }
                    });



    }

    public void ShowFullscreenAd(ActionShowAds actionShowAds) {
        mAction = actionShowAds;
        if (mInterstitialAd != null) {
           // Ad_Popup(mActivity);
            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdClicked() {
                    // Called when a click is recorded for an ad.
                    Log.d("TAG", "Ad was clicked.");
                }

                @Override
                public void onAdDismissedFullScreenContent() {
                    // Called when ad is dismissed.
                    // Set the ad reference to null so you don't show the ad a second time.
                    Log.d("TAG", "Ad dismissed fullscreen content.");
                    if (mAction != null)
                        mAction.onDone();
                   // ProgressDialog.cancel();
                    mInterstitialAd = null;

                }

                @Override
                public void onAdFailedToShowFullScreenContent(AdError adError) {
                    // Called when ad fails to show.
                    //ProgressDialog.cancel();
                    mInterstitialAd = null;
                    Log.e("TAG", "Ad failed to show fullscreen content.");
                }

                @Override
                public void onAdImpression() {
                    // Called when an impression is recorded for an ad.
                    Log.d("TAG", "Ad recorded an impression.");
                }

                @Override
                public void onAdShowedFullScreenContent() {
                    // Called when ad is shown.
                    Log.d("TAG", "Ad showed fullscreen content.");
                }
            });

            // wait 2 second to show ads
            mInterstitialAd.show(mActivity);


        } else if (mAction != null)
            mAction.onDone();
    }




    public void loadNativeAd(View view) {
        TemplateView templateView= view.findViewById(R.id.AdmobNative);
        AdLoader adLoader = new AdLoader.Builder(mActivity, sharedPrefData.LoadString("admob_native"))
                .forNativeAd(new NativeAd.OnNativeAdLoadedListener() {
                    @Override
                    public void onNativeAdLoaded(NativeAd nativeAd) {
                        view.findViewById(R.id.shimmer).setVisibility(View.GONE);
                        templateView.setVisibility(View.VISIBLE);
                        templateView.setNativeAd(nativeAd);
                    }
                })
                .build();

        adLoader.loadAd(new AdRequest.Builder().build());

    }


}
