package com.raksys.feature.query

import org.koin.dsl.module

val queryModule = module {
    single { QueryPresenter(get()) }
}
