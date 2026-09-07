package com.raksys.feature.permission

import org.koin.dsl.module

val permissionModule = module {
    single { PermissionPresenter(get(), get()) }
}
