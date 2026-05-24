# Decode Compressed Color Palette

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bitmask, Array

---

## 🗂 Problem Overview

Given an array of 32-bit ARGB-encoded color integers and a sequence of channel-set operations, apply each operation to every color using only bitwise operations. Each operation targets one 8-bit channel (A, R, G, or B) and overwrites it with a given value. The non-trivial constraint: no arithmetic — every mutation must be expressed purely as masks and bitwise OR/AND. Return the transformed array.

---

## 🌍 Engineering Impact

Packed-integer channel manipulation is the foundation of GPU framebuffer pipelines, image compositing engines (Photoshop blend modes, Cairo, Skia), and network packet field rewriting in eBPF/XDP programs. At scale — millions of pixels per frame or millions of packets per second — arithmetic overhead compounds into measurable latency. The same bitmask pattern appears in CPU flag registers, ELF symbol table encoding, and compact feature-flag bitfields in ML inference serving. Getting the mask algebra wrong corrupts entire buffers silently, which is why the discipline of isolating, clearing, and setting bit fields is a first-class engineering skill.

---

## 🔍 Problem Statement

**Input:**
- `colors`: array of 32-bit unsigned integers, each encoding ARGB as four 8-bit channels packed from MSB to LSB — `[A:31–24][R:23–16][G:15–8][B:7–0]`
- `operations`: list of `[channel, value]` tuples; `channel ∈ {'A','R','G','B'}`, `value ∈ [0, 255]`

**Output:** `colors` after all operations applied in order, in-place or as a new array.

**Constraints:** `1 ≤ colors.length ≤ 10⁵`, `1 ≤ operations.length ≤ 100`, `0 ≤ colors[i] ≤ 2³²−1`.

**Examples:**

| Input colors | Operations | Output |
|---|---|---|
| `[0xFFFF0000, 0x00FF00FF]` | `[G,128],[A,0]` | `[0x00FF8000, 0x00FF80FF]` |
| `[0x12345678]` | `[B,255],[R,0]` | `[0x120000FF]` |

**Key constraint driving the approach:** no arithmetic — forces explicit mask construction and bitwise composition.

---

## 🪜 How to Solve This

1. **Identify the structure** → each color is four independent 8-bit lanes packed into one 32-bit word. Operations are lane-targeted writes. This is textbook bit-field manipulation.

2. **Model a channel write** → to set a channel, you need two things: clear the existing bits in that lane, then OR in the new value. Clearing requires a bitmask with 0s in the target lane and 1s everywhere else (the inverse of the channel mask). Setting requires the new value shifted to the correct bit position.

3. **Precompute per-operation masks** → since the same mask pair applies to every color for a given operation, compute `clear_mask` and `set_mask` once per operation, not once per color. This separates the O(operations) mask-building cost from the O(colors × operations) application cost.

4. **Collapse operations before applying** → with 100 operations max and only 4 channels, you can fold all operations into a single final `clear_mask` and `set_mask` per channel, then apply one pass over the color array. This reduces the inner loop from `O(operations)` to `O(1)` per color.

5. **Apply** → `color = (color & clear_mask) | set_mask` for each color. Correct by construction.

---

## 🧩 Algorithm Walkthrough

**Pattern: Bitmask Field Isolation and Replacement**

This is the canonical read-modify-write pattern on packed integer fields. The abstraction is correct here because channels are non-overlapping, fixed-width, fixed-offset fields — exactly the precondition bitmask algebra requires.

**Steps:**

1. **Define channel metadata** — map each channel label to its bit shift: `A→24, R→16, G→8, B→0`. The 8-bit channel mask at position `s` is `0xFF << s`.

2. **Build the clear mask** — invert the channel mask: `clear_mask = ~(0xFF << shift) & 0xFFFFFFFF`. The `& 0xFFFFFFFF` truncates to 32 bits in languages where integers are wider (Python, Java long). This mask zeros only the target lane.

3. **Build the set mask** — shift the new value into position: `set_mask = value << shift`. This places the 8-bit value exactly in the target lane with zeros elsewhere.

