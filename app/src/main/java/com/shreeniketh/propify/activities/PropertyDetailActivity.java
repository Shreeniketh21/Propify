package com.shreeniketh.propify.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.PropertyImageSliderAdapter;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityPropertyDetailBinding;

import java.util.ArrayList;

public class PropertyDetailActivity extends AppCompatActivity {

    private ActivityPropertyDetailBinding binding;
    private PropertyImageSliderAdapter imageSliderAdapter;
    private ArrayList<String> imageUrls = new ArrayList<>();
    private static final String TAG = "PropertyDetailActivity";
    private String propertyId; // Store Property ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPropertyDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        propertyId = intent.getStringExtra("propertyId"); // Retrieve propertyId correctly

        if (propertyId != null) {
            loadPropertyImages(propertyId);
        } else {
            Log.e(TAG, "Property ID is missing.");
            return;
        }

        // Set property details
        binding.propertyTitle.setText(intent.getStringExtra("title"));
        binding.propertyPrice.setText("₹ " + intent.getDoubleExtra("price", 0));
        binding.propertyLocation.setText(intent.getStringExtra("location"));
        binding.propertyDescription.setText(intent.getStringExtra("description"));
        binding.propertyPurpose.setText("Purpose: " + intent.getStringExtra("purpose"));
        binding.propertyCategory.setText("Category: " + intent.getStringExtra("category"));
        binding.propertySubcategory.setText("Subcategory: " + intent.getStringExtra("subcategory"));
        binding.propertyAreaSize.setText("Area Size: " + intent.getDoubleExtra("areasize", 0) + " " + intent.getStringExtra("areasizeunit"));
        binding.propertyStatus.setText("Status: " + intent.getStringExtra("status"));
        binding.propertyFloors.setText("Floors: " + intent.getLongExtra("floors", 0));
        binding.propertyBedrooms.setText("Bedrooms: " + intent.getLongExtra("bedrooms", 0));
        binding.propertyBathrooms.setText("Bathrooms: " + intent.getLongExtra("bathrooms", 0));

        // Back Button
        binding.toolbarBackBtn.setOnClickListener(v -> finish());

        // Call Button
        binding.chatOwnerButton.setOnClickListener(view -> {
            String contact = intent.getStringExtra("contact");
            if (contact != null && !contact.isEmpty()) {
                Intent dialIntent = new Intent(Intent.ACTION_DIAL);
                dialIntent.setData(Uri.parse("tel:" + contact));
                startActivity(dialIntent);
            } else {
                MyUtils.toast(PropertyDetailActivity.this, "Couldn't fetch the number...");
            }
        });

        // Map Button
        binding.viewMapButton.setOnClickListener(view -> {
            String address = intent.getStringExtra("address");
            if (address != null && !address.isEmpty()) {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps"); // Open in Google Maps app

                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                } else {
                    MyUtils.toast(PropertyDetailActivity.this, "Google Maps is not installed.");
                }
            } else {
                MyUtils.toast(PropertyDetailActivity.this, "Couldn't fetch the location...");
            }
        });

        // Delete Button
        binding.deleteButton.setOnClickListener(view -> deleteProperty());

    }

    private void loadPropertyImages(String propertyId) {
        DatabaseReference propertyImagesRef = FirebaseDatabase.getInstance().getReference("Properties")
                .child(propertyId).child("Images");

        propertyImagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> imageUrls = new ArrayList<>();

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    if (imageUrl != null) {
                        Log.d(TAG, "Fetched Image URL: " + imageUrl);
                        imageUrls.add(imageUrl);
                    }
                }

                if (imageUrls.isEmpty()) {
                    Log.e(TAG, "No images found for property: " + propertyId);
                }

                // Set adapter for ViewPager
                PropertyImageSliderAdapter imageSliderAdapter = new PropertyImageSliderAdapter(PropertyDetailActivity.this, imageUrls);
                binding.propertyImageSlider.setAdapter(imageSliderAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load images: " + error.getMessage());
            }
        });
    }

    private void deleteProperty() {
        if (propertyId == null || propertyId.isEmpty()) {
            Toast.makeText(this, "Property ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference propertyRef = FirebaseDatabase.getInstance().getReference("Properties").child(propertyId);

        // First, delete images from Firebase Storage
        propertyRef.child("Images").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String imageUrl = ds.child("imageUrl").getValue(String.class);
                    if (imageUrl != null) {
                        StorageReference imageRef = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl);
                        imageRef.delete()
                                .addOnSuccessListener(aVoid -> Log.d(TAG, "Image deleted successfully"))
                                .addOnFailureListener(e -> Log.e(TAG, "Failed to delete image", e));
                    }
                }

                // Now, delete the property from Firebase Database
                propertyRef.removeValue()
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(PropertyDetailActivity.this, "Property deleted!", Toast.LENGTH_SHORT).show();
                            finish(); // Close activity and go back
                        })
                        .addOnFailureListener(e -> Toast.makeText(PropertyDetailActivity.this, "Failed to delete", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PropertyDetailActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
