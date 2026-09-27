package za.co.smartpantry.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import za.co.smartpantry.model.PantryItem;
import za.co.smartpantry.model.Recipe;
import za.co.smartpantry.model.RecipeIngredient;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE pantry (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "quantity REAL NOT NULL," +
                "unit TEXT NOT NULL," +
                "expiry_date TEXT)");

        db.execSQL("CREATE TABLE recipes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "steps TEXT NOT NULL)");

        db.execSQL("CREATE TABLE recipe_ingredients (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "recipe_id INTEGER NOT NULL," +
                "name TEXT NOT NULL," +
                "quantity REAL NOT NULL," +
                "unit TEXT NOT NULL," +
                "FOREIGN KEY(recipe_id) REFERENCES recipes(id) ON DELETE CASCADE)");

        seedRecipes(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS recipe_ingredients");
        db.execSQL("DROP TABLE IF EXISTS recipes");
        db.execSQL("DROP TABLE IF EXISTS pantry");
        onCreate(db);
    }

    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_date", item.getExpiryDate());
        return db.insert("pantry", null, v);
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("name", item.getName());
        v.put("quantity", item.getQuantity());
        v.put("unit", item.getUnit());
        v.put("expiry_date", item.getExpiryDate());
        return db.update("pantry", v, "id=?", new String[]{String.valueOf(item.getId())});
    }

    public int deletePantryItem(int id) {
        return getWritableDatabase().delete("pantry", "id=?",
                new String[]{String.valueOf(id)});
    }

    public PantryItem getPantryItem(int id) {
        Cursor c = getReadableDatabase().query("pantry", null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            return c.moveToFirst() ? pantryFromCursor(c) : null;
        } finally {
            c.close();
        }
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> result = new ArrayList<>();
        Cursor c = getReadableDatabase().query("pantry", null, null, null,
                null, null, "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) result.add(pantryFromCursor(c));
        } finally {
            c.close();
        }
        return result;
    }

    private PantryItem pantryFromCursor(Cursor c) {
        return new PantryItem(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getDouble(c.getColumnIndexOrThrow("quantity")),
                c.getString(c.getColumnIndexOrThrow("unit")),
                c.getString(c.getColumnIndexOrThrow("expiry_date"))
        );
    }

    public Recipe getRecipe(int recipeId) {
        Cursor c = getReadableDatabase().query("recipes", null, "id=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        try {
            if (!c.moveToFirst()) return null;
            Recipe r = new Recipe(
                    c.getInt(c.getColumnIndexOrThrow("id")),
                    c.getString(c.getColumnIndexOrThrow("name")),
                    c.getString(c.getColumnIndexOrThrow("steps"))
            );
            r.setIngredients(getRecipeIngredients(recipeId));
            return r;
        } finally {
            c.close();
        }
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> result = new ArrayList<>();
        Cursor c = getReadableDatabase().query("recipes", null, null, null,
                null, null, "name COLLATE NOCASE ASC");
        try {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                Recipe r = new Recipe(id,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("steps")));
                r.setIngredients(getRecipeIngredients(id));
                result.add(r);
            }
        } finally {
            c.close();
        }
        return result;
    }

    public List<RecipeIngredient> getRecipeIngredients(int recipeId) {
        List<RecipeIngredient> result = new ArrayList<>();
        Cursor c = getReadableDatabase().query("recipe_ingredients", null,
                "recipe_id=?", new String[]{String.valueOf(recipeId)},
                null, null, "id ASC");
        try {
            while (c.moveToNext()) {
                result.add(new RecipeIngredient(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getInt(c.getColumnIndexOrThrow("recipe_id")),
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getDouble(c.getColumnIndexOrThrow("quantity")),
                        c.getString(c.getColumnIndexOrThrow("unit"))
                ));
            }
        } finally {
            c.close();
        }
        return result;
    }

    /**
     * Strict matching:
     * every recipe ingredient must exist in the pantry with enough quantity.
     * Partial or almost matches are excluded.
     */
    public List<Recipe> getStrictSuggestedRecipes() {
        List<PantryItem> pantry = getAllPantryItems();
        List<Recipe> suggestions = new ArrayList<>();

        for (Recipe recipe : getAllRecipes()) {
            boolean matches = true;
            for (RecipeIngredient required : recipe.getIngredients()) {
                PantryItem available = findMatchingPantry(pantry, required.getName());
                if (available == null ||
                        !hasEnoughQuantity(available, required.getQuantity(), required.getUnit())) {
                    matches = false;
                    break;
                }
            }
            if (matches) suggestions.add(recipe);
        }
        return suggestions;
    }

    private PantryItem findMatchingPantry(List<PantryItem> pantry, String requiredName) {
        String target = normaliseIngredient(requiredName);
        for (PantryItem item : pantry) {
            if (normaliseIngredient(item.getName()).equals(target)) return item;
        }
        return null;
    }

    public static String normaliseIngredient(String value) {
        String s = value == null ? "" : value.trim().toLowerCase(Locale.US);
        if (s.endsWith("ies") && s.length() > 3) return s.substring(0, s.length() - 3) + "y";
        if (s.endsWith("oes") && s.length() > 3) return s.substring(0, s.length() - 2);
        if (s.endsWith("es") && s.length() > 3) return s.substring(0, s.length() - 2);
        if (s.endsWith("s") && s.length() > 2) return s.substring(0, s.length() - 1);
        return s;
    }

    private boolean hasEnoughQuantity(PantryItem available, double required, String requiredUnit) {
        String a = normaliseUnit(available.getUnit());
        String r = normaliseUnit(requiredUnit);

        if (a.equals(r)) return available.getQuantity() + 1e-9 >= required;

        if (isMass(a) && isMass(r)) {
            double availableGrams = a.equals("kg") ? available.getQuantity() * 1000 : available.getQuantity();
            double requiredGrams = r.equals("kg") ? required * 1000 : required;
            return availableGrams + 1e-9 >= requiredGrams;
        }

        if (isVolume(a) && isVolume(r)) {
            double availableMl = a.equals("l") ? available.getQuantity() * 1000 : available.getQuantity();
            double requiredMl = r.equals("l") ? required * 1000 : required;
            return availableMl + 1e-9 >= requiredMl;
        }

        if (isCount(a) && isCount(r)) {
            return available.getQuantity() + 1e-9 >= required;
        }

        return false;
    }

    private String normaliseUnit(String unit) {
        String u = unit == null ? "" : unit.trim().toLowerCase(Locale.US);
        Map<String, String> aliases = new HashMap<>();
        aliases.put("grams", "g"); aliases.put("gram", "g");
        aliases.put("kilograms", "kg"); aliases.put("kilogram", "kg");
        aliases.put("millilitres", "ml"); aliases.put("millilitre", "ml");
        aliases.put("milliliters", "ml"); aliases.put("milliliter", "ml");
        aliases.put("litres", "l"); aliases.put("litre", "l");
        aliases.put("liters", "l"); aliases.put("liter", "l");
        aliases.put("pieces", "piece"); aliases.put("pcs", "piece");
        aliases.put("piece", "piece"); aliases.put("items", "piece"); aliases.put("item", "piece");
        return aliases.getOrDefault(u, u);
    }

    private boolean isMass(String u) { return u.equals("g") || u.equals("kg"); }
    private boolean isVolume(String u) { return u.equals("ml") || u.equals("l"); }
    private boolean isCount(String u) { return u.equals("piece") || u.equals("unit") || u.equals("units"); }

    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Tomato Pasta",
                "Boil pasta. Sauté tomato and garlic. Combine with pasta and serve.",
                new String[][]{{"pasta","200","g"},{"tomato","2","piece"},{"garlic","1","piece"}});
        addRecipe(db, "Egg Sandwich",
                "Cook the eggs, toast bread and assemble the sandwich with eggs and tomato.",
                new String[][]{{"egg","2","piece"},{"bread","2","piece"},{"tomato","1","piece"}});
        addRecipe(db, "Chicken Rice",
                "Cook rice. Stir-fry chicken with onion and combine with the rice.",
                new String[][]{{"chicken","300","g"},{"rice","200","g"},{"onion","1","piece"}});
        addRecipe(db, "Vegetable Omelette",
                "Beat eggs, add chopped vegetables and cook in a pan until set.",
                new String[][]{{"egg","2","piece"},{"onion","1","piece"},{"tomato","1","piece"}});
        addRecipe(db, "Tuna Pasta",
                "Boil pasta and mix with tuna, tomato and onion.",
                new String[][]{{"pasta","200","g"},{"tuna","1","piece"},{"tomato","1","piece"},{"onion","1","piece"}});
        addRecipe(db, "Chicken Wrap",
                "Cook chicken, add tomato and lettuce, then wrap in a tortilla.",
                new String[][]{{"chicken","200","g"},{"tortilla","2","piece"},{"tomato","1","piece"},{"lettuce","1","piece"}});
        addRecipe(db, "Rice and Beans",
                "Cook rice and combine with cooked beans, tomato and onion.",
                new String[][]{{"rice","200","g"},{"beans","200","g"},{"tomato","1","piece"},{"onion","1","piece"}});
        addRecipe(db, "Garlic Toast",
                "Mix garlic with butter, spread on bread and toast until crisp.",
                new String[][]{{"bread","2","piece"},{"garlic","1","piece"},{"butter","20","g"}});
        addRecipe(db, "Chicken Stir Fry",
                "Stir-fry chicken and vegetables, then season and serve hot.",
                new String[][]{{"chicken","250","g"},{"onion","1","piece"},{"carrot","1","piece"},{"pepper","1","piece"}});
        addRecipe(db, "French Toast",
                "Dip bread in beaten egg and milk, then fry until golden.",
                new String[][]{{"bread","2","piece"},{"egg","2","piece"},{"milk","100","ml"}});
        addRecipe(db, "Tomato Omelette",
                "Beat eggs with tomato and onion, then cook in a lightly oiled pan.",
                new String[][]{{"egg","2","piece"},{"tomato","1","piece"},{"onion","1","piece"}});
        addRecipe(db, "Bean Wrap",
                "Warm beans, add tomato and lettuce, then roll in tortillas.",
                new String[][]{{"beans","150","g"},{"tortilla","2","piece"},{"tomato","1","piece"},{"lettuce","1","piece"}});
        addRecipe(db, "Tuna Sandwich",
                "Mix tuna with butter, place on bread and add lettuce.",
                new String[][]{{"tuna","1","piece"},{"bread","2","piece"},{"butter","20","g"},{"lettuce","1","piece"}});
        addRecipe(db, "Chicken Pasta",
                "Cook pasta and chicken, then combine with tomato and garlic.",
                new String[][]{{"pasta","200","g"},{"chicken","200","g"},{"tomato","1","piece"},{"garlic","1","piece"}});
        addRecipe(db, "Vegetable Rice",
                "Cook rice and stir-fry it with onion, carrot and pepper.",
                new String[][]{{"rice","200","g"},{"onion","1","piece"},{"carrot","1","piece"},{"pepper","1","piece"}});
        addRecipe(db, "Egg Rice",
                "Stir-fry cooked rice with eggs and onion.",
                new String[][]{{"rice","200","g"},{"egg","2","piece"},{"onion","1","piece"}});
        addRecipe(db, "Garlic Pasta",
                "Boil pasta and toss with garlic and butter.",
                new String[][]{{"pasta","200","g"},{"garlic","1","piece"},{"butter","20","g"}});
        addRecipe(db, "Chicken Sandwich",
                "Cook chicken, place it on toasted bread with lettuce and tomato.",
                new String[][]{{"chicken","150","g"},{"bread","2","piece"},{"lettuce","1","piece"},{"tomato","1","piece"}});
        addRecipe(db, "Tomato Rice",
                "Cook rice with tomato and onion until tender and fragrant.",
                new String[][]{{"rice","200","g"},{"tomato","2","piece"},{"onion","1","piece"}});
        addRecipe(db, "Milk Omelette",
                "Whisk eggs with milk and cook gently until set.",
                new String[][]{{"egg","2","piece"},{"milk","50","ml"},{"butter","10","g"}});
    }

    private void addRecipe(SQLiteDatabase db, String name, String steps, String[][] ingredients) {
        ContentValues recipe = new ContentValues();
        recipe.put("name", name);
        recipe.put("steps", steps);
        long recipeId = db.insert("recipes", null, recipe);

        for (String[] item : ingredients) {
            ContentValues v = new ContentValues();
            v.put("recipe_id", recipeId);
            v.put("name", item[0]);
            v.put("quantity", Double.parseDouble(item[1]));
            v.put("unit", item[2]);
            db.insert("recipe_ingredients", null, v);
        }
    }
}
