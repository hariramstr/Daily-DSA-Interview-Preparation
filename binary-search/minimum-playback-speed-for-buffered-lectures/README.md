# Minimum Playback Speed for Buffered Lectures

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Array, Math

---

## 🗂 Problem Overview
Given lecture segment lengths in minutes and a deadline `h` in hours, find the minimum integer playback speed `s` such that watching every segment at speed `s` finishes within `h * 60` total minutes. Each segment costs `ceil(length / s)` minutes, so rounding happens per segment, not globally. The challenge is that brute-forcing speeds is too expensive under large constraints, and feasibility changes monotonically as speed increases.

## 🌍 Engineering Impact
This pattern shows up anywhere a tunable capacity parameter must satisfy a hard deadline or budget: batch sizing in streaming pipelines, worker concurrency in job systems, shard fanout limits in search infrastructure, or throughput caps in rate-limited APIs. The key property is monotonic feasibility: once a configuration is sufficient, larger configurations remain sufficient. Without exploiting that structure, systems fall back to linear scans or simulation-heavy tuning loops that collapse at scale. Binary search over the answer turns expensive planning into a predictable control loop with bounded latency, which matters in autoscaling, admission control, and SLO-driven capacity decisions.

## 🔍 Problem Statement
You are given an integer array `lectures` where `lectures[i]` is the duration in minutes of the `i`-th recorded lecture segment, and an integer `h` representing available time in hours. A single positive integer playback speed `s` must be used for all segments.

For a segment of length `x`, time spent is `ceil(x / s)` minutes. Because rounding is applied per segment, even arbitrarily large speed still costs at least 1 minute for every non-empty segment. If total required time exceeds `h * 60` even in that best case, return `-1`.

Constraints:
- `1 <= lectures.length <= 100000`
- `1 <= lectures[i] <= 1000000000`
- `1 <= h <= 1000000000`

Examples:
- `lectures = [45, 80, 30], h = 3` → `1`
- `lectures = [120, 95, 200], h = 4` → minimum valid speed is `2`

The scale rules out naive per-speed search; the monotonic feasibility condition drives the algorithmic choice.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not minimizing total time directly; we are minimizing a **speed** subject to a time budget.
2. Notice the key monotonic fact → if speed `s` is fast enough, then any speed `s + 1`, `s + 2`, ... is also fast enough. Faster playback never increases total rounded time.
3. That immediately suggests **binary search on the answer**, not binary search on the array.
4. Define a feasibility check: for a candidate speed `s`, compute  
   `sum(ceil(lectures[i] / s))` and compare it with `h * 60`.
5. Establish bounds:
   - Lower bound is `1`
   - Upper bound can be `max(lectures)`, because at that speed every segment takes at most 1 minute
6. Handle impossibility first → if the number of segments exceeds `h * 60`, return `-1`, since each segment costs at least 1 minute.
7. Binary search for the first feasible speed. When a speed works, keep searching left for a smaller valid one; when it fails, move right.

This is the standard “minimum value satisfying a monotonic predicate” pattern.

## 🧩 Algorithm Walkthrough
1. **Convert the deadline into the same unit as the cost function.**  
   `limit = h * 60` minutes. This is necessary because each segment’s rounded viewing time is measured in minutes. Use a wide integer type to avoid overflow.

2. **Check the hard impossibility condition.**  
   Every non-empty segment takes at least 1 minute regardless of speed, so the absolute lower bound on total time is `lectures.length`. If `lectures.length > limit`, no speed can work.  
   **Invariant:** if this check passes, at least one feasible speed may exist.

3. **Choose the binary-search range.**  
   Set `left = 1`, `right = max(lectures)`. At `right`, each segment takes at most `ceil(x / max) <= 1`, so total time is at most the number of segments. Since impossibility was already ruled out, `right` is guaranteed feasible.  
   **Invariant:** the answer lies in `[left, right]`.

4. **Evaluate a candidate speed with a monotonic predicate.**  
   For `mid`, compute total minutes as  
   `total += (x + mid - 1) / mid`  
   which is integer-safe ceiling division. If `total <= limit`, `mid` is feasible.

5. **Shrink toward the first feasible value.**  
   - If feasible, record that all speeds `>= mid` are also feasible, so move `right = mid`.
   - If not feasible, move `left = mid + 1`.  
   **Invariant:** `left` always points to the smallest not-yet-eliminated candidate.

6. **Terminate when `left == right`.**  
   That value is the minimum feasible playback speed.

The pattern here is **Binary Search on Monotonic Predicate**. The abstraction fits because the search space is numeric and ordered, while feasibility flips exactly once from false to true.

## 📊 Worked Example
Example: `lectures = [120, 95, 200]`, `h = 4`

`limit = 4 * 60 = 240` minutes

| Step | left | right | mid | Total time at `mid` | Feasible? |
|---|---:|---:|---:|---:|---|
| Init | 1 | 200 | - | - | - |
| 1 | 1 | 200 | 100 | `2 + 1 + 2 = 5` | Yes |
| 2 | 1 | 100 | 50 | `3 + 2 + 4 = 9` | Yes |
| 3 | 1 | 50 | 25 | `5 + 4 + 8 = 17` | Yes |
| 4 | 1 | 25 | 13 | `10 + 8 + 16 = 34` | Yes |
| 5 | 1 | 13 | 7 | `18 + 14 + 29 = 61` | Yes |
| 6 | 1 | 7 | 4 | `30 + 24 + 50 = 104` | Yes |
| 7 | 1 | 4 | 2 | `60 + 48 + 100 = 208` | Yes |
| 8 | 1 | 2 | 1 | `120 + 95 + 200 = 415` | No |

Search ends at `left = right = 2`, so the minimum valid speed is `2`.

## ⏱ Complexity Analysis

### Time Complexity
`O(n log M)`, where `n` is the number of lecture segments and `M` is the maximum segment length. Each binary-search step scans the array once to evaluate feasibility. This scales well because `log M` is small even when durations approach `10^9`; the expensive part remains the linear pass.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only search bounds, the running total, and a few scalars. Space cannot be meaningfully reduced further without changing the input model; the main trade-off is using wider integer types for correctness, not extra memory.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer parameter** that makes a condition true, check whether feasibility is monotonic and search the answer space directly.
- When “faster/larger/more capacity” can only improve the outcome, that is a strong binary-search-on-predicate signal in both interviews and production tuning code.
- Convert units before reasoning: `h` is in hours, but the computed cost is in rounded minutes, so compare against `h * 60`, not `h`.
- Use integer ceiling division carefully: `(x + s - 1) / s`; avoid floating point and guard against overflow in both the sum and `h * 60`.
- The transferable design insight is to separate **policy search** from **cost evaluation**: a monotonic evaluator plus binary search is often the cleanest way to tune capacity under hard constraints.

## 🚀 Variations & Further Practice
- Allow different playback speeds per segment but add a switching penalty between segments. The monotonic predicate becomes more complex and may require DP or shortest-path reasoning instead of a simple global check.
- Replace per-segment rounding with a global total `ceil(sum / s)`. The monotonic property still holds, but the structure changes enough that the feasibility function becomes simpler and edge cases shift.
- Add a fixed setup cost per lecture batch and allow grouping adjacent segments before playback. The search parameter may remain monotonic, but validating a candidate now requires greedy batching or dynamic programming.