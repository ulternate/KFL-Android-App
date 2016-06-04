package com.danielcswain.kfl;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.Helpers.JSONParser;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.RosterListAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Daniel Swain (ulternate) 03/06/2016
 *
 * Activity class for the 'My Roster' activity, used to show the user's player roster that they
 * can pick from in the SelectTeamActivity. This uses the http://www.kfl.com.au/api/user_team endpoint.
 *
 * Methods:
 *  onCreate: set the layout, connect our RosterListAdapter to the ListView and get the team
 *      roster from the Database, whilst also checking for new players from the WebService (which
 *      will only add new players to the local database).
 *
 * Inner Classes:
 *  RosterAsyncTask: A private class that extends the AsyncTask class and implements the doInBackground
 *      and onPostExecute methods to connect to the WebService using the /api/user_team endpoint and
 *      parse the JSONArray into valid PlayerObjects and put any new ones into the database.
 */
public class RosterActivity extends AppCompatActivity {

    // Static variables
    private static RosterListAdapter mAdapter;
    private static ListView mListView;
    private static ProgressBar mProgressBar;
    private static TextView mProgressText;
    private static TextView mTeamName;

    /**
     * Called each time the Activity is created, set the view, connect the ListView and RosterListAdapter
     * and get the Users player roster from the database and check the WebService for new players.
     * @param savedInstanceState the information bundle saved by the system when the activity instance is destroyed
     *                           containing information about the activity's view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_roster);

        // Populate the ListView
        ArrayList<PlayerObject> playerObjects = new ArrayList<>();
        // Connect our playerObjects array to the ListView adapter
        mAdapter = new RosterListAdapter(this, playerObjects);
        // Attach the adapter to the ListView
        mListView = (ListView) findViewById(R.id.rosterListView);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        // Get the loading text and progress bar and teamName textview
        mProgressBar = (ProgressBar) findViewById(R.id.rosterLoadingBar);
        mProgressText = (TextView) findViewById(R.id.rosterLoadingText);
        mTeamName = (TextView) findViewById(R.id.rosterTeamName);

        // If we have a shared preference value for the teamName then we don't need to show the loading as we've
        // Gotten the user's roster already
        if (MainActivity.mSharedPrefs.getString("teamName", "").equals("")){
            // Set the progressBar and progressText as visible as we don't have any players for the user's team yet
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressText.setVisibility(View.VISIBLE);
        } else {
            mTeamName.setText(MainActivity.mSharedPrefs.getString("teamName", ""));
        }

        // Connect to the database and populate the list adapter with the existing playerObjects
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        mAdapter.addAll(mDatabaseHelper.getPlayers());
        mAdapter.notifyDataSetChanged();
        // Close the database helper
        mDatabaseHelper.close();

        // Get the user's player roster using the nested private RosterAsyncTask class and the user's
        // apiToken from the shared preferences file
        String apiToken = MainActivity.mSharedPrefs.getString("token", "");
        if (!apiToken.equals("")) {
            // Get the latest roster from the WebService, only if there exists a token for the User
            new RosterAsyncTask().execute(MainActivity.TEAM_URL, apiToken);
        }
    }

    /**
     * Private Inner class that extends the AsyncTask class to connect to the WebService in the background
     * and get back a JSONArray of the users playerObjects
     */
    private class RosterAsyncTask extends AsyncTask<String, Void, JSONArray>{

        /**
         * Connect to the WebService using the JSONParser helper class and get back a JSONArray of user's players
         * @param args the list ofarguments required for the JSONParser.makeHttpRequest method.
         *             In this case they are (WebService URL, API Token)
         * @return JSONArray is returned if successful, null if not successful
         */
        @Override
        protected JSONArray doInBackground(String... args) {
            try {
                // Build a params HashMap containing the users authentication token from
                // the SharedPreference file
                HashMap<String, String> params = new HashMap<>();
                // Make sure we only do this if we have enough arguments
                if (args.length > 1) {
                    params.put("token ", args[1]);

                    // Get the JSONArray using our JSONParser class's HTTPRequest method (POST method)
                    JSONArray json = JSONParser.makeHttpRequest(args[0], "GET AUTH", params);

                    // If we get a returned json then the HTTPRequest was successful so lets return it
                    if (json != null) {
                        return json;
                    }
                } else {
                    // Not enough arguments were supplied
                    Log.d(getLocalClassName(), "Not enough arguments supplied");
                }
            } catch (Exception e) {
                // There was an exception of some kind, report this (this would be sent in any bug reports)
                e.printStackTrace();
            }

            // If we've got to here then we didn't get a successful response so we return null
            return null;
        }

        /**
         * Take the resulting JSONArray and add the new playerObjects to the database and our RosterListAdapter
         * to update the ListView with the new information
         * @param jsonArray the resulting JSONArray from the doInBackground call using JSONParser.makeHttpRequest
         */
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            // ArrayList of new PlayerObjects
            ArrayList<PlayerObject> newPlayerObjects = new ArrayList<>();
            // Get an instance of our DatabaseHelper so we can add the items from the JSONArray to the db
            DatabaseHelper mDatabaseHelper = new DatabaseHelper(MainActivity.mContext);
            // Blank teamName so we can reference it outside the try/catch block
            String teamName = "";
            // We have the player objects for the user and their team name so lets add them to the database and
            // update the listView with the new information
            try{
                // Get the team JSONObject (the API returns an array always, but this call only contains 1 object as
                // it's for a single user, so the first item is the user's team
                JSONObject team = jsonArray.getJSONObject(0);
                // Get the team name from the team JSONObject
                teamName = team.getString("team_name");
                // Get the players from the team object (it will be a JSONArray
                JSONArray players = team.getJSONArray("players");
                // Create a PlayerObject for each player and save it in the database if it doesn't exist
                for (int i = 0; i < players.length(); i++) {
                    // Get the individual player and their params
                    JSONObject player = players.getJSONObject(i);
                    String playerName = player.getString("player_name");
                    String aflTeam = player.getString("player_team");
                    // Create a playerObject
                    PlayerObject playerObject = new PlayerObject(playerName, aflTeam);
                    // If the playerObject doesn't exist then add it to the newPlayerObject ArrayList (for our listAdapter)
                    if (!mDatabaseHelper.doesPlayerExist(playerObject)){
                        newPlayerObjects.add(playerObject);
                    }
                    // Try and save the playerObject to the database (this checks for uniqueness)
                    mDatabaseHelper.addPlayer(playerObject);
                }
            }catch(NullPointerException | JSONException n){
                // Print the exception stack trace
                n.printStackTrace();
            }

            // Close the connection to the database helper to avoid memory leaks now we're finished with it
            mDatabaseHelper.close();

            // If we have new players then add them to the RosterListAdapter
            if (newPlayerObjects.size() > 0) {
                mAdapter.addAll(newPlayerObjects);
                // Notify the list adapter that the Data Set was changed
                mAdapter.notifyDataSetChanged();
            }

            // Hide the loading text and the progressBar
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressText.setVisibility(View.INVISIBLE);

            // Set the TeamName textView
            mTeamName.setText(teamName);
            // Save the TeamName into the SharedPreference file
            MainActivity.mSharedPrefs.edit().putString("teamName", teamName).apply();

        }
    }
}
