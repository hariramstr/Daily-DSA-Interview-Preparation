# Minimum Cost to Build a Palindrome from Fragments

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** dynamic-programming, string, subsequence

---

## 🗂 Problem Overview
Given a string `s` and a parallel `cost[]`, choose a **non-empty palindromic subsequence** whose total keep-cost is minimum. You may delete any characters for free, but every retained character contributes its cost. The output is that minimum total cost. The challenge is that local choices are misleading: matching equal characters can extend a palindrome, but the globally cheapest answer may be a shorter subsequence, often a single character. With `n <= 1000`, exhaustive subsequence search is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must preserve a valid symmetric or structurally constrained signal while minimizing retention cost: log compaction with semantic markers, DNA/read alignment heuristics, compiler recovery passes that preserve balanced constructs, and ranking pipelines that keep only the cheapest evidence satisfying a constraint. At scale, brute-force search over subsequences explodes combinatorially, and greedy pruning silently drops optimal solutions. Interval dynamic programming gives a bounded, deterministic way to reason about all deletions over contiguous windows, which is exactly what enables predictable latency and correctness under large batch workloads.

## 🔍 Problem Statement
You are given:

- a string `s` of length `n`
- an integer array `cost` where `cost[i]` is the cost of keeping `s[i]`

You may delete any characters at zero cost. From the remaining characters, you must form a **non-empty subsequence** that is also a palindrome. Return the minimum possible sum of costs of the kept characters.

Constraints:

- `1 <= n <= 1000`
- `s.length == cost.length`
- `1 <= cost[i] <= 10^6`
- `s` contains only lowercase English letters

Examples:

```text
Input:  s = "abca", cost = [4, 2, 7, 3]
Output: 2
Explanation: Single characters are valid palindromes. The cheapest is "b" with cost 2.
```

```text
Input:  s = "racecar", cost = [8, 6, 5, 1, 5, 6, 8]
Output: 1
Explanation: The full string is a palindrome, but keeping only "e" is cheaper.
```

The key constraint is `n = 1000`: too large for exponential subsequence enumeration, but small enough for `O(n^2)` dynamic programming over substrings.

## 🪜 How to Solve This
1. Read the problem → this is not “build the longest palindrome” or “minimum deletions.” The objective is **minimum keep-cost**, and deletions are free.

2. Notice the subsequence property → when deciding whether characters at positions `i` and `j` participate, everything between them becomes an independent smaller instance. That strongly suggests **interval DP**.

3. Define the right subproblem → for every substring `s[i..j]`, ask: what is the minimum cost of any non-empty palindromic subsequence inside this interval?

4. Consider choices at the boundaries:
   - skip `s[i]`
   - skip `s[j]`
   - if `s[i] == s[j]`, use both ends and combine them with the best palindrome from the middle

5. Handle the subtle case → if you use matching ends, the middle may be empty. That means a 2-character palindrome is valid even when `i + 1 > j - 1`.

6. Base case becomes obvious → a single character is a palindrome with cost `cost[i]`.

7. Fill intervals from short to long → every transition depends only on smaller ranges, so a bottom-up quadratic DP works cleanly.

## 🧩 Algorithm Walkthrough
1. **Pattern: Interval Dynamic Programming.**  
   Let `dp[i][j]` be the minimum keep-cost of any non-empty palindromic subsequence contained in substring `s[i..j]`. This abstraction is correct because every valid subsequence decision inside `[i, j]` reduces to smaller intervals after skipping or pairing boundary characters.

2. **Initialize base cases.**  
   For every `i`, set `dp[i][i] = cost[i]`. A single character is always a valid non-empty palindrome. This establishes the invariant that every processed interval has at least one feasible answer.

3. **Process intervals by increasing length.**  
   For each window `[i, j]` with `i < j`, start with:
   - `dp[i][j] = min(dp[i+1][j], dp[i][j-1])`  
   This captures all optimal palindromes that do not use at least one boundary.

