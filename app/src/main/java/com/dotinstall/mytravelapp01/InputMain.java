package com.dotinstall.mytravelapp01;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class InputMain extends AppCompatActivity implements LocationListener {
    public final static String EXTRA_MYTIME = "com.dotinstall.mytravelapp01.MYTIME";
    //時間のKey
    public final static String EXTRA_MYMONEY = "com.dotinstall.mytravelapp01.MYMONEY";
    //お金のKey
    public final static String EXTRA_MYLONGITUDE = "com.dotinstall.mytravelapp01.MYLONGITUDE";
    //経度のKey
    public final static String EXTRA_MYLAITUDE = "com.dotinstall.mytravelapp01.MYLAITUDE";
    //経度のKey
    private static final String TAG = InputMain.class.getSimpleName();
    private static final int LOCATION_UPDATE_MIN_TIME = 0;
    //おおよその更新時間
    private static final int LOCATION_UPDATE_MIN_DISTANCE = 1;
    //おおよその最小距離
    private LocationManager mLocationManager;
    public double longitude, latitude;

    //LocationManager型のインスタンスの定義
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_main);

        mLocationManager = (LocationManager) this.getSystemService(Service.LOCATION_SERVICE);
        getLatLongitude();
    }

    public void inputButton(View view) {
        Intent intent = new Intent(getApplication(), OutputMain.class);
        int myMoney, myTime;
        EditText moneyEditText = (EditText) findViewById(R.id.moneyEditText);
        //idからmoneyEditTextの獲得
        String stringMoney = moneyEditText.getText().toString();
        //Editable型からString型に変換し、入力情報を保存
        if (stringMoney.equals("")) {
            myMoney = 0;//nullをparseIntするのをふせぐため
        } else {
            myMoney = Integer.parseInt(stringMoney);
        }
        //String型からint型に変換する。
        EditText timeEditText = (EditText) findViewById(R.id.timeEditText);
        //idからtimeEditTextの獲得
        String stringTime = timeEditText.getText().toString();
        //Editable型からString型に変換し、入力情報を保存
        if (stringTime.equals("")) {
            myTime = 0;
            //nullをparseIntするのをふせぐため"0"を入れておく
        } else {
            myTime = Integer.parseInt(stringTime);
        }
        if (myMoney == 0) {
            if (myTime == 0) {
                moneyEditText.setError("お金を入力してください");
                timeEditText.setError("時間を入力してください");
                //どちらも入力されてない場合の処理
            } else {
                myMoney = 100000;
                //お金が入力されなかった場合は10万円を初期値にする
                if (latitude != 0) {
                    intent.putExtra(EXTRA_MYTIME, myTime);
                    intent.putExtra(EXTRA_MYMONEY, myMoney);
                    intent.putExtra(EXTRA_MYLONGITUDE, longitude);
                    intent.putExtra(EXTRA_MYLAITUDE, latitude);
                    startActivity(intent);
                    //どちらかの緯度か経度を取ることができれば、その情報をすべてOutputに
                } else {
                    //緯度が取得されていれば経度も取得されている
                    //緯度は日本では0をとらないのでエラー処理
                    String message = "GPS機能が無効になっています,ONにしてください";
                    showMessage(message);
                }
            }
        } else if (myTime == 0) {
            myTime = 1440;//お金が入力されていて時間が入力されていなかったら24時間(1440分)を初期値とする
            if (latitude != 0) {
                intent.putExtra(EXTRA_MYTIME, myTime);
                intent.putExtra(EXTRA_MYMONEY, myMoney);
                intent.putExtra(EXTRA_MYLONGITUDE, longitude);
                intent.putExtra(EXTRA_MYLAITUDE, latitude);
                startActivity(intent);
            } else {
                String message = "GPS機能が無効になっています,ONにしてください";
                showMessage(message);
            }
        } else {
            if (latitude != 0) {
                intent.putExtra(EXTRA_MYTIME, myTime);
                intent.putExtra(EXTRA_MYMONEY, myMoney);
                intent.putExtra(EXTRA_MYLONGITUDE, longitude);
                intent.putExtra(EXTRA_MYLAITUDE, latitude);
                startActivity(intent);
            } else {
                String message = "GPS機能が無効になっています,ONにしてください";
                showMessage(message);
            }
        }
    }

    public void getLatLongitude() {
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //GPSが利用可能の場合
        if (isNetworkEnabled) {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_MIN_TIME,
                    LOCATION_UPDATE_MIN_DISTANCE,
                    this
            );
            Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();//緯度を取得する
                longitude = location.getLongitude();//経度を取得する
            }
        } else {
            String message = "GPS機能が無効になっています,ONにしてください";
            showMessage(message);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        getLatLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.e(TAG, "onStatusChanged");
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                //訳:プロバイダー(GPSやネットワーク)が圏外のとき、近い未来変更が期待できない場合
                //今回は保留
                String outOfServiceMessage = provider + "が圏外になっていて取得できません。";
                showMessage(outOfServiceMessage);
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                //訳:プロバイダー(GPSやネットワーク)が一時的に利用不可能の場合
                //今回は保留
                String temporarilyUnavaliableMessage = "一時的に" + provider + "が利用できません。";
                showMessage(temporarilyUnavaliableMessage);
                break;
            case LocationProvider.AVAILABLE:
                //訳:利用可能状態の場合
                if (provider.equals(LocationManager.GPS_PROVIDER)) {
                    getLatLongitude();
                }
                break;
        }
    }

    private void showMessage(String message) {
        TextView textView = (TextView) findViewById(R.id.message);
        textView.setText(message);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.e(TAG, "onProviderEnabled");
        getLatLongitude();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.e(TAG, "onProviderDisabled");
    }
}
