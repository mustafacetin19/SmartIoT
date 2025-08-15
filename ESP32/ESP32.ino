#include <WiFi.h>
#include <PubSubClient.h>
#include <SPI.h>
#include <MFRC522.h>
#include <ESP32Servo.h>
#include <DHT.h>

// ====================== KULLANICI AYARLARI ======================
// Wi-Fi
const char* ssid     = "Mustafa adlı kişiye ait S23 FE";
const char* password = "Mustafa_1903";

// MQTT Broker (Spring/Sunucu ile aynı IP olmalı!)
const char* mqtt_server = "192.168.199.225";
const int   mqtt_port   = 1883;

// === DB'deki devices.id ile EŞLEŞEN FİZİKSEL CİHAZ ID'LERİ ===
// (Lütfen kendi veritabanındaki gerçek ID'lerle doldur)
const long SERVO1_ID     = 501;   // ör: 501 id:9 için
const long SERVO2_ID     = 502;   // ör: 502 id:1 için
const long LED_WHITE_ID  = 7;     // ör: 1   id:1 için
const long LED_RED_ID    = 2;     // ör: 2   id:1 için
const long LED_YELLOW_ID = 3;     // ör: 3   id:8 için
const long LED_BLUE_ID   = 4;     // ör: 4   id:1 için
const long BUZZER_ID     = 602;   // ör: 602 id:7 için
const long SENSOR_ID     = 701;   // DHT11 için (ör: 701) id:9 için
const long RFID_ID       = 401;   // RFID okuyucu için (kendine göre ayarla) id:7 için


// ====================== DONANIM PINLERİ ======================
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

// ====================== NESNELER ======================
MFRC522 mfrc522(SS_PIN, RST_PIN);
WiFiClient espClient;
PubSubClient client(espClient);
Servo servo1;
Servo servo2;
DHT dht(DHTPIN, DHTTYPE);

// ====================== ZAMANLAYICI ======================
unsigned long lastDhtReadTime = 0;
const unsigned long dhtInterval = 10000;  // 10 sn

// ====================== YARDIMCI ======================
void beepOnce(int onMs = 100) {
  digitalWrite(BUZZER_PIN, HIGH);
  delay(onMs);
  digitalWrite(BUZZER_PIN, LOW);
}

char TOPIC_SERVO1[32];
char TOPIC_SERVO2[32];
char TOPIC_LED_WHITE[32];
char TOPIC_LED_RED[32];
char TOPIC_LED_YELLOW[32];
char TOPIC_LED_BLUE[32];
char TOPIC_BUZZER[32];

char TOPIC_SENSOR_STATUS[40];   // publish
char TOPIC_RFID_LAST[40];       // publish

void buildTopics() {
  if (SERVO1_ID > 0)  snprintf(TOPIC_SERVO1, sizeof(TOPIC_SERVO1), "servo/%ld/set",   SERVO1_ID);
  if (SERVO2_ID > 0)  snprintf(TOPIC_SERVO2, sizeof(TOPIC_SERVO2), "servo/%ld/set",   SERVO2_ID);

  if (LED_WHITE_ID > 0)  snprintf(TOPIC_LED_WHITE,  sizeof(TOPIC_LED_WHITE),  "led/%ld/set",    LED_WHITE_ID);
  if (LED_RED_ID > 0)    snprintf(TOPIC_LED_RED,    sizeof(TOPIC_LED_RED),    "led/%ld/set",    LED_RED_ID);
  if (LED_YELLOW_ID > 0) snprintf(TOPIC_LED_YELLOW, sizeof(TOPIC_LED_YELLOW), "led/%ld/set",    LED_YELLOW_ID);
  if (LED_BLUE_ID > 0)   snprintf(TOPIC_LED_BLUE,   sizeof(TOPIC_LED_BLUE),   "led/%ld/set",    LED_BLUE_ID);

  if (BUZZER_ID > 0)     snprintf(TOPIC_BUZZER,     sizeof(TOPIC_BUZZER),     "buzzer/%ld/set", BUZZER_ID);

  if (SENSOR_ID > 0) snprintf(TOPIC_SENSOR_STATUS, sizeof(TOPIC_SENSOR_STATUS), "sensor/%ld/status", SENSOR_ID);
  if (RFID_ID > 0)   snprintf(TOPIC_RFID_LAST,     sizeof(TOPIC_RFID_LAST),     "rfid/%ld/last",    RFID_ID);
}

