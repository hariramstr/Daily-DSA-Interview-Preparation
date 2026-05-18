# Flatten a Multilevel Sparse Linked List by Depth

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Breadth-First Search, Multilevel Structure

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine a chain of items where some items have their own separate sub-chains hanging off them, and those sub-chains can have further sub-chains. This problem asks you to reorganise everything into one single chain, grouping items by how deeply nested they are — all top-level items first, then all second-level items, and so on.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This technique mirrors how organisations process **hierarchical data in level-by-level priority order** — a pattern that appears constantly in business systems. Email clients that render threaded conversations, project management tools that display tasks and sub-tasks by priority tier, and content delivery networks that serve top-level pages before loading nested assets all rely on this logic. Processing data breadth-first (level by level) rather than depth-first ensures that the most visible, highest-priority content reaches users first, directly improving perceived performance, user satisfaction, and ultimately conversion rates.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a company org chart printed on paper. The CEO is at the top, then department heads below, then team leads below them. Now imagine cutting out every name and re-pasting them onto one long strip of paper — all executives first in left-to-right order, then all department heads in left-to-right order, then all team leads. That reorganised strip is exactly what this problem asks you to produce.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given the head of a multilevel linked list where each node has:
- An integer `value`
- A `next` pointer to the next node at the same level
- A `child` pointer optionally pointing to the head of a sub-list (a new depth level)

Flatten the structure into a single-level linked list ordered **by depth (BFS order), then left-to-right within each depth**. Set all `child` pointers to `null` in the result; only `next` should chain nodes.

**Constraints:** 1 ≤ nodes ≤ 10⁴ | values ∈ [−10⁵, 10⁵] | max depth ≤ 1000 | no cycles.

**Example 1:**
```
Input:  1 - 2 - 3  (node 2 → child: 4 - 5, node 5 → child: 6 - 7)
Output: 1 → 2 → 3 → 4 → 5 → 6 → 7
```

**Example 2:**
```
Input:  10 - 20 - 30  (node 10 → child: 11-12, node 20 → child: 21-22, node 12 → child: 100)
Output: 10 → 20 → 30 → 11 → 12 → 21 → 22 → 100
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

This problem is a natural fit for **Breadth-First Search (BFS)** using a queue of list heads, processing one depth level at a time.

1. **Initialise a queue** with the head node of the top-level list. The queue will hold the starting node of each sub-list we discover, in the order we discover them. This guarantees left-to-right ordering across each depth level.

2. **Process each level in the queue.** Dequeue the head of a sub-list, then walk its `next` chain from left to right, appending each node to the result list. This captures all nodes at the current depth in correct order.

3. **Collect child pointers while walking.** As you traverse each node, if it has a `child` pointer, enqueue that child head immediately. Because we enqueue children in left-to-right order as we walk, all children of the current depth level will be processed together before moving deeper — preserving BFS ordering.

4. **Clean up pointers.** After appending each node to the result, set its `child` pointer to `null` and update `next` to point to the following node in the flattened list.

5. **Return the head** of the newly constructed single-level list.

This approach is clean and avoids recursion, making it safe even with deep nesting (up to depth 1000) without risking stack overflow.

---

## 📊 Worked Example *(For Developers)*

Using **Example 2**: `10 - 20 - 30`, node 10 → child `11-12`, node 20 → child `21-22`, node 12 → child `100`.

| Step | Action | Queue State (sub-list heads) | Flattened Result So Far |
|------|--------|------------------------------|-------------------------|
| 1 | Initialise | `[10]` | `[]` |
| 2 | Dequeue `10`; walk `10→20→30`; enqueue child of 10 (`11`) and child of 20 (`21`) | `[11, 21]` | `10→20→30` |
| 3 | Dequeue `11`; walk `11→12`; enqueue child of 12 (`100`) | `[21, 100]` | `10→20→30→11→12` |
| 4 | Dequeue `21`; walk `21→22`; no children found | `[100]` | `10→20→30→11→12→21→22` |
| 5 | Dequeue `100`; walk `100`; no children found | `[]` | `10→20→30→11→12→21→22→100` |
| 6 | Queue empty — done | `[]` | **Final result** ✅ |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — where n is the total number of nodes across all levels. Every node is visited exactly once: once when it is walked during traversal and once when it is appended to the result. Even at 10,000 nodes, this runs in microseconds on modern hardware.

### Space Complexity

**O(w)** — where w is the maximum number of sub-lists active at any single depth level (the maximum "width" of the tree). In the worst case this approaches O(n), but in typical hierarchical data it is far smaller. No recursion stack is used, making this memory-safe for deeply nested inputs.

---

## 💡 Key Takeaways *(For Everyone)*

- **Ordering by depth before position is a common real-world requirement** — from rendering web page assets to processing organisational hierarchies, level-by-level ordering prioritises what matters most first.
- **BFS-based flattening is the backbone of many content-delivery and UI-rendering systems**, where top-level items must appear before nested details regardless of how the data is stored internally.
- **A queue is the natural data structure for BFS** — it enforces the "process this level fully before going deeper" contract through its first-in, first-out behaviour.
- **Enqueuing child heads during traversal (not after) is the key insight** — it preserves left-to-right ordering across all sub-lists at the same depth without needing a separate sorting step.
- **Avoiding recursion here is deliberate and important** — with nesting depths up to 1000, a recursive DFS approach risks stack overflow; the iterative BFS queue sidesteps this entirely.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Depth-First Flattening:** Solve the same problem but flatten using DFS order (follow each child chain to its deepest point before moving to the next node). Compare how the queue in BFS becomes a stack in DFS.
- **Variation 2 — Return Depth Labels:** Modify the solution to return each node paired with its original depth level (e.g., `[(1,1),(2,1),(3,1),(4,2)...]`), useful for debugging hierarchical data pipelines.
- **Variation 3 — Reconstruct the Multilevel List:** Given a flattened list and a list of `(node_value, depth)` pairs, reconstruct the original multilevel structure — the inverse of this problem and a strong test of pointer manipulation skills.

---