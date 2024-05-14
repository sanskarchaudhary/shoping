package com.android.onlineshoppingapp.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.onlineshoppingapp.R;
import com.android.onlineshoppingapp.adapters.OrderAdapter;
import com.android.onlineshoppingapp.models.Order;
import com.android.onlineshoppingapp.models.UserAddress;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ReceiveOrderFragment extends Fragment {

    private LinearLayout layoutBlank;
    private RecyclerView recyclerView;
    private OrderAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private List<Order> orderList;

    public ReceiveOrderFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_receive_order, container, false);

        // init
        recyclerView = view.findViewById(R.id.rvReceiveOrder);
        layoutBlank = view.findViewById(R.id.layoutEmptyOrder0);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        db.collection("Users")
                .document(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (Objects.requireNonNull(documentSnapshot.getString("accountType")).equals("Admin")) {
                        showOrderForAdmin();
                    } else if (Objects.requireNonNull(documentSnapshot.getString("accountType")).equals("Sell")) {
                        showOrderForCustomer("seller");
                    } else
                        showOrderForCustomer("orderer");
                });

        return view;
    }

    private void showOrderForCustomer(String str) {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        orderList = new ArrayList<>();
        db.collection("Orders")
                .whereEqualTo(str, Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())
                .whereEqualTo("orderStatus", 0)
                .addSnapshotListener((value, error) -> {
                    orderList.clear();
                    if (error != null) {
                        Log.e("error", Objects.requireNonNull(error.getMessage()));
                        return;
                    }

                    if (value != null) {
                        for (DocumentSnapshot documentSnapshot : value) {
                            //get list product
                            Order order = new Order();
                            order.setOrderId(documentSnapshot.getId());
                            order.setOrderer(documentSnapshot.getString("orderer"));
                            order.setOrderStatus(Integer.parseInt(String.valueOf(documentSnapshot.get("orderStatus"))));
                            order.setTotalPrice(Integer.parseInt(String.valueOf(documentSnapshot.get("totalPrice"))));
                            order.setAddress(documentSnapshot.get("address", UserAddress.class));
                            orderList.add(order);
                        }
                    }

                    // set up
                    if (orderList.isEmpty()) {
                        layoutBlank.setVisibility(View.VISIBLE);
                    } else {
                        layoutBlank.setVisibility(View.GONE);
                        adapter = new OrderAdapter(orderList, getContext());
                        if (str.equals("seller"))
                            adapter.isAdmin = true;
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }

    private void showOrderForAdmin() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        orderList = new ArrayList<>();
        db.collection("Orders")
                .whereEqualTo("orderStatus", 0)
                .addSnapshotListener((value, error) -> {
                    orderList.clear();
                    if (error != null) {
                        Log.e("error", Objects.requireNonNull(error.getMessage()));
                        return;
                    }

                    assert value != null;
                    for (DocumentSnapshot documentSnapshot : value) {
                        //get list product
                        Order order = new Order();
                        order.setOrderId(documentSnapshot.getId());
                        order.setOrderer(documentSnapshot.getString("orderer"));
                        order.setOrderStatus(Integer.parseInt(String.valueOf(documentSnapshot.get("orderStatus"))));
                        order.setTotalPrice(Integer.parseInt(String.valueOf(documentSnapshot.get("totalPrice"))));
                        order.setAddress(documentSnapshot.get("address", UserAddress.class));
                        orderList.add(order);

                    }
                    // set up
                    if (orderList.isEmpty()) {
                        layoutBlank.setVisibility(View.VISIBLE);
                    } else {
                        layoutBlank.setVisibility(View.GONE);
                        adapter = new OrderAdapter(orderList, getContext());
                        adapter.isAdmin = true;
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                        recyclerView.setLayoutManager(linearLayoutManager);
                        recyclerView.setAdapter(adapter);
                    }
                });
    }
}