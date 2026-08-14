# Minimum Toggles to Match a Parity Beacon

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Greedy, Array

---

## 🗂 Problem Overview
Given two binary arrays `bits` and `target` of equal length, compute the minimum number of toggle operations needed to transform `bits` into `target`. Toggling index `i` flips `bits[i]` and every later index with the same parity: `i, i+2, i+4, ...`. The challenge is that each operation affects a suffix of one parity chain, so naive search over toggle sequences is exponential. The solution must exploit that even and odd indices are independent and process both chains in linear time.

## 🌍 Engineering Impact
This pattern shows up anywhere a control action affects a strided suffix rather than a single cell: feature-flag propagation across sharded partitions, staged rollout controls over alternating replica groups, memory lane repair in hardware diagnostics, and stream processors where remediation applies to every future record in one partition class. At scale, brute-force reasoning over action sequences collapses under combinatorics and makes correctness hard to audit. Recognizing independent substructures lets you decompose the state space, derive a deterministic greedy pass, and preserve predictable latency under high-cardinality inputs.

## 🔍 Problem Statement
You are given two binary arrays, `bits` and `target`, each of length `n`, where `1 <= n <= 200000` and every value is either `0` or `1`.

One operation chooses an index `i` and toggles:
- `bits[i]`
- `bits[i+2]`
- `bits[i+4]`
- and so on, while indices remain in bounds

So each operation flips a suffix within exactly one parity class: even indices or odd indices.

Return the minimum number of operations required to transform `bits` into `target`. If no sequence can do it, return `-1`.

Examples:

- `bits = [1,0,1,1,0]`, `target = [0,0,0,1,1]` → `1`
- `bits = [0,1,0,1]`, `target = [1,0,1,0]` → `2`

The key constraint is `n` up to `200000`, which rules out backtracking or simulation over candidate sequences. The algorithm must be essentially linear.

## 🪜 How to Solve This
1. Read the operation carefully → it never mixes even and odd indices. Toggling `i` only affects indices with the same parity, so the problem immediately splits into two independent chains.

2. Reframe the array → instead of one length-`n` array, think of:
   - even chain: indices `0, 2, 4, ...`
   - odd chain: indices `1, 3, 5, ...`

3. Focus on one chain. In that chain, toggling a position flips that position and every later position in the chain. That is a classic suffix-flip problem.

4. Process left to right within the chain. By the time you reach a position, the only remaining way to change it is to toggle exactly there; later toggles cannot affect earlier positions.

5. Keep one bit of state: whether the current chain has been flipped an odd or even number of times so far. That tells you the effective current value at each position.

6. If the effective value differs from the target, you must toggle here. Greedy is forced, not just convenient.

7. Solve both chains this way and add the counts. Since each chain is independent and always solvable, `-1` is never actually needed under these rules.

## 🧩 Algorithm Walkthrough
1. **Decompose by parity**  
   Treat the input as two independent sequences: even indices and odd indices. This is the key abstraction: **Greedy over independent parity chains**. An operation on one chain has zero effect on the other, so the global optimum is the sum of the two local optima.

2. **Scan one chain left to right**  
   For a chain like `0, 2, 4, ...`, maintain a boolean `flipped` indicating whether an odd number of prior toggles in this chain has occurred.  
   Invariant: before processing the current index, all earlier indices in the chain already match `target` permanently.

3. **Compute the effective bit**  
   The visible value at index `i` is `bits[i] XOR flipped`. This captures the cumulative effect of all prior suffix toggles in the chain without explicitly mutating future elements.

4. **Make the forced greedy choice**  
   If `bits[i] XOR flipped == target[i]`, do nothing.  
   Otherwise, toggle at `i`: increment the answer and invert `flipped`.  
   Why correct: a later toggle cannot repair index `i`, because later operations only affect later positions in the same chain.

5. **Repeat for the second chain**  
   Run the same logic starting at index `1`. The invariant and correctness argument are identical.

6. **Return total operations**  
   The algorithm is optimal because every toggle decision is forced at the first mismatch in each chain. No branching, no backtracking, no dynamic programming table is needed.

## 📊 Worked Example
Use `bits = [0,1,0,1]`, `target = [1,0,1,0]`.

Process even chain first: indices `0, 2`.

| Step | Index | Parity | flipped | Effective bit | Target | Action | Ops |
|---|---:|---|---:|---:|---:|---|---:|
| 1 | 0 | even | 0 | 0 | 1 | toggle here | 1 |
| 2 | 2 | even | 1 | 1 | 1 | none | 1 |

Process odd chain: indices `1, 3`.

| Step | Index | Parity | flipped | Effective bit | Target | Action | Ops |
|---|---:|---|---:|---:|---:|---|---:|
| 3 | 1 | odd | 0 | 1 | 0 | toggle here | 2 |
| 4 | 3 | odd | 1 | 0 | 0 | none | 2 |

Final answer: `2`.

The trace shows the core invariant: once a position is processed, it never needs to be revisited. Each mismatch forces exactly one toggle at that position within its parity chain.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)`. Each index is visited exactly once, and each visit does constant work: one XOR-equivalent state check and possibly one flip of the chain state. At `10^6` elements this is routine in a single pass; at `10^9`, the algorithm remains theoretically linear but becomes dominated by memory bandwidth and I/O constraints.

### Space Complexity
`O(1)` auxiliary space. The algorithm stores only a few counters and one flip-state bit per parity chain. No extra arrays are required. You could materialize the two chains for readability, but that increases space to `O(n)` with no algorithmic benefit.

## 💡 Key Takeaways
- If an operation affects `i, i+k, i+2k, ...`, first check whether the array decomposes into independent residue classes; that usually collapses the search space.
- When an operation flips a suffix, left-to-right greedy is a strong signal because later decisions cannot repair earlier positions.
- Do not physically toggle future elements; track cumulative parity with a single `flipped` flag and derive the effective value on demand.
- Be careful with parity traversal: process `0,2,4,...` and `1,3,5,...` separately, or you will accidentally mix independent state.
- The transferable design insight is decomposition: when a global control plane actually consists of isolated subdomains, solve and reason about each domain independently, then compose the result.

## 🚀 Variations & Further Practice
- Generalize the operation to flip `i, i+k, i+2k, ...` for arbitrary `k`; the twist is recognizing `k` independent residue classes instead of just even/odd parity.
- Add costs per toggle index; now each forced mismatch still matters, but the optimization may require rethinking whether the operation set remains uniquely determined.
- Allow operations that flip either a parity suffix or a full contiguous suffix; the harder part is handling overlapping action families without losing linear-time structure.