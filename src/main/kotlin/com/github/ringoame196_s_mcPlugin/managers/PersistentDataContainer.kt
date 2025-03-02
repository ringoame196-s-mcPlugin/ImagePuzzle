package com.github.ringoame196_s_mcPlugin.managers

import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.Plugin

class PersistentDataContainer(private val plugin: Plugin) {
    fun setCustomNBT(entity: Entity, key: String, value: String) {
        val container = entity.persistentDataContainer
        val namespacedKey = NamespacedKey(plugin, key)
        container.set(namespacedKey, PersistentDataType.STRING, value)
    }

    fun acquisitionCustomNBT(entity: Entity, key: String): String? {
        val container = entity.persistentDataContainer
        val namespacedKey = NamespacedKey(plugin, key)
        return container.get(namespacedKey, PersistentDataType.STRING)
    }
}
