package com.michael.aadproject;

import android.location.Location;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class GetNearbyVenues extends AsyncTask<Object, String, String> {
    private String urlString;
    private Location userLocation;
    private String result;
    private ArrayList<ArrayList<String>> venuesArray;

    public interface taskResponse {
        void transferResults(ArrayList venuesArray);
    }

    public taskResponse passer = null;

    public GetNearbyVenues(taskResponse passer) {
        this.passer = passer;
    }

    @Override
    protected String doInBackground(Object... objects) {
        urlString = (String) objects[0];
        userLocation = (Location) objects[1];

        try {
            URL url = new URL(urlString);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url.openConnection();
            httpsURLConnection.connect();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    httpsURLConnection.getInputStream()));

            String line = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            result = stringBuilder.toString();

            bufferedReader.close();
            httpsURLConnection.disconnect();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(String data) {
        try {
            JSONObject responseObject = new JSONObject(data);
            JSONArray resultsArray = responseObject.getJSONArray("results");
            venuesArray = new ArrayList<ArrayList<String>>();
            for (int i = 0; i < resultsArray.length(); i++) {
                ArrayList<String> venue = new ArrayList<String>();
                JSONObject resultObject = resultsArray.getJSONObject(i);
                String venueName = resultObject.getString("name");
                String venueAddress = resultObject.getString("vicinity");

                JSONObject geometryObject = resultObject.getJSONObject("geometry").
                        getJSONObject("location");
                double venueLatitude = geometryObject.getDouble("lat");
                double venueLongitude = geometryObject.getDouble("lng");
                Location venueLocation = new Location("Google Places Web API");
                venueLocation.setLatitude(venueLatitude);
                venueLocation.setLongitude(venueLongitude);
                float inBetweenDistance = userLocation.distanceTo(venueLocation);
                //String venueDistance = Integer.toString(Math.round(inBetweenDistance));
                String venueDistance = Float.toString(inBetweenDistance);

                String venueStatus = "unknown";
                try {
                    JSONObject statusObject = resultObject.getJSONObject("opening_hours");
                    venueStatus = statusObject.getString("open_now");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String venuePhotoRef = "unknown";
                try {
                    JSONArray photoArray = resultObject.getJSONArray("photos");
                    JSONObject photoObject = photoArray.getJSONObject(0);
                    venuePhotoRef = photoObject.getString("photo_reference");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String venueRating = "0";
                String venueRatingCount = "0";
                try {
                    venueRating = resultObject.getString("rating");
                    venueRatingCount = resultObject.getString("user_ratings_total");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                venue.add(venueName);
                venue.add(venueAddress);
                venue.add(venueDistance);
                venue.add(venueStatus);
                venue.add(venueRating);
                venue.add(venueRatingCount);
                venue.add(venuePhotoRef);
                venuesArray.add(venue);

                System.out.println(venueName + " | " + venueAddress + " | " +
                        venueDistance + " | " + venueStatus + " | " + venueRating + " | " +
                        venueRatingCount + " | " + venuePhotoRef);
                passer.transferResults(venuesArray);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
