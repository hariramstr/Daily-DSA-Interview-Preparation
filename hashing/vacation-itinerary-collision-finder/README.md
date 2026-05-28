# Vacation Itinerary Collision Finder

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String Hashing, Sliding Window

---

## 🗂 Problem Overview

Given a list of tourist itineraries (ordered lists of city codes) and an integer `k`, identify every pair of tourists who share at least `k` consecutive cities in the same order. The output is all unique pairs `[i, j]` (i < j) sorted by `i` then `j`. The non-trivial constraint: cities can repeat within an itinerary, and a pair qualifies if *any* window of length `k` matches — not just the full sequence.

---

## 🌍 Engineering Impact

This pattern is the backbone of **plagiarism detection** (comparing document n-gram fingerprints), **genomic sequence alignment** (finding shared subsequences across DNA reads), and **log anomaly correlation** (detecting shared event chains across distributed services). At scale — think comparing millions of event streams in a Kafka pipeline — the naive O(n² · L²) cross-comparison collapses. Rolling hash reduces each sequence to a set of fingerprints, turning the problem into a set-intersection query that can be indexed, sharded, and executed in parallel without materializing all pairs.

---

## 🔍 Problem Statement

**Input:** A list of `n` itineraries where each itinerary is an ordered list of city code strings, and an integer `k`.

**Output:** All unique pairs `[i, j]` (i < j) where itinerary `i` and itinerary `j` share at least one common subsequence of exactly `k` consecutive cities in the same order. Pairs sorted ascending by `i`, then `j`.

**Constraints:**
- `2 ≤ itineraries.length ≤ 200`
- `1 ≤ itineraries[i].length ≤ 500`
- `1 ≤ k ≤ 50`
- City codes: uppercase letters, length 1–5; duplicates allowed within an itinerary.

**Key constraint driving the algorithm:** An itinerary of length `L` produces `L - k + 1` windows of length `k`. Comparing raw windows naively is O(k) per comparison. Hashing each window to a fixed-size fingerprint collapses that to O(1) lookup — the critical reduction.

**Examples:**

```
itineraries = [["NYC","LAX","CHI","MIA"],["SEA","NYC","LAX","CHI"],["BOS","MIA","DFW"]], k = 3
→ [[0, 1]]   // "NYC","LAX","CHI" is shared

itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]], k = 2
→ [[0,1],[0,2],[1,2]]
```

---

## 🪜 How to Solve This

1. **Read the problem** → we need to detect *shared substructure* between pairs of sequences. This is a membership/lookup problem, not a sorting problem.

2. **Naive path** → for each pair (i, j), slide a window of size `k` over both itineraries and compare. That's O(n² · L · k) — too slow and verbose.

3. **Key insight** → if we hash every length-`k` window in each itinerary into a fingerprint, the question "do itinerary A and B share a window?" becomes "do their fingerprint *sets* intersect?" Set intersection is O(min(|A|, |B|)) with a hash set.

4. **Rolling hash** → computing each window's hash from scratch is O(k). A rolling (Rabin-Karp style) hash reuses the previous window's computation, making each step O(1) after the first window — reducing per-itinerary hashing to O(L).

5. **Collision risk** → string hashing has collision probability. Mitigate with a strong base/mod pair or by double-hashing. At these constraint sizes (L ≤ 500, n ≤ 200), a single well-chosen hash is sufficient, but note the trade-off.

6. **Assemble result** → for each pair (i, j), check if `hash_sets[i] ∩ hash_sets[j]` is non-empty. Collect qualifying pairs, sort, return.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Rolling Hash (Rabin-Karp) + Set Intersection

**Step 1 — Canonicalize windows into strings.**
Join each length-`k` window into a single delimiter-separated string (e.g., `"NYC|LAX|CHI"`). This avoids hash collisions caused by city codes of varying length concatenating ambiguously (`"AB"+"C"` vs `"A"+"BC"`). The delimiter guarantees unique encoding.

**Step 2 — Build fingerprint sets.**
For each itinerary `i` of length `L`, slide a window from index `0` to `L - k`. At each position, construct the canonical string for the window and insert it into a set `S[i]`. This runs in O(L · k) per itinerary using simple string joins, or O(L · C) with a rolling hash where `C` is average city code length.

**Step 3 — Pairwise set intersection.**
For every pair `(i, j)` with `i < j`, check whether `S[i] ∩ S[j]` is non-empty. In Python, this is `S[i] & S[j]`. Short-circuit on the first common element found. Worst case is O(min(|S[i]|, |S[j]|)) per pair.

