package com.example.smartiot.service;

import com.example.smartiot.model.*;
import com.example.smartiot.repository.DeviceRepository;
import com.example.smartiot.repository.UserDeviceRepository;
import com.example.smartiot.repository.UserRoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserDeviceService {

    @Autowired
    private UserDeviceRepository userDeviceRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRoomRepository userRoomRepository;

    public List<UserDevice> getDevicesByUser(User user) {
        return userDeviceRepository.findByUser(user);
    }

    public List<UserDevice> getActiveDevicesByUser(User user) {
        return userDeviceRepository.findByUserAndActiveTrue(user);
    }

    public UserDevice save(UserDevice userDevice) {
        return userDeviceRepository.save(userDevice);
    }

    public Optional<UserDevice> getById(Long id) {
        return userDeviceRepository.findById(id);
    }

    public void deleteById(Long id) {
        userDeviceRepository.deleteById(id);
    }

    public List<UserDevice> getAllUserDevices() {
        return userDeviceRepository.findAll();
    }

    public UserDevice setActiveStatus(Long id, boolean active) {
        UserDevice userDevice = userDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserDevice not found"));
        userDevice.setActive(active);
        return userDeviceRepository.save(userDevice);
    }

    public boolean deactivateUserDevice(Long id) {
        Optional<UserDevice> optionalDevice = userDeviceRepository.findById(id);
        if (optionalDevice.isPresent()) {
            UserDevice device = optionalDevice.get();
            device.setActive(false);
            userDeviceRepository.save(device);
            return true;
        } else {
            return false;
        }
    }

    // ===================== HAVUZ (kullanıcının kendi kataloğu) =====================
    public UserDevice registerOwnedDevice(Long userId, String deviceModel, String deviceName, String deviceUid, String aliasAsAssignedName) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Device device = deviceRepository.findByDeviceUid(deviceUid).orElseGet(() -> {
            Device d = new Device();
            d.setDeviceModel(deviceModel);
            d.setDeviceName(deviceName);
            d.setDeviceUid(deviceUid);
            d.setActive(true);
            return deviceRepository.save(d);
        });

        // Aynı cihazdan havuzda zaten 1 kayıt varsa onu döndür
        if (userDeviceRepository.existsByUser_IdAndDevice_IdAndUserRoomIsNull(userId, device.getId())) {
            return userDeviceRepository.findByUser_IdAndUserRoomIsNullAndActiveTrue(userId).stream()
                    .filter(ud -> ud.getDevice().getId().equals(device.getId()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Kayıt durumu beklenmedik."));
        }

        UserDevice ud = new UserDevice();
        ud.setUser(user);
        ud.setDevice(device);
        ud.setAssignedName(aliasAsAssignedName);
        ud.setUserRoom(null);         // havuz/katalog
        ud.setActive(true);
        return userDeviceRepository.save(ud);
    }

    public List<UserDevice> getPool(Long userId) {
        return userDeviceRepository.findByUser_IdAndUserRoomIsNullAndActiveTrue(userId);
    }

    /**
     * ✅ KATALOGTAN (HAVUZDAN) ODAYA EKLE
     *
     * - Eğer bu kullanıcı + cihaz için havuzda SADECE 1 kayıt varsa:
     *     ➜ o kaydı ODAYA TAŞI (ID aynı kalsın)
     *     ➜ katalog kaybolmasın diye HAVUZA otomatik bir kopya ekle
     * - Eğer havuzda birden fazla kayıt varsa:
     *     ➜ katalog öğesine dokunma, KOPYA oluşturup odaya ekle
     */
    @Transactional
    public UserDevice assignFromPool(Long userDeviceId, Long userId, Long userRoomId, String assignedName) {
        // 1) Havuz kaydını bul (room=null olmalı ve kullanıcıya ait olmalı)
        UserDevice poolItem = userDeviceRepository.findByIdAndUser_Id(userDeviceId, userId)
                .orElseThrow(() -> new RuntimeException("Havuz kaydı bulunamadı"));

        if (poolItem.getUserRoom() != null) {
            throw new RuntimeException("Seçilen kayıt havuzda değil (zaten bir odaya atanmış).");
        }

        // 2) Odayı doğrula
        UserRoom room = userRoomRepository.findByIdAndUser_Id(userRoomId, userId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı veya kullanıcıya ait değil"));

        // 3) Bu kullanıcı + cihaz için kaç adet havuz kaydı var?
        long poolCount = userDeviceRepository
                .countByUser_IdAndDevice_IdAndUserRoomIsNullAndActiveTrue(userId, poolItem.getDevice().getId());

        if (poolCount == 1L) {
            // ✅ İlk atama: mevcut havuz satırını DOLDUR (ID aynı kalsın)
            poolItem.setUserRoom(room);
            poolItem.setAssignedName(assignedName);
            UserDevice saved = userDeviceRepository.save(poolItem);

            // ve katalog kaybolmasın diye otomatik bir HAVUZ kopyası bırak
            UserDevice refill = new UserDevice();
            refill.setUser(saved.getUser());
            refill.setDevice(saved.getDevice());
            refill.setUserRoom(null);
            refill.setAssignedName(null);
            refill.setActive(true);
            userDeviceRepository.save(refill);

            return saved;
        } else {
            // ➜ KOPYA oluştur (katalog öğesine dokunma)
            UserDevice newUd = new UserDevice();
            newUd.setUser(poolItem.getUser());
            newUd.setDevice(poolItem.getDevice());
            newUd.setUserRoom(room);
            newUd.setAssignedName(assignedName);
            newUd.setActive(true);
            return userDeviceRepository.save(newUd);
        }
    }

    // ===================== Katalogtan (global devices) direkt ekleme =====================
    public UserDevice addDeviceToRoom(Long userId, Long deviceId, Long userRoomId, String assignedName) {
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Cihaz (devices) bulunamadı"));

        UserRoom room = userRoomRepository.findByIdAndUser_Id(userRoomId, userId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı veya kullanıcıya ait değil"));

        UserDevice ud = new UserDevice();
        ud.setUser(user);
        ud.setDevice(device);
        ud.setUserRoom(room);
        ud.setAssignedName(assignedName);
        ud.setActive(true);

        return userDeviceRepository.save(ud);
    }
}
