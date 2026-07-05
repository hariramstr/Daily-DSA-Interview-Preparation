# Count Users With Duplicate Daily Action Sets

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Sets, Counting

---

## 🗂 Problem Overview
You are given a day’s event stream as `[userId, action]` pairs and must count how many distinct users belong to a group where at least two users have the exact same set of unique actions. Event order is irrelevant, and repeated actions for the same user must be deduplicated. The non-trivial part is scale: with up to 200,000 events and 100,000 users, pairwise set comparison is too expensive, so each user’s action set must be normalized into a hashable canonical form.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to group entities by behavior signatures rather than raw event sequences: product analytics cohorts, fraud detection, feature-flag exposure analysis, search/query intent clustering, compiler symbol usage summaries, and streaming pipelines that deduplicate state before aggregation. At scale, naive pairwise comparison explodes into quadratic work and excessive memory churn. Canonicalization plus hashing turns equivalence testing into linear aggregation, which is the difference between a batch job that fits in a daily SLA and one that falls over under cardinality spikes. It also creates a reusable boundary: normalize once, count many times.

## 🔍 Problem Statement
Given `events`, a list of `[userId, action]` pairs for a single day, compute how many distinct users have a daily action set identical to at least one other user’s set. A daily action set contains only distinct actions performed by that user; duplicate occurrences of the same action are ignored.

Two users match if their distinct action sets are exactly equal, regardless of event order or action frequency.

Constraints:
- `1 <= events.length <= 200000`
- Each event has exactly 2 values: `[userId, action]`
- `1 <= userId.length, action.length <= 20`
- Strings contain only lowercase English letters and digits
- At most `100000` distinct users

Example 1:
- Input: `events = [["u1","view"],["u2","click"],["u1","click"],["u2","view"],["u3","view"],["u3","view"]]`
- Output: `2`

Example 2:
- Input: `events = [["a","login"],["a","upload"],["b","upload"],["b","login"],["c","login"],["d","pay"],["d","refund"],["e","refund"],["e","pay"]]`
- Output: `4`

The key constraint is user cardinality: comparing every user’s set against every other user is not viable.

## 🪜 How to Solve This
1. Read the problem → this is not about event counts or sequence matching. It is about **equivalence classes of sets**.
2. For each user, first build the set of distinct actions. That immediately removes noise from repeated events like multiple `"view"` entries.
3. Once each user has a set, ask: how do we compare many sets efficiently? Direct set-to-set comparison across all users is quadratic.
4. The standard move is **canonicalization** → convert each set into a deterministic representation so equal sets produce the same key.
5. Since sets are unordered, sort each user’s distinct actions and join them into a string key, or use another stable hashable encoding.
6. Now the problem becomes simple grouping: count how many users map to each canonical key.
7. Any key with frequency `>= 2` represents a matching group, and every user in that group contributes to the answer.
8. So the flow is: `events -> userId => action set -> canonical key -> frequency map -> sum frequencies where count >= 2`.

That chain is the core insight: normalize first, then hash-group.

## 🧩 Algorithm Walkthrough
1. **Build per-user distinct action sets** using a hash map from `userId` to `Set<action>`.  
   Why: repeated actions by the same user must count once.  
   Invariant: after processing any prefix of events, each user’s stored set equals the distinct actions seen so far for that user.

2. **Canonicalize each user’s set** by converting the set to a list, sorting it, and joining with a delimiter.  
   Pattern: **Hashing + canonical representation**.  
   Why: sets are unordered and not directly safe as hash keys in most languages. Sorting gives a deterministic encoding, so equal sets produce identical keys.

3. **Count canonical signatures** in a second hash map from `signature` to number of users with that signature.  
   Why: the problem is now pure frequency counting over normalized group identifiers.  
   Invariant: after processing any subset of users, the signature map stores exact group sizes for those users.

4. **Aggregate the result** by summing frequencies for all signatures whose count is at least 2.  
   Why: the question asks for the number of users in matching groups, not the number of matching groups. A signature seen 3 times contributes `3`, not `1`.

5. **Return the total**.  
   Correctness follows from two facts: deduplication ensures each user is represented by their true action set, and canonicalization ensures two users collide iff their sets are exactly equal.

This abstraction is right because the hard part is not searching or ordering events; it is turning an unordered equivalence relation into a stable hash key.

## 📊 Worked Example
Use Example 1:

`events = [["u1","view"],["u2","click"],["u1","click"],["u2","view"],["u3","view"],["u3","view"]]`

| Step | Event | userActions state |
|---|---|---|
| 1 | `["u1","view"]` | `u1 -> {view}` |
| 2 | `["u2","click"]` | `u1 -> {view}`, `u2 -> {click}` |
| 3 | `["u1","click"]` | `u1 -> {view, click}`, `u2 -> {click}` |
| 4 | `["u2","view"]` | `u1 -> {view, click}`, `u2 -> {click, view}` |
| 5 | `["u3","view"]` | `u3 -> {view}` added |
| 6 | `["u3","view"]` | unchanged, duplicate action |

Canonical signatures:
- `u1 -> "click|view"`
- `u2 -> "click|view"`
- `u3 -> "view"`

Signature counts:
- `"click|view" -> 2`
- `"view" -> 1`

Only signatures with frequency at least 2 contribute to the result, so answer = `2`.

## ⏱ Complexity Analysis
### Time Complexity
Building per-user action sets is `O(E)` average, where `E` is the number of events. Canonicalization costs `O(sum over users of k_u log k_u)`, where `k_u` is the number of distinct actions for user `u`. In practice, sorting each user’s unique actions dominates. This scales comfortably at `10^6` events, but at `10^9` you would need streaming, partitioning, or bounded vocab encodings.

### Space Complexity
Space is `O(U + A)` in aggregate terms: the user-to-set map stores all distinct user/action memberships, and the signature map stores up to one entry per user. You can reduce peak memory with external aggregation or streaming partitions, but only by trading simplicity and latency.

## 💡 Key Takeaways
- If the problem says “same regardless of order” and “duplicates should count once,” think **set normalization before comparison**.
- If many entities must be grouped by structural equality, think **canonical representation + hash map**, not pairwise comparison.
- Do not count raw events; repeated actions for the same user must be deduplicated before signature generation.
- The final answer is the sum of users in groups with frequency `>= 2`, not the number of duplicate signatures.
- In production systems, canonicalization is often the key boundary that converts messy behavioral data into stable, composable aggregation keys.

## 🚀 Variations & Further Practice
- Count users whose action sets are identical **across multiple days**, where the harder part is composing `(day, signature)` keys efficiently without exploding memory.
- Group users by **multiset** of actions instead of set, where action frequencies matter and canonicalization must encode counts, not just membership.
- Detect users whose action sets differ by at most one action, which turns exact hashing into approximate matching and requires locality-sensitive indexing or neighborhood generation.