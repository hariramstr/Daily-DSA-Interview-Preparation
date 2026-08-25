# Longest Chat Window With Bounded Emoji Variety

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array of emoji reaction strings and an integer `k`, find the maximum length of any contiguous subarray containing at most `k` distinct emoji types. The output is a single integer: that longest valid window length. The challenge is not correctness but scale: with up to 200,000 reactions, enumerating all subarrays is too slow, so the solution must maintain validity incrementally as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent segment satisfying a bounded-cardinality constraint: log analytics over event types, streaming fraud detection over merchant categories, sessionization in clickstreams, and observability pipelines tracking service or error-code diversity. In production, brute force collapses under sustained throughput because every new event would trigger quadratic rescans. The sliding-window formulation enables single-pass processing, predictable memory tied to active distinct keys, and straightforward adaptation to online streams. Architecturally, it is the difference between per-batch recomputation and stateful incremental evaluation that can run continuously under load.

## 🔍 Problem Statement
You are given:

- `reactions`: an array of strings, where each string is one emoji code
- `k`: the maximum number of distinct emoji types allowed in a contiguous window

Return the length of the longest contiguous subarray containing at most `k` distinct strings.

Constraints:

- `0 <= reactions.length <= 200000`
- `0 <= k <= reactions.length`
- Each `reactions[i]` is a non-empty string of length `1` to `20`
- Strings contain visible ASCII characters

Edge cases matter:

- If `k == 0`, return `0`
- If `reactions` is empty, return `0`

Examples:

- `reactions = [":smile:",":fire:",":smile:",":heart:",":fire:",":fire:"], k = 2` → `3`
- `reactions = [":ok:",":ok:",":wave:",":wave:",":wave:",":star:"], k = 1` → `3`

The key constraint is input size: checking every subarray is `O(n^2)`, which is not viable at 200k elements.

## 🪜 How to Solve This
1. Read the problem → the word *contiguous* is the signal that order matters and sorting is illegal. We need a moving range over the original array.

2. The rule is “at most `k` distinct emoji types” → that means we must track counts of items currently inside a window. Distinctness suggests a `HashMap<String, Int>`.

3. Start with a window `[left, right]` and expand `right` one step at a time. Each new emoji either increases an existing count or introduces a new distinct type.

4. If the window now has more than `k` distinct types, it is invalid. The only way to repair it without skipping candidates is to move `left` forward, decrementing counts until distinct types drop back to `k`.

5. At every point where the window is valid, compute its length and keep the maximum.

6. Why this works: each index enters the window once and leaves once. That turns what looks like nested iteration into linear work.

7. The mental model is not “search all subarrays”; it is “maintain the largest valid suffix ending at each position.”

## 🧩 Algorithm Walkthrough
1. **Handle trivial cases early.**  
   If `k == 0` or `reactions` is empty, return `0`. This is correct because no non-empty window can satisfy zero allowed distinct types, and an empty input has no subarrays.

2. **Initialize the sliding window state.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`, a frequency map, and `maxLen = 0`. The map stores counts of emoji strings currently inside the window. The invariant is: counts always reflect exactly the range `[left, right]`.

3. **Expand the window by advancing `right`.**  
   For each `reactions[right]`, increment its count in the map. This may keep the distinct count unchanged or increase it by one. At this point, the window may become invalid.

4. **Shrink until the constraint is restored.**  
   While the map contains more than `k` distinct keys, decrement `reactions[left]`, remove that key if its count reaches zero, and increment `left`. This is correct because any valid window ending at `right` must start at or after the first position that restores the distinct-count bound.

5. **Record the best valid window.**  
   Once the window is valid again, update `maxLen = max(maxLen, right - left + 1)`. The invariant now is stronger: `[left, right]` is the longest valid window ending at `right` that starts no earlier than necessary.

6. **Return `maxLen`.**  
   Every candidate window is considered implicitly during expansion and contraction, but no index is processed more than twice, which is why this abstraction is the right fit.

## 📊 Worked Example
Example: `reactions = [":smile:",":fire:",":smile:",":heart:",":fire:",":fire:"]`, `k = 2`

| right | emoji     | action after add                  | left after shrink | counts                               | maxLen |
|------:|-----------|-----------------------------------|-------------------|--------------------------------------|-------:|
| 0     | :smile:   | add `:smile:`                     | 0                 | {`:smile:`: 1}                       | 1      |
| 1     | :fire:    | add `:fire:`                      | 0                 | {`:smile:`: 1, `:fire:`: 1}          | 2      |
| 2     | :smile:   | increment `:smile:`               | 0                 | {`:smile:`: 2, `:fire:`: 1}          | 3      |
| 3     | :heart:   | add third distinct, shrink        | 2                 | {`:smile:`: 1, `:heart:`: 1}         | 3      |
| 4     | :fire:    | add third distinct, shrink        | 3                 | {`:heart:`: 1, `:fire:`: 1}          | 3      |
| 5     | :fire:    | increment `:fire:`                | 3                 | {`:heart:`: 1, `:fire:`: 2}          | 3      |

Longest valid length is `3`, from `[:smile:,:fire:,:smile:]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = reactions.length`. Each reaction is added to the window once when `right` advances and removed at most once when `left` advances. Hash map updates dominate and are amortized constant time. At `10^6` elements this remains practical; at `10^9`, linear time is still expensive but fundamentally better than quadratic impossibility.

### Space Complexity
`O(min(n, k))` in practice, or more precisely `O(d)` where `d` is the number of distinct emoji types in the current window. The frequency map owns the space. You cannot reduce this asymptotically without losing constant-time distinct-count maintenance.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an **at most / at least constraint**, sliding window should be your first candidate.
- If validity depends on **frequency or distinct-count state inside a range**, a hash map plus two pointers is usually the right abstraction.
- Be careful to remove a key from the map when its count reaches zero; decrementing without deletion leaves the distinct count wrong.
- Update the answer only after restoring the window to validity; recording length before the shrink loop produces inflated results.
- In production streaming systems, this pattern matters because it converts repeated global rescans into bounded incremental state updates.

## 🚀 Variations & Further Practice
- **Longest substring with at most `k` distinct characters**: same pattern, but on a string; the twist is tighter constant-factor optimization and character-domain assumptions.
- **Minimum window substring**: still sliding window with counts, but now the objective is smallest valid window and validity depends on satisfying required frequencies, not just bounded distinctness.
- **Subarrays with exactly `k` distinct elements**: harder because “exactly” is awkward directly; the standard trick computes `atMost(k) - atMost(k - 1)`.