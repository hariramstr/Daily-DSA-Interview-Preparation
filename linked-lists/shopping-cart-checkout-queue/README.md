# Shopping Cart Checkout Queue

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Linked Lists &nbsp;|&nbsp; **Tags:** Linked List, Two Pointers, Prefix Sum

---

## 🗂 Problem Overview

Given the head of a singly linked list, determine whether the sum of values in the first half equals the sum of values in the second half. For odd-length lists, the middle node is excluded from both halves. The core constraint that makes this non-trivial: O(n) time and O(1) extra space, which eliminates the obvious approach of copying values into an array and using index arithmetic. You must find the midpoint, accumulate sums, and compare — all in a single pass or tightly bounded traversal without auxiliary storage.

---

## 🌍 Engineering Impact

This pattern — partitioning a linear sequence and comparing aggregate properties of each half without materializing the full structure — appears in stream processing pipelines where buffering is expensive, such as Kafka consumer lag balancing and sliding-window anomaly detection. It underpins checksum validation in network packet framing, where a receiver must verify payload symmetry without reassembling the full buffer. In distributed rate-limiters, the same two-pointer midpoint logic drives token bucket partitioning across time windows. Getting this wrong at scale means either excessive memory pressure from unnecessary copies or incorrect partition boundaries that silently corrupt aggregate metrics.

---

## 🔍 Problem Statement

Given the `head` of a singly linked list where each node holds an item price (`1 <= Node.val <= 1000`), return `true` if the sum of the first half equals the sum of the second half. For odd-length lists, the middle node is excluded.

**Constraints:**
- Node count: `[1, 10^4]`
- O(n) time, O(1) extra space

**Examples:**

| Input | Output | Reason |
|---|---|---|
| `[3, 7, 2, 7, 3]` | `true` | First half `[3,7]` = 10, second half `[7,3]` = 10 |
| `[1, 4, 9, 2]` | `false` | First half `[1,4]` = 5, second half `[9,2]` = 11 |

**Edge cases to consider:** single-node list (trivially `true`), two-node list (no middle exclusion), and all equal values. The O(1) space constraint is the forcing function — it eliminates prefix sum arrays and forces in-place pointer manipulation.

---

## 🪜 How to Solve This

1. **Read the constraint first** → O(1) space rules out copying values to an array. The solution must work directly on the list structure.

2. **The core sub-problem is finding the midpoint** → Without random access, you can't index to `n/2`. This is the classic slow/fast pointer (Floyd's) setup: slow moves one step, fast moves two. When fast reaches the end, slow is at the midpoint.

3. **Now you have two sub-problems: sum the first half, sum the second half** → The slow pointer naturally traverses the first half during the midpoint search. Track a running sum as slow advances — you get the first-half sum for free.

4. **Second half sum** → Once slow reaches the midpoint (adjusted for odd/even length), continue traversing the remainder of the list and accumulate the second-half sum.

5. **Compare** → No reversal needed, no extra storage. Two traversals of at most n/2 nodes each, one pointer tracking state. The insight: the midpoint search and the first-half summation are the same traversal.

---

## 🧩 Algorithm Walkthrough

**Pattern: Two Pointers (slow/fast) + inline accumulation**

This is the right abstraction because the slow/fast pointer technique gives you O(1)-space midpoint detection on a singly linked list — the only structure that doesn't support index-based bisection.

**Steps:**

1. **Initialize** `slow = head`, `fast = head`, `firstSum = 0`. The slow pointer will traverse the first half; fast will detect when we've reached the midpoint.

2. **Advance pointers** in a loop: while `fast` and `fast.next` are non-null, move `slow` one step and `fast` two steps. Add `slow.val` to `firstSum` before advancing slow. This accumulates the first-half sum inline.

3. **Handle odd-length lists**: when the loop exits, if `fast.next` is null, the list length is odd and `slow` is currently on the middle node. Skip it: `slow = slow.next`. If `fast` is null, length is even and `slow` is already at the start of the second half.

