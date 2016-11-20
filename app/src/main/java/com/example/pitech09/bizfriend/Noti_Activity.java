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

public class  Noti_Activity extends AppCompatActivity {



    SharedPreferences.Editor editor;
    String username, password, myJSON;
    private WebView webView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        editor = pref.edit();
        username=pref.getString("email", null);
        password = pref.getString("password",null);



        //Get webview
        webView = (WebView) findViewById(R.id.webView1);
        startWebView("http://makebizfriends.com/index.php/welcome/home/show");
                //("http://makebizfriends.com/index.php/welcome/mobilelogincheck/"+username+"/"+password);

        if (!isRegistered()) {

            registerDevice();
        }

    }

    private boolean isRegistered() {

        SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PREF, MODE_PRIVATE);


        return sharedPreferences.getBoolean(Constant.REGISTERED, false);
    }

    private void registerDevice() {
        //Creating a firebase object
        Firebase firebase = new Firebase(Constant.FIREBASE_APP);

        //Pushing a new element to firebase it will automatically create a unique id
        Firebase newFirebase = firebase.push();

        //Creating a map to store name value pair
        Map<String, String> val = new HashMap<>();

        //pushing msg = none in the map
        val.put("msg", "none");

        //saving the map to firebase
        newFirebase.setValue(val);

        //Getting the unique id generated at firebase
        String uniqueId = newFirebase.getKey();

        //Finally we need to implement a method to store this unique id to our server
        sendIdToServer(uniqueId);
    }

    private void sendIdToServer(final String uniqueId) {
        //Creating a progress dialog to show while it is storing the data on server
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering device...");
        progressDialog.show();



        //Creating a string request
        StringRequest req = new StringRequest(Request.Method.POST, Constant.REGISTER_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //dismissing the progress dialog
                        progressDialog.dismiss();

                        //if the server returned the string success
                        if (response.trim().equalsIgnoreCase("success")) {
                            //Displaying a success toast


                            //Opening shared preference
                            SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PREF, MODE_PRIVATE);

                            //Opening the shared preferences editor to save values
                            SharedPreferences.Editor editor = sharedPreferences.edit();

                            //Storing the unique id
                            editor.putString(Constant.UNIQUE_ID, uniqueId);

                            //Saving the boolean as true i.e. the device is registered
                            editor.putBoolean(Constant.REGISTERED, true);

                            //Applying the changes on sharedpreferences
                            editor.apply();

                            //Starting our listener service once the device is registered
                            startService(new Intent(getBaseContext(), NotiListener.class));
                        } else {
                            Toast.makeText(Noti_Activity.this, "Choose a different email", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                //adding parameters to post request as we need to send firebase id and email
                params.put("firebaseid", uniqueId);
                params.put("email", username);
                return params;
            }
        };

        //Adding the request to the queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(req);
    }

    private void startWebView(String url) {


        webView.setWebViewClient(new WebViewClient() {
            ProgressDialog progressDialog;


            public boolean shouldOverrideUrlLoading(WebView view, String url) {


                if(url.equalsIgnoreCase("http://makebizfriends.com/index.php/Welcome/logout")){
                    delete();

                    SharedPreferences pref = getApplicationContext().getSharedPreferences(Confi.LOGGEDIN_SHARED_PREF, MODE_PRIVATE);
                    SharedPreferences.Editor editorr = pref.edit();
                    editorr.putBoolean(Confi.LOGGEDIN_SHARED_PREF, false);
                    editorr.apply();

                    SharedPreferences preff = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
                    preff.edit().clear().apply();

                    SharedPreferences sharedPreferences = getSharedPreferences(Constant.SHARED_PREF, MODE_PRIVATE);
                    String id = sharedPreferences.getString(Constant.UNIQUE_ID, null);



                    Firebase firebase=new Firebase(Constant.FIREBASE_APP+id);
                    firebase.orderByChild(id).equalTo(id).addListenerForSingleValueEvent(
                            new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    dataSnapshot.getRef().removeValue();

                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {

                                }
                            });

                    sharedPreferences.edit().clear().apply();



                    /*File sharedPreferenceFile = new File("/data/data/"+ getPackageName()+ "/shared_prefs/");
                    File[] listFiles = sharedPreferenceFile.listFiles();
                    for (File file : listFiles) {
                        file.delete();
                    }*/



                    Intent intent=new Intent(getApplicationContext(),Login.class);
                    startActivity(intent);
                    finish();
                    return true;

                }else {
                    view.loadUrl(url);
                    return true;
                }
            }

            public void onLoadResource (WebView view, String url) {
                if (progressDialog == null) {

                    progressDialog = new ProgressDialog(Noti_Activity.this);
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
