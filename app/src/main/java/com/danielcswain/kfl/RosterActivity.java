package com.danielcswain.kfl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.danielcswain.kfl.AsyncHandlers.RosterAsyncTask;
import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.RosterListAdapter;

import java.util.ArrayList;

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
 */
public class RosterActivity extends AppCompatActivity {

    // Static variables
    public static RosterListAdapter mAdapter;
    public static ProgressBar mProgressBar;
    public static TextView mProgressText;
    public static TextView mTeamName;

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
        ListView mListView = (ListView) findViewById(R.id.rosterListView);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
        }
        // Get the loading text and progress bar and teamName textView
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
            new RosterAsyncTask("RosterActivity").execute(MainActivity.TEAM_URL, apiToken);
        }
    }
}
