package za.co.smartpantry;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import za.co.smartpantry.adapter.RecipeAdapter;
import za.co.smartpantry.data.DatabaseHelper;
import za.co.smartpantry.model.Recipe;

public class RecipesActivity extends AppCompatActivity {

    private RecyclerView recyclerRecipes;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipes);

        recyclerRecipes = findViewById(R.id.recyclerRecipes);

        db = new DatabaseHelper(this);

        List<Recipe> recipes = db.getAllRecipes();

        recyclerRecipes.setLayoutManager(new LinearLayoutManager(this));

        RecipeAdapter adapter = new RecipeAdapter(
                recipes,
                recipe -> {
                    Intent intent = new Intent(
                            RecipesActivity.this,
                            RecipeDetailActivity.class
                    );

                    // Send the recipe ID to RecipeDetailActivity
                    intent.putExtra("recipeId", recipe.getId());

                    startActivity(intent);
                }
        );

        recyclerRecipes.setAdapter(adapter);
    }
}