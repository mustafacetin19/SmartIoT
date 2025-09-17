// src/components/SceneForm.jsx
import React, { useEffect, useState } from "react";
import { createScene, getMyUserDevicesLight, getCurrentUserId } from "../api";

export default function SceneForm({ onSaved }) {
  const [name, setName] = useState("");
  const [enabled, setEnabled] = useState(true);
  const [rows, setRows] = useState([{ userDeviceId: "", command: "ON", value: "", sortOrder: 0 }]);
  const [options, setOptions] = useState([]);

  useEffect(() => {
    let ok = true;
    (async () => {
      try {
        const opts = await getMyUserDevicesLight();
        if (ok) setOptions(opts);
      } catch (e) {
        console.error(e);
        setOptions([]);
      }
    })();
    return () => { ok = false; };
  }, []);

  const addRow = () =>
    setRows((r) => [...r, { userDeviceId: "", command: "ON", value: "", sortOrder: r.length }]);

  const delRow = (i) => setRows((r) => r.filter((_, idx) => idx !== i));

  const changeRow = (i, patch) =>
    setRows((r) => r.map((row, idx) => (idx === i ? { ...row, ...patch } : row)));

  const save = async () => {
    const userId = getCurrentUserId();
    if (!userId) {
      alert("Kullanıcı bulunamadı (userId). Lütfen giriş yapın veya localStorage’a userId yazın.");
      return;
    }
    const payload = {
      userId,
      name: name?.trim(),
      enabled,
      commands: rows
        .filter((r) => r.userDeviceId)
        .map((r, i) => ({
          userDeviceId: Number(r.userDeviceId),
          command: r.command,
          value: r.value?.trim() || null,
          sortOrder: i,
        })),
    };
    await createScene(payload);
    setName("");
    setRows([{ userDeviceId: "", command: "ON", value: "", sortOrder: 0 }]);
    onSaved && onSaved();
  };

  return (
    <div className="card">
      <h3>Yeni Sahne</h3>
      <label className="lbl">
        <input
          type="checkbox"
          checked={enabled}
          onChange={(e) => setEnabled(e.target.checked)}
          style={{ marginRight: 8 }}
        />
        Etkin
      </label>

      <div className="field">
        <label>Sahne Adı</label>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Örn: Eve Geliş"
        />
      </div>

      <div className="field">
        <label>Komutlar</label>
        <div className="rows">
          {rows.map((r, i) => (
            <div key={i} className="row">
              <select
                value={r.userDeviceId}
                onChange={(e) => changeRow(i, { userDeviceId: e.target.value })}
              >
                <option value="">Seçiniz...</option>
                {options.map((o) => (
                  <option key={o.id} value={o.id}>
                    {o.label}
                  </option>
                ))}
              </select>

              <select
                value={r.command}
                onChange={(e) => changeRow(i, { command: e.target.value })}
              >
                <option>ON</option>
                <option>OFF</option>
                <option>SET</option>
                <option>TOGGLE</option>
              </select>

              <input
                value={r.value}
                onChange={(e) => changeRow(i, { value: e.target.value })}
                placeholder="örn: 90 / #FFAA00 / HIGH"
              />

              <button type="button" onClick={() => delRow(i)}>
                Satır Sil
              </button>
            </div>
          ))}
        </div>

        <button type="button" onClick={addRow}>
          + Satır Ekle
        </button>
      </div>

      <div className="actions">
        <button type="button" onClick={save}>
          Kaydet
        </button>
      </div>
    </div>
  );
}
