package com.shreeniketh.propify.activities;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityChangePasswordBinding;

public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private static final String TAG="CHANGE_PASSWORD_TAG";
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        firebaseUser=firebaseAuth.getCurrentUser();

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String currentPassword = "";
    private String newPassword = "";
    private String confirmNewPassword = "";

    private void validateData(){
        Log.d(TAG, "validateData: ");

    currentPassword = binding.currentPasswordEt.getText().toString().trim();
    newPassword = binding.newPasswordEt.getText().toString().trim();
    confirmNewPassword = binding.confirmNewPasswordEt.getText().toString().trim();

    if(currentPassword.isEmpty()){
        binding.currentPasswordEt.setError("Enter current password!");
        binding.currentPasswordEt.requestFocus();
    }else if(newPassword.isEmpty()){
        binding.newPasswordEt.setError("Enter new password!");
        binding.newPasswordEt.requestFocus();
    } else if (confirmNewPassword.isEmpty()) {
        binding.confirmNewPasswordEt.setError("Enter confirm password");
        binding.confirmNewPasswordEt.requestFocus();
    } else if (!newPassword.equals(confirmNewPassword)) {
        binding.confirmNewPasswordEt.setError("Password doesn't match");
        binding.confirmNewPasswordEt.requestFocus();
    }else {
        authenticateUserForUpdatePassword();
    }
    }

    private void authenticateUserForUpdatePassword(){
        Log.d(TAG,"authenticateUserForUpdatePassword: ");

        progressDialog.setMessage("Authenticating User");
        progressDialog.show();

        AuthCredential authCredential = EmailAuthProvider.getCredential(firebaseUser.getEmail(),currentPassword);
        firebaseUser.reauthenticate(authCredential)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Authentication success");
                        updatePassword();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ChangePasswordActivity.this,"Failed to authenticate due to "+e.getMessage());
                    }
                });
    }
    private void updatePassword(){
        Log.d(TAG,"updatePassword: ");

        progressDialog.setMessage("Updating password");
        progressDialog.show();

        firebaseUser.updatePassword(newPassword)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Password updated");
                        progressDialog.dismiss();
                        MyUtils.toast(ChangePasswordActivity.this,"Password updated...");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ChangePasswordActivity.this,"Failed to update due to "+e.getMessage());
                    }
                });
    }
}