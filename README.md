# Smart Pantry Manager

A Java Android application for managing leftover pantry ingredients and suggesting recipes only when every required ingredient is available in a sufficient quantity.

## Technology
- Java
- Android Studio
- SQLite via SQLiteOpenHelper
- RecyclerView with custom adapters
- Android Intents
- SharedPreferences for the settings toggle

## Main features
1. Add pantry ingredients.
2. View saved pantry ingredients.
3. Edit ingredients.
4. Delete ingredients.
5. Store optional expiry dates.
6. Suggest recipes using strict all-ingredients matching.
7. Handle basic unit aliases and conversions such as kg/g and L/ml.
8. Handle simple singular/plural ingredient names.
9. Open a recipe detail screen.
10. Persist pantry data after closing/reopening the application.
11. Toggle expiry alerts in Settings.

## Database
SQLite was selected because the application is local, requires relational tables, needs full CRUD, and does not require an internet connection.

Tables:
- pantry
- recipes
- recipe_ingredients

## How to open
1. Extract the ZIP.
2. Open Android Studio.
3. Select **Open**.
4. Select the `SmartPantryManager` folder.
5. Allow Gradle to sync.
6. Create or select an Android emulator.
7. Run the `app` configuration.

## Suggested manual test
Add:
- Chicken = 100 g
- Rice = 200 g
- Onion = 1 piece

Chicken Rice requires 300 g chicken, 200 g rice and 1 onion, so it must NOT appear.

Edit Chicken to 300 g. Chicken Rice should now appear.

This demonstrates the strict matching rule: a recipe is suggested only when every required ingredient is present in at least the required quantity.

