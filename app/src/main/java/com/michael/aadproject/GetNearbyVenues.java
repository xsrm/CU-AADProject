package com.michael.aadproject;

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

public class GetNearbyVenues extends AsyncTask<String, String, String> {
    private String urlString;
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
    protected String doInBackground(String... strings) {
        urlString = strings[0];
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

                 String venueStatus = "unknown";
                 try {
                     JSONObject statusObject = resultObject.getJSONObject("opening_hours");
                     venueStatus = statusObject.getString("open_now");
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }

                 venue.add(venueName);
                 venue.add(venueAddress);
                 venue.add(venueStatus);
                 venuesArray.add(venue);

                 System.out.println(venueName + " | " + venueAddress + " | " + venueStatus);
                 passer.transferResults(venuesArray);
             }
         } catch (JSONException e) {
             e.printStackTrace();
         }
    }
}
