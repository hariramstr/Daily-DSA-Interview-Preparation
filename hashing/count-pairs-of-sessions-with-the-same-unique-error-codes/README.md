# Count Pairs of Sessions With the Same Unique Error Codes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Arrays, Set Canonicalization

---

## 🗂 Problem Overview
Given a list of sessions, where each session is an array of error codes, count how many index pairs `(i, j)` with `i < j` have exactly the same **set of distinct codes**. Repetitions inside a session do not matter, and order does not matter. The challenge is scale: up to `100000` sessions, so comparing every pair is infeasible. The core task is to derive a canonical representation for each session’s unique codes and count equal representations efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to collapse noisy event streams into stable identities: observability pipelines deduplicating incident signatures, security systems grouping sessions by triggered rule sets, search/query engines canonicalizing token sets, and compiler or build systems memoizing dependency fingerprints. At small scale, pairwise comparison or ad hoc normalization works; at production scale, it explodes in CPU and memory churn. Canonicalization plus hashing turns an equivalence problem into a counting problem. That shift enables linear aggregation, cacheability, partition-friendly processing, and predictable behavior under high-cardinality workloads.

## 🔍 Problem Statement
You are given `sessions`, where `sessions[i]` is a non-empty integer array representing error codes observed in the `i`-th session. Two sessions are equivalent if their sets of distinct error codes are identical. Multiplicity is ignored, and ordering is irrelevant.

Return the number of pairs `(i, j)` such that `i < j` and `sessions[i]` and `sessions[j]` are equivalent.

Constraints:

- `1 <= sessions.length <= 100000`
- `1 <= sessions[i].length <= 100`
- `0 <= sessions[i][j] <= 1000000000`
- Total number of error codes across all sessions `<= 300000`

Examples:

- `[[4,7,4,9],[9,4,7],[1,2,2],[2,1],[5]] -> 2`
- `[[8,8,8],[8],[1,3,1,3],[3,1],[2,2,4],[4,2,4],[2,4,5]] -> 4`

The decisive constraint is the session count: `O(n^2)` pair checking is impossible, so the solution must group equivalent sessions in near-linear time.

## 🪜 How to Solve This
1. Start from the equivalence rule → sessions are equal if their **distinct-code sets** match. That means raw arrays are the wrong comparison unit.
2. If we need to group equivalent objects efficiently → think **hash map**. But a hash map needs a stable key.
3. What key represents a set of integers uniquely? → remove duplicates inside one session, then sort the remaining codes. The sorted unique list is a canonical form.
4. Once every session has a canonical form, the problem becomes: how many previous sessions had the same key?
5. Iterate through sessions once:
   - build canonical key for current session,
   - look up how many times that key has appeared,
   - add that count to the answer,
   - increment the key’s frequency.
6. Why this works: if a key has already appeared `f` times, the current session forms exactly `f` new valid pairs.
7. This avoids nested comparisons entirely. The only real work is canonicalizing each session, which is cheap because each session length is at most `100`.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonicalization pattern.**  
   The problem is not about comparing arrays directly; it is about grouping by set-equivalence. Hashing is the right abstraction because we want frequency counts of normalized representations.

2. **For each session, compute its canonical representation.**  
   Insert its codes into a temporary set to remove duplicates. Then convert that set to a list and sort it.  
   Why correct: two sessions are equivalent iff they contain the same distinct values, and sorting gives a deterministic ordering for that set.  
   Invariant: equal distinct-code sets always produce identical canonical lists.

3. **Serialize the canonical list into a hashable key.**  
   Depending on language, this can be a tuple, vector used directly as a map key, or a delimiter-safe string.  
   Why correct: the key must preserve exact element boundaries and order after sorting.  
   Invariant: different sets never collide at the representation layer unless the serialization is flawed.

4. **Count pairs incrementally with a frequency map.**  
   Let `freq[key]` be how many prior sessions had this canonical representation. Add `freq[key]` to the answer, then increment `freq[key]`.  
   Why correct: each previous identical session forms one new pair with the current session.  
   Invariant: after processing index `i`, the answer equals the number of valid pairs among sessions `0..i`.

5. **Return the accumulated answer.**  
   Use a 64-bit integer for safety: in the worst case, pair counts can exceed 32-bit range.  
   This is the standard “count previous equal normalized objects” pattern, common in deduplication and signature aggregation pipelines.

## 📊 Worked Example
Example: `sessions = [[4,7,4,9],[9,4,7],[1,2,2],[2,1],[5]]`

| i | session        | unique sorted key | freq before | pairs added | total |
|---|----------------|-------------------|-------------|-------------|-------|
| 0 | [4,7,4,9]      | [4,7,9]           | 0           | 0           | 0     |
| 1 | [9,4,7]        | [4,7,9]           | 1           | 1           | 1     |
| 2 | [1,2,2]        | [1,2]             | 0           | 0           | 1     |
| 3 | [2,1]          | [1,2]             | 1           | 1           | 2     |
| 4 | [5]            | [5]               | 0           | 0           | 2     |

Trace:
1. First session creates key `[4,7,9]`.
2. Second session normalizes to the same key, so it forms one pair with session `0`.
3. Third session creates a new key `[1,2]`.
4. Fourth session matches `[1,2]`, forming one pair with session `2`.
5. Fifth session is unique.

Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
Let `N` be the number of sessions and `K` the maximum session length. For each session, deduplication is `O(K)` expected, and sorting the distinct codes is `O(U log U)` where `U <= K`. Total time is `O(totalCodes + Σ U log U)`, effectively `O(N * K log K)` in the worst case. This scales comfortably for the given limits, but not for `10^9` elements without distribution or streaming partitioning.

### Space Complexity
The frequency map stores one entry per distinct canonical set, so space is `O(M * U)` across unique keys, plus `O(U)` temporary space per session. Space can be reduced only by using compressed fingerprints, at the cost of collision risk or more complex verification.

## 💡 Key Takeaways
- If equality ignores order and duplicates, the real object is usually a **set**, and the solution often starts with canonicalization.
- When the task asks for counting equivalent groups or pairs, a **hash map over normalized keys** is the default pattern to test first.
- Do not sort the raw session without removing duplicates first; `[8,8,8]` and `[8]` must normalize to the same key.
- Use a delimiter-safe serialization or a native tuple/list key; naive string concatenation can create ambiguous keys.
- In production systems, canonical representations convert noisy, high-volume event data into stable identities that can be counted, cached, partitioned, and joined efficiently.

## 🚀 Variations & Further Practice
- Count pairs where sessions are equivalent **up to one extra code**: now exact-key hashing is insufficient, and you need subset/superset-aware indexing.
- Group sessions by **multiset** equality instead of set equality: duplicates now matter, so canonicalization must preserve frequencies.
- Process sessions in a **streaming/distributed** setting with bounded memory: the harder twist is maintaining canonical counts across partitions while controlling serialization cost and skew.