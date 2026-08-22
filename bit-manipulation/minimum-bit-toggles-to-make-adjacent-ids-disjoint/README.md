# Minimum Bit Toggles to Make Adjacent IDs Disjoint

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Bit Manipulation &nbsp;|&nbsp; **Tags:** Bit Manipulation, Dynamic Programming, Bitmask

---

## 🗂 Problem Overview
Given an array of bitmask-encoded integers, remove the fewest set bits so every adjacent pair becomes bitwise disjoint. One operation clears exactly one existing `1` bit from a single element. You may only turn bits off, never on. The output is the minimum total number of cleared bits across the array. The challenge is that `n` is large (`10^5`), so local greedy edits can easily break global optimality across neighboring positions.

## 🌍 Engineering Impact
This pattern shows up anywhere compact bitsets encode capabilities, ownership, routing classes, or resource claims. Examples include scheduler affinity masks, compiler register/live-range masks, streaming pipeline feature flags, and search/ranking systems with packed eligibility signals. At scale, pairwise conflicts between neighboring records often need to be eliminated with minimal loss of information. A naive per-edge fix over-corrects because each bit decision affects two constraints at once. The useful abstraction is not “edit each conflict,” but “choose a retained state per position under adjacency constraints,” which is exactly the kind of framing that keeps large optimization pipelines predictable and cheap.

## 🔍 Problem Statement
You are given `nums`, an array of length `n`, where `1 <= n <= 100000` and each `nums[i]` satisfies `0 <= nums[i] < 2^20`. Each value is a bitmask. Adjacent elements conflict when they share any set bit:

`(nums[i] & nums[i+1]) != 0`

In one operation, you may clear exactly one set bit from any `nums[i]`. If bit `b` is set, you may replace the value with:

`nums[i] ^ (1 << b)`

You may perform any number of such operations, but cannot turn bits on. Return the minimum number of bit clears needed so that every adjacent pair satisfies:

`(nums[i] & nums[i+1]) == 0`

Examples:

- `nums = [3, 6, 5]` → `2`
- `nums = [7, 7]` → `3`

The key constraint is `n = 10^5`, while each number has only 20 bits. That strongly suggests dynamic programming over retained submasks rather than search over edit sequences.

## 🪜 How to Solve This
1. Read the operation carefully → you are not editing bits arbitrarily; you are choosing a **submask** of each original number.  
2. Reframe the cost → if you keep submask `m` from `nums[i]`, the cost is simply `popcount(nums[i]) - popcount(m)`. So maximizing kept bits is equivalent to minimizing toggles.  
3. Notice the only constraint between positions `i-1` and `i` is `kept[i-1] & kept[i] == 0`. That is a classic adjacency-constrained DP.  
4. A brute-force DP over all `2^20` masks per position is too large. But each position only allows submasks of `nums[i]`, and a 20-bit number has at most `2^20` submasks in theory, usually far fewer.  
5. The right transition is: for each kept mask at `i`, find the best previous kept mask disjoint from it.  
6. That “best value over all submasks of a complement” is a standard **SOS DP / subset DP** pattern.  
7. So the flow becomes: enumerate submasks of the current value, score them, and use subset-max preprocessing on the previous layer to answer disjointness queries efficiently.

## 🧩 Algorithm Walkthrough
1. **Convert edits into retained-state optimization.**  
   Let `FULL = (1 << 20) - 1`. For each index `i`, choose a retained mask `k` such that `k ⊆ nums[i]`. The cost contributed by `i` is the number of removed bits: `popcount(nums[i]) - popcount(k)`. This is correct because every valid sequence of bit clears ends at some submask, and every submask is reachable by clearing exactly the missing bits.

2. **Define the DP state.**  
   Let `dp_prev[k]` be the maximum total number of kept bits up to the previous index if the retained mask at that index is exactly `k`. Only masks that are submasks of `nums[i-1]` are valid. The invariant is: every stored state already satisfies all adjacency constraints up to that position.

