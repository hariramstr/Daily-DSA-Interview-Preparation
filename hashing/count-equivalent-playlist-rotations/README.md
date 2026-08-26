# Count Equivalent Playlist Rotations

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, String Algorithms, Array

---

## 🗂 Problem Overview
Given many playlists, each represented as an array of song IDs, count how many unordered pairs are identical up to circular rotation. Playlists with different lengths are never equivalent, and duplicate IDs are allowed. The challenge is scale: brute-force pairwise comparison across up to 100,000 playlists is too expensive, so the solution must normalize each playlist into a rotation-invariant signature and count equal signatures efficiently.

## 🌍 Engineering Impact
This pattern shows up anywhere cyclic structure must be deduplicated or grouped: ring-buffer state snapshots, consistent-hashing token layouts, circular genome fragments, repeated event schedules, and compiler or query-plan memoization where structurally equivalent forms differ only by starting offset. At production scale, naive pairwise equivalence checks collapse under quadratic behavior and unpredictable latency. A canonical-form-plus-hash strategy converts expensive equivalence testing into linear ingestion plus constant-time grouping. That shift matters operationally: it enables streaming aggregation, bounded CPU per record, and clean partitioning across workers without cross-comparing every object against every other object.

## 🔍 Problem Statement
You are given `playlists`, where each element is an array of song IDs. Two playlists are equivalent if one can be transformed into the other by circular rotation only; reversal does not count. Playlists of different lengths cannot match. Return the total number of unordered equivalent pairs.

Constraints are large enough to rule out all-pairs comparison:

- `1 <= playlists.length <= 100000`
- `1 <= total number of song IDs across all playlists <= 200000`
- `1 <= playlists[i].length <= 200000`
- `0 <= song IDs <= 1000000000`
- Sum of all playlist lengths does not exceed `200000`

Examples:

- `[[1,2,3],[2,3,1],[3,1,2],[1,3,2],[5],[5]] -> 4`
- `[[8,8,1],[8,1,8],[1,8,8],[2,2],[2,2],[2],[3,4,3]] -> 4`

The algorithmic driver is clear: we need to group by rotational equivalence in near-linear total input size, typically via hashing a canonical rotation such as the lexicographically smallest rotation.

## 🪜 How to Solve This
1. Read the problem → the output is a **count of equivalent pairs**, so this is fundamentally a **grouping** problem, not a comparison problem.

2. Grouping implies a hash map → but raw playlists cannot be keys, because `[1,2,3]` and `[2,3,1]` must land in the same bucket.

3. So each playlist needs a **canonical representation** shared by all of its rotations. The natural choice is its **lexicographically smallest rotation**.

4. Different lengths can never match → length is already encoded in the canonical array, so no special-case grouping is needed beyond normalization.

5. For each playlist:
   - compute its canonical rotation,
   - serialize or hash that canonical form,
   - increment its frequency in a map.

6. Once frequencies are known, each group of size `f` contributes `f * (f - 1) / 2` unordered pairs.

7. The only nontrivial subproblem is finding the smallest rotation efficiently. Doing that by checking every rotation would be quadratic per playlist. Use a linear-time smallest-rotation algorithm such as **Booth’s algorithm**, which makes the overall solution scale with total input size.

## 🧩 Algorithm Walkthrough
1. **Use canonicalization + hashing as the core pattern.**  
   The right abstraction is: convert each equivalence class into one stable representative, then count identical representatives. Hashing is not proving equivalence by itself; canonicalization is.

2. **For each playlist, compute its lexicographically smallest rotation.**  
   Apply **Booth’s algorithm** on the playlist array. Conceptually, compare candidate starting indices over the doubled sequence `A + A` and discard starts that cannot be minimal.  
   **Why correct:** Booth’s algorithm guarantees the returned start index is the smallest rotation in lexicographic order.  
   **Invariant:** at every step, eliminated start positions cannot produce the minimal rotation.

