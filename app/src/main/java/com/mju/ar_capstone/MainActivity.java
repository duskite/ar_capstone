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
    TextView textView;
    Button button, btnAr;
    EditText editText;


    String tmp_val;
    int cnt = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //화면 요소들 초기
        textView = (TextView) findViewById(R.id.tv1);
        button = (Button) findViewById(R.id.btn1);
        btnAr = (Button) findViewById(R.id.btnAr);
        editText = (EditText) findViewById(R.id.edt1);


        //아래 기존 코드를 firebase manager로 대체중
        firebaseManager = new FirebaseManager();

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmp_val = editText.getText().toString();

                firebaseManager.setContent("3ABCD" + String.valueOf(cnt), "text", tmp_val);
                cnt++;

                editText.setText("");
            }
        });

        //버튼 클릭시 ar화면으로 전환
        btnAr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ArActivity.class);
                startActivity(intent);
            }
        });

    }
}