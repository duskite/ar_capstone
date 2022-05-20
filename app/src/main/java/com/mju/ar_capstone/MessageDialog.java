package com.mju.ar_capstone;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;


public class MessageDialog extends Dialog {

    private Context context;
    private String msg;
    private CustomDialogClickListener customDialogClickListener;
    private Button dialogBtnOk, dialogBtnCancel;
    public MessageDialog(@NonNull Context context, String msg, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.msg=msg;
        this.context = context;
        this.customDialogClickListener = customDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_msg);
        dialogBtnOk = findViewById(R.id.dialog_btn_ok);
        ((TextView) findViewById(R.id.dialog_tv_test)).setText(msg);
        dialogBtnOk.setOnClickListener(v -> {
            this.customDialogClickListener.onPositiveClick(true);
            dismiss();
        });
        dialogBtnCancel = findViewById(R.id.dialog_btn_cancel);
        dialogBtnCancel.setOnClickListener(v -> {
            this.customDialogClickListener.onPositiveClick(false);
            dismiss();
        });
    }

    //이 부분 구현하다가 말았길래 마저 구현함
    // true - 삭제
    // false - 닫기
    public interface CustomDialogClickListener{
        void onPositiveClick(boolean b);
    }
}
