# Longest Whiteboard Notes Within Marker Ink Limit

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Array, Two Pointers

---

## 🗂 Problem Overview
Given a non-negative integer array `ink` and an integer `maxInk`, find the maximum length of a contiguous subarray whose total sum is at most `maxInk`. The output is a single integer: the longest valid window size. The challenge is not correctness but efficiency: `ink.length` can reach `100000`, so brute-force enumeration of all subarrays is too expensive. The non-negative constraint is the key property that enables a linear-time sliding window solution.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous span under a resource budget: API request bursts under rate limits, streaming jobs constrained by memory or latency budgets, media buffering under bandwidth caps, and batch scheduling under CPU or IO quotas. At scale, naive recomputation of every candidate window turns into quadratic behavior, which is unacceptable in hot paths or online processing. The sliding window approach converts repeated sum checks into incremental state updates, enabling predictable O(n) scans, lower tail latency, and simpler admission-control logic in systems that operate continuously over ordered events.

## 🔍 Problem Statement
You are given:

- `ink`: an array of non-negative integers where `ink[i]` is the ink used by the `i`-th note segment
- `maxInk`: the maximum total ink allowed in a contiguous group

Return the maximum number of consecutive segments whose sum is `<= maxInk`.

Constraints:

- `1 <= ink.length <= 100000`
- `0 <= ink[i] <= 10000`
- `0 <= maxInk <= 1000000000`

Examples:

- `ink = [2, 1, 3, 2, 1], maxInk = 5` → `2`
- `ink = [0, 2, 1, 0, 1, 1], maxInk = 3` → `4`

Edge cases matter: zeros can extend a valid window without increasing the sum, `maxInk` can be `0`, and a single element may already exceed the limit. The decisive constraint is that all values are non-negative, which makes shrinking the left side of the window always reduce or preserve the running sum.

## 🪜 How to Solve This
1. Read the problem → we need a **contiguous** group, so this is immediately a subarray problem, not sorting or subset selection.

2. Notice the values are **non-negative** → that is the signal for sliding window. If the current window sum becomes too large, moving the left pointer right can only make it smaller or keep it unchanged.

3. Start with a window `[left..right]` and a running sum. Expand `right` one step at a time, adding `ink[right]`.

4. If the sum exceeds `maxInk`, the current window is invalid. Shrink from the left until the sum is back within budget.

5. Once valid again, the window `[left..right]` is the longest valid window ending at `right`, because any earlier `left` would make the sum larger.

6. Track the maximum window length seen during the scan.

This avoids recomputing subarray sums and avoids nested loops. The reasoning is simple: each index enters the window once and leaves once, so the process stays linear.

## 🧩 Algorithm Walkthrough
1. **Initialize state**  
   Set `left = 0`, `windowSum = 0`, and `best = 0`.  
   This defines an empty sliding window. The invariant is: before recording `best`, the window will always be made valid.

2. **Expand the window with `right`**  
   For each index `right` from `0` to `n - 1`, add `ink[right]` to `windowSum`.  
   This is the standard **Two Pointers / Sliding Window** pattern: one pointer grows the candidate range while the other pointer repairs constraint violations.

3. **Repair invalid windows**  
   While `windowSum > maxInk`, subtract `ink[left]` and increment `left`.  
   This is correct because all values are non-negative. Removing elements from the left cannot increase the sum, so repeated shrinking must eventually restore validity.

4. **Record the current valid length**  
   After the while-loop, the window `[left..right]` satisfies `windowSum <= maxInk`. Update `best = max(best, right - left + 1)`.  
   This is correct because after shrinking as little as necessary, this is the longest valid window ending at `right`.

5. **Maintain the invariant**  
   At the end of every iteration, the maintained invariant is:  
   - `windowSum` equals the sum of `ink[left..right]`  
   - the window is valid (`windowSum <= maxInk`)  
   - `best` is the longest valid window seen so far

6. **Return `best`**  
   Since every feasible window is considered as some ending position `right`, the maximum recorded length is the answer.

## 📊 Worked Example
Use `ink = [0, 2, 1, 0, 1, 1]`, `maxInk = 3`.

| right | ink[right] | windowSum after add | shrink? | left after shrink | valid window     | length | best |
|------:|-----------:|--------------------:|--------:|------------------:|------------------|-------:|-----:|
| 0     | 0          | 0                   | no      | 0                 | `[0]`            | 1      | 1    |
| 1     | 2          | 2                   | no      | 0                 | `[0,2]`          | 2      | 2    |
| 2     | 1          | 3                   | no      | 0                 | `[0,2,1]`        | 3      | 3    |
| 3     | 0          | 3                   | no      | 0                 | `[0,2,1,0]`      | 4      | 4    |
| 4     | 1          | 4                   | yes     | 1                 | `[2,1,0,1]`      | 4      | 4    |
| 5     | 1          | 5                   | yes     | 3                 | `[0,1,1]`        | 3      | 4    |

Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each element is added to `windowSum` once when `right` advances and removed at most once when `left` advances. There is no nested reprocessing of the full array. At `10^6` elements this is practical in a single pass; at `10^9`, runtime becomes throughput-bound but the algorithmic shape remains optimal for exact scanning.

### Space Complexity
`O(1)`. The algorithm stores only a few scalar variables: two pointers, a running sum, and the best length. No auxiliary array or prefix-sum table is required. You cannot asymptotically reduce below constant extra space without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the longest or shortest **contiguous** range under a threshold and all values are non-negative, think sliding window immediately.
- The strongest recognition signal is monotonic repair: when a window becomes invalid, moving one boundary in one direction can only improve the condition.
- The example text is inconsistent; compute from the array directly. For `[0, 2, 1, 0, 1, 1]` with `maxInk = 3`, the correct answer is `4`, not `5`.
- Update `best` only after shrinking until the window is valid; otherwise you record illegal lengths and hide off-by-one bugs.
- In production systems, this pattern matters because ordered-budget constraints are often solved best with incremental state, not repeated full recomputation.

## 🚀 Variations & Further Practice
- **Shortest subarray with sum at least `K`**: harder because the objective flips and negative numbers may require prefix sums plus a monotonic deque instead of a basic window.
- **Longest subarray with at most `K` distinct values**: same sliding-window skeleton, but validity depends on frequency counts rather than a numeric sum.
- **Maximum average or weighted budget windows**: introduces non-local constraints where simple monotonic shrinking may no longer work, pushing the solution toward binary search, prefix sums, or more advanced data structures.