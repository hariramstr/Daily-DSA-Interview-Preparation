# Check if a Status Code Is a Power-of-Two Flag

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Binary Representation, Math

---

## 🗂 Problem Overview
Given a single integer `code`, determine whether it is a positive power of two. In binary terms, that means its representation contains exactly one set bit. Return `true` for values like `1`, `2`, `4`, and `16`, and `false` for `0`, negatives, or numbers with multiple set bits such as `18`. The non-trivial constraint is the expectation of an O(1) bitwise solution rather than scanning every bit.

## 🌍 Engineering Impact
This pattern shows up anywhere bit flags are used as compact state encodings: kernel event masks, network protocol capabilities, compiler token/category tables, feature-flag systems, permissions models, and telemetry pipelines. At scale, the distinction between “exactly one flag” and “multiple flags combined” matters for validation, routing, and storage semantics. Without a constant-time bit test, hot-path validation turns into avoidable per-bit work across millions of events. The underlying technique also generalizes to fast sanity checks in systems that encode orthogonal states into integers for cache efficiency and wire-format compactness.

## 🔍 Problem Statement
You are given one integer, `code`, in the range `-2^31 <= code <= 2^31 - 1`. A valid standalone status flag must be:

- strictly positive, and
- represented in binary with exactly one `1` bit.

Return `true` if `code` is a power of two; otherwise return `false`.

Examples:

- `code = 16` → `true`  
  Binary: `10000`  
  Exactly one bit is set.

- `code = 18` → `false`  
  Binary: `10010`  
  Two bits are set.

Important edge cases:

- `0` is invalid.
- Any negative number is invalid.
- Values like `3`, `6`, or `10` are invalid because they contain more than one set bit.

The key constraint is not the integer range but the expected approach: solve it in constant time using bitwise operators, not loops over all 32 bits.

## 🪜 How to Solve This
1. Read the requirement carefully → this is not asking whether the number is “large” or “even”; it is asking whether the binary form has exactly one set bit.

2. Recall the defining property of powers of two → `1, 2, 4, 8, ...` all look like `0001000...` in binary: one `1`, everything else `0`.

3. Ask what happens when subtracting `1` from such a number → the single `1` flips to `0`, and every lower bit becomes `1`.  
   Example: `10000 - 1 = 01111`.

4. Now compare the original number with `n - 1` using bitwise AND → for a true power of two, `n & (n - 1)` becomes `0` because they share no set bits.

5. Guard the obvious invalid cases first → `0` and negatives can produce misleading bit patterns, so require `n > 0`.

6. Final rule → `n` is a valid standalone flag iff `n > 0` and `(n & (n - 1)) == 0`.

That chain gets you to the standard constant-time bit manipulation test with no iteration.

## 🧩 Algorithm Walkthrough
1. **Apply the Bit Manipulation pattern.**  
   This is a direct binary representation problem: the question is about the count and placement of set bits, so bitwise operators are the right abstraction. No array traversal, hashing, or arithmetic simulation is needed.

2. **Reject non-positive inputs.**  
   Check whether `code <= 0`. If so, return `false`.  
   Why this is correct: the problem explicitly defines valid flags as positive integers only.  
   Invariant: from this point onward, any remaining candidate is strictly positive.

3. **Compute `code - 1`.**  
   For any positive power of two, binary form is `1000...0`. Subtracting one yields `0111...1`.  
   Why this matters: the original value and the decremented value have no overlapping `1` bits exactly when the original had only one set bit.

4. **Evaluate `code & (code - 1)`.**  
   If the result is `0`, return `true`; otherwise return `false`.  
   Why this is correct:  
   - For powers of two, the single set bit is cleared by subtracting one, so the AND is zero.  
   - For non-powers of two with multiple set bits, at least one set bit survives the AND.  
   Invariant: the expression is zero iff the input has exactly one set bit.

5. **Combine both conditions.**  
   The full predicate is: `code > 0 && (code & (code - 1)) == 0`.  
   This is the complete correctness condition under the given constraints and runs in constant time with constant space.

## 📊 Worked Example
Consider `code = 16`.

| Step | Value | Binary | Notes |
|---|---:|---|---|
| Input | 16 | `10000` | Candidate status code |
| Positivity check | `16 > 0` | — | Passes |
| Compute `code - 1` | 15 | `01111` | Single high bit becomes `0`; lower bits become `1` |
| Bitwise AND | `16 & 15` | `10000 & 01111 = 00000` | No overlapping set bits |
| Final result | `true` | — | Exactly one set bit |

Why this works: `16` has only one `1` in its binary representation. After subtracting `1`, every lower position is filled with `1`s, but the original high bit disappears. The AND therefore becomes zero.

For contrast, `18` is `10010`, and `17` is `10001`; `10010 & 10001 = 10000`, which is non-zero, so the answer is `false`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm is **O(1)**: it performs a fixed number of primitive operations — one comparison, one subtraction, one bitwise AND, and one equality check. There is no dependence on input magnitude or bit width in practice. At `10^6` or `10^9` validations, throughput scales linearly with request count, not per-value bit scanning.

### Space Complexity
The algorithm is **O(1)** space. It uses only a few scalar values and no auxiliary data structures. There is nothing meaningful to reduce further; any alternative would only trade clarity for no practical memory gain.

## 💡 Key Takeaways
- If a problem asks whether a number has exactly one set bit, it is usually a power-of-two check in disguise.
- When the prompt emphasizes binary representation and O(1) validation, reach for bit manipulation before loops or string conversion.
- `0` is the classic trap: `(0 & -1) == 0`, but `0` is not a power of two, so the positivity check is mandatory.
- Negative values must be rejected explicitly; two’s-complement representation can otherwise obscure the intended mathematical condition.
- In production systems, compact bit encodings are only useful if validation and classification remain constant-time on hot paths.

## 🚀 Variations & Further Practice
- **Count set bits instead of checking one bit**: determine whether a value has exactly `k` set bits; harder because the invariant is no longer a single-expression check.
- **Check whether a number is a power of four**: same base trick, plus an additional positional constraint on where the single set bit appears.
- **Validate composite flag masks**: distinguish valid combinations from invalid reserved bits using bit masks; harder because correctness depends on schema constraints, not just bit count.