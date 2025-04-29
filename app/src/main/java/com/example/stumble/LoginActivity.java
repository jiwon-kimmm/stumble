package com.example.stumble;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.room.Room;

import com.example.stumble.data.dao.UserDao;
import com.example.stumble.data.database.AppDatabase;
import com.example.stumble.data.model.User;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    public String username;
    public String password;

    private Handler dbHandler;
    private AppDatabase appDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        HandlerThread dbThread = new HandlerThread("Database worker thread");
        dbThread.start();

        dbHandler = new Handler(dbThread.getLooper());

        appDb = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "app_db").build();

        ((Button)findViewById(R.id.signupButton)).setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               username = ((EditText)findViewById((R.id.UsernameText))).getText().toString();
               password = ((EditText)findViewById((R.id.PasswordText))).getText().toString();
               Log.d("UserInfo","Sign up: " + username + ", " + password);

               dbHandler.post(() -> {
                    User newUser = new User();
                    newUser.username = username;
                    newUser.password = password;
                    appDb.userDao().insertAll(newUser);
               });
           }
        });

        ((Button)findViewById(R.id.loginButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = ((EditText)findViewById((R.id.UsernameText))).getText().toString();
                password = ((EditText)findViewById((R.id.PasswordText))).getText().toString();
                Log.d("UserInfo","Log in: " + username + ", " + password);

                dbHandler.post(() -> {
                    User currUser = appDb.userDao().findByUsername(username);
                    if (Objects.equals(password, currUser.password)) {
                        Log.d("UserInfo", "login success");
                        startActivity(new Intent(LoginActivity.this, SignUpSurvey.class));
                    } else {
                        Log.d("UserInfo", "login fail");
                    }
                });
            }
        });




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (appDb != null) {
            appDb.close();
        }
    }


}