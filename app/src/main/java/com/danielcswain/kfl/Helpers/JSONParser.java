package com.danielcswain.kfl.Helpers;

import android.util.Log;

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
 * Created by ulternate on 27/05/2016.
 *
 * Connect to a provided URL and return a JSON array
 */
public class JSONParser {
    static String charset = "UTF-8";
    static HttpURLConnection conn;
    static DataOutputStream wr;
    static StringBuilder result;
    static URL urlObj;
    static JSONObject jObj = null;
    static StringBuilder sbParams;
    static String paramsString;

    public static JSONArray makeHttpRequest(String url, String method, HashMap<String, String> params) {
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

        if (method.equals("POST")) {
            // request method is POST
            try {
                urlObj = new URL(url);
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
        } else if (method.equals("GET")) {
            // request method is GET
            if (sbParams.length() != 0) {
                url += "?" + sbParams.toString();
            }
            try {
                urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Charset", charset);
                conn.setConnectTimeout(15000);
                conn.connect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        try {
            //Receive the response from the server
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        // Return the JSON Array result
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(result.toString());
            Log.d("jsonResponse", jsonArray.toString());
        } catch (JSONException e) {
            try{
                // In case we don't get an array, but just a JSON object, then lets add it into a json Array
                JSONObject jsonObject = new JSONObject(result.toString());
                jsonArray = new JSONArray();
                jsonArray.put(jsonObject);
                Log.d("jsonResponseObject", jsonArray.toString());
            }catch (JSONException p){
                p.printStackTrace();
            }
            e.printStackTrace();
        }

        return jsonArray;
    }
}