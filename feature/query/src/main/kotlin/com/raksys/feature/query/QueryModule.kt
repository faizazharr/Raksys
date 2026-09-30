package com.raksys.feature.query

import com.raksys.core.ui.RaksysSettings
import org.koin.dsl.module

val queryModule = module {
    single { QueryPresenter(get(), FileQueryHistoryStore(enabled = { RaksysSettings.saveQueryHistory })) }
}
