```java
/*
 * Title: Balancing Bracket Distances
 * Difficulty: Medium
 * Topic: Two Pointers
 *
 * Problem Description:
 * You are given a string s consisting only of characters '(' and ')'. The string is
 * guaranteed to be a valid bracket sequence (i.e., every opening bracket has a matching
 * closing bracket and they are properly nested).
 *
 * For each matched pair of brackets (s[i], s[j]) where s[i] = '(' and s[j] = ')',
 * define the distance of that pair as j - i. Your task is to find the minimum possible
 * sum of distances across all matched pairs after you are allowed to swap any two
 * characters in the string at most once.
 *
 * Note: After the swap, the resulting string must still be a valid bracket sequence.
 * If no beneficial swap exists, return the original sum of distances.
 *
 * Constraints:
 * - 2 <= s.length <= 10^5
 * - s.length is even
 * - s consists only of '(' and ')'
 * - s is a valid bracket sequence
 *
 * Example 1:
 * Input: s = "(())"
 * Output: 4
 * Explanation: The matched pairs are (0,3) and (1,2) with distances 3 and 1,
 * giving a sum of 4. No valid swap reduces this sum further.
 *
 * Example 2:
 * Input: s = "()(())"
 * Output: 6
 * Explanation: Original matched pairs are (0,1), (2,5), (3,4) with distances 1, 3, 1
 * summing to 5. After swapping index 1 and 2 we get "(()()" — invalid. The optimal
 * valid configuration keeps the sum at 6 after evaluating all valid single swaps.
 */

import java.util.*;

/**
 * Solution class for the Balancing Bracket Distances problem.
 * 
 * Key Insight:
 * The sum of distances for a valid bracket sequence has a beautiful property:
 * Each character contributes to the sum based on its "depth" in the nesting.
 * 
 * For a valid bracket sequence, the sum of distances equals:
 * sum over all positions of: depth[i] * direction[i]
 * where direction is +1 for '(' and -1 for ')'
 * 
 * Actually, the simplest observation is:
 * Sum of distances = sum over all positions i of: (number of unmatched '(' to the left of i, including i if it's '(')
 * 
 * More precisely: for each position i, let balance[i] = number of '(' minus number of ')' in s[0..i].
 * Then sum of distances = sum of balance[i] for all i from 0 to n-2 (or equivalently, 
 * each '(' at depth d contributes d to the sum, and each ')' at depth d contributes d to the sum,
 * but that's not quite right either).
 *
 * The cleanest formulation: 
 * Sum of all pair distances = sum over i of balance[i] where balance[i] is the running 
 * count of open brackets minus close brackets after processing s[0..i-1].
 * 
 * This is because each open bracket at position i "opens" a contribution that lasts until
 * its matching close bracket.
 */
public class Solution {

    /**
     * Computes the sum of distances for all matched bracket pairs in a valid bracket sequence.
     * 
     * Key mathematical insight:
     * If we define balance[i] as the number of unmatched '(' after processing s[0..i-1]
     * (i.e., the running balance before position i), then:
     * sum of distances = sum of balance[i] for i from 1 to n-1
     * 
     * Why? Each matched pair (l, r) contributes (r - l) to the sum.
     * This equals the number of positions strictly between l and r, plus 1... 
     * Actually: each pair (l,r) contributes 1 for each position p where l <= p < r,
     * which is exactly the positions where this pair's '(' is still "open".
     * The balance at position p counts exactly the number of pairs that are "open" at p.
     * So summing balance gives us the total sum of distances.
     *
     * @param s the bracket string
     * @return sum of all matched pair distances
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public long computeDistanceSum(String s) {
        int n = s.length();
        long sum = 0;
        int balance = 0; // running count of '(' minus ')'
        
        for (int i = 0; i < n; i++) {
            // Before processing s[i], add current balance to sum
            // This counts how many open pairs "span" position i
            if (s.charAt(i) == '(') {
                // When we see '(', it opens a new pair
                // The balance BEFORE this position contributes
                sum += balance;
                balance++;
            } else {
                // When we see ')', it closes a pair
                balance--;
                // The balance AFTER decrement contributes
                sum += balance;
            }
        }
        
        return sum;
    }

    /**
     * Alternative direct computation: match brackets using a stack and sum distances.
     *
     * @param s the bracket string
     * @return sum of all matched pair distances
     * Time complexity: O(n)
     * Space complexity: O(n)
     */
    public long computeDistanceSumDirect(String s) {
        int n = s.length();
        long sum = 0;
        Deque<Integer> stack = new ArrayDeque<>();
        
        for (int i = 0; i < n; i++) {
            if (s.charAt(i) == '(') {
                stack.push(i);
            } else {
                int openIdx = stack.pop();
                sum += (i - openIdx);
            }
        }
        
        return sum;
    }

    /**
     * Checks whether a given string is a valid bracket sequence.
     *
     * @param s the string to check
     * @return true if valid, false otherwise
     * Time complexity: O(n)
     * Space complexity: O(1)
     */
    public boolean isValid(String s) {
        int balance = 0;
        for (char c : s.toCharArray()) {
            if (c == '(') balance++;
            else balance--;
            if (balance < 0) return false;
        }
        return balance == 0;
    }

    /**
     * Finds the minimum possible sum of distances after at most one swap of two characters,
     * ensuring the result is still a valid bracket sequence.
     *
     * Strategy:
     * A swap of two characters s[i] and s[j] (i < j) can only be meaningful if:
     * - s[i] != s[j] (otherwise swapping identical characters changes nothing)
     * 
     * The only meaningful swaps are:
     * Case 1: s[i] = ')' and s[j] = '(' — swap ')' at i with '(' at j
     *         This moves a ')' to the right and a '(' to the left.
     *         The change in sum = ?
     * Case 2: s[i] = '(' and s[j] = ')' — swap '(' at i with ')' at j
     *         This moves a '(' to the right and a ')' to the left.
     *         The change in sum = ?
     *
     * Mathematical analysis of sum change when swapping positions i and j (i < j):
     * 
     * The sum of distances = sum of balance[k] for k from 0 to n-1
     * where balance[k] is the running balance just BEFORE position k.
     * 
     * Wait, let me re-derive carefully using the formula:
     * sum = sum_{k=0}^{n-1} contribution(k)
     * where contribution(k) = balance_before_k if s[k]='(' 
     *                        = balance_after_k if s[k]=')'
     * which simplifies to: for each position k, add the balance at that position
     * (where balance at k = number of open brackets in s[0..k-1] minus close brackets)
     * 
     * Actually let me use the simpler formulation:
     * sum of distances = sum_{k=0}^{n-2} balance[k]
     * where balance[k] = (number of '(' in s[0..k]) - (number of ')' in s[0..k])
     * 
     * This is because each matched pair (l, r) contributes 1 to balance[k] for each k in [l, r-1].
     * Summing over all k gives sum of (r-l) = sum of distances.
     *
     * When we swap s[i] and s[j] (i < j, s[i]='(' s[j]=')' or vice versa):
     *
     * Case A: s[i]='(' becomes ')' and s[j]=')' becomes '('
     *   For positions k in [i, j-1]: balance[k] decreases by 2 (we removed a '(' and added a ')' before position k+1... 
     *   wait, let me think again.
     *
     * Let balance[k] = prefix sum of (+1 for '(', -1 for ')') up to and including index k.
     * sum of distances = sum_{k=0}^{n-2} balance[k]
     *
     * If we swap s[i]='(' with s[j]=')':
     *   - For k in [i, j-1]: balance[k] changes by -2 (we changed s[i] from +1 to -1, net -2)
     *   - For k >= j: balance[k] changes by 0 (both changes cancel: -2 from i, +2 from j)
     *   - For k < i: no change
     *   So delta = -2 * (j - i) ... but we need to check validity!
     *   Wait, this would always decrease the sum, but we need validity.
     *   
     *   After swap: s[i]=')' and s[j]='('
     *   For this to be valid: at no prefix should balance go negative.
     *   The critical constraint: balance[k] >= 0 for all k.
     *   After swap, balance[k] for k in [i, j-1] decreases by 2.
     *   So we need: original balance[k] - 2 >= 0 for all k in [i, j-1]
     *   i.e., min(balance[k] for k in [i, j-1]) >= 2
     *
     * If we swap s[i]=')' with s[j]='(':
     *   - For k in [i, j-1]: balance[k] changes by +2
     *   - This increases the sum by 2*(j-i), which is worse.
     *   So this type of swap never helps.
     *
     * Therefore, the only beneficial swaps are:
     * Find positions i < j where s[i]='(' and s[j]=')' such that:
     * - min(balance[k] for k in [i, j-1]) >= 2
     * - The decrease 2*(j-i) is maximized
     *
     * To maximize 2*(j-i), we want i as small as possible and j as large as possible.
     * 
     * Two-pointer approach:
     * Use two pointers, one from the left finding '(' positions and one from the right finding ')' positions,
     * and check validity of the swap.
     *
     * @param s the input valid bracket string
     * @return minimum possible sum of distances after at most one valid swap
     * Time complexity: O(n)
     * Space complexity: O(n) for the balance array
     */
    public long minDistanceSum(String s) {
        int n = s.length();
        
        // Step 1: Compute the original sum of distances
        long originalSum = computeDistanceSumDirect(s);
        
        // Step 2: Compute prefix balance array
        // balance[k] = number of '(' minus number of ')' in s[0..k]
        int[] balance = new int[n];
        balance[0] = (s.charAt(0) == '(') ? 1 : -1;
        for (int k = 1; k < n; k++) {
            balance[k] = balance[k-1] + (s.charAt(k) == '(' ? 1 : -1);
        }
        
        // Step 3: Compute range minimum query structure for balance array
        // We need min(balance[i..j-1]) efficiently
        // For simplicity, we'll use a sparse table for O(1) range min queries
        // But given n <= 10^5, we can also just use a simple approach
        
        // Build sparse table for range minimum queries on balance[0..n-1]
        int LOG = 17;
        int[][] sparse = new int[LOG][n];
        sparse[0] = balance.clone();
        for (int j = 1; j < LOG; j++) {
            for (int i = 0; i + (1 << j) <= n; i++) {
                sparse[j][i] = Math.min(sparse[j-1][i], sparse[j-1][i + (1 << (j-1))]);
            }
        }
        
        // Step 4: Try all candidate swaps using two pointers
        // We want to find i < j where s[i]='(' and s[j]=')' 
        // such that min(balance[i..j-1]) >= 2
        // and maximize (j - i)
        
        // Two-pointer strategy:
        // - left pointer starts from 0, looking for '('
        // - right pointer starts from n-1, looking for ')'
        // - Try to find the widest valid swap
        
        long bestDelta = 0; // best improvement (we want to maximize this)
        
        int left = 0;
        int right = n - 1;
        
        // Move left to first '('
        while (left < n && s.charAt(left) != '(') left++;
        // Move right to last ')'
        while (right >= 0 && s.charAt(right) != ')') right--;
        
        // Two pointer: try to find the best swap
        // We iterate: for each candidate left '(' position, find the rightmost ')' that makes a valid swap
        // Or: start with widest possible and narrow down
        
        // Let's try a greedy two-pointer:
        // Start with leftmost '(' and rightmost ')'
        // Check if swap is valid; if not, move pointers inward
        
        int l = 0, r = n - 1;
        
        // Find first '(' from left
        while (l < n && s.charAt(l) != '(') l++;
        // Find first ')' from right  
        while (r >= 0 && s.charAt(r) != ')') r--;
        
        while (l < r) {
            // Check if swapping s[l]='(' with s[r]=')' is valid
            // Validity condition: min(balance[l..r-1]) >= 2
            // After swap, balance in [l, r-1] decreases by 2, so we need original min >= 2
            
            if (s.charAt(l) == '(' && s.charAt(r) == ')') {
                // Query min balance in range [l, r-1]
                int minBal = queryMin(sparse, l, r - 1);
                
                if (minBal >= 2) {
                    // Valid swap! Improvement = 2 * (r - l)
                    long delta = 2L * (r - l);
                    if (delta > bestDelta) {
                        bestDelta = delta;
                    }
                    // Since we want maximum delta, and this is already the widest,
                    // we can break (but let's also try other combinations)
                    break;
                } else {
                    // Not valid, try moving one of the pointers
                    // Move the pointer