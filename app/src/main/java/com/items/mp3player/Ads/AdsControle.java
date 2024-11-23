package com.items.mp3player.Ads;

import static com.items.mp3player.Constants.Enable_Ads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import com.items.mp3player.Constants;
import com.items.mp3player.R;
import com.items.mp3player.utils.SharedPrefData;


public class AdsControle {

    private SharedPrefData sharedPrefData;
    private AppLovin appLovin;
    private AdAdmob adAdmob;
    private AdsFAN adsFAN;
    ProgressDialog progressDialog;

    private Activity _activity;

    public AdsControle(Activity activity) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Ad is loading");
        progressDialog.setMessage("Please wait...");
        progressDialog.setCancelable(false);
        sharedPrefData = new SharedPrefData(activity);
        adAdmob = new AdAdmob(activity);
        adsFAN = new AdsFAN(activity);
        appLovin = new AppLovin(activity);
        this._activity = activity;

    }

    public void LoadInterstitial() {
        if (sharedPrefData.LoadBoolean(Enable_Ads)) {
            if (sharedPrefData.LoadInt("ads") % sharedPrefData.LoadInt("maxclick") == 0) {
                if (Constants.Ads_Type == 0)
                    adAdmob.LoadFullscreenAd();
                else if (Constants.Ads_Type == 1)
                    appLovin.LoadInterstitial();
                else if (Constants.Ads_Type == 2)
                    adsFAN.LoadInterstitial();
            }
        }
    }

    public void ShowInterstitial(ActionShowAds Listen) {
        if (sharedPrefData.LoadBoolean(Enable_Ads)) {
            if (sharedPrefData.LoadInt("ads") % sharedPrefData.LoadInt("maxclick")  == 0) {
                try {
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.cancel();
                            if (Constants.Ads_Type == 0)
                                adAdmob.ShowFullscreenAd(Listen);
                            else if (Constants.Ads_Type == 1)
                                appLovin.ShowInter(Listen);
                            else if (Constants.Ads_Type == 2)
                                adsFAN.ShowAds(Listen);
                        }
                    }, 2500);
                } catch (Exception e) {
                    if (Listen != null) Listen.onDone();
                }
            } else if (Listen != null) Listen.onDone();
            sharedPrefData.SaveInt("ads", sharedPrefData.LoadInt("ads") + 1);
        }else if (Listen!=null)
            Listen.onDone();
    }


    public void ShowBanner(FrameLayout layout) {
        if (sharedPrefData.LoadBoolean(Enable_Ads)) {
            if (Constants.Ads_Type == 0)
                adAdmob.BannerAd(layout);
            else if (Constants.Ads_Type == 1)
                appLovin.Showbanner(layout);
            else if (Constants.Ads_Type == 2)
                adsFAN.Showbanner(layout);
        }

    }

    public void ShowNative(View activity) {
        if (sharedPrefData.LoadBoolean(Enable_Ads))
        {
            if (Constants.Ads_Type == 0)
                adAdmob.loadNativeAd(activity);
            else if (Constants.Ads_Type == 1)
                appLovin.NativeMediumAd(activity.findViewById(R.id.native_adsContent));
            else if (Constants.Ads_Type == 2)
                adsFAN.loadNativeAd(activity);
        }

    }

}

