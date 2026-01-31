"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { getToken } from "@/lib/auth";

type Uni = {
  id: string;
  name: string;
  country: string;
  field: string;
  yearlyCostUsd: number;
  bucket: string;
  acceptanceChance: string;
  risk: string;
  whyFit: string[];
  risks: string[];
};

export default function UniversitiesPage() {
  const router = useRouter();

  const [unis, setUnis] = useState<Uni[]>([]);
  const [my, setMy] = useState<any[]>([]);
  const [tab, setTab] = useState<"Dream" | "Target" | "Safe">("Target");

  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  async function load() {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    setLoading(true);
    setError(null);

    try {
      const discover = await apiFetch<Uni[]>("/universities/discover", {}, token);
      const mine = await apiFetch<any[]>("/universities/my", {}, token);
      setUnis(discover);
      setMy(mine);
      console.log(
  "BUCKETS:",
  discover.map((u) => u.bucket)
);
    } catch (e: any) {
      setError(e.message || "Failed to load universities");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    load();
  }, []);

  // map universityId → shortlist row
  const myMap = useMemo(() => {
    const m = new Map<string, any>();
    my.forEach((x) => {
      const uid = x.university?.id || x.universityId;
      if (uid) m.set(uid, x);
    });
    return m;
  }, [my]);

  // filter by Dream / Target / Safe (normalized)
  const filtered = useMemo(() => {
    return unis.filter(
      (u) => u.bucket?.toLowerCase() === tab.toLowerCase()
    );
  }, [unis, tab]);

  async function shortlistUni(id: string) {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    setBusyId(id);
    try {
      await apiFetch(`/universities/${id}/shortlist`, { method: "POST" }, token);
      await load();
    } catch (e: any) {
      setError(e.message || "Shortlist failed");
    } finally {
      setBusyId(null);
    }
  }

  async function lockUni(id: string) {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    const ok = confirm(
      "Locking a university is a commitment step and unlocks application guidance.\n\nContinue?"
    );
    if (!ok) return;

    setBusyId(id);
    try {
      await apiFetch(`/universities/${id}/lock`, { method: "POST" }, token);
      await load();
    } catch (e: any) {
      setError(e.message || "Lock failed");
    } finally {
      setBusyId(null);
    }
  }

  async function unlockUni(id: string) {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    const ok = confirm(
      "Unlocking removes commitment and may reduce focus.\n\nContinue?"
    );
    if (!ok) return;

    setBusyId(id);
    try {
      await apiFetch(`/universities/${id}/unlock`, { method: "POST" }, token);
      await load();
    } catch (e: any) {
      setError(e.message || "Unlock failed");
    } finally {
      setBusyId(null);
    }
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center">
        <p className="text-zinc-400">Loading universities…</p>
      </main>
    );
  }

  return (
    <main className="min-h-screen px-6 py-10">
      <div className="max-w-6xl mx-auto space-y-6">
        {/* Header */}
        <header className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold">University Discovery</h1>
            <p className="text-sm text-zinc-400">
              AI recommendations → shortlist → lock
            </p>
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

        {/* Tabs */}
        <div className="flex gap-2">
          {(["Dream", "Target", "Safe"] as const).map((t) => (
            <button
              key={t}
              onClick={() => setTab(t)}
              className={`px-4 py-2 rounded-full text-sm font-medium transition ${
                tab === t
                  ? "bg-indigo-600 text-white shadow"
                  : "bg-white/5 border border-white/10 text-zinc-300 hover:bg-white/10"
              }`}
            >
              {t}
            </button>
          ))}
        </div>

        {/* Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
          {filtered.map((u) => {
            const row = myMap.get(u.id);
            const rawStatus = row?.status as string | undefined;
            const status = rawStatus?.toLowerCase(); // shortlisted | locked

            return (
              <div
                key={u.id}
                className={`glass-card min-h-[320px] flex flex-col space-y-3 transition ${
                  status === "locked"
                    ? "border-indigo-500/50 bg-indigo-500/10"
                    : "hover:border-indigo-400/30"
                }`}
              >
                {/* Top */}
                <div className="flex items-start justify-between gap-3">
                  <div>
                    <p className="font-semibold text-lg">{u.name}</p>
                    <p className="text-sm text-zinc-400">
                      {u.country} • {u.field}
                    </p>
                  </div>

                  {/* State badge */}
                  <span
                    className={`text-xs px-2 py-1 rounded-full border ${
                      status === "locked"
                        ? "border-indigo-400/60 text-indigo-300"
                        : status === "shortlisted"
                        ? "border-emerald-400/60 text-emerald-300"
                        : "border-white/10 text-zinc-400"
                    }`}
                  >
                    {status === "locked"
                      ? "LOCKED"
                      : status === "shortlisted"
                      ? "SHORTLISTED"
                      : "RECOMMENDED"}
                  </span>
                </div>

                {/* Metrics */}
                <div className="text-sm text-zinc-300 space-y-1">
                  <p>
                    Cost/year: <b>${u.yearlyCostUsd}</b>
                  </p>
                  <p>
                    Acceptance: <b>{u.acceptanceChance}</b> • Risk:{" "}
                    <b>{u.risk}</b>
                  </p>
                </div>

                {/* Why fit */}
                <div className="text-sm">
                  <p className="font-medium mb-1">Why fit</p>
                  <ul className="list-disc ml-5 text-zinc-400">
                    {(u.whyFit || []).slice(0, 2).map((x, i) => (
                      <li key={i}>{x}</li>
                    ))}
                  </ul>
                </div>

                {/* Risks */}
                <div className="text-sm">
                  <p className="font-medium mb-1">Risks</p>
                  <ul className="list-disc ml-5 text-zinc-400">
                    {(u.risks || []).slice(0, 2).map((x, i) => (
                      <li key={i}>{x}</li>
                    ))}
                  </ul>
                </div>

                {/* Actions (pushed to bottom) */}
                <div className="mt-auto flex gap-2 pt-4">
                  {!status && (
                    <button
                      onClick={() => shortlistUni(u.id)}
                      disabled={busyId === u.id}
                      className="flex-1 px-3 py-2 rounded-lg bg-white/5 border border-white/10 hover:bg-white/10 transition disabled:opacity-50"
                    >
                      Shortlist
                    </button>
                  )}

                  {status === "shortlisted" && (
                    <button
                      onClick={() => lockUni(u.id)}
                      disabled={busyId === u.id}
                      className="flex-1 px-3 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition disabled:opacity-50"
                    >
                      Lock
                    </button>
                  )}

                  {status === "locked" && (
                    <button
                      onClick={() => unlockUni(u.id)}
                      disabled={busyId === u.id}
                      className="flex-1 px-3 py-2 rounded-lg border border-red-400/60 text-red-400 hover:bg-red-500/10 transition disabled:opacity-50"
                    >
                      Unlock
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>
    </main>
  );
}
