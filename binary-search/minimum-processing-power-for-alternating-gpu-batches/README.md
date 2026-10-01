# Minimum Processing Power for Alternating GPU Batches

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Array

---

## 🗂 Problem Overview
Given an ordered array `work`, split it into at most `m` non-empty contiguous groups. Group `1` is hot, group `2` is cool, then hot/cool alternates. A hot group sum must not exceed `hotLimit`; a cool group sum must not exceed `coolLimit`; and every group must also satisfy the time bound `groupSum <= P * T`, where `P` is the integer GPU processing power. Return the minimum feasible `P`, or `-1` if alternating mode limits make any schedule impossible.

## 🌍 Engineering Impact
This pattern shows up in production schedulers where capacity is tunable but placement rules are structural: GPU batch packing under thermal envelopes, streaming stages with alternating resource classes, storage compaction windows with tier-specific limits, or network dispatch across hot/cold paths. The failure mode at scale is expensive overprovisioning or, worse, assuming local packing decisions compose globally. The useful abstraction is separating a monotone provisioning variable from a linear-time feasibility check. That lets you answer “how much capacity is enough?” without simulating every possible partition, which is exactly what large schedulers, allocators, and admission controllers need.

## 🔍 Problem Statement
You are given `work[0..n-1]`, where `1 <= n <= 2e5` and `1 <= work[i] <= 1e9`. You must partition the array into at most `m` contiguous, non-empty segments, preserving order. Segment indices are 1-based for mode assignment: odd segments are hot, even segments are cool. A hot segment sum must be `<= hotLimit`; a cool segment sum must be `<= coolLimit`. In addition, if node processing power is `P`, every segment must satisfy `segmentSum <= P * T`, where `1 <= T <= 1e9`.

Return the minimum positive integer `P` that makes such a partition possible, or `-1` if no `P` can work.

Example 1:
- Input: `work = [8,5,6,4,7], m = 3, hotLimit = 13, coolLimit = 11, T = 2`
- Output: `6`

Example 2:
- Input: `work = [9,9,9], m = 2, hotLimit = 8, coolLimit = 20, T = 3`
- Output: `-1`

The key constraint is `n` up to `2e5`: anything quadratic over split positions is dead on arrival.

## 🪜 How to Solve This
1. Read the problem → the partition must preserve order, so this is not bin packing and not sorting. It is contiguous segmentation.

2. Notice the objective is “minimum `P`” → that strongly suggests binary search if feasibility for a fixed `P` is monotone.

3. For a fixed `P`, each segment capacity becomes:
   - hot: `min(hotLimit, P*T)`
   - cool: `min(coolLimit, P*T)`

4. Now the question is simpler: can the array be covered by at most `m` alternating-capacity segments?

5. For a fixed sequence of segment capacities, the greedy rule is obvious: extend the current segment as far right as possible. Cutting earlier never helps reduce segment count for contiguous positive weights.

6. The only subtlety is parity. Starting from hot, every new segment flips capacity. So the feasibility check is a single left-to-right pass that greedily packs under the current cap, opens a new segment when needed, and fails immediately if one item exceeds the current cap.

7. Since larger `P` only increases `P*T`, feasibility is monotone. Binary search the smallest feasible `P`.

That gives `O(n log answer)` with an `O(n)` checker.

## 🧩 Algorithm Walkthrough
1. **Pre-check impossibility under infinite power.**  
   Even if `P` were arbitrarily large, segment caps are still bounded by `hotLimit` and `coolLimit`. So feasibility can fail purely because of alternating mode limits. Run the checker once with capacities `(hotLimit, coolLimit)`. If that fails, return `-1`.  
   **Invariant:** if infeasible here, no finite `P` can fix it.

2. **Define the monotone feasibility predicate.**  
   For a candidate `P`, compute:
   - `hotCap = min(hotLimit, P*T)`
   - `coolCap = min(coolLimit, P*T)`  
   Then ask whether `work` can be partitioned into at most `m` alternating segments under these caps.  
   **Why correct:** increasing `P` never decreases either cap, so feasibility only flips from false to true once.

