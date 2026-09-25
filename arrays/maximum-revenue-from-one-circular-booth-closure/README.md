# Maximum Revenue from One Circular Booth Closure

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Prefix Sum, Monotonic Queue

---

## 🗂 Problem Overview
Given a circular array `revenue`, close exactly one contiguous circular block whose length is between `minClose` and `maxClose`, then maximize the sum of the booths left open. Since open revenue equals `totalRevenue - closedBlockSum`, the real task is to find the minimum-sum circular subarray with a bounded length. The challenge is scale: `n` is up to `200000`, so enumerating every start/end pair or every allowed length is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must remove one bounded contiguous segment from a cyclic timeline or ring topology to optimize the remainder: maintenance windows in token-ring style schedulers, bounded suppression windows in streaming pipelines, circular buffer anomaly masking, and route blackout selection in logistics or network planning. At production scale, brute force collapses under quadratic candidate generation. The useful abstraction is not “circular array” but “bounded-range optimization over a duplicated sequence with strict window validity.” Prefix sums plus a monotonic deque turn an intractable search into a linear pass, which is the difference between an online decision and an offline batch job.

## 🔍 Problem Statement
You are given an integer array `revenue` of length `n`, arranged in a circle, and two integers `minClose` and `maxClose` with `1 <= minClose <= maxClose <= n`. You must close exactly one contiguous circular block of booths whose length `L` satisfies `minClose <= L <= maxClose`. The block may wrap from the end of the array back to the beginning, and it may include all booths.

The objective is to maximize the revenue of the booths that remain open.

Equivalently:

- Let `total = sum(revenue)`.
- Choose one circular subarray of valid length.
- Remove its sum.
- Return `total - removedSum`, maximized.

Examples:

- `revenue = [8, -3, 5, -2, 4], minClose = 2, maxClose = 3` → `10`
- `revenue = [6, -5, 7, -8, 3, 2], minClose = 1, maxClose = 2` → `13`

The key constraint is `n <= 200000`: any `O(n * maxClose)` or `O(n^2)` approach will time out.

## 🪜 How to Solve This
1. Start from the objective → maximizing open revenue is the same as minimizing the revenue of the closed block. That reframes the problem into a bounded minimum circular subarray problem.

2. Circular subarray → think array doubling. If you concatenate `revenue` with itself, every circular block of length at most `n` becomes a normal subarray in the doubled array.

3. But doubling alone overcounts → you must ensure the chosen block starts within the first `n` positions, otherwise the same circular block appears multiple times.

4. Minimum subarray sum with length in `[minClose, maxClose]` → think prefix sums. For an end index `j`, the subarray sum is `prefix[j] - prefix[i]`, where the length constraint means `i` must lie in a sliding index range.

5. For fixed `j`, minimizing `prefix[j] - prefix[i]` means maximizing `prefix[i]` among valid starts.

6. Sliding range maximum over prefix values → monotonic deque. Maintain candidate start indices whose prefix sums are decreasing, evict indices that are too old, and query the best start in `O(1)` amortized.

7. One linear scan over the doubled prefix array gives the minimum removable sum, then return `total - minClosedSum`.

## 🧩 Algorithm Walkthrough
1. **Compute total revenue and build a doubled array.**  
   Let `arr = revenue + revenue`. Any circular block of length at most `n` is now a contiguous subarray in `arr`. This is the standard circular-to-linear reduction.

2. **Build prefix sums over the doubled array.**  
   Define `prefix[0] = 0`, and `prefix[k+1] = prefix[k] + arr[k]`. Then any subarray `arr[i..j-1]` has sum `prefix[j] - prefix[i]`.

3. **Restrict valid circular representations.**  
   Only consider subarrays whose start index `i` is in `[0, n-1]`. This prevents counting the same circular block multiple times from the second copy.

4. **Scan possible end positions `j`.**  
   For each `j`, valid start indices satisfy:
   - `j - maxClose <= i <= j - minClose`
   - `0 <= i < n`  
   This is the bounded-length constraint plus the “first copy only” constraint.

