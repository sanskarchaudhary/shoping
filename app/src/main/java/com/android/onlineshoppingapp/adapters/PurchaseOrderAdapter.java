package com.android.onlineshoppingapp.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.android.onlineshoppingapp.fragments.CancelOrderFragment;
import com.android.onlineshoppingapp.fragments.ReceiveOrderFragment;
import com.android.onlineshoppingapp.fragments.ShippingFragment;
import com.android.onlineshoppingapp.fragments.WaitForProductFragment;
import com.android.onlineshoppingapp.fragments.WaitForReviewFragment;

public class PurchaseOrderAdapter extends FragmentStateAdapter {

    public PurchaseOrderAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {
            case 1:
                return new WaitForProductFragment();
            case 2:
                return new ShippingFragment();
            case 3:
                return new WaitForReviewFragment();
            case 4:
                return new CancelOrderFragment();
            case 0:
            default:
                return new ReceiveOrderFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
