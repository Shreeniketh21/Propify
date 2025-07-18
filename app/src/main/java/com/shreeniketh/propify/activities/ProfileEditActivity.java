package com.shreeniketh.propify.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.Manifest;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityProfileEditBinding;

import java.util.HashMap;
import java.util.Map;

public class ProfileEditActivity extends AppCompatActivity {

    private ActivityProfileEditBinding binding;
    private ProgressDialog progressDialog;
    private static final String TAG="PROFILE_EDIT_TAG";
    private FirebaseAuth firebaseAuth;
    private String myUserType="";
    private Uri imageUri=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding=ActivityProfileEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        loadMyInfo();

        binding.toolbarBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.profileImagePickFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickDialog();
            }
        });

        binding.updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               validateData();
            }
        });
    }

    private String name="";
    private String dob="";
    private String email="";
    private String phoneCode="";
    private String phoneNumber="";
    private void validateData() {
        name = binding.nameEt.getText().toString().trim();
        dob = binding.dobEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.countryCodePicker.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        // Ensure required fields are not empty
        if (name.isEmpty()) {
            MyUtils.toast(this, "Name cannot be empty");
            return;
        }
        if (dob.isEmpty()) {
            MyUtils.toast(this, "DOB cannot be empty");
            return;
        }
        if (email.isEmpty() && myUserType.equals(MyUtils.USER_TYPE_PHONE)) {
            MyUtils.toast(this, "Email is required");
            return;
        }
        if (phoneNumber.isEmpty() && (myUserType.equals(MyUtils.USER_TYPE_EMAIL) || myUserType.equals(MyUtils.USER_TYPE_GOOGLE))) {
            MyUtils.toast(this, "Phone number is required");
            return;
        }

        if (imageUri == null) {
            updateProfileDb(null);
        } else {
            uploadProfileImageStorage();
        }
    }


    private void uploadProfileImageStorage(){
        Log.d(TAG,"uploadProfileImageStorage: ");
        progressDialog.setMessage("Uploading user profile image...");
        progressDialog.show();
        String filePathAndName="UserImages/" + firebaseAuth.getUid();

        StorageReference ref= FirebaseStorage.getInstance().getReference().child(filePathAndName);
        ref.putFile(imageUri)
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                        double progress = (100.0 * snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        Log.d(TAG,"onSuccess: Progress: "+progress);
                        progressDialog.setMessage("Uploading profile image. Progress: "+ (int)progress +"%");
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG,"onSuccess: Uploaded");
                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while(!uriTask.isSuccessful());
                        String uploadedImageUrl=uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            updateProfileDb(uploadedImageUrl);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"onFailure: ",e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this,"Failed to upload profile image due to "+e.getMessage());
                    }
                });
    }
    private void updateProfileDb(String imageUrl) {
        if (firebaseAuth.getCurrentUser() == null) {
            MyUtils.toast(this, "User not logged in!");
            return;
        }

        progressDialog.setMessage("Updating user info...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("name", name);
        hashMap.put("dob", dob);
        if (imageUrl != null) {
            hashMap.put("profileImageUrl", imageUrl);
        }
        if(myUserType.equals(MyUtils.USER_TYPE_EMAIL)||myUserType.equals(MyUtils.USER_TYPE_GOOGLE)){
            hashMap.put("phoneCode", "" +phoneCode);
            hashMap.put("phoneNumber", "" +phoneNumber);
        }else if(myUserType.equals(MyUtils.USER_TYPE_PHONE)){
            hashMap.put("email", "" +email);
        }

        String userId = firebaseAuth.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");

        ref.child(userId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Profile updated successfully");
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Profile Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Profile update failed", e);
                        progressDialog.dismiss();
                        MyUtils.toast(ProfileEditActivity.this, "Update failed: " + e.getMessage());
                    }
                });
    }

    private void imagePickDialog(){
        PopupMenu popupMenu=new PopupMenu(this,binding.profileImagePickFab);
        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId=item.getItemId();
                if(itemId==1){
                    Log.d(TAG,"onMenuClick: Camera Clicked, checking permissions");
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA});
                    }else{
                        requestCameraPermissions.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});
                    }
                }else if(itemId==2){
                    Log.d(TAG,"onMenuItemClick: Gallery Clicked...");
                    pickImageGallery();
                }
                return true;
            }
        });
    }

    private final ActivityResultLauncher<String[]> requestCameraPermissions=registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG, "onActivityResult: " + result.toString());
                    boolean areAllGranted = true;


                    for (Boolean isGranted : result.values()) {
                        areAllGranted = areAllGranted && isGranted;
                    }

                    if (areAllGranted) {
                        pickImageCamera();
                    } else {
                        Log.d(TAG, "onActivityResult: All or either one permission denied...");
                        MyUtils.toast(ProfileEditActivity.this, "Camera or Storage or both permission denied...");
                    }
                }

            });

    private void pickImageCamera(){
        Log.d(TAG,"pickImageCamera: ");

        ContentValues contentValues=new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"temp_image");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"temp_image_description");
        imageUri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()== Activity.RESULT_OK){
                     Log.d(TAG,"onActivityResult: Image Picked: "+imageUri);

                     try{
                         Glide.with(ProfileEditActivity.this)
                                 .load(imageUri)
                                 .placeholder(R.drawable.person_black)
                                 .into(binding.profileIv);
                     }catch (Exception e){
                         Log.e(TAG,"onActivityResult: ",e);
                     }
                    }else{
                        Log.d(TAG,"onActivityResult: Cancelled...");
                        MyUtils.toast(ProfileEditActivity.this,"Cancelled");
                    }
                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG,"pickImageGallery: ");
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if(result.getResultCode()==Activity.RESULT_OK){
                        Intent data=result.getData();
                        imageUri=data.getData();
                        Log.d(TAG,"onActivityResult: Image Picked From Gallery: "+imageUri);
                        try{
                            try{
                                Glide.with(ProfileEditActivity.this)
                                        .load(imageUri)
                                        .placeholder(R.drawable.person_black)
                                        .into(binding.profileIv);
                            }catch (Exception e){
                                Log.e(TAG,"onActivityResult: ",e);
                            }
                        }catch (Exception e){
                            Log.d(TAG, "onActivityResult: ",e);
                        }
                    }else{
                        Log.d(TAG,"onActivityResult: Cancelled...");
                        MyUtils.toast(ProfileEditActivity.this,"Cancelled...!");
                    }
                }
            }
    );

    private void loadMyInfo(){
        Log.d(TAG,"loadMyInfo: ");
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
        ref.child(""+firebaseAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String dob=""+ snapshot.child("dob").getValue();
                        String email=""+ snapshot.child("email").getValue();
                        String name=""+ snapshot.child("name").getValue();
                        String phoneCode=""+ snapshot.child("phoneCode").getValue();
                        String phoneNumber=""+ snapshot.child("phoneNumber").getValue();
                        String profileImageUrl=""+ snapshot.child("profileImageUrl").getValue();
                        String timestamp=""+ snapshot.child("timestamp").getValue();
                        myUserType=""+ snapshot.child("userType").getValue();

                        String phone=phoneCode+phoneNumber;

                        if(myUserType.equals(MyUtils.USER_TYPE_EMAIL) || myUserType.equals(MyUtils.USER_TYPE_GOOGLE)){
                            binding.emailEt.setEnabled(false);
                        }else{
                            binding.phoneNumberEt.setEnabled(false);
                            binding.countryCodePicker.setEnabled(false);
                        }

                        binding.emailEt.setText(email);
                        binding.dobEt.setText(dob);
                        binding.nameEt.setText(name);
                        binding.fullNameTv.setText(name);
                        binding.phoneNumberEt.setText(phoneNumber);
                        try {
                            int phoneCodeInt=Integer.parseInt(phoneCode.replace("+",""));
                            binding.countryCodePicker.setCountryForPhoneCode(phoneCodeInt);
                        }catch (Exception e){
                            Log.e(TAG,"onDataChange: ",e);
                        }

                        try{
                            Glide.with(ProfileEditActivity.this)
                                    .load(profileImageUrl)
                                    .placeholder(R.drawable.person_black)
                                    .into(binding.profileIv);
                        }catch (Exception e){
                            Log.e(TAG,"onDataChange: ",e);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}