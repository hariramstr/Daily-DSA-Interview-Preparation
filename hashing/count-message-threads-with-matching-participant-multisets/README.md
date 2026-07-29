# Count Message Threads With Matching Participant Multisets

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Frequency Map, Counting

---

## 🗂 Problem Overview
Given `n` message threads, where each thread is a list of user IDs and repeated IDs represent multiple messages from the same participant, count how many unordered thread pairs have identical participant multisets. Order does not matter; frequencies do. The output is a 64-bit integer because many threads can collapse into the same equivalence class. The non-trivial part is avoiding pairwise comparison across up to `100000` threads while handling large user IDs and repeated values efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to canonicalize unordered-but-counted events: deduplicating telemetry batches, grouping equivalent compiler symbol usages, matching search query term-frequency vectors, or collapsing chat/session fingerprints in analytics pipelines. At scale, brute-force comparison fails both latency and cost targets because equivalence is defined structurally, not by raw order. A canonical hashed representation turns an expensive all-pairs comparison into a linear aggregation pass. That shift enables streaming-friendly grouping, cacheable signatures, stable partition keys, and predictable memory behavior in distributed data processing systems.

## 🔍 Problem Statement
You are given `threads`, where `threads[i]` is the ordered list of user IDs appearing in the `i`-th message thread. A user ID may occur multiple times in a thread. Two threads are equivalent if every user ID appears the same number of times in both threads, regardless of order.

Return the number of unordered pairs `(i, j)` such that `0 <= i < j < n` and `threads[i]` and `threads[j]` are equivalent.

Constraints:

- `1 <= n <= 100000`
- `1 <= total number of user IDs across all threads <= 300000`
- `1 <= threads[i].length <= 100000`
- Sum of all `threads[i].length` is at most `300000`
- `1 <= user ID <= 10^9`
- Return a 64-bit integer

Examples:

- `[[4,1,4,2],[2,4,4,1],[3,3],[1,2,4,4],[3,3,3]] -> 3`
- `[[8,9],[9,8,8],[7],[8,9],[7],[9,8]] -> 4`

The key constraint is that `n` is large enough that comparing every pair is infeasible.

## 🪜 How to Solve This
1. Read the equivalence rule → order is irrelevant, but counts matter. So this is not sequence matching; it is multiset matching.

2. If two threads are equivalent, they should map to the same canonical representation. That immediately suggests hashing or grouping.

3. What should the key be? Not the raw thread, since `[4,1,4,2]` and `[2,4,4,1]` must match. The right key is the frequency map of `userID -> count`.

4. But hash maps are not directly usable as map keys in many languages. So convert each thread’s frequency map into a deterministic canonical form, such as a sorted list of `(userID, count)` pairs.

5. Once every thread has a canonical signature, the problem becomes: how many times does each signature occur?

6. For each signature seen `k` times, it contributes `k * (k - 1) / 2` unordered pairs.

7. This avoids nested thread comparisons entirely. Work is proportional to total input size plus the cost of canonicalizing each thread, which is exactly what the constraints are pushing you toward.

## 🧩 Algorithm Walkthrough
1. **Use the hashing + canonicalization pattern.**  
   The core abstraction is: convert each unordered counted collection into a stable key, then group identical keys with a hash map. This is the right pattern because equivalence depends on content frequencies, not adjacency or ordering.

2. **Process one thread at a time.**  
   Build a local frequency map `freq[userID]++` for the current thread. This correctly captures the participant multiset because every occurrence contributes exactly once.

3. **Canonicalize the frequency map.**  
   Extract `(userID, count)` entries and sort them by `userID`. The invariant is that two equivalent threads produce identical sorted pair sequences, and two non-equivalent threads differ in at least one pair.

4. **Serialize or otherwise encode the sorted pairs into a hashable key.**  
   Examples: tuple/vector of pairs, immutable string encoding, or custom structural hash. Correctness depends on collision-free logical representation, not probabilistic hashing alone.

5. **Count signatures globally.**  
   Maintain `seen[key]`, the number of prior threads with the same canonical multiset.

6. **Accumulate pairs incrementally.**  
   When processing a thread with key `key`, add `seen[key]` to the answer before incrementing it. This works because each previous matching thread forms exactly one new unordered pair with the current thread.

7. **Return a 64-bit result.**  
   The invariant at the end is: every equivalent pair has been counted once, at the moment the later thread in the pair was processed.

## 📊 Worked Example
Example: `threads = [[4,1,4,2],[2,4,4,1],[3,3],[1,2,4,4],[3,3,3]]`

| Step | Thread         | Frequency Map          | Canonical Key                  | `seen[key]` before | Pairs Added | Total |
|------|----------------|------------------------|--------------------------------|--------------------|-------------|-------|
| 1    | `[4,1,4,2]`    | `{4:2,1:1,2:1}`        | `[(1,1),(2,1),(4,2)]`          | 0                  | 0           | 0     |
| 2    | `[2,4,4,1]`    | `{2:1,4:2,1:1}`        | `[(1,1),(2,1),(4,2)]`          | 1                  | 1           | 1     |
| 3    | `[3,3]`        | `{3:2}`                | `[(3,2)]`                      | 0                  | 0           | 1     |
| 4    | `[1,2,4,4]`    | `{1:1,2:1,4:2}`        | `[(1,1),(2,1),(4,2)]`          | 2                  | 2           | 3     |
| 5    | `[3,3,3]`      | `{3:3}`                | `[(3,3)]`                      | 0                  | 0           | 3     |

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
Let `L` be the total number of user IDs across all threads, and let thread `i` have `u_i` distinct user IDs. Building all frequency maps costs `O(L)`. Canonicalizing each thread costs `O(u_i log u_i)` due to sorting distinct IDs. Total time is `O(L + Σ u_i log u_i)`, which is practical under the `300000` total-element cap but far better than `O(n^2)` pairwise comparison.

### Space Complexity
Space is `O(L)` in the worst case: local frequency maps plus the global hash map of canonical signatures. The dominant owner is the stored signature set. You can reduce overhead with custom compact encodings, but usually at the cost of readability and higher collision-risk if you over-compress.

## 💡 Key Takeaways
- If order is irrelevant but multiplicity matters, think multiset canonicalization rather than sequence comparison.
- If the task asks for counting equivalent pairs across many records, the usual move is “build a signature, then aggregate with a hash map.”
- Do not use only the set of user IDs; repeated senders change equivalence and must be preserved in the key.
- Be careful with answer type: pair counts can exceed 32-bit range even when individual threads are small.
- In production systems, canonical structural signatures are often the difference between quadratic comparison logic and linear-time grouping pipelines.

## 🚀 Variations & Further Practice
- Count equivalent thread pairs in a streaming system with bounded memory: the twist is approximate counting or externalized state management instead of full in-memory aggregation.
- Treat two threads as equivalent under user-ID remapping, preserving only frequency shape: now you are canonicalizing count distributions rather than concrete IDs.
- Support online updates where messages are appended to existing threads and pair counts must stay current: the harder part is maintaining mutable signatures incrementally.