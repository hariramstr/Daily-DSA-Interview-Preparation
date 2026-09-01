/*
Title: Find the First Neighbor Swap That Sorts a Line
Difficulty: Easy
Topic: Arrays

Problem Description:
You are given an integer array nums representing the priority values of items standing in a line.
You may perform at most one operation: choose an index i and swap nums[i] with nums[i + 1],
meaning only neighboring items can be swapped.

Your task is to find the smallest index i such that performing this single adjacent swap makes
the entire array sorted in non-decreasing order.

If the array is already sorted, return -1.
If no single adjacent swap can sort the array, also return -1.

Return the index of the left element in the swap.

An array is considered sorted in non-decreasing order if nums[j] <= nums[j + 1] for every valid j.

This problem is meant to test careful array scanning and boundary checking.
A correct solution should avoid trying every possible swap when unnecessary and should correctly
handle duplicates.

Constraints:
- 1 <= nums.length <= 100000
- -1000000000 <= nums[i] <= 1000000000

Example 1:
Input: nums = [1, 3, 2, 4]
Output: 1
Explanation: Swapping nums[1] and nums[2] gives [1, 2, 3, 4], which is sorted.
No smaller index works.

Example 2:
Input: nums = [1, 5, 3, 4, 2]
Output: -1
Explanation: No single swap of neighboring elements can make the full array sorted.

Notes:
- If nums = [1, 2, 2, 3], the answer is -1 because the array is already sorted.
- If multiple adjacent swaps could sort the array, return the smallest valid index.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of the complexity:
    - We scan the array once to find positions where the sorted order is broken.
    - Then we do only a constant amount of boundary checking around one candidate swap.
    - We do not try every possible swap, which would be too slow for large arrays.
    - We use only a few integer variables, so extra space is constant.
    */
    public int FindFirstNeighborSwapThatSorts(int[] nums)
    {
        // Step 1:
        // Handle very small arrays first.
        //
        // Why this is necessary:
        // - If the array has length 0 or 1, it is already sorted.
        // - The problem constraints start at length 1, but writing defensive code is still good practice.
        // - Since the array is already sorted, no swap is needed, so we return -1.
        if (nums == null || nums.Length <= 1)
        {
            return -1;
        }

        int n = nums.Length;

        // Step 2:
        // Find all "inversions" of adjacent order:
        // positions i where nums[i] > nums[i + 1].
        //
        // Why this is necessary:
        // - A sorted non-decreasing array must satisfy nums[i] <= nums[i + 1] everywhere.
        // - Any place where nums[i] > nums[i + 1] is a violation of sorted order.
        // - If one adjacent swap can fix the entire array, the structure of these violations is very limited.
        //
        // Data structure choice:
        // - We do not need a list or extra array.
        // - We only need:
        //   * the count of violations
        //   * the first violation index
        // - This keeps memory usage O(1).
        int inversionCount = 0;
        int firstInversionIndex = -1;

        for (int i = 0; i < n - 1; i++)
        {
            // We are checking whether the pair (nums[i], nums[i + 1]) breaks sorted order.
            if (nums[i] > nums[i + 1])
            {
                inversionCount++;

                // Store the first place where sorted order fails.
                // This is important because if a single adjacent swap can fix the array,
                // that swap must happen exactly at a violation position.
                if (firstInversionIndex == -1)
                {
                    firstInversionIndex = i;
                }

                // Early stopping optimization:
                // If there are more than 2 adjacent violations, one neighboring swap cannot fix all of them.
                //
                // Why?
                // - Swapping nums[i] and nums[i + 1] only changes relationships involving nearby positions.
                // - One adjacent swap cannot repair many separated disorder points.
                if (inversionCount > 2)
                {
                    return -1;
                }
            }
        }

        // Step 3:
        // If there are no violations, the array is already sorted.
        //
        // Example:
        // [1, 2, 2, 3] has no index i with nums[i] > nums[i + 1].
        // Therefore the answer must be -1.
        if (inversionCount == 0)
        {
            return -1;
        }

        // Step 4:
        // A valid one-swap solution must swap at the first inversion index.
        //
        // Why this is true:
        // - Suppose the first place the array becomes unsorted is at index k, meaning nums[k] > nums[k + 1].
        // - If we do not swap these two neighboring elements, then that broken order remains unchanged.
        // - So any successful single adjacent swap must be exactly at index k.
        int swapIndex = firstInversionIndex;

        // Step 5:
        // Before actually modifying the array, store the two values that would be swapped.
        //
        // This makes the later boundary checks easier to read.
        int leftValue = nums[swapIndex];
        int rightValue = nums[swapIndex + 1];

        // Step 6:
        // Check whether swapping these two values would preserve sorted order
        // with their immediate neighbors.
        //
        // Why only local checks are enough:
        // - Everywhere else in the array remains unchanged.
        // - The only comparisons that can change after swapping nums[swapIndex] and nums[swapIndex + 1]
        //   are the comparisons involving indices:
        //     * swapIndex - 1 with swapIndex
        //     * swapIndex with swapIndex + 1
        //     * swapIndex + 1 with swapIndex + 2
        // - All other adjacent pairs stay exactly the same.
        //
        // After swap:
        // - position swapIndex will contain rightValue
        // - position swapIndex + 1 will contain leftValue

        // Check left boundary:
        // If there is an element before swapIndex, then after the swap we need:
        // nums[swapIndex - 1] <= rightValue
        if (swapIndex - 1 >= 0 && nums[swapIndex - 1] > rightValue)
        {
            return -1;
        }

        // Check the middle pair after swap:
        // After swapping, we need rightValue <= leftValue.
        //
        // This is automatically true because swapIndex was an inversion:
        // leftValue > rightValue originally.
        // Still, keeping this check makes the logic explicit and beginner-friendly.
        if (rightValue > leftValue)
        {
            return -1;
        }

        // Check right boundary:
        // If there is an element after swapIndex + 1, then after the swap we need:
        // leftValue <= nums[swapIndex + 2]
        if (swapIndex + 2 < n && leftValue > nums[swapIndex + 2])
        {
            return -1;
        }

        // Step 7:
        // We also need to ensure that the total inversion pattern is compatible with one swap.
        //
        // Important observation:
        // - Swapping one adjacent pair can create/fix issues only in a tiny local area.
        // - Therefore, if the array has:
        //   * exactly 1 inversion, it may be fixable
        //   * exactly 2 inversions, they must be consecutive
        //
        // Why consecutive?
        // - A swap at index k affects comparisons at k-1, k, and k+1 only.
        // - So if there are two violations, they must be around the swap location, not far apart.
        if (inversionCount == 2)
        {
            // The second inversion must be exactly at swapIndex + 1.
            // If the two inversions are not consecutive, one adjacent swap cannot fix both.
            if (!(swapIndex + 1 < n - 1 && nums[swapIndex + 1] > nums[swapIndex + 2]))
            {
                return -1;
            }
        }

        // Step 8:
        // If all local checks passed, then swapping at swapIndex sorts the array.
        //
        // Let's verify against the required examples:
        //
        // Example 1: [1, 3, 2, 4]
        // - inversion at index 1 because 3 > 2
        // - swap 3 and 2 => [1, 2, 3, 4]
        // - valid, return 1
        //
        // Example 2: [1, 5, 3, 4, 2]
        // - inversions at index 1 (5 > 3) and index 3 (4 > 2)
        // - not consecutive around one swap
        // - return -1
        //
        // Example 3: [1, 2, 2, 3]
        // - no inversions
        // - already sorted, return -1
        return swapIndex;
    }
}

// Demo code:
// Create sample inputs, call the solution, and print results.

var solution = new Solution();

int[] nums1 = { 1, 3, 2, 4 };
int[] nums2 = { 1, 5, 3, 4, 2 };
int[] nums3 = { 1, 2, 2, 3 };
int[] nums4 = { 3, 1, 2 };
int[] nums5 = { 1, 4, 2, 3 };
int[] nums6 = { 2, 1 };
int[] nums7 = { 1 };
int[] nums8 = { 1, 2, 4, 3, 5 };
int[] nums9 = { 1, 3, 2, 2, 4 };

Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums1)); // Expected: 1
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums2)); // Expected: -1
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums3)); // Expected: -1
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums4)); // Expected: -1
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums5)); // Expected: -1
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums6)); // Expected: 0
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums7)); // Expected: -1
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums8)); // Expected: 2
Console.WriteLine(solution.FindFirstNeighborSwapThatSorts(nums9)); // Expected: 1