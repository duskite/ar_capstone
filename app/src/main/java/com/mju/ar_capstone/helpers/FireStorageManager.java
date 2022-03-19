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
    private StorageReference tmpReferece;
    private Uri tmpUri;

    public FireStorageManager(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        tmpReferece = storageReference.child("image/1.png");
    }

    public String getImagePath(){

        return tmpReferece.getPath();
    }

    public Uri getUri(){
        return tmpUri;
    }

    public void downloadImage(String path){
        Log.d("순서 이미지 로드", storageReference.child(path).toString());
        Log.d("순서 이미지 로드", storageReference.child(path).getDownloadUrl().toString());

        storageReference.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                tmpUri = uri;
                Log.d("순서 이미지 다운", "다운 성공");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("순서 이미지 다운", "다운 실패");
            }
        });
    }


    //액티비티에서 Uri 넘겨줘야함
    public void uploadImage(Uri file){
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
