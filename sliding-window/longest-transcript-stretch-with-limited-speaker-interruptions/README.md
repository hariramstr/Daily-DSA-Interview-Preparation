# Longest Transcript Stretch With Limited Speaker Interruptions

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `speakers`, find the maximum length of a contiguous segment whose number of adjacent speaker changes is at most `k`. A change counts when `speakers[i] != speakers[i-1]` inside the chosen segment. Return only the segment length, not the segment itself. The challenge is scale: with up to `200000` elements, checking every subarray is too expensive, so the solution must maintain validity incrementally in near-linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs the longest stable interval under bounded churn. Examples include sessionization in clickstream pipelines, longest low-volatility windows in market data, stable leader epochs in distributed systems, and contiguous QoS segments in observability traces. At scale, brute-force range evaluation explodes quadratically and becomes unusable in streaming or near-real-time workloads. A sliding-window formulation turns the problem into incremental state maintenance: one pass, bounded memory, predictable latency. That matters operationally because it enables online processing, simpler backpressure behavior, and straightforward partition-local execution in large data pipelines.

## 🔍 Problem Statement
You are given an integer array `speakers` where `speakers[i]` is the speaker ID at second `i`, and an integer `k`. A contiguous segment is valid if it contains at most `k` interruptions, where an interruption is any adjacent pair within the segment whose speaker IDs differ.

Return the length of the longest valid contiguous segment.

Constraints:

- `1 <= speakers.length <= 200000`
- `1 <= speakers[i] <= 1000000000`
- `0 <= k < speakers.length`

Examples:

- `speakers = [1, 1, 2, 2, 2, 3, 3], k = 1` → `5`
- `speakers = [4, 7, 7, 4, 4, 4, 9], k = 2` → `6`

Edge cases matter: a single-element segment always has `0` interruptions; if `k = 0`, the answer is the longest run of identical IDs. The input bound rules out any `O(n^2)` scan of all subarrays.

## 🪜 How to Solve This
1. Read the definition carefully → the constraint is not about distinct speakers in a window; it is about **adjacent transitions** inside the window.

2. Reframe the problem → for any segment `[left, right]`, the only thing that matters is how many indices `i` in `(left+1..right)` satisfy `speakers[i] != speakers[i-1]`.

3. That immediately suggests a sliding window → as `right` moves one step, the interruption count changes by at most one, based only on the new boundary pair `(right-1, right)`.

4. If the window becomes invalid (`interruptions > k`), move `left` forward until it is valid again. When `left` crosses a transition boundary, remove that interruption from the count.

5. Why this works → the validity condition is monotonic with respect to shrinking from the left. Once a window has too many interruptions, the only fix is to discard prefix elements until enough transition boundaries leave the window.

6. Track the maximum valid window length during the scan. Each index enters and leaves the window at most once, giving linear time.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]` and an integer `interruptions`, representing the number of adjacent changes fully contained in the current window. This abstraction fits because we need the longest contiguous range satisfying a monotone constraint.

2. **Expand the window by moving `right` from left to right.**  
   For each new `right`, compare `speakers[right]` with `speakers[right - 1]` when `right > 0`. If they differ, increment `interruptions`.  
   **Invariant:** after expansion, `interruptions` equals the number of change boundaries inside `[left, right]`.

3. **Restore validity by advancing `left` while needed.**  
   While `interruptions > k`, move `left` rightward. Before incrementing `left`, check whether the boundary `(left, left + 1)` is a speaker change. If so, decrement `interruptions`, because that boundary is no longer inside the window after `left` moves.  
   **Invariant:** after the loop, the window is the longest valid window ending at `right` with the current `left`.

4. **Update the answer.**  
   Once valid, compute `right - left + 1` and maximize the result. This is correct because every valid window ending at `right` and starting earlier would have been invalid or already considered before shrinking.

5. **Why linear time holds.**  
   `right` advances `n` times. `left` also advances at most `n` times total. No pointer ever moves backward, so the total work is `O(n)` with `O(1)` auxiliary space.

## 📊 Worked Example
Example: `speakers = [4, 7, 7, 4, 4, 4, 9]`, `k = 2`

| right | speakers[right] | New transition? | left after shrink | interruptions | window        | length | best |
|------:|------------------|-----------------|-------------------|---------------|---------------|-------:|-----:|
| 0     | 4                | —               | 0                 | 0             | `[4]`         | 1      | 1    |
| 1     | 7                | yes             | 0                 | 1             | `[4,7]`       | 2      | 2    |
| 2     | 7                | no              | 0                 | 1             | `[4,7,7]`     | 3      | 3    |
| 3     | 4                | yes             | 0                 | 2             | `[4,7,7,4]`   | 4      | 4    |
| 4     | 4                | no              | 0                 | 2             | `[4,7,7,4,4]` | 5      | 5    |
| 5     | 4                | no              | 0                 | 2             | `[4,7,7,4,4,4]` | 6    | 6    |
| 6     | 9                | yes             | 1                 | 2             | `[7,7,4,4,4,9]` | 6    | 6    |

At `right = 6`, interruptions become `3`, so the window is invalid. Moving `left` from `0` to `1` removes boundary `4 -> 7`, reducing interruptions back to `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed once when `right` expands the window, and at most once again when `left` shrinks it. There is no nested rescanning. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, the algorithm is still asymptotically right but becomes constrained by I/O and data partitioning.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only pointer indices, the current interruption count, and the best length. Space cannot be meaningfully reduced further; the main trade-off is not memory but whether the input is processed in-memory or streamed.

## 💡 Key Takeaways
- If the constraint is defined over a contiguous range and can be updated by adding one element on the right and removing one on the left, think sliding window immediately.
- When the rule is about adjacent differences rather than element frequency, model the window by its boundary transitions, not by a hash map of values.
- The interruption count belongs to boundaries inside the window, so when shrinking, remove the effect of `(left, left + 1)` before incrementing `left`.
- Be careful with window length versus interruption count: a one-element window has length `1` but `0` interruptions, and `k = 0` reduces to longest constant run.
- In production systems, this is the general pattern for longest stable interval under bounded churn: maintain incremental state instead of recomputing range quality from scratch.

## 🚀 Variations & Further Practice
- Return the actual segment indices, and if multiple windows tie on length, choose the earliest one. The algorithm is the same, but tie-breaking logic must be explicit and stable.
- Weight interruptions by cost, where each speaker change has a penalty and the window must stay under total budget `B`. The twist is replacing a simple count with an additive metric.
- Process an unbounded stream and continuously emit the best valid suffix or best-so-far window. The harder part is operational: online state management, checkpointing, and partition boundaries.