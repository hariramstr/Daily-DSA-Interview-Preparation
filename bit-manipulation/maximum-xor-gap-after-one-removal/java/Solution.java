import java.util.*;

/*
Problem Title: Maximum XOR Gap After One Removal

Problem Description:
You are given an array of non-negative integers nums. Define the XOR gap of a set of numbers
as the maximum value of a XOR b over all distinct pairs (a, b) in that set. Your task is to
remove exactly one element from nums so that the XOR gap of the remaining elements is as large
as possible. Return that maximum possible XOR gap.

In other words, for each possible index i, imagine deleting nums[i], then compute the maximum
XOR of any two different remaining values. Among all choices of i, return the largest such result.

If after removing one element fewer than two numbers remain, the XOR gap is defined to be 0.

A brute-force solution that recomputes the best pair after every removal is too slow for large
inputs. A strong solution should take advantage of binary representations and shared prefixes
between numbers.

Constraints:
- 1 <= nums.length <= 10^5
- 0 <= nums[i] <= 10^9
- Values may repeat

Example 1:
Input: nums = [3, 10, 5, 25]
Output: 28
Explanation: Remove 10, leaving [3, 5, 25]. The best pair is 5 XOR 25 = 28, which is the
largest XOR gap achievable after one removal.

Example 2:
Input: nums = [8, 1, 2]
Output: 10
Explanation: If you remove 1, the remaining numbers are [8, 2], and their XOR is 10.
Removing any other element gives a smaller result.
*/

public class Solution {

    /**
     * Trie node used for binary trie operations.
     *
     * Each node stores:
     * - child[0]: next node for bit 0
     * - child[1]: next node for bit 1
     * - count: how many numbers currently pass through this node
     *
     * We use counts so that we can:
     * 1. Insert all numbers
     * 2. Temporarily remove one number
     * 3. Query the best XOR partner among the remaining numbers
     * 4. Add the removed number back
     */
    private static class TrieNode {
        TrieNode[] child = new TrieNode[2];
        int count;
    }

    /**
     * Binary trie that supports insertion, deletion, and maximum XOR query.
     *
     * We store all numbers using their binary representation from the highest relevant bit
     * down to bit 0. Since nums[i] <= 10^9, 30 bits are enough, but using bit 30 down to 0
     * is perfectly safe because 10^9 < 2^30.
     */
    private static class BinaryTrie {
        private static final int MAX_BIT = 30;
        private final TrieNode root = new TrieNode();

        /**
         * Inserts one number into the trie.
         *
         * @param num the number to insert
         * @return nothing
         * Time complexity: O(MAX_BIT) = O(31) = O(1)
         * Space complexity: O(MAX_BIT) in the worst case for newly created nodes
         */
        public void insert(int num) {
            TrieNode node = root;
            node.count++;
            for (int bit = MAX_BIT; bit >= 0; bit--) {
                int currentBit = (num >> bit) & 1;
                if (node.child[currentBit] == null) {
                    node.child[currentBit] = new TrieNode();
                }
                node = node.child[currentBit];
                node.count++;
            }
        }

        /**
         * Removes one occurrence of a number from the trie.
         *
         * This method assumes the number is currently present in the trie.
         * We only decrement counts; we do not physically delete nodes because
         * it is unnecessary for correctness and keeps the code simpler.
         *
         * @param num the number to remove
         * @return nothing
         * Time complexity: O(MAX_BIT) = O(31) = O(1)
         * Space complexity: O(1)
         */
        public void remove(int num) {
            TrieNode node = root;
            node.count--;
            for (int bit = MAX_BIT; bit >= 0; bit--) {
                int currentBit = (num >> bit) & 1;
                node = node.child[currentBit];
                node.count--;
            }
        }

        /**
         * Finds the maximum possible value of num XOR x where x is any number currently
         * stored in the trie.
         *
         * Greedy idea:
         * At each bit position, to maximize XOR, we prefer to go to the opposite bit
         * if such a branch exists and contains at least one number.
         *
         * Example:
         * If current bit of num is 0, then choosing a stored number with bit 1 at this
         * position makes this XOR bit equal to 1, which is better than 0.
         *
         * @param num the query number
         * @return the maximum XOR value achievable with num against any stored number
         * Time complexity: O(MAX_BIT) = O(31) = O(1)
         * Space complexity: O(1)
         */
        public int maxXorWith(int num) {
            if (root.count == 0) {
                return 0;
            }

            TrieNode node = root;
            int result = 0;

            for (int bit = MAX_BIT; bit >= 0; bit--) {
                int currentBit = (num >> bit) & 1;
                int preferredBit = 1 - currentBit;

                if (node.child[preferredBit] != null && node.child[preferredBit].count > 0) {
                    result |= (1 << bit);
                    node = node.child[preferredBit];
                } else {
                    node = node.child[currentBit];
                }
            }

            return result;
        }
    }

