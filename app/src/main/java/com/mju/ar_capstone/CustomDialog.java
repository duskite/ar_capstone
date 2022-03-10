package com.mju.ar_capstone;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;



public class CustomDialog extends Dialog {

    private Context context;
    private CustomDialogClickListener customDialogClickListener;
    private TextView tvTitle, tvNegative, tvPositive;
    private EditText edtDialog;

    public CustomDialog(@NonNull Context context, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.context = context;
        this.customDialogClickListener = customDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_arsf);

        tvTitle = (TextView) findViewById(R.id.option_codetype_dialog_title_tv);
        tvPositive = (TextView) findViewById(R.id.option_codetype_dialog_positive);
        tvNegative = (TextView) findViewById(R.id.option_codetype_dialog_negative);
        edtDialog = (EditText) findViewById(R.id.edtDialog);

        tvPositive.setOnClickListener(v -> {
            this.customDialogClickListener.onPositiveClick();

            dismiss();
        });

        tvNegative.setOnClickListener(v -> {
            this.customDialogClickListener.onNegativeClick();

            dismiss();
        });
    }

    public interface CustomDialogClickListener{
        void onPositiveClick();
        void onNegativeClick();
    }
}
