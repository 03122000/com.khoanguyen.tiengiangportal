/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import app.tiengiangportal.data.Constant;
import app.tiengiangportal.data.SharedPref;
import app.tiengiangportal.model.DeviceInfo;
import app.tiengiangportal.utils.Callback;
import app.tiengiangportal.utils.Tools;

/**
 * Created by muslim on 19/02/2016.
 */
public class GcmLoader extends AsyncTask<String, String, String> {
    private Callback<String> callback;
    boolean gcm_success = false;
    boolean server_success = false;

    private static int CGM_LOOP = 5;    // loop for request gcm_id from google
    private static int SERVER_LOOP = 3; // loop for registering gcm_id to server

    private Context context;
    private SharedPref sharedPref;

    private HttpURLConnection conn;
    private String URL = Constant.getURLgcmserver();

    public GcmLoader(Context context, Callback<String> callback){
        this.context = context;
        this.callback = callback;
        sharedPref = new SharedPref(context);
    }

    @Override
    protected String doInBackground(String... strings) {
        int loop_idx = 0;

        Log.d("GCM", "starting registerGcm");
        while (CGM_LOOP > loop_idx && !gcm_success && sharedPref.isGcmRegIdEmpty()) {
            try {
                registerGcm();
            } catch (IOException e) {
                Log.d("GCM", "error : " + e.getMessage());
            }
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            loop_idx++;
        }

        loop_idx = 0;
        Log.d("GCM", "starting registerServer");
        while (SERVER_LOOP > loop_idx && !server_success && !sharedPref.isGcmRegIdEmpty()){
            Log.d("GCM", "loop : "+loop_idx);
            try {
                server_success = registerServer();
            }catch (Exception e) {
                Log.d("GCM", "err : "+e.getMessage());
                server_success = false;
            }
            try { Thread.sleep(100); } catch (InterruptedException e) {}
            loop_idx++;
        }

        // close connections
        if (conn != null) { conn.disconnect(); }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if(gcm_success && server_success){
            callback.onSuccess(s);
        }else{
            callback.onError(s);
        }
        super.onPostExecute(s);
    }

    // rquest gcmRegId to google
    private String registerGcm() throws IOException {
        Log.d("GCM", "trying");

        // request to google
        GoogleCloudMessaging googleCloudMessaging = GoogleCloudMessaging.getInstance(context);
        String gcmRegId = googleCloudMessaging.register(Constant.PROJECT_API_NUMBER);
        Log.d("GCM", "gcmRegId : " + gcmRegId);

        // store gcmRegId to sharedPreference
        sharedPref.setGCMRegId(gcmRegId);
        gcm_success = true;
        return gcmRegId;
    }

    // register device to server
    private boolean registerServer() throws Exception{
        String response = "";
        // prepare JSON string
        String request = createJsonRequest();

        URL url = new URL(URL);
        conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setReadTimeout(2 * 1000); // 2 second
        conn.setConnectTimeout(2 * 1000); // 2 second
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestMethod("POST");
        conn.connect();

        Log.d("GCM", "request : "+request);

        OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());
        writer.write(request);
        writer.flush();
        writer.close();

        InputStream input = conn.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        StringBuilder result = new StringBuilder();

        String line;
        while ((line = reader.readLine()) != null) { result.append(line); }

        response = result.toString();
        Log.d("GCM", "response : "+response);
        JSONObject object = new JSONObject(response);
        if(object.getString("status").equals("success")){
            sharedPref.setRegisteredOnServer(true);
            return true;
        }
        return false;
    }

    // object to JSON converter
    private String createJsonRequest() throws Exception{
        DeviceInfo deviceInfo = new DeviceInfo();

        deviceInfo.setDevice(Tools.getDeviceName());
        deviceInfo.setEmail(Tools.getEmail(context));
        deviceInfo.setVersion(Tools.getAndroidVersion());
        deviceInfo.setRegid(sharedPref.getGCMRegId());
        deviceInfo.setDate_create(System.currentTimeMillis());

        Gson gson = new Gson();

        return gson.toJson(deviceInfo);
    }
}
