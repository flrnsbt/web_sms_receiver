package com.nalakstudio.smsreceive.Adapters;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.nalakstudio.smsreceive.LoadMessages;
import com.nalakstudio.smsreceive.MainActivity;
import com.nalakstudio.smsreceive.PhoneObject;
import com.nalakstudio.smsreceive.R;

import org.json.JSONException;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;

public class PhoneObjectHolder extends RecyclerView.ViewHolder {

    private TextView country, phone_number, last_updated;
    private ImageView flag, copy;
    View itemView;
    Context context;
    LinearLayout clickToCopy;
    ViewGroup viewGroup;

    public PhoneObjectHolder(@NonNull View itemView, Context context, ViewGroup viewGroup) {
        super(itemView);
        this.context = context;
        this.itemView = itemView;
        clickToCopy=itemView.findViewById(R.id.clicktocopy);
        this.viewGroup=viewGroup;
        copy=itemView.findViewById(R.id.copy);
        copy.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
        flag = itemView.findViewById(R.id.flag);
        country = itemView.findViewById(R.id.country);
        phone_number = itemView.findViewById(R.id.phone_number);
        last_updated = itemView.findViewById(R.id.last_updated);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, context.getResources().getDisplayMetrics()));
        layoutParams.bottomMargin= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, context.getResources().getDisplayMetrics());
        itemView.findViewById(R.id.linearLayout).setLayoutParams(layoutParams);
    }

    public void bind(final PhoneObject phoneObject) throws JSONException {

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((MainActivity)context).getPhoneNumberClicked()%4==0){
                    Appodeal.show((Activity) context, Appodeal.INTERSTITIAL);
                }
                Intent intent = new Intent(context, LoadMessages.class);
                intent.putExtra("URL", phoneObject.getUrl());
                context.startActivity(intent);
                ((MainActivity)context).setPhoneNumberClicked(((MainActivity)context).getPhoneNumberClicked()+1);
            }
        });
        clickToCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("phone number", phone_number.getText().toString());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Phone Number copied to clipboard",
                        Toast.LENGTH_SHORT).show();
            }
        });
        country.setText(phoneObject.getCountry());
        phone_number.setText(formatPhoneNumber(phoneObject.getPhone_number(),phoneObject.getCountry_code()));
        last_updated.setText(phoneObject.getTimeAgo(Long.parseLong(phoneObject.getAdded_on()),context));
        flag.setImageResource(context.getResources().getIdentifier(phoneObject.getCountry_code().toLowerCase(),"drawable", context.getPackageName()));
    }

    public String formatPhoneNumber(String number, String country_code){
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.createInstance(context);
        try {
            return phoneNumberUtil.format(phoneNumberUtil.parse(number,country_code), PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return number;
    }

}
