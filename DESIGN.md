# Skim Design System

## 1. Atmosphere & identity

Skim is a quiet evidence desk: warm paper, graphite reading text, and a restrained burnt-orange signal that appears only when a listener can verify a claim. The memorable moment is the transition from a compact summary timestamp to its highlighted spoken evidence.

## 2. Color

| Role | Light | Dark | Use |
|---|---|---|---|
| Background / surface | `#FFFBF6` | `#151613` | reading canvas |
| Surface variant | `#F4EFE8` | `#242522` | cards and panels |
| On surface | `#241F1A` | `#E9E5DF` | primary text |
| On surface variant | `#6B625B` | `#CCC6BF` | metadata |
| Moss primary | `#38513F` | `#B8D3BD` | navigation and primary actions |
| Moss container | `#E2EFE1` | `#243D2A` | review queue |
| Straw container | `#F8F3D6` | `#4C461D` | summary lead |
| Evidence orange | `#C55222` | `#FFC1A0` | timestamp and selected evidence |
| Evidence container | `#FFE5D8` | `#532511` | selected evidence state |
| Error container | `#FCE8E5` | `#5B2018` | recoverable errors |

No wallpaper-driven dynamic colour or blue/purple AI gradients.

## 3. Typography

Use the platform sans stack. Display 32sp/700, headline 24sp/700, title 20sp/700, body 16sp/400 with 24sp leading, metadata 13sp/500. Large headings are tight; metadata uses small positive tracking and tabular numerals when available.

## 4. Spacing & layout

Base unit is 4dp. Screen inset 20dp; card inset 20dp; list gap 12dp; section gap 28dp. Card radii: 16dp for standard cards, 24dp for the review-queue feature card, pill only for status and timestamp badges.

## 5. Components

- **Evidence card:** status line, title, one-line takeaway, compact date and proof count. Default uses surface variant; press follows Material state layer; tap opens the record.
- **Evidence timestamp:** orange-toned action labelled `근거 듣기 <timestamp>`. Activating it opens the Evidence tab, highlights the matching transcript chunk, and seeks audio.
- **Bottom navigation:** four existing destinations with Material vectors, Korean semantic labels, and a moss selected state.
- **Notice card:** low-emphasis error or upload guidance with readable text, never an alert dialog.

## 6. Motion & interaction

Only evidence selection animates: a 150ms colour transition on the selected transcript card. Navigation and buttons use Material pressed/focus state layers; no decorative looping animation.

## 7. Depth & surface

Tonal shift with whisper outlines. Surfaces are separated by warm value changes and a 1dp outline, not heavy shadows.

## 8. Accessibility constraints & accepted debt

Target WCAG 2.2 AA: normal text 4.5:1, non-text controls 3:1, minimum 48dp interactive targets, visible focus supplied by Material components, and descriptive labels for navigation/source actions. No accepted accessibility debt.
