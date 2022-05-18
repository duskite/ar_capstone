package com.mju.ar_capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.invenfragments.HostListFragment;

public class ManageChannelActivity extends AppCompatActivity {

    private Button btnDeleteChannel;
    private TextView tvChannelName;

    private String channel;

    FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channel);

        Intent intent = getIntent();
        channel = intent.getStringExtra("selectedChannel");

        firebaseManager = new FirebaseManager();

        btnDeleteChannel = (Button) findViewById(R.id.btnDeleteChannel);
        tvChannelName = (TextView) findViewById(R.id.tvChannelName);
        tvChannelName.setText(channel);

        btnDeleteChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseManager.deleteChannel(channel);

                HostListFragment hostListFragment = (HostListFragment) HostListFragment.hostListFragment;
                hostListFragment.getActivity().finish();
                finish();
            }
        });

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