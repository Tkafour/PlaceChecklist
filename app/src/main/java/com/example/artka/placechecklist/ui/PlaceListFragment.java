package com.example.artka.placechecklist.ui;

import android.Manifest;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.artka.placechecklist.utils.Geofencing;
import com.example.artka.placechecklist.R;
import com.example.artka.placechecklist.provider.PlaceContract;
import com.example.artka.placechecklist.utils.PlaceListAdapter;
import com.example.artka.placechecklist.widget.ChecklistWidget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class PlaceListFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    private Unbinder unbinder;

    private PlaceListAdapter placeListAdapter;
    private int position = RecyclerView.NO_POSITION;

    private GoogleApiClient googleApiClient;
    private Geofencing geofencing;
    private Context context;
    private Cursor cursor;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 111;
    private static final int PLACE_PICKER_REQUEST = 256;

    private static final String[] PLACE_LIST_PROJECTION = {
            PlaceContract.PlaceEntry._ID,
            PlaceContract.PlaceEntry.COLUMN_PLACE_ID,
            PlaceContract.PlaceEntry.COLUMN_PLACE_ADDRESS,
            PlaceContract.PlaceEntry.COLUMN_PLACE_NAME,
            PlaceContract.PlaceEntry.COLUMN_PLACE_LATLONG
    };

    public static final int INDEX_ID = 0;
    public static final int INDEX_PLACE_ID = 1;
    public static final int INDEX_PLACE_ADDRESS = 2;
    public static final int INDEX_PLACE_NAME = 3;
    public static final int INDEX_PLACE_LONGLAT = 4;

    private static final int ID_PLACELIST_LOADER = 57;


    public PlaceListFragment() {

    }

    @Override
    public void onPause() {
        super.onPause();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);
        context = getContext();
        unbinder = ButterKnife.bind(this,view);
        recyclerView = view.findViewById(R.id.places_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        placeListAdapter = new PlaceListAdapter(context);
        recyclerView.setAdapter(placeListAdapter);

        if ( ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            ActivityCompat.requestPermissions( getActivity(), new String[] {  Manifest.permission.ACCESS_FINE_LOCATION  },
                    PERMISSIONS_REQUEST_FINE_LOCATION );
        }

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long id = (long) viewHolder.itemView.getTag();

                String stringId = Long.toString(id);
                Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
                uri = uri.buildUpon().appendPath(stringId).build();
                getContext().getContentResolver().delete(uri, null,null);
                getActivity().getSupportLoaderManager().restartLoader(ID_PLACELIST_LOADER, null, PlaceListFragment.this);
            }
        }).attachToRecyclerView(recyclerView);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onAddPlaceButtonClicked(view);
            }
        });

        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), this)
                .build();

        geofencing = new Geofencing(context, googleApiClient);

        geofencing.registerAllGeofences();
        getActivity().getSupportLoaderManager().initLoader(ID_PLACELIST_LOADER, null, this);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        googleApiClient.stopAutoManage(getActivity());
        googleApiClient.disconnect();
    }

    private void onAddPlaceButtonClicked(View view) {
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(getActivity());
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        refreshPlacesData();
        Log.i(TAG, "API Client Connection Successful");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "API Client Connection Suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "API Client Connection Failed!");
    }

    private void refreshPlacesData() {
        Uri uri = PlaceContract.PlaceEntry.CONTENT_URI;
        cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);

        if (cursor == null || cursor.getCount() == 0) return;
        List<String> guids = new ArrayList<>();
        while (cursor.moveToNext()) {
            guids.add(cursor.getString(cursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID)));
        }
        PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, guids.toArray(new String[guids.size()]));
        placeResult.setResultCallback(new ResultCallback<PlaceBuffer>() {
            @Override
            public void onResult(@NonNull PlaceBuffer places) {
                geofencing.updateGeofencesList(places);
                geofencing.registerAllGeofences();
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(context, data);
            if (place == null) {
                Log.i(TAG, "No place selected");
                return;
            }

            String placeName = place.getName().toString();
            String placeAddress = place.getAddress().toString();
            String placeID = place.getId();
            String placeLatLong = place.getLatLng().toString();


            ContentValues contentValues = new ContentValues();
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ID, placeID);
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_ADDRESS, placeAddress);
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_NAME, placeName);
            contentValues.put(PlaceContract.PlaceEntry.COLUMN_PLACE_LATLONG, placeLatLong);
            context.getContentResolver().insert(PlaceContract.PlaceEntry.CONTENT_URI, contentValues);


            AppWidgetManager appWidgetManager = (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
            ComponentName widget = new ComponentName(getContext(), ChecklistWidget.class);
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(widget), R.id.widget_place_list);
            refreshPlacesData();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case ID_PLACELIST_LOADER:
                Uri placeUri = PlaceContract.PlaceEntry.CONTENT_URI;
                return new CursorLoader(context,
                        placeUri,
                        PLACE_LIST_PROJECTION,
                        null,
                        null,
                        null);
                default:
                    throw new RuntimeException("Loader Not Implemeneted: " + id);

        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        placeListAdapter.swapCursor(data);
        if (position == RecyclerView.NO_POSITION) position = 0;
        recyclerView.smoothScrollToPosition(position);

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        placeListAdapter.swapCursor(null);
    }

}
