/*
 * Inbox Message Thread Flattener
 * ================================
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * You are building an email client. Messages arrive as a stream of events.
 * Each event is either:
 * - SEND id parent_id: A new message with a unique integer id is sent as a reply
 *   to message parent_id. If parent_id is 0, it starts a new top-level thread.
 * - READ depth: Return all message IDs at exactly depth levels deep in the current
 *   thread tree, in the order they were first received (left to right, BFS order).
 *   Depth 0 means only top-level messages.
 *
 * Process all events in order and return a list of results — one list of IDs per
 * READ event (in the order the READ events appear).
 *
 * A message's depth is defined as the number of ancestors it has.
 * Top-level messages (parent_id = 0) have depth 0.
 *
 * Constraints:
 * - 1 <= number of events <= 10^4
 * - 1 <= id <= 10^5, all IDs are unique
 * - parent_id is either 0 or the ID of a previously sent message
 * - 0 <= depth <= 500
 * - A READ event always comes after at least one SEND event
 *
 * Example 1:
 * Events:
 *   SEND 1 0
 *   SEND 2 0
 *   SEND 3 1
 *   SEND 4 1
 *   SEND 5 3
 *   READ 0
 *   READ 1
 *   READ 2
 * Output: [[1, 2], [3, 4], [5]]
 *
 * Example 2:
 * Events:
 *   SEND 10 0
 *   SEND 20 10
 *   READ 1
 *   SEND 30 10
 *   READ 1
 * Output: [[20], [20, 30]]
 */

import java.util.*;

/**
 * Solution class for the Inbox Message Thread Flattener problem.
 *
 * <p>Core idea:
 * We maintain a tree structure where each node represents a message.
 * Each node stores its ID, its depth, and an ordered list of children.
 * For a READ event at a given depth, we perform a BFS traversal of the
 * entire tree and collect all nodes whose depth matches the requested depth.
 * BFS naturally visits nodes level by level and left-to-right (in insertion order),
 * which satisfies the "order they were first received" requirement.
 */
public class Solution {

    // -------------------------------------------------------------------------
    // Inner class: MessageNode
    // Represents a single message in the thread tree.
    // -------------------------------------------------------------------------

    /**
     * Represents a single message node in the email thread tree.
     */
    static class MessageNode {
        /** The unique identifier of this message. */
        int id;

        /** The depth of this message (number of ancestors). */
        int depth;

        /**
         * Ordered list of child messages (replies to this message).
         * We use a LinkedList to preserve insertion order.
         */
        List<MessageNode> children;

        /**
         * Constructs a MessageNode with the given id and depth.
         *
         * @param id    the unique message ID
         * @param depth the depth of this message in the thread tree
         */
        MessageNode(int id, int depth) {
            this.id = id;
            this.depth = depth;
            this.children = new LinkedList<>();
        }
    }

    // -------------------------------------------------------------------------
    // Fields used by the solution
    // -------------------------------------------------------------------------

    /**
     * Maps each message ID to its corresponding MessageNode.
     * This allows O(1) lookup when we need to attach a new reply to its parent.
     */
    private Map<Integer, MessageNode> nodeMap;

    /**
     * A virtual "root" node with id=0 and depth=-1.
     * All top-level messages (parent_id == 0) are children of this virtual root.
     * This simplifies the tree structure — we always have a single root to start BFS from.
     */
    private MessageNode virtualRoot;

    /**
     * Constructs a new Solution instance, initializing the tree with a virtual root.
     */
    public Solution() {
        // Initialize the map that will hold all message nodes
        nodeMap = new HashMap<>();

        // Create a virtual root node at depth -1 (it is never returned in results)
        // All top-level messages will be children of this virtual root
        virtualRoot = new MessageNode(0, -1);

        // Register the virtual root in the map so that SEND events with parent_id=0
        // can find it and attach new top-level messages as its children
        nodeMap.put(0, virtualRoot);
    }

    // -------------------------------------------------------------------------
    // Core methods
    // -------------------------------------------------------------------------

