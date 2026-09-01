"""
Title: Count Mirror Inventory Code Pairs
Difficulty: Medium
Topic: Hashing

Problem Description:
You are given an array of product codes used in a warehouse system. Each code is a non-empty
lowercase string. Two codes form a mirror pair if one code can be transformed into the other by
reversing the order of its characters and then rotating the result by any number of positions,
including zero.

For example, the reverse of "abca" is "acba", and its rotations are:
"acba", "cbaa", "baac", and "aacb".
Any code equal to one of those strings forms a mirror pair with "abca".

Your task is to count how many unordered index pairs (i, j) with i < j are mirror pairs.

A straightforward O(n^2 * m) comparison is too slow when the input is large. You should design a
solution that uses hashing to build a canonical signature for each code so that equivalent codes
under this mirror rule are grouped together efficiently.

Return the total number of valid pairs.

Constraints:
- 1 <= codes.length <= 100000
- 1 <= codes[i].length <= 50
- codes[i] contains only lowercase English letters
- Only codes of the same length can form a mirror pair

Example 1:
Input: codes = ["abca", "cbaa", "zz", "zz", "aacb"]
Output: 3

Example 2:
Input: codes = ["abc", "cab", "bca", "xy", "yx", "aa"]
Output: 2
"""

from typing import Dict, List


class Solution:
    def _minimal_rotation(self, s: str) -> str:
        """
        Compute the lexicographically smallest rotation of a string.

        This method uses Booth's algorithm, which finds the starting index of the
        smallest rotation in linear time.

        Args:
            s: The input string.

        Returns:
            The lexicographically smallest rotation of s.

        Time complexity:
            O(len(s))

        Space complexity:
            O(len(s))
        """
        n: int = len(s)

        # A string of length 1 has only one rotation: itself.
        if n == 1:
            return s

        # We duplicate the string so every rotation appears as a contiguous substring
        # of length n inside this doubled string.
        doubled: str = s + s

        # i and j are candidate starting positions for the smallest rotation.
        # k is the offset used while comparing characters from those candidates.
        i: int = 0
        j: int = 1
        k: int = 0

        # Booth's algorithm:
        # Repeatedly compare rotations starting at i and j.
        # When a mismatch is found, we can discard one whole range of candidates.
        while i < n and j < n and k < n:
            a: str = doubled[i + k]
            b: str = doubled[j + k]

            if a == b:
                # Characters match, so continue comparing the next position.
                k += 1
                continue

            if a > b:
                # Rotation at i is worse than rotation at j.
                # Therefore, any start between i and i + k cannot be minimal.
                i = i + k + 1
                if i <= j:
                    i = j + 1
            else:
                # Rotation at j is worse than rotation at i.
                # Therefore, any start between j and j + k cannot be minimal.
                j = j + k + 1
                if j <= i:
                    j = i + 1

            # Reset comparison offset after eliminating candidates.
            k = 0

        # The smaller of i and j is the start index of the minimal rotation.
        start: int = min(i, j)
        return doubled[start:start + n]

    def _canonical_signature(self, code: str) -> str:
        """
        Build a canonical signature for a code under the mirror rule.

        Two strings are mirror-equivalent if one is a rotation of the reverse of the other.
        This is equivalent to saying:
        - reverse(a) is rotation-equivalent to b
        - therefore a is rotation-equivalent to reverse(b)

        A very useful consequence is:
        If we reverse a string and then take the minimal rotation, every string in the same
        mirror-equivalence class will produce the same canonical signature.

        Args:
            code: The input product code.

        Returns:
            A canonical string signature shared by all mirror-equivalent codes.

        Time complexity:
            O(len(code))

        Space complexity:
            O(len(code))
        """
        # Step 1: Reverse the code because the problem definition says
        # "reverse first, then rotate".
        reversed_code: str = code[::-1]

        # Step 2: Among all rotations of the reversed string, choose the
        # lexicographically smallest one. This gives us one stable representative
        # for the entire equivalence class.
        return self._minimal_rotation(reversed_code)

    def count_mirror_pairs(self, codes: List[str]) -> int:
        """
        Count unordered index pairs that form mirror pairs.

        The key idea is to convert each code into a canonical signature:
        - reverse the code
        - reduce all rotations of that reversed string to one minimal representative

        Then, any two codes that belong to the same mirror-equivalence class will
        have the same signature. We can count pairs by grouping equal signatures.

        Args:
            codes: List of product codes.

        Returns:
            The total number of unordered mirror pairs.

        Time complexity:
            O(n * m), where n is the number of codes and m is the maximum code length.
            More precisely, each code is processed in linear time in its length.

        Space complexity:
            O(n * m) in the worst case for storing signatures in the hash map.
        """
        # This dictionary maps:
        #   canonical_signature -> how many times we have seen it so far
        #
        # Why a dictionary?
        # Because we need fast grouping/counting by signature.
        # Python dictionaries provide average O(1) insertion and lookup.
        signature_count: Dict[str, int] = {}

        # This will accumulate the total number of valid unordered pairs.
        total_pairs: int = 0

        # Process codes one by one.
        for code in codes:
            # Build the canonical signature for the current code.
            signature: str = self._canonical_signature(code)

            # If we have already seen this signature k times, then the current code
            # forms exactly k new pairs with those previous codes.
            #
            # Example:
            # signatures seen so far for "zz" class = 1
            # current "zz" arrives -> contributes 1 new pair
            previous_count: int = signature_count.get(signature, 0)
            total_pairs += previous_count

            # Record that we have now seen one more code with this signature.
            signature_count[signature] = previous_count + 1

        return total_pairs


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    codes1: List[str] = ["abca", "cbaa", "zz", "zz", "aacb"]
    result1: int = solution.count_mirror_pairs(codes1)
    print("Example 1 result:", result1)  # Expected: 3

    # Example 2
    codes2: List[str] = ["abc", "cab", "bca", "xy", "yx", "aa"]
    result2: int = solution.count_mirror_pairs(codes2)
    print("Example 2 result:", result2)  # Expected: 2

    # Additional quick checks
    codes3: List[str] = ["a", "a", "b"]
    result3: int = solution.count_mirror_pairs(codes3)
    print("Additional test 1 result:", result3)  # Expected: 1

    codes4: List[str] = ["abc", "acb"]
    result4: int = solution.count_mirror_pairs(codes4)
    print("Additional test 2 result:", result4)  # Expected: 1

    codes5: List[str] = ["abc", "bca", "cab"]
    result5: int = solution.count_mirror_pairs(codes5)
    print("Additional test 3 result:", result5)  # Expected: 0)