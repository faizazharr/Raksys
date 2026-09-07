package com.raksys.core.security

import org.koin.dsl.module

val securityModule = module {
    single<CredentialStore> { KeyringCredentialStore() }
}
