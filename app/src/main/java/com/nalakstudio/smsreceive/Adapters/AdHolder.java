package com.nalakstudio.smsreceive.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeAdView;
import com.appodeal.ads.NativeMediaView;
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall;
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream;
import com.appodeal.ads.native_ad.views.NativeAdViewNewsFeed;
import com.appodeal.ads.native_ad.views.a;
import com.my.target.nativeads.NativeAppwallAd;
import com.nalakstudio.smsreceive.LoadMessages;
import com.nalakstudio.smsreceive.MainActivity;
import com.nalakstudio.smsreceive.PhoneObject;
import com.nalakstudio.smsreceive.R;

import org.json.JSONException;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

public class AdHolder extends RecyclerView.ViewHolder {

    View itemView;
    Context context;
    ViewGroup viewGroup;
    NativeAdViewNewsFeed nativeAdViewNewsFeed;
    NativeAdViewAppWall nativeAdViewAppWall;
    NativeAdViewContentStream nativeAdViewContentStream;

    public AdHolder(@NonNull View itemView, Context context, ViewGroup viewGroup) {
        super(itemView);
        this.context = context;
        this.itemView = itemView;
        this.viewGroup=viewGroup;
        nativeAdViewNewsFeed= new NativeAdViewNewsFeed(context);
        nativeAdViewAppWall = new NativeAdViewAppWall(context);
        nativeAdViewContentStream = new NativeAdViewContentStream(context);
        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        RecyclerView.LayoutParams layoutParams = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics()));
        layoutParams.bottomMargin= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        itemView.setLayoutParams(layoutParams);
    }

    public void bind(NativeAd nativeAd, boolean b) {
        LinearLayout linearLayout =itemView.findViewById(R.id.container);
        linearLayout.removeAllViews();
        NativeAdView nativeAdView = new NativeAdViewAppWall(context);
        ((NativeAdViewAppWall) nativeAdView).setNativeAd(nativeAd);
        if (b){
            nativeAdView=nativeAdViewContentStream;
            ((NativeAdViewContentStream) nativeAdView).setNativeAd(nativeAd);
        }

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        nativeAdView.setLayoutParams(layoutParams);
        nativeAdView.setNativeIconView(((a) nativeAdView).getNativeIconView());
        if (nativeAd.containsVideo()){
            NativeMediaView nativeMediaView = new NativeMediaView(context);
            nativeAdView.setNativeMediaView(nativeMediaView);
        }
        linearLayout.addView(nativeAdView);

    }


}
