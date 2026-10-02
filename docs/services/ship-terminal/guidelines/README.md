# ship-terminal — guidelines

Conventions for `ship-terminal`, in one place. **Each file states the rule as it is now — no dates, no
history, no ticket references.** Why a rule exists and when it changed belongs in
[`../decisions/`](../decisions/README.md); this folder only says what is true today.

| File | Covers |
|---|---|
| [`architecture.md`](architecture.md) | Application shape, the own-copy event model and tolerant reader, at-least-once handling, output wording |
| [`testing.md`](testing.md) | Deserializer and formatter tests, recorded payload samples |

A rule here applies to the whole component. Only rules that are **persistent and non-obvious from
the code** belong here — not the name of one class, not something the next refactor changes.
