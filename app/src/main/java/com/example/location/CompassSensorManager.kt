package com.example.location

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class CompassAccuracy {
    LOW,
    MEDIUM,
    HIGH
}

data class CompassReading(
    val azimuthDegrees: Float,
    val accuracy: CompassAccuracy,
    val hasSensor: Boolean
)

class CompassSensorManager(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun observeHeading(): Flow<CompassReading> = callbackFlow {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

        if (rotationSensor == null && (accelSensor == null || magnetSensor == null)) {
            trySend(CompassReading(azimuthDegrees = 0f, accuracy = CompassAccuracy.LOW, hasSensor = false))
            close()
            return@callbackFlow
        }

        var smoothedSin = 0.0
        var smoothedCos = 1.0
        var initialized = false
        var currentAccuracy = CompassAccuracy.MEDIUM

        val accelValues = FloatArray(3)
        val magnetValues = FloatArray(3)
        var hasAccel = false
        var hasMagnet = false
        val rotationMatrix = FloatArray(9)
        val orientationAngles = FloatArray(3)

        fun emitAzimuth(rawDeg: Float) {
            val rad = Math.toRadians(rawDeg.toDouble())
            val alpha = 0.18
            if (!initialized) {
                smoothedSin = sin(rad)
                smoothedCos = cos(rad)
                initialized = true
            } else {
                smoothedSin = smoothedSin * (1.0 - alpha) + sin(rad) * alpha
                smoothedCos = smoothedCos * (1.0 - alpha) + cos(rad) * alpha
            }
            val filteredDeg = ((Math.toDegrees(atan2(smoothedSin, smoothedCos)) + 360.0) % 360.0).toFloat()
            trySend(
                CompassReading(
                    azimuthDegrees = filteredDeg,
                    accuracy = currentAccuracy,
                    hasSensor = true
                )
            )
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val deg = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
                        emitAzimuth(deg)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, accelValues, 0, 3)
                        hasAccel = true
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, magnetValues, 0, 3)
                        hasMagnet = true
                    }
                }
                if (rotationSensor == null && hasAccel && hasMagnet) {
                    if (SensorManager.getRotationMatrix(rotationMatrix, null, accelValues, magnetValues)) {
                        SensorManager.getOrientation(rotationMatrix, orientationAngles)
                        val deg = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
                        emitAzimuth(deg)
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                currentAccuracy = when (accuracy) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> CompassAccuracy.HIGH
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> CompassAccuracy.MEDIUM
                    else -> CompassAccuracy.LOW
                }
            }
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(listener, magnetSensor, SensorManager.SENSOR_DELAY_UI)
        }

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
