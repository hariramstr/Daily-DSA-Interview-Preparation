# Find the Smallest Shared Category Subtree

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Trees, Depth-First Search, Lowest Common Ancestor

---

## 🗂 Problem Overview
Given a binary tree with unique category IDs and a distinct list of target IDs, return the ID of the smallest subtree root that contains every target. If any target is missing from the tree, return `-1`. The challenge is not finding pairwise paths, but identifying the lowest node whose descendant set covers the full target set. With up to `100000` nodes and targets, brute-force ancestor checks or repeated searches are too expensive.

## 🌍 Engineering Impact
This pattern shows up anywhere a hierarchy must answer “what is the tightest shared container for this set?” Examples include product taxonomy ownership, IAM policy inheritance, compiler scope resolution, file-system permission folding, and organizational rollups in analytics pipelines. At scale, naive repeated ancestor walks turn into latency spikes and cache-unfriendly traversals, especially on skewed trees. A single post-order aggregation pass gives deterministic linear behavior, which matters when the hierarchy is large, frequently queried, or embedded in request-path decision logic. The broader value is recognizing when local subtree summaries are enough to answer a global containment question.

## 🔍 Problem Statement
You are given the root of a binary tree containing unique integer IDs and a list of distinct target IDs. Return the ID of the lowest node whose subtree contains all targets. If one or more target IDs do not exist anywhere in the tree, return `-1`.

A subtree consists of a node and all of its descendants. The answer may itself be one of the target nodes.

Constraints:
- `1 <= n <= 100000`
- Node values are unique, in `[1, 10^9]`
- `1 <= targets.length <= min(100000, n)`
- Targets are distinct
- The tree may be unbalanced

Examples:
- `root = [10,5,15,3,7,12,18,null,4], targets = [4,7]` → `5`
- `root = [8,4,11,2,6,9,13,1,3,5,7], targets = [1,7,9]` → `8`

The key constraint is size: any approach that repeatedly searches from the root or computes ancestors per target risks superlinear behavior.

## 🪜 How to Solve This
1. Read the problem → this is not “find paths between targets”; it is “find the lowest subtree that covers a set.”
2. “Lowest subtree covering nodes” should trigger Lowest Common Ancestor thinking, but for **multiple** targets, not just two.
3. For any node, the only fact we really need from its children is: **how many target IDs were found in that subtree?**
4. That suggests a post-order DFS. Compute left result, compute right result, then combine them with whether the current node itself is a target.
5. Once a node’s subtree contains all `k` targets, that node is a candidate. The **first** such node encountered bottom-up is the smallest valid subtree root.
6. But correctness depends on existence: if the total count found in the entire tree is less than `k`, at least one target is missing, so return `-1`.
7. Use a hash set for target membership so each node check is `O(1)`. Then every node is visited once, which is exactly what the input size demands.

## 🧩 Algorithm Walkthrough
1. **Build a target set.**  
   Store all target IDs in a hash set for constant-time membership checks. This turns “is this node relevant?” into `O(1)` and avoids scanning the target list at each node.

2. **Run a post-order DFS.**  
   This is the right abstraction: a node’s answer depends on complete information from both subtrees before it can decide whether it covers all targets. This is a classic **Depth-First Search with subtree aggregation** pattern, closely related to multi-node **Lowest Common Ancestor**.

3. **Return a count from each subtree.**  
   For each node, recursively compute:
   - count of targets found in the left subtree
   - count of targets found in the right subtree
   - `+1` if the current node is itself a target  
   Invariant: the returned count equals the number of requested target IDs present anywhere in that subtree.

4. **Capture the smallest valid root.**  
   After combining counts, if the total equals `k` and no deeper node has already been recorded as the answer, mark the current node as the result. Because traversal is post-order, deeper candidates are seen first; the first full-cover node is therefore the lowest valid subtree root.

5. **Validate completeness at the root.**  
   If the final count from the root is less than `k`, some targets do not exist in the tree, so return `-1`. Otherwise return the recorded node’s ID.

This is correct because subtree counts compose exactly, targets are unique, and post-order guarantees minimality of the chosen covering root.

## 📊 Worked Example
Example: `root = [10,5,15,3,7,12,18,null,4]`, `targets = [4,7]`

| Node | Left Count | Right Count | Self Target? | Total | Answer Set? |
|------|------------|-------------|--------------|-------|-------------|
| 4    | 0          | 0           | Yes          | 1     | No          |
| 3    | 0          | 1           | No           | 1     | No          |
| 7    | 0          | 0           | Yes          | 1     | No          |
| 5    | 1          | 1           | No           | 2     | Yes → 5     |
| 12   | 0          | 0           | No           | 0     | No          |
| 18   | 0          | 0           | No           | 0     | No          |
| 15   | 0          | 0           | No           | 0     | No          |
| 10   | 2          | 0           | No           | 2     | Already set |

Trace:
1. Node `4` contributes one target.
2. Node `3` aggregates that `1`.
3. Node `7` contributes the second target.
4. Node `5` sees both targets in its subtree, so it becomes the answer.
5. Higher nodes may also cover all targets, but they are not smaller.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + k)`, where `n` is the number of tree nodes and `k` is the number of targets. Building the target set costs `O(k)`, and the DFS visits each node exactly once. At million-node scale this remains practical; at billion-node scale, the issue becomes memory and recursion depth, not asymptotic waste.

### Space Complexity
`O(k + h)`, where `k` is the target set size and `h` is tree height from the DFS call stack. In the worst unbalanced case, `h = O(n)`. Stack usage can be reduced with an explicit iterative traversal, trading simpler code for more bookkeeping.

## 💡 Key Takeaways
- If the prompt asks for the smallest subtree containing multiple nodes, think multi-target LCA via post-order subtree aggregation.
- If node values are unique and the question is about coverage, counting matches per subtree is usually enough; you often do not need explicit ancestor paths.
- Do not return the first node whose count equals `k` in pre-order; minimality depends on bottom-up evaluation.
- Missing targets must be handled explicitly: a candidate root is only valid if the entire tree contains all requested IDs.
- In production hierarchies, pushing summary information upward in one pass is often the difference between predictable linear behavior and repeated traversal blowups.

## 🚀 Variations & Further Practice
- Generalize from a binary tree to an N-ary taxonomy tree; the conceptual twist is aggregating across an arbitrary number of children while preserving the same subtree-count invariant.
- Answer many target-set queries on a mostly static tree; the harder part is preprocessing for repeated LCA-style queries instead of recomputing a full DFS each time.
- Return the subtree root plus the number of missing targets when the set is incomplete; this changes the contract from pure selection to partial-diagnostic aggregation.