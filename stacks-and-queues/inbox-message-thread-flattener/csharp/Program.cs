/*
 * ============================================================
 * Problem Title: Inbox Message Thread Flattener
 * Difficulty: Medium
 * Topic: Stacks and Queues
 *
 * Problem Description:
 * You are building an email client. Messages arrive as a stream of events.
 * Each event is either:
 *   - SEND id parent_id : A new message with a unique integer `id` is sent
 *     as a reply to message `parent_id`. If `parent_id` is 0, it starts a
 *     new top-level thread.
 *   - READ depth : Return all message IDs at exactly `depth` levels deep in
 *     the current thread tree, in the order they were first received
 *     (left to right, BFS order). Depth 0 means only top-level messages.
 *
 * A message's depth = number of ancestors it has.
 * Top-level messages (parent_id == 0) have depth 0.
 *
 * Constraints:
 *   - 1 <= number of events <= 10^4
 *   - 1 <= id <= 10^5, all IDs are unique
 *   - parent_id is 0 or the ID of a previously sent message
 *   - 0 <= depth <= 500
 *   - A READ event always comes after at least one SEND event
 * ============================================================
 */

using System;
using System.Collections.Generic;

// ============================================================
// Solution Class
// ============================================================
public class InboxThreadFlattener
{
    /*
     * Method: ProcessEvents
     *
     * Time Complexity:  O(E * N) in the worst case, where E = number of READ
     *                   events and N = total number of messages. Each READ
     *                   triggers a BFS over all messages to collect those at
     *                   the requested depth. In practice this is very fast
     *                   because N <= 10^5 and E <= 10^4.
     *
     * Space Complexity: O(N) for storing the tree (children lists + depth map).
     *
     * High-level approach:
     *   1. Maintain a tree of messages using an adjacency list
     *      (parent -> list of children, in insertion order).
     *   2. Also keep a dictionary that maps each message ID to its depth,
     *      so we can compute a child's depth instantly as parent_depth + 1.
     *   3. For every SEND event, record the message and update the tree.
     *   4. For every READ event, do a BFS starting from the virtual root
     *      (depth -1) and collect all nodes whose depth equals the requested
     *      depth. Return them in BFS (insertion) order.
     */
    public List<List<int>> ProcessEvents(List<string> events)
    {
        // --------------------------------------------------------
        // DATA STRUCTURES
        // --------------------------------------------------------

        // children[id] = ordered list of child message IDs.
        // We use a special virtual root with id=0 to hold all top-level
        // messages as its children. This simplifies BFS: we always start
        // from node 0 and treat its children as depth-0 messages.
        // Using Dictionary<int, List<int>> gives O(1) average lookup.
        var children = new Dictionary<int, List<int>>();

        // depth[id] = how many ancestors message `id` has.
        // Top-level messages have depth 0.
        // We store depth for every real message so that when a child
        // arrives we can set child_depth = depth[parent] + 1 in O(1).
        var depth = new Dictionary<int, int>();

        // Initialize the virtual root (id = 0) with depth -1 so that
        // its direct children (top-level messages) get depth 0.
        children[0] = new List<int>();
        depth[0] = -1; // virtual root is one level above depth 0

        // This list accumulates one sub-list per READ event encountered.
        var results = new List<List<int>>();

        // --------------------------------------------------------
        // EVENT PROCESSING LOOP
        // --------------------------------------------------------
        foreach (var eventLine in events)
        {
            // Split the event string into tokens.
            // e.g. "SEND 3 1"  -> ["SEND", "3", "1"]
            //      "READ 2"    -> ["READ", "2"]
            var tokens = eventLine.Split(' ');
            var command = tokens[0];

            if (command == "SEND")
            {
                // --------------------------------------------------
                // SEND id parent_id
                // --------------------------------------------------

                // Parse the message id and its parent's id.
                int msgId = int.Parse(tokens[1]);
                int parentId = int.Parse(tokens[2]);

                // Step 1: Register this message as a child of its parent.
                // If the parent has no children list yet, create one.
                // (The virtual root (0) was pre-initialised above.)
                if (!children.ContainsKey(parentId))
                {
                    // This should not happen given valid input (parent always
                    // sent before child), but defensive initialisation is good.
                    children[parentId] = new List<int>();
                }

                // Append the new message to the parent's children list.
                // List<T>.Add is O(1) amortised and preserves insertion order,
                // which is exactly the BFS "left-to-right" order we need.
                children[parentId].Add(msgId);

                // Step 2: Initialise the new message's own children list
                // so future SEND events can add children to it without
                // a ContainsKey check.
                children[msgId] = new List<int>();

                // Step 3: Compute and store the depth of this new message.
                // depth[parent] is already known (parent was sent earlier,
                // or parent == 0 whose depth is -1).
                // child_depth = parent_depth + 1
                depth[msgId] = depth[parentId] + 1;

                // Example trace (Example 1):
                //   SEND 1 0  -> depth[1] = depth[0]+1 = 0, children[0]=[1]
                //   SEND 2 0  -> depth[2] = 0,              children[0]=[1,2]
                //   SEND 3 1  -> depth[3] = depth[1]+1 = 1, children[1]=[3]
                //   SEND 4 1  -> depth[4] = 1,              children[1]=[3,4]
                //   SEND 5 3  -> depth[5] = depth[3]+1 = 2, children[3]=[5]
            }
            else if (command == "READ")
            {
                // --------------------------------------------------
                // READ depth_target
                // --------------------------------------------------

                // Parse the target depth we want to collect.
                int targetDepth = int.Parse(tokens[1]);

                // We will collect all message IDs at exactly targetDepth.
                var result = new List<int>();

                // --------------------------------------------------
                // BFS to find all nodes at the requested depth.
                //
                // Why BFS and not DFS?
                //   BFS naturally visits nodes level by level and in
                //   insertion (left-to-right) order within each level,
                //   which matches the problem's "BFS order" requirement.
                //
                // Why a Queue?
                //   A Queue<T> gives O(1) enqueue (Enqueue) and dequeue
                //   (Dequeue), making BFS efficient.
                // --------------------------------------------------

                // The queue stores message IDs to visit.
                // We start from the virtual root (id = 0, depth = -1).
                var queue = new Queue<int>();
                queue.Enqueue(0); // start BFS from virtual root

                // Process nodes level by level until the queue is empty
                // or we have gone past the target depth.
                while (queue.Count > 0)
                {
                    // Dequeue the next message to inspect.
                    int current = queue.Dequeue();

                    // Determine the depth of the current node.
                    int currentDepth = depth[current];

                    // If the current node is already deeper than the target,
                    // its children will be even deeper — no need to enqueue them.
                    // This is an important pruning step that avoids unnecessary work.
                    if (currentDepth >= targetDepth)
                    {
                        // If this node is exactly at the target depth, record it.
                        if (currentDepth == targetDepth)
                        {
                            result.Add(current);
                        }
                        // Do NOT enqueue children — they are too deep.
                        continue;
                    }

                    // The current node is shallower than the target depth.
                    // Enqueue all its children so they can be inspected next.
                    // Children are stored in insertion order, so BFS order
                    // is automatically preserved.
                    foreach (int child in children[current])
                    {
                        queue.Enqueue(child);
                    }
                }

                // Add the collected IDs for this READ event to our results list.
                results.Add(result);

                // Example trace (Example 1, READ 0):
                //   queue starts: [0]
                //   Dequeue 0 (depth=-1 < 0): enqueue children [1,2] -> queue=[1,2]
                //   Dequeue 1 (depth=0 == 0): add 1 to result, skip children
                //   Dequeue 2 (depth=0 == 0): add 2 to result, skip children
                //   result = [1, 2]  ✓
                //
                // Example trace (Example 1, READ 1):
                //   queue starts: [0]
                //   Dequeue 0 (depth=-1 < 1): enqueue [1,2]
                //   Dequeue 1 (depth=0 < 1):  enqueue [3,4]
                //   Dequeue 2 (depth=0 < 1):  enqueue [] (no children)
                //   Dequeue 3 (depth=1 == 1): add 3, skip children
                //   Dequeue 4 (depth=1 == 1): add 4, skip children
                //   result = [3, 4]  ✓
                //
                // Example trace (Example 1, READ 2):
                //   ... BFS reaches 5 (depth=2 == 2): add 5
                //   result = [5]  ✓
            }
        }

        return results;
    }
}

