package com.raksys.feature.connection

import org.koin.dsl.module

val connectionModule = module {
    single { ConnectionRepository() }
    single { ConnectionPresenter(get(), get(), get(), get(), get()) }
}
