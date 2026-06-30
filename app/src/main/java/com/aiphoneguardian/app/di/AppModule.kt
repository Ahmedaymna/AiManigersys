package com.aiphoneguardian.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.aiphoneguardian.app.data.local.GuardianDatabase
import com.aiphoneguardian.app.data.local.dao.ChatMessageDao
import com.aiphoneguardian.app.data.local.dao.QuarantinedFileDao
import com.aiphoneguardian.app.data.local.dao.ScanResultDao
import com.aiphoneguardian.app.data.remote.api.GeminiApiService
import com.aiphoneguardian.app.data.repository.AuthRepositoryImpl
import com.aiphoneguardian.app.data.repository.ChatRepositoryImpl
import com.aiphoneguardian.app.data.repository.FileRepositoryImpl
import com.aiphoneguardian.app.data.repository.ScanRepositoryImpl
import com.aiphoneguardian.app.data.repository.SettingsRepositoryImpl
import com.aiphoneguardian.app.data.repository.SubscriptionRepositoryImpl
import com.aiphoneguardian.app.data.repository.SystemMonitorRepositoryImpl
import com.aiphoneguardian.app.domain.repository.ScanRepository
import com.aiphoneguardian.app.domain.repository.SystemMonitorRepository
import com.aiphoneguardian.app.domain.repository.AuthRepository
import com.aiphoneguardian.app.domain.repository.ChatRepository
import com.aiphoneguardian.app.domain.repository.FileRepository
import com.aiphoneguardian.app.domain.repository.SettingsRepository
import com.aiphoneguardian.app.domain.repository.SubscriptionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "guardian_settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    @Provides
    @Singleton
    fun provideGuardianDatabase(@ApplicationContext context: Context): GuardianDatabase {
        return androidx.room.Room.databaseBuilder(
            context,
            GuardianDatabase::class.java,
            GuardianDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideScanResultDao(database: GuardianDatabase): ScanResultDao {
        return database.scanResultDao()
    }

    @Provides
    @Singleton
    fun provideChatMessageDao(database: GuardianDatabase): ChatMessageDao {
        return database.chatMessageDao()
    }

    @Provides
    @Singleton
    fun provideQuarantinedFileDao(database: GuardianDatabase): QuarantinedFileDao {
        return database.quarantinedFileDao()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideGeminiApiService(retrofit: Retrofit): GeminiApiService {
        return retrofit.create(GeminiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        dataStore: DataStore<Preferences>
    ): AuthRepository {
        return AuthRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideScanRepository(
        @ApplicationContext context: Context,
        scanResultDao: ScanResultDao,
        geminiApiService: GeminiApiService,
        gson: Gson
    ): ScanRepository {
        return ScanRepositoryImpl(context, scanResultDao, geminiApiService, gson)
    }

    @Provides
    @Singleton
    fun provideSystemMonitorRepository(
        @ApplicationContext context: Context,
        dataStore: DataStore<Preferences>
    ): SystemMonitorRepository {
        return SystemMonitorRepositoryImpl(context, dataStore)
    }

    @Provides
    @Singleton
    fun provideFileRepository(
        @ApplicationContext context: Context,
        quarantinedFileDao: QuarantinedFileDao,
        geminiApiService: GeminiApiService
    ): FileRepository {
        return FileRepositoryImpl(context, quarantinedFileDao, geminiApiService)
    }

    @Provides
    @Singleton
    fun provideChatRepository(
        chatMessageDao: ChatMessageDao,
        geminiApiService: GeminiApiService,
        subscriptionRepository: SubscriptionRepository
    ): ChatRepository {
        return ChatRepositoryImpl(chatMessageDao, geminiApiService, subscriptionRepository)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(
        dataStore: DataStore<Preferences>
    ): SubscriptionRepository {
        return SubscriptionRepositoryImpl(dataStore)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }
}
