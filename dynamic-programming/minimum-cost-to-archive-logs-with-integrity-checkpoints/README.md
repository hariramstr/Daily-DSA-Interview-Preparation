# Minimum Cost to Archive Logs with Integrity Checkpoints

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, partition-dp, prefix-sum

---

## 🗂 Problem Overview
Given `n` log batches, partition them into exactly `k` non-empty contiguous archive files. The cost of one file is the sum of its batch sizes multiplied by the maximum corruption risk inside that file. Every file except the last must end at an allowed checkpoint from a binary string. Return the minimum total cost, or `-1` if no valid partition exists. The challenge is combining partition DP with segment costs that depend on both range sums and range maxima.

## 🌍 Engineering Impact
This pattern shows up in storage tiering, log compaction, checkpoint placement in streaming systems, and batch formation in data pipelines where each group has a nonlinear cost driven by both volume and worst-case quality or risk. Think Kafka segment rolls with validation boundaries, backup chunking with integrity markers, or warehouse file packing with skew-sensitive cost models. At scale, brute-force partitioning collapses under quadratic or cubic transition cost. A disciplined DP formulation lets you enforce operational boundaries while still optimizing total cost, which is exactly the trade-off production systems face when policy constraints meet throughput and storage economics.

## 🔍 Problem Statement
You are given:

- `size[i]`: size of batch `i`
- `risk[i]`: corruption risk of batch `i`
- `checkpoint`: binary string of length `n`
- `k`: exact number of contiguous archive files

A file covering indices `[l..r]` has cost:

`sum(size[l..r]) * max(risk[l..r])`

A valid partition must:

- use exactly `k` non-empty contiguous files
- cover all batches exactly once
- require each of the first `k - 1` files to end at an index `i` where `checkpoint[i] == '1'`
- always end the last file at `n - 1`, regardless of `checkpoint[n - 1]`

Return the minimum total cost, or `-1` if impossible.

Examples:

- `size=[4,2,7,3], risk=[5,1,4,2], checkpoint="1010", k=2` → `68`
- `size=[3,6,2,5,4], risk=[2,7,1,3,6], checkpoint="01010", k=3` → under the stated rules, only cuts at `1` and `3` are allowed, yielding cost `108`

The key constraint is `n <= 2000`, `k <= 50`: large enough that naive recomputation of segment cost inside DP transitions is too expensive.

## 🪜 How to Solve This
1. Read the problem → this is a partitioning problem over a fixed order, so think **partition DP**, not greedy.
2. The cost of a segment depends on two range properties: `sum(size)` and `max(risk)`. Range sums are easy with prefix sums; range maxima are not enough by themselves because the DP needs many candidate left boundaries.
3. Start with the obvious state: `dp[t][i]` = minimum cost to partition the first `i` batches into `t` files.
4. A transition picks the last file as `[j..i-1]`, so  
   `dp[t][i] = min(dp[t-1][j] + cost(j, i-1))`, with the extra rule that `j-1` must be a checkpoint when `t > 1`.
5. If you compute `cost(j, i-1)` from scratch for every pair, you get too much repeated work.
6. Instead, fix the right endpoint and scan leftward, maintaining:
   - running segment sum
   - running segment max risk
   That makes each segment cost update O(1) during the scan.
7. This yields an O(k·n²) DP with prefix sums and incremental maxima, which is acceptable at `n=2000`, `k<=50` in optimized implementations.

## 🧩 Algorithm Walkthrough
1. **Precompute prefix sums**  
   Build `pref[i+1] = sum(size[0..i])`. Then `sum(size[l..r]) = pref[r+1] - pref[l]`.  
   This removes one dimension of repeated work and guarantees O(1) range-sum queries.

2. **Define the DP state**  
   Let `dp[t][i]` be the minimum cost to partition the first `i` batches, meaning indices `[0..i-1]`, into exactly `t` valid files.  
   Invariant: `dp[t][i]` is finite only if those `t` files satisfy all checkpoint constraints for internal cuts.

3. **Initialize base cases**  
   `dp[0][0] = 0`; all other `dp[0][i] = INF`.  
   This encodes that zero files can cover zero batches and nothing else.

