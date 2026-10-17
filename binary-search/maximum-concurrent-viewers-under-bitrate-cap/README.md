# Maximum Concurrent Viewers Under Bitrate Cap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Sorting, Greedy

---

## 🗂 Problem Overview
Given an unsorted array `limits` and a bandwidth budget `B`, choose one integer global bitrate `b >= 1` so that every viewer with `limits[i] >= b` can join. The total bandwidth used is `b * count(b)`, where `count(b)` is the number of eligible viewers. Return the maximum possible `count(b)` over all feasible bitrates. The challenge is that both `limits[i]` and `B` are large, so scanning all possible bitrates is not viable.

## 🌍 Engineering Impact
This pattern shows up anywhere a single global operating point determines how many clients can be served under a shared resource cap: adaptive streaming control planes, admission control in multi-tenant services, batch sizing under memory ceilings, and rate selection in distributed gateways. At scale, brute-force exploration across a huge parameter range collapses under latency and cost constraints. The useful abstraction is monotonic feasibility: once you can test “can we serve at least `k` clients?” efficiently, binary search turns an intractable search space into a predictable logarithmic control loop. That enables deterministic capacity decisions instead of heuristic tuning.

## 🔍 Problem Statement
You are given:

- `limits`, where `limits[i]` is the maximum bitrate viewer `i` can support
- `B`, the total bandwidth budget

For an integer bitrate `b >= 1`:

- `count(b) = number of i such that limits[i] >= b`
- `b` is feasible if `b * count(b) <= B`

Return the maximum feasible value of `count(b)`.

Constraints:

- `1 <= n <= 2 * 10^5`
- `1 <= limits[i] <= 10^12`
- `1 <= B <= 10^18`

Examples:

- `limits = [5, 8, 6, 3], B = 12` → `4`  
  Choosing `b = 3` serves all 4 viewers, and `3 * 4 = 12`.

- `limits = [10, 10, 2, 2, 2], B = 9` → `5`  
  Choosing `b = 1` serves all 5 viewers, and `1 * 5 = 5`.

The key constraint is the huge bitrate domain: you need to search over outcomes, not enumerate candidate bitrates directly.

## 🪜 How to Solve This
1. Start from the objective: maximize how many viewers can join, not maximize bitrate. That suggests searching over viewer count rather than over `b`.

2. Ask: if I want to serve exactly `k` viewers, what is the cheapest bitrate that makes that possible? Since bitrate must be at least `1`, the cheapest possible choice is always `b = 1`.

3. Then feasibility for `k` viewers becomes surprisingly simple: can at least `k` viewers support bitrate `1`, and is `1 * k <= B`? Because every `limits[i] >= 1` by constraint, the first condition is always true for any `k <= n`.

4. So the real constraint collapses to `k <= B` and `k <= n`. That means the answer is `min(n, B)`.

5. If you insist on deriving it through the sorted/binary-search lens: after sorting, for any chosen `b = 1`, `count(1) = n`, and since lower bitrate always weakly increases eligible viewers while minimizing cost, no bitrate above `1` can improve the viewer count.

6. The important reasoning move is recognizing that the optimization variable and the feasibility variable are misaligned; once you inspect the minimum valid bitrate, the hard-looking search disappears.

## 🧩 Algorithm Walkthrough
1. **Observe the lower bound on bitrate.**  
   The problem requires an integer bitrate `b >= 1`. Since all `limits[i] >= 1`, every viewer can support bitrate `1`.  
   **Invariant:** bitrate `1` is always valid from the viewer-capability perspective.

2. **Evaluate the cheapest possible global configuration.**  
   At `b = 1`, the platform serves all `n` viewers, and total bandwidth is `1 * n = n`.  
   **Why this matters:** any higher bitrate can only keep the same or fewer viewers, while costing at least as much per served viewer.

3. **Compare against the budget.**  
   - If `n <= B`, then `b = 1` is feasible and serves all viewers, so the answer is `n`.
   - If `n > B`, then no configuration can serve more than `B` viewers, because each served viewer consumes at least 1 unit of bandwidth.  
   **Invariant:** every feasible solution serving `k` viewers must satisfy `k <= B`.

4. **Conclude the optimum.**  
   The maximum feasible number of viewers is therefore `min(n, B)`.  
   This is a **greedy lower-bound argument**, not actually a binary-search problem once the constraints are read carefully. The right abstraction is: minimize per-unit cost first, then maximize cardinality under budget.

5. **Why this is correct globally.**  
   Any solution serving `k` viewers uses bandwidth `b * k` with `b >= 1`, so usage is at least `k`. Therefore `k > B` is impossible. Meanwhile, bitrate `1` achieves exactly `min(n, B)` viewers whenever capped by either population or budget. That matches the upper bound, so it is optimal.

## 📊 Worked Example
Take `limits = [5, 8, 6, 3]`, `B = 12`.

| Step | Value |
|---|---|
| `n` | `4` |
| Minimum allowed bitrate | `1` |
| Viewers eligible at `b = 1` | `4` |
| Bandwidth used at `b = 1` | `1 * 4 = 4` |
| Is `4 <= 12`? | Yes |

So all 4 viewers can be served immediately at bitrate `1`.

To see why no more analysis is needed:

1. Any feasible plan serving `k` viewers must spend at least `k` bandwidth, because `b >= 1`.
2. Here, `B = 12`, so the budget does not constrain us below `n = 4`.
3. Since `b = 1` already serves everyone, the optimum is `4`.

This also exposes why the problem statement’s first example should return `4`, not `3`: the cheapest legal bitrate dominates all higher choices when the objective is viewer count.

## ⏱ Complexity Analysis
### Time Complexity
`O(1)` if you use the direct observation, since the result is simply `min(n, B)`. There is no need to sort or binary search. At `10^6` or `10^9` scale, this distinction matters: constant-time logic eliminates both CPU cost and implementation surface area.

### Space Complexity
`O(1)`. The algorithm stores only `n`, `B`, and the final minimum. There is no auxiliary structure. Space cannot be meaningfully reduced further; the only trade-off is whether you even materialize the full input array when streaming input is available.

## 💡 Key Takeaways
- If the objective is to maximize count under `cost = value * count`, check the minimum legal `value` first; that often collapses the search space.
- When every item satisfies the threshold at the minimum allowed setting, sorting and binary search are red flags for overengineering.
- The main trap is missing that `b = 1` is always legal and always minimizes bandwidth per served viewer.
- Another easy mistake is trusting example reasoning over the formal constraints; here the provided narrative implies a harder problem than the actual one.
- In production capacity planning, lower-bound arguments often beat sophisticated search: prove an upper bound, then show a simple configuration attains it.

## 🚀 Variations & Further Practice
- Require `b >= L` for some global lower bound `L > 1`. Now feasibility becomes nontrivial, and sorting plus binary search over achievable viewer counts becomes useful.
- Allow per-viewer bandwidth weights, so serving viewer `i` at bitrate `b` costs `w[i] * b`. The uniform lower-bound shortcut disappears; you need selection logic over weighted subsets.
- Maximize bitrate subject to serving at least `k` viewers within budget. This flips the optimization target and restores a genuine monotonic binary-search problem over bitrate.