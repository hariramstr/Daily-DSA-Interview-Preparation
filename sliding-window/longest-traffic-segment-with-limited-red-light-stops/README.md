# Longest Traffic Segment With Limited Red-Light Stops

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Monotonic Queue, Two Pointers

---

## 🗂 Problem Overview
Given `lights`, `penalty`, `k`, and `limit`, find the maximum length of a contiguous segment whose red lights satisfy two constraints: there are at most `k` of them, and their penalties have spread at most `limit` (`max - min <= limit`). Green lights do not participate in the penalty-range check. The challenge is that validity depends on both red-light count and dynamic min/max over a moving window, so naive subarray enumeration is too slow for `n` up to `2 * 10^5`.

## 🌍 Engineering Impact
This pattern shows up in streaming systems and online decision engines where a window must satisfy both cardinality and bounded-variance constraints. Examples include rate-limiters that tolerate only a small number of violations within a time slice, search/ranking pipelines that need contiguous candidate batches with bounded score drift, and telemetry anomaly detectors that ignore “healthy” events but tightly constrain the spread of “bad” ones. Without a linear-time windowing approach, systems fall back to repeated rescans or heap rebuilds, which collapse under sustained throughput. Monotonic queues preserve exactness while keeping latency predictable and memory bounded.

## 🔍 Problem Statement
You are given two arrays of length `n`:

- `lights[i]` is `1` if intersection `i` is green, `0` if red.
- `penalty[i]` is the stop-delay cost at intersection `i`.

Choose a contiguous segment `[l, r]` with maximum length such that:

1. The segment contains at most `k` red lights.
2. Considering only red lights in `[l, r]`, `max(penalty) - min(penalty) <= limit`.

Green lights are ignored for the second condition. If a segment has no red lights, it is valid.

Constraints:

- `1 <= n <= 2 * 10^5`
- `0 <= penalty[i] <= 10^9`
- `0 <= k <= n`
- `0 <= limit <= 10^9`

Examples:

- `lights = [1,0,0,1,0,1,0]`, `penalty = [5,8,10,3,9,4,11]`, `k = 3`, `limit = 2` → `5`
- `lights = [0,1,0,0,1,0]`, `penalty = [7,2,20,9,1,10]`, `k = 2`, `limit = 3` → `3`

The key constraint is `n = 2 * 10^5`: any `O(n^2)` subarray validation will time out.

## 🪜 How to Solve This
1. Read the constraints → contiguous segment + longest valid window strongly suggests Two Pointers / Sliding Window.

2. Identify what makes a window invalid:
   - too many red lights
   - red penalties spread too far apart

3. Counting red lights is easy: maintain a running `redCount` as the right pointer expands and the left pointer contracts.

4. The harder part is dynamic `min` and `max` over only the red penalties currently inside the window. Recomputing them by scanning the window every time would destroy performance.

5. That points to monotonic queues:
   - one deque increasing by penalty gives current minimum
   - one deque decreasing by penalty gives current maximum
   - only red-light indices are inserted

6. Expand `right` one step at a time. If the window violates either constraint, move `left` forward until it becomes valid again, evicting deque fronts when they fall out of range.

7. Once the window is valid, update the best length. Each index enters and leaves each deque at most once, giving linear time.

This is the standard mental move: “longest contiguous region under dynamic min/max constraints” → sliding window plus monotonic deques.

## 🧩 Algorithm Walkthrough
1. **Initialize window state**  
   Use `left = 0`, `best = 0`, `redCount = 0`, and two deques storing indices of red lights:
   - `minDeque`: penalties increasing
   - `maxDeque`: penalties decreasing  
   These deques let us query current red-penalty min/max in `O(1)`.

2. **Expand with `right` from `0` to `n - 1`**  
   If `lights[right] == 0`, increment `redCount` and insert `right` into both deques:
   - pop from `minDeque` while last penalty is larger than `penalty[right]`
   - pop from `maxDeque` while last penalty is smaller than `penalty[right]`  
   This preserves monotonicity, so deque fronts are always the current min/max red penalties.

3. **Check validity**  
   The window is invalid if:
   - `redCount > k`, or
   - both deques are non-empty and `penalty[maxDeque.front] - penalty[minDeque.front] > limit`  
   Green lights never enter the deques, so they do not affect penalty spread.

4. **Shrink from the left until valid**  
   While invalid:
   - if `lights[left] == 0`, decrement `redCount`
   - if `left` equals the front of either deque, pop it
   - increment `left`  
   This maintains the invariant that both deques contain exactly the red indices still inside `[left, right]`.

5. **Record answer**  
   After restoration of validity, update `best = max(best, right - left + 1)`.

This is a textbook combination of **Two Pointers** and **Monotonic Queue**: the window gives linear traversal, and the deques give exact rolling extrema without rescans.

## 📊 Worked Example
Example: `lights = [1,0,0,1,0,1,0]`, `penalty = [5,8,10,3,9,4,11]`, `k = 3`, `limit = 2`

| right | light | action | redCount | minRed | maxRed | left | valid | best |
|---|---:|---|---:|---:|---:|---:|---|---:|
| 0 | 1 | green only | 0 | - | - | 0 | yes | 1 |
| 1 | 0 | add 8 | 1 | 8 | 8 | 0 | yes | 2 |
| 2 | 0 | add 10 | 2 | 8 | 10 | 0 | yes | 3 |
| 3 | 1 | green only | 2 | 8 | 10 | 0 | yes | 4 |
| 4 | 0 | add 9 | 3 | 8 | 10 | 0 | yes | 5 |
| 5 | 1 | green only | 3 | 8 | 10 | 0 | yes | 6 |
| 6 | 0 | add 11 | 4 | 8 | 11 | 0 | no | 6 |

At `right = 6`, the window violates both count and spread. Shrink:
- move `left` past index `0` (green): still invalid
- move past index `1` (red 8): `redCount = 3`, min becomes `9`, max `11`, spread `2`

Now window `[2,6]` is valid with length `5`. Final answer remains `6`? No: `[0,5]` has red penalties `[8,10,9]`, spread `2`, count `3`, so length `6` is valid. That is the true maximum for this trace.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is processed once by the right pointer, removed at most once by the left pointer, and inserted/removed from each monotonic deque at most once. That linear behavior is the difference between something that remains practical at `10^6` elements and something unusable at `10^9`, where even a small extra factor becomes dominant.

### Space Complexity
`O(n)` in the worst case, owned by the two deques when many red indices remain candidates for min/max. It cannot be reduced to `O(1)` without giving up exact rolling extrema or paying extra update cost, typically via rescans or balanced trees.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous region under “at most K bad items” plus a dynamic min/max bound, think sliding window immediately.
- If min/max must be maintained under window movement in linear time, monotonic deques are usually the right primitive.
- Only red-light indices belong in the deques; inserting greens corrupts the penalty-range invariant.
- Be careful when shrinking: decrement `redCount` only when `lights[left] == 0`, and evict deque fronts only if their stored index equals `left`.
- The production lesson is broader than this problem: isolate the subset that affects the expensive constraint, and maintain exact summaries only for that subset.

## 🚀 Variations & Further Practice
- Replace `max(redPenalty) - min(redPenalty) <= limit` with `median` or `percentile` bounds; the twist is that monotonic queues no longer suffice, pushing you toward balanced multisets or indexed frequency structures.
- Allow up to `k` red lights to be “forgiven” and excluded from the spread calculation; now the hard part is jointly optimizing count and extrema under selective deletion.
- Move from one static array to an online stream with expiring timestamps instead of indices; same window pattern, but eviction is time-based and integration with event-time ordering becomes the real complexity.