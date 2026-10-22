# Find the Last Passed Practice Exam

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Sorting

---

## 🗂 Problem Overview
Given a non-decreasing array `scores` and a threshold `passMark`, return the index of the rightmost score `<= passMark`. If every score is greater than `passMark`, return `-1`. The challenge is not the comparison itself but exploiting sorted order to avoid a linear scan. The required solution is a boundary-finding binary search over a monotonic condition: once scores exceed `passMark`, all later entries do too.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need the last value satisfying an ordered predicate. Search engines use it to find ranking cutoffs in sorted score lists. Time-series and streaming systems use it to locate the latest event before a watermark. Distributed rate-limiters and schedulers use similar boundary searches over sorted timestamps or quotas. Compiler and database internals use ordered indexes to find predecessor entries efficiently. Without logarithmic lookup, these operations degrade into repeated scans, inflating tail latency and making otherwise cheap control-plane decisions expensive at scale.

## 🔍 Problem Statement
You are given a sorted integer array `scores` of length up to `100000`, where `scores[i]` is the score of the `i`-th practice exam. The array is sorted in non-decreasing order, so duplicates are allowed. You are also given an integer `passMark`.

Return the index of the last element in `scores` whose value is less than or equal to `passMark`. If no such element exists, return `-1`.

This is a classic boundary-search problem: the sorted order makes the predicate `scores[i] <= passMark` monotonic, which is what enables binary search.

**Constraints**
- `1 <= scores.length <= 100000`
- `0 <= scores[i] <= 10^9`
- `scores` is sorted in non-decreasing order
- `0 <= passMark <= 10^9`

**Examples**
- `scores = [35, 42, 42, 58, 71, 88]`, `passMark = 58` → `3`
- `scores = [60, 75, 80]`, `passMark = 50` → `-1`

## 🪜 How to Solve This
1. Read the requirement carefully → we do **not** want any passing score; we want the **last** one. That means this is a right-boundary search.
2. Notice the array is already sorted → values `<= passMark` form a prefix, and values `> passMark` form the suffix after that prefix.
3. That prefix/suffix split gives a monotonic structure → perfect fit for binary search.
4. At each midpoint, ask one question: is `scores[mid] <= passMark`?
   - If yes, `mid` is a valid candidate, but there may be a later valid index, so move right.
   - If no, `mid` is too large, so move left.
5. Keep track of the best valid index seen so far.
6. When the search ends, that stored index is the rightmost passing exam, or `-1` if no score ever satisfied the condition.

The key mental move is recognizing this as “find the last true” over a sorted boolean boundary, not “search for an exact value.”

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on Answer Boundary.**  
   The predicate is `scores[i] <= passMark`. Because `scores` is sorted, this predicate is true for some prefix of indices and false afterward. That monotonicity is exactly why binary search applies.

2. **Initialize search bounds.**  
   Set `left = 0`, `right = scores.length - 1`, and `answer = -1`.  
   `answer` stores the best valid index found so far. The invariant is: every recorded `answer` points to a score `<= passMark`.

3. **Compute the midpoint safely.**  
   Use `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` could exceed integer range, even though constraints here are modest.

4. **Evaluate the midpoint against the predicate.**  
   If `scores[mid] <= passMark`, then `mid` is a valid passing exam. Record `answer = mid` and move `left = mid + 1` to search for a later valid index.  
   Why correct: any index left of `mid` cannot improve on “last passing exam.”

5. **Discard invalid upper region when needed.**  
   If `scores[mid] > passMark`, move `right = mid - 1`.  
   Why correct: `mid` and everything to its right are too large due to sorted order.

6. **Terminate when bounds cross.**  
   When `left > right`, all candidates have been classified. The invariant guarantees `answer` is the largest index seen with `scores[index] <= passMark`, or `-1` if none existed.

This is the standard “rightmost true” binary search template.

## 📊 Worked Example
Example: `scores = [35, 42, 42, 58, 71, 88]`, `passMark = 58`

| Step | left | right | mid | scores[mid] | Condition         | answer | Next move        |
|------|------|-------|-----|-------------|-------------------|--------|------------------|
| 1    | 0    | 5     | 2   | 42          | `42 <= 58` true   | 2      | search right     |
| 2    | 3    | 5     | 4   | 71          | `71 <= 58` false  | 2      | search left      |
| 3    | 3    | 3     | 3   | 58          | `58 <= 58` true   | 3      | search right     |

Now `left = 4`, `right = 3`, so the loop stops.

Result: `answer = 3`.

Why this works: each successful comparison advances the candidate boundary rightward, while each failure eliminates an entire suffix. The final stored candidate is the last passing score.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, so the dominant operation is the binary search loop. At `10^6` elements this is about 20 comparisons; at `10^9`, about 30. That difference is why boundary search remains viable in latency-sensitive paths while linear scans do not.

### Space Complexity
`O(1)`. The algorithm uses only a few scalar variables: `left`, `right`, `mid`, and `answer`. No auxiliary data structures are required. Space cannot be meaningfully reduced further unless the input representation itself changes.

## 💡 Key Takeaways
- If the input is sorted and the goal is “first/last element satisfying a condition,” think boundary binary search before considering exact-match search.
- A monotonic predicate like `scores[i] <= passMark` is the strongest signal that the problem is really “find last true.”
- The main off-by-one trap is moving `left = mid + 1` after a valid hit; failing to do so can return any valid index instead of the last one.
- Another common bug is forgetting to persist `answer` before moving right, which loses the best known valid candidate.
- In production systems, this pattern is the foundation of predecessor lookup on ordered data: cheap, predictable, and composable with indexed storage.

## 🚀 Variations & Further Practice
- Find the **first** exam score greater than or equal to a target; same pattern, but now you are searching for the left boundary instead of the right one.
- Given multiple `passMark` queries, answer each efficiently; the twist is deciding between repeated binary searches and preprocessing depending on query volume.
- Search in a rotated sorted array for the last value satisfying a threshold; the harder part is restoring enough ordering structure to make binary search valid.