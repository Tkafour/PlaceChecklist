package com.example.artka.placechecklist.utils;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.artka.placechecklist.R;
import com.example.artka.placechecklist.ui.CheckListFragment;
import com.example.artka.placechecklist.ui.PlaceDetailFragment;
import com.example.artka.placechecklist.ui.PlaceListFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlaceListAdapter extends RecyclerView.Adapter<PlaceListAdapter.PlaceViewHolder> {

    private final Context context;
    private Cursor cursor;

    public PlaceListAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public PlaceListAdapter.PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.place_list_item, parent, false);
        return new PlaceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaceListAdapter.PlaceViewHolder holder, int position) {
        cursor.moveToPosition(position);

        long placeTableId = Long.parseLong(cursor.getString(PlaceListFragment.INDEX_ID));
        String placeId = cursor.getString(PlaceListFragment.INDEX_PLACE_ID);
        String placeName = cursor.getString(PlaceListFragment.INDEX_PLACE_NAME);
        String placeAddress = cursor.getString(PlaceListFragment.INDEX_PLACE_ADDRESS);
        String placeLongLat = cursor.getString(PlaceListFragment.INDEX_PLACE_LONGLAT);

        holder.addressTextView.setText(placeAddress);
        holder.nameTextView.setText(placeName);
        holder.placeId = placeId;
        holder.placeLongLat = placeLongLat;
        holder.itemView.setTag(placeTableId);
    }

    public void swapCursor(Cursor cursor) {
        this.cursor = cursor;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (cursor == null)
            return 0;
        return cursor.getCount();
    }

    class PlaceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.name_text_view)
        TextView nameTextView;
        @BindView(R.id.address_text_view)
        TextView addressTextView;

        String placeId;
        String placeLongLat;

        private final static String ADDRESS = "address";
        private final static String PLACE_ID = "place_id";
        private final static String PLACE_LONGLAT = "placeLongLat";

        public PlaceViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View v) {
            AppCompatActivity activity = (AppCompatActivity) v.getContext();
            Fragment detailFragment = new PlaceDetailFragment();
            Fragment checkListFragment = new CheckListFragment();

            Bundle bundle = new Bundle();
            bundle.putString(ADDRESS, addressTextView.getText().toString());
            checkListFragment.setArguments(bundle);

            bundle.putString(PLACE_ID, placeId);
            bundle.putString(PLACE_LONGLAT, placeLongLat);
            detailFragment.setArguments(bundle);
            activity.findViewById(R.id.fragment_container1).setVisibility(View.VISIBLE);
            activity.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container1, checkListFragment).addToBackStack(null).commit();
            activity.getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, detailFragment).addToBackStack(null).commit();
        }
    }
}


