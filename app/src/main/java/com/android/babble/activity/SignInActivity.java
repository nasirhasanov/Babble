package com.android.babble.activity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.babble.MainActivity;
import com.android.babble.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText, passwordEditText;
    private TextView linkToSignUp;
    private boolean emailOk, passwordOk;
    private Button signInButton;
    private FirebaseAuth firebaseAuth;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        emailEditText = findViewById(R.id.edit_text_email);
        emailEditText.addTextChangedListener(new EditTextWatcher(emailEditText));

        passwordEditText = findViewById(R.id.edit_text_password);
        passwordEditText.addTextChangedListener(new EditTextWatcher(passwordEditText));

        signInButton = findViewById(R.id.button_signin);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptToSignIn();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();

        linkToSignUp = findViewById(R.id.link_to_signup);
        linkToSignUp.setText(Html.fromHtml(getString(R.string.don_t_have_account) + "<b><font color=#D32F2F>" + getString(R.string.sign_up) + "</font></b>"));
        linkToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignInActivity.this, SignUpActivity.class));
                finish();
            }
        });

    }

    public void showHide(View view) {
        if(view.getId()==R.id.show_hide_button){

            if(passwordEditText.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())){
                ((ImageView)(view)).setImageResource(R.drawable.ic_show_password);

                //Show Password
                passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordEditText.setSelection(passwordEditText.getText().length());

            }
            else{
                ((ImageView)(view)).setImageResource(R.drawable.ic_hide_password);

                //Hide Password
                passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordEditText.setSelection(passwordEditText.getText().length());

            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void checkButton(){
        if (passwordOk && emailOk){
            signInButton.setEnabled(true);
        }else{
            signInButton.setEnabled(false);
        }
    }

    private void attemptToSignIn(){

        if (!isNetworkAvailable()){
            Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();
            return;
        }


        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();

        emailEditText.clearFocus();
        passwordEditText.clearFocus();
        signInButton.setEnabled(false);
        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        }else{
                            String errorCode = ((FirebaseAuthException) task.getException()).getErrorCode();

                            switch (errorCode){
                                case  "ERROR_WRONG_PASSWORD":
                                    passwordEditText.setError("Password is incorrect");
                                    passwordEditText.requestFocus();
                                    break;

                                case   "ERROR_USER_NOT_FOUND":
                                    emailEditText.setError("User not found");
                                    emailEditText.requestFocus();
                                    break;

                                default:
                                    Toast.makeText(SignInActivity.this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                    break;

                            }
                        }
                    }
                });
    }

    private class EditTextWatcher implements TextWatcher {

        private View view;
        private EditTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        public void afterTextChanged(Editable editable) {
            switch(view.getId()){

                case R.id.edit_text_email:
                    emailOk = false;
                    if (emailEditText.getText().toString().trim().length()>6){
                        if (isEmailValid(emailEditText.getText().toString().trim())) {
                            emailOk = true;
                        }else{
                            emailOk = false;
                            emailEditText.setError("Email is not valid");
                        }
                        checkButton();
                    }
                    break;

                case R.id.edit_text_password:
                    passwordOk = false;
                    final String password = passwordEditText.getText().toString().trim();
                    if (password.length()<6){
                        passwordEditText.setError("Password should be at least 6 char");
                    }else{
                        passwordOk = true;
                    }
                    checkButton();

                    break;
            }

        }
    }
}
