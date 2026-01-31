"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { getToken } from "@/lib/auth";

type AiMessage = { role: "user" | "assistant"; content: string };

type AiAction = {
  type: string;
  args?: any;
  result?: any;
  error?: string;
};

type AiSnapshot = {
  stage: number;
  profile: any;
  tasks: any[];
  shortlist: any[];
};

type AiChatResponse = {
  reply: string;
  actions: AiAction[];
  snapshot: AiSnapshot;
};

export default function CounsellorPage() {
  const router = useRouter();

  const [messages, setMessages] = useState<AiMessage[]>([]);
  const [snapshot, setSnapshot] = useState<AiSnapshot | null>(null);
  const [actions, setActions] = useState<AiAction[]>([]);
  const [input, setInput] = useState("");
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  function stageName(n: number) {
    if (n === 1) return "Building Profile";
    if (n === 2) return "Discovering Universities";
    if (n === 3) return "Finalizing Universities";
    return "Preparing Applications";
  }

  const lockedUni = useMemo(() => {
    return snapshot?.shortlist?.find((x) => x.status === "locked")?.university;
  }, [snapshot]);

  async function loadDashboardState() {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    try {
      const d = await apiFetch<any>("/dashboard", {}, token);

      if (!d.profile?.onboardingCompleted) {
        router.push("/onboarding");
        return;
      }

      setSnapshot({
        stage: d.stage,
        profile: d.profile,
        tasks: d.tasks,
        shortlist: d.shortlist,
      });
    } catch {
      router.push("/auth/login");
    }
  }
async function loadHistory() {
  const token = getToken();
  if (!token) return;

  const history = await apiFetch<any[]>("/ai/history", {}, token);

  setMessages(
    history.map((m) => ({
      role: m.role,
      content: m.content,
    }))
  );
}

  useEffect(() => {
    loadDashboardState();
     loadHistory();
  }, []);

  async function send() {
    if (sending || !input.trim()) return;

    const token = getToken();
    if (!token) return router.push("/auth/login");

    const text = input.trim();
    setInput("");
    setSending(true);
    setError(null);

    setMessages((m) => [...m, { role: "user", content: text }]);

    try {
      const res = await apiFetch<AiChatResponse>(
        "/ai/chat",
        {
          method: "POST",
          body: JSON.stringify({ message: text }),
        },
        token
      );

      setMessages((m) => [...m, { role: "assistant", content: res.reply }]);
      setActions(res.actions || []);
      setSnapshot(res.snapshot);
    } catch (e: any) {
      setError(e.message || "AI request failed");
    } finally {
      setSending(false);
    }
  }

  function onKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  }

  return (
    <main className="min-h-screen p-6">
      <div className="max-w-6xl mx-auto h-[calc(100vh-3rem)] flex flex-col gap-4">
        {/* Header */}
        <header className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold">AI Counsellor</h1>
            <p className="text-sm text-zinc-500">
              Decision + execution system (not just chat)
            </p>
          </div>

          <button
            onClick={() => router.push("/dashboard")}
            className="px-4 py-2 rounded-lg border hover:bg-zinc-100 transition"
          >
            Back
          </button>
        </header>

        {error && (
          <div className="text-sm text-red-600 border border-red-200 bg-red-50 p-2 rounded-lg">
            {error}
          </div>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4 flex-1 min-h-0">
          {/* Chat */}
          <section className="lg:col-span-2 border rounded-2xl p-4 flex flex-col min-h-0">
            {/* System Intro (NOT part of chat messages) */}
            <div className="mb-4 mx-auto max-w-[90%] rounded-2xl bg-zinc-900/40 border border-zinc-700 px-4 py-3 text-center">
              <p className="text-xs uppercase tracking-wide text-zinc-500 mb-1">
                System
              </p>
              <p className="text-sm text-zinc-300">
                I’m your AI Counsellor. I guide you step-by-step, recommend
                universities, enforce decisions, and create tasks based on your
                current stage.
              </p>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-auto space-y-3 pr-2">
              {messages.map((m, idx) => (
                <div
                  key={idx}
                  className={`p-3 rounded-2xl text-sm leading-6 whitespace-pre-wrap break-words ${
                    m.role === "user"
                      ? "ml-auto max-w-[80%] bg-blue-600 text-white"
                      : "mr-auto max-w-[80%] bg-zinc-900/70 text-zinc-100 border border-zinc-700"
                  }`}
                >
                  {m.content}
                </div>
              ))}

              {sending && (
                <div className="mr-auto max-w-[80%] bg-zinc-900/70 text-zinc-400 border border-zinc-700 p-3 rounded-2xl text-sm">
                  Thinking…
                </div>
              )}
            </div>

            {/* Input */}
            <div className="pt-3 bg-zinc-900/40 p-2 rounded-2xl border border-zinc-700 flex gap-2">
              <textarea
                rows={2}
                value={input}
                onChange={(e) => setInput(e.target.value)}
                onKeyDown={onKeyDown}
                placeholder="Ask: recommend universities, shortlist, lock, create tasks…"
                className="flex-1 rounded-xl px-4 py-3 resize-none bg-zinc-900/60 border border-zinc-700 text-zinc-100 placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-blue-500 transition"
              />
              <button
                onClick={send}
                disabled={sending}
                className="px-5 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700 transition disabled:opacity-50"
              >
                Send
              </button>
            </div>

            <p className="text-xs text-zinc-500 mt-1">
              Press <b>Enter</b> to send • <b>Shift + Enter</b> for new line
            </p>
          </section>

          {/* Execution Panel */}
          <aside className="border rounded-2xl p-4 space-y-4">
            <div className="border rounded-xl p-3">
              <p className="text-xs text-zinc-500">Current Stage</p>
              <p className="font-semibold">
                {snapshot ? stageName(snapshot.stage) : "Loading…"}
              </p>
            </div>

            <div className="border rounded-xl p-3">
              <p className="text-xs text-zinc-500">Locked University</p>
              {lockedUni ? (
                <p className="font-semibold">{lockedUni.name}</p>
              ) : (
                <p className="text-sm text-zinc-500">
                  None locked yet (required to unlock guidance)
                </p>
              )}
            </div>

            <div className="border rounded-xl p-3 space-y-2">
              <p className="font-semibold">Actions Executed by AI</p>
              {actions.length ? (
                actions.map((a, i) => (
                  <div key={i} className="text-xs border rounded-lg p-2">
                    <p className="font-medium">{a.type}</p>
                    {a.error && (
                      <p className="text-red-600 mt-1">{a.error}</p>
                    )}
                  </div>
                ))
              ) : (
                <p className="text-sm text-zinc-500">
                  The AI will log actions here when it shortlists, locks, or
                  creates tasks.
                </p>
              )}
            </div>

            <div className="border rounded-xl p-3 space-y-2">
              <p className="font-semibold">AI To-Dos</p>

              {snapshot?.tasks?.length ? (
                snapshot.tasks.slice(0, 5).map((t: any) => (
                  <div key={t.id} className="border rounded-lg p-2">
                    <p className="text-sm font-medium">{t.title}</p>
                    <p className="text-xs text-zinc-500">{t.status}</p>
                  </div>
                ))
              ) : (
                <p className="text-sm text-zinc-500">No tasks yet.</p>
              )}

              <button
                onClick={() => router.push("/tasks")}
                className="w-full mt-2 px-3 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition"
              >
                View All Tasks
              </button>
            </div>
          </aside>
        </div>
      </div>
    </main>
  );
}
