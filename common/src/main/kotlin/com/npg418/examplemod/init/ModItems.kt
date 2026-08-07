package com.npg418.examplemod.init

import com.google.common.base.Suppliers
import com.npg418.examplemod.items.ExampleItem
import java.util.function.Supplier

val EXAMPLE_ITEM: Supplier<ExampleItem> = Suppliers.memoize { ExampleItem() }

val ITEM_MAP = mapOf(
    "example_item" to EXAMPLE_ITEM
)
