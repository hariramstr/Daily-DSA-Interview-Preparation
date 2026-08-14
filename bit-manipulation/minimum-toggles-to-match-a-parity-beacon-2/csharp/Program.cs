/*
Title: Minimum Toggles to Match a Parity Beacon
Difficulty: Medium
Topic: Bit Manipulation

Problem Description:
A monitoring system stores the state of n beacon modules in a binary array bits, where bits[i] is either 0 or 1.
You are also given a target binary array target of the same length.

In one operation, you may choose any index i and toggle bits[i]. However, toggling index i also automatically
toggles every index j > i such that j and i have the same parity (both even or both odd). In other words,
choosing i flips bits[i], bits[i+2], bits[i+4], and so on.

Your task is to return the minimum number of operations required to transform bits into target.
If it is impossible, return -1.

Key structural insight:
- An operation at an even index affects only even indices.
- An operation at an odd index affects only odd indices.
- Therefore, the array splits into two completely independent chains:
  1) even positions: 0, 2, 4, ...
  2) odd positions: 1, 3, 5, ...

Within one chain, choosing a position flips that position and every later position in the same chain.
This is exactly a suffix-flip process on a binary sequence.

For a single chain:
- Process from left to right.
- Keep track of whether an odd or even number of previous flips has affected the current position.
- If the current effective bit does not match the target bit, we MUST flip here.
  Why "must"? Because later flips cannot affect earlier positions, so this is the last chance to fix it.
- This greedy choice is optimal and unique.

This yields an O(n) solution.
*/

using System;

public class Solution
{
    /*
    Time Complexity: O(n)
    Space Complexity: O(1)

    Explanation of complexity:
    - We scan the even chain once and the odd chain once.
    - Together, every index is processed exactly one time.
    - We only store a few integer/boolean variables, so extra memory is constant.
    */
    public int MinOperations(int[] bits, int[] target)
    {
        // Basic safety check.
        // The problem guarantees equal lengths, but defensive programming is still helpful.
        if (bits == null || target == null || bits.Length != target.Length)
        {
            return -1;
        }

        int n = bits.Length;

        // Count the minimum operations needed for the even-index chain.
        int evenOps = SolveChain(bits, target, 0, n);

        // Count the minimum operations needed for the odd-index chain.
        int oddOps = SolveChain(bits, target, 1, n);

        // The two chains are independent, so the total minimum is simply the sum.
        return evenOps + oddOps;
    }

    private int SolveChain(int[] bits, int[] target, int start, int n)
    {
        // "flipped" tells us whether the current position has been toggled an odd number of times
        // by operations chosen earlier in this same parity chain.
        //
        // Why do we need this?
        // Because when we choose an index in this chain, it flips that index and all later indices
        // in the same chain. Instead of physically modifying many array elements every time
        // (which would be too slow), we keep a single parity flag:
        //
        // flipped = 0 means no net inversion currently applies
        // flipped = 1 means the current bit should be interpreted as inverted
        //
        // This is a classic optimization for suffix-flip problems.
        int flipped = 0;

        // This variable accumulates how many operations we perform on this chain.
        int operations = 0;

        // We move left to right through one parity chain:
        // start = 0 gives indices 0, 2, 4, ...
        // start = 1 gives indices 1, 3, 5, ...
        for (int i = start; i < n; i += 2)
        {
            // Compute the effective current bit after all previous flips in this chain.
            //
            // If flipped == 0, effectiveBit = bits[i]
            // If flipped == 1, effectiveBit = bits[i] ^ 1, which toggles 0<->1
            int effectiveBit = bits[i] ^ flipped;

            // If the effective bit already matches the target, we do nothing.
            // Why is doing nothing correct?
            // Because flipping here would change this position away from the target,
            // and later operations cannot come back to fix this exact position
            // (later operations only affect later indices in the chain).
            if (effectiveBit == target[i])
            {
                continue;
            }

            // If the effective bit does NOT match the target, then we MUST flip here.
            //
            // Why is this mandatory?
            // - This is the last operation that can affect index i in this chain.
            // - Any operation at a later same-parity index will not affect i.
            // Therefore, if i is wrong now, the only way to fix it is to toggle at i.
            operations++;

            // Toggling at i flips the suffix of this chain from i onward.
            // Instead of updating all those elements explicitly, we just invert the flip state.
            flipped ^= 1;
        }

        return operations;
    }
}

// ------------------------------------------------------------
// Demo code
// ------------------------------------------------------------

var solution = new Solution();

// Example 1 from the prompt:
// bits   = [1,0,1,1,0]
// target = [0,0,0,1,1]
//
// Check manually:
// - Even chain indices 0,2,4: [1,1,0] -> [0,0,1]
//   Flip at chain position index 0 once, and all three become correct.
// - Odd chain indices 1,3: [0,1] -> [0,1], already correct.
// Total = 1
int[] bits1 = { 1, 0, 1, 1, 0 };
int[] target1 = { 0, 0, 0, 1, 1 };
Console.WriteLine(solution.MinOperations(bits1, target1)); // Expected: 1

// Example 2 from the prompt:
// bits   = [0,1,0,1]
// target = [1,0,1,0]
//
// Even chain 0,2: [0,0] -> [1,1] => one flip at index 0
// Odd chain 1,3:  [1,1] -> [0,0] => one flip at index 1
// Total = 2
int[] bits2 = { 0, 1, 0, 1 };
int[] target2 = { 1, 0, 1, 0 };
Console.WriteLine(solution.MinOperations(bits2, target2)); // Expected: 2

// Additional demo 1: already equal, answer should be 0
int[] bits3 = { 1, 0, 1, 0, 1, 0 };
int[] target3 = { 1, 0, 1, 0, 1, 0 };
Console.WriteLine(solution.MinOperations(bits3, target3)); // Expected: 0

// Additional demo 2: single element
int[] bits4 = { 0 };
int[] target4 = { 1 };
Console.WriteLine(solution.MinOperations(bits4, target4)); // Expected: 1

// Additional demo 3: mixed case
int[] bits5 = { 1, 1, 0, 0, 1, 1 };
int[] target5 = { 0, 1, 1, 0, 0, 1 };
Console.WriteLine(solution.MinOperations(bits5, target5));