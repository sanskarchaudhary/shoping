package com.android.onlineshoppingapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.onlineshoppingapp.R;
import com.android.onlineshoppingapp.models.OrderProduct;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

public class OrderProductAdapter extends RecyclerView.Adapter<OrderProductAdapter.OrderProductViewHolder> {
    private final List<OrderProduct> orderProductList;
    private final Context context;

    public OrderProductAdapter(List<OrderProduct> orderProductList, Context context) {
        this.orderProductList = orderProductList;
        this.context = context;
    }

    @NonNull
    @Override
    public OrderProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_order_product_item, parent, false);
        return new OrderProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderProductViewHolder holder, int position) {
        OrderProduct orderProduct = orderProductList.get(position);
        if (orderProduct == null) return;

        if (orderProduct.getProductName().length() > 45) {
            holder.tvProductName.setText(String.format("%s...", orderProduct.getProductName().substring(0, 45)));
        } else {
            holder.tvProductName.setText(orderProduct.getProductName());
        }
        holder.tvPrice.setText(String.format("₹%,d", orderProduct.getProductPrice()));
        holder.tvNumber.setText(String.format("x%d", orderProduct.getOrderQuantity()));
        holder.tvCategory.setText(orderProduct.getCategory());

        //set image
        DocumentReference imgRef = FirebaseFirestore.getInstance()
                .collection("productImages")
                .document(orderProduct.getProductId());
        imgRef.get().addOnSuccessListener(documentSnapshot -> {
            Map<String, Object> map = documentSnapshot.getData();

            if (map != null) {
                List<String> string = (List<String>) map.get("url");
                assert string != null;
                Glide.with(context)
                        .load(string.get(0)).into(holder.ivProductImg);
            }
            else
                holder.ivProductImg.setImageResource(R.drawable.logoapp);
        });

    }

    @Override
    public int getItemCount() {
        if (orderProductList.isEmpty()) return 0;
        else return orderProductList.size();
    }

    public static class OrderProductViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvProductName;
        private final TextView tvCategory;
        private final TextView tvPrice;
        private final TextView tvNumber;
        private final ImageView ivProductImg;

        public OrderProductViewHolder(@NonNull View itemView) {
            super(itemView);

            tvProductName = itemView.findViewById(R.id.tvProductNameOrder);
            tvCategory = itemView.findViewById(R.id.tvCategoryOrder);
            tvPrice = itemView.findViewById(R.id.tvProductPriceOrder);
            tvNumber = itemView.findViewById(R.id.tvNumberOrder);
            ivProductImg = itemView.findViewById(R.id.ivProductImageOrder);

        }
    }
}
