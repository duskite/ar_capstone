package com.mju.ar_capstone;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AnchorListDialog extends Dialog {

    ListView listView;
    FirebaseManager firebaseManager;
    FirebaseAuthManager firebaseAuthManager;


    public AnchorListDialog(@NonNull Context context, String selectChannel) {
        super(context);
        firebaseAuthManager = new FirebaseAuthManager();
        firebaseManager = new FirebaseManager(selectChannel);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_list);

        Log.d("앵커리스트", "앵커릿스트 다이얼로그 실행됨" );

        listView = (ListView) findViewById(R.id.listviewAnchor);
        String userID = firebaseAuthManager.getUID().toString();
        Log.d("앵커리스트", userID );

        List<String> list = new ArrayList<>();

        ArrayList<WrappedAnchor> wrappedAnchorList = firebaseManager.getWrappedAnchorList(userID);
        Iterator<WrappedAnchor> iterator = wrappedAnchorList.iterator();
        while (iterator.hasNext()) {
            WrappedAnchor wrappedAnchor = iterator.next();
            Log.d("앵커리스트", wrappedAnchor.getCloudAnchorId() );
            list.add(wrappedAnchor.getCloudAnchorId());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, list);

        listView.setAdapter(adapter);
    }
}
