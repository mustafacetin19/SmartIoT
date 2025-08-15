import React, { useEffect, useMemo, useState } from "react";
import "./DeviceSelectionPage.css";

const API = "http://localhost:8080";

// Model ikonları
const MODEL_ICONS = {
  "Led-White": "💡",
  "Led-Red": "🔴",
  "Led-Blue": "🔵",
  "Led-Yellow": "🟡",
  RFID: "🧾",
  SERVO: "⚙️",
  BUZZER: "🔔",
  DHT11: "🌡️",
};

const MODELS = Object.keys(MODEL_ICONS);

// Küçük yardımcılar
const cls = (...xs) => xs.filter(Boolean).join(" ");

export default function DeviceSelectionPage() {
  const [user, setUser] = useState(null);

  // stok & form
  const [stock, setStock] = useState([]);
  const [model, setModel] = useState(MODELS[0]);
  const [alias, setAlias] = useState("");
  const [loading, setLoading] = useState(false);

  // odalar
  const [rooms, setRooms] = useState([]);
  const [roomId, setRoomId] = useState("");

  // modal/mesaj
  const [roomModalOpen, setRoomModalOpen] = useState(false);
  const [newRoom, setNewRoom] = useState("");
  const [roomBusy, setRoomBusy] = useState(false);
  const [toast, setToast] = useState("");

  useEffect(() => {
    const u = localStorage.getItem("user");
    if (u) {
      try { setUser(JSON.parse(u)); } catch {}
    }
  }, []);

  useEffect(() => {
    if (!user?.id) return;
    refreshStock();
    refreshRooms();
  }, [user?.id]);

  const refreshStock = async () => {
    try {
      const r = await fetch(`${API}/api/devices/stock`);
      setStock(await r.json());
    } catch {}
  };

  const refreshRooms = async () => {
    try {
      const r = await fetch(`${API}/api/user-rooms/by-user/${user.id}`);
      const data = await r.json();
      setRooms(data || []);
    } catch {}
  };

  const availableByModel = useMemo(() => {
    const m = {};
    stock.forEach((s) => (m[s.model] = s.availableUnits));
    return m;
  }, [stock]);

  const hasStock = (availableByModel[model] ?? 0) > 0;
  const requiredRoomMissing = !roomId;
  const requiredAliasMissing = !alias.trim();
  const canAssign =
    user?.id && hasStock && !loading && !requiredRoomMissing && !requiredAliasMissing;

  // --------- Actions ----------
  const assign = async () => {
    if (!canAssign) return;
    setLoading(true);
    try {
      const res = await fetch(`${API}/api/user-devices/assign-by-model`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          userId: user.id,
          model,
          userRoomId: Number(roomId),
          assignedName: alias.trim(),
        }),
      });
      if (!res.ok) throw new Error(await res.text());
      setAlias("");
      await refreshStock();
      showToast("✅ Cihaz atandı!");
    } catch (e) {
      showToast(`⚠️ ${e.message || "Atama hatası"}`);
    } finally {
      setLoading(false);
    }
  };

  const createRoom = async () => {
    if (!newRoom.trim()) return;
    setRoomBusy(true);
    try {
      const r = await fetch(`${API}/api/user-rooms`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ userId: user.id, roomName: newRoom.trim() }),
      });
      if (!r.ok) throw new Error(await r.text());
      setNewRoom("");
      setRoomModalOpen(false);
      await refreshRooms();
      showToast("✅ Oda oluşturuldu");
    } catch (e) {
      showToast(`⚠️ ${e.message || "Oda oluşturma hatası"}`);
    } finally {
      setRoomBusy(false);
    }
  };

  const showToast = (t) => {
    setToast(t);
    setTimeout(() => setToast(""), 2400);
  };

  return (
    <div className="ds-page">
      <header className="ds-header">
        <div>
          <h1 className="ds-title">Cihaz Ekle (Modelden Atama)</h1>
          <p className="ds-sub">
            Modeli seç, oda ve takma adı belirle. Stok varsa sistem uygun bir
            ünitenin atamasını otomatik yapar.
          </p>
        </div>
        <div className="ds-summary">
          <div className="ds-summary-row">
            <span className="ds-summary-key">Seçilen Model</span>
            <span className="ds-summary-val">
              {MODEL_ICONS[model]} {model}
            </span>
          </div>
          <div className="ds-summary-row">
            <span className="ds-summary-key">Stok</span>
            <span
              className={cls(
                "ds-badge",
                (availableByModel[model] ?? 0) > 0 ? "ds-badge--ok" : "ds-badge--no"
              )}
            >
              {availableByModel[model] ?? 0}
            </span>
          </div>
          <div className="ds-summary-row">
            <span className="ds-summary-key">Oda</span>
            <span className="ds-summary-val">
              {rooms.find((r) => String(r.id) === String(roomId))?.roomName || "—"}
            </span>
          </div>
          <div className="ds-summary-row">
            <span className="ds-summary-key">Takma Ad</span>
            <span className="ds-summary-val">{alias || "—"}</span>
          </div>
        </div>
      </header>

      {/* FORM CARD */}
      <section className="ds-card ds-card--form">
        {/* Model çipleri */}
        <div className="ds-field">
          <label>Model</label>
          <div className="ds-chip-row" role="tablist" aria-label="Model seçimi">
            {MODELS.map((m) => (
              <button
                key={m}
                role="tab"
                aria-selected={model === m}
                className={cls("ds-chip", model === m && "ds-chip--active")}
                onClick={() => setModel(m)}
                title={m}
              >
                <span className="ds-chip-ico">{MODEL_ICONS[m]}</span>
                <span>{m}</span>
                <span
                  className={cls(
                    "ds-dot",
                    (availableByModel[m] ?? 0) > 0 ? "ds-dot--ok" : "ds-dot--no"
                  )}
                  title={`Stok: ${availableByModel[m] ?? 0}`}
                />
              </button>
            ))}
          </div>
        </div>

        {/* Oda seçimi */}
        <div className="ds-field">
          <label>
            Oda <span className="ds-req">*</span>
          </label>
          <div className="ds-inline">
            <select
              className={requiredRoomMissing ? "ds-invalid" : ""}
              value={roomId}
              onChange={(e) => setRoomId(e.target.value)}
            >
              <option value="">(seçin)</option>
              {rooms.map((r) => (
                <option key={r.id} value={r.id}>
                  {r.roomName}
                </option>
              ))}
            </select>
            <button
              className="ds-btn ds-btn--ghost"
              type="button"
              onClick={() => setRoomModalOpen(true)}
            >
              + Oda Oluştur
            </button>
          </div>
          {requiredRoomMissing && (
            <div className="ds-err">Oda seçimi zorunludur.</div>
          )}
          {!rooms.length && (
            <div className="ds-hint">
              Henüz odanız yok. “+ Oda Oluştur” ile başlayın.
            </div>
          )}
        </div>

        {/* Takma ad */}
        <div className="ds-field">
          <label>
            Takma ad <span className="ds-req">*</span>
          </label>
          <input
            className={requiredAliasMissing ? "ds-invalid" : ""}
            value={alias}
            onChange={(e) => setAlias(e.target.value)}
            placeholder="Örn: Salon Beyaz, Mutfak Servo..."
            maxLength={40}
          />
          <div className="ds-meta">
            {alias.length}/40
            {requiredAliasMissing && (
              <span className="ds-err ml8">Takma ad zorunludur.</span>
            )}
          </div>
        </div>

        {/* CTA */}
        <div className="ds-actions">
          <button
            className="ds-btn ds-btn--primary"
            onClick={assign}
            disabled={!canAssign}
            title={
              !hasStock
                ? "Bu modelde stok yok"
                : requiredRoomMissing || requiredAliasMissing
                ? "Oda ve takma ad zorunludur"
                : ""
            }
          >
            {loading ? "Atanıyor..." : "Cihaz Ekle"}
          </button>

          {!hasStock && (
            <span className="ds-msg">Bu modelde şu an stok yok.</span>
          )}
        </div>
      </section>

      {/* Atanmış cihazlar */}
      <AssignedDevices userId={user?.id} />

      {/* Toast */}
      {toast && <div className="ds-toast">{toast}</div>}

      {/* Oda oluşturma modalı */}
      {roomModalOpen && (
        <div
          className="ds-modal"
          role="dialog"
          aria-modal="true"
          aria-label="Oda oluştur"
        >
          <div className="ds-modal-card">
            <h3>Yeni Oda</h3>
            <input
              autoFocus
              value={newRoom}
              onChange={(e) => setNewRoom(e.target.value)}
              placeholder="Örn: Salon, Mutfak, Ofis..."
            />
            <div className="ds-inline mt12">
              <button
                className="ds-btn ds-btn--secondary"
                onClick={createRoom}
                disabled={roomBusy || !newRoom.trim()}
              >
                {roomBusy ? "Oluşturuluyor..." : "Oluştur"}
              </button>
              <button
                className="ds-btn ds-btn--ghost"
                onClick={() => {
                  setRoomModalOpen(false);
                  setNewRoom("");
                }}
              >
                İptal
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

/* ---------------- Atanmış cihazlar ve kontroller ---------------- */

function AssignedDevices({ userId }) {
  const [list, setList] = useState([]);
  const [refresh, setRefresh] = useState(0);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!userId) return;
    (async () => {
      setBusy(true);
      try {
        const r = await fetch(`${API}/api/user-devices/allowed/${userId}`);
        setList(await r.json());
      } catch {}
      setBusy(false);
    })();
  }, [userId, refresh]);

  return (
    <section className="ds-card">
      <div className="ds-card-head">
        <h2>Atanmış Cihazlarım</h2>
        <button
          className="ds-btn ds-btn--ghost"
          onClick={() => setRefresh((x) => x + 1)}
        >
          {busy ? "Yükleniyor..." : "Yenile"}
        </button>
      </div>

      {!list.length ? (
        <div className="ds-empty">
          Henüz atanmış cihaz yok. Yukarıdan bir model seçerek ekleyebilirsin.
        </div>
      ) : (
        <div className="ds-grid">
          {list.map((d) => (
            <DeviceCard key={d.id} device={d} />
          ))}
        </div>
      )}
    </section>
  );
}

