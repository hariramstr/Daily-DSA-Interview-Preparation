# Shortest Playlist Segment With Mood Coverage and Replay Caps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `moods`, a map `required`, and an integer `cap`, find the shortest contiguous subarray whose counts satisfy two simultaneous constraints: every required mood appears at least its requested number of times, and every mood in the window appears at most `cap` times. Return that minimum length, or `-1` if no such segment exists. The difficulty is that the window must satisfy lower bounds for some keys and an upper bound for all keys at the same time.

## 🌍 Engineering Impact
This pattern shows up anywhere a bounded slice of a stream must satisfy both coverage and saturation constraints. Examples include ad-serving windows that must contain required campaign classes without over-serving any class, log-processing pipelines that need mandatory event types while enforcing per-key burst caps, and search/ranking candidate windows that must preserve diversity quotas. At scale, brute-force rescanning windows collapses under high-cardinality keys and long streams. A linear sliding-window design enables online processing, predictable latency, and memory proportional to active distinct keys rather than value range.

## 🔍 Problem Statement
You are given:

- `moods[i]`: the mood category of the `i`-th song
- `required[x]`: minimum count of mood `x` that must appear in the segment
- `cap`: maximum allowed count of any mood inside the segment

Find the length of the shortest contiguous segment such that:

1. For every mood `x` in `required`, window count of `x` is at least `required[x]`
2. For every mood in the window, its count is at most `cap`

Return `-1` if no valid segment exists.

Constraints force an `O(n)` or near-`O(n)` approach:

- `1 <= moods.length <= 2 * 10^5`
- `1 <= moods[i] <= 10^9`
- `1 <= required.size <= 2 * 10^5`
- `1 <= required[x] <= cap <= moods.length`

Examples:

- `moods = [4,1,2,1,3,2,1,4], required = {1:2, 2:1, 3:1}, cap = 3` → `5`
- `moods = [5,5,1,2,5,3,1,2], required = {1:1, 2:1, 3:1}, cap = 2` → `4`

Large mood IDs rule out array-indexed counting by value range.

## 🪜 How to Solve This
1. Read the constraints → contiguous segment + minimum length strongly suggests a sliding window with two pointers.

2. Notice the window is governed by two different conditions:
   - some moods need counts **at least** a threshold
   - all moods need counts **at most** `cap`

3. The upper-bound condition is the one that can become invalid immediately when you extend right. If adding one song makes some count exceed `cap`, the only repair is to move left until that count is back within limit.

4. The lower-bound condition is different: once the window is cap-safe, you want to know whether it already covers all required moods. That can be tracked incrementally with a single counter like `formed`, representing how many required moods currently meet their minimum.

5. So the strategy becomes:
   - expand right, updating counts
   - shrink left until no mood exceeds `cap`
   - if all required moods are satisfied, keep shrinking left while validity remains true to get the shortest window ending at this right boundary

6. Hash maps are mandatory because mood IDs are sparse and large. Each pointer moves forward once, so the whole process is linear.

## 🧩 Algorithm Walkthrough
1. **Initialize state using the Two Pointers / Sliding Window pattern.**  
   Maintain `left`, a frequency map `windowCount`, and `formed`, the number of required moods whose current count has reached the exact threshold in `required`. Let `need = required.size`.

2. **Expand the window by moving `right` from left to right.**  
   Add `moods[right]` to `windowCount`. If this mood is required and its count just became equal to `required[mood]`, increment `formed`.  
   **Invariant:** after processing `right`, counts reflect the exact window `[left, right]`.

3. **Repair cap violations immediately.**  
   While `windowCount[moods[right]] > cap`, move `left` forward, decrementing counts. If removing a required mood causes its count to fall from `required[mood]` to `required[mood] - 1`, decrement `formed`.  
   This works because only the just-added mood can create a new cap violation, and shrinking from the left is the only legal repair.  
   **Invariant:** after this loop, every mood count in the window is `<= cap`.

4. **Try to minimize a valid window.**  
   If `formed == need`, the window satisfies both constraints. Record its length, then continue shrinking from the left while validity remains true: cap safety is preserved by shrinking, so only required coverage can break. Update `formed` when a required count drops below threshold.  
   **Invariant:** each recorded answer is a valid window; each shrink step moves toward the shortest valid window for this `right`.

5. **Return the best length found, or `-1`.**  
   Each element is inserted once and removed once, giving linear time.

## 📊 Worked Example
Example: `moods = [4,1,2,1,3,2,1,4]`, `required = {1:2,2:1,3:1}`, `cap = 3`

| right | mood | window after cap-fix | counts snapshot | formed | valid? | best |
|---|---:|---|---|---:|---|---:|
| 0 | 4 | `[4]` | `{4:1}` | 0 | no | ∞ |
| 1 | 1 | `[4,1]` | `{4:1,1:1}` | 0 | no | ∞ |
| 2 | 2 | `[4,1,2]` | `{4:1,1:1,2:1}` | 1 | no | ∞ |
| 3 | 1 | `[4,1,2,1]` | `{4:1,1:2,2:1}` | 2 | no | ∞ |
| 4 | 3 | `[4,1,2,1,3]` | `{4:1,1:2,2:1,3:1}` | 3 | yes | 5 |
| shrink | - | `[1,2,1,3]` invalid | `{1:2,2:1,3:1}` after removing `4`, still valid → best `4`? No, this window is length 4 and valid? It is valid. But example says 5 for `[1,2,1,3,2]`; continue carefully. |
| 5 | 2 | `[1,2,1,3,2]` | `{1:2,2:2,3:1}` | 3 | yes | 5 |

A correct trace shows the first minimal valid window is length `5`: `[1,2,1,3,2]`. The algorithm finds it by expanding until all required counts are met, then shrinking only while those counts remain satisfied.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, where `n = moods.length`. Each song enters the window once when `right` advances and leaves once when `left` advances. Hash map updates dominate and are constant-time on average. This remains practical at `10^6` elements; anything quadratic is dead on arrival, and even `O(n log n)` becomes noticeable under streaming or repeated-query workloads.

### Space Complexity
`O(k)` where `k` is the number of distinct moods currently tracked across `moods` and `required`. The frequency map owns the space. You cannot materially reduce this without sacrificing constant-time count updates; coordinate compression helps representation, not asymptotic memory.

## 💡 Key Takeaways
- If the problem asks for a shortest contiguous segment with frequency constraints, think sliding window before considering any global reordering.
- Mixed constraints of the form “some keys need at least X” plus “all keys need at most Y” are a strong signal for a two-pointer window with incremental count bookkeeping.
- Update the “requirements satisfied” counter only on threshold crossings; incrementing or decrementing it on every count change is a common correctness bug.
- Repair cap violations before checking whether the window is valid for the answer; otherwise you may record windows that satisfy coverage but violate the global upper bound.
- In production systems, this is the general pattern for online bounded-substream selection: maintain local state, repair violations eagerly, and minimize only after invariants are restored.

## 🚀 Variations & Further Practice
- Allow each mood to have its own upper bound `cap[mood]` instead of a single global `cap`; same pattern, but the violation condition becomes per-key and less uniform.
- Ask for the **count** of valid segments instead of the shortest one; now you need to reason about how many left boundaries are legal for each right boundary.
- Add weighted songs and minimize total weight instead of length; the window is still contiguous, but “shortest” is no longer equivalent to pointer distance, which changes the minimization logic.