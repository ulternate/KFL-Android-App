package com.danielcswain.kfl;

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
 *
 * Inner Classes:
 *  SelectionAsyncTask: A private class that extends the AsyncTask class and implements the doInBackground
 *      and onPostExecute methods to connect to the WebService using the /api/selected_team endpoint and
 *      parse the JSONArray into valid SelectionObjects and put any new ones into the database TODO update old selection objects.
 *
 */
public class SelectionActivity extends AppCompatActivity {

    private SelectionListAdapter mAdapter;
    private ListView mListView;
    private ProgressBar mProgressBar;
    private TextView mProgressText;
    private TextView mTeamName;

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
        mListView = (ListView) findViewById(R.id.selectionListView);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        // Get the loading text and progress bar and teamName textview
        mProgressBar = (ProgressBar) findViewById(R.id.selectionLoadingBar);
        mProgressText = (TextView) findViewById(R.id.selectionLoadingText);
        mTeamName = (TextView) findViewById(R.id.selectionTeamName);

        // If we have a value for the SharedPreference string "selection" other than "" then we don't need
        // to show the loading as we've gotten the user's selections already
        if (MainActivity.mSharedPrefs.getString("selection", "").equals("")){
            // Set the progressBar and progressText as visible as we don't have any players for the user's selected team yet
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressText.setVisibility(View.VISIBLE);
        } else {
            // Set the mTeamName to the team name as we have some selections
            mTeamName.setText(MainActivity.mSharedPrefs.getString("teamName", ""));
        }

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
            // Get the latest roster from the WebService, only if there exists a token for the User
            new SelectionAsyncTask().execute(MainActivity.SELECTION_URL, apiToken);
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
            Toast.makeText(getApplicationContext(), "Currently you can't edit your selections", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Return super.onOptionsItemSelected for any item we haven't explicitly covered above.
        return super.onOptionsItemSelected(item);
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
            // We have the player objects for the user and their team name so lets add them to the database and
            // update the listView with the new information
            try{
                // Get the team JSONObject (the API returns an array always, but this call only contains 1 object as
                // it's for a single user, so the first item is the user's team
                JSONObject team = jsonArray.getJSONObject(0);
                // Get the team name from the team JSONObject
                teamName = team.getString("team");
                // Create a selection object for each player
                for (int i = 1; i < 15; i++){
                    String player = team.getString("player" + String.valueOf(i));
                    String position = team.getString("position" + String.valueOf(i));
                    // Get the playerName and aflTeam from player if player != "null"
                    if (!player.equals("null")){
                        // Split the player string from "playerName | aflTeam" as it is sent via the webservice
                        // Need to escape the | special character to split that and the space correctly
                        String[] playerArray = player.split(" \\| ");
                        if (playerArray.length == 2) {
                            String playerName = playerArray[0];
                            String aflTeam = playerArray[1];
                            // Create a SelectionObject using the playerName, aflTeam, position and playerNum (from the for loop)
                            SelectionObject selectionObject = new SelectionObject(new PlayerObject(playerName, aflTeam), position, i);
                            // Add or update the selectionObject to the database
                            mDatabaseHelper.addSelection(selectionObject);
                        }
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

            // Hide the loading text and the progressBar
            mProgressBar.setVisibility(View.INVISIBLE);
            mProgressText.setVisibility(View.INVISIBLE);

            // Set the TeamName textView
            mTeamName.setText(teamName);
            // Save the fact the user has selections into the SharedPreference file
            MainActivity.mSharedPrefs.edit().putString("selection", teamName).apply();

        }
    }
}
