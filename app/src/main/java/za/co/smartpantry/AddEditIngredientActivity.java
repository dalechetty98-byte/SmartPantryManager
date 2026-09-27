package za.co.smartpantry;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import za.co.smartpantry.data.DatabaseHelper;
import za.co.smartpantry.model.PantryItem;

public class AddEditIngredientActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private EditText name, quantity, unit, expiry;
    private int itemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);
        name = findViewById(R.id.etName);
        quantity = findViewById(R.id.etQuantity);
        unit = findViewById(R.id.etUnit);
        expiry = findViewById(R.id.etExpiry);
        TextView title = findViewById(R.id.tvFormTitle);

        if (getIntent().hasExtra("id")) {
            itemId = getIntent().getIntExtra("id", -1);
            title.setText("Edit Ingredient");
            PantryItem item = db.getPantryItem(itemId);
            if (item != null) {
                name.setText(item.getName());
                quantity.setText(String.valueOf(item.getQuantity()));
                unit.setText(item.getUnit());
                expiry.setText(item.getExpiryDate());
            }
        }

        Button save = findViewById(R.id.btnSaveIngredient);
        Button cancel = findViewById(R.id.btnCancel);
        save.setOnClickListener(v -> saveIngredient());
        cancel.setOnClickListener(v -> finish());
    }

    private void saveIngredient() {
        String n = name.getText().toString().trim();
        String q = quantity.getText().toString().trim();
        String u = unit.getText().toString().trim();
        String e = expiry.getText().toString().trim();

        if (TextUtils.isEmpty(n)) {
            name.setError("Ingredient name is required");
            name.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(q)) {
            quantity.setError("Quantity is required");
            quantity.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(u)) {
            unit.setError("Unit is required");
            unit.requestFocus();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(q);
        } catch (NumberFormatException ex) {
            quantity.setError("Enter a valid number");
            quantity.requestFocus();
            return;
        }

        if (amount <= 0) {
            quantity.setError("Quantity must be greater than zero");
            quantity.requestFocus();
            return;
        }

        PantryItem item = new PantryItem(itemId, n, amount, u, e);
        if (itemId == -1) db.addPantryItem(item);
        else db.updatePantryItem(item);

        Toast.makeText(this, itemId == -1 ? "Ingredient added" : "Ingredient updated",
                Toast.LENGTH_SHORT).show();
        finish();
    }
}
