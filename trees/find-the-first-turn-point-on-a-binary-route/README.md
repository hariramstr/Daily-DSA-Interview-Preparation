# Find the First Turn Point on a Binary Route

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Binary Tree

---

## 🗂 Problem Overview
Given a binary tree root and a target value guaranteed to exist, follow the unique root-to-target path and detect the first point where traversal direction changes from left to right or right to left. Return the value of the node where that new direction is taken. If the path is straight in one direction, or the target is the root, return `-1`. The non-trivial part is carrying path-state during traversal rather than just locating the target.

## 🌍 Engineering Impact
This pattern shows up anywhere hierarchical traversal depends not only on destination but also on path shape. Examples include compiler AST walks that detect the first semantic context switch, policy engines evaluating inheritance overrides in config trees, routing systems identifying the first branch divergence in decision trees, and search/ranking pipelines tracing the first feature-gate transition in a model tree. At scale, the issue is not tree traversal itself; it is preserving minimal path context while keeping the walk single-pass. Without that discipline, implementations become brittle, over-store full paths, or require expensive post-processing that does not compose well in latency-sensitive systems.

## 🔍 Problem Statement
You are given the root of a binary tree and a `target` value that is guaranteed to appear exactly once. Consider the unique path from the root to that target. Each step on the path is either `left` or `right`. A turn point occurs when the current step’s direction differs from the previous step’s direction. The root is never a turn point because no prior move exists.

Return the value of the first such turn point. If the path never changes direction, return `-1`. If the target is the root, also return `-1`.

Constraints:
- `1 <= number of nodes <= 1000`
- Node values are unique
- `-10^4 <= Node.val <= 10^4`
- The target always exists
- Tree is not necessarily balanced

Examples:
- `root = [8,4,12,2,6,null,14,null,null,5,7], target = 7` → `4`
- `root = [10,5,15,3,7], target = 3` → `-1`

The key constraint is that values are unique and the target exists, so a single DFS can both find the path and evaluate turns in one pass.

## 🪜 How to Solve This
1. Read the problem → this is not “find a node,” it is “find a property of the path to a node.”
2. In a tree, the root-to-target path is unique → no need for BFS across alternatives once you commit to DFS with backtracking.
3. What defines the answer? Not the target, but the first place where direction changes relative to the previous move.
4. That means traversal must carry two pieces of state:
   - the previous direction taken to reach the current node
   - whether a turn has already been found
5. Start DFS from the root. When exploring a child, label that edge as `L` or `R`.
6. If there was a previous direction and the new one differs, then the current node is the first turn point candidate.
7. Because we need the *first* turn on the successful root-to-target path, return immediately once the target is found through a branch that already recorded the first change.
8. If the target is reached without any direction change, return `-1`.

The mental model is simple: search for the target while streaming path metadata forward, instead of materializing the whole path and analyzing it afterward.

## 🧩 Algorithm Walkthrough
1. **Use Depth-First Search with path-state propagation.**  
   The right abstraction is **DFS on a binary tree** because there is exactly one root-to-target path, and DFS naturally carries state down that path without extra storage.

2. **Track the previous move direction.**  
   For each recursive call, pass the direction used to arrive at the current node: none for the root, then `L` or `R` for descendants.  
   **Invariant:** `prevDir` always describes the last edge on the current traversal path.

3. **Track the first turn point once discovered.**  
   Also pass either the recorded first turn value or a sentinel meaning “not found yet.”  
   When moving from the current node to a child with direction `nextDir`, compare `nextDir` with `prevDir`. If they differ and no turn has been recorded yet, the current node becomes the first turn point.  
   **Invariant:** once set, the recorded turn value is the earliest direction change on the current root-to-current path.

4. **Search left and right recursively.**  
   Recurse into each child, forwarding updated direction and turn state. If a branch reaches the target, bubble its answer upward immediately.  
   This is correct because only one branch contains the target.

5. **Handle base cases carefully.**  
   If the current node is `null`, return failure. If the current node’s value equals `target`, return the recorded turn value, or `-1` if none exists.  
   This ensures root-as-target and no-turn paths are handled without special-case post-processing.

6. **Return the first successful branch result.**  
   Since values are unique and target exists, exactly one DFS path succeeds. The returned value is therefore the first turn point on the true root-to-target route.

## 📊 Worked Example
Example: `root = [8,4,12,2,6,null,14,null,null,5,7]`, `target = 7`

Path to target: `8 -> 4 -> 6 -> 7`

| Current Node | Move Used to Reach It | Next Move | Direction Change? | First Turn Recorded |
|---|---:|---:|---:|---:|
| 8 | none | L to 4 | No prior move | none |
| 4 | L | R to 6 | Yes (`L -> R`) | 4 |
| 6 | R | R to 7 | No | 4 |
| 7 | R | target found | — | 4 |

Trace:
1. Start at `8`, no previous direction.
2. Move left to `4`; still no turn because this is the first move.
3. From `4`, move right to `6`; previous move was left, so this is the first direction change. Record `4`.
4. From `6`, move right to `7`; direction stays right, so no new turn.
5. Reach target `7`; return recorded answer `4`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` in the worst case, where `n` is the number of nodes. The dominant operation is visiting each node at most once during DFS until the target branch is resolved. At `10^6` nodes this is still linear work; at `10^9`, runtime and recursion depth become operationally unrealistic without a different execution model.

### Space Complexity
`O(h)` auxiliary space, where `h` is tree height, due to the recursion stack. In the worst case of a skewed tree, this becomes `O(n)`. It can be reduced to iterative `O(h)` with an explicit stack, trading simpler code for more manual state management.

## 💡 Key Takeaways
- If a tree problem asks for something about the root-to-target route rather than the target node itself, think DFS with path-state propagation.
- “First change,” “first divergence,” or “first transition” are strong signals that you should carry incremental state instead of reconstructing the full path later.
- The turn point is the node where the *new* direction is taken, not the child reached after the turn.
- Do not treat the root as a turn point; there is no previous direction to compare against.
- In production traversal code, streaming just enough context through the walk is usually cheaper and more maintainable than collecting full paths and post-processing them.

## 🚀 Variations & Further Practice
- Return **all** turn points on the root-to-target path instead of the first one; the twist is preserving ordered path events rather than a single sentinel value.
- Find the first turn point between **two arbitrary nodes**; the harder part is computing paths via LCA and then reasoning across the combined route.
- Generalize from binary trees to **n-ary routing trees** where “turn” means switching branch class or edge label; the conceptual twist is replacing binary direction comparison with category transitions.

