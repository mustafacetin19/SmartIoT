#include <WiFi.h>
#include <PubSubClient.h>
#include <SPI.h>
#include <MFRC522.h>
#include <ESP32Servo.h>
#include <DHT.h>

// === Wi-Fi Ayarları ===
// 👉 Kendi Wi-Fi ağ adını (SSID) buraya yaz
const char* ssid = "BURAYA_WIFI_ADINIZI_YAZIN";

// 👉 Kendi Wi-Fi şifrenizi buraya yaz
const char* password = "BURAYA_WIFI_SIFRENIZI_YAZIN";

// === MQTT Ayarları ===
// 👉 MQTT broker IP adresinizi buraya yaz (ör: "192.168.1.10")
const char* mqtt_server = "BURAYA_MQTT_BROKER_IP_YAZIN";

// 👉 MQTT broker portunu buraya yaz (genelde 1883)
const int mqtt_port = 1883;

// Topic'ler (İsterseniz değiştirebilirsiniz, backend ile aynı olmalı)
const char* topic_uid = "rfid/uid";
const char* topic_dht = "dht/data";
const char* topic_servo_control = "servo/control";
const char* topic_servo_status = "servo/status";
const char* topic_led_control = "iot/control/led";
const char* topic_buzzer_control = "iot/control/buzzer";


// === Pin Tanımları ===
#define SS_PIN     15
#define RST_PIN    4
#define SCK_PIN    23
#define MISO_PIN   19
#define MOSI_PIN   21

#define SERVO1_PIN 26
#define SERVO2_PIN 27
#define BUZZER_PIN 18

#define LED_WHITE  5
#define LED_RED    32
#define LED_YELLOW 33
#define LED_BLUE   25

#define DHTPIN     22
#define DHTTYPE    DHT11

// === Nesne Tanımları ===
MFRC522 mfrc522(SS_PIN, RST_PIN);
WiFiClient espClient;
PubSubClient client(espClient);
Servo servo1;
Servo servo2;
DHT dht(DHTPIN, DHTTYPE);

// === Zamanlayıcılar ===
unsigned long lastDhtReadTime = 0;
const unsigned long dhtInterval = 10000;  // 10 saniye

// === Fonksiyonlar ===
void beep() {
  Serial.println("🔔 Buzzer aktif!");
  digitalWrite(BUZZER_PIN, HIGH);
  delay(100);
  digitalWrite(BUZZER_PIN, LOW);
}

void callback(char* topic, byte* payload, unsigned int length) {
  String msg = "";
  for (int i = 0; i < length; i++) msg += (char)payload[i];

  Serial.print("📩 MQTT Mesaj ➜ Topic: ");
  Serial.print(topic);
  Serial.print(" | İçerik: ");
  Serial.println(msg);

  // === Servo kontrolü ===
  if (String(topic) == topic_servo_control) {
    Serial.println("🔁 Servo komutu işleniyor...");
    int index = msg.indexOf(':');
    if (index != -1) {
      String servo = msg.substring(0, index);
      int angle = msg.substring(index + 1).toInt();
      if (servo == "servo1") {
        servo1.write(angle);
        client.publish(topic_servo_status, ("servo1:" + String(angle)).c_str());
      } else if (servo == "servo2") {
        servo2.write(angle);
        client.publish(topic_servo_status, ("servo2:" + String(angle)).c_str());
      }
    }
  }

  // === LED kontrolü ===
  else if (String(topic) == topic_led_control) {
    Serial.println("💡 LED komutu işleniyor...");

    if (msg == "white" || msg == "white_on") digitalWrite(LED_WHITE, HIGH);
    else if (msg == "white_off") digitalWrite(LED_WHITE, LOW);
    else if (msg == "red" || msg == "red_on") digitalWrite(LED_RED, HIGH);
    else if (msg == "red_off") digitalWrite(LED_RED, LOW);
    else if (msg == "yellow" || msg == "yellow_on") digitalWrite(LED_YELLOW, HIGH);
    else if (msg == "yellow_off") digitalWrite(LED_YELLOW, LOW);
    else if (msg == "blue" || msg == "blue_on") digitalWrite(LED_BLUE, HIGH);
    else if (msg == "blue_off") digitalWrite(LED_BLUE, LOW);
    else Serial.println("⚠️ Geçersiz LED komutu");
  }

  // === Buzzer kontrolü ===
  else if (String(topic) == topic_buzzer_control) {
    Serial.println("🔊 Buzzer komutu işleniyor...");
    if (msg == "on" || msg == "beep") {
      beep();
    } else {
      Serial.println("⚠️ Geçersiz buzzer komutu");
    }
  }
}

