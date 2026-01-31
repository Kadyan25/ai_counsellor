"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { getToken } from "@/lib/auth";

type Task = {
  id: string;
  title: string;
  status: "pending" | "done";
  source: string;
};

export default function TasksPage() {
  const router = useRouter();
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    setLoading(true);
    try {
      const d = await apiFetch<any>("/dashboard", {}, token);
      setTasks(d.tasks || []);
    } catch (e: any) {
      setError(e.message || "Failed to load tasks");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  async function markDone(taskId: string) {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    setBusyId(taskId);
    try {
      await apiFetch(`/tasks/${taskId}/done`, { method: "PATCH" }, token);
      await load();
    } catch (e: any) {
      setError(e.message || "Failed to update task");
    } finally {
      setBusyId(null);
    }
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center">
        <p className="text-zinc-400">Loading tasks…</p>
      </main>
    );
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <div className="max-w-5xl mx-auto space-y-6">
        <header className="flex justify-between">
          <div>
            <h1 className="text-2xl font-semibold">Application Guidance</h1>
            <p className="text-sm text-zinc-400">AI-generated to-dos</p>
          </div>
          <button
            onClick={() => router.push("/dashboard")}
            className="text-sm text-zinc-400 hover:text-white"
          >
            Back
          </button>
        </header>

        {error && (
          <div className="glass-card border-red-500/30 text-red-400">
            {error}
          </div>
        )}

        <section className="glass-card space-y-3">
          {tasks.length ? (
            tasks.map((t) => (
              <div
                key={t.id}
                className={`border border-white/10 rounded-xl p-4 flex items-center justify-between ${
                  t.status === "done" ? "opacity-70" : ""
                }`}
              >
                <div>
                  <p className="font-medium">{t.title}</p>
                  <p className="text-xs text-zinc-400">
                    Status: {t.status} • Source: {t.source}
                  </p>
                </div>

                {t.status === "pending" ? (
                  <button
                    onClick={() => markDone(t.id)}
                    disabled={busyId === t.id}
                    className="px-4 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50"
                  >
                    {busyId === t.id ? "…" : "Mark Done"}
                  </button>
                ) : (
                  <span className="text-xs px-3 py-1 rounded-full border border-emerald-400/40 text-emerald-300">
                    ✓ Completed
                  </span>
                )}
              </div>
            ))
          ) : (
            <p className="text-sm text-zinc-400">No tasks yet</p>
          )}
        </section>
      </div>
    </main>
  );
}
