package com.example.petshopapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.petshopapplication.Adapter.ManageProductAdapter;

public class AdminManageProductFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public AdminManageProductFragment() {
        // Required empty public constructor
    }

    public static AdminManageProductFragment newInstance(String param1, String param2) {
        AdminManageProductFragment fragment = new AdminManageProductFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_admin_manage_product, container, false);

        // Set up button listeners
        view.findViewById(R.id.btnShowTrendingProduct).setOnClickListener(v -> openViewTopTrendingProduct());
        view.findViewById(R.id.btnAddNewProduct).setOnClickListener(v -> openAddProductActivity());
        view.findViewById(R.id.btnViewListProduct).setOnClickListener(v -> openUpdateProductActivity());
        view.findViewById(R.id.btnManageCategory).setOnClickListener(v -> openManageCategory());

        return view;
    }

    private void openAddProductActivity() {
        Intent intent = new Intent(getActivity(), AddProductActivity.class);
        startActivity(intent);
    }

    private void openUpdateProductActivity() {
        Intent intent = new Intent(getActivity(), ManageProductActivity.class);
        startActivity(intent);
    }

    private void openViewTopTrendingProduct() {
        Intent intent = new Intent(getActivity(), TrendingProductActivity.class);
        startActivity(intent);
    }

    public void openManageCategory() {
        Intent intent = new Intent(getActivity(), CategoryListActivity.class);
        startActivity(intent);
    }
}
