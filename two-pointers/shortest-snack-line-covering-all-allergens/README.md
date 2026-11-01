# Shortest Snack Line Covering All Allergens

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given an ordered array of allergen labels `snacks` and a list `required`, find the minimum-length contiguous subarray containing every distinct label from `required` at least once. Return that length, or `-1` if no such window exists. The challenge is scale: both arrays can reach `100000` elements, so brute-force enumeration of all subarrays is not viable. The solution must exploit the contiguous-window structure and track coverage incrementally.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must find the smallest contiguous region satisfying a coverage constraint: log pipelines isolating the shortest interval containing all required event types, observability systems locating the minimal trace segment covering critical spans, search engines extracting the tightest snippet containing all query terms, and streaming compliance scans identifying the smallest segment containing mandated markers. At scale, naive rescans create quadratic blowups, cache churn, and latency spikes. Sliding-window frequency tracking turns an otherwise batch-heavy problem into a linear pass with bounded state, which is exactly what makes it production-safe for high-volume streams and large in-memory datasets.

## 🔍 Problem Statement
You are given:

- `snacks`: an array of strings, where `snacks[i]` is the allergen label at position `i`
- `required`: an array of strings representing allergen labels that must all appear in the inspection window

Treat `required` as a set of distinct labels, even if duplicates appear in the input. Return the length of the shortest contiguous subarray of `snacks` containing every required allergen at least once. If no such subarray exists, return `-1`.

Constraints:

- `1 <= snacks.length <= 100000`
- `1 <= required.length <= 100000`
- Labels contain lowercase English letters
- Total characters across all strings are at most `300000`

Examples:

- `snacks = ["dairy","soy","nuts","gluten","soy","dairy"]`, `required = ["soy","nuts","dairy"]` → `3`
- `snacks = ["fruit","seed","dairy","fruit","seed"]`, `required = ["dairy","soy"]` → `-1`

The key constraint is input size: any approach worse than near-linear time will fail.

## 🪜 How to Solve This
1. Read the problem → the answer is a **contiguous** segment, so this is immediately a window problem, not a subset or sorting problem.

2. We need the **shortest** window that satisfies “contains all required labels.” That usually means:
   - expand until valid
   - contract while still valid
   - record the minimum

3. Since `required` behaves like a set, deduplicate it first. Otherwise duplicate entries in `required` would incorrectly inflate the target.

4. We need to know when a window becomes valid. A hash set tells us what matters; a hash map tells us how many of each required label are inside the current window.

5. Track a `formed` count: how many distinct required labels currently appear at least once in the window. When `formed == targetSize`, the window is valid.

6. Move the right pointer once across the array. For each new snack, update counts if it is required.

7. Once valid, move the left pointer rightward as aggressively as possible without losing coverage. That shrinking step is what guarantees minimality for each right boundary.

8. This gives a single-pass sliding window with frequency tracking: linear time, bounded auxiliary state, and no redundant rescans.

## 🧩 Algorithm Walkthrough
1. **Deduplicate `required` into a set.**  
   This defines the exact coverage target. Correctness depends on treating `required` as distinct labels, not multiplicities. Invariant: `targetSet` contains every allergen that must appear at least once.

2. **Initialize sliding-window state.**  
   Use two pointers `left` and `right`, a frequency map `windowCount`, and an integer `formed` for how many required labels are currently satisfied. Invariant: `windowCount[x]` reflects occurrences of required label `x` inside `snacks[left..right]`.

3. **Expand with the right pointer.**  
   For each `snacks[right]`, if it belongs to `targetSet`, increment its count. If its count changes from `0` to `1`, increment `formed`. This is the key coverage transition: a required label becomes present.

4. **Check validity.**  
   When `formed == targetSet.size`, the current window covers all required allergens. This is the central invariant of the Two Pointers / Sliding Window pattern: validity can be updated incrementally rather than recomputed from scratch.

5. **Shrink with the left pointer.**  
   While the window remains valid, update the best answer with `right - left + 1`, then try removing `snacks[left]`. If that label is required, decrement its count; if it drops from `1` to `0`, decrement `formed` because coverage was lost. Then advance `left`.

6. **Repeat until `right` reaches the end.**  
   Every element enters the window once and leaves once. That is why the pattern is linear. The abstraction fits because the problem asks for an extremal contiguous segment under a monotonic validity condition.

7. **Return the result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum recorded length.

## 📊 Worked Example
Example: `snacks = ["dairy","soy","nuts","gluten","soy","dairy"]`, `required = ["soy","nuts","dairy"]`

Target set = `{dairy, soy, nuts}`, target size = `3`

| Step | right | snack   | left | window counts                  | formed | valid | best |
|------|-------|---------|------|--------------------------------|--------|-------|------|
| 1    | 0     | dairy   | 0    | dairy:1                        | 1      | no    | ∞    |
| 2    | 1     | soy     | 0    | dairy:1, soy:1                 | 2      | no    | ∞    |
| 3    | 2     | nuts    | 0    | dairy:1, soy:1, nuts:1         | 3      | yes   | 3    |
| 4    | shrink| remove dairy | 1 | dairy:0, soy:1, nuts:1     | 2      | no    | 3    |
| 5    | 3     | gluten  | 1    | soy:1, nuts:1                  | 2      | no    | 3    |
| 6    | 4     | soy     | 1    | soy:2, nuts:1                  | 2      | no    | 3    |
| 7    | 5     | dairy   | 1    | dairy:1, soy:2, nuts:1         | 3      | yes   | 3    |

The first valid window already has length `3`, which is optimal because no window can be shorter than the number of distinct required labels.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = snacks.length` and `m = required.length`. Deduplicating `required` costs `O(m)`, and the sliding window processes each snack at most twice: once when the right pointer includes it and once when the left pointer excludes it. At `10^6` scale this is practical; at `10^9`, memory bandwidth and string hashing dominate.

### Space Complexity
`O(k)`, where `k` is the number of distinct required allergens. The set of required labels and the window frequency map own the extra space. This cannot be meaningfully reduced without sacrificing constant-time membership and count updates.

## 💡 Key Takeaways
- If the problem asks for the shortest or longest **contiguous** segment satisfying a coverage condition, think sliding window before considering nested loops.
- If validity depends on presence/counts of a small tracked subset, pair Two Pointers with a hash map and a “formed vs target” counter.
- Deduplicate `required` first; otherwise repeated labels in the input specification will corrupt the coverage target.
- Update the answer **before** removing `snacks[left]` during shrink; reversing that order causes off-by-one errors on minimal valid windows.
- The production lesson is broader than interviews: incremental validity tracking is what turns repeated full-window recomputation into stable linear-time behavior under large streams.

## 🚀 Variations & Further Practice
- Require each allergen to appear with a minimum multiplicity, not just once. The twist is that validity depends on meeting per-key thresholds rather than binary presence.
- Find the shortest window after allowing up to `k` missing required labels. The harder part is redefining validity and deciding when shrinking is still safe.
- Solve the same problem in an online stream with bounded retention. The challenge shifts from pure algorithmics to state eviction, watermarking, and incomplete future knowledge.