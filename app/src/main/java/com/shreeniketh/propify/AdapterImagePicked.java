package com.shreeniketh.propify;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.shreeniketh.propify.databinding.RowImagesPickedBinding;

import java.util.ArrayList;

public class AdapterImagePicked extends RecyclerView.Adapter<AdapterImagePicked.HolderImagePicked>{

    private RowImagesPickedBinding binding;
    private static final String TAG="IMAGES_TAG";

    private Context context;

    private ArrayList<ModelImagePicked> imagePickedArrayList;

    public AdapterImagePicked(Context context, ArrayList<ModelImagePicked> imagePickedArrayList){
        this.context=context;
        this.imagePickedArrayList=imagePickedArrayList;
    }

    @NonNull
    @Override
    public HolderImagePicked onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowImagesPickedBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderImagePicked(binding.getRoot());
    }

    @SuppressLint("RecyclerView")
    @Override
    public void onBindViewHolder(@NonNull HolderImagePicked holder, int position) {
        ModelImagePicked modelImagePicked = imagePickedArrayList.get(position);

        Uri imageUri = modelImagePicked.getImageUri();

        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.image_black)
                .into(holder.imageTv);

        holder.closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imagePickedArrayList.remove(modelImagePicked);
                notifyItemRemoved(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imagePickedArrayList.size();
    }

    class HolderImagePicked extends RecyclerView.ViewHolder{

        ImageView imageTv;
        ImageButton closeBtn;


        public HolderImagePicked(@NonNull View itemView){
            super(itemView);
            imageTv = itemView.findViewById(R.id.imageTv);  // Corrected
            closeBtn = itemView.findViewById(R.id.closeBtn);
        }
    }
}
