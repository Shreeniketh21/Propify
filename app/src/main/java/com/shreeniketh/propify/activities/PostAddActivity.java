package com.shreeniketh.propify.activities;

import android.Manifest;
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
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.shreeniketh.propify.AdapterImagePicked;
import com.shreeniketh.propify.ModelImagePicked;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityPostAddBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PostAddActivity extends AppCompatActivity {

    private static final String TAG="POST_ADD_PROB";
    private Uri imageUri=null;
    private ProgressDialog progressDialog;
    private FirebaseAuth firebaseAuth;

    private ActivityPostAddBinding binding;
    private ArrayAdapter<String> adapterPropertySubcategory;
    private ArrayList<ModelImagePicked> imagePickedArrayList;
    private AdapterImagePicked adapterImagePicked;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityPostAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog=new ProgressDialog(this);
        progressDialog.setTitle("Please wait...!");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        ArrayAdapter<String> adapterAreaSize = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, MyUtils.propertyAreaSizeUnit);
        binding.areaSizeUnitAct.setAdapter(adapterAreaSize);

        imagePickedArrayList = new ArrayList<>();
        loadImages();

        propertyCategoryHomes();

        binding.propertyCategoryTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0){
                    category = MyUtils.propertyTypes[0];
                    propertyCategoryHomes();
                } else if (position == 1) {
                    category = MyUtils.propertyTypes[1];
                    propertyCategoryPlots();
                } else if (position == 2) {
                    category = MyUtils.propertyTypes[2];
                    propertyCategoryCommercial();
                }
                Log.d(TAG,"onTabSelected: category: "+category);

                binding.propertySubCategoryAct.setAdapter(adapterPropertySubcategory);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.purposeRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                RadioButton selectedRadioButton = findViewById(checkedId);
                purpose = selectedRadioButton.getText().toString();
                Log.d(TAG,"onCheckedChanged: purpose: "+purpose);
            }
        });

        binding.pickImagesTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImagePickOptions();
            }
        });

        binding.locationAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(PostAddActivity.this,LocationPickerActivity.class);
                locationPickerActivityResultLauncher.launch(intent);
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });

        binding.toolbarBackBtn.setOnClickListener(v -> finish());
    }

    private ActivityResultLauncher<Intent> locationPickerActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG,"onActivityResult: result: "+result);
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        if (data != null){
                            latitude=data.getDoubleExtra("latitude",0);
                            longitude=data.getDoubleExtra("longitude",0);
                            address=data.getStringExtra("address");
                            city=data.getStringExtra("city");
                            country=data.getStringExtra("country");
                            state = data.getStringExtra("state");

                            Log.d(TAG,"onActivityResult: latitude: "+latitude);
                            Log.d(TAG,"onActivityResult: longitude: "+longitude);
                            Log.d(TAG,"onActivityResult: address: "+address);
                            Log.d(TAG,"onActivityResult: city: "+city);
                            Log.d(TAG,"onActivityResult: country: "+country);
                            Log.d(TAG,"onActivityResult: state: "+state);

                            binding.locationAct.setText(address);
                        }
                    }
                }
            }
    );

    private void propertyCategoryHomes(){
        binding.floorsEt.setVisibility(View.VISIBLE);
        binding.bedRoomsEt.setVisibility(View.VISIBLE);
        binding.bathRoomsEt.setVisibility(View.VISIBLE);

        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,MyUtils.propertyTypesHomes);

        binding.propertySubCategoryAct.setAdapter(adapterPropertySubcategory);

        binding.propertySubCategoryAct.setText("");
    }
    private void propertyCategoryPlots(){
        binding.floorsEt.setVisibility(View.GONE);
        binding.bedRoomsEt.setVisibility(View.GONE);
        binding.bathRoomsEt.setVisibility(View.GONE);

        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,MyUtils.propertyTypesPlots);

        binding.propertySubCategoryAct.setAdapter(adapterPropertySubcategory);

        binding.propertySubCategoryAct.setText("");
    }
    private void propertyCategoryCommercial(){
        binding.floorsEt.setVisibility(View.VISIBLE);
        binding.bedRoomsEt.setVisibility(View.GONE);
        binding.bathRoomsEt.setVisibility(View.GONE);

        adapterPropertySubcategory = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,MyUtils.propertyTypesCommercial);

        binding.propertySubCategoryAct.setAdapter(adapterPropertySubcategory);

        binding.propertySubCategoryAct.setText("");
    }

   private void loadImages(){
        Log.d(TAG,"loadImages: ");
        adapterImagePicked =new AdapterImagePicked(this,imagePickedArrayList);
        binding.imageRv.setAdapter(adapterImagePicked);
    }

    private void showImagePickOptions() {
        Log.d(TAG,"showImagePickOptions: ");

        PopupMenu popupMenu=new PopupMenu(this,binding.pickImagesTv);

        popupMenu.getMenu().add(Menu.NONE,1,1,"Camera");
        popupMenu.getMenu().add(Menu.NONE,2,2,"Gallery");

        popupMenu.show();

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == 1){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        String[] permissions = new String[]{Manifest.permission.CAMERA};
                        requestCameraPermissions.launch(permissions);
                    }else{
                        String[] permissions = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestCameraPermissions.launch(permissions);
                    }
                }else if (itemId == 2){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
                        pickImageGallery();
                    }else {
                        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
                        requestStoragePermission.launch(storagePermission);
                    }
                }
                return false;
            }
        });
    }

    private ActivityResultLauncher<String> requestStoragePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    Log.d(TAG,"onActivityResult: isGranted: "+isGranted);
                    if(isGranted){

                    }else{
                        MyUtils.toast(PostAddActivity.this,"Storage permission denied: ");
                    }
                }
            }
    );

    private ActivityResultLauncher<String[]> requestCameraPermissions = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(),
            new ActivityResultCallback<Map<String, Boolean>>() {
                @Override
                public void onActivityResult(Map<String, Boolean> result) {
                    Log.d(TAG,"onActivityResult: result: "+result);
                    boolean areAllGranted = true;
                    for (Boolean isGranted: result.values()){
                        areAllGranted=areAllGranted && isGranted;
                    }
                    if (areAllGranted){
                        pickImageCamera();
                    }else {
                        MyUtils.toast(PostAddActivity.this,"Camera or Storage or both permission denied!");
                    }
                }
            }
    );

    private void pickImageGallery(){
        Log.d(TAG,"pickImageGallery: ");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG,"onActivityResult: ");
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG,"onActivityResult: imageUri: "+imageUri);
                        String timestamp = ""+MyUtils.timestamp();
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null,false);
                        imagePickedArrayList.add(modelImagePicked);
                        loadImages();
                    }else{
                        MyUtils.toast(PostAddActivity.this,"Cancelled!");
                    }
                }
            }
    );

    private void pickImageCamera(){
        Log.d(TAG,"pickImageCamera: ");

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"TEMP_TITLE");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"TEMP_DESCRIPTION");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);

        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        cameraActivityResultLauncher.launch(intent);
    }

    private ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    Log.d(TAG,"onActivityResult: ");
                    if (result.getResultCode()==Activity.RESULT_OK){
                        Log.d(TAG,"onActivityResult: imageUri: "+imageUri);
                        String timestamp = ""+MyUtils.timestamp();
                        ModelImagePicked modelImagePicked = new ModelImagePicked(timestamp, imageUri, null,false);
                        imagePickedArrayList.add(modelImagePicked);
                        loadImages();
                    }else {
                        MyUtils.toast(PostAddActivity.this,"Cancelled!");
                    }
                }
            }
    );

    private String category = MyUtils.propertyTypes[0];
    private String purpose = MyUtils.PROERTY_PURPOSE_SELL;

    private String subCategory = "";
    private String floors = "";
    private String bedRooms = "";
    private String bathRooms = "";
    private String areaSize = "";
    private String areaSizeUnit = "";
    private String price = "";
    private String title = "";
    private String description = "";
    private String email = "";
    private String phoneCode = "";
    private String phoneNumber = "";
    private String country = "";
    private String city = "";
    private String address = "";
    private String state = "";
    private double latitude = 0;
    private double longitude = 0;

    private void validateData(){
        Log.d(TAG,"validateData: ");

        subCategory = binding.propertySubCategoryAct.getText().toString().trim();
        floors = binding.floorsEt.getText().toString().trim();
        bedRooms = binding.bedRoomsEt.getText().toString().trim();
        bathRooms = binding.bathRoomsEt.getText().toString().trim();
        areaSize = binding.areaSizeEt.getText().toString().trim();
        areaSizeUnit = binding.areaSizeUnitAct.getText().toString().trim();
        address = binding.locationAct.getText().toString().trim();
        price = binding.priceEt.getText().toString().trim();
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        email = binding.emailEt.getText().toString().trim();
        phoneCode = binding.phoneCodeTil.getSelectedCountryCodeWithPlus();
        phoneNumber = binding.phoneNumberEt.getText().toString().trim();

        if (subCategory.isEmpty()){
            binding.propertySubCategoryAct.setError("Choose Subcategory...!");
            binding.propertySubCategoryAct.requestFocus();
        }else if (category.equals(MyUtils.propertyTypes[0]) && floors.isEmpty()){
            binding.floorsEt.setError("Enter Floors Count...!");
            binding.floorsEt.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && bedRooms.isEmpty()) {
            binding.bedRoomsEt.setError("Enter Bedrooms Count...!");
            binding.bedRoomsEt.requestFocus();
        } else if (category.equals(MyUtils.propertyTypes[0]) && bathRooms.isEmpty()) {
            binding.bathRoomsEt.setError("Enter Bathrooms Count...!");
            binding.bathRoomsEt.requestFocus();
        } else if (areaSize.isEmpty()) {
            binding.areaSizeEt.setError("Enter Area Size...!");
            binding.areaSizeEt.requestFocus();
        }else if (areaSizeUnit.isEmpty()) {
            binding.areaSizeUnitAct.setError("Choose Area Size Unit...!");
            binding.areaSizeUnitAct.requestFocus();
        }else if (address.isEmpty()) {
            binding.locationAct.setError("Pick Location...!");
            binding.locationAct.requestFocus();
        } else if (price.isEmpty()) {
            binding.priceEt.setError("Enter Price");
            binding.priceEt.requestFocus();
        } else if (title.isEmpty()) {
            binding.titleEt.setError("Enter Title...!");
            binding.titleEt.requestFocus();
        } else if (description.isEmpty()) {
            binding.descriptionEt.setError("Enter Description");
            binding.descriptionEt.requestFocus();
        } else if (phoneNumber.isEmpty()) {
            binding.phoneNumberEt.setError("Enter Phone Number...!");
            binding.phoneNumberEt.requestFocus();
        } else if (imagePickedArrayList.isEmpty()) {
            MyUtils.toast(this,"Pick at-least one image...!");
        }else{
            postAd();
        }
    }
    private void postAd(){
        Log.d(TAG,"postAd: ");

        progressDialog.setMessage("Publishing Ad");
        progressDialog.show();

        if (floors.isEmpty()){
            floors = "0";
        }

        if(bedRooms.isEmpty()){
            bedRooms = "0";
        }
        if (bathRooms.isEmpty()){
            bathRooms = "0";
        }

        long timestamp = MyUtils.timestamp();

        DatabaseReference refProperties = FirebaseDatabase.getInstance().getReference("Properties");

        String ketId = refProperties.push().getKey();

        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("id", ""+ketId);
        hashMap.put("uid", ""+firebaseAuth.getUid());
        hashMap.put("purpose", ""+purpose);
        hashMap.put("category", ""+category);
        hashMap.put("subcategory", ""+subCategory);
        hashMap.put("areaSizeUnit", ""+areaSizeUnit);
        hashMap.put("areaSize", Double.parseDouble(areaSize));
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("email", ""+email);
        hashMap.put("phoneCode", ""+phoneCode);
        hashMap.put("phoneNumber", ""+phoneNumber);
        hashMap.put("country", ""+country);
        hashMap.put("city", ""+city);
        hashMap.put("address", ""+address);
        hashMap.put("state", ""+state);
        hashMap.put("status", ""+MyUtils.AD_STATUS_AVAILABLE);
        hashMap.put("floors", Long.parseLong(floors));
        hashMap.put("bedRooms", Long.parseLong(bedRooms));
        hashMap.put("bathRooms", Long.parseLong(bathRooms));
        hashMap.put("price", Double.parseDouble(price));
        hashMap.put("timestamp", timestamp);
        hashMap.put("latitude",latitude);
        hashMap.put("longitude",longitude);

        refProperties.child(ketId)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG,"onSuccess: Ad Published");
                        uploadImagesStorage(ketId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"onFailure", e);
                        progressDialog.dismiss();
                        MyUtils.toast(PostAddActivity.this,"Failed to publish due to "+e.getMessage());
                    }
                });
    }

    private void uploadImagesStorage(String propertyId){
        Log.d(TAG,"uploadImagesStorage: propertyId: "+propertyId);
        for (int i=0; i< imagePickedArrayList.size(); i++){
            ModelImagePicked modelImagePicked = imagePickedArrayList.get(i);
            if (!modelImagePicked.isFromInternet()){
                String imageName=modelImagePicked.getId();
                String filePathAndName = "Properties/"+imageName;
                int imageIndexForProgress = i+1;
                Uri pickedImageUri = modelImagePicked.getImageUri();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
                storageReference.putFile(pickedImageUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double progress=(100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                String message="uploading" + imageIndexForProgress + " of " + imagePickedArrayList.size() + " images... \n Progres " + (int)progress +"%";
                                Log.d(TAG,"onProgress: message: "+message);

                                progressDialog.setMessage(message);
                                progressDialog.show();
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.d(TAG,"onSuccess: ");

                                Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                                while (!uriTask.isSuccessful());
                                Uri uploadedImageUrl = uriTask.getResult();

                                if (uriTask.isSuccessful()){
                                    HashMap<String,Object> hashMap = new HashMap<>();
                                    hashMap.put("id", ""+modelImagePicked.getId());
                                    hashMap.put("imageUrl", ""+uploadedImageUrl);

                                    DatabaseReference refProperties = FirebaseDatabase.getInstance().getReference("Properties");
                                    refProperties.child(propertyId)
                                            .child("Images")
                                            .child(imageName)
                                            .updateChildren(hashMap);
                                }
                                progressDialog.dismiss();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "onFailure", e);
                                MyUtils.toast(PostAddActivity.this,"Failed to upload due to "+e.getMessage());
                                progressDialog.dismiss();
                            }
                        });
            }
        }
    }
}