3. **Materialize the canonical signature.**  
   Once the minimal start index `s` is known, build the normalized sequence  
   `A[s], A[s+1], ..., A[s+n-1]` with modular indexing.  
   **Why correct:** all rotations of the same playlist map to the same minimal rotation; non-equivalent playlists do not.

4. **Store the signature in a hash map.**  
   Use a serialized tuple/string or a robust rolling/hashable vector representation as the key, and increment its count.  
   **Invariant:** after processing `k` playlists, the map contains exact frequencies of canonical forms among those `k`.

5. **Compute the answer from frequencies.**  
   For each signature with count `f`, add `f * (f - 1) / 2`.  
   **Why correct:** this is the number of unordered pairs in a group of size `f`.

6. **Complexity stays linear in total input size.**  
   Booth’s algorithm is `O(m)` for a playlist of length `m`, so across all playlists the total work is `O(totalSongIds)`, plus hash-map overhead.

## 📊 Worked Example
Take `playlists = [[1,2,3],[2,3,1],[3,1,2],[1,3,2],[5],[5]]`.

| Playlist | Smallest rotation | Signature count after insert |
|---|---|---|
| `[1,2,3]` | `[1,2,3]` | `{[1,2,3]: 1}` |
| `[2,3,1]` | `[1,2,3]` | `{[1,2,3]: 2}` |
| `[3,1,2]` | `[1,2,3]` | `{[1,2,3]: 3}` |
| `[1,3,2]` | `[1,3,2]` | `{[1,2,3]: 3, [1,3,2]: 1}` |
| `[5]` | `[5]` | `{[1,2,3]: 3, [1,3,2]: 1, [5]: 1}` |
| `[5]` | `[5]` | `{[1,2,3]: 3, [1,3,2]: 1, [5]: 2}` |

Now compute pairs per group:

1. Signature `[1,2,3]` has frequency `3` → `3 * 2 / 2 = 3`
2. Signature `[1,3,2]` has frequency `1` → `0`
3. Signature `[5]` has frequency `2` → `2 * 1 / 2 = 1`

Total = `3 + 0 + 1 = 4`.

## ⏱ Complexity Analysis
### Time Complexity
Let `S` be the sum of all playlist lengths. Computing the minimal rotation for one playlist of length `m` takes `O(m)` with Booth’s algorithm, so total canonicalization cost is `O(S)`. Hash-map updates are amortized `O(1)` per playlist. At million-element scale this is practical; quadratic comparison is not. At billion-element scale, even linear scans require distributed processing and compact signatures.

### Space Complexity
`O(S)` in the worst case, dominated by stored canonical signatures in the hash map when most playlists are distinct. Extra working memory per playlist is `O(1)` beyond the output key material. Space can be reduced with compact hashing, trading away collision-free exactness unless verified.

## 💡 Key Takeaways
- If the problem asks for counting pairs under an equivalence relation, the first instinct should be “find a canonical key, then group with a hash map.”
- When equality is defined modulo rotation, “smallest rotation” is the canonicalization signal; sorting would destroy order and solve the wrong problem.
- Duplicate values matter: repeated IDs like `[8,8,1]` break naive heuristics that assume a unique pivot or first minimum.
- Be careful with all-equal arrays and modular indexing when reconstructing the canonical rotation; these are common off-by-one failure points.
- At system scale, canonicalization turns expensive many-to-many equivalence checks into local normalization plus cheap aggregation, which is the difference between quadratic collapse and streamable throughput.

## 🚀 Variations & Further Practice
- Count equivalent pairs where rotation **or reversal of any rotation** is allowed; now the canonical form must minimize across both cyclic directions.
- Group 2D cyclic structures such as circular logs with wildcard entries; the twist is matching under rotation while tolerating partial unknowns.
- Detect whether one sequence appears as a rotation of another in a stream; this shifts from canonicalization to online matching with rolling hash or KMP over doubled input.