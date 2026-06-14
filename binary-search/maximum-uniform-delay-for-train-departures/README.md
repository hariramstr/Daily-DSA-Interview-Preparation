# Maximum Uniform Delay for Train Departures

**Difficulty:** Hard &nbsp;|&nbsp; **Topic:** Binary Search &nbsp;|&nbsp; **Tags:** Binary Search, Greedy, Scheduling

---

## 🗂 Problem Overview
Given sorted planned departure times and a per-train delay budget `maxDelay`, assign each train an actual departure within its allowed window `[departures[i], departures[i] + maxDelay]`. The actual schedule must remain strictly increasing, and every adjacent pair must be separated by at least `gap` minutes. Return the maximum feasible integer `gap`. The challenge is that feasibility depends on all earlier choices, so local delay decisions must preserve future scheduling flexibility across up to `2e5` trains.

## 🌍 Engineering Impact
This pattern shows up in production schedulers, traffic shaping, and distributed admission control: assign events within bounded windows while maximizing minimum spacing. Examples include packet pacing in network stacks, job launch smoothing in cluster schedulers, stream emission control in data pipelines, and API request spreading in rate-limiters. At scale, naive search or backtracking collapses under high cardinality and wide time ranges. The binary-search-plus-greedy pattern matters because it converts an optimization problem over a huge numeric domain into repeated linear feasibility checks, which is exactly the shape needed for predictable latency and capacity planning.

## 🔍 Problem Statement
You are given a nondecreasing integer array `departures` of length `n`, where `departures[i]` is the planned departure minute of the `i`-th train, and an integer `maxDelay`. For each train, choose an integer `actual[i]` such that:

1. `departures[i] <= actual[i] <= departures[i] + maxDelay`
2. `actual[i] < actual[i + 1]`
3. `actual[i + 1] - actual[i] >= gap`

Return the largest integer `gap` for which such an assignment exists.

Constraints:

- `2 <= n <= 200000`
- `0 <= departures[i] <= 10^14`
- `departures` is sorted in nondecreasing order
- `0 <= maxDelay <= 10^14`

Examples:

- `departures = [2, 4, 7], maxDelay = 3` → `4`
- `departures = [1, 1, 1, 1], maxDelay = 5` → `1`

The key driver is the combination of large `n` and a huge answer space, ruling out constructive search over candidate schedules.

## 🪜 How to Solve This
1. Read the objective carefully → we are not asked to build the best schedule directly; we are asked for the **maximum feasible minimum gap**.

2. “Maximum value such that condition holds” is a strong binary-search signal. If a gap `g` is feasible, then every smaller gap is also feasible. That gives a monotonic predicate.

3. Now define the predicate: for a fixed `g`, can we place all trains inside their windows while keeping adjacent departures at least `g` apart?

4. To test feasibility, greedily place each train as early as possible:
   - first train at `departures[0]`
   - each next train at `max(departures[i], previous + g)`

5. Why earliest placement? Because any later choice only reduces room for later trains. Earliest feasible placement maximizes remaining slack, so if this strategy fails, no other strategy can succeed.

6. With that predicate, binary search over `g` across the integer range. Each check is linear, so the total cost becomes `O(n log R)` where `R` is the numeric search space, not the combinatorial schedule space.

## 🧩 Algorithm Walkthrough
1. **Recognize the pattern: Binary Search on Answer + Greedy Feasibility Check.**  
   We are maximizing a scalar value `gap`, and feasibility is monotonic: if spacing `g` works, any `g' <= g` also works. That is the correct abstraction because the output is a number, not a schedule.

2. **Define each train’s legal window.**  
   Train `i` can be placed anywhere in `[departures[i], departures[i] + maxDelay]`. The problem becomes selecting one integer from each interval such that consecutive selections differ by at least `g`.

3. **Greedy feasibility for a fixed `g`.**  
   Set `actual[0] = departures[0]`. For each `i > 0`, choose  
   `actual[i] = max(departures[i], actual[i - 1] + g)`.  
   This is the earliest legal placement that preserves the required gap.

4. **Maintain the invariant.**  
   After processing index `i`, `actual[i]` is the smallest feasible departure time for train `i` given gap `g` and all prior choices. Therefore the suffix has maximum remaining slack. This invariant is what makes the greedy proof work.

5. **Detect failure immediately.**  
   If `actual[i] > departures[i] + maxDelay`, train `i` cannot be placed within its window. Since we already used the earliest possible placement, no alternate earlier schedule exists for the prefix, so `g` is infeasible.

6. **Binary search the answer.**  
   Search `g` over `[0, high]`, where a safe upper bound is  
   `departures[n - 1] + maxDelay - departures[0]`.  
   On feasible `mid`, move right; otherwise move left.

7. **Return the largest feasible `g`.**  
   This yields the optimal answer in logarithmically many checks over a 64-bit range.

## 📊 Worked Example
Use `departures = [2, 4, 7]`, `maxDelay = 3`, and test `gap = 4`.

| i | window     | previous actual | chosen actual = max(start, prev + 4) | valid? |
|---|------------|-----------------|--------------------------------------|--------|
| 0 | `[2, 5]`   | —               | `2`                                  | yes    |
| 1 | `[4, 7]`   | `2`             | `max(4, 6) = 6`                      | yes    |
| 2 | `[7, 10]`  | `6`             | `max(7, 10) = 10`                    | yes    |

So `gap = 4` is feasible with `actual = [2, 6, 10]`.

Now test `gap = 5`:

- `actual[0] = 2`
- `actual[1] = max(4, 7) = 7`
- `actual[2] = max(7, 12) = 12`

But train 2’s latest allowed time is `10`, so feasibility fails. Since the greedy placement was already earliest possible at every step, no alternate assignment can rescue `gap = 5`. Therefore the maximum gap is `4`.

## ⏱ Complexity Analysis
### Time Complexity
Each feasibility check scans the array once, so it costs `O(n)`. Binary search over the answer range costs `O(log R)`, where `R` is at most the total reachable spread in time. Total complexity is `O(n log R)`, which is practical even for `n = 2e5`; linear or quadratic alternatives are not.

### Space Complexity
The algorithm uses `O(1)` auxiliary space beyond the input array. The greedy check only tracks the previous assigned departure and binary-search bounds. You could materialize the full `actual` schedule for debugging, but that increases space to `O(n)` without improving asymptotic runtime.

## 💡 Key Takeaways
- If the problem asks for the largest numeric value satisfying a condition, check whether feasibility is monotonic; that is the classic trigger for binary search on the answer.
- If each item has an interval and you need to preserve future flexibility, “place as early as possible” is often the right greedy invariant.
- Do not forget that `gap >= 1` already implies strict increase, so the explicit `actual[i] < actual[i+1]` constraint is redundant for positive gaps but still naturally satisfied by the construction.
- Use 64-bit arithmetic throughout: `departures[i]`, `maxDelay`, `prev + gap`, and the binary-search midpoint can all exceed 32-bit limits.
- The transferable design insight is to separate optimization from construction: turn a hard global objective into a monotone decision problem with a cheap, local feasibility oracle.

## 🚀 Variations & Further Practice
- Allow each train to have its own delay budget `maxDelay[i]`; same pattern, but the feasibility windows become heterogeneous and edge-case handling gets sharper.
- Maximize the minimum gap when trains may also depart earlier within bounded advance windows; feasibility remains interval-based, but the search space becomes symmetric around planned times.
- Add a requirement to skip or cancel up to `k` trains; now the feasibility check must combine greedy placement with state tracking or DP, not just a single running timestamp.