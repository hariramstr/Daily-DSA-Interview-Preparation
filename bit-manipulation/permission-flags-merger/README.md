# Permission Flags Merger

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Array, Simulation

---

## 🗂 Problem Overview

Given two integer arrays `granted` and `revoked` of length `n`, simulate a sequential permission update process on a bitmask initialized to `0`. At each step `i`, OR the current mask with `granted[i]` to add permissions, then AND it with `~revoked[i]` to remove permissions. Return the final mask. The non-trivial constraint is the mutual exclusivity guarantee (`granted[i] & revoked[i] == 0`), which eliminates ambiguity in operation ordering within a single step.

---

## 🌍 Engineering Impact

This exact pattern — accumulating and revoking capability flags via bitwise OR/AND-NOT — appears in Linux file permission systems (`chmod`/`umask`), AWS IAM policy evaluation (allow/deny merging), OAuth scope negotiation, and kernel capability sets (`CAP_*` flags). At scale, representing permission state as a single integer rather than a set of strings reduces ACL evaluation from O(k) string comparisons to O(1) bitwise ops, which matters when authorization checks sit on the hot path of millions of requests per second. Getting the operation order wrong — revoking before granting — produces incorrect privilege escalation or denial bugs that are notoriously hard to reproduce.

---

## 🔍 Problem Statement

Starting from `mask = 0`, process `n` steps in order:
1. `mask = mask | granted[i]` — set all bits present in `granted[i]`
2. `mask = mask & ~revoked[i]` — clear all bits present in `revoked[i]`

Return `mask` after all `n` steps.

**Constraints:**
- `1 <= n <= 10^4`
- `0 <= granted[i], revoked[i] <= 2^30 - 1`
- `granted[i] & revoked[i] == 0` for all `i` (no bit appears in both arrays at the same step)

**Example 1:**
```
granted = [5, 2], revoked = [0, 1]  →  Output: 6
```
**Example 2:**
```
granted = [15, 8, 4], revoked = [0, 3, 8]  →  Output: 4
```

The key algorithmic driver: values fit in 30 bits, so the entire permission state lives in one machine word — no data structure needed beyond a scalar accumulator.

---

## 🪜 How to Solve This

1. **Read the update rule** → each step is two operations on a single integer. There's no dependency between steps except through the accumulated `mask`. This is a pure left-fold over the array pair.

2. **Recognize the state is scalar** → unlike problems that require tracking which *elements* contributed to a result, here the entire system state is one 30-bit integer. No auxiliary structure is needed.

3. **Map the operations to bitwise primitives** → "add permissions" = set bits = OR. "Remove permissions" = clear bits = AND with complement. Both are single CPU instructions.

4. **Verify operation ordering matters** → grant-then-revoke vs. revoke-then-grant produces different results when the same bit is touched in both arrays within a step. The problem specifies grant first, and the mutual-exclusivity constraint (`granted[i] & revoked[i] == 0`) actually makes the order irrelevant *within* a single step — but you should still follow the spec to avoid subtle bugs when that invariant is relaxed in a future variant.

5. **Conclude** → one pass, O(n) time, O(1) space. No edge case beyond `n = 1`.

---

## 🧩 Algorithm Walkthrough

**Pattern: Linear Scan with Scalar Accumulator**

This is the simplest form of a stateful fold — the right abstraction when output state is a fixed-width value updated by each input element in sequence.

**Steps:**

1. **Initialize** `mask = 0`. This represents a user with no permissions. All 30 bits are clear.

2. **Iterate** `i` from `0` to `n - 1`. Process each step in order; the result of step `i` feeds directly into step `i+1`.

3. **Apply grant:** `mask |= granted[i]`. The OR operation sets every bit that is set in `granted[i]`, leaving all other bits unchanged. Invariant: after this line, `mask` contains all previously held permissions plus the newly granted ones.

4. **Apply revocation:** `mask &= ~revoked[i]`. The bitwise NOT flips all bits in `revoked[i]`, producing a mask where the bits-to-clear are `0` and everything else is `1`. ANDing with this clears exactly the revoked bits. Invariant: after this line, none of the bits in `revoked[i]` remain set in `mask`.

