const API_BASE = "/api";

async function http(method, url, body) {
  const res = await fetch(`${API_BASE}${url}`, {
    method,
    headers: { "Content-Type": "application/json" },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!res.ok) throw new Error(`${method} ${url} failed: ${res.status}`);
  return res.status === 204 ? null : res.json();
}

export const listScenes = (userId) => http("GET", `/scenes?userId=${userId}`);
export const createScene = (payload) => http("POST", "/scenes", payload);
export const updateScene = (id, payload) => http("PUT", `/scenes/${id}`, payload);
export const deleteScene = (id) => http("DELETE", `/scenes/${id}`);
export const runScene = (id) => http("POST", `/scenes/${id}/run`);
export const toggleScene = (id, enabled) => http("POST", `/scenes/${id}/toggle?enabled=${enabled}`);
