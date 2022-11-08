package com.nalakstudio.smsreceive.CustomViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nalakstudio.smsreceive.R;

public class MyCustomDialog extends AlertDialog.Builder {

    public MyCustomDialog(Context context, String title, String message) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewDialog = inflater.inflate(R.layout.dialog, null, false);

        TextView titleTextView = (TextView) viewDialog.findViewById(R.id.title);
        titleTextView.setText(title);
        TextView messageTextView = (TextView) viewDialog.findViewById(R.id.message);
        messageTextView.setText(message);

        this.setCancelable(false);

        this.setView(viewDialog);

    }
}
