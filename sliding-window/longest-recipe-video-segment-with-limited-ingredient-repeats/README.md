# Longest Recipe Video Segment With Limited Ingredient Repeats

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `ingredients` where `ingredients[i]` is the ingredient mentioned at second `i`, find the length of the longest contiguous segment in which every distinct ingredient appears at most `k` times. Return only that maximum length. The challenge is that the segment must stay contiguous and the input can be as large as 200,000 elements, which rules out recomputing frequencies for every candidate range.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must enforce bounded repetition inside a moving range: per-user request caps in streaming rate-limiters, duplicate suppression in event pipelines, token-frequency windows in search/query analytics, and bounded symbol reuse in compiler or log-processing passes. At scale, brute-force range validation collapses under quadratic behavior and cache-unfriendly rescans. A sliding window with incremental counts turns the problem into a single-pass state machine: predictable memory growth, linear throughput, and a design that composes cleanly with online processing where data arrives continuously rather than as a fully materialized batch.

## 🔍 Problem Statement
You are given a string array `ingredients` and an integer `k`. Each index represents one second of a cooking video, and the value at that index is the ingredient mentioned at that second. You must choose one contiguous segment such that, within that segment, every distinct ingredient appears no more than `k` times. Return the maximum possible segment length.

Constraints:

- `1 <= ingredients.length <= 200000`
- `1 <= ingredients[i].length <= 20`
- `ingredients[i]` contains lowercase English letters
- `1 <= k <= ingredients.length`

Examples:

- `ingredients = ["salt","pepper","salt","oil","salt","pepper"], k = 2` → `4`
- `ingredients = ["egg","egg","milk","egg","milk","milk","flour"], k = 2` → `5`

The key constraint is input size: with up to 200,000 timestamps, any approach that checks many subarrays explicitly is too slow. The algorithm must update validity incrementally as the window moves.

## 🪜 How to Solve This
1. Read the requirement carefully → we need the **longest contiguous range** satisfying a frequency cap.
2. “Contiguous” usually points to a **window**, not arbitrary grouping or sorting.
3. The validity rule depends on **counts of values inside the current range** → we need a `HashMap<ingredient, frequency>`.
4. Start expanding the right boundary one step at a time. Each new ingredient updates its count.
5. The only way the window becomes invalid is when the newly added ingredient exceeds `k`. That is the key simplification: we do not need to revalidate every ingredient on every step.
6. Once invalid, shrink from the left until the offending count is back within limit.
7. After restoration, the current window is valid again, so update the best length.
8. Because each element enters the window once and leaves once, total work stays linear.

This is the standard “grow until constraint breaks, then shrink just enough” sliding-window pattern. The insight is that validity is monotonic with respect to shrinking: removing elements cannot make an over-limit count worse.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use two pointers: `left = 0` and a loop variable `right`. Maintain a hash map `freq` storing counts for ingredients in the current window `[left, right]`. Also track `best = 0`.

2. **Expand the window**  
   For each `right`, add `ingredients[right]` to `freq`. This represents extending the segment by one second. The window may remain valid or may become invalid only because this ingredient’s count just increased.

3. **Detect invalidity locally**  
   If `freq[ingredients[right]] <= k`, the window is still valid. If it becomes `k + 1`, the window violates the constraint. No other ingredient can have become invalid at this step, because only one count changed.

4. **Shrink from the left until valid**  
   While `freq[ingredients[right]] > k`, decrement `freq[ingredients[left]]` and move `left` forward. This preserves the invariant that `freq` always matches the current window contents exactly.

5. **Maintain the core invariant**  
   After the shrink loop, every ingredient in `[left, right]` appears at most `k` times. That makes the window valid, and it is the longest valid window ending at `right`, because `left` was moved only as far as necessary.

6. **Update the answer**  
   Compute `right - left + 1` and maximize `best`. Repeat until the array is exhausted.

This is a classic **Two Pointers / Sliding Window** problem because the valid region moves monotonically forward. The abstraction is correct because the constraint can be repaired by only advancing the left boundary, never by revisiting earlier positions.

## 📊 Worked Example
Example: `ingredients = ["salt","pepper","salt","oil","salt","pepper"], k = 2`

| right | ingredient | action | left | freq snapshot | valid length | best |
|---|---|---|---:|---|---:|---:|
| 0 | salt | add | 0 | salt:1 | 1 | 1 |
| 1 | pepper | add | 0 | salt:1, pepper:1 | 2 | 2 |
| 2 | salt | add | 0 | salt:2, pepper:1 | 3 | 3 |
| 3 | oil | add | 0 | salt:2, pepper:1, oil:1 | 4 | 4 |
| 4 | salt | add, now salt=3 → shrink | 1 | salt:2, pepper:1, oil:1 | 4 | 4 |
| 5 | pepper | add | 1 | salt:2, pepper:2, oil:1 | 5 | 5 |

At `right = 4`, adding `"salt"` breaks the rule, so we remove from the left until `"salt"` returns to count `2`. After that repair, the window is valid again. Final answer: `5`, from `["pepper","salt","oil","salt","pepper"]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = ingredients.length`. Each ingredient is added to the window once when `right` advances and removed at most once when `left` advances. Hash map updates are `O(1)` average case, so the dominant cost is a single linear scan. This remains practical at `10^6` elements; at `10^9`, throughput and memory bandwidth become the real bottlenecks.

### Space Complexity
`O(m)`, where `m` is the number of distinct ingredients present in the current window, bounded by the total distinct ingredients in the input. The hash map owns this space. It cannot be reduced asymptotically without sacrificing constant-time frequency updates or introducing rescans.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a frequency constraint, think **sliding window with counts** immediately.
- If validity changes only when one boundary moves, and shrinking can restore validity monotonically, **two pointers** is usually the right abstraction.
- Update the answer **after** restoring validity; recording length before the shrink loop produces inflated results.
- The shrink condition should target the offending count precisely: `while freq[current] > k`, not a broader or stale condition.
- The production lesson is incremental state maintenance: keeping exact rolling counts turns expensive repeated validation into a linear online algorithm.

## 🚀 Variations & Further Practice
- Find the longest segment with **at most `d` distinct ingredients** instead of per-ingredient count caps. The twist is tracking distinct-key cardinality, not just one offending frequency.
- Find the longest segment where **each ingredient appears between `L` and `K` times**. The harder part is that validity is no longer repaired by a single local condition.
- Process an unbounded stream and continuously emit the best valid window so far. The twist is operational: online updates, bounded memory expectations, and no full-input lookahead.