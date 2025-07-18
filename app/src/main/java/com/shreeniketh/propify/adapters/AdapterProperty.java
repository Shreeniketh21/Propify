package com.shreeniketh.propify.adapters;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.activities.PropertyDetailActivity;
import com.shreeniketh.propify.databinding.RowPropertyBinding;
import com.shreeniketh.propify.models.ModelProperty;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class AdapterProperty extends RecyclerView.Adapter<AdapterProperty.HolderProperty>{

    private RowPropertyBinding binding;
    private Context context;
    private ArrayList<ModelProperty> propertyArrayList, filter;
    private Timer timer;
    private final ArrayList<ModelProperty> filterSource;

    public AdapterProperty(Context context, ArrayList<ModelProperty> propertyArrayList) {
        this.context = context;
        this.propertyArrayList = propertyArrayList;
        this.filterSource = new ArrayList<>(propertyArrayList);
        this.filter = new ArrayList<>(propertyArrayList);
    }

    @NonNull
    @Override
    public HolderProperty onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowPropertyBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolderProperty(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderProperty holder, int position) {
        ModelProperty modelProperty = propertyArrayList.get(position);

        String title = modelProperty.getTitle();
        double price = modelProperty.getPrice();
        String formattedPrice = MyUtils.formatCurrency(price);
        String description = modelProperty.getDescription();
        String address = modelProperty.getAddress();
        String purpose = modelProperty.getPurpose();
        String category = modelProperty.getCategory();
        String subcategory = modelProperty.getSubcategory();
        long timestamp = modelProperty.getTimestamp();
        String formattedDate = MyUtils.formatTimestammpDate(timestamp);
        String phoneNumber = modelProperty.getPhoneNumber();
        long propertyFloors = modelProperty.getFloors();
        long propertyBedrooms = modelProperty.getBedRooms();
        long propertyBathrooms = modelProperty.getBathRooms();
        double propertyAreaSize = modelProperty.getAreaSize();

        //propertyAreaSize

        loadPropertyFirstImage(modelProperty, holder);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.purposeTv.setText(purpose);
        holder.categoryTv.setText(category);
        holder.subcategoryTv.setText(subcategory);
        holder.addressTv.setText(address);
        holder.dateTv.setText(formattedDate);
        holder.priceTv.setText(formattedPrice);



        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PropertyDetailActivity.class);
            intent.putExtra("propertyId", modelProperty.getId());
            intent.putExtra("title", modelProperty.getTitle());
            intent.putExtra("price", modelProperty.getPrice());
            intent.putExtra("contact",modelProperty.getPhoneNumber());
            intent.putExtra("location", modelProperty.getCity() + ", " + modelProperty.getCountry());
            intent.putExtra("description", modelProperty.getDescription());
            intent.putExtra("address",modelProperty.getAddress());
            intent.putExtra("purpose",modelProperty.getPurpose());
            intent.putExtra("category",modelProperty.getCategory());
            intent.putExtra("subcategory", modelProperty.getSubcategory());
            intent.putExtra("areasize", modelProperty.getAreaSize());
            intent.putExtra("areasizeunit",modelProperty.getAreaSizeUnit());
            intent.putExtra("status", modelProperty.getStatus());
            intent.putExtra("floors", modelProperty.getFloors());
            intent.putExtra("bedrooms",modelProperty.getBedRooms());
            intent.putExtra("bathrooms", modelProperty.getBathRooms());
            context.startActivity(intent);
        });
    }

    private void loadPropertyFirstImage(ModelProperty modelProperty, HolderProperty holder) {
        Log.d(TAG, "loadPropertyFirstImage: ");
        String propertyId = modelProperty.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");
        ref.child(propertyId).child("Images").limitToFirst(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            String imageUrl = "" + ds.child("imageUrl").getValue();
                            Log.d(TAG, "onDataChange: imageUrl: " + imageUrl);

                            try {
                                Glide.with(context)
                                        .load(imageUrl)
                                        .placeholder(R.drawable.building_black)
                                        .into(holder.propertyIv);
                            } catch (Exception e) {
                                Log.e(TAG, "onDataChange: ", e);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Failed to load image", error.toException());
                    }
                });
    }

    @Override
    public int getItemCount() {
        return propertyArrayList.size();
    }

    public void searchProperty(final String searchKeyword) {
        if (timer != null) {
            timer.cancel(); // Cancel any running timer to avoid duplicate filtering
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ArrayList<ModelProperty> temp = new ArrayList<>();

                if (searchKeyword.trim().isEmpty()) {
                    temp.addAll(filterSource); // Reset to all items if search is empty
                } else {
                    for (ModelProperty item : filterSource) {
                        if (item.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || item.getCategory().toLowerCase().contains(searchKeyword.toLowerCase())
                                || item.getPurpose().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            temp.add(item);
                        }
                    }
                }

                // Update the adapter list on the main thread
                new Handler(Looper.getMainLooper()).post(() -> {
                    propertyArrayList.clear();
                    propertyArrayList.addAll(temp);
                    notifyDataSetChanged();
                });
            }
        }, 300); // Delay for better performance
    }

    class HolderProperty extends RecyclerView.ViewHolder {

        ShapeableImageView propertyIv;
        TextView titleTv, descriptionTv, purposeTv, categoryTv, subcategoryTv, addressTv, dateTv, priceTv,propertyFloors, propertyBedrooms, propertyBathrooms,propertyAreaSize;

        public HolderProperty(@NonNull View itemView) {
            super(itemView);

            propertyIv = binding.propertyIv;
            titleTv = binding.titleTv;
            descriptionTv = binding.descriptionTv;
            purposeTv = binding.purposeTv;
            categoryTv = binding.categoryTv;
            subcategoryTv = binding.subCategoryTv;
            addressTv = binding.addressTv;
            dateTv = binding.dateTv;
            priceTv = binding.priceTv;
        }
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
