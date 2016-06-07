package com.danielcswain.kfl.Teams;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielcswain.kfl.MainActivity;
import com.danielcswain.kfl.R;

import java.util.ArrayList;

/**
 * Created by Daniel Swain (ulternate) on 4/06/2016.
 *
 * ListAdapter to inflate the selection_item.xml with the PlayerObjects representing the user's Player selections
 *
 * Dependencies (Classes and Objects):
 *      MainActivity.mContext: this context object is used for grabbing application resources
 *      SelectionObject: Utility class representing a single Selected PlayerObject (position and player number)
 */
public class SelectionListAdapter extends ArrayAdapter<SelectionObject> {

    /**
     * Constructor class for our SelectionListAdapter
     * @param context the Application Context
     * @param selectionObjects the ArrayList<SelectionObject> containing the User's selected Players
     */
    public SelectionListAdapter(Context context, ArrayList<SelectionObject> selectionObjects){
        // Call the super ArrayAdapter class with our array of playerObjects
        // We only override the getView method
        super(context, 0, selectionObjects);
    }

    /**
     * Override the getView for the ArrayAdapter super class to use our custom listItem layout selection_item
     * @param position the position in the ArrayAdapter
     * @param convertView the view to be inflated, in this case our R.layout.selection_item
     * @param parent the parent viewGroup that the item is in
     * @return the resulting view (in this case the finished ListItem row with playerName and AFLTeam icon)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SelectionObject selectionObject = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.selection_item, parent, false);
        }

        // Get the textView for the PlayerName and PlayerTeamImage
        TextView tvSelectionName = (TextView) convertView.findViewById(R.id.selectionPlayerName);
        TextView tvSelectionPosition = (TextView) convertView.findViewById(R.id.selectionPlayerPosition);
        ImageView ivPlayerTeamImage = (ImageView) convertView.findViewById(R.id.selectionTeamImage);

        // Set the selected player's Name
        tvSelectionName.setText(selectionObject.getPlayerObject().getName());
        // Set the selected player's Position (expanding from the shortcode the API uses)
        switch (selectionObject.getPosition()){
            case "R":
                tvSelectionPosition.setText(R.string.ruckPosition);
                break;
            case "T":
                tvSelectionPosition.setText(R.string.tacklerPosition);
                break;
            case "M":
                tvSelectionPosition.setText(R.string.markerPosition);
                break;
            case "For":
                tvSelectionPosition.setText(R.string.forwardPosition);
                break;
            case "Mid":
                tvSelectionPosition.setText(R.string.midPosition);
                break;
            case "Flx":
                tvSelectionPosition.setText(R.string.flexPosition);
                break;
            default:
                tvSelectionPosition.setText(R.string.ruckPosition);
                break;
        }

        // Set the selected player's AFL Team (using a switch statement for the various AFL teams (18)
        switch (selectionObject.getPlayerObject().getTeam()){
            case "AD":
            case "Adelaide Crows":
            case "Adelaide":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_ad));
                break;
            case "BL":
            case "Brisbane Lions":
            case "Brisbane":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_bris));
                break;
            case "CRL":
            case "Carlton":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_carl));
                break;
            case "COL":
            case "Collingwood":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_col));
                break;
            case "Ess":
            case "Essendon":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_ess));
                break;
            case "FRE":
            case "Fremantle":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_freo));
                break;
            case "GEE":
            case "Geelong":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_geel));
                break;
            case "GC":
            case "Gold Coast Suns":
            case "Gold Coast":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_gc));
                break;
            case "GWS":
            case "GWS Giants":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_gws));
                break;
            case "HAW":
            case "Hawthorn":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_hawk));
                break;
            case "MEL":
            case "Melbourne":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_melb));
                break;
            case "NM":
            case "North Melbourne":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_nm));
                break;
            case "PA":
            case "Port Adelaide":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_pa));
                break;
            case "RIC":
            case "Richmond":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_rich));
                break;
            case "SK":
            case "St Kilda":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_sk));
                break;
            case "SS":
            case "Sydney Swans":
            case "Sydney":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_syd));
                break;
            case "WCE":
            case "West Coast Eagles":
            case "West Coast":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_wce));
                break;
            case "WB":
            case "Western Bulldogs":
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_wb));
                break;
            default:
                ivPlayerTeamImage.setImageDrawable(MainActivity.mContext.getResources().getDrawable(R.drawable.afl_wce));
                break;
        }

        return convertView;
    }
}
