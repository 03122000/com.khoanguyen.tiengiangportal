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
import java.util.List;

import app.tiengiangportal.data.Constant;
import app.tiengiangportal.jsonhelper.JSONStream;
import app.tiengiangportal.model.ApiClient;
import app.tiengiangportal.model.Images;
import app.tiengiangportal.model.Place;
import app.tiengiangportal.model.PlaceCategory;
import app.tiengiangportal.utils.Callback;

public class ApiClientLoader extends AsyncTask<String, String, ApiClient> {
    Callback<ApiClient> callback;

    JSONStream jsonStream = new JSONStream();
    String URL = Constant.getURLApiClientData();

    private Gson gson = new Gson();
    boolean success = false;

    public ApiClientLoader(Callback<ApiClient> callback) {
        this.callback = callback;
    }

    @Override
    protected ApiClient doInBackground(String... params) {
        try {
            Log.e("CITY", URL);
            Thread.sleep(100);

            JsonReader reader     = jsonStream.getJsonResult(URL, jsonStream.METHOD_GET, new ArrayList<NameValuePair>());

            ApiClient apiClient = new ApiClient();
            List<Place> listPlace = new ArrayList<>();
            List<PlaceCategory> listPlaceCategory = new ArrayList<>();
            List<Images> listImages = new ArrayList<>();

            reader.beginObject();
            while (reader.hasNext()){
                String name = reader.nextName();
                if(name.equals("places")){
                    reader.beginArray();
                    while (reader.hasNext()) {
                        Place place = gson.fromJson(reader, Place.class);
                        listPlace.add(place);
                    }
                    reader.endArray();
                }else if(name.equals("place_category")){
                    reader.beginArray();
                    while (reader.hasNext()) {
                        PlaceCategory placeCategory = gson.fromJson(reader, PlaceCategory.class);
                        listPlaceCategory.add(placeCategory);
                    }
                    reader.endArray();
                }else if(name.equals("images")){
                    reader.beginArray();
                    while (reader.hasNext()) {
                        Images images = gson.fromJson(reader, Images.class);
                        listImages.add(images);
                    }
                    reader.endArray();
                }
            }
            reader.endObject();
            reader.close();

            // set attribute object ApiClient
            apiClient.places = listPlace;
            apiClient.place_category = listPlaceCategory;
            apiClient.images = listImages;

            success = true;
            return apiClient;
        } catch (Exception e) {
            Log.e("Error",e.getMessage());
            e.printStackTrace();
            success = false;
            return null;
        }
    }

    @Override
    protected void onPostExecute(ApiClient result) {
        // Send callback when finish
        if (success) {
            callback.onSuccess(result);
        }else{
            callback.onError("failed");
        }
        super.onPostExecute(result);
    }


}
