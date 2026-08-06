# Maximum XOR Gap After One Removal

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Trie, Greedy

---

## 🗂 Problem Overview
Given an array of non-negative integers, remove exactly one element and compute the maximum possible XOR of any two distinct values among the remaining elements. Return the best such XOR over all removal choices. If fewer than two numbers remain after removal, the result is `0`. The challenge is that `n` can reach `10^5`, so recomputing the maximum pairwise XOR after every deletion is too expensive; the solution must exploit bit structure rather than pair enumeration.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need fast “best-difference” queries under churn: network telemetry deduplication, approximate nearest-neighbor candidate generation in binary spaces, compiler or database indexing over integer keys, and streaming fraud/risk pipelines comparing signatures. At scale, the naive approach collapses because every update invalidates a global optimum and forces broad recomputation. Bitwise trie-based reasoning turns a quadratic search into prefix-guided decisions, which is the difference between an online service sustaining high-cardinality streams and one that stalls under reindexing or repeated full scans.

## 🔍 Problem Statement
You are given `nums`, where `1 <= nums.length <= 10^5` and `0 <= nums[i] <= 10^9`. For each index `i`, remove `nums[i]`, then compute the XOR gap of the remaining set: the maximum value of `a XOR b` over all distinct remaining positions. Return the largest XOR gap achievable across all valid removals.

If removing one element leaves fewer than two numbers, the XOR gap is defined as `0`. Values may repeat, so distinctness is by position, not value.

Examples:

- `nums = [3, 10, 5, 25]` → `28`  
  Remove `10`, remaining `[3, 5, 25]`, best pair is `5 XOR 25 = 28`.

- `nums = [8, 1, 2]` → `10`  
  Remove `1`, remaining `[8, 2]`, best pair is `8 XOR 2 = 10`.

The key constraint is `10^5` elements: recomputing a full maximum-XOR structure for every deletion is not viable.

## 🪜 How to Solve This
1. Read the problem → the expensive part is “remove one element, then recompute best pair” for every index. That smells like avoiding repeated global recomputation.
2. Ask what maximum XOR depends on → binary prefixes. Two numbers produce large XOR when they diverge as early as possible in their high bits.
3. That points directly to a bitwise trie → the standard structure for maximum pairwise XOR because it turns “find the most different partner” into greedy bit choices.
4. But rebuilding the trie `n` times is still too slow. So shift perspective: removing one element only matters if that element is essential to every optimal pair.
5. If the global maximum XOR is achieved by some pair `(a, b)`, then removing any unrelated element keeps that pair alive. Therefore, whenever `n >= 3`, the answer is just the maximum XOR over the full array.
6. The problem collapses to: compute the maximum pairwise XOR once; only handle tiny arrays carefully.
7. Use a binary trie or equivalent greedy prefix method to find that maximum in `O(n * B)`, where `B` is the bit width.

## 🧩 Algorithm Walkthrough
1. **Handle degenerate sizes first.**  
   If `nums.length <= 2`, removing exactly one element leaves at most one number, so no valid pair remains. Return `0`.  
   **Invariant:** from this point onward, at least one removal leaves two or more values.

2. **Recognize the reduction.**  
   Let `M` be the maximum XOR of any pair in the full array. For any pair `(x, y)` achieving `M`, if `n >= 3`, there exists some index different from the positions of `x` and `y` to remove. After that removal, `(x, y)` still exists, so the remaining-array XOR gap is still at least `M`. It cannot exceed `M`, since every remaining pair came from the original array.  
   **Invariant:** for `n >= 3`, the answer equals the full-array maximum pairwise XOR.

3. **Compute full-array maximum XOR using a Bitwise Trie.**  
   Insert numbers bit by bit from the most significant relevant bit down to `0`. For each number, query the trie greedily: at each bit, prefer the opposite bit if present, because setting a higher XOR bit dominates all lower-bit choices.  
   **Pattern:** **Bitwise Trie + Greedy bit selection**. This is the right abstraction because XOR optimization is lexicographic over bits.

4. **Update the global best while scanning.**  
   Either insert all numbers then query each, or more commonly query against previously inserted numbers and then insert the current number. This avoids pairing an element with itself while still considering every unordered pair exactly once.  
   **Invariant:** after processing index `i`, the stored best is the maximum XOR among all pairs drawn from `nums[0..i]`.

5. **Return the best value.**  
   This is the maximum possible XOR gap after one removal for all arrays of size at least `3`, and `0` otherwise.

## 📊 Worked Example
Take `nums = [3, 10, 5, 25]`.

Since `n = 4`, removing one element still leaves at least two numbers. So the answer is the maximum pairwise XOR in the full array.

| Step | Current թիվ | Best partner found in trie | XOR | Global best |
|---|---:|---:|---:|---:|
| 1 | 3  | —  | —  | 0 |
| 2 | 10 | 3  | 9  | 9 |
| 3 | 5  | 10 | 15 | 15 |
| 4 | 25 | 5  | 28 | 28 |

Trace:
1. Insert `3`.
2. Query `10` against `{3}` → `10 XOR 3 = 9`.
3. Query `5` against `{3,10}` → best is `10`, giving `15`.
4. Query `25` against `{3,10,5}` → best is `5`, giving `28`.

Now use the reduction: remove `10`, keep pair `(5, 25)`, and the remaining XOR gap is still `28`. No removal can produce more than the full-array maximum, so the answer is `28`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * B)`, where `B` is the number of processed bits, at most about `31` for values up to `10^9`. Each number performs one trie query and one insertion. In practice this is linear in input size and remains feasible at `10^6`; at `10^9` elements, memory and I/O dominate long before the bit operations do.

### Space Complexity
`O(n * B)` in the worst case for the trie, though shared prefixes usually reduce constants. The trie owns essentially all auxiliary space. You can trade trie memory for a prefix-set greedy method, but not without changing implementation complexity and constant factors.

## 💡 Key Takeaways
- If the prompt says “maximize XOR” and constraints rule out pairwise comparison, think binary prefixes immediately; sorting by value is usually the wrong abstraction.
- If the operation is “remove one item, optimize over the rest,” first ask whether the global optimum survives most removals; many problems reduce before any heavy data structure is needed.
- The critical edge case is `n <= 2`: removing exactly one element leaves fewer than two values, so the answer is `0`, not the XOR of what remains.
- When querying a trie online, insert after querying the current number if you want to avoid accidentally pairing an element with itself.
- The transferable design lesson is to separate structural recomputation from sensitivity analysis: often the expensive “after each deletion” view collapses once you identify what actually invalidates an optimum.

## 🚀 Variations & Further Practice
- **Return the best removal index as well as the XOR gap.** The twist is proving when multiple removals preserve the same optimal pair and handling ties deterministically.
- **Support online insertions and deletions with max-XOR queries.** This becomes a dynamic data-structure problem; reference counts in trie nodes or persistent tries are needed.
- **Maximize XOR gap after removing up to `k` elements.** The hard part is that the simple “global optimum survives” argument no longer holds once both endpoints of the best pair may be removable.