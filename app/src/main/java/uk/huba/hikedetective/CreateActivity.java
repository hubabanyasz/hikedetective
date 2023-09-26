package uk.huba.hikedetective;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class CreateActivity extends AppCompatActivity {
    private static final String TAG = "CreateActivity";

    // UI elements
    private TextInputEditText nameEditText;
    private TextInputEditText priceEditText;
    private TextInputEditText quantityEditText;
    private TextInputEditText unitEditText;
    private TextInputEditText storeEditText;
    private Button saveButton;

    private String barcodeValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);

        // Enable "back" button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set title
        setTitle("Create Product");

        // Init views
        nameEditText = findViewById(R.id.nameTextInputEditText_activity_create);
        priceEditText = findViewById(R.id.priceTextInputEditText_activity_create);
        quantityEditText = findViewById(R.id.quantityTextInputEditText_activity_create);
        unitEditText = findViewById(R.id.unitTextInputEditText_activity_create);
        storeEditText = findViewById(R.id.storeTextInputEditText_activity_create);

        // Handle the intent that started this activity
        Intent intent = getIntent();

        // Get the barcode value from the intent
        if (intent.getExtras() != null) {
            barcodeValue = intent.getStringExtra("barcodeValue");
        }

        saveButton = findViewById(R.id.saveButton_activity_create);
        saveButton.setOnClickListener(v -> {
            createProduct();
        });
    }

    private void createProduct() {
        // Get and sanitise user input
        String productName = getUserInput(nameEditText, "Name");
        String productPrice = getUserInput(priceEditText, "Price");
        String productQuantity = getUserInput(quantityEditText, "Quantity");
        String productUnit = getUserInput(unitEditText, "Unit");
        String productStore = getUserInput(storeEditText, "Store");

        // Make sure that all required fields are filled
        if (productName.isEmpty() || productPrice.isEmpty() || productQuantity.isEmpty() || productUnit.isEmpty()) {
            return;
        }

        // Create product
        long productId = MainActivity.sqLiteHelper.createProduct(productName, productPrice, productQuantity, productUnit, productStore, barcodeValue);

        // Start ProductActivity to show the created product
        Intent intent = new Intent(CreateActivity.this, ProductActivity.class);
        String stringId = String.valueOf(productId);

        intent.putExtra("productId", stringId);
        intent.putExtra("action", "create");

        startActivity(intent);
        finish();
    }

    private String getUserInput(EditText editText, String fieldName) {
        String value = "";

        if (editText.getText() != null && !editText.getText().toString().trim().isEmpty()) {
            value = editText.getText().toString().trim();
        } else if (fieldName.equals("Store")) {
            // Store is optional
        } else {
            editText.setError(fieldName + " is required");
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
        Intent intent = new Intent(this, MainActivity.class);

        startActivity(intent);
        finish();

        super.onBackPressed();
    }
}