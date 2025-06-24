package com.example.petshopapplication;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.example.petshopapplication.Adapter.ColorAdapter;
import com.example.petshopapplication.Adapter.FeedBackAdapter;
import com.example.petshopapplication.Adapter.ListProductAdapter;
import com.example.petshopapplication.Adapter.ListProductCategoryAdapter;
import com.example.petshopapplication.Adapter.ProductImageAdapter;
import com.example.petshopapplication.Adapter.SizeAdapter;
import com.example.petshopapplication.databinding.ActivityAdminViewProductDetailBinding;
import com.example.petshopapplication.databinding.ActivityProductDetailBinding;
import com.example.petshopapplication.model.Cart;
import com.example.petshopapplication.model.Category;
import com.example.petshopapplication.model.Color;
import com.example.petshopapplication.model.FeedBack;
import com.example.petshopapplication.model.Product;
import com.example.petshopapplication.model.Size;
import com.example.petshopapplication.model.User;
import com.example.petshopapplication.model.Variant;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;

public class AdminViewProductDetailActivity extends AppCompatActivity implements ProductImageAdapter.OnProductImageClickListener, SizeAdapter.OnSizeClickEventListener, ColorAdapter.OnColorClickEventListener{

    ActivityAdminViewProductDetailBinding binding;
    FirebaseDatabase database;
    DatabaseReference reference;
    FirebaseAuth auth;

    //Intent data
    private String productId;

    //Init items
    private List<Product> productItems;
    private List<Category> categoryItems;
    private List<Color> colorItems;
    private List<Variant> variantItems;
    private List<Size> sizeItems;

    //Adapter
    private ListProductAdapter productAdapter;
    private FeedBackAdapter feedBackAdapter;
    private ProductImageAdapter productImageAdapter;
    private ColorAdapter colorAdapter;
    private SizeAdapter sizeAdapter;

    //Set up dialog cart
    ConstraintLayout layout;
    Dialog dialog;

    //pop up view
    RecyclerView colorCartRecyclerView, sizeCartRecyclerView;
    ImageView imv_cart_product;
    TextView tv_cart_new_price, tv_cart_old_price, tv_cart_stock, tv_quantity, tv_color_text, tv_size_text;
    Button btn_plus, btn_minus, btn_submit;


    //Add to cart data
    private String selectedColorId;
    private String selectedVariantId;
    Cart cartItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityAdminViewProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        colorItems = new ArrayList<>();
        sizeItems = new ArrayList<>();
        variantItems = new ArrayList<>();

        database = FirebaseDatabase.getInstance();
        binding.tvEmptyFeedback.setVisibility(View.GONE);
        layout = binding.adminProductDetails;
        auth = FirebaseAuth.getInstance();

        //create dialog of pop up for add to cart
        dialog = new Dialog(AdminViewProductDetailActivity.this);
        dialog.setContentView(R.layout.pop_up_add_cart);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //init dialog view items
        imv_cart_product = dialog.findViewById(R.id.imv_cart_product_image);
        tv_cart_new_price = dialog.findViewById(R.id.tv_cart_new_price);
        tv_cart_old_price = dialog.findViewById(R.id.tv_cart_old_price);
        tv_cart_stock = dialog.findViewById(R.id.tv_cart_stock);
        tv_quantity = dialog.findViewById(R.id.tv_quantity);
        tv_color_text = dialog.findViewById(R.id.tv_color_text);
        tv_size_text = dialog.findViewById(R.id.tv_size_text);
        btn_plus = dialog.findViewById(R.id.btn_plus);
        btn_minus = dialog.findViewById(R.id.btn_minus);
        btn_submit = dialog.findViewById(R.id.btn_submit);

