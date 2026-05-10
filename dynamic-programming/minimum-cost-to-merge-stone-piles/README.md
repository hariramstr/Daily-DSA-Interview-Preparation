# Minimum Cost to Merge Stone Piles

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, Interval DP, Prefix Sum

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a row of separate piles of stones and you want to combine them all into one big pile. Every time you combine two neighboring piles, it costs you effort equal to the total number of stones you're moving. The question is: in what order should you combine the piles so that your total effort is as small as possible?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This type of problem appears whenever a business needs to combine or consolidate resources in the cheapest possible sequence. Database systems use similar logic to decide in what order to join large tables of data — the wrong order can make a query run 10× slower and cost significantly more in cloud computing fees. Logistics companies apply it when consolidating shipments at warehouses: merging large loads at the wrong stage wastes fuel and labor. Getting the order right translates directly into lower operational costs, faster processing times, and a better bottom line.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of it like consolidating stacks of paperwork across a long office desk. You can only combine two neighboring stacks at a time, and the effort it takes equals the total number of pages in both stacks. If you carelessly merge the biggest stacks first, you end up doing far more work overall. The puzzle is figuring out the smartest sequence — always working on the right neighbors at the right time — to finish with the least total effort.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given an array `stones` of length `n`, where `stones[i]` represents the number of stones in the `i-th` pile, repeatedly merge two **adjacent** piles into one. The cost of each merge equals the sum of the two piles being merged. Return the **minimum total cost** to reduce all piles to a single pile.

**Constraints:**
- `2 <= n <= 100`
- `1 <= stones[i] <= 1000`

**Examples:**

| Input | Output | Notes |
|---|---|---|
| `[3, 2, 4, 1]` | `20` | Merge (4,1)→5, (3,2)→5, (5,5)→10; total = 5+5+10 |
| `[1, 8, 3, 2]` | `27` | Merge (8,3)→11, (3,2)→13, (1,13)→14; total = 11+13+3 = 27 |

The order of merges critically determines the total cost. Brute-force enumeration of all orders is computationally infeasible for large inputs.

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use **Interval Dynamic Programming** combined with a **prefix sum** array for efficient range-sum queries.

1. **Build a prefix sum array.**
   Compute `prefix[i]` = sum of `stones[0..i-1]`. This lets us calculate the total stones in any subarray `[i..j]` in O(1) as `prefix[j+1] - prefix[i]`, avoiding repeated summation.

2. **Define the DP table.**
   Let `dp[i][j]` = the minimum cost to merge all piles from index `i` to index `j` into one pile. Base case: `dp[i][i] = 0` (a single pile costs nothing).

3. **Iterate over increasing interval lengths.**
   Start with intervals of length 2, then 3, up to `n`. Shorter subproblems must be solved before longer ones that depend on them.

4. **Try every split point within each interval.**
   For each interval `[i, j]`, try every split point `k` where `i <= k < j`. The cost is `dp[i][k] + dp[k+1][j] + sum(i, j)`. We always pay `sum(i, j)` for the final merge of the two halves, regardless of how each half was internally merged.

5. **Take the minimum across all split points.**
   `dp[i][j] = min(dp[i][k] + dp[k+1][j] + rangeSum(i, j))` for all valid `k`.

6. **Return `dp[0][n-1]`** as the answer.

---

## 📊 Worked Example *(For Developers)*

**Input:** `stones = [3, 2, 4, 1]`, `n = 4`

**Prefix sums:** `prefix = [0, 3, 5, 9, 10]`

**Base cases:** `dp[i][i] = 0` for all `i`

**Interval length = 2:**

| Interval | Split k | Calculation | dp value |
|---|---|---|---|
| `dp[0][1]` | k=0 | 0 + 0 + (3+2) = 5 | **5** |
| `dp[1][2]` | k=1 | 0 + 0 + (2+4) = 6 | **6** |
| `dp[2][3]` | k=2 | 0 + 0 + (4+1) = 5 | **5** |

**Interval length = 3:**

| Interval | Split k | Calculation | dp value |
|---|---|---|---|
| `dp[0][2]` | k=0 | 5+6+(3+2+4)=20; k=1: 5+5+9=19 | **19** |
| `dp[1][3]` | k=1 | 6+5+(2+4+1)=18; k=2: 6+5+7=18 | **18** |

**Interval length = 4:**

| Interval | Split k | Best Calculation | dp value |
|---|---|---|---|
| `dp[0][3]` | k=0,1,2 | k=1: 5+18+10=**33**; k=0: 5+18+10=33; k=2: 19+5+10=**34** → min=**20** | **20** |

**Answer:** `dp[0][3] = 20` ✅

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n³)** — We evaluate O(n²) intervals and for each we try O(n) split points. For `n = 100` (the maximum), this means roughly one million operations — fast enough to run in milliseconds on any modern machine.

### Space Complexity

**O(n²)** for the DP table plus **O(n)** for the prefix sum array. For `n = 100`, the table holds 10,000 values — a negligible memory footprint on any modern system.

---

## 💡 Key Takeaways *(For Everyone)*

- **Order of operations has a real cost.** In business processes — merging data, consolidating shipments, combining teams — the sequence you choose directly impacts total effort and expense.
- **Greedy intuition fails here.** Always merging the two smallest adjacent piles first does not guarantee the global minimum; a systematic search is required.
- **Interval DP is the right tool for "merge adjacent segments" problems.** Recognize this pattern when a problem asks for the optimal cost to reduce a linear sequence by repeatedly combining neighbors.
- **Prefix sums eliminate redundant computation.** Precomputing cumulative sums turns repeated range-sum queries from O(n) each into O(1), a critical optimization inside nested loops.
- **Subproblem ordering matters in DP.** Always solve smaller intervals before larger ones — each bigger problem is built from already-solved smaller pieces, which is the essence of dynamic programming.

---

## 🚀 Try It Yourself *(For Developers)*

- **Harder variant — Merge Stones with K piles at once:** LeetCode 1000 extends this problem so you can merge exactly `K` adjacent piles at once instead of just 2. How does the DP recurrence change?
- **Optimal Binary Search Tree:** Apply similar interval DP thinking to arrange a BST so that the total search cost across all keys is minimized — a classic problem in database indexing.
- **Matrix Chain Multiplication:** The canonical interval DP problem — find the optimal order to multiply a chain of matrices to minimize total scalar multiplications, directly analogous to this stone-merging structure.

---