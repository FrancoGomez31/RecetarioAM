package com.app.core.data.di

import com.app.core.data.NominatimApi
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Arma la pieza de red una sola vez para toda la app.
 * Nominatim exige un User-Agent que identifique a la aplicación; sin él bloquea las consultas.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/"
    private const val USER_AGENT = "RecetarioAM/1.0 (Android; trabajo practico academico)"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("User-Agent", USER_AGENT)
                    .build()
            )
        }
        .build()

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        // La API devuelve muchos campos que no usamos: se ignoran en vez de fallar.
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideNominatimApi(client: OkHttpClient, json: Json): NominatimApi =
        Retrofit.Builder()
            .baseUrl(NOMINATIM_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NominatimApi::class.java)
}