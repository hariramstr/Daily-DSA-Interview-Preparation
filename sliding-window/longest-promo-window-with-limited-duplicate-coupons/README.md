# Longest Promo Window With Limited Duplicate Coupons

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given an array of coupon codes and an integer `k`, find the maximum length of a contiguous subarray where every distinct coupon appears at most `k` times. The input is a sequential stream of values; the output is a single integer length. The challenge is enforcing per-value frequency limits without rechecking every candidate subarray, which would be prohibitively expensive at the given input size of up to 200,000 orders.

## 🌍 Engineering Impact
This pattern shows up in streaming analytics, fraud detection, ad-serving diversity controls, log ingestion, and distributed rate-limiters where a rolling segment must satisfy bounded repetition constraints. In production, brute-force window validation collapses under sustained throughput because each new event would trigger repeated rescans of prior state. A sliding-window plus frequency map turns the problem into incremental maintenance: admit one event, evict as needed, preserve invariants. That shift matters architecturally because it enables single-pass processing, predictable memory growth, and low-latency decisions in systems that cannot afford batch recomputation.

## 🔍 Problem Statement
You are given an integer array `coupons` and an integer `k`. Return the length of the longest contiguous subarray such that, within that subarray, no coupon code appears more than `k` times.

Constraints:

- `1 <= coupons.length <= 200000`
- `1 <= coupons[i] <= 1000000000`
- `1 <= k <= coupons.length`

Examples:

- `coupons = [4, 1, 4, 2, 4, 1, 2, 2], k = 2` → `5`
- `coupons = [7, 7, 7, 8, 8, 9, 7], k = 1` → `3`

Important edge cases:
- All values unique: the whole array is valid.
- One value repeated many times: the window must contract aggressively.
- `k = 1`: the problem becomes longest subarray with all distinct values.

The key algorithmic constraint is the input size: `O(n^2)` enumeration is not viable, so the solution must update validity incrementally in near-linear time.

## 🪜 How to Solve This
1. Read the requirement carefully → this is about a **contiguous** block, so sorting is invalid because it destroys order.

2. Notice what makes a window valid → every coupon frequency inside the current range must stay `<= k`. That immediately suggests maintaining counts, which means a `HashMap`.

3. Ask what happens when you extend the window to the right by one order → only one frequency changes. That is a strong signal for a **sliding window** instead of recomputing the whole subarray.

4. If adding `coupons[right]` makes its count exceed `k`, the window is no longer valid. How do you recover? Move the left boundary rightward, decrementing counts, until that coupon is back within limit.

5. Why does this work? Because once a window is invalid, shrinking from the left is the only way to restore validity while preserving contiguity.

6. Track the maximum window length after each repair step. Each index moves forward at most once, so the full scan stays linear.

This is the standard “expand, repair, record” two-pointer pattern.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Use two pointers: `left = 0` and `right` scanning from `0` to `n - 1`. Maintain a hash map `freq` from coupon code to count in the current window. Also track `best = 0`.  
   **Invariant:** before recording `best`, the window `[left, right]` is valid.

2. **Expand the window**  
   For each `right`, increment `freq[coupons[right]]`. This tentatively includes the new order in the active window.  
   **Why correct:** only the newly added coupon can violate the constraint, because all prior counts were already valid.

3. **Repair invalid windows**  
   While `freq[coupons[right]] > k`, move `left` forward:
   - decrement `freq[coupons[left]]`
   - increment `left`  
   **Why correct:** the only way to reduce any count in a contiguous window is to evict elements from the left. When the loop ends, the offending coupon is back within limit, and therefore the whole window is valid again.

4. **Record the answer**  
   Compute `right - left + 1` and update `best`.  
   **Invariant maintained:** `best` is the maximum valid window length seen so far.

5. **Why this abstraction fits**  
   This is a textbook **Two Pointers / Sliding Window** problem: contiguous range, monotonic pointer movement, and a validity condition that can be updated incrementally with a frequency map. No backtracking is needed because once `left` advances, earlier positions can never improve a future window under the same `right`.

## 📊 Worked Example
Example: `coupons = [4, 1, 4, 2, 4, 1, 2, 2]`, `k = 2`

| right | coupon | action | left | freq snapshot | window len | best |
|---|---:|---|---:|---|---:|---:|
| 0 | 4 | add 4 | 0 | {4:1} | 1 | 1 |
| 1 | 1 | add 1 | 0 | {4:1,1:1} | 2 | 2 |
| 2 | 4 | add 4 | 0 | {4:2,1:1} | 3 | 3 |
| 3 | 2 | add 2 | 0 | {4:2,1:1,2:1} | 4 | 4 |
| 4 | 4 | add 4, invalid | 1 | {4:2,1:1,2:1} after removing left `4` | 4 | 4 |
| 5 | 1 | add 1 | 1 | {4:2,1:2,2:1} | 5 | 5 |
| 6 | 2 | add 2 | 1 | {4:2,1:2,2:2} | 6 | 6 |
| 7 | 2 | add 2, invalid | 4 | repaired to {4:1,1:1,2:2} | 4 | 6 |

Maximum valid length is `6`, from subarray `[1, 4, 2, 4, 1, 2]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each element is added to the window once by `right` and removed at most once by `left`, so total pointer movement is linear. Hash map updates are `O(1)` average-case. At `10^6` elements this remains practical; at `10^9`, the algorithm is still optimal in shape but likely constrained by memory bandwidth and runtime budget.

### Space Complexity
`O(m)` where `m` is the number of distinct coupon codes currently tracked, worst-case `O(n)`. The hash map owns essentially all auxiliary space. You cannot reduce this asymptotically without losing constant-time frequency updates or switching to a domain-bounded array, which is impossible here because coupon values are large.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** region under a dynamically checkable constraint, think sliding window before considering nested loops.
- If validity depends on per-value counts inside the current range, pair two pointers with a frequency map.
- Update `best` only after the repair loop; recording too early captures invalid windows.
- Shrink while the count is `> k`, not `>= k`; exactly `k` occurrences is still valid.
- The production lesson is incremental state maintenance: admit one event, evict until valid, and avoid recomputing aggregate properties from scratch.

## 🚀 Variations & Further Practice
- **Return the actual window, not just its length** — same core pattern, but now you must preserve start/end indices and define tie-breaking rules.
- **Allow at most `d` distinct coupon codes, each still capped at `k`** — combines two window constraints and requires tracking both frequency and distinct-count transitions.
- **Process an unbounded stream with expiring events by timestamp instead of index** — same invariant maintenance, but the left side is driven by time-based eviction rather than simple pointer progression.