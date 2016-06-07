package com.danielcswain.kfl;

import android.app.Application;

/**
 * Created by Daniel Swain (ulternate) on 28/05/2016.
 *
 * Custom Application class to only run json grabber once on application start (and not every onCreate
 * call in MainActivity)
 */
public class MainApplication extends Application {

    public static boolean firstStart;

    public MainApplication() {
        // this method fires only once per application start.
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // this method fires once as well as constructor
        // Set first start to true (it will be set to false inside MainActivity.onCreate
        firstStart = true;

    }

    // public setter method to change the application variable firstStart
    public static void setFirstStart(boolean bool){
        firstStart = bool;
    }
}