package com.sayanjalinexus.meshchat.core

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Common base for feature ViewModels.
 *
 * Provides [launchSafely], a single funnel for coroutine work that:
 *  - always runs on [viewModelScope] (cancelled automatically with the VM),
 *  - routes uncaught exceptions to [onUnhandledError] instead of crashing
 *    the app, so every ViewModel gets consistent error handling without
 *    boilerplate try/catch at every call site.
 *
 * Feature ViewModels are expected to override [onUnhandledError] to surface
 * failures into their own UI state (see [com.sayanjalinexus.meshchat.ui.home.HomeViewModel]
 * for the pattern).
 */
abstract class BaseViewModel : ViewModel() {

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onUnhandledError(throwable)
    }

    protected fun launchSafely(block: suspend () -> Unit): Job =
        viewModelScope.launch(exceptionHandler) { block() }

    protected open fun onUnhandledError(throwable: Throwable) {
        // Default no-op; overridden by ViewModels that expose an error state.
    }
}
