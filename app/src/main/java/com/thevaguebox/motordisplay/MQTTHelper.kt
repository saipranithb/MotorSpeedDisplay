package com.thevaguebox.motordisplay

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.eclipse.paho.client.mqttv3.*
import java.util.concurrent.Executors

class MQTTHelper(brokerUrl: String, clientId: String, private val topic: String) {

    private var mqttClient: MqttClient? = null
    private val executorService = Executors.newSingleThreadExecutor()

    var onMessageReceived: ((String) -> Unit)? = null

    init {
        executorService.execute { connect(brokerUrl, clientId) }
    }

    private fun connect(brokerUrl: String, clientId: String) {
        try {
            mqttClient = MqttClient(brokerUrl, clientId, null)
            val options = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = true
            }

            mqttClient?.connect(options)
            Log.d("MQTT", "✅ Connected to broker!")

            subscribeToTopic()
        } catch (e: Exception) {
            Log.e("MQTT", "❌ Connection failed: ${e.message}")
        }
    }

    private fun subscribeToTopic() {
        try {
            mqttClient?.subscribe(topic) { _, message ->
                val payload = message.toString()
                Log.d("MQTT", "📩 Received message: $payload")
                onMessageReceived?.invoke(payload)
            }
            Log.d("MQTT", "✅ Subscribed to topic: $topic")
        } catch (e: Exception) {
            Log.e("MQTT", "❌ Subscription failed: ${e.message}")
        }
    }
}
