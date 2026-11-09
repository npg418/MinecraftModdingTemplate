package com.npg418.examplemod.config.api

interface ConfigPropertyVisitor<R> {
    fun <T : Any> visit(name: String, p: NormalConfigProperty<T>): R
    fun <E : Enum<E>> visit(name: String, p: EnumConfigProperty<E>): R
    fun <T : Comparable<T>> visit(name: String, p: RangedConfigProperty<T>): R
    fun <T : Any> visit(name: String, p: OneOfConfigProperty<T>): R
    fun <E : Any> visit(name: String, p: ListConfigProperty<E>): R
}