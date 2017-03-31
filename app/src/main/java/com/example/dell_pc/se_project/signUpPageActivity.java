package com.example.dell_pc.se_project;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.view.View;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class signUpPageActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    ProgressDialog myProgressDialog;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }

        };


        Button signUp = (Button) findViewById(R.id.signup_button);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = ((EditText) findViewById(R.id.signup_id)).getText().toString().trim();
                String password = ((EditText) findViewById(R.id.signup_password)).getText().toString().trim();
                String confirmPassword = ((EditText) findViewById(R.id.signup_confirm_password)).getText().toString().trim();
                if(password.equals(confirmPassword)){
                    if(!email.equals("") && !password.equals("")) {
                        createAccount(email, password);
                        myProgressDialog = new ProgressDialog(signUpPageActivity.this, 0);
                        myProgressDialog.setMessage("Loading");
                        myProgressDialog.show();
                    }
                    else{
                        Toast.makeText(signUpPageActivity.this, "Email or Password is empty",Toast.LENGTH_SHORT).show();
                    }
                    //ProgressDialog myProgressDialog = ProgressDialog.show(signUpPageActivity.this,"Please Wait", "Loading", true);
                }

                else
                    Toast.makeText(signUpPageActivity.this, "Password not match",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void createAccount(String email,String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
                        myProgressDialog.cancel();
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.e(TAG, "Sign-in Failed: " + task.getException().getMessage());

                            try {
                                throw task.getException();
                            } catch(FirebaseAuthWeakPasswordException e) {
                                Toast.makeText(signUpPageActivity.this, "Weak Password",Toast.LENGTH_SHORT).show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                Toast.makeText(signUpPageActivity.this, "Invalid email or password",Toast.LENGTH_SHORT).show();

                            } catch(FirebaseAuthUserCollisionException e) {
                                Toast.makeText(signUpPageActivity.this, "email already exist",Toast.LENGTH_SHORT).show();

                            } catch(Exception e) {
                                Log.e(TAG, e.getMessage());

                                if("An internal error has occurred. [ WEAK_PASSWORD  ]".equals(e.getMessage())){
                                    Toast.makeText(signUpPageActivity.this, "Weak Password",Toast.LENGTH_SHORT).show();
                                }
                            }


                            Toast.makeText(signUpPageActivity.this, "Sign up failed (Check Internet Connection)",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //when successfull signup

                            finish();
                            Toast.makeText(signUpPageActivity.this, "Sign up successfull",
                                    Toast.LENGTH_SHORT).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
