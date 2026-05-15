# Merge Alternating Train Cars

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Two Pointers, In-Place Manipulation

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have two separate lines of train cars and you want to combine them into one train by picking cars one at a time from each line — first one from Line A, then one from Line B, then back to Line A, and so on. If one line runs out of cars first, you simply attach whatever is left from the other line to the end. The goal is to produce one neatly merged train.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

This "alternating merge" pattern appears everywhere data from two sources must be blended fairly and efficiently. Music streaming platforms like Spotify use similar logic to interleave songs from different playlists into a seamless queue. News aggregators alternate headlines from multiple feeds to ensure no single source dominates the top of the page. In logistics, scheduling systems merge delivery routes from two depots to balance driver workloads. Doing this *in-place* — without creating extra copies of the data — directly reduces memory costs and improves response times, which translates to lower infrastructure bills and a faster user experience.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Picture two queues of people waiting to board a single-file roller coaster. The ride operator lets one person from Queue A board, then one from Queue B, then back to Queue A, and so on. If Queue A empties first, everyone remaining in Queue B just files in at the end. Your job is to describe the final boarding order — without making anyone stand in a brand-new, third line.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given the heads of two singly linked lists, `trainA` and `trainB`, merge them into a single linked list by alternating nodes — starting with a node from `trainA` — and return the head of the merged list. The merge must be performed **in-place**: no new nodes may be allocated; only `next` pointers are re-linked.

**Constraints:**
- Each list contains `[0, 1000]` nodes.
- `0 <= Node.val <= 10000`
- Either list may be empty; return the other list unchanged.

**Examples:**

| Input | Output |
|---|---|
| `trainA = [1, 3, 5]`, `trainB = [2, 4, 6]` | `[1, 2, 3, 4, 5, 6]` |
| `trainA = [10, 20]`, `trainB = [1, 2, 3, 4]` | `[10, 1, 20, 2, 3, 4]` |

In Example 2, `trainA` is exhausted after node `20`; the remaining `trainB` nodes `[3, 4]` are appended directly.

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **two-pointer in-place re-linking** strategy. Both pointers advance through their respective lists simultaneously, and we stitch nodes together by redirecting `next` pointers.

1. **Handle edge cases first.** If either `trainA` or `trainB` is `null`/`None`, return the non-null list immediately. There is nothing to interleave.

2. **Initialise two pointers.** Set `currA` to the head of `trainA` and `currB` to the head of `trainB`. These track our current position in each list as we walk forward.

3. **Save the next pointers before overwriting.** At each iteration, store `nextA = currA.next` and `nextB = currB.next`. Because we are about to change `currA.next`, we must save where it originally pointed so we do not lose the rest of the list.

4. **Re-link alternately.** Point `currA.next` to `currB` (inserting a B-node after the current A-node), then point `currB.next` to `nextA` (re-attaching the rest of A after the inserted B-node). This creates the A → B → A → B weave.

5. **Advance both pointers.** Move `currA` to `nextA` and `currB` to `nextB`, stepping forward in both lists simultaneously.

6. **Repeat until one list is exhausted.** The loop condition is `currA != null AND currB != null`. When either runs out, the remaining nodes of the other list are already correctly attached (because the last re-link pointed to the remaining tail), so no extra work is needed.

7. **Return `trainA`'s original head**, which is the head of the merged list.

---

## 📊 Worked Example *(For Developers)*

Using **Example 2**: `trainA = [10 → 20]`, `trainB = [1 → 2 → 3 → 4]`

| Step | `currA` | `currB` | `nextA` | `nextB` | List state after re-link |
|------|---------|---------|---------|---------|--------------------------|
| Start | 10 | 1 | 20 | 2 | — |
| 1 | 10 | 1 | 20 | 2 | `10 → 1 → 20 → ...` |
| Advance | 20 | 2 | `null` | 3 | — |
| 2 | 20 | 2 | `null` | 3 | `10 → 1 → 20 → 2 → null → ...` |
| Advance | `null` | 3 | — | — | Loop exits |
| Tail | — | — | — | — | `2.next` already points to `3 → 4` |

**Final result:** `10 → 1 → 20 → 2 → 3 → 4` ✅

The remaining `trainB` tail (`3 → 4`) is automatically included because `currB.next = nextA` set `2.next = null`, but `3 → 4` was already hanging off `2` before we touched it — wait, more precisely: after the last re-link, `2.next` was set to `nextA` which is `null`, but `3 → 4` is still reachable from the saved `nextB`. Since the loop exits and `currB = 3`, the chain `3 → 4` remains intact and attached.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(min(m, n))** — where `m` and `n` are the lengths of `trainA` and `trainB`. The loop runs only as long as *both* lists have nodes remaining. Once the shorter list is exhausted, we stop. Even for lists of 1,000 nodes each, this completes in at most 1,000 iterations — effectively instant at any realistic scale.

### Space Complexity

**O(1)** — constant extra memory. We use only two pointer variables (`currA`, `currB`) and two temporary next-savers regardless of input size. No new nodes, arrays, or recursive call stack frames are created, making this highly memory-efficient.

---

## 💡 Key Takeaways *(For Everyone)*

- **Fairness by design:** Alternating merges are a simple, proven way to blend two data sources without favouring either — useful in scheduling, content feeds, and load balancing.
- **In-place operations save real money:** Avoiding extra memory allocation reduces infrastructure costs at scale, especially in high-throughput systems processing millions of records.
- **Always save your pointers before re-linking:** The most common bug in linked-list manipulation is overwriting a `next` pointer before saving where it pointed — losing the rest of the list permanently.
- **Two pointers are a powerful pattern:** Maintaining one pointer per list and advancing them in lockstep is a reusable technique that solves many linked-list and array problems efficiently.
- **Edge cases are first-class citizens:** Handling empty inputs upfront keeps the core logic clean and prevents null-pointer errors that would otherwise crash production systems.

---

## 🚀 Try It Yourself *(For Developers)*

- **Merge K lists alternately:** Extend this approach to interleave nodes from `K` linked lists in round-robin order — how would you manage `K` pointers efficiently?
- **Weighted alternating merge:** Instead of strict 1-for-1 alternation, take 2 nodes from `trainA` for every 1 from `trainB` — modify the re-linking logic to support a configurable ratio.
- **In-place merge with sorted order:** Combine this problem with the classic "merge two sorted lists" challenge — can you interleave *and* maintain ascending order in a single O(n) pass?