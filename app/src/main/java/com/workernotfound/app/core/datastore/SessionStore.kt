package com.workernotfound.app.core.datastore

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.google.gson.Gson
import com.workernotfound.app.feature.auth.domain.AuthSession
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.KeyStore
import java.util.UUID
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/** AES-GCM ciphertext in noBackupFilesDir; encryption key stays in Android Keystore. */
@Singleton
class SessionStore @Inject constructor(@ApplicationContext context: Context) : SessionStorage {
    private val directory = context.noBackupFilesDir
    private val gson = Gson()
    private val file = File(directory, "auth-session.enc")
    private val deviceFile = File(directory, "auth-device-id")

    @Synchronized override fun deviceId(): String {
        if (deviceFile.exists()) return deviceFile.readText()
        return UUID.randomUUID().toString().also { deviceFile.writeText(it) }
    }

    @Synchronized override fun read(): StoredSession? {
        if (!file.exists()) return null
        return try {
            val parts = file.readText().split(":", limit = 2)
            require(parts.size == 2)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
            val cleartext = cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP))
            val session = gson.fromJson(cleartext.toString(Charsets.UTF_8), StoredSession::class.java)
            require(session != null && session.memberId > 0 && session.role in setOf("OWNER", "WORKER") && !session.accessToken.isNullOrBlank() && !session.refreshToken.isNullOrBlank())
            session
        } catch (_: java.security.GeneralSecurityException) {
            // A removed/invalidated device key requires a new login.
            clear()
            null
        } catch (_: IllegalArgumentException) {
            clear()
            null
        } catch (_: com.google.gson.JsonParseException) {
            clear()
            null
        }
    }

    @Synchronized override fun save(session: StoredSession) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(gson.toJson(session).toByteArray(Charsets.UTF_8))
        val temporary = File(directory, "auth-session.tmp")
        temporary.writeText(Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" + Base64.encodeToString(encrypted, Base64.NO_WRAP))
        check(temporary.renameTo(file)) { "Could not persist encrypted session" }
    }

    @Synchronized override fun clear() { file.delete() }

    @Synchronized override fun replace(expected: StoredSession, updated: StoredSession): Boolean {
        if (read() != expected) return false
        save(updated)
        return true
    }

    @Synchronized override fun clearIfCurrent(expected: StoredSession) {
        if (read() == expected) clear()
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }

    private companion object { const val KEY_ALIAS = "workernotfound.auth.session" }
}

data class StoredSession(val memberId: Long, val role: String, val accessToken: String, val refreshToken: String) {
    fun toDomain() = AuthSession(memberId, role)
}

interface SessionStorage {
    fun deviceId(): String
    fun read(): StoredSession?
    fun save(session: StoredSession)
    fun clear()
    fun replace(expected: StoredSession, updated: StoredSession): Boolean
    fun clearIfCurrent(expected: StoredSession)
}
