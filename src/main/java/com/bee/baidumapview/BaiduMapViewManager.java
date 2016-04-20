package com.bee.baidumapview;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Button;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.uimanager.LayoutShadowNode;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;

import org.json.JSONObject;

public class BaiduMapViewManager extends SimpleViewManager<MapView> {
    public static final String RCT_CLASS = "RCTBaiduMap";
    public static final String TAG = "RCTBaiduMap";
    private LocationClient locationClient;

    private Activity mActivity;

    private LatLng currentLocation;

    @Override
    public LayoutShadowNode createShadowNodeInstance() {
        return new BaiduMapShadowNode();
    }

    @Override
    public Class getShadowNodeClass() {
        return BaiduMapShadowNode.class;
    }

    public BaiduMapViewManager(Activity activity) {
        mActivity = activity;
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);

        SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        Log.i(TAG, sp.getString("LNG", "") + "-lng");
        Log.i(TAG, sp.getString("LAT", "") + "-lat");
        if(!sp.getString("LNG", "").equals("")) {
            currentLocation = new LatLng(Double.parseDouble(sp.getString("LAT", "")), Double.parseDouble(sp.getString("LNG", "")));
        } else {
            currentLocation = null;
        }

    }
    public BaiduMapViewManager(Activity activity, LocationClient locationClient) {
        mActivity = activity;
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);

        SharedPreferences sp = activity.getApplicationContext().getSharedPreferences("SP", Context.MODE_PRIVATE);
        Log.i(TAG, sp.getString("LNG", "") + "-lng");
        Log.i(TAG, sp.getString("LAT", "") + "-lat");
        if(!sp.getString("LNG", "").equals("")) {
            currentLocation = new LatLng(Double.parseDouble(sp.getString("LAT", "")), Double.parseDouble(sp.getString("LNG", "")));
        } else {
            currentLocation = null;
        }
        if (locationClient != null) {
            Log.i(TAG, "get location client");
        }else {
            Log.i(TAG, "no location client");
        }
        this.locationClient = locationClient;
