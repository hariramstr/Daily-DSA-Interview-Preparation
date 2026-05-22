# Delete Every K-th Node in a Linked List

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Two Pointers, In-place Modification

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a long chain of items numbered in order. This problem asks you to go through that chain and remove every K-th item — the 3rd, 6th, 9th, and so on — while keeping everything else intact. If you reach the end and there aren't enough items left to complete another full group of K, you simply keep whatever remains.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Selectively removing items from a sequence is a surprisingly common operation in business systems. Email platforms use similar logic to purge every N-th archived message to free up storage costs. Data pipelines use it to downsample sensor readings — for example, keeping only every other temperature reading from an IoT device to reduce database load without losing meaningful trends. Video streaming services like Netflix apply comparable techniques to thin out redundant data frames during compression, directly improving playback speed and reducing bandwidth costs for millions of users.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a long line of people waiting at a theme park. A staff member walks the line and every time they count to K, they politely ask that person to step out. They then reset their count and keep walking. If they reach the end of the line before finishing a full count of K, everyone left simply stays. The result is a shorter, evenly thinned-out line.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given the `head` of a singly linked list and a positive integer `k`, delete every K-th node from the list and return the modified list's head. Nodes are counted starting from 1. Keep nodes at positions 1 through k−1, delete the node at position k, keep the next k−1 nodes, delete again, and repeat until the list is exhausted. Any trailing nodes that do not form a complete group of k are preserved.

**Constraints:**
- Number of nodes: `[1, 10^4]`
- `1 <= Node.val <= 1000`
- `1 <= k <= 10^4`

**Examples:**

| Input | k | Output |
|---|---|---|
| `[1, 2, 3, 4, 5, 6, 7, 8]` | `3` | `[1, 2, 4, 5, 7, 8]` |
| `[10, 20, 30, 40, 50]` | `2` | `[10, 30, 50]` |

---

## 🧩 Approach: How We Solve It *(For Developers)*

We traverse the linked list in a single pass, maintaining a counter to track our position within each group of K nodes. When the counter hits K, we remove that node by rewiring the previous node's `next` pointer to skip over it.

**Step-by-step:**

1. **Handle the edge case where k = 1.** If every node must be deleted, return `null` immediately — no traversal needed.

2. **Initialize a counter and a `prev` pointer.** Set `count = 0` and `prev = null`. We need `prev` to perform the deletion by skipping the target node.

3. **Traverse the list node by node.** For each node, increment `count`.

4. **Check if count equals k.** If yes, this node must be deleted. Wire `prev.next` to `current.next`, effectively unlinking the current node. Reset `count` to `0`. Do NOT advance `prev` — it should remain pointing at the node just before the next group.

5. **If count does not equal k.** Move `prev` forward to `current`, then advance `current` to `current.next`. This keeps `prev` always one step behind the potential deletion target.

6. **Continue until `current` is null.** Return the original `head` (it is never deleted since it is always position 1 within a group).

This approach modifies the list **in place**, requiring no extra data structures.

---

## 📊 Worked Example *(For Developers)*

**Input:** `[1 → 2 → 3 → 4 → 5 → 6 → 7 → 8]`, `k = 3`

| Step | Current Node | Count | Action | List State |
|------|-------------|-------|--------|------------|
| 1 | 1 | 1 | Keep; prev = node(1) | `1→2→3→4→5→6→7→8` |
| 2 | 2 | 2 | Keep; prev = node(2) | `1→2→3→4→5→6→7→8` |
| 3 | 3 | 3 | **Delete**; prev(2).next = node(4); count = 0 | `1→2→4→5→6→7→8` |
| 4 | 4 | 1 | Keep; prev = node(4) | `1→2→4→5→6→7→8` |
| 5 | 5 | 2 | Keep; prev = node(5) | `1→2→4→5→6→7→8` |
| 6 | 6 | 3 | **Delete**; prev(5).next = node(7); count = 0 | `1→2→4→5→7→8` |
| 7 | 7 | 1 | Keep; prev = node(7) | `1→2→4→5→7→8` |
| 8 | 8 | 2 | Keep; prev = node(8) | `1→2→4→5→7→8` |
| 9 | null | — | End of list; return head | **`[1, 2, 4, 5, 7, 8]`** ✅ |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n)** — We visit every node in the list exactly once, performing a constant amount of work at each step (increment a counter, compare, and occasionally rewire a pointer). Even for a list of 10,000 nodes, this completes in a single linear sweep with no nested loops.

### Space Complexity

**O(1)** — The algorithm uses only a fixed number of pointer variables (`prev`, `current`, `count`) regardless of list size. No additional arrays, stacks, or copies of the list are created, making it highly memory-efficient.

---

## 💡 Key Takeaways *(For Everyone)*

- **Selective data pruning is a real cost-saver.** Removing every K-th record from logs, sensor feeds, or archives is a practical way to reduce storage and processing overhead without discarding data randomly.
- **Predictable, rule-based deletion protects data integrity.** Because the removal pattern is deterministic, systems can reproduce or audit exactly which records were removed — important for compliance-sensitive industries.
- **A single traversal is all you need.** Tracking position with a simple counter avoids any need to re-scan the list, making this approach scalable to very large datasets.
- **The `prev` pointer is the key mechanism.** Deletion in a singly linked list requires knowing the node *before* the target — maintaining `prev` throughout the traversal is the critical implementation detail.
- **In-place modification means zero memory overhead.** By rewiring existing pointers rather than building a new list, this solution is safe to use even in memory-constrained environments like embedded systems or microservices with tight resource limits.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Delete every K-th node starting from the end:** Instead of counting from the head, can you delete every K-th node counting backwards? Hint: consider reversing the list first, or using a two-pass approach.
- **Variation 2 — Delete a range instead of a single node:** Modify the algorithm to delete M consecutive nodes after every K kept nodes (a generalisation known as the "keep K, delete M" problem — common in interview circuits).
- **Variation 3 — Doubly linked list:** Adapt the solution for a doubly linked list where each node also has a `prev` pointer, and ensure both forward and backward links are correctly updated on deletion.

---