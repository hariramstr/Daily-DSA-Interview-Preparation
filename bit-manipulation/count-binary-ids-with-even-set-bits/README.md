# Count Binary IDs With Even Set Bits

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Array, Parity

---

## 🗂 Problem Overview
Given an array of non-negative integers `ids`, count how many values have an even number of set bits in their binary representation. Each element is evaluated independently, so the task is a linear scan plus a per-number parity check. The non-trivial part is choosing an efficient bit-counting method under `ids.length <= 100000`, where unnecessary string conversion or repeated high-level allocations become avoidable overhead.

## 🌍 Engineering Impact
This pattern shows up anywhere parity or population count is part of a hot-path validation step: packet integrity checks, bitmap indexes in analytics engines, Bloom filter diagnostics, SIMD-friendly column stores, and low-level storage systems tracking allocation masks. At scale, the issue is not correctness but cost per element. A naive representation-heavy implementation can dominate CPU time in streaming pipelines or telemetry ingestion. Bitwise parity checks keep the operation branch-light, cache-friendly, and composable, which matters when this logic is embedded inside larger scans over millions of records or hardware-adjacent code paths.

## 🔍 Problem Statement
You are given an integer array `ids` where each value is a non-negative item ID. An ID is considered valid if its binary representation contains an even number of `1` bits. Return the total count of valid IDs.

Constraints:

- `1 <= ids.length <= 100000`
- `0 <= ids[i] <= 10^9`
- The result fits in a 32-bit integer

Examples:

- `ids = [0, 1, 2, 3, 4]` → `2`  
  Valid IDs: `0` (`0` set bits), `3` (`2` set bits)

- `ids = [5, 6, 7, 8, 15]` → `3`  
  Valid IDs: `5` (`2`), `6` (`2`), `15` (`4`)

Edge cases matter: `0` is valid because it has zero set bits, and zero is even. The key constraint is input size: with up to `100000` values, the solution should be a single pass with efficient per-number bit processing.

## 🪜 How to Solve This
1. Read the problem → each number is independent. There is no ordering, grouping, or cross-element dependency, so this should be a scan, not a nested comparison.

2. For each ID, the only question is parity of its set-bit count: even or odd. We do not need the exact binary string, only whether the count of `1`s is divisible by 2.

3. That immediately suggests bit manipulation. Converting every number to a binary string works, but it adds unnecessary overhead and hides the actual constant-time primitive the problem is testing.

4. Two natural approaches appear:
   - shift through all bits and count `1`s
   - repeatedly clear the lowest set bit using `x &= x - 1`

5. The second approach is usually cleaner and often faster because it loops only once per set bit, not once per bit position.

6. Maintain a running answer. For each number, compute its set-bit count, check `count % 2 == 0`, and increment the answer if true.

7. Since `ids[i] <= 10^9`, each number has at most 30 significant bits, so even the straightforward bitwise solution is easily within bounds.

## 🧩 Algorithm Walkthrough
1. **Choose the pattern: linear scan + bit counting.**  
   This is a classic **Bit Manipulation** problem. The array contributes only iteration; the real work is a per-element popcount/parity check. That abstraction is correct because validity depends solely on one integer at a time.

2. **Initialize an answer counter.**  
   Start with `valid = 0`.  
   Invariant: after processing the first `i` elements, `valid` equals the number of IDs with even set-bit parity among those `i` elements.

3. **Process each ID independently.**  
   For each `x` in `ids`, create a working copy `n = x` and a local `bits = 0`.  
   Why a copy: we need to mutate `n` during bit processing without losing the original value.

4. **Count set bits using Brian Kernighan’s trick.**  
   While `n != 0`, do:
   - `n = n & (n - 1)`
   - `bits++`  
   Why it works: `n - 1` flips the lowest set bit to `0` and turns trailing zeros/ones appropriately; `n & (n - 1)` removes exactly the lowest set bit. Therefore each iteration corresponds to one `1` bit.

5. **Check parity, not magnitude.**  
   If `bits % 2 == 0`, increment `valid`.  
   Correctness follows because the problem defines validity exactly by even popcount.

6. **Handle zero naturally.**  
   If `x = 0`, the loop never runs, `bits` stays `0`, and the value is counted as valid. This preserves the invariant without special-case branching.

7. **Return the final count.**  
   After the scan completes, the invariant implies `valid` is the answer for the full array.

## 📊 Worked Example
Use `ids = [5, 6, 7, 8, 15]`.

| ID | Binary | Bit-count trace via `n &= n-1` | Set bits | Even? | Running valid |
|---|---|---|---:|---|---:|
| 5 | `101` | `5 -> 4 -> 0` | 2 | Yes | 1 |
| 6 | `110` | `6 -> 4 -> 0` | 2 | Yes | 2 |
| 7 | `111` | `7 -> 6 -> 4 -> 0` | 3 | No | 2 |
| 8 | `1000` | `8 -> 0` | 1 | No | 2 |
| 15 | `1111` | `15 -> 14 -> 12 -> 8 -> 0` | 4 | Yes | 3 |

Trace summary:

1. Start `valid = 0`.
2. `5` has two set bits → count it.
3. `6` has two set bits → count it.
4. `7` has three set bits → skip.
5. `8` has one set bit → skip.
6. `15` has four set bits → count it.

Final answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n * k)` in the general form, where `n` is the number of IDs and `k` is the number of set bits per ID when using `n &= n - 1`. Under the given constraint, `k <= 30`, so this is effectively linear in input size. At `10^6` elements it remains practical; at `10^9`, throughput and memory bandwidth dominate.

### Space Complexity
`O(1)` auxiliary space. The algorithm uses a few scalar variables: the answer counter, a working copy of the current number, and a local bit counter. No extra arrays or hash-based structures are required. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If each array element can be classified independently by a property of its bits, expect a single-pass bit-manipulation solution rather than sorting, grouping, or dynamic programming.
- “Even/odd number of set bits” is a direct parity signal; that should trigger popcount or parity-check thinking immediately.
- `0` is easy to mishandle mentally: it has zero set bits, and zero is even, so it must be counted.
- If you mutate the number while counting bits, use a temporary variable; otherwise you destroy the original value and complicate debugging or later logic.
- In production code, compact bitwise predicates matter because they compose well inside high-volume scans where per-record overhead, not asymptotic complexity, is the real bottleneck.

## 🚀 Variations & Further Practice
- Count IDs with **odd** set-bit parity instead; same pattern, but the predicate flips and can be optimized further by tracking parity without full counts.
- Return the **sum of IDs** with even parity instead of the count; same scan, but aggregation changes from frequency to weighted accumulation.
- Given many range queries over the array, answer how many IDs in `ids[l..r]` have even parity; the twist is moving from one-pass evaluation to preprocessing with prefix counts.