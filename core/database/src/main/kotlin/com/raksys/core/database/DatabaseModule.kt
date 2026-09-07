package com.raksys.core.database

import org.koin.dsl.module

val databaseModule = module {
    single { SshTunnelManager(get()) }
    single<DatabaseDriver> { JdbcDatabaseDriver(get(), get()) }
    single<DocumentDatabaseDriver> { MongoDatabaseDriver(get(), get()) }
    single<KeyValueDriver> { RedisKeyValueDriver(get(), get()) }
    single<PermissionDriver> { JdbcPermissionDriver(get()) }
}
