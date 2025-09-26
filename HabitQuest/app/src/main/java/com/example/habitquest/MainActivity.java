package com.example.habitquest;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.habitquest.fragments.BossFragment;
import com.example.habitquest.fragments.ProfileFragment;
import com.example.habitquest.fragments.StatsFragment;
import com.example.habitquest.fragments.TasksFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Početni fragment
        loadFragment(new TasksFragment());

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment fragment;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_tasks) {
                fragment = new TasksFragment();
            } else if (itemId == R.id.nav_profile) {
                fragment = new ProfileFragment();
            } else if (itemId == R.id.nav_stats) {
                fragment = new StatsFragment();
            } else if (itemId == R.id.nav_boss) {
                fragment = new BossFragment();
            } else {
                fragment = null;
            }

            return loadFragment(fragment);
        });

    }

    private boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment, fragment)
                    .commit();
            return true;
        }
        return false;
    }
}