5. **Return `mask`** after the loop exits. No post-processing required.

**Why this is correct:** Each operation is idempotent with respect to bits not in its operand. OR only sets; AND-NOT only clears. The mutual-exclusivity constraint means no bit is simultaneously granted and revoked in the same step, eliminating any ambiguity about final state.

---

## 📊 Worked Example

**Input:** `granted = [15, 8, 4]`, `revoked = [0, 3, 8]`

| Step | Operation | `granted[i]` | `revoked[i]` | `mask` (decimal) | `mask` (binary) |
|------|-----------|-------------|-------------|-----------------|----------------|
| Init | —         | —           | —           | 0               | `0000`         |
| 0    | `\|= 15`  | 15          | —           | 15              | `1111`         |
| 0    | `&= ~0`   | —           | 0           | 15              | `1111`         |
| 1    | `\|= 8`   | 8           | —           | 15              | `1111`         |
| 1    | `&= ~3`   | —           | 3           | 12              | `1100`         |
| 2    | `\|= 4`   | 4           | —           | 12              | `1100`         |
| 2    | `&= ~8`   | —           | 8           | 4               | `0100`         |

**Output:** `4` — only bit 2 (EXECUTE) remains set.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — the loop executes exactly `n` iterations, each performing two O(1) bitwise operations on fixed-width integers. At `n = 10^6` this is trivially fast (microseconds); at `n = 10^9` it remains linear but memory bandwidth for loading the arrays becomes the bottleneck, not computation.

### Space Complexity

**O(1)** — only the scalar `mask` accumulator is needed beyond the input arrays. The input arrays themselves are O(n), but if the arrays are streamed (e.g., from a log), even that can be reduced to O(1) working memory with no trade-off.

---

## 💡 Key Takeaways

- **Pattern signal — scalar accumulator over an array:** When the problem asks you to "apply a sequence of operations" and the result state fits in a primitive type, reach for a fold/reduce with no auxiliary structure.
- **Pattern signal — set/clear semantics on bits:** Any time a problem describes "add X to a set, remove Y from a set" where X and Y are disjoint, that's a direct mapping to OR and AND-NOT — no hash set needed.
- **Gotcha — complement operator on unsigned vs. signed integers:** In languages like C/C++, `~revoked[i]` on a signed 32-bit int sets the sign bit. Ensure the mask type is `unsigned int` or use an explicit bitmask (`& 0x3FFFFFFF`) to avoid propagating unwanted high bits.
- **Gotcha — operation order within a step:** Even though the mutual-exclusivity constraint makes grant-then-revoke equivalent to revoke-then-grant *for this input*, always implement the spec's stated order. Future callers may relax that invariant, and a wrong ordering becomes a privilege escalation bug.
- **Architectural insight:** Encoding permission state as a bitmask rather than a collection makes serialization, comparison, and diffing O(1) — a single integer equality check detects whether two users have identical permissions, which is invaluable in caching and policy-change detection at scale.

---

## 🚀 Variations & Further Practice

- **Concurrent permission merging across multiple users:** Given a matrix where each row is a user's `(granted, revoked)` sequence, compute the final mask for each user in parallel, then derive the union mask (OR of all users) and intersection mask (AND of all users). The conceptual twist is reasoning about which aggregation is correct for your access model — union grants too much, intersection may grant too little.
- **Conflict resolution when `granted[i] & revoked[i] != 0`:** Remove the mutual-exclusivity guarantee. Now you must define a precedence rule (revoke wins, or grant wins) and handle the ambiguous bits explicitly — this mirrors real IAM systems where explicit deny overrides allow, requiring a three-state model (granted, revoked, unset) that no longer fits in a single bitmask without encoding tricks.
- **Minimal permission diff:** Given two final permission masks (e.g., before and after a policy change), compute the smallest set of grant/revoke operations needed to transition from one to the other. This extends the pattern into bitmask decomposition and is directly applicable to audit log generation and least-privilege enforcement tooling.