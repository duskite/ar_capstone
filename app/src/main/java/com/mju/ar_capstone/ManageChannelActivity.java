package com.mju.ar_capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.invenfragments.HostListFragment;
import com.mju.ar_capstone.managefragments.ManageChannelFragment;
import com.mju.ar_capstone.managefragments.WinnerListFragment;

import java.util.HashMap;

public class ManageChannelActivity extends AppCompatActivity {

    private String selectedChannel;

    //프래그먼트로 처리
    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;
    private ManageChannelFragment manageChannelFragment;
    private WinnerListFragment winnerListFragment;
    private Bundle bundle;

    //여기서 우승자 리스트 로드하고 프래그먼트에서 가져감
    public FirebaseManager firebaseManager;
    public static HashMap<String, String> winnerHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channel);

        //앞에서 받아온 채널 정보
        Intent intent = getIntent();
        selectedChannel = intent.getStringExtra("selectedChannel");
        //프래그먼트에 값 넘길 번들 객체
        bundle = new Bundle();
        bundle.putString("selectedChannel", selectedChannel);

        //여기서 우승자 리스트 로드하고 프래그먼트에서 가져감
        firebaseManager = new FirebaseManager();
        winnerHashMap = firebaseManager.getWinnerList(selectedChannel);

        fragmentManager = getSupportFragmentManager();
        manageChannelFragment = new ManageChannelFragment();
        winnerListFragment = new WinnerListFragment();
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.manage_or_winner_frame, manageChannelFragment).commitAllowingStateLoss();
        //처음에 디폴트 프래그먼트에 채널 넘겨줌
        manageChannelFragment.setArguments(bundle);

    }

    public void clickHandler(View view){
        fragmentTransaction = fragmentManager.beginTransaction();
        switch (view.getId()){
            case R.id.tvGoManage:
                fragmentTransaction.replace(R.id.manage_or_winner_frame, manageChannelFragment).commitAllowingStateLoss();
                manageChannelFragment.setArguments(bundle);
                break;
            case R.id.tvGoWinner:
                fragmentTransaction.replace(R.id.manage_or_winner_frame, winnerListFragment).commitAllowingStateLoss();
                winnerListFragment.setArguments(bundle);
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}