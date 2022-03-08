package com.mju.ar_capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mju.ar_capstone.helpers.FirebaseManager;

public class MainActivity extends AppCompatActivity {

    private FirebaseManager firebaseManager;

    //화면 요소들 선언
    Button btnArSf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 씬 폼 데트스용
        btnArSf = (Button) findViewById(R.id.btnArSf);


        //아래 기존 코드를 firebase manager로 대체중
        firebaseManager = new FirebaseManager();


        //버튼 클릭시 ar 씬폼 화면으로 전환
        btnArSf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ArSfActivity.class);
                startActivity(intent);
            }
        });

    }
}