# Minimum Signal Delay for Satellite Image Batches

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Monotonic Predicate, Array

---

## 🗂 Problem Overview
You are given an array `batches`, where `batches[i]` is the size of the `i`-th satellite image batch, and an integer `h` representing the total minutes available. You must choose a constant integer transmission speed `s` such that sending batch `i` takes `ceil(batches[i] / s)` minutes. Return the minimum `s` that finishes all batches within `h`, or `-1` if impossible. The challenge is that batch sizes and `h` are large enough to rule out brute-force speed testing.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must find the minimum viable capacity under hard latency or throughput constraints: batch schedulers, network shapers, storage compaction windows, stream backfill jobs, and distributed ingestion pipelines. The core question is not “simulate everything” but “find the smallest configuration that satisfies an SLA.” At scale, linear search over capacity values becomes operationally useless when the search space spans millions or billions. Recognizing monotonic feasibility lets you replace expensive tuning loops with deterministic logarithmic search, which directly improves autoscaling decisions, admission control, and capacity planning.

## 🔍 Problem Statement
Given `batches` and `h`, compute the minimum integer transmission speed `s` such that:

- each batch is transmitted independently,
- only one batch can be worked on per minute,
- a partially used minute cannot be reused for the next batch,
- total time is `sum(ceil(batches[i] / s))`.

Return `-1` if no speed can work. That happens exactly when `h < batches.length`, because every non-empty batch requires at least one full minute regardless of speed.

**Constraints**
- `1 <= batches.length <= 100000`
- `1 <= batches[i] <= 1000000000`
- `1 <= h <= 1000000000`
- answer fits in 32-bit signed integer

**Examples**
- `batches = [30, 11, 23, 4, 20], h = 6` → `23`
- `batches = [8, 5, 8], h = 2` → `-1`

The key algorithmic driver is the huge answer space: possible speeds range from `1` to `max(batches)`, so checking each candidate directly is too expensive.

## 🪜 How to Solve This
1. Start from the formula: for a fixed speed `s`, total required time is `ceil(b1/s) + ceil(b2/s) + ...`.
2. Ask what changes as `s` increases → each term stays the same or decreases, never increases.
3. That means feasibility is monotonic:
   - if speed `s` finishes within `h`,
   - then any speed `> s` also finishes within `h`.
4. Once you see a monotonic yes/no condition over an integer range, think **binary search on the answer**, not binary search on the array.
5. Define a predicate: `canFinish(s)` returns whether total minutes at speed `s` is `<= h`.
6. Establish bounds:
   - lower bound = `1`
   - upper bound = `max(batches)` because at that speed every batch takes exactly one minute.
7. Handle the impossible case first: if `h < number of batches`, return `-1`.
8. Binary search for the smallest speed where `canFinish(s)` is true.
9. This works because we are finding the leftmost true value in a monotonic boolean search space.

That is the entire mental model: convert optimization into feasibility, then search the boundary.

## 🧩 Algorithm Walkthrough
1. **Pre-check impossibility**  
   If `h < batches.length`, return `-1`. This is correct because every batch has positive size, so each needs at least one whole minute no matter how large the speed is. This maintains the invariant that any remaining search space is potentially feasible.

2. **Set binary search bounds**  
   Use `left = 1` and `right = max(batches)`. Speed `1` is the slowest valid integer speed. Speed `max(batches)` is always sufficient when the problem is feasible, because each batch then completes in one minute. This bounds the answer tightly enough for logarithmic search.

3. **Define the monotonic predicate**  
   For a candidate speed `mid`, compute total time as  
   `sum((batch + mid - 1) / mid)` using integer arithmetic.  
   This is the standard ceiling-division trick and avoids floating-point issues. The predicate is: `requiredTime <= h`.

4. **Evaluate feasibility with early exit**  
   While summing, if `requiredTime` already exceeds `h`, stop and return false. This preserves correctness and reduces unnecessary work on clearly invalid speeds.

5. **Binary search the first feasible speed**  
   If `canFinish(mid)` is true, record that the answer is at most `mid`, so move `right = mid`. Otherwise move `left = mid + 1`. The invariant is:
   - all speeds `< left` are infeasible,
   - all speeds `>= right` are feasible.

6. **Terminate when bounds converge**  
   When `left == right`, that value is the minimum feasible speed. This is the classic **Binary Search on Answer / Monotonic Predicate** pattern: search not for a value in data, but for the smallest configuration satisfying a constraint.

## 📊 Worked Example
Take `batches = [30, 11, 23, 4, 20]`, `h = 6`.

| Step | left | right | mid | Required minutes at `mid` | Feasible? |
|---|---:|---:|---:|---|---|
| 1 | 1 | 30 | 15 | `2 + 1 + 2 + 1 + 2 = 8` | No |
| 2 | 16 | 30 | 23 | `2 + 1 + 1 + 1 + 1 = 6` | Yes |
| 3 | 16 | 23 | 19 | `2 + 1 + 2 + 1 + 2 = 8` | No |
| 4 | 20 | 23 | 21 | `2 + 1 + 2 + 1 + 1 = 7` | No |
| 5 | 22 | 23 | 22 | `2 + 1 + 2 + 1 + 1 = 7` | No |

Bounds converge at `23`, so the minimum valid speed is `23`.

The important trace detail is not the arithmetic itself, but the shrinking invariant: once a speed is feasible, everything to its right is also feasible, so the search always moves toward the leftmost feasible point.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `n = batches.length` and `M = max(batches)`. Each binary-search step scans the array once to evaluate feasibility, and there are `log M` such steps. With `M` up to `10^9`, `log2(M)` is about 30, so even very large value ranges remain practical; the array scan, not the search depth, dominates runtime.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few scalar variables for bounds and accumulated time; no extra data structures proportional to input size are required. Space cannot meaningfully be reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem asks for the **minimum integer parameter** that satisfies a constraint, check whether feasibility becomes monotonic as that parameter increases.
- When the search space is a numeric range rather than a sorted collection, think **binary search on answer** with a boolean predicate.
- The impossible case is easy to miss: if `h < batches.length`, return `-1` before searching.
- Use integer ceiling division as `(x + s - 1) / s`; floating-point `ceil(x / s)` invites precision and type bugs.
- In production systems, this pattern turns capacity tuning from iterative guesswork into a deterministic boundary search over a monotonic SLA predicate.

## 🚀 Variations & Further Practice
- **Split array to minimize largest subarray sum** — same binary-search-on-answer pattern, but the predicate is based on partition count rather than per-item ceiling time.
- **Koko Eating Bananas / minimum processing rate problems** — identical monotonic structure, useful for reinforcing the “leftmost feasible rate” mental model.
- **Minimum time to complete jobs with parallel workers** — harder because feasibility depends on multiple workers contributing concurrently, which changes how the predicate is computed.