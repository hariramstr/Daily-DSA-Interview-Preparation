# Minimum Rewrite Cost for Tiered Access Rules

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Dynamic Programming &nbsp;|&nbsp; **Tags:** Dynamic Programming, String, State Compression

---

## 🗂 Problem Overview
Given a string `s` over `{A, B, C}` and three per-index rewrite-cost arrays, compute the minimum total cost to transform `s` into a nondecreasing tiered string of the form `A*B*C*`. Each position can be rewritten independently, but the global ordering constraint couples decisions across the prefix. With `n` up to `200000`, any approach that tries all split points or explicit target strings is too slow; the solution must exploit prefix structure and compress state aggressively.

## 🌍 Engineering Impact
This pattern shows up anywhere a stream or policy must be normalized into ordered phases under asymmetric rewrite cost: firewall rule compaction, IAM policy migration, compiler lowering passes, event pipeline stage normalization, and search/ranking feature bucketing. At scale, brute-force boundary enumeration turns into quadratic latency and poor cache behavior. A prefix-DP formulation gives deterministic linear time, bounded memory, and a clean way to encode “once you advance to a later tier, you cannot go back.” That same model is useful in production systems that enforce monotone lifecycle transitions, rollout stages, or ordered data-quality states.

## 🔍 Problem Statement
You are given:

- a string `s` of length `n`, where each character is `A`, `B`, or `C`
- arrays `costA`, `costB`, and `costC`, where `costX[i]` is the cost to rewrite `s[i]` into `X`

The goal is to rewrite the entire string so the result has the form `A...AB...BC...C`, equivalently `A*B*C*`, where any block may be empty. Return the minimum total rewrite cost as a 64-bit integer.

Constraints:

- `1 <= n <= 200000`
- `0 <= costA[i], costB[i], costC[i] <= 10^9`

Examples:

```text
Example 1
Input:
s = "CBAC"
costA = [3,0,2,4]
costB = [1,5,0,2]
costC = [0,3,4,0]
Output: 2
```

```text
Example 2
Input:
s = "ACBAA"
costA = [0,4,3,0,0]
costB = [2,1,1,2,2]
costC = [5,0,0,3,4]
Output: 3
```

The key algorithmic constraint is `n = 200000`: the solution must be linear or near-linear, not quadratic in split positions.

## 🪜 How to Solve This
1. Read the target form carefully → this is not arbitrary editing; the final string must move through ordered phases: `A`, then `B`, then `C`, never backward.

2. That immediately suggests prefix DP → at index `i`, the only thing that matters is which phase the rewritten prefix is currently in. You do **not** need the exact rewritten prefix, only whether you are still placing `A`s, already placing `B`s, or already placing `C`s.

3. Define three running costs:
   - minimum cost to make prefix `0..i` valid as `A*`
   - minimum cost to make prefix `0..i` valid as `A*B*`
   - minimum cost to make prefix `0..i` valid as `A*B*C*`

4. For each new character, either stay in the same phase or transition forward exactly once:
   - `A*` can only append `A`
   - `A*B*` can append `B` after either `A*` or `A*B*`
   - `A*B*C*` can append `C` after either `A*B*` or `A*B*C*`

5. Because each state depends only on the previous index, keep just three scalars. That gives `O(n)` time, `O(1)` extra space, and a direct fit for large inputs.

## 🧩 Algorithm Walkthrough
1. **Model the problem as phase-based Dynamic Programming with state compression.**  
   The valid output language is `A*B*C*`. That means every prefix belongs to one of three monotone phases: only `A`s so far, `A`s then `B`s, or `A`s then `B`s then `C`s. This abstraction is correct because future legality depends only on the latest phase reached.

2. **Define DP states over prefixes.**  
   Let:
   - `dpA` = minimum cost for processed prefix to be in phase `A*`
   - `dpB` = minimum cost for processed prefix to be in phase `A*B*`
   - `dpC` = minimum cost for processed prefix to be in phase `A*B*C*`  
   Invariant: after processing index `i`, each variable is the optimal cost among all rewrites of `s[0..i]` consistent with that phase.

