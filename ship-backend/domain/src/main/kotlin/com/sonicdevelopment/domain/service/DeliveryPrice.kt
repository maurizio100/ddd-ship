package com.sonicdevelopment.domain.service

import com.sonicdevelopment.domain.model.Cargo
import com.sonicdevelopment.domain.model.values.CargoId
import com.sonicdevelopment.domain.model.values.Money

object DeliveryPrice {
    fun of(cargo: List<Cargo>, prices: Map<CargoId, Money>): Money? = TODO("STORY-045")
}
