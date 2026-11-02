package com.workernotfound.app.feature.account.data

import com.workernotfound.app.BuildConfig
import com.workernotfound.app.core.datastore.SessionStore
import com.workernotfound.app.core.datastore.StoredSession
import com.workernotfound.app.core.network.ApiException
import com.workernotfound.app.core.network.BackendAccess
import com.workernotfound.app.feature.account.domain.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AccountRepositoryImpl @Inject constructor(private val api: AccountApi,
    private val sessions: SessionStore, private val backend: BackendAccess) : AccountRepository {
    private var original: StoredSession? = null
    private suspend fun requireAccount() {
        if (BuildConfig.USE_DEMO_DATA) throw ApiException("LIVE_ACCOUNT_ONLY")
        backend.requireSession()
        if (original == null) original = withContext(Dispatchers.IO) { sessions.read() }
    }
    override suspend fun send(channel: ContactChannel, target: String) {
        requireAccount()
        val response = if (channel == ContactChannel.EMAIL) api.email(EmailTarget(target)) else api.phone(PhoneTarget(target))
        if (!response.success) throw ApiException(response.code)
    }
    override suspend fun change(key: String, channel: ContactChannel, target: String, code: String): AccountCommand {
        requireAccount()
        return api.change(key, ContactChangeBody(channel.name, target, code)).requireData().toDomain(key)
    }
    override suspend fun contactStatus(key: String): AccountCommand {
        requireAccount()
        return api.contact(key).requireData().toDomain(key)
    }
    override suspend fun withdraw(key: String): AccountCommand {
        requireAccount()
        return api.withdraw(key).requireData().toDomain(key)
    }
    override suspend fun withdrawalStatus(key: String): AccountCommand {
        requireAccount()
        return api.withdrawal(key).requireData().toDomain(key)
    }
    override suspend fun clearCompletedSession() = withContext(Dispatchers.IO) {
        original?.let { sessions.clearIfCurrent(it) }
        Unit
    }
}
@Module @InstallIn(SingletonComponent::class)
object AccountModule {
    @Provides fun api(client: OkHttpClient): AccountApi = Retrofit.Builder().baseUrl(BuildConfig.API_BASE_URL)
        .client(client).addConverterFactory(GsonConverterFactory.create()).build().create(AccountApi::class.java)
    @Provides fun repository(impl: AccountRepositoryImpl): AccountRepository = impl
}
