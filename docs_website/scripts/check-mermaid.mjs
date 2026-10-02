/**
 * Parses every ```mermaid block under a content directory and reports the ones
 * Mermaid cannot parse.
 *
 * `vitepress build` does NOT catch these: the plugin renders diagrams in the
 * browser, so a syntactically broken diagram builds clean and only fails when a
 * reader opens the page. This script closes that gap at authoring time, using
 * the same mermaid version the site ships.
 *
 * Usage: node scripts/check-mermaid.mjs [contentDir]   (default: ../docs)
 * Exits non-zero if any diagram fails.
 */
import fs from 'node:fs'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
import { JSDOM } from 'jsdom'

// Mermaid needs a DOM even to parse. jsdom supplies a throwaway one.
const dom = new JSDOM('<!DOCTYPE html><body></body>', { pretendToBeVisual: true })
globalThis.window = dom.window
globalThis.document = dom.window.document
Object.defineProperty(globalThis, 'navigator', {
  value: dom.window.navigator,
  configurable: true,
})

const mermaid = (await import('mermaid')).default
mermaid.initialize({ startOnLoad: false })

const root = path.resolve(
  process.argv[2] ?? fileURLToPath(new URL('../../docs/', import.meta.url)),
)

if (!fs.existsSync(root)) {
  console.error(`No content directory at ${root}`)
  process.exit(1)
}

function markdownFiles(dir) {
  const out = []
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    if (e.name === 'node_modules' || e.name.startsWith('.')) continue
    const p = path.join(dir, e.name)
    if (e.isDirectory()) out.push(...markdownFiles(p))
    else if (e.name.endsWith('.md')) out.push(p)
  }
  return out
}

/** Extracts fenced mermaid blocks with the line number each one starts on. */
function mermaidBlocks(src) {
  const lines = src.split('\n')
  const blocks = []
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].trim() !== '```mermaid') continue
    const start = i + 1
    let end = start
    while (end < lines.length && lines[end].trim() !== '```') end++
    blocks.push({ code: lines.slice(start, end).join('\n'), line: start + 1 })
    i = end
  }
  return blocks
}

let checked = 0
const failures = []

for (const file of markdownFiles(root)) {
  for (const { code, line } of mermaidBlocks(fs.readFileSync(file, 'utf-8'))) {
    checked++
    try {
      await mermaid.parse(code)
    } catch (err) {
      failures.push({
        file: path.relative(root, file),
        line,
        message: String(err?.message ?? err).trim(),
      })
    }
  }
}

if (failures.length) {
  for (const f of failures) {
    console.error(`\n✗ ${f.file}:${f.line}`)
    console.error(
      f.message
        .split('\n')
        .map((l) => `    ${l}`)
        .join('\n'),
    )
  }
  console.error(`\n${failures.length} of ${checked} diagram(s) failed to parse.`)
  process.exit(1)
}

console.log(`${checked} diagram(s) parsed cleanly.`)
