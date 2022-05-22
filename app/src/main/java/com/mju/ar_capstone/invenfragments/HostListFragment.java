package com.mju.ar_capstone.invenfragments;

import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_IMG;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_SOUND;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_TEXT;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.mju.ar_capstone.InventoryActivity;
import com.mju.ar_capstone.ManageChannelActivity;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.WrappedAnchor;
import com.mju.ar_capstone.adapter.HostListAdapter;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.services.NotificationService;

import java.util.ArrayList;

public class HostListFragment extends Fragment {
    private static String TAG = HostListFragment.class.getSimpleName();
    ViewGroup viewGroup;
    TextView hostTv;
    RecyclerView recycler_sound, recycler_img, recycler_text;
    FirebaseManager firebaseManager;
    FireStorageManager fireStorageManager;
    Button btnManageChannel;
    ToggleButton btnWinNoti;
    private String selectedChannel;

    //채널 삭제시 액티비티 종료시키려고 담아둠
    //프래그먼트 액티비티를 종료함
    public static HostListFragment hostListFragment;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("주최자인벤", "onCreateView");
        hostListFragment = HostListFragment.this;

        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_host_list, container, false);
        hostTv = viewGroup.findViewById(R.id.hostTv);
        btnManageChannel = viewGroup.findViewById(R.id.btnManageChannel);
        btnWinNoti = (ToggleButton) viewGroup.findViewById(R.id.btnWinNoti);

        Bundle bundle = getArguments();
        selectedChannel = bundle.getString("selectedChannel");
        hostTv.setText("채널: " + selectedChannel);

        btnWinNoti.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Intent serviceIntent = new Intent(getContext(), NotificationService.class);
                serviceIntent.putExtra("selectedChannel",selectedChannel);
                if(isChecked){
                    // 우승자 알림 켜기
                    //서비스로 포어그라운드 돌리기어
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getActivity().startForegroundService(serviceIntent);
                    } else {
                        getActivity().startService(serviceIntent);
                    }
                }else {
                    //알림 끄기
                    getActivity().stopService(serviceIntent);
                }

            }
        });


        btnManageChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ManageChannelActivity.class);
                intent.putExtra("selectedChannel", selectedChannel);
                startActivity(intent);
            }
        });


        return viewGroup;
    }

    public void setFirebaseManager(FirebaseManager firebaseManager){
        this.firebaseManager = firebaseManager;
    }

    @Override
    public void onStart() {
        Log.d("주최자인벤", "onStart");

        //앵커 확실히 로드 된 이후에 화면 구성
        firebaseManager.getContents(new FirebaseManager.GetContentsListener() {
            @Override
            public void onDataLoaded() {
                ArrayList<WrappedAnchor> wrappedAnchorArrayList = firebaseManager.getWrappedAnchorList();
                ArrayList<WrappedAnchor> textList = new ArrayList<>();
                ArrayList<WrappedAnchor> imgList = new ArrayList<>();
                ArrayList<WrappedAnchor> soundList = new ArrayList<>();
                for (WrappedAnchor w: wrappedAnchorArrayList) {
                    if(w.getAnchorType()==ANCHOR_TEXT){
                        textList.add(w);
                    }else if(w.getAnchorType()==ANCHOR_IMG){
                        imgList.add(w);
                    }else if(w.getAnchorType()==ANCHOR_SOUND){
                        soundList.add(w);
                    }
                    Log.d(TAG, "onCreateView wrappedAnchorArrayList: "+new Gson().toJson(w));
                }
                fireStorageManager = new FireStorageManager(selectedChannel);

                recycler_text = viewGroup.findViewById(R.id.recycler_text);
                recycler_text.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
                recycler_text.setAdapter(new HostListAdapter(textList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_TEXT));

                recycler_img = viewGroup.findViewById(R.id.recycler_img);
                recycler_img.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
                recycler_img.setAdapter(new HostListAdapter(imgList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_IMG));

                recycler_sound = viewGroup.findViewById(R.id.recycler_sound);
                recycler_sound.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
                recycler_sound.setAdapter(new HostListAdapter(soundList, fireStorageManager, firebaseManager, ANCHOR_SOUND));
            }
        });

        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("주최자인벤", "onPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("주최자인벤", "onResume");
    }

    @Override
    public void onStop() {
        super.onStop();

        //다른 화면 넘어가면 앵커 리스트 한번 비워서 중복으로 가져오는거 방지
        //화면 새로 뜰때 새정보 로드하기 위해서
        firebaseManager.clearWrappedAnchorList();
        Log.d("주최자인벤", "onStop");
    }

}
