/*
 * Title: Minimum Speed to Catch All Departing Trains
 * Difficulty: Medium
 * Topic: Binary Search
 *
 * Problem Description:
 * A traveler needs to board a sequence of n trains at a station. Each train i departs
 * exactly at minute schedule[i], and the traveler arrives at the station at minute 0.
 * To board train i, the traveler must finish boarding train i-1 and then walk to the
 * next platform, which takes exactly ceil(dist[i] / s) minutes at speed s.
 *
 * The traveler starts at platform 1 at time 0 and must board every train in order.
 * To board train i, the traveler must arrive at platform i no later than schedule[i] minutes.
 *
 * Given arrays schedule and dist of length n, where dist[i] is the distance from
 * platform i to platform i+1 (there are n-1 such distances), find the minimum integer
 * speed s such that the traveler can board all n trains on time.
 * If it is impossible even at very high speed, return -1.
 *
 * Constraints:
 * - 2 <= n <= 10^5
 * - 1 <= schedule[i] <= 10^9 (schedule is strictly increasing)
 * - 1 <= dist[i] <= 10^9, dist.length == n-1
 * - The answer speed, if it exists, will not exceed 10^7
 */

using System;

public class Solution
{
    // Time Complexity:  O(n * log(maxSpeed)) where maxSpeed = 10^7
    //                   Binary search runs log(10^7) ≈ 23 iterations,
    //                   each iteration does O(n) work to simulate the journey.
    // Space Complexity: O(1) — we only use a constant amount of extra variables
    //                   (no additional data structures proportional to input size).

    /// <summary>
    /// Finds the minimum integer speed s so the traveler can catch all trains on time.
    /// Returns -1 if it is impossible.
    /// </summary>
    public int MinSpeedOnTime(int[] schedule, int[] dist)
    {
        // -----------------------------------------------------------------------
        // STEP 1: Quick impossibility check
        // -----------------------------------------------------------------------
        // The traveler boards train 0 at time 0 (they are already at platform 1).
        // After boarding train i (i < n-1), they must travel dist[i] to reach
        // platform i+1. The earliest they can DEPART platform i is schedule[i]
        // (they wait for the train, then the train departs).
        //
        // For the LAST segment (i = n-2), the traveler departs platform n-2 at
        // time schedule[n-2] and must arrive at platform n-1 by schedule[n-1].
        // Even at infinite speed (travel time = 1 minute minimum because ceil(x/∞)
        // approaches 0 but we need at least 1 minute for any positive distance),
        // wait — actually at infinite speed ceil(dist/s) → 0, but since s must be
        // a positive integer, the minimum travel time for any segment is 1 minute
        // (ceil(dist/s) >= 1 for any finite s and dist >= 1).
        //
        // However, the real impossibility comes from intermediate trains:
        // After boarding train i (0 <= i < n-2), the traveler departs at schedule[i].
        // They must arrive at platform i+1 by schedule[i+1].
        // Even at infinite speed (travel time → 0), they arrive at schedule[i] + 0.
        // So we need schedule[i] <= schedule[i+1], which is guaranteed by strictly
        // increasing schedule.
        //
        // BUT: the traveler must arrive STRICTLY before or AT schedule[i+1].
        // Since travel time is at least 1 (ceil of positive dist / finite speed),
        // for intermediate trains we need schedule[i] + 1 <= schedule[i+1],
        // i.e., schedule[i+1] - schedule[i] >= 1, which is guaranteed.
        //
        // The only TRUE impossibility: if schedule[n-2] >= schedule[n-1].
        // Since schedule is strictly increasing, schedule[n-1] > schedule[n-2],
        // so there's always at least 1 minute gap for the last segment.
        //
        // Wait — re-reading the problem: the answer won't exceed 10^7 if it exists.
        // The problem says return -1 if impossible. Let's check when it's impossible:
        // For any intermediate segment i (0 <= i < n-2):
        //   departure time from platform i = schedule[i]
        //   arrival time at platform i+1 = schedule[i] + ceil(dist[i]/s)
        //   we need: schedule[i] + ceil(dist[i]/s) <= schedule[i+1]
        //   minimum ceil(dist[i]/s) = 1 (for any finite s)
        //   so we need: schedule[i] + 1 <= schedule[i+1]
        //   i.e., schedule[i+1] > schedule[i], guaranteed by strictly increasing.
        //
        // So it's NEVER impossible given strictly increasing schedule? Let's re-check
        // Example 2: schedule=[1,3,5], dist=[5,5]
        //   At speed 3: ceil(5/3)=2, depart platform 1 at schedule[0]=1, arrive at 3 ✓
        //               depart platform 2 at schedule[1]=3, arrive at 3+ceil(5/3)=3+2=5 ✓
        //   Output = 3. The problem says output is 3 (the example description is confusing).
        //
        // Given constraints say answer won't exceed 10^7, we use that as upper bound.
        // If no speed in [1, 10^7] works, return -1.

        int n = schedule.Length;
        // dist has n-1 elements (distances between consecutive platforms)

        // -----------------------------------------------------------------------
        // STEP 2: Set up Binary Search bounds
        // -----------------------------------------------------------------------
        // We binary search on the speed value s.
        // - Minimum possible speed: 1
        // - Maximum possible speed: 10^7 (given by constraints)
        //
        // The key insight for binary search:
        // If speed s works (traveler catches all trains), then any speed s' > s also works
        // (faster travel means earlier arrival). This MONOTONIC property makes binary
        // search applicable: we search for the smallest s where CanCatch(s) = true.

        int lo = 1;
        int hi = 10_000_000; // 10^7 per constraints
        int answer = -1;

        // -----------------------------------------------------------------------
        // STEP 3: Binary Search Loop
        // -----------------------------------------------------------------------
        // Standard "find leftmost valid" binary search pattern:
        // - If mid speed works, record it as a candidate answer and try smaller speeds
        // - If mid speed doesn't work, we need a faster speed

        while (lo <= hi)
        {
            // Pick the middle speed to test
            int mid = lo + (hi - lo) / 2; // Avoid integer overflow vs (lo+hi)/2

            if (CanCatchAllTrains(schedule, dist, mid))
            {
                // This speed works! Record it and try to find a smaller valid speed
                answer = mid;
                hi = mid - 1; // Search left half
            }
            else
            {
                // This speed is too slow, need to go faster
                lo = mid + 1; // Search right half
            }
        }

        // answer remains -1 if no speed in [1, 10^7] worked
        return answer;
    }

