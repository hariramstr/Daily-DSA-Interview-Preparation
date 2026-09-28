/*
Title: Minimum Router Radius for Highway Emergency Phones
Difficulty: Medium
Topic: Binary Search

Problem Description:
A transportation agency is installing wireless routers along a straight highway to provide service to emergency phones.
The highway is modeled as a number line.

You are given two integer arrays:
- phones, where phones[i] is the position of the i-th emergency phone
- routers, where routers[j] is the position of the j-th router installation point

A router with signal radius r covers every phone whose distance from that router is at most r.

Your task is to return the minimum integer radius r such that every emergency phone is covered by at least one router.

Important details:
- The arrays are not guaranteed to be sorted.
- Positions may be negative.
- Multiple phones or routers may share the same position.
- A brute-force O(n * m) solution is too slow for large inputs.

Efficient idea:
1. Sort the router positions.
2. For each phone, use binary search to find where that phone would be inserted in the sorted router array.
3. The nearest router must be either:
   - the router immediately to the left of that insertion point, or
   - the router at that insertion point (the first router not smaller than the phone)
4. Compute the phone's distance to its nearest router.
5. The answer is the maximum of those nearest distances across all phones.

Example 1:
phones = [2, 10, 15], routers = [1, 5, 14]
Sorted routers = [1, 5, 14]

Phone 2:
- nearest routers around it are 1 and 5
- distances are 1 and 3
- minimum distance = 1

Phone 10:
- nearest routers around it are 5 and 14
- distances are 5 and 4
- minimum distance = 4

Phone 15:
- nearest routers around it are 14
- distance = 1
- minimum distance = 1

Maximum of [1, 4, 1] = 4
Output = 4

Example 2:
phones = [-8, -3, 0, 7], routers = [-10, 2]
Sorted routers = [-10, 2]

Phone -8:
- distances to nearest candidates: 2 and 10
- minimum = 2

Phone -3:
- distances: 7 and 5
- minimum = 5

Phone 0:
- distances: 10 and 2
- minimum = 2

Phone 7:
- only right-side comparison is unavailable, left router 2 gives distance 5
- minimum = 5

Maximum of [2, 5, 2, 5] = 5
Output = 5
*/

using System;

public class Solution
{
    /*
    Time Complexity:
    - Sorting routers takes O(m log m), where m = routers.Length
    - For each phone, we perform one binary search on routers, which takes O(log m)
    - Doing that for all n phones takes O(n log m)
    - Total: O(m log m + n log m)

    Space Complexity:
    - O(1) extra space beyond the sorting implementation details
    - We sort the routers array in place
    */
    public int FindMinimumRouterRadius(int[] phones, int[] routers)
    {
        // Step 1:
        // Sort the router positions so that binary search becomes possible.
        //
        // Why this is necessary:
        // Binary search only works on sorted data.
        // Once routers are sorted, for any phone position we can quickly find
        // the "boundary" where that phone would fit in the router array.
        //
        // Data structure choice:
        // We use the input int[] directly because arrays are compact and efficient.
        // Sorting in place avoids creating unnecessary extra memory.
        Array.Sort(routers);

        // This variable will store the final answer.
        //
        // Meaning:
        // For each phone, we compute the distance to its closest router.
        // The minimum radius that covers ALL phones must be at least the largest
        // of those closest distances.
        int requiredRadius = 0;

        // Step 2:
        // Process each phone independently.
        //
        // Why this works:
        // The coverage requirement for one phone does not change the nearest-router
        // distance for another phone. So we can compute the best possible coverage
        // distance for each phone separately, then take the maximum.
        foreach (int phone in phones)
        {
            // Step 3:
            // Use binary search to find the insertion index of the current phone
            // in the sorted routers array.
            //
            // Array.BinarySearch behavior:
            // - If the exact value exists, it returns its index (>= 0).
            // - If it does not exist, it returns a negative number.
            //   The insertion index can be recovered with bitwise complement: ~index.
            //
            // The insertion index is the first position where router >= phone.
            int index = Array.BinarySearch(routers, phone);

            // If the phone position is not exactly a router position,
            // convert the negative result into the insertion index.
            if (index < 0)
            {
                index = ~index;
            }

            // Step 4:
            // The nearest router can only be one of two candidates:
            // 1. The router immediately to the left of the insertion point: index - 1
            // 2. The router at the insertion point itself: index
            //
            // Why only these two?
            // Because in a sorted array, anything further left than index - 1
            // is even farther away than routers[index - 1], and anything further right
            // than index is even farther away than routers[index].
            //
            // We start with a very large value so that taking Math.Min works correctly.
            int nearestDistance = int.MaxValue;

            // Candidate 1: router on the left side, if it exists.
            if (index - 1 >= 0)
            {
                // Use long during subtraction to be extra safe with integer arithmetic,
                // then cast back to int because the problem guarantees the final result
                // fits in a 32-bit signed integer.
                int leftDistance = (int)Math.Abs((long)phone - routers[index - 1]);

                // Keep the smaller distance because we want the closest router.
                nearestDistance = Math.Min(nearestDistance, leftDistance);
            }

            // Candidate 2: router on the right side (or exact match), if it exists.
            if (index < routers.Length)
            {
                int rightDistance = (int)Math.Abs((long)routers[index] - phone);

                // Again, keep the smaller of the current best and this candidate.
                nearestDistance = Math.Min(nearestDistance, rightDistance);
            }

            // Step 5:
            // Update the global answer.
            //
            // Why take the maximum here?
            // Suppose each phone needs at least its nearestDistance to be covered.
            // Then the router radius must be large enough for the "hardest" phone,
            // meaning the phone whose nearest router is farthest away.
            requiredRadius = Math.Max(requiredRadius, nearestDistance);
        }

        // Step 6:
        // After checking every phone, requiredRadius is the smallest radius
        // that guarantees all phones are covered.
        return requiredRadius;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] phones1 = { 2, 10, 15 };
int[] routers1 = { 1, 5, 14 };
int result1 = solution.FindMinimumRouterRadius(phones1, routers1);
Console.WriteLine(result1); // Expected: 4

// Example 2
int[] phones2 = { -8, -3, 0, 7 };
int[] routers2 = { -10, 2 };
int result2 = solution.FindMinimumRouterRadius(phones2, routers2);
Console.WriteLine(result2); // Expected: 5

// Additional demo
int[] phones3 = { 1, 2, 3, 4 };
int[] routers3 = { 1, 4 };
int result3 = solution.FindMinimumRouterRadius(phones3, routers3);
Console.WriteLine(result3); // Expected: 1