//        this.locationClient.registerLocationListener(new MyLocationListener());
//        this.locationClient.requestLocation();

    }



    @ReactProp(name="showZoomControls", defaultBoolean = true)
    public void setShowZoomControls(MapView mapView, boolean showZoomControls) {
        Log.d(TAG, "show zoom controls:" + showZoomControls);
        mapView.showZoomControls(showZoomControls);
    }

    @ReactProp(name="startRequestLocation", defaultBoolean = true)
    public void setStartRequestLocation(MapView mapView, boolean startRequestLocation) {
        Log.d(TAG, "start request location:" + startRequestLocation);
        this.locationClient.start();
        this.locationClient.requestLocation();
    }

    @ReactProp(name="locationEnabled", defaultBoolean = true)
    public void setLocationEnabled(MapView mapView, boolean showLocation) {
        Log.d(TAG, "show location:" + showLocation);
        mapView.getMap().setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.NORMAL, false, null));
        mapView.getMap().setMyLocationEnabled(showLocation);
    }
    /**
     * 地图模式
     *
     * @param mapView
     * @param type
     *  1. 普通
     *  2.卫星
     */
    @ReactProp(name="mode", defaultInt = 1)
    public void setMode(MapView mapView, int type) {
        Log.i(TAG, "mode:" + type);
        mapView.getMap().setMapType(type);
    }

    /**
     * 实时交通图
     *
     * @param mapView
     * @param isEnabled
     */
    @ReactProp(name="trafficEnabled", defaultBoolean = false)
    public void setTrafficEnabled(MapView mapView, boolean isEnabled) {
        Log.d(TAG, "trafficEnabled:" + isEnabled);
        mapView.getMap().setTrafficEnabled(isEnabled);
    }

    /**
     * 实时道路热力图
     *
     * @param mapView
     * @param isEnabled
     */
    @ReactProp(name="heatMapEnabled", defaultBoolean = false)
    public void setHeatMapEnabled(MapView mapView, boolean isEnabled) {
        Log.d(TAG, "heatMapEnabled" + isEnabled);
        mapView.getMap().setBaiduHeatMapEnabled(isEnabled);
    }


    /**
     * 显示地理标记
     *
     * @param mapView
     * @param array
     */
    @ReactProp(name="marker")
    public void setMarker(MapView mapView, ReadableArray array) {
        Log.d(TAG, "marker:" + array);

        mapView.getMap().clear();
        mapView.getMap().hideInfoWindow();

        LatLngBounds.Builder boundsBuilder =  new LatLngBounds.Builder();

        if (array != null && array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                String markerInfoStr = array.getString(i);
                try {
                    JSONObject  markerJson = new JSONObject(markerInfoStr);
                    JSONObject coordinate = markerJson.getJSONObject("coordinate");
                    LatLng point = new LatLng(coordinate.getDouble("lat"), coordinate.getDouble("lng"));
                    BitmapDescriptor bitmap = BitmapDescriptorFactory.fromAssetWithDpi("Marker_Pink.png");
                    //构建MarkerOption，用于在地图上添加Marker
                    OverlayOptions option = new MarkerOptions()
                            .position(point)
                            .icon(bitmap)
                            .title(markerJson.getString("info"))
                            .draggable(markerJson.getBoolean("draggable"));
                    //在地图上添加Marker，并显示
                    mapView.getMap().addOverlay(option);
                    boundsBuilder.include(point);


                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (currentLocation != null) {
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromAssetWithDpi("Marker_Pink.png");
            //构建MarkerOption，用于在地图上添加Marker
            LatLng point = new LatLng(currentLocation.latitude, currentLocation.longitude);
            OverlayOptions option = new MarkerOptions()
                    .position(point)
                    .icon(bitmap)
                    .draggable(true);
            //在地图上添加Marker，并显示
            mapView.getMap().addOverlay(option);
        }

        boundsBuilder.include(currentLocation);
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newLatLngBounds(boundsBuilder.build());
//        MapStatusUpdate mMapStatusUpdateZoom = MapStatusUpdateFactory.zoomBy(16);
//        mapView.getMap().setMapStatus(mMapStatusUpdateZoom);
        mapView.getMap().setMapStatus(mMapStatusUpdate);
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @Override
    public String getName() {
        return RCT_CLASS;
    }


    @Override
    protected MapView createViewInstance(final ThemedReactContext reactContext) {
//
//        MapStatus mapStatus = new MapStatus.Builder().zoom(18).build();
//
//        BaiduMapOptions options = new BaiduMapOptions();
//        options.mapStatus(mapStatus);

//        final MapView mapView = new MapView(mActivity, options);
        final MapView mapView = new MapView(mActivity);
        mapView.getMap().hideInfoWindow();
//
//        if(currentLocation != null) {
//            mapView.getMap().setMyLocationEnabled(true);
//            // mapView.getMap().setMyLocationConfigeration(new MyLocationConfiguration(MyLocationConfiguration.LocationMode.COMPASS, false, BitmapDescriptorFactory.fromResource(R.drawable.icon_gcoding)));
//            MyLocationData.Builder myLocationBuilder = new MyLocationData.Builder();
//            myLocationBuilder.longitude(currentLocation.longitude).latitude(currentLocation.latitude);
//            mapView.getMap().setMyLocationData(myLocationBuilder.build());
//
//            WritableMap event = Arguments.createMap();
//            event.putDouble("lat", currentLocation.latitude);
//            event.putDouble("lng", currentLocation.longitude);
//            sendEvent(reactContext, "onGetMyLocation", event);
//        }



        mapView.getMap().setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                WritableMap event = Arguments.createMap();
                event.putDouble("lat", marker.getPosition().latitude);
                event.putDouble("lng", marker.getPosition().longitude);
                sendEvent(reactContext, "markerDragEnd", event);
            }

            @Override
            public void onMarkerDragStart(Marker marker) {

            }
        });
        mapView.getMap().setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Log.i(TAG, "marker click");
                Button button = new Button(mapView.getContext());
                button.setText(marker.getTitle());
                InfoWindow infoWindow = new InfoWindow(button, marker.getPosition(), -100);
                mapView.getMap().hideInfoWindow();
                mapView.getMap().showInfoWindow(infoWindow);
                return true;
            }
        });

        mapView.getMap().setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mapView.getMap().hideInfoWindow();
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });
        mapView.getMap().clear();

        this.locationClient.registerLocationListener(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.i(TAG, "got current location in baidu map kit");
                WritableMap event = Arguments.createMap();
                event.putDouble("lat", bdLocation.getLatitude());
                event.putDouble("lng", bdLocation.getLongitude());
                sendEvent(reactContext, "onGetMyLocation", event);
                MyLocationData.Builder myLocationBuilder = new MyLocationData.Builder();
                myLocationBuilder.longitude(currentLocation.longitude).latitude(currentLocation.latitude);
                mapView.getMap().setMyLocationData(myLocationBuilder.build());
                locationClient.stop();
            }
        });
        return mapView;
    }

    public LocationClient getLocationClient() {
        return locationClient;
    }

    public void setLocationClient(LocationClient locationClient) {
        this.locationClient = locationClient;
    }


}
