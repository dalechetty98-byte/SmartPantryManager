package za.co.smartpantry;

    import android.os.Bundle;
    import android.widget.TextView;

    import androidx.appcompat.app.AppCompatActivity;

    import java.util.Locale;

    import za.co.smartpantry.data.DatabaseHelper;
    import za.co.smartpantry.model.Recipe;
    import za.co.smartpantry.model.RecipeIngredient;

    public class RecipeDetailActivity extends AppCompatActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_recipe_detail);

            DatabaseHelper db = new DatabaseHelper(this);
            int recipeId = getIntent().getIntExtra("recipeId", -1);
            Recipe recipe = db.getRecipe(recipeId);

            TextView name = findViewById(R.id.tvDetailName);
            TextView ingredients = findViewById(R.id.tvDetailIngredients);
            TextView steps = findViewById(R.id.tvDetailSteps);

            if (recipe == null) {
                name.setText("Recipe not found");
                return;
            }

            name.setText(recipe.getName());

            StringBuilder list = new StringBuilder();
            for (RecipeIngredient item : recipe.getIngredients()) {
                list.append("• ")
                        .append(String.format(Locale.US, "%.2f", item.getQuantity()))
                        .append(" ").append(item.getUnit())
                        .append(" ").append(item.getName())
                        .append("/n");

            }
            ingredients.setText(list.toString());
            steps.setText(recipe.getSteps());
        }
    }
