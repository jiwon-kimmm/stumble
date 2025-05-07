package com.example.stumble.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.stumble.data.model.User;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM users")
    List<User> getAll();

    @Query("SELECT * FROM users WHERE userId IN (:userIds)")
    List<User> loadAllByIds(int[] userIds);

    @Query("SELECT * FROM users WHERE username LIKE :username LIMIT 1")
    User findByUsername(String username);

    @Insert
    void insertAll(User... users);

    @Delete
    void delete(User user);
}