4. **Try pairing the boundaries when characters match.**  
   If `s[i] == s[j]`, then one candidate is keeping both ends:
   - if `j == i + 1`, candidate = `cost[i] + cost[j]`
   - otherwise, candidate = `cost[i] + cost[j] + min(0? no)` actually `cost[i] + cost[j]` or `cost[i] + cost[j] + dp[i+1][j-1]`?  
   The correct recurrence is:
   - `cost[i] + cost[j]` for the 2-character palindrome formed by just the ends
   - `cost[i] + cost[j] + dp[i+1][j-1]` if we want to wrap a non-empty middle palindrome  
   Taking the minimum of these is equivalent to `cost[i] + cost[j] + min(0, dp[i+1][j-1])`, but since the middle must remain a palindrome only when included, it is clearer to evaluate both explicitly.

5. **Take the best candidate.**  
   Set `dp[i][j]` to the minimum among skipping-left, skipping-right, and pairing-ends. The invariant after each update: `dp[i][j]` is the cheapest non-empty palindromic subsequence fully contained in `[i, j]`.

6. **Return `dp[0][n-1]`.**  
   This is the optimal answer over the full string. The recurrence is exhaustive, and overlapping subproblems make DP the right tool.

## 📊 Worked Example
Use `s = "abca"`, `cost = [4, 2, 7, 3]`.

| Interval | Substring | Computation | `dp` |
|---|---|---|---:|
| `[0,0]` | `"a"` | base | 4 |
| `[1,1]` | `"b"` | base | 2 |
| `[2,2]` | `"c"` | base | 7 |
| `[3,3]` | `"a"` | base | 3 |
| `[0,1]` | `"ab"` | `min(2,4)` | 2 |
| `[1,2]` | `"bc"` | `min(7,2)` | 2 |
| `[2,3]` | `"ca"` | `min(3,7)` | 3 |
| `[0,2]` | `"abc"` | `min(dp[1][2], dp[0][1]) = min(2,2)` | 2 |
| `[1,3]` | `"bca"` | `min(3,2)` | 2 |
| `[0,3]` | `"abca"` | skip gives `min(2,2)=2`; ends match: `"aa"` cost `4+3=7`, `"aca"` cost `4+7+3=14` | 2 |

Final answer: `dp[0][3] = 2`, achieved by keeping `"b"` alone.

## ⏱ Complexity Analysis
### Time Complexity
`O(n^2)`. There are `n * (n + 1) / 2` intervals, and each state is computed in constant time from previously solved smaller intervals. At `n = 1000`, this is about 500k states, comfortably practical. At `10^6` or `10^9`, quadratic growth becomes completely infeasible, which is why the problem’s bound matters.

### Space Complexity
`O(n^2)` for the DP table storing one value per substring interval. With `n = 1000`, this is manageable. Space can be reduced only with careful diagonal compression, but the dependency pattern and implementation complexity usually make the full table the better trade-off.

## 💡 Key Takeaways
- If the problem asks for an optimal subsequence constrained by relationships between both ends of a substring, interval DP is a strong signal.
- When choices are “skip left / skip right / use both if compatible,” you are almost always in substring-DP territory rather than greedy or standard sequence DP.
- Do not assume matching endpoints should always be taken; the cheapest palindrome may be a shorter subsequence or even a single character.
- Be explicit about the “pair only the ends” case when `s[i] == s[j]`; wrapping with `dp[i+1][j-1]` alone misses valid 2-character palindromes.
- The transferable design insight: model expensive global search as a lattice of bounded local decisions over intervals, then enforce correctness with a state invariant.

## 🚀 Variations & Further Practice
- Return the **minimum cost palindrome of maximum length** instead of just minimum cost; this adds lexicographic or multi-objective tie-breaking to the DP state.
- Charge both **keep cost and delete cost** with different weights; now every character participates in the objective, changing the recurrence economics.
- Require the palindrome subsequence to have length at least `k`; this introduces an additional dimension or a richer state that tracks achievable lengths.