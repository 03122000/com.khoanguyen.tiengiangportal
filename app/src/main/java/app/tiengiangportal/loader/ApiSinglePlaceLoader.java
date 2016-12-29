/*
Create by Nguyen Nguyen
 */
package app.tiengiangportal.loader;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.http.NameValuePair;

import java.util.ArrayList;

import app.tiengiangportal.data.Constant;
import app.tiengiangportal.jsonhelper.JSONStream;
import app.tiengiangportal.model.Place;
import app.tiengiangportal.utils.Callback;

public class ApiSinglePlaceLoader extends AsyncTask<String, String, Place> {
    Callback<Place> callback;

    JSONStream jsonStream = new JSONStream();

    private Gson gson = new Gson();
    boolean success = false;
    private int place_id;

    public ApiSinglePlaceLoader(int place_id, Callback<Place> callback) {
        this.callback = callback;
        this.place_id = place_id;
    }

    @Override
    protected Place doInBackground(String... params) {

        String URL_SINGLE_PLACES = Constant.getURLApiSinglePlace(place_id);
        try {
            Log.e("CITY", URL_SINGLE_PLACES);
            Thread.sleep(300);
            JsonReader reader = jsonStream.getJsonResult(URL_SINGLE_PLACES, jsonStream.METHOD_GET, new ArrayList<NameValuePair>());

            Place place = gson.fromJson(reader, Place.class);
            reader.close();

            success = true;
            return place;
        } catch (Exception e) {
            Log.e("Error",e.getMessage());
            e.printStackTrace();
            success = false;
            return null;
        }
    }

    @Override
    protected void onPostExecute(Place result) {
        // Send callback when finish
        if (success) {
            callback.onSuccess(result);
        }else{
            callback.onError("failed");
        }
        super.onPostExecute(result);
    }

}
