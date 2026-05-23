# Playlist Song Popularity Tracker

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Sorting, Counting

---

## 🗂 Problem Overview

Given a log of song plays as a flat list of strings, return only the titles that appear more than once, sorted by descending play count with alphabetical tiebreaking. The core contract: frequency aggregation followed by a compound sort. What makes it non-trivial is the dual-key sort — count descending, title ascending — which requires a custom comparator and a clear understanding of sort stability versus explicit key ordering.

---

## 🌍 Engineering Impact

This pattern is the backbone of any real-time leaderboard, trending-content feed, or frequency-ranked autocomplete system. Spotify's "Trending Now," YouTube's view-count ranking, and Redis-backed rate limiters all reduce to the same primitive: aggregate counts into a hash map, filter by threshold, emit sorted results. At scale, the hash map becomes a distributed counter (e.g., Redis `INCR`, Flink keyed aggregations, or DynamoDB atomic counters). Without the frequency-first aggregation step, you're forced into repeated full-table scans — O(n²) per query — which collapses under streaming ingestion rates above a few thousand events per second.

---

## 🔍 Problem Statement

**Input:** `plays` — a list of strings where each element is a song title played once.
**Output:** A list of song titles played **more than once**, sorted by play count **descending**; ties broken **alphabetically ascending**.

**Constraints:**
- `1 <= plays.length <= 10^4`
- `1 <= plays[i].length <= 50`
- `plays[i]` consists of lowercase English letters and spaces
- Song titles are case-sensitive

**Examples:**

| Input | Output |
|---|---|
| `["shape of you", "blinding lights", "shape of you", "blinding lights", "shape of you", "levitating"]` | `["shape of you", "blinding lights"]` |
| `["song a", "song b", "song c", "song b", "song a", "song d", "song d"]` | `["song a", "song b", "song d"]` |

The constraint driving the algorithmic choice: you need exact frequency counts per unique title, which mandates a hash map — any comparison-based grouping would cost at least O(n log n) before you even start counting.

---

## 🪜 How to Solve This

1. **Read the problem → the ask is frequency aggregation, not just sorting.** The output depends on how many times each title appears, so you need counts before you can filter or sort anything.

2. **Counting by key → HashMap.** The key is the song title (exact string), the value is an integer counter. One pass through `plays`, incrementing on each hit. O(n) time, O(u) space where u is unique titles.

3. **Filter before sorting.** Drop any entry with count ≤ 1. Sorting is O(u log u) — the smaller u is when you enter the sort, the faster it runs. Never sort data you're about to discard.

4. **Compound sort key → `(-count, title)`.** Descending count maps naturally to negating the integer in a tuple key. Ascending alphabetical is the default string comparison. Most standard library sort functions accept a tuple key and handle this cleanly without a custom comparator class.

5. **Return titles only.** The counts were a means to an end — strip them from the output. The result is a clean list of strings.

The chain is linear: one scan to count → one filter → one sort → one projection. Each step has a single responsibility.

---

## 🧩 Algorithm Walkthrough

**Pattern: Hash Map Counting + Compound Sort**

This is the canonical "frequency map" pattern: use a hash map to reduce a multiset to a set of (element, count) pairs, then operate on the reduced structure.

**Step 1 — Build the frequency map.**
Iterate through `plays` once. For each title, increment its count in the map. This maintains the invariant: after processing index `i`, `freq[title]` equals the exact number of times `title` appeared in `plays[0..i]`. O(n) time.

**Step 2 — Filter to candidates.**
Iterate over map entries, retaining only those where count > 1. This is correct because the problem explicitly excludes songs played exactly once. Filtering here reduces the sort input size, which matters when the play log is large but the number of popular songs is small (a common real-world distribution).

**Step 3 — Sort with a compound key.**
Sort the filtered entries by `(-count, title)`. Negating count converts a descending-count sort into an ascending sort, letting you use a single stable sort call. The tuple comparison short-circuits: title is only compared when counts are equal, which is exactly the tiebreaker semantics required.

**Step 4 — Project to titles.**
Extract only the title from each sorted entry. The count was scaffolding — it doesn't belong in the output.

