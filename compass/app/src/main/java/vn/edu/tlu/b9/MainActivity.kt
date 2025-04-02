package vn.edu.tlu.b9

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var magneticFieldSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null

    private lateinit var compassImageView: ImageView
    private lateinit var angleTextView: TextView

    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        compassImageView = findViewById(R.id.compassImageView)
        angleTextView = findViewById(R.id.angleTextView)

        // Initialize sensors
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        // Check if required sensors are available
        if (magneticFieldSensor == null || accelerometerSensor == null) {
            angleTextView.text = "Thiết bị không hỗ trợ cảm biến cần thiết"
        }
    }

    override fun onResume() {
        super.onResume()

        // Register sensor listeners
        magneticFieldSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        accelerometerSensor?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onPause() {
        super.onPause()

        // Unregister sensor listeners to save battery
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        // Update sensor readings based on sensor type
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
            }
        }

        // Calculate orientation if we have readings from both sensors
        updateOrientationAngles()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // We can handle different accuracy levels here if needed
    }

    private fun updateOrientationAngles() {
        // Update rotation matrix, which is needed to update orientation angles
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )

        // Get orientation angles from the rotation matrix
        SensorManager.getOrientation(rotationMatrix, orientationAngles)

        // Convert radians to degrees
        val degrees = (Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360

        // Rotate compass image to point to north
        // Note: we rotate in the opposite direction of the azimuth
        compassImageView.rotation = (-degrees).toFloat()

        // Display the angle
        angleTextView.text = "Góc lệch: ${degrees.roundToInt()}°"
    }
}