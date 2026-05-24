/*
 * Title: Minimum Bandwidth Allocation for K Data Streams
 *
 * Problem Description:
 * You are managing a network server that must handle `n` data streams, where each stream
 * has a bandwidth requirement given in the array `streams` (streams[i] is the bandwidth
 * needed by stream i in Mbps). You have exactly `k` network channels available, and you
 * must assign every stream to exactly one channel. Each channel's total load is the sum
 * of bandwidths of all streams assigned to it.
 *
 * To ensure fair distribution and avoid bottlenecks, you want to minimize the maximum
 * total load across all channels. However, there is a constraint: streams must be assigned
 * to channels in contiguous groups (i.e., streams[0..j1] go to channel 1,
 * streams[j1+1..j2] go to channel 2, etc.).
 *
 * Return the minimum possible value of the maximum channel load.
 *
 * Constraints:
 * - 1 <= k <= n <= 10^5
 * - 1 <= streams[i] <= 10^9
 * - It is guaranteed that k <= n
 *
 * Example 1:
 * Input: streams = [10, 20, 30, 40, 50], k = 3
 * Output: 60
 *
 * Example 2:
 * Input: streams = [7, 2, 5, 10, 8], k = 2
 * Output: 18
 */

// ============================================================
// Solution Class
// ============================================================
public class Solution
{
    // Time Complexity:  O(n * log(sum)) where n = number of streams,
    //                   sum = total sum of all stream bandwidths.
    //                   - Binary search runs O(log(sum)) iterations.
    //                   - Each feasibility check is O(n).
    //
    // Space Complexity: O(1) extra space (we only use a few variables,
    //                   no additional data structures proportional to input size).

    /// <summary>
    /// Finds the minimum possible maximum channel load when splitting
    /// the streams array into exactly k contiguous groups.
    /// </summary>
    /// <param name="streams">Array of bandwidth requirements for each stream.</param>
    /// <param name="k">Number of available network channels.</param>
    /// <returns>The minimized maximum load across all channels.</returns>
    public long MinimumMaxLoad(int[] streams, int k)
    {
        // -------------------------------------------------------
        // STEP 1: Understand the search space.
        //
        // We are looking for a "maximum load" value. Let's call it `mid`.
        // The question becomes: "Can we split the streams into AT MOST k
        // contiguous groups such that no group's sum exceeds `mid`?"
        //
        // The answer is monotonic:
        //   - If `mid` is large enough, we CAN do it (feasible).
        //   - If `mid` is too small, we CANNOT do it (infeasible).
        //
        // This monotonic property means Binary Search is applicable!
        //
        // Lower bound (lo): The maximum single stream value.
        //   Why? Even if we give each stream its own channel, one channel
        //   must carry at least the largest stream. So the answer can never
        //   be less than max(streams).
        //
        // Upper bound (hi): The sum of all streams.
        //   Why? If we put everything on one channel, the load equals the
        //   total sum. This is always feasible (k >= 1).
        // -------------------------------------------------------

        long lo = 0; // Will be set to max(streams)
        long hi = 0; // Will be set to sum(streams)

        // Calculate lo (max element) and hi (total sum) in one pass.
        foreach (int bandwidth in streams)
        {
            // hi accumulates the total sum of all bandwidths.
            hi += bandwidth;

            // lo tracks the maximum individual bandwidth seen so far.
            // The answer must be at least as large as the biggest single stream,
            // because that stream must go on some channel by itself or with others,
            // and its channel load will be at least that stream's value.
            if (bandwidth > lo)
                lo = bandwidth;
        }

        // -------------------------------------------------------
        // STEP 2: Binary Search Loop.
        //
        // We search for the smallest `mid` value such that
        // IsFeasible(streams, k, mid) returns true.
        //
        // Classic "find minimum feasible value" binary search pattern:
        //   - If mid is feasible, try smaller (move hi down).
        //   - If mid is not feasible, try larger (move lo up).
        // -------------------------------------------------------

        // `result` stores the best (smallest) feasible answer found so far.
        long result = hi; // Start with the worst-case answer (all on one channel).

        while (lo <= hi)
        {
            // Calculate the midpoint to avoid integer overflow.
            // (lo + hi) / 2 could overflow if both are large longs,
            // so we use lo + (hi - lo) / 2 instead.
            long mid = lo + (hi - lo) / 2;

            // -------------------------------------------------------
            // STEP 3: Check feasibility for the current candidate `mid`.
            //
            // Ask: "Can we partition streams into at most k contiguous
            // groups where each group's sum <= mid?"
            // -------------------------------------------------------
            if (IsFeasible(streams, k, mid))
            {
                // `mid` works as a maximum load limit!
                // Record it as a potential answer (we want the minimum such value).
                result = mid;

                // Try to find an even smaller feasible value.
                // Move the upper bound down.
                hi = mid - 1;
            }
            else
            {
                // `mid` is too small — we can't fit streams into k channels
                // without exceeding `mid` on at least one channel.
                // We need a larger maximum load limit.
                // Move the lower bound up.
                lo = mid + 1;
            }
        }

        // After the loop, `result` holds the minimum possible maximum load.
        return result;
    }

