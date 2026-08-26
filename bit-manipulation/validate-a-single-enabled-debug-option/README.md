# Validate a Single Enabled Debug Option

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Bitwise AND, Binary Representation

---

## 🗂 Problem Overview
Given a non-negative integer `mask`, determine whether its binary representation contains exactly one `1` bit. Return `true` only in that case; otherwise return `false`. The input is a single integer, and the output is a boolean. The non-trivial part is handling this efficiently and correctly for `0`, which has no enabled bits and must return `false`, while recognizing the constant-time bit trick behind powers of two.

## 🌍 Engineering Impact
This pattern shows up anywhere flags are packed into integers: kernel capability masks, feature gates, compiler optimization flags, network protocol headers, and observability/debug controls. At scale, bitmasks are chosen because they are compact, cache-friendly, and cheap to transmit across process or service boundaries. Validating that exactly one mode is active prevents ambiguous execution paths, conflicting safety settings, and undefined fallback behavior. The bitwise test matters because it turns a potentially repeated per-bit scan into a branch-light primitive that can sit on hot paths, validation layers, and config admission checks without becoming measurable overhead.

## 🔍 Problem Statement
You are given a non-negative integer `mask` where each bit represents whether a debug option is enabled. A configuration is valid only if exactly one debug option is enabled, meaning the binary representation of `mask` contains exactly one set bit.

Return `true` if `mask` has exactly one set bit; otherwise return `false`.

Constraints:

- `0 <= mask <= 2^31 - 1`
- Use `O(1)` extra space
- Any solution in `O(number of bits)` or better is acceptable

Examples:

- `mask = 8` → `true`  
  Binary: `1000` → one set bit
- `mask = 10` → `false`  
  Binary: `1010` → two set bits

The key edge case is `mask = 0`: it contains no set bits, so it is invalid. The constraint that drives the algorithmic choice is that this is fundamentally a bit-pattern validation problem, not a counting or collection problem.

## 🪜 How to Solve This
1. Read the requirement carefully → we are not asking whether the number is even, odd, or positive; we are asking whether **exactly one bit** is set.

2. Translate that into binary structure → numbers with exactly one set bit look like `1`, `10`, `100`, `1000`, and so on. These are precisely powers of two.

3. Think about what happens when subtracting `1` from such a number →  
   `1000 - 1 = 0111`  
   The single `1` flips to `0`, and all lower bits become `1`.

4. Now combine the original number with `n & (n - 1)` → for a power of two, the result is always `0` because the original single set bit is cleared and there is no other `1` left.

5. Handle the exception first → `0` also satisfies `0 & (0 - 1) == 0` in many languages’ integer arithmetic, but `0` is not valid because it has no enabled option.

6. That leads directly to the rule: return `mask > 0 && (mask & (mask - 1)) == 0`.

This is the standard bit manipulation pattern for “exactly one set bit.”

## 🧩 Algorithm Walkthrough
1. **Identify the pattern: Bit Manipulation / Power-of-Two Check**  
   This problem is a direct instance of the “single set bit” pattern. A non-zero integer has exactly one set bit if and only if it is a power of two. That equivalence gives us a constant-time test.

2. **Reject `0` immediately**  
   If `mask == 0`, return `false`. This is necessary because `0` means no debug option is enabled. It also prevents treating `0` as a false positive under the bit trick.

3. **Compute `mask - 1`**  
   Subtracting `1` from a number with one set bit transforms the bit pattern in a predictable way: the highest set bit becomes `0`, and all lower bits become `1`.

4. **Apply `mask & (mask - 1)`**  
   For a value with exactly one set bit, this operation clears that bit and leaves `0`. For any value with two or more set bits, at least one set bit remains, so the result is non-zero.

5. **Return whether the result is zero**  
   The invariant is: after `n & (n - 1)`, the number of set bits decreases by exactly one. Therefore, the result is zero only when the original number had exactly one set bit.

6. **Why this abstraction is correct**  
   We are not iterating over positions or counting bits explicitly. Instead, we exploit a structural property of binary representation. That makes the solution compact, branch-light, and easy to reason about under fixed-width integer constraints.

## 📊 Worked Example
Take `mask = 8`.

Binary trace:

| Step | Value | Binary | Meaning |
|---|---:|---|---|
| Input | 8 | `1000` | One debug option enabled |
| Check `mask > 0` | true | `1000` | Non-zero, so continue |
| Compute `mask - 1` | 7 | `0111` | Single set bit cleared; lower bits filled |
| Compute `mask & (mask - 1)` | 0 | `0000` | No set bits remain |
| Final result | `true` | — | Exactly one set bit |

Contrast with `mask = 10`:

- `10` in binary is `1010`
- `10 - 1 = 9` → `1001`
- `1010 & 1001 = 1000`, which is non-zero

Since a set bit remains after the operation, the original value had more than one enabled debug option, so the answer is `false`.

## ⏱ Complexity Analysis
### Time Complexity
The bitwise solution runs in `O(1)` time because it performs a fixed number of arithmetic and bitwise operations regardless of input value. A bit-scanning alternative is `O(number of bits)`, which is still effectively constant for 32-bit integers, but the bit trick is tighter and preferable in hot paths or repeated validation across `10^6` to `10^9` checks.

### Space Complexity
The algorithm uses `O(1)` extra space. It stores only a few scalar values and allocates no auxiliary data structures. There is nothing meaningful to reduce further; the only trade-off is readability versus explicit per-bit counting.

## 💡 Key Takeaways
- If the prompt says “exactly one bit is set,” “single enabled flag,” or “power of two,” this is a strong signal for the `n & (n - 1)` pattern.
- When the input is a packed flag mask rather than a collection, think in terms of binary invariants instead of loops, maps, or counters.
- `0` is the critical edge case: it has no set bits, so it must return `false` even though the bitwise expression alone can be misleading.
- Be careful with signed integer semantics in some languages if constraints change; this problem is safe because `mask` is explicitly non-negative.
- In production systems, compact bitmask validation is valuable because it enforces configuration exclusivity with minimal CPU, memory, and wire overhead.

## 🚀 Variations & Further Practice
- Count the number of enabled debug options instead of checking for exactly one; the twist is moving from a boolean predicate to population count (`popcount`) logic.
- Check whether exactly `k` bits are set; the harder part is choosing between repeated bit-clearing, lookup-based popcount, or hardware intrinsics depending on throughput needs.
- Given a stream of masks, find the longest contiguous segment where each mask has at most one set bit; this adds a sliding-window or streaming-state constraint on top of the same bit reasoning.