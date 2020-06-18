package com.michael.aadproject;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {
    private ArrayList<Venue> venueList;
    public String textDistance;

    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageVenue;
        private TextView textVenueName;
        private TextView textVenueAddress;
        private TextView textVenueRating;
        private TextView textVenueDistance;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVenue = itemView.findViewById(R.id.imageVenue);
            textVenueName = itemView.findViewById(R.id.textViewVenueName);
            textVenueAddress = itemView.findViewById(R.id.textViewVenueAddress);
            //textVenueRating = itemView.findViewById(R.id.textViewVenueRating);
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

        holder.imageVenue.setImageResource(currentVenue.getVenueImage());
        holder.textVenueName.setText(currentVenue.getVenueName());
        holder.textVenueAddress.setText(currentVenue.getVenueAddress());

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
}
