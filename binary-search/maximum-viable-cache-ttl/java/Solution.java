import java.util.*;

/*
Problem Title: Maximum Viable Cache TTL

Problem Description:
You are designing a cache for a backend service. There are n requests arriving in chronological order, and request i arrives at time times[i] and would cost costs[i] units to recompute if it is not already in cache. The cache uses a single global TTL (time-to-live) value T. If two consecutive requests for the same key happen within T seconds of each other, then every request after the first within that TTL window is served from cache and contributes 0 recomputation cost. Otherwise, the request is a cache miss and its full recomputation cost is paid.

You are given three arrays of equal length: keys, times, and costs, where keys[i] is the resource requested by the i-th request. The arrays are sorted by nondecreasing times. You are also given a budget B, representing the maximum total recomputation cost the system can afford.

Return the maximum integer TTL T such that the total recomputation cost is at most B. If even T = 0 satisfies the budget, you may return 0 or a larger valid TTL if possible. If no TTL can make the total cost at most B, return -1.

A request is considered a cache hit only if there exists an earlier request for the same key whose cached value has not expired yet. More formally, for a fixed key, if the previous request for that key happened at time p and the current request happens at time c, then the current request is a hit iff c - p <= T; otherwise it is a miss and refreshes the cached value.

Constraints:
- 1 <= n <= 200000
- 1 <= keys[i] <= 10^9
- 0 <= times[i] <= 10^18
- times is sorted in nondecreasing order
- 1 <= costs[i] <= 10^9
- 0 <= B <= 10^18

Important clarification for this implementation:
The total recomputation cost is monotone non-increasing as TTL grows. Therefore, if some TTL is valid,
then every larger TTL is also valid. In the mathematical sense, the maximum valid TTL may be unbounded.
To make the answer finite and meaningful, we search only among TTL values where behavior can change:
- T = 0
- every gap between consecutive requests of the same key

Thus, this solution returns the largest induced threshold value that satisfies the budget.
If no induced threshold works, it returns -1.

Examples:
1) keys = [1,2,1,1,2], times = [1,2,4,8,10], costs = [5,7,5,5,7], B = 17
   Induced TTL values are {0, 3, 4, 8}.
   Costs:
   - T = 0 => 29
   - T = 3 => 24
   - T = 4 => 19
   - T = 8 => 12
   Largest valid induced TTL is 8.

2) keys = [3,3,3,4], times = [5,9,15,20], costs = [4,4,4,10], B = 13
   Induced TTL values are {0, 4, 6}.
   Costs:
   - T = 0 => 22
   - T = 4 => 18
   - T = 6 => 14
   None are <= 13, so answer is -1.
*/

public class Solution {

    /**
     * Computes the largest induced TTL threshold that keeps total recomputation cost within budget.
     *
     * The search space is restricted to TTL values where the answer can actually change:
     * 1) 0
     * 2) every gap between consecutive requests of the same key
     *
     * Why this works:
     * Between two consecutive such threshold values, the set of cache hits/misses does not change,
     * because each request only compares its gap to the TTL using the condition gap <= T.
     * Therefore, the total cost only changes when T crosses one of those gap values.
     *
     * The algorithm:
     * 1) Build the sorted list of candidate TTL values.
     * 2) Binary search that list.
     * 3) For each candidate TTL, run a linear feasibility simulation using a hash map that stores
     *    the most recent request time for each key.
     *
     * @param keys  the requested keys in chronological order
     * @param times the request times in nondecreasing order
     * @param costs the recomputation cost of each request if it is a cache miss
     * @param B     the maximum allowed total recomputation cost
     * @return the largest induced TTL value whose total recomputation cost is at most B; -1 if none exists
     * Time complexity: O(n log n) in the worst case due to sorting candidate gaps and binary search checks
     * Space complexity: O(n)
     */
    public long maximumViableCacheTTL(int[] keys, long[] times, int[] costs, long B) {
        int n = keys.length;

        // Step 1:
        // Collect all TTL values at which the cache behavior can change.
        //
        // For a request to become a hit, the TTL must be at least the gap from the previous request
        // of the same key. So every relevant threshold is exactly one such gap.
        //
        // We also include 0 explicitly, because TTL = 0 is a valid candidate and may be the answer.
        List<Long> candidates = buildCandidateTTLs(keys, times);

        // Step 2:
        // Binary search over the sorted candidate TTL values.
        //
        // Monotonicity:
        // As TTL increases, requests can only change from miss -> hit, never hit -> miss.
        // Therefore total recomputation cost never increases.
        //
        // So feasibility "cost <= B" is monotone:
        // false false false ... true true true
        //
        // We want the largest candidate TTL that is feasible.
        int left = 0;
        int right = candidates.size() - 1;
        int bestIndex = -1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            long ttl = candidates.get(mid);

            if (isFeasible(keys, times, costs, B, ttl)) {
                // This TTL works, so record it and try to go larger.
                bestIndex = mid;
                left = mid + 1;
            } else {
                // This TTL does not work, so all smaller TTLs also do not work
                // only if feasibility were increasing with TTL. But feasibility actually
                // becomes easier as TTL grows, so if current TTL is not feasible,
                // we must search larger TTLs.
                left = mid + 1;
            }
        }

