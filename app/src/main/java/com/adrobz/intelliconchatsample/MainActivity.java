package com.adrobz.intelliconchatsample;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.adrobz.intelliconlibrary.Interface.ResponseCallback;
import com.adrobz.intelliconlibrary.MyLibrary;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    EditText userName;
    Button proceedButton;
    ProgressBar mProgressBar;
    String id = "f7e7b07c66197d4b3570e9992f279318";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyLibrary myLibrary = new MyLibrary(MainActivity.this);

        userName = findViewById(R.id.userId);
        proceedButton = findViewById(R.id.proceedBtn);
        mProgressBar = findViewById(R.id.progress_bar);

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userName.getText().toString().isEmpty()) {
                    myLibrary.myToast(MainActivity.this, "Username cannot be empty");
                } else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    myLibrary.fetchUserToken("token/mobile", id, userName.getText().toString().toLowerCase().trim(), MainActivity.this, new ResponseCallback() {
                        @Override
                        public void onSuccess(String response) {
                            JSONObject jsonObject = new JSONObject();
                            try {
                                jsonObject = new JSONObject(response);
                                String token = jsonObject.getString("token");
                                String config = jsonObject.getString("configs");
                                mProgressBar.setVisibility(View.GONE);
                                Intent intent = new Intent(MainActivity.this, ChatActivity.class);
                                intent.putExtra("token", token);
                                intent.putExtra("configs", config);
                                intent.putExtra("userId", userName.getText().toString().toLowerCase().trim());
                                startActivity(intent);
                            } catch (JSONException e) {
                                mProgressBar.setVisibility(View.GONE);
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onError(String error) {
                            mProgressBar.setVisibility(View.GONE);
                            myLibrary.myToast(MainActivity.this, error);
                        }
                    });
                }
            }
        });

    }
}