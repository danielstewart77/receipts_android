package com.sparktobloom.receipts.utils

import android.content.Context
import com.sparktobloom.receipts.R
import okhttp3.OkHttpClient
import java.io.InputStream
import java.security.KeyStore
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class DevOkHttpClient {

    companion object {

        fun getDevOkHttpClient(context: Context): OkHttpClient {
            try {
                // Load CAs from an InputStream
                val certificateFactory = CertificateFactory.getInstance("X.509")
                val certificate: InputStream =
                    context.resources.openRawResource(R.raw.cert) // Load your certificate here
                val ca: X509Certificate = certificate.use {
                    certificateFactory.generateCertificate(it) as X509Certificate
                }

                // Create a KeyStore containing our trusted CAs
                val keyStore = KeyStore.getInstance(KeyStore.getDefaultType()).apply {
                    load(null, null)
                    setCertificateEntry("ca", ca)
                }

                // Create a TrustManager that trusts the CAs in our KeyStore
                val trustManagerFactory =
                    TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                        .apply {
                            init(keyStore)
                        }

                // Create an SSLContext that uses our TrustManager
                val sslContext = SSLContext.getInstance("TLS").apply {
                    init(null, trustManagerFactory.trustManagers, java.security.SecureRandom())
                }

                val trustManagers = trustManagerFactory.trustManagers
                require(trustManagers.size == 1 && trustManagers[0] is X509TrustManager) {
                    "Unexpected default trust managers: ${trustManagers.contentToString()}"
                }
                val trustManager = trustManagers[0] as X509TrustManager

                // Build the OkHttpClient
                return OkHttpClient.Builder()
                    .sslSocketFactory(sslContext.socketFactory, trustManager)
                    .hostnameVerifier { hostname, session -> true }
                    .build()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}