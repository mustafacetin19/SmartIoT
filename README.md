# SmartIoT

**SmartIoT**, ESP32, Spring Boot, React ve PostgreSQL kullanarak geliştirilmiş, kullanıcıya özel akıllı cihaz kontrol sistemidir.  
Sistem, **MQTT** protokolü üzerinden çift yönlü haberleşme sağlar. Kullanıcılar cihazlarını ekleyebilir, odalara göre organize edebilir ve uzaktan kontrol edebilir.

---

## 🧭 İçindekiler
- [Özellikler](#-özellikler)
- [Klasör Yapısı](#-klasör-yapısı)
- [API Dokümantasyonu](#-api-dokümantasyonu)
- [Hızlı Başlangıç](#-hızlı-başlangıç)
- [Konfigürasyon](#-konfigürasyon)
- [Sistem Mimarisi](#-sistem-mimarisi)
- [MQTT Topic Listesi](#-mqtt-topic-listesi)
- [Gereksinimler](#-gereksinimler)
- [Lisans](#-lisans)

---

## 🚀 Özellikler

- **Kullanıcı Girişi** _(JWT planlanıyor)_: Her kullanıcı kendi cihazlarını yönetir.
- **Oda & Cihaz Organizasyonu**: Oda, cihaz türü ve özel isim ile listeleme/filtreleme.
- **MQTT ile Gerçek Zamanlı Haberleşme**  
  - Backend → ESP32 (komut gönderme)  
  - ESP32 → Backend (sensör verisi & durum)
- **Soft Delete**: Cihaz silindiğinde veritabanında `active=false` olarak işaretlenir.
- **Modüler Mimari**: Backend, frontend ve donanım kodları bağımsız çalışabilir.

---

## 📂 Klasör Yapısı

> Aşağıdaki yapı, bu depoda önerilen klasör organizasyonunu gösterir.

```
SmartIoT/
├── backend/                                      # Spring Boot backend
│   └── src/main/resources/
│       ├── application-example.properties        # Örnek backend ayar şablonu (güvenli)
│       └── application.properties (gitignored)   # Gerçek ayarlar (repoda tutulmaz)
├── frontend/                                     # React frontend
├── esp32/                                        # Arduino (ESP32) kodları
│   └── esp32_config.h                            # ESP32 ayar şablonu
├── database/                                     # PostgreSQL şema/yedek
├── mqtt/                                         # Mosquitto MQTT config
│   ├── mosquitto.conf
│   └── data/.gitkeep
├── docs/
│   └── SmartIoT_Web_Servis_Dokumantasyonu.pdf    # İndirilebilir Web Servis dökümanı
├── .env.example                                  # Frontend için ortam değişken örneği
├── .gitignore
└── README.md
```

---

## 📘 API Dokümantasyonu

- **Swagger UI (canlı test):**  
  `http://localhost:8080/swagger-ui.html`  
  (Ayrıca: `http://localhost:8080/swagger-ui/index.html#/`)

- **OpenAPI JSON:**  
  `http://localhost:8080/v3/api-docs`

- **Web Servis Dokümanı (PDF):**  
  `docs/SmartIoT_Web_Servis_Dokumantasyonu.pdf`  
  > PDF; base URL, örnek istek/yanıtlar, tipik akış ve hata kodlarıyla _insan-okur_ kılavuzdur. Swagger ise teknik şema + “Try it out” ile canlı deneme sunar.

> **Not:** Swagger için projeye `springdoc-openapi-starter-webmvc-ui` bağımlılığı eklenmiştir ve controller’lara `@Tag`, `@Operation`, `@ApiResponse`, `@Schema` açıklamaları işlenmiştir.

---

## ⚡ Hızlı Başlangıç

### 1) MQTT Broker (lokal)
```bash
mosquitto -c mqtt/mosquitto.conf
# varsayılan port: 1883
```

### 2) Backend (Spring Boot)
```bash
cd backend
# Java 17+ & Maven 3.8+
mvn spring-boot:run
```
> `application.properties` içindeki veritabanı ve MQTT ayarlarının doğru olduğundan emin olun.  
> MQTT broker varsayılanı örneklerde `tcp://localhost:1883` olacak şekilde düzenlenmiştir.

### 3) Frontend (React)
```bash
cd frontend
npm install
npm start
```
> `.env` içinde gerekli `REACT_APP_...` değişkenlerini tanımlayın.

### 4) ESP32 (Arduino IDE)
- `esp32/` altındaki kodları Arduino IDE ile açın.
- `esp32_config.h` dosyasını kendi Wi-Fi/MQTT bilgilerinize göre düzenleyin.
- ESP32’ye yükleyin.

---

## 🧩 Konfigürasyon

### Backend örnek ayar dosyası (repoya konur)
`backend/src/main/resources/application-example.properties`
```properties
# Uygulama
spring.application.name=smartiot
server.port=8080

# PostgreSQL (ENV ile override edilebilir)
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/postgres}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD}

# JPA
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# MQTT
mqtt.broker=${MQTT_BROKER:tcp://127.0.0.1:1883}
mqtt.clientId.publisher=${MQTT_CLIENT_PUB:SpringBootPublisher}
mqtt.clientId.subscriber=${MQTT_CLIENT_SUB:SpringBootSubscriber}
mqtt.topic.led=${MQTT_TOPIC_LED:led/control}
mqtt.topic.buzzer=${MQTT_TOPIC_BUZZER:buzzer/control}
mqtt.topic.servo=${MQTT_TOPIC_SERVO:servo/control}
mqtt.topic.rfid=${MQTT_TOPIC_RFID:rfid/uid}
mqtt.topic.dht=${MQTT_TOPIC_DHT:dht/data}
mqtt.topic.status=${MQTT_TOPIC_STATUS:status/device}

# Swagger / OpenAPI
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs
springdoc.packages-to-scan=com.example.smartiot.controller
springdoc.default-produces-media-type=application/json
springdoc.default-consumes-media-type=application/json
```

### Gerçek ayar dosyası (lokalde kalır, repoya konmaz)
`backend/src/main/resources/application.properties`  
> **.gitignore** ile **hariç** tutulmalıdır.  
> DB/MQTT şifreleri ve IP’leri bu dosyada veya **ENV değişkenleri** üzerinden verilmelidir.

`.gitignore` içine şu satırın eklendiğinden emin olun:
```
backend/src/main/resources/application.properties
```

> Daha önce repoya gerçek `application.properties` işlendi ise:
> ```bash
> git rm --cached backend/src/main/resources/application.properties
> git commit -m "chore: remove secrets from repo"
> ```

---

## 🧱 Sistem Mimarisi

1. **Frontend (React)**  
   Kullanıcıdan gelen komutları Backend’e HTTP ile yollar, sonuçları gösterir.

2. **Backend (Spring Boot)**  
   Kimlik doğrulama (JWT planlı), MQTT üzerinden cihazlara komut gönderme, sensör verilerini işleme ve veritabanına kaydetme.

3. **ESP32 (Arduino)**  
   MQTT broker’a bağlanır, belirli topic’leri dinler (`led/control`, `buzzer/control`, `servo/control`), sensör verilerini yayınlar (`dht/data`, `status/device`, `rfid/uid`).

4. **PostgreSQL**  
   Kullanıcı, cihaz, oda ve sensör verilerini saklar.

5. **MQTT Broker (Mosquitto)**  
   Backend ile ESP32 arasındaki mesaj akışını yönetir.

---

## 📡 MQTT Topic Listesi

| Topic            | Yön               | Açıklama                        |
|------------------|-------------------|----------------------------------|
| `led/control`    | Backend → ESP32   | LED aç/kapat komutu              |
| `buzzer/control` | Backend → ESP32   | Buzzer çalıştırma komutu         |
| `servo/control`  | Backend → ESP32   | Servo motor pozisyon kontrolü    |
| `rfid/uid`       | ESP32 → Backend   | RFID kart UID                    |
| `dht/data`       | ESP32 → Backend   | Sıcaklık/Nem sensör verileri     |
| `status/device`  | ESP32 → Backend   | Cihaz çevrimiçi/durum bilgisi    |

---

## 📘 Swagger İpuçları

- Bir endpoint’i aç → **Try it out** → alanları doldur → **Execute**.
- Alt bölümde: **curl**, **Request URL**, **Response** detaylarını görürsün.
- Controller’lar `@Tag`, `@Operation`, `@ApiResponse`, modeller `@Schema` ile açıklanmıştır.
- Gelişmiş (opsiyonel): JWT eklenirse Swagger’a “Authorize” butonu için `bearerAuth` şeması tanımlanabilir.

---

## ✅ Gereksinimler

- **Java 17+**
- **Maven 3.8+**
- **Node.js 18+**
- **PostgreSQL 15+**
- **Mosquitto MQTT Broker**
- **Arduino IDE + ESP32 Board Manager**

---

### Notlar
- Bu README, **Swagger dokümantasyonu** ve **indirilebilir Web Servis PDF’i** ile uyumludur.  
- Postman kullanacaksanız: **OpenAPI JSON**’u (`/v3/api-docs`) Postman’e **Import** ederek koleksiyon oluşturabilirsiniz.
 
---

## 📄 Lisans

Bu proje **MIT** lisansı ile lisanslanmıştır.

