# Count User Pairs With Matching Distinct Login Hours

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Bitmask, Counting

---

## 🗂 Problem Overview
Given login records of the form `[userId, hour]`, determine how many unordered pairs of different users share exactly the same **set of distinct login hours**. Repeated logins in the same hour do not matter. The challenge is scale: up to 200,000 records and 100,000 users make pairwise user comparison too expensive. The key is to derive a compact per-user signature, then count how many users map to the same signature.

## 🌍 Engineering Impact
This pattern shows up anywhere raw events must be reduced into canonical user or entity signatures before grouping: fraud detection, feature-store generation, behavioral cohorting, access-pattern analysis, and streaming analytics. In production pipelines, the difference between comparing entities pairwise and hashing normalized signatures is the difference between linear-ish throughput and quadratic collapse. The same idea underpins compiler symbol deduplication, search indexing fingerprints, and distributed telemetry aggregation. Canonicalization enables partitioning, caching, and incremental recomputation; without it, state explodes and downstream joins become both slower and harder to reason about.

## 🔍 Problem Statement
You are given `records`, where each element is `[userId, hour]`. `userId` is a string of length `1..20`, and `hour` is an integer in `0..23`. A user may appear many times, including multiple times for the same hour. For each user, only the **distinct** hours matter.

Two users are considered equivalent if their distinct login-hour sets are identical. Return the number of unordered pairs of different users that are equivalent.

Constraints:
- `1 <= records.length <= 200000`
- `records[i].length == 2`
- `0 <= hour <= 23`
- Up to `100000` distinct users

Examples:

- `records = [["alice",1],["alice",3],["alice",3],["bob",3],["bob",1],["cara",2],["cara",5]]`
  - Output: `1`

- `records = [["u1",0],["u1",23],["u2",23],["u2",0],["u3",0],["u3",0],["u4",5]]`
  - Output: `1`

The decisive constraint is the large input size, which rules out comparing every user against every other user.

## 🪜 How to Solve This
1. Start from the definition of equivalence → two users match if their **distinct hour sets** are equal.
2. Distinct set equality suggests canonicalization → every user needs a stable representation of their hours.
3. Hours are only `0..23` → that is small enough to encode as a 24-bit bitmask instead of storing a dynamic set.
4. Process each record once → for user `u` and hour `h`, set bit `h` in `u`’s mask. Duplicate logins naturally collapse because setting an already-set bit changes nothing.
5. After one pass, each user has a compact signature describing exactly their distinct hours.
6. Now the problem becomes counting equal signatures across users → classic hashing/frequency counting.
7. For each mask frequency `f`, contribute `f * (f - 1) / 2` unordered pairs.
8. This avoids both per-user sorting and pairwise comparison. The small fixed domain of hours is the clue that turns a generic “group by set” problem into a very efficient bitmask + hash map solution.

## 🧩 Algorithm Walkthrough
1. **Build per-user signatures using a bitmask**.  
   Pattern: **Hashing + Bitmask canonicalization**. Maintain a hash map `userToMask`. For each record `[userId, hour]`, update `userToMask[userId] |= (1 << hour)`. This is correct because each bit uniquely represents one hour, and OR-ing preserves the set of distinct observed hours.

2. **Maintain the invariant**.  
   After processing the first `i` records, `userToMask[u]` equals the exact set of distinct hours seen so far for user `u`. Duplicate records do not violate the invariant because OR is idempotent for an already-set bit.

3. **Count identical signatures**.  
   Iterate through all final masks in `userToMask` and build `maskFreq[mask]++`. This groups users by equivalence class: users are hour-equivalent iff their masks are equal.

4. **Convert frequencies into pair counts**.  
   For each signature with frequency `f`, add `f * (f - 1) / 2` to the answer. This counts unordered pairs of distinct users sharing that exact signature.

5. **Why this abstraction fits**.  
   The problem is not about ordering or adjacency; it is about **canonical representation of a small bounded set**. Bitmasking is the right abstraction because the universe size is fixed at 24, making the signature compact, comparable in O(1), and hashable without extra normalization work.

## 📊 Worked Example
Use:

`[["alice",1],["alice",3],["alice",3],["bob",3],["bob",1],["cara",2],["cara",5]]`

| Step | Record | userToMask after update |
|---|---|---|
| 1 | `["alice",1]` | `alice = 0010` |
| 2 | `["alice",3]` | `alice = 1010` |
| 3 | `["alice",3]` | `alice = 1010` (unchanged) |
| 4 | `["bob",3]` | `alice = 1010`, `bob = 1000` |
| 5 | `["bob",1]` | `alice = 1010`, `bob = 1010` |
| 6 | `["cara",2]` | `cara = 0100` |
| 7 | `["cara",5]` | `cara = 100100` |

Final masks:
- `alice -> {1,3}`
- `bob -> {1,3}`
- `cara -> {2,5}`

Frequency by mask:
- mask for `{1,3}`: `2`
- mask for `{2,5}`: `1`

Pairs:
- `2 * 1 / 2 = 1`
- `1 * 0 / 2 = 0`

Answer: `1`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + u)`, where `n` is the number of records and `u` is the number of distinct users. Each record performs an O(1) hash lookup and bitwise OR; each user contributes once to frequency counting. At `10^6` records this is still practical; at `10^9`, throughput and memory locality dominate, but the algorithmic shape remains optimal.

### Space Complexity
`O(u)`, owned primarily by the `userToMask` map and the signature-frequency map. It cannot be reduced below per-user state in a single exact pass unless input is pre-grouped or externalized; the trade-off would be sorting, partitioning, or approximate counting.

## 💡 Key Takeaways
- If the problem asks whether entities have the same **set of observed attributes**, look for a canonical signature and hash-based grouping.
- If the attribute domain is tiny and fixed, a bitmask is usually better than a general-purpose set or sorted list.
- Duplicate user-hour records must not inflate counts; use bitwise OR so repeated observations are naturally deduplicated.
- Hours are `0..23`, so the correct bit operation is `1 << hour`; off-by-one errors appear if you accidentally treat hours as `1..24`.
- In production systems, canonicalizing event streams into compact signatures is what makes large-scale grouping, caching, and downstream aggregation tractable.

## 🚀 Variations & Further Practice
- Count pairs where users differ by exactly one hour instead of matching exactly; the twist is generating neighboring masks efficiently without quadratic comparison.
- Extend hours to arbitrary timestamps and compare distinct day-part buckets or rolling windows; the harder part is defining a stable signature under evolving time semantics.
- Support online updates and pair-count queries after each event; the twist is maintaining frequency counts incrementally as a user’s signature changes.