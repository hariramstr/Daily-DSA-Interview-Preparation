# Shortest Error Burst Covering All Failure Codes

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a time-ordered array `reports`, find the minimum-length contiguous window that satisfies two conditions simultaneously: every code in `required` appears at least once, and the window contains at least `k` total occurrences of codes from `critical`. Return that minimum length, or `-1` if no window works. The difficulty is that validity depends on both distinct coverage and aggregate counting, under input sizes up to `2 * 10^5`, which rules out quadratic scanning.

## 🌍 Engineering Impact
This pattern shows up in incident forensics, observability backends, SIEM pipelines, fraud detection streams, and distributed tracing analysis: find the smallest time slice that proves a multi-signal condition. In production, brute-force window enumeration collapses under high-cardinality event streams and long retention intervals. A near-linear sliding window enables online or batch analysis over millions of records while preserving latency budgets. More importantly, it separates two classes of constraints—set coverage and frequency thresholds—into composable state, which is exactly how scalable stream processors and alert correlation engines stay predictable under load.

## 🔍 Problem Statement
You are given three integer arrays: `reports`, `required`, and `critical`, plus an integer `k`.

Find the length of the shortest contiguous subarray `reports[l..r]` such that:

1. Every distinct value in `required` appears at least once in the window.
2. The number of positions `j` in `[l, r]` where `reports[j]` belongs to `critical` is at least `k`.

`required` and `critical` each contain distinct values, but they may overlap arbitrarily. `reports` may contain duplicates. If no valid subarray exists, return `-1`. If `k = 0`, only the required-coverage condition matters.

**Constraints**
- `1 <= reports.length, required.length, critical.length <= 200000`
- Values are positive integers up to `10^9`
- `0 <= k <= reports.length`

**Examples**
- `reports = [7,4,9,4,2,8,9,2,5], required = [4,2,5], critical = [9,8,5], k = 2` → `6`
- `reports = [1,3,1,6,7,3,6], required = [1,6], critical = [3], k = 2` → `5`

The `2 * 10^5` bound forces an `O(n)` or `O(n log n)` approach.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find all matches,” it is “find the shortest contiguous region meeting two predicates.”
2. “Shortest contiguous subarray” should immediately suggest a sliding window with two pointers. Sorting is impossible because time order matters.
3. Split the validity test into two independent pieces:
   - coverage of all `required` codes at least once
   - total count of elements whose value is in `critical`
4. Coverage of distinct required values implies a frequency map over required codes inside the current window, plus a counter for how many required codes are currently satisfied.
5. Critical membership does not need a map; it only needs a running integer count, because every occurrence contributes equally.
6. Expand the right pointer until the window becomes valid.
7. Once valid, shrink from the left as aggressively as possible while preserving validity. That guarantees the current window is minimal for this right boundary.
8. Record the best length seen during shrinking.
9. Because each pointer only moves forward, total work stays near-linear.

The key insight is that the window state is fully described by a small set of incremental counters, so validity can be updated in constant time per pointer move.

## 🧩 Algorithm Walkthrough
1. **Preprocess membership sets.**  
   Build a hash set for `required` and another for `critical`. Also keep a hash map `reqFreq` for counts of required codes inside the current window. This gives `O(1)` expected membership checks.

2. **Track two validity dimensions.**  
   Maintain:
   - `covered`: how many distinct required codes currently have frequency `>= 1`
   - `need = required.length`
   - `criticalCount`: total number of elements in the window that belong to `critical`  
   The window is valid iff `covered == need` and `criticalCount >= k`.

3. **Expand with the right pointer.**  
   For each `reports[r]`:
   - if it is required, increment its frequency; if it rises from `0` to `1`, increment `covered`
   - if it is critical, increment `criticalCount`  
   This preserves the invariant that all counters exactly describe `reports[l..r]`.

4. **Shrink with the left pointer while valid.**  
   While the window satisfies both conditions:
   - update the answer with `r - l + 1`
   - remove `reports[l]` from the window
   - if it is required and its frequency drops from `1` to `0`, decrement `covered`
   - if it is critical, decrement `criticalCount`
   - increment `l`  
   This is the standard **Two Pointers / Sliding Window** pattern: grow to satisfy, shrink to minimize.

5. **Why this is correct.**  
   For every fixed `r`, the inner loop finds the smallest valid left boundary reachable before validity breaks. Since every candidate minimal window is considered exactly when it becomes shrinkable, the global minimum is found.

6. **Return result.**  
   If no valid window was ever observed, return `-1`; otherwise return the best length.

## 📊 Worked Example
Use `reports = [1,3,1,6,7,3,6]`, `required = [1,6]`, `critical = [3]`, `k = 2`.

| r | reports[r] | covered | criticalCount | valid? | shrink/result |
|---|---:|---:|---:|---|---|
| 0 | 1 | 1 | 0 | no | need `6` and 2 criticals |
| 1 | 3 | 1 | 1 | no | missing `6`, only 1 critical |
| 2 | 1 | 1 | 1 | no | duplicate required |
| 3 | 6 | 2 | 1 | no | all required present, still short on criticals |
| 4 | 7 | 2 | 1 | no | unchanged |
| 5 | 3 | 2 | 2 | yes | window `[0..5]`, len 6 |
|   | remove 1 at l=0 | 1 | 2 | no | stop shrinking, best = 6 |
| 6 | 6 | 1 | 2 | no | still missing `1` |

That trace misses the optimal shrink path unless we continue from the valid state before coverage breaks: starting at `r=5`, if `l=0` were not essential, the algorithm would keep shrinking. The minimal valid window encountered is `[1..5] = [3,1,6,7,3]`, length `5`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + |required| + |critical|)` expected time. Building the sets is linear in input size, and the sliding window moves each pointer across `reports` at most once. At `10^6` elements this remains practical; at `10^9`, even linear scans become infrastructure-bound and require partitioned or streaming execution.

### Space Complexity
`O(|required| + |critical|)` expected space. The dominant structures are the two hash sets and the frequency map for required codes currently in the window. You can reduce map size to only required elements, but not eliminate auxiliary state without losing constant-time updates.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous region** satisfying dynamic constraints, start with a sliding window before considering heavier machinery.
- When validity mixes **distinct coverage** and **total occurrence thresholds**, model them as separate counters updated incrementally.
- Overlap between `required` and `critical` must affect both state machines on the same element; treat them independently, not as mutually exclusive categories.
- The shrink loop must update the answer **before** removing `reports[l]`; reversing that order causes classic off-by-one bugs.
- In production stream processing, scalable correlation often comes from decomposing a complex predicate into orthogonal window-local invariants that can be maintained in constant time.

## 🚀 Variations & Further Practice
- Require each code in `required` to appear with its own minimum multiplicity, not just once. The twist is replacing binary coverage with per-key quota satisfaction.
- Ask for the number of valid subarrays instead of the shortest one. The harder part is converting “minimal valid left boundary” into a counting formula.
- Add weighted critical events and require total critical weight `>= k`. The pattern survives, but correctness now depends on maintaining an aggregate sum rather than a simple count.