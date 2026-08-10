"""
Title: Find the Earliest Repeated Access Pattern

Problem Description:
A security system records each employee's building access events as an array of lowercase
strings, where each string is a room code visited in order during one day. You are also
given an integer windowSize. For every contiguous block of exactly windowSize room codes,
define its access pattern as the ordered sequence of those room codes. Your task is to
return the starting index of the earliest window whose exact access pattern appears again
later in the array. If multiple windows repeat, choose the one with the smallest starting
index. If no length-windowSize pattern appears at least twice, return -1.

Two windows are considered the same only if they have the same length and every position
contains the same room code. Overlapping windows are allowed. For example, with
windowSize = 3, the windows starting at indices 1 and 3 may match even if they overlap.

Design an efficient solution using hashing so that large inputs can be processed quickly.
A naive comparison of every pair of windows will be too slow.

Constraints:
- 1 <= accessLog.length <= 100000
- 1 <= windowSize <= accessLog.length
- accessLog[i] consists of lowercase English letters
- The total number of characters across all room codes is at most 200000
"""

from typing import Dict, List, Tuple


class Solution:
    def earliest_repeated_access_pattern(self, accessLog: List[str], windowSize: int) -> int:
        """
        Find the smallest starting index of a length-windowSize subarray that appears again later.

        This method uses a rolling hash over integer IDs assigned to room-code strings.
        To make collisions extremely unlikely, it uses double hashing. When a hash match
        is found, the actual window contents are compared to guarantee correctness.

        Args:
            accessLog: List of room codes visited in order.
            windowSize: Exact size of each contiguous window to compare.

        Returns:
            The earliest starting index whose window appears again later, or -1 if none repeat.

        Time complexity:
            O(n) average, where n is len(accessLog), because each window hash is processed once.
            In the rare event of hash collisions, extra comparisons may occur.

        Space complexity:
            O(n) for storing first-seen hashes and integer-mapped room codes.
        """
        # Store the number of room visits for easier repeated use.
        n: int = len(accessLog)

        # If the window size is larger than the array length, no valid window exists.
        # The constraints say windowSize <= n, but this guard keeps the method robust.
        if windowSize > n:
            return -1

        # If windowSize is exactly n, there is only one possible window.
        # A pattern must appear at least twice, so repetition is impossible.
        if windowSize == n:
            return -1

        # ---------------------------------------------------------------------
        # Step 1: Convert each room-code string into a compact integer ID.
        #
        # Why do this?
        # - Rolling hash works more naturally and efficiently on integers.
        # - Strings can be long, and comparing or hashing tuples of strings repeatedly
        #   would be slower and use more memory.
        # - Equal strings receive equal IDs, preserving exact pattern equality.
        # ---------------------------------------------------------------------
        string_to_id: Dict[str, int] = {}
        ids: List[int] = []
        next_id: int = 1

        for room in accessLog:
            if room not in string_to_id:
                string_to_id[room] = next_id
                next_id += 1
            ids.append(string_to_id[room])

        # ---------------------------------------------------------------------
        # Step 2: Prepare double rolling-hash parameters.
        #
        # We use two large moduli and one base. Double hashing dramatically reduces
        # collision risk compared with a single hash.
        #
        # Hash formula for a window [x0, x1, ..., x(k-1)]:
        #   H = x0 * base^(k-1) + x1 * base^(k-2) + ... + x(k-1)
        #
        # Rolling update when sliding one step:
        #   Remove outgoing value contribution,
        #   multiply remaining hash by base,
        #   add incoming value.
        # ---------------------------------------------------------------------
        base: int = 911382323
        mod1: int = 1_000_000_007
        mod2: int = 1_000_000_009

        # Precompute base^(windowSize - 1) under both moduli.
        # This is needed to remove the outgoing element's contribution efficiently.
        highest_power_1: int = pow(base, windowSize - 1, mod1)
        highest_power_2: int = pow(base, windowSize - 1, mod2)

        # ---------------------------------------------------------------------
        # Step 3: Build the hash of the first window of length windowSize.
        #
        # We compute it incrementally:
        #   current_hash = current_hash * base + new_value
        # under each modulus.
        # ---------------------------------------------------------------------
        hash1: int = 0
        hash2: int = 0

        for i in range(windowSize):
            hash1 = (hash1 * base + ids[i]) % mod1
            hash2 = (hash2 * base + ids[i]) % mod2

        # ---------------------------------------------------------------------
        # Step 4: Track the first index where each window hash appears.
        #
        # Key idea:
        # - We only care about the earliest starting index for each distinct pattern.
        # - If the same pattern appears later, we should return the earliest one.
        #
        # So for each hash pair, we store only the first index where it was seen.
        # ---------------------------------------------------------------------
        first_seen: Dict[Tuple[int, int], int] = {(hash1, hash2): 0}

        # This variable stores the best answer found so far.
        # We want the minimum starting index among all repeated windows.
        answer: int = -1

        # ---------------------------------------------------------------------
        # Step 5: Slide the window across the array using rolling hash.
        #
        # For each new window starting at index start:
        # - outgoing element is ids[start - 1]
        # - incoming element is ids[start + windowSize - 1]
        #
        # We update both hashes in O(1).
        # ---------------------------------------------------------------------
        for start in range(1, n - windowSize + 1):
            outgoing: int = ids[start - 1]
            incoming: int = ids[start + windowSize - 1]

            # Remove the outgoing value's highest-place contribution.
            hash1 = (hash1 - outgoing * highest_power_1) % mod1
            hash2 = (hash2 - outgoing * highest_power_2) % mod2

            # Shift the remaining sequence left by one position in polynomial terms.
            hash1 = (hash1 * base) % mod1
            hash2 = (hash2 * base) % mod2

            # Add the incoming value at the lowest-place position.
            hash1 = (hash1 + incoming) % mod1
            hash2 = (hash2 + incoming) % mod2

            key: Tuple[int, int] = (hash1, hash2)

            # -----------------------------------------------------------------
            # If this hash pair has been seen before, we may have found a repeated
            # pattern. Because hashes can theoretically collide, we verify by
            # comparing the actual integer-ID windows.
            #
            # This verification guarantees correctness.
            # -----------------------------------------------------------------
            if key in first_seen:
                previous_start: int = first_seen[key]

                # Compare the two windows exactly.
                # Since ids preserve exact string equality, matching ID slices means
                # the original room-code sequences are identical.
                if ids[previous_start:previous_start + windowSize] == ids[start:start + windowSize]:
                    # We found a repeated pattern. The candidate answer is the first
                    # occurrence index, because the problem asks for the earliest
                    # window whose pattern appears again later.
                    if answer == -1 or previous_start < answer:
                        answer = previous_start

                        # Best possible answer is 0, so we can stop early.
                        if answer == 0:
                            return 0
                else:
                    # Hash collision case:
                    # We do nothing here because this current window does not match
                    # the previously stored first occurrence exactly.
                    #
                    # We intentionally keep the earliest stored index for this hash.
                    # Even if collisions happen, correctness is preserved because we
                    # always verify before accepting a match.
                    pass
            else:
                # First time seeing this hash pair, so store its starting index.
                first_seen[key] = start

        return answer

    def solve(self, accessLog: List[str], windowSize: int) -> int:
        """
        Wrapper method that calls the main algorithm.

        Args:
            accessLog: List of room codes visited in order.
            windowSize: Exact size of each contiguous window.

        Returns:
            Earliest starting index of a repeated access pattern, or -1 if none exists.

        Time complexity:
            O(n) average.

        Space complexity:
            O(n).
        """
        return self.earliest_repeated_access_pattern(accessLog, windowSize)


