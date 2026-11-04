# Longest Snack Cart Run Within Sodium Limit

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `sodium` of non-negative integers and an integer `maxSodium`, find the maximum length of a contiguous subarray whose sum is at most `maxSodium`. Return `0` if every individual snack exceeds the limit. The challenge is not computing sums, but doing so efficiently for large inputs: `n` can reach `200,000`, so quadratic scanning of all subarrays is not acceptable.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must find the longest valid contiguous span under a budget: API request bursts under rate limits, streaming jobs constrained by memory or latency budgets, ad-serving windows under spend caps, and log ingestion batches bounded by payload size. At scale, brute-force range evaluation collapses under `O(n^2)` behavior and destroys tail latency. The sliding-window formulation exploits monotonicity from non-negative values, enabling single-pass processing with constant extra space. That matters operationally: predictable throughput, low memory pressure, and a design that can be embedded directly into streaming or online decision paths.

## 🔍 Problem Statement
You are given:

- `sodium.length` in `[1, 200000]`
- `sodium[i]` in `[0, 100000]`
- `maxSodium` in `[0, 1000000000]`

Each `sodium[i]` is the sodium content of the `i`-th snack. You must choose one **contiguous** run of snacks such that the total sodium does not exceed `maxSodium`. Return the maximum possible length of such a run. If no valid run exists, return `0`.

Examples:

- `sodium = [120, 80, 200, 90, 60], maxSodium = 300` → `2`
- `sodium = [40, 0, 30, 20, 10], maxSodium = 60` → `4`

Important edge cases:
- `maxSodium = 0`
- arrays containing zeros
- every element larger than `maxSodium`
- large inputs where only linear-time solutions are viable

The decisive constraint is that all values are **non-negative**, which makes a shrinking/expanding window correct.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is not subset selection or knapsack.
2. Notice all values are **non-negative** → that is the key structural property. When you extend a window to the right, the sum can only stay the same or increase.
3. That monotonic behavior suggests a **sliding window**:
   - grow the window by moving `right`
   - if the sum becomes too large, shrink from the left until it is valid again
4. Why this works: once a window exceeds `maxSodium`, adding more elements cannot fix it. The only way back to validity is to remove elements from the left.
5. Track the best window length seen while maintaining the invariant that the current window sum is always `<= maxSodium` after adjustment.
6. This avoids recomputing sums for overlapping subarrays. Each element enters the window once and leaves once, giving linear time.

That chain of reasoning is the tell: contiguous range + bounded aggregate + non-negative values → sliding window.

## 🧩 Algorithm Walkthrough
1. **Initialize two pointers and state**  
   Set `left = 0`, `currentSum = 0`, and `best = 0`.  
   This defines the current window as `sodium[left..right]` once iteration begins.

2. **Expand the window with `right`**  
   For each index `right`, add `sodium[right]` to `currentSum`.  
   This considers every possible right boundary exactly once.

3. **Restore validity by shrinking from the left**  
   While `currentSum > maxSodium`, subtract `sodium[left]` and increment `left`.  
   This is correct because all values are non-negative: removing from the left is the only way to reduce the sum, and once the sum is valid, any further shrinking would only shorten the candidate.

4. **Update the answer after the window is valid**  
   When the loop finishes, the invariant is:  
   `currentSum <= maxSodium` and the active window is contiguous.  
   Now compute `right - left + 1` and update `best` if larger.

5. **Maintain the core invariant throughout**  
   At every outer-loop iteration, the algorithm maintains the longest valid window ending at `right`. Because `left` only moves forward, no window is revisited, which is why the Two Pointers / Sliding Window pattern is the right abstraction here.

6. **Return `best`**  
   If no single element fits, `best` remains `0`, which matches the required behavior.

## 📊 Worked Example
Example: `sodium = [40, 0, 30, 20, 10]`, `maxSodium = 60`

| right | sodium[right] | currentSum after add | action while sum > 60 | left | valid window     | best |
|------:|---------------:|---------------------:|-----------------------|-----:|------------------|-----:|
| 0     | 40             | 40                   | none                  | 0    | `[40]`           | 1    |
| 1     | 0              | 40                   | none                  | 0    | `[40, 0]`        | 2    |
| 2     | 30             | 70                   | remove 40             | 1    | `[0, 30]`        | 2    |
| 3     | 20             | 50                   | none                  | 1    | `[0, 30, 20]`    | 3    |
| 4     | 10             | 60                   | none                  | 1    | `[0, 30, 20,10]` | 4    |

The longest valid contiguous run has length `4`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. Each element is added to `currentSum` once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing of ranges. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally better than any quadratic alternative.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, a running sum, and the best length. Space cannot be meaningfully reduced below constant; the main trade-off is using a 64-bit accumulator for safety even when inputs fit in 32 bits.

## 💡 Key Takeaways
- If the problem asks for a **contiguous** range with a bounded sum and all numbers are non-negative, sliding window should be the default candidate.
- When the aggregate becomes invalid and can be repaired only by moving one boundary forward, that is a strong Two Pointers signal.
- Update the answer **after** shrinking, not before; otherwise you may record invalid windows.
- Be careful with zeros: they preserve validity but still extend window length, so they must not trigger unnecessary shrinking.
- The production-level insight is that monotonic constraints let you replace repeated range recomputation with single-pass state maintenance, which is the difference between toy and scalable throughput.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: harder because the optimization target flips, and with negative values the simple sliding window breaks.
- **Longest subarray with sum exactly `K`**: typically solved with prefix sums and a hash map, since exact equality does not preserve the same monotonic shrink/expand behavior.
- **Longest valid window with negative numbers allowed**: the core twist is that non-negativity disappears, so standard sliding window is no longer correct and more advanced prefix-sum structures are required.