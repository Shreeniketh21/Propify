package com.shreeniketh.propify.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.shreeniketh.propify.MyUtils;
import com.shreeniketh.propify.R;
import com.shreeniketh.propify.databinding.ActivityMainBinding;
import com.shreeniketh.propify.fragments.HomeFragment;
import com.shreeniketh.propify.fragments.ProfileFragment;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth=FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser()==null){
            startLoginOptionsActivity();
        }

        

        showHomeFragment();

        binding.bottonNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId=item.getItemId();
                if(itemId== R.id.item_home){
                    showHomeFragment();
                    return true;
                }  else if (itemId==R.id.item_profile) {
                    if(firebaseAuth.getCurrentUser()==null){
                        MyUtils.toast(MainActivity.this,"Login Required...!");
                        return false;
                    }else{
                        showProfileFragment();
                        return true;
                    }
                } else{
                    return false;
                }
            }
        });
    }

    private void showHomeFragment(){
        binding.toolbarTitleTv.setText("Home");
        HomeFragment homeFragment=new HomeFragment();
        androidx.fragment.app.FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(),homeFragment,"HomeFragment");
        fragmentTransaction.commit();
    }

    private void showProfileFragment(){
        binding.toolbarTitleTv.setText("Profile");
        ProfileFragment profileFragment=new ProfileFragment();
        FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(binding.fragmentsFl.getId(), profileFragment,"FavouriteListFragment");
        fragmentTransaction.commit();
    }
    private void startLoginOptionsActivity() {
        startActivity(new Intent(this, LoginOptionsActivity.class));
    }
}