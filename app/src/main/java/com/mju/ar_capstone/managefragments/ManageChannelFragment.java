package com.mju.ar_capstone.managefragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.mju.ar_capstone.ArSfActivity;
import com.mju.ar_capstone.R;
import com.mju.ar_capstone.helpers.FirebaseAuthManager;
import com.mju.ar_capstone.helpers.FirebaseManager;
import com.mju.ar_capstone.invenfragments.HostListFragment;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ManageChannelFragment extends Fragment {

    private Button btnDeleteChannel, btnHostAdd;
    private TextView tvChannelName;
    private EditText edtHostAdd;
    private MaterialCheckBox materialCheckBox;

    private String selectedChannel;
    private String addHostID;
    private static int ID_LENGTH = 28;

    private FirebaseManager firebaseManager;
    private FirebaseAuthManager firebaseAuthManager;
    private ViewGroup viewGroup;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_manage_channel, container, false);

        Bundle bundle = getArguments();
        selectedChannel = bundle.getString("selectedChannel");
        firebaseManager = new FirebaseManager();
        firebaseAuthManager = new FirebaseAuthManager();

        btnHostAdd = viewGroup.findViewById(R.id.btnHostAdd);
        edtHostAdd = viewGroup.findViewById(R.id.edtHostAdd);
        btnDeleteChannel = viewGroup.findViewById(R.id.btnDeleteChannel);
        tvChannelName = viewGroup.findViewById(R.id.tvChannelName);
        tvChannelName.setText("?????????: " + selectedChannel);
        materialCheckBox = viewGroup.findViewById(R.id.check_delete_channel);

        materialCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    btnDeleteChannel.setEnabled(true);
                }else {
                    btnDeleteChannel.setEnabled(false);
                }
            }
        });

        btnDeleteChannel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseManager.checkMainHost(selectedChannel, firebaseAuthManager.getUID(), new FirebaseManager.GetHostType() {
                    @Override
                    public void onHostTypeLoaded(boolean isMainHost) {
                        //?????? ???????????? ?????? ?????? ??????????????? ???
                        if(isMainHost){
                            firebaseManager.deleteChannel(selectedChannel);

                            HostListFragment hostListFragment = (HostListFragment) HostListFragment.hostListFragment;

                            hostListFragment.getActivity().finish();
                            getActivity().finish();
                        }else {
                            SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE);
                            sweetAlertDialog.setContentText("?????? ?????? ????????? ????????????.");
                            sweetAlertDialog.show();
                        }
                    }

                });

            }
        });

        edtHostAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                addHostID = edtHostAdd.getText().toString();
                //?????? ????????? ?????? 28??? ????????? ???????????? ?????????
                if(addHostID.length() >= ID_LENGTH){
                    btnHostAdd.setEnabled(true);
                }else{
                    btnHostAdd.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnHostAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ????????? ?????? ?????? ?????????
                addHostID = edtHostAdd.getText().toString();
                firebaseManager.addHostInChannel(selectedChannel, addHostID);

                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE);
                sweetAlertDialog.setContentText("????????? ?????? ??????");
                sweetAlertDialog.setConfirmButton("??????", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        edtHostAdd.setText("");
                        sweetAlertDialog.dismiss();
                    }
                });
                sweetAlertDialog.show();
            }
        });

        return viewGroup;
    }
}
