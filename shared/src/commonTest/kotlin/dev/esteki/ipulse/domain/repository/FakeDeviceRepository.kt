package dev.esteki.ipulse.domain.repository

import androidx.paging.PagingData
import dev.esteki.ipulse.domain.model.Device
import dev.esteki.ipulse.domain.model.SignalQuality
import dev.esteki.ipulse.domain.model.Stability
import dev.esteki.ipulse.domain.model.TelemetryReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeDeviceRepository : DeviceRepository {

    private val _devices = MutableStateFlow<List<Device>>(emptyList())
    override val devices: Flow<List<Device>> get() = _devices

    private val _signalQuality = MutableStateFlow(SignalQuality(0.0, Stability.NO_DATA))
    override val signalQuality: Flow<SignalQuality> get() = _signalQuality

    private val deviceById = mutableMapOf<String, Device>()
    private val readings = mutableMapOf<String, List<TelemetryReading>>()

    override fun observeDeviceById(id: String): Flow<Device?> = flowOf(deviceById[id])

    override suspend fun getDeviceById(id: String): Result<Device> {
        val device = deviceById[id]
        return if (device != null) Result.success(device) else Result.failure(Exception("Not found"))
    }

    override fun observeReadingsForDevice(deviceId: String): Flow<List<TelemetryReading>> =
        flowOf(readings[deviceId] ?: emptyList())

    override fun observeDevicesPaged(): Flow<PagingData<Device>> = flowOf(PagingData.empty())

    fun setDevices(devices: List<Device>) {
        _devices.value = devices
    }

    fun setSignalQuality(quality: SignalQuality) {
        _signalQuality.value = quality
    }

    fun setDevice(device: Device) {
        deviceById[device.id] = device
    }

    fun setReadings(deviceId: String, readings: List<TelemetryReading>) {
        this.readings[deviceId] = readings
    }
}
