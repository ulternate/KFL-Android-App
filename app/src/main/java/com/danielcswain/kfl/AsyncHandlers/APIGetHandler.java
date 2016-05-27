package com.danielcswain.kfl.AsyncHandlers;

import android.os.AsyncTask;
import android.util.Log;

import com.danielcswain.kfl.Articles.Article;
import com.danielcswain.kfl.Helpers.JSONParser;
import com.danielcswain.kfl.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ulternate on 27/05/2016.
 *
 * Get a json from the provided url
 */
public class APIGetHandler extends AsyncTask<String, Void, JSONArray> {

    JSONParser jsonParser = new JSONParser();
    JSONArray json;

    private static final String LOGIN_URL = "http://www.kfl.com.au/api/articles";

    public APIGetHandler(JSONArray json){
        this.json = json;
    }

    @Override
    protected JSONArray doInBackground(String... args) {

        try {

            HashMap<String, String> params = new HashMap<>();
            params.put("name", args[0]);
            params.put("password", args[1]);

            Log.d("request", "starting");

            JSONArray json = jsonParser.makeHttpRequest(
                    LOGIN_URL, "GET", params);

            if (json != null) {
                Log.d("JSON result", json.toString());

                return json;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        json = jsonArray;
        // Loop through the JSONArray and populate our Articles array list and array list adapter
        ArrayList<Article> articles = new ArrayList<Article>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                articles.add(new Article(jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Update the list view
        MainActivity.mAdapter.addAll(articles);
        MainActivity.mAdapter.notifyDataSetChanged();

    }
}