    /// <summary>
    /// Greedy feasibility check:
    /// Determines whether we can split `streams` into AT MOST `k` contiguous
    /// groups such that no group's total bandwidth exceeds `maxLoad`.
    ///
    /// Strategy (Greedy):
    /// Scan left to right, accumulating bandwidth into the current channel.
    /// Whenever adding the next stream would exceed `maxLoad`, start a new channel.
    /// Count how many channels we need. If it's <= k, it's feasible.
    /// </summary>
    /// <param name="streams">Array of bandwidth requirements.</param>
    /// <param name="k">Maximum number of channels allowed.</param>
    /// <param name="maxLoad">The candidate maximum load per channel.</param>
    /// <returns>True if the partition is possible within k channels; false otherwise.</returns>
    private bool IsFeasible(int[] streams, int k, long maxLoad)
    {
        // -------------------------------------------------------
        // GREEDY APPROACH EXPLANATION:
        //
        // We want to use as few channels as possible while keeping
        // each channel's load <= maxLoad.
        //
        // The greedy strategy: always fill the current channel as much
        // as possible before opening a new one. This minimizes the
        // number of channels used, which is optimal for our check.
        //
        // Why greedy works here:
        // If we "waste" space by splitting earlier than necessary,
        // we only increase the number of channels needed. So being
        // as greedy as possible (filling each channel to the max)
        // gives us the minimum channel count.
        // -------------------------------------------------------

        // Start with 1 channel (we always need at least one).
        int channelsUsed = 1;

        // Track the current channel's accumulated load.
        long currentLoad = 0;

        foreach (int bandwidth in streams)
        {
            // -------------------------------------------------------
            // STEP A: Check if a single stream exceeds maxLoad.
            //
            // If one stream alone is bigger than maxLoad, it's impossible
            // to assign it to any channel without exceeding the limit.
            // This case is actually prevented by our binary search lower bound
            // (lo = max(streams)), but it's good defensive programming.
            // -------------------------------------------------------
            // (This won't trigger in practice because lo >= max(streams),
            //  but included for clarity and safety.)

            // -------------------------------------------------------
            // STEP B: Try to add this stream to the current channel.
            //
            // If adding `bandwidth` to `currentLoad` would exceed `maxLoad`,
            // we must open a new channel and start fresh with this stream.
            // -------------------------------------------------------
            if (currentLoad + bandwidth > maxLoad)
            {
                // The current channel is "full" — open a new one.
                channelsUsed++;

                // If we've already exceeded the allowed number of channels,
                // this `maxLoad` value is not feasible. Return early.
                if (channelsUsed > k)
                    return false;

                // Start the new channel's load with the current stream.
                currentLoad = bandwidth;
            }
            else
            {
                // There's room — add this stream to the current channel.
                currentLoad += bandwidth;
            }
        }

        // If we processed all streams without needing more than k channels,
        // then `maxLoad` is a feasible maximum load.
        return true;
    }
}

// ============================================================
// Demo / Test Code (Top-Level Statements)
// ============================================================

var solution = new Solution();

