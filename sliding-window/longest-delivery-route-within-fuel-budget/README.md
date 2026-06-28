# Longest Delivery Route Within Fuel Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Two Pointers, Array

---

## 🗂 Problem Overview
Given an array `costs` of non-negative fuel costs per stop and an integer `budget`, find the maximum length of any contiguous subarray whose sum is at most `budget`. The output is a single integer: the longest affordable consecutive route segment. The challenge is scale: `costs.length` can reach `100000`, so brute-forcing all subarrays is too slow. The non-negative cost constraint is the key property that makes a linear sliding window solution possible.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the longest contiguous span under a cumulative resource cap: streaming pipelines bounded by memory, API gateways enforcing rolling request budgets, media buffering under bandwidth ceilings, and logistics planners fitting route segments into fuel or time envelopes. At scale, naive enumeration turns into quadratic latency and cache-hostile scans that collapse under production traffic. The sliding-window approach converts the problem into a single pass with constant extra state, which matters for hot paths, online processing, and services that must make admission or scheduling decisions without materializing large intermediate structures.

## 🔍 Problem Statement
You are given an integer array `costs` where `costs[i]` is the fuel required for stop `i`, and an integer `budget` representing the maximum total fuel allowed. Return the length of the longest contiguous subarray whose sum is less than or equal to `budget`.

Constraints:

- `1 <= costs.length <= 100000`
- `0 <= costs[i] <= 10000`
- `0 <= budget <= 1000000000`

Examples:

- `costs = [4, 2, 1, 7, 3, 2]`, `budget = 8` → `3`
  - Longest valid window: `[4, 2, 1]` with sum `7`
- `costs = [9, 1, 2, 1, 1]`, `budget = 4` → `4`
  - Longest valid window: `[1, 2, 1, 1]` with sum `4`

Edge cases matter: if every individual stop exceeds `budget`, the answer is `0`. The algorithmic choice is driven by the need for linear time; checking every start/end pair would be `O(n^2)`.

## 🪜 How to Solve This
1. Start from the requirement: we need the **longest contiguous** segment with sum `<= budget`. Contiguous usually suggests a window over the array rather than arbitrary subset logic.

2. Notice the critical constraint: all `costs[i]` are **non-negative**. That changes everything. If you expand a window to the right, the sum can only stay the same or increase — never decrease.

3. That monotonic behavior suggests **sliding window / two pointers**:
   - grow the right edge to include more stops,
   - if the sum becomes too large, move the left edge rightward until the window is valid again.

4. Why does this work? Because once a window exceeds the budget, keeping the same left boundary and adding more elements will never fix it. The only useful move is to shrink from the left.

5. Track the maximum valid window length seen during this process. Since each pointer only moves forward, the full scan is linear.

6. If no single element fits, the maximum length never increases from `0`, which naturally handles that edge case.

## 🧩 Algorithm Walkthrough
1. **Initialize window state**  
   Set `left = 0`, `windowSum = 0`, and `maxLen = 0`.  
   This defines an initially empty window. The invariant we want to maintain is: after any required shrinking, the active window `[left..right]` has `windowSum <= budget`.

2. **Expand the window with the right pointer**  
   Iterate `right` from `0` to `costs.length - 1`. Add `costs[right]` to `windowSum`.  
   This is the standard **Two Pointers / Sliding Window** pattern: one pointer grows the candidate region, the other pointer restores validity when constraints are violated.

3. **Restore validity when over budget**  
   While `windowSum > budget`, subtract `costs[left]` from `windowSum` and increment `left`.  
   This step is correct because all values are non-negative. Once the sum is too large, moving `left` right is the only operation that can reduce it. We never need to revisit earlier left positions.

4. **Record the best valid window**  
   After shrinking, the current window is valid again. Its length is `right - left + 1`. Update `maxLen` if this is larger.  
   The invariant now holds: `maxLen` is the largest valid window length seen so far.

5. **Finish after one pass**  
   Every element enters the window once and leaves at most once. That bounded pointer movement is why the algorithm is `O(n)` rather than quadratic.

## 📊 Worked Example
Use `costs = [4, 2, 1, 7, 3, 2]`, `budget = 8`.

| right | costs[right] | windowSum after add | left after shrink | valid window | length | maxLen |
|------:|-------------:|--------------------:|------------------:|-------------|-------:|-------:|
| 0 | 4 | 4 | 0 | `[4]` | 1 | 1 |
| 1 | 2 | 6 | 0 | `[4,2]` | 2 | 2 |
| 2 | 1 | 7 | 0 | `[4,2,1]` | 3 | 3 |
| 3 | 7 | 14 | 3 | `[7]` | 1 | 3 |
| 4 | 3 | 10 | 4 | `[3]` | 1 | 3 |
| 5 | 2 | 5 | 4 | `[3,2]` | 2 | 3 |

Trace notes:
- At `right = 3`, adding `7` breaks the budget, so the window shrinks from the left: remove `4`, then `2`, then `1`, leaving `[7]`.
- The longest valid segment remains `[4,2,1]` with length `3`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in `O(n)` time. The right pointer advances `n` times, and the left pointer also advances at most `n` times across the entire run. There is no nested rescanning. At `10^6` elements this remains practical; at `10^9`, linear work is still expensive but fundamentally different from infeasible quadratic growth.

### Space Complexity
The algorithm uses `O(1)` extra space. It stores only scalar state: two pointers, a running sum, and the best length. No auxiliary arrays or maps are required. Space cannot be meaningfully reduced further without losing the ability to track the active window state.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under a threshold and all values are non-negative, sliding window should be your first candidate.
- A strong signal for two pointers is monotonic window behavior: expanding right never decreases the aggregate, so shrinking left is enough to restore validity.
- Update `maxLen` **after** shrinking, not before; otherwise you may record an invalid over-budget window.
- Be careful with window length calculation: for inclusive bounds it is `right - left + 1`, not `right - left`.
- In production code, this pattern matters because monotonic constraints let you replace expensive recomputation with streaming, single-pass admission logic.

## 🚀 Variations & Further Practice
- **Minimum size subarray sum**: instead of the longest window under a budget, find the shortest window meeting or exceeding a target. Same pattern, but the optimization direction flips.
- **Longest subarray with sum exactly `k` when negatives are allowed**: sliding window breaks because sums are no longer monotonic; prefix sums plus a hash map become necessary.
- **Maximum consecutive requests within a rolling rate limit over timestamps**: same two-pointer core, but the window constraint is driven by time span and cumulative cost together.