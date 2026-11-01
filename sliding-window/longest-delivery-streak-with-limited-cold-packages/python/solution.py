"""
Title: Longest Delivery Streak With Limited Cold Packages

Problem Description:
A courier company records deliveries in the order they are loaded onto a truck.
Each delivery is labeled with a category:
- 'N' for normal packages
- 'C' for cold-chain packages that require expensive refrigerated handling

The company wants to find the longest consecutive stretch of deliveries that can be
assigned to a standard truck, as long as that stretch contains at most k cold-chain
packages.

Given a string deliveries where each character is either 'N' or 'C', and an integer k,
return the length of the longest contiguous substring that contains at most k
occurrences of 'C'.

This is a realistic scheduling problem: the deliveries must remain in their original
order, and you may only choose one contiguous block. A block with more than k
cold-chain packages is invalid because it would exceed the truck's refrigeration
allowance.

Constraints:
- 1 <= deliveries.length <= 200000
- deliveries[i] is either 'N' or 'C'
- 0 <= k <= deliveries.length

Examples:
1)
Input: deliveries = "NCCNNNCN", k = 2
Output: 6

2)
Input: deliveries = "CCCCNNC", k = 1
Output: 3
"""

from typing import List, Tuple


class Solution:
    def longest_delivery_streak(self, deliveries: str, k: int) -> int:
        """
        Find the length of the longest contiguous substring containing at most k 'C' characters.

        Args:
            deliveries: A string of 'N' and 'C' characters representing package categories.
            k: Maximum allowed number of cold-chain packages ('C') in the chosen block.

        Returns:
            The maximum length of a contiguous valid block.

        Time Complexity:
            O(n), where n is the length of deliveries.
            Each character is processed at most twice:
            - once when the right pointer expands
            - once when the left pointer contracts

        Space Complexity:
            O(1), because only a few integer variables are used regardless of input size.
        """
        # We use the classic "sliding window" technique.
        #
        # Main idea:
        # - Maintain a window deliveries[left:right+1]
        # - Track how many 'C' characters are currently inside that window
        # - Expand the window by moving right forward one step at a time
        # - If the window becomes invalid (too many 'C'), shrink it from the left
        # - At every valid step, record the largest window size seen so far
        #
        # Why this works:
        # - We need a contiguous substring, which naturally suggests a window
        # - We only care whether the count of 'C' is <= k
        # - When the count exceeds k, the only way to restore validity while keeping
        #   the substring contiguous is to move the left boundary rightward
        #
        # This avoids checking all substrings, which would be far too slow.

        left: int = 0
        cold_count: int = 0
        max_length: int = 0

        # Move the right pointer across the string one character at a time.
        for right in range(len(deliveries)):
            # Include the new character at position "right" into the current window.
            if deliveries[right] == "C":
                cold_count += 1

            # If the window now contains too many cold packages, it is invalid.
            # We must shrink from the left until the window becomes valid again.
            #
            # Important detail:
            # We use a while loop, not an if statement, because removing just one
            # character may still leave too many 'C' characters in the window.
            while cold_count > k:
                # Before moving left forward, remove the effect of deliveries[left]
                # from our window statistics.
                if deliveries[left] == "C":
                    cold_count -= 1

                # Actually shrink the window.
                left += 1

            # At this point, the window deliveries[left:right+1] is guaranteed valid:
            # it contains at most k 'C' characters.
            current_length: int = right - left + 1

            # Update the best answer found so far.
            if current_length > max_length:
                max_length = current_length

        return max_length

    def solve_examples(self) -> List[Tuple[str, int, int]]:
        """
        Run the algorithm on the problem's sample examples.

        Args:
            None

        Returns:
            A list of tuples in the form:
            (deliveries, k, computed_answer)

        Time Complexity:
            O(total length of all example strings)

        Space Complexity:
            O(number of examples) for storing the returned results
        """
        examples: List[Tuple[str, int]] = [
            ("NCCNNNCN", 2),
            ("CCCCNNC", 1),
        ]

        results: List[Tuple[str, int, int]] = []
        for deliveries, k in examples:
            answer: int = self.longest_delivery_streak(deliveries, k)
            results.append((deliveries, k, answer))
        return results


if __name__ == "__main__":
    solution = Solution()

    # Sample inputs from the problem statement.
    sample_deliveries_1: str = "NCCNNNCN"
    sample_k_1: int = 2
    result_1: int = solution.longest_delivery_streak(sample_deliveries_1, sample_k_1)
    print(f"deliveries = {sample_deliveries_1}, k = {sample_k_1} -> {result_1}")

    sample_deliveries_2: str = "CCCCNNC"
    sample_k_2: int = 1
    result_2: int = solution.longest_delivery_streak(sample_deliveries_2, sample_k_2)
    print(f"deliveries = {sample_deliveries_2}, k = {sample_k_2} -> {result_2}")

    # Additional beginner-friendly checks.
    extra_deliveries_1: str = "NNNNN"
    extra_k_1: int = 0
    extra_result_1: int = solution.longest_delivery_streak(extra_deliveries_1, extra_k_1)
    print(f"deliveries = {extra_deliveries_1}, k = {extra_k_1} -> {extra_result_1}")

    extra_deliveries_2: str = "CNCNCN"
    extra_k_2: int = 2
    extra_result_2: int = solution.longest_delivery_streak(extra_deliveries_2, extra_k_2)
    print(f"deliveries = {extra_deliveries_2}, k = {extra_k_2} -> {extra_result_2}")