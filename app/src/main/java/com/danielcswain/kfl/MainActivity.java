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
import android.view.Menu;
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

import org.json.JSONArray;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static ListView mListView;
    public static ArticleListAdapter mAdapter;
    public static Context mContext;
    public static ProgressBar mProgressBar;
    public final String SHARED_PREFS_NAME = "com.danielcswain.kfl.sharedPreferences";
    public static SharedPreferences mSharedPrefs;
    private NavigationView navigationView;
    private static final int LOGIN_ACTIVITY_REQUEST = 1;

    public static final String LOGIN_URL = "http://www.kfl.com.au/rest-auth/login/";
    public static final String LOGOUT_URL = "http://www.kfl.com.au/rest-auth/logout/";
    public static final String ARTICLES_URL = "http://www.kfl.com.au/api/articles/";
    public static final String TEAM_URL = "http://www.kfl.com.au/api/user_team/";

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
        ArrayList<ArticleObject> articleObjects = new ArrayList<>();
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
                    intent.putExtra("title", articleObject.title);
                    intent.putExtra("author", articleObject.author);
                    intent.putExtra("imageURL", articleObject.imageURL);
                    intent.putExtra("pubDate", articleObject.postDate);
                    intent.putExtra("longText", articleObject.longText);
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
            // Hide login and show logout
            navigationView.getMenu().findItem(R.id.navLogin).setVisible(false);
            navigationView.getMenu().findItem(R.id.navLogout).setVisible(true);
            // Show the navMenu items that require being logged in
            navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(true);
            navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(true);
            navigationView.getMenu().findItem(R.id.navSelectReserves).setVisible(true);
        } else {
            // Hide logout and show login
            navigationView.getMenu().findItem(R.id.navLogin).setVisible(true);
            navigationView.getMenu().findItem(R.id.navLogout).setVisible(false);
            // Hide the navMenu items that require being logged in
            navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(false);
            navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(false);
            navigationView.getMenu().findItem(R.id.navSelectReserves).setVisible(false);
        }
        // Set the navigation view listener to navigate between views
        navigationView.setNavigationItemSelectedListener(this);
    }


    private void getLatestArticlesFromWebService(String url){
        // Show the progressBar
        assert mProgressBar != null;
        mProgressBar.setVisibility(View.VISIBLE);
        // Load the json array through the web service api
        JSONArray json = null;
        new ArticleGetHandler(json).execute(url);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.actionSettings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.navArticles) {
            // Refresh the articles
            getLatestArticlesFromWebService(ARTICLES_URL);
        } else if (id == R.id.navLogin) {
            // Show the login activity
            Intent intent = new Intent(mContext, LoginActivity.class);
            startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST);
        } else if (id == R.id.navLogout){
            // Set the login action as visible and hide this action
            navigationView.getMenu().findItem(R.id.navLogin).setVisible(true);
            navigationView.getMenu().findItem(R.id.navLogout).setVisible(false);
            // Hide the navMenu items that require being logged in
            navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(false);
            navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(false);
            navigationView.getMenu().findItem(R.id.navSelectReserves).setVisible(false);
            // Call the LogoutAsyncTask with the url and token to log the user out
            new LogoutAsyncTask().execute(LOGOUT_URL, mSharedPrefs.getString("token", ""));
            // Delete the API Token, username and teamName from the SharedPreferences file as well
            mSharedPrefs.edit().putString("token", "").apply();
            mSharedPrefs.edit().putString("username", "").apply();
            mSharedPrefs.edit().putString("teamName", "").apply();
            // Delete the tables from the database that pertain to the user
            DatabaseHelper mDatabaseHelper = new DatabaseHelper(mContext);
            mDatabaseHelper.deleteAllObjects(DatabaseHelper.TABLE_NAME_TEAM);
            mDatabaseHelper.close();
        } else if (id == R.id.navMyTeam){
            // Start the Roster activity
            Intent intent = new Intent(mContext, RosterActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LOGIN_ACTIVITY_REQUEST){
            if (resultCode == RESULT_OK){
                // Get the token and username from the returning intent
                String token = data.getStringExtra("token");
                String username = data.getStringExtra("username");
                // Save the token and username in the SharedPreferences file
                mSharedPrefs.edit().putString("token", token).apply();
                mSharedPrefs.edit().putString("username", username).apply();
                // Hide the login navMenu item and show the logout action
                navigationView.getMenu().findItem(R.id.navLogin).setVisible(false);
                navigationView.getMenu().findItem(R.id.navLogout).setVisible(true);
                // Show the navMenu items that require being logged in
                navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(true);
                navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(true);
                navigationView.getMenu().findItem(R.id.navSelectReserves).setVisible(true);
                // Display a welcome message to the user
                Toast.makeText(mContext, getResources().getString(R.string.welcomeUser, mSharedPrefs.getString("username", "")), Toast.LENGTH_LONG).show();
            }
        }

    }
}