3. **Prepare fast disjoint transitions with subset DP.**  
   For current mask `k`, we need the best previous mask `p` with `p & k == 0`. Equivalently, `p` must be a submask of `FULL ^ k`. Build an array `best[s]` initialized from `dp_prev`, then run SOS DP so `best[s]` becomes the maximum `dp_prev[p]` over all `p ⊆ s`. This is the standard subset-maximum transform.

4. **Transition to the current index.**  
   Enumerate every submask `k` of `nums[i]`. The best chain ending at `k` is `best[FULL ^ k] + popcount(k)`. This is correct because the complement query enforces disjointness with the previous retained mask, and adding `popcount(k)` accumulates kept bits.

5. **Roll the layer.**  
   Store these values as the next `dp_prev` and repeat. The invariant remains: each state represents the best total kept bits for a valid prefix ending with that exact retained mask.

6. **Recover the answer.**  
   Let `total = sum(popcount(nums[i]))`. If `max_kept` is the best final DP value, the minimum toggles are `total - max_kept`. This works because every original set bit is either kept once or removed once; minimizing removals is identical to maximizing retained bits.

## 📊 Worked Example
Take `nums = [3, 6, 5]`, i.e. `011, 110, 101`.

| i | nums[i] | valid kept submasks | best choice per step |
|---|---|---|---|
| 0 | `011` | `000,001,010,011` | keep `011` → kept bits = 2 |
| 1 | `110` | `000,010,100,110` | cannot follow `011` with `010` or `110`; best valid is keep `100` after previous `011` → total kept = 3 |
| 2 | `101` | `000,001,100,101` | previous best ending at `100`; disjoint choices are `001` or `000`; keep `001` → total kept = 4 |

Trace:
1. Total original set bits = `2 + 2 + 2 = 6`.
2. Best valid retained sequence is `[011, 100, 001]`.
3. Adjacent checks: `011 & 100 = 0`, `100 & 001 = 0`.
4. Total kept bits = `2 + 1 + 1 = 4`.
5. Minimum toggles = `6 - 4 = 2`.

## ⏱ Complexity Analysis
### Time Complexity
For each position, enumerate all submasks of `nums[i]`, then run a 20-bit SOS DP over the mask universe. That yields `O(n * (2^20 * 20 + submasks(nums[i])))` in the straightforward formulation, dominated by the subset transform. This is viable because the bit-width is fixed at 20; it would be impossible at `10^6` bits, but acceptable for `10^5` elements with a small mask domain.

### Space Complexity
`O(2^20)` for the DP layer and subset-max buffer over the mask universe. The space is owned by fixed-size arrays indexed by mask, not by `n`. You can roll layers to avoid `O(n * 2^20)`, trading simpler debugging for production-feasible memory.

## 💡 Key Takeaways
• If the operation only clears bits, think “choose a submask,” not “simulate edits.” That usually collapses the state space.  
• If adjacent compatibility is `a & b == 0`, look for DP over masks plus complement/subset queries rather than greedy conflict removal.  
• Be careful to optimize for **kept bits** and subtract from the total at the end; minimizing removals directly is easier to get wrong in transitions.  
• When enumerating submasks, include `0`; excluding it silently breaks cases where a position must be fully cleared to satisfy both neighbors.  
• The production-level insight is to separate a local state representation from a global compatibility constraint, then accelerate the compatibility lookup with a transform instead of pairwise comparisons.

## 🚀 Variations & Further Practice
- Require every pair within distance `k` to be disjoint, not just immediate neighbors. The twist is that the DP state must remember more history, often as a sliding window of retained masks.  
- Allow toggling bits on as well as off with different costs. The twist is that states are no longer restricted to submasks, so the search space and transition logic change substantially.  
- Replace the path with a tree or DAG of adjacency constraints. The twist is moving from linear DP to tree DP or graph optimization over bitmask states.