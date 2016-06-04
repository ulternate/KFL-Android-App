package com.danielcswain.kfl;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/**
 * Created Daniel Swain (ulternate) 04/06/2016
 *
 * Selection Activity to View the Users current selected team, as returned by the WebService api
 * https://www.kfl.com.au/api/selected_team/
 *
 * Methods:
 *  onCreate: Create and initiate the activity layout, as well as connecting to the ListView and SelectionListAdapter
 *
 */
public class SelectionActivity extends AppCompatActivity {

    /**
     * Create and initiate the activity layout.
     * @param savedInstanceState the information bundle saved by the system when the activity instance is destroyed
     *                           containing information about the activity's view
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);
    }

    /**
     * Inflate the menu resource for this activity so we have an overflow menu to enable users to edit selections
     * @param menu the menu inside the activity
     * @return true to ensure the menu popup is opened
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu for the Activity
        getMenuInflater().inflate(R.menu.selection_menu, menu);
        return true;
    }

    /**
     * Handle the selection of MenuItems inside the activity's options menu
     * @param item the MenuItem being selected
     * @return true if the action is actionSelectionsEdit, else it returns super.onOptionsItemSelected(item)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // Handle the item selection, in this case, if it's actionSelectionsEdit then it will allow users to edit
        // their selections (not currently implemented)
        if (id == R.id.actionSelectionsEdit) {
            Toast.makeText(getApplicationContext(), "Currently you can't edit your selections", Toast.LENGTH_SHORT).show();
            return true;
        }

        // Return super.onOptionsItemSelected for any item we haven't explicitly covered above.
        return super.onOptionsItemSelected(item);
    }
}
