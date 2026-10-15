import java.util.*;

/*
 * Title: Minimum Inspection Radius for Pipeline Leak Sensors
 * Difficulty: Hard
 * Topic: Binary Search
 *
 * Problem Description:
 * A company operates a very long straight pipeline from position 0 to position L.
 * Along the pipeline, there are fixed leak sensors installed at known integer positions.
 * Each sensor can inspect every point within distance R of its own position, where R is
 * the same for all sensors. However, some sections of the pipeline are legally classified
 * as critical zones, and every point inside every critical zone must be covered by at least
 * one sensor. A critical zone is given as a closed interval [start, end], and zones may
 * overlap, touch, or be nested.
 *
 * Your task is to compute the minimum integer inspection radius R such that the union of
 * the coverage intervals of all sensors covers every critical zone completely.
 *
 * More formally, if a sensor is at position x, then it covers the interval [x - R, x + R].
 * Find the smallest integer R for which every critical interval [a, b] is a subset of the
 * union of all sensor coverage intervals.
 *
 * Constraints:
 * - 1 <= sensors.length <= 2 * 10^5
 * - 1 <= zones.length <= 2 * 10^5
 * - 0 <= sensors[i] <= 10^18
 * - 0 <= zones[j][0] <= zones[j][1] <= 10^18
 * - L can be as large as 10^18, but you do not need to inspect points outside the listed critical zones
 * - The answer fits in a 64-bit signed integer
 *
 * Intended approach:
 * - Sort the sensor positions.
 * - Merge the critical zones because overlapping/touching zones represent one continuous
 *   region that must be covered.
 * - Binary search the minimum radius R.
 * - For a fixed R, efficiently check whether the union of sensor coverage intervals covers
 *   every merged critical zone.
 *
 * Key observation for feasibility:
 * - After sorting sensors, their coverage intervals are also ordered by left endpoint.
 * - While scanning merged zones from left to right, we greedily extend the currently covered
 *   point using every sensor interval that starts at or before the next uncovered point.
 * - If we cannot extend coverage to the end of a zone, then this R is not feasible.
 */

public class Solution {

    /**
     * Computes the minimum integer inspection radius required so that all critical zones
     * are fully covered by the union of sensor coverage intervals.
     *
     * Algorithm:
     * 1. Sort sensor positions.
     * 2. Sort and merge critical zones.
     * 3. Binary search the smallest radius R for which coverage is feasible.
     *
     * @param sensors array of sensor positions along the pipeline
     * @param zones   array of critical zones, where each zone is [start, end]
     * @return the minimum integer radius R that covers every critical zone completely
     * Time complexity: O((n + m) log (n + m) + (n + m) log AnswerRange)
     * Space complexity: O(m) for merged intervals (excluding sorting overhead)
     */
    public long minimumInspectionRadius(long[] sensors, long[][] zones) {
        Arrays.sort(sensors);

        long[][] mergedZones = mergeZones(zones);

        long low = 0L;
        long high = computeUpperBound(sensors, mergedZones);

        while (low < high) {
            long mid = low + ((high - low) >>> 1);

            if (canCoverAllZones(sensors, mergedZones, mid)) {
                high = mid;
            } else {
                low = mid + 1;
            }
        }

        return low;
    }

