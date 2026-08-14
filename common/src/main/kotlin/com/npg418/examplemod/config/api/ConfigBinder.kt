package com.npg418.examplemod.config.api

interface ConfigBinder {
    fun <T : Any> source(entry: ConfigEntry<T>): () -> T
    fun section(name: String, block: ConfigBinder.() -> Unit)
}
