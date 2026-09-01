# Longest Checkout Span With Gift Card Balance Floor

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Prefix Sum, Deque

---

## 🗂 Problem Overview
Given `transactions` and an initial `startBalance`, find the longest contiguous subarray such that if you process its elements left to right, the running balance never becomes negative. The output is the maximum valid span length. The challenge is that validity depends not just on the total sum of a window, but on the minimum prefix sum reached inside it, so naive sliding-window checks are too slow for `n` up to 200,000.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream must remain above a safety floor while maximizing throughput or retention. Examples include prepaid wallet ledgers, token-bucket or rate-limit credit streams, warehouse inventory deltas, and streaming resource schedulers that cannot overdraw capacity mid-batch. At scale, recomputing the worst intermediate state for every candidate interval collapses under high-cardinality event streams. Maintaining the minimum prefix state incrementally enables linear-time admission checks, which is the difference between online processing and expensive replay, backtracking, or over-conservative truncation of otherwise valid work.

## 🔍 Problem Statement
You are given an integer array `transactions` where positive values add to a gift card and negative values spend from it, plus an integer `startBalance`. For any contiguous span `transactions[l...r]`, define the running balance as `startBalance` plus the cumulative sum from `l` through each position `k` in `[l, r]`. The span is valid only if every such intermediate balance is at least `0`.

Return the length of the longest valid contiguous span.

Constraints:

- `1 <= transactions.length <= 200000`
- `-100000 <= transactions[i] <= 100000`
- `0 <= startBalance <= 1000000000`

Examples:

- `transactions = [4, -3, -2, 5, -1], startBalance = 2` → `5`
- `transactions = [-4, 3, -2, -1, 2], startBalance = 2` → `4`

The key constraint is input size: any approach that rechecks all prefixes inside each window is too slow.

## 🪜 How to Solve This
1. Read the condition carefully → the window is valid if its **worst prefix dip** is no lower than `-startBalance`.
2. That immediately suggests prefix sums: if `P[i]` is the global prefix sum, then inside window `[l, r]`, the running balance at position `k` is `startBalance + (P[k+1] - P[l])`.
3. So the whole window is valid iff `min(P[l+1...r+1]) - P[l] >= -startBalance`.
4. Now the problem becomes: while moving `r` forward, how do we know the minimum prefix sum inside the current window quickly?
5. A monotonic deque solves exactly that: keep candidate prefix indices in increasing prefix-sum order, so the front is always the minimum.
6. Expand right → add the new prefix sum into the deque.
7. If the window becomes invalid, move `l` right until the minimum prefix in the window satisfies the balance floor again.
8. Because each index enters and leaves the deque once, the whole process is linear.

This is the standard “sliding window + maintain window minimum” pattern, expressed over prefix sums rather than raw values.

## 🧩 Algorithm Walkthrough
1. **Build prefix sums.**  
   Let `prefix[0] = 0`, and `prefix[i+1] = prefix[i] + transactions[i]`.  
   For a window `[l, r]`, every in-window running total is `prefix[t] - prefix[l]` for `t in [l+1, r+1]`. This converts a dynamic “running balance” condition into a range-min query over prefix sums.

2. **Reframe validity.**  
   The window `[l, r]` is valid iff  
   `startBalance + min(prefix[l+1...r+1]) - prefix[l] >= 0`.  
   So for fixed `l`, `r`, we only need the minimum prefix sum among indices currently inside the window.

3. **Use the Sliding Window + Monotonic Deque pattern.**  
   As `r` grows, insert `prefix[r+1]` into a deque while popping larger values from the back. The deque stores indices with nondecreasing prefix sums, so its front always holds the minimum prefix sum in the active range.

4. **Shrink when invalid.**  
   If `startBalance + prefix[deque.front] - prefix[l] < 0`, the window violates the floor. Increment `l` until it becomes valid again. While advancing `l`, remove `l+1` from the deque front if that prefix index leaves the window.

5. **Track the best length.**  
   After restoring validity, update `answer = max(answer, r - l + 1)`.

6. **Why this is correct.**  
   The deque invariant guarantees exact access to the minimum relevant prefix sum. The two-pointer invariant guarantees `l` is always the smallest left boundary that makes the current window valid, so every maximal valid window ending at `r` is considered.

## 📊 Worked Example
Example: `transactions = [-4, 3, -2, -1, 2]`, `startBalance = 2`

Prefix sums: `prefix = [0, -4, -1, -3, -4, -2]`

| r | add `prefix[r+1]` | deque (idx:val) after push | l before shrink | valid? | l after shrink | best |
|---|---:|---|---:|---|---:|---:|
| 0 | -4 | `[1:-4]` | 0 | no (`2 + -4 - 0 < 0`) | 1 | 0 |
| 1 | -1 | `[2:-1]` | 1 | yes (`2 + -1 - (-4) = 5`) | 1 | 1 |
| 2 | -3 | `[3:-3] [2:-1]` | 1 | yes | 1 | 2 |
| 3 | -4 | `[4:-4] [3:-3] [2:-1]` | 1 | yes | 1 | 3 |
| 4 | -2 | `[4:-4] [3:-3] [5:-2]` | 1 | yes | 1 | 4 |

The longest valid span is from `l = 1` to `r = 4`, i.e. `[3, -2, -1, 2]`, length `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each prefix index is pushed into the deque once and popped at most once from either end, and both pointers only move forward. For `10^6` elements this is still practical in a single pass; at `10^9`, the algorithmic shape is right but memory bandwidth and storage dominate.

### Space Complexity
`O(n)` if you materialize the full prefix array, plus `O(n)` worst-case deque space. You can reduce auxiliary storage to `O(n)` total by storing only needed prefix values with indices, but not below linear without changing how window minima are queried.

## 💡 Key Takeaways
- If a subarray condition says “every prefix inside the window must satisfy X,” translate the window into prefix sums and look for a range minimum or maximum constraint.
- When a sliding window needs the minimum value of a changing range in `O(1)` amortized time, a monotonic deque is the right signal.
- The validity check is against `min(prefix[l+1...r+1]) - prefix[l]`, not the total window sum; using only the total sum is incorrect.
- Be careful with indices: the deque stores prefix indices `r+1`, while the left boundary uses `prefix[l]`; mixing transaction indices and prefix indices causes off-by-one bugs.
- In production systems, this is the general pattern for maximizing contiguous work under a hard safety floor by tracking the worst intermediate state, not just the final aggregate.

## 🚀 Variations & Further Practice
- **Longest subarray with sum at least `K`**: similar prefix-sum reasoning, but the constraint is on total span sum rather than every intermediate prefix; the deque logic changes from window minimum maintenance to candidate-start pruning.
- **Shortest subarray with sum at least `K`**: harder because you optimize for minimum length, not maximum, and the monotonic deque is used over prefix sums globally rather than as a standard shrink-to-valid window.
- **2D balance floor over matrix strips**: extend the idea to row-compressed submatrices, where each strip becomes a 1D stream; complexity jumps because you now combine prefix sums with nested boundary enumeration.