    /**
     * Processes a SEND event: creates a new message node and attaches it to its parent.
     *
     * <p>Algorithm steps:
     * 1. Look up the parent node in nodeMap using parent_id.
     * 2. Compute the new message's depth = parent.depth + 1.
     *    (Virtual root has depth -1, so top-level messages get depth 0. ✓)
     * 3. Create a new MessageNode with the given id and computed depth.
     * 4. Add the new node as a child of the parent node (preserving insertion order).
     * 5. Register the new node in nodeMap for future lookups.
     *
     * @param id       the unique ID of the new message
     * @param parentId the ID of the parent message (0 if top-level)
     *                 Time complexity:  O(1) — just a map lookup and list append
     *                 Space complexity: O(1) per call; O(N) total for N messages
     */
    public void sendMessage(int id, int parentId) {
        // Step 1: Retrieve the parent node from the map.
        // Since the problem guarantees parent_id is always valid (0 or a previously sent ID),
        // this lookup will always succeed.
        MessageNode parentNode = nodeMap.get(parentId);

        // Step 2: Compute depth.
        // Virtual root is at depth -1, so its children (top-level messages) are at depth 0.
        // Any other parent at depth d produces children at depth d+1.
        int newDepth = parentNode.depth + 1;

        // Step 3: Create the new message node.
        MessageNode newNode = new MessageNode(id, newDepth);

        // Step 4: Attach the new node as a child of the parent.
        // LinkedList.add() appends to the end, preserving insertion (arrival) order.
        parentNode.children.add(newNode);

        // Step 5: Register the new node so future SEND events can reference it as a parent.
        nodeMap.put(id, newNode);
    }

    /**
     * Processes a READ event: returns all message IDs at exactly the given depth,
     * in BFS (insertion) order.
     *
     * <p>Algorithm steps:
     * 1. Initialize a Queue for BFS, starting from the virtual root's children.
     *    (We skip the virtual root itself since it's not a real message.)
     * 2. Dequeue nodes one by one.
     *    - If a node's depth equals the target depth, add its ID to the result list.
     *    - If a node's depth is less than the target depth, enqueue all its children
     *      (they might be at or closer to the target depth).
     *    - If a node's depth equals the target depth, we do NOT enqueue its children
     *      (they would be deeper than needed, and we can prune here for efficiency).
     *    - If a node's depth exceeds the target depth, skip it (shouldn't happen with pruning).
     * 3. Return the collected list of IDs.
     *
     * <p>Why BFS guarantees left-to-right, insertion-order traversal:
     * BFS processes nodes level by level. Within each level, nodes are processed in
     * the order they were enqueued, which mirrors the order they were inserted as
     * children of their respective parents. Since we always append children in
     * insertion order, BFS naturally produces the "first received" ordering.
     *
     * @param depth the target depth level to query
     * @return a list of message IDs at exactly the given depth, in BFS order
     *         Time complexity:  O(N) where N is the total number of messages sent so far
     *         Space complexity: O(W) where W is the maximum width of the tree at any level
     */
    public List<Integer> readAtDepth(int depth) {
        // This list will accumulate the IDs of all messages at the target depth
        List<Integer> result = new ArrayList<>();

        // Step 1: Initialize BFS queue.
        // We use a LinkedList as a Queue (FIFO).
        Queue<MessageNode> queue = new LinkedList<>();

        // Seed the BFS with the real top-level messages (children of the virtual root).
        // The virtual root itself (depth -1) is never added to the queue.
        queue.addAll(virtualRoot.children);

        // Step 2: BFS traversal
        while (!queue.isEmpty()) {
            // Dequeue the front node
            MessageNode current = queue.poll();

            if (current.depth == depth) {
                // This node is exactly at the target depth — record its ID
                result.add(current.id);

                // Pruning: do NOT enqueue children of this node.
                // Children would be at depth+1, which is deeper than requested.
                // This optimization avoids unnecessary work.

            } else if (current.depth < depth) {
                // This node is shallower than the target depth.
                // Its children might be at or closer to the target depth,
                // so we enqueue all of them for further exploration.
                queue.addAll(current.children);

                // Note: if current.depth + 1 == depth, the children will be collected
                // in the next iteration of the while loop.
            }
            // If current.depth > depth: this case won't occur with our pruning strategy,
            // but if it did, we'd simply skip the node (no action needed).
        }

        // Step 3: Return the collected IDs
        return result;
    }

