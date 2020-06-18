package com.michael.aadproject;

public class Venue {
    private int venueImage;
    private String venueName;
    private String venueAddress;
    private float venueRating;
    private int venueDistance;

    public Venue(int image, String name, String address, int distance) {
        venueImage = image;
        venueName = name;
        venueAddress = address;
        venueDistance = distance;
    }

    public int getVenueImage() {
        return venueImage;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public int getVenueDistance() {
        return venueDistance;
    }
}
