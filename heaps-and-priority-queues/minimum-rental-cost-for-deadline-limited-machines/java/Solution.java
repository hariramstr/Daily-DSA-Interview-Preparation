import java.util.*;

/*
Problem Title: Minimum Rental Cost for Deadline-Limited Machines

Problem Description:
A factory must complete n production jobs. Job i becomes available on day start[i], must be
finished no later than day end[i], and requires exactly one machine for one full day.
You may rent any number of identical machines. Renting one machine costs cost[j] for day j,
and a rented machine can process at most one job on that day. A single job may be scheduled
on any integer day d such that start[i] <= d <= end[i].

Your task is to compute the minimum total rental cost needed to complete all jobs, or return -1
if it is impossible.

You are not assigning jobs to specific machine identities ahead of time. Instead, think of
choosing how many machine slots to rent on each day, then placing each job into one feasible
slot within its allowed interval. Multiple jobs may share the same machine across different days,
but on the same day each rented machine handles only one job.

This is a hard scheduling problem because a cheap day should be used only when doing so does not
block more urgent jobs. Efficient solutions typically process days in order and use a priority
queue to decide which currently available jobs must be scheduled before they expire.

Important clarification:
Jobs may only be scheduled on days that appear in the rentalDays list.

Core idea of the solution:
This problem can be modeled as a minimum-cost feasible assignment of jobs to allowed days with
unbounded capacity per day but unit cost per assigned job on that day.

A standard and correct greedy way to solve this is:
1. Compress all usable rental days in sorted order.
2. Convert every job interval [start, end] into the index range of rental days that lie inside it.
   If a job contains no rental day, the answer is impossible.
3. Process jobs in order of increasing left endpoint.
4. Maintain a min-heap of "active jobs" ordered by right endpoint.
5. Sweep rental days from left to right. At each day:
   - add all jobs whose left endpoint is now available,
   - remove jobs that already expired before this day -> impossible,
   - decide how many jobs to assign to this day.
6. The crucial optimization is:
   among all active jobs, assigning one more job to the current day costs price[day].
   Delaying that assignment to a future day can only use days to the right.
   Therefore, when we compare all days globally, the optimal structure is equivalent to selecting
   for each job one day in its interval so that the total chosen day prices are minimized.
   This can be solved by processing days in increasing price and using a disjoint-set structure
   over jobs sorted by deadline. For each day, greedily assign that day to as many still-unassigned
   compatible jobs as possible, always taking the compatible jobs with earliest deadlines first.

Equivalent correct formulation used below:
- Sort jobs by deadline index.
- For each rental day in increasing cost order, assign this day to every still-unassigned job
  whose interval contains this day, prioritizing earliest deadlines.
- To do this efficiently, we use:
  * jobs grouped by left endpoint,
  * a priority queue by right endpoint for jobs that can use the current day,
  * and process days in increasing cost, not chronological order.

Why this is correct:
This is the classic greedy for minimum-cost points covering intervals with multiplicity 1:
when considering a day of some cost, any job assigned here pays exactly this cost.
Taking the currently cheapest available day for the jobs that can use it is always safe,
provided we assign it first to the most urgent compatible jobs (smallest right endpoint),
because those jobs have the fewest future options.

Since days are processed by nondecreasing cost, once we skip assigning a compatible urgent job
to the current day, any later assignment for that job cannot be cheaper.

Implementation details:
Because compatibility is interval-based over sorted rental-day indices, we:
- map each job to [L, R] over day indices,
- sort day indices by price ascending,
- activate jobs whose L <= currentDayIndex using a Fenwick-tree-assisted offline sweep per price order
  is awkward, so instead we use a segment tree over day indices where each node stores jobs by L.
  A simpler and efficient approach is min-cost max-flow on interval graph, but that is too heavy.

A much cleaner optimal solution is:
Min-cost assignment on a line with interval constraints can be solved by dynamic greedy using a
priority queue while scanning days in chronological order after first sorting days by price is not enough.

Therefore we use the correct primal-dual equivalent:
Build a min-cost max-flow network on compressed days with consecutive-day edges.
Because m,n <= 200000, a naive graph is too large if we connect each job to every day in its interval.
But interval edges can be represented compactly using a segment tree:
- source -> each job (capacity 1, cost 0)
- each job -> segment tree nodes covering its allowed day-index interval (capacity 1, cost 0)
- segment tree leaves(day) -> sink with infinite capacity and cost = price[day]
This computes exactly the minimum total cost assignment.
To keep the graph sparse:
- each job connects to O(log m) segment tree nodes
- segment tree has O(m) nodes
Total edges O((n + m) log m), feasible.

We implement successive shortest augmenting path with potentials using Dijkstra.
Because every augment sends 1 unit and n can be 200000, that would be too slow.

So we exploit the special structure:
All costs are only on leaf->sink edges, and capacities there are infinite.
This means each augmenting path cost is just chosen day price.
Still, generic MCMF remains too slow.

Final efficient and correct approach:
Use a segment tree where each leaf day has a multiset capacity "infinite" with fixed cost.
Repeatedly assigning cheapest feasible day to jobs can be done by sorting jobs by right endpoint and
querying the minimum-cost available day in [L, R]. Since day capacity is unlimited, "available" never
decreases. Therefore every job independently chooses the cheapest day in its interval.
There is no coupling between jobs because any number of machines may be rented on the same day.
This is the key observation.

So the problem is much simpler than it first appears:
- Renting k machines on a day costs k * price[day].
- There is no upper bound on k.
- Therefore jobs do not compete for day slots except through cost, but since capacity is unlimited,
  each job can independently choose the cheapest rental day in its allowed interval.
- The total minimum cost is simply the sum, over all jobs, of the minimum rental price among listed
  rental days within [start[i], end[i]].
- If a job has no listed rental day in its interval, answer is -1.

This matches the corrected examples:
Example 1:
[1,3] -> min cost among days 1,2,3 = 2
[2,2] -> day 2 = 2
[2,4] -> min among 2,3,4 = 2
Total = 6
The narrative in the prompt contradicts the stated "any number of machines" rule.
Under the formal rules, 6 is correct.
Example 2:
[1,2] -> 3
[1,2] -> 3
[2,3] -> 1
[3,3] -> 1
Total = 8

Because the prompt explicitly says "You may rent any number of identical machines", unlimited same-day
capacity is allowed by paying multiple times the day cost. Hence the independent-choice solution is the
correct one.

We implement:
- sort rentalDays by day
- build segment tree for range minimum query on prices
- for each job, binary search the first rental day >= start and last rental day <= end
- if none exists, return -1
- otherwise add the minimum price in that index range
*/

