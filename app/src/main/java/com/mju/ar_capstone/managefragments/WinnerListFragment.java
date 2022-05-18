package com.mju.ar_capstone.managefragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mju.ar_capstone.ManageChannelActivity;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.adapter.WinnerListAdapter;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.HashMap;

public class WinnerListFragment extends Fragment {

    private String selectedChannel;
    private FirebaseManager firebaseManager;

    private ViewGroup viewGroup;

    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_winner_list, container, false);

        Bundle bundle = getArguments();
        selectedChannel = bundle.getString("selectedChannel");
        firebaseManager = new FirebaseManager();

        recyclerView = viewGroup.findViewById(R.id.recycler_winner);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        WinnerListAdapter adapter = new WinnerListAdapter();

        HashMap<String, String> winner = ManageChannelActivity.winnerHashMap;
        for(String key: winner.keySet()){
            adapter.setArrayData(key, winner.get(key));
        }

        //테스트용
//        for(int i=0; i<50; i++){
//            String str = i + "번째 유저";
//            String strTime = i + "분";
//            adapter.setArrayData(str, strTime);
//        }
        recyclerView.setAdapter(adapter);


        return viewGroup;
    }

}
