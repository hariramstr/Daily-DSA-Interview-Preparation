import java.util.*;

/*
Problem Title: Count Distinct Folder Paths After Renames

Problem Description:
A cloud storage system stores file locations as absolute folder paths such as "/docs/team" or
"/photos/2024/trip". You are given a list of existing paths and a list of rename operations.
Each rename operation changes exactly one folder name globally within a single parent path.
For example, renaming "/docs/team" to "/docs/eng" means that the folder named "team" directly
under "/docs" becomes "eng", and every stored path that starts with "/docs/team" must be updated
to start with "/docs/eng" instead. Paths not under that exact folder are unchanged.

After applying all rename operations in order, return the number of distinct final paths in the
system. Multiple original paths may collapse into the same final path after renames, and such
duplicates should only be counted once.

Each operation is guaranteed to refer to a source folder path that exists at the moment the
operation is applied. Folder names contain only lowercase English letters and digits. No path has
trailing slashes except the root, and the root path "/" itself will not appear in the input.

Design an efficient solution using hashing so you can update and deduplicate paths without
rebuilding the full directory tree from scratch after every rename.

Constraints:
- 1 <= paths.length <= 100000
- 1 <= renames.length <= 100000
- 1 <= total number of characters across all paths and rename endpoints <= 600000
- Each path is a valid absolute path with components separated by '/'
- Renames are given as pairs [oldFolderPath, newFolderName]
- newFolderName is a single folder name, not a full path

Example 1:
Input:
paths = ["/docs/team/a", "/docs/team/b", "/docs/hr/c", "/docs/eng/b"]
renames = [["/docs/team", "eng"]]
Output: 3

Example 2:
Input:
paths = ["/x/a/log", "/x/b/log", "/x/a/tmp", "/y/a/log"]
renames = [["/x/a", "b"], ["/x/b", "c"]]
Output: 3

Explanation for Example 2:
After the first rename:
- "/x/a/log" -> "/x/b/log"
- "/x/a/tmp" -> "/x/b/tmp"
Other paths stay:
- "/x/b/log"
- "/y/a/log"

Distinct paths now:
- "/x/b/log"
- "/x/b/tmp"
- "/y/a/log"

After the second rename:
- everything under "/x/b" moves to "/x/c"
So final distinct paths are:
- "/x/c/log"
- "/x/c/tmp"
- "/y/a/log"

Therefore the correct answer is 3.

Approach:
We model folder renames using a dynamic mapping on folder nodes, not by rewriting every stored path
for every operation.

Key idea:
1. Every unique folder path prefix becomes a node.
2. A rename only changes the name of one node under its parent.
3. Final path of a stored file/folder path can be reconstructed by walking through its original
   prefix nodes and asking each parent which child name that node currently has.
4. To count distinct final paths efficiently, we compute a rolling hash of the final path string
   without necessarily materializing all intermediate renamed paths repeatedly.

This avoids rebuilding the entire directory tree after each rename and uses hashing to deduplicate
final results.
*/
public class Solution {

    /**
     * Large modulus for polynomial rolling hash.
     */
    private static final long MOD = 1_000_000_007L;

    /**
     * Base for polynomial rolling hash.
     */
    private static final long BASE = 911_382_323L;

    /**
     * Represents one folder node in the prefix tree of all folder prefixes that appear in input paths.
     *
     * Each node stores:
     * - its parent
     * - its original name
     * - its current name after all applied renames
     * - children by current name, so future rename operations can find the target node quickly
     */
    private static class Node {
        int id;
        int parent;
        String originalName;
        String currentName;
        Map<String, Integer> childrenByCurrentName = new HashMap<>();

        Node(int id, int parent, String name) {
            this.id = id;
            this.parent = parent;
            this.originalName = name;
            this.currentName = name;
        }
    }

    /**
     * Stores one original input path as a sequence of folder node ids.
     *
     * Example:
     * "/docs/team/a" might be represented as [docsNode, teamNode, aNode]
     *
     * Later, after renames, we can reconstruct the final path by reading the current names
     * of these same nodes in order.
     */
    private static class StoredPath {
        int[] nodeIds;

        StoredPath(int[] nodeIds) {
            this.nodeIds = nodeIds;
        }
    }

