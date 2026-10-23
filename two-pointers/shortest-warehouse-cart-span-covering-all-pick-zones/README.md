# Shortest Warehouse Cart Span Covering All Pick Zones

**Difficulty:** Medium &nbsp;|&nbsp; **Topic:** Two Pointers &nbsp;|&nbsp; **Tags:** Two Pointers, Sliding Window, Hash Map

---

## 🗂 Problem Overview
Given a route log `zones` and a distinct set `requiredZones`, find the minimum length of a contiguous subarray containing every required zone at least once. Return only that length, or `-1` if no such segment exists. The challenge is scale: `zones.length` can reach 200,000, so checking all subarrays is infeasible. The solution must detect coverage dynamically while scanning once.

## 🌍 Engineering Impact
This pattern shows up anywhere a system must identify the smallest contiguous event span satisfying a full requirement set: observability pipelines finding the shortest trace segment containing all critical services, security analytics locating the minimal log window covering all indicators of compromise, and warehouse or fulfillment systems isolating the shortest route fragment that touched every mandatory handling zone. At scale, brute-force range evaluation collapses under quadratic growth and destroys latency budgets. Sliding-window designs enable single-pass processing, bounded memory, and predictable behavior on large streams, which matters for online analytics, replay systems, and operational dashboards.

## 🔍 Problem Statement
You are given:

- `zones`: an array of strings representing the pick zones visited by a cart in order
- `requiredZones`: a distinct array of strings that must all appear in a valid contiguous segment

Return the length of the shortest contiguous segment of `zones` that contains every string in `requiredZones` at least once. Extra zones may appear inside the segment. If any required zone never appears, return `-1`.

Constraints:

- `1 <= zones.length <= 200000`
- `1 <= requiredZones.length <= 5000`
- `requiredZones` contains distinct strings
- zone names have length `1..20`

Examples:

- `zones = ["Inbound","A1","Cold","B2","Fragile","Cold","A1","Packing"]`
- `requiredZones = ["A1","Cold","Fragile"]`
- Output: `3`

- `zones = ["Dock","A1","B2","Dock","Packing"]`
- `requiredZones = ["A1","Cold"]`
- Output: `-1`

The key constraint is the route length: any algorithm that inspects many overlapping subarrays repeatedly will time out.

## 🪜 How to Solve This
1. Read the problem → we need the **shortest contiguous segment**, which is a strong signal for a sliding-window approach rather than sorting or global counting.

2. Notice the requirement is **set coverage**, not exact frequency matching. Each required zone only needs to appear at least once, so we care about whether a zone is currently present in the window, not how many total times it appears beyond one.

3. Start with two pointers defining a window `[left, right]` and expand `right` across `zones`. As each zone enters the window, update a frequency map only if that zone is required.

4. Track how many distinct required zones are currently satisfied. Once all required zones are covered, the window is valid.

5. Now shrink from the left as aggressively as possible while keeping the window valid. This is the critical optimization: every time the window is valid, the best answer for that `right` endpoint is the smallest valid left boundary.

6. Repeat until `right` reaches the end. Each pointer only moves forward, so the scan stays linear.

This works because the problem asks for a minimum over contiguous ranges under a monotonic condition: adding elements can make a window valid, and removing elements can make it invalid.

## 🧩 Algorithm Walkthrough
1. **Build a required set / map.**  
   Create a hash-based structure for all strings in `requiredZones`. This gives O(1)-average membership checks and lets us ignore irrelevant zones. The invariant is: only required zones affect validity.

2. **Initialize the sliding window.**  
   Use the **Two Pointers / Sliding Window** pattern with `left = 0`. Maintain a frequency map `windowCount` for required zones inside the current window and an integer `formed` representing how many distinct required zones currently have count `>= 1`.

3. **Expand the right pointer.**  
   For each `right` from `0` to `zones.length - 1`, if `zones[right]` is required, increment its count. If its count transitions from `0` to `1`, increment `formed`. The invariant becomes: `formed` exactly equals the number of required zone types present in the window.

4. **Check validity.**  
   When `formed == requiredZones.length`, the current window covers all required zones. This condition is correct because required zones are distinct and each must appear at least once.

5. **Shrink from the left.**  
   While the window remains valid, update the best answer with `right - left + 1`, then move `left` forward. If `zones[left]` is required, decrement its count; if the count drops from `1` to `0`, decrement `formed` because the window no longer covers that zone.

6. **Finish and return.**  
   If no valid window was ever found, return `-1`; otherwise return the minimum recorded length.

Why this abstraction fits: the window validity changes incrementally as boundaries move. That makes repeated recomputation unnecessary and gives linear-time behavior.

## 📊 Worked Example
Example: `zones = ["Inbound","A1","Cold","B2","Fragile","Cold","A1","Packing"]`, `requiredZones = ["A1","Cold","Fragile"]`

| right | zone      | left | window counts (required only)      | formed | valid? | best |
|------:|-----------|-----:|------------------------------------|-------:|:------:|-----:|
| 0     | Inbound   | 0    | `{}`                               | 0      | No     | inf  |
| 1     | A1        | 0    | `{A1:1}`                           | 1      | No     | inf  |
| 2     | Cold      | 0    | `{A1:1, Cold:1}`                   | 2      | No     | inf  |
| 3     | B2        | 0    | `{A1:1, Cold:1}`                   | 2      | No     | inf  |
| 4     | Fragile   | 0    | `{A1:1, Cold:1, Fragile:1}`        | 3      | Yes    | 5    |

Now shrink:

- Remove `Inbound` → still valid, best = `4`
- Remove `A1` → window loses `A1`, invalid

Continue expanding:

- `right = 5`, add `Cold` → still missing `A1`
- `right = 6`, add `A1` → valid again
- Shrink from `left = 2`: remove `Cold` (still valid because another `Cold` exists), best becomes `3`
- Next removal would drop `Fragile`, so stop

Answer: `3`.

## ⏱ Complexity Analysis
### Time Complexity
`O(n + m)`, where `n = zones.length` and `m = requiredZones.length`. Building the required set costs `O(m)`, and each pointer moves across `zones` at most once, so the dominant work is linear. This remains practical at `10^6` elements; at `10^9`, even linear scans become throughput-bound and require streaming or distributed processing.

### Space Complexity
`O(m)` for the required set and the window frequency map, since only required zones are tracked. This is already near-minimal for exact coverage checking. You could compress strings to integer IDs, reducing constant factors but not asymptotic space.

## 💡 Key Takeaways
- If the problem asks for a **shortest contiguous segment** satisfying a coverage condition, think sliding window before considering anything more complex.
- If validity depends on a dynamic subset being fully represented, a hash map plus a “formed vs required” counter is usually the right state model.
- Be careful to update the answer **before** invalidating the window while shrinking; reversing that order misses optimal windows.
- Only decrement the satisfied counter when a required zone’s count drops from `1` to `0`; decrementing on every removal is a common bug.
- At system scale, this pattern is valuable because it converts repeated range validation into incremental state maintenance with predictable linear cost.

## 🚀 Variations & Further Practice
- Find the shortest segment containing all required zones with **minimum frequency thresholds** per zone, not just presence; the twist is matching a multiset instead of a set.
- Return the **actual segment indices** and break ties by earliest start or lexicographic zone sequence; the twist is adding deterministic tie-handling without changing asymptotic cost.
- Process an **unbounded event stream** and continuously emit the best valid window seen so far; the twist is online operation, memory discipline, and checkpointable state.