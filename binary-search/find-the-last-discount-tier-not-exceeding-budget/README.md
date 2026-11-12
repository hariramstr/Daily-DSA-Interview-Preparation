# Find the Last Discount Tier Not Exceeding Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Sorted Array

---

## 🗂 Problem Overview
Given a strictly increasing array `tiers` and a target `budgetQty`, return the index of the rightmost threshold that does not exceed the target. Formally, find the greatest `i` such that `tiers[i] <= budgetQty`, or `-1` if no such index exists. The problem is trivial with a linear scan, but the sorted input and large upper bound on array size make the intended solution a boundary-finding binary search in `O(log n)` time.

## 🌍 Engineering Impact
This pattern shows up anywhere a system maps a numeric input to the most recent valid threshold: pricing tiers, rate-limit buckets, feature rollout percentages, compiler lookup tables, storage class selection, and time-series retention policies. At scale, replacing linear scans with boundary-search over sorted metadata removes avoidable latency from hot paths and keeps lookup cost stable as configuration grows. In distributed systems, these searches often sit behind every request. The difference between `O(n)` and `O(log n)` becomes operationally meaningful when tier tables, shard maps, or policy lists are queried millions of times per second.

## 🔍 Problem Statement
You are given a strictly increasing integer array `tiers`, where `tiers[i]` is the quantity at which a discount tier begins, and an integer `budgetQty`, the maximum quantity a customer can buy. Return the index of the last tier whose threshold is less than or equal to `budgetQty`.

If all thresholds are greater than `budgetQty`, return `-1`. If `budgetQty` is at least the largest threshold, return the last index.

**Constraints**
- `1 <= tiers.length <= 100000`
- `1 <= tiers[i] <= 1000000000`
- `tiers` is strictly increasing
- `1 <= budgetQty <= 1000000000`

**Examples**
- `tiers = [5, 10, 20, 50], budgetQty = 23` → `2`
- `tiers = [4, 8, 15], budgetQty = 2` → `-1`

The decisive constraint is that `tiers` is already sorted, which makes binary search the correct asymptotic choice over a full scan.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find whether the target exists.” It is “find the last value `<= target`.” That signals a boundary search, not a standard exact-match lookup.

2. Notice the array is strictly increasing → once `tiers[mid] <= budgetQty`, every element to the left is also valid, but there may still be a better answer to the right.

3. Reframe the problem → we want the rightmost valid index. That means:
   - if `tiers[mid] <= budgetQty`, record `mid` as a candidate and move right
   - if `tiers[mid] > budgetQty`, move left

4. Maintain a running answer initialized to `-1` → this cleanly handles the case where no threshold qualifies.

5. Continue until the search space is exhausted → by shrinking `[left, right]` each iteration, we converge in logarithmic time.

6. The key mental model: binary search is not just for equality. It is a general tool for finding transition points in monotonic data, and this problem is exactly “valid” followed by “invalid.”

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: Binary Search on a Monotonic Predicate.**  
   Define the predicate `tiers[i] <= budgetQty`. Because `tiers` is strictly increasing, this predicate is monotonic across indices: it is `true` for some prefix, then `false` for the remaining suffix. We need the last `true`.

2. **Initialize search bounds.**  
   Set `left = 0`, `right = tiers.length - 1`, and `answer = -1`.  
   `answer` stores the best valid index seen so far. Initializing it to `-1` preserves correctness when no tier qualifies.

3. **Compute the midpoint safely.**  
   Use `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` could exceed integer range, and it is the standard production-safe formulation.

4. **Evaluate the midpoint.**  
   If `tiers[mid] <= budgetQty`, then `mid` is a valid candidate. Store `answer = mid` and search the right half with `left = mid + 1`.  
   Why: a later valid tier may exist, and we want the rightmost one.

5. **Otherwise, discard the right side.**  
   If `tiers[mid] > budgetQty`, then `mid` and everything to its right are invalid, so set `right = mid - 1`.

6. **Maintain the invariant.**  
   At every step:
   - all indices `<= answer` are known to include at least one valid candidate at `answer`
   - the remaining search interval may still contain a larger valid index
   - discarded right-side elements after an invalid midpoint can never become valid

7. **Terminate and return.**  
   When `left > right`, the search space is empty. `answer` is the largest index satisfying `tiers[i] <= budgetQty`, or `-1` if none exists.

## 📊 Worked Example
Example: `tiers = [5, 10, 20, 50]`, `budgetQty = 23`

| Step | left | right | mid | tiers[mid] | Condition | answer | Next move |
|------|------|-------|-----|------------|-----------|--------|-----------|
| 1 | 0 | 3 | 1 | 10 | `10 <= 23` | 1 | move right: `left = 2` |
| 2 | 2 | 3 | 2 | 20 | `20 <= 23` | 2 | move right: `left = 3` |
| 3 | 3 | 3 | 3 | 50 | `50 > 23` | 2 | move left: `right = 2` |

Now `left = 3` and `right = 2`, so the loop stops.

Result: `answer = 2`.

This trace shows the core idea: every valid midpoint becomes a provisional answer, but the algorithm keeps searching right to see whether a later valid threshold exists. The first invalid midpoint on the right closes the search and confirms index `2` as the last qualifying tier.

## ⏱ Complexity Analysis
### Time Complexity
Binary search examines at most `log2(n)` midpoints, so the runtime is `O(log n)`. The dominant operation is the repeated midpoint comparison against `budgetQty`. At `10^6` elements, that is about 20 comparisons; at `10^9`, about 30. This is why sorted lookup tables remain viable even as configuration size grows dramatically.

### Space Complexity
The algorithm uses `O(1)` extra space. It stores only a few scalar variables: `left`, `right`, `mid`, and `answer`. There is no auxiliary data structure. Space cannot be meaningfully reduced further without changing the execution model, since the input array itself is read-only and already provided.

## 💡 Key Takeaways
- If the array is sorted and the question asks for the **first/last index satisfying a condition**, think boundary-oriented binary search, not exact-match binary search.
- Phrases like “greatest index such that `arr[i] <= x`” or “rightmost valid value” are strong signals that the problem is about a monotonic predicate over indices.
- The most common bug is returning immediately on `tiers[mid] <= budgetQty`; that finds a valid index, not necessarily the last valid one.
- Initializing `answer = -1` is not optional; it is what makes the “no qualifying tier” case correct without special-case branching.
- In production systems, this pattern is the core of efficient threshold resolution: pre-sort immutable policy data once, then answer hot-path queries in logarithmic time.

## 🚀 Variations & Further Practice
- Find the **first** discount tier meeting a minimum purchase quantity (`tiers[i] >= target`) instead of the last tier not exceeding a budget; same pattern, opposite boundary.
- Search in a sorted array with **duplicate thresholds** and return the last occurrence `<= budgetQty`; the conceptual twist is separating value comparison from duplicate-boundary handling.
- Support **online updates** to tiers while preserving fast lookups; the harder part is choosing a structure such as a balanced tree or indexed skip list instead of relying on static-array binary search.