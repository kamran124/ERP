package com.maenterprise.erp.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.maenterprise.erp.R;
import com.maenterprise.erp.models.ProductInventory;
import java.util.List;

public class ProductDropdownAdapter extends ArrayAdapter<ProductInventory> {
    private final LayoutInflater inflater;

    public ProductDropdownAdapter(@NonNull Context context, @NonNull List<ProductInventory> products) {
        super(context, 0, products);
        inflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return createView(position, convertView, parent);
    }

    private View createView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_product_dropdown, parent, false);
        }

        ProductInventory item = getItem(position);
        ImageView ivProductImage = convertView.findViewById(R.id.ivProductImage);
        TextView tvProductName = convertView.findViewById(R.id.tvProductName);
        TextView tvStock = convertView.findViewById(R.id.tvStock);

        if (item != null) {
            tvProductName.setText(item.product.productName);
            tvStock.setText("In Stock: " + item.totalQuantity);
            
            if (item.product.imageUrl != null) {
                ivProductImage.setImageURI(Uri.parse(item.product.imageUrl));
            } else {
                ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        return convertView;
    }
}
