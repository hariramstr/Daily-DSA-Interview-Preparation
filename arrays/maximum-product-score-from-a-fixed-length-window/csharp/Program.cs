/*
Title: Maximum Product Score from a Fixed-Length Window

Problem Description:
You are given an integer array nums and an integer k. A contiguous window of length k is called valid if it contains no zero.
The product score of a valid window is the product of all elements inside that window.
Your task is to return the maximum product score among all valid windows of length exactly k.
If no valid window exists, return 0.

This problem is designed for large inputs, so recomputing the product from scratch for every window will be too slow.
You need to process the array efficiently while handling positive numbers, negative numbers, and zeros.
Because negative values can flip the sign of the product, the maximum answer is not always produced by the window with the largest absolute values.
Windows containing even one zero are invalid and must be skipped entirely.

Return the maximum product as a 64-bit integer. You may assume the final answer fits in a signed 64-bit range.

Constraints:
- 1 <= nums.length <= 100000
- -10 <= nums[i] <= 10
- 1 <= k <= nums.length
- The maximum valid product fits in a signed 64-bit range

Examples:
1) nums = [2, -3, 4, -1, 5], k = 3
   Windows:
   [2, -3, 4]  => -24
   [-3, 4, -1] => 12
   [4, -1, 5]  => -20
   Answer: 12

2) nums = [0, -2, -3, 4, 0, 5], k = 2
   Windows:
   [0, -2]  => invalid
   [-2, -3] => 6
   [-3, 4]  => -12
   [4, 0]   => invalid
   [0, 5]   => invalid
   Answer: 6
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    - We process each array element a constant number of times while sliding the window.
    - Multiplication and division are O(1) operations here.

    Space Complexity: O(1)
    - We only store a few variables for the sliding window state.
    - No extra arrays or collections are needed.
    */
    public long MaxProductScore(int[] nums, int k)
    {
        // This variable stores the product of the current window,
        // but only when the current window contains no zero.
        //
        // Why long?
        // The problem explicitly asks us to return a 64-bit integer,
        // and guarantees the final answer fits in signed 64-bit range.
        long currentProduct = 1;

        // This counts how many zeros are currently inside the sliding window.
        //
        // Why do we need this?
        // Any window containing at least one zero is invalid.
        // Instead of recomputing whether a window has a zero from scratch,
        // we maintain this count as the window moves.
        int zeroCount = 0;

        // This tells us whether we have found at least one valid window.
        // If we never find one, the answer must be 0.
        bool foundValidWindow = false;

        // This will store the best (maximum) product among all valid windows.
        long maxProduct = long.MinValue;

        // We will use a standard sliding window:
        // - right expands the window by adding a new element
        // - once the window size exceeds k, we remove the leftmost element
        //
        // At every step where the window size is exactly k, we evaluate it.
        for (int right = 0; right < nums.Length; right++)
        {
            // STEP 1: Add nums[right] into the window.
            //
            // There are two cases:
            // A) The new value is zero:
            //    - The window becomes invalid (or remains invalid).
            //    - We increase zeroCount.
            //    - We do NOT multiply currentProduct by zero, because that would
            //      destroy the product information for the non-zero values.
            //
            // B) The new value is non-zero:
            //    - We multiply it into currentProduct.
            //    - This is safe because currentProduct is intended to track
            //      the product of all non-zero values currently in the window.
            if (nums[right] == 0)
            {
                zeroCount++;
            }
            else
            {
                currentProduct *= nums[right];
            }

            // STEP 2: If the window is now too large, remove the leftmost element.
            //
            // The current window is [right - currentSize + 1, right].
            // Once its size becomes greater than k, we must shrink it from the left.
            if (right >= k)
            {
                int leftValue = nums[right - k];

                // If the outgoing value is zero, we simply reduce zeroCount.
                // This means one zero has left the window.
                if (leftValue == 0)
                {
                    zeroCount--;
                }
                else
                {
                    // If the outgoing value is non-zero, we divide it out
                    // from currentProduct.
                    //
                    // Why is division valid here?
                    // Because currentProduct currently includes this exact factor,
                    // and there are no rounding issues since the product was built
                    // by multiplying integers and we are dividing by one of those
                    // exact integer factors.
                    currentProduct /= leftValue;
                }
            }

            // STEP 3: Once the window size reaches exactly k, evaluate it.
            //
            // The first time this happens is when right == k - 1.
            if (right >= k - 1)
            {
                // A window is valid if and only if it contains no zero.
                // Our zeroCount tells us this in O(1) time.
                if (zeroCount == 0)
                {
                    // Since there are no zeros in the window,
                    // currentProduct is exactly the product of all k elements.
                    if (!foundValidWindow)
                    {
                        // First valid window found:
                        // initialize maxProduct with its product.
                        maxProduct = currentProduct;
                        foundValidWindow = true;
                    }
                    else
                    {
                        // Compare against the best answer seen so far.
                        if (currentProduct > maxProduct)
                        {
                            maxProduct = currentProduct;
                        }
                    }
                }
            }
        }

        // If we never found a valid window, return 0 as required.
        return foundValidWindow ? maxProduct : 0L;
    }
}

// Demo code

var solution = new Solution();

// Example 1
int[] nums1 = { 2, -3, 4, -1, 5 };
int k1 = 3;
long result1 = solution.MaxProductScore(nums1, k1);
Console.WriteLine(result1); // Expected: 12

// Example 2
int[] nums2 = { 0, -2, -3, 4, 0, 5 };
int k2 = 2;
long result2 = solution.MaxProductScore(nums2, k2);
Console.WriteLine(result2); // Expected: 6

// Additional demo cases

// No valid window because every length-2 window contains a zero
int[] nums3 = { 0, 1, 0, 2, 0 };
int k3 = 2;
long result3 = solution.MaxProductScore(nums3, k3);
Console.WriteLine(result3); // Expected: 0

// Single-element windows
int[] nums4 = { -5, 0, 3, -2 };
int k4 = 1;
long result4 = solution.MaxProductScore(nums4, k4);
Console.WriteLine(result4); // Expected: 3

// Entire array as one window
int[] nums5 = { -2, -3, 4 };
int k5 = 3;
long result5 = solution.MaxProductScore(nums5, k5);
Console.WriteLine(result5); // Expected: 24