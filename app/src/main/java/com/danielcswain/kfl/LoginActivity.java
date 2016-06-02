package com.danielcswain.kfl;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.danielcswain.kfl.Helpers.JSONParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Daniel Swain (ulternate) on 02/06/2016
 *
 * A login activity that takes the users username, email and password and tries to get a REST token
 * from the WebServer's user authentication api (https://www.kfl.com.au/rest-auth/login/)
 *
 * This activity is started for a result, and that result is the token and username if the POST request
 * to the WebServer is successful
 *
 * Methods:
 *  onCreate: Initiate the view and assign the button.onClickListener for submitting the form
 *
 * Inner Class:
 *  LoginAsyncTask: a private class that extends AsyncTask and connects to the LOGIN API in the background
 *      returning a JSONArray containing the user auth token that will be used for subsequent API calls to
 *      the WebService (that require authorisation)
 */
public class LoginActivity extends AppCompatActivity {

    // Private constants for referencing UI Elements
    private static Button submitButton;
    private static TextView loginLoadingText;
    private static ProgressBar loginLoadingBar;
    private static EditText usernameField;
    private static EditText emailField;
    private static EditText passwordField;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Get the shared preferences object for the application (to check if the user is already logged in

        String apiToken = MainActivity.mSharedPrefs.getString("token", "");
        if (!apiToken.equals("")){
            // User already logged in, lets take them back to the main Activity
            finish();
        }

        // Get the submit button and edit text field references
        submitButton = (Button) findViewById(R.id.loginSubmit);
        usernameField = (EditText) findViewById(R.id.usernameField);
        emailField = (EditText) findViewById(R.id.emailField);
        passwordField = (EditText) findViewById(R.id.passwordField);
        loginLoadingText = (TextView) findViewById(R.id.loginLoadingText);
        loginLoadingBar = (ProgressBar) findViewById(R.id.loginLoadingBar);

        // Asserting that the items are in the view
        assert submitButton != null;
        assert usernameField != null;
        assert emailField != null;
        assert passwordField != null;
        assert loginLoadingText != null;
        assert loginLoadingBar != null;

        // Show the button and hide the loading bar and logging in text (to make sure it's in the right state)
        submitButton.setVisibility(View.VISIBLE);
        loginLoadingBar.setVisibility(View.INVISIBLE);
        loginLoadingText.setVisibility(View.INVISIBLE);

        // Set the submit button to do the login action (using the API)
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!usernameField.getText().toString().matches("") &&
                        !emailField.getText().toString().matches("") &&
                        !passwordField.getText().toString().matches("")) {
                    // Hide the button and show the loading bar and logging in text
                    submitButton.setVisibility(View.INVISIBLE);
                    loginLoadingBar.setVisibility(View.VISIBLE);
                    loginLoadingText.setVisibility(View.VISIBLE);

                    // Call the LoginAsyncTask
                    new LoginAsyncTask().execute(MainActivity.LOGIN_URL,
                            usernameField.getText().toString(),
                            emailField.getText().toString(),
                            passwordField.getText().toString());
                } else {
                    Toast.makeText(getApplicationContext(), R.string.noInputData, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Our AsyncTask used to log the user in via the WebServer API (and get a token for later API calls)
     */
    private class LoginAsyncTask extends AsyncTask<String, Void, JSONArray>{

        /**
         * The background/AsyncTask that connects to the login API and returns either null or the
         * resulting JSONArray from the WebServer
         * @param args: the postData for the api (in this case: url, username, email, password)
         * @return a JSONArray or null
         */
        @Override
        protected JSONArray doInBackground(String... args) {
            try {
                // Build a params HashMap containing the users username, email and password from
                // the EditText fields (NB, args is [url, username, email, password])
                HashMap<String, String> params = new HashMap<>();
                // Make sure we only do this if we have enough arguments
                if (args.length > 3) {
                    params.put("username", args[1]);
                    params.put("email", args[2]);
                    params.put("password", args[3]);

                    // Get the JSONArray using our JSONParser class's HTTPRequest method (POST method)
                    JSONArray json = JSONParser.makeHttpRequest(args[0], "POST", params);

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
         * This method runs after the AsyncTask completes it's doInBackground method
         * It is used to get the API response user token and send this back to the main activity
         * to be saved in the application's SharedPreference file (along with the username)
         * @param jsonArray: The resulting jsonArray from our login api (kfl.com.au/rest-auth/login/)
         */
        @Override
        protected void onPostExecute(JSONArray jsonArray) {
            // Empty token string to store the retrieved token
            String token = "";

            try{
                // Try and get the token from the JSONArray (the JSONObject is the 1st element in the array)
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                token = String.valueOf(jsonObject.get("key"));
            }catch(NullPointerException | JSONException n){
                // Print the exception stack trace
                n.printStackTrace();
            }

            // if the token isn't "" anymore then save the token in the sharedPreference file for the user
            if (!token.equals("")){
                // Create the return intent
                Intent returnIntent = new Intent();
                // Add the token and username to the return intent
                returnIntent.putExtra("token", token);
                returnIntent.putExtra("username", usernameField.getText().toString());
                setResult(RESULT_OK, returnIntent);
                // Finish the application to go back to main with the return intent information
                finish();
            }else{
                Toast.makeText(getApplicationContext(), R.string.loginError, Toast.LENGTH_LONG).show();
                // Make the button visible again and hide the loadingBar and loadingText
                // (If we got here, it means there was an error logging the user in
                submitButton.setVisibility(View.VISIBLE);
                loginLoadingBar.setVisibility(View.INVISIBLE);
                loginLoadingText.setVisibility(View.INVISIBLE);
            }
        }
    }
}
