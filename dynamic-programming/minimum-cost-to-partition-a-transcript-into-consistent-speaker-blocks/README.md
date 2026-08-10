# Minimum Cost to Partition a Transcript into Consistent Speaker Blocks

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, array, partition-dp

---

## 🗂 Problem Overview
Given an array `labels`, partition it into contiguous blocks to minimize total cost. A block costs `overhead` plus the number of utterances that do not match the most frequent speaker inside that block. The output is the minimum total cost over all valid partitions. The challenge is that every cut changes both fixed overhead and local mismatch cost, so greedy choices fail; the problem is a classic partition DP under `n <= 5000`.

## 🌍 Engineering Impact
This pattern shows up anywhere a long sequence must be segmented under a fixed per-segment cost plus an internal inconsistency penalty: diarization cleanup in speech pipelines, log/session boundary inference, compiler basic-block formation with repair costs, and streaming event bucketing with schema drift. At scale, naive local heuristics over-fragment data or merge incompatible regions, both of which inflate downstream cost. A partition-DP framing gives a globally optimal segmentation policy, which is exactly what matters when segment boundaries drive billing, indexing, cache locality, or human review workload.

## 🔍 Problem Statement
You are given an integer array `labels` of length `n`, where `labels[i]` is the speaker ID of the `i`-th utterance, and an integer `overhead`. Split the array into one or more contiguous blocks covering the full transcript. For each block `[l..r]`, choose exactly one owner speaker. The optimal owner is the speaker with maximum frequency in that block, so the block cost is:

`overhead + ((r - l + 1) - maxFreq(l, r))`

Return the minimum total cost across all partitions.

Constraints:

- `1 <= n <= 5000`
- `1 <= labels[i] <= 5000`
- `0 <= overhead <= 10^9`
- Answer fits in signed 64-bit integer

Examples:

- `labels = [1,2,1,1,3], overhead = 2` → `4`
- `labels = [4,4,2,2,2,4,4], overhead = 1` → `3`

The key constraint is `n = 5000`: too large for exponential partitioning, but small enough for `O(n^2)` dynamic programming.

## 🪜 How to Solve This
1. Read the cost formula → each partition is a sequence of contiguous blocks, so this is immediately a **prefix optimization** problem.

2. Define `dp[i]` = minimum cost to partition the first `i` utterances. Then the last block must start at some `j` and end at `i - 1`.

3. That gives the recurrence:  
   `dp[i] = min(dp[j] + cost(j, i-1))` for all `0 <= j < i`.

4. Now focus on `cost(j, i-1)`. For a fixed right endpoint, if you extend the block leftward one element at a time, you can maintain:
   - block length
   - frequency of each speaker
   - current maximum frequency

5. That means you do **not** need a precomputed `O(n^2)` cost table. For each `i`, scan `j` backward, update counts incrementally, and relax `dp[i]`.

6. Why this works: every valid partition has a unique last cut. Enumerating all `j` explores all partitions exactly once, and incremental frequency maintenance keeps each transition `O(1)` amortized.

This is the standard mental path to **partition DP with online segment-cost maintenance**.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Partition DP.**  
   The problem asks for an optimal way to cut an array into contiguous pieces, with total cost equal to the sum of per-piece costs. That is the canonical signature of partition dynamic programming.

2. **Define state.**  
   Let `dp[i]` be the minimum cost to partition prefix `labels[0..i-1]`.  
   Invariant: after computing `dp[i]`, it equals the optimal answer for the first `i` elements.

3. **Base case.**  
   `dp[0] = 0`, since an empty prefix needs no blocks.  
   This anchors all later transitions.

4. **Enumerate the last block.**  
   For each `i` from `1` to `n`, consider every possible start `j` of the final block ending at `i - 1`.  
   Candidate value: `dp[j] + overhead + ((i - j) - maxFreq(j, i-1))`.

5. **Maintain block statistics incrementally.**  
   While iterating `j` backward from `i - 1` to `0`, update a frequency array/hash map for `labels[j]` and track `maxFreq`.  
   Invariant: after processing `j`, the data structure exactly represents block `[j..i-1]`.

6. **Relax the DP transition.**  
   Compute the current block cost and minimize `dp[i]`.  
   This is correct because every optimal partition of prefix `i` must end with some block `[j..i-1]`.

7. **Return `dp[n]`.**  
   Total runtime is `O(n^2)`, because each pair `(j, i)` is visited once, and block statistics are updated in constant time.

## 📊 Worked Example
Take `labels = [1,2,1,1,3]`, `overhead = 2`.

We compute `dp[i]` for prefixes of length `i`.

| `i` | backward blocks considered | best `dp[i]` |
|---|---|---|
| 1 | `[1]`: cost `2 + (1-1)=2` → `dp[0]+2=2` | 2 |
| 2 | `[2]`: `dp[1]+2=4`; `[1,2]`: maxFreq=1, cost=3 → `dp[0]+3=3` | 3 |
| 3 | `[1]`: `dp[2]+2=5`; `[2,1]`: cost=3 → `dp[1]+3=5`; `[1,2,1]`: maxFreq=2, cost=3 → `dp[0]+3=3` | 3 |
| 4 | `[1]`: 5; `[1,1]`: `dp[2]+2=5`; `[2,1,1]`: cost=3 → `dp[1]+3=5`; `[1,2,1,1]`: maxFreq=3, cost=3 → `dp[0]+3=3` | 3 |
| 5 | `[3]`: `dp[4]+2=5`; longer blocks never beat whole prefix | 4 |

Final answer: `dp[5] = 4`, achieved by taking the entire array as one block.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)`. For each right endpoint `i`, we scan all possible left endpoints `j` backward once, updating frequencies and `maxFreq` in constant time. At `n = 5000`, this is about 25 million transitions, which is practical. At `10^6`, quadratic DP is already infeasible; at `10^9`, it is completely out of scope.

### Space Complexity
`O(n + V)`, where `n` is for `dp` and `V` is the speaker-ID domain used by the frequency structure. With constraints here, `V <= 5000`, so an array is simplest. Space can be reduced only marginally; the main trade-off is array versus hash map.

## 💡 Key Takeaways
- If the problem asks for an optimal split of an array into contiguous segments with additive segment cost, think **partition DP** immediately.
- If segment cost depends on a statistic that can be updated while expanding or shrinking a window, look for **incremental transition evaluation** instead of precomputing all segment costs.
- Be careful with indexing: `dp[i]` should represent the first `i` elements, so the last block is `[j..i-1]`, not `[j..i]`.
- Reset the frequency structure for each new right endpoint `i`; reusing counts across different `i` values without clearing corrupts `maxFreq`.
- The transferable design insight is to separate **global optimization over boundaries** from **local incremental maintenance of segment state**; that decomposition scales well in both algorithms and production pipelines.

## 🚀 Variations & Further Practice
- Add a maximum or minimum allowed block length. Same partition DP, but transitions are restricted; the twist is enforcing feasibility while preserving efficient segment-cost updates.
- Make the owner speaker chosen from a weighted set, where mismatches and matches have non-uniform costs. The twist is that `maxFreq` is replaced by a more general per-label score aggregate.
- Allow editing operations between adjacent blocks, such as merge discounts or switch penalties. The twist is that block cost is no longer independent, so DP state must include boundary context.