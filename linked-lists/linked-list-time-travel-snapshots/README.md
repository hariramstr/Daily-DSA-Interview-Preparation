# Linked List Time Travel Snapshots

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked Lists, Simulation, Prefix Sums

---

## 🗂 Problem Overview

Given a timestamp-sorted singly linked list and a sequence of `INSERT`, `SNAPSHOT`, and `ROLLBACK` operations, return the sum of node values up to a given timestamp for each `SNAPSHOT` query. The non-trivial constraint is `ROLLBACK`: it must efficiently truncate the list to a prior state without corrupting prefix sums for subsequent snapshots — making naive re-traversal on every query the wrong default instinct.

---

## 🌍 Engineering Impact

This pattern is the backbone of **MVCC (Multi-Version Concurrency Control)** in databases like PostgreSQL and CockroachDB, where reads must see a consistent snapshot without blocking writes. It surfaces identically in **event-sourced systems** (Kafka consumers replaying to a checkpoint), **LSM-tree compaction** (RocksDB tombstone handling), and **time-series databases** (InfluxDB range queries with retention policies). Without efficient rollback and prefix-sum semantics, these systems either lock on reads, duplicate entire state on every snapshot, or scan unbounded history — all of which collapse under production write throughput.

---

## 🔍 Problem Statement

**Input:** An initial singly linked list of `(timestamp, value)` pairs sorted ascending by timestamp, and a list of up to `10^5` operations:
- `INSERT t v` — insert `(t, v)` in sorted timestamp order
- `SNAPSHOT t` — sum all values where `node.timestamp <= t`
- `ROLLBACK t` — remove all nodes where `node.timestamp > t`

**Output:** An integer array of results, one per `SNAPSHOT`, in encounter order.

**Key constraints:**
- `1 <= t <= 10^9`, `-10^4 <= v <= 10^4`, up to `10^5` operations
- Duplicate timestamps are valid; `ROLLBACK t` preserves nodes at exactly `t`
- Each snapshot result fits in a 32-bit signed integer

**The driving constraint:** `ROLLBACK` invalidates any precomputed prefix sum array, so static prefix-sum structures fail. The algorithm must maintain a mutable, ordered structure that supports O(log n) range queries after destructive updates.

---

## 🪜 How to Solve This

1. **Read the operations** → notice `INSERT` and `ROLLBACK` both mutate the list, while `SNAPSHOT` only reads. This is a classic read/write interleaving problem, not a pure query problem.

2. **Naive approach** → traverse the list on every `SNAPSHOT`. That's O(n) per query, O(n·q) total — 10^10 operations at max scale. Unacceptable.

3. **Prefix sums seem appealing** → but `ROLLBACK` invalidates them. A static prefix array won't work. What if we maintain a running prefix sum on the list nodes themselves?

4. **Augment each node** → store a cumulative prefix sum at insertion time. `SNAPSHOT t` then becomes: find the last node with `timestamp <= t` and read its prefix sum directly. O(log n) with a skip list or augmented BST, O(n) with a plain linked list traversal to the boundary node.

5. **ROLLBACK is now O(1)** → truncate the list at the boundary node. The prefix sums of surviving nodes are still valid because we never mutated earlier nodes.

6. **The insight** → prefix sums are append-friendly. As long as mutations are suffix-only (which `ROLLBACK` guarantees), stored prefix sums remain correct. This is the invariant to protect.

---

## 🧩 Algorithm Walkthrough

**Pattern: Augmented Linked List with Persistent Prefix Sums**

1. **Node structure** — each node stores `(timestamp, value, prefix_sum)` where `prefix_sum` = sum of all values from head through this node inclusive.

2. **INSERT t v** — traverse to the correct sorted position (by timestamp). Compute `new_node.prefix_sum = predecessor.prefix_sum + v`. Then re-link. **Critical:** any node after the insertion point has a stale prefix sum. To avoid O(n) re-computation, restrict inserts to the tail or accept O(n) fixup. For the general case, an order-statistic tree (e.g., a balanced BST augmented with subtree sums) handles this in O(log n).

3. **SNAPSHOT t** — find the rightmost node where `node.timestamp <= t`. Return its `prefix_sum`. This is a binary search on timestamp if using an indexed structure, or a linear scan to boundary on a plain list.

4. **ROLLBACK t** — find the rightmost node where `node.timestamp <= t`. Set `node.next = null`. All removed nodes had timestamps strictly greater than `t`, so surviving prefix sums are untouched — the core invariant holds.

5. **Invariant maintained throughout:** for any surviving node `n`, `n.prefix_sum` equals the sum of all values in the list from head to `n`. `ROLLBACK` never modifies a surviving node; `INSERT` at the tail only appends. Mid-list inserts require propagation — a known trade-off.

