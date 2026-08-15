# Count Mirror-Shifted Coupon Codes

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String, Canonical Representation

---

## 🗂 Problem Overview
Given an array of lowercase coupon codes, count how many unordered index pairs belong to the same mirror-shifted class. Two codes are equivalent if every character in one can be shifted by the same amount modulo 26 to obtain the other, which also implies equal length. The challenge is avoiding pairwise comparison across up to 100,000 strings while total input size remains large enough that only near-linear processing is acceptable.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must collapse many syntactically different values into a stable equivalence class: compiler symbol normalization, streaming dedup pipelines, search query canonicalization, fraud-signature grouping, and telemetry aggregation. At scale, brute-force comparison explodes quadratically and destroys latency budgets. Canonical representation plus hashing turns “compare everything with everything” into “compute once, count once.” That shift matters operationally: it enables shard-local aggregation, bounded-memory streaming, predictable throughput, and straightforward parallelization because equivalence is encoded as a deterministic key rather than an expensive relational check.

## 🔍 Problem Statement
You are given `codes`, an array of non-empty strings containing only lowercase English letters. Two codes are in the same mirror-shifted group if one can be transformed into the other by applying the same cyclic shift to every character, with wraparound from `z` to `a`. Codes of different lengths can never match.

Return the number of unordered pairs `(i, j)` such that `i < j` and `codes[i]` and `codes[j]` are in the same group.

Constraints:

- `1 <= codes.length <= 100000`
- `1 <= codes[i].length <= 100000`
- `codes[i]` contains only lowercase English letters
- Sum of all string lengths `<= 200000`

Examples:

- `["abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"] -> 5`
- `["aa", "bb", "ab", "za", "yx"] -> 4`

The key constraint is total scale: nested comparisons across strings are infeasible, so the solution must reduce each string to a canonical signature and count matches with hashing.

## 🪜 How to Solve This
1. Read the equivalence rule → this is not about exact string equality, but about grouping strings under a transformation.
2. Grouping problems usually imply a hash map → the real question becomes: what key uniquely identifies a mirror-shifted class?
3. Absolute characters do not matter; relative movement does. If every character is shifted by the same amount, then the differences between consecutive characters stay unchanged modulo 26.
4. So normalize each string into a signature like `(s[1]-s[0], s[2]-s[1], ...) mod 26`.  
   - `"abc"` → `(1,1)`  
   - `"bcd"` → `(1,1)`  
   - `"xyz"` → `(1,1)`
5. Single-character strings have no internal differences, so they all share the same empty signature and therefore belong to one group.
6. Once each string has a signature, counting pairs is standard frequency aggregation: if a signature has appeared `k` times already, the next occurrence adds `k` new pairs.
7. This avoids post-processing combinations and keeps the pass linear in total input size.

## 🧩 Algorithm Walkthrough
1. **Use canonical representation + hashing.**  
   The right abstraction is **hash-based grouping by canonical form**. We are not searching, sorting, or comparing ranges; we are collapsing each string into an equivalence-class key.

2. **For each code, build its signature.**  
   For a string `s` of length `m`, compute the sequence of `m - 1` consecutive differences:  
   `diff[i] = (s[i] - s[i-1] + 26) % 26`.  
   This is correct because adding the same shift to every character cancels out in adjacent differences. The invariant is: all strings in the same mirror-shifted group produce identical signatures.

3. **Include length implicitly through signature size.**  
   Different lengths cannot match, and the signature length is `m - 1`, so length separation is automatic. No extra field is required if the signature encoding preserves boundaries.

4. **Hash the signature in a map.**  
   Let `count[signature]` be how many prior strings produced this signature. Before incrementing it, add `count[signature]` to the answer. This works because each previous occurrence forms exactly one new unordered pair with the current string.

5. **Handle single-character strings naturally.**  
   Their signature is empty. Every single-character code maps to that same key, which is correct because any one-letter string can be shifted into any other.

6. **Return the accumulated pair count.**  
   The invariant after processing index `i` is: `answer` equals the number of valid pairs among `codes[0..i]`, and `count` stores exact frequencies of all signatures seen so far.

## 📊 Worked Example
Example: `codes = ["abc", "bcd", "ace", "xyz", "az", "ba", "a", "z"]`

| Step | Code | Signature | Prior Count | Pairs Added | Total |
|---|---|---|---:|---:|---:|
| 1 | `abc` | `(1,1)` | 0 | 0 | 0 |
| 2 | `bcd` | `(1,1)` | 1 | 1 | 1 |
| 3 | `ace` | `(2,2)` | 0 | 0 | 1 |
| 4 | `xyz` | `(1,1)` | 2 | 2 | 3 |
| 5 | `az` | `(25)` | 0 | 0 | 3 |
| 6 | `ba` | `(25)` | 1 | 1 | 4 |
| 7 | `a` | `()` | 0 | 0 | 4 |
| 8 | `z` | `()` | 1 | 1 | 5 |

Result: `5`.

The important observation is that signatures capture relative structure, not starting letters. `"abc"`, `"bcd"`, and `"xyz"` all reduce to `(1,1)`, so they collapse into one hash bucket.

## ⏱ Complexity Analysis
### Time Complexity
`O(total_length)` where `total_length` is the sum of all string lengths. Each character is visited once while building signatures, and each signature is inserted or looked up once in the hash map. At `10^6` characters this is routine; at `10^9`, throughput, allocation strategy, and hash-key encoding become the real bottlenecks.

### Space Complexity
`O(total_length)` in the worst case, dominated by stored signature keys in the hash map when most strings fall into distinct groups. It can be reduced only by using a more compact signature encoding or rolling hash, trading memory for collision-handling complexity.

## 💡 Key Takeaways
- If the problem says “equivalent under uniform transformation,” look for an invariant representation rather than direct comparison.
- If you need pair counts across equivalence classes, hash frequencies and add prior count on each insert instead of generating combinations explicitly.
- Use modulo arithmetic carefully: `(curr - prev + 26) % 26`, not just `curr - prev`, or wraparound cases like `"za"` break.
- Single-character strings are a deliberate edge case: their signature is empty, and they all belong to the same group.
- In production systems, canonicalization is often the difference between quadratic relational logic and linear, shard-friendly aggregation.

## 🚀 Variations & Further Practice
- Count groups under **arbitrary alphabet size or Unicode code-point cycles**; the harder part is defining stable normalization and efficient key encoding for larger symbol spaces.
- Support **online updates and deletions** of codes; the conceptual twist is maintaining pair counts incrementally as frequencies change over time.
- Group strings where equivalence allows **rotation plus shift**; now the canonical form must factor out two transformations, often requiring doubled-string techniques or minimal-rotation normalization.