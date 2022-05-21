package com.mju.ar_capstone.bottomsheets;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mju.ar_capstone.InventoryActivity;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BottomSheetChannelSecretEnter extends BottomSheetDialogFragment {

    private View view;
    private EditText edtSecretChannelName;
    private String channelName;
    private Button btnSceretEnter;
    //비공개 채널 참가는 참가자 유형일때 가능
    //주최자로는 해당 채널에 초대를 받아야함
    private static int USER_TYPE = 2;
    private FirebaseManager firebaseManager;
    private int userType;
    private ArrayList<String> accessChannelList = new ArrayList<>();

    public BottomSheetChannelSecretEnter(FirebaseManager firebaseManager, int userType){
        this.firebaseManager = firebaseManager;
        this.userType = userType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.bottomsheet_channel_sceretenter, container, false);
        edtSecretChannelName = view.findViewById(R.id.edtSecretChannelName);
        btnSceretEnter = view.findViewById(R.id.btnSecretEnter);
        firebaseManager.getChannelList(new FirebaseManager.GetChannelListListener() {
            @Override
            public void onDataLoaded() {
                //공개 채널과 비공개 채널 모두 불러와서 하나로 합침
                //중복이 있을 수 없음

                accessChannelList.addAll(firebaseManager.getPrivateChannelList());
                accessChannelList.addAll(firebaseManager.getPublicChannelList());

                //데이터 로드 된 이후에 채널 입장 버튼 활성화되도록
                //입력값이 있어야 생성 버튼 활성화
                edtSecretChannelName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        channelName = edtSecretChannelName.getText().toString();
                        //최소한 채널명 1글자 이상
                        if(channelName.length() >= 1){
                            btnSceretEnter.setEnabled(true);
                        }else{
                            btnSceretEnter.setEnabled(false);
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
        });




        //입장하기 버튼 클릭시
        btnSceretEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtSecretChannelName.getText().toString().equals("")){
                    channelName = edtSecretChannelName.getText().toString();
                }
                // 공개 채널과 비공개 채널에 이 이름이 있는지 체크
                // 채널이 없으면 존재하지 않는다고 띄움
                if(!accessChannelList.contains(channelName)){
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.ERROR_TYPE);
                    sweetAlertDialog.setTitleText("존재하지 않는 채널입니다.");
                    sweetAlertDialog.setConfirmText("닫기");
                    sweetAlertDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                        }
                    });
                    sweetAlertDialog.show();
                    return;
                }
                Intent intent = new Intent(getContext(), InventoryActivity.class);
                intent.putExtra("channel", channelName);
                intent.putExtra("userType", USER_TYPE);
                startActivity(intent);
                dismiss();
            }
        });


        return view;
    }
}
