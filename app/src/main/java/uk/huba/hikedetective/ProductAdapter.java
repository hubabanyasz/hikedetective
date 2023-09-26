package uk.huba.hikedetective;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final ArrayList<ProductModel> productArrayList;
    private MainActivity mainActivity;

    protected ProductAdapter(Context context, ArrayList<ProductModel> productModelArrayList, MainActivity mainActivity) {
        this.productArrayList = productModelArrayList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductAdapter.ProductViewHolder holder, int position) {
        ProductModel model = productArrayList.get(position);

        holder.nameText.setText(model.getProductName());
        holder.latestStoreText.setText(model.getLatestProductStore());
        holder.latestTimeStampText.setText(model.getLatestProductTimestamp());

        // Combine price, quantity, and unit into one string
        String priceDetails = String.format("£%s / %s %s", model.getLatestProductPrice(), model.getProductQuantity(), model.getProductUnit());
        holder.latestPriceText.setText(priceDetails);
    }

    protected class ProductViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView nameText;
        public TextView latestStoreText;
        public TextView latestTimeStampText;
        public TextView latestPriceText;

        protected ProductViewHolder(View view) {
            super(view);
            nameText = view.findViewById(R.id.nameTextView_layout_product);
            latestStoreText = view.findViewById(R.id.latestStoreTextView_layout_product);
            latestTimeStampText = view.findViewById(R.id.latestDateTextView_layout_product);
            latestPriceText = view.findViewById(R.id.latestPriceTextView_layout_product);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int productId = productArrayList.get(getLayoutPosition()).getProductId();
            String stringId = Integer.toString(productId);

            Intent intent = new Intent(view.getContext(), ProductActivity.class);
            intent.putExtra("productId", stringId);

            // Start ProductActivity
            view.getContext().startActivity(intent);

            // Finish MainActivity to prevent old data from being displayed
            mainActivity.finish();
        }
    }

    @Override
    public int getItemCount() {
        return productArrayList.size();
    }
}