    /**
     * Counts the number of distinct final paths after applying all rename operations in order.
     *
     * Detailed idea:
     * 1. Build a prefix tree (trie-like structure) from all given paths.
     *    Every folder prefix becomes a unique node.
     * 2. Each rename operation identifies a node by its current full path and changes only that node's
     *    current name under its parent.
     * 3. For every original stored path, reconstruct its final path using the same node sequence but
     *    with each node's current name.
     * 4. Compute a hash for the final path and also store the final string to guarantee correctness
     *    even in the extremely unlikely event of a hash collision.
     * 5. Return the number of distinct final paths.
     *
     * @param paths the original absolute paths stored in the system
     * @param renames rename operations where each element is [oldFolderPath, newFolderName]
     * @return the number of distinct final paths after all renames are applied
     * Time complexity: O(totalPathChars + totalRenameChars + totalComponentsInPathsAndRenames)
     * Space complexity: O(totalPathChars + numberOfPrefixNodes + numberOfStoredPaths)
     */
    public int countDistinctPaths(String[] paths, String[][] renames) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(new Node(0, -1, "")); // root node representing "/"

        Map<String, Integer> absolutePathToNode = new HashMap<>();
        absolutePathToNode.put("/", 0);

        StoredPath[] storedPaths = new StoredPath[paths.length];

        // --------------------------------------------------------------------
        // STEP 1: Build all prefix nodes from the original paths.
        //
        // We create one node per unique folder prefix.
        // Example:
        // "/docs/team/a" creates or reuses:
        //   "/docs"
        //   "/docs/team"
        //   "/docs/team/a"
        //
        // We also remember, for each original path, the exact sequence of node ids
        // that forms that path. This sequence never changes. Only node current names change.
        // --------------------------------------------------------------------
        for (int i = 0; i < paths.length; i++) {
            storedPaths[i] = buildPath(nodes, absolutePathToNode, paths[i]);
        }

        // --------------------------------------------------------------------
        // STEP 2: Apply renames in order.
        //
        // A rename operation is [oldFolderPath, newFolderName].
        // The problem guarantees that oldFolderPath exists at the moment of rename.
        //
        // Because folder names can change over time, we must locate the target node using
        // the CURRENT tree structure, not the original absolutePathToNode map.
        //
        // So for each rename:
        //   - parse oldFolderPath into components
        //   - walk from root using childrenByCurrentName
        //   - find the target node
        //   - update the parent's child-name map:
        //       remove old current name
        //       insert new current name
        //   - update the node's currentName
        // --------------------------------------------------------------------
        for (String[] rename : renames) {
            String oldFolderPath = rename[0];
            String newFolderName = rename[1];
            applyRename(nodes, oldFolderPath, newFolderName);
        }

        // --------------------------------------------------------------------
        // STEP 3: Reconstruct final paths and count distinct ones.
        //
        // We use hashing to deduplicate efficiently.
        // To make correctness absolutely robust, we store the final string inside a bucket
        // keyed by hash. This protects against hash collisions.
        //
        // For each stored path:
        //   - walk through its node ids
        //   - append "/" + currentName of each node
        //   - compute rolling hash at the same time
        //   - insert into hash bucket set
        // --------------------------------------------------------------------
        Map<Long, Set<String>> buckets = new HashMap<>();

        for (StoredPath storedPath : storedPaths) {
            StringBuilder sb = new StringBuilder();
            long hash = 0L;

            for (int nodeId : storedPath.nodeIds) {
                String name = nodes.get(nodeId).currentName;

                // Append '/'
                sb.append('/');
                hash = (hash * BASE + '/') % MOD;

                // Append folder name characters
                for (int j = 0; j < name.length(); j++) {
                    char c = name.charAt(j);
                    sb.append(c);
                    hash = (hash * BASE + c) % MOD;
                }
            }

            String finalPath = sb.toString();
            buckets.computeIfAbsent(hash, k -> new HashSet<>()).add(finalPath);
        }

