package dev.esteki.ipulse.domain.usecase

import androidx.paging.PagingData
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.repository.DeviceRepository
import kotlinx.coroutines.flow.Flow

class ObserveDevicesPaged(
    private val deviceRepository: DeviceRepository
) {
    operator fun invoke(): Flow<PagingData<Device>> = deviceRepository.observeDevicesPaged()
}
