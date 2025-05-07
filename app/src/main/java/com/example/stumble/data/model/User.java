package com.example.stumble.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    public int userId;

    @ColumnInfo(name = "username")
    public String username;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "HomeLatitude")
    public double homeLatitude;

    @ColumnInfo(name = "HomeLongitude")
    public double homeLongitude;

    @ColumnInfo(name = "HomeLongitude")
    public String password;
}