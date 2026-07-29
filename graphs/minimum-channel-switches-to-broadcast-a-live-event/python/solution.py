"""
Minimum Channel Switches to Broadcast a Live Event

Problem Description:
A media company needs to send a live video stream from studio 0 to studio n - 1
through a network of relay stations. Each directed connection between stations is
labeled with a channel ID, representing the transmission channel used on that link.
Moving along an edge is always possible, but every time the stream uses a link whose
channel ID is different from the previous link's channel ID, engineers must perform
a costly channel switch.

Your task is to compute the minimum number of channel switches required to send the
stream from node 0 to node n - 1. The first chosen edge does not count as a switch,
because no channel is active before transmission begins. If it is impossible to
reach node n - 1, return -1.

Formally, you are given an integer n and a list edges where each element is
[u, v, c], meaning there is a directed edge from u to v using channel c. A path may
revisit nodes, but you want the minimum possible number of channel changes along any
valid path from 0 to n - 1.

This is a graph shortest-path problem where the state must include both the current
node and the channel used to arrive there. A solution that only tracks nodes may
miss the optimal answer.
"""

from collections import defaultdict, deque
from typing import DefaultDict, Deque, Dict, List, Set, Tuple


class Solution:
    def min_channel_switches(self, n: int, edges: List[List[int]]) -> int:
        """
        Compute the minimum number of channel switches needed to travel from node 0
        to node n - 1 in a directed graph whose edges are labeled by channel IDs.

        The first chosen edge does not count as a switch. After that, moving from one
        edge to the next costs:
        - 0 if both edges use the same channel
        - 1 if the channel changes

        This implementation builds an auxiliary 0-1 BFS graph:
        - Each original state is represented as (node, channel), meaning:
          "we are currently at this node, and the last edge used channel = channel"
        - From a super source, we can start with any outgoing channel from node 0
          at cost 0, because the first edge does not count as a switch.
        - From (u, c), we can:
          1) move along an original edge u -> v with the same channel c at cost 0
             to state (v, c)
          2) switch channel at the same node u from c to another channel d that is
             available as an outgoing channel from u, paying cost 1, reaching (u, d)

        0-1 BFS is ideal because every transition cost is either 0 or 1.

        Args:
            n: Number of nodes in the graph.
            edges: Directed edges in the form [u, v, c], where u -> v uses channel c.

        Returns:
            The minimum number of channel switches required to reach node n - 1,
            or -1 if it is impossible.

        Time complexity:
            O(E + S + X), where:
            - E is the number of original edges
            - S is the number of distinct (node, channel) states that appear
            - X is the total number of channel-switch adjacency links built between
              states at the same node
            In practice, this is linear in the size of the constructed auxiliary graph.

        Space complexity:
            O(E + S + X) for the auxiliary graph structures and BFS bookkeeping.
        """
        # Special case:
        # If the start node is already the destination node, no travel is needed,
        # therefore no channel switches are needed.
        if n == 1:
            return 0

        # ---------------------------------------------------------------------
        # STEP 1: Build helpful structures from the original edge list.
        #
        # We need to reason about states of the form (node, channel), because the
        # cost of the next move depends on the previously used channel.
        #
        # outgoing_by_channel[u][c] = list of neighbors v such that there is an edge
        # u -> v using channel c.
        #
        # This lets us do "same-channel travel" transitions with cost 0.
        #
        # channels_at_node[u] = set of channels that appear on outgoing edges of u.
        #
        # This lets us do "switch channel at node u" transitions with cost 1.
        # ---------------------------------------------------------------------
        outgoing_by_channel: DefaultDict[int, DefaultDict[int, List[int]]] = defaultdict(
            lambda: defaultdict(list)
        )
        channels_at_node: DefaultDict[int, Set[int]] = defaultdict(set)

        for u, v, c in edges:
            outgoing_by_channel[u][c].append(v)
            channels_at_node[u].add(c)

        # If node 0 has no outgoing edges, we cannot even begin transmission.
        if 0 not in channels_at_node:
            return -1

        # ---------------------------------------------------------------------
        # STEP 2: Assign an integer ID to every state (node, channel).
        #
        # Why do this?
        # - Integer IDs are faster and simpler to use in BFS arrays/dictionaries.
        # - We will build an auxiliary graph whose nodes are these states.
        #
        # A state (u, c) means:
        # "We are currently at node u, and the most recently used edge had channel c."
        #
        # Important:
        # We only need states for channels that appear as outgoing channels from a node,
        # because from state (u, c) we must be able to choose a next edge from u.
        # ---------------------------------------------------------------------
        state_to_id: Dict[Tuple[int, int], int] = {}
        id_to_state: List[Tuple[int, int]] = []

        def get_state_id(node: int, channel: int) -> int:
            """
            Get or create the integer ID for a (node, channel) state.

            Args:
                node: Current graph node.
                channel: Last-used channel at this node.

            Returns:
                Integer ID representing the state.

            Time complexity:
                O(1) average.

            Space complexity:
                O(1) additional per new state created.
            """
            key: Tuple[int, int] = (node, channel)
            if key not in state_to_id:
                state_to_id[key] = len(id_to_state)
                id_to_state.append(key)
            return state_to_id[key]

        # Create all states that can actually be used for making outgoing decisions.
        for node, channel_set in channels_at_node.items():
            for channel in channel_set:
                get_state_id(node, channel)

        # ---------------------------------------------------------------------
        # STEP 3: Build the auxiliary graph for 0-1 BFS.
        #
        # We will store:
        # zero_edges[s] = list of states reachable from s with cost 0
        # one_edges[s]  = list of states reachable from s with cost 1
        #
        # There are two types of transitions:
        #
        # A) Same-channel movement along original graph edges:
        #    From (u, c), for every original edge u -> v with channel c:
        #    - If v has outgoing edges with channel c, then we can continue as (v, c)
        #      at cost 0.
        #    - If v is the destination n - 1, we can finish immediately with no extra
        #      switch cost. We will handle this during BFS by checking node == n - 1.
        #
        # B) Channel switching at the same node:
        #    From (u, c), for every other outgoing channel d available at u:
        #    move to (u, d) with cost 1.
        #
        # This exactly models the problem:
        # - Continuing on the same channel costs nothing.
        # - Changing the channel costs one switch.
        # ---------------------------------------------------------------------
        state_count: int = len(id_to_state)
        zero_edges: List[List[int]] = [[] for _ in range(state_count)]
        one_edges: List[List[int]] = [[] for _ in range(state_count)]

        # Build cost-0 transitions for traversing original edges while keeping
        # the same channel.
        for u, channel_map in outgoing_by_channel.items():
            for channel, neighbors in channel_map.items():
                from_state: int = state_to_id[(u, channel)]

                for v in neighbors:
                    # We only create a next state if the destination node has outgoing
                    # edges using the same channel, because then state (v, channel)
                    # is meaningful for future moves.
                    #
                    # If v does not have outgoing channel "channel", that does NOT mean
                    # the path is invalid. It only means we cannot continue with the
                    # same channel from there. Reaching v may still be useful if v is
                    # the final destination, and that is handled by checking the node
                    # when a state is popped / initialized.
                    if channel in channels_at_node.get(v, set()):
                        to_state: int = state_to_id[(v, channel)]
                        zero_edges[from_state].append(to_state)

        # Build cost-1 transitions for switching channels at the same node.
        #
        # For each node, if it has k outgoing channels, then from each channel-state
        # we can switch to the other k - 1 channel-states with cost 1.
        #
        # This is the most direct and beginner-friendly construction.
        for node, channel_set in channels_at_node.items():
            channel_list: List[int] = list(channel_set)
            for i in range(len(channel_list)):
                from_channel: int = channel_list[i]
                from_state = state_to_id[(node, from_channel)]
                for j in range(len(channel_list)):
                    if i == j:
                        continue
                    to_channel: int = channel_list[j]
                    to_state = state_to_id[(node, to_channel)]
                    one_edges[from_state].append(to_state)

        # ---------------------------------------------------------------------
        # STEP 4: Initialize 0-1 BFS.
        #
        # The first chosen edge does NOT count as a switch.
        #
        # How do we model that?
        # We can "start" at node 0 with any outgoing channel from node 0 at cost 0.
        # That means every state (0, c) is an initial BFS source with distance 0.
        #
        # Then:
        # - Taking edges of channel c from node 0 costs 0
        # - Switching away from c later costs 1 as usual
        # ---------------------------------------------------------------------
        INF: int = 10**18
        dist: List[int] = [INF] * state_count
        dq: Deque[int] = deque()

        for channel in channels_at_node[0]:
            start_state: int = state_to_id[(0, channel)]
            if dist[start_state] > 0:
                dist[start_state] = 0
                dq.append(start_state)

        # ---------------------------------------------------------------------
        # STEP 5: Run 0-1 BFS.
        #
        # 0-1 BFS is like Dijkstra specialized for edge weights only in {0, 1}.
        #
        # Rule:
        # - If we relax a 0-cost edge, push the new state to the LEFT of the deque.
        # - If we relax a 1-cost edge, push the new state to the RIGHT of the deque.
        #
        # This guarantees states are processed in nondecreasing distance order.
        #
        # We also need to detect reaching the destination node n - 1.
        #
        # A state is (u, c), meaning we are at node u after using channel c.
        # If u == n - 1, then we have already reached the destination, and the
        # current distance is a valid answer.
        #
        # Additionally, there is one subtle case:
        # Suppose from (u, c) there is an original edge u -> (n - 1) with channel c,
        # but we did not create state (n - 1, c) because the destination may have no
        # outgoing edges. That move still reaches the goal with no extra switch.
        #
        # To handle this cleanly, during BFS we also inspect original same-channel
        # outgoing edges from the current state's node/channel and if any neighbor is
        # the destination, we can return the current distance immediately.
        # ---------------------------------------------------------------------
        while dq:
            current_state: int = dq.popleft()
            current_cost: int = dist[current_state]
            node, channel = id_to_state[current_state]

            # If the current state itself is already at the destination node,
            # we are done. Because 0-1 BFS processes states in increasing cost order,
            # this is guaranteed to be the minimum possible number of switches.
            if node == n - 1:
                return current_cost

            # Check whether we can directly reach the destination by taking one more
            # edge with the SAME channel. That move costs 0 additional switches.
            #
            # This is necessary because we may not have created a state for the
            # destination if it has no outgoing edges with this channel (or no
            # outgoing edges at all), but reaching the destination is still valid.
            for neighbor in outgoing_by_channel[node].get(channel, []):
                if neighbor == n - 1:
                    return current_cost

            # Process all 0-cost transitions first:
            # continue moving along edges that keep the same channel.
            for next_state in zero_edges[current_state]:
                if dist[next_state] > current_cost:
                    dist[next_state] = current_cost
                    dq.appendleft(next_state)

            # Process all 1-cost transitions:
            # switch to a different outgoing channel at the same node.
            next_cost: int = current_cost + 1
            for next_state in one_edges[current_state]:
                if dist[next_state] > next_cost:
                    dist[next_state] = next_cost
                    dq.append(next_state)

        # If BFS finishes without reaching the destination, then no valid path exists.
        return -1


if __name__ == "__main__":
    solution = Solution()

    # Example 1
    n1: int = 5
    edges1: List[List[int]] = [
        [0, 1, 7],
        [1, 2, 7],
        [2, 4, 7],
        [0, 3, 2],
        [3, 4, 2],
        [1, 3, 5],
    ]
    result1: int = solution.min_channel_switches(n1, edges1)
    print(result1)  # Expected: 0

    # Example 2
    n2: int = 6
    edges2: List[List[int]] = [
        [0, 1, 1],
        [1, 2, 2],
        [2, 5, 2],
        [0, 3, 3],
        [3, 4, 4],
        [4, 5, 5],
        [1, 4, 1],
    ]
    result2: int = solution.min_channel_switches(n2, edges2)
    print(result2)  # Expected: 1

    # Additional unreachable example
    n3: int = 4
    edges3: List[List[int]] = [
        [0, 1, 10],
        [1, 0, 10],
        [2, 3, 20],
    ]
    result3: int = solution.min_channel_switches(n3, edges3)
    print(result3)  # Expected: -1