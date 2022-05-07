package com.mju.ar_capstone.invenfragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.mju.ar_capstone.Adapter;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.ArrayList;
import java.util.Iterator;

public class UserInvenFragment extends Fragment  implements Adapter.AdapterCallback{


    @BindView(R.id.template_recycler)
    RecyclerView template_recycler;
    ViewGroup viewGroup;
    TextView userTv;
    FirebaseManager firebaseManager;

    Context mContext;
    //앵커 아이디 리스트
    public static ArrayList<String> mtitle = new ArrayList<>();
    // 앵커 아이디로 내용물 담은 리스트
    public static ArrayList<String> mtitleContent = new ArrayList<>();

    public UserInvenFragment(Context mContet) {
        // Required empty public constructor
        this.mContext = mContet;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_inven, container, false);
        userTv = viewGroup.findViewById(R.id.userTv);
        ButterKnife.bind(this, viewGroup);

        Bundle bundle = getArguments();
        String selectedChannel = bundle.getString("selectedChannel");

        userTv.setText("참가자로 접속. 참가한 채널: " + selectedChannel );

        //mtitle.add("aa");
        template_recycler.setLayoutManager(new LinearLayoutManager(mContext)) ;
        Adapter adapter = new Adapter(mtitleContent,this,mContext) ;
//        Adapter adapter = new Adapter(mtitle,this,mContext);

        template_recycler.setAdapter(adapter) ;

        return viewGroup;
    }


    @Override
    public void DoSomeThing(String roomname){

    }

    public void setFirebaseManager(FirebaseManager firebaseManager){
        this.firebaseManager = firebaseManager;
    }

}
