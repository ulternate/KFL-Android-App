package com.danielcswain.kfl;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danielcswain.kfl.AsyncHandlers.LogoutAsyncTask;
import com.danielcswain.kfl.AsyncHandlers.RosterAsyncTask;
import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.Helpers.JSONParser;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.SelectionListAdapter;
import com.danielcswain.kfl.Teams.SelectionObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created Daniel Swain (ulternate) 04/06/2016
 *
 * Selection Activity to View the Users current selected team, as returned by the WebService api
 * https://www.kfl.com.au/api/selected_team/
 *
 * Methods:
 *  onCreate: Create and initiate the activity layout, as well as connecting to the ListView and SelectionListAdapter
 *  onCreateOptionsMenu: Initiate the options menu in the actionBar/toolbar
 *  onOptionsItemSelected: Handle option menu item click events
 *  onActivityResult: Handle finished activity results (in this instance, updating the user's selections was successful)
 *  parseJSONResponse(JSONObject jO): Used in onActivityResult to update the view and database with the new selections from the user.
 *
 * Inner Classes:
 *  SelectionAsyncTask: A private class that extends the AsyncTask class and implements the doInBackground
 *      and onPostExecute methods to connect to the WebService using the /api/selected_team endpoint and
 *      parse the JSONArray into valid SelectionObjects and put any new ones into the database
 */
public class SelectionActivity extends AppCompatActivity {

    // The SelectionListAdapter and view objects
    private SelectionListAdapter mAdapter;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    // The request code used when starting the SelectionEditActivity for a result
    private static final int REQUEST_CODE_EDIT_SELECTIONS = 1;

    /**
     * Create and initiate the activity layout.
     * @param savedInstanceState the information bundle saved by the system when the activity instance is destroyed
     *                           containing information about the activity's view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        // Populate the ListView
        ArrayList<SelectionObject> selectionObjects = new ArrayList<>();
        // Connect our SelectionObjects array to the ListView adapter
        mAdapter = new SelectionListAdapter(this, selectionObjects);
        // Attach the adapter to the ListView
        ListView mListView = (ListView) findViewById(R.id.selectionListView);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        // Get the loading text and progress bar layout objects
        mProgressBar = (ProgressBar) findViewById(R.id.selectionLoadingBar);
        mProgressText = (TextView) findViewById(R.id.selectionLoadingText);

        // Connect to the database and populate the list adapter with the existing playerObjects
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        mAdapter.addAll(mDatabaseHelper.getSelections());
        mAdapter.notifyDataSetChanged();
        // Close the database helper
        mDatabaseHelper.close();

        // Get the user's player roster using the nested private SelectionAsyncTask class and the user's
        // apiToken from the shared preferences file
        String apiToken = MainActivity.mSharedPrefs.getString("token", "");
        if (!apiToken.equals("")) {
            // Show the loadingProgressBar
            mProgressBar.setVisibility(View.VISIBLE);
            // Set the ProgressText to Loading (This will get updated to the teamName after the API call completes
            mProgressText.setText(R.string.loading);
            // Get the latest roster from the WebService, only if there exists a token for the User
            new RosterAsyncTask("SelectionActivity").execute(MainActivity.TEAM_URL, apiToken);
            // Get the latest selections from the WebService, only if there exists a token for the User
            new SelectionAsyncTask().execute(MainActivity.SELECTION_URL, apiToken);
        } else {
            // Send a toast message to the user and finish the activity as they don't have an API key
            Toast.makeText(getApplicationContext(), R.string.noAPI, Toast.LENGTH_SHORT).show();
            // Log the user out to make sure
            new LogoutAsyncTask().execute(MainActivity.LOGOUT_URL);
            // Go back to the mainActivity and set the flag to clear all activities from the stack
            // above it. This is to ensure that any roster/selection activities that the user
            // can't access are closed now that the user is logged out.
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    /**
     * Inflate the menu resource for this activity so we have an overflow menu to enable users to edit selections
     * @param menu the menu inside the activity
     * @return true to ensure the menu popup is opened
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu for the Activity
        getMenuInflater().inflate(R.menu.selection_menu, menu);
        return true;
    }

    /**
     * Handle the selection of MenuItems inside the activity's options menu
     * @param item the MenuItem being selected
     * @return true if the action is actionSelectionsEdit, else it returns super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        // Handle the item selection, in this case, if it's actionSelectionsEdit then it will allow users to edit
        // their selections (not currently implemented)
        if (id == R.id.actionSelectionsEdit) {
            Intent intent = new Intent(getApplicationContext(), SelectionEditActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EDIT_SELECTIONS);
            return true;
        }
        // Return super.onOptionsItemSelected for any item we haven't explicitly covered above.
        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle the result of any activity that was started from this activity for a resutl
     * @param requestCode the request code sent with the original activity start request
     * @param resultCode the result code set by the activity that was started for a result
     * @param data the intent data from the activity that we're getting the result from
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_EDIT_SELECTIONS){
            if (resultCode == RESULT_OK){
                // The user successfully updated their selections, lets parse the JSONObject and update
                // the view and the database
                try{
                    JSONObject jsonResponse = new JSONObject(data.getStringExtra("jsonResponseString"));
                    parseJSONResponse(jsonResponse);
                    // Send a toast to the user notifying their selections were updated
                    Toast.makeText(getApplicationContext(), R.string.editSelectionsSuccess, Toast.LENGTH_LONG).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Similar to the SelectionAsyncTask.onPostExecute, parse the JSONObject into valid PlayerObjects
     * and SelectionObjects and update the selection ListView, SelectionListAdapter and Database with the
     * updated/new selections
     * @param jsonObject the response JSON from the SelectionEditActivity response
     */
    public void parseJSONResponse(JSONObject jsonObject){
        // Get an instance of our DatabaseHelper so we can add the items from the JSONArray to the db
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(MainActivity.mContext);
        // Blank teamName so we can reference it outside the try/catch block
        String teamName = "";
        // Blank int selectionId so we can update these selections later (via the selectionId)
        int selectionId = 0;
        // Get the team name from the team JSONObject
        try {
            teamName = jsonObject.getString("team");
            // Get the selectionId from the team JSONObject
            selectionId = jsonObject.getInt("id");
            // Create a selection object for each player
            for (int i = 1; i < 15; i++){
                JSONObject player = jsonObject.getJSONObject("player" + String.valueOf(i));
                String position = jsonObject.getString("position" + String.valueOf(i));
                // Get the playerName and aflTeam from player if player JSONObject has them
                if (player.has("player_name") && player.has("player_team")){
                    // Get the playerName and aflTeam from the player response
                    String playerName = player.getString("player_name");
                    String aflTeam = player.getString("player_team");
                    // Create a SelectionObject using the playerName, aflTeam, position and playerNum (from the for loop)
                    SelectionObject selectionObject = new SelectionObject(new PlayerObject(playerName, aflTeam), position, i);
                    // Add or update the selectionObject to the database
                    mDatabaseHelper.addSelection(selectionObject);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // Get the latest selectionObjects from the database and update the SelectionListAdapter to contain
        // the updated selections (removing the old selections)
        mAdapter.clear();
        mAdapter.addAll(mDatabaseHelper.getSelections());
        mAdapter.notifyDataSetChanged();
        // Close the connection to the database helper to avoid memory leaks now we're finished with it
        mDatabaseHelper.close();
        // Hide the loading progressBar and change the ProgressText from "Loading" to the teamName
        mProgressBar.setVisibility(View.GONE);
        mProgressText.setText(teamName);
        // Save the selectionId in the database
        MainActivity.mSharedPrefs.edit().putInt("selectionId", selectionId).apply();
    }

    /**
     * Private Inner class that extends the AsyncTask class to connect to the WebService in the background
     * and get back a JSONArray of the user's selectionobjects
     */
    private class SelectionAsyncTask extends AsyncTask<String, Void, JSONArray> {

        /**
         * Connect to the WebService using the JSONParser helper class and get back a JSONArray of user's selections
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
         * Take the resulting JSONArray and add the new/updated SelectionObjects to the database and the
         * SelectionListAdapter to update the ListView with the new information
         * @param jsonArray the resulting JSONArray from the doInBackground call using JSONParser.makeHttpRequest
         */
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            // Get an instance of our DatabaseHelper so we can add the items from the JSONArray to the db
            DatabaseHelper mDatabaseHelper = new DatabaseHelper(MainActivity.mContext);
            // Blank teamName so we can reference it outside the try/catch block
            String teamName = "";
            // Blank int selectionId so we can update these selections later (via the selectionId)
            int selectionId = 0;
            // We have the player objects for the user and their team name so lets add them to the database and
            // update the listView with the new information
            try{
                // Get the team JSONObject (the API returns an array always, but this call only contains 1 object as
                // it's for a single user, so the first item is the user's team
                JSONObject team = jsonArray.getJSONObject(0);
                // Get the team name from the team JSONObject
                teamName = team.getString("team");
                // Get the selectionId from the team JSONObject
                selectionId = team.getInt("id");
                // Create a selection object for each player
                for (int i = 1; i < 15; i++){
                    JSONObject player = team.getJSONObject("player" + String.valueOf(i));
                    String position = team.getString("position" + String.valueOf(i));
                    // Get the playerName and aflTeam from player if player JSONObject has them
                    if (player.has("player_name") && player.has("player_team")){
                        // Get the playerName and aflTeam from the player response
                        String playerName = player.getString("player_name");
                        String aflTeam = player.getString("player_team");
                        // Create a SelectionObject using the playerName, aflTeam, position and playerNum (from the for loop)
                        SelectionObject selectionObject = new SelectionObject(new PlayerObject(playerName, aflTeam), position, i);
                        // Add or update the selectionObject to the database
                        mDatabaseHelper.addSelection(selectionObject);
                    }
                }
            }catch(NullPointerException | JSONException n){
                // Print the exception stack trace
                n.printStackTrace();
            }
            // Get the latest selectionObjects from the database and update the SelectionListAdapter to contain
            // the updated selections (removing the old selections)
            mAdapter.clear();
            mAdapter.addAll(mDatabaseHelper.getSelections());
            mAdapter.notifyDataSetChanged();
            // Close the connection to the database helper to avoid memory leaks now we're finished with it
            mDatabaseHelper.close();
            // Hide the loading progressBar and change the ProgressText from "Loading" to the teamName
            mProgressBar.setVisibility(View.GONE);
            mProgressText.setText(teamName);
            // Save the selectionId in the database
            MainActivity.mSharedPrefs.edit().putInt("selectionId", selectionId).apply();
        }
    }
}
