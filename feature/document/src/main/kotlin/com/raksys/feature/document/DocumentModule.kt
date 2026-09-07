package com.raksys.feature.document

import org.koin.dsl.module

val documentModule = module {
    single { DocumentPresenter(get()) }
}