// ============================================================
// Demo / Driver Code (top-level statements)
// ============================================================

var solver = new InboxThreadFlattener();

// Helper to print results nicely.
static void PrintResults(List<List<int>> results)
{
    Console.Write("[");
    for (int i = 0; i < results.Count; i++)
    {
        Console.Write("[");
        Console.Write(string.Join(", ", results[i]));
        Console.Write("]");
        if (i < results.Count - 1) Console.Write(", ");
    }
    Console.WriteLine("]");
}

// -------------------------------------------------------
// Example 1
// Expected output: [[1, 2], [3, 4], [5]]
// -------------------------------------------------------
Console.WriteLine("=== Example 1 ===");
var events1 = new List<string>
{
    "SEND 1 0",
    "SEND 2 0",
    "SEND 3 1",
    "SEND 4 1",
    "SEND 5 3",
    "READ 0",
    "READ 1",
    "READ 2"
};
var result1 = solver.ProcessEvents(events1);
Console.Write("Output: ");
PrintResults(result1);
// Expected: [[1, 2], [3, 4], [5]]

// -------------------------------------------------------
// Example 2
// Expected output: [[20], [20, 30]]
// -------------------------------------------------------
Console.WriteLine("\n=== Example 2 ===");
var events2 = new List<string>
{
    "SEND 10 0",
    "SEND 20 10",
    "READ 1",
    "SEND 30 10",
    "READ 1"
};
var result2 = solver.ProcessEvents(events2);
Console.Write("Output: ");
PrintResults(result2);
// Expected: [[20], [20, 30]]

// -------------------------------------------------------
// Example 3 — Edge case: READ at depth that has no messages
// -------------------------------------------------------
Console.WriteLine("\n=== Example 3 (depth with no messages) ===");
var events3 = new List<string>
{
    "SEND 7 0",
    "READ 5"   // depth 5 has no messages
};
var result3 = solver.ProcessEvents(events3);
Console.Write("Output: ");
PrintResults(result3);
// Expected: [[]]

// -------------------------------------------------------
// Example 4 — Deeper chain
// -------------------------------------------------------
Console.WriteLine("\n=== Example 4 (deep chain) ===");
var events4 = new List<string>
{
    "SEND 100 0",
    "SEND 200 100",
    "SEND 300 200",
    "SEND 400 300",
    "READ 0",
    "READ 1",
    "READ 2",
    "READ 3"
};
var result4 = solver.ProcessEvents(events4);
Console.Write("Output: ");
PrintResults(result4);
// Expected: [[100], [200], [300], [400]]