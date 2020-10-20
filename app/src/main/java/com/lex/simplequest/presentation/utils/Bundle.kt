package com.lex.simplequest.presentation.utils

import android.os.Bundle
import android.os.Parcelable

private object Consts {
    const val MAP_KEYS_SUFFIX = "_keys"
    const val MAP_VALUES_SUFFIX = "_values"
}

fun <K : Parcelable, V : Parcelable> Bundle.putMap(key: String, map: Map<K, V>) {
    val keys = arrayListOf<K>()
    val values = arrayListOf<V>()
    map.entries.forEach { (mapKey, mapValue) ->
        keys.add(mapKey)
        values.add(mapValue)
    }
    this.putParcelableArrayList("$key${Consts.MAP_KEYS_SUFFIX}", keys)
    this.putParcelableArrayList("$key${Consts.MAP_VALUES_SUFFIX}", values)
}

fun <K : Parcelable, V : Parcelable> Bundle.getMap(key: String, defaultValue: Map<K, V>? = null): Map<K, V>? {
    val keyKeys = "$key${Consts.MAP_KEYS_SUFFIX}"
    val keyValues = "$key${Consts.MAP_VALUES_SUFFIX}"
    return if (this.containsKey(keyKeys) && this.containsKey(keyValues)) {
        val keys = this.getParcelableArrayList<K>(keyKeys)!!
        val values = this.getParcelableArrayList<V>(keyValues)!!
        val map = mutableMapOf<K, V>()
        require(keys.size == values.size)
        for (index in 0 until keys.size) {
            map[keys[index]] = values[index]
        }
        map.toMap()
    } else defaultValue
}

fun <V : Enum<V>> Bundle.putMapOfEnum(key: String, map: Map<String, V>) {
    val keys = arrayListOf<String>()
    val values = arrayListOf<String>()
    map.entries.forEach { (mapKey, mapValue) ->
        keys.add(mapKey)
        values.add(mapValue.name)
    }
    this.putStringArrayList("$key${Consts.MAP_KEYS_SUFFIX}", keys)
    this.putStringArrayList("$key${Consts.MAP_VALUES_SUFFIX}", values)
}

fun <V : Enum<V>> Bundle.getMapOfEnum(
    key: String,
    eClass: Class<V>,
    defaultValue: Map<String, V>? = null
): Map<String, V>? {
    val keyKeys = "$key${Consts.MAP_KEYS_SUFFIX}"
    val keyValues = "$key${Consts.MAP_VALUES_SUFFIX}"
    return if (this.containsKey(keyKeys) && this.containsKey(keyValues)) {
        val keys = this.getStringArrayList(keyKeys)!!
        val values = this.getStringArrayList(keyValues)!!
        val map = mutableMapOf<String, V>()
        require(keys.size == values.size)
        for (index in 0 until keys.size) {
            map[keys[index]] = java.lang.Enum.valueOf(eClass, values[index])
        }
        map.toMap()
    } else defaultValue
}

fun <V : Parcelable> Bundle.putParcelableMap(key: String, map: Map<String, V>) {
    val keys = arrayListOf<String>()
    val values = arrayListOf<V>()
    map.entries.forEach { (mapKey, mapValue) ->
        keys.add(mapKey)
        values.add(mapValue)
    }
    this.putStringArrayList("$key${Consts.MAP_KEYS_SUFFIX}", keys)
    this.putParcelableArrayList("$key${Consts.MAP_VALUES_SUFFIX}", values)
}

fun <V : Parcelable> Bundle.getParcelableMap(key: String, defaultValue: Map<String, V>? = null): Map<String, V>? {
    val keyKeys = "$key${Consts.MAP_KEYS_SUFFIX}"
    val keyValues = "$key${Consts.MAP_VALUES_SUFFIX}"
    return if (this.containsKey(keyKeys) && this.containsKey(keyValues)) {
        val keys = this.getStringArrayList(keyKeys)!!
        val values = this.getParcelableArrayList<V>(keyValues)!!
        val map = mutableMapOf<String, V>()
        require(keys.size == values.size)
        for (index in 0 until keys.size) {
            map[keys[index]] = values[index]
        }
        map.toMap()
    } else defaultValue
}

fun <V : Parcelable> Bundle.putParcelableMapList(key: String, map: Map<String, List<V>>) {
    this.putStringArrayList("$key${Consts.MAP_KEYS_SUFFIX}", ArrayList(map.keys))
    map.entries.forEach { (mapKey, mapValue) ->
        this.putParcelableArrayList("$key$mapKey${Consts.MAP_VALUES_SUFFIX}", ArrayList(mapValue))
    }
}

fun <V : Parcelable> Bundle.getParcelableMapList(
    key: String,
    defaultValue: Map<String, List<V>>? = null
): Map<String, List<V>>? {
    val keyKeys = "$key${Consts.MAP_KEYS_SUFFIX}"
    return if (this.containsKey(keyKeys)) {
        val keys = this.getStringArrayList(keyKeys)!!
        val map = mutableMapOf<String, List<V>>()
        for (index in 0 until keys.size) {
            val mapKey = keys[index]
            val valueKey = "$key$mapKey${Consts.MAP_VALUES_SUFFIX}"
            map[mapKey] = this.getParcelableArrayList(valueKey)!!
        }
        map.toMap()
    } else defaultValue
}

fun Bundle.putMapOfStringSet(key: String, map: Map<String, Set<String>>) {
    this.putStringArrayList("$key${Consts.MAP_KEYS_SUFFIX}", ArrayList(map.keys))
    map.entries.forEach { (mapKey, mapValue) ->
        this.putStringArrayList("$key$mapKey${Consts.MAP_VALUES_SUFFIX}", ArrayList(mapValue))
    }
}

fun Bundle.getMapOfStringSet(
    key: String,
    defaultValue: Map<String, Set<String>>? = null
): Map<String, Set<String>>? {
    val keyKeys = "$key${Consts.MAP_KEYS_SUFFIX}"
    return if (this.containsKey(keyKeys)) {
        val keys = this.getStringArrayList(keyKeys)!!
        val map = mutableMapOf<String, Set<String>>()
        for (index in 0 until keys.size) {
            val mapKey = keys[index]
            val valueKey = "$key$mapKey${Consts.MAP_VALUES_SUFFIX}"
            map[mapKey] = this.getStringArrayList(valueKey)!!.toSet()
        }
        map.toMap()
    } else defaultValue
}