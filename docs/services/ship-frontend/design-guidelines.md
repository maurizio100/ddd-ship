# ship-frontend — Design guidelines

The look and interaction rules for the UI: a "captain's chart" theme built on Angular Material and the
Angular CDK ([decision 0001](decisions/0001-build-the-ui-on-angular-material-with-a-pirate-theme.md)).
The approved mockup is the visual reference: https://claude.ai/artifact/DcvRaKWmvmaU348SqFS4gz.
Screens not yet migrated still use the old hand-written CSS; every redesign story moves one screen onto
these rules.

## Theme

| Token | Value | Used for |
|---|---|---|
| `ink` | `#1B2A3A` | App bar, body text, dark surfaces (drop zone, notices) |
| `parchment` | `#F3E9D2` | Page background; text on `ink` |
| `card` | `#FBF5E6` | Cards and panels |
| `raised` | `#FFF8EC` | Selected card, crate cards, inputs; text on `oxblood` |
| `sand` | `#E8D9B5` | Chips, avatar ground, active navigation pill |
| `border` | `#D9C79E` | Card borders |
| `field-border` | `#8A7A5C` | Input and select borders |
| `muted` | `#5A4E3C` | Secondary text and kicker lines |
| `oxblood` | `#8C2F1B` | Primary action, selection frame, alerts (hover `#6A2112`) |
| `brass` | `#B8862B` | Trim only: app bar underline (4px), portrait rings, focus ring, progress fill |
| `warning` | `#C0532F` | Current Weight bar above 85 % of the Max Weight |

Angular Material mapping: primary = `oxblood`, tertiary = `brass`, surface = `parchment`, on-surface =
`ink`. Define the palette and typography once in the theme; component CSS uses the theme's system
variables, never hex values.

Brass is never used for text on parchment (contrast too low). All text meets 4.5:1.

## Typography

| Font (Google Fonts) | Used for |
|---|---|
| Pirata One | The "Hexagonship" wordmark only |
| IM Fell English | Page headings (48px), section headings, Ship and Catain names; italic for kicker lines |
| Alegreya Sans 400/500/700 | Everything else |

## Shapes and components

- Buttons: pill-shaped (fully rounded), at least 44px high. One filled `oxblood` primary action per
  view; secondary actions outlined in `ink`.
- Cards: 16–20px radius, `border`, soft shadow. Chips: pill-shaped.
- Icons: Material Symbols or inline stroke icons, never emoji.
  `MatIconRegistry` registers Material Symbols Outlined as the default font set with the
  `mat-ligature-font` class (`src/app/app.config.ts`), so `<mat-icon fontIcon="anchor">` works
  everywhere; without that class the icon renders blank.
- Focus: a 3px `brass` outline on every focusable element.

## Wording

Headings, labels, buttons and notices use the ubiquitous language exactly (`docs/domain/`):
"Available Ships", "Create New Ship", "Choose Your Catain", "Available Cargo", "Loaded Cargo",
"Current Weight", "Destination Harbor", "Release". Pirate flavour goes only into kicker lines
("The ship's register"), empty states ("The hold is empty") and the drag overlay ("Heave it aboard!").

## Screen patterns

- **App bar** — `ink` bar with brass underline: wordmark, a chip with the current Harbor Name, and
  navigation pills (Fleet; Harbor once the harbor management page exists).
- **Page header** — italic `muted` kicker line above an IM Fell English heading.
- **Fleet** — a responsive grid of ship cards: ship picture, status chip (In port / Preparing / At
  sea), the Catain portrait in a brass ring overlapping the picture, Ship Name, "under <Catain>",
  "Arrived from <Harbor>" when set, and the card's actions. Arrival notices are an `ink` bar with a
  dismiss button above the grid.
- **Create ship** — Ship Name field, then the Catain grid as radio cards with large portraits; the
  selected Catain is lifted, framed in `oxblood` and badged. It stays a real radio group.
- **Load Cargo** — Available Cargo as crate cards (picture, Stock badge, Weight) dragged onto the
  ship (CDK drag-drop); the drop zone shows the overlay while a crate is over it. Loaded Cargo as chips
  with a remove button (and a quantity once ships carry several of one Cargo). Current Weight as a
  progress bar "x / 15". Rejections (Max Weight, out of Stock) appear as an `oxblood` alert in the
  drop zone.

## Accessibility

- Every drag has a keyboard and click alternative: each crate card has a Load button.
- Loaded Cargo and Catain selection are operable with the keyboard alone.
- Images: Catain portraits carry the Catain's name as alt text; decorative pictures have empty alt.

## Assets

Cargo pictures: `public/cargo/<cargo>.jpg`. Ship picture: `public/img/ship.jpg`. Catain portraits:
`/web/catains/{id}/image`.
