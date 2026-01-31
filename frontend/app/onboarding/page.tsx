import { Suspense } from "react";
import OnboardingClient from "./onboarding-client";

export default function Page() {
  return (
    <Suspense fallback={<p className="p-6 text-zinc-400">Loading…</p>}>
      <OnboardingClient />
    </Suspense>
  );
}
