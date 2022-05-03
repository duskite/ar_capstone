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

public class UserInvenFragment extends Fragment {

    ViewGroup viewGroup;
    TextView userTv;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_user_inven, container, false);
        userTv = viewGroup.findViewById(R.id.userTv);

        Bundle bundle = getArguments();
        String selectedChannel = bundle.getString("selectedChannel");

        userTv.setText("참가자로 접속. 참가한 채널: " + selectedChannel );

        return viewGroup;
    }
}
