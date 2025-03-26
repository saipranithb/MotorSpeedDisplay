package com.thevaguebox.motordisplay

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var mqttHelper: MQTTHelper
    private lateinit var textView: TextView
    private lateinit var switchServerButton: Button
    private lateinit var subscribeButton: Button
    private lateinit var serverStatus: TextView
    private lateinit var topicInput: EditText
    private lateinit var stopUpdatesButton: Button
    private var topicGiven: String = ""

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timeoutRunnable: Runnable

    // MQTT Broker URLs
    private val hivemqBroker = "tcp://broker.hivemq.com:1883"
    private val mosquittoBroker = "tcp://test.mosquitto.org:1883"

    private val sessionData = mutableListOf<Pair<String, String>>()  // For logging values

    // Flag to track current server (true = HiveMQ, false = Mosquitto)
    private var isUsingHiveMQ = true

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        textView = findViewById(R.id.textView)
        switchServerButton = findViewById(R.id.switchServerButton)
        serverStatus = findViewById(R.id.serverStatus)
        topicInput = findViewById(R.id.topicInput)
        subscribeButton = findViewById(R.id.subscribeButton)
        stopUpdatesButton = findViewById(R.id.stopUpdatesButton)

        //Handle button click to subscribe to a topic
        subscribeButton.setOnClickListener {
            topicGiven = topicInput.text.toString().trim() // Captures the text in topicInput
            if (topicGiven.isNotEmpty()) {
                connectToMQTT() //Connect to subscribe to the topic with default broker (HiveMQ)
            } else {
                textView.text = "❌ Enter a valid topic!"
            }
        }

        stopUpdatesButton.setOnClickListener {
            stopAndExportData()
        }

        // Handle button click to switch servers
        switchServerButton.setOnClickListener {
            isUsingHiveMQ = !isUsingHiveMQ  // Toggle between HiveMQ & Mosquitto
            connectToMQTT()  // Reconnect with new broker
        }

    }

    private fun connectToMQTT() {
        val brokerUrl = if (isUsingHiveMQ) hivemqBroker else mosquittoBroker
        val brokerName = if (isUsingHiveMQ) "HiveMQ" else "Mosquitto"

        // Show the connected broker in UI
        serverStatus.text = "Connected: $brokerName"
        textView.text = "Waiting for MQTT data..." // Reset textView before checking timeout

        // Initialize MQTT connection
        mqttHelper = MQTTHelper(
            brokerUrl = brokerUrl,
            clientId = "Android_Client",
            topic = topicGiven
        )

        startTimeoutChecker()

        // Update UI when new message arrives
        mqttHelper.onMessageReceived = { message ->
            runOnUiThread {
                textView.text = "Speed: $message RPM"
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val timestamp = sdf.format(Date())
                sessionData.add(Pair(timestamp, message))
                handler.removeCallbacks(timeoutRunnable)
            }
        }
    }

    private fun startTimeoutChecker() {
        timeoutRunnable = Runnable {
            if (textView.text == "Waiting for MQTT data...") {
                textView.text = "⚠️ Try switching the server"
            }
        }
        handler.postDelayed(timeoutRunnable, 10000)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun stopAndExportData() {

        if (sessionData.isEmpty()) {
            Toast.makeText(this, "No data received to export", Toast.LENGTH_SHORT).show()
            return
        }

        val csvContent = StringBuilder()
        csvContent.append("Timestamp (ms),Speed (RPM)\n")
        sessionData.forEach { (timestamp, speed) ->
            csvContent.append("$timestamp,$speed\n")
        }

        try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, "motor_speed_${System.currentTimeMillis()}.csv")
                put(MediaStore.Downloads.MIME_TYPE, "text/csv")
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csvContent.toString().toByteArray())
                    outputStream.flush()
                }
                Toast.makeText(this, "✅ CSV saved to Downloads", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "❌ Failed to save CSV", Toast.LENGTH_SHORT).show()
        }

        sessionData.clear()
        mqttHelper.disconnect()
    }


}
