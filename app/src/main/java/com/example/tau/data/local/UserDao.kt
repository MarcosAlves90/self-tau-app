package com.example.tau.data.local

import android.content.Context

class UserDao(private val context: Context) {
    private val db = AppDatabase(context)

    fun saveUserId(userId: Int) {
        db.saveUserId(userId)
    }

    fun getUserId(): Int? {
        return db.getUserId()
    }

    fun clearSession() {
        db.clearSession()
    }

    fun isLoggedIn(): Boolean {
        return db.isLoggedIn()
    }
}
