package com.danielcswain.kfl.Teams;

/**
 * Created by Daniel Swain (ulternate) on 4/06/2016.
 *
 * Represents a single Player Selection
 *
 * Methods:
 *  SelectionObject(PlayerObject pO, String playerPosition, int playerNum): create a selectionObject
 *      with the provided PlayerObject, playerPosition title and the player's number
 *  get...: Get the PlayerObject, position string or player's number
 *  set...: Set the PlayerObject, position string or player's number
 */
public class SelectionObject {

    private PlayerObject playerObject;
    private String position;
    private int number;

    /**
     * Constructor to create a single selectionObject using the provided details
     * @param player the PlayerObject this selection is for
     * @param playerPosition the player's position
     * @param playerNum the number (from 1 to 14) for the player
     */
    public SelectionObject(PlayerObject player, String playerPosition, int playerNum){
        playerObject = player;
        position = playerPosition;
        number = playerNum;
    }

    /**
     * Get the playerObject from the selectionObject
     * @return a PlayerObject
     */
    public PlayerObject getPlayerObject() {
        return playerObject;
    }

    /**
     * Get the Position of the Selected Player
     * @return a string representing the Player's position
     */
    public String getPosition() {
        return position;
    }

    /**
     * Get the number of the SelectedPlayer
     * @return an integer representing the Player's number
     */
    public int getNumber() {
        return number;
    }

    /**
     * Set the SelectionObject's player
     * @param playerObject the PlayerObject for the SelectionObject
     */
    public void setPlayerObject(PlayerObject playerObject) {
        this.playerObject = playerObject;
    }

    /**
     * Set the Player's position
     * @param position the position of the Player (string)
     */
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * Set the Player's number
     * @param number an int representing the Player's number
     */
    public void setNumber(int number) {
        this.number = number;
    }
}

