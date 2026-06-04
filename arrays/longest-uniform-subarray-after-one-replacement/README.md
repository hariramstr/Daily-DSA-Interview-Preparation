# Find the Longest Uniform Subarray After One Replacement

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview

Given an integer array, return the length of the longest contiguous subarray where every element is identical, with the allowance to replace **at most one** element with any value. The core input/output contract: integer array in, single integer length out. The non-trivial constraint is that "at most one" replacement — it prevents a brute-force scan and demands a strategy that tracks heterogeneity within a window without re-scanning it on every move.

---

## 🌍 Engineering Impact

This pattern surfaces anywhere a system needs to detect the longest stable run in a stream while tolerating bounded noise. Network monitoring systems use it to find the longest period of consistent latency with one anomalous spike. Log analyzers apply it to identify stable operational states interrupted by a single error event. In time-series databases (InfluxDB, Prometheus), identifying the longest uniform metric window with one outlier drives alerting thresholds. Without an O(n) approach, scanning at 10⁵–10⁶ events per second with a nested loop collapses throughput and makes real-time decisions impossible.

---

## 🔍 Problem Statement

**Input:** An integer array `nums` where `1 <= nums.length <= 10^5` and `1 <= nums[i] <= 10^4`.  
**Output:** The length of the longest subarray where all elements are equal, after replacing **at most one** element with any integer value of your choice.

**Examples:**

| Input | Output | Reasoning |
|---|---|---|
| `[1, 1, 2, 1, 1]` | `5` | Replace index 2 with `1` → entire array uniform |
| `[3, 3, 5, 5, 5, 3]` | `4` | Replace index 3 with `5` → `[5,5,5,5]` subarray |

**Edge cases to consider:** all elements already equal (answer is `n`, no replacement needed); all elements distinct (answer is `2`, any adjacent pair with one replaced); array of length 1 (answer is `1`). The "at most one replacement" constraint is what rules out a simple frequency scan and demands tracking window composition dynamically.

---

## 🪜 How to Solve This

1. **Read the constraint** → "longest subarray, all equal, one replacement allowed." This is a bounded-defect window problem — we want the longest window containing at most one element that differs from the dominant value.

2. **Brute force cost** → For every pair `(i, j)`, scan the subarray, count non-majority elements. O(n²) or worse. At n=10⁵ that's 10¹⁰ operations. Unacceptable.

3. **Notice the window structure** → A valid window has one dominant value and at most one "bad" element. We don't need to restart from scratch when the window shifts — we can slide it.

4. **Sliding window fit** → Maintain a `left` pointer and a `right` pointer. Expand `right` freely. Track the count of the most frequent element in the current window (`maxFreq`). If `window_size - maxFreq > 1`, we've exceeded our one-replacement budget — shrink from `left`.

5. **Key insight** → We never need to shrink `maxFreq` when we move `left`. We only care about windows *at least as large* as the best we've seen. This makes the window monotonically non-decreasing in size, giving us O(n) total moves.

---

## 🧩 Algorithm Walkthrough

**Pattern: Sliding Window with Frequency Tracking**

This is the right abstraction because the validity condition (`window_size - maxFreq <= 1`) is monotonic — once a window becomes invalid by growing, shrinking it by one from the left restores validity. That monotonicity is the prerequisite for sliding window correctness.

**Steps:**

1. **Initialize** `left = 0`, `maxFreq = 0`, `result = 0`, and a frequency map (array of size 10⁴+1 suffices given constraints).

2. **Expand right:** For each `right` from `0` to `n-1`, increment `freq[nums[right]]` and update `maxFreq = max(maxFreq, freq[nums[right]])`.

3. **Check validity:** Compute `window_size = right - left + 1`. If `window_size - maxFreq > 1`, the window has more than one non-dominant element. Shrink: decrement `freq[nums[left]]`, increment `left`. **Do not update `maxFreq` downward** — we only care about windows larger than our current best.

4. **Record result:** After the validity check, `result = max(result, right - left + 1)`.

