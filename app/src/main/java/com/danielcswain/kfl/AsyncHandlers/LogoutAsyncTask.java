package com.danielcswain.kfl.AsyncHandlers;

import android.os.AsyncTask;
import android.widget.Toast;

import com.danielcswain.kfl.MainActivity;
import com.danielcswain.kfl.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by Daniel Swain ulternate on 2/06/2016.
 * 
 * AsyncTask to log the user out (deleting their token in the WebServer's Database
 */
public class LogoutAsyncTask extends AsyncTask<String, Void, Void> {
    private static String charset = "UTF-8";
    private static URL urlObj;
    private static HttpURLConnection conn;
    private static String paramsString;
    private static StringBuilder sbParams;
    private static DataOutputStream wr;

    /**
     * Log the user out in an AsyncTask
     * @param args: the url and user token as array [url, token]
     * @return null: nothing is returned from the API
     */
    @Override
    protected Void doInBackground(String... args) {
        // Build the params hashmap from the string args in the .execute call
        HashMap<String, String> params = new HashMap<>();
        // Make sure we only do this if we have enough arguments
        if (args.length > 1) {
            params.put("token", args[1]);
        }

        // Build the API Params string
        sbParams = new StringBuilder();
        int i = 0;
        for (String key : params.keySet()) {
            try {
                if (i != 0) {
                    sbParams.append("&");
                }
                sbParams.append(key).append("=")
                        .append(URLEncoder.encode(params.get(key), charset));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }
        // request method is only going to be POST
        // Make a HttpURLConnection to the logout url and include the Users API Token for authentication
        try {
            urlObj = new URL(args[0]);
            conn = (HttpURLConnection) urlObj.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Accept-Charset", charset);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(15000);
            conn.connect();
            paramsString = sbParams.toString();
            wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(paramsString);
            wr.flush();
            wr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Disconnect from the WebService
        conn.disconnect();

        // We return null as the api doesn't return anything
        return null;
    }

    /**
     * After the background task has completed send a toast to inform the user it was completed
     * @param aVoid: the void stares back at you when you stare into it
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        // Send a Toast message to the MainActivity saying the user was logged out
        Toast.makeText(MainActivity.mContext, R.string.loggedOut, Toast.LENGTH_SHORT).show();
    }
}
