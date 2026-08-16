# Longest Commute Stretch Within Fare Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** sliding window, two pointers, array

---

## 🗂 Problem Overview
Given an array `fares` of positive integers and a `budget`, find the maximum length of any contiguous subarray whose sum is at most `budget`. The output is a single integer: the longest valid commute stretch. The non-trivial constraint is scale: `fares.length` can reach `100000`, which rules out checking every subarray. The fact that all fares are positive is the key property that makes a linear-time sliding window possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous interval under a cumulative cap: API request bursts within quota, streaming event batches under memory thresholds, reimbursement windows in expense analytics, or media buffering segments under bandwidth budgets. At production scale, brute-force range evaluation collapses under quadratic behavior and destroys latency predictability. The sliding-window approach turns a potentially explosive search space into a single pass with bounded state. That matters operationally: lower CPU, stable tail latency, simpler memory behavior, and a design that works equally well in offline analytics jobs and near-real-time stream processors.

## 🔍 Problem Statement
You are given an integer array `fares` where `fares[i]` is the fare paid on the `i`-th ride, and an integer `budget`. A commute stretch is any contiguous sequence of rides. Return the maximum number of consecutive rides whose total fare is less than or equal to `budget`.

Constraints:

- `1 <= fares.length <= 100000`
- `1 <= fares[i] <= 10000`
- `1 <= budget <= 1000000000`
- All fares are positive integers

If no single ride fits within the budget, return `0`.

Examples:

- `fares = [2, 1, 3, 2, 1], budget = 5` → `2`
- `fares = [4, 2, 1, 1, 3], budget = 6` → `3`

The decisive constraint is positivity: once a window exceeds `budget`, extending it further can only increase the sum, so the left side must move forward.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** segment, so this is about subarrays, not arbitrary subsets.

2. Notice the objective → maximize window length subject to `sum <= budget`. That usually suggests maintaining a running range instead of recomputing sums for every candidate.

3. Check the value constraints → all fares are **positive**. That is the signal for a sliding window:
   - expanding the right pointer always increases the sum,
   - shrinking the left pointer always decreases the sum.

4. Start with an empty window and grow it ride by ride.
   - Add `fares[right]` to the running sum.
   - If the sum exceeds `budget`, the current window is invalid.

5. Restore validity by moving `left` forward until `sum <= budget` again.
   - Because all values are positive, this is the only way to recover.

6. After each adjustment, the current window is the longest valid window ending at `right`, so update the best length.

7. Since each pointer only moves forward, the whole scan is linear. That is the core reason this approach beats brute force.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` and a running `sum` of fares inside it. This pattern fits because the window condition is monotonic under positive numbers: adding elements can only increase `sum`, removing from the left can only decrease it.

2. **Initialize state.**  
   Set `left = 0`, `sum = 0`, and `maxLen = 0`. These variables are sufficient; no auxiliary array or prefix structure is required.

3. **Expand the window with `right`.**  
   For each index `right`, add `fares[right]` to `sum`. This considers every ride as the new end of a candidate commute stretch.

4. **Shrink while invalid.**  
   While `sum > budget`, subtract `fares[left]` from `sum` and increment `left`. This step is correct because any valid window ending at `right` must exclude enough prefix rides to bring the total back under budget.

5. **Record the best valid window.**  
   Once the loop exits, `[left, right]` is valid and is the longest valid window ending at `right`. Any earlier `left` would make the sum too large, so `right - left + 1` is the correct local optimum.

6. **Maintain the invariant.**  
   After each iteration:
   - `sum` equals the total of `fares[left..right]`
   - `sum <= budget`
   - `left` is the smallest index that makes the current window valid

7. **Return `maxLen`.**  
   If every individual fare exceeds `budget`, the window repeatedly collapses and `maxLen` remains `0`, which matches the required behavior.

## 📊 Worked Example
Example: `fares = [4, 2, 1, 1, 3]`, `budget = 6`

| right | fare | action                         | left | sum | window       | maxLen |
|------:|-----:|--------------------------------|-----:|----:|--------------|-------:|
| 0     | 4    | add 4                          | 0    | 4   | `[4]`        | 1      |
| 1     | 2    | add 2                          | 0    | 6   | `[4,2]`      | 2      |
| 2     | 1    | add 1, sum=7 > 6, remove 4     | 1    | 3   | `[2,1]`      | 2      |
| 3     | 1    | add 1                          | 1    | 4   | `[2,1,1]`    | 3      |
| 4     | 3    | add 3, sum=7 > 6, remove 2     | 2    | 5   | `[1,1,3]`    | 3      |

The best valid stretches are `[2,1,1]` and `[1,1,3]`, both length `3`. The algorithm never revisits earlier right positions and only advances `left` when required to restore `sum <= budget`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`, where `n = fares.length`. Each element is added to the window once by `right` and removed at most once by `left`, so the dominant work is a single linear scan. At `10^6` elements this is routine; at `10^9`, the algorithm is still asymptotically optimal but becomes constrained by I/O and runtime budgets.

### Space Complexity
`O(1)`. The algorithm stores only scalar state: two pointers, a running sum, and the best length. No extra data structure grows with input size. Space cannot be meaningfully reduced further without sacrificing the ability to track the active window sum.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a threshold and all values are positive, sliding window should be your first candidate.
- “Expand until invalid, then shrink until valid” is the recognition pattern for many two-pointer range problems with monotonic constraints.
- Update the answer **after** restoring `sum <= budget`; doing it before shrink logic records invalid windows.
- Be precise about window length: for an inclusive range `[left, right]`, the size is `right - left + 1`, not `right - left`.
- In production systems, positivity or monotonicity constraints often determine whether a streaming one-pass solution is possible or whether you need heavier range-query machinery.

## 🚀 Variations & Further Practice
- **Allow negative fares or credits.** Sliding window breaks because expanding the window no longer guarantees a larger sum; this typically pushes you toward prefix sums plus ordered structures.
- **Count how many subarrays fit within the budget.** Same window idea, but instead of tracking only the maximum length, accumulate the number of valid windows ending at each `right`.
- **Find the shortest subarray with sum at least `target`.** Similar mechanics, but the optimization direction flips and the shrink condition changes, which alters when you update the answer.