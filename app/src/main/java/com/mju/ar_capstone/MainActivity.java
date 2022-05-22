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

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.mju.ar_capstone.bottomsheets.BottomSheetChannelCreate;
import com.mju.ar_capstone.bottomsheets.BottomSheetChannelEnter;
import com.mju.ar_capstone.bottomsheets.BottomSheetChannelSecretEnter;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import java.util.ArrayList;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends AppCompatActivity {

    private Button btnHostCreate, btnHostEnter, btnUserEnter, btnUserSecretEnter;
    private RadioGroup rdUserType;
    private int userType = 0;

    private LinearLayout layoutHost, layoutUser;
    private MaterialButtonToggleGroup btnHostToggle;

    private TextView tvUserId;

    private FirebaseAuthManager firebaseAuthManager;
    private FirebaseManager firebaseManager;

    //앱 종료처리
    private long backKeyPressedTime = 0;
    private Toast toast;

    private void viewInit(){
        //firebase 설정
        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager();
        firebaseManager.setAuth(firebaseAuthManager.getUID().toString());

        rdUserType = (RadioGroup) findViewById(R.id.userType);
        layoutHost = (LinearLayout) findViewById(R.id.layout_host);
        layoutUser = (LinearLayout) findViewById(R.id.layout_user);
        //최초에는 하단 레이아웃 안보이게
        layoutHost.setVisibility(View.GONE);
        layoutUser.setVisibility(View.GONE);

        btnHostToggle = (MaterialButtonToggleGroup) findViewById(R.id.btnHostToggle);
        btnHostToggle.setSingleSelection(true);

        tvUserId = (TextView) findViewById(R.id.userId);

        btnHostCreate = (Button) findViewById(R.id.btnHostCreate);
        btnHostEnter = (Button) findViewById(R.id.btnHostEnter);
        btnUserEnter = (Button) findViewById(R.id.btnUserEnter);
        btnUserSecretEnter = (Button) findViewById(R.id.btnUserSecretEnter);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewInit();

        //유저 타입 고름
        rdUserType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId){
                    case R.id.host:
                        userType = 1;
                        layoutUser.setVisibility(View.GONE);
                        layoutHost.setVisibility(View.VISIBLE);
                        break;
                    case R.id.participant:
                        userType = 2;
                        layoutHost.setVisibility(View.GONE);
                        layoutUser.setVisibility(View.VISIBLE);
                        break;
                }
                Log.d("유저타입", String.valueOf(userType));
            }
        });
        tvUserId.setText("익명ID 발급완료. 터치하여 복사하기.\n" + "익명ID: " + firebaseAuthManager.getUID());
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

        //채널 생성 바텀시트 호스트
        btnHostCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetChannelCreate bottomSheetChannelCreate = new BottomSheetChannelCreate();
                bottomSheetChannelCreate.show(getSupportFragmentManager(), "create");
            }
        });
        //채널 참여 바텀시트 호스트
        btnHostEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetChannelEnter bottomSheetChannelEnter = new BottomSheetChannelEnter(firebaseManager, userType);
                bottomSheetChannelEnter.show(getSupportFragmentManager(), "enter");
            }
        });

        //채널 입장 바텀시트 유저
        btnUserEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetChannelEnter bottomSheetChannelEnter = new BottomSheetChannelEnter(firebaseManager, userType);
                bottomSheetChannelEnter.show(getSupportFragmentManager(), "enter");
            }
        });
        //비공개 채널 입장 바텀시트 유저
        btnUserSecretEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BottomSheetChannelSecretEnter bottomSheetChannelSecretEnter = new BottomSheetChannelSecretEnter(firebaseManager, userType);
                bottomSheetChannelSecretEnter.show(getSupportFragmentManager(), "enter");
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