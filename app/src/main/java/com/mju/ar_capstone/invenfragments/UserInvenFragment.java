package com.mju.ar_capstone.invenfragments;

import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_IMG;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_SOUND;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_TEXT;

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

import com.google.gson.Gson;
import com.mju.ar_capstone.Adapter;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.WrappedAnchor;
import com.mju.ar_capstone.adapter.HostListAdapter;
import com.mju.ar_capstone.adapter.UserListAdapter;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.ArrayList;
import java.util.Iterator;

public class UserInvenFragment extends Fragment implements Adapter.AdapterCallback{
    private final String TAG = UserInvenFragment.class.getSimpleName();
    RecyclerView recycler_sound, recycler_img, recycler_text;
    ViewGroup viewGroup;
    TextView userTv;
    FirebaseManager firebaseManager;
    FireStorageManager fireStorageManager;
    private String selectedChannel;

    Context mContext;
    //앵커 아이디 리스트
    public static ArrayList<String> mtitle = new ArrayList<>();
    // 앵커 아이디로 내용물 담은 리스트
    public static ArrayList<String> mtitleContent = new ArrayList<>();

    public static ArrayList<WrappedAnchor> wrappedAnchorArrayList = new ArrayList<>();

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
        selectedChannel = bundle.getString("selectedChannel");

        userTv.setText("참가자로 접속. 참가한 채널: " + selectedChannel );


//        Adapter adapter = new Adapter(mtitleContent,this,mContext) ;
//        Adapter adapter = new Adapter(mtitle,this,mContext);

//        template_recycler.setAdapter(adapter) ;

        return viewGroup;
    }

    @Override
    public void onResume() {
        super.onResume();

        ArrayList<WrappedAnchor> textList = new ArrayList<>();
        ArrayList<WrappedAnchor> imgList = new ArrayList<>();
        ArrayList<WrappedAnchor> soundList = new ArrayList<>();

        for (WrappedAnchor w: wrappedAnchorArrayList) {
            if(w.getAnchorType()==ANCHOR_TEXT){
                textList.add(w);
            }else if(w.getAnchorType()==ANCHOR_IMG){
                imgList.add(w);
            }else if(w.getAnchorType()==ANCHOR_SOUND){
                soundList.add(w);
            }
            Log.d(TAG, "onCreateView wrappedAnchorArrayList: "+new Gson().toJson(w));
        }

        //mtitle.add("aa");

        fireStorageManager = new FireStorageManager(selectedChannel);

        recycler_text = viewGroup.findViewById(R.id.recycler_text);
        recycler_text.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        recycler_text.setAdapter(new UserListAdapter(textList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_TEXT));

        recycler_img = viewGroup.findViewById(R.id.recycler_img);
        recycler_img.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        recycler_img.setAdapter(new UserListAdapter(imgList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_IMG));

        recycler_sound = viewGroup.findViewById(R.id.recycler_sound);
        recycler_sound.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        recycler_sound.setAdapter(new UserListAdapter(soundList, fireStorageManager, firebaseManager, ANCHOR_SOUND));
    }

    @Override
    public void DoSomeThing(String roomname){

    }

    public void setFirebaseManager(FirebaseManager firebaseManager){
        this.firebaseManager = firebaseManager;
    }

}
