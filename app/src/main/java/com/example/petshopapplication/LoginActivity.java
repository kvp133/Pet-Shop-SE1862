package com.example.petshopapplication;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.petshopapplication.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;

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

        // Clear error messages when user starts typing
        edt_email.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                edt_email.setError(null);
            }
        });

        edt_password.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                edt_password.setError(null);
            }
        });

        database = FirebaseDatabase.getInstance();
        reference = database.getReference(getString(R.string.tbl_user_name));
        firebaseAuth = FirebaseAuth.getInstance();

        // Log Firebase configuration for debugging
        Log.d("LoginActivity", "Firebase Auth initialized: " + (firebaseAuth != null));
        Log.d("LoginActivity", "Firebase Database initialized: " + (database != null));


        //SharedPreferences init
        sharedPreferences = getSharedPreferences("User", MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Check if user is already signed in
        if (firebaseAuth.getCurrentUser() != null) {
            // User is already signed in, redirect to appropriate activity
            redirectToAppropriateActivity();
        } else {
            // Clear any existing auth state to ensure clean login
            firebaseAuth.signOut();
        }

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
            // Validate input first
            if (validateInput()) {
                if(cb_remember.isChecked()) {
                    editor.putBoolean("isRemembered", true);
                    editor.putString("username", edt_email.getText().toString()); // Save the username if needed
                    editor.putString("password", edt_password.getText().toString());
                    editor.apply();
                }
                //CONTENT: Implement login logic
                checkUser();
            }
        });

        //Handling register link click
        tv_registerRedirect = findViewById(R.id.linkRegister);
        tv_registerRedirect.setOnClickListener(v -> {
            // CONTENT: Implement register redirect logic
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean validateInput() {
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();

        if (email.isEmpty()) {
            edt_email.setError("Email is required");
            edt_email.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edt_email.setError("Please enter a valid email");
            edt_email.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            edt_password.setError("Password is required");
            edt_password.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            edt_password.setError("Password must be at least 6 characters");
            edt_password.requestFocus();
            return false;
        }

        if (!isNetworkAvailable()) {
            Toast.makeText(this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private void redirectToAppropriateActivity() {
        String currentUserEmail = firebaseAuth.getCurrentUser().getEmail();
        if (currentUserEmail != null) {
            Query query = reference.orderByChild("email").equalTo(currentUserEmail);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot ds : dataSnapshot.getChildren()) {
                        User user = ds.getValue(User.class);
                        if (user != null) {
                            Intent intent;
                            if(user.getRoleId() == ROLE_USER) {
                                intent = new Intent(LoginActivity.this, HomeActivity.class);
                            } else if(user.getRoleId() == ROLE_INVENTORY) {
                                intent = new Intent(LoginActivity.this, ListOrderManageActivity.class);
                            } else if(user.getRoleId() == ROLE_MARKETING) {
                                intent = new Intent(LoginActivity.this, ViewAdminDashBoardActivity.class);
                            } else {
                                // Default to home activity
                                intent = new Intent(LoginActivity.this, HomeActivity.class);
                            }
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // If there's an error, sign out the user
                    firebaseAuth.signOut();
                }
            });
        }
    }

    public void checkUser() {
        //Retrieve user input data
        String email = edt_email.getText().toString().trim();
        String password = edt_password.getText().toString().trim();

        // Show loading state
        btn_login.setEnabled(false);
        btn_login.setText("Logging in...");

        //Verify user with firebase
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // Reset button state
                        btn_login.setEnabled(true);
                        btn_login.setText("Login");

                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("LoginActivity", "signInWithEmail:success");
                            fetchUserData();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("LoginActivity", "signInWithEmail:failure", task.getException());

                            String errorMessage = "Authentication failed";

                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                errorMessage = "No account found with this email address";
                            } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                errorMessage = "Invalid password";
                            } else if (task.getException() != null) {
                                String exceptionMessage = task.getException().getMessage();
                                if (exceptionMessage != null && exceptionMessage.contains("RecaptchaAction")) {
                                    errorMessage = "Authentication error. Please try again or check your internet connection.";
                                    // Try to retry authentication after a delay
                                    retryAuthentication();
                                } else {
                                    errorMessage = exceptionMessage;
                                }
                            }

                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserData() {
        Query query = reference.orderByChild("email").equalTo(edt_email.getText().toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean userFound = false;
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    User user = ds.getValue(User.class);
                    if (user != null) {
                        userFound = true;
                        if(user.getRoleId() == ROLE_USER) {
                            //User is authenticated, go to home activity
                            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else if(user.getRoleId() == ROLE_INVENTORY) {
                            //User is authenticated, go to inventory activity
                            Intent intent = new Intent(LoginActivity.this, ListOrderManageActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else if(user.getRoleId() == ROLE_MARKETING) {
                            //User is authenticated, go to admin dashboard
                            Intent intent = new Intent(LoginActivity.this, ViewAdminDashBoardActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        }
                        break; // Exit loop after finding the user
                    }
                }

                if (!userFound) {
                    Toast.makeText(LoginActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    // Sign out if user data is not found
                    firebaseAuth.signOut();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(LoginActivity.this, "Get user data failed!", Toast.LENGTH_SHORT).show();
                // Sign out if database error occurs
                firebaseAuth.signOut();
            }
        });
    }

    private void retryAuthentication() {
        // Wait for 2 seconds before retrying
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isNetworkAvailable()) {
                    Toast.makeText(LoginActivity.this, "Retrying authentication...", Toast.LENGTH_SHORT).show();
                    checkUser();
                } else {
                    Toast.makeText(LoginActivity.this, "No internet connection. Please check your network.", Toast.LENGTH_LONG).show();
                }
            }
        }, 2000);
    }
}