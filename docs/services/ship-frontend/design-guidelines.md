# ship-frontend — Design guidelines

The look and interaction rules for the UI: a "captain's chart" theme built on Angular Material and the
Angular CDK ([decision 0001](decisions/0001-build-the-ui-on-angular-material-with-a-pirate-theme.md)).
The approved mockup of the pirate redesign is kept for history: https://claude.ai/artifact/DcvRaKWmvmaU348SqFS4gz; the design system below is the current visual reference.
Screens not yet migrated still use the old hand-written CSS; every redesign story moves one screen onto
these rules.

## Design system

The Hexagonship design system (https://claude.ai/artifact/EMavsufZDSEPPMWJiBF9PN) mirrors these
guidelines: the tokens, the type scale and the screen components (app bar, ship card, crate card,
deck, Incoming Ship row and the rest) with their exact values, and the ship and Cargo pictures. Its
components are static HTML/CSS renditions hand-written from this component's templates and styles,
first synced from `ship-frontend@1385e47`; the Angular code stays the source of truth.

- Visual prototypes for a story are built from the design system, so they look like the app.
- A story that changes a token, a component's look or the wording rules updates the design system
  in the same story: the token in its `tokens.json`, the component's preview and README, and the
  brand book where a rule changed. A change to these guidelines changes the design system with it.
- Where the design system and the code disagree, the code wins; fix the design system.

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
  navigation pills (Fleet, Harbor, and Imprisoned Ships only while this Harbor holds an Imprisoned Ship).
- **Page header** — italic `muted` kicker line above an IM Fell English heading.
- **Fleet** — a responsive grid of ship cards: ship picture, status chip (In port / Preparing / At
  sea / Imprisoned, the last an `ink` chip with a brass lock icon), the Catain portrait in a brass ring overlapping the picture, Ship Name, "under `<Catain>`",
  "Home Harbor `<Harbor>`", "`<Ship Class>` · Crew Capacity `<n>`" (sailing icon; the Crew Capacity taken from the backend), "Arrived from `<Harbor>`" when set, a Crew row when the ship has Crew (a chip with the groups icon "Crew <n> / <Crew Capacity>", overlapping brass-ringed initials (aria-hidden) and the muted names; none for a ship without Crew), and the card's actions. Arrival notices (", imprisoned" appended for an imprisoned Arrival, "with its Crew: <A> and <B>" appended for an Arrival with Crew) and imprisonment notices (lock icon) are `ink` bars with a
  dismiss button above the grid; a refused new Shipping is an `oxblood` alert in the ship card's actions. An Imprisoned Ship's card carries a Fine panel (the Fine, whether the Savings cover it, a filled Pay Fine button); a paid Fine is an `ink` notice bar with a dismiss button that also dismisses itself after 8 s.
- **Crew** — `/ships/:id/crew`, reached by a "Crew" button on every ship card: a back link "Available
  Ships", a header (kicker "The crew of", Ship Name, muted facts line, Savings on the right) and two panels
  side by side, stacked at phone width. The "Crew" panel has a capacity chip "n / m · Class", a card per Crew
  Member (initial in a brass ring, name, trait chip) and dashed "A free berth: Hire a Recruit" cards. The
  "Recruit Pool" panel has the line "Hiring Fee 50.00 $, paid from the Savings of `<Home Harbor>`", a filled
  Hire button per Recruit (aria-label "Hire `<name>` for `<fee>` $") and a "new in the pool" chip. A refused
  Hire is an `oxblood` alert. A full Crew replaces the pool with an empty-state line. Away from the Home
  Harbor or at sea the screen is read-only, with an italic line and a `home` icon. The Recruit Pool is shown
  on this screen only, not on the Harbor page.
- **Imprisoned Ships** — kicker "Held in the harbor brig", one card per Imprisoned Ship with its Fine and Pay Fine.
- **Create ship** — Ship Name field, then the Ship Class picker (Sloop, Brig, Galleon as smaller radio
  cards with a `sailing` icon, the selected one framed in `oxblood` and badged; a "Choose a Ship Class"
  hint in the actions while none is chosen), then the Catain grid as radio cards with large portraits; the
  selected Catain is lifted, framed in `oxblood` and badged. Both stay real radio groups.
- **Load Cargo** — Available Cargo as crate cards (picture, Stock badge, Weight) dragged onto the
  ship (CDK drag-drop); the drop zone shows the overlay while a crate is over it. Loaded Cargo as chips
  with a remove button (and a quantity once ships carry several of one Cargo). Current Weight as a
  progress bar "x / 15". Rejections (Max Weight, out of Stock) appear as an `oxblood` alert in the
  drop zone.

## Accessibility

- Every drag has a keyboard and click alternative: each crate card has a Load button.
- Loaded Cargo, Catain selection and Ship Class selection are operable with the keyboard alone.
- Images: Catain portraits carry the Catain's name as alt text; decorative pictures have empty alt.

## Assets

Cargo pictures: `public/cargo/<cargo>.jpg`. Ship picture: `public/img/ship.jpg`. Catain portraits:
`/web/catains/{id}/image`.
