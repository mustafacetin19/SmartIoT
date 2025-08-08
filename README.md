# SmartIoT

**SmartIoT**, ESP32, Spring Boot, React ve PostgreSQL kullanarak geliştirilmiş kullanıcıya özel akıllı cihaz kontrol sistemidir.  
Sistem, MQTT protokolü üzerinden çift yönlü haberleşme sağlar. Kullanıcılar kendi cihazlarını ekleyebilir, odalara göre filtreleyebilir ve uzaktan kontrol edebilir.

---

## 🚀 Özellikler

- **Kullanıcı Girişi (JWT)**: Her kullanıcı kendi cihazlarını yönetir.
- **Oda ve Cihaz Filtreleme**: Oda, cihaz türü ve özel isim ile filtreleme yapılabilir.
- **MQTT ile Gerçek Zamanlı Haberleşme**:
  - Backend → ESP32 (komut gönderme)
  - ESP32 → Backend (sensör verisi ve durum bilgisi gönderme)
- **Soft Delete ile Cihaz Yönetimi**: Cihaz silindiğinde veritabanında `active=false` olarak işaretlenir.
- **Modüler Mimari**: Backend, frontend ve donanım kodları bağımsız çalışabilir.

---

## 📂 Klasör Yapısı

```
SmartIoT/
├── backend/                 # Spring Boot backend kodları
│   └── src/main/resources/
│       └── application.properties.example  # Backend ayar şablonu
├── frontend/                # React frontend kodları
├── esp32/                   # Arduino (ESP32) kodları
│   └── esp32_config.h       # ESP32 ayar şablonu
├── database/                # PostgreSQL şema ve yedek dosyaları
├── mqtt/                    # Mosquitto MQTT broker yapılandırması
│   ├── mosquitto.conf
│   └── data/.gitkeep
├── .env.example             # Ortam değişkenleri şablonu
├── .gitignore               # Gereksiz dosyaları hariç tutar
└── README.md                # Proje dokümantasyonu
```

---

## 🔧 Kullanıcı Tarafından Düzenlenecek Alanlar

Proje ilk çalıştırılmadan önce aşağıdaki dosyalardaki alanlar **kendi ortamınıza göre** düzenlenmelidir:

### 1️⃣ Backend — `application.properties.example`  
```properties
# PostgreSQL Ayarları
spring.datasource.url=jdbc:postgresql://<SUNUCU_IP_ADRESI>:5432/<VERITABANI_ADI>
spring.datasource.username=<DB_KULLANICI_ADI>
spring.datasource.password=<DB_SIFRE>

# MQTT Ayarları
mqtt.broker=tcp://<MQTT_BROKER_IP>:1883
```
> Bu dosyayı `application.properties` olarak kopyalayın ve `<...>` alanlarını kendi bilgilerinize göre doldurun.

### 2️⃣ ESP32 — `esp32_config.h`  
```cpp
// Wi-Fi Ayarları
const char* ssid = "BURAYA_WIFI_ADINIZI_YAZIN";
const char* password = "BURAYA_WIFI_SIFRENIZI_YAZIN";

// MQTT Ayarları
const char* mqtt_server = "BURAYA_MQTT_BROKER_IP_YAZIN";
const int mqtt_port = 1883; // Genelde 1883
```
> Bu dosyadaki değerleri doldurduktan sonra ESP32 koduna dahil edin.

---

## 🔌 Sistem Mimarisi

1. **Frontend (React)**  
   Kullanıcıdan gelen cihaz kontrol komutlarını backend’e HTTP ile gönderir, backend’den gelen verileri görüntüler.

2. **Backend (Spring Boot)**  
   Kullanıcı doğrulama ve yetkilendirme yapar, MQTT broker üzerinden ESP32’ye komut gönderir, gelen sensör verilerini alır ve veritabanına kaydeder.

3. **ESP32 (Arduino)**  
   MQTT broker’a bağlanır, belirli topic’leri dinler (`led/control`, `buzzer/control`, vb.), sensör verilerini ilgili topic’lere gönderir (`dht/data`, `status/device`).

4. **PostgreSQL**  
   Kullanıcı, cihaz, oda ve sensör verilerini saklar.

5. **MQTT Broker (Mosquitto)**  
   Backend ile ESP32 arasındaki mesaj trafiğini yönetir.

---

## 🛠 Kurulum

### 1️⃣ MQTT Broker (Lokal Kurulum)
Mosquitto yüklü olduğundan emin olun. Ardından:
```bash
mosquitto -c mqtt/mosquitto.conf
```
> Varsayılan olarak `1883` portu üzerinden çalışır.

---

### 2️⃣ Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```
> `application.properties` içindeki veritabanı ve MQTT ayarlarının doğru olduğundan emin olun.

---

### 3️⃣ Frontend (React)
```bash
cd frontend
npm install
npm start
```
> `.env` dosyasında gerekli `REACT_APP_` değişkenlerini tanımlayın.

---

### 4️⃣ ESP32 (Arduino IDE)
- `esp32/` klasöründeki kodları Arduino IDE ile açın.
- `esp32_config.h` dosyasındaki ayarları doldurun.
- ESP32’ye yükleyin.

---

## 📡 MQTT Topic Listesi

| Topic            | Yön               | Açıklama                        |
|------------------|-------------------|----------------------------------|
| `led/control`    | Backend → ESP32   | LED aç/kapat komutu              |
| `buzzer/control` | Backend → ESP32   | Buzzer çalıştırma komutu         |
| `servo/control`  | Backend → ESP32   | Servo motor pozisyon kontrolü    |
| `rfid/uid`       | ESP32 → Backend   | RFID kart UID bilgisi            |
| `dht/data`       | ESP32 → Backend   | Sıcaklık/Nem sensör verileri     |
| `status/device`  | ESP32 → Backend   | Cihaz çevrimiçi/durum bilgisi    |

---

## 📋 Gereksinimler

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+**
- **PostgreSQL 15+**
- **Mosquitto MQTT Broker**
- **Arduino IDE + ESP32 Board Manager**

---

## 📄 Lisans
Bu proje MIT lisansı ile lisanslanmıştır.
