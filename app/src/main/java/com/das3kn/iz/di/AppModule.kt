package com.das3kn.iz.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.firebase.ktx.Firebase
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.repository.ChatRepository
import com.das3kn.iz.data.repository.CommentRepository
import com.das3kn.iz.data.repository.PostRepository
import com.das3kn.iz.data.repository.SavedPostRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Singleton
    fun provideFirebaseMessaging(): FirebaseMessaging = Firebase.messaging

    @Provides
    @Singleton
    fun provideAuthRepository(
        auth: FirebaseAuth,
        firestore: FirebaseFirestore,
        messaging: FirebaseMessaging
    ): AuthRepository = AuthRepository(auth, firestore, messaging)

    @Provides
    @Singleton
    fun providePostRepository(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): PostRepository = PostRepository(firestore, storage)

    @Provides
    @Singleton
    fun provideCommentRepository(
        firestore: FirebaseFirestore
    ): CommentRepository = CommentRepository(firestore)

    @Provides
    @Singleton
    fun provideChatRepository(
        firestore: FirebaseFirestore
    ): ChatRepository = ChatRepository(firestore)

    @Provides
    @Singleton
    fun provideSavedPostRepository(
        firestore: FirebaseFirestore
    ): SavedPostRepository = SavedPostRepository(firestore)
}
