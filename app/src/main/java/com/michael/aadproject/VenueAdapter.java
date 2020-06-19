package com.michael.aadproject;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {
    private ArrayList<Venue> venueList;
    public String textDistance;

    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageVenue;
        private TextView textVenueName;
        private TextView textVenueAddress;
        private TextView textVenueDistance;
        private TextView textVenueStatus;
        private TextView textVenueRating;
        private TextView textVenueRatingCount;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVenue = itemView.findViewById(R.id.imageVenue);
            textVenueName = itemView.findViewById(R.id.textViewVenueName);
            textVenueAddress = itemView.findViewById(R.id.textViewVenueAddress);
            textVenueStatus = itemView.findViewById(R.id.textViewVenueStatus);
            textVenueRating = itemView.findViewById(R.id.textViewVenueRating);
            textVenueRatingCount = itemView.findViewById(R.id.textViewVenueRatingCount);
            textVenueDistance = itemView.findViewById(R.id.textViewVenueDistance);
        }
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_item,
                parent, false);
        VenueViewHolder venueViewHolder = new VenueViewHolder(view);
        return venueViewHolder;
    }

    public VenueAdapter (ArrayList<Venue> listOfVenues) {
        venueList = listOfVenues;
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        Venue currentVenue = venueList.get(position);

        new getPhoto(holder.imageVenue).execute(currentVenue.getVenueImage());

        holder.textVenueName.setText(currentVenue.getVenueName());
        holder.textVenueAddress.setText(currentVenue.getVenueAddress());

        String venueStatus = currentVenue.getVenueStatus();
        String statusString;
        if (venueStatus.equals("true")) {
            statusString = "CURRENTLY OPEN";
        } else if (venueStatus.equals("false")) {
            statusString = "CLOSED";
        } else {
            statusString = "";
        }
        holder.textVenueStatus.setText(statusString);

        float venueRating = currentVenue.getVenueRating();
        float venueRatingCount = currentVenue.getVenueRatingCount();
        if (venueRatingCount == 0) {
            holder.textVenueRating.setVisibility(View.INVISIBLE);
            holder.textVenueRatingCount.setVisibility(View.INVISIBLE);
        } else {
            holder.textVenueRating.setText(String.format("%.1f", venueRating));
            holder.textVenueRating.setText(String.format("%d", (int) venueRating));
        }

        double venueDistance = currentVenue.getVenueDistance();
        String distanceString;
        if (venueDistance >= 1000) {
            venueDistance = venueDistance / 1000;
            distanceString = String.format("%.2fkm away", venueDistance);
        } else {
            distanceString = String.format("%dm away", (int) venueDistance);
        }
        textDistance = (String) holder.textVenueDistance.getText();
        holder.textVenueDistance.setText(distanceString);
    }

    @Override
    public int getItemCount() {
        return venueList.size();
    }

    private class getPhoto extends AsyncTask<String, Void, Bitmap> {
        ImageView imageVenue;

        public getPhoto (ImageView imageVenue) {
            this.imageVenue = imageVenue;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String urlString = strings[0];
            Bitmap bitmapVenue = null;
            try {
                InputStream inputStream = new URL(urlString).openStream();
                bitmapVenue = BitmapFactory.decodeStream(inputStream);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmapVenue;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageVenue.setImageBitmap(bitmap);
        }
    }
}