3. **Initialize for the empty prefix.**  
   Before reading any character, all three phases are reachable with cost `0`, because blocks may be empty. This is the subtle point that allows outputs beginning with `B` or `C` without forcing earlier `A` or `B` characters.

4. **Process each position once.**  
   For index `i`, compute:
   - `nextA = dpA + costA[i]`
   - `nextB = min(dpA, dpB) + costB[i]`
   - `nextC = min(dpB, dpC) + costC[i]`  
   Why correct:
   - To end in `A*`, current char must be `A`, and you must already be in `A*`.
   - To end in `A*B*`, current char must be `B`, and previous phase can be `A*` or `A*B*`.
   - To end in `A*B*C*`, current char must be `C`, and previous phase can be `A*B*` or `A*B*C*`.

5. **Roll the state forward.**  
   Assign `dpA = nextA`, `dpB = nextB`, `dpC = nextC`. This preserves the prefix-optimality invariant while using constant memory.

6. **Return the best terminal phase.**  
   The answer is `min(dpA, dpB, dpC)` after the final index, since the final valid string may end in any of the three phases. Use 64-bit arithmetic throughout because total cost can exceed 32-bit range.

## 📊 Worked Example
Consider:

```text
s = "CBAC"
costA = [3,0,2,4]
costB = [1,5,0,2]
costC = [0,3,4,0]
```

Initialize empty-prefix state: `dpA = 0, dpB = 0, dpC = 0`.

| i | s[i] | nextA = dpA+costA | nextB = min(dpA,dpB)+costB | nextC = min(dpB,dpC)+costC | new `(dpA, dpB, dpC)` |
|---|------|-------------------|-----------------------------|-----------------------------|------------------------|
| 0 | C    | 3                 | 1                           | 0                           | (3, 1, 0)              |
| 1 | B    | 3                 | 6                           | 3                           | (3, 6, 3)              |
| 2 | A    | 5                 | 3                           | 7                           | (5, 3, 7)              |
| 3 | C    | 9                 | 5                           | 3                           | (9, 5, 3)              |

Final answer: `min(9, 5, 3) = 3`.

The important observation is not the exact target string but the phase-optimal prefix costs. Each row keeps only the cheapest way to end in each legal phase.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index performs a constant number of arithmetic operations and `min` comparisons across three states. This scales comfortably to `10^6` elements in typical production runtimes; at `10^9`, the issue is no longer algorithmic complexity alone but raw throughput, memory bandwidth, and I/O limits.

### Space Complexity
`O(1)` extra space. The algorithm stores only three running DP values plus temporary next-state variables. You could keep a full DP table for debugging or reconstruction, but that raises memory to `O(n)` with no benefit for computing only the minimum cost.

## 💡 Key Takeaways
- If the target output is constrained to ordered blocks like `X*Y*Z*`, think prefix DP over phases rather than trying all split points.
- If each position has independent rewrite cost but legality is global and monotone, the state is usually “which phase have we reached,” not the full rewritten prefix.
- Empty blocks matter: initialize all phases as reachable on the empty prefix, or you will incorrectly forbid outputs starting with `B` or `C`.
- Return `min(dpA, dpB, dpC)`, not just `dpC`; the optimal valid string may end before ever starting later blocks.
- State compression is the production-grade move here: once the recurrence depends only on the previous row, carrying the full table is wasted memory and worse cache behavior.

## 🚀 Variations & Further Practice
- Generalize from 3 tiers to `k` ordered tiers. The twist is scaling the phase transition from 3 scalar states to `k` states while preserving `O(nk)` time and deciding whether further optimization is possible.
- Require reconstruction of one optimal rewritten string, not just the cost. The harder part is storing predecessor decisions without giving up too much memory efficiency.
- Add penalties for opening a new block or limit the number of transitions. That turns simple phase DP into a richer state machine where transition count becomes part of the DP state.