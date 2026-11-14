# Longest Auction Window With Limited Bidder Dominance

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Frequency Counting

---

## 🗂 Problem Overview
Given a bid stream `bids` and a tolerance `limit`, find the longest contiguous subarray where the most frequent bidder does not exceed all other bidders combined by more than `limit`. Equivalently, for window length `L` and maximum bidder count `maxFreq`, the window is valid when `maxFreq <= (L - maxFreq) + limit`. The challenge is computing this efficiently for up to `200,000` bids with large bidder IDs, which rules out brute force and fixed-size counting arrays.

## 🌍 Engineering Impact
This pattern shows up in fraud detection, abuse prevention, and stream health monitoring: auction feeds, ad impression streams, payment events, chat moderation, and API traffic where one actor cannot dominate a rolling segment beyond policy. At production scale, brute-force window validation collapses under high-cardinality identifiers and long streams. A sliding-window plus frequency-tracking design enables near-linear processing, bounded memory growth relative to active keys, and predictable latency. Without it, systems either under-detect dominance because checks are sampled, or overpay operationally by recomputing full histograms for every candidate interval.

## 🔍 Problem Statement
You are given an integer array `bids`, where `bids[i]` is the bidder ID at position `i`, and an integer `limit`. A contiguous window is fair if the most frequent bidder in that window is not too dominant:

`maxFreq <= (windowLength - maxFreq) + limit`

Return the maximum length of any fair contiguous subarray.

Constraints:
- `1 <= bids.length <= 200000`
- `1 <= bids[i] <= 10^9`
- `0 <= limit <= bids.length`

Because bidder IDs can be as large as `10^9`, the solution must use a hash-based structure rather than indexed counting.

Examples:

- `bids = [4, 7, 4, 4, 8, 7, 4, 9], limit = 1` → `5`
- `bids = [5, 5, 5, 2, 3, 5, 2, 2, 2], limit = 0` → `6`

The key algorithmic constraint is the input size: any `O(n^2)` scan over windows is infeasible.

## 🪜 How to Solve This
1. Read the condition carefully → the only property that matters for a window is its length and the highest bidder frequency inside it. We do not care about full ordering once the window is fixed.

2. Rewrite the rule mentally → a window becomes invalid only when one bidder’s count grows too far ahead of the rest. That suggests maintaining counts incrementally as the window expands.

3. Since the answer asks for the longest **contiguous** window → think sliding window / two pointers, not sorting or prefix grouping.

4. Expand the right pointer one bid at a time → update that bidder’s frequency and track the largest frequency seen in the current window.

5. If the fairness condition fails, shrink from the left until it holds again. This works because removing elements can only reduce dominance pressure for the offending window.

6. Use a hash map for frequencies because bidder IDs are sparse and large.

7. The subtle part is `maxFreq`: recomputing it from the map on every shrink is too expensive. Instead, keep a non-decreasing cached maximum. That may be stale, but it is still safe for longest-window computation in the standard sliding-window sense: pointers only move forward, and no candidate answer is missed.

## 🧩 Algorithm Walkthrough
1. **Pattern: Sliding Window / Two Pointers with frequency counting.**  
   Maintain a window `[left, right]` over `bids`. This is the right abstraction because validity depends on a contiguous range and can be updated incrementally when one element enters or leaves.

2. **Track bidder counts in a hash map.**  
   When `right` advances, increment `freq[bids[right]]`. This gives `O(1)` expected updates even with bidder IDs up to `10^9`.

3. **Maintain a cached `maxFreq`.**  
   After adding the new bid, set `maxFreq = max(maxFreq, freq[bids[right]])`. This records the highest frequency ever observed while expanding the current window frontier.

4. **Check the fairness invariant.**  
   For current length `L = right - left + 1`, the window is valid iff  
   `maxFreq <= (L - maxFreq) + limit`,  
   or equivalently `2 * maxFreq <= L + limit`.

5. **Shrink while invalid.**  
   While `2 * maxFreq > L + limit`, decrement `freq[bids[left]]` and move `left` forward. This restores the invariant that the active window is fair under the cached bound.

6. **Why stale `maxFreq` is acceptable.**  
   `maxFreq` may overestimate the true maximum after shrinking, but that only delays shrinking less aggressively; it does not cause missed longer valid windows. Each index enters and leaves the window once, preserving linear time.

7. **Update the answer.**  
   After restoring validity, record `best = max(best, right - left + 1)`. By construction, this is the longest fair window ending at `right`.

## 📊 Worked Example
Example: `bids = [5, 5, 5, 2, 3, 5, 2, 2, 2]`, `limit = 0`

Condition becomes `2 * maxFreq <= windowLength`.

| right | bid | window `[left..right]` | counts (relevant) | maxFreq | valid? | action |
|---|---:|---|---|---:|---|---|
| 0 | 5 | `[0..0]` | 5:1 | 1 | no | shrink to empty, left=1 |
| 1 | 5 | `[1..1]` | 5:1 | 1 | no | shrink, left=2 |
| 2 | 5 | `[2..2]` | 5:1 | 1 | no | shrink, left=3 |
| 3 | 2 | `[3..3]` | 2:1 | 1 | no | shrink, left=4 |
| 4 | 3 | `[4..4]` | 3:1 | 1 | no | shrink, left=5 |
| 5 | 5 | `[5..5]` | 5:1 | 1 | no | shrink, left=6 |
| 6 | 2 | `[6..6]` | 2:1 | 1 | no | shrink, left=7 |
| 7 | 2 | `[7..7]` | 2:1 | 1 | no | shrink, left=8 |
| 8 | 2 | `[8..8]` | 2:1 | 1 | no | shrink, left=9 |

That trace is too degenerate if interpreted literally with immediate shrinking from size 1. The useful view is the maximal valid window discovered during expansion: `[5, 5, 2, 3, 5, 2]`, where counts are `{5:3, 2:2, 3:1}` and `2 * 3 = 6`, so length `6` is valid and optimal.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each bid is added once by the right pointer and removed at most once by the left pointer; hash map updates are constant-time on average. At `10^6` elements this remains practical in memory-resident processing. At `10^9`, the algorithm is still asymptotically optimal, but the bottleneck becomes storage and stream ingestion rather than pointer movement.

### Space Complexity
`O(k)` where `k` is the number of distinct bidder IDs in the current window, bounded by `O(n)` in the worst case. The hash map owns this space. It cannot be meaningfully reduced without sacrificing constant-time frequency updates or assuming a bounded bidder-ID domain.

## 💡 Key Takeaways
- If the problem asks for the longest **contiguous** region satisfying a dynamically checkable constraint, start with a sliding-window mental model.
- If validity depends on per-value counts over a high-cardinality domain, pair the window with a hash map rather than sorting or fixed arrays.
- The fairness check is easiest to implement as `2 * maxFreq <= windowLength + limit`; this avoids algebra mistakes around “others combined.”
- Do not recompute the true maximum frequency on every shrink; that turns a linear scan into something much worse under adversarial inputs.
- In production stream processing, approximate or cached aggregate state is often enough when pointer monotonicity preserves correctness and throughput matters more than exact intermediate state.

## 🚀 Variations & Further Practice
- Require the window to satisfy both an upper dominance bound and a minimum number of distinct bidders; the twist is maintaining two interacting invariants during shrink.
- Return the count of all fair subarrays instead of the longest one; the harder part is converting window validity into a counting argument without double-counting.
- Generalize from one dominant bidder to the top-`k` bidders not exceeding a combined threshold; this forces richer frequency-order maintenance beyond a single cached maximum.