package com.mju.ar_capstone.invenfragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.mju.ar_capstone.R;

public class HostListFragment extends Fragment {

    ViewGroup viewGroup;
    View hostlist0, hostlist1, hostlist2;
    TextView hostTv, hostText, hostImage, hostMp3;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_host_list, container, false);
        hostTv = viewGroup.findViewById(R.id.hostTv);
        hostlist0 = viewGroup.findViewById(R.id.hostlist0);
        hostlist1 = viewGroup.findViewById(R.id.hostlist1);
        hostlist2 = viewGroup.findViewById(R.id.hostlist2);
        hostText = viewGroup.findViewById(R.id.hostText);
        hostImage = viewGroup.findViewById(R.id.hostImage);
        hostMp3 = viewGroup.findViewById(R.id.hostMp3);

        Bundle bundle = getArguments();
        String selectedChannel = bundle.getString("selectedChannel");

        hostTv.setText("주최자로 접속. 참가한 채널: " + selectedChannel);

        return viewGroup;
    }
}