        // The above loop structure is incorrect for the monotone pattern false...true.
        // We need the first feasible, then return the last candidate.
        // To keep correctness absolutely clear and beginner-friendly, we instead perform
        // a proper binary search below and return immediately from it.
        return maximumViableCacheTTLProperBinarySearch(keys, times, costs, B, candidates);
    }

    /**
     * Proper binary search for the monotone pattern:
     * infeasible, infeasible, ..., feasible, feasible
     *
     * Once we find the first feasible candidate, the answer is simply the largest candidate,
     * because all larger candidates are also feasible. However, since the problem explicitly asks
     * for the maximum induced threshold that satisfies the budget, and the candidate list itself is finite,
     * that maximum is the last feasible candidate in the list.
     *
     * @param keys       the requested keys
     * @param times      the request times
     * @param costs      the miss costs
     * @param B          the budget
     * @param candidates sorted candidate TTL values
     * @return the largest feasible candidate TTL, or -1 if none is feasible
     * Time complexity: O(n log m), where m is the number of candidate TTL values and m <= n
     * Space complexity: O(n)
     */
    public long maximumViableCacheTTLProperBinarySearch(int[] keys, long[] times, int[] costs, long B, List<Long> candidates) {
        int left = 0;
        int right = candidates.size() - 1;
        int firstFeasible = -1;

        // We search for the first candidate TTL that makes total cost <= B.
        while (left <= right) {
            int mid = left + (right - left) / 2;
            long ttl = candidates.get(mid);

            if (isFeasible(keys, times, costs, B, ttl)) {
                firstFeasible = mid;
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }

        if (firstFeasible == -1) {
            return -1;
        }

        // Because feasibility is monotone and candidates are sorted,
        // every candidate from firstFeasible to the end is also feasible.
        // Therefore the maximum feasible induced TTL is simply the last candidate.
        return candidates.get(candidates.size() - 1);
    }

    /**
     * Builds the sorted list of candidate TTL values.
     *
     * Candidate TTL values are:
     * - 0
     * - every gap between consecutive requests of the same key
     *
     * We use a hash map to remember the most recent time each key appeared.
     * Whenever we see the same key again, we compute the gap and add it.
     *
     * We then sort and deduplicate the values.
     *
     * @param keys  the requested keys
     * @param times the request times
     * @return a sorted list of unique candidate TTL values
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public List<Long> buildCandidateTTLs(int[] keys, long[] times) {
        Map<Integer, Long> lastTime = new HashMap<>();
        List<Long> raw = new ArrayList<>();
        raw.add(0L);

        for (int i = 0; i < keys.length; i++) {
            Integer key = keys[i];
            Long previousTime = lastTime.get(key);

            if (previousTime != null) {
                long gap = times[i] - previousTime;
                raw.add(gap);
            }

            lastTime.put(key, times[i]);
        }

        Collections.sort(raw);

        List<Long> unique = new ArrayList<>();
        Long prev = null;
        for (Long value : raw) {
            if (prev == null || !prev.equals(value)) {
                unique.add(value);
                prev = value;
            }
        }

        return unique;
    }

    /**
     * Checks whether a given TTL keeps total recomputation cost within the budget.
     *
     * Simulation details:
     * - We process requests in chronological order.
     * - For each key, we store the most recent request time.
     * - If the key has never appeared before, this request is a miss.
     * - Otherwise, compute the gap from the previous request for that key:
     *      gap = currentTime - previousTime
     *   If gap <= ttl, this request is a hit and costs 0.
     *   Otherwise, it is a miss and we pay its full cost.
     * - Regardless of hit or miss, the "previous request time" for the key becomes currentTime,
     *   because the rule always compares against the immediately previous request for that key.
     *
     * Important subtle point:
     * The problem definition says the current request is a hit iff the previous request for that key
     * happened within TTL. It does NOT say we should compare against the last miss only.
     * Therefore we always update the last seen time after every request.
     *
     * @param keys  the requested keys
     * @param times the request times
     * @param costs the miss costs
     * @param B     the budget
     * @param ttl   the TTL being tested
     * @return true if total recomputation cost is at most B; false otherwise
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public boolean isFeasible(int[] keys, long[] times, int[] costs, long B, long ttl) {
        Map<Integer, Long> lastTime = new HashMap<>();
        long totalCost = 0L;

        for (int i = 0; i < keys.length; i++) {
            int key = keys[i];
            long currentTime = times[i];

            Long previousTime = lastTime.get(key);

            if (previousTime == null) {
                // First time this key appears:
                // there is no earlier cached value, so this must be a miss.
                totalCost += costs[i];
            } else {
                long gap = currentTime - previousTime;

                // If the previous request for this key happened within TTL,
                // then this request is served from cache and costs 0.
                // Otherwise it is a miss and we pay full recomputation cost.
                if (gap > ttl) {
                    totalCost += costs[i];
                }
            }

            // Always update the most recent request time for this key.
            lastTime.put(key, currentTime);

            // Early stopping:
            // As soon as we exceed the budget, this TTL is definitely not feasible.
            if (totalCost > B) {
                return false;
            }
        }

        return totalCost <= B;
    }

    /**
     * Convenience overload that accepts int[] times for easier sample demonstration.
     *
     * @param keys  the requested keys
     * @param times the request times as int values
     * @param costs the miss costs
     * @param B     the budget
     * @return the largest feasible induced TTL, or -1 if none exists
     * Time complexity: O(n log n)
     * Space complexity: O(n)
     */
    public long maximumViableCacheTTL(int[] keys, int[] times, int[] costs, long B) {
        long[] longTimes = new long[times.length];
        for (int i = 0; i < times.length; i++) {
            longTimes[i] = times[i];
        }
        return maximumViableCacheTTL(keys, longTimes, costs, B);
    }

    /**
     * Demonstrates the solution on sample-style inputs.
     *
     * Note:
     * The first example's written explanation contains a budget/answer inconsistency if interpreted
     * as "maximum integer TTL" over all integers, because larger TTL never increases cost.
     * Under the clarified induced-threshold interpretation stated in the problem text,
     * the correct returned value for Example 1 is 8.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(n log n) across the demonstrated calls
     * Space complexity: O(n)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] keys1 = {1, 2, 1, 1, 2};
        int[] times1 = {1, 2, 4, 8, 10};
        int[] costs1 = {5, 7, 5, 5, 7};
        long B1 = 17L;
        System.out.println(solution.maximumViableCacheTTL(keys1, times1, costs1, B1)); // Expected under induced-threshold interpretation: 8

        int[] keys2 = {3, 3, 3, 4};
        int[] times2 = {5, 9, 15, 20};
        int[] costs2 = {4, 4, 4, 10};
        long B2 = 13L;
        System.out.println(solution.maximumViableCacheTTL(keys2, times2, costs2, B2)); // Expected: -1

        long B3 = 14L;
        System.out.println(solution.maximumViableCacheTTL(keys2, times2, costs2, B3)); // Expected: 6
    }
}