    /**
     * Processes a list of string events and returns results for each READ event.
     *
     * <p>Each event string is either:
     * - "SEND id parent_id"
     * - "READ depth"
     *
     * <p>This method parses each event, delegates to sendMessage() or readAtDepth(),
     * and collects READ results in order.
     *
     * @param events a list of event strings to process
     * @return a list of lists, where each inner list contains the IDs returned by one READ event
     *         Time complexity:  O(E * N) where E = number of READ events, N = number of messages
     *         Space complexity: O(N) for the tree structure
     */
    public List<List<Integer>> processEvents(List<String> events) {
        // This will hold one List<Integer> per READ event encountered
        List<List<Integer>> results = new ArrayList<>();

        // Process each event string in order
        for (String event : events) {
            // Split the event string on whitespace to extract tokens
            // e.g., "SEND 3 1" -> ["SEND", "3", "1"]
            // e.g., "READ 2"   -> ["READ", "2"]
            String[] tokens = event.trim().split("\\s+");

            // Identify the event type by the first token
            String eventType = tokens[0];

            if (eventType.equals("SEND")) {
                // Parse the message ID (second token) and parent ID (third token)
                int id = Integer.parseInt(tokens[1]);
                int parentId = Integer.parseInt(tokens[2]);

                // Delegate to sendMessage to insert the node into the tree
                sendMessage(id, parentId);

            } else if (eventType.equals("READ")) {
                // Parse the target depth (second token)
                int depth = Integer.parseInt(tokens[1]);

                // Delegate to readAtDepth to collect all IDs at that depth
                List<Integer> readResult = readAtDepth(depth);

                // Add the result to our output list
                results.add(readResult);
            }
            // (No other event types are defined in this problem)
        }

        return results;
    }

    // -------------------------------------------------------------------------
    // Main method — demonstrates the solution with both examples
    // -------------------------------------------------------------------------

    /**
     * Entry point. Demonstrates the solution with the two examples from the problem description.
     *
     * <p>Example 1 expected output: [[1, 2], [3, 4], [5]]
     * <p>Example 2 expected output: [[20], [20, 30]]
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {

        // =====================================================================
        // EXAMPLE 1
        // Tree structure after all SENDs:
        //
        //   (virtual root, depth=-1)
        //       |
        //   +---+---+
        //   1       2       <- depth 0
        //   |
        //  +-+
        //  3  4             <- depth 1
        //  |
        //  5                <- depth 2
        //
        // READ 0 -> [1, 2]
        // READ 1 -> [3, 4]
        // READ 2 -> [5]
        // =====================================================================

        System.out.println("=== Example 1 ===");

        // Build the list of events for Example 1
        List<String> events1 = Arrays.asList(
                "SEND 1 0",
                "SEND 2 0",
                "SEND 3 1",
                "SEND 4 1",
                "SEND 5 3",
                "READ 0",
                "READ 1",
                "READ 2"
        );

        // Create a fresh Solution instance for Example 1
        Solution sol1 = new Solution();

        // Process all events and collect READ results
        List<List<Integer>> output1 = sol1.processEvents(events1);

        // Print the results
        System.out.println("Output: " + output1);
        // Expected: [[1, 2], [3, 4], [5]]

        System.out.println();

        // =====================================================================
        // EXAMPLE 2
        // Tree structure evolves over time:
        //
        // After SEND 10 0, SEND 20 10:
        //   (virtual root)
        //       |
        //      10           <- depth 0
        //       |
        //      20           <- depth 1
        //
        // READ 1 -> [20]
        //
        // After SEND 30 10:
        //   (virtual root)
        //       |
        //      10           <- depth 0
        //      |
        //    +--+
        //   20  30          <- depth 1
        //
        // READ 1 -> [20, 30]
        // =====================================================================

        System.out.println("=== Example 2 ===");

        // Build the list of events for Example 2
        List<String> events2 = Arrays.asList(
                "SEND 10 0",
                "SEND 20 10",
                "READ 1",
                "SEND 30 10",
                "READ 1"
        );

        // Create a fresh Solution instance for Example 2
        Solution sol2 = new Solution();

        // Process all events and collect READ results
        List<List<Integer>> output2 = sol2.processEvents(events2);

        // Print the results
        System.out.println("Output: " + output2);
        // Expected: [[20], [20, 30]]

        System.out.println();

        // =====================================================================
        // ADDITIONAL EDGE CASE: READ at a depth with no messages
        // =====================================================================

        System.out.println("=== Edge Case: READ at depth with no messages ===");

        List<String> events3 = Arrays.asList(
                "SEND 100