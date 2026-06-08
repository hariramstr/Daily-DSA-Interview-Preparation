# Longest Snack Break Within Calorie Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview

Given an ordered array of snack calorie values and an upper bound `maxCalories`, find the length of the longest contiguous subarray whose sum does not exceed that bound. The input is a sequence (order matters), the output is a single integer count, and the non-trivial constraint is that you must evaluate all possible contiguous windows efficiently — brute-force enumeration of every subarray is O(n²), which becomes untenable at the stated scale of 10⁵ elements.

---

## 🌍 Engineering Impact

This pattern is the backbone of **sliding-window rate limiters** — e.g., counting requests within a rolling time window without reprocessing the entire history on each tick. It appears in **network traffic shaping** (byte budgets over packet sequences), **real-time telemetry pipelines** (max events per rolling interval in systems like Kafka Streams or Flink), and **compiler register allocation** (live-range windows within a budget of available registers). Without the O(n) sliding window, each of these degrades to quadratic scans, which at millions of events per second translates directly to dropped data or missed SLA windows.

---

## 🔍 Problem Statement

**Input:** Integer array `calories` (1 ≤ n ≤ 10⁵, 1 ≤ calories[i] ≤ 1000) and integer `maxCalories` (1 ≤ maxCalories ≤ 10⁷).  
**Output:** Length of the longest contiguous subarray with sum ≤ `maxCalories`. Return `0` if no single element satisfies the constraint.

**Examples:**

| Input | maxCalories | Output | Winning subarray |
|---|---|---|---|
| `[100, 200, 150, 50, 300, 80]` | 400 | 3 | `[200, 150, 50]` |
| `[500, 600, 700]` | 400 | 0 | none |

**Key constraint driving the algorithm:** the subarray must be *contiguous*. This rules out greedy selection of the smallest elements and forces a window-based approach. The sum-not-exceed condition means the window can expand and contract monotonically, which is what makes a two-pointer solution valid.

---

## 🪜 How to Solve This

1. **Recognize the shape** → "longest contiguous subarray satisfying a sum constraint" is a canonical sliding window signal. The word *contiguous* combined with a *monotonic aggregate constraint* (sum ≤ limit) is the fingerprint.

2. **Ask: can the window only grow or must it shrink?** → Adding an element can only increase the sum; removing from the left can only decrease it. The constraint is monotonic with respect to window size, so a two-pointer approach is valid — no need for a deque or segment tree.

3. **Define the invariant** → Maintain a window `[left, right]` where the sum is always ≤ `maxCalories`. Expand `right` greedily; when the sum exceeds the limit, advance `left` until the invariant is restored.

4. **Track the answer** → After each valid expansion of `right`, record `right - left + 1` as a candidate maximum. The answer is the largest such value seen.

5. **Edge case** → If even a single element exceeds `maxCalories`, `left` will advance past `right`, the window length never becomes positive, and the result stays `0` — no special-case code needed.

---

## 🧩 Algorithm Walkthrough

**Pattern: Variable-size Sliding Window (Two Pointers)**

This is the right abstraction because the window's validity depends on a running aggregate (sum), not on individual element properties, and that aggregate responds predictably to expansion and contraction.

**Steps:**

1. **Initialize** `left = 0`, `windowSum = 0`, `maxLen = 0`. Both pointers start at the beginning; the window is empty.

2. **Expand right pointer** — iterate `right` from `0` to `n - 1`. Add `calories[right]` to `windowSum`. This grows the window by one element.

3. **Shrink from left while over budget** — while `windowSum > maxCalories`, subtract `calories[left]` from `windowSum` and increment `left`. This restores the invariant. Because each element enters and exits the window at most once, this inner loop is amortized O(1) per outer iteration.

4. **Update answer** — after the shrink phase, the window `[left, right]` is guaranteed valid. Compute `right - left + 1` and update `maxLen` if larger.

5. **Termination** — once `right` reaches `n - 1`, every possible window has been considered as a right endpoint. Return `maxLen`.

**Invariant maintained throughout:** at the start of step 4 for any `right`, `windowSum` equals the true sum of `calories[left..right]` and is ≤ `maxCalories`.

---

## 📊 Worked Example

`calories = [100, 200, 150, 50, 300, 80]`, `maxCalories = 400`

| right | calories[right] | windowSum (before shrink) | left (after shrink) | windowSum (after shrink) | window length | maxLen |
|---|---|---|---|---|---|---|
| 0 | 100 | 100 | 0 | 100 | 1 | 1 |
| 1 | 200 | 300 | 0 | 300 | 2 | 2 |
| 2 | 150 | 450 | 1 | 350 | 2 | 2 |
| 3 | 50  | 400 | 1 | 400 | 3 | **3** |
| 4 | 300 | 700 | 3 | 350 | 2 | 3 |
| 5 | 80  | 430 | 4 | 380 | 2 | 3 |

At `right = 3`, the window `[1, 3]` = `[200, 150, 50]` hits exactly 400 — valid and longest. No subsequent window beats length 3.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — each element is added to `windowSum` exactly once (when `right` passes it) and removed at most once (when `left` passes it). The total number of operations across both pointers is bounded by 2n. At 10⁶ elements this is trivially fast; at 10⁹ it remains linear but memory bandwidth becomes the bottleneck, not compute.

### Space Complexity

**O(1)** — only three scalar variables (`left`, `windowSum`, `maxLen`) are maintained regardless of input size. The input array itself is read-only. No auxiliary structures are needed, and there is no trade-off available — this is already optimal.

---

## 💡 Key Takeaways

- **Pattern signal #1:** "Longest/shortest contiguous subarray satisfying an aggregate constraint" → reach for sliding window before any other structure. The word *contiguous* is the trigger.
- **Pattern signal #2:** If the aggregate (sum, product, count) changes monotonically as the window grows, two pointers suffice. If it doesn't (e.g., absolute difference, modular arithmetic), you likely need a more complex structure like a deque or segment tree.
- **Implementation gotcha:** The shrink loop must use `while`, not `if`. A single shrink step may not be enough to restore the invariant if a large element was just added; `if` introduces a subtle bug that only surfaces on inputs with large value spikes.
- **Off-by-one trap:** Window length is `right - left + 1`, not `right - left`. Forgetting the `+ 1` produces a result consistently off by one — the kind of bug that passes most hand-crafted tests but fails on single-element inputs.
- **Architectural insight:** This same expand/shrink loop is the core of production sliding-window rate limiters. The key design decision is identical — maintain a running aggregate rather than recomputing from scratch, and evict from the tail only when the invariant breaks. That amortized O(1) eviction is what makes the pattern scale.

---

## 🚀 Variations & Further Practice

- **Minimum Size Subarray Sum (LeetCode 209):** Find the *shortest* subarray with sum ≥ target rather than longest with sum ≤ limit. The twist: you flip the shrink condition and track a minimum, which means the window shrinks aggressively rather than conservatively — easy to conflate the two directions under pressure.
- **Longest Substring with At Most K Distinct Characters (LeetCode 340):** The aggregate is now a frequency map, not a scalar sum. The window validity check requires a hash map lookup, adding O(1) average overhead per step but introducing the complexity of managing a non-scalar invariant and handling the map's size as the constraint.
- **Subarray Product Less Than K (LeetCode 713):** Replace sum with product. The critical twist is that product can jump orders of magnitude with a single element (unlike sum with bounded values), and a zero in the array collapses the product entirely — requiring careful handling of edge cases that the sum variant never encounters.