public class Solution {

    /**
     * Computes the minimum total rental cost needed to complete all jobs.
     *
     * Each job can be processed on any listed rental day within its interval.
     * Because the factory may rent any number of machines on the same day, there is no effective
     * capacity limit per day: assigning one more job to a day simply adds one more copy of that day's
     * rental price. Therefore, each job can be optimized independently by choosing the cheapest listed
     * rental day inside its allowed interval.
     *
     * @param jobs       an array where jobs[i] = [start, end] describes the availability window of job i
     * @param rentalDays an array where rentalDays[j] = [day, price] gives the machine rental price on that day
     * @return the minimum total rental cost, or -1 if at least one job cannot be scheduled on any listed rental day
     *
     * Time complexity: O((n + m) log m)
     * Space complexity: O(m)
     */
    public long minimumRentalCost(int[][] jobs, int[][] rentalDays) {
        if (jobs == null || rentalDays == null || rentalDays.length == 0) {
            return jobs == null || jobs.length == 0 ? 0L : -1L;
        }

        // Step 1:
        // Sort rental days by actual calendar day.
        // This lets us binary-search which listed rental days fall inside a job interval.
        Arrays.sort(rentalDays, Comparator.comparingInt(a -> a[0]));

        int m = rentalDays.length;
        int[] days = new int[m];
        long[] prices = new long[m];

        for (int i = 0; i < m; i++) {
            days[i] = rentalDays[i][0];
            prices[i] = rentalDays[i][1];
        }

        // Step 2:
        // Build a segment tree over the sorted rental-day prices.
        // The tree supports:
        //   queryMin(leftIndex, rightIndex) = cheapest rental price among listed days in that index range.
        SegmentTree segTree = new SegmentTree(prices);

        long total = 0L;

        // Step 3:
        // Process each job independently.
        // Since unlimited machines may be rented on the same day, jobs do not block each other.
        // For each job:
        //   - find the first listed rental day >= start
        //   - find the last listed rental day <= end
        //   - if no such day exists, impossible
        //   - otherwise add the minimum price in that subarray
        for (int[] job : jobs) {
            int start = job[0];
            int end = job[1];

            int left = lowerBound(days, start);
            int right = upperBound(days, end) - 1;

            // If the computed index range is empty, then there is no listed rental day
            // that lies inside [start, end], so this job cannot be completed.
            if (left > right) {
                return -1L;
            }

            long bestPrice = segTree.queryMin(left, right);
            total += bestPrice;
        }

        return total;
    }

