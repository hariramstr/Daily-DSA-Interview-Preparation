# Longest Playlist Window With Limited Artist Repeats

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `artists` and an integer `k`, find the length of the longest contiguous subarray in which every artist ID appears at most `k` times. The output is a single integer: the maximum valid window length. The challenge is that validity depends on frequency counts inside moving contiguous ranges, so brute-forcing all subarrays is too slow under `artists.length <= 200000`.

## 🌍 Engineering Impact
This pattern shows up anywhere systems enforce local frequency caps over ordered events: playlist diversification, ad-serving repetition control, stream processing with per-key burst limits, fraud detection on event sequences, and search/ranking pipelines that constrain duplicate sources within a page or session slice. At scale, naive rescans of every candidate range collapse under high-throughput streams and large cardinality keys. A sliding-window plus hash-map approach turns repeated global recomputation into incremental state maintenance, which is exactly the difference between an online algorithm that can run in hot paths and one that must be pushed into offline batch analysis.

## 🔍 Problem Statement
You are given an integer array `artists`, where `artists[i]` is the artist ID of the `i`-th song in listening order, and an integer `k`. Return the length of the longest contiguous subarray such that no distinct artist appears more than `k` times within that subarray.

Constraints:

- `1 <= artists.length <= 200000`
- `1 <= artists[i] <= 1000000000`
- `0 <= k <= artists.length`

If `k = 0`, no song can be included, so the answer is `0`.

Examples:

- `artists = [4, 1, 4, 2, 4, 1, 3], k = 2` → `5`
- `artists = [7, 7, 7, 2, 2, 3, 7], k = 1` → `3`

The key constraint is input size: checking all subarrays is `O(n^2)` or worse, which is not viable for `n = 200000`. The solution must update window validity incrementally as the range moves.

## 🪜 How to Solve This
1. Start with the brute-force mental model: for every left boundary, extend right and count artist frequencies. That works logically, but repeated recounting makes it quadratic.

2. Notice the property is window-local and monotonic in one direction: when you extend the window by one song, only one artist’s count changes. That strongly suggests a Sliding Window / Two Pointers approach.

3. Ask what state determines validity. It is not the full subarray contents, only the frequency of each artist inside the current window. That means a `HashMap<artistId, count>` is sufficient.

4. Grow the right pointer one step at a time, incrementing that artist’s count.

5. If this addition makes some artist exceed `k`, the window is invalid. Since only the newly added artist can break validity, shrink from the left until that artist’s count is back within limit.

6. After restoring validity, record the current window length.

7. Because each pointer only moves forward, the total work is linear. The key insight is incremental repair: never rebuild counts for a window you can update in place.

## 🧩 Algorithm Walkthrough
1. **Handle the degenerate case early.** If `k == 0`, return `0`. No non-empty window can satisfy the constraint, and short-circuiting avoids unnecessary map work.

2. **Initialize the sliding window state.** Maintain:
   - `left = 0`
   - a hash map `freq` from artist ID to count in the current window
   - `best = 0`

3. **Expand with the right pointer.** For each index `right` from `0` to `n - 1`, add `artists[right]` to `freq`. This is the standard **Two Pointers / Sliding Window** pattern: one pointer grows the candidate range, the other pointer repairs it when constraints are violated.

4. **Detect invalidity locally.** After incrementing, only `artists[right]` could now exceed `k`. No other count changed, so no other artist can newly violate the rule. This localizes the repair logic.

5. **Shrink from the left until valid.** While `freq[artists[right]] > k`, decrement `freq[artists[left]]` and advance `left`. This maintains the invariant: after the loop, every artist in window `[left..right]` appears at most `k` times.

6. **Update the answer.** Once valid, compute `right - left + 1` and maximize `best`.

7. **Why this is correct.** For each `right`, the algorithm keeps the leftmost valid boundary after repairing violations. Any longer window ending at `right` would include excluded elements and therefore be invalid. So the maximum over all repaired windows is the global optimum.

## 📊 Worked Example
Example: `artists = [4, 1, 4, 2, 4, 1, 3]`, `k = 2`

| right | artists[right] | action | left | freq snapshot | valid length | best |
|---|---:|---|---:|---|---:|---:|
| 0 | 4 | add 4 | 0 | {4:1} | 1 | 1 |
| 1 | 1 | add 1 | 0 | {4:1,1:1} | 2 | 2 |
| 2 | 4 | add 4 | 0 | {4:2,1:1} | 3 | 3 |
| 3 | 2 | add 2 | 0 | {4:2,1:1,2:1} | 4 | 4 |
| 4 | 4 | add 4, invalid | 1 | {4:2,1:1,2:1} after removing leftmost 4 | 4 | 4 |
| 5 | 1 | add 1 | 1 | {4:2,1:2,2:1} | 5 | 5 |
| 6 | 3 | add 3 | 1 | {4:2,1:2,2:1,3:1} | 6 | 6 |

The longest valid window is actually `[1, 4, 2, 4, 1, 3]`, length `6`. Every artist appears at most twice.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each song enters the window once when `right` advances and leaves the window at most once when `left` advances. Hash map updates are constant-time on average, so the dominant cost is a single linear pass. This remains practical at `10^6` elements; at `10^9`, memory bandwidth and storage dominate before asymptotics do.

### Space Complexity
`O(m)`, where `m` is the number of distinct artist IDs in the current window, worst-case `O(n)`. The hash map owns the extra space. You cannot generally reduce this without losing constant-time frequency updates; trading it away usually means rescanning or sorting, which worsens runtime.

## 💡 Key Takeaways
- If the problem asks for a longest/shortest **contiguous** range under a frequency constraint, default to Sliding Window before considering heavier machinery.
- When adding one element can only break validity through that element’s key, a hash map plus local repair is usually enough for linear time.
- The `while` loop must shrink until the violating artist count is back to `<= k`; using a single `if` is a common bug.
- Window length is `right - left + 1`; updating `best` before restoring validity produces off-by-one errors and inflated answers.
- The transferable design insight is incremental state maintenance: online systems scale when they update local counters instead of recomputing global properties for every candidate range.

## 🚀 Variations & Further Practice
- Return the actual window boundaries or subarray, not just the length. Same pattern, but you must capture the best `[left, right]` snapshot without corrupting it during later repairs.
- Allow at most `k` repeats for only a subset of artists, while others are unconstrained. The twist is mixed policy enforcement per key rather than a single uniform threshold.
- Find the longest window where **total duplicate count** is at most `k`, not per-artist count. Harder because validity depends on an aggregate over all keys, not a single local violation.