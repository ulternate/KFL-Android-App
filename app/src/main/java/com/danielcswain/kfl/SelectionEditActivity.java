package com.danielcswain.kfl;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.danielcswain.kfl.Helpers.DatabaseHelper;
import com.danielcswain.kfl.Teams.PlayerObject;
import com.danielcswain.kfl.Teams.SelectionObject;

import java.util.ArrayList;

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

        // Get the playerObjects for the User
        DatabaseHelper mDbHelper = new DatabaseHelper(getApplicationContext());
        final ArrayList<PlayerObject> playerObjects = mDbHelper.getPlayers();

        // Loop through and grab all the spinners
        for(int i = 1; i < 15; i++){
            // Get the spinner's id
            int spinnerId = getResources().getIdentifier("editSelectionPlayer" + String.valueOf(i), "id", getPackageName());
            // Get the spinner
            Spinner spinner = (Spinner) findViewById(spinnerId);
            assert spinner != null;
            // Use a simple arrayAdapter for the spinner
            ArrayAdapter<PlayerObject> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, playerObjects);
            // Assign the adapter to the spinner
            spinner.setAdapter(adapter);
            // Check to see if there already exists a selection for this position
            SelectionObject selectionObject = mDbHelper.getSelectionAtPosition(i);
            // If there is a selectionObject then set the spinner selection to it
            if (selectionObject != null){
                Log.d("i", String.valueOf(i));
                spinner.setSelection(getPositionOfPlayerObject(playerObjects, selectionObject.getPlayerObject()));
            } else {
                spinner.setSelection(-1);
            }
        }

        // Connect with the submit button and add an onClickListener
        Button submit = (Button) findViewById(R.id.editSelectionSubmit);
        assert submit != null;
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean valid = true;
                for(int i = 1; i < 15; i++) {
                    int spinnerId = getResources().getIdentifier("editSelectionPlayer" + String.valueOf(i), "id", getPackageName());
                    Spinner spinner = (Spinner) findViewById(spinnerId);
                    if(!doesSpinnerHaveSelection(spinner)) {
                        // a spinner didn't have a selection
                        Toast.makeText(getApplicationContext(), R.string.selectionNoneWarn, Toast.LENGTH_SHORT).show();
                        valid = false;
                        break;
                    }
                }
                if (valid){
                    Toast.makeText(getApplicationContext(), "Not ready yet, but valid sels", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean doesSpinnerHaveSelection(Spinner spinner){
        boolean ans = false;
        if (spinner != null && spinner.getSelectedItem() != null){
            ans = true;
        }
        return ans;
    }

    private int getPositionOfPlayerObject(ArrayList<PlayerObject> playerObjects, PlayerObject playerObject){
        int returnInt = -1;
        for(int i = 0; i < playerObjects.size(); i++){
            String playerName = playerObjects.get(i).getName();
            String aflTeam = playerObjects.get(i).getTeam();
            if (playerName.equals(playerObject.getName()) && aflTeam.equals(playerObject.getTeam())){
                returnInt = i;
                break;
            }
        }
        return returnInt;
    }
}
