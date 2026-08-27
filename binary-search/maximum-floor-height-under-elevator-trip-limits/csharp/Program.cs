/*
Title: Maximum Floor Height Under Elevator Trip Limits
Difficulty: Hard
Topic: Binary Search

Problem Description:
A logistics company is configuring a freight elevator in a warehouse tower. There are n delivery batches, and batch i contains boxes[i] identical boxes. The elevator can carry at most cap boxes per trip, where cap is a positive integer chosen once for all batches. A single batch may be split across multiple trips, but boxes from different batches cannot be mixed in the same trip because each batch must remain sealed and tracked separately. Therefore, batch i requires ceil(boxes[i] / cap) trips.

The elevator is only allowed to make at most maxTrips total trips during the shift. Your task is to compute the largest integer capacity cap such that all batches can still be transported within maxTrips trips.

If it is impossible even when cap is arbitrarily large, return -1. Note that making cap larger never increases the number of trips, so the answer is monotonic and should be solved efficiently.

Formally, find the maximum integer cap >= 1 satisfying:
ceil(boxes[0] / cap) + ceil(boxes[1] / cap) + ... + ceil(boxes[n-1] / cap) <= maxTrips.

Constraints:
- 1 <= n <= 200000
- 1 <= boxes[i] <= 10^12
- 1 <= maxTrips <= 10^18
- The answer must fit in a signed 64-bit integer

Important clarification for correctness:
Because the problem asks for the "largest" capacity, the search must be bounded.
As stated in the prompt, we define the meaningful search range as:
cap in [1, max(boxes)].

Why this matters:
- For any cap > max(boxes), every batch needs exactly 1 trip.
- That means all capacities above max(boxes) behave identically.
- Without a bound, there would be infinitely many feasible capacities whenever n <= maxTrips.
- Therefore, the intended answer is the largest feasible cap inside [1, max(boxes)].

Example 1:
Input: boxes = [8, 5, 13], maxTrips = 8
Trips with cap = 4:
- ceil(8/4) = 2
- ceil(5/4) = 2
- ceil(13/4) = 4
Total = 8, so cap = 4 is feasible.

But the problem asks for the largest feasible cap in [1, max(boxes)].
max(boxes) = 13.

Check cap = 13:
- ceil(8/13) = 1
- ceil(5/13) = 1
- ceil(13/13) = 1
Total = 3 <= 8, so cap = 13 is feasible.

Therefore the correct output is 13.

Example 2:
Input: boxes = [4, 4, 4], maxTrips = 2
Even with cap = 4:
- ceil(4/4) = 1
- ceil(4/4) = 1
- ceil(4/4) = 1
Total = 3 > 2

So it is impossible, and the answer is -1.
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - O(n log M)
      where:
      n = number of batches
      M = max(boxes)
    Explanation:
    - We binary search over capacity values from 1 to max(boxes).
    - For each guessed capacity, we scan the entire boxes array once to count trips.
    - That gives O(n) work per binary search step and O(log M) steps total.

    Space Complexity:
    - O(1) extra space
    Explanation:
    - We only use a few variables.
    - No additional arrays or complex data structures are needed.
    */
    public long MaximumCapacity(long[] boxes, long maxTrips)
    {
        // Step 1:
        // We first determine the upper bound of our binary search.
        //
        // Why is this necessary?
        // The prompt explicitly defines the meaningful search range as [1, max(boxes)].
        // Any capacity larger than the largest batch size behaves the same:
        // each batch would take exactly 1 trip.
        //
        // So the largest meaningful capacity we ever need to consider is max(boxes).
        long maxBox = 0;
        foreach (long box in boxes)
        {
            if (box > maxBox)
            {
                maxBox = box;
            }
        }

        // Step 2:
        // Before binary searching, we check whether the task is impossible even at the
        // largest meaningful capacity.
        //
        // Why does this work?
        // At cap = max(boxes), every batch needs exactly 1 trip, because each batch size
        // is <= cap.
        // Therefore, the minimum possible number of trips inside our search range is exactly
        // the number of batches.
        //
        // If number of batches > maxTrips, then even the best possible capacity in [1, max(boxes)]
        // cannot satisfy the limit, so we must return -1.
        if (boxes.LongLength > maxTrips)
        {
            return -1;
        }

        // Step 3:
        // Set up the binary search boundaries.
        //
        // We are searching for the largest feasible capacity.
        // "Feasible" means total trips <= maxTrips.
        //
        // Because increasing capacity never increases the number of trips,
        // feasibility is monotonic:
        // - If some capacity is feasible, then every larger capacity is also feasible.
        // - If some capacity is not feasible, then every smaller capacity may or may not be feasible,
        //   but that does not break binary search because the boundary is ordered.
        //
        // So we can binary search for the rightmost feasible value.
        long left = 1;
        long right = maxBox;
        long answer = -1;

        // Step 4:
        // Standard binary search loop.
        //
        // We continue while the search interval is valid.
        while (left <= right)
        {
            // Step 4a:
            // Compute the middle capacity carefully.
            //
            // Why use this formula instead of (left + right) / 2?
            // To avoid overflow in general binary search patterns.
            long mid = left + (right - left) / 2;

            // Step 4b:
            // Check whether this candidate capacity is feasible.
            //
            // If feasible:
            // - record it as a possible answer
            // - try to go larger, because we want the maximum feasible capacity
            //
            // If not feasible:
            // - we must go smaller
            if (IsFeasible(boxes, maxTrips, mid))
            {
                answer = mid;
                left = mid + 1;
            }
            else
            {
                right = mid - 1;
            }
        }

        // Step 5:
        // After binary search finishes, 'answer' stores the largest feasible capacity
        // in the range [1, max(boxes)], or -1 if none exists.
        return answer;
    }

    private bool IsFeasible(long[] boxes, long maxTrips, long cap)
    {
        // This helper method computes how many total trips are needed if the elevator
        // capacity is exactly 'cap'.
        //
        // We return true if total trips <= maxTrips, otherwise false.

        long totalTrips = 0;

        foreach (long box in boxes)
        {
            // For one batch of size 'box', the number of trips needed is:
            // ceil(box / cap)
            //
            // In integer arithmetic, we compute ceiling division as:
            // (box + cap - 1) / cap
            //
            // Why this formula works:
            // - If box divides evenly by cap, it gives the exact quotient.
            // - Otherwise, it rounds up to account for the partially filled final trip.
            long tripsForThisBatch = (box + cap - 1) / cap;

            totalTrips += tripsForThisBatch;

            // Very important optimization:
            // If totalTrips already exceeds maxTrips, we can stop early.
            //
            // Why is this safe?
            // Because adding more batches can only increase totalTrips further.
            // So once we are already above the allowed limit, the candidate capacity
            // is definitely not feasible.
            //
            // This early exit can save a lot of time on large inputs.
            if (totalTrips > maxTrips)
            {
                return false;
            }
        }

        // If we finished processing all batches and never exceeded maxTrips,
        // then this capacity works.
        return true;
    }
}

// Demo code:
// The problem statement asked for working sample usage after the solution class.
// We create the sample inputs, call the solution, and print the results.

var solution = new Solution();

// Example 1
long[] boxes1 = { 8, 5, 13 };
long maxTrips1 = 8;
long result1 = solution.MaximumCapacity(boxes1, maxTrips1);
Console.WriteLine(result1); // Expected: 13

// Example 2
long[] boxes2 = { 4, 4, 4 };
long maxTrips2 = 2;
long result2 = solution.MaximumCapacity(boxes2, maxTrips2);
Console.WriteLine(result2); // Expected: -1