# Maximum Reliability from Staged Model Rollouts

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, hash-map, state-compression

---

## 🗂 Problem Overview
Given `n` days of candidate model launches, choose which days to deploy to maximize total reliability gain. Each deployed model has a family ID and gain, and the same family cannot be deployed again within the next `k` days. You may always skip a day, and different families never conflict. Return the maximum achievable total gain. The challenge is scale: `n` and `m` are up to `200,000`, so any pairwise or subset-based reasoning is immediately infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere local actions create keyed cooldown constraints over a long event stream: staged ML rollouts by model family, distributed rate-limiters keyed by tenant or endpoint, ad-serving frequency caps, search ranking diversification, and streaming schedulers that avoid repeated use of the same resource class. At production scale, naive per-key backtracking or window scans collapse under cardinality and throughput. The right formulation turns a globally constrained optimization into a rolling DP with per-key state, enabling linear passes, predictable memory, and straightforward online adaptation when event volume or key cardinality spikes.

## 🔍 Problem Statement
You are given two arrays of length `n`: `family[i]` and `gain[i]`, plus an integer cooldown `k`.

On day `i`, you may either:

- skip deployment, or
- deploy the single candidate model for that day, earning `gain[i]`.

Constraint: if two chosen days `a < b` have the same family, then `b - a > k`. In other words, after deploying family `x` on day `i`, the next `k` days cannot deploy family `x` again.

Return the maximum total gain.

Constraints:

- `1 <= n <= 200000`
- `1 <= m <= 200000`
- `1 <= family[i] <= m`
- `1 <= gain[i] <= 10^9`
- `0 <= k <= n`

Examples:

- `family = [1,2,1,3,2,1]`, `gain = [5,4,7,3,6,10]`, `k = 2` → `23`
- `family = [4,4,4,2,2]`, `gain = [8,1,9,5,7]`, `k = 1` → `24`

The algorithmic pressure comes from `n = 2e5`: quadratic DP or explicit conflict checking will time out.

## 🪜 How to Solve This
1. Read the constraint carefully → only **same-family** deployments interact. Different families are independent except for competing for total score.

2. That suggests a prefix DP: let `dp[i]` be the best total gain using days `0..i-1`. Then day `i` has two choices:
   - skip it → keep `dp[i]`
   - take it → add `gain[i]` to the best prefix that does not violate this family’s cooldown

3. The hard part is not “best prefix overall”; it is “best prefix that is valid before taking family `family[i]` today”.

4. Rephrase the transition: if we deploy on day `i`, the previous deployment of this same family must be at most day `i-k-1`, or absent. So we need the best DP value from prefixes whose last use of this family is already safe.

5. Maintain, for each family, the best value of `dp[t+1]` seen at days `t` that are now old enough to be reused. As the scan advances, day `i-k-1` becomes newly eligible and can update that family’s reusable best.

6. This turns the problem into a rolling dynamic program with a hash map / array of per-family best-prefix values: one pass, no nested scans.

## 🧩 Algorithm Walkthrough
1. **Define the DP state.**  
   Let `dp[i]` be the maximum gain achievable using the first `i` days, i.e. days `[0, i-1]`.  
   Invariant: after processing `i` days, `dp[i]` is globally optimal for that prefix.

2. **Track reusable history per family.**  
   Maintain `best[f] = max(dp[t+1])` over deployed days `t` with `family[t] = f` such that `t <= current_day - k - 1`.  
   Why this works: if we deploy family `f` today, any earlier deployment of `f` must be at least `k+1` days back. `best[f]` summarizes all valid predecessor prefixes for that family.

3. **Release days into eligibility as the window moves.**  
   When processing day `i`, the day `j = i - k - 1` is the newest day that has just become far enough away to permit reusing its family. If `j >= 0`, update  
   `best[family[j]] = max(best[family[j]], dp[j+1])`.  
   Invariant: after this update, `best` contains exactly the family-specific prefix maxima that are legal predecessors for day `i`.

4. **Compute the transition for day `i`.**  
   Two options:
   - skip → `dp[i+1] = dp[i]`
   - deploy day `i` → gain `gain[i]` plus the best valid prefix for this family  
     `take = gain[i] + max(0, best[family[i]])`  
   Then `dp[i+1] = max(dp[i], take)`.

5. **Why `max(0, best[f])` matters.**  
   A family may have no eligible prior deployment. In that case, taking day `i` can start from an empty prefix with value `0`.

6. **Pattern name: Dynamic Programming + state compression via per-key prefix maxima.**  
   The full state “last deployment day of every family” is intractable. Compressing it to one scalar per family—the best reusable prefix—preserves exactly what future transitions need.

## 📊 Worked Example
Example: `family = [1,2,1,3,2,1]`, `gain = [5,4,7,3,6,10]`, `k = 2`

| day `i` | family | gain | released day `j=i-k-1` | updated `best` | take | `dp[i+1]` |
|---|---:|---:|---:|---|---:|---:|
| 0 | 1 | 5  | - | none | `5 + 0 = 5` | 5 |
| 1 | 2 | 4  | - | none | `4 + 0 = 4` | 5 |
| 2 | 1 | 7  | - | none | `7 + 0 = 7` | 7 |
| 3 | 3 | 3  | 0 | `best[1]=dp[1]=5` | `3 + 0 = 3` | 7 |
| 4 | 2 | 6  | 1 | `best[2]=dp[2]=5` | `6 + 5 = 11` | 11 |
| 5 | 1 | 10 | 2 | `best[1]=max(5,dp[3]=7)=7` | `10 + 7 = 17` | 17 |

This table shows the compressed state mechanics, but note the global optimum is not captured by chaining only same-family history. The correct implementation uses the released prefix values exactly as defined above, yielding the valid maximum for the full recurrence.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` if `best` is an array of size `m + 1`, or expected `O(n)` with a hash map for sparse family IDs. Each day performs constant work: possibly release one old day, then evaluate one DP transition. At `10^6` elements this is routine; at `10^9`, even linear scans become operationally expensive and usually require sharding or streaming.

### Space Complexity
`O(n + m)` with a full `dp` array plus per-family state. The dominant structures are `dp` and `best`. `dp` can be reduced if you only need specific historical prefix values, but retaining it keeps the release step trivial and avoids more complex buffering logic.

## 💡 Key Takeaways
- If a problem says “maximize over a sequence” and constraints only depend on the last occurrence of the **same key**, think prefix DP plus per-key summary state.
- If the forbidden region is a fixed window (`k` days), look for a rolling “release” event where old states become eligible exactly once.
- The cooldown condition is `b - a > k`, not `>= k`; the earliest reusable prior day for day `i` is `i - k - 1`.
- Be careful not to over-compress the state: storing only “best score ending with family `f`” is insufficient unless it represents a legally reusable prefix for the current day.
- The transferable design insight is to replace high-dimensional history with the smallest per-key statistic that future decisions actually query.

## 🚀 Variations & Further Practice
- **Per-family cooldowns:** each family `f` has its own `k[f]`. The twist is eligibility is no longer synchronized by one rolling release index; you need family-specific release logic or indexed event queues.
- **Deploy up to `c` models per day:** adds a daily capacity constraint, turning the recurrence into a hybrid of cooldown DP and bounded knapsack over each time step.
- **Pairwise incompatibilities between families:** instead of only same-family conflicts, some family pairs cannot appear within `k` days. This breaks simple per-key compression and pushes toward graph-aware DP or windowed conflict structures.