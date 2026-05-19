# Cyclic Shift of Linked List Segments

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Simulation, Two Pointers

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a sequence of items in a line. This problem asks you to repeatedly take a specific portion of that line and shift it — moving items from the back of that portion to the front. You do this multiple times, each time on a potentially different portion. The goal is to figure out what the final arrangement looks like after all the shifts are done.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Reordering segments of a sequence is a surprisingly common operation in real systems. Music streaming platforms like Spotify reorder song queues based on user preferences mid-playback. Financial trading systems reorder pending transactions within specific time windows to optimise settlement. Video editors rearrange clips within a timeline segment without disturbing the rest. Getting this operation fast and correct directly impacts user experience, processing throughput, and operational cost — especially when thousands of reordering operations must be applied in real time.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture a row of numbered seats in a cinema. An usher is given a list of instructions: "Take seats 2 through 5 and move the last two people in that group to the front of it." Then another instruction: "Now take the whole row and move the last three people to the front." Each instruction reshuffles only a specific section of the row, and you must follow them in order to find out where everyone ends up sitting.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given the `head` of a singly linked list and a list of segment descriptors, apply each rotation in order and return the modified list's head. Each descriptor `(left, right, k)` means: rotate the sublist spanning 1-indexed positions `left` to `right` (inclusive) to the right by `k` positions. A right rotation by `k` moves the last `k` nodes of the sublist to its front. If `k` exceeds the segment length `L`, use `k mod L`. If `k mod L == 0`, the segment is unchanged.

**Constraints:** 1 ≤ n ≤ 10⁴ nodes, values in [-10⁵, 10⁵], 1 ≤ descriptors ≤ 500, 0 ≤ k ≤ 10⁹.

**Example 1:**
- Input: `[1,2,3,4,5,6,7]`, descriptors `[[2,5,2],[1,7,3]]`
- Output: `[2,3,6,7,1,4,5]`

**Example 2:**
- Input: `[10,20,30,40,50]`, descriptors `[[1,3,4],[3,5,1]]`
- Output: `[30,10,50,20,40]`

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Traverse to segment boundaries.** For each descriptor `(left, right, k)`, walk the list to find four key nodes: the node just before position `left` (the "pre-left" anchor), the node at position `left` (segment head), the node at position `right` (segment tail), and the node just after position `right` (the "post-right" anchor). We need these pointers to surgically re-link the list.

2. **Compute effective rotation.** Calculate the segment length `L = right - left + 1`. Reduce `k` to `k mod L`. If the result is `0`, skip this descriptor entirely — no change is needed.

3. **Find the new tail of the segment.** The rotation splits the segment at position `right - k` (within the segment). Walk from the segment head exactly `L - k - 1` steps to land on the new tail node. The node immediately after it becomes the new segment head.

4. **Re-link the four pointer connections.** Connect `pre-left` → new segment head, new segment tail → old segment head, old segment tail → `post-right`. This performs the rotation in O(segment length) time using only pointer manipulation — no extra array or copying needed.

5. **Repeat for every descriptor in order.** Each rotation is independent and applied to the current state of the list, so descriptors must be processed sequentially.

---

## 📊 Worked Example *(For Developers)*

Using **Example 1**, first descriptor `(2, 5, 2)` on list `[1→2→3→4→5→6→7]`:

| Step | Action | Key Nodes / State |
|------|--------|-------------------|
| 1 | Traverse to boundaries | pre-left = node(1), seg-head = node(2), seg-tail = node(5), post-right = node(6) |
| 2 | Compute effective k | L = 4, k = 2 mod 4 = 2 (no change) |
| 3 | Find new tail | Walk L−k−1 = 1 step from node(2) → new-tail = node(3); new-head = node(4) |
| 4 | Re-link pointers | node(1)→node(4), node(3)→node(2), node(5)→node(6) |
| 5 | Result after step 1 | `[1→4→5→2→3→6→7]` ✓ |

Second descriptor `(1, 7, 3)` then rotates the full list right by 3, yielding `[2→3→6→7→1→4→5]`.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(D × n)** where D is the number of descriptors and n is the list length. Each descriptor requires at most one full traversal of the list to locate boundaries and the split point. With up to 500 descriptors and 10,000 nodes, this means at most 5,000,000 operations — fast enough for real-time use.

### Space Complexity

**O(1) auxiliary space.** The algorithm manipulates existing pointers in place and allocates no additional data structures regardless of list size. Memory usage stays constant and predictable, which matters in memory-constrained environments.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real systems reorder data in segments constantly** — from playlist management to transaction batching — making this pattern practically valuable beyond competitive programming.
- **Correctness depends on order** — each rotation modifies the list that the next rotation operates on, so sequence matters and cannot be parallelised naively.
- **Pointer manipulation beats copying** — re-linking nodes in place avoids allocating temporary arrays, keeping memory usage at O(1) regardless of input size.
- **Modular arithmetic is essential** — reducing `k mod L` before acting prevents unnecessary work and handles arbitrarily large `k` values (up to 10⁹) gracefully.
- **Four boundary pointers are the key insight** — identifying pre-left, segment-head, new-tail/new-head split, and post-right before touching anything ensures safe, bug-free re-linking every time.

---

## 🚀 Try It Yourself *(For Developers)*

- **Reverse a sublist instead of rotating it:** Modify the approach to reverse nodes between positions `left` and `right` — a classic linked list problem that uses a similar four-pointer boundary technique (LeetCode #92).
- **Apply descriptors in reverse order:** What if you need to undo all rotations? Explore how to invert each `(left, right, k)` operation and process the descriptor list backwards to restore the original sequence.
- **Batch overlapping segments:** Investigate what happens when multiple descriptors share overlapping ranges and whether any two adjacent descriptors can be merged into a single rotation to reduce total traversal cost.

---