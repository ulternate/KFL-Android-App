package com.danielcswain.kfl.AsyncHandlers;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.danielcswain.kfl.Articles.ArticleComparator;
import com.danielcswain.kfl.Articles.ArticleObject;
import com.danielcswain.kfl.Helpers.DatabaseHelper;
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
public class ArticleGetHandler extends AsyncTask<String, Void, JSONArray> {

//    JSONParser jsonParser = new JSONParser();
    JSONArray json;

    private static final String LOGIN_URL = "http://www.kfl.com.au/api/articles";

    public ArticleGetHandler(JSONArray json){
        this.json = json;
    }

    @Override
    protected JSONArray doInBackground(String... args) {

        try {

            HashMap<String, String> params = new HashMap<>();
            if (args.length > 2) {
                params.put("name", args[1]);
                params.put("password", args[2]);
            }

            JSONArray json = JSONParser.makeHttpRequest(
                    args[0], "GET", params);

            if (json != null) {
                return json;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        // ArrayList of new articles
        ArrayList<ArticleObject> newArticleObjects = new ArrayList<>();
        // Get an instance of our DatabaseHelper so we can add the items from the JSONArray to the db
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(MainActivity.mContext);
        // Loop through the JSONArray and add only the newArticleObjects to the Database and newArticleObjects array
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                // Try and create an ArticleObject object from the JSONObject at i
                ArticleObject articleObject = new ArticleObject(jsonArray.getJSONObject(i));
                // If the articleObject doesn't exist then it will be added and it needs to be added to our ArrayList first
                if (!mDatabaseHelper.doesArticleExist(articleObject)) {
                    newArticleObjects.add(articleObject);
                }
                // Add the articleObject to the database (this method uses the doesArticleExist method internally
                mDatabaseHelper.addArticle(articleObject);

            } catch (JSONException e) {
                // There was an exception grabbing a JSONObject from our JSONArray
                Log.e("APIGet.onPostExecute", e.toString());
                e.printStackTrace();
            }
        }
        // Close the connection to the database helper to avoid memory leaks now we're finished with it
        mDatabaseHelper.close();

        // If we have new articles then add them to the list adapter (This handles new articles and first launch with no articles
        if (newArticleObjects.size() > 0) {
            MainActivity.mAdapter.addAll(newArticleObjects);
        }

        // Sort the mAdapter regardless of if there's new content, as the database might not be sorted
        MainActivity.mAdapter.sort(new ArticleComparator());
        // Notify the list adapter that the Data Set was changed
        MainActivity.mAdapter.notifyDataSetChanged();

        // Hide the loading/progress bar as the AsyncTask has finished
        MainActivity.mProgressBar.setVisibility(View.INVISIBLE);
    }
}
