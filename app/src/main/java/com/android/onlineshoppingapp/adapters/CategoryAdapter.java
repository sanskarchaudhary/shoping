package com.android.onlineshoppingapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.onlineshoppingapp.ListOfProductActivity;
import com.android.onlineshoppingapp.R;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private final List<String> categoryList;
    private final Context context;

    public CategoryAdapter(List<String> categoryList, Context context) {
        this.categoryList = categoryList;
        this.context = context;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {

        holder.tvCateName.setText(categoryList.get(position));

        switch (categoryList.get(position)) {
            case "Skincare":
                holder.ivImage.setImageResource(R.drawable.skincare);
                break;
            case "Haircare":
                holder.ivImage.setImageResource(R.drawable.haircare);
                break;
            case "Oils":
                holder.ivImage.setImageResource(R.drawable.oils);
                break;
            case "Syrup":
                holder.ivImage.setImageResource(R.drawable.syruph);
                break;
            case "Ointment":
                holder.ivImage.setImageResource(R.drawable.oaintment);
                break;
            case "Others":
                holder.ivImage.setImageResource(R.drawable.others);
                break;
        }

        holder.cardCategoryItem.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), ListOfProductActivity.class);
            intent.putExtra("see_more_product", categoryList.get(position));
            view.getContext().startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        if (categoryList.isEmpty())
            return 0;
        return categoryList.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImage;
        private final TextView tvCateName;
        private final MaterialCardView cardCategoryItem;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            ivImage = itemView.findViewById(R.id.ivImageCategoryItem);
            tvCateName = itemView.findViewById(R.id.tvCategoryItem);
            cardCategoryItem = itemView.findViewById(R.id.cardCategoryItem);

        }
    }
}
