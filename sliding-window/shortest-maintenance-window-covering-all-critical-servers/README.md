# Shortest Maintenance Window Covering All Critical Servers

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given a time-ordered array `events` and a distinct set `critical`, find the shortest contiguous subarray containing every critical server at least once. The window must also satisfy a reliability cap: every non-critical server may appear at most `L` times inside that window. Return the minimum valid window length, or `-1` if none exists. The challenge is scale: with up to `200,000` events, brute-force enumeration of subarrays is not viable.

## 🌍 Engineering Impact
This pattern shows up in observability pipelines, distributed audit systems, fraud detection streams, and log-based compliance checks: find the smallest interval that proves required signals occurred while bounding tolerated noise. In production, the difference between `O(n^2)` scanning and a linear sliding window is the difference between online validation and backlog collapse. The extra “bounded non-critical frequency” constraint mirrors real SLO enforcement, where required entities must be present but incidental traffic cannot exceed policy. Efficient frequency tracking enables single-pass processing, predictable memory use, and deployability in streaming or near-real-time systems.

## 🔍 Problem Statement
You are given:

- `events`: an integer array where `events[i]` is the server ID observed at second `i`
- `critical`: a distinct integer array of required server IDs
- `L`: the maximum allowed frequency for any non-critical server inside the chosen window

Find the minimum length of a contiguous subarray `events[left...right]` such that:

1. Every ID in `critical` appears at least once.
2. Every ID not in `critical` appears at most `L` times.

Return `-1` if no such window exists.

Constraints:

- `1 <= events.length <= 200000`
- `1 <= critical.length <= min(100000, events.length)`
- `1 <= events[i], critical[i] <= 10^9`
- `critical` values are distinct
- `0 <= L <= events.length`

Examples:

- `events = [7,2,9,2,5,7,3,9,5], critical = [2,5,9], L = 1` → `3`
- `events = [4,8,1,8,6,4,2,6,1], critical = [1,2,6], L = 0` → `3`

The input size rules out checking all subarrays; the algorithm must be near-linear.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find any window containing all targets.” The window must satisfy two independent conditions: full coverage of `critical`, and bounded frequency for every non-critical ID.

2. “Shortest contiguous subarray” is the strongest signal for a sliding-window / two-pointer approach. If a window becomes valid, shrinking it from the left is the natural way to search for a minimum.

3. To know whether the window covers all required IDs, track counts for critical servers and maintain how many distinct critical IDs are currently satisfied.

4. To enforce the reliability rule, also track counts for non-critical servers. The moment one non-critical count exceeds `L`, the window is invalid.

5. That gives a clean state model:
   - `criticalCovered == critical.length`
   - `violatingNonCritical == 0`

6. Expand `right` to acquire missing requirements. Shrink `left` while the window remains valid to remove unnecessary prefix elements and minimize length.

7. Each element enters and leaves the window once, so with hash-based counting this becomes linear-time and scales to the stated constraints.

## 🧩 Algorithm Walkthrough
1. **Preprocess critical IDs using a hash set.**  
   This gives `O(1)` membership checks for deciding whether an event is critical or non-critical. That distinction drives both counting logic and validity checks.

2. **Maintain a sliding window with Two Pointers (`left`, `right`).**  
   Move `right` from `0` to `n - 1`, adding `events[right]` into the window. This is the correct abstraction because the problem asks for the shortest contiguous segment under dynamic constraints.

3. **Track critical frequencies and coverage.**  
   Use a hash map `critFreq`. When a critical ID count changes from `0 -> 1`, increment `coveredCritical`.  
   Invariant: `coveredCritical` equals the number of distinct critical IDs currently present in the window.

4. **Track non-critical frequencies and violations.**  
   Use another hash map `otherFreq`. When a non-critical ID count changes from `L -> L+1`, increment `violatingNonCritical`.  
   Invariant: `violatingNonCritical` equals the number of non-critical IDs currently exceeding the allowed cap.

5. **Check window validity.**  
   The window is valid iff:
   - `coveredCritical == criticalCount`
   - `violatingNonCritical == 0`

6. **Shrink greedily from the left while valid.**  
   Record the current length as a candidate answer, then remove `events[left]` and advance `left`. If removing a critical ID drops its count from `1 -> 0`, coverage breaks. If removing a non-critical ID drops its count from `L+1 -> L`, one violation disappears.

7. **Return the best length found, else `-1`.**  
   Correctness follows from the standard minimal-window invariant: for each `right`, the inner shrink loop finds the smallest valid window ending at `right`, and the global minimum over all such windows is the answer.

## 📊 Worked Example
Example: `events = [7,2,9,2,5,7,3,9,5]`, `critical = [2,5,9]`, `L = 1`

| Step | right | val | coveredCritical | violatingNonCritical | left after shrink | best |
|---|---:|---:|---:|---:|---:|---:|
| add 7 | 0 | 7 | 0 | 0 | 0 | ∞ |
| add 2 | 1 | 2 | 1 | 0 | 0 | ∞ |
| add 9 | 2 | 9 | 2 | 0 | 0 | ∞ |
| add 2 | 3 | 2 | 2 | 0 | 0 | ∞ |
| add 5 | 4 | 5 | 3 | 0 | 2 | 3 |
| add 7 | 5 | 7 | 3 | 1 | 2 | 3 |
| add 3 | 6 | 3 | 3 | 2 | 2 | 3 |
| add 9 | 7 | 9 | 3 | 2 | 2 | 3 |
| add 5 | 8 | 5 | 3 | 2 | 2 | 3 |

At `right = 4`, the window first becomes valid: `[7,2,9,2,5]`. Shrinking removes `7`, then removes the extra `2`, yielding `[9,2,5]` of length `3`. Later windows either violate the non-critical cap or are not shorter.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, where `n = events.length`. Each event is inserted into the window once and removed once, and each update is an `O(1)` hash-map operation on average. At `10^6` scale this is practical; at `10^9`, even linear scans become bandwidth-bound and require streaming or partitioned execution.

### Space Complexity
`O(k + m)` where `k = critical.length` and `m` is the number of distinct non-critical IDs that appear in the active or processed window maps. The space is owned by the membership set and frequency maps. It can only be reduced by trading exactness for compression or approximate counting.

## 💡 Key Takeaways
- If the problem asks for the **shortest contiguous segment** satisfying dynamic membership constraints, start with a sliding window before considering anything heavier.
- When validity depends on **counts crossing thresholds**, model the window with frequency maps plus small derived counters instead of rescanning state.
- Be precise about threshold transitions: non-critical violations change only on `L -> L+1` when adding and `L+1 -> L` when removing.
- Shrink only while the window is valid; shrinking during invalid states can skip feasible minima or corrupt the coverage invariant.
- The production lesson is broader than interviews: maintain incremental validity state so policy checks stay online, local, and linear under sustained stream volume.

## 🚀 Variations & Further Practice
- Require each critical server to appear at least `need[id]` times instead of once. The twist is heterogeneous per-key thresholds, which changes the coverage condition from binary presence to quota satisfaction.
- Allow at most `K` total non-critical events in the window rather than per-ID cap `L`. This is easier to state but changes the invariant from per-key threshold tracking to aggregate noise budgeting.
- Find the shortest window covering all critical servers in a circular stream. The twist is wraparound handling without duplicating work or breaking minimality guarantees.