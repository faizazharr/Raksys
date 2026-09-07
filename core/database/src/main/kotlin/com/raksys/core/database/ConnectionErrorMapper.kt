package com.raksys.core.database

import com.raksys.core.model.ConnectionProfile
import java.net.ConnectException
import java.net.UnknownHostException

/**
 * Turns a raw JDBC/network exception into a message a non-DBA user can act on, instead of a
 * driver's internal stack-trace-flavored text.
 */
fun friendlyConnectionError(profile: ConnectionProfile, throwable: Throwable): String {
    val message = throwable.message.orEmpty()
    val target = "${profile.host}:${profile.port}"

    return when {
        throwable is ConnectException || message.contains("Connection refused", ignoreCase = true) ->
            "Gak bisa connect ke $target — server ${profile.dbType.name} mungkin belum jalan, atau port-nya salah. Cek lagi host/port-nya."

        throwable is UnknownHostException ->
            "Host '${profile.host}' gak ditemukan — cek lagi alamatnya, atau ini mungkin masalah DNS/network."

        message.contains("timeout", ignoreCase = true) || message.contains("timed out", ignoreCase = true) ->
            "Koneksi ke $target timeout — cek firewall, VPN, atau server-nya lagi lambat merespons."

        message.contains("password authentication failed", ignoreCase = true) ||
            message.contains("Access denied for user", ignoreCase = true) ->
            "Username atau password salah."

        message.contains("already exists", ignoreCase = true) ->
            "Database '${profile.database}' udah ada di server ini — pilih nama lain, atau connect ke yang udah ada (bukan mode buat baru)."

        message.contains("does not exist", ignoreCase = true) && message.contains("database", ignoreCase = true) ->
            "Database '${profile.database}' gak ditemukan di server ini."

        message.contains("SSL", ignoreCase = true) ->
            "Gagal negosiasi SSL/TLS ke $target — coba matikan opsi SSL kalau server-nya gak mewajibkan itu."

        else -> message.ifBlank { "Gagal connect ke database." }
    }
}
