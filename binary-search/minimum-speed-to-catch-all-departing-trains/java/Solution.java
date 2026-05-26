/*
 * Title: Minimum Speed to Catch All Departing Trains
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A traveler needs to board a sequence of n trains at a station. Each train i departs
 * exactly at minute schedule[i], and the traveler arrives at the station at minute 0.
 * To board train i, the traveler must finish boarding train i-1 and then walk to the
 * next platform, which takes exactly 1 minute per unit of distance dist[i] (the distance
 * to platform i+1). However, the traveler can move at a chosen speed s (a positive integer),
 * meaning crossing distance dist[i] takes ceil(dist[i] / s) minutes.
 *
 * The traveler starts at platform 1 at time 0 and must board every train in order.
 * To board train i, the traveler must arrive at platform i no later than schedule[i] minutes.
 * The traveler always boards the train the moment they arrive (or waits if early).
 *
 * Given arrays schedule and dist of length n, where dist[i] is the distance from platform i
 * to platform i+1 (there are n-1 such distances), find the minimum integer speed s such that
 * the traveler can board all n trains on time. If it is impossible even at very high speed,
 * return -1.
 *
 * Constraints:
 * - 2 <= n <= 10^5
 * - 1 <= schedule[i] <= 10^9 (schedule is strictly increasing)
 * - 1 <= dist[i] <= 10^9, dist.length == n - 1
 * - The answer speed, if it exists, will not exceed 10^7
 *
 * Example 1:
 * Input: schedule = [2, 5, 9], dist = [3, 4]
 * Output: 2
 *
 * Example 2:
 * Input: schedule = [1, 3, 5], dist = [5, 5]
 * Output: 3
 */

import java.util.*;

/**
 * Solution class for the "Minimum Speed to Catch All Departing Trains" problem.
 *
 * <p>Key Insight: We use binary search on the answer (speed). For a given speed,
 * we can check in O(n) time whether the traveler can catch all trains. The feasibility
 * function is monotone: if speed s works, then any speed > s also works. So we binary
 * search for the minimum valid speed.</p>
 */
public class Solution {

    /**
     * Checks whether a given speed allows the traveler to catch all trains on time.
     *
     * <p>Algorithm:
     * - Start at platform 1 at time 0.
     * - For each segment i (from platform i to platform i+1), compute travel time = ceil(dist[i] / speed).
     * - The traveler departs platform i at time max(currentTime, schedule[i]).
     * - After traveling, currentTime = departureTime + travelTime.
     * - The traveler must arrive at platform i+1 no later than schedule[i+1].
     * - The last train (index n-1) just needs to be reached on time; no travel after it.
     * </p>
     *
     * @param schedule array of departure times for each train
     * @param dist     array of distances between consecutive platforms (length = n-1)
     * @param speed    the candidate speed to test
     * @return true if the traveler can catch all trains at this speed, false otherwise
     * @implNote Time complexity: O(n). Space complexity: O(1).
     */
    private boolean canCatchAll(int[] schedule, int[] dist, int speed) {
        int n = schedule.length;

        // currentTime tracks when the traveler is ready to depart from the current platform
        // Initially, the traveler is at platform 1 (index 0) at time 0
        // They board train 0 at time schedule[0] (or earlier if they arrive before schedule[0])
        // Since they start at time 0 and schedule[0] >= 0, they always catch train 0.
        // After boarding train 0, they depart at time schedule[0].
        double currentTime = 0.0;

        // Iterate over each segment: from platform i to platform i+1
        // There are n-1 segments (dist has n-1 elements)
        for (int i = 0; i < n - 1; i++) {
            // Step 1: The traveler boards train i.
            // They must wait until schedule[i] if they arrive early.
            // So the departure time from platform i is max(currentTime, schedule[i]).
            // Since schedule is strictly increasing and we check feasibility,
            // we use double arithmetic to avoid overflow.
            double departureTime = Math.max(currentTime, (double) schedule[i]);

            // Step 2: Compute travel time for segment i.
            // Travel time = ceil(dist[i] / speed)
            // Using integer ceiling formula: ceil(a/b) = (a + b - 1) / b for positive integers
            // But we use Math.ceil with doubles to handle large values safely
            double travelTime = Math.ceil((double) dist[i] / speed);

            // Step 3: Update currentTime to when the traveler arrives at platform i+1
            currentTime = departureTime + travelTime;

            // Step 4: Check if the traveler arrives at platform i+1 in time for train i+1
            // They must arrive no later than schedule[i+1]
            if (currentTime > schedule[i + 1]) {
                // Arrived too late — this speed is insufficient
                return false;
            }
        }

        // If we've successfully passed all segment checks, the traveler catches all trains
        return true;
    }

