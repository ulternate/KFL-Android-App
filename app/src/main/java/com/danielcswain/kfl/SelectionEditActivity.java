package com.danielcswain.kfl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Created by Daniel Swain (ulternate) 05/06/2016
 *
 * Activity to allow users to edit their team selections using the WebService api /api/selected_team using POST
 */
public class SelectionEditActivity extends AppCompatActivity {

    /**
     * Create and initiate the activity layout.
     * @param savedInstanceState the information bundle saved by the system when the activity instance is destroyed
     *                           containing information about the activity's view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection_edit);
    }
}
