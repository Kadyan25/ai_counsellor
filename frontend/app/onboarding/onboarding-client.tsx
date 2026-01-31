"use client";
export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import { apiFetch } from "@/lib/api";
import { getToken } from "@/lib/auth";

const countriesList = ["USA", "Canada", "Germany", "UK", "Australia"];

const input =
  "w-full bg-zinc-900 text-white border border-zinc-700 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500";

const select =
  "w-full bg-zinc-900 text-white border border-zinc-700 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-indigo-500";

export default function OnboardingClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const isEdit = searchParams.get("edit") === "true";

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // Academic
  const [educationLevel, setEducationLevel] = useState("Bachelors");
  const [major, setMajor] = useState("");
  const [gradYear, setGradYear] = useState(2024);
  const [gpa, setGpa] = useState(3.0);

  // Study Goal
  const [intendedDegree, setIntendedDegree] = useState("Masters");
  const [fieldOfStudy, setFieldOfStudy] = useState("");
  const [intakeYear, setIntakeYear] = useState(2026);

  // Budget
  const [preferredCountries, setPreferredCountries] = useState<string[]>(["USA"]);
  const [budgetPerYear, setBudgetPerYear] = useState(40000);
  const [fundingPlan, setFundingPlan] = useState("Loan");

  // Readiness
  const [ieltsStatus, setIeltsStatus] = useState("Not started");
  const [greStatus, setGreStatus] = useState("Not started");
  const [sopStatus, setSopStatus] = useState("Not started");

  useEffect(() => {
    const token = getToken();
    if (!token) return router.push("/auth/login");

    apiFetch<any>("/profile", {}, token)
      .then((p) => {
        setEducationLevel(p.educationLevel || "Bachelors");
        setMajor(p.major || "");
        setGradYear(p.gradYear || 2024);
        setGpa(p.gpa || 3.0);

        setIntendedDegree(p.intendedDegree || "Masters");
        setFieldOfStudy(p.fieldOfStudy || "");
        setIntakeYear(p.intakeYear || 2026);

        setPreferredCountries(
          p.preferredCountries?.length ? p.preferredCountries : ["USA"]
        );
        setBudgetPerYear(p.budgetPerYear || 40000);
        setFundingPlan(p.fundingPlan || "Loan");

        setIeltsStatus(p.ieltsStatus || "Not started");
        setGreStatus(p.greStatus || "Not started");
        setSopStatus(p.sopStatus || "Not started");

        if (p.onboardingCompleted && !isEdit) {
          router.push("/dashboard");
        }
      })
      .finally(() => setLoading(false));
  }, [router, isEdit]);

  async function onSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError(null);

    const token = getToken();
    if (!token) return;

    setSaving(true);
    try {
      await apiFetch(
        "/profile",
        {
          method: "PUT",
          body: JSON.stringify({
            educationLevel,
            major,
            gradYear,
            gpa,
            intendedDegree,
            fieldOfStudy,
            intakeYear,
            preferredCountries,
            budgetPerYear,
            fundingPlan,
            ieltsStatus,
            greStatus,
            sopStatus,
          }),
        },
        token
      );

      if (!isEdit) {
        await apiFetch("/profile/complete", { method: "POST" }, token);
      }

      router.push("/dashboard");
    } catch (e: any) {
      setError(e.message || "Onboarding failed");
    } finally {
      setSaving(false);
    }
  }

  function toggleCountry(c: string) {
    setPreferredCountries((prev) =>
      prev.includes(c) ? prev.filter((x) => x !== c) : [...prev, c]
    );
  }

  if (loading) return <p className="p-6 text-zinc-400">Loading…</p>;

  return (
    <main className="min-h-screen bg-black text-white p-6">
      <form onSubmit={onSubmit} className="max-w-3xl mx-auto glass-card space-y-6">
        <h1 className="text-2xl font-semibold">
          {isEdit ? "Edit Profile" : "Onboarding"}
        </h1>

        {error && <p className="text-red-400">{error}</p>}

        {/* Academic */}
        <section className="space-y-3">
          <h2 className="font-medium">A) Academic Background</h2>
          <div className="grid sm:grid-cols-2 gap-3">
            <select className={select} value={educationLevel} onChange={(e) => setEducationLevel(e.target.value)}>
              <option>Bachelors</option>
              <option>Masters</option>
              <option>Diploma</option>
            </select>

            <input className={input} placeholder="Major" value={major} onChange={(e) => setMajor(e.target.value)} />
            <input className={input} type="number" placeholder="Graduation Year" value={gradYear} onChange={(e) => setGradYear(+e.target.value)} />
            <input className={input} type="number" step="0.1" placeholder="GPA" value={gpa} onChange={(e) => setGpa(+e.target.value)} />
          </div>
        </section>

        {/* Study Goal */}
        <section className="space-y-3">
          <h2 className="font-medium">B) Study Goal</h2>
          <div className="grid sm:grid-cols-2 gap-3">
            <select className={select} value={intendedDegree} onChange={(e) => setIntendedDegree(e.target.value)}>
              <option>Masters</option>
              <option>Bachelors</option>
              <option>MBA</option>
              <option>PhD</option>
            </select>

            <input className={input} placeholder="Field of Study" value={fieldOfStudy} onChange={(e) => setFieldOfStudy(e.target.value)} />
            <input className={input} type="number" placeholder="Target Intake Year" value={intakeYear} onChange={(e) => setIntakeYear(+e.target.value)} />
          </div>

          <div className="flex flex-wrap gap-2">
            {countriesList.map((c) => (
              <button
                key={c}
                type="button"
                onClick={() => toggleCountry(c)}
                className={`px-3 py-1 rounded-full border ${
                  preferredCountries.includes(c)
                    ? "bg-indigo-600 border-indigo-500"
                    : "border-white/20 text-zinc-400"
                }`}
              >
                {c}
              </button>
            ))}
          </div>
        </section>

        {/* Budget */}
        <section className="space-y-3">
          <h2 className="font-medium">C) Budget</h2>
          <div className="grid sm:grid-cols-2 gap-3">
            <input className={input} type="number" placeholder="Budget per year (USD)" value={budgetPerYear} onChange={(e) => setBudgetPerYear(+e.target.value)} />
            <select className={select} value={fundingPlan} onChange={(e) => setFundingPlan(e.target.value)}>
              <option>Self</option>
              <option>Loan</option>
              <option>Scholarship</option>
            </select>
          </div>
        </section>

        {/* Readiness */}
        <section className="space-y-3">
          <h2 className="font-medium">D) Exams & Readiness</h2>
          <div className="grid sm:grid-cols-3 gap-3">
            <div>
              <p className="text-xs text-zinc-400 mb-1">IELTS</p>
              <select className={select} value={ieltsStatus} onChange={(e) => setIeltsStatus(e.target.value)}>
                <option>Not started</option>
                <option>In progress</option>
                <option>Completed</option>
              </select>
            </div>

            <div>
              <p className="text-xs text-zinc-400 mb-1">GRE</p>
              <select className={select} value={greStatus} onChange={(e) => setGreStatus(e.target.value)}>
                <option>Not started</option>
                <option>In progress</option>
                <option>Completed</option>
              </select>
            </div>

            <div>
              <p className="text-xs text-zinc-400 mb-1">SOP</p>
              <select className={select} value={sopStatus} onChange={(e) => setSopStatus(e.target.value)}>
                <option>Not started</option>
                <option>Draft</option>
                <option>Ready</option>
              </select>
            </div>
          </div>
        </section>

        <button
          disabled={saving}
          className="w-full py-2 rounded-lg bg-indigo-600 hover:bg-indigo-700 disabled:opacity-50"
        >
          {saving ? "Saving…" : isEdit ? "Save Changes" : "Complete Onboarding"}
        </button>
      </form>
    </main>
  );
}
