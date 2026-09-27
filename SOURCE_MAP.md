# Source Map

MainActivity.java
- Main pantry screen
- RecyclerView setup
- Add/Edit/Delete navigation
- Suggestions and Settings navigation

AddEditIngredientActivity.java
- Add and edit form
- Input validation
- SQLite CRUD calls

SuggestedRecipesActivity.java
- Strict recipe suggestions
- Empty-state handling
- Navigation to recipe details

RecipeDetailActivity.java
- Displays required ingredients and preparation steps

SettingsActivity.java
- Stores expiry-alert preference

DatabaseHelper.java
- SQLite schema
- Seed data
- Pantry CRUD
- Recipe retrieval
- Strict matching and unit conversion

PantryAdapter.java
- Custom RecyclerView adapter for pantry rows

RecipeAdapter.java
- Custom RecyclerView adapter for recipe rows
