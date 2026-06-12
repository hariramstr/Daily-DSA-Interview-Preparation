# Find the Missing Permission Flag

**Difficulty:** Easy &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, XOR, Array

---

## 🗂 Problem Overview
You are given `n - 1` distinct permission flags, where each flag is a power of two and the full bundle should contain every value from `2^0` through `2^(n-1)` exactly once. One flag is missing. Return that missing value. The key constraint is that the solution should use bit manipulation in linear time and constant extra space, which rules out unnecessary sorting or auxiliary sets.

## 🌍 Engineering Impact
This pattern shows up anywhere systems encode state as bit flags: IAM permission bundles, feature-gate masks, kernel capability sets, compiler optimization flags, and telemetry schemas. At scale, you do not want reconciliation logic that sorts, hashes, or materializes extra structures just to detect a single missing bit. XOR-based recovery gives deterministic O(n) behavior with constant memory, which matters in hot paths, streaming validation, and control-plane audits. Without this approach, simple integrity checks become allocation-heavy and harder to reason about under throughput, memory pressure, or constrained runtime environments.

## 🔍 Problem Statement
A complete permission bundle of size `n` should contain every power of two from `1` to `2^(n-1)` exactly once. You are given an integer array `flags` of length `n - 1`, containing distinct powers of two drawn from that set. Exactly one expected flag is absent, and you must return it.

Constraints:

- `2 <= n <= 30`
- `flags.length == n - 1`
- Each `flags[i]` is a power of two
- All values in `flags` are distinct
- Every `flags[i]` belongs to `{1, 2, 4, ..., 2^(n-1)}`
- Exactly one flag is missing

Examples:

- `flags = [1, 2, 8, 16]` → `4`
- `flags = [2, 4, 8, 16, 32, 1]` → `64`

The algorithmic driver is the requirement for linear time and constant extra space. That strongly suggests using XOR rather than sorting or membership tracking.

## 🪜 How to Solve This
1. Read the problem → the values are not arbitrary integers; they are a complete known set of powers of two with one missing.
2. Since every expected value appears exactly once except one, think in terms of cancellation rather than lookup.
3. XOR is the right tool because `a ^ a = 0` and `a ^ 0 = a`. If you XOR all expected flags and all provided flags together, every present value cancels out.
4. What remains after cancellation is exactly the missing flag.
5. So compute XOR over the full expected sequence: `1, 2, 4, ..., 2^(n-1)`.
6. Then XOR every value in `flags`.
7. The final accumulator is the answer.

Why this approach is natural: the input guarantees uniqueness and exactly one missing element from a known universe. That is the classic signal for XOR. You avoid sorting, avoid extra memory, and get a solution whose correctness follows directly from XOR’s cancellation property.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: XOR cancellation.**  
   This is a **Bit Manipulation / XOR** problem. The defining signal is: known complete set, one missing element, no duplicates, and constant-space expectation. XOR is the right abstraction because it cancels matched values without storing them.

2. **Initialize an accumulator.**  
   Start with `missing = 0`. This works because `0` is XOR’s identity element, so it does not affect the result.

3. **XOR all expected permission flags.**  
   Iterate `i` from `0` to `n - 1`, compute `1 << i`, and XOR it into `missing`.  
   After this step, the accumulator equals:  
   `1 ^ 2 ^ 4 ^ ... ^ 2^(n-1)`  
   Invariant: `missing` holds the XOR of every flag that should exist in the full bundle.

4. **XOR all observed flags from the input array.**  
   For each `flag` in `flags`, do `missing ^= flag`.  
   Every flag that exists in both the expected set and the input appears twice in the total XOR and cancels to zero.

5. **Return the accumulator.**  
   The only value that was included once, not twice, is the absent permission flag.  
   Invariant at termination: all present flags have canceled; only the missing flag remains.

This is correct because XOR is associative and commutative, so order does not matter, and pairwise cancellation always holds.

## 📊 Worked Example
Example: `flags = [1, 2, 8, 16]`, so `n = 5`

Expected full set: `[1, 2, 4, 8, 16]`

| Step | Operation | `missing` |
|---|---|---:|
| 1 | start | 0 |
| 2 | XOR expected `1` | 1 |
| 3 | XOR expected `2` | 3 |
| 4 | XOR expected `4` | 7 |
| 5 | XOR expected `8` | 15 |
| 6 | XOR expected `16` | 31 |
| 7 | XOR actual `1` | 30 |
| 8 | XOR actual `2` | 28 |
| 9 | XOR actual `8` | 20 |
| 10 | XOR actual `16` | 4 |

Final result: `4`

Why it works: every present flag is XORed twice — once from the expected set and once from `flags` — so those values cancel. Only `4` appears once, so it survives as the answer.

## ⏱ Complexity Analysis
### Time Complexity
The algorithm runs in **O(n)** time: one pass over the `n` expected flags and one pass over the `n - 1` input flags. The dominant operation is XOR, which is constant time. At `10^6` elements this is trivial; at `10^9`, linear scan cost dominates but remains asymptotically optimal.

### Space Complexity
The algorithm uses **O(1)** extra space. The only additional state is a single accumulator and loop variables. There is no auxiliary array, hash set, or sort buffer. Space cannot be meaningfully reduced further without changing the execution model.

## 💡 Key Takeaways
- If the problem gives a complete known set with exactly one missing item and no duplicates, XOR should be one of the first tools you consider.
- Constant-space requirements plus pairwise cancellation semantics are strong signals for a bit-manipulation solution instead of sorting or hashing.
- Be careful to generate the full expected range correctly: it is `2^0` through `2^(n-1)`, so the loop must run exactly `n` times.
- Do not infer `n` incorrectly from the array length without accounting for the missing element; `flags.length = n - 1`.
- In production code, XOR-based reconciliation is valuable because it turns integrity checks into streaming, allocation-free operations with predictable performance.

## 🚀 Variations & Further Practice
- Find two missing flags instead of one: XOR alone is not enough; you must partition by a distinguishing bit after XORing the combined result.
- One number appears once while all others appear twice: same cancellation pattern, but the expected universe is implicit rather than explicitly generated.
- Detect a missing and a duplicated flag in the same bundle: cancellation still helps, but now you need arithmetic or bit partitioning to separate the two anomalies.

---