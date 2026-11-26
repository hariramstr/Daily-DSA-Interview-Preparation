# Shortest Comment Burst Covering Required Reactions

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a time-ordered array of reaction strings and a multiset of required reaction counts, find the minimum-length contiguous window whose contents satisfy every required count. Return that minimum length, or `-1` if no such window exists. The challenge is multiplicity: this is not “contains these labels once,” but “contains each label at least *k* times,” under input sizes large enough that checking all subarrays is infeasible.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, moderation pipelines, fraud detection, and observability systems: find the smallest time span or event burst that satisfies a threshold profile. Examples include alert windows containing enough distinct error signatures, ad-serving sessions meeting impression quotas, or security streams matching a rule set with per-event cardinalities. At production scale, brute force collapses under quadratic scans and repeated recounting. A linear sliding-window design enables online processing, bounded per-event work, and predictable latency under high-cardinality keys, which matters for real-time dashboards, ranking features, and policy enforcement.

## 🔍 Problem Statement
You are given `reactions`, where `reactions[i]` is the reaction type posted at second `i`, and `required`, where each entry is `[type, count]`. A valid answer is the length of the shortest contiguous subarray whose frequency map contains at least the required count for every required type. If no such window exists, return `-1`.

If a reaction type appears multiple times in `required`, its counts must be summed first. Reaction types not mentioned in `required` may appear in the chosen window and should be ignored for validation.

Constraints are large: `reactions.length <= 200000`, `required.length <= 100000`, and required counts can be as large as `10^9`. That rules out any algorithm that enumerates subarrays or recomputes counts from scratch.

Examples:

- `reactions = ["like","wow","sad","like","heart","wow","like","heart"]`  
  `required = [["like",2],["heart",1],["wow",1]]` → `4`

- `reactions = ["fire","clap","fire","laugh","clap","fire"]`  
  `required = [["fire",2],["clap",2],["boo",1]]` → `-1`

## 🪜 How to Solve This
1. Read the requirement carefully → this is a **minimum window** problem, but over strings with **per-key counts**, not a simple set-membership check.

2. Brute force fails immediately → there are `O(n^2)` subarrays, and validating each by recounting makes it worse. With `n = 200000`, that is not operationally relevant.

3. The key observation: as the right boundary expands, counts only increase; as the left boundary contracts, counts only decrease. That monotonic behavior suggests **Two Pointers / Sliding Window**.

4. Preprocess `required` into a single map because duplicate keys must be merged. Then track a second map for the current window.

5. Instead of rechecking every required key after each move, maintain a scalar: how many required types are currently satisfied. A type becomes “satisfied” exactly when its window count reaches its required threshold, and becomes “unsatisfied” when shrinking drops it below threshold.

6. Expand right until all requirements are satisfied → then shrink left aggressively while preserving validity. Each time the window is valid, record its length.

7. Because each pointer only moves forward, total work is linear in the stream size, with hash-map operations handling sparse, high-cardinality reaction types efficiently.

## 🧩 Algorithm Walkthrough
1. **Aggregate requirements.**  
   Build `need[type] += count` from `required`. This is necessary because the same reaction type may appear multiple times. The invariant after this step: `need` is the canonical requirement map.

2. **Initialize sliding-window state.**  
   Use two pointers `left = 0`, `right` scanning from `0..n-1`. Maintain `have[type]` for counts inside the current window and `formed`, the number of required types whose count currently meets `need[type]`. Let `target = need.size`.

3. **Expand the window with `right`.**  
   For each `reactions[right]`, if the type exists in `need`, increment `have[type]`. If `have[type]` becomes exactly `need[type]`, increment `formed`.  
   Why correct: a requirement should contribute to validity once, at threshold crossing, not for every extra occurrence.

4. **Check validity.**  
   When `formed == target`, the current window satisfies all constraints. This is the core invariant: every required type has enough occurrences in `have`.

5. **Shrink from the left.**  
   While the window remains valid, update the best answer with `right - left + 1`, then move `left` forward. If `reactions[left]` is required, decrement its count in `have`; if it drops from `need[type]` to `need[type] - 1`, decrement `formed`.  
   Why correct: this finds the smallest valid window ending at the current `right`.

6. **Continue until the stream ends.**  
   Since each index enters and leaves the window at most once, this **Two Pointers / Sliding Window** pattern gives linear traversal with hash-based counting.

7. **Return result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum recorded length.

## 📊 Worked Example
Example: `reactions = ["like","wow","sad","like","heart","wow","like","heart"]`  
`need = {like:2, heart:1, wow:1}`

| right | reaction | have (relevant) | formed | action |
|---|---|---|---:|---|
| 0 | like  | like=1 | 0 | not valid |
| 1 | wow   | like=1,wow=1 | 1 | wow satisfied |
| 2 | sad   | unchanged relevantly | 1 | ignore |
| 3 | like  | like=2,wow=1 | 2 | like satisfied |
| 4 | heart | like=2,wow=1,heart=1 | 3 | valid |
| 4 | shrink from left | remove like at 0 → like=1 | 2 | window `[0..4]` len 5 recorded |
| 5 | wow   | like=1,wow=2,heart=1 | 2 | still invalid |
| 6 | like  | like=2,wow=2,heart=1 | 3 | valid again |
| 6 | shrink | remove wow at 1, remove sad at 2, remove like at 3 | drops after removing like | 2 | best valid window `[3..6]` len 4 |

Answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = reactions.length` and `m = required.length`. Building the requirement map is linear in `m`; the sliding window moves each pointer across the stream at most once, so the dominant cost is linear hash-map updates. This scales to millions of events; it does not scale to billions without distribution or streaming partitioning.

### Space Complexity
`O(k)`, where `k` is the number of distinct required reaction types. The space is owned by the `need` and `have` hash maps. You can avoid storing counts for non-required types entirely; reducing below `O(k)` would require sacrificing constant-time updates or exactness.

## 💡 Key Takeaways
• If the problem asks for a **shortest contiguous segment** satisfying frequency thresholds, think **sliding window with counts**, not prefix sums or sorting.  
• If validity depends on **per-key multiplicity** rather than total size, track a requirement map plus a scalar like `formed`, not repeated full-map comparisons.  
• Merge duplicate entries in `required` before processing; forgetting this silently produces undercounted thresholds and incorrect windows.  
• Update `formed` only on exact threshold crossings (`need[x]-1 -> need[x]` and `need[x] -> need[x]-1`); using `>=` carelessly causes double-counting bugs.  
• The production lesson is to convert expensive global validation into cheap incremental state transitions; that is the difference between quadratic rescans and stable linear throughput.

## 🚀 Variations & Further Practice
- Return the actual window indices or subarray, not just the length; same pattern, but now tie-breaking and output semantics matter.
- Add weighted events or per-type costs, where validity is count-based but optimization target is total cost instead of length; the shrink condition becomes less straightforward.
- Process an unbounded stream with expiring events by wall-clock time rather than array index; same counting model, but now the window is driven by timestamps and online eviction.