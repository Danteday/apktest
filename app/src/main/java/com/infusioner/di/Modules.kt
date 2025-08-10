package com.infusioner.di
import android.content.Context
import androidx.room.Room
import com.infusioner.data.local.AppDatabase
import com.infusioner.data.repository.*
import com.infusioner.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
abstract class BindModule{
  @Binds @Singleton abstract fun dRepo(impl: DeviceRepositoryImpl): DeviceRepository
  @Binds @Singleton abstract fun rRepo(impl: RecipeRepositoryImpl): RecipeRepository
  @Binds @Singleton abstract fun sRepo(impl: DeviceStatusRepositoryImpl): DeviceStatusRepository
}

@Module @InstallIn(SingletonComponent::class)
object AppModule{
  @Provides @Singleton fun db(@ApplicationContext c:Context) = Room.databaseBuilder(c, AppDatabase::class.java, "infusioner.db").build()
  @Provides @Singleton fun okHttp() = OkHttpClient.Builder().build()
  @Provides @Singleton fun retrofitBuilder(client: OkHttpClient) = Retrofit.Builder().client(client)
}
