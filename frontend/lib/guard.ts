import { apiFetch } from "@/lib/api";
import { getToken, clearToken } from "@/lib/auth";

export async function requireAuth() {
  const token = getToken();
  if (!token) throw new Error("NO_TOKEN");
  return token;
}

export async function safeFetchDashboard() {
  const token = await requireAuth();
  try {
    return await apiFetch<any>("/dashboard", {}, token);
  } catch (e) {
    // if token invalid → clear
    clearToken();
    throw e;
  }
}

export async function safeFetchProfile() {
  const token = await requireAuth();
  return apiFetch<any>("/profile", {}, token);
}
