package com.example.smartiot.service;

import com.example.smartiot.model.Device;
import com.example.smartiot.model.User;
import com.example.smartiot.model.UserDevice;
import com.example.smartiot.repository.DeviceRepository;
import com.example.smartiot.repository.UserDeviceRepository;
import com.example.smartiot.repository.UserRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final UserService userService;
    private final DeviceRepository deviceRepository;
    private final UserRoomRepository userRoomRepository;

    public List<UserDevice> getDevicesByUser(User user) { return userDeviceRepository.findByUser(user); }
    public List<UserDevice> getActiveDevicesByUser(User user) { return userDeviceRepository.findByUserAndActiveTrue(user); }
    public UserDevice save(UserDevice userDevice) { return userDeviceRepository.save(userDevice); }
    public Optional<UserDevice> getById(Long id) { return userDeviceRepository.findById(id); }
    public void deleteById(Long id) { userDeviceRepository.deleteById(id); }
    public List<UserDevice> getAllUserDevices() { return userDeviceRepository.findAll(); }

    public UserDevice setActiveStatus(Long id, boolean active) {
        UserDevice userDevice = userDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UserDevice not found"));
        userDevice.setActive(active);
        userDevice.setStatus(active ? UserDevice.Status.ACTIVE : UserDevice.Status.INACTIVE_RESERVED);
        return userDeviceRepository.save(userDevice);
    }

    public boolean deactivateUserDevice(Long id) {
        Optional<UserDevice> optionalDevice = userDeviceRepository.findById(id);
        if (optionalDevice.isPresent()) {
            UserDevice device = optionalDevice.get();
            device.setActive(false);
            device.setStatus(UserDevice.Status.INACTIVE_RESERVED);
            userDeviceRepository.save(device);
            return true;
        }
        return false;
    }

    // ====== Yeni: Remove akışı (temporary/replace) ======
    @Transactional
    public void removeUserDevice(Long userDeviceId, Long userId, String mode) {
        UserDevice ud = userDeviceRepository.findById(userDeviceId)
                .orElseThrow(() -> new IllegalArgumentException("UserDevice bulunamadı: " + userDeviceId));
        if (!ud.getUser().getId().equals(userId)) {
            throw new SecurityException("Bu cihazı kaldırma yetkiniz yok.");
        }

        if ("temporary".equalsIgnoreCase(mode)) {
            ud.setActive(false);
            ud.setStatus(UserDevice.Status.INACTIVE_RESERVED);
        } else if ("replace".equalsIgnoreCase(mode)) {
            ud.setActive(false);
            ud.setStatus(UserDevice.Status.RETIRED);
        } else {
            throw new IllegalArgumentException("mode 'temporary' veya 'replace' olmalı");
        }
        userDeviceRepository.save(ud);
    }

    // ====== Modelden otomatik atama: önce REAKTİVASYON, yoksa ilk boş ünite ======
    @Transactional
    public AssignedResult assignByModel(Long userId, String model, Long userRoomId, String assignedName) {
        if (userId == null || model == null || model.trim().isEmpty()
                || userRoomId == null
                || assignedName == null || assignedName.trim().isEmpty()) {
            throw new IllegalArgumentException("userId, model, userRoomId ve assignedName zorunludur.");
        }

        var user = userService.getUserById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Kullanıcı bulunamadı"));

        var room = userRoomRepository.findByIdAndUser_Id(userRoomId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Oda bulunamadı veya size ait değil"));

        // 1) Aynı kullanıcı + aynı model için REZERVE kayıt varsa → reaktivasyon (AYNI cihaz_id)
        Optional<UserDevice> reserved =
                userDeviceRepository.findFirstReservedByUserAndModel(userId, model);
        if (reserved.isPresent()) {
            UserDevice ud = reserved.get();
            ud.setActive(true);
            ud.setStatus(UserDevice.Status.ACTIVE);
            ud.setUserRoom(room);
            ud.setAssignedName(assignedName.trim());
            userDeviceRepository.saveAndFlush(ud);
            return new AssignedResult(ud.getDevice().getId());
        }

        // 2) Yoksa, bu modelde HİÇ kimseye ait olmayan ilk boş üniteyi seç ve ata
        final int MAX_RETRY = 3;
        for (int attempt = 1; attempt <= MAX_RETRY; attempt++) {
            Long deviceId = deviceRepository.pickOneAvailableDeviceIdByModel(model);
            if (deviceId == null) throw new OutOfStockException("Stokta uygun ünite yok: " + model);

            try {
                var ud = new UserDevice();
                ud.setUser(user);
                var deviceRef = new Device(); deviceRef.setId(deviceId);
                ud.setDevice(deviceRef);
                ud.setActive(true);
                ud.setStatus(UserDevice.Status.ACTIVE);
                ud.setAssignedName(assignedName.trim());
                ud.setUserRoom(room);

                userDeviceRepository.saveAndFlush(ud);
                return new AssignedResult(deviceId);
            } catch (DataIntegrityViolationException e) {
                if (attempt == MAX_RETRY) throw e; // unique index yarışı → tekrar dene
            }
        }
        throw new IllegalStateException("Atama yeniden denemelerden sonra başarısız");
    }

    public static record AssignedResult(Long deviceId) {}

    // ====== Yetki ve allowed list ======
    public boolean userHasAccess(Long userId, Long deviceId) {
        return userDeviceRepository.existsByUser_IdAndDevice_IdAndActiveTrue(userId, deviceId);
    }

    public List<Device> listAllowedDevices(Long userId) {
        return userDeviceRepository.findAllAllowedDevicesForUser(userId);
    }

    // ====== Eski havuz akışı (geri uyumlu – aynen korundu) ======
    public UserDevice registerOwnedDevice(Long userId, String deviceModel, String deviceName, String deviceUid, String aliasAsAssignedName) {
        var user = userService.getUserById(userId).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        var device = deviceRepository.findByDeviceUid(deviceUid).orElseGet(() -> {
            Device d = new Device();
            d.setDeviceModel(deviceModel);
            d.setDeviceName(deviceName);
            d.setDeviceUid(deviceUid);
            d.setActive(true);
            return deviceRepository.save(d);
        });

        if (userDeviceRepository.existsByUser_IdAndDevice_IdAndUserRoomIsNull(userId, device.getId())) {
            return userDeviceRepository.findByUser_IdAndUserRoomIsNullAndActiveTrue(userId).stream()
                    .filter(ud -> ud.getDevice().getId().equals(device.getId()))
                    .findFirst().orElseThrow(() -> new RuntimeException("Kayıt durumu beklenmedik."));
        }

        UserDevice ud = new UserDevice();
        ud.setUser(user);
        ud.setDevice(device);
        ud.setAssignedName(aliasAsAssignedName);
        ud.setUserRoom(null);
        ud.setActive(true);
        ud.setStatus(UserDevice.Status.ACTIVE);
        return userDeviceRepository.save(ud);
    }

    public List<UserDevice> getPool(Long userId) {
        return userDeviceRepository.findByUser_IdAndUserRoomIsNullAndActiveTrue(userId);
    }

    @Transactional
    public UserDevice assignFromPool(Long userDeviceId, Long userId, Long userRoomId, String assignedName) {
        UserDevice poolItem = userDeviceRepository.findByIdAndUser_Id(userDeviceId, userId)
                .orElseThrow(() -> new RuntimeException("Havuz kaydı bulunamadı"));
        if (poolItem.getUserRoom() != null) throw new RuntimeException("Seçilen kayıt havuzda değil.");

        var room = userRoomRepository.findByIdAndUser_Id(userRoomId, userId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı veya kullanıcıya ait değil"));

        long poolCount = userDeviceRepository
                .countByUser_IdAndDevice_IdAndUserRoomIsNullAndActiveTrue(userId, poolItem.getDevice().getId());

        if (poolCount == 1L) {
            poolItem.setUserRoom(room);
            poolItem.setAssignedName(assignedName);
            poolItem.setStatus(UserDevice.Status.ACTIVE);
            UserDevice saved = userDeviceRepository.save(poolItem);

            UserDevice refill = new UserDevice();
            refill.setUser(saved.getUser());
            refill.setDevice(saved.getDevice());
            refill.setUserRoom(null);
            refill.setAssignedName(null);
            refill.setActive(true);
            refill.setStatus(UserDevice.Status.ACTIVE);
            userDeviceRepository.save(refill);

            return saved;
        } else {
            UserDevice newUd = new UserDevice();
            newUd.setUser(poolItem.getUser());
            newUd.setDevice(poolItem.getDevice());
            newUd.setUserRoom(room);
            newUd.setAssignedName(assignedName);
            newUd.setActive(true);
            newUd.setStatus(UserDevice.Status.ACTIVE);
            return userDeviceRepository.save(newUd);
        }
    }

    public UserDevice addDeviceToRoom(Long userId, Long deviceId, Long userRoomId, String assignedName) {
        if (userId == null || deviceId == null || userRoomId == null
                || assignedName == null || assignedName.trim().isEmpty()) {
            throw new IllegalArgumentException("userId, deviceId, userRoomId ve assignedName zorunludur.");
        }

        var user = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        var device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("Cihaz (devices) bulunamadı"));
        var room = userRoomRepository.findByIdAndUser_Id(userRoomId, userId)
                .orElseThrow(() -> new RuntimeException("Oda bulunamadı veya kullanıcıya ait değil"));

        UserDevice ud = new UserDevice();
        ud.setUser(user);
        ud.setDevice(device);
        ud.setUserRoom(room);
        ud.setAssignedName(assignedName.trim());
        ud.setActive(true);
        ud.setStatus(UserDevice.Status.ACTIVE);
        return userDeviceRepository.save(ud);
    }
}
