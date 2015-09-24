package com.sam.inclass4;

/*
In Class Assingment 4
MainActivity.java
Sam Painter and Praveen Surenani
 */

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private final String BASE_URL = "http://dev.theappsdr.com/lectures/inclass_photos/index.php";

    private ImageView image;
    private ImageButton prev;
    private ImageButton next;
    private ArrayList<String> images;
    private int position;
    private ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        image = (ImageView) findViewById(R.id.imageViewPicture);
        prev = (ImageButton) findViewById(R.id.buttonPrevious);
        next = (ImageButton) findViewById(R.id.buttonNext);
        position = 0;

        if (isConnected()) {
            RequestParams params = new RequestParams("GET", BASE_URL);
            new GetImageList().execute(params);
            showDialog();
        }

        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams("GET", BASE_URL);
                if (position == 0)
                    position = images.size() - 1;
                else
                    position--;

                params.addParam("pid", images.get(position));
                new GetImage().execute(params);
                showDialog();
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestParams params = new RequestParams("GET", BASE_URL);
                if (position == images.size() - 1)
                    position = 0;
                else
                    position++;

                params.addParam("pid", images.get(position));
                new GetImage().execute(params);
                showDialog();
            }
        });

    }

    public void setUp() {
        RequestParams params = new RequestParams("GET", BASE_URL);
        params.addParam("pid", images.get(position));
        new GetImage().execute(params);
    }

    public class RequestParams {
        String baseUrl;
        String method;
        HashMap<String, String> params = new HashMap<String, String>();

        public RequestParams(String method, String baseUrl) {
            super();
            this.method = method;
            this.baseUrl = baseUrl;
        }
        public void addParam(String key, String value){
            params.put(key, value);
        }

        public String getEncodedParams(){
            StringBuilder sb = new StringBuilder();
            for(String key : params.keySet()){
                try {
                    String value = URLEncoder.encode(params.get(key), "UTF-8");
                    if(sb.length() > 0){
                        sb.append("&");
                    }
                    sb.append(key+"="+value);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            return sb.toString();
        }

        public String getEncodedUrl() {
            return this.baseUrl + "?" + getEncodedParams();
        }

        public String getMethod() {
            return this.method;
        }
    }


    private class GetImageList extends AsyncTask<RequestParams, Void, ArrayList<String>> {

        @Override
        protected ArrayList<String> doInBackground(RequestParams... params) {
            BufferedReader reader = null;
            try {
                URL url = new URL(params[0].getEncodedUrl());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod(params[0].getMethod());
                reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                ArrayList<String> imageList = new ArrayList<String>();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    imageList.add(line);
                }
                return imageList;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<String> strings) {
            images = strings;
            setUp();
            pd.dismiss();
        }
    }


    private class GetImage extends AsyncTask<RequestParams, Void, Bitmap> {
        @Override
        protected Bitmap doInBackground(RequestParams... params) {
            try {
                URL url = new URL(params[0].getEncodedUrl());
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                Bitmap bitmap = BitmapFactory.decodeStream(con.getInputStream());
                return bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            if (result != null)
                image.setImageBitmap(result);
            pd.dismiss();
        }
    }

    private boolean isConnected() {
        ConnectivityManager cm = ((ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE));
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni != null && ni.isConnected()) {
            return true;
        }
        Toast.makeText(this, "There is no internet connection", Toast.LENGTH_LONG).show();
        return false;
    }

    private void showDialog() {
        pd = new ProgressDialog(MainActivity.this);
        pd.setMessage("Loading Image ...");
        pd.setCancelable(true);
        pd.show();
    }

}
