import java.util.*;

/*
Title: Find the Smallest Shared Category Subtree
Difficulty: Medium
Topic: Trees

Problem Description:
You are given the root of a binary tree representing a product category hierarchy. Each node contains an integer category ID, and IDs are unique within the tree. You are also given a list of distinct target category IDs that correspond to categories selected by different teams. Your task is to return the category ID of the smallest subtree that contains all target categories. A subtree is considered rooted at some node and includes that node and all of its descendants.

If all target categories appear in the tree, return the ID of the lowest node whose subtree contains every target ID. If one or more target IDs do not exist in the tree, return -1.

This problem is not asking for the path between nodes. Instead, you must identify the single subtree root that covers all requested categories as tightly as possible. The selected node may itself be one of the target categories.

You should design an algorithm that works efficiently for large trees.

Constraints:
- The number of nodes in the tree is in the range [1, 100000].
- Node values are unique integers in the range [1, 10^9].
- The number of target IDs is in the range [1, min(100000, n)].
- All target IDs are distinct.
- The tree is a binary tree and may be unbalanced.

Example 1:
Input: root = [10,5,15,3,7,12,18,null,4], targets = [4,7]
Output: 5
Explanation: Category 4 is in the left subtree of 5 through node 3, and category 7 is the right child of 5. The subtree rooted at 5 is the smallest subtree containing both targets.

Example 2:
Input: root = [8,4,11,2,6,9,13,1,3,5,7], targets = [1,7,9]
Output: 8
Explanation: Node 1 and node 7 are inside the left subtree of 8, while node 9 is inside the right subtree of 8. No lower node contains all three targets, so the answer is 8.
*/

public class Solution {

    /**
     * Basic binary tree node definition.
     */
    public static class TreeNode {
        int val;
        TreeNode left;
        TreeNode right;

        /**
         * Creates a tree node with the given value.
         *
         * @param val the integer value stored in the node
         */
        TreeNode(int val) {
            this.val = val;
        }
    }

    /**
     * Small helper object used during DFS.
     * It stores:
     * 1) how many target values were found in the current subtree
     * 2) the answer node if the smallest valid subtree has already been identified below
     */
    private static class Result {
        int foundCount;
        TreeNode answerNode;

        /**
         * Creates a DFS result object.
         *
         * @param foundCount number of target IDs found in the subtree
         * @param answerNode smallest subtree root containing all targets, if already determined
         */
        Result(int foundCount, TreeNode answerNode) {
            this.foundCount = foundCount;
            this.answerNode = answerNode;
        }
    }

    /**
     * Returns the category ID of the smallest subtree that contains all target IDs.
     * If one or more target IDs do not exist in the tree, returns -1.
     *
     * The key idea:
     * - Convert targets into a HashSet for O(1) membership checks.
     * - Perform a post-order DFS.
     * - For each node, count how many target IDs appear in its subtree.
     * - The first node from the bottom whose subtree count equals the total number of targets
     *   is exactly the lowest subtree root containing all targets.
     *
     * This works because post-order processes children before parent, so if a lower subtree
     * already contains all targets, we keep that answer and do not replace it with a higher node.
     *
     * @param root the root of the binary tree
     * @param targets list of distinct target category IDs
     * @return the category ID of the smallest subtree containing all targets, or -1 if any target is missing
     * Time complexity: O(n + k), where n is the number of nodes and k is the number of targets
     * Space complexity: O(h + k), where h is the tree height due to recursion stack, and k is for the target set
     */
    public int findSmallestSharedCategorySubtree(TreeNode root, List<Integer> targets) {
        if (root == null || targets == null || targets.isEmpty()) {
            return -1;
        }

        Set<Integer> targetSet = new HashSet<>(targets);
        int totalTargets = targetSet.size();

        Result result = dfs(root, targetSet, totalTargets);

        if (result.foundCount != totalTargets || result.answerNode == null) {
            return -1;
        }

        return result.answerNode.val;
    }

