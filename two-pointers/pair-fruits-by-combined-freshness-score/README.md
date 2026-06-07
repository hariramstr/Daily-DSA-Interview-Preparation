# Pair Fruits by Combined Freshness Score

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Array, Greedy

---

## 🗂 Problem Overview

Given a sorted array of integer freshness scores and a target sum `k`, find the maximum number of non-overlapping pairs whose scores sum to exactly `k`. Each element participates in at most one pair. The non-trivial constraint is maximizing pair count — a greedy claim must hold that consuming the outermost valid pair never forfeits a better pairing opportunity, which requires justification before trusting the approach.

---

## 🌍 Engineering Impact

This pattern surfaces anywhere you need to optimally match two resource pools under a combined-value constraint. Examples: matching buy/sell orders in a trading engine where combined price hits a target band; pairing upload/download bandwidth allocations in a network scheduler; matching request latency budgets across microservice call chains. At scale, the O(n) two-pointer scan is what keeps these operations inside a single-pass pipeline stage rather than requiring a nested join that blows up at 10⁵–10⁶ events per second.

---

## 🔍 Problem Statement

**Input:** A sorted (non-decreasing) integer array `freshness` of length `n` and an integer `k`.

**Output:** The maximum count of non-overlapping index pairs `(i, j)` where `i < j` and `freshness[i] + freshness[j] == k`.

**Constraints:**
- `2 <= n <= 10^5`
- `1 <= freshness[i] <= 100`
- `1 <= k <= 200`
- Array is pre-sorted in non-decreasing order

**Examples:**

| Input | k | Output |
|---|---|---|
| `[1, 2, 3, 4, 5, 6, 7]` | `8` | `3` — pairs: (1,7),(2,6),(3,5) |
| `[1, 1, 2, 3, 4, 4]` | `5` | `2` — pairs: (1,4),(1,4) |

**Key driver:** The pre-sorted order eliminates the need for a hash structure and makes the greedy convergence argument straightforward — the algorithmic choice follows directly from the sorted precondition.

---

## 🪜 How to Solve This

1. **Read the constraint** → array is already sorted. Sorted arrays with pair-sum targets are the canonical two-pointer setup. Resist the urge to reach for a hash map; you don't need O(n) extra space here.

2. **Model the search space** → place a pointer at each end. Their sum is either too small, too large, or exactly `k`. This gives you a deterministic move rule with no ambiguity.

3. **Justify the greedy** → if `freshness[left] + freshness[right] == k`, consuming both is always at least as good as skipping either. Skipping the left element means it can only pair with something ≥ `freshness[right]`, which would overshoot `k`. Skipping the right is symmetric. So consuming the match is strictly optimal.

4. **Handle the sum-too-small case** → increment `left` to increase the sum. Handle sum-too-large → decrement `right`. These moves are forced, not heuristic.

5. **Termination** → pointers cross when no valid pairs remain. Count is the answer. The whole reasoning chain takes about 30 seconds once you notice the sorted precondition.

---

## 🧩 Algorithm Walkthrough

**Pattern:** Two Pointers (converging from both ends)

**Why this abstraction fits:** A sorted array with a fixed-sum target creates a monotone relationship — moving left pointer right strictly increases the sum; moving right pointer left strictly decreases it. This monotonicity is exactly what two pointers exploit.

**Steps:**

1. **Initialize:** Set `left = 0`, `right = n - 1`, `count = 0`. These pointers represent the current candidate pair.

2. **Loop while `left < right`:** Ensures we never pair an element with itself and terminates correctly when the search space is exhausted.

3. **Compute sum:** `s = freshness[left] + freshness[right]`.

4. **If `s == k`:** Valid pair found. Increment `count`, advance `left`, decrement `right`. Both elements are consumed — the non-overlapping constraint is satisfied by construction.

5. **If `s < k`:** The left value is too small to reach the target with any element ≤ `freshness[right]`. Increment `left`.

6. **If `s > k`:** The right value is too large. Decrement `right`.

7. **Invariant maintained:** At every step, all elements outside `[left, right]` are either already paired or proven unable to form a valid pair. The count only increases on confirmed matches.

8. **Return `count`.**

---

## 📊 Worked Example

Input: `freshness = [1, 2, 3, 4, 5, 6, 7]`, `k = 8`

| Step | left | right | freshness[left] | freshness[right] | Sum | Action | count |
|------|------|-------|-----------------|------------------|-----|--------|-------|
| 1 | 0 | 6 | 1 | 7 | 8 | Match → consume both | 1 |
| 2 | 1 | 5 | 2 | 6 | 8 | Match → consume both | 2 |
| 3 | 2 | 4 | 3 | 5 | 8 | Match → consume both | 3 |
| 4 | 3 | 3 | — | — | — | left == right → stop | 3 |

Element `4` at index 3 is never paired — no remaining counterpart sums to `k`. Final answer: **3**.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — each pointer moves monotonically inward; together they traverse at most `n` positions total. At 10⁶ elements this is a single tight loop, well within microsecond range. At 10⁹ elements it remains linear, though memory bandwidth becomes the bottleneck before algorithmic complexity does.

### Space Complexity

**O(1)** — only three integer variables (`left`, `right`, `count`) regardless of input size. No auxiliary structures. The sorted precondition is what buys you this; a hash-based approach would cost O(n) space to handle an unsorted input.

---

## 💡 Key Takeaways

- **Pattern signal #1:** Any problem combining "sorted array" + "pair with fixed sum/difference" is a two-pointer candidate — the sorted order provides the monotonicity that makes pointer movement deterministic.
- **Pattern signal #2:** When the problem asks to *maximize the count* of non-overlapping pairs (not just detect existence), a greedy consume-on-match strategy is worth proving before coding; here the proof is one sentence.
- **Gotcha #1:** The loop condition must be `left < right`, not `left <= right`. Allowing `left == right` would pair an element with itself, violating the distinct-index requirement.
- **Gotcha #2:** Duplicate values (e.g., `[1, 1, 4, 4]`, `k = 5`) are handled correctly without special-casing — both pointers advance on a match, consuming one instance of each duplicate, not two of the same.
- **Architectural insight:** The two-pointer pattern is fundamentally a way to exploit a sorted invariant to reduce a O(n²) search space to O(n). In production, maintaining sort order at insertion time (via a sorted structure or periodic re-sort) is often worth the overhead precisely because it unlocks this class of linear-time operations downstream.

---

## 🚀 Variations & Further Practice

- **Three-sum to target:** Find all unique triplets summing to `k`. The conceptual twist is fixing one element and running two pointers on the remainder — O(n²) overall — plus deduplication logic for repeated values that requires careful pointer advancement rules.
- **Pairs within a distance `d` (not exact sum):** Instead of `sum == k`, accept pairs where `|sum - k| <= d`. The move rule becomes ambiguous — both pointers may be valid moves — which breaks the greedy and forces a sliding window or segment tree approach depending on what you're optimizing.
- **LeetCode 15 – 3Sum / LeetCode 167 – Two Sum II:** Direct extensions that apply the same sorted two-pointer skeleton under tighter constraints (no duplicates in output, 1-indexed input), stress-testing your understanding of the invariant rather than the core idea.