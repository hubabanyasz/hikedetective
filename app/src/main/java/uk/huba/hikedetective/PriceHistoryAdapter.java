package uk.huba.hikedetective;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

public class PriceHistoryAdapter extends RecyclerView.Adapter<PriceHistoryAdapter.PriceHistoryViewHolder> {

    private final ArrayList<PriceHistoryModel> priceHistoryArrayList;
    private CoordinatorLayout coordinatorLayout;

    protected PriceHistoryAdapter(Context context, ArrayList<PriceHistoryModel> priceHistoryModelArrayList, CoordinatorLayout coordinatorLayout) {
        this.priceHistoryArrayList = priceHistoryModelArrayList;
        this.coordinatorLayout = coordinatorLayout;
    }

    @NonNull
    @Override
    public PriceHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_price_history, parent, false);
        return new PriceHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PriceHistoryAdapter.PriceHistoryViewHolder holder, int position) {
        PriceHistoryModel model = priceHistoryArrayList.get(position);

        if (model.getProductStore() != null && !model.getProductStore().isEmpty()) {
            // If a store value is assigned, set the text and make the view visible
            holder.storeText.setText(model.getProductStore());
        } else {
            // If no store value is assigned, hide the view
            holder.storeText.setVisibility(View.GONE);
        }

        holder.timestampText.setText(model.getProductTimestamp());

        // Format price details
        String priceDetails = String.format("£%s / %s %s", model.getProductPrice(), model.getProductQuantity(), model.getProductUnit());
        holder.priceText.setText(priceDetails);
    }

    protected class PriceHistoryViewHolder extends RecyclerView.ViewHolder {
        public TextView priceText;
        public TextView storeText;
        public TextView timestampText;
        public ImageButton removeButton;

        protected PriceHistoryViewHolder(View view) {
            super(view);
            priceText = view.findViewById(R.id.priceTextView_layout_price_history);
            storeText = view.findViewById(R.id.storeTextView_layout_price_history);
            timestampText = view.findViewById(R.id.timestampTextView_layout_price_history);
            removeButton = view.findViewById(R.id.removeImageButton_layout_price_history);

            removeButton.setOnClickListener(v -> {
                // Check if the last price is being removed
                if (getItemCount() == 1) {
                    // Show a Snackbar to inform the user that the last price cannot be removed
                    Snackbar.make(coordinatorLayout, "Cannot remove the last price", Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Get the position of the item that was clicked
                int position = getAdapterPosition();

                // Retrieve corresponding price ID
                PriceHistoryModel model = priceHistoryArrayList.get(position);
                String priceId = model.getPriceId();

                // Remove the price from the ArrayList
                priceHistoryArrayList.remove(position);

                // Notify the adapter that the item has been removed
                notifyItemRemoved(position);

                // Remove the price from the database
                MainActivity.sqLiteHelper.deletePrice(priceId);

                // Show a Snackbar to confirm that the price has been removed
                Snackbar.make(coordinatorLayout, "Price removed", Snackbar.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return priceHistoryArrayList.size();
    }
}
