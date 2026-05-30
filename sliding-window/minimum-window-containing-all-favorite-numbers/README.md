# Minimum Window Containing All Favorite Numbers

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview

Given an integer array `nums` and a set of distinct integers `favorites`, find the length of the shortest contiguous subarray that contains every element in `favorites` at least once. The non-trivial constraint is that the window must satisfy a multi-element coverage requirement simultaneously — ruling out simple prefix/suffix scans — while the array can be up to 10⁵ elements with favorites up to size 100, demanding a sub-quadratic approach.

---

## 🌍 Engineering Impact

This pattern is the backbone of **stream-based coverage problems** at scale. In log analysis pipelines (Splunk, Datadog), you need the shortest time window containing all required event types — a direct isomorphism. In search ranking, finding the tightest document span containing all query terms drives snippet extraction quality. In network intrusion detection, identifying the minimal packet sequence containing all attack signatures determines alert latency. Without the sliding window approach, a naïve O(n²) scan collapses under high-throughput ingestion, breaking real-time SLA guarantees.

---

## 🔍 Problem Statement

**Input:** An integer array `nums` (length 1–10⁵, values 1–10⁶) and a list of distinct integers `favorites` (length 1–100, values 1–10⁶).

**Output:** The length of the shortest contiguous subarray of `nums` containing every element of `favorites` at least once. Return `-1` if no such subarray exists.

**Examples:**

| Input | Output | Notes |
|---|---|---|
| `nums=[4,1,3,2,1,5,3,2]`, `favorites=[1,3,2]` | `3` | `nums[1..3] = [1,3,2]` covers all three |
| `nums=[7,2,5,1,8]`, `favorites=[3,6]` | `-1` | Neither 3 nor 6 appears in `nums` |

**Key constraint driving the algorithm:** `favorites.length ≤ 100` means the "satisfied" state is cheap to track with a counter, but `nums.length` up to 10⁵ demands a single linear pass — ruling out re-scanning from every index.

---

## 🪜 How to Solve This

1. **Read the problem → recognize "shortest subarray containing all of X."** This is a classic coverage minimization over a contiguous range. The word "contiguous" immediately signals a window-based approach rather than a greedy or sort-based one.

2. **Contiguous + shrinkable bounds → Two Pointers / Sliding Window.** We need the tightest window satisfying a condition. The condition is monotone: expanding the right pointer can only add coverage, shrinking the left pointer can only remove it. That monotonicity is the prerequisite for sliding window correctness.

3. **Tracking "have we covered everything?" → a frequency HashMap + a deficit counter.** Rather than re-checking all favorites on every window change, maintain a `need` map of required counts and a single integer `missing` representing how many distinct favorites are still unsatisfied. This reduces the coverage check to O(1) per pointer move.

4. **Shrink aggressively once satisfied.** When `missing == 0`, record the window length, then advance the left pointer to find a potentially smaller valid window. Stop shrinking when coverage breaks, then resume expanding right.

5. **Result:** One pass, O(n) time. The answer is the minimum recorded window length, or `-1` if `missing` never reached zero.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Sliding Window with a Hash Map coverage counter.

**Why this abstraction?** The window's validity is a function of its contents, not its position. As the window expands/contracts, validity changes incrementally — exactly what two pointers exploit. The HashMap lets us track that incremental state in O(1) per step.

**Steps:**

1. **Build `need` map.** Populate `need[f] = 1` for each `f` in `favorites`. Set `missing = len(favorites)`. Elements not in `favorites` are irrelevant — the map acts as a filter.

2. **Initialize pointers.** Set `left = 0`, `min_len = ∞`.

3. **Expand right pointer** across `nums`. For each `nums[right]`:
   - If it's in `need`, decrement `need[nums[right]]`. If that count drops from 1 to 0, decrement `missing` — this favorite is now satisfied.

4. **Shrink left while `missing == 0`.** The window is valid; try to minimize it:
   - Update `min_len = min(min_len, right - left + 1)`.
   - If `nums[left]` is in `need`, increment `need[nums[left]]`. If it rises from 0 to 1, increment `missing` — coverage is broken.
   - Advance `left`.