void setup_wifi() {
  Serial.print("Wi-Fi bağlanıyor...");
  WiFi.begin(ssid, password);
  int attempt = 0;
  while (WiFi.status() != WL_CONNECTED && attempt < 30) {
    delay(500);
    Serial.print(".");
    attempt++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✅ Wi-Fi bağlantısı başarılı.");
    Serial.print("📡 IP adresi: ");
    Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n❌ Wi-Fi bağlantısı başarısız!");
  }
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("MQTT bağlanıyor...");
    if (client.connect("ESP32Client")) {
      Serial.println("Bağlandı!");
      client.subscribe(topic_servo_control);
      client.subscribe(topic_led_control);
      client.subscribe(topic_buzzer_control);
    } else {
      Serial.print(" Hata: ");
      Serial.println(client.state());
      delay(2000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  SPI.begin(SCK_PIN, MISO_PIN, MOSI_PIN, SS_PIN);
  mfrc522.PCD_Init();
  dht.begin();

  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);

  servo1.setPeriodHertz(50);
  servo2.setPeriodHertz(50);
  servo1.attach(SERVO1_PIN, 500, 2400);
  servo2.attach(SERVO2_PIN, 500, 2400);
  servo1.write(0);
  servo2.write(0);

  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_WHITE, OUTPUT);
  pinMode(LED_RED, OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_BLUE, OUTPUT);

  digitalWrite(LED_RED, HIGH); // sistem aktif göstergesi
  delay(500);
  digitalWrite(LED_BLUE, HIGH);
  delay(500);
  digitalWrite(LED_BLUE, LOW);
}

void loop() {
  if (!client.connected()) reconnect();
  client.loop();

  // === Sıcaklık/Nem ===
  unsigned long currentMillis = millis();
  if (currentMillis - lastDhtReadTime >= dhtInterval) {
    lastDhtReadTime = currentMillis;
    float temperature = dht.readTemperature();
    float humidity = dht.readHumidity();
    if (!isnan(temperature)) {
      Serial.printf("🌡 Sıcaklık: %.2f °C | 💧 Nem: %.2f %%\n", temperature, humidity);
      String sensorData = "temperature=" + String(temperature) + ";humidity=" + String(humidity);
      client.publish(topic_dht, sensorData.c_str());
      Serial.print("📤 MQTT DHT gönderimi: ");
      Serial.println(sensorData);

      if (temperature > 28.0) {
        Serial.println("🔥 Sıcaklık 28°C üzeri! Kapılar açılıyor...");
        servo1.write(89);
        servo2.write(89);
        beep();
        delay(15000);
        servo1.write(0);
        servo2.write(0);
        beep();
      }
    } else {
      Serial.println("⚠ DHT11 verisi okunamadı.");
    }
  }

  // === RFID Okuma ===
  if (!mfrc522.PICC_IsNewCardPresent()) return;
  if (!mfrc522.PICC_ReadCardSerial()) return;

  String uid = "";
  for (byte i = 0; i < mfrc522.uid.size; i++) {
    if (mfrc522.uid.uidByte[i] < 0x10) uid += "0";
    uid += String(mfrc522.uid.uidByte[i], HEX);
  }
  uid.toUpperCase();

  Serial.print("🎫 Kart Okundu, UID: ");
  Serial.println(uid);
  beep();

  bool mqttSent = client.publish(topic_uid, uid.c_str());
  Serial.print("📤 MQTT gönderme durumu: ");
  Serial.println(mqttSent ? "Başarılı" : "Başarısız");

  digitalWrite(LED_WHITE, HIGH);
  digitalWrite(LED_YELLOW, HIGH);
  digitalWrite(LED_BLUE, HIGH);
  servo1.write(89);
  servo2.write(89);
  beep();
  delay(5000);
  servo1.write(0);
  servo2.write(0);
  beep();
  digitalWrite(LED_WHITE, LOW);
  digitalWrite(LED_YELLOW, LOW);
  digitalWrite(LED_BLUE, LOW);

  delay(1500);
}