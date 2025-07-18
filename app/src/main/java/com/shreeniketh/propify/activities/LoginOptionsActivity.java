package com.shreeniketh.propify.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityLoginOptionsBinding;

import com.google.android.gms.tasks.Task;

import java.util.HashMap;

public class LoginOptionsActivity extends AppCompatActivity {
    private ActivityLoginOptionsBinding binding;
    private static final String TAG="LOGIN_OPTIONS_TAG";
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityLoginOptionsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please Wait");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth=FirebaseAuth.getInstance();

        GoogleSignInOptions gso=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,gso);

        binding.skipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.loginGoogleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                beginGoogleLogin();
            }
        });

        binding.loginEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginOptionsActivity.this,LoginEmailActivity.class));
            }
        });

        binding.loginPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginOptionsActivity.this, LoginPhoneActivity.class));
            }
        });

    }




    private void beginGoogleLogin(){
        Log.d(TAG,"beginGoogleLogin");
        Intent googleSignInIntent=mGoogleSignInClient.getSignInIntent();
        googleSignInnARL.launch(googleSignInIntent);

    }
    private ActivityResultLauncher<Intent> googleSignInnARL = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG, "onActivityResult: ");

                    if(result.getResultCode() == Activity.RESULT_OK){
                        Intent data=result.getData();
                        Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
                    try{
                        GoogleSignInAccount account=task.getResult(ApiException.class);
                        Log.d(TAG,"onActivityResult: AccountID: "+account.getId());
                        firebaseAuthWithGoogleAccount(account.getIdToken());
                    }catch(Exception e){
                        Log.d(TAG,"onActivityResult: ", e);
                    }
                    }else{
                        Log.d(TAG, "onActivityResult: Cancelled");
                        MyUtils.toast(LoginOptionsActivity.this,"Cancelled...!");
                    }
                }
            });
    private void firebaseAuthWithGoogleAccount(String idToken){
        Log.d(TAG,"firebaseAuthWithGoogleAccount: idToken: "+idToken);
        AuthCredential credential= GoogleAuthProvider.getCredential(idToken,null);
        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        if (authResult.getAdditionalUserInfo().isNewUser()){
                            Log.d(TAG,"onSuccess: Account Created...!");
                            updateUserInfoDb();
                        }else{
                            Log.d(TAG,"onSucess: Logged In...!");
                            startActivity(new Intent(LoginOptionsActivity.this, MainActivity.class));
                            finishAffinity();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: ", e);
                    }
                });
    }

    private void updateUserInfoDb() {
        Log.d(TAG,"updateUserInfoDb: ");

        progressDialog.setMessage("Saving user info...!");
        progressDialog.show();

        long timestamp=MyUtils.timestamp();
        String registeredUserUId=firebaseAuth.getUid();
        String registeredUserEmail=firebaseAuth.getCurrentUser().getEmail();
        String name=firebaseAuth.getCurrentUser().getDisplayName();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("uid",registeredUserUId);
        hashMap.put("email",registeredUserEmail);
        hashMap.put("timestamp",timestamp);
        hashMap.put("phoneCode","");
        hashMap.put("phoneNumber","");
        hashMap.put("profileImageUrl","");
        hashMap.put("dob","");
        hashMap.put("userType",MyUtils.USER_TYPE_GOOGLE);
        hashMap.put("token","");

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(registeredUserUId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSucess: User info saved...!");
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginOptionsActivity.this,MainActivity.class));
                        finishAffinity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure: ", e);
                        progressDialog.dismiss();
                        MyUtils.toast(LoginOptionsActivity.this,"Failed to save due to "+e.getMessage());
                    }
                });
    }
}