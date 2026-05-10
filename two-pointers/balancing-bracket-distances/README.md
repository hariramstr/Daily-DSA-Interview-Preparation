# Balancing Bracket Distances

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Stack, String, Greedy

---

## 🗂 What Is This Problem? *(For Everyone)*

Imagine you have a string of opening and closing parentheses that are perfectly paired up. Each pair has a "distance" — how far apart the two matching symbols are. This problem asks: can you swap just two characters in the string to make all the pairs sit closer together, reducing the total distance? And if so, what is the smallest total distance you can achieve?

---

## 🌍 Why Does This Matter in the Real World? *(For Business Readers)*

Bracket-matching and distance-minimisation problems appear in compilers, code editors, and data serialisation systems — tools that power every software product your business relies on. When a compiler parses code or a database validates a nested query, it solves a version of this problem millions of times per second. Minimising structural "spread" in nested data reduces parsing time, lowers server costs, and speeds up response times for end users. Optimising these small structural decisions at scale translates directly into faster applications, reduced cloud infrastructure bills, and a better customer experience.

---

## 🎯 The Challenge in Plain English *(For Everyone)*

Think of a row of lockers, each with a lock on the left and its matching key on the right. The further apart a lock and its key are, the more time it takes to retrieve them. You are allowed to swap the positions of just two items in the row — but the row must still make sense (every lock still has a reachable key). Your goal is to rearrange so that locks and keys are as close together as possible, minimising total retrieval time.

---

## 🔍 Technical Problem Statement *(For Developers)*

Given a string `s` consisting solely of `'('` and `')'` characters that forms a **valid bracket sequence**, define the **distance** of each matched pair `(i, j)` — where `s[i] = '('` and `s[j] = ')'` — as `j - i`. Find the **minimum possible sum of all pair distances** after performing **at most one swap** of any two characters, provided the resulting string remains a valid bracket sequence.

**Constraints:**
- `2 <= s.length <= 10^5`
- `s.length` is even
- `s` consists only of `'('` and `')'`
- `s` is a valid bracket sequence

**Examples:**

| Input | Output | Notes |
|-------|--------|-------|
| `"(())"` | `4` | Pairs `(0,3)` and `(1,2)`: distances `3 + 1 = 4`. No valid swap improves this. |
| `"()(())"` | `6` | Original sum is `5`; all valid single swaps yield a minimum achievable sum of `6`. |

---

## 🧩 Approach: How We Solve It *(For Developers)*

1. **Compute the baseline matching and sum.**
   Use a stack to match each `'('` with its corresponding `')'`. Record each matched pair `(i, j)` and accumulate the baseline sum of distances `j - i`. This gives us our starting benchmark.

2. **Identify swap candidates.**
   A beneficial swap involves exchanging a `')'` at position `i` with a `'('` at position `j` where `j > i`. Swapping a `')'` leftward and a `'('` rightward can bring certain pairs closer. Only swaps between a `')'` and a `'('` can alter the structure; swapping two identical characters changes nothing.

3. **Enumerate valid swaps efficiently with two pointers.**
   Use a left pointer scanning for `')'` characters and a right pointer scanning for `'('` characters (with left < right). For each candidate swap `(left, right)`, simulate the effect on the matching by re-running the stack matcher on the modified string — or use a greedy delta calculation to determine whether the swap reduces the total distance.

4. **Validate the resulting sequence.**
   After a candidate swap, verify the string remains a valid bracket sequence. An invalid sequence (one that goes negative in prefix sum at any point) is discarded immediately.

5. **Track and return the minimum.**
   Compare the sum after each valid swap against the running minimum. Return the smallest value found.

---

## 📊 Worked Example *(For Developers)*

**Input:** `s = "(())"`

**Step 1 — Baseline matching with a stack:**

| Index | Char | Stack (after action) | Matched Pair | Distance |
|-------|------|----------------------|--------------|----------|
| 0 | `(` | `[0]` | — | — |
| 1 | `(` | `[0, 1]` | — | — |
| 2 | `)` | `[0]` | `(1, 2)` | 1 |
| 3 | `)` | `[]` | `(0, 3)` | 3 |

**Baseline sum = 1 + 3 = 4**

**Step 2 — Candidate swaps (left `)`, right `(`):**

| Swap (i, j) | Resulting String | Valid? | New Sum |
|-------------|-----------------|--------|---------|
| (2, —) | No `'('` to the right of index 2 | N/A | — |

No valid swap exists that reduces the sum. **Return 4.**

---

## ⏱ Performance Analysis *(For Developers + Technical Managers)*

### Time Complexity

**O(n²)** in the naive swap-enumeration approach, since there are O(n) candidate swaps and each validation/re-matching costs O(n). With greedy delta analysis, this can be reduced to **O(n)** — meaning even strings 100,000 characters long are processed in a single pass, making the solution production-ready at scale.

### Space Complexity

**O(n)** — the stack used for bracket matching holds at most n/2 elements at any time. No additional data structures grow with input size beyond linear, keeping memory usage predictable and manageable even for large inputs.

---

## 💡 Key Takeaways *(For Everyone)*

- **Small structural optimisations compound at scale** — shaving microseconds off nested-data parsing across millions of daily requests translates into real cost savings on cloud infrastructure.
- **Constraints are opportunities** — the guarantee that the input is always a valid bracket sequence dramatically narrows the search space, making an efficient solution achievable.
- **Stack-based bracket matching is a foundational pattern** — mastering it unlocks solutions to compiler design, XML/JSON validation, and expression evaluation problems.
- **Two pointers reduce redundant work** — rather than checking every possible pair of indices, the two-pointer technique focuses only on structurally meaningful swap candidates, cutting unnecessary computation.
- **Always validate after mutation** — when modifying a structured sequence (brackets, trees, graphs), re-validating the invariant (prefix sum ≥ 0 throughout) is essential and should be a reflex in your solution design.

---

## 🚀 Try It Yourself *(For Developers)*

- **Variation 1 — Multiple swaps:** Extend the problem to allow at most `k` swaps. How does your approach change? Consider a dynamic programming or greedy layered strategy.
- **Variation 2 — Weighted distances:** Assign a cost to each bracket position (e.g., based on nesting depth) and minimise the weighted sum of distances instead of the raw positional distance.
- **Variation 3 — Different bracket types:** Generalise to strings containing `()`, `[]`, and `{}`. How does multi-type validation affect your stack logic and swap candidate selection?

---