package com.shreeniketh.propify.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.activities.ChangePasswordActivity;
import com.shreeniketh.propify.activities.MainActivity;
import com.shreeniketh.propify.activities.PostAddActivity;
import com.shreeniketh.propify.activities.ProfileEditActivity;
import com.shreeniketh.propify.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private static final String TAG = "PROFILE_TAG";
    private FirebaseAuth firebaseAuth;
    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        mContext = context;
        super.onAttach(context);
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressDialog progressDialog = new ProgressDialog(mContext);
        progressDialog.setTitle("Loading Profile...");
        progressDialog.setCanceledOnTouchOutside(false);

        firebaseAuth = FirebaseAuth.getInstance();

        loadMyInfo();

        binding.logoutCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                startActivity(new Intent(mContext, MainActivity.class));
                requireActivity().finishAffinity();
            }
        });

        binding.editProfileCv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             startActivity(new Intent(mContext, ProfileEditActivity.class));
            }
        });

         binding.changePasswordCv.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(mContext, ChangePasswordActivity.class));
             }
         });

         binding.postAdBtn.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 startActivity(new Intent(mContext, PostAddActivity.class));
             }
         });
    }

    private void loadMyInfo() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        String userId = firebaseAuth.getUid();

        if (userId == null) return;

        ref.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                String email = snapshot.child("email").getValue(String.class);
                String dob = snapshot.child("dob").getValue(String.class);
                String phoneCode = snapshot.child("phoneCode").getValue(String.class);
                String phoneNumber = snapshot.child("phoneNumber").getValue(String.class);
                String profileImageUrl = snapshot.child("profileImageUrl").getValue(String.class);
                String userType = snapshot.child("userType").getValue(String.class);

                // Convert timestamp to formatted date
                Long timestamp = snapshot.child("timestamp").getValue(Long.class);
                String memberSince = timestamp != null ? MyUtils.formatTimestammpDate(timestamp) : "N/A";

                // Ensure defaults for null values
                name = (name != null) ? name : "N/A";
                email = (email != null) ? email : "N/A";
                dob = (dob != null) ? dob : "N/A";
                phoneCode = (phoneCode != null) ? phoneCode : "";
                phoneNumber = (phoneNumber != null) ? phoneNumber : "N/A";
                profileImageUrl = (profileImageUrl != null) ? profileImageUrl : "";

                // Concatenate phone number
                String phone = phoneCode + " " + phoneNumber;

                // Set values to UI
                binding.fullNameTv.setText(name);
                binding.emailTv.setText(email);
                binding.dobtv.setText(dob);
                binding.phoneTv.setText(phone);
                binding.memberSinceTv.setText(memberSince);

                // Handle account verification
                if (userType != null && userType.equals(MyUtils.USER_TYPE_EMAIL)) {
                    boolean isVerified = firebaseAuth.getCurrentUser() != null && firebaseAuth.getCurrentUser().isEmailVerified();
                    binding.verificationTv.setText(isVerified ? "Verified" : "Not Verified");
                    binding.verifyAccountCv.setVisibility(isVerified ? View.GONE : View.VISIBLE);
                } else {
                    binding.verificationTv.setText("Verified");
                    binding.verifyAccountCv.setVisibility(View.GONE);
                }

                // Load profile image
                Glide.with(mContext)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.person_black)
                        .error(R.drawable.person_black)
                        .into(binding.profileIv);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error loading profile: " + error.getMessage());
            }
        });
    }
}
