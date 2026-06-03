# Generate All Valid Bracket Colorings

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Recursion and Backtracking &nbsp;|&nbsp; **Tags:** Recursion, Backtracking, String Generation

---

## 🗂 Problem Overview

Given `n` pairs of brackets, generate every string that is both a valid bracket sequence and a valid coloring — where each individual bracket character is independently labeled Red or Blue. The output is a flat list of strings using the notation `R(`, `B(`, `R)`, `B)`. The non-trivial constraint is the cross-product nature: structural validity (proper nesting) and color assignment are orthogonal dimensions that must be enumerated jointly, not sequentially.

---

## 🌍 Engineering Impact

This pattern — enumerating a constrained combinatorial space by interleaving structural rules with independent attribute choices — appears directly in:

- **Code formatters and pretty-printers** that must enumerate valid AST node arrangements with style annotations.
- **Test case generators** for parser and compiler validation, where you need all structurally valid token sequences tagged with metadata (types, scopes).
- **Protocol fuzzing frameworks** that generate valid-but-varied message frames to stress-test parsers.
- **UI component tree generators** in design systems, where nesting rules constrain layout but theme tokens are independently assignable.

Without a clean separation of structural constraints from attribute enumeration, these generators either miss valid cases or produce invalid ones at scale.

---

## 🔍 Problem Statement

**Input:** A positive integer `n` (1 ≤ n ≤ 4).  
**Output:** All strings representing valid bracket sequences of `n` pairs where each bracket is colored Red or Blue.

**Notation:** `R(` = red open, `B(` = blue open, `R)` = red close, `B)` = blue close.

**Validity rules:**
- At no prefix may closing brackets exceed opening brackets.
- Total opening brackets = total closing brackets = `n`.

**Examples:**

| Input | Output Size | Notes |
|-------|-------------|-------|
| `n=1` | 4 | One structure `()`, 2² colorings |
| `n=2` | 32 | Two structures (`(())`, `()()`), each with 2⁴ = 16 colorings |

**Key driver:** For `n` pairs, there are `Catalan(n)` valid structures, each with `2^(2n)` colorings, so output size = `C(n) × 4^n`. At `n=4`: 14 × 256 = 3,584 entries.

---

## 🪜 How to Solve This

1. **Recognize two independent axes** → bracket structure (validity) and color (free choice). These can be handled in a single recursive pass rather than generate-then-annotate.

2. **Standard bracket generation** → classic backtracking: track `open` and `close` counts, add `(` when `open < n`, add `)` when `close < open`. This enforces nesting invariants at each step.

3. **Color is a free variable** → at every position where you'd place a bracket, you have two structural choices (`(` or `)`) AND two color choices (`R` or `B`). That's up to four branches per recursive call.

4. **Combine them** → instead of generating structures first and coloring second (two passes, extra memory), emit colored tokens directly. Each recursive branch picks a token from `{R(, B(, R), B)}` subject to the same open/close count constraints.

5. **Base case** → when `open == n` and `close == n`, the current string is complete and valid — record it.

This collapses what looks like a two-phase problem into a single backtracking tree, keeping the implementation clean and the state minimal.

---

## 🧩 Algorithm Walkthrough

**Pattern: Constrained Backtracking with Attribute Decoration**

The core insight is that color is a free dimension — it never affects structural validity. So the branching factor at each node is: (number of valid structural moves) × 2.

**Steps:**

1. **Initialize** with `open=0`, `close=0`, `current=""`, `results=[]`.

2. **At each recursive call**, determine valid structural moves:
   - If `open < n`: can place an open bracket → branch for `R(` and `B(`.
   - If `close < open`: can place a close bracket → branch for `R)` and `B)`.

3. **Recurse** for each valid colored token, incrementing the appropriate counter (`open` or `close`).

4. **Invariant maintained:** At every node, `close ≤ open ≤ n`. This is the same invariant as standard bracket generation — color never violates it.