if __name__ == "__main__":
    solution = Solution()

    # Example 1:
    # Windows of size 3:
    # index 0 -> ["lab", "hall", "vault"]
    # index 1 -> ["hall", "vault", "lab"]
    # index 2 -> ["vault", "lab", "hall"]
    # index 3 -> ["lab", "hall", "vault"]
    #
    # The pattern at index 0 repeats at index 3, so the answer is 0.
    accessLog1: List[str] = ["lab", "hall", "vault", "lab", "hall", "vault", "exit"]
    windowSize1: int = 3
    print(solution.solve(accessLog1, windowSize1))  # Expected: 0

    # Example 2:
    # Windows of size 2:
    # index 0 -> ["a", "b"]
    # index 1 -> ["b", "a"]
    # index 2 -> ["a", "b"]
    # index 3 -> ["b", "c"]
    #
    # The pattern at index 0 repeats at index 2, so the answer is 0.
    accessLog2: List[str] = ["a", "b", "a", "b", "c"]
    windowSize2: int = 2
    print(solution.solve(accessLog2, windowSize2))  # Expected: 0

    # Additional sample with no repeated window.
    accessLog3: List[str] = ["x", "y", "z", "w"]
    windowSize3: int = 2
    print(solution.solve(accessLog3, windowSize3))  # Expected: -1