package com.mju.ar_capstone.invenfragments;

import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_IMG;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_SOUND;
import static com.mju.ar_capstone.WrappedAnchor.ANCHOR_TEXT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.WrappedAnchor;
import com.mju.ar_capstone.adapter.HostListAdapter;
import com.mju.ar_capstone.helpers.FireStorageManager;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.ArrayList;

public class HostListFragment extends Fragment {
    private static String TAG = HostListFragment.class.getSimpleName();
    ViewGroup viewGroup;
    TextView hostTv;
    RecyclerView recycler_sound, recycler_img, recycler_text;
    FirebaseManager firebaseManager;
    FireStorageManager fireStorageManager;
    private String selectedChannel;

    private static ArrayList<WrappedAnchor> wrappedAnchorArrayList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_host_list, container, false);
        hostTv = viewGroup.findViewById(R.id.hostTv);

        Bundle bundle = getArguments();
        selectedChannel = bundle.getString("selectedChannel");
        hostTv.setText("주최자로 접속. 참가한 채널: " + selectedChannel);
        return viewGroup;
    }

    public void setFirebaseManager(FirebaseManager firebaseManager){
        this.firebaseManager = firebaseManager;
    }

    @Override
    public void onStart() {

        ArrayList<WrappedAnchor> wrappedAnchorArrayList = firebaseManager.getWrappedAnchorList();
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
        fireStorageManager = new FireStorageManager(selectedChannel);

        recycler_text = viewGroup.findViewById(R.id.recycler_text);
        recycler_text.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        recycler_text.setAdapter(new HostListAdapter(textList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_TEXT));

        recycler_img = viewGroup.findViewById(R.id.recycler_img);
        recycler_img.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        recycler_img.setAdapter(new HostListAdapter(imgList, fireStorageManager, firebaseManager, WrappedAnchor.ANCHOR_IMG));

        recycler_sound = viewGroup.findViewById(R.id.recycler_sound);
        recycler_sound.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        recycler_sound.setAdapter(new HostListAdapter(soundList, fireStorageManager, firebaseManager, ANCHOR_SOUND));

        super.onStart();
    }

    @Override
    public void onStop() {
        recycler_text.setAdapter(null);
        recycler_text.setLayoutManager(null);
        recycler_img.setAdapter(null);
        recycler_img.setLayoutManager(null);
        recycler_sound.setAdapter(null);
        recycler_sound.setLayoutManager(null);
        super.onStop();
    }
}
