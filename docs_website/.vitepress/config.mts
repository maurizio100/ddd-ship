import { fileURLToPath } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'
import { withMermaid } from 'vitepress-plugin-mermaid'
import type { DefaultTheme } from 'vitepress'

// Base path the site is served under. '/ddd-ship/' suits a GitHub Pages project
// site (https://<owner>.github.io/ddd-ship/). Set to '/' if you serve at a domain root.
const BASE = '/ddd-ship/'
// GitHub repo the "Edit this page" / social links point at. Update the owner.
const REPO_URL = 'https://github.com/maurizio100/ddd-ship'
// Content lives in the sibling ../docs; this app (VitePress root) lives in docs_website/.
const docsRoot = fileURLToPath(new URL('../../docs/', import.meta.url))

/* ------------------------------------------------------------------ *
 * README.md is each folder's page. Every docs folder is entered through
 * its README.md — the agents read it as the folder's index, and a forge
 * shows it when you browse the folder. VitePress wants index.md for a
 * folder's own URL, so README.md is served as index.md at build time
 * (docs/arc42/README.md -> /arc42/); the file is never renamed. The one
 * index.md on disk is docs/index.md, the portal's home page.
 * ------------------------------------------------------------------ */

const README_RE = /\/README\.md$/i

function rewriteReadme(id: string): string {
  return id.replace(README_RE, '/index.md')
}

/* ------------------------------------------------------------------ *
 * Sidebar generation — scans the docs tree so every new markdown file
 * shows up automatically. Group titles come from folder names; page
 * titles come from each file's first H1.
 * ------------------------------------------------------------------ */

