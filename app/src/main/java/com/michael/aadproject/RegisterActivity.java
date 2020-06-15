package com.michael.aadproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText editFirstName, editLastName, editEmail, editPassword;
    private Button btnRegister;
    private TextView textLogin;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    //private FirebaseDatabase mDatabase;
    private DatabaseReference mDatabase;

    public static final String EMAIL_KEY = "EMAIL_KEY";
    private SharedPreferences sharedPreferences;

    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate - " + getClass().getSimpleName() + " | Activity ID - " +
                this.hashCode());
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
        setContentView(R.layout.activity_register);

        editFirstName = findViewById(R.id.editTextFirstName);
        editLastName = findViewById(R.id.editTextLastName);
        editEmail = findViewById(R.id.editTextRegisterEmail);
        editPassword = findViewById(R.id.editTextRegisterPassword);
        btnRegister = findViewById(R.id.buttonRegister);
        textLogin = findViewById(R.id.textViewActionLogin);
        progressBar = findViewById(R.id.progressRegister);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        sharedPreferences = getSharedPreferences("FitnessLocator", Context.MODE_PRIVATE);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = editFirstName.getText().toString();
                final String lastName = editLastName.getText().toString();
                final String email = editEmail.getText().toString();
                String password = editPassword.getText().toString();

                boolean validFirstName = EntryHelper.checkName(firstName);
                boolean validLastName = EntryHelper.checkName(lastName);
                boolean validEmail = EntryHelper.checkEmail(email);
                boolean validPassword = EntryHelper.checkPassword(password);
                //Toast.makeText(getApplicationContext(), Boolean.toString(validEmail), Toast.LENGTH_SHORT).show();

                if (!validFirstName) {
                    editFirstName.setError("Please enter a valid first name. ");
                    editFirstName.setText("");
                }
                if (!validLastName) {
                    editLastName.setError("Please enter a valid last name.");
                    editLastName.setText("");
                }
                if (!validEmail) {
                    editEmail.setError("Please enter a valid email address.");
                    editEmail.setText("");
                }
                if (!validPassword) {
                    editPassword.setError("Please enter a valid password.");
                    editPassword.setText("");
                }
                if (validFirstName && validLastName && validEmail && validPassword) {
                    progressBar.setVisibility(View.VISIBLE);
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        String userId = task.getResult().getUser().getUid();
                                        User user = new User(firstName, lastName, email);
                                        mDatabase.child(userId).setValue(user);
                                        // to update - mDatabaseRef.child("users").child(userId).child("name").setValue(name);

                                        EntryHelper.saveEmail(sharedPreferences, EMAIL_KEY, editEmail);
                                        mAuth.signOut();
                                        Toast.makeText(getBaseContext(),"Registration successful! Please log in.",
                                                Toast.LENGTH_SHORT).show();
                                        Intent toLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                                        toLogin.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                        startActivity(toLogin);
                                        finish();
                                    }
                                    else {
                                        progressBar.setVisibility(View.INVISIBLE);
                                        String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();
                                        switch (errorCode) {
                                            case "ERROR_EMAIL_ALREADY_IN_USE":
                                                EntryHelper.saveEmail(sharedPreferences, EMAIL_KEY, editEmail);
                                                editEmail.setError("Email is registered. Please log in or enter a different email address.");
                                                break;
                                            case "ERROR_WEAK_PASSWORD":
                                                editPassword.setError("Weak password. Please enter a stronger password.");
                                                break;
                                            default:
                                                Toast.makeText(getBaseContext(), ((FirebaseAuthException) task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }

            }
        });

        textLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent toLogin = new Intent(RegisterActivity.this, LoginActivity.class);
                toLogin.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(toLogin);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());
        if (sharedPreferences.contains(EMAIL_KEY)) {
            editEmail.setText(sharedPreferences.getString(EMAIL_KEY, ""));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy - " + getClass().getSimpleName() + " | Activity ID - " + this.hashCode());
    }
}

