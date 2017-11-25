package com.example.oyun.saveme;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by oyun on 2017-11-25.
 */

public class FragmentMap extends Fragment implements OnMapReadyCallback {
    private GoogleMap Map;
    private MapView mapView;
    private LatLng current_point;
    private boolean isInit = false;
    private boolean mapsSupported = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.googlemap, container, true);
        mapView = (MapView) rootView.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Map = googleMap;
        LatLng seoul = new LatLng(37.52487, 126.92723);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(seoul).title("서울이다 씨발");
        Map.addMarker(markerOptions);
        Map.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        Map.moveCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

//        MapsInitializer.initialize(getActivity().getApplicationContext());
        if (mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

}
