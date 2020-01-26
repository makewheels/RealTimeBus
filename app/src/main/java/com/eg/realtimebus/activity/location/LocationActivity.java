package com.eg.realtimebus.activity.location;

import android.Manifest;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.eg.realtimebus.R;
import com.eg.realtimebus.util.HttpUtil;

import java.text.DecimalFormat;
import java.util.List;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

public class LocationActivity extends AppCompatActivity {

    private static final int BAIDU_READ_PHONE_STATE = 100;
    private static final int PRIVATE_CODE = 1315;

    private String TAG = "tag";
    private TextView tv_distance;

    private LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    private final static int WHAT_PARSE_BUS_JSON = 1;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_PARSE_BUS_JSON:
                    List<BusPositionResult> busPositionResultList =
                            JSON.parseArray((String) msg.obj, BusPositionResult.class);
                    //如果现在没有公交
                    if (busPositionResultList.size() == 0) {
                        tv_distance.setText(R.string.no_bus_right_now);
                        return;
                    }
                    //解析出距离最近的公交
                    for (BusPositionResult busPositionResult : busPositionResultList) {
                        //我先把所有的距离都计算出来，放到结果对象中
                        double buslat = Double.parseDouble(busPositionResult.getLat());
                        double buslng = Double.parseDouble(busPositionResult.getLng());
                        double distance = (double) AMapUtils.calculateLineDistance(
                                new LatLng(mylatitude, mylongitude), new LatLng(buslng, buslat));
                        busPositionResult.setDistance(distance);
                    }
                    //这是最小的索引
                    int minIndex = 0;
                    //然后我再遍历一次，找最小值
                    for (int i = 0; i < busPositionResultList.size(); i++) {
                        if (busPositionResultList.get(i).getDistance()
                                < busPositionResultList.get(minIndex).getDistance()) {
                            minIndex = i;
                        }
                    }
                    //这回可以拿到最小的距离了
                    double minDistance = busPositionResultList.get(minIndex).getDistance();
                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                    //显示距离
                    tv_distance.setText(decimalFormat.format(minDistance) + " m");
            }
        }
    };

    private double mylatitude;
    private double mylongitude;

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //获取我当前位置
            mylatitude = location.getLatitude();
            mylongitude = location.getLongitude();
            //发请求获取公交信息
            new Thread() {
                @Override
                public void run() {
                    String json = HttpUtil.get("http://116.62.123.9/gongjiaoluxian/xianlu50b.php");
                    Message message = Message.obtain();
                    message.what = WHAT_PARSE_BUS_JSON;
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

        Intent intent = getIntent();
        String busName = intent.getStringExtra("busName");

        check();

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
        option.setEnableSimulateGps(false);
        mLocationClient.setLocOption(option);
        mLocationClient.start();
    }

    LocationManager lm;

    /**
     * 检测GPS、位置权限是否开启
     */
    private void check() {
        lm = (LocationManager) this.getSystemService(this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
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
                    check();
                }
                break;
            default:
                break;
        }
    }

}
