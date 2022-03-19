package com.mju.ar_capstone;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.ar.core.Anchor;


public class CustomDialog extends Dialog {

    private Context context;
    private CustomDialogClickListener customDialogClickListener;
    private Button dialogBtnOk, dialogBtnDelete;
    private RadioGroup dialogRdGroup;
    private RadioButton dialogRdBtnText,dialogRdBtnImg, dialogRdBtnTest;

    public enum AnchorType{
        text,
        image,
        test
    }
    AnchorType anchorType;


    public EditText dialogEdt;
    public ImageView dialogImg;
    private TextView textView;

    public CustomDialog(@NonNull Context context, CustomDialogClickListener customDialogClickListener) {
        super(context);
        this.context = context;
        this.customDialogClickListener = customDialogClickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select);


        dialogBtnOk = (Button) findViewById(R.id.dialog_btn_ok);
        dialogBtnDelete = (Button) findViewById(R.id.dialog_btn_delete);
        dialogRdGroup = (RadioGroup) findViewById(R.id.dialog_rd_group);
        dialogRdBtnText = (RadioButton) findViewById(R.id.dialog_rdbtn_text);
        dialogRdBtnImg = (RadioButton) findViewById(R.id.dialog_rdbtn_img);
        dialogRdBtnTest = (RadioButton) findViewById(R.id.dialog_rdbtn_test);
        dialogEdt = (EditText) findViewById(R.id.dialog_edt);
        dialogImg = (ImageView) findViewById(R.id.dialog_img);

        textView = (TextView) findViewById(R.id.dialog_tv_test);

        dialogRdGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.dialog_rdbtn_text){
                    textView.setText("텍스트 모드");
                    dialogEdt.setVisibility(View.VISIBLE);
                    dialogImg.setVisibility(View.GONE);
                    anchorType = anchorType.text;


                    Log.d("순서", "텍스트 모드 눌림");

                }else if(checkedId == R.id.dialog_rdbtn_img){
                    textView.setText("이미지 모드");
                    dialogImg.setVisibility(View.VISIBLE);
                    dialogEdt.setVisibility(View.GONE);
                    anchorType = anchorType.image;

                    Log.d("순서", "이미지 모드 눌림");


                }else if(checkedId == R.id.dialog_rdbtn_test){

                    dialogImg.setVisibility(View.GONE);
                    dialogEdt.setVisibility(View.GONE);
                    anchorType = anchorType.test;

                    textView.setText("테스트트트트  모드");
                }
            }
        });


        dialogBtnOk.setOnClickListener(v -> {

            String tmpText = dialogEdt.getText().toString();
            this.customDialogClickListener.onPositiveClick(tmpText, anchorType);

            dismiss();
        });

        dialogBtnDelete.setOnClickListener(v -> {
            this.customDialogClickListener.onNegativeClick();
            dismiss();
        });

        dialogImg.setOnClickListener(v -> {
            this.customDialogClickListener.onImageClick(dialogImg);
        });
    }

    public interface CustomDialogClickListener{
        void onPositiveClick(String tmpText, AnchorType anchorType);
        void onNegativeClick();

        void onImageClick(ImageView dialogImg);
    }
}
