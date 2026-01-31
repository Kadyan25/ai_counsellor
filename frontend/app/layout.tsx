import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "AI Counsellor — Guided Study Abroad Decisions",
  description:
    "A stage-based AI counsellor that guides students from profile building to university locking and application preparation.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body
        className={`${geistSans.variable} ${geistMono.variable} antialiased bg-[#07090f] text-zinc-100`}
      >
        {/* Aurora background blobs */}
        <div className="pointer-events-none fixed inset-0 -z-10">
          <div className="absolute -top-40 -left-40 h-[500px] w-[500px] rounded-full bg-indigo-600/25 blur-[120px]" />
          <div className="absolute top-1/3 -right-40 h-[600px] w-[600px] rounded-full bg-cyan-500/20 blur-[140px]" />
          <div className="absolute bottom-0 left-1/3 h-[400px] w-[400px] rounded-full bg-purple-600/20 blur-[120px]" />
        </div>

        {children}
      </body>
    </html>
  );
}