5. **Invariant maintained:** At every step, the window `[left, right]` either satisfies the one-replacement constraint, or is exactly one element larger than the last valid window — which means `result` always reflects the longest valid window seen.

6. **Return `result`.**

The frequency map is O(1) per operation; each element enters and exits the window at most once, giving O(n) total.

---

## 📊 Worked Example

Input: `nums = [3, 3, 5, 5, 5, 3]`

| right | nums[right] | freq map (partial) | maxFreq | window [left..right] | size | size−maxFreq | left | result |
|---|---|---|---|---|---|---|---|---|
| 0 | 3 | {3:1} | 1 | [0..0] | 1 | 0 | 0 | 1 |
| 1 | 3 | {3:2} | 2 | [0..1] | 2 | 0 | 0 | 2 |
| 2 | 5 | {3:2,5:1} | 2 | [0..2] | 3 | 1 | 0 | 3 |
| 3 | 5 | {3:2,5:2} | 2 | [0..3] | 4 | 2 → shrink | 1 | 3 |
| 3 | — | {3:1,5:2} | 2 | [1..3] | 3 | 1 | 1 | 3 |
| 4 | 5 | {3:1,5:3} | 3 | [1..4] | 4 | 1 | 1 | **4** |
| 5 | 3 | {3:2,5:3} | 3 | [1..5] | 5 | 2 → shrink | 2 | 4 |

Final answer: **4** (window `[1..4]` = `[3,5,5,5]`, replace `3` → `[5,5,5,5]`).

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — each element is added to the window once (`right` advances) and removed at most once (`left` advances). The frequency map update is O(1). At 10⁶ elements this is a single linear pass; at 10⁹ elements it remains feasible in-memory if the array fits, with no algorithmic degradation.

### Space Complexity

**O(k)** where `k` is the value range (here, 10⁴). The frequency array owns this space. It cannot be reduced to O(1) without losing O(1) frequency lookup — a HashMap trades constant-factor memory for the same asymptotic bound.

---

## 💡 Key Takeaways

- **Pattern signal #1:** Any problem asking for the "longest subarray/substring satisfying a constraint with at most K exceptions" is a sliding window candidate — the K-budget structure maps directly onto the `size - maxFreq <= K` validity check.
- **Pattern signal #2:** If the validity condition is monotonic (valid window can only become invalid by growing, never by shrinking), a two-pointer approach will yield O(n) — test for this before reaching for O(n log n) segment trees.
- **Gotcha #1:** Never decrement `maxFreq` when shrinking the window. The algorithm's correctness relies on only seeking windows *larger* than the current best; a stale `maxFreq` is intentional, not a bug.
- **Gotcha #2:** The window size after a shrink step is `right - left + 1`, not `right - left` — off-by-one here silently produces answers that are one short and will pass most naive tests but fail on single-element arrays.
- **Architectural insight:** The "tolerate one anomaly in a run" pattern generalizes directly to stream processors and circuit breakers — encoding the budget as a window invariant rather than a stateful counter makes the logic composable, testable in isolation, and trivially adjustable when the tolerance budget changes.

---

## 🚀 Variations & Further Practice

- **Longest Subarray with At Most K Replacements (LeetCode 2401 / variant of 424):** Generalize the budget from 1 to K. The same sliding window structure holds, but choosing the right `maxFreq` update strategy becomes more nuanced — the window can now absorb K defects, and the interaction between `maxFreq` staleness and correctness requires deeper justification.
- **Longest Repeating Character Replacement (LeetCode 424):** The same core algorithm applied to strings with a 26-character alphabet. The conceptual twist: the "dominant value" is now a character, and the problem asks you to derive the pattern from a character-frequency context rather than an integer one — a useful exercise in recognizing the abstraction beneath the surface framing.
- **Max Consecutive Ones III (LeetCode 1004):** Restricts the array to binary values (0s and 1s) and asks for the longest subarray of 1s after flipping at most K zeros. The binary constraint makes `maxFreq` implicit (it's always the count of 1s), which strips the algorithm to its skeleton and is the cleanest way to internalize why the invariant works before applying it to the general case.