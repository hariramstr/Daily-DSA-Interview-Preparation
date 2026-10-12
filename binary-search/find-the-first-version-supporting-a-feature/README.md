# Find the First Version Supporting a Feature

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Search

---

## 🗂 Problem Overview
Given a sorted binary array `support`, return the index of the first `1`, or `-1` if no `1` exists. The input is monotonic: all `0`s come before all `1`s. That ordering is the only reason this is more than a trivial scan problem — it lets us replace `O(n)` inspection with `O(log n)` boundary search. The task is not to find any supporting version, but the earliest one.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the first point where a monotonic condition flips: the first bad deployment in a release train, the earliest LSM-tree segment containing a key range, the first timestamp where an SLO breach begins, or the first feature-flag rollout cohort that enables behavior. At small scale, linear scans are acceptable; at fleet or storage-engine scale, repeated scans become latency and cost multipliers. Binary search turns ordered state into a decision boundary query, enabling predictable lookup latency, cheaper control-plane checks, and cleaner APIs around versioned or time-ordered data.

## 🔍 Problem Statement
You are given a binary array `support` of length `n`, where each element is either `0` or `1`. The array is sorted in non-decreasing order, meaning all `0`s appear before any `1`s. Return the index of the first version that supports the feature, i.e. the first position containing `1`. If the array contains no `1`, return `-1`.

Constraints:

- `1 <= support.length <= 100000`
- `support[i]` is either `0` or `1`
- `support` is sorted in non-decreasing order

Examples:

- `support = [0,0,0,1,1,1]` → `3`
- `support = [0,0,0,0]` → `-1`

Edge cases matter: the first element may already be `1`, the array may contain only one element, or every element may be `0`. The sorted monotonic structure is the key constraint that makes binary search the intended approach.

## 🪜 How to Solve This
1. Read the array shape → it is sorted and binary, so the data has a single transition point: `0 ... 0 1 ... 1`.
2. The question is really “where does the predicate `support[i] == 1` become true for the first time?” → that is a boundary-finding problem, not a value-search problem.
3. Boundary problems on monotonic data strongly suggest binary search → each midpoint tells you which half can still contain the first `1`.
4. If `support[mid] == 1`, the answer could be `mid`, but there might be an earlier `1` → keep `mid` as a candidate and continue left.
5. If `support[mid] == 0`, then every index at or left of `mid` is definitely not the answer → move right.
6. Continue until the search interval is exhausted.
7. If you recorded a candidate index, return it; otherwise return `-1`.

The key mental shift is this: you are not searching for existence of `1`, you are searching for the leftmost index satisfying a monotonic predicate.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Binary Search on a Monotonic Predicate.**  
   Define the predicate as “this version supports the feature,” i.e. `support[i] == 1`. Because the array is sorted, once the predicate becomes true, it stays true. That monotonicity is exactly what binary search requires.

2. **Initialize the search interval.**  
   Set `left = 0`, `right = n - 1`, and `answer = -1`.  
   Invariant: if a valid first `1` exists, it is inside `[left, right]` or already stored in `answer`.

3. **Pick the midpoint safely.**  
   Compute `mid = left + (right - left) // 2`.  
   This avoids overflow in languages with fixed-width integers and is the standard production-safe midpoint formula.

4. **Handle the `1` case.**  
   If `support[mid] == 1`, record `answer = mid` and move `right = mid - 1`.  
   Why correct: `mid` is a valid supporting version, but not necessarily the first. Searching left preserves the possibility of an earlier `1`.

5. **Handle the `0` case.**  
   If `support[mid] == 0`, move `left = mid + 1`.  
   Why correct: sorted order guarantees no index `<= mid` can be the first `1`.

6. **Terminate when the interval collapses.**  
   When `left > right`, all candidates have been resolved. Return `answer`.  
   If no `1` was ever seen, `answer` remains `-1`, which correctly represents “no supporting version exists.”

This works because each step preserves the boundary-search invariant while halving the remaining search space.

## 📊 Worked Example
Example: `support = [0,0,0,1,1,1]`

| Step | left | right | mid | support[mid] | answer | Action |
|---|---:|---:|---:|---:|---:|---|
| 1 | 0 | 5 | 2 | 0 | -1 | First `1` must be right of `2` → `left = 3` |
| 2 | 3 | 5 | 4 | 1 | 4 | Valid candidate, but maybe earlier → `right = 3` |
| 3 | 3 | 3 | 3 | 1 | 3 | Better candidate → `right = 2` |

Loop ends because `left = 3` and `right = 2`.

Return `3`.

What changed across iterations:
- On `0`, we discarded the left half because it cannot contain the transition.
- On `1`, we kept searching left because the goal is the **first** supporting version, not just any supporting version.
- `answer` always held the best candidate seen so far.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration inspects one midpoint and discards half the remaining search interval. That makes the dominant operation the repeated halving, not element-by-element scanning. At `10^6` elements, this is about 20 checks; at `10^9`, about 30 — effectively constant-feeling latency compared with linear traversal.

### Space Complexity
`O(1)`. The algorithm uses a fixed number of variables: `left`, `right`, `mid`, and `answer`. No auxiliary data structures are allocated. Space cannot be meaningfully reduced further unless the API allows destructive mutation, which provides no benefit here.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the **first**, **last**, or **boundary** position where a condition becomes true, think binary search immediately.
- A monotonic predicate like “version supports feature” is often the real abstraction; once you see the predicate flips only once, the search strategy becomes obvious.
- When `support[mid] == 1`, do not stop; record the candidate and continue left, or you will return an arbitrary `1` instead of the first one.
- Be careful with loop bounds and midpoint updates: using `right = mid - 1` and `left = mid + 1` is what guarantees progress and avoids infinite loops.
- At system scale, ordered data is valuable not just for storage efficiency but because it enables boundary queries with predictable logarithmic latency.

## 🚀 Variations & Further Practice
- Find the **last** version supporting a feature instead of the first; same pattern, but the boundary moves in the opposite direction when you see `1`.
- Search in a sorted array with duplicates for the first occurrence of a target value; the twist is the predicate is derived from comparison to a target, not a binary state.
- First bad version / first failing build APIs; conceptually identical, but the array is implicit and each midpoint check may be an expensive remote call, making minimization of probes operationally important.