package com.android.babble.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.android.babble.R;
import com.android.babble.adapter.HomePagerAdapter;
import com.android.babble.fragment.inner.HotPostsFragment;
import com.android.babble.fragment.inner.NewPostsFragment;
import com.android.babble.helper.Constants;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeFragment extends Fragment {

    private ViewPager viewPager;
    private TabLayout tabLayout;
    private FirebaseUser user;
    private SharedPreferences sharedpreferences;
    private TextView toolbarTitle;

    public HomeFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_home, container, false);

        if (getActivity()!=null)
        sharedpreferences = getActivity().getSharedPreferences(Constants.SHARED_PREFS_LOCATION_SETTINGS, Context.MODE_PRIVATE);
        toolbarTitle = myFragment.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(sharedpreferences.getString("country_name","My Feed"));

        user = FirebaseAuth.getInstance().getCurrentUser();

        tabLayout = myFragment.findViewById(R.id.tab_layout);
        viewPager = myFragment.findViewById(R.id.view_pager);

        return myFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setUpViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void setUpViewPager(ViewPager viewPager) {
        HomePagerAdapter adapter = new HomePagerAdapter(getChildFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        adapter.addFragment(new NewPostsFragment(),"New");
        adapter.addFragment(new HotPostsFragment(),"Hot");
        viewPager.setAdapter(adapter);
    }
}
