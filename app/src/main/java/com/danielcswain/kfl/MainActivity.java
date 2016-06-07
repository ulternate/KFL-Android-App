package com.danielcswain.kfl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.danielcswain.kfl.Articles.ArticleComparator;
import com.danielcswain.kfl.Articles.ArticleListAdapter;
import com.danielcswain.kfl.Articles.ArticleObject;
import com.danielcswain.kfl.AsyncHandlers.ArticleGetHandler;
import com.danielcswain.kfl.AsyncHandlers.LogoutAsyncTask;
import com.danielcswain.kfl.Helpers.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by Daniel Swain (ulternate) 27/05/2016
 *
 * MainActivity class that represents the starting point for the Application.
 *
 * This view shows the user the latest articles from the WebService www.kfl.com.au/api/articles
 * as well as given users the options to login, logout, view their roster and view/edit their team selections
 * via a side drawer navMenu
 *
 * Methods:
 *  onCreate: Create and initiate the layout, set up the navMenu items based on the user's logged in status,
 *      get the latest news Articles if the application is just starting.
 *  setNavMenuItemVisibility(boolean loggedIn): Show the navMenuItems based on the user's logged in status.
 *  getLatestArticlesFromWebService(String url): Get the latest articles from the WebService Asynchronously.
 *  onBackPressed(): Handle the back press action to either hide the navMenuDrawer if open, or go back.
 *  onNavigationItemSelected(MenuItem item): Handle the selection of NavMenuItems.
 *  onActivityResult(int requestCode, int resultCode, Intent data): Used to process the results of the
 *      LoginActivity.
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Class objects, some used by AsyncTasks to update the views or get the application context
    public static ListView mListView;
    public static ArticleListAdapter mAdapter;
    public static Context mContext;
    public static ProgressBar mProgressBar;
    public final String SHARED_PREFS_NAME = "com.danielcswain.kfl.sharedPreferences";
    public static SharedPreferences mSharedPrefs;
    public static NavigationView navigationView;

    // Request codes for startActivityForResult actions
    private static final int REQUEST_CODE_LOGIN_ACTIVITY = 1;

    // API paths for the WebService actions
    public static final String LOGIN_URL = "https://www.kfl.com.au/rest-auth/login/";
    public static final String LOGOUT_URL = "https://www.kfl.com.au/rest-auth/logout/";
    public static final String ARTICLES_URL = "https://www.kfl.com.au/api/articles/";
    public static final String TEAM_URL = "https://www.kfl.com.au/api/user_team/";
    public static final String SELECTION_URL = "https://www.kfl.com.au/api/selected_team/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the content view to the main activity view (which contains our listview)
        setContentView(R.layout.activity_main);

        // Get the application context for our Helpers/Handlers to use
        mContext = this.getApplicationContext();

        // Find and connect/set up the actionbar/toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Connect to the nav drawer layout and set it's toggle
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        // Populate the ListView
        final ArrayList<ArticleObject> articleObjects = new ArrayList<>();
        // Connect our articleObjects array to the ListView adapter
        mAdapter = new ArticleListAdapter(this, articleObjects);
        // Attach the adapter to the ListView
        mListView = (ListView) findViewById(R.id.articlesListView);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
            // Set an onItemClickedListener to launch our individual ArticleActivity activity when clicked
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Create the launch intent
                    Intent intent = new Intent(mContext, ArticleActivity.class);
                    // Add the article string extras to the intent for showing the article content
                    ArticleObject articleObject = (ArticleObject) mListView.getItemAtPosition(position);
                    intent.putExtra("title", articleObject.getTitle());
                    intent.putExtra("author", articleObject.getAuthor());
                    intent.putExtra("imageURL", articleObject.getImageURL());
                    intent.putExtra("pubDate", articleObject.getPostDate());
                    intent.putExtra("longText", articleObject.getLongText());
                    intent.putExtra("category", articleObject.getCategory());
                    startActivity(intent);
                }
            });
        }

        // Connect to the database and populate the list adapter with the existing records
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(mContext);
        mAdapter.addAll(mDatabaseHelper.getArticles());
        // Sort the data set and notify it was changed
        mAdapter.sort(new ArticleComparator());
        mAdapter.notifyDataSetChanged();
        mDatabaseHelper.close();

        // Connect to the progress bar so we can turn it off when the AsyncTask has finished
        mProgressBar = (ProgressBar) findViewById(R.id.loadingBar);

        // Add the new articleObjects to our database and ListView but only the first time this application loads
        if (MainApplication.firstStart) {
            // Call the helper method used to get the latest articles from the web service
            getLatestArticlesFromWebService(ARTICLES_URL);
            // Set firstStart to false so this doesn't run again unless manually called by user later
            MainApplication.setFirstStart(false);
        }

        // Get the navigationView object
        navigationView = (NavigationView) findViewById(R.id.navView);
        assert navigationView != null;
        // If the user is logged in then hide the Login option from the navMenu
        mSharedPrefs = mContext.getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        if (!mSharedPrefs.getString("token", "").equals("") && !mSharedPrefs.getString("username", "").equals("")) {
            // Show the Welcome Back String only if it's the firstStart
            if (MainApplication.firstStart) {
                Toast.makeText(mContext, getResources().getString(R.string.welcomeBackUser, mSharedPrefs.getString("username", "")), Toast.LENGTH_SHORT).show();
            }
            // Set the navMenuItem visibility for a user that is loggedIn
            setNavMenuItemVisibility(true);
        } else {
            // Set the navMenuItem visibility for a user that isn't loggedIn
            setNavMenuItemVisibility(false);
        }
        // Set the navigation view listener to navigate between views
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Set up the navMenuItem's based upon if the user is logged in or not.
     * @param loggedIn true for a logged in user, false for a logged out user or user that's not logged in
     */
    private void setNavMenuItemVisibility(boolean loggedIn){
        if(loggedIn){
            // Hide login and show logout
            navigationView.getMenu().findItem(R.id.navLogin).setVisible(false);
            navigationView.getMenu().findItem(R.id.navLogout).setVisible(true);
            // Show the navMenu items that require being logged in
            navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(true);
            navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(true);
        } else {
            // Hide logout and show login
            navigationView.getMenu().findItem(R.id.navLogin).setVisible(true);
            navigationView.getMenu().findItem(R.id.navLogout).setVisible(false);
            // Hide the navMenu items that require being logged in
            navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(false);
            navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(false);
        }
    }

    /**
     * Get the latest articles from the WebService using the provided URL and the ArticleGetHandler
     * @param url the url for the WebService where the Articles can be retrieved from
     */
    private void getLatestArticlesFromWebService(String url){
        // Show the progressBar
        assert mProgressBar != null;
        mProgressBar.setVisibility(View.VISIBLE);
        // Get the latest ArticleObjects from the WebService
        new ArticleGetHandler().execute(url);
    }

    /**
     * Handle the back pressed button inside the activity. this will close the drawer if it is open
     * otherwise it will go back in the activity stack
     */
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Handle the selection of the navMenu items
     * @param item the item in the nav menu that was selected
     * @return true to continue through the selection action
     */
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navArticles) {
            // Deselect the item
            item.setChecked(false);
            // Refresh the articles
            getLatestArticlesFromWebService(ARTICLES_URL);
        } else if (id == R.id.navLogin) {
            // Deselect the item
            item.setChecked(false);
            // Show the login activity
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOGIN_ACTIVITY);
        } else if (id == R.id.navLogout){
            // Deselect the item
            item.setChecked(false);
            // Call the LogoutAsyncTask with the url and token to log the user out
            new LogoutAsyncTask().execute(LOGOUT_URL, mSharedPrefs.getString("token", ""));
        } else if (id == R.id.navMyTeam){
            // Deselect the item
            item.setChecked(false);
            // Start the Roster activity
            Intent intent = new Intent(mContext, RosterActivity.class);
            startActivity(intent);
        } else if (id == R.id.navSelectTeam){
            // Deselect the item
            item.setChecked(false);
            // Start the Select Team Activity
            Intent intent = new Intent(mContext, SelectionActivity.class);
            startActivity(intent);
        }
        // Close the drawer upon selection
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Handle the result from an activity started with startActivityForResult
     * @param requestCode the request code used to differentiate activities
     * @param resultCode the result code set by the called activity
     * @param data the intent data returned by the called activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_LOGIN_ACTIVITY){
            if (resultCode == RESULT_OK){
                // Get the token and username from the returning intent
                String token = data.getStringExtra("token");
                String username = data.getStringExtra("username");
                // Save the token and username in the SharedPreferences file
                mSharedPrefs.edit().putString("token", token).apply();
                mSharedPrefs.edit().putString("username", username).apply();
                // Set the navMenuItem visibility for a user that is loggedIn
                setNavMenuItemVisibility(true);
                // Display a welcome message to the user
                Toast.makeText(mContext, getResources().getString(R.string.welcomeUser, mSharedPrefs.getString("username", "")), Toast.LENGTH_LONG).show();
            }
        }

    }
}