3. **Use a greedy linear scan for the predicate.**  
   This is a **Greedy + Binary Search on Answer** pattern. Start with segment `1` in hot mode. Accumulate `work[i]` into the current segment while the sum stays within the current cap. Otherwise, open the next segment, flip mode, and retry the same item there.  
   **Invariant:** after processing index `i`, the algorithm has used the minimum possible number of segments for prefix `work[0..i]` under the alternating-cap rule.

4. **Why the greedy cut is optimal.**  
   All `work[i]` are positive. If the current item still fits, cutting earlier only leaves less room in the current segment and cannot reduce future segment count. Therefore “pack as much as possible” minimizes segments for each prefix.

5. **Binary search the minimum feasible `P`.**  
   Lower bound can be `1`. Upper bound can be `ceil(max(work)/T)` adjusted upward until feasible, or simply a safe large bound such as `1e18`. In practice, doubling from `1` until feasible is robust and avoids reasoning mistakes.  
   **Invariant:** search interval always contains the minimum feasible `P`.

## 📊 Worked Example
Use `work = [8,5,6,4,7], m = 3, hotLimit = 13, coolLimit = 11, T = 2`.

Try `P = 6`:
- `P*T = 12`
- `hotCap = min(13,12) = 12`
- `coolCap = min(11,12) = 11`

| i | work[i] | mode | current sum before | action | segments used |
|---|---------|------|--------------------|--------|---------------|
| 0 | 8 | hot  | 0  | add to seg1 → 8   | 1 |
| 1 | 5 | hot  | 8  | 13 > 12, open seg2 | 2 |
| 1 | 5 | cool | 0  | add to seg2 → 5   | 2 |
| 2 | 6 | cool | 5  | add to seg2 → 11  | 2 |
| 3 | 4 | cool | 11 | 15 > 11, open seg3 | 3 |
| 3 | 4 | hot  | 0  | add to seg3 → 4   | 3 |
| 4 | 7 | hot  | 4  | add to seg3 → 11  | 3 |

Finished with exactly `3` segments, so `P = 6` is feasible. Testing `P = 5` gives caps `10/10`, which forces more than `3` segments, so the minimum is `6`.

## ⏱ Complexity Analysis
### Time Complexity
The feasibility check is `O(n)` because it scans the array once and performs constant work per element. Binary search adds a `log answer` factor, so total complexity is `O(n log answer)`. At `n = 2e5` this is comfortably practical; at `10^6`, still fine in optimized code. Anything quadratic is completely non-viable.

### Space Complexity
`O(1)` auxiliary space beyond the input. The checker maintains only running sum, segment count, and current mode/parity. There is no DP table or prefix-state structure. Space cannot be meaningfully reduced further; the main trade-off is using wider integer types to avoid overflow.

## 💡 Key Takeaways
- If the problem asks for the minimum capacity/power/threshold and feasibility gets easier as the value grows, think binary search on the answer immediately.
- If items are positive and groups must be contiguous, “extend the current group as far as possible” is a strong signal that a greedy feasibility check may be optimal.
- The first segment is always hot; getting the starting parity wrong silently invalidates the whole checker.
- Use 64-bit or wider arithmetic for `P*T` and running sums; `work[i]`, limits, and products can exceed 32-bit range easily.
- The production-grade insight is to decouple provisioning from placement: search over a monotone capacity variable, but keep the placement validator linear and deterministic.

## 🚀 Variations & Further Practice
- Allow choosing the starting mode (`hot` or `cool`). The twist is that feasibility for a fixed `P` becomes the minimum over two parity starts, which changes the impossibility pre-check and edge handling.
- Replace “at most `m` segments” with “exactly `m` segments.” The harder part is that greedy minimum-segment packing is no longer sufficient; you must reason about whether extra valid cuts can be inserted.
- Generalize from alternating two-mode caps to a periodic pattern of length `k` (for example, hot/warm/cool). The conceptual jump is preserving the same monotone outer search while extending the greedy checker to cyclic capacities.