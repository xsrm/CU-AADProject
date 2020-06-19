package com.michael.aadproject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

public class VenueAdapter extends RecyclerView.Adapter<VenueAdapter.VenueViewHolder> {
    private ArrayList<Venue> venueList;
    public String textDistance;
    private Context context;

    public static class VenueViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageVenue;
        private TextView textVenueName;
        private TextView textVenueAddress;
        private TextView textVenueDistance;
        private TextView textVenueStatus;
        private TextView textVenueRating;
        private TextView textVenueRatingCount;
        private ImageView mapLink;
        private ImageView imageBroken;
        private TextView imageBrokenText;

        public VenueViewHolder(@NonNull View itemView) {
            super(itemView);
            imageVenue = itemView.findViewById(R.id.imageVenue);
            textVenueName = itemView.findViewById(R.id.textViewVenueName);
            textVenueAddress = itemView.findViewById(R.id.textViewVenueAddress);
            textVenueStatus = itemView.findViewById(R.id.textViewVenueStatus);
            textVenueRating = itemView.findViewById(R.id.textViewVenueRating);
            textVenueRatingCount = itemView.findViewById(R.id.textViewVenueRatingCount);
            textVenueDistance = itemView.findViewById(R.id.textViewVenueDistance);
            mapLink = itemView.findViewById(R.id.mapLink);
            imageBroken = itemView.findViewById(R.id.imageBroken);
            imageBrokenText = itemView.findViewById(R.id.textViewBrokenImage);
        }
    }

    @NonNull
    @Override
    public VenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.venue_item,
                parent, false);
        VenueViewHolder venueViewHolder = new VenueViewHolder(view);
        context = parent.getContext();
        return venueViewHolder;
    }

    public VenueAdapter (ArrayList<Venue> listOfVenues) {
        venueList = listOfVenues;
    }

    @Override
    public void onBindViewHolder(@NonNull VenueViewHolder holder, int position) {
        Venue currentVenue = venueList.get(position);

        holder.imageVenue.setImageResource(0);

        String venueImage = currentVenue.getVenueImage();
        if (!venueImage.contains("no_image")) {
            holder.imageBroken.setVisibility(View.INVISIBLE);
            holder.imageBrokenText.setVisibility(View.INVISIBLE);
            try {
                new getPhoto(holder.imageVenue).execute(venueImage);
            } catch (Exception e) {
                Toast.makeText(context, "Error displaying some images.", Toast.LENGTH_SHORT).show();
            }
        } else {
            holder.imageVenue.setImageBitmap(null);
            holder.imageBroken.setVisibility(View.VISIBLE);
            holder.imageBrokenText.setVisibility(View.VISIBLE);
        }

        holder.textVenueName.setText(currentVenue.getVenueName());
        System.out.println(currentVenue.getVenueName());
        System.out.println(currentVenue.getVenueImage());
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
            holder.textVenueRatingCount.setText(String.format("(%d)", (int) venueRatingCount));
        }

        double venueDistance = currentVenue.getVenueDistance();
        String distanceString;
        if (venueDistance >= 1000) {
            venueDistance = venueDistance / 1000;
            distanceString = String.format(" %.2fkm away ", venueDistance);
        } else {
            distanceString = String.format(" %dm away ", (int) venueDistance);
        }
        textDistance = (String) holder.textVenueDistance.getText();
        holder.textVenueDistance.setText(distanceString);

        StringBuilder urlBuilder = new StringBuilder("https://maps.google.com/?q=");
        urlBuilder.append(currentVenue.getVenueName());
        final String mapUrl = urlBuilder.toString();

        holder.mapLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent redirect = new Intent();
                redirect.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                redirect.setAction(Intent.ACTION_VIEW);
                redirect.addCategory(Intent.CATEGORY_BROWSABLE);
                redirect.setData(Uri.parse(mapUrl));
                context.startActivity(redirect);
            }
        });
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
                inputStream.close();
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
