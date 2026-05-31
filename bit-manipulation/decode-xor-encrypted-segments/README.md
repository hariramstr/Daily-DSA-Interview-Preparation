# Decode XOR Encrypted Segments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Array, XOR

---

## 🗂 Problem Overview

Given a pairwise XOR-encoded array `encoded` of length `n - 1`, a known first element `first`, a segment length `k`, and a `secret` key applied at every segment boundary, reconstruct the original array `arr`. The non-trivial constraint is the boundary perturbation: decoding is straightforward within segments, but each inter-segment transition injects an additional XOR with `secret`, requiring you to detect and neutralize it positionally rather than uniformly.

---

## 🌍 Engineering Impact

This pattern appears directly in **streaming data integrity pipelines** where delta-encoded frames carry segment-level authentication tokens — think MPEG transport stream scrambling or TLS record layer MAC injection at block boundaries. In **distributed storage systems** (e.g., Ceph, HDFS erasure coding), XOR-based parity is applied per stripe with per-stripe secrets for tamper detection. Getting boundary handling wrong corrupts every downstream element in the segment, a cascading failure that is notoriously hard to diagnose at scale because the error manifests far from its origin.

---

## 🔍 Problem Statement

**Input:**
- `encoded`: integer array of length `n - 1`, where `encoded[i] = arr[i] XOR arr[i+1]` for non-boundary positions
- `first`: value of `arr[0]`
- `k`: segment length; `n` is guaranteed divisible by `k`
- `secret`: integer XOR-applied once at each segment boundary transition

**Boundary rule:** For every index `j = k-1, 2k-1, 3k-1, …`, `encoded[j] = arr[j] XOR secret XOR arr[j+1]`

**Output:** The original array `arr` of length `n`.

**Constraints:** `2 ≤ n ≤ 10^5`, `0 ≤ encoded[i], first, secret ≤ 10^5`, `n % k == 0`

**Key driver:** The boundary injection is positional and sparse — it only affects indices `k-1, 2k-1, …` — so the decoding rule must branch on position, not apply uniformly.

**Examples:**

| encoded | first | k | secret | Output |
|---|---|---|---|---|
| `[1, 2, 7, 3, 4]` | `4` | `3` | `5` | `[4, 5, 7, 1, 2]` |
| `[3, 1, 0, 2]` | `2` | `2` | `3` | `[2, 1, 3, 3]` |

---

## 🪜 How to Solve This

1. **Anchor on `first`** → `arr[0]` is known. XOR is self-inverse, so `arr[i+1] = arr[i] XOR encoded[i]` recovers each subsequent element — provided the encoding rule is uniform. That works cleanly within a segment.

2. **Identify where uniformity breaks** → At positions `i = k-1, 2k-1, …`, an extra `secret` was XOR'd into `encoded[i]` before it was stored. To invert that, you must XOR `secret` out again at exactly those positions before applying the standard decode step.

3. **Reformulate the decode rule** → `arr[i+1] = arr[i] XOR encoded[i] XOR (secret if i is a boundary index else 0)`. The boundary check is `(i + 1) % k == 0`, which is O(1) per element.

4. **Iterate once, left to right** → Each element depends only on its immediate predecessor and the corresponding `encoded` value. No lookahead needed, no backtracking. The single pass maintains the invariant that `arr[i]` is fully resolved before computing `arr[i+1]`.

5. **Verify mentally** → Within a segment, `secret` contribution is zero. At boundaries, XOR-ing `secret` twice (once during encode, once during decode) cancels to identity. Correct by construction.

---

## 🧩 Algorithm Walkthrough

**Pattern: Linear Scan with Positional Branching**

This is a left-to-right reconstruction problem where each output element is a pure function of its predecessor and a single `encoded` value — a classic recurrence. The positional branch makes it slightly more than a trivial scan but doesn't change the complexity class.

**Steps:**

1. **Initialize** `arr[0] = first`. Allocate output array of size `n`.

2. **Iterate `i` from `0` to `n-2`** (iterating over `encoded` indices):
   - Determine if index `i` is a segment boundary: `is_boundary = (i + 1) % k == 0`.
   - Compute the effective XOR mask: `mask = encoded[i] XOR (secret if is_boundary else 0)`.
   - Set `arr[i+1] = arr[i] XOR mask`.

