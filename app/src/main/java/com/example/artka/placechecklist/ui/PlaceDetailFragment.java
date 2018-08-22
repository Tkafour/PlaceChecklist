package com.example.artka.placechecklist.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.artka.placechecklist.R;
import com.example.artka.placechecklist.utils.Utility;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResponse;
import com.google.android.gms.location.places.PlacePhotoResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;

public class PlaceDetailFragment extends Fragment {

    @BindView(R.id.place_address)
    TextView addressTextView;
    @BindView(R.id.photo_imageview)
    ImageView photoView;
    @BindView(R.id.map_imageview)
    ImageView mapView;

    private Unbinder unbinder;

    private Uri photoUri;
    private String placeId;

    private GeoDataClient geoDataClient;

    private List<PlacePhotoMetadata> photoMetadataList;
    private int currentPhotoIndex = 0;

    private static final int PICTURE_REQUEST = 1200;
    private final static String ADDRESS = "address";
    private final static String PLACE_ID = "place_id";
    private final static String PLACE_LONGLAT = "placeLongLat";

    private final static String MAP_API_ENDPOINT = "http://maps.google.com/maps/api/staticmap?";
    private final static String MAP_ZOOM = "&zoom=15";
    private final static String MAP_SIZE = "&size=300x300";
    private final static String MAP_SENSOR = "&sensor=false";
    private final static String MAP_MARKER = "&markers=";

    private String mapLongLat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.detail_fragment, container, false);

        unbinder = ButterKnife.bind(this, view);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            addressTextView.setText(bundle.getString(ADDRESS));
            placeId = bundle.getString(PLACE_ID);
            if (mapLongLat != null) mapLongLat = null;
            mapLongLat = bundle.getString(PLACE_LONGLAT);
        }


        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},
                    PICTURE_REQUEST);
        }
        if (Utility.getImageUrl(getContext(), addressTextView.getText().toString()) != null) {
            photoUri = Uri.parse(Utility.getImageUrl(getContext(), addressTextView.getText().toString()));
            grabImage(photoView);
        }
        geoDataClient = Places.getGeoDataClient(getContext(), null);
        setImageFromUrl.execute();
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, PICTURE_REQUEST);
            }
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
    }

    private void grabImage(ImageView imageView) {
        ContentResolver contentResolver = getContext().getContentResolver();
        Bitmap bitmap;
        try {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(contentResolver, photoUri);
            imageView.setImageBitmap(bitmap);
            Utility.saveImageUrl(getContext(), addressTextView.getText().toString(), photoUri.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICTURE_REQUEST && resultCode == RESULT_OK) {
            photoView.setVisibility(View.VISIBLE);
            this.grabImage(photoView);
        }
    }

    private void getPhotoMetaData(String placeId) {
        final Task<PlacePhotoMetadataResponse> photoResponse = geoDataClient.getPlacePhotos(placeId);
        photoResponse.addOnCompleteListener(new OnCompleteListener<PlacePhotoMetadataResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoMetadataResponse> task) {
                currentPhotoIndex = 0;
                photoMetadataList = new ArrayList<>();
                PlacePhotoMetadataResponse photos = task.getResult();
                PlacePhotoMetadataBuffer photoMetadataBuffer = photos.getPhotoMetadata();

                for (PlacePhotoMetadata photoMetadata : photoMetadataBuffer) {
                    photoMetadataList.add(photoMetadataBuffer.get(0).freeze());
                }

                photoMetadataBuffer.release();
                displayPhoto();
            }
        });
    }

    private void getPhoto(PlacePhotoMetadata photoMetadata) {
        Task<PlacePhotoResponse> photoResponseTask = geoDataClient.getPhoto(photoMetadata);
        photoResponseTask.addOnCompleteListener(new OnCompleteListener<PlacePhotoResponse>() {
            @Override
            public void onComplete(@NonNull Task<PlacePhotoResponse> task) {
                PlacePhotoResponse photo = task.getResult();
                Bitmap photoBitmap = photo.getBitmap();
                photoView.invalidate();
                photoView.setImageBitmap(photoBitmap);
            }
        });
    }

    private void displayPhoto() {
        if (photoMetadataList.isEmpty() || currentPhotoIndex > photoMetadataList.size() - 1) {
            if (photoUri.toString().isEmpty()) {
                photoView.setImageDrawable(getResources().getDrawable(R.drawable.camera_icon));
                photoView.setScaleType(ImageView.ScaleType.CENTER);
                photoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                            photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            dispatchTakePictureIntent();

                        } else {
                            Toast.makeText(getContext(), R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            return;
        }
        photoView.setVisibility(View.VISIBLE);
        getPhoto(photoMetadataList.get(currentPhotoIndex));
    }

    @NonNull
    private String getLocationUri() {
        mapLongLat = mapLongLat.substring(10, mapLongLat.length() - 1);
        return MAP_API_ENDPOINT + mapLongLat + MAP_SENSOR + MAP_SIZE + MAP_ZOOM + MAP_MARKER + mapLongLat;
    }

    private final AsyncTask<Void, Void, Bitmap> setImageFromUrl = new AsyncTask<Void, Void, Bitmap>() {

        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getContext(), null, getResources().getString(R.string.loading_text), true);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            Bitmap bmp = null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(getLocationUri());
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                bmp = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return bmp;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            progressDialog.dismiss();
            getPhotoMetaData(placeId);
            mapView.setVisibility(View.VISIBLE);
            mapView.setImageBitmap(bitmap);
        }
    };
}
