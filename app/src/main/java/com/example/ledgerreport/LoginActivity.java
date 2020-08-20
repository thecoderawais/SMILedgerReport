package com.example.ledgerreport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.ledgerreport.APIInterface.ApiInterface;
import com.example.ledgerreport.Models.UserLoginModel;
import com.example.ledgerreport.Utils.CONST;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    EditText etCode, etUsername, etPassword;
    Button btnLogin;
    ApiInterface apiInterface;

    SharedPreferences sharedpreferences;

    RelativeLayout relativeLayout;
    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            relativeLayout.setVisibility(View.VISIBLE);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etCode = findViewById(R.id.etAccCode);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        sharedpreferences = getSharedPreferences(getString(R.string.pref), Context.MODE_PRIVATE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(CONST.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiInterface = retrofit.create(ApiInterface.class);

        //View Animations (Splash Screen etc)
        relativeLayout = findViewById(R.id.rellay1);
        handler.postDelayed(runnable, 2000); //2000 is the timeout for the splash

    }

    public void login(View view) {
        String username = etUsername.getText().toString(), password = etPassword.getText().toString();
        int code = Integer.parseInt(etCode.getText().toString());
        if (!username.isEmpty() && !password.isEmpty()) {
            loginUser(code, username, password, view);
        }
    }

    public void loginUser(final int code, String username, String password,final View v) {
        Call<List<UserLoginModel>> call = apiInterface.userLogin(code, username, password);
        if (call != null) {
            try {
                call.enqueue(new Callback<List<UserLoginModel>>() {
                    @Override
                    public void onResponse(Call<List<UserLoginModel>> call, Response<List<UserLoginModel>> response) {
                        try {
                            if (response.isSuccessful() && response.body() != null) {
                                if (response.body().get(0).isActive()) {
                                    Intent intent = new Intent(LoginActivity.this, LedgerSelectActivity.class);
                                    String companyName = response.body().get(0).getOwnerName();
                                    intent.putExtra("CompanyName", companyName);
                                    Log.d(getString(R.string.txtLogTag), response.toString());

                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    editor.putInt(getString(R.string.prefKey), code);
                                    editor.apply();
                                    startActivity(intent);
                                    Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(v, "Welcome, " + response.body().get(0).getOwnerName(), Snackbar.LENGTH_LONG).show();
                                    Log.d(getString(R.string.txtLogTag), "User Logged in!");
                                } else {
                                    Toast.makeText(LoginActivity.this, "Sorry, but your account is not activated!", Toast.LENGTH_SHORT).show();
                                    Snackbar.make(v, "Account not activated!", Snackbar.LENGTH_LONG).show();
                                    Log.d(getString(R.string.txtLogTag), "Account not activated!");
                                }
                            } else {
                                Toast.makeText(LoginActivity.this, "Wrong Credentials!", Toast.LENGTH_SHORT).show();
                                Log.d(getString(R.string.txtLogTag), "Response was not successful!");
                            }
                        } catch (Exception e) {
                            Log.d(getString(R.string.txtLogTag), "Error while getting User's info: " + e.getMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<UserLoginModel>> call, Throwable t) {
                        Log.d(getString(R.string.txtLogTag), "onFailure: " + t.getMessage());
                        Toast.makeText(LoginActivity.this, "Retrofit onFailure: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                Log.d(getString(R.string.txtLogTag), "loginUser Exception: " + e.getMessage());
            }
        } else {
            Log.d(getString(R.string.txtLogTag), "Call was null");
        }
    }

    @Override
    public void onBackPressed() {
        this.finishAffinity();
    }
}