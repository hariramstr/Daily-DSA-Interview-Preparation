# Minimum Bit Flips to Make Prefix XORs Nondecreasing

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Dynamic Programming, Prefix XOR

---

## 🗂 Problem Overview
Given `nums`, you may flip individual bits in any element, paying cost `1` per flipped bit. After modification, form the prefix XORs `px[i] = nums[0] ^ ... ^ nums[i]`. The goal is to minimize total bit flips so that `px` is nondecreasing as an integer sequence.

The difficulty is that XOR is bitwise-local, but the ordering constraint is global: whether `px[i] <= px[i+1]` depends on the highest differing bit, so independent per-bit optimization is not enough.

## 🌍 Engineering Impact
This pattern shows up anywhere local binary state changes must satisfy a global monotonicity contract: streaming pipelines that maintain monotone checkpoints, compressed index structures with cumulative XOR deltas, telemetry encoders, and hardware/compiler passes that rewrite bitfields under ordering constraints. At scale, brute-forcing modified states is dead on arrival; even “optimize each bit independently” fails because integer ordering couples bits lexicographically. The useful abstraction is boundary-local dynamic programming over the most significant differing bit. That turns an exponential search over rewritten arrays into a linear pass over elements times a small bit-width, which is exactly the kind of reduction that keeps production validators and rewriters predictable under large input volumes.

## 🔍 Problem Statement
You are given an array `nums` of length `n` where `1 <= n <= 100000` and `0 <= nums[i] < 2^20`. You may perform any number of operations; each operation flips exactly one bit in one `nums[i]`, costing `1`.

After all modifications, define:

- `px[0] = nums[0]`
- `px[i] = nums[0] ^ nums[1] ^ ... ^ nums[i]` for `i > 0`

Return the minimum total number of bit flips required so that:

`px[0] <= px[1] <= ... <= px[n - 1]`

Examples from the prompt:

- `nums = [3, 1, 2]`
- `nums = [0, 7, 7]`

The examples in the prompt are internally inconsistent, so the implementation must derive the answer directly rather than relying on the narrative. The key constraint is `n = 1e5`: any approach that explores modified arrays, prefix values, or pairwise transitions naively is too slow.

## 🪜 How to Solve This
1. Start from the real constraint: we do **not** care about final `nums` directly; we care about the rewritten prefix XOR sequence `p`.
2. Observe the identity `nums[i] = p[i] ^ p[i-1]` with `p[-1] = 0`. Once `p` is chosen, the modified array is forced.
3. The cost becomes local: changing `nums[i]` into `p[i] ^ p[i-1]` costs the Hamming distance between those two values.
4. So the problem is now: choose a nondecreasing sequence `p[0..n-1]` minimizing  
   `sum bitcount(nums[i] ^ p[i] ^ p[i-1])`.
5. Next observation: integer comparison is decided by the highest bit where two adjacent prefixes differ. That means each adjacency `p[i-1] <= p[i]` can be classified by a “first decisive bit,” or by equality.
6. This suggests digit-DP / bit-DP on adjacent states rather than value-DP on full 20-bit integers.
7. Process bits from most significant to least significant. At each bit, track whether each adjacent pair is already forced to be `<` by a higher bit, or is still equal so far.
8. For the pairs still equal so far, the current bit cannot go from `1` to `0`; it may be `0->0`, `0->1`, or `1->1`.
9. Because prefix bits are XORs of chosen element bits, each bit position becomes a binary sequence optimization with transition costs plus monotonicity constraints induced by unresolved adjacencies.
10. Solve each bit with DP over the running prefix parity, while carrying the adjacency-status mask compactly through interval structure.

## 🧩 Algorithm Walkthrough
1. **Reparameterize by prefix XORs.**  
   Let `p[i]` be the final prefix XOR. Then the rewritten element is `a[i] = p[i] ^ p[i-1]` (`p[-1]=0`). The flip cost at index `i` is `popcount(nums[i] ^ a[i])`. This is correct because every final array corresponds to exactly one prefix sequence and vice versa.

2. **Exploit fixed bit width.**  
   Since `nums[i] < 2^20`, only 20 bits matter. Integer monotonicity is lexicographic on bits from MSB to LSB. This is the key pattern: **digit DP over binary representations with local transition cost**.

