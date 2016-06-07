package com.danielcswain.kfl;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.danielcswain.kfl.AsyncHandlers.LogoutAsyncTask;
import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.Helpers.JSONParser;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.SelectionObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Daniel Swain (ulternate) 05/06/2016
 *
 * Activity to allow users to edit their team selections using the WebService api /api/selected_team using PUT method
 *
 * Methods:
 *  onCreate: Create and initiate the activity layout and views
 *  buildPostDataFromSelections(): Build a HashMap of string key/value pairs containing the selection data to be sent
 *      to the server.
 *  doesSpinnerHaveSelection(Spinner spinner): Check to see if the provided spinner has a valid selection.
 *  getPositionOfPlayerObject(ArrayList<PlayerObject> objs, PlayerObject obj): Get the position of the playerObject
 *      in the ArrayList of PlayerObjects so the spinner can get it's selection set to that to show the current
 *      selection when the activity is launched.
 *
 * Inner Classes:
 *  SelectionEditAsyncTask: Send the selections to the WebService via the PUT method to update the user's selections
 *      in the Site database (also reflects those changes in the application database)
 *
 * * Dependencies (Classes and Objects):
 *      DatabaseHelper: Allows connections and actions to be performed on the Application database
 *      JSONParser: Makes the HttpUrlConnection to put (update) the latest Selections for the user on the WebService
 *      LogoutAsyncTask: Used when an in valid API token is used, performs the log out action and wipes the database
 *      SelectionObject: Utility class representing a selected Player object (their position and player number)
 *      PlayerObject: Utility class that represents a single Player object
 */
public class SelectionEditActivity extends AppCompatActivity {

    private static HashMap<String, String> postData;
    /**
     * Create and initiate the activity layout.
     * @param savedInstanceState the information bundle saved by the system when the activity instance is destroyed
     *                           containing information about the activity's view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_edit);

        // Get the playerObjects for the User
        DatabaseHelper mDbHelper = new DatabaseHelper(getApplicationContext());
        final ArrayList<PlayerObject> playerObjects = mDbHelper.getPlayers();

        // Loop through and grab all the spinners
        for(int i = 1; i < 15; i++){
            // Get the spinner id
            int spinnerId = getResources().getIdentifier("editSelectionPlayer" + String.valueOf(i), "id", getPackageName());
            // Get the spinner
            Spinner spinner = (Spinner) findViewById(spinnerId);
            assert spinner != null;
            // Use a simple arrayAdapter for the spinner with the playerObjects from the User
            ArrayAdapter<PlayerObject> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, playerObjects);
            // Assign the adapter to the spinner
            spinner.setAdapter(adapter);
            // Check to see if there already exists a selection for this position
            SelectionObject selectionObject = mDbHelper.getSelectionAtPosition(i);
            // If there is a selectionObject then set the spinner selection to it
            if (selectionObject != null){
                spinner.setSelection(getPositionOfPlayerObject(playerObjects, selectionObject.getPlayerObject()));
            } else {
                spinner.setSelection(-1);
            }
        }

        // Connect with the Loading/Submit progress bar and submit button
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.editSelectionLoadingBar);
        final Button submit = (Button) findViewById(R.id.editSelectionSubmit);
        assert submit != null;
        assert progressBar != null;

        // Show the button and hide the progress bar
        submit.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.INVISIBLE);

        // Set the onClickListener for the Submit button
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                for(int i = 1; i < 15; i++) {
                    // Get the spinner and check if it has a valid selection
                    int spinnerId = getResources().getIdentifier("editSelectionPlayer" + String.valueOf(i), "id", getPackageName());
                    Spinner spinner = (Spinner) findViewById(spinnerId);
                    if(!doesSpinnerHaveSelection(spinner)) {
                        // a spinner didn't have a selection
                        Toast.makeText(getApplicationContext(), R.string.selectionNoneWarn, Toast.LENGTH_SHORT).show();
                        // If a spinner doesn't have a selection then set the valid flag to false and break the loop
                        valid = false;
                        break;
                    }
                }
                // If the selections are valid then complete the submit action
                if (valid){
                    // Get the SelectionId from the SharedPreferences and send the selections to the WebService
                    if(MainActivity.mSharedPrefs.getInt("selectionId", 0) != 0 && !MainActivity.mSharedPrefs.getString("token", "").equals("")) {
                        // Get the apiToken and selectionId
                        String apiToken = MainActivity.mSharedPrefs.getString("token", "");
                        int selectionId = MainActivity.mSharedPrefs.getInt("selectionId", 0);
                        // Build the apiURL (needs the selectionId appended)
                        String apiUrl = MainActivity.SELECTION_URL + String.valueOf(selectionId) + "/";
                        // Set the Loading/Submit progress spinner to be visible and hide the button
                        progressBar.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.INVISIBLE);
                        // Execute the API call in the background
                        new SelectionEditAsyncTask().execute(apiUrl, apiToken, String.valueOf(selectionId));
                    } else {
                        // The user didn't have a selectionId or apiToken so they need to log out and log back in
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
            }
        });
    }

    /**
     * Build the postData HashMap from the User's selections, this is sent to the WebService API
     * @return a hashMap properly configured for the WebService API
     */
    private HashMap<String, String> buildPostDataFromSelections(){
        postData = new HashMap<>();
        for(int i = 1; i < 15; i++){
            // Get the spinner
            int spinnerId = getResources().getIdentifier("editSelectionPlayer" + String.valueOf(i), "id", getPackageName());
            Spinner spinner = (Spinner) findViewById(spinnerId);
            // Get the playerObject from the selected item in the spinner
            assert spinner != null;
            PlayerObject playerObject = (PlayerObject) spinner.getSelectedItem();
            // Get the playerName and aflTeam from the PlayerObject
            String playerName = playerObject.getName();
            String aflTeam = playerObject.getTeam();
            // Get the position (the user's can't change the position so it's dependent on the loop counter
            String position = "";
            switch (i){
                case 1:
                    // Ruck position
                    position = "R";
                    break;
                case 2:
                case 3:
                    // Tackler position
                    position = "T";
                    break;
                case 4:
                case 5:
                    // Marker position
                    position = "M";
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    // Forward position
                    position = "For";
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                    // Midfielder position
                    position = "Mid";
                    break;
                case 14:
                    // Flex position
                    position = "Flx";
                    break;
            }
            // Use the playerName, aflTeam and position string and add them to the postData hashmap
            postData.put("player" + String.valueOf(i) + ".player_name", playerName);
            postData.put("player" + String.valueOf(i) + ".player_team", aflTeam);
            postData.put("position" + String.valueOf(i), position);
        }
        // Return the postData hashMap with the user's selections
        return postData;
    }

