package com.shreeniketh.propify;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.shreeniketh.propify.activities.FullScreenImageActivity;

import java.util.ArrayList;
import java.util.List;

public class PropertyImageSliderAdapter extends PagerAdapter {

    private Context context;
    private List<String> imageUrls;

    public PropertyImageSliderAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.slider_image_item, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        String imageUrl = imageUrls.get(position);

        Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.image_black)
                .error(R.drawable.house_logo)
                .into(imageView);

        // Open Full-Screen on Click
        view.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenImageActivity.class);
            intent.putStringArrayListExtra("imageUrls", new ArrayList<>(imageUrls));
            intent.putExtra("position", position);
            context.startActivity(intent);
        });

        container.addView(view);
        return view;
    }


    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