4. **Collapse operations** — process operations in order, maintaining a running `(clear_mask, set_mask)` pair per channel. Later operations on the same channel simply overwrite. After processing all operations, derive a single combined `clear_mask = AND of all channel clear_masks` and `set_mask = OR of all channel set_masks`.

5. **Single-pass application** — iterate `colors`, applying `(color & combined_clear) | combined_set`. Each color is touched exactly once.

6. **Invariant maintained** — untargeted channel bits are never modified; targeted channel bits are fully replaced regardless of prior value.

---

## 📊 Worked Example

Input: `color = 0x12345678`, operations: `[B, 255]` then `[R, 0]`

| Step | Operation | shift | clear_mask | set_mask | color state |
|---|---|---|---|---|---|
| Initial | — | — | — | — | `0x12345678` |
| Op 1 | B=255 | 0 | `0xFFFFFF00` | `0x000000FF` | `(0x12345678 & 0xFFFFFF00) \| 0xFF` = `0x123456FF` |
| Op 2 | R=0 | 16 | `0xFF00FFFF` | `0x00000000` | `(0x123456FF & 0xFF00FFFF) \| 0x00` = `0x120056FF` |

Wait — example output is `0x120000FF`. Re-checking: R occupies bits 16–23, so `0x123456FF & 0xFF00FFFF` clears bits 16–23 of `0x123456FF` → `0x12 00 56 FF`. The green channel (bits 8–15) holds `0x56` and is untouched. Output: `0x120056FF`. *(Note: the problem's stated output `0x120000FF` implies green is also cleared — verify the problem's operation scope if green must be preserved.)*

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n × m)** where `n = colors.length` and `m = operations.length`. With the collapsed-mask optimization, it reduces to **O(n + m)** — O(m) to build combined masks, O(n) for a single array pass. At `n = 10⁶`, the single-pass variant processes in microseconds; the naïve O(n×m) variant at `m = 100` is still linear but 100× slower per element.

### Space Complexity

**O(1)** auxiliary space beyond the output array. The combined masks are four scalar pairs regardless of input size. If in-place mutation is allowed, the output array reuses input storage, keeping total space at O(n) for the array itself with no additional overhead.

---

## 💡 Key Takeaways

- **Pattern signal — packed fields with independent lane writes:** whenever a problem says "set/get a specific sub-field of an integer without touching others," the solution is always `(value & clear_mask) | set_mask`. The shape of the problem, not the domain, triggers this pattern.
- **Pattern signal — "bitwise only, no arithmetic":** this constraint is a direct hint to construct explicit masks. Arithmetic alternatives (division, modulo) are ruled out, leaving only shift and mask composition.
- **Gotcha — sign extension in 32-bit masks:** in Python and Java, `~(0xFF << 24)` produces a negative 64-bit integer. Always AND with `0xFFFFFFFF` after inversion to keep masks in the 32-bit unsigned domain, or use `>>> 0` in JavaScript.
- **Gotcha — operation order matters for the same channel:** if two operations target the same channel, only the last one's value survives. When collapsing operations, process them in sequence and let later operations overwrite earlier set_masks for the same channel.
- **Architectural insight:** precomputing masks outside the hot loop (separating mask construction from mask application) is the same principle behind SIMD shuffle table precomputation, eBPF JIT compilation, and shader uniform uploads — pay the setup cost once, amortize it across the data volume.

---

## 🚀 Variations & Further Practice

- **Per-color operations:** instead of applying each operation to all colors, each operation targets a specific index range `[l, r]`. The conceptual twist is that you can no longer collapse operations globally — you need a range-update structure (difference array or segment tree with lazy propagation) to efficiently apply bitmask operations over arbitrary sub-ranges.
- **Composite blend modes:** instead of setting a channel to a fixed value, blend the existing channel with the new value using a weight (e.g., alpha compositing: `out = src_alpha * src + (1 - src_alpha) * dst`). This breaks the pure-bitwise constraint and forces you to reason about fixed-point arithmetic within packed integer lanes — the core challenge in software rasterizers.
- **Bidirectional decode + re-encode:** given two color arrays, merge them by taking specific channels from each (e.g., R and G from array 1, B and A from array 2). The twist is composing multiple clear/set masks in a single pass without intermediate unpacking, which tests whether you can reason about mask algebra across two sources simultaneously.