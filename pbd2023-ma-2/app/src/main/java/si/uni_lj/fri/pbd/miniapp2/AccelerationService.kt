package si.uni_lj.fri.pbd.miniapp2

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Math.abs

class AccelerationService: Service(), SensorEventListener {

    private var serviceBinder = RunServiceBinder()
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private var prevX = 0f
    private var prevY = 0f
    private var prevZ = 0f
    private var firstReading = true

    private val sleepScope = CoroutineScope((Dispatchers.IO))

    companion object {
        private val TAG: String? = AccelerationService::class.simpleName
        private const val NOISE_THRESHOLD = 5
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.d(MediaPlayerService.TAG, "Binding service")

        return serviceBinder
    }

    inner class RunServiceBinder : Binder() {
        val service: AccelerationService
            get() = this@AccelerationService
    }

    private suspend fun sleep() {
        withContext(Dispatchers.Main) {
            delay(500)
        }
    }

    // check for sensor changes and if so change commands and notify MediaServicePlayer
    override fun onSensorChanged(event: SensorEvent?) {
        if(event != null) {
            if(firstReading) {
                prevX = event.values[0]
                prevY = event.values[1]
                prevZ = event.values[2]
                firstReading = false
            } else {
                var dx = abs(prevX - event.values[0])
                var dy = abs(prevY- event.values[1])
                var dz = abs(prevZ - event.values[2])

                if(dx <= NOISE_THRESHOLD) {
                    dx = 0f
                }
                if(dy <= NOISE_THRESHOLD) {
                    dy = 0f
                }
                if(dz <= NOISE_THRESHOLD) {
                    dz = 0f
                }

                /*
                COMMANDS
                0 = IDLE
                1 = HORIZONTAL
                2 = VERTICAL
                */

                var command = 0
                if(dx > dz) {
                    command = 1
                }
                if(dz > dx) {
                    command = 2
                }

                if(command != 0) {
                    // update MediaPlayerService
                    if(command == 1) {
                        // calls pause
                        val i: Intent = Intent("pause")
                        sendBroadcast(i)
                    }
                    if(command == 2) {
                        // calls play
                        val i: Intent = Intent("play")
                        sendBroadcast(i)

                    }
                }
                sleepScope.launch {
                    sleep()
                }
            }
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Nothing
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
    }
}