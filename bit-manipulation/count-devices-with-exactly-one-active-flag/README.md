# Count Devices with Exactly One Active Flag

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Array, Counting

---

## 🗂 Problem Overview
Given an integer array `states`, count how many elements represent a device with exactly one enabled feature bit. In practice, that means counting values whose binary representation contains exactly one `1`. The input can contain up to `100000` values, so the solution should scan once and use constant-time bit checks per element. The main constraint is avoiding wasteful per-number conversions or bit-by-bit counting when a direct bit-manipulation test exists.

## 🌍 Engineering Impact
This pattern shows up anywhere flags are packed into integers for density and speed: kernel capability masks, feature-flag systems, network protocol headers, compiler IR metadata, telemetry pipelines, and search or ranking systems that encode categorical state as bitfields. At scale, you do not want to stringify integers or iterate over every bit unless necessary. The difference matters in hot loops, stream processors, and observability backends where millions of events are classified per second. Recognizing power-of-two masks enables fast validation, routing, and aggregation logic while keeping CPU cost predictable and memory access linear.

## 🔍 Problem Statement
You are given an array `states` of length `1` to `100000`, where each `states[i]` is an integer in the range `0` to `10^9`. Each integer is a bitmask describing which device features are enabled. A device is considered simple-active if and only if exactly one bit is set in its binary representation.

Return the total number of simple-active devices.

A positive integer has exactly one set bit if and only if it is a power of two, which can be tested efficiently with bit manipulation.

Examples:

- `states = [1, 2, 3, 4, 6]` → `3`
- `states = [0, 7, 8, 16, 18]` → `2`

Edge cases matter:
- `0` is not valid because it has no set bits.
- Values with multiple set bits, such as `3`, `6`, or `18`, must be excluded.
- The array may be large enough that the per-element check should be O(1).

## 🪜 How to Solve This
1. Read the requirement carefully → we are not asked to decode all enabled features, only to decide whether there is exactly one.
2. “Exactly one set bit” should trigger a known bit-manipulation identity: powers of two have a single `1` in binary.
3. Why does that help? Because for a positive power of two, subtracting `1` flips the only set bit to `0` and turns all lower bits to `1`. Their bitwise `AND` becomes `0`.
4. That gives the test: `x > 0 && (x & (x - 1)) == 0`.
5. Once the predicate is constant time, the rest is straightforward → scan the array once, apply the predicate to each value, increment a counter when it passes.
6. No sorting, no auxiliary structures, no nested loops. The problem is really “count items satisfying a bit predicate.”
7. The key reasoning move is recognizing that the binary property can be checked structurally, not by counting bits explicitly.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: linear scan + bit predicate.**  
   This is not a search or dynamic programming problem. The right abstraction is a single pass over the array, where each element is classified independently using a bit-manipulation rule.

2. **Initialize a counter to zero.**  
   This counter represents the number of simple-active devices seen so far.  
   **Invariant:** after processing index `i`, the counter equals the number of valid values in `states[0..i]`.

3. **Iterate through each value `x` in `states`.**  
   Each device state is evaluated exactly once, which keeps the algorithm O(n). There is no dependency between elements, so no additional state is required beyond the running count.

4. **Reject non-positive values first.**  
   `0` must not be counted because it has zero set bits. The identity `x & (x - 1) == 0` also holds for `0`, so the positivity check is required for correctness.

5. **Apply the bit test `x & (x - 1) == 0`.**  
   For powers of two, binary form looks like `1000...0`. Subtracting one gives `0111...1`, so the `AND` clears to zero. For any number with two or more set bits, at least one set bit survives the `AND`, so the result is nonzero.

6. **Increment the counter when the predicate passes.**  
   This maintains the invariant that the counter always reflects the number of qualifying devices processed so far.

7. **Return the counter after the scan completes.**  
   Correctness follows from evaluating every element exactly once with a necessary-and-sufficient condition for “exactly one set bit.”

## 📊 Worked Example
Example: `states = [1, 2, 3, 4, 6]`

| Index | Value | Binary | `value > 0` | `value & (value - 1)` | Simple-active? | Count |
|---|---:|---|---|---:|---|---:|
| 0 | 1 | `0001` | true | 0 | yes | 1 |
| 1 | 2 | `0010` | true | 0 | yes | 2 |
| 2 | 3 | `0011` | true | 2 | no | 2 |
| 3 | 4 | `0100` | true | 0 | yes | 3 |
| 4 | 6 | `0110` | true | 4 | no | 3 |

Trace:
1. Start with `count = 0`.
2. `1` passes the power-of-two test → `count = 1`.
3. `2` passes → `count = 2`.
4. `3` has two set bits, so the `AND` result is nonzero.
5. `4` passes → `count = 3`.
6. `6` fails because it has multiple enabled bits. Return `3`.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time, where `n` is `states.length`, because it performs one pass and a constant-time bit test per element. At `10^6` elements this is still routine. At `10^9`, the bottleneck is no longer the predicate but total scan time and memory bandwidth.

### Space Complexity
The algorithm uses **O(1)** extra space. The only additional state is the running counter and loop variable. Space cannot be meaningfully reduced further; any alternative approach that stores intermediate results would only increase memory usage without improving asymptotic runtime.

## 💡 Key Takeaways
- If a problem asks for values with exactly one enabled bit, think “power of two” before thinking “count bits.”
- When each array element can be classified independently, a single linear scan is usually the right baseline, not sorting or hashing.
- `0` is the main correctness trap: `0 & (0 - 1) == 0`, but `0` does **not** have exactly one set bit.
- Do not replace the bit test with binary-string conversion or manual popcount unless the problem explicitly requires bit counts for other reasons.
- In production systems, compact bitmasks are only valuable if you also use constant-time bitwise predicates to classify them efficiently.

## 🚀 Variations & Further Practice
- Count devices with exactly `k` active flags instead of one; the twist is choosing between repeated bit clearing, built-in popcount, or lookup-table strategies depending on throughput requirements.
- Return the indices or values of all simple-active devices; same predicate, but now output size affects space complexity and interface design.
- Given a stream of state updates, maintain the count online; the harder part is incremental correctness under inserts, deletes, or mutable device state.