package com.toVisit_varshini_779380_android.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.toVisit_varshini_779380_android.models.FavoritePlacesModel;
import com.toVisit_varshini_779380_android.R;
import com.toVisit_varshini_779380_android.activities.FavoriteLocationMap;

import java.util.List;

public class FavoritePlacesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<FavoritePlacesModel> favoritePlaces;
    Context context;

    public FavoritePlacesAdapter(Context context, List<FavoritePlacesModel> favoritePlaces) {
        this.favoritePlaces = favoritePlaces;
        this.context = context;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_place, parent, false);
        return new MyViewHolder(v);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final FavoritePlacesModel entity = favoritePlaces.get(position);
        ((MyViewHolder) holder).title.setText(entity.getTitle());
        ((MyViewHolder) holder).icon.setImageDrawable(context.getResources().getDrawable(entity.getIcon()));
        ((MyViewHolder) holder).container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                context.startActivity(new Intent(context, FavoriteLocationMap.class)
                        .putExtra("Title", entity.getTitle())
                        .putExtra("Latitude", entity.getLat())
                        .putExtra("Longitude", entity.getLon()));
            }
        });



    }

    @Override
    public int getItemCount() {
        return favoritePlaces.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        ConstraintLayout container;
        ImageView icon;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            container = itemView.findViewById(R.id.container);
            icon = itemView.findViewById(R.id.icon);
        }
    }

    public void removeItem(int position) {
        favoritePlaces.remove(position);
        notifyItemRemoved(position);
    }
}