# Minimum Cost to Cut a Ribbon into Segments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Greedy, Sorting

---

## 🗂 Problem Overview

Given a ribbon of length `n`, a set of candidate cut positions, and a per-cut cost, select exactly `k-1` cuts to partition the ribbon into `k` segments at minimum total cost. The non-trivial constraint is the exact count requirement: you cannot greedily take the cheapest cuts without verifying that exactly `k-1` are available. If fewer than `k-1` cut positions exist, return `-1`. The problem reduces to a constrained subset-selection over an indexed cost array.

---

## 🌍 Engineering Impact

This pattern — selecting exactly `m` items from a candidate set to minimize total cost subject to a hard count constraint — appears in resource allocation across distributed systems. Concrete examples: partition key selection in sharded databases (choosing split points to balance load at minimum rebalancing cost), segment boundary decisions in video transcoding pipelines, and budget-constrained feature selection in ML pipelines. Without the exact-count enforcement, greedy works; with it, greedy fails silently, producing wrong segment counts that corrupt downstream invariants at scale.

---

## 🔍 Problem Statement

**Input:** Integer `k`, array `cuts` of distinct positions, array `costs` where `costs[i]` is the cost to cut at `cuts[i]`.
**Output:** Minimum total cost to make exactly `k-1` cuts; `-1` if impossible.

**Constraints:**
- `2 ≤ k ≤ 100`
- `1 ≤ cuts.length ≤ 300`
- `cuts.length == costs.length`
- `1 ≤ costs[i] ≤ 10^4`

**Examples:**

| Input | k | Output | Reason |
|---|---|---|---|
| cuts=[2,5,7,9], costs=[3,8,2,6] | 3 | 5 | Pick positions 2 (cost 3) + 7 (cost 2) |
| cuts=[1,4,6], costs=[10,5,7] | 4 | 22 | Need 3 cuts, exactly 3 available; must use all |

The algorithmic driver: the hard constraint on cut count rules out pure greedy and demands a selection strategy that tracks how many cuts have been chosen.

---

## 🪜 How to Solve This

1. **Read the constraint** → we need exactly `k-1` cuts, not "at most." Greedy (sort by cost, take cheapest) ignores count and breaks the exact-segment requirement.

2. **Exact count + minimize cost** → classic 0/1 knapsack framing. Items are cuts, the "weight" is count (each cut contributes 1), the "value" is cost (we minimize, not maximize), and the capacity is exactly `k-1`.

3. **Reframe as DP** → define `dp[j]` = minimum cost to make exactly `j` cuts from the cuts seen so far. Initialize `dp[0] = 0`, all others `= ∞` (impossible states).

4. **Iterate** → for each cut, update `dp[j] = min(dp[j], dp[j-1] + costs[i])` in reverse order (standard 0/1 knapsack direction to prevent reuse).

5. **Answer** → `dp[k-1]` after processing all cuts. If still `∞`, return `-1`.

The reverse-iteration insight is the key: it enforces that each cut is used at most once without a second dimension in the table.

---

## 🧩 Algorithm Walkthrough

**Pattern: 0/1 Knapsack DP** — appropriate because each cut is either selected or not (no repetition), we have a hard capacity (exactly `k-1` selections), and we optimize a scalar cost.

**Steps:**

1. **Validate early:** If `cuts.length < k-1`, immediately return `-1`. No DP needed.

2. **Initialize DP table:** Allocate `dp[0..k-1]` where `dp[0] = 0` and `dp[1..k-1] = ∞`. The invariant: `dp[j]` always holds the minimum cost achievable using exactly `j` cuts from cuts processed so far.

3. **Outer loop — iterate cuts:** For each index `i` from `0` to `cuts.length - 1`, process `costs[i]`.

4. **Inner loop — update in reverse:** For `j` from `min(i+1, k-1)` down to `1`:
   - `dp[j] = min(dp[j], dp[j-1] + costs[i])`
   - Reverse order ensures cut `i` is counted at most once. Forward order would allow the same cut to fill multiple slots.

