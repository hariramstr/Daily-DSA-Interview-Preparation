# Maximum Upgrade Score from One Contiguous Patch Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Kadane's Algorithm, Prefix Sum

---

## 🗂 Problem Overview
Given an array `impact` and a fixed per-element `penalty`, choose exactly one non-empty contiguous subarray whose score is `sum(window) - len(window) * penalty`. Return the maximum possible score. The challenge is not computing one window, but finding the best among up to `O(n^2)` candidates under `n <= 200000`, which rules out brute force and forces a linear-time view of the scoring function.

## 🌍 Engineering Impact
This pattern shows up anywhere a batch has both additive value and fixed per-item cost: search ranking windows with indexing overhead, streaming pipelines that coalesce events but pay serialization cost per record, rollout planners that bundle patches but incur validation cost per change, and ad selection where each impression slot adds opportunity cost. At scale, naive enumeration collapses immediately: `O(n^2)` windows over 200k items is operationally irrelevant. The useful abstraction is to fold the cost model into each element, then reuse a proven linear scan. That shift turns an intractable planning pass into a streaming-friendly primitive.

## 🔍 Problem Statement
You are given:

- `impact`, an integer array of length `n`
- `penalty`, a non-negative integer

For every non-empty contiguous window `impact[l..r]`, define:

`score(l, r) = sum(impact[l..r]) - (r - l + 1) * penalty`

Return the maximum score over all valid `l, r`.

Constraints:

- `1 <= impact.length <= 200000`
- `-10^9 <= impact[i] <= 10^9`
- `0 <= penalty <= 10^9`
- Result fits in signed 64-bit integer

Examples:

- `impact = [8, -1, 3, -2, 4], penalty = 2` → `4`
- `impact = [-5, 7, -1, 7, -6], penalty = 3` → `4`

Edge cases matter: all values may be negative after accounting for penalty, `penalty` may be zero, and you must choose at least one element. The input size eliminates any quadratic scan.

## 🪜 How to Solve This
1. Read the score formula → notice the penalty is charged once per included element, not once per window.  
2. Rewrite the expression:  
   `score(l, r) = Σ(impact[i] - penalty)` for `i in [l, r]`.  
   That means the problem is no longer “subarray sum minus length cost”; it is just “maximum subarray sum” on a transformed array.
3. Once you see “maximum sum over one contiguous non-empty segment,” the pattern is Kadane’s Algorithm.
4. Transform conceptually, not necessarily physically: for each element, use `adjusted = impact[i] - penalty`.
5. Scan left to right, maintaining:
   - best subarray ending at current index
   - best subarray seen anywhere so far
6. At each step, decide whether to extend the previous window or start fresh at the current element.
7. Because the window must be non-empty, initialize from the first adjusted value rather than zero.

That is the whole reduction: cost model → element-wise transformation → standard linear maximum-subarray scan.

## 🧩 Algorithm Walkthrough
1. **Apply the reduction.**  
   Define `adjusted[i] = impact[i] - penalty`. Then  
   `score(l, r) = adjusted[l] + ... + adjusted[r]`.  
   This is correct because subtracting `penalty` once per element is exactly the same as subtracting `len(window) * penalty` once per window.

2. **Recognize the pattern: Kadane’s Algorithm.**  
   We need the maximum sum of any non-empty contiguous subarray in `adjusted`. Kadane is the right abstraction because it solves exactly this problem in one pass with constant extra space.

3. **Maintain the local optimum.**  
   Let `current` be the maximum score of a subarray that must end at index `i`.  
   Transition: `current = max(adjusted[i], current + adjusted[i])`.  
   Why correct: any best subarray ending at `i` either starts at `i` or extends the best one ending at `i-1`.

4. **Maintain the global optimum.**  
   Let `best` be the maximum score seen across all endings so far.  
   Update: `best = max(best, current)`.  
   Invariant: after processing index `i`, `best` equals the best non-empty window within `impact[0..i]`.

5. **Initialize carefully.**  
   Set both `current` and `best` to `impact[0] - penalty`.  
   This enforces the non-empty requirement and avoids the common bug where zero incorrectly wins when all adjusted values are negative.

6. **Use 64-bit arithmetic.**  
   Values can reach roughly `±2 * 10^9` per element after adjustment, and sums span up to 200k elements. `long long` / `long` is required.

## 📊 Worked Example
Take `impact = [8, -1, 3, -2, 4]`, `penalty = 2`.

Transformed values: `[6, -3, 1, -4, 2]`

| i | impact[i] | adjusted | current = max(adjusted, current + adjusted) | best |
|---|-----------|----------|---------------------------------------------|------|
| 0 | 8         | 6        | 6                                           | 6    |
| 1 | -1        | -3       | max(-3, 6-3) = 3                            | 6    |
| 2 | 3         | 1        | max(1, 3+1) = 4                             | 6    |
| 3 | -2        | -4       | max(-4, 4-4) = 0                            | 6    |
| 4 | 4         | 2        | max(2, 0+2) = 2                             | 6    |

The best adjusted subarray is just `[6]`, corresponding to original window `[8]`. Its score is `8 - 1*2 = 6`.

This also exposes a mismatch in the prompt’s first example explanation: `[8, -1, 3]` scores `4`, but `[8]` scores `6`, which is better.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is processed once, with constant-time arithmetic and two `max` operations per step. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, the issue is no longer algorithmic complexity but data movement, streaming, and I/O throughput.

### Space Complexity
`O(1)` auxiliary space. The scan only needs the running `current` and `best` values; the transformed array does not need to be materialized. You can store `adjusted` explicitly for debugging, but that trades constant space for easier inspection.

## 💡 Key Takeaways
- If a window score is `sum(...) - k * len(window)`, try pushing the length-dependent cost into each element and see whether it becomes a standard subarray problem.
- “Choose one non-empty contiguous segment for maximum total” is a strong signal for Kadane once the scoring function is linearized.
- Do not initialize the running answer to `0`; that breaks cases where every valid window has negative score and the problem still requires choosing one.
- Use 64-bit integers throughout; `impact[i] - penalty` and cumulative sums can exceed 32-bit bounds comfortably.
- In production cost models, the winning move is often algebraic normalization: convert a domain-specific objective into a known linear-time primitive instead of designing bespoke search logic.

## 🚀 Variations & Further Practice
- **Maximum score with up to `k` disjoint patch windows**: now the problem becomes DP over segments rather than a single Kadane pass; the twist is managing segment boundaries and non-overlap.
- **Maximum score with window length constrained to `[L, R]`**: the unconstrained maximum-subarray reduction is no longer sufficient; prefix sums plus a monotonic deque or balanced structure becomes the right tool.
- **2D patch grid with per-cell penalty**: choose one submatrix maximizing `sum - area * penalty`; the twist is lifting Kadane into 2D with row compression, increasing complexity substantially.