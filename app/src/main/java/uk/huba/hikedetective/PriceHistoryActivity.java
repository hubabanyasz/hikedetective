package uk.huba.hikedetective;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class PriceHistoryActivity extends AppCompatActivity {
    private static final String TAG = "PriceHistoryActivity";

    // Dynamic price history list
    private RecyclerView recyclerView;
    private PriceHistoryAdapter priceHistoryAdapter;
    private ArrayList<PriceHistoryModel> priceHistoryArrayList;

    // UI elements
    private CoordinatorLayout coordinatorLayout;

    // Product details
    private String productId;
    private String productQuantity;
    private String productUnit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_price_history);

        // Enable "back" button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set title
        setTitle("Price History");

        // Init views
        coordinatorLayout = findViewById(R.id.coordinatorLayout_activity_price_history);
        recyclerView = findViewById(R.id.recyclerView_activity_price_history);

        // Get product ID from intent
        Intent intentReceived = getIntent();

        if (intentReceived.getExtras() != null) {
            productId = intentReceived.getStringExtra("productId");
            productQuantity = intentReceived.getStringExtra("productQuantity");
            productUnit = intentReceived.getStringExtra("productUnit");
        }

        // ArrayList for price data from database
        priceHistoryArrayList = new ArrayList<>();

        // Get data from database
        readPriceHistory();

        // Create adapter with the data from the database
        priceHistoryAdapter = new PriceHistoryAdapter(this, priceHistoryArrayList, coordinatorLayout);

        // Set the ProductAdapter as the adapter for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(priceHistoryAdapter);

        // Create a DividerItemDecoration and set it as the item decorator
        DividerItemDecoration itemDecoration = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(itemDecoration);
    }

    public void readPriceHistory() {
        // Get product details
        Cursor cursor = MainActivity.sqLiteHelper.readProductPrices(productId);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String priceId = cursor.getString(0);
                String productPrice = cursor.getString(1);
                String productStore = cursor.getString(2);
                String timestamp = cursor.getString(3);

                priceHistoryArrayList.add(new PriceHistoryModel(priceId, productPrice, productQuantity, productUnit, productStore, timestamp));
            }
            cursor.close();
        }
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