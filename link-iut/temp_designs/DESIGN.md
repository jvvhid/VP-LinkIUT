---
name: Tectonic Modular System
colors:
  surface: '#faf9f8'
  surface-dim: '#dadad9'
  surface-bright: '#faf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f4f3f2'
  surface-container: '#eeeeed'
  surface-container-high: '#e9e8e7'
  surface-container-highest: '#e3e2e1'
  on-surface: '#1a1c1c'
  on-surface-variant: '#57423e'
  inverse-surface: '#2f3130'
  inverse-on-surface: '#f1f0f0'
  outline: '#8a716d'
  outline-variant: '#dec0ba'
  surface-tint: '#a33c2b'
  primary: '#6e1609'
  on-primary: '#ffffff'
  primary-container: '#8e2d1d'
  on-primary-container: '#ffab9c'
  inverse-primary: '#ffb4a6'
  secondary: '#586062'
  on-secondary: '#ffffff'
  secondary-container: '#dae1e3'
  on-secondary-container: '#5d6466'
  tertiary: '#2f393d'
  on-tertiary: '#ffffff'
  tertiary-container: '#455054'
  on-tertiary-container: '#b7c2c6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#ffdad4'
  primary-fixed-dim: '#ffb4a6'
  on-primary-fixed: '#3f0300'
  on-primary-fixed-variant: '#832516'
  secondary-fixed: '#dde4e6'
  secondary-fixed-dim: '#c1c8ca'
  on-secondary-fixed: '#161d1f'
  on-secondary-fixed-variant: '#41484a'
  tertiary-fixed: '#d9e4e9'
  tertiary-fixed-dim: '#bdc8cd'
  on-tertiary-fixed: '#131d21'
  on-tertiary-fixed-variant: '#3e484c'
  background: '#faf9f8'
  on-background: '#1a1c1c'
  surface-variant: '#e3e2e1'
typography:
  headline-xl:
    fontFamily: Anybody
    fontSize: 48px
    fontWeight: '800'
    lineHeight: 56px
    letterSpacing: -0.02em
  headline-lg:
    fontFamily: Anybody
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  headline-md:
    fontFamily: Anybody
    fontSize: 24px
    fontWeight: '700'
    lineHeight: 32px
  headline-sm:
    fontFamily: Anybody
    fontSize: 20px
    fontWeight: '700'
    lineHeight: 28px
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 18px
    fontWeight: '400'
    lineHeight: 28px
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  label-md:
    fontFamily: JetBrains Mono
    fontSize: 14px
    fontWeight: '500'
    lineHeight: 20px
    letterSpacing: 0.05em
  headline-xl-mobile:
    fontFamily: Anybody
    fontSize: 32px
    fontWeight: '800'
    lineHeight: 40px
spacing:
  unit: 8px
  gutter: 24px
  margin-desktop: 64px
  margin-mobile: 20px
  border-width: 3px
  isometric-depth: 4px
---

## Brand & Style

This design system is inspired by the monumental "brick-by-brick" modularity of architectural heritage, specifically the iconic terracotta structures of IUT. It evokes a sense of **stability, legacy, and structural growth**. 

The aesthetic sits at the intersection of **Architectural Minimalism** and **Modern Brutalism**. It utilizes interlocking components and a rigid grid to simulate the feeling of a physical campus. The target audience—university alumni—should feel a professional yet nostalgic connection, as if they are contributing to a digital "ever-expanding" monument of their community.

Visual hallmarks include:
- **Modular Interlocking:** Elements fit together with precise, heavy borders.
- **Isometric Tactility:** Use of hard-edged shadows to provide a building-block physical presence.
- **Structured Permanence:** Heavy stroke weights and sharp corners that emphasize the strength of the network.

## Colors

The palette is rooted in the "Deep Terracotta" of fired brick, providing an authoritative and warm primary anchor.

- **Deep Terracotta (#8E2D1D):** Used for primary actions, structural accents, and bold branding.
- **Crisp White (#FDFCFB):** A slightly warm off-white that prevents visual fatigue while maintaining high contrast.
- **Charcoal Gray (#2D3436):** Used for heavy borders, primary text, and isometric shadows to give depth.
- **Slate Accent (#636E72):** Used for secondary UI elements and auxiliary information, grounding the warmer red tones.

Use the primary color sparingly for structural emphasis and call-to-actions to maintain a high-end, professional feel.

## Typography

The typography strategy mirrors the "chunky yet professional" requirement. 

- **Headlines:** Use **Anybody** in bold/extra-bold weights. The variable nature of the font allows for "stretched" or "compressed" feeling headers that look like engraved building blocks.
- **Body:** Use **Hanken Grotesk** for long-form content. It provides a sharp, modern, and highly legible contrast to the aggressive headings.
- **Labels:** **JetBrains Mono** is used for metadata, tags, and small technical labels to reinforce the "engineered" feel of the platform.

All headlines should be set in optical sizing to ensure the blocky character is preserved even at smaller scales.

## Layout & Spacing

The layout is a **Rigid Architectural Grid**. 

- **The 8px Rhythm:** All spacing, padding, and margins must be multiples of 8px to ensure a "pixel-perfect" brick alignment.
- **Grid Model:** Use a 12-column fixed grid for desktop (max-width 1440px) with 24px gutters.
- **Modular Blocks:** Components should "snap" to the grid. Avoid fluid widths that create awkward whitespace; instead, use modular containers that take up specific column spans (e.g., 3-column "bricks" for cards).
- **Responsive Reflow:** On mobile, the grid collapses to 4 columns. Spacing between "bricks" should remain consistent (24px) to maintain the modular look.

## Elevation & Depth

This design system rejects soft, ambient shadows in favor of **Isometric 3D Shadows**.

- **Shadow Style:** Use hard-edged, 100% opacity shadows shifted 4px down and 4px right. The color should be a darker shade of the element's border or the Charcoal Gray.
- **The "Press" Effect:** Interactive elements (buttons, cards) should physically "press" into the page on hover or click. This is achieved by moving the element +4px/+4px and removing the isometric shadow.
- **Tonal Tiers:** Use the primary Terracotta for "Raised" surfaces and the neutral Slate for "Sunken" or background areas.
- **Borders:** Every "brick" (card/input) must have a **3px solid border** in Charcoal Gray to define its footprint.

## Shapes

To maintain the architectural integrity of red-brick construction, **sharp corners (0px roundedness)** are mandatory for all structural components. 

- **Rectilinear Forms:** Buttons, cards, and input fields are strictly rectangular. 
- **The Arch Motif:** Occasional use of the pointed "IUT Arch" can be used for decorative framing or large-scale section dividers, but never for functional buttons or inputs.

## Components

### Buttons
- **Primary:** Terracotta background, Charcoal 3px border, 4px isometric shadow. Text in Bold Anybody, uppercase.
- **Hover State:** Translate element +4px, +4px. Shadow disappears.

### Modular Cards
- **Base:** Crisp White background with a Charcoal border. 
- **Header:** A 40px top-bar section in Terracotta or Gray with the label in JetBrains Mono.
- **Content:** Information should be padded by 24px (3 units of 8px).

### Input Fields
- **Style:** "Sunken" appearance. 3px border with an inner-shadow on the top and left to simulate depth. 
- **Typography:** Hanken Grotesk 16px.

### Chips & Tags
- **Style:** Small 0px-rounded rectangles. Primary color background with white text or vice versa. No shadow to keep them "flat" against the brick surface.

### List Items
- **Interlocking:** Items in a list should share borders (border-collapse style) to look like a single vertical column of bricks. Use a 1px divider between them, but a 3px outer border for the entire list container.