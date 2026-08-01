import type { LucideIcon } from "lucide-react";
import Link from "next/link";

type PlaceholderPageProps = {
  eyebrow: string;
  title: string;
  description: string;
  icon: LucideIcon;
};

export function PlaceholderPage({
  eyebrow,
  title,
  description,
  icon: Icon,
}: PlaceholderPageProps) {
  return (
    <section className="mx-auto flex w-full max-w-5xl flex-1 flex-col justify-center px-6 py-16 lg:px-8">
      <div className="max-w-3xl rounded-[2rem] border border-black/8 bg-white/80 p-8 shadow-xl shadow-black/5 backdrop-blur dark:border-white/10 dark:bg-zinc-900/75 dark:shadow-black/20 sm:p-10">
        <div className="mb-6 inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-violet-600/12 text-violet-700 dark:bg-violet-500/15 dark:text-violet-300">
          <Icon className="h-7 w-7" />
        </div>
        <p className="text-sm font-semibold tracking-[0.3em] text-violet-700 uppercase dark:text-violet-300">
          {eyebrow}
        </p>
        <h1 className="mt-4 text-4xl font-semibold tracking-tight text-zinc-950 dark:text-white sm:text-5xl">
          {title}
        </h1>
        <p className="mt-4 max-w-2xl text-lg leading-8 text-zinc-600 dark:text-zinc-300">
          {description}
        </p>
        <div className="mt-8 flex flex-wrap gap-3 text-sm">
          <Link
            href="/"
            className="rounded-full bg-zinc-950 px-5 py-3 font-medium text-white transition hover:bg-zinc-800 dark:bg-white dark:text-zinc-950 dark:hover:bg-zinc-200"
          >
            Back to overview
          </Link>
          <span className="rounded-full border border-black/8 px-5 py-3 text-zinc-500 dark:border-white/10 dark:text-zinc-400">
            Placeholder page ready for real data
          </span>
        </div>
      </div>
    </section>
  );
}
