package com.sharemyfood.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.ButterKnife;
import butterknife.OnClick;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.sharemyfood.base.BaseActivity;
import com.sharemyfood.R;
import com.sharemyfood.commons.Commons;
import com.sharemyfood.fragments.SentOrdersFragment;
import com.sharemyfood.fragments.ReceivedOrdersFragment;

import java.util.ArrayList;
import java.util.List;

public class OrdersHistoryActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_history);

        ButterKnife.bind(this);

        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        adapter.addFragment(new ReceivedOrdersFragment(), "RECEIVED");
        adapter.addFragment(new SentOrdersFragment(), "SENT");

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tablayout);
        tabLayout.setupWithViewPager(viewPager);

    }

    @OnClick(R.id.img_back_order)
    void onBack(){
        finish();
    }

    // Adapter for the viewpager using FragmentPagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
