package com.mju.ar_capstone.bottomsheets;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mju.ar_capstone.InventoryActivity;
import com.mju.ar_capstone.MainActivity;
import com.mju.ar_capstone.R;

public class BottomSheetChannelCreate extends BottomSheetDialogFragment {

    private View view;
    private RadioGroup rdChannelType;
    private int channelType = 1;
    private EditText edtChannelName;
    private String channelName;
    private Button btnCreate;
    //여기 화면은 주최자만 해당함
    private static int USER_TYPE = 1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.bottomsheet_channel_create, container, false);
        edtChannelName = view.findViewById(R.id.edtChannelName);
        rdChannelType = view.findViewById(R.id.channelType);
        btnCreate = view.findViewById(R.id.btnCreate);

        //입력값이 있어야 생성 버튼 활성화
        edtChannelName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                channelName = edtChannelName.getText().toString();
                //최소한 채널명 1글자 이상
                if(channelName.length() >= 1){
                    btnCreate.setEnabled(true);
                }else{
                    btnCreate.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //기본적으로 채널타입 공개에 체크되어 있음
        rdChannelType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.channelPublic:
                        channelType = 1;
                        break;
                    case R.id.channelSecret:
                        channelType = 2;
                        break;
                }
            }
        });


        //생성하기 버튼 클릭시
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtChannelName.getText().toString().equals("")){
                    channelName = edtChannelName.getText().toString();
                }
                Intent intent = new Intent(getContext(), InventoryActivity.class);
                intent.putExtra("channel", channelName);
                intent.putExtra("userType", USER_TYPE);
                intent.putExtra("channelType", channelType);
                startActivity(intent);
                dismiss();
            }
        });

        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
                setupRatio(bottomSheetDialog);
            }
        });

        return dialog;
    }

    private void setupRatio(BottomSheetDialog bottomSheetDialog) {
        //id = com.google.android.material.R.id.design_bottom_sheet for Material Components
        // id = android.support.design.R.id.design_bottom_sheet for support librares
        FrameLayout bottomSheet = (FrameLayout) bottomSheetDialog.findViewById(R.id.design_bottom_sheet);
        BottomSheetBehavior behavior = BottomSheetBehavior.from(bottomSheet);
        ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
        layoutParams.height = getBottomSheetDialogDefaultHeight();
        bottomSheet.setLayoutParams(layoutParams);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }
    private int getBottomSheetDialogDefaultHeight() { return getWindowHeight() * 80 / 100; }
    private int getWindowHeight() {
        // Calculate window height for fullscreen use
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

}