    /**
     * Computes the maximum XOR of any pair in the given array.
     *
     * This is the classic "maximum XOR of two numbers in an array" problem solved with a binary trie.
     *
     * Step-by-step:
     * 1. Insert all numbers into the trie.
     * 2. For each number, query the trie for the best possible XOR partner.
     * 3. Track the largest XOR value seen.
     *
     * Important note:
     * Querying against the full trie is safe even when the same value exists only once,
     * because num XOR num = 0, and the maximum pair XOR over the whole set is still correctly
     * discovered by considering all numbers. If duplicates exist, pairing equal values is also
     * allowed only when they come from different indices, and XOR remains 0 anyway.
     *
     * @param nums the array of numbers
     * @return the maximum XOR value among all distinct index pairs in nums
     * Time complexity: O(n * MAX_BIT) = O(n)
     * Space complexity: O(n * MAX_BIT) = O(n)
     */
    public int maximumPairXor(int[] nums) {
        if (nums == null || nums.length < 2) {
            return 0;
        }

        BinaryTrie trie = new BinaryTrie();

        for (int num : nums) {
            trie.insert(num);
        }

        int best = 0;
        for (int num : nums) {
            best = Math.max(best, trie.maxXorWith(num));
        }

        return best;
    }

    /**
     * Returns the maximum possible XOR gap after removing exactly one element.
     *
     * Key observation:
     * After removing one element, the remaining set is some subset of size n - 1.
     * We want the maximum pair XOR inside that remaining set.
     *
     * A very important simplification:
     * Let (a, b) be a pair of indices in the original array that achieves the global maximum
     * pair XOR among all pairs in the original array.
     *
     * If n >= 3, then we can remove any element that is NOT a or b. After that removal,
     * both a and b still remain, so their XOR value is still present in the remaining array.
     * Therefore, the best XOR gap after one removal is at least the original global maximum.
     *
     * On the other hand, removing an element cannot create a pair that did not already exist
     * in the original array. So the best XOR gap after one removal can never exceed the original
     * global maximum pair XOR.
     *
     * Therefore:
     * - If n < 3, after removing one element fewer than two numbers remain, so answer is 0
     *   when n == 1, and for n == 2 the remaining size is 1 so answer is also 0.
     * - If n >= 3, the answer is exactly the maximum pair XOR of the original array.
     *
     * This means the problem reduces to computing the maximum pair XOR once.
     *
     * Let's verify the examples:
     * Example 1: [3, 10, 5, 25]
     * Original maximum pair XOR is 5 XOR 25 = 28.
     * Since n = 4 >= 3, we can remove 10 (or 3) and keep 5 and 25.
     * Answer = 28.
     *
     * Example 2: [8, 1, 2]
     * Original maximum pair XOR is 8 XOR 2 = 10.
     * Since n = 3, remove 1 and keep 8 and 2.
     * Answer = 10.
     *
     * @param nums the input array
     * @return the maximum possible XOR gap after removing exactly one element
     * Time complexity: O(n * MAX_BIT) = O(n)
     * Space complexity: O(n * MAX_BIT) = O(n)
     */
    public int maximumXorGapAfterOneRemoval(int[] nums) {
        if (nums == null || nums.length <= 2) {
            return 0;
        }
        return maximumPairXor(nums);
    }

    /**
     * Demonstrates the solution on sample inputs and a few extra cases.
     *
     * @param args command-line arguments (unused)
     * @return nothing
     * Time complexity: O(total input size used in examples)
     * Space complexity: O(total trie size used in examples)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        int[] nums1 = {3, 10, 5, 25};
        int[] nums2 = {8, 1, 2};
        int[] nums3 = {7};
        int[] nums4 = {4, 9};
        int[] nums5 = {0, 0, 0, 0};
        int[] nums6 = {1, 2, 3, 4, 5};

        System.out.println(solution.maximumXorGapAfterOneRemoval(nums1)); // Expected: 28
        System.out.println(solution.maximumXorGapAfterOneRemoval(nums2)); // Expected: 10
        System.out.println(solution.maximumXorGapAfterOneRemoval(nums3)); // Expected: 0
        System.out.println(solution.maximumXorGapAfterOneRemoval(nums4)); // Expected: 0
        System.out.println(solution.maximumXorGapAfterOneRemoval(nums5)); // Expected: 0
        System.out.println(solution.maximumXorGapAfterOneRemoval(nums6)); // Example extra test
    }
}