void subscribeTopics() {
  if (SERVO1_ID > 0)  { client.subscribe(TOPIC_SERVO1);  Serial.printf("📡 sub: %s\n", TOPIC_SERVO1); }
  if (SERVO2_ID > 0)  { client.subscribe(TOPIC_SERVO2);  Serial.printf("📡 sub: %s\n", TOPIC_SERVO2); }

  if (LED_WHITE_ID > 0)  { client.subscribe(TOPIC_LED_WHITE);  Serial.printf("📡 sub: %s\n", TOPIC_LED_WHITE); }
  if (LED_RED_ID > 0)    { client.subscribe(TOPIC_LED_RED);    Serial.printf("📡 sub: %s\n", TOPIC_LED_RED); }
  if (LED_YELLOW_ID > 0) { client.subscribe(TOPIC_LED_YELLOW); Serial.printf("📡 sub: %s\n", TOPIC_LED_YELLOW); }
  if (LED_BLUE_ID > 0)   { client.subscribe(TOPIC_LED_BLUE);   Serial.printf("📡 sub: %s\n", TOPIC_LED_BLUE); }

  if (BUZZER_ID > 0)     { client.subscribe(TOPIC_BUZZER);     Serial.printf("📡 sub: %s\n", TOPIC_BUZZER); }
}

// ====================== MQTT CALLBACK ======================
void callback(char* topic, byte* payload, unsigned int length) {
  String msg; msg.reserve(length+1);
  for (unsigned int i = 0; i < length; i++) msg += (char)payload[i];
  msg.trim();

  Serial.print("📩 MQTT ➜ ");
  Serial.print(topic);
  Serial.print(" | ");
  Serial.println(msg);

  // --- SERVO ---
  if (SERVO1_ID > 0 && strcmp(topic, TOPIC_SERVO1) == 0) {
    int angle = constrain(msg.toInt(), 0, 180);
    servo1.write(angle);
    Serial.printf("🛠️ SERVO1 (%ld) → %d°\n", SERVO1_ID, angle);
    return;
  }
  if (SERVO2_ID > 0 && strcmp(topic, TOPIC_SERVO2) == 0) {
    int angle = constrain(msg.toInt(), 0, 180);
    servo2.write(angle);
    Serial.printf("🛠️ SERVO2 (%ld) → %d°\n", SERVO2_ID, angle);
    return;
  }

  // --- LED ---
  auto applyLed = [&](int pin, const char* name) {
    bool on = (msg == "on" || msg == "ON" || msg == "1" || msg == "true");
    digitalWrite(pin, on ? HIGH : LOW);
    Serial.printf("💡 %s -> %s\n", name, on ? "ON" : "OFF");
  };

  if (LED_WHITE_ID  > 0 && strcmp(topic, TOPIC_LED_WHITE)  == 0) { applyLed(LED_WHITE,  "WHITE");  return; }
  if (LED_RED_ID    > 0 && strcmp(topic, TOPIC_LED_RED)    == 0) { applyLed(LED_RED,    "RED");    return; }
  if (LED_YELLOW_ID > 0 && strcmp(topic, TOPIC_LED_YELLOW) == 0) { applyLed(LED_YELLOW, "YELLOW"); return; }
  if (LED_BLUE_ID   > 0 && strcmp(topic, TOPIC_LED_BLUE)   == 0) { applyLed(LED_BLUE,   "BLUE");   return; }

  // --- BUZZER ---
  if (BUZZER_ID > 0 && strcmp(topic, TOPIC_BUZZER) == 0) {
    if (msg == "beep" || msg == "on") {
      beepOnce();
    } else if (msg == "beep2") {
      beepOnce(); delay(120); beepOnce();
    } else if (msg == "beep3") {
      beepOnce(); delay(120); beepOnce(); delay(120); beepOnce();
    } else {
      Serial.println("⚠️ Geçersiz buzzer komutu.");
    }
    return;
  }
}

