# Minimum Drift to Align Beacon Pulses

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Prefix Sum, Partition DP

---

## 🗂 Problem Overview
Given two sorted integer arrays `a` and `b`, partition each into the same number of non-empty contiguous groups, preserving order. Matching group `k` from `a` with group `k` from `b` incurs cost equal to the absolute difference of their segment sums. Return the minimum total cost across all valid paired partitions. The difficulty is global coupling: a cheap early cut can force expensive later cuts, so local greedy decisions are not reliable.

## 🌍 Engineering Impact
This pattern shows up in stream alignment, telemetry reconciliation, and distributed event ingestion where two ordered feeds must be coalesced into comparable chunks despite drift, loss, or batching differences. Think CDC pipelines, replicated logs, sensor fusion, or billing aggregation across independently buffered producers. At scale, naive pairwise alignment or greedy chunking amplifies skew and creates unstable downstream joins. Prefix-sum-backed partition DP gives a deterministic global optimum under ordering constraints, which matters when reconciliation cost drives alerting, settlement, or correctness-sensitive rollups.

## 🔍 Problem Statement
You are given two non-decreasing integer arrays `a` and `b` with lengths up to `200`. You may partition each array into some number `k` of contiguous, non-empty segments, where both arrays must use the same `k`. Segment `t` in `a` must be matched with segment `t` in `b`.

If a matched pair covers `a[l1..r1]` and `b[l2..r2]`, its drift cost is:

`|sum(a[l1..r1]) - sum(b[l2..r2])|`

Return the minimum total drift cost over all valid paired partitions.

Key constraints:
- `1 <= a.length, b.length <= 200`
- `1 <= a[i], b[j] <= 10^6`
- Arrays are sorted in non-decreasing order
- Result fits in signed 64-bit integer

Examples:
- `a = [2,5,9], b = [4,6,7]` → `1`
- `a = [1,3,8,10], b = [2,4,6,12]` → `2`

The algorithmic driver is the quadratic prefix state space combined with many possible previous cut positions.

## 🪜 How to Solve This
1. Read the problem → the decision is not element matching, it is **where to cut prefixes** of both arrays.

2. Each final answer for prefixes `a[0..i-1]` and `b[0..j-1]` depends on the last matched segment ending at `(i, j)` → this strongly suggests **DP over prefixes**.

3. To form that last segment, choose previous cut positions `(p, q)` with `p < i` and `q < j`. Then the transition is:
   `dp[p][q] + |sum(a[p..i-1]) - sum(b[q..j-1])|`.

4. Segment sums appear in every transition → precompute **prefix sums** so each range sum is `O(1)`.

5. The state becomes: `dp[i][j] = minimum cost to align first i elements of a with first j elements of b`.

6. Base case: `dp[0][0] = 0`; any state with one side empty and the other non-empty is invalid.

7. Then enumerate all `(p, q)` for each `(i, j)`. The constraints are only `200`, so an `O(n^2 m^2)` DP is acceptable and far safer than trying to force a greedy or heuristic shortcut.

## 🧩 Algorithm Walkthrough
1. **Build prefix sums**  
   Let `pa[i]` be the sum of the first `i` elements of `a`, and `pb[j]` similarly for `b`.  
   Then `sum(a[p..i-1]) = pa[i] - pa[p]`, and likewise for `b`.  
   Invariant: any candidate segment sum is available in constant time.

2. **Define the DP state**  
   `dp[i][j]` = minimum total drift cost to partition `a[0..i-1]` and `b[0..j-1]` into the same number of matched non-empty groups.  
   This is classic **Partition DP over prefixes**: every valid solution ends with one last matched segment pair.

3. **Initialize invalid and base states**  
   Set all states to infinity except `dp[0][0] = 0`.  
   `dp[i][0]` and `dp[0][j]` remain invalid for positive `i` or `j`, because groups must be non-empty on both sides.  
   Invariant: every finite state corresponds to a legal paired partitioning.

4. **Enumerate transitions**  
   For each `i in [1..n]` and `j in [1..m]`, try all previous cuts `p in [0..i-1]` and `q in [0..j-1]`.  
   The last matched groups are `a[p..i-1]` and `b[q..j-1]`, so:
   `dp[i][j] = min(dp[i][j], dp[p][q] + abs((pa[i]-pa[p]) - (pb[j]-pb[q])))`.

5. **Why this is correct**  
   Every legal partition of prefixes `(i, j)` has a unique final cut pair `(p, q)`. Removing that last pair leaves a smaller valid subproblem `(p, q)`. Conversely, any valid subproblem plus one non-empty segment pair yields a valid larger solution.  
   Invariant: after processing `(i, j)`, `dp[i][j]` is the optimal cost for those prefixes.

6. **Return the full-prefix answer**  
   The result is `dp[n][m]`, the optimum over all possible numbers of matched groups, because the recurrence implicitly considers every legal cut structure.

## 📊 Worked Example
Take `a = [1,3,8,10]`, `b = [2,4,6,12]`.

Prefix sums:
- `pa = [0,1,4,12,22]`
- `pb = [0,2,6,12,24]`

Selected DP states:

| `dp[i][j]` | Best transition | Cost |
|---|---:|---:|
| `dp[1][1]` | `(0,0)` → `|1-2|` | `1` |
| `dp[2][2]` | `(0,0)` → `|4-6|` | `2` |
| `dp[3][3]` | `(0,0)` → `|12-12|` | `0` |
| `dp[4][4]` | min over all cuts | `2` |

Key winning transition for the answer:
- Use `dp[3][3] = 0`
- Last segments: `a[3..3] = [10]`, `b[3..3] = [12]`
- Added cost: `|10 - 12| = 2`

So:
`dp[4][4] = dp[3][3] + 2 = 2`

This corresponds to:
- `a`: `[1,3,8] | [10]`
- `b`: `[2,4,6] | [12]`

## ⏱ Complexity Analysis

### Time Complexity
`O(n^2 * m^2)`, because each state `dp[i][j]` scans all prior cut pairs `(p, q)`. With `n, m <= 200`, that is about `1.6e9` primitive transition combinations in the worst rectangular case, which is high but still feasible in optimized languages with pruning-free tight loops. At `10^6` or `10^9` scale, this approach is completely intractable.

### Space Complexity
`O(n * m)` for the DP table, plus `O(n + m)` for prefix sums. The DP table dominates memory. Space cannot be trivially reduced to one row because each state depends on all earlier rows and columns, not just local neighbors.

## 💡 Key Takeaways
- If the problem asks for splitting ordered sequences into matched contiguous groups, think **prefix DP with cut positions** rather than element-wise alignment.
- If every transition needs repeated segment sums, prefix sums are the immediate signal that the naive recurrence can be made practical.
- Use 1-based prefix indices carefully: segment `a[p..i-1]` is `pa[i] - pa[p]`, not `pa[i-1] - pa[p]`.
- States where one prefix is empty and the other is not must stay invalid; allowing them silently creates illegal zero-length groups.
- In production reconciliation systems, globally optimal partitioning often matters more than locally cheap matches because early batching decisions propagate downstream and distort every later comparison.

## 🚀 Variations & Further Practice
- Add a fixed penalty per matched group. The twist is that the DP must now optimize both drift and segmentation count, changing the trade-off surface.
- Replace absolute difference of sums with squared difference or a weighted cost. The recurrence structure survives, but optimization opportunities and numerical behavior change.
- Allow dropping elements with explicit deletion cost before partitioning. This combines sequence editing with partition DP and substantially expands the state space.