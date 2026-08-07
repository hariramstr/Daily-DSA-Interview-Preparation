# Minimum XOR Merges to Isolate a Signature

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Prefix XOR, Hash Map

---

## 🗂 Problem Overview
Given an array `nums` and a target value `x`, each merge operation replaces an adjacent pair with their XOR, shrinking the array by one. After any number of merges, every remaining element represents the XOR of some contiguous subarray of the original array. The goal is to return the minimum merges needed so that some remaining segment equals `x`, or `-1` if impossible. The challenge is `n <= 200000`, which rules out quadratic subarray search.

## 🌍 Engineering Impact
This pattern shows up anywhere local reductions preserve order: streaming telemetry compaction, packet-signature aggregation, log-segment folding, and incremental compiler or query-plan summarization. At scale, the wrong approach degenerates into repeated rescans of long histories or state snapshots, which is exactly how latency spikes and memory churn appear in production pipelines. Prefix-XOR plus hash indexing turns a structurally mutating problem into a single linear pass over immutable history. That shift matters architecturally: you stop simulating transformations and instead query reduction properties directly, which is the difference between something that works on thousands of events and something that survives hundreds of millions.

## 🔍 Problem Statement
You are given `nums`, an array of `n` non-negative integers, and a target `x`. In one operation, you may choose adjacent elements `nums[i]` and `nums[i+1]`, remove them, and insert `nums[i] XOR nums[i+1]` in their place. Each operation reduces the array length by `1`.

You may stop at any time. Return the minimum number of merges required so that some value in the current array is exactly `x`. If no sequence of valid adjacent merges can produce such a segment, return `-1`.

Key equivalence: after any sequence of merges, each remaining value is the XOR of a contiguous subarray of the original array. So the problem becomes finding the shortest contiguous subarray with XOR `x`; a subarray of length `L` needs exactly `L - 1` merges.

Examples:

- `nums = [5,1,4,1], x = 4` → `1`
- `nums = [2,7,2,7], x = 0` → `3`

The decisive constraint is `n <= 200000`, so `O(n^2)` subarray enumeration is not acceptable.

## 🪜 How to Solve This
1. Read the merge rule → notice adjacency is preserved, so merges never reorder data. That means every final element must correspond to some contiguous original segment.

2. Translate operations into cost → if a segment has length `L`, collapsing it into one value takes exactly `L - 1` merges. So minimizing merges means minimizing the length of a contiguous subarray whose XOR is `x`.

3. Reframe subarray XOR → for prefix XOR `px[i] = nums[0] ^ ... ^ nums[i]`, the XOR of subarray `(l..r)` is:
   `px[r] ^ px[l-1]`.
   We want that to equal `x`.

4. Rearrange the condition → for each position `r`, we need an earlier prefix value:
   `px[l-1] = px[r] ^ x`.

5. Optimize the search → this is a hash-map lookup problem. As we scan left to right, store the latest index where each prefix XOR occurred. Latest matters because for fixed `r`, it gives the shortest valid subarray ending at `r`.

6. Track the best length seen → if no valid subarray exists, return `-1`; otherwise return `bestLength - 1`.

Once you see “shortest contiguous subarray with XOR target,” the prefix-XOR + hash-map pattern is the obvious fit.

## 🧩 Algorithm Walkthrough
1. **Use the Prefix XOR pattern.**  
   Maintain a running XOR `pref` while scanning the array. After processing index `i`, `pref` equals the XOR of `nums[0..i]`. This gives constant-time subarray XOR checks via prefix cancellation.

2. **Seed the map with the empty prefix.**  
   Initialize a hash map with `{0: -1}`. This represents the prefix before the array starts. It is required to detect subarrays beginning at index `0`, since if `pref == x` at index `i`, then the segment `0..i` is valid.

3. **At each index, compute the needed prior prefix.**  
   If the current prefix is `pref`, then a subarray ending at `i` has XOR `x` exactly when some earlier prefix equals `pref ^ x`. This is the core invariant:
   `prefix[j] ^ prefix[i] = x` rearranges to `prefix[j] = pref ^ x`.

4. **Minimize length by storing the latest index per prefix XOR.**  
   When `pref ^ x` exists in the map at index `j`, the candidate subarray length is `i - j`. For shortest length, we want the largest possible `j`, so after processing each position, overwrite `map[pref] = i`. This is a subtle but important choice: unlike longest-subarray problems, shortest-subarray XOR wants the most recent occurrence.

5. **Track the minimum valid length.**  
   Maintain `best = +inf`. Every successful lookup updates `best = min(best, i - j)`. The invariant is that after index `i`, `best` is the shortest valid subarray found among all suffixes ending at or before `i`.

6. **Convert segment length to merge count.**  
   If `best` was never updated, return `-1`. Otherwise return `best - 1`, because collapsing a length-`L` contiguous segment into one value requires exactly `L - 1` adjacent merges.

This is the right abstraction because the array mutations are a distraction; the algebraic property of XOR over contiguous ranges is what actually determines feasibility and minimum cost.

## 📊 Worked Example
Consider `nums = [5, 1, 4, 1]`, `x = 4`.

| i | nums[i] | pref | needed = pref ^ x | map hit? | candidate length | best |
|---|---------|------|-------------------|----------|------------------|------|
| -1 | - | 0 | - | seed `map[0] = -1` | - | inf |
| 0 | 5 | 5 | 1 | no | - | inf |
| 1 | 1 | 4 | 0 | yes, at `-1` | `2` | 2 |
| 2 | 4 | 0 | 4 | yes, at `1` | `1` | 1 |
| 3 | 1 | 1 | 5 | yes, at `0` | `3` | 1 |

Trace:
1. At `i = 1`, prefix XOR is `4`, so subarray `[5,1]` has XOR `4`, length `2`.
2. At `i = 2`, we need prior prefix `4`, found at index `1`; subarray `[4]` has XOR `4`, length `1`.
3. Minimum valid segment length is `1`, so required merges are `1 - 1 = 0`.

If the problem example insists on performing one merge first, that is a valid construction, but not minimal. The true minimum is `0` because `nums[2]` already equals `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each element is processed once, each step does constant work: one running XOR update, one hash lookup, and one hash write. At `10^6` elements this is routine in memory-resident systems; at `10^9`, linear time is still the lower-bound shape, but memory bandwidth and distributed partitioning dominate.

### Space Complexity
`O(n)` in the worst case for the hash map of distinct prefix XOR values. That structure owns essentially all auxiliary space. It cannot be reduced to `O(1)` without giving up constant-time prefix lookups, which would typically force slower search or offline processing trade-offs.

## 💡 Key Takeaways
- If adjacent merges preserve order and combine values associatively, suspect that the final state corresponds to contiguous segments of the original array.
- If the target condition is “subarray XOR equals `x`,” prefix XOR plus a hash map is the default pattern to test before considering anything more complex.
- For **shortest** valid XOR subarray, store the **latest** index of each prefix XOR; storing the earliest gives the wrong optimization objective.
- Seed the map with prefix XOR `0` at index `-1`, or you will miss valid segments that start at the first element.
- In production systems, the scalable move is often to stop simulating structural mutations and instead query an invariant over the original sequence.

## 🚀 Variations & Further Practice
- Return the actual merge plan, not just the minimum count. The conceptual twist is reconstructing segment boundaries while preserving the same linear-time detection.
- Find the minimum merges to create **at least `k` segments** whose XOR equals `x`. This adds global coordination across multiple non-overlapping subarrays.
- Support online updates to `nums` with repeated queries for different `x`. The harder part is that prefix XOR indexing becomes unstable under mutation, pushing you toward segment trees or offline batching.