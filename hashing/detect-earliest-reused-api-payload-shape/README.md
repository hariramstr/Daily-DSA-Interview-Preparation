# Detect Earliest Reused API Payload Shape

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Canonical Signature, String, Data Structures

---

## 🗂 Problem Overview
Given a stream of request payloads, return the smallest index `j` for which some earlier payload `i < j` has the same shape. Shape means identical keys and identical value lengths per key; actual value contents and pair ordering do not matter. The challenge is scale: with up to 200,000 requests, pairwise comparison is too expensive. The key is to convert each payload into a canonical, order-independent signature and detect the first repeated signature with hashing.

## 🌍 Engineering Impact
This pattern shows up in schema fingerprinting, log deduplication, API anomaly detection, streaming ETL validation, and compiler or query-plan memoization. In production, you often care about structural equivalence rather than byte-for-byte equality: same fields, same cardinalities, same shape. Without canonicalization plus hashing, systems fall back to repeated deep comparisons, which collapse under high-cardinality streams and large fan-in pipelines. A stable signature enables O(1)-ish lookup, early duplicate detection, compact indexing, and clean separation between normalization logic and matching logic. That matters in hot paths like request gateways, telemetry ingestion, and distributed event processors.

## 🔍 Problem Statement
You are given `n` API requests, where each request is a list of unique `(key, value)` pairs. Pair order is arbitrary, keys are lowercase strings, and values are lowercase strings. Two requests have the same payload shape iff:

- they contain exactly the same set of keys, and
- for every key, the associated value lengths are equal.

Return the smallest index `j` such that there exists some `i < j` with the same shape. If no shape repeats, return `-1`.

Constraints:

- `1 <= n <= 200000`
- `0 <= m_i <= 100`
- `sum(m_i) <= 400000`
- `1 <= key.length <= 20`
- `0 <= value.length <= 100`

Example 1:
`[[("user","amy"),("region","us")],[("device","ios")],[("region","eu"),("user","bob")],[("user","anna"),("region","uk")]] -> 2`

Example 2:
`[[("id","7")],[("id","88")],[("id","999")],[("id","44")]] -> 3`

The decisive constraint is input size: nested request-to-request comparison will time out.

## 🪜 How to Solve This
1. Read the equivalence rule carefully → payload order is irrelevant, but key presence and value length per key define equality.

2. That means raw payloads are not directly hashable in a stable way → we need an order-independent canonical representation.

3. For one request, transform each pair into `(key, value.length)` → this strips irrelevant value content while preserving exactly what defines shape.

4. Since pair order is arbitrary, sort those transformed pairs by key → now equivalent payloads produce identical normalized sequences.

5. Once every request can be represented by one canonical signature, the problem becomes: “find the first repeated signature in a stream.”

6. First repeated in stream order suggests a single left-to-right pass with a hash map or hash set.

7. For each index `j`, build its signature and check whether it has been seen before:
   - if yes, return `j` immediately, because scanning left to right guarantees this is the smallest repeated index;
   - if no, record it and continue.

8. This avoids comparing a request against all previous requests. The expensive part is only normalizing each payload once, which is bounded by the total number of pairs.

## 🧩 Algorithm Walkthrough
1. **Use the canonical-signature + hashing pattern.**  
   This is the right abstraction because the problem defines an equivalence relation over payloads. Hashing works only after equivalent payloads are normalized into the same representation.

2. **Process requests from left to right.**  
   At index `j`, only earlier requests matter. The invariant is: before handling `j`, the hash structure contains signatures of exactly requests `0..j-1`.

3. **Normalize one request.**  
   For each `(key, value)` pair, compute `(key, len(value))`. This preserves all shape-defining information and discards irrelevant content.

4. **Make the representation order-independent.**  
   Sort the `(key, length)` pairs by `key`, then serialize them into a tuple, vector, or delimited string. Because keys are unique within a request, sorting by key is sufficient to produce a unique canonical form.

5. **Check for prior occurrence in a hash map/set.**  
   If the signature already exists, return the current index `j`. This is correct because we scan in increasing index order, so the first match encountered is the smallest valid `j`.

6. **Otherwise, store the signature.**  
   You can store just presence in a hash set, or the earliest index in a hash map. Presence is enough for this problem; earliest index is useful for debugging or extensions.

7. **Finish the scan.**  
   If no signature repeats, return `-1`.

The core invariant is simple: every seen signature corresponds to at least one earlier request with that exact shape. The first time a signature reappears, we have found the earliest reused payload shape.

## 📊 Worked Example
Consider:

`requests = [[("user","amy"),("region","us")],[("device","ios")],[("region","eu"),("user","bob")],[("user","anna"),("region","uk")]]`

| `j` | Raw request | Canonical `(key,len)` after sort | Seen before? | Action |
|---|---|---|---|---|
| 0 | `[("user","amy"),("region","us")]` | `[(region,2),(user,3)]` | No | Insert |
| 1 | `[("device","ios")]` | `[(device,3)]` | No | Insert |
| 2 | `[("region","eu"),("user","bob")]` | `[(region,2),(user,3)]` | Yes | Return `2` |

Trace:

1. Request `0` normalizes to `[(region,2),(user,3)]`.
2. Request `1` normalizes to `[(device,3)]`.
3. Request `2` normalizes to the same signature as request `0`.
4. Because we scan left to right, `2` is the smallest index whose shape appeared earlier.

Request `3` is never examined once `2` is found, which is exactly what “earliest repeated index” requires.

## ⏱ Complexity Analysis
### Time Complexity
For each request with `m_i` pairs, we sort its pairs by key, costing `O(m_i log m_i)`, then hash the canonical signature. Total time is `O(sum(m_i log m_i))`, with `m_i <= 100`, so this is effectively near-linear in input size. At `10^6` elements this remains practical; quadratic comparison does not. At `10^9`, even linear scans become infrastructure problems.

### Space Complexity
Space is `O(U)` for stored signatures, where `U` is the number of distinct payload shapes seen before termination, plus temporary space for one normalized request. The hash structure owns the dominant memory. You can reduce footprint with custom hashing or compact binary encoding, trading readability and collision-handling complexity.

## 💡 Key Takeaways
- If equality ignores ordering and some fields are irrelevant, the signal is usually “build a canonical form, then hash it.”
- If the problem asks for the first repeated structure in a stream, think single pass with a hash set/map, not pairwise comparison.
- Do not hash raw pair order; equivalent payloads may arrive in different key orders and must normalize identically.
- Return the current index `j` on first repeated signature, not the earlier index `i`; that off-by-one interpretation is the main correctness trap.
- In production, canonical signatures turn expensive structural comparisons into indexable fingerprints, which is the foundation of scalable deduplication and schema-aware routing.

## 🚀 Variations & Further Practice
- Detect the earliest repeated shape when payloads may contain nested objects or arrays; the harder twist is recursive canonicalization with stable encoding of hierarchical structure.
- Return all groups of matching payload shapes, not just the first repeated index; the twist is preserving full occurrence lists while keeping memory bounded.
- Support online updates where keys can be added or removed from a payload template over time; the twist is incremental signature maintenance instead of recomputing from scratch.