// ====================== WIFI & MQTT ======================
void setup_wifi() {
  Serial.print("Wi-Fi bağlanıyor");
  WiFi.begin(ssid, password);
  int attempt = 0;
  while (WiFi.status() != WL_CONNECTED && attempt < 30) {
    delay(500); Serial.print(".");
    attempt++;
  }
  if (WiFi.status() == WL_CONNECTED) {
    Serial.println("\n✅ Wi-Fi bağlı.");
    Serial.print("📡 IP: "); Serial.println(WiFi.localIP());
  } else {
    Serial.println("\n❌ Wi-Fi bağlantısı başarısız!");
  }
}

void reconnect() {
  while (!client.connected()) {
    Serial.print("MQTT bağlanıyor...");
    if (client.connect("ESP32Client")) {
      Serial.println("bağlandı!");
      subscribeTopics();
    } else {
      Serial.print(" hata="); Serial.println(client.state());
      delay(2000);
    }
  }
}

// ====================== SETUP ======================
void setup() {
  Serial.begin(115200);

  // Bus & sensörler
  SPI.begin(SCK_PIN, MISO_PIN, MOSI_PIN, SS_PIN);
  mfrc522.PCD_Init();
  dht.begin();

  // IO
  pinMode(BUZZER_PIN, OUTPUT);
  pinMode(LED_WHITE,  OUTPUT);
  pinMode(LED_RED,    OUTPUT);
  pinMode(LED_YELLOW, OUTPUT);
  pinMode(LED_BLUE,   OUTPUT);

  // Servo
  servo1.setPeriodHertz(50);
  servo2.setPeriodHertz(50);
  servo1.attach(SERVO1_PIN, 500, 2400);
  servo2.attach(SERVO2_PIN, 500, 2400);
  servo1.write(0);
  servo2.write(0);

  // MQTT
  setup_wifi();
  client.setServer(mqtt_server, mqtt_port);
  client.setCallback(callback);

  buildTopics();
  reconnect();

  // küçük açılış animasyonu
  digitalWrite(LED_RED, HIGH);  delay(150);
  digitalWrite(LED_BLUE, HIGH); delay(150);
  digitalWrite(LED_BLUE, LOW);  delay(150);
  digitalWrite(LED_RED, LOW);
}

// ====================== LOOP ======================
void loop() {
  if (!client.connected()) reconnect();
  client.loop();

  // --- DHT11 TELEMETRİ ---
  unsigned long now = millis();
  if (SENSOR_ID > 0 && now - lastDhtReadTime >= dhtInterval) {
    lastDhtReadTime = now;
    float t = dht.readTemperature();
    float h = dht.readHumidity();
    if (!isnan(t) && !isnan(h)) {
      Serial.printf("🌡 T=%.2f°C  💧 H=%.2f%%\n", t, h);
      // JSON yayınla: sensor/{id}/status
      char json[80];
      snprintf(json, sizeof(json), "{\"temperature\":%.2f,\"humidity\":%.2f}", t, h);
      client.publish(TOPIC_SENSOR_STATUS, json);
    } else {
      Serial.println("⚠️ DHT11 verisi okunamadı.");
    }
  }

  // --- RFID TELEMETRİ ---
  if (RFID_ID > 0) {
    if (mfrc522.PICC_IsNewCardPresent() && mfrc522.PICC_ReadCardSerial()) {
      String uid = "";
      for (byte i = 0; i < mfrc522.uid.size; i++) {
        if (mfrc522.uid.uidByte[i] < 0x10) uid += "0";
        uid += String(mfrc522.uid.uidByte[i], HEX);
      }
      uid.toUpperCase();
      Serial.print("🎫 RFID UID: "); Serial.println(uid);
      beepOnce();
      client.publish(TOPIC_RFID_LAST, uid.c_str());
      // küçük görsel geri bildirim:
      digitalWrite(LED_WHITE, HIGH);
      digitalWrite(LED_YELLOW, HIGH);
      digitalWrite(LED_BLUE, HIGH);
      servo1.write(89); servo2.write(89);
      beepOnce(); delay(500);
      servo1.write(0);  servo2.write(0);
      digitalWrite(LED_WHITE, LOW);
      digitalWrite(LED_YELLOW, LOW);
      digitalWrite(LED_BLUE, LOW);
      delay(300);
    }
  }
}
