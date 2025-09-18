package pl.soulsnaps.di

import org.koin.core.module.Module

/**
 * Platform-specific module declaration
 * Will be provided by platform-specific modules
 */
expect val platformModule: Module
