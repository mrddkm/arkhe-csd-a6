package com.arkhe.csd.di

import com.arkhe.csd.utils.ClipboardManager
import com.arkhe.csd.utils.PdfGenerator
import com.arkhe.csd.utils.ShareManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { ClipboardManager(androidContext()) }
    single { ShareManager(androidContext()) }
    single { PdfGenerator(androidContext()) }
}