**Why not sort first, then count?** Sorting the raw play log costs O(n log n) and doesn't give you counts directly — you'd still need a linear scan over the sorted array. The hash map approach is O(n) for counting and O(u log u) for sorting, where u ≤ n. It's strictly no worse and often significantly better.

---

## 📊 Worked Example

Input: `["song a", "song b", "song c", "song b", "song a", "song d", "song d"]`

**Step 1 — Frequency map construction:**

| After processing index | freq map state |
|---|---|
| 0 (`"song a"`) | `{song a: 1}` |
| 1 (`"song b"`) | `{song a: 1, song b: 1}` |
| 2 (`"song c"`) | `{song a: 1, song b: 1, song c: 1}` |
| 3 (`"song b"`) | `{song a: 1, song b: 2, song c: 1}` |
| 4 (`"song a"`) | `{song a: 2, song b: 2, song c: 1}` |
| 5 (`"song d"`) | `{song a: 2, song b: 2, song c: 1, song d: 1}` |
| 6 (`"song d"`) | `{song a: 2, song b: 2, song c: 1, song d: 2}` |

**Step 2 — Filter (count > 1):** `[(song a, 2), (song b, 2), (song d, 2)]` — `song c` dropped.

**Step 3 — Sort by `(-count, title)`:** All counts equal at 2, so sort falls through to alphabetical → `["song a", "song b", "song d"]`.

**Output:** `["song a", "song b", "song d"]` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n + u log u)** where n is `plays.length` and u is the number of unique titles with count > 1. The O(n) term dominates for the counting pass; O(u log u) covers the sort. At 10⁶ plays with 10⁴ unique popular titles, this is effectively linear. At 10⁹ plays, the counting pass becomes the bottleneck and maps naturally onto a distributed streaming aggregation.

### Space Complexity

**O(u)** where u is the number of unique song titles. The frequency map owns all allocated space. This cannot be reduced below O(u) without losing the ability to do exact counting — any streaming approximation (e.g., Count-Min Sketch) trades space for accuracy, which this problem's exact-output requirement doesn't permit.

---

## 💡 Key Takeaways

- **Pattern signal — "more than once" or "at least k times":** Any threshold-based frequency filter on a collection is a hash map counting problem. The moment you see "how many times does X appear," reach for a frequency map before considering any other structure.
- **Pattern signal — compound sort with mixed directions:** When a sort has two keys with opposite ordering directions (descending count, ascending name), a negated-integer tuple key is cleaner and less error-prone than a custom comparator with explicit `cmp` logic.
- **Gotcha — case sensitivity:** The problem states titles are case-sensitive, but `plays[i]` consists only of lowercase letters and spaces, so there's no normalization needed here. In a real system, failing to normalize before hashing is a silent correctness bug — "Shape Of You" and "shape of you" would count separately.
- **Gotcha — filter before sort, not after:** Sorting the full frequency map and then slicing is wasteful. Filter to count > 1 first; the sort input is often an order of magnitude smaller, especially under a power-law play distribution.
- **Architectural insight:** The frequency map → filter → sort pipeline is a micro-implementation of the MapReduce model: map (count per key), filter (threshold), reduce (sort and project). Recognizing this abstraction makes it straightforward to scale: replace the in-memory map with a distributed counter store and the sort with a top-K heap for large-scale streaming deployments.

---

## 🚀 Variations & Further Practice

- **Top-K most played songs:** Instead of filtering by a count threshold, return only the K highest-frequency titles. The conceptual twist: a full sort is O(u log u), but a min-heap of size K gives you O(u log K) — a meaningful win when K ≪ u. This is the foundation of real-time leaderboard systems.
- **Sliding window play counts:** Count plays only within the last T seconds (e.g., "trending in the last hour"). The hash map now needs to expire entries, which introduces a time-bucketed counter or a deque-based sliding window — the static counting approach breaks entirely, and you need a fundamentally different data structure.
- **Weighted play counts:** Different events carry different weights (full listen vs. skip vs. repeat). Each map entry becomes a float accumulator rather than an integer counter, and the filter threshold becomes a minimum weighted score. This mirrors how recommendation engines score engagement signals rather than raw play counts.