package com.example.pitech09.bizfriend;

/**
 * Created by Pitech09 on 10/25/2016.
 */
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class  Register extends AppCompatActivity {



    SharedPreferences.Editor editor;
    String username, password, myJSON;
    private WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Get webview
        webView = (WebView) findViewById(R.id.webView1);
        startWebView("http://makebizfriends.com/index.php/welcome/signup_view");
        //("http://makebizfriends.com/index.php/welcome/mobilelogincheck/"+username+"/"+password);


    }


    private void startWebView(String url) {


        webView.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog;


            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                    view.loadUrl(url);
                    return true;

            }

            public void onLoadResource (WebView view, String url) {
                if (progressDialog == null) {

                    progressDialog = new ProgressDialog(Register.this);
                    progressDialog.setMessage("Please Wait..");
                    progressDialog.show();
                }
            }
            public void onPageFinished(WebView view, String url) {
                try{
                    progressDialog.dismiss();

                }catch(Exception exception){
                    exception.printStackTrace();
                }
            }

        });

        // Javascript inabled on webview
        webView.getSettings().setJavaScriptEnabled(true);

        // Other webview options

        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setScrollbarFadingEnabled(false);
        //webView.getSettings().setBuiltInZoomControls(true);


        /*
         String summary = "<html><body>You scored <b>192</b> points.</body></html>";
         webview.loadData(summary, "text/html", null);
         */

        //Load url in webview
        webView.loadUrl(url);




    }


    public void delete(){
        class GetDataJSON extends AsyncTask<String, Void, String> {
            public void onPreExecute() {
                // Pbar.setVisibility(View.VISIBLE);
            }
            @Override
            protected String doInBackground(String... params) {

                InputStream inputStream = null;
                String result = null;
                try {

                    URL url = new URL("http://makebizfriends.com/app/firebase/delete.php");

                    JSONObject postDataParams = new JSONObject();
                    postDataParams.put("email", username);

                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(15000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(postDataParams));

                    writer.flush();
                    writer.close();
                    os.close();

                    int responseCode=conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {

                        BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder("");
                        String line="";
                        while ((line = in.readLine()) != null)
                        {
                            sb.append(line).append("\n");
                        }
                        result = sb.toString();
                    }

                    assert inputStream != null;
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        sb.append(line).append("\n");
                    }
                    result = sb.toString();
                } catch (Exception e) {
                    Log.i("tagconvertstr", "["+result+"]");
                    System.out.println(e);
                }
                finally {
                    try{if(inputStream != null)inputStream.close();}catch(Exception squish){}
                }
                return result;
            }

            @Override
            protected void onPostExecute(String result){

                myJSON = result;


            }


        }
        GetDataJSON g = new GetDataJSON();
        g.execute();
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }


    // Open previous opened link from history on webview when back button pressed

    @Override
    // Detect when the back button is pressed
    public void onBackPressed() {
        if(webView.canGoBack()) {
            webView.goBack();
        } else {
            // Let the system handle the back button
            super.onBackPressed();
        }
    }


}

