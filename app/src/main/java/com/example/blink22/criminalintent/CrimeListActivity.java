package com.example.blink22.criminalintent;

import android.support.v4.app.Fragment;

/**
 * Created by blink22 on 5/29/18.
 */
public class CrimeListActivity extends SingleFragmentActivity{
    @Override
    protected Fragment createFragment() {
        return new CrimeListFragment();
    }
}
