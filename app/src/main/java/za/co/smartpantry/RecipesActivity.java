package za.co.smartpantry;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import za.co.smartpantry.adapter.RecipeAdapter;
import za.co.smartpantry.data.DatabaseHelper;
import za.co.smartpantry.model.Recipe;

public class RecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        db = new DatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerRecipes);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Recipe> recipes = db.getAllRecipes();

        adapter = new RecipeAdapter(recipes, recipe -> {
            // Open recipe details when a recipe is selected
            android.content.Intent intent =
                    new android.content.Intent(RecipesActivity.this, RecipeDetailActivity.class);

            intent.putExtra("recipe_id", recipe.getId());

            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}