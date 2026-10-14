# Longest Code Review Window With Per-Language Bug Caps

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Monotonic Queue

---

## 🗂 Problem Overview
Given two aligned arrays describing a chronological stream of findings — `languages[i]` and `severities[i]` — find the longest contiguous window satisfying two constraints simultaneously: each language appears at most `limit` times, and the severity spread within the window (`max - min`) is at most `budget`. Return only the maximum valid window length. The challenge is scale: with up to 200,000 items, any brute-force scan over all subarrays is too slow, so validity must be maintained incrementally.

## 🌍 Engineering Impact
This pattern shows up in streaming observability, abuse detection, review dashboards, and ranking pipelines where a live slice must satisfy multiple bounded constraints at once. One constraint is categorical cardinality per key; the other is numeric range stability. Without an online sliding-window approach, systems fall back to repeated rescans, which collapse under sustained throughput and high-cardinality keys. The combination of a hash map for per-key counts and monotonic queues for rolling extrema is the production-grade shape: predictable linear behavior, bounded per-event work, and no need to materialize or recompute aggregate state for every candidate interval.

## 🔍 Problem Statement
You are given:

- `languages`, an array of lowercase strings
- `severities`, an equally sized array of positive integers
- `limit`, the maximum allowed count of any single language inside a window
- `budget`, the maximum allowed severity spread inside a window

Find the length of the longest **contiguous** subarray such that:

1. For every language in the window, its frequency is at most `limit`
2. `max(severities in window) - min(severities in window) <= budget`

Constraints:

- `1 <= n <= 200000`
- `1 <= severities[i] <= 10^9`
- `1 <= limit <= n`
- `0 <= budget <= 10^9`

Examples:

- `languages = ["py","java","py","cpp","java","py"]`
- `severities = [4,7,5,6,8,5]`, `limit = 2`, `budget = 3` → `4`

- `languages = ["go","go","rs","go","rs","ts","rs"]`
- `severities = [10,3,6,4,7,5,8]`, `limit = 2`, `budget = 4` → `5`

The decisive constraint is `n = 200000`: quadratic enumeration is not viable.

## 🪜 How to Solve This
1. Read the problem → it asks for the **longest contiguous** region under constraints. That is a strong signal for a sliding window with two pointers.

2. Split the constraints by type:
   - “Each language count ≤ limit” is a frequency-tracking problem → use a hash map.
   - “Max severity - min severity ≤ budget” is a rolling extrema problem → use two monotonic deques, one decreasing for max and one increasing for min.

3. Expand the right pointer one item at a time. Every new element updates:
   - its language count
   - the max deque
   - the min deque

4. If either constraint is violated, the current window is invalid. Since the window must stay contiguous, the only repair action is to move the left pointer forward until validity is restored.

5. Because each index enters and leaves the window once, and each deque element is pushed and popped at most once, the whole process is linear.

6. Track the best window length after every successful expansion/shrink cycle.

The key insight: don’t recompute validity for each subarray; maintain just enough state to know validity in O(1) amortized time.

## 🧩 Algorithm Walkthrough
1. **Use the Sliding Window / Two Pointers pattern.**  
   Maintain a window `[left, right]` that always moves forward. This is the right abstraction because the target is the longest valid contiguous segment, and both constraints can be updated incrementally as the window changes.

2. **Track per-language counts with a hash map.**  
   When `right` advances, increment `count[languages[right]]`. This captures the categorical cap exactly. The invariant is: the map reflects frequencies for the current window only.

3. **Track the current maximum severity with a decreasing deque.**  
   Before appending `right`, pop from the back while the stored severity is smaller than `severities[right]`. Then append `right`. The front always holds the index of the maximum severity in the window.

4. **Track the current minimum severity with an increasing deque.**  
   Symmetrically, pop from the back while the stored severity is larger than `severities[right]`, then append `right`. The front always holds the minimum severity index.

5. **Shrink from the left while the window is invalid.**  
   The window is invalid if either:
   - `count[languages[right]] > limit` for some language, or more generally any language count exceeds `limit`
   - `severities[maxDeque.front] - severities[minDeque.front] > budget`

   Since only the newly added element can introduce a count violation, checking counts during shrink is straightforward. On each left move, decrement that language’s count and discard deque fronts that fall left of the window.

6. **Record the best length after restoring validity.**  
   Once both constraints hold, update `answer = max(answer, right - left + 1)`.

7. **Why this is correct.**  
   For each `right`, the algorithm computes the smallest `left` that restores validity. Any earlier `left` is invalid; any later one gives a shorter window. Therefore the longest valid window ending at each `right` is considered, and the global maximum is found.

## 📊 Worked Example
Use Example 2:

- `languages = ["go","go","rs","go","rs","ts","rs"]`
- `severities = [10,3,6,4,7,5,8]`
- `limit = 2`, `budget = 4`

| right | add        | window after shrink | counts                 | min..max | valid |
|------:|------------|---------------------|------------------------|----------|-------|
| 0     | go,10      | [0..0]              | go=1                   | 10..10   | yes   |
| 1     | go,3       | [1..1]              | go=1                   | 3..3     | no → shrink by budget |
| 2     | rs,6       | [1..2]              | go=1, rs=1             | 3..6     | yes   |
| 3     | go,4       | [1..3]              | go=2, rs=1             | 3..6     | yes   |
| 4     | rs,7       | [1..4]              | go=2, rs=2             | 3..7     | yes   |
| 5     | ts,5       | [1..5]              | go=2, rs=2, ts=1       | 3..7     | yes   |
| 6     | rs,8       | [3..6]              | go=1, rs=2, ts=1       | 4..8     | shrink by rs count |

Best valid length is `5`, from indices `[1..5]`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized. Each element is added once by the right pointer, removed once by the left pointer, and pushed/popped at most once from each monotonic deque. At `10^6` elements this remains practical; at `10^9`, linear work is still large but algorithmically optimal for a single pass over the stream.

### Space Complexity
`O(n + k)` in the worst case, where `k` is the number of distinct languages in the current window. The hash map stores per-language counts; the two deques can together hold up to `O(n)` indices in adversarial monotone inputs. Reducing this further would require sacrificing O(1) rolling extrema updates.

## 💡 Key Takeaways
- If a problem asks for the longest contiguous segment under dynamically changing validity rules, start with two pointers before considering heavier structures.
- When window validity depends on both per-key frequencies and rolling min/max, the usual pairing is `HashMap + two monotonic deques`.
- Store indices, not just severity values, in the deques; otherwise you cannot evict stale extrema when `left` advances.
- Shrink in a `while`, not an `if`: one left move may not be enough to repair either a count violation or an excessive severity spread.
- The production lesson is composability: independent window constraints can often be enforced by maintaining orthogonal summaries and intersecting their validity conditions online.

## 🚀 Variations & Further Practice
- Allow at most `m` languages to exceed `limit` by one occurrence. The twist is that validity is no longer a single-key local check; you must track how many keys are currently over cap.
- Replace `max - min <= budget` with median-based deviation or percentile spread. Monotonic queues no longer suffice; you need balanced trees, heaps with lazy deletion, or order-statistics structures.
- Ask for the number of valid windows instead of the longest one. Same sliding-window core, but the counting logic changes from maximizing length to accumulating `right - left + 1` after each repair.