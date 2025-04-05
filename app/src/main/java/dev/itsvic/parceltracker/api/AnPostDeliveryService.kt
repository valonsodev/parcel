// SPDX-License-Identifier: GPL-3.0-or-later
package dev.itsvic.parceltracker.api

import dev.itsvic.parceltracker.R

object AnPostDeliveryService : DeliveryService {
    override val nameResource: Int = R.string.service_example
    override val acceptsPostCode: Boolean = false
    override val requiresPostCode: Boolean = false
}
