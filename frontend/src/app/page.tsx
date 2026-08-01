import Link from "next/link";
import { ArrowRight, BarChart3, Library, Search } from "lucide-react";

const sections = [
  {
    href: "/search",
    title: "Search Catalog",
    description:
      "Start exploring artists, albums, tracks, and metadata from your growing catalog.",
    icon: Search,
  },
  {
    href: "/library",
    title: "My Library",
    description:
      "Keep saved releases, curated collections, and listening priorities close at hand.",
    icon: Library,
  },
  {
    href: "/analytics",
    title: "Analytics & AI Insights",
    description:
      "Track performance signals and surface the next questions worth investigating.",
    icon: BarChart3,
  },
];

export default function Home() {
  return (
    <section className="mx-auto flex w-full max-w-7xl flex-1 flex-col px-6 py-16 lg:px-8">
      <div className="max-w-3xl">
        <p className="text-sm font-semibold tracking-[0.3em] text-violet-700 uppercase dark:text-violet-300">
          Music Catalog Platform
        </p>
        <h1 className="mt-6 text-5xl font-semibold tracking-tight text-zinc-950 dark:text-white sm:text-6xl">
          A clean workspace for catalog search, library management, and insight generation.
        </h1>
        <p className="mt-6 max-w-2xl text-lg leading-8 text-zinc-600 dark:text-zinc-300">
          This starter frontend is ready for the Spring Boot backend you just scaffolded. The
          navigation stays shared across routes, and each section has a placeholder page ready for
          real data.
        </p>
      </div>

      <div className="mt-12 grid gap-6 lg:grid-cols-3">
        {sections.map(({ href, title, description, icon: Icon }) => (
          <Link
            key={href}
            href={href}
            className="group rounded-[2rem] border border-black/8 bg-white/80 p-6 shadow-lg shadow-black/5 transition hover:-translate-y-1 hover:shadow-xl dark:border-white/10 dark:bg-zinc-900/75 dark:shadow-black/20"
          >
            <div className="flex h-12 w-12 items-center justify-center rounded-2xl bg-violet-600/12 text-violet-700 dark:bg-violet-500/15 dark:text-violet-300">
              <Icon className="h-6 w-6" />
            </div>
            <h2 className="mt-6 text-2xl font-semibold text-zinc-950 dark:text-white">
              {title}
            </h2>
            <p className="mt-3 text-base leading-7 text-zinc-600 dark:text-zinc-300">
              {description}
            </p>
            <span className="mt-6 inline-flex items-center gap-2 text-sm font-medium text-violet-700 transition group-hover:gap-3 dark:text-violet-300">
              Open section
              <ArrowRight className="h-4 w-4" />
            </span>
          </Link>
        ))}
      </div>
    </section>
  );
}
