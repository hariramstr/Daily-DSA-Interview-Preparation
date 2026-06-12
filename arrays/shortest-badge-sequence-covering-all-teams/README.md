# Shortest Badge Sequence Covering All Teams

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an array `scans` of team IDs and an integer `m` representing all required teams `1..m`, find the length of the shortest contiguous subarray containing every team at least once. Return `-1` if any team is missing entirely. The challenge is scale: with `scans.length` up to `200000`, enumerating all subarrays is infeasible, so the solution must maintain coverage incrementally while scanning the array once.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the smallest interval satisfying a full coverage constraint: streaming observability windows containing all critical services, fraud pipelines requiring all event categories before scoring, ad-tech sessions needing all targeting signals, or log analytics searching for the tightest incident window spanning multiple components. At production scale, brute force explodes quadratically and creates latency cliffs under bursty traffic. Sliding-window coverage tracking enables linear-time processing, bounded memory, and predictable tail latency, which matters when this logic sits inside online ranking, alerting, or real-time compliance workflows.

## 🔍 Problem Statement
You are given an integer array `scans`, where `scans[i]` is the team ID of the `i`-th badge scan, and an integer `m` representing the total number of distinct teams labeled from `1` to `m`.

Return the length of the shortest contiguous subarray that contains at least one occurrence of every team in `1..m`. If no such subarray exists, return `-1`.

Constraints:

- `1 <= scans.length <= 200000`
- `1 <= m <= 100000`
- `1 <= scans[i] <= m`
- `scans` is unsorted and may contain duplicates
- If any team in `1..m` never appears, the answer is `-1`

Examples:

- `scans = [2, 1, 3, 2, 4, 1, 3], m = 4` → `4`
- `scans = [1, 2, 2, 1, 3], m = 4` → `-1`

The key constraint is input size: checking every subarray would be `O(n^2)`, which is too slow.

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **shortest contiguous range** covering a fixed set of values `1..m`.
2. “Contiguous” plus “shortest” is a strong signal for a **sliding window / two pointers** approach.
3. Ask what makes a window valid → it must contain every team at least once. That means we need frequency tracking, not just membership.
4. Use a count structure keyed by team ID. As the right pointer expands, increment counts. When a team count goes from `0` to `1`, coverage improves.
5. Track how many distinct required teams are currently covered. Once that number reaches `m`, the window is valid.
6. Now shrink from the left as aggressively as possible while preserving validity. This is where the minimum-length answer gets updated.
7. Why this works: each pointer only moves forward, so instead of rechecking overlapping subarrays repeatedly, we reuse state across adjacent windows.
8. If we never reach coverage `m`, some team never appeared in any valid window, so return `-1`.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window / Two Pointers.**  
   We need the minimum-length contiguous segment satisfying a coverage condition. This is exactly the class of problems where a window expands until valid, then contracts to minimality.

2. **Maintain frequency counts for team IDs in the current window.**  
   Use an array of size `m + 1` or a hash map. Since team IDs are bounded in `1..m`, an array is simpler and faster.  
   Invariant: `count[t]` equals the number of times team `t` appears between `left` and `right`.

3. **Track `covered`, the number of teams currently present at least once.**  
   When adding `scans[right]`, if its count changes from `0` to `1`, increment `covered`.  
   Invariant: `covered == number of team IDs with positive count in the window`.

4. **Expand the right pointer across the array.**  
   Each new element potentially makes the window valid. This is the only way to add missing teams, so expansion is necessary until `covered == m`.

5. **When the window is valid, shrink from the left.**  
   Update the best length with `right - left + 1`, then decrement `count[scans[left]]` and move `left`.  
   If a count drops from `1` to `0`, decrement `covered`; the window is no longer valid.  
   Invariant: after each contraction loop, the previous window was the smallest valid window ending at `right`.

6. **Repeat until `right` reaches the end.**  
   Because both pointers move monotonically, total work is linear.

7. **Return the result or `-1`.**  
   If no valid window was found, coverage never reached all `m` teams.

## 📊 Worked Example
Example: `scans = [2, 1, 3, 2, 4, 1, 3]`, `m = 4`

| Step | right | value | left | covered | Action | best |
|---|---:|---:|---:|---:|---|---:|
| 1 | 0 | 2 | 0 | 1 | add 2 | ∞ |
| 2 | 1 | 1 | 0 | 2 | add 1 | ∞ |
| 3 | 2 | 3 | 0 | 3 | add 3 | ∞ |
| 4 | 3 | 2 | 0 | 3 | add duplicate 2 | ∞ |
| 5 | 4 | 4 | 0 | 4 | window valid `[0..4]` | 5 |
| 6 | 4 | 4 | 1 | 4 | remove 2, still valid `[1..4]` | 4 |
| 7 | 4 | 4 | 2 | 3 | remove 1, invalid | 4 |
| 8 | 5 | 1 | 2 | 4 | valid again `[2..5]` | 4 |
| 9 | 6 | 3 | 2 | 4 | valid, shrink attempts fail to beat 4 | 4 |

Shortest valid length is `4`, from subarray `[1, 3, 2, 4]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = scans.length`. Each element is added to the window once by the right pointer and removed at most once by the left pointer. There is no nested reprocessing of subarrays. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally the right asymptotic shape.

### Space Complexity
`O(m)` for the frequency structure storing counts for team IDs `1..m`. If implemented with an array, this is compact and cache-friendly. A hash map can reduce space when effective IDs are sparse, but adds higher constant factors and more operational overhead.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous segment** satisfying a coverage requirement, think **sliding window with frequency tracking**.
- If validity depends on “contains all required categories at least once,” track **counts plus a covered-distinct counter**, not repeated full-window scans.
- Be careful to decrement `covered` only when a team’s count drops from `1` to `0`; decrementing on every left move is wrong.
- The minimum window length is `right - left + 1`; this is the classic off-by-one bug in two-pointer implementations.
- The transferable design insight is to maintain **incremental validity state** so adjacent windows reuse computation instead of recomputing global properties.

## 🚀 Variations & Further Practice
- Find the **shortest subarray containing a target multiset**, where each required team has a minimum count; the twist is validity depends on per-key thresholds, not simple presence.
- Count **how many subarrays** contain all teams `1..m`; the twist is converting a minimum-window existence pattern into combinatorial counting.
- Solve the streaming version where scans arrive continuously and you must emit the current best window online; the twist is operationalizing the same invariant under unbounded input.