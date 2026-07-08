package com.example.jhdkasjhd.core.network

import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Uses system DNS first, then Google DNS-over-HTTPS when the emulator/device DNS fails
 * (common UnknownHostException on Android emulators).
 */
object NetworkDns {

    private val dnsOverHttps: Dns by lazy {
        val bootstrapClient = OkHttpClient.Builder().build()
        DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://dns.google/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("8.8.8.8"),
                    InetAddress.getByName("8.8.4.4"),
                    InetAddress.getByName("1.1.1.1")
                )
            )
            .build()
    }

    val fallback: Dns = object : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                Dns.SYSTEM.lookup(hostname)
            } catch (systemError: UnknownHostException) {
                try {
                    dnsOverHttps.lookup(hostname)
                } catch (dohError: UnknownHostException) {
                    throw UnknownHostException(
                        "No se pudo resolver $hostname. Verifica tu conexión a Internet."
                    ).apply {
                        initCause(dohError)
                    }
                }
            }
        }
    }
}
