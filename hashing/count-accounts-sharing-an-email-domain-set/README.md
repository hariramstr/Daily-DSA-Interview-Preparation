# Count Accounts Sharing an Email Domain Set

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Hashing &nbsp;|&nbsp; **Tags:** Hashing, Sets, String

---

## 🗂 Problem Overview
Given many user accounts, each with a name and a list of emails, count how many unordered account pairs have exactly the same **set of email domains**. Matching ignores account names, duplicate emails, duplicate domains within one account, and email order. The challenge is not extraction but normalization: each account must be reduced to a canonical domain-set key efficiently enough to handle up to `10^5` accounts and `2 * 10^5` total emails without pairwise comparison.

## 🌍 Engineering Impact
This pattern shows up anywhere systems must deduplicate or group entities by a normalized feature set rather than raw records: identity resolution, fraud clustering, tenant classification, search indexing, compiler symbol canonicalization, and streaming aggregation. In production, the naive all-pairs comparison collapses under cardinality growth and creates pathological latency spikes. Canonicalization plus hashing turns an equivalence problem into a counting problem, which is what makes large-scale aggregation feasible. The same design choice enables partitionable pipelines, cacheable intermediate representations, and predictable memory behavior in batch and stream processors.

## 🔍 Problem Statement
You are given `accounts`, where each element is `[name, emails]`. The name is irrelevant. For each account, extract the domain from every email by taking the substring after `'@'`, deduplicate those domains, and treat the result as that account’s domain set. Two accounts are equivalent iff these domain sets are identical.

Return the number of unordered equivalent account pairs.

Constraints:
- `1 <= accounts.length <= 100000`
- Each account has at least one email
- Total emails across all accounts `<= 200000`
- Total input email characters across all accounts `<= 100000`
- Each email contains exactly one `'@'`

Examples:

- `accounts = [["Alice", ["a@x.com", "b@y.com", "c@x.com"]], ["Bob", ["u@y.com", "v@x.com"]], ["Cara", ["k@z.com"]], ["Dan", ["p@z.com", "q@z.com"]]]`
  → `2`

- `accounts = [["Rita", ["r@a.io", "s@b.io"]], ["Sam", ["t@b.io", "u@a.io", "v@c.io"]], ["Tina", ["w@a.io", "x@b.io"]], ["Uma", ["y@c.io"]]]`
  → `1`

The scale constraint rules out comparing every account against every other account.

## 🪜 How to Solve This
1. Read the equivalence rule carefully → accounts match by **set of domains**, not by raw email list, counts, or names.

2. Once the problem says “same normalized representation,” think **hashing**. We do not want pairwise account comparison; we want to map each account to a key and count equal keys.

3. For one account, extract domains from all emails → put them in a set to remove duplicates. That gives the true semantic identity of the account.

4. A set itself is not directly a stable hash key in many languages, so convert it into a **canonical representation**: typically sort the unique domains and join them with a delimiter.

5. Now every equivalent account produces the same canonical key, and every non-equivalent account produces a different one.

6. Maintain a frequency map from canonical key → number of accounts seen so far.

7. Each time a key repeats, it forms new pairs with all prior accounts having that same key. So if the current count is `f`, add `f` to the answer, then increment the stored frequency.

That turns the problem into one pass over the data plus per-account normalization.

## 🧩 Algorithm Walkthrough
1. **Use the Hashing + Canonicalization pattern.**  
   The core abstraction is: convert each complex object into a deterministic key representing its equivalence class. This is the right pattern because equality is defined over a normalized set, not over original structure.

2. **Iterate through each account once.**  
   Ignore the account name entirely. Only the email list matters, because names are explicitly excluded from matching.

3. **Extract domains for the current account.**  
   For every email, split at `'@'` and keep the suffix. Insert each domain into a temporary set.  
   **Why correct:** duplicates like `a@x.com` and `c@x.com` collapse to one domain, matching the problem definition.  
   **Invariant:** after processing all emails in the account, the set contains exactly the unique domains for that account.

4. **Build a canonical key from the set.**  
   Convert the set to a list, sort it lexicographically, then join with a delimiter that cannot create ambiguity.  
   **Why correct:** sets are unordered, so sorting gives a stable representation. Equivalent domain sets now map to identical keys.

5. **Count pairs incrementally with a frequency map.**  
   Let `freq[key]` be how many prior accounts had this exact domain-set key. Add `freq[key]` to the answer, then increment `freq[key]`.  
   **Why correct:** the current account forms one new unordered pair with each previous equivalent account.  
   **Invariant:** after processing `i` accounts, the answer equals the number of equivalent unordered pairs among those `i` accounts.

6. **Return the accumulated answer.**  
   This avoids a second pass for combinatorics, though an alternative is to count all frequencies first and sum `f * (f - 1) / 2`.

## 📊 Worked Example
Example:  
`[["Alice", ["a@x.com", "b@y.com", "c@x.com"]], ["Bob", ["u@y.com", "v@x.com"]], ["Cara", ["k@z.com"]], ["Dan", ["p@z.com", "q@z.com"]]]`

| Step | Account | Unique domains | Canonical key | `freq[key]` before | Pairs added | Total |
|---|---|---|---|---:|---:|---:|
| 1 | Alice | `{x.com, y.com}` | `x.com|y.com` | 0 | 0 | 0 |
| 2 | Bob | `{y.com, x.com}` | `x.com|y.com` | 1 | 1 | 1 |
| 3 | Cara | `{z.com}` | `z.com` | 0 | 0 | 1 |
| 4 | Dan | `{z.com}` | `z.com` | 1 | 1 | 2 |

Trace:
1. Alice establishes key `x.com|y.com`.
2. Bob normalizes to the same key, so he pairs with Alice.
3. Cara establishes key `z.com`.
4. Dan normalizes to `z.com`, so he pairs with Cara.

Final answer: `2`.

## ⏱ Complexity Analysis

### Time Complexity
Let `E` be the total number of emails and `D_a` the number of unique domains in one account. Domain extraction is `O(E)`. Canonicalization costs `O(D_a log D_a)` per account due to sorting. Total time is `O(E + Σ D_a log D_a)`. At million-scale inputs this is practical; at billion-scale, sort cost and key materialization become the throughput bottlenecks.

### Space Complexity
`O(U + K)`, where `U` is the temporary per-account unique-domain set size and `K` is the total size of stored canonical keys in the frequency map. You can reduce key memory by interning domains or hashing sorted IDs, trading readability and collision-handling complexity.

## 💡 Key Takeaways
- If equality is defined on a normalized form rather than raw input, the first move is usually canonicalization plus a hash map.
- When asked to count equivalent unordered pairs, look for a frequency-based aggregation instead of explicit pair generation.
- Do not include account names in the key; the statement makes them irrelevant, and doing so silently breaks valid matches.
- Deduplicate domains within each account before key construction; repeated emails or repeated domains must not affect equivalence.
- In production, stable normalization is often the real system boundary: once representations are canonical, counting, caching, partitioning, and deduplication all become straightforward.

## 🚀 Variations & Further Practice
- Count accounts equivalent by full email set instead of domain set; same pattern, but cardinality and key size grow, increasing memory pressure.
- Group accounts by domains with subset/superset matching rather than exact equality; exact hashing no longer works, pushing you toward bitsets, inverted indexes, or set-containment search.
- Support online updates where emails are added or removed from accounts and pair counts must stay current; the harder twist is maintaining canonical identities incrementally without full recomputation.