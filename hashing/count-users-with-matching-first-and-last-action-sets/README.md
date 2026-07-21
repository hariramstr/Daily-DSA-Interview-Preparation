# Count Users With Matching First and Last Action Sets

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Arrays, Set

---

## 🗂 Problem Overview
Given a chronological log of `[userId, action]` records, count users whose first contiguous session and last contiguous session contain the same **set** of distinct actions. A session is a maximal adjacent block for the same user in the global log. Users with only one session count automatically. The challenge is detecting session boundaries and comparing per-session action sets in one pass under `2 * 10^5` records, where quadratic regrouping or per-user rescans are unnecessary and too expensive.

## 🌍 Engineering Impact
This pattern shows up in clickstream analytics, fraud detection, sessionized event pipelines, and user-behavior summarization over append-only logs. The core issue is not just grouping by user, but grouping by user **and** preserving session boundaries induced by global ordering. In production streaming systems, losing that distinction collapses separate visits into one aggregate and corrupts retention, funnel, or anomaly metrics. A hashing-based approach enables linear-time processing, bounded per-user state, and straightforward adaptation to Kafka consumers, Flink operators, or warehouse pre-aggregation jobs where full-history rescans are operationally unacceptable.

## 🔍 Problem Statement
You are given `logs`, where each entry is `[userId, action]`, in chronological order. For each user, define:

- **First session**: the first maximal contiguous block of records for that user.
- **Last session**: the last maximal contiguous block of records for that user.

Within a session, only the **distinct** actions matter; order and duplicates do not.

Return the number of users whose first-session action set equals their last-session action set. If a user appears in exactly one session, count that user because first and last refer to the same block.

Constraints:
- `1 <= logs.length <= 2 * 10^5`
- At most `2 * 10^5` distinct users
- `userId` and `action` are case-sensitive strings

Examples:

- `logs = [["u1","open"],["u1","click"],["u2","open"],["u1","click"],["u1","open"],["u2","open"]]` → `2`
- `logs = [["a","x"],["a","x"],["b","y"],["a","z"],["a","z"],["c","m"],["c","n"]]` → `2`

The scale constraint rules out nested comparisons or rebuilding sessions per user after the fact.

## 🪜 How to Solve This
1. Read the problem → the key word is **contiguous**. This is not “all actions by user”; it is “actions in each adjacent run for that user.”
2. Once you notice runs, the log should be processed as a sequence of **sessions**, not individual records.
3. For each session, you need only two things: the `userId` and the set of distinct actions in that run.
4. For each user, only the **first** session set and the **most recent** session set matter. Middle sessions can be discarded after updating the “last” value.
5. That suggests a single left-to-right scan:
   - detect session boundaries when `userId` changes,
   - build the current session’s action set,
   - on session close, write it into a hash map keyed by user.
6. If the user is new, store this set as both first and last.
7. If the user already exists, leave first unchanged and overwrite last.
8. After the scan, compare first vs. last for each user and count matches.

The mental model is: **stream sessions, retain only boundary summaries**.

## 🧩 Algorithm Walkthrough
1. **Use a single-pass sessionization + hashing pattern.**  
   The right abstraction is a streaming scan with hash-based per-user state. We are not sorting, because chronological order defines sessions. We are not using two pointers over a sorted key space, because adjacency in the original log is the actual grouping rule.

2. **Track the current open session.**  
   Maintain:
   - `currentUser`
   - `currentSet` = distinct actions seen in the current contiguous block  
   As long as consecutive records have the same `userId`, insert actions into `currentSet`.

3. **Close a session when the user changes.**  
   When `logs[i][0] != currentUser`, the previous session is complete. This is correct because a session is a **maximal** contiguous block. No future record can belong to that session once another user appears.

4. **Persist only first and last session sets per user.**  
   In a hash map `user -> {firstSet, lastSet}`:
   - if user is unseen, set both `firstSet` and `lastSet` to the closed session set;
   - otherwise, overwrite only `lastSet`.  
   Invariant: after processing any prefix of the log, `firstSet` is the user’s earliest completed session in that prefix, and `lastSet` is the latest completed one.

5. **Finalize the trailing session.**  
   After the loop, flush the last open session into the map. This is the standard end-of-stream boundary case.

6. **Count equal pairs.**  
   Iterate over all users and compare `firstSet` with `lastSet`. Set equality is exact membership equality, independent of insertion order or duplicates in the raw logs.

This is correct because every session is processed exactly once, and each user’s retained state is exactly the minimum needed to answer the question.

## 📊 Worked Example
Use:

`[["u1","open"],["u1","click"],["u2","open"],["u1","click"],["u1","open"],["u2","open"]]`

| Step | Record | Current Session | Closed? | Stored State |
|---|---|---|---|---|
| 1 | `u1, open` | `u1 -> {open}` | No | `{}` |
| 2 | `u1, click` | `u1 -> {open, click}` | No | `{}` |
| 3 | `u2, open` | switch user | Close `u1` | `u1: first={open,click}, last={open,click}` |
| 4 | `u2, open` active | `u2 -> {open}` | No | unchanged |
| 5 | `u1, click` | switch user | Close `u2` | `u2: first={open}, last={open}` |
| 6 | `u1, open` | `u1 -> {click, open}` | No | unchanged |
| end | flush trailing session | Close `u1` | Yes | `u1: first={open,click}, last={click,open}` |

Final comparison:
- `u1`: `{open, click} == {click, open}` → count
- `u2`: `{open} == {open}` → count

Answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + U * S_cmp)` in practice, where `n` is the number of log records and `U` is the number of users. Building sessions is linear. Final comparisons depend on set equality costs, but each action is inserted once per session, so the dominant work is still linear in total input size. At `10^6` records this is routine; at `10^9`, only a streaming/distributed variant is realistic.

### Space Complexity
`O(U + A_session_summaries)`, effectively `O(total distinct actions retained across first/last sessions per user)`. The owning structures are the per-user first/last sets plus the current session set. You can reduce memory by canonicalizing sets into hashed signatures, trading exactness guarantees unless collisions are handled carefully.

## 💡 Key Takeaways
- If the problem says “contiguous block,” think sessionization over the original sequence, not plain grouping by key.
- If only boundary summaries matter, store first/last state per entity and discard intermediate detail.
- The main bug is forgetting to flush the final session after the loop.
- Another common mistake is comparing lists or raw event sequences instead of deduplicated action sets.
- In production pipelines, preserving ordering-derived boundaries is often more important than preserving every raw event.

## 🚀 Variations & Further Practice
- Count users whose first and last sessions have the same actions **and the same frequencies**; the twist is replacing set equality with multiset equality.
- Process the same problem in a distributed stream with out-of-order events; the twist is that session boundaries now depend on watermarking or reorder buffers.
- Return all users grouped by canonical session signature pair `(firstSet, lastSet)`; the twist is designing stable, memory-efficient set canonicalization.