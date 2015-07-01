package gg.destiny.app;

/**
 * Created by Hayk on 4/24/2015.
 */

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

/**
 * A simple pager adapter that represents 5 ScreenSlidePageFragment objects, in
 * sequence.
 */
public class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    public ViewPager parentPager;
    public ScreenSlidePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        // Our object is just an integer :-P
        args.putInt(ScreenSlidePageFragment.ARG_OBJECT, position);
        fragment.setArguments(args);
        fragment.pager = parentPager;

        return fragment;
    }

    @Override
    public int getCount() {
        return MainActivity.NUM_PAGES;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position == 0) {
            return "Destiny Chat";
        }else if (position == 1) {
            return "Web Browser";
        }else if (position == 2) {
            return "Other Chat";
        }else {
            return "OBJECT " + Integer.toString(position);
        }
    }
}
