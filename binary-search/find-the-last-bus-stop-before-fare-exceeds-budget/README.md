# Find the Last Bus Stop Before Fare Exceeds Budget

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Sorted Array

---

## 🗂 Problem Overview
Given a non-decreasing array `fares`, where `fares[i]` is the total fare to reach stop `i`, return the largest index whose fare does not exceed `budget`. If no stop is affordable, return `-1`. The challenge is not scanning for any valid stop, but finding the **rightmost** valid one, including across duplicate fare values. The sorted input is the key constraint: it turns a linear search problem into a binary-search boundary problem.

## 🌍 Engineering Impact
This pattern appears anywhere systems need the last admissible value under a threshold in ordered data. Examples include distributed rate-limiters finding the highest allowed quota tier, search/ranking systems locating the last document below a score cutoff, storage engines resolving upper bounds in SSTables or B-trees, and compiler/linker symbol tables doing predecessor lookups. At scale, replacing linear scans with logarithmic boundary search reduces tail latency and avoids cache-unfriendly full passes. More importantly, it enables predictable performance under growth, which matters when these lookups sit on hot paths or inside higher-level query plans.

## 🔍 Problem Statement
You are given an integer array `fares` of length `1 <= n <= 100000`, sorted in non-decreasing order, where `0 <= fares[i] <= 10^9`. Each value represents the total fare from the starting terminal to stop `i`. You are also given `budget`, where `0 <= budget <= 10^9`.

Return the largest index `i` such that `fares[i] <= budget`. If `fares[0] > budget`, return `-1`.

The important edge case is duplicates: if multiple stops have the same fare and that fare is within budget, the answer is the **last** such index, not the first.

Examples:

- `fares = [2, 4, 4, 7, 9], budget = 4` → `2`
- `fares = [5, 8, 12], budget = 3` → `-1`

The sorted-array constraint drives the algorithmic choice: this is a right-boundary binary search, not a linear scan.

## 🪜 How to Solve This
1. Read the problem → we need the **largest index** satisfying `fares[i] <= budget`, not merely whether such an index exists.

2. Notice the array is sorted in non-decreasing order → that means the predicate  
   `fares[i] <= budget` is monotonic: once it becomes false, it stays false for all larger indices.

3. Monotonic predicate on a sorted array → think **binary search for a boundary**.

4. Which boundary? Not the first affordable stop, but the **rightmost affordable** one. That means when `fares[mid] <= budget`, `mid` is a valid candidate, but there may be a later valid stop, so move right.

5. When `fares[mid] > budget`, `mid` and everything to its right are invalid, so move left.

6. Keep track of the best valid index seen so far. If none is found, return `-1`.

7. Duplicates do not require special casing if the search is framed correctly: “valid → record answer and continue right” naturally lands on the last duplicate within budget.

This is the standard mental model for “find the last true value” in a sorted domain.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer Boundary.**  
   The array is sorted, and the condition `fares[i] <= budget` is monotonic across indices. This is exactly the “find the rightmost true” variant of binary search.

2. **Initialize search bounds.**  
   Set `left = 0`, `right = n - 1`, and `answer = -1`.  
   Invariant: any index `< left` has already been processed or ruled into the candidate region; any index `> right` has been ruled out or deferred by prior decisions.

3. **Compute the midpoint safely.**  
   Use `mid = left + (right - left) // 2`.  
   This avoids overflow in languages with fixed-width integers and is the standard production-safe midpoint formula.

4. **Evaluate the midpoint against the budget.**  
   If `fares[mid] <= budget`, then `mid` is a valid stop. Record `answer = mid`.  
   Why correct: `mid` satisfies the requirement, but because we need the **largest** such index, we must continue searching the right half for a later valid stop.

5. **Shrink the search space based on monotonicity.**  
   - If `fares[mid] <= budget`, set `left = mid + 1`.  
   - Otherwise, set `right = mid - 1`.  
   Invariant: `answer` is always the largest valid index seen so far.

6. **Terminate when bounds cross.**  
   When `left > right`, no unexplored indices remain. `answer` is either the rightmost affordable stop or `-1` if none were valid.

7. **Why this abstraction fits.**  
   Binary search is the right tool because the problem is not about values themselves, but about locating a transition point in a monotonic boolean space: affordable → unaffordable.

## 📊 Worked Example
Example: `fares = [2, 4, 4, 7, 9]`, `budget = 4`

| Step | left | right | mid | fares[mid] | Condition        | answer |
|------|------|-------|-----|------------|------------------|--------|
| 1    | 0    | 4     | 2   | 4          | `4 <= 4` true    | 2      |
| 2    | 3    | 4     | 3   | 7          | `7 <= 4` false   | 2      |
| 3    | 3    | 2     | —   | —          | stop             | 2      |

Trace:
1. Start with the full range.
2. At `mid = 2`, fare `4` is within budget, so index `2` is valid. Record it, then move right to check whether a later stop is also affordable.
3. At `mid = 3`, fare `7` exceeds budget, so everything from `3` onward is invalid.
4. Search ends with `answer = 2`, which is the last stop whose fare does not exceed the budget.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, so the dominant operation is the binary-search loop. At `10^6` elements this is about 20 comparisons; at `10^9`, about 30. That bounded growth is why boundary search remains viable on hot paths where linear scans do not.

### Space Complexity
`O(1)`. The algorithm uses a fixed number of scalar variables: pointers and one result slot. No auxiliary data structures are required. Space cannot be meaningfully reduced further without changing the execution model; recursion would only make it worse by adding call-stack overhead.

## 💡 Key Takeaways
- If the input is sorted and the condition can be phrased as “true up to some point, false after,” you are looking at a boundary-search problem.
- Phrases like “largest index,” “rightmost value,” or “last occurrence within threshold” are strong signals for binary search on a monotonic predicate.
- When `fares[mid] <= budget`, do not stop; record `mid` and continue right, or you will miss later duplicates that are also valid.
- Initialize the result to `-1`; otherwise the unaffordable-all-stops case becomes an off-by-one bug disguised as a default index.
- In production systems, this pattern matters because ordered data plus logarithmic predecessor lookup is a foundational building block for predictable latency.

## 🚀 Variations & Further Practice
- Return the **first** stop whose fare exceeds the budget instead of the last affordable stop; same data, opposite boundary, different pointer movement.
- Given many budget queries against the same `fares` array, optimize for repeated lookups; the twist is shifting from one-off search to query-serving strategy and API design.
- Extend to a 2D setting such as finding the last timestamped event before a cutoff across partitioned shards; the harder part is composing local binary searches into a global predecessor result.