    /**
     * Finds the minimum integer speed required for the traveler to catch all trains.
     *
     * <p>Binary Search Strategy:
     * - Lower bound: speed = 1 (minimum possible speed)
     * - Upper bound: speed = 10^7 (given by constraints)
     * - For each midpoint speed, check feasibility using canCatchAll().
     * - If feasible, try a smaller speed (move right boundary left).
     * - If not feasible, try a larger speed (move left boundary right).
     * - If even speed = 10^7 doesn't work, return -1.
     * </p>
     *
     * <p>Impossibility Check:
     * It's impossible if consecutive trains have schedule[i+1] - schedule[i] <= 0,
     * but since schedule is strictly increasing, the only impossible case is when
     * even at maximum speed the traveler cannot make it. This happens when
     * schedule[i+1] - schedule[i] = 1 for some i < n-2 (not the last segment),
     * because ceil(dist[i]/inf) = 1 minute minimum, and departing at schedule[i]
     * means arriving at schedule[i] + 1 = schedule[i+1], which is exactly on time.
     * But if schedule[i+1] <= schedule[i] (impossible by constraint), or if
     * the gap is 1 and we need to travel (always takes at least 1 minute), it might fail.
     * We handle this by simply checking if speed = MAX_SPEED works.
     * </p>
     *
     * @param schedule array of departure times for each train (strictly increasing)
     * @param dist     array of distances between consecutive platforms (length = n-1)
     * @return minimum integer speed to catch all trains, or -1 if impossible
     * @implNote Time complexity: O(n * log(MAX_SPEED)) = O(n * 24) ≈ O(n).
     *           Space complexity: O(1).
     */
    public int minSpeedToCatchAll(int[] schedule, int[] dist) {
        // Define the search space for speed
        int lo = 1;           // Minimum possible speed
        int hi = 10_000_000;  // Maximum possible speed per constraints

        // First, check if it's even possible at maximum speed.
        // If not, return -1 immediately.
        if (!canCatchAll(schedule, dist, hi)) {
            return -1;
        }

        // Binary search for the minimum valid speed
        // Invariant: canCatchAll(schedule, dist, hi) is always true
        //            canCatchAll(schedule, dist, lo-1) is always false (or lo=1)
        while (lo < hi) {
            // Compute midpoint, avoiding integer overflow
            int mid = lo + (hi - lo) / 2;

            // Check if speed 'mid' is sufficient to catch all trains
            if (canCatchAll(schedule, dist, mid)) {
                // Speed 'mid' works! Try to find a smaller valid speed.
                // Move the upper bound down to 'mid'.
                hi = mid;
            } else {
                // Speed 'mid' is not enough. We need a higher speed.
                // Move the lower bound up to 'mid + 1'.
                lo = mid + 1;
            }
        }

        // At this point, lo == hi, and this is the minimum valid speed
        return lo;
    }

