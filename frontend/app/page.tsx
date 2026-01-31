"use client";

import Link from "next/link";

export default function HomePage() {
  return (
    <main className="relative min-h-screen overflow-hidden bg-[#07090f] text-zinc-100">
      {/* ===== AURORA BACKGROUND ===== */}
      <div className="pointer-events-none absolute inset-0">
        <div className="absolute -top-40 -left-40 h-[500px] w-[500px] rounded-full bg-indigo-600/30 blur-[120px]" />
        <div className="absolute top-1/3 -right-40 h-[600px] w-[600px] rounded-full bg-cyan-500/20 blur-[140px]" />
        <div className="absolute bottom-0 left-1/3 h-[400px] w-[400px] rounded-full bg-purple-600/20 blur-[120px]" />
      </div>

      {/* ===== NAVBAR ===== */}
      <header className="relative z-10 max-w-7xl mx-auto px-6 py-6 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="h-11 w-11 rounded-xl bg-indigo-600 flex items-center justify-center shadow-lg">
            <span className="text-white font-bold text-lg">AI</span>
          </div>
          <div>
            <p className="text-lg font-semibold leading-none">
              AI Counsellor
            </p>
            <p className="text-xs text-zinc-400">
              Guided Study Abroad Decisions
            </p>
          </div>
        </div>

        <div className="flex gap-3">
          <Link
            href="/auth/login"
            className="px-4 py-2 rounded-lg text-sm text-zinc-300 hover:text-white transition"
          >
            Login
          </Link>
          <Link
            href="/auth/signup"
            className="px-5 py-2 rounded-lg bg-indigo-600 text-white hover:bg-indigo-700 transition shadow"
          >
            Get Started
          </Link>
        </div>
      </header>

      {/* ===== HERO ===== */}
      <section className="relative z-10 max-w-7xl mx-auto px-6 pt-24 pb-32 grid grid-cols-1 lg:grid-cols-2 gap-16 items-center">
        {/* LEFT */}
        <div className="space-y-8">
          <h1 className="text-4xl sm:text-5xl xl:text-6xl font-semibold leading-tight tracking-tight">
            Make your study-abroad decisions with{" "}
            <span className="bg-gradient-to-r from-indigo-400 to-cyan-400 bg-clip-text text-transparent">
              clarity, not confusion
            </span>
            .
          </h1>

          <p className="text-lg text-zinc-400 leading-8 max-w-xl">
            AI Counsellor is a stage-based decision system that understands
            your profile, budget, and readiness — then guides you step by
            step from onboarding to university locking and application
            preparation.
          </p>

          <div className="flex flex-col sm:flex-row gap-4">
            <Link
              href="/auth/signup"
              className="px-7 py-3 rounded-xl bg-indigo-600 text-white text-lg font-medium hover:bg-indigo-700 transition shadow-lg"
            >
              Start Your Journey
            </Link>

            <Link
              href="/auth/login"
              className="px-7 py-3 rounded-xl border border-zinc-700 text-lg text-zinc-300 hover:text-white hover:border-zinc-500 transition"
            >
              Login
            </Link>
          </div>

          <div className="flex flex-wrap gap-3 pt-2">
            <Pill text="Stage-based flow" />
            <Pill text="AI takes actions" />
            <Pill text="Decision discipline" />
            <Pill text="Application guidance" />
          </div>
        </div>

        {/* RIGHT – GLASS CARD */}
        <div className="relative">
          <div className="absolute -inset-4 rounded-3xl bg-gradient-to-r from-indigo-500/20 to-cyan-500/20 blur-3xl" />

          <div className="relative backdrop-blur-xl bg-white/5 border border-white/10 rounded-3xl p-8 shadow-xl space-y-6">
            <p className="text-sm text-zinc-400">
              Example guided journey
            </p>

            <FlowCard
              step="Stage 1"
              title="Build Profile"
              desc="Academics, goals, budget, exam readiness."
            />
            <FlowCard
              step="Stage 2"
              title="Discover Universities"
              desc="Dream / Target / Safe recommendations."
            />
            <FlowCard
              step="Stage 3"
              title="Shortlist & Lock"
              desc="AI explains risk and enforces commitment."
            />
            <FlowCard
              step="Stage 4"
              title="Application Guidance"
              desc="To-dos, documents, and timelines."
            />

            <Link
              href="/auth/signup"
              className="block w-full text-center px-6 py-3 rounded-xl bg-indigo-600 text-white font-medium hover:bg-indigo-700 transition"
            >
              Get Started
            </Link>
          </div>
        </div>
      </section>

      {/* ===== VALUE SECTION ===== */}
      <section className="relative z-10 border-t border-white/10 bg-[#07090f]">
        <div className="max-w-7xl mx-auto px-6 py-24 grid grid-cols-1 md:grid-cols-3 gap-10">
          <ValueCard
            title="Not just a chatbot"
            desc="AI Counsellor reasons, explains trade-offs, and performs real actions."
          />
          <ValueCard
            title="Decision discipline"
            desc="Strict stage-based flow prevents random browsing and confusion."
          />
          <ValueCard
            title="Built for real journeys"
            desc="Mirrors how real students make high-stakes study-abroad decisions."
          />
        </div>
      </section>

      {/* ===== FINAL CTA ===== */}
      <section className="relative z-10 bg-gradient-to-r from-indigo-600/90 to-cyan-600/90">
        <div className="max-w-7xl mx-auto px-6 py-20 text-center space-y-6">
          <h2 className="text-3xl sm:text-4xl font-semibold">
            Ready to move forward with confidence?
          </h2>
          <p className="text-lg opacity-90">
            Let the AI counsellor guide you — one stage at a time.
          </p>
          <Link
            href="/auth/signup"
            className="inline-block px-8 py-3 rounded-xl bg-black/80 text-white font-medium text-lg hover:bg-black transition"
          >
            Start Now
          </Link>
        </div>
      </section>

      {/* ===== FOOTER ===== */}
      <footer className="relative z-10 border-t border-white/10 bg-[#07090f]">
        <div className="max-w-7xl mx-auto px-6 py-8 text-xs text-zinc-500 text-center">
          AI Counsellor • Stage-based AI decision system • Hackathon prototype
        </div>
      </footer>
    </main>
  );
}

/* ---------- Components ---------- */

function Pill({ text }: { text: string }) {
  return (
    <span className="text-sm px-4 py-1.5 rounded-full border border-white/10 bg-white/5 text-zinc-300 backdrop-blur">
      {text}
    </span>
  );
}

function FlowCard({
  step,
  title,
  desc,
}: {
  step: string;
  title: string;
  desc: string;
}) {
  return (
    <div className="border border-white/10 rounded-xl p-4 hover:border-indigo-400/40 transition">
      <p className="text-xs text-zinc-500">{step}</p>
      <p className="font-semibold text-zinc-100">{title}</p>
      <p className="text-sm text-zinc-400">{desc}</p>
    </div>
  );
}

function ValueCard({
  title,
  desc,
}: {
  title: string;
  desc: string;
}) {
  return (
    <div className="border border-white/10 rounded-2xl p-8 bg-white/5 backdrop-blur hover:border-indigo-400/40 transition">
      <h3 className="font-semibold text-lg mb-2 text-zinc-100">
        {title}
      </h3>
      <p className="text-sm text-zinc-400 leading-6">{desc}</p>
    </div>
  );
}
