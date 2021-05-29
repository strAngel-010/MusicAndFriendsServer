package com.example.demo;

public class User {
	public String name = null;
	public int ID = -1;
    public boolean[] musicPreferences = null;
    public String city = null;
    public Integer[] friendsIDs = null;
    public String[] friendsNames = null;   
    public int notifications;
    
    public User(int ID, String name, boolean[] musicPreferences, String city, Integer[] friendsIDs, String[] friendsNames, int notifications) {
    	this.ID = ID;
    	this.name = name;
    	this.musicPreferences = musicPreferences;
    	this.city = city;
    	this.friendsIDs = friendsIDs;
    	this.friendsNames = friendsNames;
    	this.notifications = notifications;
    }
}
