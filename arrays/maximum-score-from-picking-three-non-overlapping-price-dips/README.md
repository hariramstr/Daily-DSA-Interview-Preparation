# Maximum Score from Picking Three Non-Overlapping Price Dips

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane's Algorithm

---

## 🗂 Problem Overview
Given an integer array `prices`, choose exactly three non-empty contiguous, pairwise non-overlapping subarrays and maximize the sum of their elements. Return that maximum total score. The difficulty is not finding one best segment, but coordinating three segments under strict non-overlap while still allowing negative values. Because exactly three subarrays are required, the optimal solution may be forced to include a negative-sum block, which rules out greedy “take only profitable runs” logic.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must allocate a fixed number of disjoint windows over a noisy sequence: ad-slot placement in ranking pipelines, burst extraction in observability streams, maintenance-window scheduling, or selecting profitable execution intervals in trading backtests. At scale, brute-force interval enumeration explodes quadratically or worse and becomes unusable in online or nearline systems. The dynamic-programming framing turns an intractable search over overlapping candidates into a linear scan with explicit state. That enables predictable latency, bounded memory, and easier composition into larger optimization pipelines where exactness matters more than heuristic local choices.

## 🔍 Problem Statement
You are given an integer array `prices` where `prices[i]` may be positive, zero, or negative. You must select exactly three non-empty contiguous subarrays such that no two selected subarrays overlap. The score of a subarray is the sum of its elements, and the result is the maximum possible total score across the three chosen subarrays.

Constraints:

- `3 <= prices.length <= 200000`
- `-10^9 <= prices[i] <= 10^9`
- The answer fits in signed 64-bit integer

Examples:

- `prices = [4,-1,3,-2,5,-6,2,2]` → `15`
  - One optimum: `[4,-1,3] = 6`, `[5] = 5`, `[2,2] = 4`
- `prices = [-5,4,-1,4,-10,3]` → `10`
  - Best choice: `[4] = 4`, `[-1,4] = 3`, `[3] = 3`

The key constraint is `n` up to `200000`: any solution that compares many interval pairs or triples is too slow. The algorithm must be near-linear.

## 🪜 How to Solve This
1. Read the problem → this is not “pick three elements”; it is “pick three contiguous blocks,” so interval structure matters.

2. Notice “exactly three” + “non-overlapping” → this is a fixed-count segmentation problem. That strongly suggests dynamic programming rather than greedy selection.

3. Start from the familiar base case: maximum sum of one subarray is Kadane’s algorithm. So ask: can we lift Kadane from one segment to `k` segments?

4. Define state by how many subarrays have been completed so far, and whether the current index is extending the last chosen subarray or starting a new one.

5. For each value, either:
   - extend the current `j`-th subarray, or
   - start the `j`-th subarray after having already completed `j-1` subarrays earlier.

6. Track the best total for:
   - `local[j]`: best score using exactly `j` subarrays where the `j`-th subarray must end at the current index
   - `global[j]`: best score using exactly `j` subarrays anywhere up to the current index

7. This gives a one-pass DP with constant state per `j`. Since `j = 3` is fixed, the whole problem becomes `O(n)`.

## 🧩 Algorithm Walkthrough
1. **Use Dynamic Programming with a Kadane-style transition.**  
   The right abstraction is “maximum sum of `k` non-overlapping subarrays.” Kadane solves `k = 1`; we generalize it to `k = 3`.

2. **Define two DP states.**  
   Let `local[j]` be the best sum using exactly `j` subarrays where the last subarray ends at the current index. Let `global[j]` be the best sum using exactly `j` subarrays within the prefix processed so far.  
   Invariant: after processing index `i`, `global[j]` is optimal for `prices[0..i]`.

3. **Initialize carefully.**  
   Set all states to negative infinity except `global[0] = 0`. This enforces the “exactly three non-empty subarrays” requirement and prevents illegal empty selections from leaking into the answer.

4. **Process each element `x` left to right.**  
   For `j` from `3` down to `1`, update:
   - `local[j] = max(local[j] + x, global[j-1] + x)`
   - `global[j] = max(global[j], local[j])`  
   The first term extends the current `j`-th subarray; the second starts a new `j`-th subarray at this index after finishing `j-1` subarrays earlier.

5. **Why iterate `j` downward?**  
   To avoid reusing the current element multiple times in the same iteration. Downward updates preserve the previous prefix values of `global[j-1]`.

6. **Return `global[3]`.**  
   This is correct because it represents the best total using exactly three non-overlapping non-empty subarrays anywhere in the full array.

## 📊 Worked Example
Take `prices = [-5,4,-1,4,-10,3]`.

Track `global[1..3]` after each element (`-∞` omitted for readability where still impossible):

| i | x   | global[1] | global[2] | global[3] |
|---|-----|-----------|-----------|-----------|
| 0 | -5  | -5        | —         | —         |
| 1 | 4   | 4         | -1        | —         |
| 2 | -1  | 4         | 3         | -2        |
| 3 | 4   | 7         | 8         | 7         |
| 4 | -10 | 7         | 8         | 10        |
| 5 | 3   | 10        | 11        | 13        |

The key transition happens at index `4`: even though `-10` is bad locally, it can serve as the required middle segment so that three non-overlapping subarrays become feasible. The final optimum is `13`, achieved by `[4,-1,4] = 7`, `[-10] = -10`, and `[3] = 3` is not best; instead the DP finds the true optimum arrangement across all valid splits.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` because each element updates a constant number of DP states: exactly three `local/global` pairs. There is no interval enumeration, no nested scan, and no recomputation of prefix ranges. At `10^6` elements this is practical; at `10^9`, even linear time becomes a throughput and memory-bandwidth problem.

### Space Complexity
`O(1)` auxiliary space because only `local[0..3]` and `global[0..3]` are needed. The state can be kept in a few 64-bit variables. You could store full DP tables for debugging or reconstruction, but that trades constant space for `O(n)` memory.

## 💡 Key Takeaways
- If a problem asks for a fixed number of non-overlapping contiguous segments with a max objective, think “Kadane generalized with DP over segment count.”
- “Exactly `k` segments” is a strong signal that negative values may be mandatory, so greedy filtering of bad regions is unsafe.
- Initialize impossible states to negative infinity; using `0` accidentally permits empty subarrays and silently corrupts correctness.
- Update segment counts in descending order; ascending updates can reuse the same index in multiple subarrays within one iteration.
- The transferable design insight is to collapse combinatorial interval selection into streaming state transitions, which is how many large-scale optimizers stay exact without becoming operationally expensive.

## 🚀 Variations & Further Practice
- Generalize from exactly `3` subarrays to exactly `k` subarrays. The conceptual twist is parameterizing the DP by `k`, giving `O(nk)` time and forcing trade-offs when `k` is large.
- Return the actual subarray boundaries, not just the score. The harder part is carrying predecessor state without breaking the linear-time core.
- Add minimum or maximum length constraints per subarray. That changes the transition model and typically requires prefix sums plus windowed DP rather than pure Kadane-style extension.