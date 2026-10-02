import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import { setupMermaidZoom } from './mermaid-zoom'

// Inter — a clean, OpenFont-licensed UI typeface, safe to ship on a public site.
import '@fontsource/inter/300.css'
import '@fontsource/inter/400.css'
import '@fontsource/inter/500.css'
import '@fontsource/inter/600.css'
import '@fontsource/inter/700.css'

import './custom.css'

export default {
  extends: DefaultTheme,
  setup() {
    setupMermaidZoom()
  },
} satisfies Theme
