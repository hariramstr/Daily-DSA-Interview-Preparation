# Cousin Nodes at Same Depth

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Trees &nbsp;|&nbsp; **Tags:** Tree, Breadth-First Search, Depth-First Search

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a family tree. Two people are "cousins" if they belong to the same generation but have different parents. This problem asks the same question about a data structure called a tree: given two specific members, are they at the same level AND do they have different direct parents? The answer is simply yes or no.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Relationship-checking in tree structures powers many everyday digital experiences. Social networks like LinkedIn use similar logic to determine degrees of connection between users — helping surface relevant contacts without overwhelming you with distant ones. In corporate org-chart tools, this logic ensures that peer-level employees (same rank, different managers) are correctly grouped for performance reviews or salary benchmarking. Getting these relationships right reduces misclassification errors, improves recommendation accuracy, and ultimately saves time and money by automating what would otherwise require manual review.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a company org chart. The CEO is at the top, managers are one level below, and their direct reports are another level below that. Two employees are "cousins" if they work at the same level of the company but report to different managers. Your job is to look at any two employees and answer: are they truly cousins, or are they actually siblings working under the same boss?

---

## 🔍 Technical Problem Statement *(For Developers)*

Given the `root` of a binary tree and two integer values `x` and `y`, return `true` if the nodes containing `x` and `y` are **cousins**, and `false` otherwise.

Two nodes are cousins if and only if:
1. They reside at the **same depth** (distance from root, where root is depth 0).
2. They have **different parent nodes**.

**Constraints:**
- Number of nodes: `[2, 100]`
- Node values are unique integers in `[1, 100]`
- Both `x` and `y` are guaranteed to exist in the tree

**Examples:**

```
Input: root = [1,2,3,4,5,null,null], x=4, y=5  →  Output: false
# Same depth (2), but same parent (node 2) → siblings, not cousins

Input: root = [1,2,3,null,4,null,5], x=4, y=5  →  Output: true
# Same depth (2), different parents (nodes 2 and 3) → cousins ✓
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use **Breadth-First Search (BFS)** — processing the tree level by level — to find both nodes and record their depth and parent.

**Step-by-step algorithm:**

1. **Initialize a queue** with the root node, its depth (`0`), and its parent (`None`). A queue ensures we visit nodes level by level, which is essential for tracking depth accurately.

2. **Process each node** by dequeuing it and checking whether its value matches `x` or `y`. When a match is found, record the node's `depth` and `parent`. We need both pieces of information to evaluate the cousin condition.

3. **Enqueue children** of the current node, passing along the current node as their parent and incrementing depth by 1. This propagates relationship metadata downward through the tree.

4. **Repeat** until both `x` and `y` have been found. There is no need to continue searching once both targets are located, which provides a minor early-exit optimization.

5. **Evaluate the cousin condition**: return `true` if and only if the recorded depths are **equal** AND the recorded parents are **different**. Both conditions must hold simultaneously — same depth alone makes them peers, but same parent makes them siblings instead.

This approach is clean, avoids recursion stack concerns, and naturally handles depth tracking through the BFS level structure.

---

## 📊 Worked Example *(For Developers)*

**Input:** `root = [1,2,3,null,4,null,5]`, `x = 4`, `y = 5`

| Step | Node Dequeued | Depth | Parent | Action |
|------|--------------|-------|--------|--------|
| 1 | 1 | 0 | None | Enqueue children: 2 (depth 1, parent 1), 3 (depth 1, parent 1) |
| 2 | 2 | 1 | 1 | No match. Enqueue child: 4 (depth 2, parent 2) |
| 3 | 3 | 1 | 1 | No match. Enqueue child: 5 (depth 2, parent 3) |
| 4 | 4 | 2 | 2 | **Match x=4!** Record: depth_x=2, parent_x=2 |
| 5 | 5 | 2 | 3 | **Match y=5!** Record: depth_y=2, parent_y=3 |

**Final check:** `depth_x == depth_y` → `2 == 2` ✅ AND `parent_x != parent_y` → `2 != 3` ✅

**Output:** `true`

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — In the worst case, we visit every node in the tree once before finding both targets. For a tree with 100 nodes (the maximum here), this is negligible. Even at massive scale, this linear relationship means doubling the tree size only doubles the processing time — a very manageable growth rate.

### Space Complexity

**O(n)** — The BFS queue can hold at most one full level of the tree at a time. In the worst case (a perfectly balanced tree), the widest level contains roughly n/2 nodes, so memory usage grows linearly with tree size. For trees up to 100 nodes, this is trivially small.

---

## 💡 Key Takeaways *(For Everyone)*

- **Relationship classification matters in business:** Correctly identifying peer-level vs. parent-child relationships in hierarchical data prevents costly misclassification in org tools, recommendation engines, and access-control systems.
- **Simple rules, powerful applications:** The cousin check (same level + different parent) is a small building block that underpins more complex graph-relationship features in real products.
- **BFS is the natural fit for depth-based problems:** Whenever a problem involves "which level is this node on?", Breadth-First Search gives you that information for free as you traverse.
- **Always track both conditions together:** Checking depth alone is insufficient — siblings share depth too. Coupling depth with parent identity is what makes the cousin check precise.
- **Early termination is a valid optimization:** Once both target nodes are found, halt the search. In large trees, this can significantly reduce unnecessary work.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — All cousins at a depth:** Instead of checking two specific nodes, return a list of all groups of cousins at a given depth `d`. This extends the BFS approach to group nodes by parent at each level.
- **Variation 2 — N-ary trees:** Adapt the solution to trees where each node can have more than two children (e.g., a file system directory tree). How does the cousin definition change when siblings can number more than two?
- **Variation 3 — Distance between cousins:** Rather than a boolean result, return the minimum number of edges between nodes `x` and `y` in the tree — a classic problem that builds on the same BFS foundation.

---