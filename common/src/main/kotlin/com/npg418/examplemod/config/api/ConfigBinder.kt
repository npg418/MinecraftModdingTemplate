package com.npg418.examplemod.config.api

interface ConfigBinder {
    fun <T : Any> bindEntry(name: String, entry: ConfigEntry<T>): () -> T
    fun <T : Comparable<T>> bindRangedEntry(name: String, entry: RangedConfigEntry<T>): () -> T
    fun bindSection(section: ConfigSection)
}
