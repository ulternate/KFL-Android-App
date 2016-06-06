package com.danielcswain.kfl.Helpers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by Daniel Swain (ulternate) on 27/05/2016.
 *
 * Connect to a provided WebService URL using the given Method and return a JSON array if the WebService Api
 * supports that.
 *
 * Methods:
 *  makeHttpRequest(String url, String method, HashMap params): Make the http request using the provided
 *      webservice url, the http method (GET, POST) and the params (i.e. username/password, api token, postData).
 *      uses the HttpUrlConnection class to connect to the WebService api.
 */
public class JSONParser {
    // Private class variables
    private static String charset = "UTF-8";
    private static HttpURLConnection conn;
    private static DataOutputStream wr;
    private static StringBuilder result;
    private static URL urlObj;
    private static StringBuilder sbParams;
    private static String paramsString;

    /**
     * Make the http request to the WebService url provided with the given http method and postData params.
     * Return a JSONArray from the WebService API (if the api returns one). This uses the HttpUrlConnection class
     * @param url (String) the WebService API endpoint url
     * @param method (String) the http method to use
     * @param params (HashMap<String, String>) the postData params (i.e. username/password, api token...)
     * @return a JSONArray from the WebService API call response
     */
    public static JSONArray makeHttpRequest(String url, String method, HashMap<String, String> params) {
        // String builder object to store the built PostData from the provided params HashMap
        sbParams = new StringBuilder();
        String apiToken = "";

        // Switch statement to perform the HttpUrlConnection based upon the provided method string
        switch (method) {
            case "POST":
                // request method is POST and doesn't require the API token
                try {
                    urlObj = new URL(url);
                    // Build the HttpURLConnection object with the POST method
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    conn.setRequestProperty("Accept-Charset", charset);
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.connect();
                    // Get the PostData from our params and add them to the dataOutputStream
                    sbParams = buildPostData(params);
                    paramsString = sbParams.toString();
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "GET":
                // request method is GET and doesn't require the API token (but can use other postData)
                sbParams = buildPostData(params);
                if (sbParams.length() != 0) {
                    // Build the url to include the postData params
                    url += "?" + sbParams.toString();
                }
                try {
                    urlObj = new URL(url);
                    // Build the HttpURLConnection object with the GET method and provided postData params
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept-Charset", charset);
                    conn.setConnectTimeout(15000);
                    conn.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "GET AUTH":
                // Request method is GET, but it needs to use the API token as its an authenticated endpoint
                // Grab the apiToken from the postData params as it needs to go into the connection header
                sbParams = buildPostData(params);
                if (sbParams.length() != 0) {
                    // sbParams is formatted as "token=APITokenValue". Need to split via "=" and grab just the token value
                    apiToken = sbParams.toString().split("=")[1];
                }
                try {
                    urlObj = new URL(url);
                    // Build the HttpURLConnection object with the GET method and add the authorization token to the header.
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(false);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Authorization", "Token " + apiToken);
                    conn.setRequestProperty("Accept-Charset", charset);
                    conn.setConnectTimeout(15000);
                    conn.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case "PUT":
                // Request method is PUT, this requires the API token as it's an authenticated endpoint
                // Grab the apiToken from the postData params as it is needed for the connection header
                apiToken = params.get("token");
                // Remove the token from the hashMap so it isn't sent in the PostData
                params.remove("token");
                // Build the connection using the postData and apiToken
                try {
                    urlObj = new URL(url);
                    // Build the HttpURLConnection object with the PUT method and API Token
                    conn = (HttpURLConnection) urlObj.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("PUT");
                    conn.setRequestProperty("Authorization", "Token " + apiToken);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Accept-Charset", charset);
                    conn.setRequestProperty("Accept", "application/json");
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(15000);
                    conn.connect();
                    // Get the Post JSON from the params and add them to the dataOutputStream
                    JSONObject jsonParams = buildSelectionsJSONPostData(params);
                    paramsString = jsonParams.toString();
                    wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(paramsString);
                    wr.flush();
                    wr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }

        // Handle the response from the WebService api call
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

        // Close the connection now we have the result
        conn.disconnect();

        // The JSONArray object that will be returned
        JSONArray jsonArray = null;
        try {
            // Parse the HttpURLConnection result to a JSONArray
            jsonArray = new JSONArray(result.toString());
        } catch (JSONException e) {
            try{
                // Try if response isn't an array, but just a single JSON object, then add it into a JSONArray
                JSONObject jsonObject = new JSONObject(result.toString());
                jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
            }catch (JSONException p){
                // Bundle up both exceptions for logging/error checking purposes
                p.printStackTrace();
                e.printStackTrace();
            }
        }

        // Return the JSONArray from the WebService (or null)
        return jsonArray;
    }

    /**
     * Build the PostData for the POST/GET action using the params HashMap from the user's info
     * @param params the HashMap of user info/data to be encoded for the WebService request
     * @return the resulting string builder
     */
    private static StringBuilder buildPostData(HashMap<String, String> params){
        // The StringBuilder object that will be returned
        StringBuilder stringBuilder = new StringBuilder();
        int i = 0;
        // For each key/value pair in the HashMap append it to the StringBuilder to build the PostData
        for (String key : params.keySet()) {
            try {
                if (i != 0) {
                    // Add '&' if it's not the first key/value pair
                    stringBuilder.append("&");
                }
                // URL encode the params value in the desired character set (UTF-8) and append it to the PostData
                stringBuilder.append(key).append("=").append(URLEncoder.encode(params.get(key), charset));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            i++;
        }
        // Return the postData
        return stringBuilder;
    }

    /**
     * Build a JSON object to sent the User's selection data to the WebService for the PUT call
     * @param params the HashMap of the user's selections, to be converted into a JSONObject
     * @return the resulting JSONObject of the user's selections
     */
    private static JSONObject buildSelectionsJSONPostData(HashMap<String, String> params){
        // The JSONObject that will be returned
        JSONObject jsonObject = new JSONObject();
        for(int i = 1; i < 15; i++){
            try {
                // Create a new JSONObject for this player
                JSONObject player = new JSONObject();
                // The JSONObject key (needed for getting from params and putting into the final JSONObject)
                String jsonPlayerKey = "player" + String.valueOf(i);
                // Put the player_name and player_team values into the player JSONObject
                player.put("player_name", params.get(jsonPlayerKey + ".player_name"));
                player.put("player_team", params.get(jsonPlayerKey + ".player_team"));
                // Put the player JSONObject into the returning object
                jsonObject.put(jsonPlayerKey, player);
                // Put the position into the returning object
                jsonObject.put("position" + String.valueOf(i), params.get("position" + String.valueOf(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Return the JSONObject containing the user's selections
        return jsonObject;
    }
}