package com.danielcswain.kfl;

import android.content.Context;
import android.content.Intent;
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

import com.danielcswain.kfl.Articles.ArticleListAdapter;
import com.danielcswain.kfl.Articles.ArticleObject;
import com.danielcswain.kfl.AsyncHandlers.APIGetHandler;
import com.danielcswain.kfl.Helpers.DatabaseHelper;

import org.json.JSONArray;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static ListView mListView;
    public static ArticleListAdapter mAdapter;
    public static Context mContext;

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
        ArrayList<ArticleObject> articleObjects = new ArrayList<ArticleObject>();
        // Connect our articleObjects array to the ListView adapter
        mAdapter = new ArticleListAdapter(this, articleObjects);
        // Attach the adapter to the ListView
        mListView = (ListView) findViewById(R.id.articlesListView);
        if (mListView != null) {
            mListView.setAdapter(mAdapter);
            // Set an onItemClickedListener to launch our individual Article activity when clicked
            mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Create the launch intent
                    Intent intent = new Intent(mContext, Article.class);
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
        mAdapter.notifyDataSetChanged();
        mDatabaseHelper.close();

        // Add the new articleObjects to our database and listview but only the first time this application loads
        if (MainApplication.firstStart) {
            JSONArray json = null;
            new APIGetHandler(json).execute("");
            MainApplication.setFirstStart(false);
        }



        // Set the navigation view listener to navigate between views
                NavigationView navigationView = (NavigationView) findViewById(R.id.navView);
        navigationView.setNavigationItemSelectedListener(this);
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
            // Handle the articles action
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawerLayout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
