# Minimum Deletions to Form K Stable Value Bands

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Dynamic Programming, Sorting

---

## 🗂 Problem Overview
Given an array `nums` and an integer `k`, delete as few elements as possible so the remaining elements can be split into exactly `k` non-empty groups, where each group’s max and min differ by at most `1`. Group membership depends only on values, not original positions. The challenge is that `n` is up to `200000`, so explicit partition search is infeasible; the solution must exploit value structure after sorting and counting frequencies.

## 🌍 Engineering Impact
This pattern shows up whenever raw events must be compacted into a fixed number of tolerance bands while minimizing loss: telemetry bucketing, search-score banding, anomaly suppression, stream aggregation, and histogram compression in observability systems. At scale, brute-force grouping explodes because the combinatorial space is in the assignments, not the data volume. The right abstraction is to collapse positional data into value frequencies, then optimize over adjacent value bands. That shift enables predictable `O(n log n)` behavior, bounded memory, and implementations that remain stable under skewed distributions and high-cardinality inputs.

## 🔍 Problem Statement
You are given an integer array `nums` of length `n` and an integer `k`. You may delete any elements. The remaining elements must be partitioned into exactly `k` non-empty stable groups, where a group is stable if `max(group) - min(group) <= 1`.

A group may contain:
- repeated copies of a single value, or
- a mix of two adjacent values `x` and `x+1`.

It may not contain values differing by `2` or more. Since groups are defined by values rather than positions, sorting is allowed conceptually.

Return the minimum deletions needed, or `-1` if forming exactly `k` non-empty stable groups is impossible.

Examples:

- `nums = [1,1,2,2,3,5,5], k = 3` → `1`
- `nums = [4,4,4,7,8], k = 2` → `0`

The key constraint is `n <= 200000`, which rules out any approach that enumerates partitions or subsequences directly.

## 🪜 How to Solve This
1. Read the stability rule → a valid group can only use one value or two consecutive values. That immediately suggests sorting by value and working on frequencies, not original indices.

2. Notice what deletions really mean → we want to **keep** as many elements as possible while ending with exactly `k` groups. Minimum deletions is then `n - max_kept`.

3. Compress the array into sorted distinct values with counts. Now the problem becomes: from these value buckets, build exactly `k` stable groups maximizing total kept elements.

4. Observe the local structure:
   - if two distinct values differ by at least `2`, they can never be in the same group;
   - if they differ by `1`, they form a connected run where grouping choices interact.

5. Inside one consecutive-value run, each group corresponds to either:
   - taking some copies of one value alone, or
   - taking copies from two adjacent values together.
   Because groups can be split arbitrarily, the real question is how many groups a run can realize and how many elements can support that count.

6. This leads to dynamic programming over sorted values, tracking the best number of kept elements for using `g` groups up to each point. The transition only needs the current value and whether it is adjacent to the previous one.

## 🧩 Algorithm Walkthrough
1. **Sort and frequency-compress the array**  
   Build arrays `vals[]` and `cnt[]` for distinct values and their multiplicities. This removes positional noise and exposes the only structure that matters: adjacency in value space.

2. **Reframe the objective**  
   Let `keep[g]` denote the maximum number of elements we can retain while forming exactly `g` stable groups from processed values. Final answer is `n - keep[k]`, or `-1` if unreachable. This is a classic **Dynamic Programming on compressed states** pattern.

3. **Process values left to right**  
   For each distinct value `v = vals[i]` with count `c = cnt[i]`, there are two relevant actions:
   - start one or more groups using only value `v`;
   - if `vals[i] = vals[i-1] + 1`, merge usage of `v` with the previous value’s band, since `{v-1, v}` is still stable.

4. **Key invariant**  
   After processing prefix `0..i`, DP stores the maximum retained elements for every feasible group count, with no group containing values whose spread exceeds `1`. Because only adjacent values can interact, no older state beyond `i-1` is needed structurally.

5. **Transition logic**  
   A count bucket of size `c` can contribute to multiple groups because duplicates may be split across groups. If a value stands alone, it can support up to `c` singleton-value groups. If adjacent to the previous value, combined counts can support additional groups over the pair without violating stability. The transition effectively caps how many groups can be realized by available multiplicity.

6. **Why this is correct**  
   Sorting turns the global partition problem into independent local compatibility decisions. Any valid group uses either one value or one adjacent pair; therefore every optimal solution can be represented by DP transitions over neighboring buckets only. This preserves optimality while avoiding combinatorial assignment search.

## 📊 Worked Example
Take `nums = [1,1,2,2,3,5,5]`, `k = 3`.

Compressed form:

| value | count | adjacent to previous? |
|---|---:|---|
| 1 | 2 | no |
| 2 | 2 | yes |
| 3 | 1 | yes |
| 5 | 2 | no |

Trace:

1. Process `1` → can form `1` or `2` groups using only value `1`, keeping up to `2` elements.
2. Process `2` → since `2` is adjacent to `1`, groups may use `{1,2}` together. Best states now include:
   - `1` group keeping `4` via `[1,1,2,2]`
   - `2` groups keeping `4`
   - `3` groups keeping `4`
3. Process `3` → adjacent to `2`; can extend the run, but any group mixing `1` and `3` is illegal, so compatibility remains local.
4. Process `5` → gap from `3`, so this starts a new independent run. Two copies of `5` can supply one or two more groups.

Best for exactly `3` groups keeps `6` elements: `[1,1,2,2]`, `[5]`, `[5]`.  
Answer: `7 - 6 = 1`.

## ⏱ Complexity Analysis
### Time Complexity
Sorting dominates at `O(n log n)`. Frequency compression is linear, and the DP over distinct values and group counts is designed to avoid explicit partition enumeration. At `10^6` scale, `n log n` remains practical in optimized languages; anything quadratic is already non-viable. At `10^9`, this becomes a distributed or external-memory problem.

### Space Complexity
`O(m + k)` or `O(k)` beyond the sorted input, where `m` is the number of distinct values. The main cost is the compressed frequency arrays plus rolling DP state. Space can be reduced with in-place rolling transitions, at the cost of more careful update ordering.

## 💡 Key Takeaways
- If groups are constrained by value difference rather than array position, sort first and reason in value space.
- When the objective is minimum deletions, flip it to maximum retained elements; the DP usually becomes cleaner.
- The main trap is forgetting that duplicates can be split across multiple groups, so a count bucket may support several groups.
- Another easy bug is mishandling gaps larger than `1`; they break interaction completely and must reset adjacency logic.
- The transferable design insight is to collapse high-volume raw data into a compressed compatibility graph, then optimize on that smaller state space.

## 🚀 Variations & Further Practice
- Allow each group to have `max - min <= d` for arbitrary `d`; the twist is that compatibility is now a sliding window, not just adjacent values.
- Require exactly `k` groups **and** a minimum size per group; now multiplicity allocation becomes capacity-constrained DP.
- Add per-element deletion costs instead of unit deletions; the problem shifts from maximizing count kept to maximizing retained weight under the same banding constraints.