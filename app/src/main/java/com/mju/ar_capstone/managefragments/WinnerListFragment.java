package com.mju.ar_capstone.managefragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.card.MaterialCardView;
import com.mju.ar_capstone.ManageChannelActivity;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.adapter.WinnerListAdapter;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.HashMap;

public class WinnerListFragment extends Fragment {

    public static Context context;
    private String selectedChannel;
    private FirebaseManager firebaseManager;

    private ViewGroup viewGroup;

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private HashMap<String, String> winner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_winner_list, container, false);

        context = getContext();
        Bundle bundle = getArguments();
        selectedChannel = bundle.getString("selectedChannel");
        firebaseManager = new FirebaseManager();

        recyclerView = viewGroup.findViewById(R.id.recycler_winner);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        WinnerListAdapter adapter = new WinnerListAdapter();

        //들어오자마자 바로 우승자 리스트 띄워줌 - 매니지 채널 액티비티에서 미리 로드 안하면 빈걸로 나옴
        winner = ManageChannelActivity.winnerHashMap;
        for(String key: winner.keySet()){
            adapter.setArrayData(key, winner.get(key));
        }
        adapter.sortListInAdapter();

        //페이지 다시 로드하면 새로 불러와서 우승자 리스트 최신화
        swipeRefreshLayout = viewGroup.findViewById(R.id.swiperefresh_winner);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                firebaseManager.getWinnerList(selectedChannel, new FirebaseManager.GetWinnerListListener() {
                    @Override
                    public void onDataLoaded(HashMap<String, String> hashMap) {
                        if(hashMap != null) {
                            Log.d("우승자 리프레시", "성공");
                            for (String key : hashMap.keySet()) {
                                Log.d("우승자 리프레시", "성공: " + key + ", " + hashMap.get(key));
                                adapter.setArrayData(key, hashMap.get(key));
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.sortListInAdapter();
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        });
        recyclerView.setAdapter(adapter);


        return viewGroup;
    }

}
