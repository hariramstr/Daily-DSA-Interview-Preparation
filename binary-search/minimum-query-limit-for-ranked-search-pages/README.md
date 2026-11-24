# Minimum Query Limit for Ranked Search Pages

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Prefix Sums, Sorting

---

## 🗂 Problem Overview
Given a fixed-ranked array `scores[1..n]` and analyst requirements `[k, t]`, find the smallest prefix length `L` such that within the first `L` documents there are at least `k` scores greater than or equal to `t` for every requirement. Return `-1` if no prefix works. The challenge is scale: both documents and requirements can reach `2e5`, so checking every prefix against every requirement is too expensive. The key property is monotonicity: once a prefix works, every larger prefix also works.

## 🌍 Engineering Impact
This pattern shows up anywhere you need the earliest point at which a cumulative system state satisfies multiple SLA-style constraints. Search ranking is the direct analog: how deep must retrieval go before enough high-quality results appear? The same shape appears in streaming pipelines deciding watermark advancement, observability systems checking alert thresholds over prefixes of events, and ad-serving or recommendation systems validating exposure guarantees over ranked inventories. Without exploiting monotonicity plus offline preprocessing, implementations degrade into repeated scans, blowing latency budgets and making “what is the minimum sufficient frontier?” queries operationally expensive.

## 🔍 Problem Statement
You are given `n` documents in fixed order and an array `scores`, where `scores[i]` is the relevance score of document `i`. You are also given `m` requirements, each of the form `[k, t]`: among the first `L` documents, at least `k` must have score `>= t`.

Return the minimum `L` in `[1, n]` that satisfies all requirements simultaneously, or `-1` if no such prefix exists.

Constraints:
- `1 <= n, m <= 200000`
- `1 <= scores[i], t <= 10^9`
- `1 <= k <= n`

Examples:

- `scores = [5, 1, 7, 3, 9, 6]`
- `requirements = [[2, 6], [3, 5]]`
- Output: `5`

- `scores = [2, 2, 2, 2]`
- `requirements = [[1, 3]]`
- Output: `-1`

The decisive constraint is size: an `O(nm)` or even `O(nm / word_size)` approach is dead on arrival.

## 🪜 How to Solve This
1. Read the requirement carefully → for a fixed prefix `L`, each condition asks for a count of values `>= t` inside `scores[1..L]`.

2. Notice the monotonicity → if prefix `L` satisfies all requirements, then any larger prefix also satisfies them, because counts inside the prefix never decrease.

3. Monotonic answer usually suggests binary search on `L`.

4. That leaves the hard part: how to check one candidate `L` efficiently. A naive check scans the prefix for every requirement, which is too slow.

5. Reframe the check: for each threshold `t`, compute how many values in the first `L` are `>= t`. Then every requirement with that threshold becomes a simple comparison against its required `k`.

6. Since thresholds vary, compress the work offline: sort scores in the prefix by value contribution, sort unique thresholds descending, and sweep once while counting how many prefix scores meet each threshold.

7. Precompute, for each threshold, the maximum `k` demanded among requirements using that threshold. Then a candidate `L` is valid iff the prefix count for every threshold is at least that maximum.

8. Binary search the smallest valid `L`.

## 🧩 Algorithm Walkthrough
1. **Normalize requirements by threshold.**  
   Build a map `need[t] = max k among requirements with threshold t`. This is correct because for the same `t`, only the strongest count requirement matters. The invariant is: satisfying `need[t]` automatically satisfies all original requirements with threshold `t`.

2. **Extract and sort unique thresholds descending.**  
   This enables an offline sweep. We only care about thresholds that actually appear in requirements, not arbitrary score values.

3. **Define a feasibility check for a prefix length `L`.**  
   Take the first `L` scores, sort them descending, and sweep thresholds descending. Maintain pointer `p` over sorted prefix scores and a counter `cnt` of how many seen scores are `>= current threshold`.  
   Invariant: after processing threshold `t`, `cnt` equals the number of prefix scores `>= t`.

4. **Validate all thresholds during the sweep.**  
   For each threshold `t`, compare `cnt` with `need[t]`. If `cnt < need[t]`, prefix `L` fails immediately. This is correct because the sweep computes exact counts for every required threshold.

5. **Use Binary Search on Answer.**  
   Search `L` in `[1, n]`. If `check(mid)` succeeds, move left; otherwise move right. This works because feasibility is monotone in `L`.

6. **Return the leftmost feasible `L`, or `-1`.**  
   First test whether `check(n)` succeeds at all. If not, no prefix can satisfy the requirements.

This is a **Binary Search on Answer + Offline Threshold Sweep** pattern. Binary search handles the monotone frontier; sorting and sweeping make each feasibility check tractable.

## 📊 Worked Example
Use `scores = [5, 1, 7, 3, 9, 6]`, `requirements = [[2, 6], [3, 5]]`.

Preprocess:
- `need[6] = 2`
- `need[5] = 3`
- thresholds descending: `[6, 5]`

Check `L = 4`:
- prefix = `[5, 1, 7, 3]`
- sorted desc = `[7, 5, 3, 1]`

| Threshold `t` | Scores `>= t` in prefix | Required `need[t]` | Pass? |
|---|---:|---:|---|
| 6 | 1 (`7`) | 2 | No |
| 5 | 2 (`7,5`) | 3 | No |

So `L=4` fails.

Check `L = 5`:
- prefix = `[5, 1, 7, 3, 9]`
- sorted desc = `[9, 7, 5, 3, 1]`

| Threshold `t` | Scores `>= t` in prefix | Required `need[t]` | Pass? |
|---|---:|---:|---|
| 6 | 2 (`9,7`) | 2 | Yes |
| 5 | 3 (`9,7,5`) | 3 | Yes |

`L=5` works, and binary search confirms it is the minimum.

## ⏱ Complexity Analysis
### Time Complexity
With the straightforward feasibility check described here, each check sorts a prefix in `O(L log L)`, giving `O(n log^2 n)` worst-case across binary search. In practice this is acceptable for `2e5`, but not for `10^6+` repeated online queries. The dominant cost is sorting during each feasibility test.

### Space Complexity
`O(n + m)` in the worst case: prefix storage or sorted working memory plus the threshold-to-required-count map. Space is owned mainly by sorted arrays and requirement aggregation. You can reduce some copying with in-place or indexed views, but usually at the cost of code complexity.

## 💡 Key Takeaways
- If the question asks for the minimum prefix, time, capacity, or limit where all constraints become true, check for a monotone predicate and binary-search the answer.
- If many constraints differ only by threshold, aggregate by threshold first; repeated conditions often collapse to a single strongest requirement.
- Be careful with `>= t` versus `> t`; one comparator bug invalidates every prefix count.
- Binary search boundaries are easy to get wrong here: search on `[1, n]`, but still explicitly test whether `n` is feasible before returning an answer.
- In production systems, the scalable move is often not “optimize the loop” but “change the query shape” through offline aggregation and monotone search.

## 🚀 Variations & Further Practice
- Support many online queries over changing requirement sets. The twist is moving from one offline solve to reusable indexing structures such as Fenwick trees, wavelet trees, or persistent segment trees.
- Allow score updates between queries. The harder part is preserving fast threshold-count queries over prefixes under mutation, which pushes the design toward balanced trees or segment trees with order statistics.
- Replace “all requirements must hold” with “maximize the number of satisfied requirements under a budgeted `L`.” The monotone feasibility predicate disappears, so binary search no longer applies directly.