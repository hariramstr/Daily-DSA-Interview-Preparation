import java.util.*;

/*
Problem Title: Minimum Cooldown for Battery Cell Assembly

Problem Description:
You are given an array stations where stations[i] is the number of battery cells that must be processed at assembly station i, in order from left to right. A single robot arm starts at station 0 and must process all cells at every station in order. Processing one cell takes 1 second. Moving from station i to station i + 1 takes 1 second. After the robot has processed x consecutive cells without resting, its motor temperature becomes x. To avoid overheating, the robot is required to rest before processing the next cell whenever its temperature would exceed a chosen cooldown limit C. A rest resets the consecutive processed-cell count back to 0 and takes exactly 1 second. The robot may rest at any time, including between two cells at the same station or immediately after moving.

Given a total time budget T, find the minimum integer cooldown limit C such that the robot can finish processing all stations within at most T seconds.

Your task is to return the smallest feasible C. If the work cannot be completed even with arbitrarily large cooldown (that is, just processing plus movement already exceeds T), return -1.

Constraints:
- 1 <= stations.length <= 2 * 10^5
- 1 <= stations[i] <= 10^9
- 1 <= T <= 10^18
- Answer fits in 64-bit signed integer

Example 1:
Input: stations = [3, 2, 4], T = 12
Output: 2

Example 2:
Input: stations = [5, 1, 5], T = 15
Output: 3

Core idea:
- Total time = processing time + movement time + number of rests.
- Processing time is fixed: sum(stations).
- Movement time is fixed: stations.length - 1.
- Therefore, for a chosen cooldown C, we only need to minimize the number of rests.
- Feasibility is monotonic:
  if cooldown C works, then any larger cooldown also works.
- So we can binary search the minimum feasible C.

Important modeling detail:
- The consecutive processed-cell count carries across station boundaries.
- Moving does NOT reset the count.
- Resting resets the count to 0.
- To minimize rests, at every station we only need to know the best possible ending count
  among schedules that use the minimum number of rests so far.

State compression:
For a fixed C, after processing some prefix of stations, among all schedules with the minimum
possible number of rests, it is always optimal to keep the smallest possible positive ending
count (or 0 only before any processing, which does not occur after the first station because
station sizes are positive). A smaller ending count is never worse for future stations because
it leaves more room before the next forced rest.

Transition for one station of size a, given current ending count s (1 <= s <= C):
- If s + a <= C:
    no rest needed inside this station, new ending count = s + a
- Else:
    we must split the station into blocks of size at most C.
    There are two possibilities:
    1) continue current block with up to (C - s) cells, then rest, then finish remaining cells
    2) rest immediately before this station, then process the whole station from fresh
    We choose the option with fewer rests; if tied, choose the smaller ending count.

A key simplification:
Let full = a / C and rem = a % C.

Starting fresh on this station:
- rests needed inside/before this station = floor((a - 1) / C) = (a - 1) / C
- ending count = rem == 0 ? C : rem

Continuing from current count s:
- if s + a <= C:
    extra rests = 0, ending = s + a
- else:
    extra rests = a / C
    ending = (s + a) % C; if ending == 0 then ending = C

This formula is correct because once the current partial block overflows, one forced rest occurs,
and then every additional full block of size C before the final partial block contributes another
forced rest exactly as counted by a / C in the overflow case.

Then compare with "rest immediately before station":
- extra rests = 1 + (a - 1) / C
- ending = rem == 0 ? C : rem

We keep the lexicographically best pair:
1) smaller total rests
2) if tied, smaller ending count

This yields an O(n) feasibility check and O(log answer) binary search.
*/
public class Solution {

