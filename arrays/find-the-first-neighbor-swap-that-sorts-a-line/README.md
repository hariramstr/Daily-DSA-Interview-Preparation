# Find the First Neighbor Swap That Sorts a Line

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** Arrays, Simulation, Greedy

---

## 🗂 Problem Overview
Given an integer array `nums`, return the smallest index `i` such that swapping `nums[i]` and `nums[i + 1]` makes the entire array sorted in non-decreasing order. You may perform at most one adjacent swap. If the array is already sorted, return `-1`; if no single neighboring swap can sort it, also return `-1`. The non-trivial part is avoiding brute-force trial of every swap on arrays up to `100000` elements.

## 🌍 Engineering Impact
This pattern shows up anywhere a mostly ordered sequence must be validated or repaired with minimal local change: streaming pipelines correcting one out-of-order event, search ranking systems detecting whether a single adjacent inversion explains a bad result, compiler passes validating nearly sorted symbol or instruction lists, and warehouse or scheduling systems reconciling one misplaced priority item. At scale, brute-force “try every mutation and revalidate globally” burns latency and cache efficiency. Recognizing that disorder is local enables linear-time validation, predictable tail latency, and simpler correctness reasoning in hot paths.

## 🔍 Problem Statement
You are given an integer array `nums` with `1 <= nums.length <= 100000` and values in `[-10^9, 10^9]`. You may choose at most one index `i` and swap `nums[i]` with `nums[i + 1]`. The goal is to find the smallest such `i` for which the resulting array is sorted in non-decreasing order. Return the left index of that swap. If `nums` is already sorted, return `-1`. If no single adjacent swap can sort the array, return `-1`.

Examples:

- `nums = [1, 3, 2, 4]` → `1`
- `nums = [1, 5, 3, 4, 2]` → `-1`
- `nums = [1, 2, 2, 3]` → `-1`

The key constraint is array size: `O(n^2)` retry-and-check approaches are unnecessary and unsafe under the upper bound.

## 🪜 How to Solve This
1. Scan the array once → look for the first inversion, where `nums[i] > nums[i + 1]`. If none exists, the array is already sorted, so return `-1`.
2. Once you see the first inversion, the only plausible fixing swap is exactly that adjacent pair. Why? A sorted array can only become valid with one adjacent swap if the disorder is localized to one neighboring inversion.
3. Conceptually swap `nums[i]` and `nums[i + 1]` → now ask a narrower question: did that local repair also preserve ordering at the boundaries?
4. You do not need to re-check the whole array from scratch if you reason carefully, but a linear validation after one candidate swap is still `O(n)` and simple.
5. If the swapped array is sorted, return `i`. If not, return `-1`.
6. The “smallest valid index” requirement is automatically satisfied because the first inversion is the earliest place where order breaks; no earlier index can be the first successful repair if the prefix was already sorted.

## 🧩 Algorithm Walkthrough
1. **Pattern: Greedy + Array Scan.**  
   Traverse from left to right and find the first index `i` such that `nums[i] > nums[i + 1]`. This is the first proof that the array is not sorted. The invariant before this point is: `nums[0..i]` is non-decreasing.

2. **Handle the already-sorted case.**  
   If no such `i` exists, return `-1`. This is required by the problem contract and avoids unnecessary mutation or validation.

3. **Try the only meaningful local repair.**  
   Swap `nums[i]` and `nums[i + 1]`. Because only one adjacent swap is allowed, any successful solution must resolve the earliest inversion. Swapping any later pair leaves this inversion untouched, so the array cannot become sorted.

4. **Validate the result.**  
   Scan the array once more and confirm `nums[j] <= nums[j + 1]` for all valid `j`. The invariant during this pass is straightforward: every checked prefix remains sorted. If any inversion remains, one adjacent swap was insufficient.

5. **Return the answer.**  
   If validation succeeds, return `i`; otherwise return `-1`.

This abstraction is right because the problem is not “search over all swaps”; it is “identify whether a single local inversion explains the global disorder.” That is exactly a greedy local-repair check on a nearly sorted array.

## 📊 Worked Example
Example: `nums = [1, 3, 2, 4]`

| Step | Index / Action | Array State     | Observation |
|------|----------------|-----------------|-------------|
| 1 | Scan `i = 0` | `[1, 3, 2, 4]` | `1 <= 3`, still sorted so far |
| 2 | Scan `i = 1` | `[1, 3, 2, 4]` | `3 > 2`, first inversion found |
| 3 | Swap `1` and `2` | `[1, 2, 3, 4]` | Candidate repair applied |
| 4 | Validate full array | `[1, 2, 3, 4]` | Every adjacent pair is ordered |
| 5 | Return | `1` | Smallest valid swap index |

Why this works: the prefix before index `1` was already sorted, and the first inversion is the earliest possible place where order can be repaired. After swapping that pair, no other inversion remains, so the whole array is sorted.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. One pass finds the first inversion, and one pass validates after the candidate swap. The dominant operation is linear scanning over the array. At `10^6` elements this is routine in memory-resident workloads; at `10^9`, even linear passes become bandwidth-bound, so avoiding quadratic retry logic is the difference between feasible and impossible.

### Space Complexity
`O(1)`. The algorithm uses a few indices and optionally performs the swap in place. No auxiliary data structure grows with input size. You could avoid mutating the array by simulating comparisons, but that adds branching complexity without reducing asymptotic space.

## 💡 Key Takeaways
- If a problem allows only one adjacent swap and asks whether the whole array becomes sorted, look first for the earliest inversion rather than enumerating all swaps.
- “Nearly sorted array” is a strong signal for a greedy local-repair scan instead of sort-based or quadratic simulation approaches.
- Be careful with the already-sorted case: the correct answer is `-1`, not `0`, because no swap is needed and the problem asks for an actual operation.
- Off-by-one errors cluster around the swap boundary; always ensure `i + 1` is valid and re-check ordering after the swap, especially with duplicates.
- In production systems, constraining repair logic to the minimal local inconsistency is often what keeps validation paths linear, cache-friendly, and operationally predictable.

## 🚀 Variations & Further Practice
- Allow one swap of **any two indices**, not just neighbors. The harder part is distinguishing a single local inversion from a broader mismatch between the array and its sorted order.
- Return whether the array can be sorted with **at most one adjacent swap or one adjacent reversal of length 2–k**. The twist is identifying a contiguous disorder segment instead of one pair.
- Process a stream of updates and answer after each mutation whether the array is sortable by one adjacent swap. The challenge shifts from one-shot scanning to maintaining local inversion state incrementally.