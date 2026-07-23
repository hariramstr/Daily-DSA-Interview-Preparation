/*
Title: Minimum Station Range to Relay a Mountain Convoy

Problem Description:
A rescue convoy must travel from checkpoint 0 to checkpoint D along a one-dimensional mountain road.
Along the road, there are n possible relay station locations given in sorted order by the array positions,
where positions[i] is the distance of station i from checkpoint 0.

Each station can be activated or skipped. If activated, a station with transmission range R can relay
commands to any other activated station or checkpoint whose distance is at most R away.

Checkpoint 0 and checkpoint D behave like fixed endpoints that are always available, but they do not count
toward the station limit.

You are also given an integer k. Due to budget constraints, you may activate at most k stations.
Your task is to compute the minimum integer range R such that commands can be relayed from checkpoint 0
to checkpoint D using at most k activated stations.

In other words, after choosing at most k stations, there must exist a chain starting at 0 and ending at D
where every consecutive pair in the chain is at distance at most R.

Return the minimum possible R.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Binary search over the answer range: O(log D)
    - Feasibility check for one candidate range R: O(n)
    - Total: O(n log D)

    Space Complexity:
    - O(1) extra space beyond the input array

    Beginner-friendly idea:
    We binary-search the smallest range R that works.

    For a fixed R, we must answer:
    "What is the minimum number of stations needed so that we can go from 0 to D
     with every hop length <= R?"

    Greedy rule for a fixed R:
    - From the current reachable point, jump to the farthest station within distance R.
    - This is optimal because choosing the farthest possible next station can only help:
      it never increases the number of stations needed later, and usually reduces it.

    Important detail:
    - Checkpoint 0 and checkpoint D are endpoints, not counted as activated stations.
    - Only actual chosen stations count toward the limit k.
    */
    public long MinimumRange(long[] positions, long D, int k)
    {
        // -----------------------------
        // Step 1: Establish binary search bounds.
        // -----------------------------
        // The minimum possible range cannot be negative, so low starts at 0.
        // The maximum possible range can safely be D, because with range D
        // we can always go directly from checkpoint 0 to checkpoint D with no stations.
        long low = 0;
        long high = D;

        // -----------------------------
        // Step 2: Standard binary search for the smallest feasible range.
        // -----------------------------
        // We repeatedly test the middle value:
        // - If it is feasible, try smaller.
        // - If it is not feasible, try larger.
        while (low < high)
        {
            long mid = low + (high - low) / 2;

            if (CanRelayWithAtMostKStations(positions, D, k, mid))
            {
                // mid works, so the answer is <= mid
                high = mid;
            }
            else
            {
                // mid does not work, so the answer must be > mid
                low = mid + 1;
            }
        }

        // When low == high, we have found the minimum feasible range.
        return low;
    }

    private bool CanRelayWithAtMostKStations(long[] positions, long D, int k, long R)
    {
        // ------------------------------------------------------------
        // Goal of this method:
        // Determine whether there exists a chain from 0 to D
        // using at most k stations, where every hop is <= R.
        // ------------------------------------------------------------

        // ------------------------------------------------------------
        // Greedy strategy:
        // Start at checkpoint 0.
        // While D is still farther than R away, we must use another station.
        // Among all stations reachable within distance R from the current point,
        // choose the farthest one.
        //
        // Why farthest?
        // Because it gives the maximum progress in one station activation.
        // If a closer station were better, then the farther one would still be
        // at least as good for future reachability on a 1D line.
        // ------------------------------------------------------------

        long current = 0;   // Current point in the chain: starts at checkpoint 0
        int used = 0;       // Number of activated stations used so far
        int i = 0;          // Pointer scanning the sorted station positions array
        int n = positions.Length;

        // ------------------------------------------------------------
        // If D is already within range from current, we are done immediately.
        // Otherwise, we need to keep selecting stations.
        // ------------------------------------------------------------
        while (D - current > R)
        {
            // --------------------------------------------------------
            // We need to find the farthest station whose position is
            // <= current + R, meaning it can be reached in one hop.
            //
            // Because positions is sorted, we can advance pointer i
            // while stations remain reachable.
            // --------------------------------------------------------
            long farthestReachableStation = current;

            while (i < n && positions[i] - current <= R)
            {
                farthestReachableStation = positions[i];
                i++;
            }

            // --------------------------------------------------------
            // If we could not move at all, then no station is reachable
            // from the current point, and D is also not directly reachable
            // (otherwise the loop would have ended).
            //
            // Therefore this R is impossible.
            // --------------------------------------------------------
            if (farthestReachableStation == current)
            {
                return false;
            }

            // --------------------------------------------------------
            // Activate that farthest reachable station.
            // This counts toward the station budget.
            // --------------------------------------------------------
            current = farthestReachableStation;
            used++;

            // --------------------------------------------------------
            // If we already exceeded the allowed number of stations,
            // then this candidate range R is not feasible.
            // --------------------------------------------------------
            if (used > k)
            {
                return false;
            }
        }

        // ------------------------------------------------------------
        // If we exit the loop, then D is within one final hop from current.
        // Since D is an endpoint, not a station, it does not increase 'used'.
        // Therefore feasibility is simply whether used <= k, which is already true.
        // ------------------------------------------------------------
        return true;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1
long[] positions1 = { 2, 5, 8, 12 };
long D1 = 15;
int k1 = 2;
long result1 = solution.MinimumRange(positions1, D1, k1);
Console.WriteLine(result1); // Expected: 7

// Example 2
// Note:
// The problem statement text contains contradictory reasoning and says output 9,
// but its own explanation concludes the smallest feasible range is 10.
// The correct answer for this input is 10.
long[] positions2 = { 4, 9, 14, 20, 27 };
long D2 = 30;
int k2 = 3;
long result2 = solution.MinimumRange(positions2, D2, k2);
Console.WriteLine(result2); // Correct: 10

// Additional small sanity checks
long[] positions3 = { 3, 6, 9 };
long D3 = 12;
int k3 = 0;
long result3 = solution.MinimumRange(positions3, D3, k3);
Console.WriteLine(result3); // Need direct hop 0 -> 12, so expected: 12

long[] positions4 = { 3, 6, 9 };
long D4 = 12;
int k4 = 2;
long result4 = solution.MinimumRange(positions4, D4, k4);
Console.WriteLine(result4); // 0 -> 6 -> 12, expected: 6