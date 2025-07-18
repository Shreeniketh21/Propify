package com.shreeniketh.propify.fragments;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.activities.PropertyDetailActivity;
import com.shreeniketh.propify.adapters.AdapterProperty;
import com.shreeniketh.propify.databinding.FragmentHomeBinding;
import com.shreeniketh.propify.models.ModelProperty;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private Context mContext;
    private final static String TAG = "HOME_FRAG";
    private ArrayList<ModelProperty> propertyArrayList, filter;
    private AdapterProperty adapterProperty;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.propertiesRv.setLayoutManager(new LinearLayoutManager(mContext));

        // Initialize Lists
        propertyArrayList = new ArrayList<>();
        filter = new ArrayList<>();

        adapterProperty = new AdapterProperty(mContext, propertyArrayList);
        binding.propertiesRv.setAdapter(adapterProperty);

        loadProperties();

        // Search Filter
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                adapterProperty.cancelTimer(); // Cancel any existing search timer
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (adapterProperty != null) {
                    adapterProperty.searchProperty(editable.toString()); // Call the search function
                }
            }
        });



    }

    private void loadProperties() {
        Log.d(TAG, "loadProperties: ");
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Properties");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                propertyArrayList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelProperty modelProperty = ds.getValue(ModelProperty.class);
                    if (modelProperty != null) {
                        propertyArrayList.add(modelProperty);
                        Log.d(TAG, "Property loaded: " + modelProperty.getTitle());
                    } else {
                        Log.w(TAG, "Null property encountered in Firebase!");
                    }
                }
                adapterProperty = new AdapterProperty(mContext, propertyArrayList);
                binding.propertiesRv.setAdapter(adapterProperty);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load properties", error.toException());
            }
        });
    }
}
