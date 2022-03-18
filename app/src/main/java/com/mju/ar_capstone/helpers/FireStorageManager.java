package com.mju.ar_capstone.helpers;

import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mju.ar_capstone.ArSfActivity;

import java.io.IOException;

public class FireStorageManager {

    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;

    public FireStorageManager(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
    }

    //액티비티에서 Uri 넘겨줘야함
    public void uploadImage(Uri file){
        StorageReference tmpReferece = storageReference.child("image/1.png");
        UploadTask uploadTask = tmpReferece.putFile(file);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("순서 업로드", "업로드 실패");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("순서 업로드", "업로드 성공");
            }
        });

    }

}
