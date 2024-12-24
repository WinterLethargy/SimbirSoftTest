package com.example.simbirsofttest.core.di

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val sfDispatcher: SSDispatchers)

enum class SSDispatchers {
    Default,
    IO,
}