function DeviceCard({ device }) {
  const icon = MODEL_ICONS[device.deviceModel] || "🔧";
  return (
    <div className="ds-device">
      <div className="ds-device-head">
        <div className="ds-device-ico">{icon}</div>
        <div>
          <div className="ds-device-name">
            {device.deviceName || device.deviceModel}
          </div>
          <div className="ds-device-sub">
            Model: {device.deviceModel} • ID: {device.id}
          </div>
        </div>
      </div>
      <DeviceControls device={device} />
    </div>
  );
}

function DeviceControls({ device }) {
  const model = (device.deviceModel || "").toUpperCase();
  if (model.startsWith("LED")) return <LedControl device={device} />;
  if (model.startsWith("SERVO")) return <ServoControl device={device} />;
  if (model.startsWith("BUZZER")) return <BuzzerControl device={device} />;
  return <div className="ds-note">Bu model için kontrol yok.</div>;
}

function LedControl({ device }) {
  const [on, setOn] = useState(false);
  const [busy, setBusy] = useState(false);
  const toggle = async (state) => {
    setBusy(true);
    try {
      const url = new URL(`${API}/api/control/led`);
      url.searchParams.set("deviceId", device.id);
      url.searchParams.set("state", state ? "true" : "false");
      const r = await fetch(url, { method: "POST" });
      if (!r.ok) throw new Error(await r.text());
      setOn(state);
    } catch (e) {
      alert(e.message || "LED komutu başarısız");
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="ds-controls">
      <button
        className={cls("ds-btn", on ? "ds-btn--success" : "ds-btn--secondary")}
        onClick={() => toggle(!on)}
        disabled={busy}
      >
        {on ? "LED Kapat" : "LED Aç"}
      </button>
    </div>
  );
}

function ServoControl({ device }) {
  const [angle, setAngle] = useState(0);
  const [busy, setBusy] = useState(false);
  const send = async (a) => {
    setBusy(true);
    try {
      const url = new URL(`${API}/api/control/servo`);
      url.searchParams.set("deviceId", device.id);
      url.searchParams.set("angle", a);
      const r = await fetch(url, { method: "POST" });
      if (!r.ok) throw new Error(await r.text());
    } catch (e) {
      alert(e.message || "Servo komutu başarısız");
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="ds-controls">
      <input
        type="range"
        min="0"
        max="180"
        value={angle}
        onChange={(e) => setAngle(Number(e.target.value))}
      />
      <div className="ds-inline">
        <span className="ds-note">Açı: {angle}°</span>
        <button
          className="ds-btn ds-btn--primary"
          onClick={() => send(angle)}
          disabled={busy}
        >
          Gönder
        </button>
      </div>
    </div>
  );
}

function BuzzerControl({ device }) {
  const [busy, setBusy] = useState(false);
  const beep = async () => {
    setBusy(true);
    try {
      const url = new URL(`${API}/api/control/buzzer`);
      url.searchParams.set("deviceId", device.id);
      url.searchParams.set("action", "beep");
      const r = await fetch(url, { method: "POST" });
      if (!r.ok) throw new Error(await r.text());
    } catch (e) {
      alert(e.message || "Buzzer komutu başarısız");
    } finally {
      setBusy(false);
    }
  };
  return (
    <div className="ds-controls">
      <button
        className="ds-btn ds-btn--warning"
        onClick={beep}
        disabled={busy}
      >
        Beep
      </button>
    </div>
  );
}
