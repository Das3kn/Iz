package com.das3kn.iz.di

import com.das3kn.iz.data.supabase.SupabaseClient
import com.das3kn.iz.data.supabase.SupabaseStorageService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SupabaseModule {

    @Provides
    @Singleton
    fun provideStorage(): Storage {
        return SupabaseClient.client.storage
    }

    @Provides
    @Singleton
    fun provideSupabaseStorageService(storage: Storage): SupabaseStorageService {
        return SupabaseStorageService(storage)
    }
}
