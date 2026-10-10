# Longest Alarm Timeline With Limited Snooze Resets

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given an array `alarms`, find the maximum length of a contiguous subarray such that, after applying at most `k` snooze resets, no alarm label appears more than `limit` times. A reset removes one chosen occurrence from frequency accounting inside the window. For any label with frequency `f`, the window consumes `max(0, f - limit)` resets for that label. The challenge is computing the longest valid window under large input sizes, where quadratic enumeration is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must maintain a bounded-frequency window under selective exemptions. Examples include distributed rate-limiters with override tokens, streaming fraud pipelines that tolerate a fixed number of anomalous events, search ranking sessions that cap repeated content while allowing limited boosts, and telemetry aggregation with exception budgets. At scale, recomputing per-window violations from scratch collapses under throughput and latency targets. The sliding-window formulation enables single-pass admission control, online analytics, and bounded-memory processing over high-cardinality streams without sacrificing exactness.

## 🔍 Problem Statement
You are given an integer array `alarms`, an integer `limit`, and an integer `k`.

A contiguous subarray is valid if each alarm label appears at most `limit` times after assigning at most `k` snooze resets within that subarray. A reset can be applied to any single day and causes that occurrence to not count toward its label’s frequency. If a label appears `f` times in the window, it requires `max(0, f - limit)` resets. The window is valid when the sum of these excess counts across all labels is at most `k`.

Return the maximum valid window length.

Constraints:
- `1 <= alarms.length <= 200000`
- `1 <= alarms[i] <= 10^9`
- `0 <= k <= alarms.length`
- `1 <= limit <= alarms.length`

Examples:
- `alarms = [5,1,5,2,5,1,1], limit = 2, k = 1` → `5`
- `alarms = [4,4,4,3,3,4,3,3], limit = 1, k = 3` → `5`

The key constraint is `n = 200000`: this rules out checking all subarrays and forces an `O(n)` or `O(n log n)` approach.

## 🪜 How to Solve This
1. Read the validity rule carefully → resets are not arbitrary magic; they exactly cover the amount by which frequencies exceed `limit`.

2. Rewrite the condition → for a window, required resets =  
   `sum over labels of max(0, freq[label] - limit)`.  
   The window is valid iff that sum is `<= k`.

3. Notice what changes when expanding the window by one element → only one label’s frequency changes, so the required-reset total can be updated incrementally.

4. That immediately suggests a sliding window → grow the right boundary, track frequencies in a hash map, and maintain a single scalar `excess` representing total required resets.

5. When adding `alarms[right]`, if its new frequency becomes `limit + 1` or higher, `excess` increases by 1. When removing `alarms[left]`, if its old frequency was above `limit`, `excess` decreases by 1.

6. Maintain the invariant `excess <= k` by advancing `left` whenever needed. Since both pointers only move forward, the whole scan is linear.

7. Track the maximum window length seen while valid. That is the answer.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   This is the right abstraction because validity depends on a contiguous range, and the constraint can be updated locally when one element enters or leaves the window.

2. **Maintain a frequency map `freq`.**  
   `freq[x]` stores how many times alarm label `x` appears in the current window `[left, right]`. A hash map is required because `alarms[i]` can be as large as `10^9`.

3. **Maintain `excess`, the total resets required by the current window.**  
   Formally, `excess = Σ max(0, freq[x] - limit)`.  
   This scalar is the key invariant: the window is valid exactly when `excess <= k`.

4. **Expand the window by moving `right`.**  
   Increment `freq[alarms[right]]`. If the updated count is now greater than `limit`, then this new occurrence contributes one additional excess unit, so increment `excess`.

5. **Shrink the window while invalid.**  
   While `excess > k`, remove `alarms[left]` from the window. Before decrementing its frequency, if its current count is greater than `limit`, then removing it eliminates one excess unit, so decrement `excess`. Then decrement the frequency and advance `left`.

6. **Record the best valid length.**  
   After restoration of `excess <= k`, the current window is valid. Update `answer = max(answer, right - left + 1)`.

7. **Why this is correct.**  
   At every step, the algorithm maintains exact frequencies and exact required resets for the current window. The inner loop restores minimal validity by moving `left` only as far as necessary. Since every valid window ending at `right` is considered through this process, the maximum length is found.

## 📊 Worked Example
Example: `alarms = [5,1,5,2,5,1,1]`, `limit = 2`, `k = 1`

| right | alarms[right] | freq change | excess | left after shrink | valid window |
|---|---:|---|---:|---:|---|
| 0 | 5 | `5: 1` | 0 | 0 | `[5]` |
| 1 | 1 | `1: 1` | 0 | 0 | `[5,1]` |
| 2 | 5 | `5: 2` | 0 | 0 | `[5,1,5]` |
| 3 | 2 | `2: 1` | 0 | 0 | `[5,1,5,2]` |
| 4 | 5 | `5: 3` | 1 | 0 | `[5,1,5,2,5]` |
| 5 | 1 | `1: 2` | 1 | 0 | `[5,1,5,2,5,1]` |
| 6 | 1 | `1: 3` | 2 | shrink to 2 | `[5,2,5,1,1]` |

At `right = 6`, the window needs 2 resets (`5` exceeds by 1, `1` exceeds by 1), so it becomes invalid. Shrinking from the left removes `5`, then `1`, reducing `excess` back to 1. The maximum valid length observed is `6`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each element is inserted into the window once and removed once, so `left` and `right` each advance at most `n` times. Hash map updates are `O(1)` expected. At `10^6` elements this remains practical; at `10^9`, even linear scans become infrastructure-scale workloads.

### Space Complexity
`O(m)` where `m` is the number of distinct alarm labels in the current or overall array, owned by the frequency hash map. In the worst case `m = O(n)`. This cannot be meaningfully reduced without sacrificing exact frequency tracking or switching to approximate structures.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous range under a constraint that can be updated when one item enters or leaves, think sliding window before considering DP or prefix structures.
- When validity is expressed as “sum of per-key violations,” look for a hash map plus one aggregate scalar instead of recomputing the full condition each step.
- The excess update is off-by-one sensitive: increment `excess` only when a frequency crosses from `limit` to `limit + 1`, and decrement when it crosses back.
- Shrink while `excess > k`, not while any individual frequency exceeds `limit`; resets are globally budgeted across labels, not enforced label-by-label.
- The transferable design insight is to convert a complex local policy into an incrementally maintained budget metric, enabling exact online decisions in one pass.

## 🚀 Variations & Further Practice
- Return the actual subarray, not just its length; the conceptual twist is preserving best-window boundaries while maintaining the same invariant.
- Make each label have its own `limit[label]`; the harder part is replacing a uniform threshold with per-key policy while keeping constant-time updates.
- Assign different reset costs per label or per occurrence; this breaks the simple excess counter and pushes the problem toward weighted windows or more complex data structures.