**Step 4 — Collect and sort results.**
Append qualifying `[i, j]` pairs to the result list. Since we iterate `i` from 0 upward and `j` from `i+1` upward, pairs are naturally generated in sorted order — no explicit sort needed.

**Invariant maintained:** Every hash set `S[i]` contains exactly the canonical fingerprints of all length-`k` windows in itinerary `i`. Intersection non-emptiness is both necessary and sufficient for a collision.

---

## 📊 Worked Example

**Input:** `itineraries = [["A","B","C"],["B","C","D"],["A","B","C","D"]]`, `k = 2`

**Step 1 — Build window sets:**

| Itinerary | Windows (k=2)         | Set S[i]                        |
|-----------|-----------------------|---------------------------------|
| 0         | [A,B], [B,C]          | `{"A\|B", "B\|C"}`              |
| 1         | [B,C], [C,D]          | `{"B\|C", "C\|D"}`              |
| 2         | [A,B], [B,C], [C,D]   | `{"A\|B", "B\|C", "C\|D"}`      |

**Step 2 — Pairwise intersections:**

| Pair  | S[i] ∩ S[j]       | Collision? |
|-------|--------------------|------------|
| (0,1) | `{"B\|C"}`         | ✅ Yes     |
| (0,2) | `{"A\|B", "B\|C"}` | ✅ Yes     |
| (1,2) | `{"B\|C", "C\|D"}` | ✅ Yes     |

**Output:** `[[0,1],[0,2],[1,2]]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n² · L · C + n · L · k)** where `n` = number of itineraries, `L` = max itinerary length, `C` = average city code length, `k` = window size. The dominant term is window set construction: `n · L · k` for joining strings. Pairwise intersection is `O(n² · L)`. At the given constraints (n=200, L=500, k=50), this is comfortably under 10⁸ operations. At n=10⁶ itineraries, the n² term makes this approach infeasible — you'd need an inverted index mapping fingerprints to itinerary IDs.

### Space Complexity

**O(n · L)** for storing all fingerprint sets — each of the `n` itineraries contributes at most `L - k + 1` entries. The sets own this space. You cannot reduce it without sacrificing O(1) lookup; a streaming approach could reduce to O(L) per pair but reintroduces O(n²) passes.

---

## 💡 Key Takeaways

- **Pattern signal — "shared substructure across pairs":** Whenever a problem asks whether any two sequences share a common contiguous chunk, think fingerprint sets + intersection before reaching for nested substring search.
- **Pattern signal — fixed-length sliding window over strings:** Fixed `k` + ordered sequence + equality check is the canonical setup for Rabin-Karp. If `k` were variable, you'd need suffix arrays or generalized suffix automata.
- **Gotcha — delimiter in canonical string:** Joining `["A","BC"]` and `["AB","C"]` without a delimiter produces the same string `"ABC"`. Always use a separator character that cannot appear in city codes (e.g., `|`) to prevent false positives.
- **Gotcha — window count off-by-one:** A sequence of length `L` has `L - k + 1` valid windows, not `L - k`. When `L < k`, the itinerary produces zero windows — handle this explicitly or the range will silently produce no iterations.
- **Architectural insight:** The fingerprint-set-per-entity pattern generalizes to an *inverted index*: map each fingerprint to the list of entities that contain it. This flips the query from "for each pair, do they intersect?" to "for each fingerprint, which entities share it?" — enabling sub-linear pair detection at scale, which is how systems like Copyscape and BLAST operate at production volume.

---

## 🚀 Variations & Further Practice

- **Variable-length collision (k is a minimum, not exact):** Find pairs sharing *any* common subsequence of length *at least* `k`. A single hash set no longer works — you need suffix arrays or a generalized suffix automaton to efficiently enumerate all shared substrings above the threshold, adding significant structural complexity.
- **Streaming itineraries (online detection):** Tourists' itineraries arrive as streams; report collisions as soon as they're detected without storing full itineraries. The twist: you must maintain rolling hash state per active stream and query a shared fingerprint-to-tourist inverted index on each new window — turning a batch problem into a stateful stream-processing design with eviction and concurrency concerns.
- **Top-K most similar pairs:** Instead of binary collision detection, rank all pairs by the *count* of shared windows. This requires moving from set intersection (non-empty?) to set intersection *cardinality*, and at scale demands approximate methods like MinHash/LSH to avoid O(n²) exact comparisons.