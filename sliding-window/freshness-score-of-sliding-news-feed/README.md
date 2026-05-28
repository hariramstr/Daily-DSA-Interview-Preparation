# Freshness Score of a Sliding News Feed

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Math

---

## 🗂 Problem Overview

Given an array of non-negative integer scores and a fixed window size `k`, find the maximum average value across all contiguous subarrays of exactly length `k`. The core contract: one pass over the array, one floating-point result. The non-trivial constraint is that `scores.length` reaches `10^5`, making any O(n·k) recomputation approach wasteful — the window overlap is the signal to exploit.

---

## 🌍 Engineering Impact

This pattern is the backbone of any fixed-interval aggregation over a stream: sliding-window rate limiters (token bucket counters over the last N requests), time-series anomaly detection (CPU/memory baselines in observability platforms like Datadog or Prometheus), search ranking pipelines that score document freshness over a rolling corpus window, and real-time recommendation engines computing engagement velocity. Without O(n) incremental updates, recalculating from scratch at each step causes latency spikes proportional to window size — catastrophic at millions of events per second where even microseconds of per-event overhead compound into SLA violations.

---

## 🔍 Problem Statement

**Input:** Integer array `scores` (`1 ≤ scores.length ≤ 10^5`, `0 ≤ scores[i] ≤ 10^4`) and integer `k` (`1 ≤ k ≤ scores.length`).

**Output:** Maximum average of any contiguous subarray of exactly length `k`, accepted within `10^-5` tolerance.

**Examples:**

| Input | k | Output |
|---|---|---|
| `[3, 7, 2, 9, 4, 6, 1]` | 3 | `6.66667` — window `[9,4,6]`, sum=19 |
| `[5, 5, 5, 5]` | 2 | `5.00000` — all windows equal |

**Edge cases to consider:** `k == scores.length` (single window, no sliding needed); all scores identical (every window ties); a single maximum element surrounded by zeros (window placement matters). The constraint driving algorithmic choice: adjacent windows share `k-1` elements — recomputing sum from scratch discards that overlap entirely.

---

## 🪜 How to Solve This

1. **Read the problem** → we need the maximum *average* over a fixed-length window. Average is monotonically proportional to sum, so maximizing sum is equivalent — defer the division to the end.

2. **Observe the overlap** → window `[i, i+k-1]` and window `[i+1, i+k]` share `k-1` elements. Recomputing the sum from scratch each time throws away `k-1` operations of work already done.

3. **Incremental update** → instead of `sum = sum of k elements`, think `new_sum = old_sum - scores[i] + scores[i+k]`. One subtraction, one addition — O(1) per slide.

4. **Seed the first window** → compute the initial sum over `scores[0..k-1]` in O(k), then slide from index `1` to `n-k`, updating the running sum and tracking the maximum.

5. **Return max_sum / k** → a single division at the end. The pattern is textbook **Sliding Window** — fixed-size variant, no shrink/expand logic needed, which keeps the implementation minimal.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Fixed-Size Sliding Window. The right abstraction because window size is constant — no need for the two-pointer expand/contract logic used in variable-size variants.

**Steps:**

1. **Guard:** If `k > scores.length`, the problem constraints prevent this, but defensive code returns `0.0`.

2. **Initialize window sum:** Iterate `i` from `0` to `k-1`, accumulating `window_sum`. This seeds the first valid window. **Invariant:** `window_sum` always holds the sum of exactly `k` consecutive elements ending at the current right boundary.

3. **Set baseline:** `max_sum = window_sum`. This handles the edge case where `k == n` — the loop below never executes, and we still return the correct answer.

4. **Slide the window:** For each index `i` from `1` to `n-k` (inclusive):
   - Subtract the element leaving the window: `scores[i - 1]`
   - Add the element entering the window: `scores[i + k - 1]`
   - Update `max_sum = max(max_sum, window_sum)`
   - **Invariant maintained:** window always covers exactly `k` elements; no element is counted twice or skipped.

5. **Return:** `max_sum / k` as a float. Dividing once at the end avoids floating-point accumulation across `n` iterations.

**Why this is correct:** Every contiguous subarray of length `k` is visited exactly once. The incremental update is algebraically equivalent to a full recomputation — it just reuses shared prefix work.

---

## 📊 Worked Example

Input: `scores = [3, 7, 2, 9, 4, 6, 1]`, `k = 3`

| Step | Action | window_sum | max_sum | Window |
|------|--------|-----------|---------|--------|
| Init | Sum indices 0–2 | 12 | 12 | `[3,7,2]` |
| i=1 | −scores[0]=3, +scores[3]=9 | 18 | 18 | `[7,2,9]` |
| i=2 | −scores[1]=7, +scores[4]=4 | 15 | 18 | `[2,9,4]` |
| i=3 | −scores[2]=2, +scores[5]=6 | **19** | **19** | `[9,4,6]` |
| i=4 | −scores[3]=9, +scores[6]=1 | 11 | 19 | `[4,6,1]` |

**Result:** `19 / 3 = 6.66667` ✓

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — the initialization loop runs `k` iterations; the slide loop runs `n-k` iterations; total is exactly `n` operations. At `10^6` elements this is sub-millisecond on modern hardware. At `10^9` elements it remains linear, though memory bandwidth becomes the bottleneck, not compute.

### Space Complexity

**O(1)** — only scalar variables (`window_sum`, `max_sum`, loop indices) are allocated regardless of input size. The input array is read in-place with no auxiliary data structure. No trade-off exists here; this is already optimal.

---

## 💡 Key Takeaways

- **Pattern signal — fixed window size:** When a problem specifies "exactly k consecutive elements" and asks for an aggregate (sum, average, max), that's the fixed-size sliding window trigger — no dynamic resizing needed.
- **Pattern signal — adjacent overlap:** If you catch yourself writing a nested loop where the inner loop re-traverses elements the outer loop already visited, check whether adjacent windows share a suffix/prefix — that overlap is the sliding window optimization waiting to happen.
- **Gotcha — off-by-one in slide bounds:** The slide loop runs from `i=1` to `i=n-k` inclusive. A common mistake is `i < n-k` (exclusive), which skips the last valid window. Always verify the final window index: `[n-k, n-1]`.
- **Gotcha — integer overflow:** `scores[i] ≤ 10^4` and `k ≤ 10^5` means maximum sum is `10^9`, which fits in a 32-bit signed integer — barely. Use `long`/`int64` for `window_sum` defensively, especially if constraints are relaxed in a follow-up.
- **Architectural insight:** Deferring the division to a single operation after the loop is the production-correct pattern — it avoids floating-point drift from repeated division and mirrors how streaming aggregation systems (Flink, Kafka Streams) accumulate integer counters and emit rates only at reporting boundaries.

---

## 🚀 Variations & Further Practice

- **Variable-size window (LeetCode 209 — Minimum Size Subarray Sum):** The window must now shrink dynamically when a condition is met, requiring the full two-pointer expand/contract pattern. The conceptual twist: you lose the fixed-index arithmetic and must manage both left and right pointers explicitly.
- **Maximum average with length ≥ k (LeetCode 644 — Maximum Average Subarray II):** The window size is no longer fixed — it can be any length ≥ k. This breaks the O(1) slide update and requires binary search on the answer combined with prefix-sum validation, pushing complexity to O(n log(max−min)).
- **Sliding window over a data stream with evictions (e.g., moving average from a data stream, LeetCode 346):** Data arrives incrementally with no random access, requiring a circular buffer or deque to maintain the window. The twist is managing bounded memory while preserving O(1) amortized updates — the exact model used in production time-series databases.