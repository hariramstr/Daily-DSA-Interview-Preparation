# Detect the First Fully Reconciled Invoice Pair

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Table, Array, Pair Matching

---

## 🗂 Problem Overview
Given a stream of invoice records `records[i] = [vendorId, amount]`, return the first pair of indices `[i, j]` where `i < j`, both records belong to the same vendor, and their amounts sum to zero. The result must correspond to the earliest index `j` that completes any valid pair during a left-to-right scan. The non-trivial part is scale: with up to 200,000 records, comparing each record against all prior records is too expensive.

## 🌍 Engineering Impact
This pattern shows up in financial reconciliation pipelines, event deduplication, fraud detection, inventory corrections, and stream processors that must detect the first matching counter-event under partitioned keys. The key architectural issue is keyed correlation under arrival order: matching globally is wrong, and rescanning history per event does not survive production volumes. A hash-indexed approach enables single-pass processing, predictable latency, and straightforward partitioning by business key such as vendor, account, tenant, or session. Without it, systems degrade into quadratic scans, unbounded state lookups, or incorrect cross-entity matches.

## 🔍 Problem Statement
You receive an array `records`, where each element is `[vendorId, amount]`. Two records form a fully reconciled pair if they have the same `vendorId` and their amounts sum to `0`. You must return `[i, j]`, where `i < j` and record `j` is the earliest record in the scan that completes at least one such pair with an earlier record `i`. If no pair is ever completed, return `[-1, -1]`.

Constraints:
- `1 <= records.length <= 200000`
- `records[i].length == 2`
- `1 <= vendorId <= 1000000000`
- `-1000000000 <= amount <= 1000000000`

Examples:
- `[[7,100],[3,50],[7,-100],[7,100],[3,-20]] -> [0,2]`
- `[[5,40],[5,10],[8,-40],[5,-10],[8,40]] -> [1,3]`

The decisive constraint is input size: `O(n^2)` pair checking is not viable, so the solution must find prior complements in near-constant time.

## 🪜 How to Solve This
1. Read the problem carefully → the output is not just “does a pair exist,” but “which pair appears first as we scan left to right.”
2. That immediately suggests a streaming view → when we are at index `j`, we only care whether some earlier record can match it.
3. Matching requires two dimensions:
   - same `vendorId`
   - amount equal to `-currentAmount`
4. That means the lookup key is effectively `(vendorId, amount)` or, operationally, “for this vendor, have we already seen the opposite amount?”
5. Once you see “find prior complement quickly,” think hashing.
6. Maintain a hash map from `(vendorId, amount)` to an earlier index. For each record `[v, a]` at index `j`, first check whether `(v, -a)` already exists.
7. If it does, return that stored index with `j`. This is correct because scanning left to right guarantees the first returned `j` is the earliest completed pair.
8. If not, store the current record for future matches and continue.

The core insight is that order matters only for `j`; hashing removes the need to rescan prior records.

## 🧩 Algorithm Walkthrough
1. **Use the Hashing pattern.**  
   This is a keyed complement lookup problem: for each record, find whether the inverse amount has already appeared under the same vendor. Hashing is the right abstraction because we need constant-time membership checks over a growing prefix of the array.

2. **Initialize a hash map from `(vendorId, amount)` to earliest index.**  
   The map represents all unmatched-or-available prior records seen so far. Storing the earliest index for each key is sufficient because if multiple earlier matches exist, any one is acceptable, and the earliest `j` dominates correctness.

3. **Scan records from left to right.**  
   At index `j`, read `(vendorId, amount) = (v, a)`. Compute the complement key `(v, -a)`.

4. **Check the complement before inserting the current record.**  
   If `(v, -a)` exists in the map at index `i`, return `[i, j]`. This ordering prevents a record from pairing with itself, which matters especially when `amount == 0`.

5. **If no complement exists, insert the current key if absent.**  
   Store `(v, a) -> j`, typically only if the key is not already present. Keeping the earliest index preserves deterministic earliest-left behavior and avoids overwriting useful earlier candidates.

6. **Maintain the invariant.**  
   After processing index `j`, the map contains earliest indices for all `(vendorId, amount)` combinations in `records[0..j]` that may serve as future matches. Therefore, when a match is found, it is the first completed pair in scan order.

7. **If the scan ends with no match, return `[-1, -1]`.**  
   This means no record ever encountered a valid earlier complement under the same vendor.

## 📊 Worked Example
Example: `records = [[5,40],[5,10],[8,-40],[5,-10],[8,40]]`

| j | record   | complement needed | map before check                  | result |
|---|----------|-------------------|-----------------------------------|--------|
| 0 | `[5,40]` | `(5,-40)`         | `{}`                              | store `(5,40)->0` |
| 1 | `[5,10]` | `(5,-10)`         | `{(5,40):0}`                      | store `(5,10)->1` |
| 2 | `[8,-40]`| `(8,40)`          | `{(5,40):0,(5,10):1}`             | store `(8,-40)->2` |
| 3 | `[5,-10]`| `(5,10)`          | `{(5,40):0,(5,10):1,(8,-40):2}`   | match at `i=1` |

Return `[1,3]`.

Why not `[0,2]`? Because record `2` has amount `-40`, but vendor `8`; record `0` has vendor `5`. Vendor identity is part of the matching key, so cross-vendor cancellation is invalid.

## ⏱ Complexity Analysis
### Time Complexity
`O(n)` expected time, because each of the `n` records performs one hash lookup and at most one hash insertion. There is no nested scan. At `10^6` elements this remains practical in memory-resident systems; at `10^9`, the algorithm is still linear but state management and partitioning become the real bottlenecks.

### Space Complexity
`O(n)` in the worst case, owned by the hash map of previously seen `(vendorId, amount)` keys. It can only be reduced by sacrificing correctness or by introducing domain-specific eviction rules, which would change the problem from exact matching to bounded-history matching.

## 💡 Key Takeaways
- If the problem asks for the first event that completes a pair while scanning left to right, think streaming complement lookup rather than post-processing.
- If matching depends on both identity and value, the hash key must encode both dimensions; here, `vendorId` alone is insufficient.
- Check for the complement before inserting the current record, or `amount == 0` can incorrectly self-match.
- Do not match across vendors even when amounts cancel; the composite key is `(vendorId, amount)`, not just `amount`.
- At production scale, this is the standard pattern for low-latency keyed correlation: partition by entity, hash by state, and resolve matches in one pass.

## 🚀 Variations & Further Practice
- Return **all** reconciled pairs instead of the first one; the harder part is handling duplicate amounts per vendor without losing multiplicity, which requires queues or counts per key.
- Detect the first pair where amounts sum to a target `T` instead of `0`; same pattern, but the complement becomes `T - amount`, and target configurability changes keying and API design.
- Process records in a distributed stream where events arrive out of order; the twist is watermarking, late data, and maintaining keyed state with correctness under event-time semantics.