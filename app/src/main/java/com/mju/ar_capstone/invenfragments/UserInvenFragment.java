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

import java.util.ArrayList;

public class UserInvenFragment extends Fragment  implements Adapter.AdapterCallback{


    @BindView(R.id.template_recycler)
    RecyclerView template_recycler;
    ViewGroup viewGroup;
    TextView userTv;

    Context mContext;
    public static ArrayList<String> mtitle = new ArrayList<>();

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
        Adapter adapter = new Adapter(mtitle,this,mContext) ;
        template_recycler.setAdapter(adapter) ;

        return viewGroup;
    }


    @Override
    public void DoSomeThing(String roomname){

    }
}
