# Minimum Recolors to Form a Centered Price Peak

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Sliding Window, Prefix Sum

---

## 🗂 Problem Overview
Given an array `prices` and an exact radius `k`, choose any window of length `2k + 1` and compute the fewest element recolors needed so that the window becomes a strict mountain centered at its middle index. A valid mountain requires `k` strict increases on the left and `k` strict decreases on the right, making the center the unique maximum. The challenge is scale: up to `2 * 10^5` elements, so recomputing every comparison for every window is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a local shape constraint must be enforced over large streams: anomaly windows in observability pipelines, price-shape validation in trading systems, waveform normalization in signal processing, and ranking-score smoothing in search or recommendation stacks. At production scale, the issue is not expressing the constraint but evaluating it over millions of overlapping windows without quadratic blowups. The right abstraction—precomputing local validity and aggregating with prefix sums or sliding windows—turns repeated structural checks into constant-time window queries. Without that, latency grows with overlap; with it, the system remains predictable under sustained throughput.

## 🔍 Problem Statement
You are given an integer array `prices` of length `n` and an integer `k`, where `2k + 1 <= n`. A centered peak of exact radius `k` is any index `i` such that the window `prices[i-k..i+k]` forms a strict mountain: for every `d` in `[1, k]`, the left side is strictly increasing toward the center and the right side is strictly decreasing away from the center. Equivalently, the `k` adjacent comparisons on the left must satisfy `<`, and the `k` adjacent comparisons on the right must satisfy `>`.

You may recolor any element to any integer in one operation. Only elements inside the chosen window matter. Return the minimum recolors needed over all valid centers.

Examples:

- `prices = [1,3,5,4,2,6,7], k = 2` → `0`
- `prices = [4,4,4,4,4,4], k = 2` → `4`

The key constraint is `n <= 200000`, which rules out checking all `2k` comparisons independently for every window in `O(nk)` time.

## 🪜 How to Solve This
1. Start from the structure, not the values → a window is valid if each adjacent pair on the left has the relation `<` and each adjacent pair on the right has the relation `>`.
2. Recoloring is flexible → if a comparison already has the required direction, we can keep both endpoints as-is for that relation; otherwise at least one endpoint involved in the window must change.
3. But counting bad comparisons directly is wrong → one recolored element can fix two adjacent comparisons, so the unit of cost is not “failed edge.”
4. Reframe the window as two monotone chains sharing the center. To keep an element unchanged, it must already participate in a valid increasing run on the left or decreasing run on the right.
5. For a fixed center, the optimal strategy is to preserve the longest valid suffix ending at the center on the left and the longest valid prefix starting at the center on the right; everything else in the window can be recolored.
6. That suggests precomputing local monotone-run lengths once, then evaluating every center in `O(1)`.
7. Scan left-to-right for increasing-run lengths, right-to-left for decreasing-run lengths, and derive how many positions in each candidate window can stay unchanged.
8. Minimize recolors over all centers.

## 🧩 Algorithm Walkthrough
1. **Precompute left increasing runs**  
   Build `inc[i]`: the length of the longest strict increasing contiguous run ending at `i`.  
   If `prices[i-1] < prices[i]`, then `inc[i] = inc[i-1] + 1`; otherwise `1`.  
   Invariant: `inc[i]` tells how many consecutive positions, including `i`, already satisfy the left-half mountain condition.

2. **Precompute right decreasing runs**  
   Build `dec[i]`: the length of the longest strict decreasing contiguous run starting at `i`.  
   If `prices[i] > prices[i+1]`, then `dec[i] = dec[i+1] + 1`; otherwise `1`.  
   Invariant: `dec[i]` tells how many consecutive positions, including `i`, already satisfy the right-half mountain condition.

3. **Evaluate each valid center**  
   For center `c`, the target window is `[c-k, c+k]`.  
   The left side can keep at most `min(inc[c], k+1)` positions ending at `c`.  
   The right side can keep at most `min(dec[c], k+1)` positions starting at `c`.

4. **Avoid double-counting the center**  
   The center belongs to both runs, so unchanged positions in the full mountain are  
   `keep = min(inc[c], k+1) + min(dec[c], k+1) - 1`.

5. **Compute recolors for that center**  
   Window length is `2k + 1`, so  
   `changes = (2k + 1) - keep`.

6. **Take the global minimum**  
   Scan centers `c` from `k` to `n-k-1` and minimize `changes`.

7. **Why this is correct**  
   This is a **dynamic programming on contiguous runs** problem with a **linear scan** evaluation phase. A position can remain unchanged only if it already lies in the required monotone chain relative to the center. Because recoloring can assign arbitrary integers, every position outside the maximal keepable left/right chains can always be fixed independently. Therefore the optimum for each center is exactly “window size minus keepable positions.”

## 📊 Worked Example
Take `prices = [4,4,4,4,4,4]`, `k = 2`.

| i | prices[i] | inc[i] | dec[i] |
|---|-----------|--------|--------|
| 0 | 4 | 1 | 1 |
| 1 | 4 | 1 | 1 |
| 2 | 4 | 1 | 1 |
| 3 | 4 | 1 | 1 |
| 4 | 4 | 1 | 1 |
| 5 | 4 | 1 | 1 |

Valid centers are `2` and `3`.

For `c = 2`:
- `leftKeep = min(inc[2], 3) = 1`
- `rightKeep = min(dec[2], 3) = 1`
- `keep = 1 + 1 - 1 = 1`
- `changes = 5 - 1 = 4`

For `c = 3`, the values are identical, so `changes = 4`.

Minimum over all centers is `4`.  
Interpretation: no adjacent comparison already helps, so only one element—typically the center—can be preserved while recoloring the other four positions into a strict mountain.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Two linear passes build `inc` and `dec`, and one more linear pass evaluates all valid centers. There is no per-window rescanning. At `10^6` elements this remains practical; at `10^9`, even linear time becomes infrastructure-bound and requires partitioned or streaming execution.

### Space Complexity
`O(n)`. The dominant space is the two auxiliary arrays `inc` and `dec`. This can be reduced only partially: one side can be streamed while the other is stored, but fully eliminating auxiliary state complicates evaluation and usually hurts clarity more than it helps memory.

## 💡 Key Takeaways
- If a problem asks for the best among many overlapping windows with a local shape constraint, look for precomputed adjacency/run information instead of rescanning each window.
- When values can be changed arbitrarily, the real question is often “how many positions can remain untouched,” not “how many violations exist.”
- The center is shared by both monotone halves; forgetting to subtract one causes a systematic off-by-one error.
- `inc[i]` and `dec[i]` count positions, not edges; compare them against `k + 1`, not `k`.
- At scale, converting repeated structural validation into reusable local summaries is the difference between predictable linear throughput and overlap-driven quadratic behavior.

## 🚀 Variations & Further Practice
- Allow radius **at least** `k` instead of exactly `k`: now you must optimize over variable window sizes, which introduces a second dimension and often pushes toward two-pointers or range-query structures.
- Charge recoloring by magnitude of change instead of unit cost: the problem stops being pure structural counting and becomes an optimization over feasible value assignments.
- Require the entire array to be partitioned into valid mountains: local window optimization becomes a global segmentation problem, typically requiring dynamic programming over intervals.