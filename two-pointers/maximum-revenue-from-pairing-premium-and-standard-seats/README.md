# Maximum Revenue from Pairing Premium and Standard Seats

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Greedy, Binary Search

---

## 🗂 Problem Overview
Given two sorted arrays, `premium` and `standard`, choose exactly `k` one-to-one pairs such that each chosen customer can afford the chosen premium seat: `standard[j] >= premium[i]`. Each pair contributes revenue `standard[j] - premium[i]`, and the goal is to maximize total revenue. Return that maximum total, or `-1` if forming exactly `k` valid pairs is impossible. The difficulty is that local greedy choices can destroy future feasibility or reduce global profit under the matching constraint.

## 🌍 Engineering Impact
This pattern shows up in allocation systems where eligibility and margin must both be optimized under one-to-one constraints: ad serving with reserve prices, cloud capacity upgrades, marketplace order matching, ticketing and yield management, and batch schedulers assigning premium resources to jobs with minimum bid thresholds. At scale, naive greedy assignment either leaves money on the table or fails feasibility late in the pipeline. The useful abstraction is monotone feasibility over sorted inputs: once recognized, it enables `O((n+m) log V)` or `O((n+m) log n)` designs instead of combinatorial matching, which is the difference between an online service and an offline analysis job.

## 🔍 Problem Statement
You are given two sorted integer arrays:

- `premium[i]`: minimum payment required by the `i`-th premium seat holder
- `standard[j]`: amount the `j`-th standard customer will pay for an upgrade

A pair `(i, j)` is valid if `standard[j] >= premium[i]`. Each premium seat and each standard customer may be used at most once. You must form **exactly** `k` valid pairs and maximize:

`sum(standard[j] - premium[i])`

Return the maximum total revenue, or `-1` if exactly `k` valid pairs cannot be formed.

Constraints:

- `1 <= premium.length, standard.length <= 2 * 10^5`
- `0 <= premium[i], standard[j] <= 10^9`
- both arrays are sorted in non-decreasing order
- `1 <= k <= min(premium.length, standard.length)`

Examples:

- `premium = [2,4,7], standard = [5,8,10], k = 2` → `9`
- `premium = [3,6,9], standard = [4,5,7], k = 2` → `-1`

The scale rules out DP over pair counts or general weighted bipartite matching.

## 🪜 How to Solve This
1. Start from the objective: total revenue is  
   `sum(chosen standard) - sum(chosen premium)`.  
   So we want expensive customers and cheap premium seats — but only if they can still be matched feasibly.

2. Notice the one-to-one constraint on **sorted** arrays. That is the signal for a two-pointer or monotone matching argument, not arbitrary graph matching.

3. Reframe the problem: if we decide which `k` customers to use and which `k` premium seats to use, the best feasible matching between those sorted subsets is order-preserving. Crossing pairs never help.

4. That suggests a structural optimum: use the `k` largest customers whenever possible, because replacing any chosen customer with a larger unused one can only increase revenue.

5. Symmetrically, among premium seats that can be matched to those customers, we want the cheapest feasible `k` seats.

6. The remaining question is feasibility under exact cardinality. This becomes a monotone check: can the `k` largest customers cover some `k` premium seats? A two-pointer scan answers that.

7. Once feasible, greedily match those `k` largest customers against the cheapest premium seats they can support, preserving future feasibility. The sorted structure makes this optimal.

## 🧩 Algorithm Walkthrough
1. **Check whether exactly `k` pairs are possible at all.**  
   Use two pointers from the start of both arrays to compute the maximum number of valid matches. Whenever `standard[j] >= premium[i]`, match them and advance both; otherwise advance `j`.  
   This is the standard **Two Pointers greedy feasibility** pattern. It maximizes match count because each premium seat is assigned the smallest customer that can satisfy it, preserving larger customers for harder seats.