        int distinctCount = 0;
        for (Set<String> set : buckets.values()) {
            distinctCount += set.size();
        }
        return distinctCount;
    }

    /**
     * Builds or reuses nodes for one absolute path and returns the sequence of node ids
     * corresponding to that path.
     *
     * Example:
     * For "/a/b/c", this method ensures nodes for:
     * - "/a"
     * - "/a/b"
     * - "/a/b/c"
     *
     * It uses the original absolute path string map only during initial construction,
     * before any renames happen.
     *
     * @param nodes all folder nodes
     * @param absolutePathToNode map from original absolute prefix path to node id
     * @param path one absolute path from the input
     * @return a StoredPath containing the node id sequence for this path
     * Time complexity: O(length of path)
     * Space complexity: O(number of new prefixes created for this path)
     */
    public StoredPath buildPath(List<Node> nodes, Map<String, Integer> absolutePathToNode, String path) {
        List<String> parts = splitPath(path);
        int[] ids = new int[parts.size()];

        int parentId = 0;
        StringBuilder prefix = new StringBuilder();

        for (int i = 0; i < parts.size(); i++) {
            String part = parts.get(i);
            prefix.append('/').append(part);
            String prefixPath = prefix.toString();

            Integer existing = absolutePathToNode.get(prefixPath);
            int nodeId;

            if (existing != null) {
                nodeId = existing;
            } else {
                nodeId = nodes.size();
                Node node = new Node(nodeId, parentId, part);
                nodes.add(node);
                absolutePathToNode.put(prefixPath, nodeId);

                // Since this is initial construction, current name equals original name.
                nodes.get(parentId).childrenByCurrentName.put(part, nodeId);
            }

            ids[i] = nodeId;
            parentId = nodeId;
        }

        return new StoredPath(ids);
    }

    /**
     * Applies one rename operation to the current folder structure.
     *
     * Important details:
     * - The source path is interpreted using CURRENT names, because previous renames may already
     *   have changed the visible path.
     * - Only one folder node changes its current name.
     * - All descendants automatically inherit the rename when final paths are reconstructed,
     *   because they still reference the same ancestor node.
     *
     * @param nodes all folder nodes
     * @param oldFolderPath the current absolute path of the folder being renamed
     * @param newFolderName the new single folder name
     * @return the id of the renamed node
     * Time complexity: O(number of components in oldFolderPath)
     * Space complexity: O(1) auxiliary, excluding path splitting storage
     */
    public int applyRename(List<Node> nodes, String oldFolderPath, String newFolderName) {
        List<String> parts = splitPath(oldFolderPath);

        int current = 0;
        for (String part : parts) {
            Integer next = nodes.get(current).childrenByCurrentName.get(part);
            if (next == null) {
                throw new IllegalStateException("Rename source path does not exist: " + oldFolderPath);
            }
            current = next;
        }

        Node target = nodes.get(current);
        int parentId = target.parent;
        Node parent = nodes.get(parentId);

        // Remove the old visible name from the parent's current-name map.
        parent.childrenByCurrentName.remove(target.currentName);

        // Update the node's visible name.
        target.currentName = newFolderName;

        // Insert the new visible name into the parent's current-name map.
        // If another child already had this name, they now collapse from the perspective
        // of final path strings. That is allowed by the problem statement.
        // For future renames, the problem guarantees source paths are valid when used.
        parent.childrenByCurrentName.put(newFolderName, current);

        return current;
    }

    /**
     * Splits an absolute path into its folder components.
     *
     * Example:
     * "/docs/team/a" -> ["docs", "team", "a"]
     *
     * The root "/" itself does not appear in input according to the constraints.
     *
     * @param path an absolute path
     * @return list of folder names in order
     * Time complexity: O(length of path)
     * Space complexity: O(number of components)
     */
    public List<String> splitPath(String path) {
        List<String> parts = new ArrayList<>();
        int n = path.length();
        int i = 1; // skip leading '/'

        while (i < n) {
            int j = i;
            while (j < n && path.charAt(j) != '/') {
                j++;
            }
            parts.add(path.substring(i, j));
            i = j + 1;
        }

        return parts;
    }

    /**
     * Demonstrates the solution on the sample inputs.
     *
     * @param args command-line arguments, unused
     * @return nothing
     * Time complexity: O(total size of demo inputs)
     * Space complexity: O(total size of demo inputs)
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        String[] paths1 = {
                "/docs/team/a",
                "/docs/team/b",
                "/docs/hr/c",
                "/docs/eng/b"
        };
        String[][] renames1 = {
                {"/docs/team", "eng"}
        };
        System.out.println(solution.countDistinctPaths(paths1, renames1)); // Expected: 3

        String[] paths2 = {
                "/x/a/log",
                "/x/b/log",
                "/x/a/tmp",
                "/y/a/log"
        };
        String[][] renames2 = {
                {"/x/a", "b"},
                {"/x/b", "c"}
        };
        System.out.println(solution.countDistinctPaths(paths2, renames2)); // Expected: 3
    }
}