    /**
     * Performs a post-order DFS and returns:
     * - how many target IDs are found in the current subtree
     * - the smallest subtree root containing all targets if already determined
     *
     * Very detailed logic:
     * 1. Recurse into the left subtree.
     * 2. Recurse into the right subtree.
     * 3. Count whether the current node itself is one of the targets.
     * 4. Total targets in this subtree = left count + right count + self contribution.
     * 5. If the left subtree already found the final answer, keep it.
     * 6. Else if the right subtree already found the final answer, keep it.
     * 7. Else if the current subtree contains all targets, then the current node is the answer.
     *    This is correct because both children were checked first, so no lower node qualified.
     *
     * @param node current tree node being processed
     * @param targetSet set of target IDs for O(1) lookup
     * @param totalTargets total number of distinct target IDs we need to cover
     * @return a Result object containing subtree target count and the answer node if found
     * Time complexity: O(n), because each node is visited once
     * Space complexity: O(h), where h is the recursion depth
     */
    public Result dfs(TreeNode node, Set<Integer> targetSet, int totalTargets) {
        if (node == null) {
            return new Result(0, null);
        }

        // Step 1:
        // Recursively solve the left subtree first.
        // Because this is post-order traversal, we want complete information from children
        // before deciding anything about the current node.
        Result leftResult = dfs(node.left, targetSet, totalTargets);

        // Step 2:
        // Recursively solve the right subtree.
        Result rightResult = dfs(node.right, targetSet, totalTargets);

        // Step 3:
        // Check whether the current node itself is one of the requested target IDs.
        // If yes, it contributes 1 to the count of found targets in this subtree.
        int selfMatch = targetSet.contains(node.val) ? 1 : 0;

        // Step 4:
        // Combine the counts from:
        // - left subtree
        // - right subtree
        // - current node
        //
        // Because node values are unique in the tree and target IDs are distinct,
        // this count is exact and there is no risk of double counting.
        int totalFoundInSubtree = leftResult.foundCount + rightResult.foundCount + selfMatch;

        // Step 5:
        // If the left subtree already discovered the smallest valid subtree root,
        // we must preserve it.
        //
        // Why?
        // Because a valid answer found lower in the tree is always better (smaller / deeper)
        // than the current node or any ancestor.
        if (leftResult.answerNode != null) {
            return new Result(totalFoundInSubtree, leftResult.answerNode);
        }

        // Step 6:
        // If the left subtree did not already contain the final answer,
        // but the right subtree did, preserve the right subtree's answer.
        if (rightResult.answerNode != null) {
            return new Result(totalFoundInSubtree, rightResult.answerNode);
        }

        // Step 7:
        // If neither child subtree already contains all targets as a smaller valid answer,
        // then we check the current node.
        //
        // If the total number of targets found in this subtree equals the number of targets required,
        // then the subtree rooted at the current node contains every target.
        //
        // Since no lower node in either child qualified (otherwise answerNode would already be set),
        // the current node is exactly the LOWEST node whose subtree contains all targets.
        if (totalFoundInSubtree == totalTargets) {
            return new Result(totalFoundInSubtree, node);
        }

        // Step 8:
        // Otherwise, this subtree does not contain all targets,
        // and no answer has been found below it.
        return new Result(totalFoundInSubtree, null);
    }

    /**
     * Builds a binary tree from a level-order array representation.
     * Null values represent missing children.
     *
     * Example:
     * [10, 5, 15, 3, 7, 12, 18, null, 4]
     *
     * This helper is used only for demonstration in main.
     *
     * @param values level-order node values, where null means no node
     * @return the root of the constructed binary tree
     * Time complexity: O(n), where n is the number of array entries
     * Space complexity: O(n), due to the queue used during construction
     */
    public static TreeNode buildTree(Integer[] values) {
        if (values == null || values.length == 0 || values[0] == null) {
            return null;
        }

        TreeNode root = new TreeNode(values[0]);
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);

        int index = 1;

        while (!queue.isEmpty() && index < values.length) {
            TreeNode current = queue.poll();

            if (index < values.length && values[index] != null) {
                current.left = new TreeNode(values[index]);
                queue.offer(current.left);
            }
            index++;

            if (index < values.length && values[index] != null) {
                current.right = new TreeNode(values[index]);
                queue.offer(current.right);
            }
            index++;
        }

        return root;
    }

    /**
     * Demonstrates the solution using the sample inputs from the problem statement
     * and one extra case where a target is missing.
     *
     * @param args command-line arguments (not used)
     * @return nothing
     * Time complexity: O(n + k) per demonstrated test case
     * Space complexity: O(h + k) per demonstrated test case
     */
    public static void main(String[] args) {
        Solution solution = new Solution();

        // Example 1:
        // Tree: [10,5,15,3,7,12,18,null,4]
        // Targets: [4,7]
        // Expected output: 5
        Integer[] tree1 = {10, 5, 15, 3, 7, 12, 18, null, 4};
        TreeNode root1 = buildTree(tree1);
        List<Integer> targets1 = Arrays.asList(4, 7);
        int answer1 = solution.findSmallestSharedCategorySubtree(root1, targets1);
        System.out.println(answer1); // Expected: 5

        // Example 2:
        // Tree: [8,4,11,2,6,9,13,1,3,5,7]
        // Targets: [1,7,9]
        // Expected output: 8
        Integer[] tree2 = {8, 4, 11, 2, 6, 9, 13, 1, 3, 5, 7};
        TreeNode root2 = buildTree(tree2);
        List<Integer> targets2 = Arrays.asList(1, 7, 9);
        int answer2 = solution.findSmallestSharedCategorySubtree(root2, targets2);
        System.out.println(answer2); // Expected: 8

        // Extra example:
        // One target does not exist in the tree, so answer should be -1.
        List<Integer> targets3 = Arrays.asList(1, 7, 999);
        int answer3 = solution.findSmallestSharedCategorySubtree(root2, targets3);
        System.out.println(answer3); // Expected: -1
    }
}