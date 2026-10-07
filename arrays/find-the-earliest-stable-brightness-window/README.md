# Find the Earliest Stable Brightness Window

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Arrays &nbsp;|&nbsp; **Tags:** arrays, sliding-window, simulation

---

## 🗂 Problem Overview
Given an integer array `brightness`, a window size `k`, and a tolerance `limit`, find the earliest starting index of any contiguous subarray of exactly length `k` whose `max - min <= limit`. If no such window exists, return `-1`.

The only real work is validating each fixed-size window efficiently and stopping at the first valid one. Even with small constraints, this is a classic sliding-window scan over time-series data.

## 🌍 Engineering Impact
This pattern shows up anywhere systems validate local stability over ordered measurements: display telemetry, sensor health checks, streaming anomaly detection, market data smoothing, and SLO burn-rate monitoring. The question is rarely “is the whole series stable?”; it is “when do we first observe a stable interval of the required duration?”

At scale, naive rescans of every window become a throughput tax in ingestion pipelines and online evaluators. Sliding-window thinking enables bounded-latency checks, early termination, and cleaner separation between window maintenance and acceptance criteria. That same design scales from batch jobs to real-time stream processors.

## 🔍 Problem Statement
You are given:

- `brightness`, where `brightness[i]` is the measured brightness at minute `i`
- an integer `k`, the exact window length
- an integer `limit`, the maximum allowed spread inside a window

A window is **stable** if the difference between its maximum and minimum values is at most `limit`. Return the starting index of the earliest stable window of length `k`, or `-1` if none exists.

Constraints:

- `1 <= brightness.length <= 200`
- `1 <= brightness[i] <= 10^4`
- `1 <= k <= brightness.length`
- `0 <= limit <= 10^4`

Examples:

- `brightness = [7, 9, 8, 8, 10, 13], k = 3, limit = 2` → `0`
- `brightness = [4, 10, 3, 12, 8], k = 2, limit = 1` → `-1`

The key constraint is that windows are fixed-size and contiguous. Since `n <= 200`, a straightforward per-window max/min scan is fully acceptable, though the structure naturally suggests a sliding-window formulation.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not searching for any arbitrary subset; we need **contiguous** windows of **exactly** length `k`.

2. The validation rule for a window is simple: compute its minimum and maximum, then check whether `max - min <= limit`.

3. Because the answer must be the **earliest** valid window, scan windows from left to right and return immediately on the first success. That observation avoids unnecessary work once a valid window is found.

4. With small constraints, the simplest approach is enough:
   - for each start index `i`
   - inspect elements `brightness[i..i+k-1]`
   - track `minVal` and `maxVal`
   - test the spread

5. Why think “sliding window” at all? Because the problem is fundamentally “evaluate every contiguous fixed-size block.” Even if you recompute min/max each time, the mental model is still a fixed-size window moving one step right.

6. If constraints were larger, the same framing would lead naturally to monotonic deques for rolling min/max. Here, clarity beats premature optimization.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: fixed-size Sliding Window.**  
   We must evaluate every contiguous block of size `k`, in order. That is the defining signal for a sliding-window problem, even if the implementation is the simple simulation version.

2. **Iterate over all valid starting indices.**  
   Start positions range from `0` through `n - k`. This guarantees every window has exactly `k` elements and prevents out-of-bounds access.  
   **Invariant:** before processing index `start`, all earlier windows have already been checked and rejected.

3. **Scan the current window to compute min and max.**  
   Initialize `minVal = +∞` and `maxVal = -∞`, then loop from `start` to `start + k - 1`. Update both values for each element.  
   **Why correct:** by the end of the inner loop, `minVal` and `maxVal` are the true extrema of that window.

4. **Evaluate the stability condition.**  
   If `maxVal - minVal <= limit`, return `start`.  
   **Invariant:** the first returned index is the earliest valid one because windows are examined left to right.

5. **Continue if unstable.**  
   If the condition fails, move to the next starting index and repeat. No state from the previous window is required in the straightforward version.

6. **Return `-1` if no window qualifies.**  
   If the loop ends, every possible window has been tested and none satisfied the rule.

This abstraction is right because it matches the problem’s shape exactly: ordered data, contiguous ranges, fixed width, and a per-window predicate.

## 📊 Worked Example
Example: `brightness = [7, 9, 8, 8, 10, 13]`, `k = 3`, `limit = 2`

| Start | Window       | Min | Max | Max - Min | Stable? |
|------:|--------------|----:|----:|----------:|:-------:|
| 0     | `[7, 9, 8]`  | 7   | 9   | 2         | Yes     |

Trace:

1. `start = 0`, inspect indices `0..2`.
2. Read `7` → `min = 7`, `max = 7`
3. Read `9` → `min = 7`, `max = 9`
4. Read `8` → `min = 7`, `max = 9`
5. Compute spread: `9 - 7 = 2`
6. Since `2 <= limit`, the window is stable.
7. Because windows are checked from left to right, `0` is the earliest valid answer, so the algorithm returns immediately.

No later window matters once the first stable one is found.

## ⏱ Complexity Analysis
### Time Complexity
The straightforward solution runs in `O((n - k + 1) * k)`, which is `O(nk)` in the worst case. Each window recomputes its min and max from scratch. For `n <= 200`, this is trivial. At `10^6` or `10^9` scale, this approach would be unacceptable and should be replaced with rolling min/max structures.

### Space Complexity
Space is `O(1)`. The algorithm stores only loop indices and the current window’s `minVal` and `maxVal`. You cannot reduce asymptotic space further; the only trade-off is adding auxiliary structures, such as deques, to improve runtime for larger inputs.

## 💡 Key Takeaways
- Fixed-size contiguous subarray + “check every window” is the clearest signal for a sliding-window formulation.
- If the requirement asks for the **earliest** valid window, left-to-right scanning with immediate return is usually the simplest correct strategy.
- The valid start indices end at `n - k`; using `n - k + 1` as the loop count avoids classic off-by-one errors.
- Be careful to reset `minVal` and `maxVal` for each new window in the straightforward implementation; carrying state across windows is incorrect unless using a true rolling structure.
- In production systems, this pattern generalizes to local stability detection over streams, where the core design choice is whether to recompute per window or maintain incremental state.

## 🚀 Variations & Further Practice
- Return **all** stable window start indices instead of the first one; same pattern, but no early exit and more emphasis on output handling.
- Scale to large `n` by maintaining rolling min and max with **monotonic deques**; the conceptual twist is incremental window maintenance in `O(n)`.
- Allow variable-length windows and ask for the earliest window of length **at least** `k`; the harder part is that the acceptance condition no longer maps to a single fixed-size scan.