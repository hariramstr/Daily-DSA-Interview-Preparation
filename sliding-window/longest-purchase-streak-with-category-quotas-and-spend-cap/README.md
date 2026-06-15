# Longest Purchase Streak With Category Quotas and Spend Cap

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Sliding Window &nbsp;|&nbsp; **Tags:** Sliding Window, Hash Map, Two Pointers

---

## 🗂 Problem Overview
Given parallel arrays `cost` and `category`, find the maximum length of a contiguous purchase streak whose total spend is at most `budget` and whose category frequencies satisfy every minimum in `quota`. Return that maximum length, or `0` if no such streak exists. The challenge is that `n` and the number of quota categories can both reach `2 * 10^5`, so brute-force enumeration of subarrays or repeated full quota checks is not viable.

## 🌍 Engineering Impact
This pattern shows up in streaming eligibility checks, fraud windows, ad-delivery pacing, session analytics, and promotion engines where a contiguous event window must satisfy both resource caps and composition constraints. At scale, naive rescans turn linear ingestion into quadratic latency spikes, which is unacceptable in real-time pipelines or customer-facing ranking systems. The right sliding-window design gives predictable O(n) behavior, bounded memory, and a clean separation between window state and validation logic. That matters when the same primitive is embedded in Kafka consumers, Flink operators, online feature computation, or campaign-rule evaluators processing millions of events per minute.

## 🔍 Problem Statement
You are given `n` chronological purchases, with `cost[i]` and `category[i]` describing the `i`-th purchase. Also given are a spend cap `budget` and a dictionary `quota`, where each `quota[c]` is the minimum number of purchases from category `c` required inside a valid contiguous streak.

A streak is eligible iff:

1. `sum(cost[l..r]) <= budget`
2. For every category `c` in `quota`, the window contains at least `quota[c]` occurrences of `c`

Return the maximum valid window length, else `0`.

Constraints force an almost-linear solution:

- `1 <= n <= 2 * 10^5`
- `1 <= cost[i] <= 10^9`
- `1 <= budget <= 10^15`
- up to `2 * 10^5` distinct quota categories
- quota categories may not appear in the purchase list

Examples:

- `cost=[4,2,3,1,2,5,1]`, `category=["grocery","book","grocery","toy","book","grocery","toy"]`, `budget=10`, `quota={"grocery":2,"book":1}` → `4`
- `cost=[2,2,2,2,2]`, `category=["a","b","a","c","b"]`, `budget=6`, `quota={"a":1,"b":2}` → `0`

## 🪜 How to Solve This
1. Read the constraints → contiguous subarray + maximize length + positive costs strongly suggests a sliding window.

2. Notice there are **two independent validity conditions**:
   - total cost must stay within `budget`
   - category counts must satisfy all quotas

3. For the budget condition, positive `cost[i]` means that when the window gets too expensive, moving the left pointer right is the only way to restore validity. That monotonicity is exactly what two pointers exploit.

4. For the quota condition, recomputing “do we satisfy every category?” on every step would be too expensive. Instead, track counts only for quota categories and maintain a single integer: how many required categories are currently satisfied.

5. Expand the right pointer one purchase at a time → update running sum and category count.

6. Shrink from the left only while over budget. Once the window is budget-valid again, quota satisfaction can be checked in O(1) via the satisfied-category counter.

7. Any window that is both budget-valid and quota-valid is a candidate answer; update the maximum length.

The key realization: budget enforcement is handled structurally by the window, while quota enforcement is handled incrementally by bookkeeping.

## 🧩 Algorithm Walkthrough
1. **Initialize window state**  
   Use the **Sliding Window / Two Pointers** pattern with `left = 0`, `sumCost = 0`, a hash map `have` for counts of quota categories seen in the current window, and `met = 0` for how many quota categories currently meet their required minimum. Let `need = quota.size()`.

