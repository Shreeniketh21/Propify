package com.shreeniketh.propify.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityLoginEmailBinding;
import com.shreeniketh.propify.databinding.ActivityLoginOptionsBinding;

public class LoginEmailActivity extends AppCompatActivity {

    private ActivityLoginEmailBinding binding;
    private static final String TAG="LOGIN_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.loginBtnTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDate();
            }
        });

        binding.noAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginEmailActivity.this, RegisterEmailActivity.class));
            }
        });

        binding.forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginEmailActivity.this,ForgotPasswordActivity.class));

            }
        });

    }
    private String email,password;
    private void validateDate(){
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString();

        Log.d(TAG,"validateDate: Email: "+email);
        Log.d(TAG,"validateDate: Password: "+password);

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email");
            binding.emailEt.requestFocus();
        }else if(password.isEmpty()){
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        }else{
            loginUser();
        }
    }
    private void loginUser(){
        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Log.d(TAG,"onSuccess: Logged In...");
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginEmailActivity.this,MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(LoginEmailActivity.this,"Failed due to "+e.getMessage());
                    }
                });
    }
}