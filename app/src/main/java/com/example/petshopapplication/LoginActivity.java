package com.example.petshopapplication;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.model.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import lombok.NonNull;

public class LoginActivity extends AppCompatActivity {

    private static final int ROLE_USER = 1;
    private static final int ROLE_INVENTORY = 3;
    private static final int ROLE_MARKETING = 4;



    EditText edt_email, edt_password;
    Button btn_login;
    TextView tv_registerRedirect;
    CheckBox cb_remember;

    //Share reference to store user data
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    FirebaseDatabase database;
    DatabaseReference reference;

    //Authentication with firebases
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

//        Intent intent1 = new Intent(LoginActivity.this, CartActivity.class);
//        startActivity(intent1);
        //Binding views
        edt_email = findViewById(R.id.edt_login_email);
        edt_password = findViewById(R.id.edt_login_password);
        btn_login = findViewById(R.id.btn_login);
        cb_remember = findViewById(R.id.cb_remember);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(getString(R.string.tbl_user_name));
        firebaseAuth = FirebaseAuth.getInstance();


        //SharedPreferences init
        sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        //Check if user is remembered
        boolean isRemembered = sharedPreferences.getBoolean("remember", false);
        if(isRemembered) {
            String email = sharedPreferences.getString("email", "");
            String password = sharedPreferences.getString("password", "");

            edt_email.setText(email);
            edt_password.setText(password);
            cb_remember.setChecked(true);

        }



        //Save user data when checkbox is checked
        //Handling login button click
        btn_login.setOnClickListener(v -> {
            if(cb_remember.isChecked()) {
                editor.putBoolean("isRemembered", true);
                editor.putString("username", edt_email.getText().toString()); // Save the username if needed
                editor.putString("password", edt_password.getText().toString());
                editor.apply();
            }
            //CONTENT: Implement login logic
            checkUser();

        });

        //Handling register link click
        tv_registerRedirect = findViewById(R.id.linkRegister);
        tv_registerRedirect.setOnClickListener(v -> {
            // CONTENT: Implement register redirect logic
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }


    public void checkUser() {
        //Retrieve user input data
        String email = edt_email.getText().toString();
        String password = edt_password.getText().toString();

        //Verify user with firebase
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                fetchUserData();
            }

        });

    }

    private void fetchUserData() {
        Query query = reference.orderByChild("email").equalTo(edt_email.getText().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if(user.getRoleId() == ROLE_USER) {
                        //User is authenticated, go to home activity
                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                        startActivity(intent);
                    } else if(user.getRoleId() == ROLE_INVENTORY) {
                        //User is authenticated, go to home activity
                        Intent intent = new Intent(LoginActivity.this, ListOrderManageActivity.class);
                        startActivity(intent);
                    }
                    else if(user.getRoleId() == ROLE_MARKETING) {
                        //User is authenticated, go to home activity
                        Intent intent = new Intent(LoginActivity.this, ViewAdminDashBoardActivity.class);
                        startActivity(intent);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Get user data failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }


}