    /**
     * Returns the first index i such that arr[i] >= target.
     *
     * @param arr    sorted integer array
     * @param target target value
     * @return first index with value >= target, or arr.length if no such index exists
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int lowerBound(int[] arr, int target) {
        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + ((right - left) >>> 1);
            if (arr[mid] >= target) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Returns the first index i such that arr[i] > target.
     *
     * @param arr    sorted integer array
     * @param target target value
     * @return first index with value > target, or arr.length if no such index exists
     *
     * Time complexity: O(log n)
     * Space complexity: O(1)
     */
    public int upperBound(int[] arr, int target) {
        int left = 0;
        int right = arr.length;

        while (left < right) {
            int mid = left + ((right - left) >>> 1);
            if (arr[mid] > target) {
                right = mid;
            } else {
                left = mid + 1;
            }
        }

        return left;
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The textual examples in the prompt contain contradictions.
     * Under the formal rule "You may rent any number of identical machines",
     * multiple jobs may be processed on the same day by renting multiple machines,
     * each costing that day's price.
     *
     * Therefore the mathematically correct outputs are:
     * Example 1 -> 6
     * Example 2 -> 8
     *
     * @param args command-line arguments (unused)
     *
     * Time complexity: O((n + m) log m) for each demonstration call
     * Space complexity: O(m)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[][] jobs1 = {
                {1, 3},
                {2, 2},
                {2, 4}
        };
        int[][] rentalDays1 = {
                {1, 5},
                {2, 2},
                {3, 4},
                {4, 7}
        };

        int[][] jobs2 = {
                {1, 2},
                {1, 2},
                {2, 3},
                {3, 3}
        };
        int[][] rentalDays2 = {
                {1, 8},
                {2, 3},
                {3, 1}
        };

        System.out.println(solution.minimumRentalCost(jobs1, rentalDays1)); // Correct under stated rules: 6
        System.out.println(solution.minimumRentalCost(jobs2, rentalDays2)); // 8

        int[][] jobs3 = {
                {5, 5},
                {1, 10}
        };
        int[][] rentalDays3 = {
                {1, 100},
                {3, 50},
                {10, 1}
        };

        System.out.println(solution.minimumRentalCost(jobs3, rentalDays3)); // -1 because job [5,5] has no listed rental day
    }

    /**
     * Segment tree for range minimum queries on long values.
     */
    static class SegmentTree {
        private final long[] tree;
        private final int n;

        /**
         * Builds a segment tree from the given array.
         *
         * @param values the base array of prices
         *
         * Time complexity: O(n)
         * Space complexity: O(n)
         */
        SegmentTree(long[] values) {
            this.n = values.length;
            this.tree = new long[n * 4];
            build(1, 0, n - 1, values);
        }

        /**
         * Queries the minimum value in the inclusive range [left, right].
         *
         * @param left  left index, inclusive
         * @param right right index, inclusive
         * @return minimum value in that range
         *
         * Time complexity: O(log n)
         * Space complexity: O(log n) due to recursion
         */
        public long queryMin(int left, int right) {
            return queryMin(1, 0, n - 1, left, right);
        }

        /**
         * Recursively builds the segment tree.
         *
         * @param node   current tree node index
         * @param start  segment start
         * @param end    segment end
         * @param values source array
         *
         * Time complexity: O(n)
         * Space complexity: O(log n) due to recursion
         */
        private void build(int node, int start, int end, long[] values) {
            if (start == end) {
                tree[node] = values[start];
                return;
            }

            int mid = start + ((end - start) >>> 1);
            build(node << 1, start, mid, values);
            build(node << 1 | 1, mid + 1, end, values);
            tree[node] = Math.min(tree[node << 1], tree[node << 1 | 1]);
        }

        /**
         * Recursively queries the minimum in an interval.
         *
         * @param node      current tree node index
         * @param start     current segment start
         * @param end       current segment end
         * @param left      query left
         * @param right     query right
         * @return minimum value in the requested range
         *
         * Time complexity: O(log n)
         * Space complexity: O(log n) due to recursion
         */
        private long queryMin(int node, int start, int end, int left, int right) {
            // Completely outside the query interval.
            if (end < left || start > right) {
                return Long.MAX_VALUE;
            }

            // Completely inside the query interval.
            if (left <= start && end <= right) {
                return tree[node];
            }

            // Partial overlap: split and combine.
            int mid = start + ((end - start) >>> 1);
            long minLeft = queryMin(node << 1, start, mid, left, right);
            long minRight = queryMin(node << 1 | 1, mid + 1, end, left, right);
            return Math.min(minLeft, minRight);
        }
    }
}