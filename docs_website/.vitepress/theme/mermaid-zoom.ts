/**
 * Client-only enhancement: adds a zoom affordance to every rendered Mermaid
 * diagram. Clicking the diagram (or its button) opens a lightbox with
 * wheel/pinch zoom, drag-to-pan, and "open the SVG in a new tab".
 *
 * Mermaid renders asynchronously and on every route change, so a
 * MutationObserver is used to catch diagrams whenever they appear. Opening is
 * handled by a SINGLE delegated document-level click listener (rather than a
 * listener per diagram) so nothing can be bound twice and a click can never
 * open two lightboxes.
 */
interface MmZoomGlobals {
  __mmClick?: (e: MouseEvent) => void
  __mmObserver?: MutationObserver
}

export function setupMermaidZoom(): void {
  if (typeof window === 'undefined') return
  const g = window as unknown as MmZoomGlobals

  // Self-healing init: tear down any previous listener/observer before wiring
  // up fresh ones. This makes re-running setup (HMR, remounts) idempotent —
  // it always ends with exactly ONE delegated click listener and ONE observer,
  // never a stale duplicate that could open the lightbox twice.
  if (g.__mmClick) document.removeEventListener('click', g.__mmClick)
  if (g.__mmObserver) g.__mmObserver.disconnect()

  // Decorate diagrams with the hover button + affordance class. No click
  // listeners are attached here — those are delegated (see below). This runs
  // idempotently on every mutation: the plugin re-renders a diagram's innerHTML
  // on theme toggle (and on the salt re-render it does for mindmap/c4/zenuml),
  // which wipes our button — so we always re-attach it when missing rather than
  // gating on a one-shot "ready" flag.
  function enhance(): void {
    document.querySelectorAll<HTMLElement>('.vp-doc .mermaid').forEach((block) => {
      const svg = block.querySelector('svg')
      if (!svg) return
      block.classList.add('mm-zoomable')

      if (!block.querySelector('.mm-zoom-btn')) {
        const btn = document.createElement('button')
        btn.type = 'button'
        btn.className = 'mm-zoom-btn'
        btn.title = 'Zoom diagram'
        btn.setAttribute('aria-label', 'Zoom diagram')
        btn.textContent = '⤢'
        block.appendChild(btn)
      }
    })
  }

  // One delegated listener for the whole document. It fires exactly once per
  // click regardless of how many diagrams exist, and cannot accumulate.
  const onClick = (e: MouseEvent) => {
    const target = e.target as HTMLElement
    if (target.closest('a')) return // don't hijack links inside a diagram
    // Ignore clicks already inside an open lightbox.
    if (target.closest('.mm-lightbox')) return
    const host = target.closest<HTMLElement>('.vp-doc .mermaid')
    if (!host) return
    const svg = host.querySelector('svg') as SVGSVGElement | null
    if (svg) openLightbox(svg)
  }
  document.addEventListener('click', onClick)
  g.__mmClick = onClick

  function openLightbox(sourceSvg: SVGSVGElement): void {
    // Never stack lightboxes — a bubbled/duplicate click must be a no-op.
    if (document.querySelector('.mm-lightbox')) return

    const overlay = document.createElement('div')
    overlay.className = 'mm-lightbox'

    const toolbar = document.createElement('div')
    toolbar.className = 'mm-toolbar'
    toolbar.innerHTML =
      '<button data-act="out" title="Zoom out" aria-label="Zoom out">−</button>' +
      '<button data-act="reset" title="Reset" aria-label="Reset view">Reset</button>' +
      '<button data-act="in" title="Zoom in" aria-label="Zoom in">+</button>' +
      '<button data-act="tab" title="Open SVG in new tab" aria-label="Open in new tab">↗</button>' +
      '<button data-act="close" title="Close (Esc)" aria-label="Close">✕</button>'

    const stage = document.createElement('div')
    stage.className = 'mm-stage'
    const clone = sourceSvg.cloneNode(true) as SVGSVGElement
    clone.removeAttribute('style')
    // Mermaid often sets width="100%", which stretches and off-centres the
    // diagram in the lightbox. Pin an explicit natural size from the viewBox
    // so it renders centred at its true aspect ratio (CSS then caps it).
    const vb = clone.viewBox?.baseVal
    if (vb && vb.width && vb.height) {
      clone.setAttribute('width', String(Math.round(vb.width)))
      clone.setAttribute('height', String(Math.round(vb.height)))
    }
    stage.appendChild(clone)

    overlay.append(toolbar, stage)
    document.body.appendChild(overlay)
    // Scroll-lock on <body>, NOT <html>: the mermaid plugin puts a
    // MutationObserver on document.documentElement's attributes and re-renders
    // the diagram on any change. Touching documentElement.style here would trip
    // it, re-rendering the diagram underneath the lightbox — and mermaid's
    // re-render can leave a duplicate/orphan SVG (the "second diagram behind
    // it"). Locking <body> keeps the same scroll behaviour without the trigger.
    document.body.style.overflow = 'hidden'

    let scale = 1
    let tx = 0
    let ty = 0
    const MIN = 0.2
    const MAX = 12
    const apply = () => {
      clone.style.transform = `translate(${tx}px, ${ty}px) scale(${scale})`
    }
    const clamp = (s: number) => Math.min(MAX, Math.max(MIN, s))
    const zoomBy = (factor: number) => {
      scale = clamp(scale * factor)
      apply()
    }
    const reset = () => {
      scale = 1
      tx = 0
      ty = 0
      apply()
    }

    // Wheel zoom, anchored on the pointer position.
    stage.addEventListener(
      'wheel',
      (e) => {
        e.preventDefault()
        const rect = clone.getBoundingClientRect()
        const cx = e.clientX - (rect.left + rect.width / 2)
        const cy = e.clientY - (rect.top + rect.height / 2)
        const prev = scale
        scale = clamp(scale * (e.deltaY < 0 ? 1.1 : 1 / 1.1))
        const k = scale / prev - 1
        tx -= cx * k
        ty -= cy * k
        apply()
      },
      { passive: false },
    )

    // Drag to pan.
    let dragging = false
    let sx = 0
    let sy = 0
    stage.addEventListener('pointerdown', (e) => {
      dragging = true
      sx = e.clientX - tx
      sy = e.clientY - ty
      stage.setPointerCapture(e.pointerId)
      stage.style.cursor = 'grabbing'
    })
    stage.addEventListener('pointermove', (e) => {
      if (!dragging) return
      tx = e.clientX - sx
      ty = e.clientY - sy
      apply()
    })
    const endDrag = () => {
      dragging = false
      stage.style.cursor = 'grab'
    }
    stage.addEventListener('pointerup', endDrag)
    stage.addEventListener('pointercancel', endDrag)

    const close = () => {
      overlay.remove()
      document.body.style.overflow = ''
      document.removeEventListener('keydown', onKey)
    }
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') close()
      else if (e.key === '+' || e.key === '=') zoomBy(1.2)
      else if (e.key === '-') zoomBy(1 / 1.2)
      else if (e.key === '0') reset()
    }
    document.addEventListener('keydown', onKey)

    toolbar.addEventListener('click', (e) => {
      const act = (e.target as HTMLElement).dataset.act
      if (!act) return
      e.stopPropagation()
      if (act === 'in') zoomBy(1.25)
      else if (act === 'out') zoomBy(1 / 1.25)
      else if (act === 'reset') reset()
      else if (act === 'close') close()
      else if (act === 'tab') openInNewTab(sourceSvg)
    })

    // Click the dimmed backdrop (not the diagram) to close.
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay || e.target === stage) close()
    })
  }

  function openInNewTab(sourceSvg: SVGSVGElement): void {
    const clone = sourceSvg.cloneNode(true) as SVGSVGElement
    if (!clone.getAttribute('xmlns')) {
      clone.setAttribute('xmlns', 'http://www.w3.org/2000/svg')
    }
    const markup = new XMLSerializer().serializeToString(clone)
    const blob = new Blob([markup], { type: 'image/svg+xml;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    window.open(url, '_blank', 'noopener')
    window.setTimeout(() => URL.revokeObjectURL(url), 60_000)
  }

  const observer = new MutationObserver(() => enhance())
  g.__mmObserver = observer
  const start = () => {
    enhance()
    observer.observe(document.body, { childList: true, subtree: true })
  }
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', start)
  } else {
    start()
  }
}