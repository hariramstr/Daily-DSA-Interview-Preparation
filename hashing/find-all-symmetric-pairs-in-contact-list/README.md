# Find All Symmetric Pairs in a Contact List

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Table, Array, Set

---

## 🗂 Problem Overview

Given a list of directed contact pairs `[caller, receiver]`, find all pairs where both directions exist — i.e., `[A, B]` and `[B, A]` are both present. Each symmetric pair should appear exactly once in the output, normalized so the smaller value leads. The non-trivial constraint: duplicates in the input must not produce duplicate results, requiring deduplication logic layered on top of the symmetry detection.

---

## 🌍 Engineering Impact

This pattern is pervasive in bidirectional relationship modeling. Social graphs (Twitter mutual follows, LinkedIn connections) need exactly this to distinguish directed edges from undirected ones. Network intrusion systems flag symmetric TCP handshake anomalies using the same lookup. Distributed event-correlation pipelines — think Kafka consumers reconciling request/response pairs — rely on hash-based reverse lookups to avoid O(n²) cross-stream joins. Without it, naive nested-loop approaches collapse under even modest data volumes, and missing deduplication produces phantom relationship counts that corrupt downstream analytics.

---

## 🔍 Problem Statement

**Input:** A list of `n` integer pairs `contacts[i] = [caller, receiver]`, each representing a directed call.

**Output:** All unique symmetric pairs `[A, B]` where both `[A, B]` and `[B, A]` exist in the input. Report each pair once with the smaller value first.

**Constraints:**
- `1 <= n <= 10^5`
- `1 <= caller, receiver <= 10^6`
- `caller != receiver`
- Input may contain duplicate pairs

**Examples:**

| Input | Output |
|---|---|
| `[[1,2],[3,4],[2,1],[5,6],[4,3],[7,8]]` | `[[1,2],[3,4]]` |
| `[[10,20],[20,10],[10,20],[30,40]]` | `[[10,20]]` |

The duplicate-pair constraint is the key driver: a set-based deduplication step must precede or accompany the symmetry check, otherwise `[10,20]` appearing twice alongside `[20,10]` could naively emit two results.

---

## 🪜 How to Solve This

1. **Read the problem → notice two distinct sub-problems:** detecting symmetry and suppressing duplicates. Conflating them leads to bugs.

2. **Duplicates first →** if `[A, B]` appears three times, it should behave identically to appearing once. Normalize the input into a set of unique pairs before any symmetry logic.

3. **Symmetry check → think HashMap, not nested loops.** For each pair `[A, B]`, we want O(1) lookup of `[B, A]`. A hash set of pairs gives exactly that.

4. **Avoid double-reporting →** if `[A, B]` is symmetric, processing `[B, A]` later would emit it again. Canonical form (smaller value first) as the output key, combined with a result set, prevents this.

5. **Single pass after deduplication →** iterate the unique-pair set once, check if the reverse exists, emit the canonical form. The whole pipeline is O(n) with no nested loops.

The insight: deduplication and symmetry detection are both O(1)-per-element operations when backed by a hash set — the solution is really just two set lookups per pair.

---

## 🧩 Algorithm Walkthrough

**Pattern: Hash Set Membership Testing**

This is the right abstraction because the problem reduces to repeated "does this element exist?" queries — exactly what hash sets are built for.

**Steps:**

1. **Deduplicate input.** Insert every `[caller, receiver]` pair into a set `seen`. This collapses all duplicate pairs into a single entry and gives O(1) reverse lookup. Invariant: `seen` contains only unique directed pairs.

2. **Initialize result set.** Use a set `result` keyed on canonical pairs `(min(A,B), max(A,B))` to prevent double-reporting. Invariant: no symmetric pair appears in `result` more than once.

3. **Iterate `seen`.** For each pair `[A, B]` in `seen`, check whether `[B, A]` is also in `seen`. This is O(1) per lookup.

4. **Emit canonical form.** If the reverse exists, compute `key = (min(A,B), max(A,B))` and insert into `result`. The canonical form ensures `[1,2]` and `[2,1]` both map to the same output entry.

5. **Return result.** Convert the result set to a list. Order is not specified, so no sort is required.

**Why this works:** The set membership check is the inverse-lookup mechanism. By separating deduplication (step 1) from detection (step 3), each concern is handled cleanly and independently.

---

## 📊 Worked Example

Input: `[[10,20],[20,10],[10,20],[30,40]]`

**Step 1 — Build `seen` set:**

| Pair processed | `seen` after insert |
|---|---|
| `[10, 20]` | `{(10,20)}` |
| `[20, 10]` | `{(10,20), (20,10)}` |
| `[10, 20]` (dup) | `{(10,20), (20,10)}` — no change |
| `[30, 40]` | `{(10,20), (20,10), (30,40)}` |

**Step 2 — Iterate `seen`, check reverse:**

| Current pair | Reverse | In `seen`? | Canonical key | `result` |
|---|---|---|---|---|
| `(10,20)` | `(20,10)` | ✅ | `(10,20)` | `{(10,20)}` |
| `(20,10)` | `(10,20)` | ✅ | `(10,20)` | `{(10,20)}` — no change |
| `(30,40)` | `(40,30)` | ❌ | — | `{(10,20)}` |

**Output:** `[[10, 20]]`

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — building the deduplication set is O(n) with O(1) amortized inserts; the single iteration over unique pairs performs O(1) hash lookups per element. At 10⁶ elements this is comfortably sub-second; at 10⁹ it remains linear but memory pressure (not CPU) becomes the binding constraint.

### Space Complexity

**O(n)** — the `seen` set and `result` set together hold at most `n` entries. No reduction is possible without sacrificing O(1) lookup; trading space for time here would reintroduce O(n²) comparisons.

---

## 💡 Key Takeaways

- **Pattern signal — bidirectional relationship detection:** any time a problem asks "does the reverse of this directed edge exist?", a hash set reverse-lookup is the canonical O(1) answer.
- **Pattern signal — deduplication before logic:** when the problem says "even if duplicates exist, report once," treat deduplication as a preprocessing stage, not an afterthought woven into the main logic.
- **Gotcha — double-reporting without canonical form:** iterating a set that contains both `(A,B)` and `(B,A)` will emit the symmetric pair twice unless you normalize to a canonical key before inserting into the result structure.
- **Gotcha — mutating `seen` during iteration:** if you attempt to remove processed pairs from `seen` to prevent double-reporting instead of using a canonical result set, you risk `ConcurrentModificationException`-class bugs and make the logic harder to reason about.
- **Architectural insight:** the deduplication-then-detect pipeline mirrors the idempotency pattern in distributed systems — normalize inputs to a canonical form before applying business logic, so the logic itself never needs to handle duplicates.

---

## 🚀 Variations & Further Practice

- **Weighted symmetric pairs:** each contact pair carries a call duration. A symmetric pair is only valid if both directions exist *and* the duration difference is within a threshold `k`. The twist: the hash map must store values, not just keys, and the lookup becomes a range check rather than a boolean membership test.
- **Streaming contacts with expiry:** pairs arrive in a real-time stream and a symmetric pair is only valid if both directions are observed within a sliding time window `W`. The twist: the data structure must support TTL-based eviction (e.g., a time-bucketed hash map), turning a static set problem into a stateful streaming one.
- **Transitive symmetry groups:** find all groups of contacts where every member has called every other member (a fully symmetric clique). The twist: single-pair reverse lookup is no longer sufficient — this generalizes to clique detection in a directed graph, requiring adjacency set intersection per node.