    /// <summary>
    /// Simulates the journey at the given speed and returns true if the traveler
    /// can board all trains on time.
    /// </summary>
    private bool CanCatchAllTrains(int[] schedule, int[] dist, int speed)
    {
        // -----------------------------------------------------------------------
        // SIMULATION STEP 1: Initialize
        // -----------------------------------------------------------------------
        // currentTime tracks when the traveler is ready to depart the current platform.
        // At the start, the traveler is at platform 0 (index 0) at time 0.
        // They board train 0 at time 0 (schedule[0] >= 0 always since schedule[i] >= 1,
        // and the traveler is already there at time 0 <= schedule[0]).
        // After boarding train 0, they depart at schedule[0].

        // We use 'long' to avoid integer overflow since schedule values can be up to 10^9
        // and we might add large numbers during simulation.
        long currentTime = 0L;

        int n = schedule.Length;

        // -----------------------------------------------------------------------
        // SIMULATION STEP 2: Process each train boarding
        // -----------------------------------------------------------------------
        // For train 0: traveler is already at platform 0 at time 0.
        //              They board train 0 (schedule[0] >= 0, so they wait until schedule[0]).
        //              After boarding, they depart at schedule[0].
        //
        // For train i (1 <= i <= n-1):
        //   - Traveler departs previous platform at currentTime (= schedule[i-1] after waiting)
        //   - Travel time = ceil(dist[i-1] / speed)
        //   - Arrival time = currentTime + ceil(dist[i-1] / speed)
        //   - Must arrive by schedule[i]
        //   - If they arrive early, they wait; currentTime becomes schedule[i]
        //   - If they arrive late, return false

        // Board train 0: traveler waits at platform 0 until schedule[0]
        // currentTime after boarding train 0 = schedule[0]
        currentTime = schedule[0]; // Depart platform 0 at schedule[0]

        // Now process each intermediate travel segment
        // dist[i] = distance from platform i to platform i+1
        // We have n-1 distances, so dist indices 0..n-2
        // After traveling dist[i], we arrive at platform i+1 to board train i+1

        for (int i = 0; i < dist.Length; i++)
        {
            // -----------------------------------------------------------------------
            // SIMULATION STEP 2a: Calculate travel time for this segment
            // -----------------------------------------------------------------------
            // Travel time = ceil(dist[i] / speed)
            // Integer ceiling division: ceil(a/b) = (a + b - 1) / b  (for positive a, b)
            // We use long arithmetic to prevent overflow (dist[i] up to 10^9, speed up to 10^7)

            long travelTime = ((long)dist[i] + speed - 1) / speed;

            // -----------------------------------------------------------------------
            // SIMULATION STEP 2b: Calculate arrival time at next platform
            // -----------------------------------------------------------------------
            // Arrival at platform i+1 = currentTime + travelTime
            long arrivalTime = currentTime + travelTime;

            // -----------------------------------------------------------------------
            // SIMULATION STEP 2c: Check if we made it on time
            // -----------------------------------------------------------------------
            // We need to arrive by schedule[i+1] (the (i+1)-th train's departure time)
            // schedule index for next train is i+1
            long nextSchedule = schedule[i + 1];

            if (arrivalTime > nextSchedule)
            {
                // Arrived too late! This speed is insufficient.
                return false;
            }

            // -----------------------------------------------------------------------
            // SIMULATION STEP 2d: Update currentTime
            // -----------------------------------------------------------------------
            // If we arrived early or on time, we wait for the train.
            // After boarding train i+1, we depart at schedule[i+1].
            // So currentTime becomes schedule[i+1] for the next segment.
            currentTime = nextSchedule;
        }

        // -----------------------------------------------------------------------
        // SIMULATION STEP 3: All trains boarded successfully
        // -----------------------------------------------------------------------
        // If we processed all dist segments without returning false, the traveler
        // successfully boarded all trains at this speed.
        return true;
    }
}

