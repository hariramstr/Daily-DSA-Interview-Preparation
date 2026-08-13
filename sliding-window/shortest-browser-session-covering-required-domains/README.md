# Shortest Browser Session Covering Required Domains

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given a browsing history array `visits` and a requirement map `need`, find the minimum-length contiguous subarray whose domain frequencies meet or exceed every required count in `need`. Return that shortest length, or `-1` if no such session exists. The challenge is scale: `visits` can reach 200,000 entries, so checking all subarrays is infeasible. The solution must maintain coverage state incrementally and run close to linear time.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, fraud detection, SIEM pipelines, ad attribution, and observability systems where you need the smallest time-bounded segment satisfying a multi-key threshold. Examples include detecting the shortest suspicious request burst across required hosts, finding the minimal trace span containing mandatory services, or isolating the smallest clickstream segment covering campaign events. Without a sliding-window approach, systems fall back to quadratic scans, which collapse under high-cardinality logs and long event streams. The right abstraction enables online processing, bounded per-event work, and predictable latency under skewed distributions.

## 🔍 Problem Statement
You are given:

- `visits`: an array where `visits[i]` is the domain opened at minute `i`
- `need`: a map from domain → required frequency

Find the length of the shortest contiguous subarray such that for every domain `d` in `need`, the window contains at least `need[d]` occurrences of `d`. Domains not present in `need` are irrelevant and may appear any number of times. If no valid window exists, return `-1`.

Constraints:

- `1 <= visits.length <= 200000`
- `1 <= need.size <= 50000`
- `sum(need.values()) <= visits.length`
- domain names contain lowercase letters, digits, dots, and hyphens
- `1 <= need[d] <= 100000`

Example 1:

- `visits = ["news.com","mail.com","shop.com","news.com","video.com","mail.com","news.com"]`
- `need = {"news.com": 2, "mail.com": 1}`
- Output: `4`

Example 2:

- `visits = ["a.com","b.com","a.com","c.com","b.com"]`
- `need = {"a.com": 2, "b.com": 2, "d.com": 1}`
- Output: `-1`

The decisive constraint is input size: any `O(n^2)` enumeration of sessions will time out.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “contains these domains once,” but “contains each domain at least `k` times.” That immediately suggests frequency tracking, not set membership.

2. The subarray must be contiguous → sorting or reordering is invalid. We need a technique that explores candidate ranges in-place over the original timeline.

3. We want the *shortest* valid range → classic signal for a grow-then-shrink sliding window:
   - expand right until the window becomes valid
   - then contract left as much as possible while preserving validity

4. To know whether a window is valid efficiently, maintain:
   - a frequency map for domains inside the current window
   - a counter of how many required domains currently meet their target

5. Each right move adds one domain; each left move removes one. If we update validity incrementally, every index enters and leaves the window at most once.

6. That gives a near-linear solution: two pointers over `visits`, plus hash maps for required counts and current counts. The key insight is that validity is monotonic with expansion and only breaks at specific removals during contraction.

## 🧩 Algorithm Walkthrough
1. **Initialize requirement state using a Hash Map.**  
   Store `need[d]` for each required domain. Also track `requiredKinds = need.size`, the number of distinct domains that must be satisfied. This defines the validity target.

2. **Maintain a sliding window with Two Pointers.**  
   Use `left = 0` and iterate `right` from `0` to `n - 1`. The window is always `visits[left..right]`. This pattern is correct because the problem asks for a contiguous minimum-length segment under a monotonic coverage condition.

3. **Expand the window by adding `visits[right]`.**  
   If the domain is in `need`, increment its count in a `window` map. When `window[d]` becomes exactly `need[d]`, increment `formed`.  
   **Invariant:** `formed` equals the number of required domains whose counts currently meet the threshold.

4. **Check validity.**  
   When `formed == requiredKinds`, the current window satisfies all requirements. At this point, record its length as a candidate answer.

5. **Shrink from the left while still valid.**  
   Repeatedly try removing `visits[left]`. Before advancing `left`, if that domain is required and its count is exactly at the threshold, removing it will break validity, so decrement `formed` after decrementing the count.  
   **Invariant:** after each contraction, the window remains the smallest valid window for the current `right` until validity breaks.

6. **Continue until the scan ends.**  
   Because each pointer only moves forward, total work is linear in `visits.length`, aside from hash-map operations.

7. **Return the result.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum recorded length.

## 📊 Worked Example
Use:

- `visits = ["news.com","mail.com","shop.com","news.com","video.com","mail.com","news.com"]`
- `need = {"news.com": 2, "mail.com": 1}`

| right | domain      | window counts (`news`,`mail`) | formed | action |
|------:|-------------|-------------------------------:|-------:|--------|
| 0 | news.com  | (1,0) | 0 | not valid |
| 1 | mail.com  | (1,1) | 1 | `mail` satisfied |
| 2 | shop.com  | (1,1) | 1 | irrelevant domain |
| 3 | news.com  | (2,1) | 2 | valid, answer = 4 (`0..3`) |
| shrink | remove news.com | (1,1) | 1 | invalid, stop shrinking |
| 4 | video.com | (1,1) | 1 | not valid |
| 5 | mail.com  | (1,2) | 1 | extra mail, still missing news |
| 6 | news.com  | (2,2) | 2 | valid again |

Now shrink:
- remove `mail.com` at left=1 → `(2,1)`, still valid, window `2..6`, length `5`
- remove `shop.com` → still valid, window `3..6`, length `4`
- remove `news.com` → `(1,1)`, invalid

Minimum found is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` average time, where `n = visits.length`. Each element is processed at most twice: once when `right` includes it and once when `left` excludes it. Hash-map updates dominate but remain constant-time on average. At `10^6` elements this is practical; at `10^9`, throughput and memory locality become the real bottlenecks.

### Space Complexity
`O(m)` where `m = need.size`, plus at most the number of required domains currently present in the window map. The space is owned by the frequency hash maps. It can be reduced only by domain ID compression, trading readability and preprocessing for better cache behavior.

## 💡 Key Takeaways
- If the problem asks for the shortest contiguous range satisfying per-key frequency thresholds, think sliding window with incremental validity tracking.
- When validity depends on “at least these counts,” a `formed == requiredKinds` counter is usually cleaner than recomputing full-map equality on every step.
- Only increment `formed` when a count reaches the threshold exactly; extra occurrences must not double-count satisfaction.
- During contraction, update the answer before removing `visits[left]`, or you risk missing the smallest valid window by one position.
- The production-grade insight is to model window validity as maintained state, not recomputed state; that distinction is what makes high-volume stream processing feasible.

## 🚀 Variations & Further Practice
- Return the actual session boundaries or subarray, not just the length; same pattern, but now correctness depends on preserving the best `(left, right)` pair during contraction.
- Add weighted requirements or per-domain costs, where validity is based on cumulative score rather than raw counts; the harder part is that threshold satisfaction may no longer be tracked with a simple exact-hit counter.
- Solve the offline multi-query version: many `need` maps over the same `visits` stream. The twist is architectural, not just algorithmic—you need preprocessing, indexing, or query-specific acceleration beyond a single sliding window.