5. **Invariant maintained:** `missing == 0` iff the current window contains all favorites.

6. **Return** `min_len` if it was updated, else `-1`.

Each element is added and removed from the window at most once, giving O(n) total pointer moves.

---

## 📊 Worked Example

`nums = [4, 1, 3, 2, 1, 5, 3, 2]`, `favorites = [1, 3, 2]`  
Initial state: `need = {1:1, 3:1, 2:1}`, `missing = 3`, `min_len = ∞`

| right | nums[right] | need after expand | missing | Action | left | Window len | min_len |
|---|---|---|---|---|---|---|---|
| 0 | 4 | `{1:1,3:1,2:1}` | 3 | expand | 0 | — | ∞ |
| 1 | 1 | `{1:0,3:1,2:1}` | 2 | expand | 0 | — | ∞ |
| 2 | 3 | `{1:0,3:0,2:1}` | 1 | expand | 0 | — | ∞ |
| 3 | 2 | `{1:0,3:0,2:0}` | 0 | **shrink** | 0 | 4 | 4 |
| — | shrink left=0 (4, not in need) | unchanged | 0 | shrink | 1 | 3 | **3** |
| — | shrink left=1 (1, need[1]: 0→1) | `{1:1,...}` | 1 | stop shrink | 2 | — | 3 |
| 4–7 | ... | further windows ≥ 3 | — | — | — | — | **3** |

**Output: 3**

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — each element in `nums` is visited at most twice: once when `right` advances over it, once when `left` passes it. The HashMap lookups are O(1) average. At 10⁵ elements this is trivially fast; even at 10⁸ it remains practical as a single-pass stream operation.

### Space Complexity

**O(f)** where `f = len(favorites)` — the `need` HashMap holds at most `f` entries (capped at 100 per constraints). This is effectively O(1) given the constraint. No reduction trade-off is meaningful here; the map is the algorithm's core state.

---

## 💡 Key Takeaways

- **Pattern signal — "shortest/minimum subarray/substring containing all of X":** this phrasing is the canonical trigger for sliding window with a coverage counter. If you see it in a code review or interview, the O(n²) nested loop is always wrong.
- **Pattern signal — monotone validity:** if expanding a window can only improve (or maintain) validity and shrinking can only degrade it, two pointers are applicable. Verify this monotonicity before committing to the pattern.
- **Gotcha — elements not in `favorites` must be ignored silently:** checking `if nums[i] in need` before any update is mandatory. Updating counts for irrelevant elements corrupts `missing` and produces wrong answers.
- **Gotcha — the `missing` counter tracks distinct unsatisfied favorites, not total missing occurrences:** decrement `missing` only when a count crosses zero (satisfied), not on every frequency decrement. Off-by-one here is the most common bug in implementations.
- **Architectural insight:** the `need` map + `missing` counter pattern is a general-purpose "multi-condition satisfaction tracker" — the same structure applies to rate-limiting across multiple resource dimensions, multi-metric SLO compliance windows, and distributed saga completion tracking, anywhere you need O(1) aggregate state over a changing set of conditions.

---

## 🚀 Variations & Further Practice

- **Minimum Window Substring (LeetCode #76):** Characters replace integers and duplicates in `favorites` are allowed (e.g., need two `'a'`s). The twist: `need` values can exceed 1, so the `missing` decrement condition becomes `need[c] > 0` before decrement — a subtle but breaking change to the counter logic.
- **Smallest Range Covering Elements from K Lists (LeetCode #632):** Instead of one array with a target set, you have K sorted lists and must find the smallest range containing at least one element from each. The conceptual leap: you merge all lists into a single event stream tagged by source, then apply the same sliding window — but now the window must cover K distinct sources, and the "shrink" step requires a min-heap to efficiently find the new left boundary.
- **Longest Subarray with All Favorites Appearing Exactly Once:** Flip the objective from minimum to maximum and add an upper-bound constraint (no element can appear more than once). This forces tracking both a lower and upper validity bound simultaneously, breaking the simple two-pointer shrink strategy and requiring a more careful window invalidation approach.