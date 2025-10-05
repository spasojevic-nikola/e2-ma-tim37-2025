package com.example.newhabitquest;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class SocialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        // Setup ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Prijatelji i Savezi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        TabLayout tabLayout = findViewById(R.id.social_tab_layout);
        ViewPager2 viewPager = findViewById(R.id.social_view_pager);

        SocialPagerAdapter adapter = new SocialPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("👥 Prijatelji");
                            break;
                        case 1:
                            tab.setText("🤝 Savezi");
                            break;
                    }
                }).attach();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private static class SocialPagerAdapter extends FragmentStateAdapter {
        public SocialPagerAdapter(FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new FriendsFragment();
                case 1:
                    return new AlliancesFragment();
                default:
                    return new FriendsFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}