    /**
     * Main method to demonstrate the solution with sample inputs.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // -----------------------------------------------------------------------
        // Example 1:
        // schedule = [2, 5, 9], dist = [3, 4]
        // Expected Output: 2
        //
        // Trace at speed 2:
        // - Start at platform 1 at time 0.
        // - Board train 0 at time max(0, schedule[0]) = max(0, 2) = 2.
        //   Wait: currentTime=0, departureTime=max(0,2)=2
        //   Travel dist[0]=3 at speed 2: ceil(3/2)=2 minutes
        //   Arrive at platform 2 at time 2+2=4
        //   Check: 4 <= schedule[1]=5 ✓
        // - Board train 1 at time max(4, 5) = 5.
        //   Travel dist[1]=4 at speed 2: ceil(4/2)=2 minutes
        //   Arrive at platform 3 at time 5+2=7
        //   Check: 7 <= schedule[2]=9 ✓
        // All trains caught! Speed 2 works.
        //
        // Trace at speed 1:
        // - Start at platform 1 at time 0.
        //   departureTime=max(0,2)=2
        //   Travel dist[0]=3 at speed 1: ceil(3/1)=3 minutes
        //   Arrive at platform 2 at time 2+3=5
        //   Check: 5 <= schedule[1]=5 ✓
        // - Board train 1 at time max(5,5)=5.
        //   Travel dist[1]=4 at speed 1: ceil(4/1)=4 minutes
        //   Arrive at platform 3 at time 5+4=9
        //   Check: 9 <= schedule[2]=9 ✓
        // Hmm, speed 1 also works? Let me re-check...
        // Actually speed 1 works too! But wait, let me re-read the problem.
        // The problem says dist[i] is distance from platform i to platform i+1.
        // dist = [3, 4] means dist[0]=3 (platform 1 to 2), dist[1]=4 (platform 2 to 3).
        // At speed 1: depart platform 1 at schedule[0]=2, travel 3 min, arrive at 5 = schedule[1]. OK.
        //             depart platform 2 at schedule[1]=5, travel 4 min, arrive at 9 = schedule[2]. OK.
        // So speed 1 works! But expected output is 2...
        // Let me re-read the problem more carefully.
        //
        // Oh wait - re-reading: "the traveler must arrive at platform i no later than schedule[i]"
        // So the traveler must arrive at platform i (to board train i) by schedule[i].
        // The traveler is at platform 1 at time 0. Train 0 departs at schedule[0]=2.
        // They board train 0 at time 0 (they're already there, and schedule[0]=2 >= 0).
        // After boarding train 0, they travel to platform 2. They depart at time schedule[0]=2.
        // Wait, do they depart at time 0 or schedule[0]?
        //
        // Re-reading the explanation: "traveling dist[0]=3 takes ceil(3/2)=2 min, arriving at
        // platform 2 at time 2 (on time for schedule[1]=5)"
        // So at speed 2: arrive at platform 2 at time 2. But they departed at time 0!
        // ceil(3/2) = 2, so 0 + 2 = 2. They departed at time 0!
        //
        // So the traveler departs immediately (at time 0) from platform 1, not waiting for schedule[0].
        // They board train 0 at time 0 (since schedule[0]=2 >= 0, they're there on time).
        // But they don't wait for train 0 to depart before moving to platform 2?
        //
        // Hmm, let me re-read: "To board train i, the traveler must finish boarding train i-1
        // and then walk to the next platform"
        // So they must FINISH boarding train i-1 first. Boarding train i-1 happens at schedule[i-1].
        // So they depart for platform i+1 at time schedule[i] (after boarding train i).
        //
        // But the example says: at speed 2, arriving at platform 2 at time 2.
        // If they depart at schedule[0]=2 and travel ceil(3/2)=2 min, they arrive at 4, not 2.
        //
        // Let me re-read the example explanation again:
        // "At speed 2, traveling dist[0]=3 takes ceil(3/2)=2 min, arriving at platform 2 at time 2"
        // This means they departed at time 0 and arrived at time 2.
        // So they depart from platform 1 at time 0 (immediately), not waiting for train 0.
        //
        // But then: "Then traveling dist[1]=4 takes ceil(4/2)=2 min, arriving at platform 3
        // at time max(2,5)+2=7"
        // They arrive at platform 2 at time 2, but train 1 departs at schedule[1]=5.
        // They wait until 5, then travel 2 min, arriving at 7. Check: 7 <= 9. OK.
        //
        // So the model is:
        // - Traveler starts at platform 1 at time 0.
        // - They immediately start traveling to platform 2 (no need to wait for train 0).
        // - Wait, but they need to BOARD train 0 first!
        //
        // Actually, re-reading more carefully: the traveler is AT platform 1 at time 0.
        // Train 0 departs at schedule[0]. The traveler boards train 0 (they're already there).
        // Then they need to get to platform 2 for train 1.
        // But when do they start traveling? After boarding train 0, which is at time schedule[0]?
        // Or do they travel before train 0 departs?
        //
        // The example says they arrive at platform 2 at time 2 (with speed 2, dist=3).
        // ceil(3/2)=2. So they departed at time 0. This means they start traveling immediately
        // without waiting for train 0 to depart.
        //
        // But that contradicts "must finish boarding train i-1 and then walk to the next platform".
        //
        // I think the key insight is: the traveler boards train 0 at time 0 (