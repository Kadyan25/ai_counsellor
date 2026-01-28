"use client";

import { useEffect, useState } from "react";
import { apiFetch } from "@/lib/api";
import { clearToken, getToken } from "@/lib/auth";
import { useRouter } from "next/navigation";

type Me = {
  id: string;
  name: string;
  email: string;
  createdAt: string;
};

export default function DashboardPage() {
  const router = useRouter();
  const [me, setMe] = useState<Me | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const token = getToken();
    if (!token) {
      router.push("/auth/login");
      return;
    }

    apiFetch<Me>("/me", {}, token)
      .then(setMe)
      .catch(() => {
        clearToken();
        setError("Session expired. Please login again.");
        router.push("/auth/login");
      });
  }, [router]);

  return (
    <main className="min-h-screen p-6">
      <div className="max-w-2xl mx-auto space-y-4">
        <h1 className="text-2xl font-semibold">Dashboard</h1>

        {error && (
          <div className="text-sm text-red-600 border border-red-200 bg-red-50 p-2 rounded">
            {error}
          </div>
        )}

        {!me ? (
          <p>Loading...</p>
        ) : (
          <div className="border rounded-xl p-4 space-y-1">
            <p className="font-medium">Logged in ✅</p>
            <p>
              <span className="font-medium">Name:</span> {me.name}
            </p>
            <p>
              <span className="font-medium">Email:</span> {me.email}
            </p>
          </div>
        )}

        <button
          onClick={() => {
            clearToken();
            router.push("/auth/login");
          }}
          className="border rounded px-4 py-2"
        >
          Logout
        </button>
      </div>
    </main>
  );
}
