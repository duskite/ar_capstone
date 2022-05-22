package com.mju.ar_capstone.bottomsheets;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.adapter.ChannelListAdapter;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.ArrayList;

public class BottomSheetChannelEnter extends BottomSheetDialogFragment {

    private View view;
    private RecyclerView recyclerView;
    private FirebaseManager firebaseManager;
    private int userType;
    private Button btnChannelLoad;
    private ChannelListAdapter adapter = new ChannelListAdapter();

    public BottomSheetChannelEnter(FirebaseManager firebaseManager, int userType){
        this.firebaseManager = firebaseManager;
        this.userType = userType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.bottomsheet_channel_enter, container, false);
        recyclerView = view.findViewById(R.id.recycler_channel);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        btnChannelLoad = view.findViewById(R.id.btnChannelLoad);

        //firebase에서 채널 불러온후
        //사용자 유형에 맞게 리턴
        firebaseManager.getChannelList(new FirebaseManager.GetChannelListListener() {
            @Override
            public void onDataLoaded() {
                String[] strArray = channelLoad(userType);
                adapter = new ChannelListAdapter();
                adapter.setUserType(userType);
                for(int i=0; i<strArray.length; i++){
                    adapter.setArrayData(strArray[i]);
                }

                recyclerView.setAdapter(adapter);

            }
        });

        return view;
    }



    //문자열로 뽑아서 넘겨줌
    //여기 그냥 arraylist로 해도 될 꺼 같은데 우선 문자열 배열로
    public String[] channelLoad(int userType){

        int arraySize = 0;
        ArrayList<String> selectedChannelList = new ArrayList<>();
        if(userType == 1){
            selectedChannelList = firebaseManager.getHostChannelList();
        }else if(userType == 2){
            selectedChannelList = firebaseManager.getPublicChannelList();
        }
        arraySize = selectedChannelList.size();
        String[] adapterArray = new String[arraySize];
        for(int i=0; i<arraySize; i++){
            adapterArray[i] = selectedChannelList.get(i);
        }

        return adapterArray;

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

    @Override
    public void onPause() {
        super.onPause();
        firebaseManager.clearChannelList();
        adapter = null;
        dismiss();
    }


    // onResume일때 데이터 재로드
    // 현재 화면이 밑으로 내려갔다가 다시 최상단에 위치했을때,
    // 채널 삭제, 추가 등 변경사항 바로 반영
    @Override
    public void onResume() {
        super.onResume();

        firebaseManager.getChannelList(new FirebaseManager.GetChannelListListener() {
            @Override
            public void onDataLoaded() {
                String[] strArray = channelLoad(userType);
                adapter = new ChannelListAdapter();
                adapter.setUserType(userType);
                for(int i=0; i<strArray.length; i++){
                    adapter.setArrayData(strArray[i]);
                }

                recyclerView.setAdapter(adapter);

                adapter.notifyDataSetChanged();

            }
        });
        adapter.notifyDataSetChanged();
    }
}