5. **Maintain a monotonic deque of candidate starts.**  
   The deque stores indices `i` in the current valid range, ordered so `prefix[i]` is decreasing. Why decreasing? Because for fixed `j`, we want to minimize `prefix[j] - prefix[i]`, so we want the largest `prefix[i]`.

6. **Update the deque incrementally.**  
   When `j` advances:
   - Add the new eligible start `i = j - minClose`.
   - Remove deque front indices `< max(0, j - maxClose)` or `>= n`.
   - While the new start has `prefix[new] >= prefix[back]`, pop the back.  
   The invariant is: deque contains exactly the valid starts, and its front has the maximum prefix sum.

7. **Evaluate the best closed block ending at `j`.**  
   If the deque is non-empty, candidate closed sum is `prefix[j] - prefix[deque.front]`. Track the global minimum over all `j`.

8. **Return the answer.**  
   `maxOpenRevenue = totalRevenue - minClosedSum`.  
   This is correct because every valid circular closure is represented once, and the deque always exposes the best start for each end in amortized constant time.

## 📊 Worked Example
Take `revenue = [6, -5, 7, -8, 3, 2]`, `minClose = 1`, `maxClose = 2`.

`total = 5`  
`arr = [6, -5, 7, -8, 3, 2, 6, -5, 7, -8, 3, 2]`

Prefix sums over `arr` begin:
`[0, 6, 1, 8, 0, 3, 5, 11, 6, 13, 5, 8, 10]`

Trace on relevant early positions:

| `j` | new start added | valid start range | best `i` from deque | closed sum `prefix[j]-prefix[i]` |
|---|---:|---|---:|---:|
| 1 | 0 | `[0,0]` | 0 | `6` |
| 2 | 1 | `[0,1]` | 0 | `1` |
| 3 | 2 | `[1,2]` | 2 | `7` |
| 4 | 3 | `[2,3]` | 2 | `-8` |
| 5 | 4 | `[3,4]` | 4 | `3` |
| 6 | 5 | `[4,5]` | 5 | `2` |

The minimum closed sum found is `-8`, achieved by closing the single booth `[-8]`. Therefore the maximum remaining open revenue is `5 - (-8) = 13`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. The doubled array and prefix sums are linear in `2n`, and each index enters and leaves the monotonic deque at most once. That remains practical at `10^6` scale, while any quadratic or per-length scan becomes infeasible long before `10^9` candidate subarrays.

### Space Complexity
`O(n)`. The doubled array, prefix sum array, and deque all scale linearly with `n`. You can reduce constants by streaming prefix sums instead of storing all subarray sums, but the asymptotic bound stays linear because the deque still needs up to `O(n)` candidates.

## 💡 Key Takeaways
- If the problem says “circular contiguous segment” and the segment length is bounded, first try doubling the array and turning wraparound into a linear interval problem.
- If a subarray objective under length constraints can be written as `prefix[j] - prefix[i]`, ask whether each end index needs a min/max prefix over a sliding index range.
- The most common bug is mixing prefix indices with array indices: a subarray of length `L` is represented by `j - i = L`, not `j - i + 1 = L`.
- Another easy trap is failing to restrict starts to the first `n` positions after doubling, which silently double-counts circular blocks and can produce invalid answers.
- At scale, the transferable design move is to convert expensive global search into a stream of local decisions backed by a data structure that preserves exactly the candidates the objective can still use.

## 🚀 Variations & Further Practice
- **Return the actual closed interval, not just the value.** The twist is preserving start/end provenance through deque updates and mapping doubled-array indices back to circular coordinates.
- **Allow multiple closures with a total closed length budget.** This becomes a segmented DP problem over prefix sums; the monotonic-queue trick alone is no longer sufficient.
- **Constrain the open segment instead of the closed one.** The complement relationship still helps, but wraparound and bounded lengths invert differently and require careful case handling.