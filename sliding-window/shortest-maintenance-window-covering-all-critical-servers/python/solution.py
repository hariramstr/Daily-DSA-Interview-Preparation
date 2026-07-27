"""
Title: Shortest Maintenance Window Covering All Critical Servers

Problem Description:
A data center records a time-ordered stream of server IDs representing which server
emitted the most recent heartbeat at each second. You are given an integer array
events, where events[i] is the server ID seen at second i, and an integer array
critical containing distinct server IDs that must all be observed during a
maintenance audit.

Your task is to find the length of the shortest contiguous time window in events
that contains every server in critical at least once. However, there is an
additional reliability rule: within the chosen window, no non-critical server ID
is allowed to appear more than L times. If no such window exists, return -1.

Formally, find the minimum value of (right - left + 1) such that the subarray
events[left...right] satisfies both conditions:
1. Every server ID in critical appears at least once in the window.
2. For every server ID x not in critical, its frequency inside the window is at most L.

Constraints:
- 1 <= events.length <= 200000
- 1 <= critical.length <= min(100000, events.length)
- 1 <= events[i], critical[i] <= 10^9
- All values in critical are distinct
- 0 <= L <= events.length
"""

from typing import Dict, List, Set


class Solution:
    def shortest_maintenance_window(
        self, events: List[int], critical: List[int], L: int
    ) -> int:
        """
        Find the length of the shortest contiguous subarray that:
        1) contains every critical server at least once, and
        2) does not contain any non-critical server more than L times.

        Args:
            events: Time-ordered list of observed server IDs.
            critical: Distinct server IDs that must all appear in the chosen window.
            L: Maximum allowed frequency for each non-critical server inside the window.

        Returns:
            The minimum valid window length, or -1 if no valid window exists.

        Time complexity:
            O(n), where n is len(events), because each pointer moves at most n times.

        Space complexity:
            O(k), where k is the number of distinct values tracked in the current window.
        """
        # Convert the list of required server IDs into a set so membership checks
        # are O(1) on average. We need to ask very frequently:
        # "Is this server critical or not?"
        critical_set: Set[int] = set(critical)

        # Total number of distinct critical IDs that must be present in a valid window.
        required_count: int = len(critical_set)

        # This dictionary stores frequencies of critical IDs currently inside the window.
        # We only track critical IDs here because for the "all critical present" rule,
        # we only care whether each required ID appears at least once.
        critical_freq: Dict[int, int] = {}

        # This dictionary stores frequencies of non-critical IDs currently inside the window.
        # We separate it from critical_freq to keep the logic clear:
        # - critical IDs are used to satisfy coverage
        # - non-critical IDs are used to enforce the reliability limit L
        non_critical_freq: Dict[int, int] = {}

        # Number of distinct critical IDs currently present in the window with frequency >= 1.
        # When this equals required_count, the window covers all critical servers.
        formed: int = 0

        # Number of non-critical IDs that currently violate the rule "frequency <= L".
        # If this value is 0, then the reliability rule is satisfied.
        #
        # Why count violators instead of checking all non-critical frequencies each time?
        # Because scanning all frequencies repeatedly would be too slow.
        # Instead, we update this count incrementally whenever a frequency crosses
        # the threshold L.
        violating_non_critical: int = 0

        # Left boundary of the sliding window.
        left: int = 0

        # Best answer found so far. Start with infinity to mean "not found yet".
        best: int = float("inf")

        # Expand the window by moving the right boundary one step at a time.
        for right, server_id in enumerate(events):
            # Step 1: Add events[right] into the current window.
            if server_id in critical_set:
                # This is a critical server.
                # Increase its count in the critical frequency map.
                new_count: int = critical_freq.get(server_id, 0) + 1
                critical_freq[server_id] = new_count

                # If its count became 1, that means this critical server was previously
                # missing from the window and is now covered.
                if new_count == 1:
                    formed += 1
            else:
                # This is a non-critical server.
                # Increase its count in the non-critical frequency map.
                new_count = non_critical_freq.get(server_id, 0) + 1
                non_critical_freq[server_id] = new_count

                # If the count just became L + 1, then this server has started violating
                # the reliability rule. We increase the number of violating IDs by 1.
                #
                # Important:
                # - count <= L is allowed
                # - count == L + 1 is the first invalid count
                if new_count == L + 1:
                    violating_non_critical += 1

            # Step 2: If the window is invalid because some non-critical server appears
            # too many times, shrink from the left until the reliability rule is restored.
            #
            # We do this first because any candidate answer must satisfy BOTH conditions.
            while violating_non_critical > 0:
                left_server: int = events[left]

                if left_server in critical_set:
                    # Removing a critical server from the left.
                    old_count = critical_freq[left_server]
                    new_left_count = old_count - 1

                    if new_left_count == 0:
                        # This critical server is no longer present in the window.
                        # Therefore the window loses coverage for one required ID.
                        del critical_freq[left_server]
                        formed -= 1
                    else:
                        critical_freq[left_server] = new_left_count
                else:
                    # Removing a non-critical server from the left.
                    old_count = non_critical_freq[left_server]
                    new_left_count = old_count - 1

                    # If the old count was L + 1, then this server was violating before
                    # removal, and after decrement it becomes exactly L, which is valid.
                    # So the number of violating IDs decreases by 1.
                    if old_count == L + 1:
                        violating_non_critical -= 1

                    if new_left_count == 0:
                        del non_critical_freq[left_server]
                    else:
                        non_critical_freq[left_server] = new_left_count

                # Move the left boundary rightward after removing that element.
                left += 1

            # Step 3: At this point, the reliability rule is satisfied.
            # If we also cover all critical servers, then the current window is valid.
            #
            # Now we try to shrink it greedily from the left while it remains valid,
            # because for a fixed right boundary, the shortest valid window is always
            # obtained by pushing left as far right as possible.
            while formed == required_count and violating_non_critical == 0:
                # Update the best answer using the current valid window.
                current_length: int = right - left + 1
                if current_length < best:
                    best = current_length

                # Try removing events[left] to see whether we can keep validity
                # and get an even shorter window.
                left_server = events[left]

                if left_server in critical_set:
                    old_count = critical_freq[left_server]
                    new_left_count = old_count - 1

                    if new_left_count == 0:
                        # Removing this element causes one required critical server
                        # to disappear from the window, so coverage breaks.
                        del critical_freq[left_server]
                        formed -= 1
                    else:
                        critical_freq[left_server] = new_left_count
                else:
                    old_count = non_critical_freq[left_server]
                    new_left_count = old_count - 1

                    # Because the window is already valid here, old_count cannot be > L.
                    # So removing a non-critical element cannot create a violation.
                    if new_left_count == 0:
                        del non_critical_freq[left_server]
                    else:
                        non_critical_freq[left_server] = new_left_count

                left += 1

        # If best was never updated, then no valid window exists.
        return -1 if best == float("inf") else best


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    events1 = [7, 2, 9, 2, 5, 7, 3, 9, 5]
    critical1 = [2, 5, 9]
    L1 = 1
    result1 = solution.shortest_maintenance_window(events1, critical1, L1)
    print("Example 1 Result:", result1)  # Expected: 3

    # Example 2
    events2 = [4, 8, 1, 8, 6, 4, 2, 6, 1]
    critical2 = [1, 2, 6]
    L2 = 0
    result2 = solution.shortest_maintenance_window(events2, critical2, L2)
    print("Example 2 Result:", result2)  # Expected: 3

    # Additional quick checks
    events3 = [1, 2, 3]
    critical3 = [1, 3]
    L3 = 0
    result3 = solution.shortest_maintenance_window(events3, critical3, L3)
    print("Additional Check 1:", result3)  # Expected: -1 because 2 is non-critical and cannot appear

    events4 = [5, 1, 2, 1, 3, 2]
    critical4 = [1, 2, 3]
    L4 = 1
    result4 = solution.shortest_maintenance_window(events4, critical4, L4)
    print("Additional Check 2:", result4)  # Expected: 4 ([1,3,2] not contiguous with all; shortest valid is [2,1,3,2] or [1,2,1,3])