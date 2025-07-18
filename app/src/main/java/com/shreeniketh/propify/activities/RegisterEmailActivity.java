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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityRegisterEmailBinding;

import java.util.HashMap;

public class RegisterEmailActivity extends AppCompatActivity {

    private ActivityRegisterEmailBinding binding;
    private static final String TAG="REGISTER_EMAIL_TAG";
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityRegisterEmailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait...");
        progressDialog.setCanceledOnTouchOutside(false);

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.haveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterEmailActivity.this,LoginEmailActivity.class));
            }
        });

        binding.registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateDate();
            }
        });
    }

    private String email, password, cPassword;
    private void validateDate() {
        email=binding.emailEt.getText().toString().trim();
        password=binding.passwordEt.getText().toString();
        cPassword=binding.cpasswordEt.getText().toString();

        Log.d(TAG,"validateData: Email: "+email);
        Log.d(TAG,"validateData: Password: "+password);
        Log.d(TAG,"validateData: Confirm Password: "+cPassword);

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            binding.emailEt.setError("Invalid Email Pattern");
            binding.emailEt.requestFocus();
        }else if(password.isEmpty()){
            binding.passwordEt.setError("Enter Password");
            binding.passwordEt.requestFocus();
        }else if (!password.equals(cPassword)){
            binding.cpasswordEt.setError("Password doesn't match");
            binding.cpasswordEt.requestFocus();
        }else {
            registerUser();
        }
    }

    private void registerUser() {
        progressDialog.setMessage("Creating Account");
        progressDialog.show();
        firebaseAuth.createUserWithEmailAndPassword(email,password)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                      Log.d(TAG, "onSuccess: Registered");
                      updateUserInfo();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ",e);
                        MyUtils.toast(RegisterEmailActivity.this,"Failed due to"+e.getMessage());
                        progressDialog.dismiss();
                    }
                });
    }
    private void updateUserInfo(){
        progressDialog.setMessage("Saving User Info...");
        long timestamp=MyUtils.timestamp();
        String registeredUserEmail = firebaseAuth.getCurrentUser().getEmail();
        String registeredUserUid = firebaseAuth.getUid();

        HashMap<String, Object> hashMap=new HashMap<>();
        hashMap.put("uid", registeredUserUid);
        hashMap.put("email", registeredUserEmail);
        hashMap.put("name", "");
        hashMap.put("timestamp", timestamp);
        hashMap.put("phoneCode", "");
        hashMap.put("phoneNumber", "");
        hashMap.put("profileImageUrl", "");
        hashMap.put("dob", "");
        hashMap.put("userType",MyUtils.USER_TYPE_EMAIL);
        hashMap.put("token", "");

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registeredUserUid)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Info saved...");
                        progressDialog.dismiss();
                        startActivity(new Intent(RegisterEmailActivity.this, MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ", e);
                        MyUtils.toast(RegisterEmailActivity.this,"Failed to save due to "+e.getMessage());
                        progressDialog.dismiss();
                    }
                });


    }
}