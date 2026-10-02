// The Markdown content is the sibling ../docs directory (kept content-only so the doc paths the
// team's skills/agents reference stay stable). VitePress resolves each page's runtime deps
// (vue/server-renderer, …) from the page's own location, so ../docs needs a node_modules on its
// path. We point a symlink at this app's real node_modules instead of installing a second copy.
// Runs as `postinstall`, so it's recreated automatically after every `npm install`.
import { symlinkSync, lstatSync } from 'node:fs'
import { fileURLToPath } from 'node:url'

const link = fileURLToPath(new URL('../../docs/node_modules', import.meta.url))

let exists = false
try {
  lstatSync(link)
  exists = true
} catch {
  /* not there yet */
}

if (!exists) {
  try {
    symlinkSync('../docs_website/node_modules', link, 'dir')
    console.log('linked docs/node_modules -> ../docs_website/node_modules')
  } catch (e) {
    console.warn('could not create docs/node_modules symlink:', e.message)
  }
}
