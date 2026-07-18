# Count Distinct User Sets per Alert Pattern

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Set Canonicalization, Counting

---

## 🗂 Problem Overview
Given alert records as `(userId, alertCode)` pairs, count how many unordered pairs of distinct alert codes have exactly the same set of triggering users. Duplicate records for the same pair must be ignored, since user sets are distinct-by-definition. The challenge is scale: comparing every alert code against every other alert code by set equality is too expensive, so the solution needs an efficient way to canonicalize and group equivalent user sets.

## 🌍 Engineering Impact
This pattern shows up anywhere systems need to deduplicate or classify entities by the exact membership of related identifiers: fraud signatures by affected accounts, feature flags by exposure cohorts, search indexes by posting lists, compiler intern tables by symbol usage, and streaming pipelines that collapse equivalent event neighborhoods. At small scale, pairwise comparison is tolerable; at production scale it explodes in both CPU and cache behavior. Canonicalizing sets into stable keys turns an expensive equivalence problem into a counting problem, enabling linear-ish aggregation, shard-local precomputation, and predictable memory use.

## 🔍 Problem Statement
You are given `records`, where each element is `[userId, alertCode]`. Multiple identical pairs may appear, but they represent the same user triggering the same alert repeatedly and must contribute only once to that alert’s user set.

For each `alertCode`, define:

- `userSet(alertCode) = { distinct userId values paired with that alertCode }`

Two alert codes are equivalent if their user sets are exactly equal. Return the number of unordered pairs of distinct alert codes that are equivalent.

Constraints:

- `1 <= records.length <= 2 * 10^5`
- `1 <= userId, alertCode <= 10^9`
- Duplicate `(userId, alertCode)` pairs may appear
- Use 64-bit arithmetic for the answer

Examples:

- `[[1,10],[2,10],[2,10],[1,20],[2,20],[3,30],[4,30]] -> 1`
- `[[5,100],[7,100],[5,200],[7,200],[8,300],[8,300],[9,400],[10,400],[9,500],[10,500]] -> 2`

The key constraint is that naive pairwise alert-set comparison is too slow.

## 🪜 How to Solve This
1. Read the problem → the real object is not each record, but each alert’s **distinct user set**.

2. Duplicate `(userId, alertCode)` pairs do not matter → first instinct should be: for each alert, accumulate users in a set.

3. Once every alert has its deduplicated user set, the problem becomes: **how many alert codes share the exact same set?**

4. Equality of sets is awkward to compare repeatedly → give each set a **canonical representation**. The standard move is:
   - collect users for one alert,
   - sort them,
   - use the sorted sequence as the key.

5. Now alerts with identical user sets map to the same canonical key. That turns the problem into frequency counting with a hash map.

6. If one canonical key appears `k` times, it contributes `k * (k - 1) / 2` unordered pairs.

7. This avoids nested alert-to-alert comparisons entirely. The work is concentrated in:
   - building per-alert distinct users,
   - canonicalizing each set once,
   - counting equal keys once.

That is the mental shift: from pairwise equivalence checking to hash-based grouping by canonical form.

## 🧩 Algorithm Walkthrough
1. **Group records by alert code using hashing.**  
   Maintain `Map<alertCode, Set<userId>>`. For each record `[u, a]`, insert `u` into the set for `a`.  
   **Why correct:** this exactly constructs `userSet(a)` and automatically removes duplicate records.  
   **Invariant:** after processing any prefix of input, each alert’s set contains all distinct users seen so far for that alert.

2. **Canonicalize each alert’s user set.**  
   For every alert code, extract its user IDs, sort them, and serialize the sorted list into a stable key.  
   **Pattern:** **Hashing + Set Canonicalization**. This is the right abstraction because set equality is order-independent, while hash maps need deterministic keys.  
   **Why correct:** two sets are equal iff their sorted distinct element lists are identical.

3. **Count equal canonical keys.**  
   Maintain `Map<canonicalKey, count>`. Increment the count for each alert’s canonical representation.  
   **Invariant:** after processing alerts, `count[key]` equals the number of alert codes with that exact user set.

4. **Compute pair contributions.**  
   For each frequency `k` in the canonical-count map, add `k * (k - 1) / 2` to the answer using 64-bit arithmetic.  
   **Why correct:** this is the number of unordered pairs among `k` equivalent alert codes.

5. **Return the total.**  
   This is asymptotically efficient because each alert set is built once, canonicalized once, and counted once. The expensive alternative—comparing every pair of alerts by set equality—is avoided entirely.

## 📊 Worked Example
Use `records = [[1,10],[2,10],[2,10],[1,20],[2,20],[3,30],[4,30]]`.

| Step | Record | alertToUsers state |
|---|---|---|
| 1 | `[1,10]` | `10 -> {1}` |
| 2 | `[2,10]` | `10 -> {1,2}` |
| 3 | `[2,10]` | `10 -> {1,2}` (duplicate ignored) |
| 4 | `[1,20]` | `10 -> {1,2}`, `20 -> {1}` |
| 5 | `[2,20]` | `10 -> {1,2}`, `20 -> {1,2}` |
| 6 | `[3,30]` | `30 -> {3}` added |
| 7 | `[4,30]` | `30 -> {3,4}` |

Canonical keys after sorting:

- `10 -> "1,2"`
- `20 -> "1,2"`
- `30 -> "3,4"`

Frequency map:

- `"1,2" -> 2`
- `"3,4" -> 1`

Pair count:

- for `2`: `2 * 1 / 2 = 1`
- for `1`: `0`

Final answer: **1**.

## ⏱ Complexity Analysis
### Time Complexity
Building `alertCode -> set(userId)` is `O(records.length)` average-case hashing. Canonicalization costs `Σ |S_a| log |S_a|` across alerts due to sorting each distinct user set. Total time is `O(n + Σ |S_a| log |S_a|)`. This is practical at `10^6` scale; pairwise alert comparison would collapse long before `10^9`-class cardinalities.

### Space Complexity
Space is `O(U + A)`, where `U` is the number of distinct `(userId, alertCode)` relationships retained across per-alert sets and `A` is the number of alert codes / canonical keys. Most memory sits in the hash sets. You can reduce peak usage with streaming or external sort strategies, but at higher implementation complexity.

## 💡 Key Takeaways
- If the problem says “two entities are equal when the set of associated items is identical,” think **canonicalize the set and hash it**.
- If naive wording suggests comparing every pair of groups, look for a way to convert equivalence testing into **group-by counting**.
- Do not forget to deduplicate repeated `(userId, alertCode)` records before canonicalization; duplicates must not alter set identity.
- Use 64-bit arithmetic for `k * (k - 1) / 2`; the count of equivalent alert pairs can exceed 32-bit range.
- In production systems, stable canonical representations are often the difference between quadratic reconciliation logic and scalable aggregation pipelines.

## 🚀 Variations & Further Practice
- Count equivalent alert codes when user sets are considered equal up to a transformation, such as user-ID remapping within a tenant; the twist is canonicalizing under an equivalence relation, not exact values.
- Support online updates and queries: insert/delete records and report the current number of equivalent alert-code pairs; the harder part is maintaining canonical counts incrementally.
- Extend from exact set equality to high-overlap detection (for example, Jaccard similarity above a threshold); exact hashing no longer works, so you need sketches such as MinHash or locality-sensitive hashing.