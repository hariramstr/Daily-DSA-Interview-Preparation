# Maximum Emergency Detour Sum in a Hazard Tree

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, DFS, Sliding Window, Prefix Sum, Hash Map

---

## 🗂 Problem Overview
Given a tree rooted at node `0`, find the maximum sum over any downward contiguous path `ancestor -> ... -> descendant` such that along that path, no risk value appears more than twice and at most one distinct value appears exactly twice. The path may start anywhere, not necessarily at the root. The challenge is that validity depends on value frequencies over the current ancestor chain, while the optimal path sum must still be extracted efficiently under `n <= 2e5`.

## 🌍 Engineering Impact
This pattern shows up whenever you need the best contiguous segment under a stateful uniqueness constraint over a hierarchical traversal: compiler scope chains with limited symbol shadowing, distributed tracing trees with bounded duplicate tags, policy evaluation over org/resource hierarchies, and streaming anomaly detection on lineage graphs. At scale, brute-force subtree path enumeration collapses immediately: `O(n^2)` path checks are dead on arrival for 200k nodes. What matters is combining traversal-local state, rollback, and a window invariant so each node is processed once while still supporting “best subpath ending here” decisions.

## 🔍 Problem Statement
You are given `n` facilities, an undirected tree `edges` of size `n - 1`, and an integer array `risk` where `risk[i]` may be negative. Root the tree at `0`. A valid detour is any simple downward path from some ancestor to some descendant. Its score is the sum of `risk` values on that path.

A path is valid iff:
- no value appears more than twice,
- at most one distinct value appears exactly twice,
- every other value appears once.

Return the maximum score among all valid downward paths.

Constraints:
- `1 <= n <= 200000`
- `edges.length == n - 1`
- `-10^9 <= risk[i] <= 10^9`

Examples:

- `n=7, edges=[[0,1],[0,2],[1,3],[1,4],[2,5],[2,6]], risk=[5,2,5,1,2,3,4]` → `14`
- `n=6, edges=[[0,1],[1,2],[2,3],[3,4],[4,5]], risk=[4,-2,4,7,7,1]` → `19`

The key algorithmic pressure is that the best path may start in the middle, and path validity depends on frequency counts over the current root-to-node prefix.

## 🪜 How to Solve This
1. Root the tree at `0` and think only about root-to-current DFS paths. Any valid downward path in the tree is a contiguous segment of some root-to-node path.

2. Once you see “contiguous segment with frequency constraints,” this stops being a generic tree problem and starts looking like a sliding-window problem layered onto DFS.

3. For each current node `u`, imagine the current root-to-`u` path as an array. We need the best valid suffix ending at `u`, because every ancestor-to-`u` segment is one candidate downward path.

4. Validity depends only on counts of `risk` values inside that suffix:
   - no count > 2
   - at most one value with count = 2

5. So maintain a left boundary `L` for the smallest valid suffix on the current DFS path, plus frequency counts for values inside `[L..current]`.

6. To score the suffix quickly, maintain prefix sums along the DFS path. Then any segment sum is `prefix[r+1] - prefix[L]`.

7. DFS introduces the hard part: state must be reversible. Every time you enter a child, update counts/window/prefix; when you leave, roll everything back exactly.

8. The result is a tree DFS with a sliding-window-like invariant and rollbackable hash-map counts, giving linear traversal instead of quadratic path enumeration.

## 🧩 Algorithm Walkthrough
1. **Build adjacency and root the traversal with DFS.**  
   We process each node once from parent to children. The active DFS stack represents the current root-to-node path. This is the only path context needed because every downward path is a contiguous segment of such a stack.

2. **Maintain path arrays.**  
   Keep:
   - `pathValues[i]`: risk value at depth `i`
   - `prefix[i+1]`: sum of risks from root to depth `i`
   These let us compute any ancestor-to-current sum in `O(1)`.

3. **Use a sliding-window invariant on the DFS stack.**  
   Pattern: **Sliding Window + DFS Rollback**.  
   Maintain a left index `L` so the segment `pathValues[L..depth]` is valid:
   - no frequency exceeds `2`
   - at most one value has frequency `2`

