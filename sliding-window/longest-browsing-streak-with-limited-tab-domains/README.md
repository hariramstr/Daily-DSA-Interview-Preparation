# Longest Browsing Streak With Limited Tab Domains

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a time-ordered array of browser domains and an integer `k`, compute the maximum length of any contiguous interval containing visits to at most `k` distinct domains. The output is a single integer: the longest valid streak length. The challenge is scale: with up to `200000` entries, any approach that enumerates all subarrays or repeatedly recomputes distinct counts is too slow. The solution must maintain validity incrementally as the window moves.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest recent interval satisfying a bounded-cardinality constraint. Examples include session analytics over clickstreams, fraud detection on merchant diversity, streaming observability over limited error classes, and network telemetry over bounded destination sets. At scale, brute-force window scans collapse under quadratic behavior and excessive recomputation. A sliding window with frequency accounting enables single-pass processing, predictable memory growth, and online operation over append-only streams. Architecturally, it is the difference between batch-only postprocessing and low-latency, continuously updated metrics that can run inside ingestion pipelines or edge services.

## 🔍 Problem Statement
You are given an array `domains` where `domains[i]` is the website domain visited at minute `i`, and an integer `k`. A browsing streak is any contiguous subarray. Return the length of the longest streak containing visits to at most `k` distinct domains.

Constraints:

- `1 <= domains.length <= 200000`
- `1 <= domains[i].length <= 30`
- `domains[i]` contains lowercase letters, digits, dots, and hyphens
- `1 <= k <= domains.length`

Examples:

- `domains = ["docs.com","mail.com","docs.com","video.com","mail.com","mail.com"], k = 2` → `3`
- `domains = ["news.com","news.com","shop.com","music.com","shop.com","shop.com","news.com"], k = 2` → `4`

Edge cases matter: `k = 1` reduces to the longest run using one domain; if `k` is at least the number of distinct domains in the full array, the answer is `domains.length`. The key constraint is input size: `O(n^2)` is not viable, so the algorithm must be linear or near-linear.

## 🪜 How to Solve This
1. Read the problem → the word **contiguous** rules out sorting or arbitrary regrouping. We need a subarray technique.
2. The condition is **at most `k` distinct domains** → that means we need to know, for the current interval, how many unique values exist. That suggests a frequency map.
3. We want the **longest** valid interval → instead of checking every start/end pair, grow a window as far as possible.
4. Start with two pointers: `left` and `right`. Move `right` forward one domain at a time, adding it into a hash map of counts.
5. If the window becomes invalid (`distinct > k`), move `left` forward, decrementing counts, until validity is restored.
6. At every step, once the window is valid again, update the best length using `right - left + 1`.
7. Why this works: each element enters the window once and leaves once. We never restart counting from scratch, so the algorithm stays linear.

This is the standard “expand until invalid, shrink until valid” sliding-window pattern.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use the **Sliding Window / Two Pointers** pattern with `left = 0`, `best = 0`, and a hash map `freq` from domain → count. The invariant we want after each shrink phase is: the current window `domains[left..right]` contains at most `k` distinct domains.

2. **Expand the window with `right`**  
   For each `right` from `0` to `n - 1`, add `domains[right]` to `freq`. If this domain was absent before, the number of distinct domains increases by one. This is correct because the map exactly tracks multiplicity inside the current window.

3. **Detect invalidity**  
   If `freq.size() > k`, the window violates the problem constraint. A larger window ending at the same `right` cannot be valid unless we remove items from the left, because adding more elements never reduces distinct count.

4. **Shrink from the left until valid**  
   While `freq.size() > k`, decrement `freq[domains[left]]`, remove the key if its count reaches zero, and increment `left`. This restores the invariant. Removing zero-count keys is essential; otherwise distinct-domain tracking is wrong.

5. **Record the best valid window**  
   Once the window is valid, compute `right - left + 1` and update `best`. This is correct because for the current `right`, `left` is the smallest index that keeps the window valid after shrinking, so the resulting valid window is maximal for that endpoint.

6. **Return `best`**  
   The algorithm is optimal because every candidate longest valid streak appears as some valid window during the scan, and each pointer moves only forward.

## 📊 Worked Example
Example: `domains = ["docs.com","mail.com","docs.com","video.com","mail.com","mail.com"]`, `k = 2`

| right | domain      | action                         | left | freq                                      | valid? | best |
|------:|-------------|--------------------------------|-----:|-------------------------------------------|--------|-----:|
| 0     | docs.com    | add                            | 0    | {docs:1}                                  | yes    | 1    |
| 1     | mail.com    | add                            | 0    | {docs:1, mail:1}                          | yes    | 2    |
| 2     | docs.com    | add                            | 0    | {docs:2, mail:1}                          | yes    | 3    |
| 3     | video.com   | add, too many distinct         | 0    | {docs:2, mail:1, video:1}                 | no     | 3    |
| 3     | video.com   | shrink: remove docs at `left`  | 1    | {docs:1, mail:1, video:1}                 | no     | 3    |
| 3     | video.com   | shrink: remove mail at `left`  | 2    | {docs:1, video:1}                         | yes    | 3    |
| 4     | mail.com    | add, then shrink once          | 3    | {video:1, mail:1}                         | yes    | 3    |
| 5     | mail.com    | add                            | 3    | {video:1, mail:2}                         | yes    | 3    |

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = domains.length`. Each domain is added to the window once by `right` and removed at most once by `left`; hash map updates are amortized `O(1)`. At `10^6` elements this is routine in memory-bound workloads; at `10^9`, linear time is still expensive but remains the only realistic asymptotic option for a single-machine scan.

### Space Complexity
`O(min(n, k))`, owned by the frequency hash map for domains currently in the window. In practice it is `O(k)` distinct active keys. You cannot reduce this below tracking current-domain membership without sacrificing constant-time updates or correctness under repeated values.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** segment under an “at most `k` distinct” constraint, think sliding window immediately.
- If validity depends on dynamic membership counts rather than order statistics, pair two pointers with a hash map of frequencies.
- The main bug source is forgetting to delete a domain when its count drops to zero; `freq.size()` then overstates distinct domains.
- Update the answer only after the shrink loop finishes; measuring window length while still invalid produces off-by-one errors.
- At scale, incremental state maintenance beats recomputation: streaming systems rely on the same pattern to enforce bounded-cardinality constraints in one pass.

## 🚀 Variations & Further Practice
- **Exactly `k` distinct domains**: harder because “at most” is monotonic but “exactly” is not; typically solved as `atMost(k) - atMost(k - 1)`.
- **Weighted windows**: each visit has a cost or duration, and the window must satisfy both distinct-domain and budget constraints; now you maintain multiple invariants simultaneously.
- **Longest substring with at most `k` distinct characters**: same pattern on strings, but useful for recognizing the abstraction independent of domain-specific naming.