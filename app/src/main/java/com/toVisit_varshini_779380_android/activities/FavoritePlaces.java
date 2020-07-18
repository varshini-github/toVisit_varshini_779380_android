package com.toVisit_varshini_779380_android.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.toVisit_varshini_779380_android.adapters.FavoritePlacesAdapter;
import com.toVisit_varshini_779380_android.models.FavoritePlacesModel;
import com.toVisit_varshini_779380_android.R;

import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FavoritePlaces extends AppCompatActivity {
    ArrayList<FavoritePlacesModel> favoritePlaces;
    RecyclerView recyclerView;
    FavoritePlacesAdapter favoritePlacesAdapter;
    SQLiteDatabase sqLiteDatabase;
    ArrayList<String> title, lat, lon;
    ArrayList<Integer> icons;
    ImageView waiting;
    TextView waitingText;
    FloatingActionButton floatingActionButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_places);

        getSupportActionBar().setTitle("Favorite Places");

        sqLiteDatabase = this.openOrCreateDatabase("UserDB", MODE_PRIVATE, null);

        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS FavoritePlaces (Title VARCHAR, Latitude VARCHAR, Longitude VARCHAR, Visited VARCHAR);");

        recyclerView = findViewById(R.id.recyclerView);
        waiting = findViewById(R.id.waiting);
        waitingText = findViewById(R.id.waitingText);
        floatingActionButton = findViewById(R.id.fab);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavoritePlaces.this, MapsActivity.class));
            }
        });


    }

    private void delete(int position) {
        sqLiteDatabase.execSQL("DELETE FROM FavoritePlaces WHERE Latitude = '" + lat.get(position) + "' AND Longitude = '" + lon.get(position) + "';");


        favoritePlacesAdapter.removeItem(position);
        title.remove(position);
        lat.remove(position);
        lon.remove(position);
        icons.remove(position);

        Toast.makeText(this, "Location deleted successfully", Toast.LENGTH_SHORT).show();
        if (favoritePlacesAdapter.getItemCount() == 0) {
            waitingText.setVisibility(View.VISIBLE);
            waiting.setVisibility(View.VISIBLE);
        }
    }

    private void update(int position) {
        startActivity(new Intent(FavoritePlaces.this, UpdateFavoriteLocation.class)
                .putExtra("Latitude", lat.get(position))
                .putExtra("Longitude", lon.get(position)));
    }

    private void fetchPlaces() {
        favoritePlaces = new ArrayList<>();
        title = new ArrayList<>();
        lat = new ArrayList<>();
        lon = new ArrayList<>();
        icons = new ArrayList<>();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM " + "FavoritePlaces", null);
        int latColumn = cursor.getColumnIndex("Latitude");
        int lonColumn = cursor.getColumnIndex("Longitude");
        int titleColumn = cursor.getColumnIndex("Title");
        int visitedColumn = cursor.getColumnIndex("Visited");

        cursor.moveToFirst();

        if (cursor.getCount() != 0) {
            do {
                title.add(cursor.getString(titleColumn));
                lat.add(cursor.getString(latColumn));
                lon.add(cursor.getString(lonColumn));
                if (cursor.getString(visitedColumn).equals("0")) {
                    icons.add(R.drawable.ic_baseline_favorite_24);
                } else {
                    icons.add(R.drawable.ic_check);
                }
            } while (cursor.moveToNext());
        }
        for (int i = 0; i < title.size(); i++) {
            waitingText.setVisibility(View.GONE);
            waiting.setVisibility(View.GONE);
            favoritePlaces.add(new FavoritePlacesModel(title.get(i), lat.get(i), lon.get(i), icons.get(i)));
        }
        if (title.size() == 0) {
            waiting.setVisibility(View.VISIBLE);
            waitingText.setVisibility(View.VISIBLE);
        } else {
            favoritePlacesAdapter = new FavoritePlacesAdapter(this, favoritePlaces);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(favoritePlacesAdapter);

            ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
                @Override
                public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                    return false;
                }

                @Override
                public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    if (direction == ItemTouchHelper.LEFT) {
                        delete(viewHolder.getAdapterPosition());
                    } else if (direction == ItemTouchHelper.RIGHT) {
                        update(viewHolder.getAdapterPosition());
                    }
                }

                @Override
                public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                    new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeLeftBackgroundColor(ContextCompat.getColor(FavoritePlaces.this, R.color.colorDeleteBackground))
                            .addSwipeLeftActionIcon(R.drawable.ic_delete)
                            .addSwipeRightBackgroundColor(ContextCompat.getColor(FavoritePlaces.this, R.color.colorUpdateBackground))
                            .addSwipeRightActionIcon(R.drawable.ic_update)
                            .addSwipeRightLabel("Update")
                            .setSwipeRightLabelColor(Color.WHITE)
                            .addSwipeLeftLabel("Delete")
                            .setSwipeLeftLabelColor(Color.WHITE)
                            .create()
                            .decorate();
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                }
            };
            ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
            itemTouchHelper.attachToRecyclerView(recyclerView);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPlaces();
    }
}