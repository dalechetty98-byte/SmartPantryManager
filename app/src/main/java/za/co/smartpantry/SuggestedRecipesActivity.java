package za.co.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import za.co.smartpantry.adapter.RecipeAdapter;
import za.co.smartpantry.data.DatabaseHelper;
import za.co.smartpantry.model.Recipe;

public class SuggestedRecipesActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView empty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);

        db = new DatabaseHelper(this);
        empty = findViewById(R.id.tvNoRecipes);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new java.util.ArrayList<>(),
                recipe -> {
                    Intent i = new Intent(this, RecipeDetailActivity.class);
                    i.putExtra("recipeId", recipe.getId());
                    startActivity(i);
                });
        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> matches = db.getStrictSuggestedRecipes();
        adapter = new RecipeAdapter(matches, recipe -> {
            Intent i = new Intent(this, RecipeDetailActivity.class);
            i.putExtra("recipeId", recipe.getId());
            startActivity(i);
        });
        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setAdapter(adapter);
        empty.setVisibility(matches.isEmpty() ? TextView.VISIBLE : TextView.GONE);
    }
}