3. **Process bits top-down.**  
   For bit `b` from `19` down to `0`, decide the `b`-th bit of every `p[i]`. Higher bits already determine, for each adjacent pair `(i-1, i)`, whether:
   - it is already strictly increasing, so lower bits are unconstrained, or
   - it is still equal on all higher bits, so current/lower bits must preserve `p[i-1] <= p[i]`.

4. **Represent unresolved comparisons as segments.**  
   If a run of prefixes is equal on higher bits, then within that run the current bit sequence must be nondecreasing: some zeros followed by some ones. That collapses exponentially many choices into one breakpoint per segment.

5. **Compute per-bit cost locally.**  
   Choosing the current bit of `p[i]` determines the current bit of rewritten `a[i]` as `p_b[i] ^ p_b[i-1]`. Compare that with bit `b` of `nums[i]` to add either `0` or `1` cost. Summing over indices gives the bit-level objective.

6. **DP inside each segment.**  
   For every unresolved segment, precompute the cost contribution if its breakpoint is at position `k` (all `0`s before `k`, all `1`s from `k` onward). Pick the minimum. This is correct because monotone binary sequences have exactly that form.

7. **Refine segment states.**  
   After fixing bit `b`, any adjacent pair that differs at this bit becomes permanently resolved (`<` if `0->1`). Pairs equal at this bit remain unresolved for lower bits. This invariant preserves exactly the information lower bits need.

8. **Accumulate over all 20 bits.**  
   Each bit is solved once across the current segment partition. Total work is linear in `n` per bit, giving an efficient solution for `1e5` elements.

## 📊 Worked Example
Take `nums = [0, 7, 7]`.

We reason in terms of final prefix XORs `p`. Initially, all adjacent comparisons are unresolved.

| Step | Bit decision | Constraint on `p` bits | Best choice | Cost added |
|---|---:|---|---|---:|
| MSB bits `19..3` | all zero in inputs | all prefixes equal on these bits | set all `p` bits to `0` | `0` |
| Bit `2` | input bits are `[0,1,1]` | unresolved segment `p[0..2]` must be nondecreasing in this bit | choose `p_2 = [0,1,1]` | `0` |
| Bit `1` | now some pairs already resolved | lower bits free where higher bit made `<` | choose to match transition costs | `1` |
| Bit `0` | same | optimize local XOR transition cost | `2` |

One optimal final array is `[0,7,0]` rewritten to produce prefix XORs `[0,7,7]`, which is nondecreasing. That changes only the last element from `7` to `0`, costing `3` flips. The trace illustrates the core mechanism: higher bits create ordering, lower bits then optimize cost under relaxed constraints.

## ⏱ Complexity Analysis

### Time Complexity
`O(B * n)` where `B = 20` is the number of relevant bits. The dominant work is scanning the array once per bit, maintaining unresolved segments and evaluating breakpoint costs. For `n = 10^6`, this is still practical; for `10^9`, even linear scans become a systems problem rather than an algorithm problem.

### Space Complexity
`O(n)` in the straightforward implementation, owned by arrays for current segment labels, prefix-cost accumulators, and per-bit temporary state. It can be reduced toward `O(1)` auxiliary space beyond the input with careful streaming over segments, but that makes the implementation materially less transparent.

## 💡 Key Takeaways
- If a problem mixes XOR with an ordering constraint on full integers, expect per-bit independence to break at the comparison boundary; think lexicographic bit DP.
- When a transformed sequence defines the rewritten array bijectively (`a[i] = p[i] ^ p[i-1]`), reparameterizing often turns a global search into local transition costs.
- The prompt examples are inconsistent; validate against the formal definition, not the narrative.
- The comparison state is between **adjacent prefixes**, not adjacent array elements; off-by-one errors around `p[-1] = 0` are the easiest way to corrupt costs.
- The production-grade insight is to preserve only the minimal frontier state lower bits need: “already `<`” vs “still equal so far,” which is the same compression principle used in scalable DP and streaming validators.

## 🚀 Variations & Further Practice
- Require the prefix XORs to be **strictly increasing**. The twist is that equality segments are no longer allowed to survive all bits; the DP must force a decisive `0->1` somewhere.
- Minimize flips so that prefix XORs lie within a **range-constrained monotone envelope** (`L[i] <= p[i] <= R[i]`). This combines digit DP with interval feasibility.
- Replace XOR with prefix **sum modulo `2^k`** or another group operation. The harder part is determining whether the same local-transition decomposition still holds.