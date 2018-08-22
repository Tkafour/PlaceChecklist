package com.example.artka.placechecklist.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.example.artka.placechecklist.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{

    private final static String ADDRESS = "address";
    private final static String PLACE_ID = "place_id";
    private final static String PLACE_LONGLAT = "placeLongLat";

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = new PlaceListFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).addToBackStack(null).commit();
        }

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            setFragmentsFromWidget(extras);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() <= 1) {
            supportFinishAfterTransition();
        } else {
            if (getSupportFragmentManager().findFragmentById(R.id.fragment_container1) != null) {
                this.findViewById(R.id.fragment_container1).setVisibility(View.GONE);
                getSupportFragmentManager().popBackStack();
                if (getSupportFragmentManager().getBackStackEntryCount() == 2) {
                    getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void setFragmentsFromWidget(Bundle extras) {
            String placeAddress = extras.getString(ADDRESS);
            String placeId = extras.getString(PLACE_ID);
            String placeLongLat = extras.getString(PLACE_LONGLAT);
            Fragment detailFragment = new PlaceDetailFragment();
            Fragment checkListFragment = new CheckListFragment();

            Bundle bundle = new Bundle();
            bundle.putString(ADDRESS, placeAddress);
            checkListFragment.setArguments(bundle);

            bundle.putString(PLACE_ID, placeId);
            bundle.putString(PLACE_LONGLAT, placeLongLat);
            detailFragment.setArguments(bundle);
            this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container1, checkListFragment).addToBackStack(null).show(checkListFragment).commit();
            this.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, detailFragment).addToBackStack(null).commit();
            this.findViewById(R.id.fragment_container1).setVisibility(View.VISIBLE);
    }
}