        //modify cart quantity function
        btn_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(tv_quantity.getText().toString());
                int maxStock = Integer.parseInt(tv_cart_stock.getText().toString().replace("Stock: ", ""));
                if(currentQuantity < maxStock) {
                    tv_quantity.setText(String.valueOf(currentQuantity + 1));
                }
            }
        });

        btn_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentQuantity = Integer.parseInt(tv_quantity.getText().toString());
                if(currentQuantity > 1) {
                    tv_quantity.setText(String.valueOf(currentQuantity - 1));
                }
            }
        });

        tv_quantity.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //No used
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    int quantity = Integer.parseInt(tv_quantity.getText().toString());
                    if(quantity <= 0) {
                        tv_quantity.setText("1");
                    } else if(quantity > Integer.parseInt(tv_cart_stock.getText().toString().replace("Stock: ", ""))) {
                        tv_quantity.setText(String.valueOf(Integer.parseInt(tv_cart_stock.getText().toString().replace("Stock: ", ""))));
                    }
                } catch (Exception e) {
                    tv_quantity.setText("1");
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                //No used
            }
        });

        getIntend();
        initProductDetail(productId);

        binding.tvViewListProductFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListFeedbackActivity.class);
            intent.putExtra("PRODUCT_ID", productId); // Pass product ID
            startActivity(intent);
        });
    }


    private void initProductDetail(String productID) {
        reference = database.getReference(getString(R.string.tbl_product_name));
        Query query = reference.orderByChild("id").equalTo(productID);


        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //Update product detail view
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Product product = dataSnapshot.getValue(Product.class);
                        binding.tvProductName.setText(product.getName());

                        //Check if product have variants
                        if(!product.getListVariant().isEmpty()) {
                            Variant variant = product.getListVariant().get(0);
                            selectedVariantId = variant.getId();
                            if(variant.getListColor() != null && !variant.getListColor().isEmpty()) {
                                Color color = variant.getListColor().get(0);
                                selectedColorId = color.getId();
                                fillProductData(product, variant, color);
                            } else {
                                fillProductData(product, variant, null);
                            }
                        } else {
                            fillProductData(product, null, null);
                        }
                        initCategory(product);
                        fetchFeedback(product);
                        fetchProductImage(product);
                    }

                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void fillProductData(Product product, Variant variant, Color color) {

        //fill product information into dialog
        binding.tvProductName.setText(product.getName());
        double oldPrice = product.getBasePrice();
        String imageUrl = product.getBaseImageURL();
        int stock = 0;

        //Check if product have variants
        if(variant != null) {
            oldPrice = variant.getPrice();
            stock = variant.getStock();

            //check if product have color variants
            if(color != null) {
                imageUrl = color.getImageUrl();
                stock = color.getStock();
                tv_color_text.setVisibility(View.VISIBLE);
            } else {
                tv_color_text.setVisibility(View.INVISIBLE);
            }
        }

        //check if product is discounted
        if(product.getDiscount() > 0) {
            //fill cart information
            tv_cart_old_price.setText(String.format("%,.0fđ", oldPrice));
            tv_cart_old_price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            tv_cart_new_price.setText(String.format("%,.0fđ", oldPrice * (1 - product.getDiscount()/100.0)));
            //fill product information
            binding.tvDiscount.setText(String.valueOf("-" + product.getDiscount()) + "%");
            binding.tvOldPrice.setText(String.format("%,.0fđ", oldPrice));
            binding.tvOldPrice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            binding.tvNewPrice.setText(String.format("%,.0fđ", oldPrice * (1 - product.getDiscount()/100.0)));
        } else {
            //fill cart information
            tv_cart_old_price.setVisibility(View.GONE);
            tv_cart_new_price.setText(String.format("%,.0fđ", oldPrice));
            //fill product information
            binding.tvDiscount.setVisibility(View.GONE);
            binding.tvOldPrice.setVisibility(View.GONE);
            binding.tvNewPrice.setText(String.format("%,.0fđ", oldPrice));
        }

        binding.tvDescription.setText(product.getDescription());
        binding.tvStockProduct.setText("Stock: " + stock);

        //fill stock cart
        tv_cart_stock.setText("Stock: " + stock);

        //Product image preview
        Glide.with(AdminViewProductDetailActivity.this)
                .load(imageUrl)
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(binding.imvProductImage);

        //Get Image into product cart image
        Glide.with(AdminViewProductDetailActivity.this)
                .load(imageUrl)
                .transform(new CenterCrop(), new RoundedCorners(30))
                .into(imv_cart_product);
    }


    public void fetchProductImage(Product product) {
        productImageAdapter = new ProductImageAdapter(product, AdminViewProductDetailActivity.this);
        binding.rcvImageProduct.setLayoutManager(new LinearLayoutManager(AdminViewProductDetailActivity.this, LinearLayoutManager.HORIZONTAL, false));
        binding.rcvImageProduct.setAdapter(productImageAdapter);
    }



    private void fetchFeedback(Product product) {
        reference = database.getReference(getString(R.string.tbl_feedback_name));

        List<FeedBack> feedbackItems = new ArrayList<>();

        Query query = reference.orderByChild("productId").equalTo(product.getId());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    int totalRating = 0;
                    int feedbackCount = 0;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        FeedBack feedback = dataSnapshot.getValue(FeedBack.class);
                        if (!feedback.isDeleted()) {
                            feedbackItems.add(feedback); // Add feedback to the list
                            totalRating += feedback.getRating(); // Sum up ratings
                            feedbackCount++;
                        }
                    }
                    if(feedbackCount > 0) {
                        float averageRating = (float) totalRating / feedbackCount;
                        String formattedRating = String.format("%.1f", averageRating);
                        binding.tvRating.setText(formattedRating);
                        binding.rtbFeedbackRating.setRating(averageRating);
                        binding.tvRating2.setText(String.valueOf(feedbackCount));
                        fetchUserData(feedbackItems);
                    } else {
                        binding.tvEmptyFeedback.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    private void fetchUserData(List<FeedBack> feedbackItems) {
        reference = database.getReference(getString(R.string.tbl_user_name));
        List<User> userItems = new ArrayList<>();
        for(FeedBack feedBack : feedbackItems) {
            //Reference to the user table
            reference = database.getReference(getString(R.string.tbl_user_name));
            //Get user data by user Id in feed back
            Query query = reference.orderByChild("id").equalTo(feedBack.getUserId());
            query.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                    if(snapshot.exists()) {
                        //Get user data from database
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            User user = dataSnapshot.getValue(User.class);
                            userItems.add(user);
                        }
                        if(userItems.size() > 0) {
                            feedBackAdapter = new FeedBackAdapter(feedbackItems, userItems);
                            binding.rcvFeedback.setLayoutManager(new LinearLayoutManager(AdminViewProductDetailActivity.this, RecyclerView.VERTICAL, true));
                            binding.rcvFeedback.setNestedScrollingEnabled(false);
                            binding.rcvFeedback.setAdapter(feedBackAdapter);
                        }
                    }
                }

                @Override
                public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

                }
            });
        }
    }

    private void initCategory(Product product) {
        reference = database.getReference(getString(R.string.tbl_category_name));

        categoryItems = new ArrayList<>();
        Query query = reference.orderByChild("isDeleted");
        query.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@androidx.annotation.NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        categoryItems.add(dataSnapshot.getValue(Category.class));
                    }
                }
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }



    private void getIntend() {
        productId = getIntent().getStringExtra("PRODUCT_ID");
        binding.backBtn.setOnClickListener(v -> finish());
    }


    @Override
    public void onProductImageClick(Product product, Variant variant, Color color) {
        fillProductData(product, variant, color);
    }

    @Override
    public void onSizeClickEvent(Size size, Product product) {
        Variant variant = getVariantBySize(size.getId());
        selectedVariantId = variant.getId();
        colorItems.clear();
        if(variant.getListColor()!= null && !variant.getListColor().isEmpty()) {
            colorItems.addAll(variant.getListColor());
            fillColorInformation(colorItems.get(0));
            selectedColorId = colorItems.get(0).getId();
            colorAdapter.notifyDataSetChanged();
        } else {
            selectedColorId = null;
            tv_cart_stock.setText("Stock: " + variant.getStock());
        }


        double oldPrice = variant.getPrice();

        if(product.getDiscount() > 0) {
            //fill cart information
            tv_cart_old_price.setText(String.format("%,.0fđ", oldPrice));
            tv_cart_old_price.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            tv_cart_new_price.setText(String.format("%,.0fđ", oldPrice * (1 - product.getDiscount()/100.0)));
        }  else {
            //fill cart information
            tv_cart_old_price.setVisibility(View.GONE);
            tv_cart_new_price.setText(String.format("%,.0fđ", oldPrice));
        }
    }

    private Variant getVariantBySize(String sizeId) {
        for (Variant variant : variantItems) {
            if(variant.getSize().getId().equals(sizeId)) {
                return variant;
            }
        }
        return null;
    }

    private void fillColorInformation(Color color) {
        //Set color image
        Glide.with(dialog.getContext())
                .load(color.getImageUrl())
                .into(imv_cart_product);

        tv_cart_stock.setText("Stock: " + color.getStock());
    }

    @Override
    public void onColorClick(Color color) {
        selectedColorId = color.getId();
        fillColorInformation(color);
    }
}
