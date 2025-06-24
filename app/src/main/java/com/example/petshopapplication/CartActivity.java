package com.example.petshopapplication;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.petshopapplication.Adapter.CartAdapter;
import com.example.petshopapplication.databinding.ActivityAddCategoryBinding;
import com.example.petshopapplication.databinding.ActivityCategoryListBinding;
import com.example.petshopapplication.model.Cart;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Variant;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemCheckedListener, CartAdapter.OnItemLongPressListener {
    FirebaseDatabase database;
    DatabaseReference reference;
    CartAdapter.OnCartItemCheckedListener listener;
    List<Cart> cartList = new ArrayList<>();
    List<Product> productList = new ArrayList<>();
    List<String> cartProductId = new ArrayList<>();
    List<Cart> selectedItemList = new ArrayList<>();
    ActivityAddCategoryBinding binding;
    FirebaseAuth auth;
    FirebaseUser user;
    Button purchaseButton;
    ImageView btn_back;
    TextView tv_cart_empty;
    RecyclerView rec_cart;

    //Set up dialog cart
    Dialog dialog;

    //pop up view
    Button btn_delete_cart_item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = ActivityAddCategoryBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
        setContentView(R.layout.activity_cart);

        //Get Id of current User
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        String userId = user.getUid();

        //Initialize firebase
        database = FirebaseDatabase.getInstance();

        tv_cart_empty = findViewById(R.id.tv_cart_empty);
        rec_cart = findViewById(R.id.rec_cart);


        initCart(userId);

        //Handle back button
        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            finish();
        });

        purchaseButton = findViewById(R.id.btn_purchase); // Khởi tạo nút mua hàng

        // Thiết lập sự kiện khi nút mua hàng được nhấn
        purchaseButton.setOnClickListener(v -> {
            List<Cart> selectedItems = selectedItemList; // Lấy danh sách sản phẩm đã chọn

            // Kiểm tra nếu danh sách sản phẩm đã chọn trống hoặc null
            if (selectedItems == null || selectedItems.isEmpty()) {
                Toast.makeText(CartActivity.this, "Vui lòng chọn sản phẩm để mua", Toast.LENGTH_SHORT).show();
            } else {
                double totalAmount = calculateTotalPrice();
                Intent intent = new Intent(CartActivity.this, PaymentActivity.class);
                intent.putExtra("selectedItems", (ArrayList<Cart>) selectedItems); // Chuyển danh sách sản phẩm đã chọn
                intent.putExtra("totalAmount", totalAmount);
                startActivity(intent); // Chuyển sang PaymentActivity
            }
        });

    }

    public void initCart(String userId) {
        reference = database.getReference(getString(R.string.tbl_cart_name));

        //Read data cart of current user from firebase
        reference.orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Cart cart = dataSnapshot.getValue(Cart.class);
                                cartList.add(cart);
                            }


                            getListProductInCart(cartList);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    public void getListProductInCart(List<Cart> cartList) {

        for (Cart cart : cartList) {
            cartProductId.add(cart.getProductId());
        }

        reference = database.getReference(getString(R.string.tbl_product_name));
        reference.orderByChild("id")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                                //Check if product is in cart or not
                                if (cartProductId.contains(dataSnapshot.
                                        child("id").getValue(String.class))) {
                                    productList.add(dataSnapshot.getValue(Product.class));
                                }
                            }

                            RecyclerView rec = findViewById(R.id.rec_cart);
                            CartAdapter adapter = new CartAdapter(productList, cartList, CartActivity.this, CartActivity.this, CartActivity.this);
                            rec.setLayoutManager(new LinearLayoutManager(CartActivity.this));
                            rec.setAdapter(adapter);


                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }


    @Override
    public void onCartItemCheckedChanged() {
        updateSelectedItemList();
        Log.d("CartAdapter", "Selected items: " + selectedItemList.toString());
        calculateTotalPrice();
    }

    private void updateSelectedItemList() {
        selectedItemList.clear();  // Xóa danh sách cũ để cập nhật lại
        for (Cart cart : cartList) {
            if (cart.getIsChecked() != null && cart.getIsChecked()) { // Chỉ thêm các Cart được chọn
                selectedItemList.add(cart);
            }
        }
    }

    public double calculateTotalPrice() { // Đổi kiểu trả về thành double
        double totalPrice = 0.;
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        for (Cart cart : selectedItemList) { // Duyệt qua selectedItemList để tính tổng giá
            totalPrice += getPriceForSelectedProduct(cart) * cart.getQuantity();
        }

        ((TextView) findViewById(R.id.tv_total_price)).setText(currencyFormatter.format(totalPrice));
        return totalPrice; // Trả về tổng số tiền
    }

    public Double getPriceForSelectedProduct(Cart cart) {
        double price = 0.;

        //Find related product
        for (Product product : productList) {
            if (cart.getProductId().equals(product.getId())) {
                List<Variant> variantList = product.getListVariant();

                //Find related variant
                for (Variant variant : variantList) {
                    if (cart.getSelectedVariantId().equals(variant.getId())) {
                        Double oldPrice = variant.getPrice();
                        Double newPrice = oldPrice * (1 - product.getDiscount() / 100.0);
                        return newPrice;

                    }

                }
            }
        }
        return null;
    }

    @Override
    public void onItemLongPress(Cart cart) {
        //create dialog of delete cart item
        dialog = new Dialog(CartActivity.this);
        dialog.setContentView(R.layout.pop_up_delete_cart_item);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //init dialog view items
        btn_delete_cart_item = dialog.findViewById(R.id.btn_delete_cart_item);

        dialog.show();

        //Handle delete button on dialog
        btn_delete_cart_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = database.getReference(getString(R.string.tbl_cart_name));
                reference.orderByChild("cartId").equalTo(cart.getCartId())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                        Log.e("Cart Item Delete", dataSnapshot.getRef() + "");

                                        dataSnapshot.getRef().removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(CartActivity.this, "Delete cart item successfullu!", Toast.LENGTH_SHORT).show();
                                                    //Exit dialog
                                                    dialog.dismiss();
                                                    //Reload cart list
                                                    reloadCartList();

                                                } else {
                                                    Toast.makeText(CartActivity.this, "Failed to delete cart item!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }
        });

    }

    public void reloadCartList() {
        cartList.clear();
        CartAdapter emptyAdapter = new CartAdapter(new ArrayList<>(), new ArrayList<>(), CartActivity.this, CartActivity.this, CartActivity.this);
        rec_cart.setAdapter(emptyAdapter);

        initCart(user.getUid());

    }

}