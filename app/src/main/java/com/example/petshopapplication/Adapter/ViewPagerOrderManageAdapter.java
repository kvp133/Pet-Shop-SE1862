package com.example.petshopapplication.Adapter;

import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.petshopapplication.AllOrdersTablayoutFragment;
import com.example.petshopapplication.CanceledTablayoutFragment;
import com.example.petshopapplication.DeliveredTablayoutFragment;
import com.example.petshopapplication.ProcessingTablayoutFragment;
import com.example.petshopapplication.R;
import com.example.petshopapplication.ShippingTablayoutFragment;
import com.example.petshopapplication.WaitingTablayoutFragment;


import lombok.NonNull;

public class ViewPagerOrderManageAdapter extends FragmentStateAdapter {
    private final String[] tabTitles;
    private boolean isInventory;

    public ViewPagerOrderManageAdapter(@NonNull FragmentActivity fragmentActivity, boolean isInventory) {
        super(fragmentActivity);
        tabTitles = fragmentActivity.getResources().getStringArray(R.array.tab_order_manage_titles);
        this.isInventory = isInventory;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (isInventory) {
            switch (position) {
                case 0: {
                    Log.d("createFragment", "All orders tablayout fragment");
                    return new AllOrdersTablayoutFragment(isInventory); // All orders
                }
                case 1: {
                    Log.d("createFragment", "ProcessingTablayoutFragment");
                    return new ProcessingTablayoutFragment(isInventory); // Processing - waiting confirm of inventory
                }
                case 2: {
                    Log.d("createFragment", "ShippingTablayoutFragment");
                    return new ShippingTablayoutFragment(isInventory); // Shipping
                }
                case 3: {
                    Log.d("createFragment", "Return goods");
                    return new DeliveredTablayoutFragment(isInventory); // Returning
                }
                case 4: {
                    Log.d("createFragment", "DeliveredTablayoutFragment");
                    return new CanceledTablayoutFragment(isInventory); // Delivered
                }
                default:
                    return new AllOrdersTablayoutFragment(isInventory); // Default
            }
        } else {
            switch (position) {
                case 0: {
                    Log.d("createFragment", "ProcessingTablayoutFragment");
                    return new ProcessingTablayoutFragment(isInventory); // Processing - waiting confirm of inventory
                }
                case 1: {
                    Log.d("createFragment", "ShippingTablayoutFragment");
                    return new ShippingTablayoutFragment(isInventory); // Shipping
                }
                case 2: {
                    Log.d("createFragment", "Return goods");
                    return new ShippingTablayoutFragment(isInventory); // Returning
                }
                case 3: {
                    Log.d("createFragment", "DeliveredTablayoutFragment");
                    return new DeliveredTablayoutFragment(isInventory); // Delivered
                }
                case 4: {
                    Log.d("createFragment", "CanceledTablayoutFragment");
                    return new CanceledTablayoutFragment(isInventory); // Canceled
                }
                default:
                    return new ProcessingTablayoutFragment(isInventory); // Default
            }
        }
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }
}
