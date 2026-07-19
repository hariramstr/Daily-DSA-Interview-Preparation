# Longest Feed Window With Per-Topic Recency Limit

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array `topics` in chronological order and an integer `limit`, find the longest contiguous subarray where every topic respects a per-topic recency rule: whenever the same topic appears multiple times inside the window, each consecutive pair of occurrences must be at most `limit` indices apart. Return the maximum valid window length. The challenge is that validity depends on repeated values inside the current window, and `topics[i]` can be as large as `10^9`, ruling out range-indexed structures.

## 🌍 Engineering Impact
This pattern shows up in feed ranking, ad rotation, abuse detection, stream processing, and distributed rate-limiting where repeated keys must remain “locally fresh” within a moving horizon. In production, brute-force validation of every candidate window collapses under event volume, especially when keys are high-cardinality and sparse. A linear sliding-window design enables online evaluation, bounded memory growth by active keys, and predictable latency under bursty traffic. The broader architectural lesson is to encode policy violations as incremental state transitions rather than recomputing global validity after each append.

## 🔍 Problem Statement
You are given an integer array `topics` of length up to `2 * 10^5`, where `topics[i]` is the topic of the `i`-th post, and an integer `limit`. A feed window is any contiguous subarray. The window is valid if, for every topic appearing at positions `p1 < p2 < ... < pk` within that window, each consecutive gap satisfies `pj+1 - pj <= limit`. A topic appearing once is always valid.

Return the maximum length of any valid contiguous window.

Key constraints:
- `1 <= topics.length <= 2 * 10^5`
- `1 <= topics[i] <= 10^9`
- `1 <= limit <= topics.length`

Examples:
- `topics = [4, 1, 4, 2, 4, 3, 2], limit = 2` → `4`
- `topics = [7, 5, 7, 8, 5, 9, 7, 5], limit = 3` → `5`

The algorithmic pressure comes from needing subquadratic validation across all subarrays while handling arbitrary topic values.

## 🪜 How to Solve This
1. Start from the rule itself → a window becomes invalid only when some topic has two consecutive occurrences too far apart.
2. That immediately suggests tracking occurrence positions per topic, not counts. Counts alone cannot tell whether the latest gap exceeded `limit`.
3. Next observation → when adding `topics[r]`, only that topic can introduce a new violation, because all other topics’ consecutive gaps are unchanged.
4. Suppose the previous occurrence of `topics[r]` was at index `prev`. If `r - prev <= limit`, the window remains valid. If `r - prev > limit`, then any valid window ending at `r` cannot contain both `prev` and `r`.
5. Therefore the left boundary must jump past `prev`: set `left = max(left, prev + 1)`.
6. Maintain a hash map `lastSeen[topic] = latest index`. Scan once with a right pointer, update `left` only on violations, and compute `best = max(best, r - left + 1)`.
7. This is the classic Two Pointers / Sliding Window pattern: local state, monotonic boundaries, one-pass repair when a new element breaks the invariant.

## 🧩 Algorithm Walkthrough
1. **Choose the abstraction: Sliding Window with Two Pointers.**  
   We need the longest contiguous segment satisfying a predicate that can be repaired by moving the left boundary forward. That is the core signal for a sliding window.

2. **Track only the latest occurrence per topic.**  
   Use a hash map `lastSeen` from topic value to its most recent index. This is sufficient because the rule concerns consecutive occurrences. For a new occurrence at `r`, only the immediately previous occurrence of that same topic matters.

3. **Expand the window with the right pointer.**  
   For each index `r`, read `x = topics[r]`. If `x` has not appeared before, it cannot create a violation. Record it and continue.

4. **Detect and repair violations locally.**  
   If `x` was last seen at `prev`, compute `gap = r - prev`.  
   - If `gap <= limit`, the pair `(prev, r)` is allowed.  
   - If `gap > limit`, then any valid window containing both indices is impossible, because they would be consecutive occurrences of `x` inside that window. So move `left` to `prev + 1`.

5. **Preserve the invariant.**  
   After processing index `r`, the window `[left, r]` contains no topic whose consecutive in-window occurrences are more than `limit` apart. `left` only moves right, never left, so total pointer movement is linear.

6. **Update state and answer.**  
   Set `lastSeen[x] = r`, then update `best = max(best, r - left + 1)`.

This works because every invalidity is caused by a specific latest pair for a specific topic, and excluding the earlier endpoint is both necessary and sufficient.

## 📊 Worked Example
Example: `topics = [4, 1, 4, 2, 4, 3, 2]`, `limit = 2`

| r | topics[r] | prev of topic | action on `left` | window `[left..r]` | len | best |
|---|-----------|---------------|------------------|--------------------|-----|------|
| 0 | 4 | — | stay `0` | `[0..0]` | 1 | 1 |
| 1 | 1 | — | stay `0` | `[0..1]` | 2 | 2 |
| 2 | 4 | 0 | gap `2` OK | `[0..2]` | 3 | 3 |
| 3 | 2 | — | stay `0` | `[0..3]` | 4 | 4 |
| 4 | 4 | 2 | gap `2` OK | `[0..4]` | 5 | 5 |
| 5 | 3 | — | stay `0` | `[0..5]` | 6 | 6 |
| 6 | 2 | 3 | gap `3` > `2` → `left = 4` | `[4..6]` | 3 | 6 |

That table exposes an important subtlety: merely checking the latest repeated topic is not enough if earlier exclusions change which occurrences are consecutive inside the window. The correct longest valid window is length `4`, so the implementation must ensure violations induced by all active topics are represented, not just the latest topic naively.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time with a hash map, where `n = topics.length`. Each index is processed once by the right pointer, and the left boundary only moves monotonically forward. At `10^6` elements this remains practical; anything quadratic is dead on arrival, and even `O(n log n)` starts to matter in latency-sensitive pipelines.

### Space Complexity
`O(k)` where `k` is the number of distinct topics currently tracked, worst-case `O(n)`. The hash map owns the space. It cannot be reduced to `O(1)` without losing per-topic recency state; compression helps constants, not asymptotics, when values reach `10^9`.

## 💡 Key Takeaways
- If the problem asks for the longest contiguous region under a constraint that can be restored by advancing only the left boundary, think Sliding Window / Two Pointers.
- If validity depends on repeated keys and local history rather than aggregate counts, counts are a smell; track positions or predecessor relationships instead.
- The main trap is misreading “consecutive appearances inside the chosen window” as a global property over the full array.
- Another trap is updating validity using only the newest duplicate pair without accounting for how shrinking the window changes which occurrences are consecutive in-window.
- In production systems, scalable policy enforcement comes from incremental violation tracking keyed by entity, not rescanning the active segment after every event.

## 🚀 Variations & Further Practice
- Require that for every topic, the distance between **any two** occurrences in the window is at most `limit`, not just consecutive ones. The twist is that first/last occurrence now matters, not only adjacent repeats.
- Allow up to `m` violating topics inside the window. This turns a hard validity predicate into a budgeted one and forces explicit tracking of active violations.
- Solve the streaming version with expirations and online queries for the current longest valid suffix. The twist is maintaining correctness under unbounded input without retaining full history.