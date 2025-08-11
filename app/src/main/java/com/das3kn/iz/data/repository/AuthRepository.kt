package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    
    val currentUser: FirebaseUser?
        get() = auth.currentUser

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            updateFCMToken()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(email: String, password: String, username: String, displayName: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user!!
            
            // Firestore'a kullanıcı bilgilerini kaydet
            val userData = User(
                id = user.uid,
                username = username,
                email = email,
                displayName = displayName
            )
            
            firestore.collection("users").document(user.uid).set(userData).await()
            updateFCMToken()
            
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
    }

    suspend fun updateFCMToken() {
        try {
            val token = messaging.token.await()
            currentUser?.let { user ->
                firestore.collection("users").document(user.uid)
                    .update("fcmToken", token).await()
            }
        } catch (e: Exception) {
            // Token alınamadı
        }
    }

    suspend fun getUserProfile(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Kullanıcı bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): Result<Unit> {
        return try {
            currentUser?.let { user ->
                firestore.collection("users").document(user.uid).update(updates).await()
                Result.success(Unit)
            } ?: Result.failure(Exception("Kullanıcı giriş yapmamış"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
