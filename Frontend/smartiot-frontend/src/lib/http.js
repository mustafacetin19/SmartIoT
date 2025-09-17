// PATH: src/lib/http.js
import axios from "axios";

// Tüm istekler aynı origin'in /api altına gider.
// (Örn: 192.168.x.x:3000 → 192.168.x.x:3000/api, dev proxy 8080'e yönlendirir)
export const api = axios.create({
  baseURL: "/api",
  withCredentials: true,
});
