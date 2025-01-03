package dev.itsvic.parceltracker.db

import dev.itsvic.parceltracker.api.Service

// Parcel metadata and mock DB for demo mode.

private var demoId = 0

private fun defineDemoParcel(name: String, id: String): Parcel {
    return Parcel(demoId++, name, id, null, Service.EXAMPLE)
}

val demoModeParcels = listOf(
    defineDemoParcel("Phone case", "2503894188"),
    defineDemoParcel("Keyring tracker", "7301626157"),
    defineDemoParcel("Game merch", "6171197286"),
)
