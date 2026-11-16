# Minimum Verification Timeout for Batched Signatures

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Arrays, Scheduling

---

## 🗂 Problem Overview
Given `verifyTime[i]` and nondecreasing `deadline[i]`, find the smallest integer timeout `T` such that processing signatures sequentially in arrival order still finishes each signature by its deadline. Under timeout `T`, a signature taking `x` milliseconds consumes `ceil(x / T)` slices, each of length `T`, so its effective runtime becomes `ceil(x / T) * T`. The challenge is that changing one global parameter affects every job’s rounded runtime and therefore all cumulative completion times.

## 🌍 Engineering Impact
This pattern shows up anywhere a platform chooses a single batching or quantum parameter that globally changes latency behavior: RPC timeout buckets, GPU kernel scheduling quanta, storage compaction windows, packet coalescing intervals, and verification or scanning pipelines with fixed worker budgets. At scale, you rarely tune each item independently; you pick one operational knob and need to know the minimum safe value. Without a monotonic-search approach, teams end up simulating too many configurations or overprovisioning conservatively. Binary search over a feasibility predicate turns a potentially massive tuning space into something operationally cheap and predictable.

## 🔍 Problem Statement
You are given two arrays of length `n`:

- `verifyTime[i]`: CPU time required by the `i`-th signature
- `deadline[i]`: latest allowed completion time for the `i`-th signature, measured from batch start

Signatures must be processed sequentially in original order. For a chosen integer timeout `T`, signature `i` consumes `ceil(verifyTime[i] / T) * T` milliseconds. Its completion time is the cumulative sum of those rounded runtimes from index `0` through `i`. The goal is to return the minimum integer `T` such that every completion time is `<= deadline[i]`.

Constraints:

- `1 <= n <= 100000`
- `1 <= verifyTime[i] <= 10^9`
- `1 <= deadline[i] <= 10^18`
- `deadline` is sorted in nondecreasing order
- At least one valid `T` exists

Examples:

- `verifyTime = [5, 8, 6], deadline = [10, 20, 30]` → answer `1`
- `verifyTime = [7, 7, 7], deadline = [7, 20, 27]` → answer `7`

The key constraint is `n = 1e5`, which rules out scanning many candidate `T` values directly.

## 🪜 How to Solve This
1. Read the formula carefully → for a fixed `T`, every job runtime becomes `ceil(x / T) * T`, and completion times are just prefix sums of those values.

2. Ask the critical question: if some `T` works, what happens for a smaller `T`?  
   Smaller slices reduce rounding waste. In fact, `ceil(x / T) * T` is nondecreasing as `T` grows, so larger `T` never helps.

3. That gives a monotonic predicate:  
   `feasible(T) = all completion times meet deadlines`.  
   If `feasible(T)` is true, then every smaller `T` is also true.

4. Monotonic predicate over integer search space → think binary search immediately.

5. How do we check feasibility?  
   Scan left to right, accumulate rounded runtimes, and fail as soon as cumulative time exceeds `deadline[i]`.

6. What are the bounds?  
   Minimum possible `T` is `1`. A safe upper bound is `max(verifyTime)`, because then each job takes exactly its raw time with no extra rounding, and the problem guarantees some valid answer exists.

7. So the full solution is: binary search `T` in `[1, maxVerifyTime]`, using an `O(n)` feasibility check.

## 🧩 Algorithm Walkthrough
1. **Define the pattern: binary search on answer.**  
   We are not searching an array; we are searching the smallest integer parameter `T` for which a monotonic condition holds. This is the right abstraction because feasibility flips only once: from true at small `T` to false at large `T`.

2. **Choose search bounds.**  
   Set `lo = 1`, `hi = max(verifyTime)`.  
   Why is `hi` safe? For any `T >= verifyTime[i]`, job `i` takes exactly `verifyTime[i]`, so at `T = max(verifyTime)` the total schedule is the raw prefix sums. Since at least one valid `T` exists and larger `T` only hurts, validity must already hold by this point.