    /**
     * Check if the provided spinner object has a selection
     * @param spinner the spinner object
     * @return true or false depending on if a selection exists or not
     */
    private boolean doesSpinnerHaveSelection(Spinner spinner){
        // Return true if a selection exists else return false
        return spinner != null && spinner.getSelectedItem() != null;
    }

    /**
     * Get the position of the playerObject in the ArrayList by comparing the name and afl team as the PlayerObjects
     * in the spinner and playerObjects in the ArrayList aren't the same, even if they have the same values/properties
     * so playerObjects.indexOf(PlayerObject playerObject) returns -1.
     * @param playerObjects the ArrayList of playerObjects
     * @param playerObject the playerObject being found
     * @return the index of the provided playerObject in playerObjects
     */
    private int getPositionOfPlayerObject(ArrayList<PlayerObject> playerObjects, PlayerObject playerObject){
        // Set returnInt = -1 initially in case the PlayerObject isn't found
        int returnInt = -1;
        for(int i = 0; i < playerObjects.size(); i++){
            // Find the matching PlayerObject by comparing the playerName and aflTeam
            String playerName = playerObjects.get(i).getName();
            String aflTeam = playerObjects.get(i).getTeam();
            if (playerName.equals(playerObject.getName()) && aflTeam.equals(playerObject.getTeam())){
                returnInt = i;
                // Break out early on the first match
                break;
            }
        }
        // Return the position as either -1 (not found) or the position in the ArrayList
        return returnInt;
    }

    /**
     * Send the changes to the User's selections to the WebService asynchronously.
     */
    private class SelectionEditAsyncTask extends AsyncTask<String, Void, JSONArray> {

        /**
         * Send the changes to the User's selections to the WebService using the JSONParser.makeHttpRequest
         * method and the provided params (built using buildPostDataFromSelections())
         * @param args the string arguments for the task, in this case [url, apiToken]
         * @return a JSONArray containing the WebService's response
         */
        @Override
        protected JSONArray doInBackground(String... args) {
            try {
                // Build a params HashMap containing the users authentication token from
                // the SharedPreference file
                HashMap<String, String> params = new HashMap<>();
                // Make sure we only do this if we have enough arguments
                if (args.length > 1) {
                    // Put the api token in the params list
                    params.put("token", args[1]);
                    // Add the user's selections to the params hashMap
                    params.putAll(buildPostDataFromSelections());
                    // Get the JSONArray using JSONParser class's HTTPRequest method (PUT method)
                    JSONArray json = JSONParser.makeHttpRequest(args[0], "PUT", params);
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
         * Send the updated selections from the JSONArray from the WebService to the SelectionActivity
         * for parsing and updating in the Application View and Database
         * @param jsonArray the response from the server
         */
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                JSONObject jsonResponse = jsonArray.getJSONObject(0);
                if (jsonResponse.has("id")){
                    // The update was a success, send the jsonResponse object back to the SelectionActivity
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("jsonResponseString", jsonResponse.toString());
                    setResult(RESULT_OK, returnIntent);
                    // Finish this activity
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
