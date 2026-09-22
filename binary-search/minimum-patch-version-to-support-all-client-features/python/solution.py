"""
Title: Minimum Patch Version to Support All Client Features

Problem Description:
A software platform publishes patch versions in increasing order from 1 to n.
For each patch version i, you are given an integer compatibility score patches[i],
where a larger score means that version supports a wider set of client-side features.
The array is guaranteed to be non-decreasing, because later patches never remove
previously supported features.

You are also given several client requirements. Each requirement is a target
compatibility score target. For every target, return the smallest patch version
index (1-indexed) whose compatibility score is greater than or equal to target.
If no patch version can satisfy the target, return -1 for that query.

Your task is to implement a function that answers all queries efficiently.

This is not a simple linear scan problem: the number of patch versions and queries
can both be large, so an efficient binary search solution is expected. Since the
compatibility scores are sorted in non-decreasing order, you should search for the
leftmost version that satisfies each target.

Constraints:
- 1 <= n <= 200000
- 1 <= q <= 200000
- 0 <= patches[i] <= 1000000000
- patches is non-decreasing
- 0 <= target <= 1000000000

Example 1:
Input: patches = [2, 4, 4, 7, 10], queries = [4, 5, 10, 11]
Output: [2, 4, 5, -1]

Example 2:
Input: patches = [0, 0, 3, 3, 8], queries = [0, 1, 3, 6]
Output: [1, 3, 3, 5]
"""

from typing import List


class Solution:
    def lower_bound(self, patches: List[int], target: int) -> int:
        """
        Find the first 1-indexed patch version whose compatibility score
        is greater than or equal to the given target.

        Args:
            patches: A non-decreasing list of compatibility scores.
            target: The required compatibility score.

        Returns:
            The smallest 1-indexed position where patches[position - 1] >= target.
            Returns -1 if no such position exists.

        Time complexity:
            O(log n), where n is the number of patch versions.

        Space complexity:
            O(1), because only a few variables are used.
        """
        # We perform a classic binary search for the "leftmost valid position".
        #
        # Why binary search works:
        # - The array is sorted in non-decreasing order.
        # - That means once we find a value >= target, every value to its right
        #   is also >= target or at least not smaller in a way that breaks ordering.
        # - So we can safely discard half of the search space each step.
        #
        # We use two pointers:
        # - left: the beginning of the current search range
        # - right: the end of the current search range
        #
        # Both are 0-indexed because Python lists are 0-indexed.
        left: int = 0
        right: int = len(patches) - 1

        # This variable will store the best candidate index found so far.
        # We initialize it to -1 to mean "not found yet".
        answer_index: int = -1

        # Continue searching while the search range is valid.
        while left <= right:
            # Compute the middle index safely.
            # In Python, overflow is not a practical issue for integers,
            # but this formula is still the standard good practice.
            mid: int = left + (right - left) // 2

            # If the middle value is large enough, it is a valid candidate.
            if patches[mid] >= target:
                # Record this index as a possible answer.
                answer_index = mid

                # But we are not done yet:
                # we want the *smallest* index that satisfies the condition.
                # So we continue searching on the LEFT side to see if there is
                # an earlier patch version that also works.
                right = mid - 1
            else:
                # If patches[mid] < target, then this position is too small.
                # Because the array is sorted, every position to the left of mid
                # is also <= patches[mid], so none of them can satisfy the target.
                # Therefore, we move to the RIGHT half.
                left = mid + 1

        # If answer_index stayed -1, no patch version satisfies the target.
        if answer_index == -1:
            return -1

        # Convert from 0-indexed array position to 1-indexed patch version number.
        return answer_index + 1

    def minimum_patch_versions(self, patches: List[int], queries: List[int]) -> List[int]:
        """
        Answer all client compatibility queries using binary search.

        Args:
            patches: A non-decreasing list of patch compatibility scores.
            queries: A list of target compatibility scores.

        Returns:
            A list where each element is the smallest 1-indexed patch version
            whose score is >= the corresponding query target, or -1 if impossible.

        Time complexity:
            O(q log n), where:
            - n is the number of patch versions
            - q is the number of queries

        Space complexity:
            O(q) for the output list.
        """
        # This list will store the answer for each query in the same order
        # as the input queries, exactly as the problem requires.
        results: List[int] = []

        # Process each query independently.
        #
        # Why independently?
        # - Each query asks for the first patch version meeting a target.
        # - Since the patches array is fixed and sorted, binary search is ideal.
        # - We do not need extra complex data structures here because each query
        #   can be answered in O(log n), which is efficient enough for the constraints.
        for target in queries:
            # For the current target, find the leftmost patch version that works.
            version: int = self.lower_bound(patches, target)

            # Store the result.
            results.append(version)

        # Return all answers in the original query order.
        return results


if __name__ == "__main__":
    # Create an instance of the solution class.
    solution = Solution()

    # Example 1 from the problem statement.
    patches1: List[int] = [2, 4, 4, 7, 10]
    queries1: List[int] = [4, 5, 10, 11]
    result1: List[int] = solution.minimum_patch_versions(patches1, queries1)
    print("Example 1 Output:", result1)
    # Expected: [2, 4, 5, -1]

    # Manual correctness check for Example 1:
    # target 4  -> first value >= 4 is patches[1] = 4  -> version 2
    # target 5  -> first value >= 5 is patches[3] = 7  -> version 4
    # target 10 -> first value >= 10 is patches[4] = 10 -> version 5
    # target 11 -> no value >= 11 -> -1

    # Example 2 from the problem statement.
    patches2: List[int] = [0, 0, 3, 3, 8]
    queries2: List[int] = [0, 1, 3, 6]
    result2: List[int] = solution.minimum_patch_versions(patches2, queries2)
    print("Example 2 Output:", result2)
    # Expected: [1, 3, 3, 5]

    # Manual correctness check for Example 2:
    # target 0 -> first value >= 0 is patches[0] = 0 -> version 1
    # target 1 -> first value >= 1 is patches[2] = 3 -> version 3
    # target 3 -> first value >= 3 is patches[2] = 3 -> version 3
    # target 6 -> first value >= 6 is patches[4] = 8 -> version 5

    # Additional simple demonstration.
    patches3: List[int] = [1, 2, 2, 2, 9]
    queries3: List[int] = [2, 8, 9, 10]
    result3: List[int] = solution.minimum_patch_versions(patches3, queries3)
    print("Additional Example Output:", result3)
    # Expected: [2, 5, 5, -1]