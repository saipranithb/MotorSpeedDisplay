package com.thevaguebox.motordisplay

import android.os.Bundle
import android.os.Handler
import android.os.Looper

import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mqttHelper: MQTTHelper
    private lateinit var textView: TextView
    private lateinit var switchServerButton: Button
    private lateinit var subscribeButton: Button
    private lateinit var serverStatus: TextView
    private lateinit var topicInput: EditText
    private var topicGiven: String = ""

    private val handler = Handler(Looper.getMainLooper()) // ✅ Timer handler
    private lateinit var timeoutRunnable: Runnable // ✅ Runnable for timeout check

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
        topicInput = findViewById(R.id.topicInput)
        subscribeButton = findViewById(R.id.subscribeButton)

        //Handle button click to subscribe to a topic
        subscribeButton.setOnClickListener {
            topicGiven = topicInput.text.toString().trim() // Captures the text in topicInput
            if (topicGiven.isNotEmpty()) {
                connectToMQTT() //Connect to subscribe to the topic with default broker (HiveMQ)
            } else {
                textView.text = "❌ Enter a valid topic!"
            }
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

        startTimeoutChecker() // ✅ Start timeout check (if no message in 5 sec, show error)



        // Update UI when new message arrives
        mqttHelper.onMessageReceived = { message ->
            runOnUiThread {
                textView.text = "Speed: $message RPM"
                handler.removeCallbacks(timeoutRunnable) // ✅ Stop timeout checker when data is received
            }
        }
    }

    // ✅ Timer function to check if data is received in 5 sec
    private fun startTimeoutChecker() {
        timeoutRunnable = Runnable {
            if (textView.text == "Waiting for MQTT data...") {
                textView.text = "⚠️ Try switching the server"
            }
        }
        handler.postDelayed(timeoutRunnable, 5000) // ✅ Run after 5 sec
    }
}
