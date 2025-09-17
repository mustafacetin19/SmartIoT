// PATH: src/api.js
const BASE = "/api";

// Güvenli fetch sarmalayıcı (boş gövdede JSON parse etmez)
async function http(path, opts = {}) {
  const res = await fetch(BASE + path, {
    headers: { "Content-Type": "application/json", ...(opts.headers || {}) },
    credentials: "include",
    ...opts,
  });

  if (!res.ok) {
    // Hata içerikleri çoğu zaman düz metin; önce text al
    const errText = await res.text().catch(() => "");
    throw new Error(errText || `${res.status} ${res.statusText}`);
  }

  // Bazı uçlar 200 ama boş gövde döndürüyor → güvenli şekilde parse et
  const text = await res.text().catch(() => "");
  if (!text || text.trim() === "") return null;

  try {
    return JSON.parse(text);
  } catch {
    // JSON değilse (ör. plain text), düz metni döndür
    return text;
  }
}

/* -------------------------------------------------------
 * Oturum yardımcıları
 * -----------------------------------------------------*/
export function setSession(userLike) {
  const id = String(userLike?.id ?? userLike?.userId ?? "");
  const first =
    userLike?.first_name ?? userLike?.firstName ?? userLike?.firstname ?? "";
  const last =
    userLike?.last_name ?? userLike?.lastName ?? userLike?.lastname ?? "";
  const fullName =
    userLike?.fullName ??
    userLike?.full_name ??
    (first && last ? `${first} ${last}` : "");

  if (id) localStorage.setItem("userId", id);
  if (first) localStorage.setItem("first_name", first);
  if (last) localStorage.setItem("last_name", last);
  if (fullName) localStorage.setItem("fullName", fullName);

  try { localStorage.setItem("user", JSON.stringify(userLike)); } catch {}

  window.dispatchEvent(new StorageEvent("storage", { key: "userId", newValue: id }));
}

export function getCurrentUserId() {
  return localStorage.getItem("userId") || localStorage.getItem("id") || "";
}

export function getCurrentUserName() {
  const full = localStorage.getItem("fullName");
  const first = localStorage.getItem("first_name");
  const last = localStorage.getItem("last_name");
  if (full && full.trim()) return full.trim();
  if (first || last) return [first, last].filter(Boolean).join(" ").trim();
  return "Misafir";
}

export function clearSession() {
  [
    "userId",
    "id",
    "fullName",
    "first_name",
    "last_name",
    "email",
    "token",
    "user",
  ].forEach((k) => localStorage.removeItem(k));
  window.dispatchEvent(new StorageEvent("storage", { key: "userId", newValue: null }));
}

/* -------------------------------------------------------
 * API – UserDevice’lar (Scenes için hafif liste)
 * -----------------------------------------------------*/
export async function getMyUserDevicesLight() {
  const userId = getCurrentUserId();
  if (!userId) return [];
  const list = await http(`/user-devices/mine/minimal?userId=${encodeURIComponent(userId)}`);
  const arr = Array.isArray(list) ? list : [];
  return arr
    .filter((x) => typeof x?.id === "number" || typeof x?.id === "string")
    .map((x) => ({
      id: typeof x.id === "string" ? Number(x.id) : x.id,
      label: x?.label ?? "",
    }));
}

/* -------------------------------------------------------
 * API – Scenes
 * -----------------------------------------------------*/
export async function getScenes() {
  const userId = getCurrentUserId();
  if (!userId) return [];
  return http(`/scenes?userId=${encodeURIComponent(userId)}`);
}

export async function createScene(payload) {
  const userId = getCurrentUserId();
  if (!userId) throw new Error("Kullanıcı oturumu bulunamadı.");
  const body = {
    userId,
    name: payload?.name || "",
    enabled: payload?.enabled ?? true,
    commands: Array.isArray(payload?.commands) ? payload.commands : [],
  };
  return http("/scenes", { method: "POST", body: JSON.stringify(body) });
}

export async function updateScene(id, payload) {
  const body = {
    name: payload?.name ?? undefined,
    enabled: payload?.enabled ?? undefined,
    commands: Array.isArray(payload?.commands) ? payload?.commands : undefined,
  };
  return http(`/scenes/${encodeURIComponent(id)}`, {
    method: "PUT",
    body: JSON.stringify(body),
  });
}

export async function deleteScene(id) {
  return http(`/scenes/${encodeURIComponent(id)}`, { method: "DELETE" });
}

export async function runScene(id) {
  return http(`/scenes/${encodeURIComponent(id)}/run`, { method: "POST" });
}

export async function toggleScene(id, enabled) {
  const e = enabled ? "true" : "false";
  return http(`/scenes/${encodeURIComponent(id)}/toggle?enabled=${e}`, { method: "POST" });
}