function cleanTitle(raw: string): string {
  return raw
    .replace(/`/g, '')
    .replace(/\[([^\]]+)\]\([^)]*\)/g, '$1')
    .replace(/[*_]/g, '')
    .trim()
}

function pageTitle(absFile: string): string {
  try {
    const src = fs.readFileSync(absFile, 'utf-8')
    const m = src.match(/^#\s+(.+)$/m)
    if (m) return cleanTitle(m[1])
  } catch {
    /* fall through to filename */
  }
  return path
    .basename(absFile, '.md')
    .replace(/[-_]/g, ' ')
    .replace(/\b\w/g, (c) => c.toUpperCase())
}

function prettyDir(name: string): string {
  return name
    .replace(/[-_]/g, ' ')
    .replace(/\b\w/g, (c) => c.toUpperCase())
}

const collator = new Intl.Collator('en', { numeric: true, sensitivity: 'base' })

function buildItems(absDir: string, urlPrefix: string): DefaultTheme.SidebarItem[] {
  const entries = fs.readdirSync(absDir, { withFileTypes: true })
  const hasReadme = entries.some((e) => e.isFile() && e.name === 'README.md')

  const files = entries
    .filter((e) => e.isFile() && e.name.endsWith('.md') && e.name !== 'README.md')
    .map((e) => e.name)
    .sort((a, b) => collator.compare(a, b)) // natural (numeric-aware) order

  const dirs = entries
    .filter((e) => e.isDirectory() && !e.name.startsWith('.') && !e.name.startsWith('_'))
    .map((e) => e.name)
    .sort((a, b) => collator.compare(a, b))

  const items: DefaultTheme.SidebarItem[] = []

  // A folder's README.md becomes its landing item. Without this, a section
  // holding nothing but its README yields no items, so section() drops it and
  // the nav link falls back to '/' — which is what an empty, freshly
  // scaffolded section looks like.
  if (hasReadme) {
    items.push({
      text: pageTitle(path.join(absDir, 'README.md')),
      link: urlPrefix,
    })
  }

  for (const f of files) {
    items.push({
      text: pageTitle(path.join(absDir, f)),
      link: urlPrefix + f.replace(/\.md$/, ''),
    })
  }

  for (const d of dirs) {
    const childItems = buildItems(path.join(absDir, d), `${urlPrefix}${d}/`)
    if (childItems.length) {
      items.push({ text: prettyDir(d), collapsed: true, items: childItems })
    }
  }

  return items
}

function section(dir: string, heading: string): DefaultTheme.SidebarItem[] {
  const abs = path.join(docsRoot, dir)
  if (!fs.existsSync(abs)) return []
  const items = buildItems(abs, `/${dir}/`)
  if (!items.length) return []
  return [{ text: heading, items }]
}

/* First concrete page in a section — used as the nav-bar landing link. */
function firstLink(items: DefaultTheme.SidebarItem[]): string {
  for (const it of items) {
    if (it.link) return it.link
    if (it.items) {
      const nested = firstLink(it.items)
      if (nested) return nested
    }
  }
  return '/'
}

const arc42 = section('arc42', 'Architecture (arc42)')
const adr = section('adr', 'Decisions (ADR)')
const domain = section('domain', 'Domain')
const services = section('services', 'Services')

export default withMermaid({
  title: 'Hexagonship Docs',
  description: 'Pet project for trying out Hexagonal Architecture, Domain-Driven Design and the Transactional Outbox pattern with Kafka Connect/Debezium',
  // Markdown content is the sibling ../docs dir; only the site tooling lives here.
  srcDir: docsRoot,
  rewrites: rewriteReadme,
  base: BASE,
  lang: 'en',
  cleanUrls: true,
  lastUpdated: true,
  ignoreDeadLinks: true,

  // Relative links are written against the files on disk (`../services/api/README.md`),
  // which is what a forge and the docs checker resolve. With the rewrite above the
  // page lives at the folder's URL, so point those links at the folder's index.md.
  markdown: {
    config(md) {
      md.core.ruler.push('readme-links', (state) => {
        for (const block of state.tokens) {
          for (const tok of block.children ?? []) {
            if (tok.type !== 'link_open') continue
            const href = tok.attrGet('href')
            if (!href || /^[a-z]+:|^\/|^#/i.test(href)) continue
            const [file, hash] = href.split('#', 2)
            if (!/(^|\/)README\.md$/i.test(file)) continue
            tok.attrSet('href', file.replace(/README\.md$/i, 'index.md') + (hash ? `#${hash}` : ''))
          }
        }
      })
    },
  },

  // Mermaid pulls in CommonJS leaf deps (fastdom, strictdom, …). Force Vite to
  // pre-bundle the whole cluster so its ESM `import x from 'cjs-pkg'` calls get
  // a synthesized default export — otherwise dev throws
  // "does not provide an export named 'default'" and hydration halts.
  vite: {
    optimizeDeps: {
      include: ['mermaid', 'fastdom', 'strictdom'],
    },
    ssr: {
      noExternal: ['mermaid'],
    },
  },

  head: [
    ['meta', { name: 'theme-color', content: '#00417b' }],
  ],

  themeConfig: {
    siteTitle: 'Hexagonship Docs',

    nav: [
      { text: 'Architecture', link: firstLink(arc42) },
      { text: 'Decisions', link: firstLink(adr) },
      { text: 'Domain', link: firstLink(domain) },
      { text: 'Services', link: firstLink(services) },
    ],

    // One unified sidebar shown on every page: all main sections live in the
    // left area together, rather than a separate per-section sidebar that only
    // shows the active area.
    sidebar: [...arc42, ...adr, ...domain, ...services],

    search: {
      provider: 'local',
    },

    socialLinks: [
      { icon: 'github', link: REPO_URL },
    ],

    outline: { level: [2, 3], label: 'On this page' },

    editLink: {
      pattern: `${REPO_URL}/edit/main/docs/:path`,
      text: 'Edit this page on GitHub',
    },

    lastUpdatedText: 'Last updated',

    docFooter: { prev: 'Previous', next: 'Next' },

    footer: {
      message: 'Internal documentation — Hexagonship.',
      copyright: 'Hexagonship',
    },
  },

  mermaid: {
    theme: 'default',
    themeVariables: {
      primaryColor: '#E5EEF5',
      primaryBorderColor: '#00417b',
      primaryTextColor: '#16212C',
      lineColor: '#5a5f62',
      fontFamily:
        'Inter, system-ui, -apple-system, Segoe UI, Roboto, sans-serif',
    },
  },
})
