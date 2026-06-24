# Minimum Swaps to Cluster Priority Orders

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Greedy

---

## 🗂 Problem Overview
Given a binary array `orders`, return the minimum number of swaps needed so that all `1`s become contiguous somewhere in the array. You may swap any two indices, so distance does not matter. The key observation is that if there are `k` priority orders, the final contiguous block must occupy some window of length `k`. The problem is therefore to find the length-`k` window containing the fewest `0`s, because each such `0` requires one swap.

## 🌍 Engineering Impact
This pattern shows up when optimizing locality under fixed cardinality: packing hot records in cache-friendly segments, clustering high-priority jobs in scheduler queues, compacting live tuples in storage engines, or grouping flagged events in streaming buffers before downstream processing. At scale, brute-force rearrangement or simulation is wasteful because the system does not need the final layout, only the minimum correction cost. The sliding-window framing turns a potentially quadratic search into a linear pass, which is the difference between something usable in hot paths and something that collapses under large batch sizes or high-throughput pipelines.

## 🔍 Problem Statement
You are given an array `orders` of length `n` where each element is either `0` or `1`. A `1` represents a priority order and a `0` represents a regular order. In one operation, you may swap values at any two distinct indices. Return the minimum number of swaps required to make all `1`s appear in one contiguous block.

Constraints:

- `1 <= orders.length <= 100000`
- `orders[i] ∈ {0, 1}`
- The result fits in a 32-bit integer

Edge cases matter:

- If the array contains no `1`s, answer is `0`
- If the array contains exactly one `1`, answer is `0`
- The contiguous block of `1`s may appear anywhere

Examples:

- `orders = [1,0,1,0,1]` → `1`
- `orders = [0,0,1,0,1,1,0]` → `1`

The decisive constraint is `n` up to `100000`, which rules out trying every rearrangement or recomputing counts for each candidate window from scratch.

## 🪜 How to Solve This
1. Read the problem → notice the target state is not “sorted,” it is “all `1`s grouped together somewhere.”

2. Count how many `1`s exist. Call that `k`. Any valid final arrangement must place all `1`s inside one contiguous block of length `k`. There is no other possible block size.

3. Once that clicks, the problem becomes: among all windows of length `k`, which one is cheapest to convert into all `1`s?

4. Inside such a window, every existing `1` is already in the right region. Every `0` is a defect that must be swapped with a `1` outside the window.

5. Because swaps can occur between any two indices, one `0` inside the chosen window can always be fixed by one `1` outside it. So the number of required swaps for a window is exactly the number of `0`s in that window.

6. Now avoid recomputing each window from scratch. Slide a fixed-size window across the array, maintain either the count of `1`s or the count of `0`s, and track the best window seen.

7. Minimum swaps = minimum zeros in any length-`k` window, equivalently `k - maxOnesInWindow`.

## 🧩 Algorithm Walkthrough
1. **Count total priority orders (`k`)**  
   Scan the array once and count the number of `1`s. This defines the only valid block length for the final clustered segment. If `k <= 1`, return `0` immediately because zero or one `1` is already contiguous.

2. **Choose the right pattern: Sliding Window**  
   This is a fixed-size window problem. We need to evaluate every subarray of length `k` and compute how many `1`s it already contains. Sliding Window is the right abstraction because adjacent windows differ by only one outgoing element and one incoming element.

3. **Initialize the first window**  
   Count the number of `1`s in indices `[0, k-1]`. Let this be `windowOnes`. This establishes the invariant: `windowOnes` always equals the number of `1`s in the current length-`k` window.

4. **Slide one position at a time**  
   For each new right boundary, remove the contribution of the element leaving on the left and add the contribution of the new element entering on the right. Update `windowOnes` in O(1).

5. **Track the best window**  
   Maintain `maxOnes`, the maximum number of `1`s seen in any valid window. This is correct because the best target block is the one requiring the fewest replacements.

6. **Derive the answer**  
   A length-`k` window with `maxOnes` already-correct elements contains `k - maxOnes` zeros. Each zero requires exactly one swap with a `1` outside the window. Therefore answer = `k - maxOnes`.

The invariant throughout: every considered state corresponds to one exact candidate block of length `k`, and `maxOnes` summarizes the cheapest such block seen so far.

## 📊 Worked Example
Example: `orders = [1,0,1,0,1]`

Total `1`s: `k = 3`, so evaluate all windows of length `3`.

| Window Indices | Window Values | `windowOnes` | `zeros = k - windowOnes` | Best So Far |
|---|---|---:|---:|---:|
| `[0..2]` | `[1,0,1]` | 2 | 1 | 1 |
| `[1..3]` | `[0,1,0]` | 1 | 2 | 1 |
| `[2..4]` | `[1,0,1]` | 2 | 1 | 1 |

Trace:

1. First window `[1,0,1]` has two `1`s, so one `0` must be swapped out.
2. Slide right: remove `1`, add `0` → window now has one `1`.
3. Slide right again: remove `0`, add `1` → window returns to two `1`s.
4. Maximum `1`s in any length-3 window is `2`.
5. Minimum swaps = `3 - 2 = 1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass counts total `1`s`, and one pass slides the fixed-size window across the array. Each element is processed a constant number of times. At `10^6` elements this is routine; at `10^9`, linear work is still expensive but remains the only viable asymptotic class for a single-machine scan.

### Space Complexity
`O(1)`. The algorithm stores only scalar counters such as `k`, `windowOnes`, and `maxOnes`. No auxiliary array or prefix structure is required. You could express it with prefix sums, but that increases space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the target says “make all occurrences of X contiguous,” first ask whether the final block size is fixed by the count of X; that often reduces the problem to scanning windows.
- If swaps can occur between arbitrary indices, cost usually depends on membership mismatch, not physical distance; that is a strong signal for counting defects inside a candidate region.
- Handle `k = 0` and `k = 1` explicitly; otherwise window initialization can become awkward or subtly wrong.
- Be precise about window length: it must equal the total number of `1`s, not “at most” that number; using any other size invalidates the swap-count logic.
- The transferable design insight is to optimize for the metric actually requested—minimum correction cost—not to simulate the final state when a cheaper aggregate computation is sufficient.

## 🚀 Variations & Further Practice
- **Circular array version**: all `1`s must be contiguous in a ring, not a linear array. The twist is handling wraparound windows, typically by doubling the array or using modular indexing.
- **Adjacent swaps only**: now distance matters, so counting zeros in a window is no longer enough. The problem shifts toward median positions and movement cost.
- **Group all equal values with minimum changes**: extend from binary arrays to multi-class clustering, where the harder part is choosing both the target value and the optimal segment efficiently.

