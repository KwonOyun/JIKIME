package com.example.oyun.saveme;

import android.content.Context;

import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;


public class Setting extends AppCompatActivity {

    static final int REQUEST_CONTACTS = 1000;
    private ArrayAdapter adapter;
    private Spinner spinner;

    EditText p1;  //첫번째 번호
    EditText p2;  //두번째 번호
    EditText p3;  //세번째 번호
    EditText smsmessage; //보낼 메시지 내용

    TextView guide;

    Button settingbutton;  //설정 버튼
    Button checkingbutton;  //등록 정보 확인 버튼

    String receiveName;
    String receiveNumber;
    int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        guide = (TextView) findViewById(R.id.how);
        spinner = (Spinner) findViewById(R.id.residenceSpinner);
        adapter = ArrayAdapter.createFromResource(this, R.array.residence, R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        p1 = (EditText) findViewById(R.id.Pnumber1);  //첫번째 번호
        p2 = (EditText) findViewById(R.id.Pnumber2);  //두번째 번호
        p3 = (EditText) findViewById(R.id.Pnumber3);  //세번째 번호
        smsmessage = (EditText) findViewById(R.id.Smsmessage); //보낼 메시지 내용
        settingbutton = (Button) findViewById(R.id.settingfinish);  //설정 버튼
        checkingbutton = (Button) findViewById(R.id.settingcheck);  //등록 정보 확인 버튼

        guide.setOnClickListener(new View.OnClickListener() {  //팝업창 띄우기
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PopUpActivity.class);
                intent.putExtra("data", "1. 비상시 연락이 갈 번호를 입력하여주세요.\n2. 비상시 보낼 문자메세지를 입력하여주세요\n3. 번호와 문자메세지 내용을 입력하셨으면 설정 버튼을 클릭하여 주십시오.\n4. 게시판을 통해서 안전정보의 소식 및 업데이트 정보를 만나보실 수 있습니다.\n5. 상황에 맞게 Notification을 클릭하여 비상시를 대처하세요.\n6.위급상황시에 홀드 버튼을 5번 연속으로 누르세요.");
                startActivityForResult(intent, 1);
            }
        });


        p1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag =1;
                LoadContract();
            }
        });
        p2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag =2;
                LoadContract();
            }
        });
        p3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                flag =3;
                LoadContract();
            }
        });

        settingbutton.setOnClickListener(new View.OnClickListener() {  //설정버튼을 눌렀을 경우
            @Override
            public void onClick(View view) {
                String p1data = p1.getText().toString();
                String p2data = p2.getText().toString();
                String p3data = p3.getText().toString();
                String smsdata = smsmessage.getText().toString();
                String residencedata = spinner.getSelectedItem().toString();
                InsertData(p1data, p2data, p3data, smsdata, residencedata);      //데이터를 txt파일로 생성해주는 함수
                Toast.makeText(getApplicationContext(), "설정이 완료되었습니다." , Toast.LENGTH_LONG).show();
            }
        });

        checkingbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String contents = LoadData();   //입력된 정보를 Load하고 정보들을 리턴하는 메소드
            }
        });

    }

    public void InsertData(String first, String second, String third, String sms, String res){   //txt파일을 생성해주는 메소드
        try{

            FileOutputStream fos = openFileOutput("savemedatainformation.txt", Context.MODE_WORLD_READABLE);
            PrintWriter writer = new PrintWriter(fos);
            writer.println(first); writer.println(second); writer.println(third); writer.println(sms); writer.println(res);   //각각의 데이터들을 입력
            writer.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public String LoadData(){  //등록정보확인
        StringBuffer buffer = new StringBuffer();
        try{
            FileInputStream fis = openFileInput("savemedatainformation.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            String str = reader.readLine();

            Log.d("Test", str);

            while(str!=null) {
                buffer.append(str + "\n");
                str = reader.readLine();
            }
            Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_LONG).show();

        }
        catch (Exception e){
            e.printStackTrace();
        }
        return buffer.toString();
    }

    public void LoadContract(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACTS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {  //전화번호부 정보를 받아주는 onActivityResult메소드
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            return;
        }
        if(requestCode == REQUEST_CONTACTS){
            Cursor cursor = getContentResolver().query(data.getData(), new String[]{
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER},null,null,null);

            cursor.moveToFirst(); //이름 획득
            receiveName = cursor.getString(0);
            receiveNumber = cursor.getString(1);
            cursor.close();
            if(flag ==1){
                p1.setText(receiveNumber.toString()+" ("+receiveName+")");
            }
            else if(flag==2){
                p2.setText(receiveNumber.toString()+" ("+receiveName+")");
            }
            else if(flag==3){
                p3.setText(receiveNumber.toString()+" ("+receiveName+")");
            }
        }
    }
}
