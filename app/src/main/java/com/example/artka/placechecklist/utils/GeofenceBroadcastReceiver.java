package com.example.artka.placechecklist.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.artka.placechecklist.R;
import com.example.artka.placechecklist.ui.MainActivity;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    private Geocoder geocoder;
    private List<Address> addressList;
    private String address;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Location location = geofencingEvent.getTriggeringLocation();
        geocoder = new Geocoder(context, Locale.getDefault());
        try {
            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        address = addressList.get(0).getAddressLine(0);

        Log.d("my broadcast", "works");
        if (geofencingEvent.hasError()) {
            Log.e(TAG, String.format("Error code: %d", geofencingEvent.getErrorCode()));
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        sendNotification(context, geofenceTransition);
    }

    private void sendNotification(Context context, int transitionType) {
        Intent notificationIntent = new Intent(context, MainActivity.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notificationIntent);

        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        if (transitionType == Geofence.GEOFENCE_TRANSITION_DWELL) {
            builder.setContentTitle(context.getString(R.string.enter_string) + " " + address)
                    .setSmallIcon(R.drawable.ic_pin_place_red)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_pin_place_red));
            Utility.saveGeofenceStatus(context, address, true);
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Utility.saveGeofenceStatus(context, address, false);
        }
        builder.setContentText(context.getString(R.string.touch_to_relaunch));
        builder.setContentIntent(notificationPendingIntent);

        builder.setAutoCancel(true);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, builder.build());
    }


}
