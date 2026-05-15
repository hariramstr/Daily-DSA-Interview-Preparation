# Contact List Prefix Search with Wildcards

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Tries &nbsp;|&nbsp; **Tags:** Trie, String Matching, Depth-First Search

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you're searching your phone's contact list by typing the beginning of a name — but sometimes you can't remember one of the letters, so you use a "wildcard" placeholder instead. This problem asks: given a list of contacts and a list of such search queries, how many contacts match each query? It's about building a smart, flexible name-search feature.

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Fast, flexible search is at the heart of nearly every consumer app. WhatsApp, iMessage, LinkedIn, and Gmail all need to surface the right contact or result within milliseconds, even across millions of entries. The technique behind this problem — prefix search with wildcards — is also used by search engines for autocomplete, by e-commerce platforms for fuzzy product search, and by enterprise CRM tools to find customers even when a user misspells a name. A faster, smarter search directly reduces user frustration, increases engagement, and lowers the cost of running search infrastructure at scale.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a physical address book where every entry is alphabetically filed. You want to find everyone whose name starts with "Al" — easy, just flip to that section. Now imagine you remember the first two letters and know there's one more letter after them, but you've forgotten what it is. This problem is about building a system that handles both cases: exact prefix lookups and "fill-in-the-blank" prefix lookups, quickly and reliably.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a list of contact name strings and a list of query strings, return an integer array where each element represents the count of contacts whose names match the corresponding query as a prefix pattern.

A contact name matches a query if:
1. The query length does not exceed the contact name length.
2. Every non-`?` character in the query matches the contact name at the same position exactly.
3. Every `?` in the query matches exactly one (any) lowercase letter at that position.

**Constraints:**
- `1 <= contacts.length, queries.length <= 10^4`
- `1 <= contacts[i].length, queries[i].length <= 20`
- Contact names: lowercase English letters only
- Queries: lowercase English letters and `?` only

**Example 1:**
```
Input:  contacts = ["alice","alfred","bob","alicia","alba"]
        queries  = ["al", "al?", "b?b"]
Output: [4, 4, 1]
```

**Example 2:**
```
Input:  contacts = ["sam","samuel","sandy","sandra"]
        queries  = ["sa?", "san??", "s?m"]
Output: [4, 2, 2]
```

---

## 🧩 Approach: How We Solve It *(For Developers)*

We use a **Trie** (prefix tree) to store all contacts, then answer each query with a **Depth-First Search (DFS)** that respects wildcard characters.

**Step 1 — Build the Trie.**
Insert every contact name into the Trie one character at a time. At each node, maintain a `count` field that records how many contact names pass through that node. This lets us instantly know how many names share a given prefix without re-scanning the list.

**Step 2 — Handle a plain-character query step.**
When the current query character is a regular letter, we simply follow the single matching child in the Trie (if it exists). This is O(1) per character.

**Step 3 — Handle a wildcard `?` query step.**
When the current query character is `?`, we cannot follow just one path — any child could match. We branch into **all existing children** of the current Trie node and recurse down each branch. This is where DFS comes in.

**Step 4 — Base case: query exhausted.**
Once we've consumed every character in the query, the `count` stored at the current Trie node is exactly the number of contacts that match. We return this value.

**Step 5 — Aggregate results.**
Repeat Steps 2–4 for every query and collect the results into the output array.

This approach avoids re-scanning contacts for every query, making repeated queries extremely efficient after the one-time Trie build.

---

## 📊 Worked Example *(For Developers)*

**Input:** `contacts = ["sam", "samuel", "sandy", "sandra"]`, query = `"sa?"`

| Step | Action | Trie Node / State | Notes |
|------|--------|-------------------|-------|
| Build | Insert all 4 names | Root → s(4) → a(4) → m(2), n(2) → ... | `count` at each node = names passing through |
| Query `"sa?"` — char `s` | Follow child `s` | Node `s`, count=4 | Exact match, one path |
| Query `"sa?"` — char `a` | Follow child `a` | Node `a`, count=4 | Exact match, one path |
| Query `"sa?"` — char `?` | Branch to ALL children of `a` | Children: `m` (count=2), `n` (count=2) | Wildcard — explore every child |
| Base case at `m` | Query exhausted | Return count=2 | "sam", "samuel" |
| Base case at `n` | Query exhausted | Return count=2 | "sandy", "sandra" |
| **Total** | Sum branches | **2 + 2 = 4** | ✅ Correct |

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

- **Build:** O(N × L) where N = number of contacts and L = maximum name length. Each character of each name is inserted once.
- **Query:** O(Q × 26^W × L) where Q = number of queries, W = number of wildcards per query, and L = query length. In the worst case, each `?` fans out to 26 branches, but in practice contact lists are sparse and branching is limited. For typical inputs this is very fast.

### Space Complexity

O(N × L) for the Trie itself — one node per unique character position across all inserted names. With up to 10^4 contacts of length 20, this is at most 200,000 nodes, each holding a small fixed-size array of 26 child pointers and one integer count. Memory usage is predictable and bounded.

---

## 💡 Key Takeaways *(For Everyone)*

- **Smart search is a competitive advantage** — apps that return the right result instantly keep users engaged; slow or inaccurate search drives them away.
- **Wildcards make search forgiving** — supporting "fuzzy" queries means users find what they need even when they can't remember an exact spelling, directly reducing support requests and improving satisfaction.
- **Tries are purpose-built for prefix problems** — unlike a hash map or sorted array, a Trie groups all names sharing a prefix under the same path, making prefix queries a simple traversal rather than a full scan.
- **Storing counts at each node is a powerful optimisation** — it turns "how many names match?" from an O(N) counting loop into an O(1) lookup at the right node, a classic space-for-time trade-off.
- **DFS + wildcards = controlled branching** — the key insight is that a `?` character simply means "visit all children"; combining DFS with a Trie keeps this branching bounded by the actual structure of your data, not the size of the full alphabet.

---

## 🚀 Try It Yourself *(For Developers)*

- **Multi-wildcard stress test:** Generate queries with every character replaced by `?` (e.g., `"????"`) and measure how the DFS branching behaves — can you add memoisation or pruning to speed it up?
- **Suffix and infix matching:** Extend the solution to match `?` patterns anywhere in the contact name, not just as a prefix — this mirrors how tools like `grep` work and requires a different traversal strategy.
- **Top-K results:** Instead of returning a count, return the actual matching contact names sorted alphabetically — modify the Trie nodes to support this and analyse how the space complexity changes.

---