2. **If the maximum match count is less than `k`, return `-1`.**  
   This is a hard feasibility boundary; no revenue optimization matters after that.

3. **Select the `k` largest customers.**  
   Let `B = standard[m-k ... m-1]`. Any optimal solution can be transformed to use these customers: replacing a chosen customer with a larger unused one never breaks validity if we keep pair order, and strictly improves or preserves revenue.

4. **Find the cheapest `k` premium seats that can be matched to `B`.**  
   Scan `premium` from left to right and `B` from left to right. Whenever `B[j] >= premium[i]`, take `premium[i]` into the solution and advance both pointers. Otherwise advance `j` until a large enough customer is found.  
   Invariant: after choosing `t` seats, they are the cheapest possible `t` premium seats that can be matched to the first `t` usable customers in `B`.

5. **Compute revenue.**  
   Since both chosen sets are sorted, pair them in order. The exchange argument is standard: if `p1 <= p2` and `b1 <= b2`, then crossing pairs cannot improve feasibility or profit. Revenue is  
   `sum(B) - sum(chosenPremium)`.

6. **Why this works.**  
   The problem looks like weighted matching, but sorted inputs collapse it into a monotone matching problem. The right abstraction is **greedy + two pointers on ordered feasible pairs**, not general graph optimization.

## 📊 Worked Example
Take `premium = [2,4,7]`, `standard = [5,8,10]`, `k = 2`.

Use the `k` largest customers: `B = [8,10]`.

Now find the cheapest `2` premium seats matchable to `B`:

| Step | `i` | `j` | `premium[i]` | `B[j]` | Action | Chosen Seats |
|---|---:|---:|---:|---:|---|---|
| 1 | 0 | 0 | 2 | 8 | match | [2] |
| 2 | 1 | 1 | 4 | 10 | match | [2,4] |

We already have `k = 2` seats, so stop.

Revenue:

- chosen customers: `[8,10]`, sum = `18`
- chosen premium seats: `[2,4]`, sum = `6`

Maximum revenue = `18 - 6 = 12`.

Order pairing is `(2,8)` and `(4,10)`. Both are valid, one-to-one, and no other exact-2 pairing yields more because any alternative either uses a smaller customer or a more expensive premium seat.

## ⏱ Complexity Analysis

### Time Complexity
`O(n + m)` where `n = premium.length` and `m = standard.length`. We do at most two linear scans with two pointers plus constant-time arithmetic. At `10^6` elements this is routine; at `10^9`, the bottleneck is memory and I/O, not algorithmic overhead.

### Space Complexity
`O(1)` auxiliary space if sums are accumulated on the fly over index ranges. The input arrays dominate memory. You can materialize the chosen suffix of `standard`, but that is unnecessary; index arithmetic preserves constant extra space.

## 💡 Key Takeaways
- If inputs are sorted and matching is one-to-one with a threshold condition like `a <= b`, think ordered greedy matching before considering general bipartite matching.
- If the objective decomposes into `sum(selected right side) - sum(selected left side)`, look for an exchange argument that isolates “largest feasible rights” and “smallest feasible lefts.”
- The main trap is optimizing revenue before proving exact-`k` feasibility; always compute or preserve the maximum possible match count first.
- Be careful with pointer movement when a customer cannot satisfy the current premium seat: advancing the wrong side silently drops feasible future matches.
- In production allocation systems, sorted monotone structure often turns an apparently global optimization problem into a linear-time admission-and-assignment pass.

## 🚀 Variations & Further Practice
- Add a per-pair transaction fee or nonlinear revenue function; now `sum(standard) - sum(premium)` no longer fully characterizes the objective, so the simple exchange argument may fail.
- Allow unsorted streaming arrivals with online decisions; the hard part becomes maintaining feasibility and profit under partial information rather than exploiting static order.
- Require maximizing revenue for **at most** `k` pairs with penalties for unused premium seats; this introduces a stop/continue decision and changes the monotone structure of the optimal set.