# Minimum Launch Power for Satellite Relay Windows

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Monotonic Predicate, Arrays

---

## 🗂 Problem Overview
Given two length-`n` arrays, `windows` and `power`, find the minimum integer launch power limit `X` such that at least `k` relay windows can be served in chronological order. A window is usable iff `power[i] <= X`; unusable windows are skipped permanently. The output is the smallest feasible `X`. The non-trivial part is scale: `n` is up to 200,000, so testing every possible threshold or simulating many candidate limits naively is too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must choose the smallest configuration threshold that satisfies a throughput or coverage target. Examples include distributed rate-limiters finding the minimum quota to admit `k` requests, streaming pipelines selecting the smallest resource tier that processes enough events, and search/ranking systems tuning a score cutoff to retain sufficient candidates. At scale, linear scans over all possible thresholds collapse under cardinality and latency budgets. A monotonic-feasibility formulation enables predictable `O(n log V)` behavior, turning an open-ended search space into a bounded decision problem with operationally stable performance.

## 🔍 Problem Statement
You are given:

- `windows`, a strictly increasing array of length `n`
- `power`, an array of required launch energies of length `n`
- an integer `k`, where `1 <= k <= n`

A transmitter processes relay windows only in the given order. For a chosen power limit `X`, it can transmit to window `i` exactly when `power[i] <= X`; otherwise that window is skipped and cannot be revisited. The goal is to return the minimum integer `X` that allows at least `k` successful transmissions.

Constraints:

- `1 <= n <= 200000`
- `1 <= k <= n`
- `1 <= windows[i], power[i] <= 10^9`
- `windows` is strictly increasing

Examples:

- `windows = [2, 5, 9, 12], power = [7, 3, 6, 4], k = 3` → `6`
- `windows = [1, 4, 8, 10, 15], power = [9, 2, 5, 8, 1], k = 4` → `8`

The key algorithmic constraint is the large value range for `power[i]`, combined with monotonic feasibility.

## 🪜 How to Solve This
1. Read the problem → the actual `windows` timestamps do not affect eligibility beyond preserving order. Since every usable window can simply be taken when encountered, the question reduces to: for a given `X`, how many indices satisfy `power[i] <= X`?

2. That immediately suggests a feasibility check: scan `power`, count usable windows, and return whether the count reaches `k`.

3. Notice the monotonic property → if some `X` works, then any larger `X` also works, because increasing the limit never invalidates a previously usable window.

4. Monotonic feasibility over an integer answer space is the standard trigger for binary search on the answer, not on indices.

5. Define the search range from the data: the minimum possible answer is `min(power)`, and the maximum is `max(power)`.

6. Binary search that range. For each midpoint, run the linear feasibility check. If it works, try smaller; if it fails, go larger.

7. The first feasible value is the minimum required launch power.

## 🧩 Algorithm Walkthrough
1. **Model the problem as a monotonic predicate.**  
   Pattern: **Binary Search on Answer** with a **Monotonic Predicate**. Define `can(X)` as “there are at least `k` indices with `power[i] <= X`.” This is the right abstraction because the output is not an index or position; it is the smallest integer threshold satisfying a yes/no condition.

2. **Observe why order does not complicate the check.**  
   Since windows are processed chronologically and skipping is allowed, every window with `power[i] <= X` can be accepted when reached. There is no capacity conflict, cooldown, or adjacency rule. Therefore feasibility depends only on the count of eligible windows, not on any more complex scheduling state.

3. **Implement `can(X)` with one pass.**  
   Scan `power`, increment a counter when `power[i] <= X`, and return `true` once the counter reaches `k`. The invariant is: after processing the first `i` elements, the counter equals the number of windows among those `i` that are usable under threshold `X`.

4. **Choose search bounds.**  
   Let `lo = min(power)` and `hi = max(power)`. Any answer below `lo` is impossible; `hi` is always feasible because it admits every window. This guarantees the answer lies in `[lo, hi]`.

5. **Run lower-bound binary search.**  
   Compute `mid = lo + (hi - lo) / 2`. If `can(mid)` is true, keep the left half including `mid` by setting `hi = mid`; otherwise set `lo = mid + 1`. The invariant is: the true answer always remains inside `[lo, hi]`.

6. **Terminate when `lo == hi`.**  
   At convergence, the interval contains exactly one value, and by the maintained invariant it is the smallest feasible launch power.

## 📊 Worked Example
Use `windows = [2, 5, 9, 12]`, `power = [7, 3, 6, 4]`, `k = 3`.

Initial bounds: `lo = 3`, `hi = 7`

| Step | lo | hi | mid | Usable powers (`<= mid`) | Count | Feasible? |
|---|---:|---:|---:|---|---:|---|
| 1 | 3 | 7 | 5 | 3, 4 | 2 | No |
| 2 | 6 | 7 | 6 | 3, 6, 4 | 3 | Yes |
| 3 | 6 | 6 | — | — | — | Stop |

Trace:
1. `mid = 5` fails because only two windows are usable.
2. Move right: `lo = 6`.
3. `mid = 6` succeeds with exactly three usable windows.
4. Tighten left: `hi = 6`.
5. `lo == hi == 6`, so the minimum valid launch power is `6`.

The `windows` values matter only for preserving order; the feasibility count is entirely driven by `power`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n log M)`, where `M = max(power) - min(power) + 1`, or equivalently `O(n log V)` for value range `V`. Each binary-search step performs one linear scan of `power`. For `n = 10^6`, this remains practical because `log2(10^9) ≈ 30`; for `n = 10^9`, the scan cost dominates and the problem becomes I/O-bound regardless of the search strategy.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only search bounds, a midpoint, and a running counter. No extra arrays, heaps, or maps are required. Space cannot meaningfully be reduced further; the only trade-off is streaming input, which preserves constant auxiliary memory.

## 💡 Key Takeaways
- If the question asks for the minimum threshold, capacity, or limit that makes a condition true, check for a monotonic predicate and consider binary search on the answer.
- When a higher parameter value can only add valid choices and never remove them, you likely have the exact shape needed for lower-bound binary search.
- Do not over-model the timestamps: `windows` is strictly increasing, but in this formulation it contributes ordering only, not feasibility logic.
- Use lower-bound updates correctly: on success set `hi = mid`, on failure set `lo = mid + 1`; using `hi = mid - 1` here is a classic off-by-one bug.
- In production systems, reframing optimization as repeated feasibility checks often yields simpler invariants, better testability, and more predictable scaling behavior than direct constructive search.

## 🚀 Variations & Further Practice
- Require transmitting to **`k` consecutive usable windows** instead of any `k` windows. The monotonic predicate still holds, but the feasibility check becomes a longest-streak scan rather than a simple count.
- Add a **budget on total skipped windows** before the `k`-th success. Feasibility remains monotonic, but the check must track both successes and skips, introducing a tighter state invariant.
- Allow **multiple transmitters** with per-transmitter limits and assignment rules. The core threshold search may survive, but the predicate becomes a matching or scheduling problem rather than a linear pass.