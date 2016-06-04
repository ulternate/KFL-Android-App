package com.danielcswain.kfl.AsyncHandlers;

import android.os.AsyncTask;
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
 * Created by Daniel Swain (ulternate) on 27/05/2016.
 *
 * Get a JSONArray of ArticleObjects from the WebService URL in an AsyncTask
 *
 * Methods:
 *  ArticleGetHandler(JSONArray json): public constructor for the AsyncTask, takes a JSONArray object
 *  doInBackground(String... args): Use the JSONParser.makeHttpRequest using the provided arguments
 *      to connect to the WebService API in the background.
 *  onPostExecute(JSONArray array): Use the returned JSONArray to update the MainActivity ListView with the
 *      new ArticleObjects received from the WebService API call.
 */
public class ArticleGetHandler extends AsyncTask<String, Void, JSONArray> {

    /**
     * Connect to the WebService to get the ArticleObjects from the API endpoint
     * @param args the arguments for the api, in this case, the URL is all that is required)
     * @return
     */
    @Override
    protected JSONArray doInBackground(String... args) {
        try {
            // This AsyncTask doesn't need any Params, but the JSONParser expects some so just send the empty list
            HashMap<String, String> params = new HashMap<>();
            // Perform the HttpRequest and store the returned JSONArray
            // args[0] represents the url for the WebService API
            JSONArray json = JSONParser.makeHttpRequest(args[0], "GET", params);
            // Return the JSONArray if it's not null
            if (json != null) {
                return json;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // The WebService connection didn't return a JSON or we had an error so return null
        return null;
    }

    /**
     * Use the returned JSONArray to update the MainActivity ListView with the new articles
     * @param jsonArray the JSONArray of ArticleObjects returned from the WebService
     */
    @Override
    protected void onPostExecute(JSONArray jsonArray) {
        // ArrayList of new articles that will be added if there are any new articles on the WebService
        ArrayList<ArticleObject> newArticleObjects = new ArrayList<>();
        // Get an instance of our DatabaseHelper so we can add any new items from the JSONArray to the db
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(MainActivity.mContext);
        // Loop through the JSONArray and add only the newArticleObjects to the Database and newArticleObjects array
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                // Try and create an ArticleObject object from the JSONObject at i
                ArticleObject articleObject = new ArticleObject(jsonArray.getJSONObject(i));
                // If the articleObject doesn't exist then it add it to the ArrayList first
                if (!mDatabaseHelper.doesArticleExist(articleObject)) {
                    newArticleObjects.add(articleObject);
                }
                // Add the articleObject to the database (this method uses the doesArticleExist method internally
                mDatabaseHelper.addArticle(articleObject);
            } catch (JSONException e) {
                // There was an exception grabbing a JSONObject from our JSONArray
                e.printStackTrace();
            }
        }
        // Close the connection to the database helper to avoid memory leaks now we're finished with it
        mDatabaseHelper.close();

        // If new articles then add to the list adapter (This handles new articles and first launch with no articles)
        if (newArticleObjects.size() > 0) {
            MainActivity.mAdapter.addAll(newArticleObjects);
        }

        // Sort the mAdapter regardless of if there's new content, as the database might not be sorted
        // This uses the ArticleComparator to sort via Article Date
        MainActivity.mAdapter.sort(new ArticleComparator());
        // Notify the list adapter that the Data Set was changed
        MainActivity.mAdapter.notifyDataSetChanged();

        // Hide the loading/progress bar as the AsyncTask has finished
        MainActivity.mProgressBar.setVisibility(View.INVISIBLE);
    }
}
