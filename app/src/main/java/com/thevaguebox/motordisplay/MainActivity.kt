package com.thevaguebox.motordisplay

import android.os.Bundle

import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mqttHelper: MQTTHelper
    private lateinit var textView: TextView
    private lateinit var switchServerButton: Button
    private lateinit var serverStatus: TextView

    // MQTT Broker URLs
    private val hivemqBroker = "tcp://broker.hivemq.com:1883"
    private val mosquittoBroker = "tcp://test.mosquitto.org:1883"

    // Flag to track current server (true = HiveMQ, false = Mosquitto)
    private var isUsingHiveMQ = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        switchServerButton = findViewById(R.id.switchServerButton)
        serverStatus = findViewById(R.id.serverStatus)

        // Initialize MQTT connection with default broker (HiveMQ)
        connectToMQTT()

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

        // Initialize MQTT connection
        mqttHelper = MQTTHelper(
            brokerUrl = brokerUrl,
            clientId = "Android_Client",
            topic = "esp8266/gp_motor"
        )

        // Update UI when new message arrives
        mqttHelper.onMessageReceived = { message ->
            runOnUiThread {
                textView.text = "Speed: $message RPM"
            }
        }
    }
}
