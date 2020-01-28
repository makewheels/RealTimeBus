package com.eg.realtimebus.activity.location;

import android.Manifest;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.eg.realtimebus.R;
import com.eg.realtimebus.util.Constants;
import com.eg.realtimebus.util.HttpUtil;

import java.text.DecimalFormat;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class LocationActivity extends AppCompatActivity {
    private String TAG = "tag";

    private TextView tv_distance;
    private TextView tv_stationName;
    private TextView tv_busName;
    private Button btn_changeDirection;
    private TextView tv_startStation;
    private TextView tv_endStation;

    private static final int BAIDU_READ_PHONE_STATE = 100;
    private static final int PRIVATE_CODE = 1315;
    private LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();
    private LocationManager locationManager;
    private double mylatitude;
    private double mylongitude;

    private String busName;
    private String direction = "a";

    private final static int WHAT_BUS_DISTANCE = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_BUS_DISTANCE:
                    DistanceResponse distanceResponse = JSON.parseObject((String) msg.obj, DistanceResponse.class);
                    //如果没车
                    if (distanceResponse.isHasBus() == false) {
                        tv_distance.setText(R.string.no_bus_right_now);
                        return;
                    }
                    //如果有车，显示距离
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    tv_distance.setText(decimalFormat.format(distanceResponse.getDistance()) + " m");
            }
        }
    };

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //获取我当前位置
            mylatitude = location.getLatitude();
            mylongitude = location.getLongitude();
            //发请求获取公交位置
            new Thread() {
                @Override
                public void run() {
                    String json = HttpUtil.get(Constants.BASE_URL
                            + "/bus/getDistance?position=" + mylatitude + ","
                            + mylongitude + "&busName=" + busName + "&direction=" + direction);
                    Message message = Message.obtain();
                    message.what = WHAT_BUS_DISTANCE;
                    message.obj = json;
                    handler.sendMessage(message);
                }
            }.start();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        tv_distance = findViewById(R.id.tv_distance);
        tv_stationName = findViewById(R.id.tv_stationName);
        tv_busName = findViewById(R.id.tv_busName);
        btn_changeDirection = findViewById(R.id.btn_changeDirection);
        tv_startStation = findViewById(R.id.tv_startStation);
        tv_endStation = findViewById(R.id.tv_endStation);

        initView();

        checkPermission();

        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setEnableSimulateGps(true);
        mLocationClient.setLocOption(option);
        mLocationClient.start();

    }

    /**
     * 监听器
     */
    private void initView() {
        //公交名
        Intent intent = getIntent();
        busName = intent.getStringExtra("busName");
        tv_busName.setText(busName);
        //换向按钮
        btn_changeDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (busName.startsWith("h") == false) {
                    if (direction == "a") {
                        direction = "b";
                    } else {
                        direction = "a";
                    }
                } else {
                    //不支持换向
                }
            }
        });
    }

    /**
     * 检测GPS、位置权限是否开启
     */
    private void checkPermission() {
        locationManager = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PERMISSION_GRANTED) {// 没有权限，申请权限。
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.READ_PHONE_STATE},
                            BAIDU_READ_PHONE_STATE);
                } else {
                    getLocation();//getLocation为定位方法
                }
            } else {
                getLocation();//getLocation为定位方法
            }
        } else {
            Toast.makeText(this, "系统检测到未开启GPS定位服务,请开启", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, PRIVATE_CODE);
        }
    }

    /**
     * 获取具体位置的经纬度
     */
    private void getLocation() {

    }

    /**
     * Android6.0申请权限的回调方法
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            // requestCode即所声明的权限获取码，在checkSelfPermission时传入
            case BAIDU_READ_PHONE_STATE:
                //如果用户取消，permissions可能为null.
                if (grantResults[0] == PERMISSION_GRANTED && grantResults.length > 0) {  //有权限
                    // 获取到权限，作相应处理
                    getLocation();
                } else {
                    //如果没拿到权限
                    checkPermission();
                }
                break;
            default:
                break;
        }
    }

}
