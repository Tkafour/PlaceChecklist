package com.example.artka.placechecklist.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.artka.placechecklist.R;
import com.example.artka.placechecklist.provider.PlaceContract;
import com.example.artka.placechecklist.ui.MainActivity;
import com.example.artka.placechecklist.utils.Utility;

class ChecklistRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final Context context;
    private final int widgetId;
    private Cursor cursor;
    private Boolean geofenceStatus;

    private final static String ADDRESS = "address";
    private final static String PLACE_ID = "place_id";
    private final static String PLACE_LONGLAT = "placeLongLat";

    public ChecklistRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        final long token = Binder.clearCallingIdentity();
        try {

            if (cursor != null) {
                cursor.close();
            }

            cursor = context.getContentResolver().query(
                    PlaceContract.PlaceEntry.CONTENT_URI,
                    null,
                    null,
                    null,
                    null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    @Override
    public void onDestroy() {
        if (cursor != null) {
            cursor.close();
        }
    }

    @Override
    public int getCount() {
        return cursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);

        if (cursor.moveToPosition(position)) {

            String placeId = cursor.getString(cursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ID));
            String placeLongLat = cursor.getString(cursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_LATLONG));
            String placeAddress = cursor.getString(cursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_ADDRESS));

            geofenceStatus = Utility.getGeofenceStatus(context, placeAddress);

            if (geofenceStatus) {
                remoteViews.setImageViewResource(R.id.widget_geofence_status, R.drawable.ic_pin_place_red);
            } else if (!geofenceStatus) {
                remoteViews.setImageViewResource(R.id.widget_geofence_status, R.drawable.ic_pin_drop_widget);
            }

            remoteViews.setTextViewText(R.id.widget_item_address, placeAddress);
            remoteViews.setTextViewText(R.id.widget_item_name, cursor.getString(cursor.getColumnIndex(PlaceContract.PlaceEntry.COLUMN_PLACE_NAME)));

            Intent intent = new Intent(context, MainActivity.class);
            intent.putExtra(ADDRESS, placeAddress);
            intent.putExtra(PLACE_ID, placeId);
            intent.putExtra(PLACE_LONGLAT, placeLongLat);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            remoteViews.setOnClickFillInIntent(R.id.place_row, intent);

        }
        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
