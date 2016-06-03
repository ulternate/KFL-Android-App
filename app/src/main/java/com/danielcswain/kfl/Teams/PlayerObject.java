package com.danielcswain.kfl.Teams;

/**
 * Created by ulternate on 3/06/2016.
 *
 * Represents a Player Object
 */
public class PlayerObject {

    private String name;
    private String team;

    /**
     * Public constructor used to create a single Player Object
     * @param playerName: The Player's name
     * @param aflTeam: The player's AFL team
     */
    public PlayerObject(String playerName, String aflTeam){
        name = playerName;
        team = aflTeam;
    }

    /**
     * getName get the player name
     * @return the name of the player object
     */
    public String getName() {
        return name;
    }

    /**
     * getTeam, get the player's AFL team
     * @return team, the afl team of the player
     */
    public String getTeam() {
        return team;
    }

    /**
     * setName, set the player's name (if changing)
     * @param name: The new name as String
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * setTeam, set the player's AFL team (if changing)
     * @param team: the new afl team as String
     */
    public void setTeam(String team) {
        this.team = team;
    }
}
