package com.example.artka.placechecklist.widget;
import android.content.Intent;
import android.widget.RemoteViewsService;

public class ChecklistWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ChecklistRemoteViewsFactory(getApplicationContext(), intent);
    }
}
