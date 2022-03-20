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

    private FirebaseManager firebaseManager;
    private FirebaseStorage firebaseStorage;
    private StorageReference storageReference;
    private StorageReference tmpReferece;
    private Uri tmpUri;

    //이미지 파일명 동적 생성
    private static String strImageID = "imageID_";
    private String currentImageID;

    public FireStorageManager(){
        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        tmpReferece = storageReference.child("image");
    }

    public synchronized void setFirebaseManager(FirebaseManager firebaseManager) {this.firebaseManager = firebaseManager;}

    public String makeImageFileID(){
        currentImageID = strImageID + String.valueOf(firebaseManager.getNextImageNum());
        return currentImageID;
    }

    public String getImagePath(){
        return makeImageFileID();
    }

    public Uri getUri(){
        Log.d("이미지", "Uri요청");
        Log.d("이미지", String.valueOf(tmpUri));
        return tmpUri;
    }

    public void downloadImage(String path){
        tmpReferece.child(path).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                tmpUri = uri;
                Log.d("이미지", "다운 성공");
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("이미지", "다운 실패");
            }
        });
    }

    //액티비티에서 Uri 넘겨줘야함
    public void uploadImage(Uri file){
        UploadTask uploadTask = tmpReferece.child(currentImageID).putFile(file);

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
