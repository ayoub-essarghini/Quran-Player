package com.items.mp3player.Ads;

import static com.items.mp3player.Constants.Ads_Type;
import static com.items.mp3player.Constants.JSON_URL;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.ads.Ad;
import com.items.mp3player.utils.SharedPrefData;


import org.json.JSONException;
import org.json.JSONObject;

public class GetLoadAds {

    // todo change link json
    Context mContext;
    ActionListener Mylistener;
    SharedPrefData sharedPrefData;

    public GetLoadAds(Context context, ActionListener listener) {
        mContext = context;
        sharedPrefData = new SharedPrefData(context);
        getMyIdsFromServers();
        Mylistener = listener;
    }

    private void getMyIdsFromServers() {
        RequestQueue requestQueue = Volley.newRequestQueue(mContext);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, JSON_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONObject object = response.getJSONObject("QuranData");
                    sharedPrefData.SaveInt("maxclick", object.getInt("maxclick"));

                    sharedPrefData.SaveString("Fb_banner", object.getString("Fb_banner"));
                    sharedPrefData.SaveString("Fb_inter", object.getString("Fb_inter"));
                    sharedPrefData.SaveString("Fb_native", object.getString("Fb_native"));

                    sharedPrefData.SaveString("applovin_banner", object.getString("applovin_banner"));
                    sharedPrefData.SaveString("applovin_inter", object.getString("applovin_inter"));
                    sharedPrefData.SaveString("applovin_native", object.getString("applovin_native"));
                    //ADMOB
                    sharedPrefData.SaveString("admob_banner", object.getString("admob_banner"));
                    sharedPrefData.SaveString("admob_inter", object.getString("admob_inter"));
                    sharedPrefData.SaveString("admob_native", object.getString("admob_native"));

                    sharedPrefData.SaveBoolean("enableAdmob", object.getBoolean("admob"));
                    sharedPrefData.SaveBoolean("enableFb", object.getBoolean("fan"));
                    sharedPrefData.SaveBoolean("enableApplovin", object.getBoolean("applovin"));

                    if (sharedPrefData.LoadBoolean("enableAdmob"))
                        Ads_Type = 0;
                    else  if (sharedPrefData.LoadBoolean("enableApplovin"))
                        Ads_Type = 1;
                    else if (sharedPrefData.LoadBoolean("enableFb"))
                        Ads_Type = 2;
                    else
                        Ads_Type = -1;

                    if (Mylistener != null)
                        Mylistener.onDone();

                } catch (JSONException e) {
                    e.printStackTrace();
                    getMyIdsFromServers();

                    Toast.makeText(mContext, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                getMyIdsFromServers();
                if (Mylistener != null)
                    Mylistener.onFailed();
                Log.d("getAds", "error" + error.getMessage());
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}
