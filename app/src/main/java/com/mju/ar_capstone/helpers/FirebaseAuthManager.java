package com.mju.ar_capstone.helpers;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mju.ar_capstone.ArSfActivity;
import com.mju.ar_capstone.MainActivity;

import java.util.concurrent.Executor;

public class FirebaseAuthManager {

    public FirebaseAuth firebaseAuth;

    public FirebaseAuthManager(){
        firebaseAuth = FirebaseAuth.getInstance();
        singInAnonymously();

    }

    //익명 로그인 처리
    private void singInAnonymously(){
        // [START signin_anonymously]
        firebaseAuth.signInAnonymously()
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("순서", "signInAnonymously:success");
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("순서", "signInAnonymously:failure", task.getException());

                        }
                    }
                });
        // [END signin_anonymously]
    }

    public String getUID(){
        try {
            return firebaseAuth.getUid().toString();
        }catch (NullPointerException e){
            return "temp_account";
        }

    }
}