3. **Implement `feasible(T)`.**  
   Maintain `currentTime = 0`. For each signature:
   - compute `slices = (verifyTime[i] + T - 1) // T`
   - add `slices * T` to `currentTime`
   - if `currentTime > deadline[i]`, return false immediately  
   Invariant: after processing index `i`, `currentTime` equals the exact completion time of signature `i` under timeout `T`.

4. **Binary search for the minimum valid `T`.**  
   While `lo < hi`, test `mid = lo + (hi - lo) // 2`.  
   - If `feasible(mid)` is true, keep the left half: `hi = mid`
   - Otherwise, discard it: `lo = mid + 1`  
   Invariant: the answer always remains inside `[lo, hi]`.

5. **Return `lo`.**  
   At termination, `lo == hi`, and by the invariant it is the smallest feasible timeout.

This yields `O(n log M)` time where `M = max(verifyTime)`.

## 📊 Worked Example
Use `verifyTime = [7, 7, 7]`, `deadline = [7, 20, 27]`.

Binary search range: `T in [1, 7]`.

| T | Rounded runtimes | Completion times | Feasible? |
|---|---|---|---|
| 4 | `8, 8, 8` | `8, 16, 24` | No, first misses `7` |
| 2 | `8, 8, 8` | `8, 16, 24` | No |
| 1 | `7, 7, 7` | `7, 14, 21` | Yes |
| 3 | `9, 9, 9` | `9, 18, 27` | No |

Binary search trace:

1. `lo=1, hi=7, mid=4` → infeasible, so `lo=5` would be wrong only if predicate increased; it does not. Since larger `T` is worse, use standard monotone-valid-on-left framing: search smallest invalid boundary or equivalently smallest feasible with true on left.  
2. More simply, keep `feasible(T)` as “all deadlines met,” which is true for smaller `T`. Then search rightmost true or invert predicate.  
3. The minimum valid timeout here is `1`.

Implementation detail: because validity is monotone toward smaller `T`, many engineers invert the search and instead find the first failing `T`, then subtract one. An equally clean approach is to binary search on `T` with adjusted branch semantics.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check is a single linear pass over `n` signatures, so it costs `O(n)`. Binary search performs `O(log M)` checks where `M = max(verifyTime)`, giving total time `O(n log M)`. With `n = 10^6`, this remains practical; with `n = 10^9`, the linear scan itself dominates and distribution or streaming becomes the real constraint.

### Space Complexity
The algorithm uses `O(1)` auxiliary space beyond the input arrays. The only extra state is the binary-search bounds and a 64-bit running completion time. Space cannot be meaningfully reduced further unless inputs are streamed, which trades random access for one-pass feasibility evaluation.

## 💡 Key Takeaways
- If the problem asks for the minimum global parameter that makes a schedule or prefix constraint valid, look for a monotonic feasibility predicate and binary search the answer.
- When one knob changes every item’s rounded cost in the same direction, cumulative deadlines usually reduce to a linear feasibility scan.
- Use 64-bit arithmetic for completion times and rounded runtimes; `deadline[i]` can reach `10^18`, so 32-bit accumulation will overflow.
- Be explicit about monotonic direction: in this problem, smaller `T` is better, which is the opposite of many “minimum capacity” binary-search problems.
- In production systems, a single global tuning parameter often creates stepwise cost changes via rounding or batching; the right model is usually “search the control plane, simulate the data plane.”

## 🚀 Variations & Further Practice
- **Per-signature reordering allowed:** deadlines remain, but jobs can be reordered. The twist is that feasibility is no longer a simple prefix check; scheduling policy now matters.
- **Multiple workers processing in parallel:** each signature still rounds by `T`, but assignment across `k` workers introduces load balancing and machine scheduling complexity.
- **Timeout has a fixed per-slice overhead:** each slice costs `T + c` instead of `T`. The monotonic behavior may still hold, but the feasibility function changes and edge cases become sharper.