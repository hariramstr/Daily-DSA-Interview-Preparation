# Minimum Patch Version to Support All Client Features

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Lower Bound

---

## 🗂 Problem Overview
Given a non-decreasing array `patches`, answer each query `target` with the smallest 1-indexed patch version whose compatibility score is at least `target`. If no such version exists, return `-1`. The challenge is scale: both the number of patch versions and queries can reach `200,000`, so scanning linearly per query is too expensive. The key property is monotonicity, which makes leftmost binary search the correct tool.

## 🌍 Engineering Impact
This pattern shows up anywhere a monotonic capability frontier must be queried repeatedly: API version negotiation, feature-flag rollout thresholds, search index segment selection, compiler symbol/version resolution, storage engine SSTable lookup, and latency SLO tiering. At small scale, linear scans are tolerable; at production scale, they turn query fan-out into avoidable CPU burn and tail-latency inflation. Binary search over a sorted compatibility surface gives predictable `O(log n)` lookup cost, preserves throughput under bursty query loads, and cleanly separates write-time ordering guarantees from read-time query efficiency.

## 🔍 Problem Statement
You are given a non-decreasing integer array `patches` of length `n`, where `patches[i]` is the compatibility score of patch version `i + 1`. Later versions never reduce compatibility, so the array is sorted in ascending order with duplicates allowed.

For each query value `target`, return the smallest version index `v` such that `patches[v - 1] >= target`. If every score is smaller than `target`, return `-1`.

Constraints:
- `1 <= n <= 200000`
- `1 <= q <= 200000`
- `0 <= patches[i] <= 1000000000`
- `0 <= target <= 1000000000`

Examples:

- `patches = [2, 4, 4, 7, 10]`, `queries = [4, 5, 10, 11]` → `[2, 4, 5, -1]`
- `patches = [0, 0, 3, 3, 8]`, `queries = [0, 1, 3, 6]` → `[1, 3, 3, 5]`

The decisive constraint is large `n` and `q`: `O(nq)` is not viable, so each query must be answered in logarithmic time.

## 🪜 How to Solve This
1. Read the problem → notice the array is already sorted and never decreases. That immediately suggests a monotonic search space.
2. For one `target`, we do **not** want any satisfying version; we want the **first** one. That wording maps directly to a **lower bound** query.
3. Lower bound means: find the leftmost index where `patches[index] >= target`.
4. In a sorted array, if `patches[mid]` satisfies the target, the answer could still be earlier → move left.
5. If `patches[mid] < target`, nothing at or left of `mid` can work → move right.
6. Repeat until the search interval collapses. The remaining position is the first candidate index.
7. After the search, validate whether that position is inside the array and actually satisfies the target. If not, return `-1`.
8. Since queries are independent, run the same binary search for each one and collect answers in order.

The core insight is not “binary search because sorted,” but “leftmost satisfying position because duplicates exist and the first valid version matters.”

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Lower Bound Binary Search.**  
   The array is sorted and the predicate `patches[i] >= target` is monotonic: once true, it stays true for all later indices. That is exactly the shape binary search exploits.

2. **Define the search interval.**  
   Use zero-based indices over `[0, n)`. Maintain the invariant that the answer, if it exists, lies within the current interval. A common formulation uses `left = 0`, `right = n`, where `right` is exclusive.

3. **Pick the midpoint safely.**  
   Compute `mid = left + (right - left) // 2`. This avoids overflow in languages where `left + right` can exceed integer bounds.

4. **Evaluate the monotonic predicate.**  
   If `patches[mid] >= target`, then `mid` is a valid candidate, but there may be an earlier one. Set `right = mid`.  
   Otherwise, `patches[mid] < target`, so every index `<= mid` is impossible. Set `left = mid + 1`.

5. **Preserve the invariant on every iteration.**  
   After each update, all discarded positions are proven irrelevant: either too small to satisfy the target or strictly to the right of a better candidate.

6. **Terminate when `left == right`.**  
   At this point, `left` is the first index where the predicate could be true. This is the lower bound position.

7. **Convert to the required output.**  
   If `left == n`, no patch satisfies the target → return `-1`. Otherwise return `left + 1`, because the problem expects 1-indexed version numbers.

8. **Repeat per query.**  
   Each query costs `O(log n)`, and no preprocessing beyond reading the array is required because the sorted order is already guaranteed.

## 📊 Worked Example
Take `patches = [2, 4, 4, 7, 10]` and query `target = 5`.

| Step | left | right | mid | patches[mid] | Decision |
|---|---:|---:|---:|---:|---|
| Start | 0 | 5 | 2 | 4 | `4 < 5`, discard left half incl. `mid` → `left = 3` |
| Next | 3 | 5 | 4 | 10 | `10 >= 5`, valid candidate, search earlier → `right = 4` |
| Next | 3 | 4 | 3 | 7 | `7 >= 5`, still valid, search earlier → `right = 3` |
| End | 3 | 3 | — | — | lower bound found at index `3` |

Index `3` in zero-based form corresponds to version `4` in 1-based form, so the answer is `4`.

This trace shows the key invariant: once a value is too small, everything to its left is also too small; once a value is large enough, it may be the answer, but only the left side can contain an earlier valid version.

## ⏱ Complexity Analysis
### Time Complexity
For each of `q` queries, binary search over `n` sorted patch scores takes `O(log n)`, so total time is `O(q log n)`. The dominant cost is repeated midpoint checks. At `10^6` scale this remains practical; at `10^9`, only logarithmic search is operationally reasonable.

### Space Complexity
`O(1)` auxiliary space beyond the output array. The algorithm stores only search bounds and a midpoint per query. Space cannot be meaningfully reduced further unless answers are streamed instead of accumulated, trading convenience for lower output retention.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the **first** position meeting a condition, think **lower bound binary search** immediately.
- Monotonic predicates like “score is at least target” are the strongest signal that binary search applies even when the problem is framed in domain language.
- The result is 1-indexed, but the search is usually easiest in 0-indexed arrays; convert only at the end.
- Duplicates matter: returning the first matching value is different from returning any matching value, so standard equality-based binary search is insufficient.
- In production systems, enforcing monotonic ordering once lets you answer many read-path capability queries cheaply and predictably.

## 🚀 Variations & Further Practice
- **Batch queries with offline processing:** sort queries and sweep the array once; harder because you trade per-query logarithmic search for coordination between two ordered streams.
- **2D capability lookup:** versions and regions/features form a matrix; harder because the search space is no longer a single monotonic axis.
- **Dynamic updates plus queries:** patch scores can change or new versions are inserted; harder because static binary search no longer suffices and you need balanced trees or segment structures.