// ===============================================================================
// DEMO / TEST CODE
// ===============================================================================
// Using top-level statements (C# 9+, .NET 5+)

var solution = new Solution();

Console.WriteLine("=== Minimum Speed to Catch All Departing Trains ===");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 1: schedule = [2, 5, 9], dist = [3, 4]
// Expected Output: 2
// Trace at speed 2:
//   Board train 0 at time 0 (schedule[0]=2, traveler is there at 0, waits until 2)
//   Depart platform 0 at time 2
//   Travel dist[0]=3: ceil(3/2)=2 min, arrive platform 1 at time 4
//   Wait for train 1 (schedule[1]=5): depart at 5
//   Travel dist[1]=4: ceil(4/2)=2 min, arrive platform 2 at time 7
//   schedule[2]=9, 7 <= 9 ✓
//   All trains caught! Speed 2 works.
// -----------------------------------------------------------------------
int[] schedule1 = { 2, 5, 9 };
int[] dist1 = { 3, 4 };
int result1 = solution.MinSpeedOnTime(schedule1, dist1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  schedule = [2, 5, 9], dist = [3, 4]");
Console.WriteLine($"  Expected: 2");
Console.WriteLine($"  Got:      {result1}");
Console.WriteLine($"  Correct:  {result1 == 2}");
Console.WriteLine();

// -----------------------------------------------------------------------
// Example 2: schedule = [1, 3, 5], dist = [5, 5]
// Expected Output: 3
// Trace at speed 3:
//   Board train 0 at time 0 (schedule[0]=1, wait until 1), depart at 1
//   Travel dist[0]=5: ceil(5/3)=2 min, arrive platform 1 at time 3
//   schedule[1]=3, 3 <= 3 ✓, depart at 3
//   Travel dist[1]=5: ceil(5/3)=2 min, arrive platform 2 at time 5
//   schedule[2]=5, 5 <= 5 ✓
//   All trains caught! Speed 3 works.
// Trace at speed 2:
//   Board train 0, depart at 1
//   Travel dist[0]=5: ceil(5/2)=3 min, arrive platform 1 at time 4
//   schedule[1]=3, 4 > 3 ✗ — too slow!
// So minimum speed is 3.
// -----------------------------------------------------------------------
int[] schedule2 = { 1, 3, 5 };
int[] dist2 = { 5, 5 };
int result2 = solution.MinSpeedOnTime(schedule2, dist2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  schedule = [1, 3, 5], dist = [5, 5]");
Console.WriteLine($"  Expected: 3");
Console.WriteLine($"  Got:      {result2}");
Console.WriteLine($"  Correct:  {result2 == 3}");
Console.WriteLine();

// -----------------------------------------------------------------------
// Additional Example 3: Very tight schedule
// schedule = [1, 2, 3], dist = [1, 1]
// Train 0 at time 1, train 1 at time 2, train 2 at time 3
// After boarding train 0 at time 1, travel dist[0]=1: ceil(1/s)=1 for any s>=1
// Arrive at platform 1 at time 2 = schedule[1] ✓
// After boarding train 1 at time 2, travel dist[1]=1: ceil(1/s)=1 for any s>=1
// Arrive at platform 2 at time 3 = schedule[2] ✓
// Minimum speed = 1
// -----------------------------------------------------------------------
int[] schedule3 = { 1, 2, 3