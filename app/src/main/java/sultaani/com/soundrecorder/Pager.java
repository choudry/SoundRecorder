package sultaani.com.soundrecorder;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by lenovo on 9/14/2017.
 */

public class Pager extends FragmentPagerAdapter {

    //integer to count number of tabs
    int tabCount;

    //Constructor to the class
    public Pager(FragmentManager fm, int tabCount) {
        super(fm);
        //Initializing tab count
        this.tabCount = tabCount;

    }

    //Overriding method getItem
    @Override
    public Fragment getItem(int position) {
        //Returning the current tabs

        Fragment fragment = null;

        if (position == 0) {
            fragment = new SoundRecordFragment();
        } else if (position == 1) {
            fragment = new PlayRecordFragment();
        } else if (position == 2){
            fragment = new FilesFragment();
        }
        return fragment;
    }

    //Overriden method getCount to get the number of tabs
    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Record";
            case 1:
                return "Saved Recordings";

            case 2:
                return "Files";

        }
        return null;
    }
}