package fr.msg.simbaste.phenixtest.service

import com.google.gson.GsonBuilder
import fr.msg.simbaste.phenixtest.App
import fr.msg.simbaste.phenixtest.BuildConfig
import fr.msg.simbaste.phenixtest.utils.SslUtils
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.SecureRandom
import java.util.*
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class RetrofitInstance {

    companion object {
        private const val BASE_URL =
            BuildConfig.BASE_URL
        private val interceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        private val client = OkHttpClient.Builder().apply {
            try {
                val keyStore = SslUtils.getKeyStore(App.getAppContext(), "openweather_cert.pem")
                val sslContext = SSLContext.getInstance("SSL")
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)
                val trustManagers = trustManagerFactory.trustManagers
                check(!(trustManagers.count() != 1 || trustManagers[0] !is X509TrustManager)) {
                    "Unexpected default trust managers:" + Arrays.toString(
                        trustManagers
                    )
                }
                val trustManager = trustManagers[0] as X509TrustManager
                sslContext.init(null, trustManagerFactory.trustManagers, SecureRandom())
                this.addInterceptor(interceptor)
                    .sslSocketFactory(sslContext.socketFactory, trustManager)
            } catch (e: Exception) {
                val msg = "Error during creating SslContext for certificate from assets"
                e.printStackTrace()
                throw RuntimeException(msg)
            }
        }

        fun getRetrofitInstance(): Retrofit {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client.build())
                .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
                .build()
        }
    }

}