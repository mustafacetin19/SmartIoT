// PATH: src/pages/ScenesPage.jsx
import React, { useEffect, useMemo, useState } from "react";
import {
  getScenes,
  getMyUserDevicesLight,
  createScene,
  updateScene,
  deleteScene,
  runScene,
  toggleScene,
} from "../api";
import "./ScenesPage.css";

const EMPTY_ROW = { deviceId: "", command: "ON", value: "" };

export default function ScenesPage() {
  const [devices, setDevices] = useState([]);
  const [scenes, setScenes] = useState([]);
  const [name, setName] = useState("");
  const [enabled, setEnabled] = useState(true);
  const [rows, setRows] = useState([{ ...EMPTY_ROW }]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [editingId, setEditingId] = useState(null);

  // küçük toast
  const [toast, setToast] = useState("");
  const showToast = (t) => {
    setToast(t);
    setTimeout(() => setToast(""), 2200);
  };

  async function loadAll() {
    setLoading(true);
    try {
      const [devs, sc] = await Promise.all([getMyUserDevicesLight(), getScenes()]);
      setDevices(devs);
      setScenes(sc || []);
    } catch (e) {
      console.error(e);
      alert("Veriler alınırken hata oluştu.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { loadAll(); }, []);

  const setRow = (i, patch) =>
    setRows((r) => r.map((x, idx) => (idx === i ? { ...x, ...patch } : x)));

  const addRow = () => setRows((r) => [...r, { ...EMPTY_ROW }]);
  const removeRow = (i) => setRows((r) => r.filter((_, idx) => idx !== i));

  const ensureDeviceOption = (id) => {
    if (!id) return;
    const exists = devices.some((d) => String(d.id) === String(id));
    if (!exists) setDevices((prev) => [...prev, { id, label: `#${id} (devre dışı/erişilemez)` }]);
  };

  const resetForm = () => {
    setEditingId(null);
    setName("");
    setEnabled(true);
    setRows([{ ...EMPTY_ROW }]);
  };

  const startEdit = (scene) => {
    setEditingId(scene.id);
    setName(scene.name || "");
    setEnabled(Boolean(scene.enabled));
    const mapped = Array.isArray(scene.commands)
      ? scene.commands.map((c) => {
          ensureDeviceOption(c.userDeviceId);
          return {
            deviceId: c.userDeviceId ?? "",
            command: c.command || "ON",
            value: c.value ?? "",
          };
        })
      : [{ ...EMPTY_ROW }];
    setRows(mapped.length ? mapped : [{ ...EMPTY_ROW }]);
    window.scrollTo({ top: 0, behavior: "smooth" });
  };

  const duplicate = async (scene) => {
    try {
      setSaving(true);
      const commands = (scene.commands || []).map((c, i) => ({
        userDeviceId: c.userDeviceId,
        command: c.command || "ON",
        value: c.value ?? null,
        sortOrder: i,
      }));
      await createScene({
        name: `${scene.name || "Sahne"} (Kopya)`,
        enabled: Boolean(scene.enabled),
        commands,
      });
      await loadAll();
      showToast("Sahne kopyalandı.");
    } catch (e) {
      console.error(e);
      alert("Kopyalama hatası: " + e.message);
    } finally {
      setSaving(false);
    }
  };

  const removeScene = async (scene) => {
    if (!window.confirm(`"${scene.name}" sahnesini silmek istiyor musun?`)) return;
    try {
      await deleteScene(scene.id);    // gövdesiz 200 gelebilir → http() artık güvenli
      if (editingId === scene.id) resetForm();
      await loadAll();
      showToast("Sahne silindi.");
    } catch (e) {
      console.error(e);
      alert("Silme hatası: " + e.message);
    }
  };

  const runNow = async (scene) => {
    try {
      await runScene(scene.id);       // gövdesiz 200 gelebilir → http() güvenli
      showToast("Sahne çalıştırıldı.");
    } catch (e) {
      console.error(e);
      alert("Çalıştırma hatası: " + e.message);
    }
  };

  const toggleOne = async (scene) => {
    try {
      await toggleScene(scene.id, !scene.enabled);
      setScenes((list) =>
        list.map((s) => (s.id === scene.id ? { ...s, enabled: !s.enabled } : s))
      );
    } catch (e) {
      console.error(e);
      alert("Güncelleme hatası: " + e.message);
    }
  };

  async function save() {
    const cleaned = rows
      .map((r, i) => ({
        userDeviceId: r.deviceId ? Number(r.deviceId) : null,
        command: r.command || "ON",
        value: r.value?.trim() ? r.value.trim() : null,
        sortOrder: i,
      }))
      .filter((r) => r.userDeviceId);

    if (!name.trim()) return alert("Sahne adı zorunludur.");
    if (cleaned.length === 0) return alert("En az bir komut satırı ekleyin.");

    setSaving(true);
    try {
      if (editingId) {
        await updateScene(editingId, { name: name.trim(), enabled, commands: cleaned });
        showToast("Sahne güncellendi.");
      } else {
        await createScene({ name: name.trim(), enabled, commands: cleaned });
        showToast("Sahne kaydedildi.");
      }
      resetForm();
      await loadAll();
    } catch (e) {
      console.error(e);
      alert("Kaydederken hata oluştu. (Detay: " + e.message + ")");
    } finally {
      setSaving(false);
    }
  }

  const editing = useMemo(() => Boolean(editingId), [editingId]);

  return (
    <div className="pageContainer">
      <div className="pageGrid">
        <div className="sceneFormCard">
          <div className="headerLine">
            <h2>{editing ? `Sahneyi Düzenle #${editingId}` : "Yeni Sahne"}</h2>
            <label className="switch">
              <input type="checkbox" checked={enabled} onChange={(e) => setEnabled(e.target.checked)} />
              <span>Etkin</span>
            </label>
          </div>

          <label className="field">
            <span>Sahne Adı</span>
            <input
              className="input"
              placeholder="Örn: Eve Geliş"
              value={name}
              onChange={(e) => setName(e.target.value)}
            />
          </label>

          <div className="commands">
            <div className="commandsHead">
              <span>Cihaz</span><span>Komut</span><span>Değer (opsiyonel)</span>
            </div>

            {rows.map((r, i) => (
              <div key={i} className="row">
                <div className="rowGrid">
                  <select
                    value={r.deviceId}
                    onChange={(e) => setRow(i, { deviceId: e.target.value })}
                  >
                    <option value="">Seçiniz...</option>
                    {devices
                      .slice()
                      .sort((a, b) =>
                        String(a.label).includes("(devre dışı)") - String(b.label).includes("(devre dışı)")
                      )
                      .map((d) => (
                        <option key={d.id} value={d.id}>{d.label}</option>
                      ))}
                  </select>

                  <select
                    value={r.command}
                    onChange={(e) => setRow(i, { command: e.target.value })}
                  >
                    <option value="ON">ON</option>
                    <option value="OFF">OFF</option>
                    <option value="SET">SET</option>
                  </select>

                  <input
                    className="input"
                    placeholder="örn: 90 / #FFAA00 / HIGH"
                    value={r.value || ""}
                    onChange={(e) => setRow(i, { value: e.target.value })}
                  />
                </div>

                <div className="actions">
                  <button type="button" className="btnGhost" onClick={() => removeRow(i)}>
                    Satır Sil
                  </button>
                </div>
              </div>
            ))}

            <div className="row">
              <button type="button" className="btnGhost" onClick={addRow}>
                + Satır Ekle
              </button>
              <div className="flexGrow" />
              {editing && (
                <button type="button" className="btnGhost" onClick={resetForm} style={{ marginRight: 8 }}>
                  İptal
                </button>
              )}
              <button type="button" className="btnPrimary" disabled={saving} onClick={save}>
                {editing ? "Güncelle" : "Kaydet"}
              </button>
            </div>
          </div>
        </div>

        <div className="sceneListCard">
          <h3>Kayıtlı Sahneler</h3>
          {loading ? (
            <div className="muted">Yükleniyor…</div>
          ) : scenes.length === 0 ? (
            <div className="muted">Kayıtlı sahne yok.</div>
          ) : (
            <ul className="sceneList">
              {scenes.map((s) => (
                <li key={s.id}>
                  <div className="title">
                    <span className={`dot ${s.enabled ? "on" : ""}`} />
                    {s.name}
                  </div>

                  <div className="muted">
                    {(Array.isArray(s.commands) ? s.commands.length : 0)} komut
                  </div>

                  <div className="actions">
                    <button
                      type="button"
                      className={`chipToggle ${s.enabled ? "on" : "off"}`}
                      title={s.enabled ? "Devre dışı bırak" : "Etkinleştir"}
                      onClick={() => toggleOne(s)}
                    >
                      {s.enabled ? "Açık" : "Kapalı"}
                    </button>
                    <button type="button" className="btnGhost" onClick={() => startEdit(s)}>
                      Düzenle
                    </button>
                    <button type="button" className="btnGhost" onClick={() => duplicate(s)}>
                      Kopyala
                    </button>
                    <button type="button" className="btnGhost" onClick={() => runNow(s)}>
                      Çalıştır
                    </button>
                    <button
                      type="button"
                      className="btnGhost"
                      onClick={() => removeScene(s)}
                      style={{ borderColor: "rgba(255,80,80,.45)", color: "#ffb3b3" }}
                    >
                      Sil
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {toast && <div className="toast">{toast}</div>}
    </div>
  );
}