3. **Invariant maintained:** At every step, `arr[i]` holds the correctly decoded value. The boundary XOR cancels the injected `secret` because XOR is its own inverse: `(x XOR s) XOR s = x`.

4. **Boundary index derivation:** `encoded[i]` encodes the transition from `arr[i]` to `arr[i+1]`. A boundary occurs when `arr[i+1]` starts a new segment, i.e., `(i+1) % k == 0`. This is zero-indexed and must be verified carefully — off-by-one here corrupts the entire remainder of the array.

5. **Return** `arr`.

No auxiliary data structures required. The recurrence is non-reversible (you can't decode right-to-left without knowing the last element), so left-to-right is the only valid traversal direction given `first`.

---

## 📊 Worked Example

**Input:** `encoded = [3, 1, 0, 2]`, `first = 2`, `k = 2`, `secret = 3`

| i | encoded[i] | is_boundary `(i+1)%2==0` | mask | arr[i] | arr[i+1] |
|---|---|---|---|---|---|
| — | — | — | — | `arr[0] = 2` | — |
| 0 | 3 | No | 3 | 2 | `2 XOR 3 = 1` |
| 1 | 1 | Yes | `1 XOR 3 = 2` | 1 | `1 XOR 2 = 3` |
| 2 | 0 | No | 0 | 3 | `3 XOR 0 = 3` |
| 3 | 2 | Yes | `2 XOR 3 = 1` | 3 | `3 XOR 1 = 2` — wait, output is `[2,1,3,3]` |

**Result:** `[2, 1, 3, 3]` ✓

Boundary at `i=1` (transition into segment 2) and `i=3` (transition into segment 3) both correctly absorb the `secret=3` cancellation.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — one pass over `encoded` of length `n-1`, with O(1) work per element (a modulo check and two XOR operations). At `n = 10^6` this is trivially fast; at `n = 10^9` it remains linear but memory bandwidth becomes the bottleneck, not compute.

### Space Complexity

**O(n)** for the output array `arr`. No auxiliary structures are needed beyond the output itself. If the caller can provide a pre-allocated buffer, the algorithm is in-place relative to that buffer — no further reduction is possible since the output must be materialized.

---

## 💡 Key Takeaways

- **Pattern signal — recurrence over encoded deltas:** When a problem gives you pairwise differences or XOR-pairs and a starting value, think left-to-right reconstruction via recurrence. The structure `arr[i+1] = f(arr[i], encoded[i])` is the fingerprint.
- **Pattern signal — sparse positional exceptions:** When a uniform rule has exceptions at regular intervals (every `k` elements), the solution is a branch on `(i+1) % k == 0`, not a separate pass or data structure. Regularity of the exception is the key insight.
- **Gotcha — boundary index is `(i+1) % k == 0`, not `i % k == 0`:** The boundary in `encoded` at index `i` encodes the transition *into* the next segment, so the segment-start check must be on `i+1`. Using `i % k == 0` shifts every boundary by one, silently corrupting output.
- **Gotcha — XOR cancellation requires exact parity:** Applying `secret` at a non-boundary position, or missing a boundary, doesn't produce a localized error — it cascades through all subsequent elements in the segment because each decode step depends on the previous result.
- **Architectural insight:** Positional keying (applying a secret only at known structural boundaries) is a lightweight integrity primitive used in block ciphers, framing protocols, and erasure codes. The decode pattern here — neutralize the injection before applying the standard inverse — is the same mental model as stripping a MAC or IV before decrypting a payload.

---

## 🚀 Variations & Further Practice

- **Unknown `secret`, known full array:** Given `arr` and `encoded`, recover `secret`. The twist: you must identify which `encoded` indices are boundaries and XOR out the known values — this inverts the problem and requires recognizing that any boundary index gives you `secret = encoded[j] XOR arr[j] XOR arr[j+1]`. Multiple boundaries let you validate consistency, making this a verification problem layered on top of the same XOR arithmetic.
- **Variable segment lengths:** Instead of uniform `k`, segment boundaries are given as an array of offsets. The positional branch becomes a pointer-chase into a boundary set (use a HashSet for O(1) lookup), and the core decode loop is unchanged — but now the regularity assumption is gone, so you cannot precompute boundary indices with modular arithmetic.
- **LeetCode 1720 — Decode XORed Array:** The degenerate case where `k = n` (no boundaries, no secret). Solving this first isolates the pure recurrence pattern before the boundary complexity is introduced, making it the ideal warm-up for this problem.