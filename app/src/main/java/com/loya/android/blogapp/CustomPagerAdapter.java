package com.loya.android.blogapp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by user on 10/5/2017.
 */

public class CustomPagerAdapter extends FragmentStatePagerAdapter {

    int mNoOfTabs;

    public CustomPagerAdapter(FragmentManager fm, int noOfTabs) {
        super(fm);
        this.mNoOfTabs = noOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                //post fragment
                PostFragment tab1 = new PostFragment();
                return tab1;

            case 1:
                //profile fragment
                ProfileFragment tab2 = new ProfileFragment();
                return tab2;

            case 2:
                //Likes Fragment
                LikesFragment tab3 = new LikesFragment();
                return tab3;

            default:
                return null;

        }

    }

    @Override
    public int getCount() {
        return mNoOfTabs;
    }
}
