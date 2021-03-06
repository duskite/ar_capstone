package com.mju.ar_capstone.invenfragments;

import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_IMG;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_KEY;
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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import butterknife.BindView;
import butterknife.ButterKnife;

import com.google.gson.Gson;
import com.mju.ar_capstone.Adapter;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.WrappedAnchor;
import com.mju.ar_capstone.adapter.HostListAdapter;
import com.mju.ar_capstone.adapter.UserListAdapter;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.helpers.ItemTouchHelperCallback;
import com.mju.ar_capstone.helpers.ItemTouchHelperListner;

import java.util.ArrayList;
import java.util.Iterator;

public class UserInvenFragment extends Fragment implements Adapter.AdapterCallback{
    private final String TAG = UserInvenFragment.class.getSimpleName();
    RecyclerView recycler_sound, recycler_img, recycler_text;
    ViewGroup viewGroup;
    TextView userTv, haveKeyTv;
    FirebaseManager firebaseManager;
    FireStorageManager fireStorageManager;
    private FirebaseAuthManager firebaseAuthManager;
    private String selectedChannel;
    private boolean stateHaveKey = false;

    // ???????????? ?????? ?????? ??????
    private ItemTouchHelper itemTouchHelper;
    private UserListAdapter userListAdapter;

    Context mContext;


    public UserInvenFragment(Context mContet) {
        // Required empty public constructor
        this.mContext = mContet;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Log.d("???????????????", "onCreateView");

        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_inven, container, false);
        userTv = viewGroup.findViewById(R.id.userTv);
        haveKeyTv = viewGroup.findViewById(R.id.haveKeyTv);
        ButterKnife.bind(this, viewGroup);

        Bundle bundle = getArguments();
        selectedChannel = bundle.getString("selectedChannel");

        userTv.setText("??????: " + selectedChannel );

        return viewGroup;
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d("???????????????", "onStart");

        //?????? ????????? ?????? ????????????
        firebaseManager.getContents(new FirebaseManager.GetContentsListener() {
            @Override
            public void onDataLoaded() {

                //???????????? ???????????? ??????id??? ???????????? ?????? ???????????? ???????????? ??????
                firebaseManager.loadUserScrapAnchors(selectedChannel, firebaseAuthManager.getUID(), new FirebaseManager.GetLoadUserScrapAnchors() {
                    @Override
                    public void onDataLoaded() {
                        ArrayList<WrappedAnchor> wrappedAnchorArrayList = firebaseManager.getUserScrapAnchorList();

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
                            }else if(w.getAnchorType()==ANCHOR_KEY){
                                stateHaveKey = true;
                            }else {
                                stateHaveKey = false; //?????? ?????? ?????? ??? ???????????? ??????
                            }
                            if(stateHaveKey){ //??? ?????? ?????? ??????
                                haveKeyTv.setText(" Key: ?????????");
                            }
                            Log.d(TAG, "onCreateView wrappedAnchorArrayList: "+new Gson().toJson(w));
                        }

                        fireStorageManager = new FireStorageManager(selectedChannel);

                        recycler_text = viewGroup.findViewById(R.id.recycler_text);
                        recycler_text.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
                        recycler_text.setAdapter(new UserListAdapter(textList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_TEXT));

                        Log.d("???????????????", "????????????");

                        recycler_img = viewGroup.findViewById(R.id.recycler_img);
//                        recycler_img.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
                        recycler_img.setLayoutManager(new GridLayoutManager(getContext(), 2, GridLayoutManager.HORIZONTAL, false));

                        userListAdapter = new UserListAdapter(imgList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_IMG);
                        itemTouchHelper = new ItemTouchHelper(new ItemTouchHelperCallback(userListAdapter));
                        itemTouchHelper.attachToRecyclerView(recycler_img);
                        recycler_img.setAdapter(userListAdapter);



                        recycler_sound = viewGroup.findViewById(R.id.recycler_sound);
                        recycler_sound.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
                        recycler_sound.setAdapter(new UserListAdapter(soundList, fireStorageManager, firebaseManager, ANCHOR_SOUND));

                    }
                });
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("???????????????", "onPause");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("???????????????", "onResume");
    }

    @Override
    public void DoSomeThing(String roomname){

    }

    public void setFirebaseManager(FirebaseManager firebaseManager, FirebaseAuthManager firebaseAuthManager){
        this.firebaseManager = firebaseManager;
        this.firebaseAuthManager = firebaseAuthManager;
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("???????????????", "onStop");

        //?????? ?????? ???????????? ?????? ????????? ?????? ????????? ???????????? ??????????????? ??????
        //?????? ?????? ?????? ????????? ???????????? ?????????
        firebaseManager.clearWrappedAnchorList();
        firebaseManager.clearUserScrapAnchorIdList();
        firebaseManager.clearUserScrapAnchorList();
    }


}