    /**
     * Finds the minimum integer cooldown limit C such that the robot can finish
     * all processing and movement within time T.
     *
     * Algorithm:
     * 1. Compute the base time = total processing time + total movement time.
     * 2. If base time > T, return -1 immediately because even zero rests are impossible.
     * 3. Binary search the smallest cooldown C in [1, sum(stations)] that is feasible.
     *    - Feasibility means: minimum required rests for this C <= T - base time.
     *
     * @param stations the number of cells to process at each station, in order
     * @param T the total allowed time budget
     * @return the smallest feasible cooldown limit, or -1 if impossible even with no rests
     *
     * Time complexity: O(n log S), where n = stations.length and S = sum(stations)
     * Space complexity: O(1) extra space
     */
    public long minimumCooldown(int[] stations, long T) {
        long totalCells = 0L;

        // Sum all processing work.
        for (int cells : stations) {
            totalCells += cells;
        }

        // Moving between adjacent stations happens exactly (n - 1) times.
        long movementTime = stations.length - 1L;

        // This is the absolute minimum possible time:
        // process every cell + perform every move + no rests at all.
        long baseTime = totalCells + movementTime;

        // If even this minimum exceeds T, no cooldown can help.
        if (baseTime > T) {
            return -1L;
        }

        // We are allowed at most this many rests.
        long maxAllowedRests = T - baseTime;

        // Binary search over cooldown C.
        // Lower bound = 1
        // Upper bound = totalCells
        // Why totalCells is enough:
        // if C >= totalCells, the robot can process everything without any rest.
        long left = 1L;
        long right = totalCells;
        long answer = totalCells;

        while (left <= right) {
            long mid = left + (right - left) / 2;

            if (isFeasible(stations, mid, maxAllowedRests)) {
                answer = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        return answer;
    }

    /**
     * Checks whether a given cooldown limit C allows completion within the rest budget.
     *
     * We compute the minimum number of rests needed when processing stations in order.
     *
     * State maintained while scanning stations:
     * - rests: minimum rests used so far
     * - endCount: among all schedules achieving that minimum rests, the smallest possible
     *             current consecutive processed-cell count after finishing the current prefix
     *
     * Why keeping the smallest endCount is enough:
     * - Future constraints only care about how much room remains before hitting C.
     * - A smaller endCount always leaves at least as much room as a larger one.
     * - Therefore, among equal-rest schedules, the smallest endCount dominates all others.
     *
     * Transition for station size a:
     * Option A: continue from current endCount s
     *   - If s + a <= C:
     *       extra rests = 0
     *       new endCount = s + a
     *   - Else:
     *       extra rests = a / C
     *       new endCount = (s + a) % C; if 0, use C
     *
     * Option B: rest immediately before this station
     *   - extra rests = 1 + (a - 1) / C
     *   - new endCount = a % C; if 0, use C
     *
     * We choose the better option:
     * - fewer total rests
     * - if tied, smaller new endCount
     *
     * Special handling for the first station:
     * - Before any processing, the current consecutive count is 0.
     * - Starting from 0 is equivalent to "fresh" without paying a rest.
     * - So for the first station:
     *     rests = (a - 1) / C
     *     endCount = a % C; if 0, use C
     *
     * @param stations the station workloads
     * @param cooldown the candidate cooldown limit C
     * @param maxAllowedRests the maximum number of rests allowed by the time budget
     * @return true if the minimum required rests is at most maxAllowedRests, otherwise false
     *
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isFeasible(int[] stations, long cooldown, long maxAllowedRests) {
        long rests;
        long endCount;

        // Initialize using the first station.
        long first = stations[0];

        // Starting from zero consecutive processed cells, the minimum rests needed
        // to process 'first' cells in blocks of size at most cooldown is:
        // number of separators between blocks = ceil(first / cooldown) - 1
        // which equals floor((first - 1) / cooldown).
        rests = (first - 1) / cooldown;

        // The ending consecutive count is the size of the last block.
        // If first is a multiple of cooldown, the last block size is exactly cooldown.
        endCount = first % cooldown;
        if (endCount == 0) {
            endCount = cooldown;
        }

        // Early exit if already too many rests.
        if (rests > maxAllowedRests) {
            return false;
        }

        // Process remaining stations one by one.
        for (int i = 1; i < stations.length; i++) {
            long a = stations[i];

            // ------------------------------------------------------------
            // Option A: do NOT rest before this station; continue from endCount.
            // ------------------------------------------------------------
            long continueRests;
            long continueEnd;

            if (endCount + a <= cooldown) {
                // Entire station fits into the current ongoing streak.
                continueRests = rests;
                continueEnd = endCount + a;
            } else {
                // Overflow occurs, so at least one rest is forced somewhere inside/at the boundary.
                // The exact minimum extra rests in this case is a / cooldown.
                continueRests = rests + (a / cooldown);

                // Final ending count is determined by total cells consumed in the ongoing streak.
                continueEnd = (endCount + a) % cooldown;
                if (continueEnd == 0) {
                    continueEnd = cooldown;
                }
            }

            // ------------------------------------------------------------
            // Option B: rest immediately before this station.
            // ------------------------------------------------------------
            // One rest now, then process this station from a fresh streak.
            long freshRests = rests + 1L + (a - 1) / cooldown;
            long freshEnd = a % cooldown;
            if (freshEnd == 0) {
                freshEnd = cooldown;
            }

            // ------------------------------------------------------------
            // Choose the better option.
            // Primary key: fewer rests.
            // Secondary key: smaller ending count.
            // ------------------------------------------------------------
            if (continueRests < freshRests) {
                rests = continueRests;
                endCount = continueEnd;
            } else if (freshRests < continueRests) {
                rests = freshRests;
                endCount = freshEnd;
            } else {
                // Same number of rests: keep the smaller ending count because it is
                // always at least as good for all future stations.
                rests = continueRests;
                endCount = Math.min(continueEnd, freshEnd);
            }

            // If we already exceed the allowed rest budget, no need to continue.
            if (rests > maxAllowedRests) {
                return false;
            }
        }

        return rests <= maxAllowedRests;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * @param args command-line arguments (unused)
     *
     * @return nothing
     *
     * Time complexity: O(n log S) for each demonstration call
     * Space complexity: O(1) extra space
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] stations1 = {3, 2, 4};
        long T1 = 12L;
        System.out.println(solution.minimumCooldown(stations1, T1)); // Expected: 2

        int[] stations2 = {5, 1, 5};
        long T2 = 15L;
        System.out.println(solution.minimumCooldown(stations2, T2)); // Expected: 3

        // Additional quick checks
        int[] stations3 = {1};
        long T3 = 1L;
        System.out.println(solution.minimumCooldown(stations3, T3)); // Expected: 1

        int[] stations4 = {10};
        long T4 = 9L;
        System.out.println(solution.minimumCooldown(stations4, T4)); // Expected: -1
    }
}