package com.raksys.feature.erd

import org.koin.dsl.module

val erdModule = module {
    single { ErdPresenter(get()) }
}
