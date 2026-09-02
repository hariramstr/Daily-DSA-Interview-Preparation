# Count User Pairs With the Same Relative Notification Delays

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Array, Sorting

---

## 🗂 Problem Overview
Given many users, each with a list of notification timestamps, count how many unordered user pairs share the same relative delay pattern. For each user, sort timestamps and normalize them by subtracting the first timestamp, producing a canonical signature like `[0, d1, d2, ...]`. Users are equivalent only if these signatures match exactly, including length and duplicate delays. The challenge is scale: up to `10^5` users, so pairwise comparison is infeasible.

## 🌍 Engineering Impact
This pattern shows up anywhere absolute time is noisy but relative timing is the signal: notification delivery analysis, distributed tracing, fraud detection, telemetry deduplication, and event-sequence clustering in streaming pipelines. In production, comparing every sequence against every other sequence collapses immediately under cardinality growth. Canonicalization plus hashing turns an equivalence problem into a counting problem, which is the difference between a system that can classify millions of event traces online and one that requires offline batch joins. The broader architectural move is to normalize away irrelevant dimensions before grouping.

## 🔍 Problem Statement
You are given `users`, where `users[i]` is an integer array of notification times for one user during a day. Two users are equivalent if, after sorting their timestamps, they produce the same delay signature relative to the first timestamp: for sorted times `[t0, t1, t2, ...]`, the signature is `[0, t1 - t0, t2 - t0, ...]`.

Users with different numbers of notifications cannot match. Duplicate timestamps are allowed and must be preserved after sorting, so `[7,7,9]` becomes `[0,0,2]`. A single notification always yields `[0]`.

Return the number of unordered equivalent user pairs.

**Constraints**
- `1 <= users.length <= 100000`
- `1 <= users[i].length <= 100`
- `0 <= users[i][j] <= 10^9`
- `sum(users[i].length) <= 2 * 10^5`

**Examples**
- `[[5,10,20],[100,105,115],[3,8,18],[7,7,9],[20,20,22]] -> 4`
- `[[4],[9],[1,4,4],[10,13,13],[2,5,6]] -> 2`

The scale constraint rules out `O(n^2)` pair checking.

## 🪜 How to Solve This
1. Read the equivalence rule → absolute timestamps do not matter, only relative offsets from the earliest notification.
2. That means each user can be transformed into a canonical representation independent of start time.
3. To build that representation, sort the user’s timestamps first; otherwise the same multiset in different orders would look different.
4. After sorting, subtract the first timestamp from every element. Now equivalent users produce identical signatures.
5. Once the problem becomes “count identical signatures,” the natural tool is a hash map from signature → frequency.
6. Process users one by one: generate signature, look up how many times it has already appeared, and add that count to the answer.
7. Then increment the stored frequency for that signature.
8. This avoids nested comparisons entirely. Instead of asking “does this user match every earlier user?”, you ask “how many earlier users already normalized to the same key?”
9. The result is a standard canonicalization + hashing pattern: normalize first, group second, count pairs incrementally.

## 🧩 Algorithm Walkthrough
1. **Apply the canonicalization + hashing pattern.**  
   The right abstraction is not pairwise comparison but equivalence-class counting. Canonicalization removes irrelevant absolute offsets; hashing groups identical normalized sequences efficiently.

2. **For each user, sort their timestamps in nondecreasing order.**  
   This is required because the signature is defined on sorted times. Preserving duplicates matters: `[7,7,9]` and `[7,9]` are different because multiplicity changes the signature length and values.

3. **Build the delay signature by subtracting the first sorted timestamp from every element.**  
   The first value is always `0`. If two users differ only by a constant shift, subtracting their respective first timestamps makes them identical. This maintains the invariant that equivalent users map to the same canonical key.

4. **Serialize the signature into a hashable key.**  
   Depending on language, use a tuple, immutable list, or delimiter-safe string. The invariant here is one-to-one mapping: equal signatures must produce equal keys, and unequal signatures must not collide structurally.

5. **Use a hash map `freq` from signature key to count.**  
   Before incrementing `freq[key]`, add its current value to the answer. Why this works: if the key has already appeared `f` times, the current user forms exactly `f` new unordered pairs with prior equivalent users.

6. **Increment the frequency and continue.**  
   After processing all users, the accumulated answer equals the sum over all signature groups of `f * (f - 1) / 2`, computed online without a second pass.

## 📊 Worked Example
Using `users = [[5,10,20],[100,105,115],[3,8,18],[7,7,9],[20,20,22]]`:

| Step | Sorted User Data | Signature | `freq` Before | Pairs Added | Total |
|---|---|---|---:|---:|---:|
| 1 | `[5,10,20]` | `[0,5,15]` | 0 | 0 | 0 |
| 2 | `[100,105,115]` | `[0,5,15]` | 1 | 1 | 1 |
| 3 | `[3,8,18]` | `[0,5,15]` | 2 | 2 | 3 |
| 4 | `[7,7,9]` | `[0,0,2]` | 0 | 0 | 3 |
| 5 | `[20,20,22]` | `[0,0,2]` | 1 | 1 | 4 |

Trace:
1. First signature starts a new group.
2. Second matches that group, forming one new pair.
3. Third matches again, forming two new pairs with the previous two.
4. Fourth starts a different group.
5. Fifth matches the fourth, adding one more pair.

Final answer: `4`.

## ⏱ Complexity Analysis
### Time Complexity
For each user with `k` timestamps, sorting costs `O(k log k)` and signature construction costs `O(k)`. Across all users, total time is `O(Σ k log k)`, bounded by the input size and small per-user length. At `10^6` elements this is practical; at `10^9`, repeated sorting and materialization would require a different storage and streaming model.

### Space Complexity
`O(Σ k)` in the worst case for storing canonical signatures in the hash map, plus temporary space for sorting or signature construction per user. Space is owned primarily by the frequency map. You can reduce transient allocations with in-place sort and compact key encoding, trading readability for lower allocator pressure.

## 💡 Key Takeaways
• If the problem says absolute values do not matter but relative structure does, look for canonicalization before comparison.  
• If you need to count equivalent groups under a transformation, think hash map keyed by the canonical form, not nested matching.  
• Do not forget to sort each user’s timestamps first; the signature is defined on sorted order, not input order.  
• Preserve duplicates when building the signature; collapsing repeated timestamps changes equivalence semantics and produces wrong counts.  
• In production systems, normalizing away irrelevant dimensions early turns expensive join-style comparisons into cheap aggregation over stable keys.

## 🚀 Variations & Further Practice
- Count pairs where signatures match up to reversal as well as translation; the twist is choosing a canonical orientation before hashing.
- Group event sequences by consecutive gaps instead of offsets from the first timestamp; the twist is that the canonical form changes shape and may better support streaming updates.
- Support online inserts and deletes of users while maintaining the pair count; the twist is dynamic frequency maintenance and decrement-safe bookkeeping.