5. **Invariant check:** After processing cut `i`, `dp[j]` is optimal for exactly `j` cuts chosen from the first `i+1` candidates.

6. **Return result:** `dp[k-1] == ∞ ? -1 : dp[k-1]`.

No sorting required — cut positions are irrelevant to cost minimization; only `costs[i]` values matter.

---

## 📊 Worked Example

**Input:** `cuts=[2,5,7,9]`, `costs=[3,8,2,6]`, `k=3` → need exactly 2 cuts.

Initial state: `dp = [0, ∞, ∞]`

| Cut index | cost | j=2 update | j=1 update | dp after |
|---|---|---|---|---|
| 0 | 3 | dp[2]=min(∞, dp[1]+3)=∞ | dp[1]=min(∞, dp[0]+3)=**3** | [0, 3, ∞] |
| 1 | 8 | dp[2]=min(∞, dp[1]+8)=**11** | dp[1]=min(3, dp[0]+8)=3 | [0, 3, 11] |
| 2 | 2 | dp[2]=min(11, dp[1]+2)=**5** | dp[1]=min(3, dp[0]+2)=**2** | [0, 2, 5] |
| 3 | 6 | dp[2]=min(5, dp[1]+6)=5 | dp[1]=min(2, dp[0]+6)=2 | [0, 2, 5] |

**Result:** `dp[2] = 5` ✓ — cuts at positions 2 (cost 3) and 7 (cost 2).

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n · k)** where `n = cuts.length` and `k` is the segment count. The dominant operation is the double loop: `n` cuts × `k-1` DP states. With `n ≤ 300` and `k ≤ 100`, this is at most 30,000 operations — trivial. At `n = 10^6` and `k = 10^3`, it reaches `10^9` and requires optimization or problem restructuring.

### Space Complexity

**O(k)** — the DP table owns all space, sized to `k`. No auxiliary structures needed. This is already optimal for 1D knapsack; reducing further would require recomputing states, trading time for space with no practical benefit given the constraint bounds.

---

## 💡 Key Takeaways

- **Pattern signal — exact count constraint:** When a problem requires selecting *exactly* `m` items (not "at most"), greedy fails and 0/1 knapsack is the right frame. The word "exactly" in the problem statement is the trigger.
- **Pattern signal — minimize sum over a subset:** Minimize-cost subset selection with a cardinality constraint maps directly to knapsack; recognize it by the combination of a cost array, a count limit, and an optimization objective.
- **Gotcha — reverse inner loop:** Iterating `j` downward is non-negotiable for 0/1 knapsack. Forward iteration silently allows a single cut to be "used" multiple times, producing incorrect results that pass naive test cases.
- **Gotcha — ∞ initialization vs. 0:** Initializing non-zero DP states to `0` instead of `∞` conflates "zero cost" with "impossible state," causing the algorithm to return `0` instead of `-1` for infeasible inputs — a correctness bug that only surfaces on edge-case inputs.
- **Architectural insight:** The reverse-iteration trick that collapses a 2D knapsack table into 1D is a general memory optimization applicable anywhere you process items sequentially with a bounded capacity — relevant in streaming systems where you cannot materialize the full state matrix.

---

## 🚀 Variations & Further Practice

- **Unbounded cut reuse (Cutting Rod Problem):** If each cut position can be used multiple times, the 0/1 constraint lifts and the inner loop runs forward. The conceptual twist: forward vs. reverse iteration encodes the entire difference between bounded and unbounded selection — a single loop direction change changes the problem class.
- **Interval DP — Cost Depends on Segment Length:** In LeetCode 1547 (*Minimum Cost to Cut a Stick*), cut cost equals the length of the stick being cut, making cost order-dependent. This breaks the independence assumption here and requires a 2D interval DP over `dp[i][j]` — a strictly harder problem that shares the setup but demands a fundamentally different recurrence.
- **K-partition with balance constraints:** Extend by adding a constraint that no segment exceeds length `L`. Now you need both the cut count and a feasibility check on segment lengths, combining knapsack with a geometric validity condition — relevant in database horizontal partitioning with shard size limits.