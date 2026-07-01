# Count Customers with a Unique Favorite Product

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hash Map, Counting, Array

---

## 🗂 Problem Overview
Given an array of product codes, where each entry is one customer’s favorite product, return how many customers chose a product that appears exactly once in the full list. The input is an array of strings; the output is a single integer. The non-trivial constraint is scale: with up to 100,000 entries, pairwise comparison is too expensive, so the solution needs average-case linear time using hashing.

## 🌍 Engineering Impact
This is the same counting primitive used in event analytics, recommender systems, fraud detection, search query analysis, and streaming telemetry. You ingest identifiers, aggregate frequencies, then ask which keys are rare, unique, or duplicated. In production, this pattern sits behind “top-N” jobs, anomaly detectors, compiler symbol tables, log aggregation, and distributed rate-limiters. Without hash-based counting, systems fall back to repeated scans or sorts, which increases latency and infrastructure cost. With it, you get predictable one-pass aggregation, straightforward parallelization, and a clean boundary between ingestion and downstream decision logic.

## 🔍 Problem Statement
You are given an array `favorites` of length `n`, where `1 <= n <= 100000`. Each `favorites[i]` is a product code string of length `1..50`, containing lowercase English letters, digits, and underscores.

A product is **unique** if it appears exactly once in the entire array. Since each array entry represents one customer, the task is to return the number of customers whose chosen product is unique.

Formally:
- **Input:** `string[] favorites`
- **Output:** `int` = number of entries whose value has frequency `1`

Examples:

- `["phone_case", "charger", "phone_case", "notebook", "pen"] -> 3`
- `["mouse", "mouse", "keyboard", "keyboard", "monitor"] -> 1`

Edge cases matter:
- A single-element array always returns `1`
- If every product repeats, return `0`
- If all product codes are distinct, return `favorites.length`

The key algorithmic constraint is the required average-case `O(n)` solution, which rules out nested scans.

## 🪜 How to Solve This
1. Read the problem carefully → we are not asked for the number of distinct products; we are asked for how many **array entries** belong to products with frequency `1`.

2. That immediately turns the task into a **frequency counting** problem. Whenever the question is “how many times does each value occur?”, a hash map should be the default candidate.

3. First pass → count occurrences of every product code:
   - key = product code
   - value = number of times it appears

4. Once frequencies are known, the answer is easy:
   - either scan the original array and count entries whose frequency is `1`
   - or scan the frequency map and count keys with value `1`

5. In this problem, both produce the same numeric answer because each unique product contributes exactly one customer. Scanning the map is slightly more direct.

6. Why this approach? Because it converts repeated membership/count checks from `O(n)` each into average-case `O(1)` lookups, collapsing the overall runtime from quadratic to linear.

7. Sorting could also work, but it costs `O(n log n)` and is unnecessary given the hashing requirement.

## 🧩 Algorithm Walkthrough
**Pattern:** Hash Map frequency counting

This is the right abstraction because the problem is about grouping identical values and measuring how often each group occurs. A hash map gives average-case constant-time updates and lookups, which matches the `O(n)` target.

1. **Initialize an empty hash map**  
   Create a map `freq` from `string -> int`.  
   **Why:** We need a compact representation of occurrence counts for each product code.  
   **Invariant:** After processing any prefix of the array, `freq[x]` equals the number of times `x` has appeared in that prefix.

2. **Traverse the array once to build frequencies**  
   For each `product` in `favorites`, increment `freq[product]`.  
   **Why:** Every occurrence contributes exactly one to that product’s total count.  
   **Invariant:** After the first pass completes, `freq[x]` is the total number of occurrences of `x` in the full array.

3. **Count products with frequency exactly one**  
   Iterate over the map values and count how many are `1`.  
   **Why:** A product selected by exactly one customer contributes exactly one customer to the answer.  
   **Invariant:** The running total equals the number of unique products seen so far in the map iteration.

4. **Return the total**  
   This total is also the number of customers with a unique favorite product.  
   **Why it's correct:** There is a one-to-one correspondence between:
   - a product code with frequency `1`, and
   - the single customer entry that chose it.

An equivalent second pass over the original array would also work, but scanning the map avoids redundant lookups.

## 📊 Worked Example
Example: `favorites = ["phone_case", "charger", "phone_case", "notebook", "pen"]`

| Step | Current product | Frequency map after update |
|---|---|---|
| 1 | `phone_case` | `{phone_case: 1}` |
| 2 | `charger` | `{phone_case: 1, charger: 1}` |
| 3 | `phone_case` | `{phone_case: 2, charger: 1}` |
| 4 | `notebook` | `{phone_case: 2, charger: 1, notebook: 1}` |
| 5 | `pen` | `{phone_case: 2, charger: 1, notebook: 1, pen: 1}` |

Now scan the map:
- `phone_case -> 2` → not unique
- `charger -> 1` → count it
- `notebook -> 1` → count it
- `pen -> 1` → count it

Final answer: `3`

The important state transition is that duplicates stop being eligible as soon as their count exceeds `1`. Only keys finishing at exactly `1` contribute to the result.

## ⏱ Complexity Analysis
### Time Complexity
The runtime is `O(n)` on average: one pass to build the hash map and one pass over at most `n` distinct keys to count frequencies equal to `1`. At `10^6` elements this remains practical; at `10^9`, the algorithm is still linear but memory bandwidth and distributed aggregation become the real constraints.

### Space Complexity
The space usage is `O(k)`, where `k` is the number of distinct product codes, because the hash map stores one entry per unique string. It can only be reduced by sorting in place or externalizing counts, trading memory for `O(n log n)` time or system complexity.

## 💡 Key Takeaways
- If the question asks “how many times does each value occur?” or “which values appear exactly once?”, think frequency map immediately.
- When identifiers are arbitrary strings rather than bounded integers, hashing is usually the cleanest linear-time grouping mechanism.
- Do not confuse “number of unique products” with “number of distinct products”; frequency `1` is the criterion, not deduplication.
- If you do a second pass over the array, count entries whose frequency is `1`; if you scan the map, count keys with value `1`—both are valid here, but only because each unique key contributes one customer.
- At scale, hash-based counting is the core primitive behind rare-event detection, telemetry summarization, and many streaming aggregation pipelines.

## 🚀 Variations & Further Practice
- Return the list of unique favorite products instead of the count; same frequency map, but now output construction and ordering requirements may matter.
- Count products that appear exactly `k` times; the conceptual twist is parameterizing the rarity threshold instead of hardcoding `1`.
- Process a continuous event stream with sliding windows; the harder part is maintaining counts under both insertions and expirations rather than a single static pass.