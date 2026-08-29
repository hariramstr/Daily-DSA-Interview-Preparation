# Longest Notification Feed With Cooldowned App Repeats

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a chronological array of app IDs and an integer `cooldown`, find the maximum length of a contiguous subarray where repeated notifications from the same app are spaced more than `cooldown` indices apart. The output is a single integer: the longest valid segment length. The challenge is that validity depends on relative positions inside a moving range, so brute-force checking all subarrays is too expensive for `n` up to 200,000.

## 🌍 Engineering Impact
This pattern shows up in notification deduplication, ad pacing, event-stream suppression, and rate-limited recommendation feeds. In production, the requirement is rarely “count distinct”; it is usually “preserve order while enforcing spacing constraints.” That changes the architecture: sorting is illegal, and full recomputation per window does not scale. A sliding-window plus last-seen index map gives predictable linear behavior under high-cardinality streams. Without it, feed assembly, abuse controls, or alert-throttling pipelines degrade into quadratic scans, causing latency spikes and unstable throughput exactly where traffic is burstiest.

## 🔍 Problem Statement
You are given an integer array `apps` of length `n`, where `apps[i]` is the app ID for the `i`-th notification, and an integer `cooldown`. A contiguous segment is valid if, for every app ID appearing in that segment, any two equal values are more than `cooldown` positions apart. Equivalently, if `apps[i] == apps[j]` and both indices are inside the segment, then `|i - j| > cooldown`.

Return the length of the longest valid contiguous segment.

Constraints:
- `1 <= n <= 200000`
- `1 <= apps[i] <= 1000000000`
- `0 <= cooldown <= n`

Examples:
- `apps = [4, 1, 2, 4, 3, 1, 5], cooldown = 2` → `5`
- `apps = [7, 7, 8, 9, 7, 8, 10], cooldown = 3` → `4`

Edge cases:
- If `cooldown = 0`, every segment is valid, so the answer is `n`.
- The input size rules out `O(n^2)` subarray validation.

## 🪜 How to Solve This
1. Start from the constraint: we need the **longest contiguous** segment, not an arbitrary subset. That is a strong signal for a sliding window.
2. Ask what makes a window invalid. Only one thing: the current app has appeared too recently inside the window, within `cooldown` positions.
3. That means we do not need full frequency history. We only need the **most recent index** of each app.
4. Scan left to right with a right pointer. For each `apps[right]`, look up its last seen index.
5. If that previous occurrence is too close — `right - prev <= cooldown` — then the current window cannot start at or before `prev`. Move the left boundary to `prev + 1`.
6. Critically, never move `left` backward. Use `left = max(left, prev + 1)`.
7. Update the app’s last seen index to `right`, then compute the current window length.
8. This yields a single-pass `O(n)` solution: each element is processed once, and window validity is maintained incrementally instead of recomputed.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Sliding Window with Two Pointers.**  
   We need the longest contiguous range under a local spacing constraint. Two pointers are the right abstraction because the valid region expands monotonically to the right, and violations can be repaired by advancing the left boundary.

2. **Track last occurrence per app in a hash map.**  
   Store `lastSeen[appId] = most recent index`. This is sufficient because only the nearest previous duplicate can invalidate the current position. Older duplicates are farther away and therefore less restrictive.

3. **Initialize `left = 0`, `best = 0`.**  
   The invariant is: before processing `right`, the window `[left, right - 1]` is valid.

4. **Process each index `right`.**  
   Let `app = apps[right]`. If `app` has been seen at `prev`, check whether `right - prev <= cooldown`. If true, the duplicate is too close, so any valid window ending at `right` must start after `prev`. Set `left = max(left, prev + 1)`.

5. **Update state after repairing the window.**  
   Record `lastSeen[app] = right`. Now `[left, right]` is valid again because the only newly introduced risk was the current app, and we moved `left` far enough to exclude the conflicting occurrence.

6. **Measure the candidate answer.**  
   Compute `right - left + 1` and update `best`. The invariant after each iteration is that `[left, right]` is the longest valid window ending at `right`.

7. **Return `best`.**  
   Correctness follows from maintaining the maximal valid suffix for every `right` and taking the maximum over all suffix endpoints.

## 📊 Worked Example
Use `apps = [4, 1, 2, 4, 3, 1, 5]`, `cooldown = 2`.

| right | app | lastSeen before | action on left | window `[left..right]` | len | best |
|---|---:|---:|---:|---|---:|---:|
| 0 | 4 | — | `left=0` | `[4]` | 1 | 1 |
| 1 | 1 | — | `left=0` | `[4,1]` | 2 | 2 |
| 2 | 2 | — | `left=0` | `[4,1,2]` | 3 | 3 |
| 3 | 4 | 0 | `3-0=3 > 2`, keep | `[4,1,2,4]` | 4 | 4 |
| 4 | 3 | — | `left=0` | `[4,1,2,4,3]` | 5 | 5 |
| 5 | 1 | 1 | `5-1=4 > 2`, keep | `[4,1,2,4,3,1]` | 6 | 6 |
| 6 | 5 | — | `left=0` | `[4,1,2,4,3,1,5]` | 7 | 7 |

Every repeat is more than 2 positions apart, so the entire array is valid and the answer is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time with a hash map. Each notification is processed once, each map lookup/update is constant-time on average, and `left` only moves forward. At `10^6` elements this is routine; at `10^9`, the algorithmic shape is still right, but memory bandwidth and storage become the practical bottlenecks.

### Space Complexity
`O(k)` where `k` is the number of distinct app IDs in the feed, owned by the `lastSeen` map. In the worst case `k = n`. You cannot generally reduce this without sacrificing lookup speed or correctness for high-cardinality streams.

## 💡 Key Takeaways
- If the requirement is “longest contiguous segment” and violations are caused by recent history, think sliding window before considering sorting or nested scans.
- When validity depends on the nearest prior occurrence of a key, a hash map of last-seen positions is usually enough; full frequency state is unnecessary.
- The condition is **strictly greater than** `cooldown`, so a repeat at distance exactly `cooldown` is invalid.
- When repairing the window, use `left = max(left, prev + 1)`; assigning `prev + 1` directly can move `left` backward and corrupt the invariant.
- In production stream processing, incremental constraint maintenance beats recomputation: preserve order, update local state, and keep latency proportional to input size.

## 🚀 Variations & Further Practice
- Return the actual segment boundaries, and if multiple optimal windows exist, break ties by earliest start; the twist is preserving correctness while adding deterministic selection rules.
- Allow up to `m` cooldown violations inside a window; this turns a hard validity constraint into a budgeted one and requires tracking violation counts, not just last positions.
- Generalize cooldown per app ID instead of a single global value; the core pattern remains, but the invalidity check becomes key-dependent and state management gets less uniform.