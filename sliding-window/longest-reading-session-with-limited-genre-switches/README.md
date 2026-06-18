# Longest Reading Session With Limited Genre Switches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `genres`, find the maximum length of a contiguous subarray whose number of adjacent genre changes is at most `k`. A switch is counted only when `genres[i] != genres[i-1]` inside the chosen window. Return the length of the longest valid session. The challenge is that `genres.length` can reach `200000`, which rules out checking all subarrays and pushes the solution toward a linear-time sliding window.

## 🌍 Engineering Impact
This pattern shows up anywhere systems optimize for bounded transitions rather than bounded cardinality. Examples include sessionization in recommendation systems, log segmentation in streaming pipelines, burst detection in observability traces, and UI behavior analysis where state changes matter more than unique states. At scale, a naive quadratic scan collapses under long event streams and makes near-real-time analytics infeasible. A linear sliding window preserves throughput, supports online processing, and maps cleanly to stream processors where windows expand and contract incrementally without re-evaluating historical data.

## 🔍 Problem Statement
You are given an integer array `genres` where `genres[i]` is the genre ID of the `i`-th article read in order, and an integer `k`. A valid reading session is any contiguous segment containing at most `k` genre switches, where a switch occurs between adjacent elements with different values.

Return the maximum length of such a session.

Constraints:

- `1 <= genres.length <= 200000`
- `1 <= genres[i] <= 1000000000`
- `0 <= k < genres.length`

Examples:

- `genres = [1, 1, 2, 2, 2, 3, 3], k = 1` → `5`
- `genres = [4, 7, 7, 4, 4, 9, 9, 9, 4], k = 2` → `7`

Edge cases matter:

- A length-1 session always has `0` switches.
- If `k = 0`, the answer is the longest contiguous run of equal values.
- The key constraint is input size: an `O(n)` solution is expected.

## 🪜 How to Solve This
1. Read the requirement carefully → the limit is on **adjacent changes**, not on distinct genres. That immediately rules out the common “at most `k` distinct values” frequency-map window.

2. Ask what makes a window valid → only the boundaries between neighboring elements matter. For a window `[l..r]`, the switch count is the number of indices `i` in `(l+1..r)` where `genres[i] != genres[i-1]`.

3. That suggests a sliding window → when extending `r` by one element, we can update the switch count in `O(1)` by comparing `genres[r]` with `genres[r-1]`.

4. If the window becomes invalid (`switches > k`), move `l` right until it is valid again. When `l` crosses a boundary where `genres[l] != genres[l+1]`, that boundary leaves the window, so the switch count decreases by one.

5. This works because both pointers move only forward. No recomputation of the whole window is needed, so the total cost stays linear.

The key mental move is to track **boundary events**, not values.

## 🧩 Algorithm Walkthrough
1. **Use the Two Pointers / Sliding Window pattern.**  
   Maintain a window `[left, right]` and an integer `switches` representing how many adjacent genre changes exist inside that window. This is the right abstraction because validity depends on a local property that can be updated incrementally as the window grows or shrinks.

2. **Expand the right pointer one step at a time.**  
   For each `right > 0`, compare `genres[right]` with `genres[right - 1]`. If they differ, a new internal boundary has entered the window, so increment `switches`.  
   Invariant: after this update, `switches` equals the number of switches in `[left, right]`.

3. **Restore validity by advancing the left pointer.**  
   While `switches > k`, move `left` rightward. Before incrementing `left`, check whether the boundary between `genres[left]` and `genres[left + 1]` is a switch. If it is, that boundary is leaving the window, so decrement `switches`.  
   Invariant: after each shrink step, `switches` still matches the current window exactly.

4. **Record the best valid length.**  
   Once `switches <= k`, the current window is valid. Update `answer = max(answer, right - left + 1)`.

5. **Why this is correct.**  
   For every `right`, the algorithm finds the leftmost position that keeps the window valid after shrinking. Any wider window ending at `right` would violate the constraint, so the current valid window is the longest possible for that `right`. Taking the maximum over all `right` yields the global optimum.

## 📊 Worked Example
Example: `genres = [4, 7, 7, 4, 4, 9, 9, 9, 4]`, `k = 2`

| right | genres[right] | new switch? | left after shrink | switches | window              | len | best |
|------:|---------------:|------------:|------------------:|---------:|---------------------|----:|-----:|
| 0 | 4 | no  | 0 | 0 | `[4]` | 1 | 1 |
| 1 | 7 | yes | 0 | 1 | `[4,7]` | 2 | 2 |
| 2 | 7 | no  | 0 | 1 | `[4,7,7]` | 3 | 3 |
| 3 | 4 | yes | 0 | 2 | `[4,7,7,4]` | 4 | 4 |
| 4 | 4 | no  | 0 | 2 | `[4,7,7,4,4]` | 5 | 5 |
| 5 | 9 | yes | 1 | 2 | `[7,7,4,4,9]` | 5 | 5 |
| 6 | 9 | no  | 1 | 2 | `[7,7,4,4,9,9]` | 6 | 6 |
| 7 | 9 | no  | 1 | 2 | `[7,7,4,4,9,9,9]` | 7 | 7 |
| 8 | 4 | yes | 3 | 2 | `[4,4,9,9,9,4]` | 6 | 7 |

Best length is `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed once when `right` advances, and each index is removed at most once when `left` advances. There is no nested rescanning of the window. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but algorithmically optimal for a single pass.

### Space Complexity
`O(1)`. The algorithm stores only pointer indices, the current switch count, and the best length. No auxiliary map or array is required. Space cannot be meaningfully reduced further without changing the input model; the main trade-off is readability, not memory.

## 💡 Key Takeaways
- If the constraint is about **adjacent transitions** inside a contiguous range, think sliding window over boundary events, not frequency counting.
- If adding one element changes validity via a local comparison with its neighbor, that is a strong signal for a two-pointer `O(n)` solution.
- Be precise about which boundary leaves the window when `left` moves: the relevant comparison is `genres[left]` vs `genres[left + 1]`, not `genres[left - 1]`.
- The switch count is defined over edges inside the window, so length-1 windows have zero switches and should never trigger special invalid-state logic.
- In production analytics, modeling the right primitive—transitions instead of distinct values—often removes whole classes of unnecessary state and unlocks streaming implementations.

## 🚀 Variations & Further Practice
- Allow up to `k` switches, but also require at most `m` distinct genres in the same window. The harder part is combining boundary-based validity with frequency-based validity.
- Process an online event stream and emit the longest valid suffix after each new article. The twist is adapting the same invariant to incremental, real-time outputs.
- Assign a weighted cost to each genre switch instead of unit cost. The window remains contiguous, but validity now depends on cumulative edge weights rather than simple counts.