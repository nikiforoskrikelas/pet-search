package nk00322.surrey.petsearch.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petsearch.R;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import nk00322.surrey.petsearch.fragments.MeFragments.MyAccountFragment;
import nk00322.surrey.petsearch.fragments.MeFragments.MyEventsFragment;
import nk00322.surrey.petsearch.fragments.MeFragments.MyPartiesFragment;


public class MeFragment extends Fragment {

    private ViewPager viewPager;
    private TabPageAdapter tabPageAdapter;
    private TabItem tabParties;
    private TabItem tabEvents;
    private TabItem tabAccount;
    private View view;
    private TabLayout tabLayout;

    public MeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_me, container, false);

        initViews();
        setListeners();
        // Inflate the layout for this fragment
        return view;
    }

    private void initViews() {
        tabLayout = view.findViewById(R.id.tab_layout);
        tabParties = view.findViewById(R.id.tab_parties);
        tabEvents = view.findViewById(R.id.tab_events);
        tabAccount = view.findViewById(R.id.tab_account);
        viewPager = view.findViewById(R.id.viewpager);

        tabPageAdapter = new TabPageAdapter(getChildFragmentManager());
        viewPager.setAdapter(tabPageAdapter);
    }


    private void setListeners() {
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }

    public static class TabPageAdapter extends FragmentPagerAdapter {
        public TabPageAdapter(FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    return new MyPartiesFragment();
                case 1:
                    return new MyEventsFragment();
                case 2:
                    return new MyAccountFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

}
