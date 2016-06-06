package com.danielcswain.kfl;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
 * Activity to allow users to edit their team selections using the WebService api /api/selected_team using POST
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
            // Get the spinner's id
            int spinnerId = getResources().getIdentifier("editSelectionPlayer" + String.valueOf(i), "id", getPackageName());
            // Get the spinner
            Spinner spinner = (Spinner) findViewById(spinnerId);
            assert spinner != null;
            // Use a simple arrayAdapter for the spinner
            ArrayAdapter<PlayerObject> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, playerObjects);
            // Assign the adapter to the spinner
            spinner.setAdapter(adapter);
            // Check to see if there already exists a selection for this position
            SelectionObject selectionObject = mDbHelper.getSelectionAtPosition(i);
            // If there is a selectionObject then set the spinner selection to it
            if (selectionObject != null){
                Log.d("i", String.valueOf(i));
                spinner.setSelection(getPositionOfPlayerObject(playerObjects, selectionObject.getPlayerObject()));
            } else {
                spinner.setSelection(-1);
            }
        }

        // Connect with the submit button and add an onClickListener
        Button submit = (Button) findViewById(R.id.editSelectionSubmit);
        assert submit != null;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                for(int i = 1; i < 15; i++) {
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
                        String apiUrl = MainActivity.SELECTION_URL + String.valueOf(MainActivity.mSharedPrefs.getInt("selectionId", 0)) + "/";
                        String apiToken = MainActivity.mSharedPrefs.getString("token", "");
                        int selectionId = MainActivity.mSharedPrefs.getInt("selectionId", 0);
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

    private boolean doesSpinnerHaveSelection(Spinner spinner){
        boolean ans = false;
        if (spinner != null && spinner.getSelectedItem() != null){
            ans = true;
        }
        return ans;
    }

    private int getPositionOfPlayerObject(ArrayList<PlayerObject> playerObjects, PlayerObject playerObject){
        int returnInt = -1;
        for(int i = 0; i < playerObjects.size(); i++){
            String playerName = playerObjects.get(i).getName();
            String aflTeam = playerObjects.get(i).getTeam();
            if (playerName.equals(playerObject.getName()) && aflTeam.equals(playerObject.getTeam())){
                returnInt = i;
                break;
            }
        }
        return returnInt;
    }

    private class SelectionEditAsyncTask extends AsyncTask<String, Void, JSONArray> {

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

        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            try {
                JSONObject jsonResponse = jsonArray.getJSONObject(0);
                if (jsonResponse.has("id")){
                    Toast.makeText(getApplicationContext(), R.string.editSelectionsSuccess, Toast.LENGTH_SHORT).show();
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
