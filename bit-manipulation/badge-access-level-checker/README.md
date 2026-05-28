# Badge Access Level Checker

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bitmasking, Math

---

## 🗂 Problem Overview

Given two integers — `badge` (an employee's permission bitmask) and `required` (a bitmask of permissions needed to enter an area) — determine whether the employee holds every required permission and count how many permissions they hold beyond what's required. Output `[allowed, extras]` where `allowed` is `1` if `badge` satisfies all bits in `required`, and `extras` is the popcount of bits set in `badge` but absent from `required`. The constraint driving complexity is that both values can reach 10⁹, so bit-level operations are the only sensible tool.

---

## 🌍 Engineering Impact

This exact pattern — subset membership via bitmask AND — is pervasive in production infrastructure. Linux file permission checks (`rwx` bits), AWS IAM policy evaluation, and OAuth scope validation all reduce to "does this mask contain the required mask as a subset?" At scale, UNIX kernels execute this check on every `open(2)` syscall. In network packet classification (iptables, eBPF), flag-matching across millions of packets per second relies on bitmask intersection because any branching alternative would blow instruction cache. Getting this wrong means either over-granting access (security breach) or under-granting it (availability incident).

---

## 🔍 Problem Statement

**Inputs:**
- `badge`: integer in `[0, 10^9]`, representing an employee's granted permissions as a bitmask.
- `required`: integer in `[0, 10^9]`, representing the minimum permissions needed.

**Output:** `[allowed, extras]`
- `allowed = 1` if `(badge & required) == required`, else `0`.
- `extras = popcount(badge & ~required)` — bits set in `badge` that are not in `required`.

**Examples:**

| badge | required | allowed | extras | Notes |
|-------|----------|---------|--------|-------|
| 29 (`11101`) | 21 (`10101`) | 1 | 1 | Bit 1 is extra |
| 12 (`01100`) | 21 (`10101`) | 0 | 2 | Bits 2,3 are extra; bits 0,4 missing |

**Edge cases:** `required = 0` means any badge is allowed; `badge = 0` with any nonzero `required` is always denied. Both can be 0 simultaneously — result is `[1, 0]`.

---

## 🪜 How to Solve This

1. **Read the access check** → "employee has all required bits" is a classic subset test. The moment you see "every bit in A must also be in B," reach for `(A & B) == A`.

2. **Verify the subset direction** → `required` is the subset we're testing against `badge`. So the check is `(badge & required) == required`. If any required bit is absent in `badge`, the AND clears it and the equality fails.

3. **Count the extras** → "bits in `badge` not in `required`" is the set difference `badge \ required`. In bitmask terms, isolate those bits with `badge & ~required`, then count set bits (popcount).

4. **Popcount strategy** → At this scale (≤ 30 bits), any popcount method works: Brian Kernighan's loop, language built-ins (`Integer.bitCount` in Java, `__builtin_popcount` in C++, `bin(...).count('1')` in Python). Choose the one your language makes idiomatic.

5. **Combine** → Two independent operations, one integer output each. No loops over input required — this is O(1) per query.

The solution crystallizes into two lines of logic once you recognize both operations as standard bitmask primitives.

---

## 🧩 Algorithm Walkthrough

**Pattern: Bitmask Subset Test + Bitmask Set Difference**

**Step 1 — Subset check (`allowed`):**
Compute `badge & required`. This AND zeroes out any bit in `required` that is absent in `badge`. If the result equals `required`, every required bit was present — the employee is allowed. This is correct because AND is idempotent on matching bits and destructive on mismatches. Invariant: the equality holds if and only if `required` is a bitwise subset of `badge`.

**Step 2 — Isolate extra bits:**
Compute `~required` to flip all bits in `required` (invert the required mask). Then `badge & ~required` retains only the bits in `badge` that fall outside the required set. This is the bitwise set difference. Invariant: the result has a 1 only where `badge` has a 1 and `required` has a 0.

**Step 3 — Count extra bits (`extras`):**
Apply popcount to the result of Step 2. Each set bit represents one extra permission. In Python, `bin(badge & ~required).count('1')` is idiomatic. In systems languages, use the hardware intrinsic.

**Step 4 — Return `[allowed, extras]`.**

No loops over bits are required; both operations are constant-time on fixed-width integers. The pattern generalizes directly: any "does entity X satisfy policy Y?" question over flag sets maps to this two-step structure.

---

## 📊 Worked Example

**Input:** `badge = 29` (`11101`), `required = 21` (`10101`)

| Step | Operation | Binary | Decimal |
|------|-----------|--------|---------|
| badge | — | `11101` | 29 |
| required | — | `10101` | 21 |
| `badge & required` | Subset test | `10101` | 21 |
| `== required?` | `21 == 21` → `allowed = 1` | ✓ | — |
| `~required` (low 5 bits) | Invert required | `01010` | 10 |
| `badge & ~required` | Extra bits | `01000` → only bit 1 set... wait: `11101 & 01010 = 01000` | 8 |
| `popcount(01000)` | Count extras | 1 set bit | `extras = 1` |

**Output:** `[1, 1]` ✓

Bit 1 (value 2) is set in `badge` but not in `required` — that's the single extra permission.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(1)** — both the subset test and the popcount operate on fixed-width integers (≤ 30 bits for inputs up to 10⁹). The dominant operation is popcount, which executes in a single CPU instruction on modern hardware. At any scale of queries (10⁶ or 10⁹), each individual check remains constant time; throughput is purely I/O-bound.

### Space Complexity

**O(1)** — no auxiliary data structures are allocated. All intermediate values (`badge & required`, `badge & ~required`) are scalar integers held in registers. There is no space/time trade-off available or necessary here.

---

## 💡 Key Takeaways

- **Pattern signal — subset language:** Phrases like "must have all of," "every flag in A is also in B," or "satisfies all requirements" in a problem involving integers or sets are direct indicators of a bitmask subset test (`(a & b) == b`).
- **Pattern signal — "extras" or "beyond":** Any ask for bits/flags/features present in one set but absent in another maps to bitmask set difference (`a & ~b`) followed by popcount — not iteration.
- **Gotcha — complement width:** `~required` in languages with fixed-width signed integers (Java, C++) produces a 32- or 64-bit complement with leading 1s. Always mask with `badge & ~required`; since `badge` has no spurious high bits, the AND naturally clears them. In Python, integers are arbitrary-precision, so `~required` is negative — use `badge & (badge ^ (badge & required))` or explicitly mask if needed.
- **Gotcha — direction of the subset test:** `(badge & required) == required` checks that `required ⊆ badge`. Reversing to `== badge` tests the opposite direction and is a silent logic error that passes most basic tests.
- **Architectural insight:** Model permission systems as bitmasks from day one. A single integer comparison replaces a loop over permission lists, making policy evaluation branchless and cache-friendly — critical when the check sits in a hot path like a request authentication middleware or a kernel syscall handler.

---

## 🚀 Variations & Further Practice

- **Multi-role access:** An employee holds multiple role badges; they gain entry if the union of all their badges satisfies `required`. This extends the pattern to `(badge1 | badge2 | ... | badgeN) & required == required` — the twist is efficiently computing the union across a dynamic role set and handling role revocation (intersection vs. union semantics).
- **Minimum privilege delta:** Given a set of employees and a required mask, find the employee whose badge requires the fewest additional bits granted to satisfy `required` (i.e., minimize `popcount(required & ~badge)`). This turns a single O(1) check into an O(n) scan with a popcount-based comparator — and at scale motivates bitwise indexing structures like a trie over permission masks.
- **Permission conflict detection:** Given two required masks for two areas, determine whether any badge that satisfies both would necessarily hold a specific dangerous permission (e.g., both server room and lobby simultaneously). This introduces bitmask intersection of requirement sets and reasoning about satisfiability — a stepping stone toward SAT-based policy analysis used in tools like AWS IAM Access Analyzer.