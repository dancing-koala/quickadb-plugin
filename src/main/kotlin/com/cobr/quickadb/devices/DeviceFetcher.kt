package com.cobr.quickadb.devices

import com.android.ddmlib.AndroidDebugBridge
import com.android.ddmlib.IDevice
import com.cobr.quickadb.QuickADBService
import com.cobr.quickadb.platform.Notifier
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project

fun getDevice(
    project: Project,
    bridge: AndroidDebugBridge,
): IDevice? {
    val devices = bridge.devices.sortedBy { it.serialNumber }

    if (devices.isEmpty()) {
        Notifier.info("No devices found.")
        return null
    }

    val preferences = project.service<QuickADBService>().preferences
    val lastTargetSerial = preferences.lastTargetSerial

    if (devices.size == 1) {
        val device = devices.first()

        if (device.serialNumber != lastTargetSerial) {
            preferences.clearTargetSerial()
        }

        return device
    }

    val lastTarget = lastTargetSerial?.let { serial ->
        devices.firstOrNull { it.serialNumber == serial }
    }

    if (lastTarget != null) {
        return lastTarget
    }

    if (lastTargetSerial != null) {
        preferences.clearTargetSerial()
    }

    val result = TargetDeviceDialog(project, devices, preferences)
        .showAndGetResult()

    return result.also {
        it ?: Notifier.info("No device selected.")
    }
}