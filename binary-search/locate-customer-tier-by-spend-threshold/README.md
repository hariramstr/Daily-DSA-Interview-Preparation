# Locate Customer Tier by Spend Threshold

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** binary-search, array, search

---

## 🗂 Problem Overview
Given a strictly increasing sorted array `thresholds` and an integer `spend`, return the index of the largest threshold value that does not exceed `spend`. If every threshold is greater than `spend`, return `-1`. The challenge is not exact lookup but finding the rightmost valid position. Because the array can contain up to `100000` elements, a linear scan is avoidable; the required solution should run in `O(log n)` time.

## 🌍 Engineering Impact
This pattern shows up anywhere a continuous input must be mapped onto ordered policy bands: pricing tiers, tax brackets, rate-limit buckets, feature rollout cohorts, ad bidding floors, and storage class selection. In production, these lookups often sit on hot paths and execute millions of times per second. A linear scan over tier boundaries is acceptable at toy scale but becomes latency noise and cache waste under load. Binary search over immutable sorted thresholds enables predictable lookup cost, simpler data structures, and clean separation between configuration management and request-time decision logic.

## 🔍 Problem Statement
You are given an integer array `thresholds` sorted in strictly increasing order, where each value is the minimum spend required to qualify for a loyalty tier. You are also given an integer `spend`. Return the index of the highest threshold such that `thresholds[index] <= spend`. If `spend` is smaller than `thresholds[0]`, return `-1`.

Constraints:

- `1 <= thresholds.length <= 100000`
- `0 <= thresholds[i] <= 1000000000`
- `thresholds` is sorted in strictly increasing order
- `0 <= spend <= 1000000000`

Examples:

- `thresholds = [0, 100, 300, 700], spend = 450` → `2`
- `thresholds = [50, 120, 200], spend = 20` → `-1`

The key requirement is `O(log n)` time, which rules out scanning from left to right and points directly to binary search. Edge cases include values below all thresholds, exactly matching a threshold, and exceeding the final threshold.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not “find `spend` in the array.” It is “find the last value `<= spend`.”
2. Notice the array is already sorted and strictly increasing → that means validity is monotonic:
   - if `thresholds[i] <= spend`, everything to the left is also valid
   - if `thresholds[i] > spend`, everything to the right is also invalid
3. Monotonic validity immediately suggests binary search, but not the exact-match variant. We need the **rightmost true** position.
4. Keep two pointers, `left` and `right`, over the search range. At each midpoint:
   - if `thresholds[mid] <= spend`, `mid` is a candidate, but there may be a better answer to the right
   - otherwise, discard the right half including `mid`
5. Track the best candidate seen so far, or structure the loop so the final boundary encodes it.
6. Stop when the search space is exhausted. If no candidate was ever valid, return `-1`.

The key mental shift is to search over a boolean condition boundary, not over exact equality.

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Binary Search on a Monotonic Predicate.**  
   Define the predicate as `thresholds[i] <= spend`. Because the array is sorted in increasing order, this predicate is true for a prefix of indices and false afterward. We are searching for the boundary between those two regions.

2. **Initialize search bounds.**  
   Set `left = 0`, `right = thresholds.length - 1`, and `answer = -1`.  
   `answer = -1` is the correct default because there may be no threshold small enough for the given spend.

3. **Compute the midpoint safely.**  
   Use `mid = left + (right - left) / 2`.  
   This avoids overflow in languages where `left + right` could exceed integer limits, and it preserves the invariant that `mid` lies within the current search interval.

4. **Evaluate the midpoint against the spend.**  
   If `thresholds[mid] <= spend`, then `mid` is a valid tier index. Record `answer = mid` and move `left = mid + 1` to search for a higher valid threshold.  
   If `thresholds[mid] > spend`, then `mid` and everything to its right are invalid, so move `right = mid - 1`.

5. **Maintain the invariant.**  
   At every step:
   - all indices `<= answer` are known to contain at least one valid candidate up to `answer`
   - all indices beyond `right` are either unexamined no longer or proven too large
   - the remaining interval `[left, right]` is the only place a better answer can exist

6. **Terminate and return.**  
   When `left > right`, the search space is empty. `answer` holds the rightmost index satisfying `thresholds[index] <= spend`, or `-1` if none exists.

This abstraction is correct because sorted order turns the problem into finding the last true value in a monotonic sequence.

## 📊 Worked Example
Example: `thresholds = [0, 100, 300, 700]`, `spend = 450`

| Step | left | right | mid | thresholds[mid] | Condition | answer | Next move |
|------|------|-------|-----|-----------------|-----------|--------|-----------|
| 1 | 0 | 3 | 1 | 100 | `100 <= 450` true | 1 | move right: `left = 2` |
| 2 | 2 | 3 | 2 | 300 | `300 <= 450` true | 2 | move right: `left = 3` |
| 3 | 3 | 3 | 3 | 700 | `700 <= 450` false | 2 | move left: `right = 2` |

Now `left = 3`, `right = 2`, so the loop stops.

Result: `answer = 2`.

Why this works: every time a threshold is valid, it becomes a candidate, but the algorithm keeps searching right to see whether a higher qualifying tier exists. Once it encounters `700`, it knows that index and everything beyond it are too large, so `300` at index `2` is the highest valid threshold.

## ⏱ Complexity Analysis
### Time Complexity
`O(log n)`. Each iteration halves the remaining search interval, so the dominant operation is the binary search loop. At `10^6` elements, this is about 20 comparisons; at `10^9`, about 30. That is the difference between stable request-time lookup and work proportional to dataset size.

### Space Complexity
`O(1)`. The algorithm uses only a few scalar variables: bounds, midpoint, and answer. No auxiliary data structure is required. Space cannot be meaningfully reduced further; the only trade-off would be recursion, which would increase call-stack usage without improving clarity or performance.

## 💡 Key Takeaways
- If the input is sorted and the question asks for the last value `<= target`, think “binary search on boundary,” not exact lookup.
- When validity forms a true-prefix / false-suffix pattern, the problem is a monotonic predicate search.
- The main off-by-one trap is failing to continue right after finding a valid midpoint; that returns any valid index, not the highest one.
- Another common bug is returning `left` or `right` blindly without proving what they mean after loop termination; tracking `answer` avoids ambiguity.
- In production systems, ordered threshold tables plus binary search are a clean way to separate mutable business policy from low-latency decision execution.

## 🚀 Variations & Further Practice
- Return the tier value instead of the index, or return both lower and upper tier bounds; the twist is handling “no upper bound” cleanly for the final tier.
- Support repeated queries over changing thresholds; the harder part is deciding when to rebuild sorted arrays versus using balanced trees or indexed structures.
- Extend to 2D or multi-attribute tiering, where a single sorted axis no longer works and the main challenge becomes preserving efficient search under more complex eligibility rules.