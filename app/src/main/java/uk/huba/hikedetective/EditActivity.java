package uk.huba.hikedetective;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class EditActivity extends AppCompatActivity {
    private static final String TAG = "EditActivity";

    // UI elements
    private TextInputEditText nameEditText;
    private TextInputEditText quantityEditText;
    private TextInputEditText unitEditText;
    private Button updateButton;

    // Product data
    private String productId;
    private String productNameContent;
    private String productQuantityContent;
    private String productUnitContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Enable "back" button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set title
        setTitle("Edit Product");

        // Get product data
        Bundle bundle = getIntent().getExtras();
        productId = bundle.getString("productId");
        productNameContent = bundle.getString("productName");
        productQuantityContent = bundle.getString("productQuantity");
        productUnitContent = bundle.getString("productUnit");

        // Get references to EditTexts
        nameEditText = findViewById(R.id.nameTextInputEditText_activity_edit);
        quantityEditText = findViewById(R.id.quantityTextInputEditText_activity_edit);
        unitEditText = findViewById(R.id.unitTextInputEditText_activity_edit);

        // Set hints for EditTexts
        nameEditText.setText(productNameContent);
        quantityEditText.setText(productQuantityContent);
        unitEditText.setText(productUnitContent);

        updateButton = findViewById(R.id.updateButton_activity_edit);
        updateButton.setOnClickListener(v -> {
            saveChanges();
        });
    }

    private void saveChanges() {
        // Get and sanitise user input
        String name = getUserInput(nameEditText, "Name");
        String quantity = getUserInput(quantityEditText, "Quantity");
        String unit = getUserInput(unitEditText, "Unit");

        // Make sure that all required fields are filled
        if (name.isEmpty() || quantity.isEmpty() || unit.isEmpty()) {
            return;
        }

        // Update product
        MainActivity.sqLiteHelper.updateProduct(productId, name, quantity, unit);

        // Start ProductActivity to show the updated product
        Intent intent = new Intent(this, ProductActivity.class);

        intent.putExtra("productId", productId);
        intent.putExtra("action", "update");

        startActivity(intent);
        finish();
    }

    private String getUserInput(EditText editText, String fieldName) {
        String value = "";

        if (editText.getText() != null && !editText.getText().toString().trim().isEmpty()) {
            value = editText.getText().toString().trim();
        } else {
            editText.setError(fieldName + " cannot be empty");
        }
        return value;
    }

    // To make sure that the "back" button on the ActionBar
    // acts the same way as the physical "back" button on the device
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return false;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ProductActivity.class);
        intent.putExtra("productId", productId);

        startActivity(intent);
        finish();

        super.onBackPressed();
    }
}