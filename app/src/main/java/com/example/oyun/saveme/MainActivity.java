package com.example.oyun.saveme;

import android.content.Intent;
import android.hardware.Camera;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    int flag;
    private Camera camera;  //후레쉬 제어를 위해 Camera 객체 생성
    boolean isFlashOn = true;  //후레쉬 점멸을 위한 boolean 값
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SmsManager smsManager = SmsManager.getDefault();
    private GPS gps;
    SoundPool sp;   //경보음 재생을 위해 SoundPool API사용
    int soundID ;

//    ToggleButton emergencybutton;
    Button settingbuttion;
    Button newsbutton;

    String phonenumber1;
    String phonenumber2;
    String phonenumber3;
    String emergencysms;

    String lat;
    String lon;

    LatLng latLng = new LatLng(37.52, 126.93);
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);   //경보음 재생을 위해 SoundPool API사용
        soundID = sp.load(this, R.raw.emergencysound, 1);

//        emergencybutton = (ToggleButton) findViewById(R.id.emergencyButton);  //토클버튼으로 비상버튼 설정
        settingbuttion = (Button) findViewById(R.id.settingButton);   //설정버튼
        newsbutton = (Button) findViewById(R.id.newsButton);   //공지사항 버튼

        settingbuttion.setOnClickListener(new View.OnClickListener() {   //설정 버튼을 눌렀을 경우
            @Override
            public void onClick(View view) {
                Intent settingintent = new Intent(MainActivity.this, Setting.class);  //설정화면으로 넘김
                MainActivity.this.startActivity(settingintent);
            }
        });

        newsbutton.setOnClickListener(new View.OnClickListener() {  //공지사항 버튼을 눌렀을 경우
            @Override
            public void onClick(View view) {
                Intent newsintent = new Intent(MainActivity.this, News.class);  //공지사항화면으로 넘김
                MainActivity.this.startActivity(newsintent);
            }
        });

//        emergencybutton.setOnClickListener(new View.OnClickListener() {    //경보 버튼을 눌렀을 때
//            @Override
//            public void onClick(View view) {
//                if (emergencybutton.isChecked()) {
//                    flag = 0;
//                    blink();  //점멸 Turn On
//                    SoundOn();  //소리 On
//                    FindLocation();  //위도 경도 찾기
//                    LoadData();   //설정되어 있는 번호 불러오기
//                    SendMessage();  //설정되어 있는 번호로 문자 보내기
//                    SendLocation();   //설정되어 있는 번호로 위치정보 보내기
//                } else {
//                    flag = 1;
//                    SoundOff();  //소리 OFF
//                    blink(); //점멸 Turn Off
//                    LocationOff(); //GPS 종료
//                }
//            }
//        });
    }


    private void blink() {  //점멸하는 메소드
        Thread t = new Thread() {  //스레드 생성
            public void run() {
                try {
                    while (flag != 1) { //점멸을 위해 while문 안에서 반복을 하여 후레쉬가 켜졌다 꺼졌다를 반복한다.
                        if (isFlashOn) {
                            turnOnFlash();
                        } else {
                            sleep(500);
                            turnOffFlash();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    private void turnOnFlash() {  //Flash On

        camera = Camera.open();
        Camera.Parameters param = camera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(param);
        camera.startPreview();
        isFlashOn = false;  //isFlashOn변수를 false로 설정하여 flash가 다시 꺼지게 함.
    }

    private void turnOffFlash() { //Flash off

        Camera.Parameters param = camera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(param);
        camera.stopPreview();
        camera.release();
        isFlashOn = true;  //isFlashOn변수를 true로 설정하여 flash가 다시 켜지게 함.
    }

    public void SoundOn(){  //경보음을 울리는 메소드
        sp.play(soundID, 1f, 1f, 0, -1, 1f); //경보음 시작
    }

    public void SoundOff(){  //경보음을 끄는 메소드
        sp.stop(soundID); //경보음 중지
    }

    public void FindLocation(){  //위치정보를 얻어오는 메소드
        gps = new GPS(MainActivity.this);   //위치 정보 얻어오기
        if(gps.isGetLocation()){
            double latitude = gps.getLatitude();   //위도 받아옴
            double longitude = gps.getLongitude();   //경도 받아옴
            lat = String.valueOf(latitude);
            lon = String.valueOf(longitude);
            Toast.makeText(getApplicationContext(), "위도:"+lat+"경도:"+lon, Toast.LENGTH_LONG).show();
        }
        else {
            gps.showSettingsAlert();  //위치정보 허용이 되있지 않은 경우 설정창으로 이동하는 Alert창을 띄움
        }
    }

    public void LocationOff(){  //GPS를 종료하는 메소드
        gps.stopUsingGPS();
    }

    public void LoadData(){  //등록정보읽어오는 메소드
        try{
            FileInputStream fis = openFileInput("savemedatainformation.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            phonenumber1 = reader.readLine();  //첫번째 번호
            phonenumber2 = reader.readLine();  //두번째 번호
            phonenumber3 = reader.readLine();  //세번째 번호
            emergencysms = reader.readLine();  //메세지
            Log.d("Test", phonenumber1);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void SendMessage(){   //위험 메세지를 보내주는 메소드
        smsManager.sendTextMessage(phonenumber1, null,  emergencysms, null, null);   //첫번째 번호로 문자 메세지 보내기
        smsManager.sendTextMessage(phonenumber2, null,  emergencysms, null, null);   //두번째 번호로 문자 메세지 보내기
        smsManager.sendTextMessage(phonenumber3, null,  emergencysms, null, null);   //세번째 번호로 문자 메세지 보내기

    }

    public void SendLocation(){  //위치를 전송해주는 메소드
        smsManager.sendTextMessage(phonenumber1, null,  "https://maps.google.com/?q="+lat+","+lon, null, null);  //위치 전송
        smsManager.sendTextMessage(phonenumber2, null,  "https://maps.google.com/?q="+lat+","+lon, null, null);
        smsManager.sendTextMessage(phonenumber3, null,  "https://maps.google.com/?q="+lat+","+lon, null, null);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng seoul = new LatLng(37.56, 126.97);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(seoul);
        markerOptions.title("씨발");
        map.addMarker(markerOptions);

        map.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
        map.animateCamera(zoom);
    }
}