// ----------------------------------------------------------
// Example 1:
// streams = [10, 20, 30, 40, 50], k = 3
// Expected Output: 60
//
// Trace:
//   lo = max(10,20,30,40,50) = 50
//   hi = 10+20+30+40+50 = 150
//
//   Iteration 1: mid = 50 + (150-50)/2 = 100
//     IsFeasible([10,20,30,40,50], 3, 100)?
//       ch=1, load=0
//       +10 -> load=10
//       +20 -> load=30
//       +30 -> load=60
//       +40 -> load=100
//       +50 -> 100+50=150 > 100, open ch=2, load=50
//       Result: 2 channels <= 3 => feasible
//     result=100, hi=99
//
//   Iteration 2: mid = 50 + (99-50)/2 = 74
//     IsFeasible([10,20,30,40,50], 3, 74)?
//       ch=1, load=0
//       +10->10, +20->30, +30->60, +40->100>74 => ch=2, load=40
//       +50->90>74 => ch=3, load=50
//       Result: 3 channels <= 3 => feasible
//     result=74, hi=73
//
//   Iteration 3: mid = 50 + (73-50)/2 = 61
//     IsFeasible([10,20,30,40,50], 3, 61)?
//       ch=1, load=0
//       +10->10, +20->30, +30->60, +40->100>61 => ch=2, load=40
//       +50->90>61 => ch=3, load=50
//       Result: 3 channels <= 3 => feasible
//     result=61, hi=60
//
//   Iteration 4: mid = 50 + (60-50)/2 = 55
//     IsFeasible([10,20,30,40,50], 3, 55)?
//       ch=1, load=0
//       +10->10, +20->30, +30->60>55 => ch=2, load=30
//       +40->70>55 => ch=3, load=40
//       +50->90>55 => ch=4 > 3 => NOT feasible
//     lo=56
//
//   Iteration 5: mid = 56 + (60-56)/2 = 58
//     IsFeasible([10,20,30,40,50], 3, 58)?
//       ch=1: +10->10, +20->30, +30->60>58 => ch=2, load=30
//       +40->70>58 => ch=3, load=40
//       +50->90>58 => ch=4 > 3 => NOT feasible
//     lo=59
//
//   Iteration 6: mid = 59 + (60-59)/2 = 59
//     IsFeasible([10,20,30,40,50], 3, 59)?
//       ch=1: +10->10, +20->30, +30->60>59 => ch=2, load=30
//       +40->70>59 => ch=3, load=40
//       +50->90>59 => ch=4 > 3 => NOT feasible
//     lo=60
//
//   Iteration 7: mid = 60 + (60-60)/2 = 60
//     IsFeasible([10,20,30,40,50], 3, 60)?
//       ch=1: +10->10, +20->30, +30->60, +40->100>60 => ch=2, load=40
//       +50->90>60 => ch=3, load=50
//       Result: 3 channels <= 3 => feasible
//     result=60, hi=59
//
//   lo=60 > hi=59 => loop ends. Answer = 60. ✓
// ----------------------------------------------------------
int[] streams1 = { 10, 20, 30, 40, 50 };
int k1 = 3;
long result1 = solution.MinimumMaxLoad(streams1, k1);
Console.WriteLine($"Example 1:");
Console.WriteLine($"  Input:    streams = [10, 20, 30, 40, 50], k = {k1}");
Console.WriteLine($"  Output:   {result1}");
Console.WriteLine($"  Expected: 60");
Console.WriteLine($"  Correct:  {result1 == 60}");
Console.WriteLine();

// ----------------------------------------------------------
// Example 2:
// streams = [7, 2, 5, 10, 8], k = 2
// Expected Output: 18
//
// Trace:
//   lo = max(7,2,5,10,8) = 10
//   hi = 7+2+5+10+8 = 32
//
//   Binary search will converge to 18:
//   Split [7,2,5] (sum=14) and [10,8] (sum=18). Max = 18.
//   Any smaller max (e.g., 17) would require 3+ channels.
// ----------------------------------------------------------
int[] streams2 = { 7, 2, 5, 10, 8 };
int k2 = 2;
long result2 = solution.MinimumMaxLoad(streams2, k2);
Console.WriteLine($"Example 2:");
Console.WriteLine($"  Input:    streams = [7, 2, 5, 10, 8], k = {k2}");
Console.WriteLine($"  Output:   {result2}");
Console.WriteLine($"  Expected: 18");
Console.WriteLine($"  Correct:  {result2 == 18}");
Console.WriteLine();

// ----------------------------------------------------------
// Additional Edge Case Tests
// ----------------------------------------------------------

// Edge Case 1: k == n (each stream gets its own channel)
// The answer should be the maximum single stream value.
int[] streams3 = { 5, 3, 8, 1, 6 };
int k3 = 5;
long result3 = solution.MinimumMaxLoad(streams3, k3);
Console.WriteLine($"Edge Case 1 (k == n, each stream its own channel):");
Console.WriteLine($"  Input:    streams = [5, 3, 8, 1, 6], k = {k3}");
Console.WriteLine($"  Output:   {result3}");
Console.WriteLine($"  Expected: 