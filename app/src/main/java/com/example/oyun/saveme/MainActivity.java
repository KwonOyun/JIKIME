package com.example.oyun.saveme;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
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

    private static MediaPlayer mp;

    int flag;
    private Camera camera;  //후레쉬 제어를 위해 Camera 객체 생성
    boolean isFlashOn = true;  //후레쉬 점멸을 위한 boolean 값
    private LocationManager locationManager;
    private LocationListener locationListener;
    private SmsManager smsManager = SmsManager.getDefault();
    private GPS gps;
    SoundPool sp;   //경보음 재생을 위해 SoundPool API사용
    int soundID ;

    Button settingbuttion;
    Button newsbutton;

    String phonenumber1;
    String phonenumber2;
    String phonenumber3;
    String emergencysms;

    String lat;
    String lon;

    GoogleMap map;

    int checkFlag = 0;
    public NotificationManager nm;
    public Notification.Builder builder;

    SharedPreferences setting;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        setting = getSharedPreferences("test", MODE_PRIVATE);
//        editor = setting.edit();
//        checkFlag = setting.getInt("flag", 1);
//        if(checkFlag!=1){
            Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
            intent.putExtra("data", "1. 비상시 연락이 갈 번호를 입력하여주세요.\n2. 비상시 보낼 문자메세지를 입력하여주세요\n3. 번호와 문자메세지 내용을 입력하셨으면 설정 버튼을 클릭하여 주십시오.\n4. 게시판을 통해서 안전정보의 소식 및 업데이트 정보를 만나보실 수 있습니다.\n5. 상황에 맞게 Notification을 클릭하여 비상시를 대처하세요.\n6.위급상황시에 홀드 버튼을 5번 연속으로 누르세요.");
            startActivityForResult(intent, 1);
//        }
//        editor.putInt("flag", 1);
//        editor.commit();
//        ReadShared();
//        WriteShared();

        startService(new Intent(getApplicationContext(), LockService.class));
        mp=MediaPlayer.create(this, R.raw.bgm);
        mp.setLooping(true);   //무한반복
        mp.start();            //배경음악 재생시작

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        PermissionCheck();

        notification();
        add_receiver();

        sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);   //경보음 재생을 위해 SoundPool API사용
        soundID = sp.load(this, R.raw.emergencysound, 1);

        settingbuttion = (Button) findViewById(R.id.settingButton);   //설정버튼
        newsbutton = (Button) findViewById(R.id.newsButton);   //공지사항 버튼

        settingbuttion.getBackground().setAlpha(50);
        newsbutton.getBackground().setAlpha(50);

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
    }

    public void ReadShared(){
        checkFlag = setting.getInt("flag", 0);
        if(checkFlag!=1){
            Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
            intent.putExtra("data", "Test Popup");
            startActivityForResult(intent, 1);
        }
    }

    public void WriteShared(){
        SharedPreferences.Editor editor = setting.edit();
        editor.putInt("flag", 1);
        editor.commit();
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

    public void turnOnFlash() {  //Flash On

        camera = Camera.open();
        Camera.Parameters param = camera.getParameters();
        param.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        camera.setParameters(param);
        camera.startPreview();
        isFlashOn = false;  //isFlashOn변수를 false로 설정하여 flash가 다시 꺼지게 함.
    }

    public void turnOffFlash() { //Flash off

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

    @Override
    public void onDestroy(){
        super.onDestroy();
        nm.cancelAll();
        builder.setAutoCancel(true);
        System.exit(0);
    }

    public void notification(){
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new Notification.Builder(getApplicationContext());
        builder.setSmallIcon(R.drawable.emergencybutton);
        builder.setTicker("JIKIME");
        builder.setWhen(System.currentTimeMillis());
        builder.setNumber(10);
        builder.setContentTitle("Title");
        builder.setContentText("");
        Notification noti = builder.build();

        RemoteViews contentiew = new RemoteViews(getPackageName(), R.layout.remoteview);

        Intent intent_call_police = new Intent("call_police");
        PendingIntent pendingIntent_call_police = PendingIntent.getBroadcast(this, 0, intent_call_police,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent_message = new Intent("message");
        PendingIntent pendingIntent_message = PendingIntent.getBroadcast(this, 0, intent_message,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent_siren_message = new Intent("siren_message");
        PendingIntent pendingIntent_siren_message = PendingIntent.getBroadcast(this, 0, intent_siren_message,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Intent intent_siren_flash = new Intent("siren_flash");
        PendingIntent pendingIntent_siren_flash = PendingIntent.getBroadcast(this, 0, intent_siren_flash,
                PendingIntent.FLAG_UPDATE_CURRENT);

        contentiew.setOnClickPendingIntent(R.id.button, pendingIntent_call_police);
        contentiew.setOnClickPendingIntent(R.id.button1, pendingIntent_message);
        contentiew.setOnClickPendingIntent(R.id.button2, pendingIntent_siren_message);
        contentiew.setOnClickPendingIntent(R.id.button3, pendingIntent_siren_flash);

        noti.contentView = contentiew;
        nm.notify(123123, noti);

    }

    public void PermissionCheck(){
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED){
        }
        else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
    }
    public void add_receiver(){
        MyReceiver myreceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("call_police");
        intentFilter.addAction("message");
        intentFilter.addAction("siren_message");
        intentFilter.addAction("siren_flash");

        registerReceiver(myreceiver, intentFilter);
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case 1:{
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "승인됨",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(this, "거절됨", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}



