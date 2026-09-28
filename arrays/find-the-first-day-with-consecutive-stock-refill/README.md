# Find the First Day With Consecutive Stock Refill

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Linear Scan

---

## 🗂 Problem Overview
Given an array `refills` and an integer `k`, return the earliest index where a run of `k` consecutive days all have positive refill counts. If no such run exists, return `-1`. The contract is simple, but correctness depends on handling streak boundaries precisely: zeros break runs, `k = 1` changes the threshold, and arrays shorter than `k` can never satisfy the requirement.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to detect the first sustained healthy interval in a time-ordered signal. Examples include inventory pipelines detecting continuous replenishment, streaming systems identifying uninterrupted event flow, SRE dashboards finding the first stable success window after an outage, and fraud/risk systems spotting consecutive compliant periods. At scale, the difference between a single linear scan and repeated window validation matters: naive rescanning inflates CPU, cache misses, and end-to-end latency. The linear streak-count approach preserves predictable throughput, supports online processing, and composes cleanly into larger monitoring or decision pipelines.

## 🔍 Problem Statement
You are given a non-negative integer array `refills` where `refills[i]` is the number of items added to inventory on day `i`, and an integer `k`. Find the smallest index `i` such that the subarray `refills[i..i+k-1]` exists and every value in it is greater than `0`. If no such index exists, return `-1`.

Constraints:

- `1 <= refills.length <= 100000`
- `0 <= refills[i] <= 1000000`
- `1 <= k <= 100000`

Important edge cases:

- If `k == 1`, the first positive day is immediately valid.
- If `refills.length < k`, the answer must be `-1`.
- Any `0` breaks a consecutive streak.

Examples:

- `refills = [0, 3, 2, 5, 0, 4], k = 3` → `1`
- `refills = [1, 0, 2, 3, 0, 1], k = 2` → `2`

The key constraint is array size up to `10^5`, which rules out repeatedly checking every length-`k` window from scratch.

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** need the longest streak or the count of all streaks. We only need the **first starting index** of any streak with length at least `k`.

2. Notice what breaks validity → only `0` matters. Positive values extend a streak; zero resets it. That immediately suggests tracking a running count instead of evaluating every window independently.

3. Ask what state is sufficient while scanning left to right → a single integer `streak` is enough:
   - if `refills[i] > 0`, increment `streak`
   - otherwise reset `streak = 0`

4. Decide when we have enough information to return → the first time `streak == k`, we know the current day closes the earliest valid run. Its start index is `i - k + 1`.

5. Why this is better than checking each window → a naive approach would re-read up to `k` elements per start position. That is unnecessary repeated work. The streak counter compresses all prior evidence into one variable and gives an `O(n)` scan with constant space.

## 🧩 Algorithm Walkthrough
1. **Handle impossible cases early.**  
   If `k > refills.length`, return `-1`. No subarray of length `k` can exist. This avoids unnecessary scanning and makes the boundary condition explicit.

2. **Initialize a running streak counter.**  
   Set `streak = 0`. This variable represents the number of consecutive positive refill days ending at the current index. The invariant is: after processing `refills[i]`, `streak` equals the length of the current suffix of positive values.

3. **Scan the array once from left to right.**  
   This is a **Linear Scan / Simulation** pattern. We are not maintaining a general sliding window with arbitrary updates; we are simulating streak formation under a reset condition.

4. **Update state per element.**  
   For each index `i`:
   - If `refills[i] > 0`, increment `streak`.
   - Otherwise, set `streak = 0`.  
   This is correct because a zero invalidates any run crossing that position.

5. **Check for the first valid streak.**  
   After updating `streak`, if `streak == k`, return `i - k + 1`. This is the earliest possible start for the current valid run, and because we scan left to right, it is also the first valid answer globally.

6. **Return failure if no run is found.**  
   If the scan completes without `streak` reaching `k`, return `-1`. The invariant guarantees no qualifying segment exists.

## 📊 Worked Example
Example: `refills = [0, 3, 2, 5, 0, 4]`, `k = 3`

| i | refills[i] | Action                  | streak | Return? |
|---|------------|-------------------------|--------|---------|
| 0 | 0          | zero resets streak      | 0      | No      |
| 1 | 3          | positive → increment    | 1      | No      |
| 2 | 2          | positive → increment    | 2      | No      |
| 3 | 5          | positive → increment    | 3      | Yes     |

At index `3`, the running streak reaches `3`, which means days `1, 2, 3` are all positive. The start index is:

`3 - 3 + 1 = 1`

So the answer is `1`.

The important observation is that we never re-check `[3, 2, 5]` as a separate window. The `streak` variable already encodes that all three consecutive values were positive.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`, where `n = refills.length`. Each element is read once, and each iteration performs constant-time work: one comparison, one counter update, and one threshold check. At `10^6` elements this is trivial for modern CPUs; at `10^9`, linear cost is still substantial but remains the only viable asymptotic choice for a full scan.

### Space Complexity
`O(1)`. The algorithm uses only a small fixed amount of extra state, primarily the `streak` counter. No auxiliary arrays, queues, or prefix structures are required. Space cannot be meaningfully reduced further without changing the execution model, since the current approach is already constant-space.

## 💡 Key Takeaways
- If the problem asks for the first occurrence of `k` consecutive items satisfying a local condition, think linear scan with a running streak before reaching for nested loops.
- When invalid elements fully reset progress, that is a strong signal that a single counter can replace explicit window revalidation.
- The returned index is `i - k + 1` when the streak reaches `k`; returning `i` is the most common off-by-one mistake.
- Check `k > refills.length` up front, and remember that `k == 1` means the first positive element is immediately valid.
- In production pipelines, compressing repeated window checks into incremental state is a general strategy for reducing latency and improving streaming friendliness.

## 🚀 Variations & Further Practice
- Return **all** starting indices of streaks with length at least `k`; the twist is deciding whether overlapping answers should be emitted or coalesced into maximal runs.
- Allow up to `m` zero-refill days inside a window of length `k`; this becomes a true sliding-window problem with violation counting instead of simple reset logic.
- Process refills as an unbounded stream and emit the first qualifying index online; the twist is preserving correctness without storing historical windows.