4. **Accumulate second-half sum**: traverse from `slow` to the end, summing values into `secondSum`.

5. **Return** `firstSum == secondSum`.

**Invariant maintained**: at every step, `firstSum` holds the exact sum of all nodes the slow pointer has visited, and the slow pointer is always `⌊steps_of_fast / 2⌋` nodes from the head.

---

## 📊 Worked Example

Input: `[3, 7, 2, 7, 3]` (odd length = 5)

**Phase 1 — midpoint search with first-half accumulation:**

| Step | slow.val | fast position | firstSum | Note |
|------|----------|---------------|----------|------|
| Init | 3 | node(3) | 0 | before loop |
| 1 | 3 | node(2) | 3 | slow→7, fast→2 |
| 2 | 7 | null (past end) | 10 | slow→2, fast exhausted |

Loop exits. `fast` is null → odd list. Skip middle: `slow` advances to node(7).

**Phase 2 — second-half accumulation from node(7):**

| Node | secondSum |
|------|-----------|
| 7 | 7 |
| 3 | 10 |

`firstSum (10) == secondSum (10)` → return `true`.

---

## ⏱ Complexity Analysis

### Time Complexity

**O(n)** — the slow pointer traverses at most n/2 nodes in phase 1 and at most n/2 nodes in phase 2. The fast pointer traverses at most n nodes total. All operations are constant per node. At 10⁶ elements this is a single-digit millisecond operation; at 10⁹ it remains linear and cache-friendly, bounded only by memory bandwidth.

### Space Complexity

**O(1)** — only a fixed set of scalar variables (`slow`, `fast`, `firstSum`, `secondSum`) are allocated regardless of input size. No auxiliary structure owns heap space. This cannot be reduced further; the two pointer variables are the theoretical minimum for this traversal.

---

## 💡 Key Takeaways

- **Pattern signal — midpoint on a linked list**: any problem requiring bisection of a linked list without knowing its length upfront is a slow/fast pointer candidate. The moment you see "first half vs. second half" on a list, reach for this pattern.
- **Pattern signal — O(1) space + aggregate comparison**: when a problem asks you to compare properties of two halves of a sequence under a space constraint, look for ways to compute both aggregates in the same traversal rather than storing intermediate results.
- **Gotcha — odd vs. even length handling**: the exit condition of the fast pointer loop differs by one depending on parity. Off-by-one here means slow lands on the middle node instead of past it, silently including the excluded middle value in one of the sums.
- **Gotcha — when to add to firstSum**: accumulate `slow.val` before advancing slow, not after. Advancing first and then reading means you miss the first node's value entirely, a subtle bug that only manifests when the first-half sum is wrong by exactly `head.val`.
- **Architectural insight**: the pattern of computing a running aggregate during pointer advancement — rather than as a separate pass — is directly applicable to stream processors and pipeline stages where you want to avoid buffering: compute-while-traversing is the production-grade idiom for memory-constrained linear scans.

---

## 🚀 Variations & Further Practice

- **Palindrome Linked List (LeetCode #234)**: instead of comparing sums, compare individual node values between halves. The twist: you must reverse the second half in-place to enable forward comparison, introducing mutation and the need to restore the list afterward — a harder correctness and concurrency concern in production.
- **Partition List by Value with Balanced Weights**: given a target value `k`, partition the list into nodes `< k` and nodes `>= k`, then verify the two partitions have equal weight sums. The added complexity is that the partition boundary is value-driven, not positional, so slow/fast pointers no longer directly apply and you need a different invariant to maintain O(1) space.
- **Sliding Window Sum Balance on a Stream**: extend the problem to a data stream where you must continuously report whether the last `2n` elements are balanced as new elements arrive. This forces you to think about prefix sums, circular buffers, and eviction — the O(1) space constraint becomes an O(window) space constraint with strict bounds, bridging this toy problem to real-time analytics pipelines.