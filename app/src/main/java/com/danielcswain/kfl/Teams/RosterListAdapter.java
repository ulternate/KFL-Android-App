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
 * Created by Daniel Swain (ulternate) on 3/06/2016.
 *
 * List adapter to inflate the player_item view with the playerObjects from the User's available roster
 */
public class RosterListAdapter extends ArrayAdapter<PlayerObject> {

    /**
     * Constructor class for our RosterListAdapter
     * @param context the Application Context
     * @param playerObjects the ArrayList<PlayerObject> containing the User's available
     */
    public RosterListAdapter(Context context, ArrayList<PlayerObject> playerObjects){
        // Call the super ArrayAdapter class with our array of playerObjects
        // We only override the getView method
        super(context, 0, playerObjects);
    }

    /**
     * Override the getView for the ArrayAdapter super class to use our custom listItem layout player_item
     * @param position the position in the ArrayAdapter
     * @param convertView the view to be inflated, in this case our R.layout.player_item
     * @param parent the parent viewGroup that the item is in
     * @return the resulting view (in this case the finished ListItem row with playerName and AFLTeam icon)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PlayerObject playerObject = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.player_item, parent, false);
        }

        // Get the textView for the PlayerName and PlayerTeamImage
        TextView tvPlayerName = (TextView) convertView.findViewById(R.id.playerName);
        ImageView ivPlayerTeamImage = (ImageView) convertView.findViewById(R.id.playerTeamImage);

        // Set the player's Name
        tvPlayerName.setText(playerObject.getName());

        // Set the player's AFL Team (using a switch statement for the various AFL teams (18)
        switch (playerObject.getTeam()){
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
