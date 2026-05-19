```python
"""
Title: Cyclic Shift of Linked List Segments
Difficulty: Hard
Topic: Linked Lists

Problem Description:
You are given the head of a singly linked list and a list of segment descriptors.
Each descriptor is a tuple (left, right, k) meaning: rotate the sublist from position
left to position right (1-indexed, inclusive) to the right by k positions.
You must apply all segment rotations in the given order and return the head of the
modified linked list.

A right rotation by k on a sublist means the last k nodes of that sublist move to the
front of the sublist, while the rest shift right.

If k is greater than the length of the segment, use k modulo the segment length.
If k modulo segment length equals 0, the segment remains unchanged.

Constraints:
- The number of nodes in the list is in the range [1, 10^4]
- Node values are in the range [-10^5, 10^5]
- 1 <= number of descriptors <= 500
- For each descriptor: 1 <= left <= right <= n, 0 <= k <= 10^9

Example 1:
Input: head = [1, 2, 3, 4, 5, 6, 7], descriptors = [[2, 5, 2], [1, 7, 3]]
- After rotating positions 2-5 ([2,3,4,5]) right by 2: list becomes [1, 4, 5, 2, 3, 6, 7]
- After rotating positions 1-7 ([1,4,5,2,3,6,7]) right by 3: list becomes [2, 3, 6, 7, 1, 4, 5]
Output: [2, 3, 6, 7, 1, 4, 5]

Example 2:
Input: head = [10, 20, 30, 40, 50], descriptors = [[1, 3, 4], [3, 5, 1]]
- After rotating positions 1-3 ([10,20,30]) right by 4 (4 mod 3 = 1): list becomes [30, 10, 20, 40, 50]
- After rotating positions 3-5 ([20,40,50]) right by 1: list becomes [30, 10, 50, 20, 40]
Output: [30, 10, 50, 20, 40]
"""

from typing import Optional, List, Tuple


class ListNode:
    """A node in a singly linked list."""
    
    def __init__(self, val: int = 0, next: Optional['ListNode'] = None):
        self.val = val
        self.next = next


class Solution:
    """Solution class for Cyclic Shift of Linked List Segments."""
    
    def rotateSegment(
        self,
        head: Optional[ListNode],
        descriptors: List[List[int]]
    ) -> Optional[ListNode]:
        """
        Apply a series of right rotations to specified segments of a linked list.
        
        For each descriptor (left, right, k):
        - Extract the sublist from position 'left' to 'right' (1-indexed)
        - Rotate that sublist right by k positions (last k nodes move to front)
        - Reconnect the rotated sublist back into the main list
        
        Args:
            head: The head node of the singly linked list.
            descriptors: A list of [left, right, k] triples describing rotations.
        
        Returns:
            The head of the modified linked list after all rotations.
        
        Time Complexity:
            O(D * N) where D = number of descriptors, N = number of nodes.
            Each descriptor requires traversing up to N nodes.
        
        Space Complexity:
            O(1) extra space (we only manipulate pointers, no extra data structures).
        """
        
        # -----------------------------------------------------------------------
        # STEP 1: Handle edge cases
        # If the list is empty or has only one node, no rotation is possible.
        # -----------------------------------------------------------------------
        if head is None or head.next is None:
            return head
        
        # -----------------------------------------------------------------------
        # STEP 2: Use a dummy (sentinel) node to simplify edge cases.
        # A dummy node before the head means we never have to special-case
        # "what if the segment starts at position 1?" — the node before position 1
        # is always the dummy node.
        # -----------------------------------------------------------------------
        dummy = ListNode(0)
        dummy.next = head
        
        # -----------------------------------------------------------------------
        # STEP 3: Process each descriptor one at a time.
        # -----------------------------------------------------------------------
        for descriptor in descriptors:
            left, right, k = descriptor[0], descriptor[1], descriptor[2]
            
            # -------------------------------------------------------------------
            # STEP 3a: Calculate the length of the segment.
            # segment_length = right - left + 1
            # For example, positions 2 to 5 gives length = 5 - 2 + 1 = 4.
            # -------------------------------------------------------------------
            segment_length = right - left + 1
            
            # -------------------------------------------------------------------
            # STEP 3b: Reduce k modulo segment_length.
            # A rotation by segment_length is the same as no rotation.
            # If k mod segment_length == 0, skip this descriptor entirely.
            # -------------------------------------------------------------------
            k = k % segment_length
            if k == 0:
                # No effective rotation needed; move to next descriptor.
                continue
            
            # -------------------------------------------------------------------
            # STEP 3c: Traverse to find the four key pointers we need:
            #
            #   dummy -> ... -> [pre_left] -> [seg_start] -> ... -> [split_node]
            #                                                -> [new_seg_start] -> ...
            #                                                -> [seg_end] -> [post_right] -> ...
            #
            # Where:
            #   pre_left     = node just BEFORE the segment (at position left-1)
            #   seg_start    = first node of the segment (at position left)
            #   split_node   = node at position (right - k), i.e., the last node
            #                  of the "front part" that will move to the back
            #   new_seg_start = node at position (right - k + 1), i.e., the first
            #                   node of the "back part" that will move to the front
            #   seg_end      = last node of the segment (at position right)
            #   post_right   = node just AFTER the segment (at position right+1)
            #
            # After rotation:
            #   pre_left -> new_seg_start -> ... -> seg_end -> seg_start -> ... -> split_node -> post_right
            # -------------------------------------------------------------------
            
            # Start from the dummy node (position 0).
            current = dummy
            
            # Move to position (left - 1), i.e., the node just before the segment.
            # We take (left - 1) steps from the dummy node.
            for _ in range(left - 1):
                current = current.next
            
            # 'pre_left' is the node just before the segment starts.
            pre_left = current
            
            # 'seg_start' is the first node of the segment (position 'left').
            seg_start = pre_left.next
            
            # -------------------------------------------------------------------
            # STEP 3d: Find 'split_node' — the node at position (right - k).
            # This is the last node of the "front portion" of the segment.
            # The front portion has (segment_length - k) nodes.
            # We need to move (segment_length - k - 1) steps from seg_start
            # to reach split_node.
            #
            # Example: segment [2,3,4,5], k=2, segment_length=4
            #   Front portion: [2,3] (segment_length - k = 2 nodes)
            #   Back portion:  [4,5] (k = 2 nodes)
            #   split_node = node with value 3 (position right - k = 5 - 2 = 3)
            #   new_seg_start = node with value 4 (position right - k + 1 = 4)
            # -------------------------------------------------------------------
            split_node = seg_start
            # Move (segment_length - k - 1) steps to reach split_node.
            for _ in range(segment_length - k - 1):
                split_node = split_node.next
            
            # 'new_seg_start' is the first node of the back portion (moves to front).
            new_seg_start = split_node.next
            
            # -------------------------------------------------------------------
            # STEP 3e: Find 'seg_end' — the last node of the segment (position 'right').
            # We need to move (k - 1) more steps from new_seg_start.
            # -------------------------------------------------------------------
            seg_end = new_seg_start
            for _ in range(k - 1):
                seg_end = seg_end.next
            
            # 'post_right' is the node immediately after the segment ends.
            post_right = seg_end.next
            
            # -------------------------------------------------------------------
            # STEP 3f: Rewire the pointers to perform the rotation.
            #
            # Before:
            #   pre_left -> seg_start -> ... -> split_node -> new_seg_start -> ... -> seg_end -> post_right
            #
            # After right rotation by k:
            #   pre_left -> new_seg_start -> ... -> seg_end -> seg_start -> ... -> split_node -> post_right
            #
            # We need to change:
            #   1. pre_left.next = new_seg_start   (attach back portion to pre_left)
            #   2. seg_end.next = seg_start         (attach front portion after back portion)
            #   3. split_node.next = post_right     (detach back portion from front, attach to rest of list)
            # -------------------------------------------------------------------
            
            # 1. The node before the segment now points to the start of the back portion.
            pre_left.next = new_seg_start
            
            # 2. The end of the back portion now points to the start of the front portion.
            seg_end.next = seg_start
            
            # 3. The end of the front portion now points to the node after the segment.
            split_node.next = post_right
        
        # -----------------------------------------------------------------------
        # STEP 4: Return the new head of the list.
        # dummy.next is the actual head (which may have changed if left == 1).
        # -----------------------------------------------------------------------
        return dummy.next
    
    # ---------------------------------------------------------------------------
    # Helper methods for building and printing linked lists (for testing)
    # ---------------------------------------------------------------------------
    
    def build_linked_list(self, values: List[int]) -> Optional[ListNode]:
        """
        Build a linked list from a Python list of integers.
        
        Args:
            values: A list of integer values.
        
        Returns:
            The head node of the constructed linked list.
        
        Time Complexity: O(N)
        Space Complexity: O(N)
        """
        if not values:
            return None
        
        # Create a dummy node to simplify building the list.
        dummy = ListNode(0)
        current = dummy
        
        for val in values:
            current.next = ListNode(val)
            current = current.next
        
        return dummy.next
    
    def linked_list_to_python_list(self, head: Optional[ListNode]) -> List[int]:
        """
        Convert a linked list back to a Python list for easy printing/comparison.
        
        Args:
            head: The head node of the linked list.
        
        Returns:
            A Python list of integer values from the linked list.
        
        Time Complexity: O(N)
        Space Complexity: O(N)
        """
        result = []
        current = head
        
        while current is not None:
            result.append(current.val)
            current = current.next
        
        return result


# =============================================================================
# TRACE-THROUGH VERIFICATION
# =============================================================================
#
# Example 1: head = [1, 2, 3, 4, 5, 6, 7], descriptors = [[2, 5, 2], [1, 7, 3]]
#
# Descriptor 1: left=2, right=5, k=2
#   segment_length = 5 - 2 + 1 = 4
#   k = 2 % 4 = 2 (no change)
#   pre_left = node(1) [position 1]
#   seg_start = node(2) [position 2]
#   split_node: move (4 - 2 - 1) = 1 step from seg_start
#     seg_start = node(2), move 1 step -> split_node = node(3) [position 3]
#   new_seg_start = node(4) [position 4]
#   seg_end: move (2 - 1) = 1 step from new_seg_start
#     new_seg_start = node(4), move 1 step -> seg_end = node(5) [position 5]
#   post_right = node(6) [position 6]
#   Rewire:
#     pre_left(1).next = new_seg_start(4)
#     seg_end(5).next = seg_start(2)
#     split_node(3).next = post_right(6)
#   Result: 1 -> 4 -> 5 -> 2 -> 3 -> 6 -> 7  ✓
#
# Descriptor 2: left=1, right=7, k=3
#   List is now: [1, 4, 5, 2, 3, 6, 7]
#   segment_length = 7 - 1 + 1 = 7
#   k = 3 % 7 = 3 (no change)
#   pre_left = dummy [position 0]
#   seg_start = node(1) [position 1]
#   split_node: move (7 - 3 - 1) = 3 steps from seg_start
#     node(1) -> node(4) -> node(5) -> node(2) = split_node [position 4]
#   new_seg_start = node(3) [position 5]
#   seg_end: move (3 - 1) = 2 steps from new_seg_start
#     node(3) -> node(6) -> node(7) = seg_end [position 7]
#   post_right = None [after position 7]
#   Rewire:
#     dummy.next = new_seg_start(3)
#     seg_end(7).next = seg_start(1)
#     split_node(2).next = post_right(None)
#   Result: 3 -> 6 -> 7 -> 1 -> 4 -> 5 -> 2 -> None
#
# Wait, that gives [3, 6, 7, 1, 4, 5, 2] but expected [2, 3, 6, 7, 1, 4, 5].
# Let me re-check...
#
# The list after descriptor 1 is: [1, 4, 5, 2, 3, 6, 7]
# Positions:                         1  2  3  4  5  6  7
#
# Descriptor 2: left=1, right=7, k=3
# Right rotation by 3 means last 3 nodes [3, 6, 7] move to front.
# Wait: positions 5, 6, 7 have values 3, 6, 7.
# So result should be: [3, 6, 7, 1, 4, 5, 2]
#
# But the expected output is [2, 3, 6, 7, 1, 4, 5].
# Let me re-read the problem...
#
# "After rotating positions 1-7 ([1,4,5,2,3,6,7]) right by 3: list becomes [2, 3, 6, 7, 1, 4, 5]"
#
# Hmm, [1,4,5,2,3,