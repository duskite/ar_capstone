package com.mju.ar_capstone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.helpers.SensorAllManager;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private Button btnInven;
    private RadioGroup rdUserType, rdChannelType;
    private int userType = 0;
    private int channelType = 0;
    private LinearLayout layoutChannelType;

    private TextView tvUserId;
    private Spinner spinner;
    private Button btnSpinnerLoad;

    private String selectedChannel = "base_channel"; //기본 채널
    private EditText edtChannelName;

    private ArrayList<String> channelList;

    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;

    private CheckBox checkCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkCreate = findViewById(R.id.check_create);
        layoutChannelType = findViewById(R.id.layout_channel_type);
        layoutChannelType.setVisibility(View.INVISIBLE);

        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager();
        //채널 리스트 가져옴
        channelList = firebaseManager.getChannelList();

        tvUserId = (TextView) findViewById(R.id.userId);
        tvUserId.setText("로그인 성공\n" + "익명ID: " + firebaseAuthManager.getUID());
        btnInven = findViewById(R.id.btnInven);
        edtChannelName = findViewById(R.id.edtChannelName);
        btnSpinnerLoad = findViewById(R.id.btnSpinnerLoad);
        spinner = (Spinner) findViewById(R.id.spinner_channel);
        spinner.setVisibility(View.GONE);


        checkCreate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(buttonView.isChecked() == true){
                    edtChannelName.setEnabled(true);
                    edtChannelName.setHint("생성할 채널명을 입력해주세요");
                    btnInven.setText("생성하기");
                    layoutChannelType.setVisibility(View.VISIBLE);
                }else{
                    edtChannelName.setEnabled(false);
                    btnInven.setText("입장하기");
                    layoutChannelType.setVisibility(View.INVISIBLE);
                }
            }
        });

        rdChannelType = (RadioGroup) findViewById(R.id.channelType);
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


        // 입장하기 인벤토리로 이동
        rdUserType = (RadioGroup) findViewById(R.id.userType);
        rdUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                checkCreate.setVisibility(View.VISIBLE);
                btnInven.setEnabled(true);

                switch (checkedId){
                    case R.id.host:
                        edtChannelName.setEnabled(false);
                        userType = 1;
                        break;
                    case R.id.participant:
                        userType = 2;
                        checkCreate.setVisibility(View.GONE);
                        edtChannelName.setEnabled(true);
                        edtChannelName.setHint("입장할 채널명을 입력해주세요");
                        btnInven.setText("입장하기");
                        break;
                }
                Log.d("유저타입", String.valueOf(userType));
            }
        });
        btnInven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edtChannelName.getText().toString().equals("")){
                    selectedChannel = edtChannelName.getText().toString();
                }
                Intent intent = new Intent(MainActivity.this, InventoryActivity.class);
                intent.putExtra("channel", selectedChannel);
                intent.putExtra("userType", userType);
                intent.putExtra("channelType", channelType);
                Log.d("채널이름 넘기는거 메인", selectedChannel);
                startActivity(intent);
            }
        });



        btnSpinnerLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int arraySize = channelList.size();
                Log.d("채널 사이즈", String.valueOf(arraySize));
                String[] spinnerList = new String[arraySize];
                for(int i=0; i<arraySize; i++){
                    spinnerList[i] = channelList.get(i);
                    Log.d("채널 순서" + i, spinnerList[i]);
                }
                Log.d("채널", "선택" + selectedChannel);

                //채널리스트로 선택 스피너 생성
                spinner.setVisibility(View.VISIBLE);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_dropdown_item_1line, spinnerList);
                adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedChannel = spinnerList[position];
                        Log.d("채널", "선택" + selectedChannel);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

            }
        });




        permisionCheck();
    }

    private void permisionCheck(){
        //권한 관련 얻기
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION,false);
                            if (fineLocationGranted != null && fineLocationGranted) {
                                // Precise location access granted.
                            } else if (coarseLocationGranted != null && coarseLocationGranted) {
                                // Only approximate location access granted.
                            } else {
                                // No location access granted.
                            }
                        }
                );

        // ...

        // Before you perform the actual permission request, check whether your app
        // already has the permissions, and whether your app needs to show a permission
        // rationale dialog. For more details, see Request permissions.
        locationPermissionRequest.launch(new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

    }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }



}