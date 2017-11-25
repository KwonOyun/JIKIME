package com.example.oyun.saveme;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class News extends AppCompatActivity {  //공지사항을 나타내는 클래스

    private ListView noticeListView;
    private  NoticeListAdapter adapter;
    private Notice notice;
    private List<Notice> noticeList = new ArrayList<Notice>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        noticeListView = (ListView) findViewById(R.id.newsListView);

        new BackgroundTask().execute("http://13.124.158.232/saveme.php");

        adapter = new NoticeListAdapter(getApplicationContext(), noticeList);
        noticeListView.setAdapter(adapter);

        Log.d("size", String.valueOf(adapter.getCount()));
}

    class BackgroundTask extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
                String target = params[0];

            try{
                URL url = new URL(target);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                InputStream inputStream = httpURLConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String temp; //한줄 한줄 저장하기 위해서 임의의 temp설정
                StringBuilder stringBuilder = new StringBuilder();

                while ((temp = bufferedReader.readLine()) != null){
                    stringBuilder.append(temp+"\n");
                }
                bufferedReader.close();
                inputStream.close();
                inputStreamReader.close();
                httpURLConnection.disconnect();
                return stringBuilder.toString().trim(); //다 들어간 문자열을 반환
            }
            catch (Exception e){
                e.printStackTrace();
                return  null;
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try{
                Log.d("test", result);
                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("response");
                int count = 0;
                String noticeText, noticeWriter, noticeDate;
                while(count<jsonArray.length()){
                    JSONObject object = jsonArray.getJSONObject(count);

                    noticeText = object.getString("text");
                    noticeWriter = object.getString("writer");
                    noticeDate = object.getString("date");

                    notice = new Notice(noticeText, noticeWriter, noticeDate);

                    Log.d("test", notice.getNotice());
                    Log.d("writer", notice.getName());

                    noticeList.add(notice);

                    count++;
                }
                adapter.notifyDataSetChanged();
                Log.d("test", String.valueOf(noticeList.size()));

            }
            catch (Exception e){
                e.printStackTrace();
            }
        }

    }
}
