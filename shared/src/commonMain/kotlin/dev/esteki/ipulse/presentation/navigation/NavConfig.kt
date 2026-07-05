package dev.esteki.ipulse.presentation.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

val navConfig = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Dashboard::class, Route.Dashboard.serializer())
            subclass(Route.DeviceDetail::class, Route.DeviceDetail.serializer())
        }
    }
}
