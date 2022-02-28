package com.mju.ar_capstone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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

public class MainActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;

    //화면 요소들 선언
    TextView textView;
    Button button;
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
        editText = (EditText) findViewById(R.id.edt1);

        //데이터베이스 연결
        mDatabase = FirebaseDatabase
                .getInstance("https://ar-capstone-dbf8e-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        //버튼 클릭시 동작
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tmp_val = editText.getText().toString();

                //db에 값 넣고 카운트 증가
                mDatabase.child(String.valueOf(cnt)).setValue(tmp_val);
                editText.setText("");
                cnt++;
            }
        });

        //db에 값 추가될경우 리스너
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //화면에 db값 우선 다 뿌려줌
                //나중에는 좀 더 세세하게 접근할 예정, 여기서는 통째로 예시
                textView.setText(snapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}