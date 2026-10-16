<script setup lang="ts">
// Home-hero illustration: a black-sailed galleon with a hexagon on the
// mainsail, bobbing on the waves. Pure SVG + CSS, no assets to ship.
</script>

<template>
  <div class="hero-ship" aria-hidden="true">
    <svg viewBox="0 0 320 300" xmlns="http://www.w3.org/2000/svg">
      <defs>
        <!-- fade the sea out at both ends so it has no hard edges -->
        <linearGradient id="hs-fade" x1="0" x2="1" y1="0" y2="0">
          <stop offset="0" stop-color="#fff" stop-opacity="0" />
          <stop offset="0.18" stop-color="#fff" stop-opacity="1" />
          <stop offset="0.82" stop-color="#fff" stop-opacity="1" />
          <stop offset="1" stop-color="#fff" stop-opacity="0" />
        </linearGradient>
        <mask id="hs-sea-mask" maskUnits="userSpaceOnUse" x="0" y="0" width="320" height="300">
          <rect x="0" y="0" width="320" height="300" fill="url(#hs-fade)" />
        </mask>
      </defs>
      <!-- moon -->
      <circle class="hs-moon" cx="250" cy="60" r="26" />

      <g class="hs-bob">
        <!-- masts -->
        <rect class="hs-wood" x="156" y="40" width="6" height="170" />
        <rect class="hs-wood" x="96" y="80" width="5" height="128" />
        <rect class="hs-wood" x="214" y="90" width="5" height="118" />

        <!-- yards -->
        <rect class="hs-wood" x="112" y="62" width="94" height="4" rx="2" />
        <rect class="hs-wood" x="68" y="100" width="60" height="4" rx="2" />
        <rect class="hs-wood" x="190" y="108" width="54" height="4" rx="2" />

        <!-- sails -->
        <path class="hs-sail" d="M116 66 Q159 78 202 66 L198 168 Q159 182 120 168 Z" />
        <path class="hs-sail" d="M72 104 Q98 112 124 104 L121 176 Q98 186 75 176 Z" />
        <path class="hs-sail" d="M194 112 Q217 119 240 112 L237 178 Q217 186 197 178 Z" />

        <!-- hexagon emblem with crossed bones on the mainsail -->
        <g class="hs-emblem" transform="translate(159 118)">
          <polygon points="0,-30 26,-15 26,15 0,30 -26,15 -26,-15" />
          <line x1="-16" y1="-16" x2="16" y2="16" />
          <line x1="16" y1="-16" x2="-16" y2="16" />
          <circle cx="0" cy="-2" r="9" />
          <circle class="hs-eye" cx="-3.5" cy="-3" r="2" />
          <circle class="hs-eye" cx="3.5" cy="-3" r="2" />
        </g>

        <!-- jolly roger flag -->
        <path class="hs-flag" d="M162 40 L192 46 L162 54 Z" />

        <!-- hull -->
        <path class="hs-hull" d="M48 200 L272 200 L250 240 Q160 254 72 240 Z" />
        <path class="hs-trim" d="M60 214 L262 214" />
        <circle class="hs-port" cx="100" cy="226" r="4" />
        <circle class="hs-port" cx="140" cy="229" r="4" />
        <circle class="hs-port" cx="180" cy="229" r="4" />
        <circle class="hs-port" cx="220" cy="226" r="4" />
      </g>

      <!-- waves -->
      <g mask="url(#hs-sea-mask)">
      <path class="hs-wave hs-wave-1"
        d="M-40 246 Q-20 236 0 246 T40 246 T80 246 T120 246 T160 246 T200 246 T240 246 T280 246 T320 246 T360 246 V300 H-40 Z" />
      <path class="hs-wave hs-wave-2"
        d="M-40 262 Q-20 252 0 262 T40 262 T80 262 T120 262 T160 262 T200 262 T240 262 T280 262 T320 262 T360 262 V300 H-40 Z" />
      </g>
    </svg>
  </div>
</template>

<style scoped>
.hero-ship {
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
}
svg {
  width: 100%;
  height: auto;
  /* hidden: the drifting waves run past the viewBox and would show their edges */
  overflow: hidden;
}
.hs-moon { fill: var(--pirate-gold); opacity: 0.35; }
.hs-wood { fill: var(--pirate-wood); }
.hs-sail { fill: var(--pirate-sail); stroke: var(--pirate-wood); stroke-width: 1.5; }
.hs-flag { fill: var(--pirate-ink); }
.hs-hull { fill: var(--pirate-wood); stroke: var(--pirate-ink); stroke-width: 1.5; }
.hs-trim { stroke: var(--pirate-gold); stroke-width: 3; }
.hs-port { fill: var(--pirate-ink); }
.hs-emblem polygon { fill: none; stroke: var(--pirate-gold); stroke-width: 3; }
.hs-emblem line { stroke: var(--pirate-bone); stroke-width: 4; stroke-linecap: round; }
.hs-emblem circle { fill: var(--pirate-bone); }
.hs-emblem .hs-eye { fill: var(--pirate-sail); }
.hs-wave-1 { fill: var(--pirate-sea-2); }
.hs-wave-2 { fill: var(--pirate-sea); }

.hs-bob {
  transform-box: fill-box;
  transform-origin: center bottom;
  animation: hs-bob 5s ease-in-out infinite;
}
.hs-wave-1 { animation: hs-drift 7s linear infinite; }
.hs-wave-2 { animation: hs-drift 5s linear infinite reverse; }

@keyframes hs-bob {
  0%, 100% { transform: translateY(0) rotate(-1.5deg); }
  50%      { transform: translateY(5px) rotate(1.5deg); }
}
@keyframes hs-drift {
  from { transform: translateX(0); }
  to   { transform: translateX(40px); }
}
@media (prefers-reduced-motion: reduce) {
  .hs-bob, .hs-wave-1, .hs-wave-2 { animation: none; }
}
</style>
