package com.danielcswain.kfl.AsyncHandlers;

import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.Helpers.JSONParser;
import com.danielcswain.kfl.MainActivity;
import com.danielcswain.kfl.R;
import com.danielcswain.kfl.RosterActivity;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.PlayerObjectComparator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Daniel Swain (ulternate) on 6/06/2016.
 *
 * AsyncTask to get the User's Roster from the WebService (Can be used in a RosterActivity or SelectionActivity)
 *
 * methods:
 *  RosterAsyncTask: Constructor for the AsyncTask, requires a String from the calling activity so RosterActivity
 *      actions only run if the RosterActivity starts this task.
 *  doInBackground: Connect to the webService using the JSONParser
 *  onPostExecute: Handle the saving of the Data retrieved from JSONParser via the WebService for the user's roster
 *
 * Dependencies (Classes and Objects):
 *      MainActivity.mContext: this context object is used for initialising the DatabaseHelper
 *      JSONParser: Utility class used to make a HttpUrlConnection to the webservice's User Roster api
 *      DatabaseHelper: Utility class used to save the User's roster to the Application's database
 *      PlayerObject: Class representing a single Player Object
 */
public class RosterAsyncTask extends AsyncTask<String, Void, JSONArray> {

    // Tag used for logging purposes
    private final String LOG_TAG = getClass().getSimpleName();
    private String callingActivity;

    public RosterAsyncTask(String triggeringClass){
        this.callingActivity = triggeringClass;
    }

    /**
     * Connect to the WebService using the JSONParser helper class and get back a JSONArray of user's players
     * @param args the list of arguments required for the JSONParser.makeHttpRequest method.
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
                params.put("token", args[1]);

                // Get the JSONArray using our JSONParser class's HTTPRequest method (POST method)
                JSONArray json = JSONParser.makeHttpRequest(args[0], "GET AUTH", params);

                // If we get a returned json then the HTTPRequest was successful so lets return it
                if (json != null) {
                    return json;
                }
            } else {
                // Not enough arguments were supplied
                Log.e(LOG_TAG, "Not enough arguments supplied");
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
            // Delete all the old players from the Database to ensure there are only the latest from the server
            if(players != null && players.length() > 0){
                // Only delete all the objects if there was a valid player array in the response from the webService
                mDatabaseHelper.deleteAllObjects(DatabaseHelper.TABLE_NAME_TEAM);
            }
            // Create a PlayerObject for each player and save it in the database
            if (players != null) {
                for (int i = 0; i < players.length(); i++) {
                    // Get the individual player and their params
                    JSONObject player = players.getJSONObject(i);
                    String playerName = player.getString("player_name");
                    String aflTeam = player.getString("player_team");
                    // Create a playerObject
                    PlayerObject playerObject = new PlayerObject(playerName, aflTeam);
                    // If the playerObject doesn't exist then add it to the newPlayerObject ArrayList
                    if (!mDatabaseHelper.doesPlayerExist(playerObject)) {
                        newPlayerObjects.add(playerObject);
                    }
                    // Save the playerObject to the database
                    mDatabaseHelper.addPlayer(playerObject);
                }
            }
        }catch(NullPointerException | JSONException n){
            // Print the exception stack trace
            n.printStackTrace();
        }

        // Close the connection to the database helper to avoid memory leaks now we're finished with it
        mDatabaseHelper.close();

        // If the callingClass was the RosterActivity then update the views, otherwise skip this step
        if(callingActivity.equals("RosterActivity")) {
            // Add the new players to the RosterListAdapter, clearing the old players first
            if (newPlayerObjects.size() > 0) {
                RosterActivity.mAdapter.clear();
                RosterActivity.mAdapter.addAll(newPlayerObjects);
                // Sort the ListView using the PlayerObjectComparator
                RosterActivity.mAdapter.sort(new PlayerObjectComparator());
                // Notify the list adapter that the Data Set was changed
                RosterActivity.mAdapter.notifyDataSetChanged();
                // Send toast to user notifying the latest roster has been grabbed
                Toast.makeText(MainActivity.mContext, R.string.gotLatestRoster, Toast.LENGTH_SHORT).show();
            }

            // Hide the loading text and the progressBar
            RosterActivity.mProgressBar.setVisibility(View.INVISIBLE);
            RosterActivity.mProgressText.setVisibility(View.INVISIBLE);

            // Set the TeamName textView
            RosterActivity.mTeamName.setText(teamName);
        }

        // Save the TeamName into the SharedPreference file
        MainActivity.mSharedPrefs.edit().putString("teamName", teamName).apply();
    }
}

