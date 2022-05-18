package com.mju.ar_capstone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button btnInven;
    private RadioGroup rdUserType, rdChannelType;
    private int userType = 0;
    private int channelType = 1;
    private LinearLayout layoutChannelType;

    private TextView tvUserId, tvChannelLoad;
    private Spinner spinner;
    private Button btnSpinnerLoad;

    private String selectedChannel = "base_channel"; //기본 채널
    private EditText edtChannelName;

    private ArrayList<String> publicChannelList, allChannelList;

    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;

    private CheckBox checkCreate;

    //앱 종료처리
    private long backKeyPressedTime = 0;
    private Toast toast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        checkCreate = findViewById(R.id.check_create);
        layoutChannelType = findViewById(R.id.layout_channel_type);
        layoutChannelType.setVisibility(View.INVISIBLE);

        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager();
        firebaseManager.setAuth(firebaseAuthManager.getUID().toString());
        //채널 리스트 가져옴
        firebaseManager.getChannelList();
        publicChannelList = firebaseManager.getPublicChannelList();
        allChannelList = firebaseManager.getHostChannelList();

        tvUserId = (TextView) findViewById(R.id.userId);
        tvUserId.setText("참가자ID 발급완료. 터치시 자동으로 ID가 복사됩니다.\n" + "익명ID: " + firebaseAuthManager.getUID());
        //클립보드에 id저장할 수 있도록 지원
        tvUserId.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(event.getAction()==MotionEvent.ACTION_DOWN){
                    //눌렀을 때 동작
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("ID", firebaseAuthManager.getUID());
                    //클립보드에 ID라는 이름표로 id 값을 복사하여 저장
                    clipboardManager.setPrimaryClip(clipData);
                }
                return true;
            }
        });

        btnInven = findViewById(R.id.btnInven);
        edtChannelName = findViewById(R.id.edtChannelName);
        btnSpinnerLoad = findViewById(R.id.btnSpinnerLoad);
        tvChannelLoad = findViewById(R.id.tvChannelLoad);
        spinner = (Spinner) findViewById(R.id.spinner_channel);
        spinner.setVisibility(View.INVISIBLE);


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
                        edtChannelName.setHint("위 버튼 체크시 생성가능");
                        tvChannelLoad.setText("접근가능한 채널 불러오기");
                        btnSpinnerLoad.setEnabled(true);
                        userType = 1;
                        break;
                    case R.id.participant:
                        userType = 2;
                        checkCreate.setVisibility(View.GONE);
                        edtChannelName.setEnabled(true);
                        tvChannelLoad.setText("공개된 채널 불러오기");
                        edtChannelName.setHint("입장할 채널명을 입력해주세요");
                        btnInven.setText("입장하기");
                        btnSpinnerLoad.setEnabled(true);
                        rdChannelType.setVisibility(View.INVISIBLE);
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

                // 공용 채널에 이 이름이 있는지 체크
                if(!allChannelList.contains(selectedChannel)){ // 이 이름으로 생성된 채널이 없는데
                    if(userType == 2){ //그러나 참가자일경우는 채널 생성 하지 않고 멈춤
                        android.app.AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        dialog.setMessage("존재하지 않는 채널입니다.");
                        dialog.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                        return;
                    }
                }

                //채널이 있을때
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

                int arraySize = 0;
                ArrayList<String> selectedChannelList = new ArrayList<>();
                if(userType == 1){
                    selectedChannelList = allChannelList;
                }else if(userType == 2){
                    selectedChannelList = publicChannelList;
                }
                arraySize = selectedChannelList.size();
                Log.d("채널 사이즈", String.valueOf(arraySize));
                String[] spinnerList = new String[arraySize];
                for(int i=0; i<arraySize; i++){
                    spinnerList[i] = selectedChannelList.get(i);
                    Log.d("채널 순서" + i, spinnerList[i]);
                }

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
    public void onBackPressed() {
        // 기존 뒤로가기 버튼의 기능을 막기위해 주석처리 또는 삭제
        // super.onBackPressed();

        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지났으면 Toast Show
        // 2000 milliseconds = 2 seconds
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis();
            toast = Toast.makeText(this, "\'뒤로\' 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간에 2초를 더해 현재시간과 비교 후
        // 마지막으로 뒤로가기 버튼을 눌렀던 시간이 2초가 지나지 않았으면 종료
        // 현재 표시된 Toast 취소
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            finish();
            toast.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        firebaseManager = null;
        firebaseAuthManager = null;
    }
}