# Maximum Alternating Gain from a Contiguous Trade Window

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Kadane Variant

---

## 🗂 Problem Overview
Given an integer array `profits`, choose one non-empty contiguous subarray and evaluate it with alternating signs starting from that subarray’s first element: `+ - + - ...`. Return the maximum possible score across all such windows. The challenge is that the sign pattern resets at every candidate start index, so this is not a standard maximum subarray problem. With up to `200000` elements, any approach that evaluates many windows explicitly is too slow.

## 🌍 Engineering Impact
This pattern shows up anywhere a contiguous event window is scored with position-dependent weighting: market microstructure analysis, streaming anomaly detection, ranking pipelines with alternating penalties/bonuses, and telemetry windows where adjacent events offset each other. At scale, brute-force window evaluation collapses under quadratic growth and becomes unusable in real-time systems or batch jobs with tight SLAs. The right abstraction—a constant-state dynamic program over a stream—lets you score windows online, bound memory, and preserve throughput under high-cardinality inputs without materializing candidate segments or replaying history.

## 🔍 Problem Statement
You are given an integer array `profits` where `profits[i]` is the profit or loss of the `i`-th trade. Select exactly one non-empty contiguous subarray `profits[l..r]`. Its score is computed relative to its own start:

`profits[l] - profits[l+1] + profits[l+2] - profits[l+3] + ...`

Return the maximum score over all valid subarrays.

Constraints:

- `1 <= profits.length <= 200000`
- `-1000000000 <= profits[i] <= 1000000000`
- Result fits in signed 64-bit integer

Examples:

- `profits = [4, 2, 5, 3]` → `7`  
  Best window: `[4, 2, 5]` → `4 - 2 + 5 = 7`

- `profits = [5, -1, -3, 4]` → `6`  
  Best window: `[5, -1]` → `5 - (-1) = 6`

The key constraint is input size: `O(n^2)` enumeration of all windows is infeasible, so the solution must be linear or close to it.

## 🪜 How to Solve This
1. Start with the obvious definition: every chosen subarray begins with a `+`, then alternates `-`, `+`, `-`.  
   → That means the score of a window depends not just on values, but on the parity of its length.

2. Ask what state matters when extending a window by one element.  
   → If the current window length is odd, the next element will be subtracted. If even, it will be added.

3. That suggests two DP states per position:  
   → best alternating score of a subarray ending here with odd length  
   → best alternating score of a subarray ending here with even length

4. Derive transitions:  
   → odd-length window ending at `i` is either a new window `[profits[i]]` or an even-length window extended by `+profits[i]`  
   → even-length window ending at `i` must come from an odd-length window extended by `-profits[i]`

5. This is a Kadane variant.  
   → Instead of “best ending here,” we track two parity-aware versions of “best ending here.”

6. Scan once, update both states, and keep a global maximum over odd and even endings.

## 🧩 Algorithm Walkthrough
1. **Use dynamic programming with two rolling states**  
   This is a **Kadane Variant / DP on subarray endings**. Standard Kadane tracks the best sum ending at each index. Here, the alternating sign forces us to also track the parity of the chosen window length.

2. **Define the states**  
   Let:
   - `odd` = maximum alternating score of a subarray ending at the current index with odd length
   - `even` = maximum alternating score of a subarray ending at the current index with even length

   These states are sufficient because the next sign is determined entirely by current parity.

3. **Initialize from the first element**  
   At index `0`, the only valid subarray is `[profits[0]]`, so:
   - `odd = profits[0]`
   - `even = -∞` because no even-length non-empty subarray can end at the first element

4. **Transition for each next value `x`**  
   - New odd-length subarray ending here: either start fresh with `x`, or extend a previous even-length subarray by adding `x`  
     `newOdd = max(x, even + x)`
   - New even-length subarray ending here: must extend a previous odd-length subarray by subtracting `x`  
     `newEven = odd - x`

   This is correct because every valid alternating window ending at `i` must fall into exactly one of those constructions.

5. **Preserve the invariant**  
   After processing index `i`, `odd` and `even` represent the best scores among all subarrays ending exactly at `i` with the corresponding parity.

6. **Track the answer globally**  
   The optimal window can end anywhere, so update `ans = max(ans, odd, even)` at each step.

7. **Why this abstraction fits**  
   The problem is contiguous, local, and extension-based. That is exactly where rolling DP dominates: one pass, constant memory, no backtracking, no prefix-structure overhead.

## 📊 Worked Example
Take `profits = [4, 2, 5, 3]`.

| i | x | newOdd = max(x, even + x) | newEven = odd - x | odd | even | ans |
|---|---:|---:|---:|---:|---:|---:|
| 0 | 4 | 4 | -∞ | 4 | -∞ | 4 |
| 1 | 2 | max(2, -∞) = 2 | 4 - 2 = 2 | 2 | 2 | 4 |
| 2 | 5 | max(5, 2 + 5) = 7 | 2 - 5 = -3 | 7 | -3 | 7 |
| 3 | 3 | max(3, -3 + 3) = 3 | 7 - 3 = 4 | 3 | 4 | 7 |

Trace interpretation:

- At `i = 2`, `odd = 7` corresponds to extending the even-length window ending at `i = 1` with `+5`, giving `[4, 2, 5]`.
- At `i = 3`, the best even-length window ending there is `4`, from `[4, 2, 5, 3]`.

Final answer: `7`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` where `n = profits.length`. Each element is processed once with constant-time state updates and comparisons. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, linear time is still the lower bound, so throughput and streaming execution become the real operational concerns.

### Space Complexity
`O(1)`. The algorithm stores only two DP states and one global maximum. No auxiliary arrays are required. You could store full DP tables for debugging or reconstruction, but that increases memory to `O(n)` without improving the optimal score computation.

## 💡 Key Takeaways
- If a contiguous-window problem says “score depends on position within the chosen subarray,” look for DP state keyed by parity, phase, or modulo class.
- If extending a window only depends on a small summary of the previous window, it is often a Kadane-style rolling DP rather than prefix-sum enumeration.
- The alternating pattern restarts at the chosen subarray’s start, so you cannot assign fixed signs by original array index and run plain max-subarray logic.
- Initialize the even-length state to negative infinity, not zero; otherwise you accidentally allow invalid empty-prefix transitions.
- In production scoring pipelines, compact state machines often replace expensive window enumeration when the scoring rule is local and compositional.

## 🚀 Variations & Further Practice
- **Maximum alternating score with up to `k` disjoint windows** — adds segmentation decisions on top of parity state, pushing the solution toward higher-dimensional DP.
- **Maximum alternating score with arbitrary periodic coefficients** — replace `+1, -1` with a repeating weight pattern like `[3, -2, 5]`, requiring DP over phase modulo pattern length.
- **Return the actual optimal window indices** — same core DP, but now you must carry predecessor metadata and handle tie-breaking cleanly.