5. **Terminal condition:** `open == n && close == n` → append `current` to results.

6. **Backtracking** is implicit via string concatenation (no mutation to undo) — each recursive frame receives a new string, so no explicit state rollback is needed.

**Why this abstraction?** Backtracking is correct here because the constraint (nesting validity) is prefix-checkable — you can prune invalid branches immediately rather than generating and filtering. Color adds no constraints, so it multiplies the branching factor without adding pruning logic.

---

## 📊 Worked Example

**Input:** `n = 1`

| Call | `open` | `close` | `current` | Action |
|------|--------|---------|-----------|--------|
| 1 | 0 | 0 | `""` | `open < 1` → branch `R(` and `B(` |
| 2 | 1 | 0 | `"R("` | `close < open` → branch `R)` and `B)` |
| 3 | 1 | 1 | `"R(R)"` | Base case → **emit** |
| 4 | 1 | 1 | `"R(B)"` | Base case → **emit** |
| 5 | 1 | 0 | `"B("` | `close < open` → branch `R)` and `B)` |
| 6 | 1 | 1 | `"B(R)"` | Base case → **emit** |
| 7 | 1 | 1 | `"B(B)"` | Base case → **emit** |

**Result:** `["R(R)", "R(B)", "B(R)", "B(B)"]` — 4 entries, matching `C(1) × 4¹ = 1 × 4`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(C(n) × 4ⁿ × n)** — `C(n)` valid structures, each with `4ⁿ` colorings, and each result string has length `2n` (in tokens). The dominant cost is string construction at leaf nodes. At `n=4`: ~28,672 character operations — trivially fast. This doesn't scale to large `n`, but the constraint `n ≤ 4` makes that irrelevant.

### Space Complexity

**O(C(n) × 4ⁿ × n)** for the result list, which owns the dominant allocation. Recursion depth is `O(2n)` = `O(n)` for the call stack. No memoization structure is needed, and the per-frame string overhead is proportional to depth — not reducible without switching to a mutable buffer with explicit backtracking.

---

## 💡 Key Takeaways

- **Pattern signal — combinatorial enumeration with structural constraints:** When a problem asks for "all valid X with independent attribute Y," backtracking on structure with free branching on attributes is the natural fit — not generate-then-filter.
- **Pattern signal — cross-product output size:** If output size grows as `f(n) × g(n)` where `f` is a Catalan-like count and `g` is a power of a small constant, a single recursive pass can enumerate both dimensions simultaneously.
- **Gotcha — token length vs. character length:** The output strings use multi-character tokens (`R(`, `B)`, etc.), so string length is `4n` characters but `2n` tokens. Off-by-one errors appear if you conflate token count with character index in any post-processing.
- **Gotcha — base case ordering:** Check `open == n && close == n` before checking branching conditions. If you check branch conditions first, you may attempt to recurse with a full string and silently emit nothing.
- **Architectural insight:** Separating structural validity from attribute assignment — enforcing constraints on structure while leaving attributes as free variables — is a broadly applicable design principle in code generators, schema validators, and test data factories. It keeps constraint logic focused and attribute logic composable.

---

## 🚀 Variations & Further Practice

- **Generalize to `k` colors:** Replace the binary color choice with `k` colors per bracket. Output size becomes `C(n) × (2k)^(2n)` — the same backtracking structure holds, but the branching factor scales with `k`, making pruning strategy and output representation more important.
- **Add color-validity constraints:** Require that matching bracket pairs share the same color, or that no two adjacent brackets share a color. This makes color a *constrained* dimension rather than a free one — you now need to propagate color state through the recursion and prune, turning a simple multiplier into a genuine constraint-satisfaction problem.
- **Generate and count without materializing:** For large `n`, compute the count of valid colored sequences without building the list — requires deriving a closed-form recurrence over `(open, close, last_color)` state, which is a standard dynamic programming extension of this backtracking problem.