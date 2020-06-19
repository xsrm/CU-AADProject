package com.michael.aadproject;

public class Venue {
    private String venueImage;
    private String venueName;
    private String venueAddress;
    private float venueDistance;
    private String venueStatus;
    private float venueRating;
    private float venueRatingCount;

    public Venue(String image, String name, String address, float distance, String status,
                 float rating, float ratingCount) {
        venueImage = image;
        venueName = name;
        venueAddress = address;
        venueDistance = distance;
        venueStatus = status;
        venueRating = rating;
        venueRatingCount = ratingCount;
    }

    public String getVenueImage() {
        return venueImage;
    }

    public String getVenueName() {
        return venueName;
    }

    public String getVenueAddress() {
        return venueAddress;
    }

    public float getVenueDistance() {
        return venueDistance;
    }

    public String getVenueStatus() {
        return venueStatus;
    }

    public float getVenueRating() {
        return venueRating;
    }

    public float getVenueRatingCount() {
        return venueRatingCount;
    }
}