4. **Track frequencies with a hash map and a duplicate counter.**  
   `freq[value]` stores occurrences inside the active window.  
   `twiceCount` stores how many distinct values currently have frequency exactly `2`.  
   When adding a node’s value:
   - `1 -> 2` increments `twiceCount`
   - `2 -> 3` makes the window invalid immediately

5. **Shrink from the left until valid.**  
   While `freq[newValue] > 2` or `twiceCount > 1`, remove `pathValues[L]` and increment `L`. This is exactly the standard sliding-window repair step, just applied to the current DFS path.

6. **Evaluate the best path ending at the current node.**  
   Once valid, the candidate score is the sum of the valid suffix `L..depth`:  
   `prefix[depth + 1] - prefix[L]`.  
   This is correct because every valid downward path ending at the current node corresponds to some suffix, and `L` is the leftmost valid one under the maintained invariant.

7. **Rollback on DFS exit.**  
   Before descending, snapshot all mutable state changes needed to restore:
   `L`, `twiceCount`, and the frequency updates caused by both expansion and shrink steps. After returning from a child, undo them. Correct rollback is what turns a linear-path window into a tree-capable algorithm.

## 📊 Worked Example
Example: `risk = [4,-2,4,7,7,1]`, chain `0-1-2-3-4-5`

| Depth | Node | Value | Window Values | `L` | `twiceCount` | Sum |
|---|---:|---:|---|---:|---:|---:|
| 0 | 0 | 4  | `[4]` | 0 | 0 | 4 |
| 1 | 1 | -2 | `[4,-2]` | 0 | 0 | 2 |
| 2 | 2 | 4  | `[4,-2,4]` | 0 | 1 | 6 |
| 3 | 3 | 7  | `[4,-2,4,7]` | 0 | 1 | 13 |
| 4 | 4 | 7  | `[4,-2,4,7,7]` invalid (`4` and `7` both repeated) | shrink |  |  |
| 4 | 4 | 7  | `[-2,4,7,7]` | 1 | 1 | 16 |
| 5 | 5 | 1  | `[-2,4,7,7,1]` | 1 | 1 | 17 |

The full root-based path is not always optimal. The best valid downward path here is actually `2 -> 3 -> 4 -> 5 = [4,7,7,1]`, sum `19`, obtained by considering the valid suffix ending at node `5` after the effective left boundary moves past the negative `-2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` amortized. Each node is entered once in DFS, and each path position is added to and removed from the sliding window at most once per active traversal segment. Hash-map updates are `O(1)` average. At `10^6` scale this is still practical; `O(n^2)` is not. At `10^9`, neither memory nor traversal is realistic.

### Space Complexity
`O(n)` for adjacency, DFS path storage, prefix sums, and the frequency map in the worst case of all distinct values on a deep path. You can trade recursive clarity for iterative DFS to avoid call-stack risk, but asymptotic space does not improve.

## 💡 Key Takeaways
- If a tree problem asks for any ancestor-to-descendant segment, flatten your thinking to “contiguous subarray on the current DFS path.”
- Frequency-constrained path validity is a strong signal for sliding-window state, even when the underlying structure is a tree rather than an array.
- The left boundary is depth-indexed, so prefix-sum subtraction must use path indices consistently; off-by-one errors usually come from mixing node depth with prefix length.
- Rollback must undo both the node insertion and every shrink-step mutation; restoring only `L` without restoring counts corrupts sibling traversals.
- The transferable design insight is to pair local incremental state with exact rollback, which is the core technique behind scalable hierarchical evaluators, transactional traversals, and backtracking systems.

## 🚀 Variations & Further Practice
- Allow paths between any two nodes, not just ancestor-to-descendant. The twist is that the path is no longer a single DFS suffix; you need LCA-aware decomposition or centroid/HLD-style machinery.
- Replace the “at most one value may repeat twice” rule with “at most `k` repeated values” or arbitrary per-value caps. The twist is generalizing the window validity structure without losing `O(1)` update checks.
- Ask for the number of valid downward paths instead of the maximum sum. The twist shifts from best-suffix scoring to counting all valid starts for each endpoint under the same rollbackable window invariant.