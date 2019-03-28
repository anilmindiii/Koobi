package com.mualab.org.user.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.mualab.org.user.R;

/**
 * Created by mindiii on 24/9/18.
 */

public class CustomAlertDialog extends AlertDialog.Builder {

    public CustomAlertDialog(Context context, String title, String message) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View viewDialog = inflater.inflate(R.layout.dialog_simple, null, false);

        TextView titleTextView =viewDialog.findViewById(R.id.title);
        titleTextView.setText(title);
        TextView messageTextView = viewDialog.findViewById(R.id.message);
        messageTextView.setText(message);

        this.setCancelable(false);

        this.setView(viewDialog);

    } }
