"use client";

import { useEffect, useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { clearToken, getToken } from "@/lib/auth";

type Dashboard = {
  stage: number;
  profile: {
    onboardingCompleted: boolean;
    educationLevel?: string;
    major?: string;
    intakeYear?: number;
    preferredCountries?: string[];
    budgetPerYear?: number;
    ieltsStatus?: string;
    greStatus?: string;
    sopStatus?: string;
    gpa?: number;
  };
  tasks: any[];
  shortlist: any[];
};

export default function DashboardPage() {
  const router = useRouter();
  const [data, setData] = useState<Dashboard | null>(null);

  /* ---------------- effects ALWAYS first ---------------- */
  useEffect(() => {
    const token = getToken();
    if (!token) {
      router.push("/auth/login");
      return;
    }

    apiFetch<Dashboard>("/dashboard", {}, token)
      .then((d) => {
        setData(d);
        if (!d.profile?.onboardingCompleted) {
          router.push("/onboarding");
        }
      })
      .catch(() => {
        clearToken();
        router.push("/auth/login");
      });
  }, [router]);

  /* ---------------- hooks must NOT depend on early return ---------------- */
  const nextStep = useMemo(() => {
    if (!data) return null;

    const stage = data.stage;

    if (stage <= 1) {
      return {
        title: "Discover universities",
        desc: "Get AI-recommended Dream / Target / Safe universities",
        btn: "Go to Universities",
        route: "/universities",
      };
    }

    if (stage === 2 || stage === 3) {
      return {
        title: "Lock at least one university",
        desc: "This commitment step unlocks application guidance",
        btn: "Go to Universities",
        route: "/universities",
      };
    }

    return {
      title: "Prepare applications",
      desc: "Follow AI-generated tasks and timelines",
      btn: "Go to Tasks",
      route: "/tasks",
    };
  }, [data]);

  /* ---------------- NOW it is safe to early-return ---------------- */
  if (!data) {
    return (
      <main className="min-h-screen flex items-center justify-center">
        <p className="text-zinc-400">Loading dashboard…</p>
      </main>
    );
  }

  const profile = data.profile;
  const stage = data.stage;

  function stageName(n: number) {
    if (n === 1) return "Building Profile";
    if (n === 2) return "Discovering Universities";
    if (n === 3) return "Finalizing Universities";
    return "Preparing Applications";
  }

  const academicsStrength =
    profile.gpa && profile.gpa >= 3.5
      ? "Strong"
      : profile.gpa && profile.gpa >= 3.0
      ? "Average"
      : "Weak";

  return (
    <main className="min-h-screen p-6">
      <div className="max-w-5xl mx-auto space-y-6">
        {/* Header */}
        <header className="flex items-center justify-between">
          <div>
            <h1 className="text-2xl font-semibold">Dashboard</h1>
            <p className="text-sm text-zinc-500">
              Your guided study-abroad control center
            </p>
          </div>

          <button
            onClick={() => {
              clearToken();
              router.push("/auth/login");
            }}
            className="px-4 py-2 rounded-lg border hover:bg-zinc-100 transition"
          >
            Logout
          </button>
        </header>

        {/* Stage Tracker */}
        <section className="border rounded-2xl p-5 space-y-3">
          <div className="flex items-center justify-between">
            <p className="font-semibold">
              Current Stage:{" "}
              <span className="text-blue-600">{stageName(stage)}</span>
            </p>
            <span className="text-xs border rounded-full px-2 py-1">
              Stage {stage}
            </span>
          </div>

          <div className="grid grid-cols-4 gap-2 text-xs">
            {[1, 2, 3, 4].map((n) => {
              const completed = n < stage;
              const current = n === stage;

              return (
                <div
                  key={n}
                  className={`rounded-xl border p-2 text-center transition ${
                    current
                      ? "bg-blue-600 text-white border-blue-600"
                      : completed
                      ? "bg-blue-50 text-blue-700 border-blue-200"
                      : "opacity-50"
                  }`}
                >
                  {n}. {stageName(n)}
                </div>
              );
            })}
          </div>
        </section>

        {/* Next Step */}
        {nextStep && (
          <section className="border rounded-2xl p-5 flex items-center justify-between">
            <div>
              <p className="font-semibold">{nextStep.title}</p>
              <p className="text-sm text-zinc-500">{nextStep.desc}</p>
            </div>

            <button
              onClick={() => router.push(nextStep.route)}
              className="px-5 py-2 rounded-xl bg-blue-600 text-white hover:bg-blue-700 transition"
            >
              {nextStep.btn}
            </button>
          </section>
        )}

        {/* Profile */}
        <section className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="border rounded-2xl p-5 space-y-2">
            <h2 className="font-semibold">Profile Summary</h2>
            <p className="text-sm text-zinc-500">
              {profile.educationLevel || "-"} • {profile.major || "-"} • Intake{" "}
              {profile.intakeYear || "-"}
            </p>
            <p className="text-sm">
              Countries:{" "}
              <b>
                {profile.preferredCountries?.length
                  ? profile.preferredCountries.join(", ")
                  : "-"}
              </b>
            </p>
            <p className="text-sm">
              Budget/year:{" "}
              <b>{profile.budgetPerYear ? `$${profile.budgetPerYear}` : "-"}</b>
            </p>

            <button
              onClick={() => router.push("/onboarding?edit=true")}
              className="mt-2 px-4 py-2 rounded-lg border hover:bg-zinc-100 transition"
            >
              Edit Profile
            </button>
          </div>

          <div className="border rounded-2xl p-5 space-y-3">
            <h2 className="font-semibold">Profile Strength</h2>

            <div className="grid grid-cols-3 gap-2 text-sm">
              <Badge label="Academics" value={academicsStrength} />
              <Badge label="Exams" value={profile.ieltsStatus || "Not started"} />
              <Badge label="SOP" value={profile.sopStatus || "Not started"} />
            </div>

            <button
              onClick={() => router.push("/counsellor")}
              className="px-4 py-2 rounded-lg bg-blue-600 text-white hover:bg-blue-700 transition"
            >
              Ask AI Counsellor
            </button>
          </div>
        </section>
      </div>
    </main>
  );
}

function Badge({ label, value }: { label: string; value: string }) {
  return (
    <div className="border rounded-xl p-3">
      <p className="text-xs text-zinc-500">{label}</p>
      <p className="font-semibold">{value}</p>
    </div>
  );
}
