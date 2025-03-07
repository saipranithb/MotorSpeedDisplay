# Motor Speed Display App

A real-time motor speed monitoring app that receives sensor data via **MQTT** and displays it beautifully. Built using **Kotlin**, **ESP8266/ESP32**, and **Mosquitto/HiveMQ MQTT Brokers**.

---

## Features
✔ **Real-time motor speed display**  
✔ **Switch between MQTT brokers (Mosquitto & HiveMQ)**  
✔ **Automatic broker fallback when one is down**  
✔ **Smooth & modern UI**  
✔ **Designed for ESP8266 & ESP32**  

---

## Installation & Setup
### **Clone the Repository**
```sh
git clone https://github.com/your-username/MotorSpeedDisplay.git
cd MotorSpeedDisplay
```

### **Run the Android App**
- Open the project in **Android Studio**  
- Click **Run** to launch the app  

### **Flash ESP8266/ESP32 Code**
- Open `MotorSpeedDisplay.ino` in **Arduino IDE**  
- Configure **WiFi credentials** & **MQTT settings**  
- Upload to **ESP8266/ESP32**  

---

## Switching MQTT Brokers
If one broker is down, click **"Switch Server"** in the app to swap between:
- 🟢 **HiveMQ** (`broker.hivemq.com`)
- 🔵 **Mosquitto** (`test.mosquitto.org`)

---

## License
This project is **MIT Licensed**. Feel free to modify and distribute.  

---

## Author & Credits
- **Sai Pranith Bhagavatula** – Creator & Developer  
- **Prof. Prasun Mishra, IIT Kharagpur** – Guide & Advisor  

---

## Support & Contributions
Contributions are welcome! Fork the repo, make changes, and open a **Pull Request**.  

---

