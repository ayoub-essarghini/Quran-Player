package com.items.mp3player.Ads;

import static com.facebook.ads.AdSize.BANNER_HEIGHT_50;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdView;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdScrollView;
import com.facebook.ads.NativeAdsManager;
import com.items.mp3player.Constants;
import com.items.mp3player.R;
import com.items.mp3player.utils.SharedPrefData;

import java.util.ArrayList;
import java.util.List;


@SuppressLint("ResourceAsColor")
public class AdsFAN {
    private  AdView adView;
     ActionShowAds ActionShowAds;
    public  NativeAdsManager manager;
    public  NativeAdScrollView nativeAdScrollView;
    public InterstitialAd doc_interstitialAd1;
    Activity context;
    SharedPrefData sharedPrefData;

    public AdsFAN(Activity activity) {
        sharedPrefData = new SharedPrefData(activity);
        this.context= activity;
    }

    public  void Showbanner( FrameLayout layout) {
        adView = new AdView(context, sharedPrefData.LoadString("Fb_banner"), BANNER_HEIGHT_50);
        AdListener adListener = new AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                Log.d("fb", "facebook banner failed" + adError.getErrorMessage());

            }

            @Override
            public void onAdLoaded(Ad ad) {
                layout.setVisibility(View.VISIBLE);
                Log.d("fb", "facebook banner loaded");
            }

            @Override
            public void onAdClicked(Ad ad) {
                adView.destroy();
                Log.d("fb", "facebook banner clicked");
            }

            @Override
            public void onLoggingImpression(Ad ad) {

            }
        };
        ;
        AdView.AdViewLoadConfig loadAdConfig = adView.buildLoadAdConfig()
                .withAdListener(adListener)
                .build();
        adView.loadAd(loadAdConfig);
        layout.addView(adView);
        Log.d("FAN", "LoadBanner");
    }

    public  void LoadInterstitial() {

        Log.d("Fan Ads", "Load Inter");
        doc_interstitialAd1 = new InterstitialAd(context,sharedPrefData.LoadString("Fb_inter"));
        InterstitialAdListener interstitialAdListener = new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {

            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                if (ActionShowAds != null) {
                    ActionShowAds.onDone();
                }
             //   doc_interstitialAd1.loadAd();

                //======code here===========
            }

            @Override
            public void onError(Ad ad, AdError adError) {
              /*  if (ActionShowAds != null) {
                    ActionShowAds.onDone();
                }*/
                Log.d("Fan Ads", "Load Inter failed" + adError);
            }

            @Override
            public void onAdLoaded(Ad ad) {
            }

            @Override
            public void onAdClicked(Ad ad) {
            }

            @Override
            public void onLoggingImpression(Ad ad) {
            }
        };
        doc_interstitialAd1.loadAd(
                doc_interstitialAd1.buildLoadAdConfig()
                        .withAdListener(interstitialAdListener)
                        .build());

    }

    public  void ShowAds(ActionShowAds li) {
        ActionShowAds = li;
        try {
            if (doc_interstitialAd1.isAdLoaded()) {
                Log.d("FAN", "Show INTER");
                doc_interstitialAd1.show();
            } else if (li != null)
                li.onDone();
        } catch (Exception e) {
            if (li != null)
                li.onDone();
            Log.d("FAN", " Crash INTER");
        }


    }

    public boolean isLoad() {
        return doc_interstitialAd1.isAdLoaded();
    }

       String TAG = "Tag";
    private  NativeAd nativeAd;
    private  LinearLayout ladView;

    public  void loadNativeAd(View view) {
        NativeAdLayout nativeAdLayout= view.findViewById(R.id.native_ad_container);
        nativeAd = new NativeAd(context, sharedPrefData.LoadString("Fb_native"));
        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                view.findViewById(R.id.shimmer).setVisibility(View.GONE);
                // Inflate Native Ad into Container
                inflateAdd(nativeAd, nativeAdLayout,context);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        };
        // Request an ad
        nativeAd.loadAd(nativeAd.buildLoadAdConfig().withAdListener(nativeAdListener).build());
    }

    private  void inflateAdd(NativeAd nativeAd, NativeAdLayout nativeAdLayout,Activity context) {
        nativeAd.unregisterView();
        // Add the Ad view into the ad container.
        LayoutInflater inflater = LayoutInflater.from(context);
        // Inflate the Ad view. The layout referenced should be the one you created in the last step.
        ladView = (LinearLayout) inflater.inflate(R.layout.custom_native_ad_layout, nativeAdLayout, false);
        nativeAdLayout.addView(ladView);
        // Add the AdOptionsView
        LinearLayout adChoicesContainer = context.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);
        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = ladView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = ladView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = ladView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = ladView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = ladView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = ladView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = ladView.findViewById(R.id.native_ad_call_to_action);
        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());
        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);
        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                ladView,
                nativeAdMedia,
                nativeAdIcon,
                clickableViews);
        ladView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ladView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

              //  Log.d("Native height na ", "" + pxtodp(ladView.getHeight()));

            }
        });

    }
}
