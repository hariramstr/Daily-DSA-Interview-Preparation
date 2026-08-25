# Count Sensor Readings With Odd Parity

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Parity, Array

---

## 🗂 Problem Overview
Given an integer array `readings`, count how many values have an odd number of set bits in their binary representation. The function returns a single integer: the number of odd-parity readings. The main constraint is not asymptotic difficulty so much as implementation discipline: the expected approach uses bit manipulation directly, not string conversion, and must scale cleanly to arrays as large as `10^5` elements.

## 🌍 Engineering Impact
Parity checks show up in telemetry pipelines, embedded systems, network protocols, storage integrity paths, and SIMD-friendly analytics code. A compact integer often encodes multiple flags, and downstream logic needs fast classification based on bit population rather than decoded fields. At scale, stringifying integers or using heavyweight parsing in hot loops creates avoidable CPU and allocation overhead. Bitwise solutions preserve cache locality, reduce instruction count, and compose well with streaming architectures where millions of events must be classified, counted, or routed with predictable latency and minimal memory pressure.

## 🔍 Problem Statement
You are given an integer array `readings` where each element is a non-negative sensor reading encoded as an integer. A reading is **odd-parity** if the count of `1` bits in its binary form is odd. Return the number of readings with odd parity.

Constraints:

- `1 <= readings.length <= 100000`
- `0 <= readings[i] <= 10^9`
- An `O(n * number_of_bits)` solution is acceptable

Examples:

- `readings = [1, 2, 3, 4]` → `3`
  - `1 = 1` → 1 set bit → odd
  - `2 = 10` → 1 set bit → odd
  - `3 = 11` → 2 set bits → even
  - `4 = 100` → 1 set bit → odd

- `readings = [0, 5, 7, 8, 10]` → `2`

Important edge case: `0` has zero set bits, which is even, so it must not be counted. The algorithmic choice is driven by the requirement to inspect bit structure directly rather than converting numbers to strings.

## 🪜 How to Solve This
1. Read the problem → this is not about arithmetic value, ordering, or frequency. It is about a property of the binary representation: whether the number of `1` bits is odd.

2. “Count set bits” should immediately suggest bit manipulation. Converting to binary strings works functionally, but it is the wrong abstraction and violates the intended constraint.

3. For each reading, determine its parity:
   - Either count all set bits and check if the total is odd.
   - Or toggle a parity flag while scanning bits, which avoids storing the full count.

4. How do we scan bits? Repeatedly inspect the least significant bit with `x & 1`, then shift right with `x >>= 1`.

5. If the final count or parity flag is odd, increment the answer.

6. Repeat for every element in the array.

The key realization is that each number can be processed independently in a small bounded number of bit operations. With values up to `10^9`, each reading has at most about 30 significant bits, so a straightforward bitwise scan is already efficient enough.

## 🧩 Algorithm Walkthrough
1. **Use the Bit Manipulation pattern**: process each integer by examining its bits directly. This is the right abstraction because the property we care about—parity of set bits—lives entirely at the bit level.

2. Initialize `oddCount = 0`. This variable maintains the invariant: after processing the first `i` readings, `oddCount` equals the number of odd-parity values among them.

3. Iterate through each `reading` in `readings`.

4. For the current number, initialize either:
   - `setBits = 0`, or
   - `parity = 0`
   
   The parity approach is slightly tighter: every time you see a `1` bit, flip `parity` between `0` and `1`.

5. While `reading > 0`:
   - Inspect the lowest bit using `reading & 1`.
   - If that bit is `1`, update `setBits++` or `parity ^= 1`.
   - Shift right by one bit: `reading >>= 1`.

   Invariant: after each loop iteration, you have correctly accounted for all bits removed from the right, and the remaining value still contains all unprocessed higher-order bits.

6. After the loop, decide whether the original number had odd parity:
   - `setBits % 2 == 1`, or
   - `parity == 1`

7. If odd, increment `oddCount`.

8. After all elements are processed, return `oddCount`.

This is correct because every bit of every number is examined exactly once, and parity depends only on whether the total number of observed `1` bits is odd or even. Since each value is at most `10^9`, the inner loop is bounded by roughly 30 iterations.

## 📊 Worked Example
Take `readings = [0, 5, 7, 8, 10]`.

| Reading | Binary | Bit scan result | Odd parity? | Running total |
|---|---|---:|---|---:|
| 0 | `0` | 0 set bits | No | 0 |
| 5 | `101` | bits: `1,0,1` → 2 set bits | No | 0 |
| 7 | `111` | bits: `1,1,1` → 3 set bits | Yes | 1 |
| 8 | `1000` | bits: `0,0,0,1` → 1 set bit | Yes | 2 |
| 10 | `1010` | bits: `0,1,0,1` → 2 set bits | No | 2 |

Trace for `7`:
1. `7 & 1 = 1` → parity flips to odd, `7 >> 1 = 3`
2. `3 & 1 = 1` → parity flips to even, `3 >> 1 = 1`
3. `1 & 1 = 1` → parity flips to odd, `1 >> 1 = 0`

Final answer: `2`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * b)`, where `n` is the number of readings and `b` is the number of bits per integer. Here `b` is at most about 30 for values up to `10^9`, so this is effectively linear in input size. At `10^6` elements it remains practical; at `10^9`, total scan volume becomes the real bottleneck regardless of implementation details.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses only a few scalar variables such as the running answer and per-number bit counters or parity flags. Space cannot be meaningfully reduced further; the only trade-off is readability versus using built-in popcount primitives.

## 💡 Key Takeaways
- If a problem asks about the number of `1` bits, parity, masks, or binary properties, treat bit manipulation as the default tool before considering higher-level representations.
- Independent per-element classification over an array is a strong signal for a single-pass solution with constant auxiliary state.
- `0` is an easy trap: it has zero set bits, and zero is even, so it must not be counted.
- Be careful not to mutate the original value if later logic still needs it; use a temporary variable when scanning bits.
- In production code, choosing bitwise operations over string-based inspection is less about asymptotics and more about keeping hot paths allocation-free, predictable, and CPU-efficient.

## 🚀 Variations & Further Practice
- Count readings with exactly `k` set bits instead of odd parity; the twist is moving from parity classification to exact population count.
- Return whether the XOR of all readings has odd parity; the twist is exploiting parity algebra across the full array rather than evaluating each element independently.
- Support online updates and range queries for odd-parity counts; the twist is adding a Fenwick tree or segment tree over per-element parity states.