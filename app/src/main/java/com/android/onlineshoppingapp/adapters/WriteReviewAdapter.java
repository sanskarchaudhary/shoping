package com.android.onlineshoppingapp.adapters;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.onlineshoppingapp.R;
import com.android.onlineshoppingapp.models.Product;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WriteReviewAdapter extends RecyclerView.Adapter<WriteReviewAdapter.WriteReviewViewHolder> {

    private final List<Product> productList;
    private String orderId;


    public WriteReviewAdapter(List<Product> reviewList) {
        this.productList = reviewList;
    }

    public WriteReviewAdapter(List<Product> productList, String orderId) {
        this.productList = productList;
        this.orderId = orderId;
    }

    @NonNull
    @Override
    public WriteReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_write_review, parent, false);
        return new WriteReviewViewHolder(view);
    }

    @SuppressLint({"SimpleDateFormat", "NotifyDataSetChanged"})
    @Override
    public void onBindViewHolder(@NonNull WriteReviewViewHolder holder, int position) {
        Product product = productList.get(position);

        if (product == null)
            return;

        AsyncTask.execute(() -> {
            FirebaseFirestore.getInstance()
                    .collection("productImages")
                    .document(product.getProductId())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            List<String> url = (List<String>) documentSnapshot.getData().get("url");
                            assert url != null;
                            Glide.with(holder.itemView.getContext())
                                    .load(url.get(0)).into(holder.ivProductImageReview);
                        } else holder.ivProductImageReview.setImageResource(R.drawable.logoapp);
                    });
        });

        if (product.getProductName().length() > 50) {
            holder.tvProductName.setText(String.format("%s...", product.getProductName().substring(0, 50)));
        } else {
            holder.tvProductName.setText(product.getProductName());
        }

        holder.tvCategory.setText(String.format("Loại: %s", product.getCategory()));


        holder.btnSubmit.setOnClickListener(view -> {
            float ratePoint = holder.ratingBar.getRating();
            String content = holder.etWriteReview.getText().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("reviewer", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
            map.put("rate", ratePoint);
            map.put("content", content);
            map.put("createdDate", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
            FirebaseFirestore.getInstance()
                    .collection("Products")
                    .document(product.getProductId())
                    .collection("productRates")
                    .add(map)
                    .addOnSuccessListener(documentReference -> {
                        //set isRated to true
                        setIsRated(productList.get(position), orderId);

                        //set product rate
                        FirebaseFirestore.getInstance()
                                .collection("Products")
                                .document(product.getProductId())
                                .collection("productRates")
                                .get()
                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                    if (!queryDocumentSnapshots.isEmpty()) {
                                        Double total = (double) 0;
                                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                                            total += documentSnapshot.getDouble("rate");
                                        }
                                        total = total / queryDocumentSnapshots.size();
                                        Map<String, Object> map1 = new HashMap<>();
                                        map1.put("rate", total);
                                        FirebaseFirestore.getInstance()
                                                .collection("Products")
                                                .document(product.getProductId())
                                                .set(map1, SetOptions.merge())
                                                .addOnFailureListener(e -> Log.e("set product rate", Objects.requireNonNull(e.getMessage())));
                                    }
                                });

                        productList.remove(position);
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("submit review", Objects.requireNonNull(e.getMessage()));
                        Toast.makeText(view.getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
                    });
//                Toast.makeText(view.getContext(), "Gửi đánh giá", Toast.LENGTH_SHORT).show();
        });

    }

    private void setIsRated(Product product, String orderId) {
        Map<String, Object> map = new HashMap<>();
        map.put("isRated", true);
        FirebaseFirestore.getInstance()
                .collection("Orders")
                .document(orderId)
                .collection("Products")
                .document(product.getProductId())
                .set(map, SetOptions.merge())
                .addOnFailureListener(e -> Log.e("setIsRated", Objects.requireNonNull(e.getMessage())));
    }


    @Override
    public int getItemCount() {
        if (productList.isEmpty())
            return 0;
        return productList.size();
    }

    static class WriteReviewViewHolder extends RecyclerView.ViewHolder {

        private final Button btnSubmit;
        private final TextView tvProductName, tvCategory;
        private final EditText etWriteReview;
        private final RatingBar ratingBar;
        private final ImageView ivProductImageReview;

        public WriteReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProductName = itemView.findViewById(R.id.tvProductNameReview);
            tvCategory = itemView.findViewById(R.id.tvCategoryReview);
            etWriteReview = itemView.findViewById(R.id.etWriteReview);
            btnSubmit = itemView.findViewById(R.id.btnWriteReview);
            ratingBar = itemView.findViewById(R.id.ratingWriteReview);
            ivProductImageReview = itemView.findViewById(R.id.ivProductImageReview);
        }
    }

}
