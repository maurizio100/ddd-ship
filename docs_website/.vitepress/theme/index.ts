import { h } from 'vue'
import DefaultTheme from 'vitepress/theme'
import type { Theme } from 'vitepress'
import { setupMermaidZoom } from './mermaid-zoom'
import HeroShip from './HeroShip.vue'

// Fonts (Alegreya Sans, IM Fell English, Pirata One) come from Google Fonts,
// linked in config.mts — the same faces as ship-frontend.
import './custom.css'

export default {
  extends: DefaultTheme,
  // The home page's hero shows the galleon instead of an image file.
  Layout: () => h(DefaultTheme.Layout, null, { 'home-hero-image': () => h(HeroShip) }),
  setup() {
    setupMermaidZoom()
  },
} satisfies Theme
