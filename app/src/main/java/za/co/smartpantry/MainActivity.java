package za.co.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import za.co.smartpantry.adapter.PantryAdapter;
import za.co.smartpantry.data.DatabaseHelper;
import za.co.smartpantry.model.PantryItem;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView emptyState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        emptyState = findViewById(R.id.tvEmptyPantry);

        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new ArrayList<>(), new PantryAdapter.Listener() {
            @Override public void onEdit(PantryItem item) {
                Intent i = new Intent(MainActivity.this, AddEditIngredientActivity.class);
                i.putExtra("id", item.getId());
                startActivity(i);
            }

            @Override public void onDelete(PantryItem item) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete ingredient")
                        .setMessage("Remove " + item.getName() + " from the pantry?")
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Delete", (d, w) -> {
                            db.deletePantryItem(item.getId());
                            loadPantry();
                        }).show();
            }
        });
        recycler.setAdapter(adapter);

        Button add = findViewById(R.id.btnAddIngredient);
        Button suggestions = findViewById(R.id.btnSuggestions);
        Button settings = findViewById(R.id.btnSettings);

        add.setOnClickListener(v ->
                startActivity(new Intent(this, AddEditIngredientActivity.class)));
        suggestions.setOnClickListener(v ->
                startActivity(new Intent(this, SuggestedRecipesActivity.class)));
        settings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        adapter.replaceData(db.getAllPantryItems());
        emptyState.setVisibility(adapter.getItemCount() == 0 ? TextView.VISIBLE : TextView.GONE);
    }
}
