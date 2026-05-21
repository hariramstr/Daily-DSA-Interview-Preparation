# Inbox Message Thread Flattener

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Stacks and Queues &nbsp;|&nbsp; **Tags:** Queue, BFS, Tree, Hash Map, Simulation

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine your email inbox, where messages can be replies to other messages, forming conversation threads. This problem asks you to build the logic that organises those messages into a tree of conversations and then retrieves all messages at a specific reply depth — for example, "show me every second-level reply across all threads" — as new messages keep arriving in real time.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Thread-based messaging is everywhere — Gmail, Slack, Reddit, GitHub comments, and customer support platforms like Zendesk all rely on this exact structure. Efficiently organising and querying threaded conversations directly impacts how quickly users find relevant replies, how support agents triage tickets, and how platforms surface the most engaged discussions. A slow or broken thread engine means frustrated users, missed support escalations, and lost revenue. Getting this right is foundational to any product built around communication.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a company-wide email chain. Some emails start fresh conversations; others are replies to replies. Picture this as a family tree: top-level emails are grandparents, direct replies are children, and replies-to-replies are grandchildren. Now imagine someone asks: "Show me every grandchild email, in the order they arrived." Your job is to answer that question instantly, even as new emails keep arriving and the family tree keeps growing.

---

## 🔍 Technical Problem Statement *(For Developers)*

You are given a stream of events of two types:

- **`SEND id parent_id`** — Insert a node with unique integer `id` as a child of `parent_id`. If `parent_id == 0`, the node is a root (depth 0).
- **`READ depth`** — Return all node IDs currently at exactly `depth` levels from the root(s), in BFS insertion order.

Return one result list per `READ` event, in the order the `READ` events appear.

**Constraints:** Up to 10⁴ events; IDs in range [1, 10⁵]; all IDs unique; `parent_id` always refers to a previously sent message; depth in [0, 500].

**Example:**
```
Input:  SEND 1 0, SEND 2 0, SEND 3 1, SEND 4 1, SEND 5 3, READ 0, READ 1, READ 2
Output: [[1, 2], [3, 4], [5]]
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Build a children map (Hash Map).** Maintain a `HashMap<id → List<child_ids>>`. On every `SEND`, append the new `id` to its parent's child list. For root messages (`parent_id == 0`), append to a dedicated `roots` list. This gives O(1) insertion and preserves arrival order automatically.

2. **Record each node's depth (Hash Map).** Maintain a second `HashMap<id → depth>`. When inserting, set `depth[id] = depth[parent_id] + 1` (or 0 for roots). This avoids recomputing depth during every `READ`.

3. **Maintain a depth-indexed bucket list.** Keep a `List<List<id>>` where index `d` holds all IDs at depth `d`, appended in arrival order. On `SEND`, simply append `id` to `buckets[depth]`, resizing if necessary. This makes `READ` a pure O(1) list lookup.

4. **Answer READ queries in O(k).** For `READ depth`, return `buckets[depth]` directly (or an empty list if that depth hasn't been reached yet). Because buckets are built incrementally, the list is always up to date.

5. **Why not BFS on every READ?** Running a full BFS per query would cost O(n) per `READ`. Pre-bucketing by depth reduces each query to a direct array access, which is critical when both events and depths are large.

---

## 📊 Worked Example *(For Developers)*

Using **Example 2** — tracing state after each event:

| Step | Event | `children` map | `depth` map | `buckets` |
|------|-------------|--------------------------------|-------------------------|--------------------------|
| 1 | SEND 10 0 | `roots=[10]` | `{10:0}` | `[[10]]` |
| 2 | SEND 20 10 | `roots=[10], 10→[20]` | `{10:0, 20:1}` | `[[10],[20]]` |
| 3 | READ 1 | — | — | **returns `[20]`** ✓ |
| 4 | SEND 30 10 | `roots=[10], 10→[20,30]` | `{10:0, 20:1, 30:1}` | `[[10],[20,30]]` |
| 5 | READ 1 | — | — | **returns `[20, 30]`** ✓ |

**Final output:** `[[20], [20, 30]]`

Notice that bucket index 1 grows in place — no re-traversal needed between the two `READ` events.

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(E)** overall, where E is the number of events. Each `SEND` costs O(1) for map and bucket updates. Each `READ` costs O(k) where k is the number of IDs at the requested depth (unavoidable output cost). At scale with 10⁴ events, this is effectively instantaneous.

### Space Complexity

**O(N)** where N is the total number of messages sent. The children map, depth map, and buckets each store at most one entry per message. Memory usage grows linearly and predictably — no hidden overhead from repeated traversals.

---

## 💡 Key Takeaways *(For Everyone)*

- **Real-time thread organisation is a core feature** of every major communication platform — getting it right keeps users engaged and support teams effective.
- **Pre-computing structure on write is almost always faster than recomputing on read** — the same principle powers database indexes, search engines, and caches.
- **BFS naturally models depth in trees** — its level-by-level expansion maps directly onto the concept of "reply depth" in threaded conversations.
- **Hash maps are the right tool for dynamic parent-child relationships** — they give O(1) lookup and insertion without requiring a fixed tree structure upfront.
- **Bucket/bin indexing by a known attribute (depth) eliminates repeated traversal** — whenever you find yourself re-scanning a structure to answer the same category of query, consider pre-sorting into buckets on insert.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Range reads:** Modify the problem so `READ d1 d2` returns all messages at depths between `d1` and `d2` inclusive. How does your bucketing strategy need to change, and can you still answer in O(k) time?
- **Variation 2 — Delete events:** Add a `DELETE id` event that removes a message and all its descendants. How do you efficiently update the buckets and depth map without a full re-scan?
- **Variation 3 — Most active depth:** After all events, return the single depth level containing the most messages. Can you track this incrementally during `SEND` events so the final answer is O(1)?

---