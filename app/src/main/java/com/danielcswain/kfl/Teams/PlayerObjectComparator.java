package com.danielcswain.kfl.Teams;

import java.util.Comparator;

/**
 * Created by Daniel Swain (ulternate) on 7/06/2016.
 *
 * Comparator class used by the RosterListAdapter to sort the PlayerObjects in alphabetical order
 */
public class PlayerObjectComparator implements Comparator<PlayerObject> {

    /**
     * Compare two PlayerObjects and return whether they are smaller, the same or larger (via playerName)
     * This will sort playerObjects from A - Z via PlayerObject.playerName
     * @param lhs the lhs PlayerObject being compared
     * @param rhs the rhs PlayerObject being compared
     * @return whether the playerObjects are the same, or one is bigger or smaller (returned as int -1, 0, 1)
     */
    @Override
    public int compare(PlayerObject lhs, PlayerObject rhs) {
        return lhs.getName().compareTo(rhs.getName());
    }
}