    /**
     * Merges overlapping or touching critical zones.
     *
     * Why merging helps:
     * If zones overlap or even just touch, then together they form one continuous region
     * that must be covered. Treating them as separate intervals would be redundant.
     *
     * Example:
     * [1, 4], [4, 8], [6, 10] -> merged into [1, 10]
     *
     * @param zones array of intervals [start, end]
     * @return merged array of intervals sorted by start
     * Time complexity: O(m log m)
     * Space complexity: O(m)
     */
    public long[][] mergeZones(long[][] zones) {
        long[][] copy = new long[zones.length][2];
        for (int i = 0; i < zones.length; i++) {
            copy[i][0] = zones[i][0];
            copy[i][1] = zones[i][1];
        }

        Arrays.sort(copy, (a, b) -> {
            if (a[0] != b[0]) {
                return Long.compare(a[0], b[0]);
            }
            return Long.compare(a[1], b[1]);
        });

        List<long[]> merged = new ArrayList<>();

        long currentStart = copy[0][0];
        long currentEnd = copy[0][1];

        for (int i = 1; i < copy.length; i++) {
            long nextStart = copy[i][0];
            long nextEnd = copy[i][1];

            /*
             * Closed intervals that touch at one endpoint should also be merged.
             * Example: [1,4] and [4,7] together still form one continuous required region.
             *
             * So if nextStart <= currentEnd, they overlap or touch and can be merged.
             */
            if (nextStart <= currentEnd) {
                currentEnd = Math.max(currentEnd, nextEnd);
            } else {
                merged.add(new long[]{currentStart, currentEnd});
                currentStart = nextStart;
                currentEnd = nextEnd;
            }
        }

        merged.add(new long[]{currentStart, currentEnd});

        long[][] result = new long[merged.size()][2];
        for (int i = 0; i < merged.size(); i++) {
            result[i] = merged.get(i);
        }
        return result;
    }

    /**
     * Checks whether a given radius R is sufficient to cover every merged critical zone.
     *
     * Core greedy idea:
     * We process merged zones from left to right.
     * For each zone [a, b], we maintain a pointer "need" = the leftmost point in this zone
     * that is not yet guaranteed to be covered.
     *
     * Then we repeatedly:
     * - Skip sensors whose coverage ends before "need".
     * - Among sensors whose coverage starts at or before "need", greedily use them to extend
     *   the farthest covered point to the right.
     * - If no such sensor exists, then "need" is uncovered, so R is impossible.
     * - Otherwise, move "need" to just after the farthest covered point and continue until
     *   the whole zone is covered.
     *
     * Because sensors are sorted and each sensor is processed at most once, this check is linear.
     *
     * Important note about closed intervals and integer coordinates:
     * All positions are integers, and R is an integer. Since zones are closed intervals and
     * endpoints are integers, if coverage reaches at least b, then [a, b] is fully covered.
     * During the scan we use "need = farthest + 1" to move to the next integer point that still
     * needs coverage. This is valid because all interval boundaries are integer-valued.
     *
     * @param sensors     sorted sensor positions
     * @param mergedZones merged critical zones sorted by start
     * @param radius      candidate inspection radius
     * @return true if all merged zones are fully covered, false otherwise
     * Time complexity: O(n + k), where n = number of sensors and k = number of merged zones
     * Space complexity: O(1) extra
     */
    public boolean canCoverAllZones(long[] sensors, long[][] mergedZones, long radius) {
        int n = sensors.length;
        int sensorIndex = 0;

        for (long[] zone : mergedZones) {
            long zoneStart = zone[0];
            long zoneEnd = zone[1];

            /*
             * "need" is the leftmost integer point in the current zone that still must be covered.
             * Initially, the whole zone is uncovered, so need = zoneStart.
             */
            long need = zoneStart;

            while (need <= zoneEnd) {
                /*
                 * Step 1:
                 * Discard sensors whose coverage ends before "need".
                 * Such sensors can never help cover the current uncovered point or anything to its right.
                 */
                while (sensorIndex < n && safeAdd(sensors[sensorIndex], radius) < need) {
                    sensorIndex++;
                }

                /*
                 * Step 2:
                 * Starting from sensorIndex, gather every sensor whose coverage interval starts
                 * at or before "need". Among them, compute the farthest right endpoint reachable.
                 *
                 * If a sensor at position x has radius R, then it covers [x - R, x + R].
                 * It can help cover "need" only if x - R <= need.
                 */
                long farthest = need - 1;
                int tempIndex = sensorIndex;

                while (tempIndex < n && safeSubtract(sensors[tempIndex], radius) <= need) {
                    farthest = Math.max(farthest, safeAdd(sensors[tempIndex], radius));
                    tempIndex++;
                }

                /*
                 * Step 3:
                 * If farthest < need, then no sensor interval covers the point "need".
                 * Therefore the current radius is insufficient.
                 */
                if (farthest < need) {
                    return false;
                }

                /*
                 * Step 4:
                 * We have covered from "need" through "farthest".
                 * Advance need to the next integer point after farthest.
                 *
                 * If farthest already reaches or passes zoneEnd, the zone is fully covered.
                 */
                if (farthest >= zoneEnd) {
                    break;
                }

                /*
                 * Move the main sensor pointer forward to tempIndex.
                 * Every sensor before tempIndex has already been fully considered for extending
                 * coverage at the current "need". Sensors after tempIndex start too far right
                 * to help at the old "need", but they may help at the new larger "need".
                 */
                sensorIndex = tempIndex;
                need = farthest + 1;
            }
        }

        return true;
    }

