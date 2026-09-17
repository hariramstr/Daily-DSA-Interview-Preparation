# Longest Citation Window With Per-Author Cap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an ordered array of author IDs, find the maximum-length contiguous window in which every distinct author appears at most `limit` times. Return only that window length. The challenge is not counting globally, but maintaining per-author frequency constraints while scanning a very large sequence of arbitrary strings. Any solution that rechecks counts across candidate subarrays will collapse under the `2 * 10^5` input bound.

## 🌍 Engineering Impact
This pattern shows up anywhere a streaming system must enforce local frequency caps over ordered events: per-tenant rate limiting, abuse detection, search result diversification, recommendation deduplication, log anomaly windows, and compiler or query planners that bound repeated symbols within spans. At scale, brute-force rescans create quadratic behavior, cache churn, and unpredictable latency tails. A sliding-window design turns the problem into incremental state maintenance: one event enters, one leaves, counts update in O(1). That enables online processing, bounded memory growth relative to active keys, and predictable throughput under bursty distributions.

## 🔍 Problem Statement
You are given `authors`, where `authors[i]` is the author ID cited at position `i` in a document, and an integer `limit`. Find the length of the longest contiguous subarray such that every author appears at most `limit` times inside that subarray.

Formally, maximize `r - l + 1` over all `0 <= l <= r < authors.length` such that for every author ID `x`, its frequency in `authors[l..r]` is at most `limit`.

Constraints:
- `1 <= authors.length <= 2 * 10^5`
- `1 <= authors[i].length <= 20`
- IDs contain lowercase letters, digits, or `_`
- `1 <= limit <= authors.length`

Examples:
- `authors = ["lee","kim","lee","patel","kim","lee","ng"], limit = 2` → `5`
- `authors = ["a","b","a","c","a","b","b","d"], limit = 1` → `3`

The decisive constraint is input size: nested scans over subarrays are too expensive, so the algorithm must update validity incrementally in near-linear time.

## 🪜 How to Solve This
1. Read the requirement carefully → the subarray must be contiguous, so this is not a global counting problem and not something sorting can help with.

2. Notice the validity rule is local to a moving range → “every author count within the current window must stay `<= limit`.” That is a classic sliding-window signal.

3. Ask what changes when the window moves by one position:
   - expanding right adds exactly one author
   - shrinking left removes exactly one author  
   This means frequencies can be maintained incrementally with a hash map.

4. Observe an important asymmetry → adding a new rightmost author can only break the constraint for that one author. You do not need to revalidate every key.

5. So the strategy becomes:
   - grow the window by moving `right`
   - increment that author’s count
   - while that count exceeds `limit`, move `left` forward and decrement counts until the window is valid again

6. Once valid, update the best length. Each index moves forward at most once, which is why the approach stays linear instead of degenerating into repeated rescans.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use the **Sliding Window / Two Pointers** pattern with `left = 0`, `best = 0`, and a hash map `freq` from author ID to count. This abstraction fits because we need the longest contiguous region satisfying an incremental constraint.

2. **Expand the window**  
   Iterate `right` from `0` to `authors.length - 1`. For each `authors[right]`, increment `freq[authors[right]]`. This represents adding one citation to the active window `[left, right]`.

3. **Detect violation locally**  
   After insertion, only the count of `authors[right]` could have crossed `limit`. No other author’s frequency increased, so no other key can become newly invalid. This is the key observation that avoids full-map validation.

4. **Shrink until valid**  
   While `freq[authors[right]] > limit`, decrement `freq[authors[left]]` and advance `left`. This removes citations from the window’s front until the violating author is back within the cap.

5. **Maintain invariant**  
   After the shrink loop finishes, the invariant is: every author in `authors[left..right]` appears at most `limit` times. Because we only stop shrinking when the sole possible violation is fixed, the entire window is valid.

6. **Update the optimum**  
   Compute `right - left + 1` and update `best` if larger. Since the current window is valid and as wide as possible for this `right`, it is a legitimate candidate.

7. **Why it is efficient**  
   Each element is added once when `right` advances and removed at most once when `left` advances. That gives linear pointer movement overall, with O(1) average hash-map updates.

## 📊 Worked Example
Example: `authors = ["lee","kim","lee","patel","kim","lee","ng"]`, `limit = 2`

| right | author | action | left after shrink | freq snapshot | window len | best |
|---|---|---|---:|---|---:|---:|
| 0 | lee | add lee | 0 | lee:1 | 1 | 1 |
| 1 | kim | add kim | 0 | lee:1, kim:1 | 2 | 2 |
| 2 | lee | add lee | 0 | lee:2, kim:1 | 3 | 3 |
| 3 | patel | add patel | 0 | lee:2, kim:1, patel:1 | 4 | 4 |
| 4 | kim | add kim | 0 | lee:2, kim:2, patel:1 | 5 | 5 |
| 5 | lee | add lee, violates | 1 | lee:2, kim:2, patel:1 | 5 | 5 |
| 6 | ng | add ng | 1 | lee:2, kim:2, patel:1, ng:1 | 6 | 6 |

At `right = 5`, `lee` becomes `3`, so we shrink from the left, removing the first `"lee"`. Validity is restored immediately. The final best window is `["kim","lee","patel","kim","lee","ng"]`, length `6`.

## ⏱ Complexity Analysis
### Time Complexity
Average-case **O(n)**, where `n = authors.length`. Each citation enters the window once and leaves at most once, so total pointer movement is linear. Hash-map increments/decrements are O(1) average. At `10^6` elements this remains practical; at `10^9`, the algorithmic shape is still right, but memory bandwidth and runtime become the real bottlenecks.

### Space Complexity
**O(k)**, where `k` is the number of distinct authors currently tracked, bounded by the number of distinct IDs in the input. The hash map owns this space. You can reduce constant factors with integer ID compression, but not the asymptotic bound without losing exactness.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** segment under a mutable frequency constraint, think sliding window before considering prefix sums or sorting.
- If adding one element can only invalidate the window through that element’s key, a hash map plus two pointers is usually the right abstraction.
- Update `best` only **after** the shrink loop; doing it before restoring validity records illegal windows.
- Be careful with the shrink condition: it is `while freq[current] > limit`, not `if`, because one removal may not be enough.
- The production-grade insight is incremental constraint maintenance: enforce policy by updating state on boundary changes, not by rescanning the full active set.

## 🚀 Variations & Further Practice
- Return the actual window indices, and break ties by earliest start or lexicographically smallest window. The twist is preserving deterministic tie-breaking without disturbing linear complexity.
- Allow each author to have its own cap, e.g. `limitByAuthor[id]`. The pattern stays the same, but the validity rule becomes per-key dynamic rather than globally uniform.
- Find the longest window where **at most `m` authors** exceed their cap. This is harder because validity is no longer tied to a single offending key; you must track the number of violated keys globally.