2. **Expand the right edge**  
   For each index `right` from `0` to `n - 1`, add `cost[right]` to `sumCost`. If `category[right]` exists in `quota`, increment `have[category[right]]`. If that count just reached `quota[category[right]]`, increment `met`.  
   **Invariant:** `have` and `sumCost` exactly describe window `[left, right]`.

3. **Restore budget validity**  
   While `sumCost > budget`, remove the purchase at `left` and advance `left`. If the removed category is quota-tracked and its count drops from exactly `quota[c]` to `quota[c] - 1`, decrement `met`. Also subtract `cost[left]` from `sumCost`.  
   **Why correct:** costs are positive, so shrinking is the only way to reduce spend; once `sumCost <= budget`, every earlier `left` would still be over budget.

4. **Check quota validity in O(1)**  
   After budget repair, the window is eligible iff `met == need`. No full scan of `quota` is needed.  
   **Invariant:** `met == need` means every required category count is currently at or above its quota.

5. **Update answer**  
   If both conditions hold, set `ans = max(ans, right - left + 1)`.

6. **Why this abstraction fits**  
   Two pointers work because the budget predicate is monotone under left-shrinking with positive costs. Hash-map counting works because quota validation depends only on frequency state, not order inside the window. Together they yield linear traversal with bounded per-step work.

## 📊 Worked Example
Use Example 1:

`cost = [4,2,3,1,2,5,1]`  
`category = [grocery, book, grocery, toy, book, grocery, toy]`  
`budget = 10`, `quota = {grocery:2, book:1}`

| right | add              | sum | left after shrink | counts (g,b) | met | valid? | best |
|------:|------------------|----:|------------------:|--------------|----:|--------|-----:|
| 0     | grocery, 4       | 4   | 0                 | (1,0)        | 0   | no     | 0    |
| 1     | book, 2          | 6   | 0                 | (1,1)        | 1   | no     | 0    |
| 2     | grocery, 3       | 9   | 0                 | (2,1)        | 2   | yes    | 3    |
| 3     | toy, 1           | 10  | 0                 | (2,1)        | 2   | yes    | 4    |
| 4     | book, 2          | 12  | 1                 | (1,2)        | 1   | no     | 4    |
| 5     | grocery, 5       | 13  | 4                 | (1,1)        | 1   | no     | 4    |
| 6     | toy, 1           | 9   | 4                 | (1,1)        | 1   | no     | 4    |

Best eligible length is `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time. Each purchase enters the window once when `right` advances and leaves at most once when `left` advances. Hash-map updates and quota-satisfaction transitions are O(1) expected. At `10^6` elements this is still practical; at `10^9`, the bottleneck becomes I/O and memory bandwidth rather than asymptotic structure.

### Space Complexity
`O(k)` where `k` is the number of distinct categories present in `quota`, due to the frequency map and quota map. This is already near-minimal for exact validation. You could compress category strings to integer IDs to reduce constant factors, but not asymptotic space.

## 💡 Key Takeaways
- If the problem asks for a **longest contiguous segment** under an additive cap with all-positive values, sliding window should be your first candidate.
- If validity depends on **meeting many frequency thresholds**, track a single “requirements satisfied” counter instead of rescanning the whole map.
- Be careful when shrinking: decrement `met` only when a category count falls from `quota[c]` to `quota[c] - 1`, not whenever it decreases.
- Update the answer only after restoring `sumCost <= budget`; checking before shrink produces invalid windows and off-by-one bugs.
- The production-grade insight is to convert expensive global validation into incremental state transitions so stream processing stays linear and predictable.

## 🚀 Variations & Further Practice
- Allow negative or zero `cost[i]`: the budget predicate is no longer monotone, so standard two pointers break and you need prefix sums plus a more complex data structure.
- Replace minimum quotas with exact quotas or upper bounds per category: now both expansion and shrink can violate constraints in both directions, which changes the window-validity logic.
- Ask for the number of eligible streaks instead of the longest one: same primitives, but counting all valid windows requires reasoning about how many left boundaries remain feasible for each right boundary.