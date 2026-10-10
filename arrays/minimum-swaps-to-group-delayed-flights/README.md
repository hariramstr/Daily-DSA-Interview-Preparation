# Minimum Swaps to Group Delayed Flights

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Two Pointers

---

## 🗂 Problem Overview
Given a binary array `flights`, return the minimum number of swaps needed so that all `1`s appear in one contiguous block. You may swap any two indices, not just adjacent ones, so the cost is determined by how many `0`s remain inside the chosen block of delayed flights. The non-trivial part is choosing the best block efficiently under `n <= 100000`, which rules out brute-force evaluation of all rearrangements.

## 🌍 Engineering Impact
This pattern shows up anywhere a system needs to compact sparse “interesting” events into a dense region without fully reordering data: log segmentation, hot-key clustering in caches, packet prioritization windows, search result bucketing, and telemetry pipelines that isolate anomalous records. At scale, brute-force relocation logic becomes quadratic and unusable under real-time latency budgets. The sliding-window view turns a global rearrangement problem into a local density optimization problem. That shift matters architecturally: it enables single-pass processing, predictable memory usage, and straightforward integration into streaming or batch systems where throughput and tail latency dominate design choices.

## 🔍 Problem Statement
You are given a binary array `flights` where `1` represents a delayed flight and `0` represents an on-time flight. Return the minimum number of swaps required to group all delayed flights into one contiguous segment.

If the array contains no delayed flights or exactly one delayed flight, return `0`, since they are already trivially grouped.

Constraints:

- `1 <= flights.length <= 100000`
- `flights[i] ∈ {0, 1}`

Key observation: if there are `k` delayed flights total, then any valid final grouping must occupy some contiguous window of length `k`. For a chosen window, every `0` inside it must be swapped with a `1` outside it, so the swap count equals the number of `0`s in that window.

Examples:

- `flights = [1,0,1,0,1]` → `1`
- `flights = [0,0,1,0,1,1,0]` → `1`

The array can be large, so the algorithm must be linear or close to it.

## 🪜 How to Solve This
1. Read the problem → notice the target is not sorting, only grouping all `1`s together.
2. Count how many delayed flights exist. Call that `k`. If there are `k` delayed flights, the final grouped block must have length `k` — no other length can contain all `1`s contiguously.
3. Reframe the problem: instead of simulating swaps, ask which length-`k` window is cheapest to convert into “all delayed.”
4. In any such window, the bad elements are the `0`s. Each `0` inside must be swapped with some `1` outside. So swaps needed for that window = number of `0`s in it.
5. Therefore, the whole problem becomes: find the length-`k` window with the fewest `0`s, or equivalently the most `1`s.
6. A fixed-size sliding window gives that in one pass. Initialize the first window, then slide right one step at a time, updating counts by removing the left element and adding the new right element.
7. Track the minimum `0`s seen across all windows. That minimum is the answer.

Once you see “all valid targets have the same fixed width,” sliding window is the natural abstraction.

## 🧩 Algorithm Walkthrough
1. **Count total delayed flights (`k`)**  
   Compute the number of `1`s in `flights`. This defines the only possible width of the final contiguous block. If `k <= 1`, return `0`.  
   **Why correct:** zero or one delayed flight is already grouped.  
   **Invariant:** all candidate solutions are windows of size `k`.

2. **Initialize the first fixed-size window**  
   Examine indices `[0, k - 1]` and count how many `0`s it contains. Store this as both the current window cost and the best answer so far.  
   **Why correct:** this is the first valid target block of length `k`.  
   **Invariant:** `currentZeros` equals the number of on-time flights in the active window.

3. **Slide the window across the array**  
   For each new position, remove the contribution of the outgoing left element and add the incoming right element. If the outgoing value was `0`, decrement `currentZeros`; if the incoming value is `0`, increment it.  
   **Pattern:** fixed-size **Sliding Window** with **Two Pointers** semantics (`left`, `right`).  
   **Why correct:** each step transforms one valid length-`k` window into the next in O(1).

4. **Track the minimum window cost**  
   After each slide, update `best = min(best, currentZeros)`.  
   **Why correct:** every `0` inside the chosen block must be replaced by a `1` from outside, and arbitrary swaps make that one-for-one.  
   **Invariant:** `best` is the minimum swaps required among all windows processed so far.

5. **Return `best`**  
   After scanning all windows, `best` is globally optimal because every possible contiguous block of length `k` has been evaluated exactly once.

## 📊 Worked Example
Example: `flights = [0,0,1,0,1,1,0]`

Total delayed flights: `k = 3`, so inspect all windows of length 3.

| Window Indices | Window   | Zeros in Window | Best So Far |
|---|---|---:|---:|
| 0..2 | `[0,0,1]` | 2 | 2 |
| 1..3 | `[0,1,0]` | 2 | 2 |
| 2..4 | `[1,0,1]` | 1 | 1 |
| 3..5 | `[0,1,1]` | 1 | 1 |
| 4..6 | `[1,1,0]` | 1 | 1 |

Interpretation: any final grouped block must contain exactly 3 elements because there are 3 delayed flights total. The best windows contain only one `0`, meaning only one on-time flight is misplaced inside the target block. Since swaps can occur between any two indices, one swap is sufficient to exchange that `0` with a `1` outside the window.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = flights.length`. Counting total `1`s is one pass, and scanning all length-`k` windows is another linear pass with O(1) work per shift. At `10^6` elements this is routine; at `10^9`, the algorithm is still asymptotically right but becomes constrained by memory bandwidth and data locality.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only scalar counters such as `k`, `currentZeros`, and `best`; no extra array or prefix-sum structure is required. You could use prefix sums instead, but that increases space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If all valid end states share a fixed segment length, look for a fixed-size sliding window before considering explicit swaps or rearrangements.
- In binary-array grouping problems, minimizing swaps often reduces to maximizing the number of desired values already inside a candidate window.
- Handle `k = 0` and `k = 1` early; otherwise window initialization can become awkward or subtly wrong.
- Be precise about what the window cost represents: here it is the count of `0`s inside a length-`k` window, not the count of `1`s outside it.
- The transferable design insight is to replace expensive global movement reasoning with local window scoring when arbitrary relocation makes only membership, not order, matter.

## 🚀 Variations & Further Practice
- **Circular array version**: delayed flights are considered adjacent across the array boundary. The twist is handling wraparound windows, usually by doubling the array or using modular indexing.
- **Adjacent swaps only**: now swap distance matters, so counting misplaced elements is no longer enough; you need median-position reasoning over the indices of `1`s.
- **Group all `0`s or either value optimally**: same sliding-window core, but the harder part is generalizing the scoring function and choosing which class to compact.