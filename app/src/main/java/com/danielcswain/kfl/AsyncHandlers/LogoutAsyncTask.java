package com.danielcswain.kfl.AsyncHandlers;

import android.os.AsyncTask;
import android.widget.Toast;

import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.MainActivity;
import com.danielcswain.kfl.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Daniel Swain ulternate on 2/06/2016.
 * 
 * AsyncTask to log the user out (deleting their token in the WebServer's Database) and removing their data from
 * this application
 */
public class LogoutAsyncTask extends AsyncTask<String, Void, Void> {

    private static HttpURLConnection conn;
    private static StringBuilder result;
    private static String responseText;

    /**
     * Log the user out in an AsyncTask
     * @param args: the url and user token as array [url, token]
     * @return null: nothing is returned from the API
     */
    @Override
    protected Void doInBackground(String... args) {
        // String to hold the apiToken used to determine which user to log out and which token to remove/invalidate
        String apiToken = "";
        // Set the apiToken to the provided args, but only if we gave enough arguments
        if (args.length > 1) {
            apiToken = args[1];
        }

        // request method is only going to be POST and use the UTF-8 character set
        String charset = "UTF-8";
        // Make a HttpURLConnection to the logout url and include the Users API Token for authorization
        try {
            URL urlObj = new URL(args[0]);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            // Set the request header to use the Authorization value as the user's api token
            conn.setRequestProperty("Authorization", "Token " + apiToken);
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();
            responseText = conn.getResponseMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Handle the response from the WebService api call to see if the user was successfully logged out
        if (responseText.equals("OK")) {
            try {
                //Receive the response from the server
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                // Build the result using the StringBuilder class
                result = new StringBuilder();
                String line;
                // For each line from the response append it to the result using the StringBuilder
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Disconnect from the WebService
        conn.disconnect();

        // We return null as the api doesn't return anything of use
        return null;
    }

    /**
     * After the background task has completed send a toast to inform the user it was completed
     * @param aVoid: the void stares back at you when you stare into it
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        JSONObject jsonResult;
        String resultString = "";
        // The response from the server was OK so we have a response object to check
        if (responseText.equals("OK")) {
            // Check to see if the user was logged out
            try {
                jsonResult = new JSONObject(result.toString());
                resultString = jsonResult.getString("success");
            } catch (JSONException | NullPointerException e) {
                e.printStackTrace();
            }
            // If the user was successfully logged out then resultString != "" it will be "Successfully logged out."
            if (!resultString.equals("")) {
                // The user was successfully logged out so send a toast and reset the view to show login actions
                Toast.makeText(MainActivity.mContext, R.string.loggedOut, Toast.LENGTH_SHORT).show();
                resetViewAndUserData();
            } else {
                // The user was unsuccessful in their request to log out.
                // Send a Toast message to the MainActivity saying the user was unsuccessful in logging out
                Toast.makeText(MainActivity.mContext, R.string.loggedOutFailed, Toast.LENGTH_SHORT).show();
            }
        } else {
            // ResponseText != "OK", it likely equals "UNAUTHORIZED".
            // The user used an unauthorised or invalid token, so lets reset the nav menu to show the login action
            // and send a toast to the user
            Toast.makeText(MainActivity.mContext, R.string.loggedOutUnauthorised, Toast.LENGTH_SHORT).show();
            resetViewAndUserData();
        }
    }

    /**
     * Reset the navMenu view to show only the login items (and hide items that aren't accessible unless logged in)
     * Also, remove any user info from the database and the sharedPreference file
     */
    private void resetViewAndUserData(){
        // Delete the API Token, username, teamName and selection from the SharedPreferences file as well
        MainActivity.mSharedPrefs.edit().putString("token", "").apply();
        MainActivity.mSharedPrefs.edit().putString("username", "").apply();
        MainActivity.mSharedPrefs.edit().putString("teamName", "").apply();
        MainActivity.mSharedPrefs.edit().putString("selection", "").apply();
        // Delete the tables from the database that pertain to the user
        DatabaseHelper mDatabaseHelper = new DatabaseHelper(MainActivity.mContext);
        mDatabaseHelper.deleteAllObjects(DatabaseHelper.TABLE_NAME_TEAM);
        mDatabaseHelper.deleteAllObjects(DatabaseHelper.TABLE_NAME_SELECTED_TEAM);
        mDatabaseHelper.close();
        // Set the login action as visible and hide this action
        MainActivity.navigationView.getMenu().findItem(R.id.navLogin).setVisible(true);
        MainActivity.navigationView.getMenu().findItem(R.id.navLogout).setVisible(false);
        // Hide the navMenu items that require being logged in
        MainActivity.navigationView.getMenu().findItem(R.id.navMyTeam).setVisible(false);
        MainActivity.navigationView.getMenu().findItem(R.id.navSelectTeam).setVisible(false);
    }
}
