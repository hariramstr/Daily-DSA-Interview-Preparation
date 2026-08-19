# Longest Restock Streak with One Overstock Removal

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Two Pointers

---

## 🗂 Problem Overview
Given an integer array `restocks`, find the maximum length of a contiguous subarray such that, after deleting at most one element from that chosen subarray, the remaining elements are strictly increasing. You may also choose not to delete anything. The challenge is not checking one candidate streak, but finding the best streak across up to `200000` elements, which rules out quadratic enumeration of all subarrays.

## 🌍 Engineering Impact
This pattern shows up in telemetry smoothing, anomaly-tolerant trend detection, warehouse forecasting, and streaming quality checks where one bad sample should not invalidate an otherwise monotonic run. In production pipelines, the difference between “strictly increasing” and “strictly increasing after one repair” matters when sensors glitch, events arrive slightly malformed, or one outlier appears in a batch. Without a linear-time approach, large daily partitions or near-real-time streams force either expensive rescans or weaker heuristics. The right formulation enables exact answers under high throughput, predictable latency, and bounded memory.

## 🔍 Problem Statement
You are given an integer array `restocks` where `restocks[i]` is the number of units restocked on day `i]`. Return the maximum length of a contiguous streak such that removing at most one element from that streak makes the remaining sequence strictly increasing. The deletion, if used, must come from inside the chosen contiguous streak; elements cannot be reordered.

Constraints:

- `1 <= restocks.length <= 200000`
- `-10^9 <= restocks[i] <= 10^9`

Examples:

- `restocks = [3, 5, 4, 6, 7]` → `5`
  - Remove `4` to get `[3, 5, 6, 7]`
- `restocks = [1, 2, 3, 2, 3, 4]` → `4`
  - `[1, 2, 3, 2]` works by removing the last `2`

The key constraint is array size: any solution that checks many candidate subarrays explicitly will time out. The algorithm must be linear or close to linear.

## 🪜 How to Solve This
1. Start with the easier version → longest contiguous strictly increasing subarray. That is local: each position depends on whether `restocks[i - 1] < restocks[i]`.

2. Now add one deletion → deleting one element can connect an increasing run on the left with an increasing run on the right. That suggests precomputing increasing-run lengths from both directions.

3. Define:
   - `incLeft[i]` = length of strictly increasing subarray ending at `i`
   - `incRight[i]` = length of strictly increasing subarray starting at `i`

4. Once those are known, ask: if we delete index `i`, can we stitch the run ending at `i - 1` with the run starting at `i + 1`? We can only do that when `restocks[i - 1] < restocks[i + 1]`.

5. If stitching is valid, candidate length is `incLeft[i - 1] + incRight[i + 1]`. If not, deleting `i` still leaves either side alone, so the best local answer falls back to one of the runs.

6. Also remember the no-deletion case: the answer might already be a fully increasing streak.

This is the standard “local repair over monotonic runs” pattern: precompute maximal valid segments, then evaluate a constant-time merge at each possible repair point.

## 🧩 Algorithm Walkthrough
1. **Precompute increasing suffixes from the left (`incLeft`)**  
   For each index `i`, if `restocks[i - 1] < restocks[i]`, then `incLeft[i] = incLeft[i - 1] + 1`; otherwise reset to `1`.  
   **Why correct:** this exactly captures the longest strictly increasing contiguous run ending at `i`.  
   **Invariant:** `incLeft[i]` always describes a valid run ending at `i`.

2. **Precompute increasing prefixes from the right (`incRight`)**  
   Traverse right-to-left. If `restocks[i] < restocks[i + 1]`, then `incRight[i] = incRight[i + 1] + 1`; otherwise `1`.  
   **Why correct:** this is the symmetric structure for runs starting at `i`.  
   **Invariant:** `incRight[i]` always describes a valid run starting at `i`.

3. **Initialize the answer with the no-deletion case**  
   The maximum value in `incLeft` is already a valid answer because deletion is optional.  
   **Why correct:** some arrays are already strictly increasing over their best streak.

4. **Try deleting each index `i`**  
   Consider three cases:
   - delete first element of a streak → candidate `incRight[1]`
   - delete last element of a streak → candidate `incLeft[n - 2]`
   - delete interior index `i` where `1 <= i <= n - 2`

5. **For interior deletion, attempt to stitch left and right runs**  
   If `restocks[i - 1] < restocks[i + 1]`, then deleting `restocks[i]` connects both sides, yielding  
   `incLeft[i - 1] + incRight[i + 1]`.  
   Otherwise, they cannot be merged into one strictly increasing sequence across the gap.

6. **Take the global maximum**  
   Scan all deletion positions and keep the best candidate.  
   This is a **Dynamic Programming + Two-Sided Run Length** pattern. The DP arrays encode local monotonic structure; the “one deletion” operation becomes a constant-time bridge check instead of a subarray search.

## 📊 Worked Example
Example: `restocks = [3, 5, 4, 6, 7]`

| i | restocks[i] | incLeft | incRight |
|---|-------------|---------|----------|
| 0 | 3           | 1       | 2        |
| 1 | 5           | 2       | 1        |
| 2 | 4           | 1       | 3        |
| 3 | 6           | 2       | 2        |
| 4 | 7           | 3       | 1        |

Trace:

1. `incLeft` says the best increasing run ending at each index is `[1, 2, 1, 2, 3]`.
2. `incRight` says the best increasing run starting at each index is `[2, 1, 3, 2, 1]`.
3. No-deletion best is `3` from `[4, 6, 7]`.
4. Try deleting index `2` (`4`):
   - left endpoint value = `5`
   - right start value = `6`
   - `5 < 6`, so runs can merge
   - candidate length = `incLeft[1] + incRight[3] = 2 + 2 = 4`
5. The chosen contiguous streak includes the deleted element, so final streak length is `4 + 1 = 5`.

Answer: `5`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n)`. One left-to-right pass builds `incLeft`, one right-to-left pass builds `incRight`, and one final pass evaluates every possible deletion point. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, the bottleneck becomes storage and scan bandwidth rather than algorithmic overhead.

### Space Complexity
`O(n)` for the two auxiliary arrays `incLeft` and `incRight`. That space is owned entirely by precomputed run lengths. It can be reduced with a more intricate streaming formulation, but the trade-off is lower clarity and harder correctness reasoning around boundary merges.

## 💡 Key Takeaways
- If a problem says “contiguous segment becomes valid after deleting at most one element,” look for a way to precompute valid runs on both sides and test a constant-time bridge.
- Strict monotonicity with one local repair is a strong signal for prefix/suffix DP rather than brute-force sliding windows.
- The merge condition is value-based, not length-based: `restocks[i - 1] < restocks[i + 1]` is what determines whether deletion truly stitches both sides.
- Be careful about what length you return: the chosen streak includes the deleted element, so a stitched kept-length of `L` corresponds to a streak length of `L + 1`.
- In production systems, precomputing local structure and evaluating cheap repair points is a recurring pattern for exact anomaly-tolerant analytics at scale.

## 🚀 Variations & Further Practice
- Allow up to `k` deletions instead of one. The conceptual twist is that local bridging is no longer enough; you need a richer DP or window state to track repair budget.
- Return the actual streak boundaries and deleted index, not just the length. Same core idea, but now tie-breaking and reconstruction matter.
- Replace “strictly increasing” with “non-decreasing” or with bounded slope changes. The harder part is redefining the merge condition and preserving correctness under weaker monotonic constraints.