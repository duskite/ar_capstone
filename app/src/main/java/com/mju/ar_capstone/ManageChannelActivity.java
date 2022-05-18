package com.mju.ar_capstone;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

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

public class ManageChannelActivity extends AppCompatActivity {

    private Button btnDeleteChannel, btnHostAdd;
    private TextView tvChannelName;
    private EditText edtHostAdd;

    private String channel;
    private String addHostID;
    private static int ID_LENGTH = 28;

    FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_channel);

        Intent intent = getIntent();
        channel = intent.getStringExtra("selectedChannel");

        firebaseManager = new FirebaseManager();

        btnHostAdd = (Button) findViewById(R.id.btnHostAdd);
        edtHostAdd = (EditText) findViewById(R.id.edtHostAdd);
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

        edtHostAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addHostID = edtHostAdd.getText().toString();
                //현재 입력된 값이 28자 이상일 경우에만 활성화
                if(addHostID.length() >= ID_LENGTH){
                    btnHostAdd.setEnabled(true);
                }else{
                    btnHostAdd.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnHostAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //혹시 몰라서 다시 한번 가져옴
                addHostID = edtHostAdd.getText().toString();
                firebaseManager.addHostInChannel(channel, addHostID);

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