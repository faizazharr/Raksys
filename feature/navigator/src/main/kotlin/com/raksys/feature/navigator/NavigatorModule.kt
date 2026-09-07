package com.raksys.feature.navigator

import org.koin.dsl.module

val navigatorModule = module {
    single { NavigatorPresenter(get()) }
}
