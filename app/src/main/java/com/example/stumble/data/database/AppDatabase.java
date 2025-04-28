package com.example.stumble.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.example.stumble.data.dao.UserDao;
import com.example.stumble.data.model.User;

@Database(entities = {User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
}