4. **Enumerate number of files**  
   For each `t` from `1` to `k`, compute the next DP row.  
   The last file must be non-empty, so `i >= t`.

5. **Enumerate right endpoint and scan leftward**  
   For each `i`, consider the last file ending at `i-1`. Scan `j` from `i-1` down to `t-1`, representing last segment `[j..i-1]`. Maintain:
   - `mx = max(risk[j..i-1])`
   - `segSum = pref[i] - pref[j]`
   Then `cost = segSum * mx`.

6. **Enforce checkpoint legality on the previous cut**  
   If `t == 1`, then `j` must be `0`.  
   If `t > 1`, the previous file ends at `j-1`, so require `checkpoint[j-1] == '1'`.  
   This is the only place the checkpoint rule enters, and it keeps the DP definition clean.

7. **Take the best transition**  
   Update `dp[t][i] = min(dp[t][i], dp[t-1][j] + cost)` whenever the cut is legal and `dp[t-1][j]` is finite.  
   Pattern: **Partition DP with incremental segment evaluation**. It is the right abstraction because the order is fixed, groups are contiguous, and each solution is composed of an optimal prefix plus one final segment.

## 📊 Worked Example
Use `size=[4,2,7,3]`, `risk=[5,1,4,2]`, `checkpoint="1010"`, `k=2`.

Prefix sums: `pref=[0,4,6,13,16]`

We compute `dp[1][i]` first:

| `i` | segment | sum | max risk | `dp[1][i]` |
|---|---|---:|---:|---:|
| 1 | `[0..0]` | 4 | 5 | 20 |
| 2 | `[0..1]` | 6 | 5 | 30 |
| 3 | `[0..2]` | 13 | 5 | 65 |
| 4 | `[0..3]` | 16 | 5 | 80 |

Now `t=2`, `i=4`:

- `j=3` → last file `[3..3]`, cost `3*2=6`; previous cut at `2`, `checkpoint[2]='1'`; total `dp[1][3]+6 = 65+6 = 71`
- `j=2` → last file `[2..3]`, cost `(7+3)*4=40`; previous cut at `1`, `checkpoint[1]='0'`; invalid
- `j=1` → last file `[1..3]`, cost `(2+7+3)*4=48`; previous cut at `0`, `checkpoint[0]='1'`; total `20+48=68`

So `dp[2][4]=68`, which is the answer.

## ⏱ Complexity Analysis
### Time Complexity
The DP has `k` layers, each considering `O(n)` right endpoints and `O(n)` possible left boundaries, for **O(k·n²)** time. Segment sums are O(1) via prefix sums, and segment maxima are updated incrementally during the backward scan. At `n=2000`, `k=50`, this is around 200 million transition checks: heavy but practical in compiled languages.

### Space Complexity
The straightforward table uses **O(k·n)** space. Since each layer depends only on the previous one, it can be reduced to **O(n)** with rolling arrays. The trade-off is slightly less debuggability and more care around overwrite order.

## 💡 Key Takeaways
- If the problem says “partition into exactly `k` contiguous groups” with additive objective over groups, start from partition DP immediately.
- If each group cost depends on range aggregates like sum, max, min, or bitwise properties, look for prefix structures plus incremental maintenance inside transitions.
- The checkpoint rule applies to the end of the previous file, so in transition `[j..i-1]`, validate `checkpoint[j-1]`, not `checkpoint[i-1]`.
- `dp[t][i]` over the first `i` elements is cleaner than `dp[t][r]` over ending indices and avoids many off-by-one errors in prefix sums.
- In production optimization problems, policy constraints usually do not change the core DP shape; they prune legal transitions. Model them there, not by distorting the state.

## 🚀 Variations & Further Practice
- Allow arbitrary penalty per checkpoint instead of a binary allowed/disallowed rule. The twist is that every transition remains legal, but the DP must absorb an additional cut cost.
- Replace `max(risk)` with `max(risk) - min(risk)` or another non-monotone range statistic. The harder part is that incremental maintenance may no longer be enough without extra data structures.
- Scale `n` to `10^5` and ask for subquadratic optimization. The conceptual jump is from basic partition DP to advanced optimizations such as monotone queues, divide-and-conquer DP, or structure-specific convexity arguments.