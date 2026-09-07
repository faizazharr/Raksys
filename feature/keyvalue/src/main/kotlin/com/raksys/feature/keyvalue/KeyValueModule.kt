package com.raksys.feature.keyvalue

import org.koin.dsl.module

val keyValueModule = module {
    single { KeyValuePresenter(get()) }
}