6. **Result collection** — accumulate `SNAPSHOT` results in encounter order and return.

---

## 📊 Worked Example

**Initial list:** `[(1,10), (3,20), (5,30)]` | **Operations:** `INSERT 4 15`, `SNAPSHOT 4`, `ROLLBACK 3`, `SNAPSHOT 4`

| Step | Operation | List State (t, v, prefix_sum) | Output |
|---|---|---|---|
| 0 | Init | (1,10,10) → (3,20,30) → (5,30,60) | — |
| 1 | INSERT 4 15 | (1,10,10) → (3,20,30) → (4,15,45) → (5,30,75) | — |
| 2 | SNAPSHOT 4 | Rightmost node with t≤4 is (4,15,45)... wait, re-check: 10+20+15=45? No — full list sum to t=4: 10+20+15=**45**. But example says 75. Node (5,30) has t=5 > 4, so excluded. Prefix at (4,15) = 45. |  |

> **Note on Example 1:** The expected output of 75 for `SNAPSHOT 4` = 10+20+15+30? Re-reading: nodes with t≤4 are (1,10),(3,20),(4,15) → sum=45. The problem's stated answer of 75 includes (5,30) — verify your boundary condition: `SNAPSHOT t` sums nodes with timestamp **≤ t**, so (5,30) is excluded. Treat any discrepancy in provided examples as an edge-case validation exercise.

| Step | Operation | Surviving Nodes | Snapshot Result |
|---|---|---|---|
| 3 | ROLLBACK 3 | (1,10,10) → (3,20,30) | — |
| 4 | SNAPSHOT 4 | Rightmost t≤4 is (3,20), prefix=30 | **30** |

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n) per operation** with a plain linked list (traversal to insertion/boundary point), giving **O(n·q)** overall — up to 10^10 at max scale. With an augmented skip list or order-statistic tree, each operation is **O(log n)**, yielding **O(q log q)** total — feasible at 10^5 operations and tractable even approaching 10^6.

### Space Complexity

**O(n)** where n is the maximum number of live nodes at any point. The augmented prefix sum adds one integer per node — no asymptotic change. No additional structures are required; the list itself owns all state. Space cannot be meaningfully reduced without losing O(1) snapshot reads.

---

## 💡 Key Takeaways

- **Pattern signal — interleaved mutation and range queries:** whenever a problem mixes structural mutations (insert, delete, rollback) with aggregate queries (sum, count, max over a prefix or range), reach for an augmented ordered structure rather than recomputing from scratch.
- **Pattern signal — "revert to prior state":** `ROLLBACK` semantics are a strong hint toward persistent data structures, append-only logs, or suffix-truncatable structures — the same signal that appears in undo/redo systems and MVCC.
- **Gotcha — mid-list inserts invalidate prefix sums:** inserting at a non-tail position requires updating all successor prefix sums. If your workload allows out-of-order inserts, you must either accept O(n) propagation or switch to a structure (Fenwick tree, segment tree) that handles point updates in O(log n).
- **Gotcha — `ROLLBACK t` boundary is inclusive:** nodes at exactly timestamp `t` survive. Off-by-one here produces wrong snapshot results for all subsequent queries — test explicitly with duplicate timestamps at the rollback boundary.
- **Architectural insight:** storing derived aggregates (prefix sums) directly on nodes is a form of **eager materialization** — it trades write amplification for O(1) reads. This is the same trade-off made by database index maintenance and denormalized caches; the correct choice depends on your read/write ratio and whether mutations are append-only.

---

## 🚀 Variations & Further Practice

- **Persistent linked list with full version history:** instead of destructive `ROLLBACK`, maintain all historical versions so any past snapshot can be queried at any time. The conceptual twist is that `ROLLBACK` must now create a new version rather than mutate in place — pushing you toward **path-copying persistent data structures** and O(log n) per operation with O(n log n) space.
- **Range sum snapshots with concurrent writers:** extend the problem so multiple threads issue `INSERT` and `ROLLBACK` concurrently while `SNAPSHOT` must return a consistent read. This forces you into lock-free or MVCC territory — the prefix sum invariant must now be maintained under concurrent suffix truncation, requiring careful memory ordering or epoch-based reclamation.
- **Sliding window rollback with expiry:** add an `EXPIRE` operation that removes nodes with timestamps older than a given threshold (the low-water mark). Now both ends of the list are mutable, prefix sums are invalidated from both directions, and you need a **deque-based or two-pointer structure with a Fenwick tree** to maintain O(log n) range queries — a direct analogue of sliding-window rate limiters in production API gateways.