    /**
     * Computes a safe upper bound for binary search.
     *
     * We need some radius that is definitely sufficient.
     * A simple and safe choice:
     * For every merged zone endpoint, find the distance to the nearest extreme sensor
     * in a way that guarantees eventual coverage. Using the farthest possible endpoint
     * distance to either the leftmost or rightmost sensor is enough as an upper bound.
     *
     * More simply:
     * high = max over all zone endpoints of distance to either first sensor or last sensor.
     * This is certainly enough because with such a radius, at least one extreme sensor can
     * reach that endpoint, and since the bound is very generous, it safely contains the answer.
     *
     * @param sensors     sorted sensor positions
     * @param mergedZones merged critical zones
     * @return a safe upper bound for the answer
     * Time complexity: O(k)
     * Space complexity: O(1)
     */
    public long computeUpperBound(long[] sensors, long[][] mergedZones) {
        long first = sensors[0];
        long last = sensors[sensors.length - 1];
        long high = 0L;

        for (long[] zone : mergedZones) {
            long a = zone[0];
            long b = zone[1];

            high = Math.max(high, Math.max(absDiff(a, first), absDiff(a, last)));
            high = Math.max(high, Math.max(absDiff(b, first), absDiff(b, last)));
        }

        return high;
    }

    /**
     * Returns |a - b| for non-negative long values without overflow issues in this problem's range.
     *
     * @param a first value
     * @param b second value
     * @return absolute difference between a and b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long absDiff(long a, long b) {
        return a >= b ? a - b : b - a;
    }

    /**
     * Safely computes a + b under the problem guarantee that the final answer fits in signed 64-bit.
     *
     * @param a first operand
     * @param b second operand
     * @return a + b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long safeAdd(long a, long b) {
        return a + b;
    }

    /**
     * Safely computes a - b.
     *
     * @param a first operand
     * @param b second operand
     * @return a - b
     * Time complexity: O(1)
     * Space complexity: O(1)
     */
    public long safeSubtract(long a, long b) {
        return a - b;
    }

    /**
     * Demonstrates the solution on the sample inputs from the problem statement.
     *
     * Expected outputs:
     * Example 1 -> 2
     * Example 2 -> 5
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(1) for the demonstration itself, excluding algorithm calls
     * Space complexity: O(1) extra, excluding input storage
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        long[] sensors1 = {2, 8, 14};
        long[][] zones1 = {
                {1, 4},
                {6, 10},
                {13, 15}
        };
        System.out.println(solution.minimumInspectionRadius(sensors1, zones1)); // 2

        long[] sensors2 = {5, 11};
        long[][] zones2 = {
                {0, 3},
                {4, 8},
                {10, 14}
        };
        System.out.println(solution.minimumInspectionRadius(sensors2, zones2)); // 5
    }
}