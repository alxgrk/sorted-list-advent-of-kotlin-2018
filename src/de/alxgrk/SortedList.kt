package de.alxgrk

interface SortedMutableList<T> : Iterable<T> {
    val size: Int
    fun add(element: T)
    fun remove(element: T)
    operator fun get(index: Int): T
    operator fun contains(element: T): Boolean
}

fun <T : Comparable<T>> sortedMutableListOf(vararg elements: T): SortedMutableList<T> {
    return BTreeSortedMutableList(Comparator.naturalOrder<T>(), elements)
}

fun <T> sortedMutableListOf(comparator: Comparator<T>, vararg elements: T): SortedMutableList<T> {
    return BTreeSortedMutableList(comparator, elements)
}

class BTreeSortedMutableList<T>(
    comparator: Comparator<T>,
    elements: Array<out T>
) : SortedMutableList<T> {

    private val binTree: BinaryTree<T> = BinaryTree(comparator)

    override val size: Int
        get() = binTree.size

    init {
        elements.forEach { binTree += it }
    }

    override fun add(element: T) {
        binTree += element
    }

    override fun remove(element: T) {
        binTree -= element
    }

    override fun get(index: Int): T = binTree[index]

    override fun contains(element: T): Boolean = element in binTree

    override fun iterator(): Iterator<T> = binTree.toList().iterator()

}

class BinaryTree<T>(private val comparator: Comparator<T>) {

    private var root: BinaryTreeElement<T> = Empty()

    val size: Int
        get() = toList().size

    operator fun contains(element: T) = recContains(root, element)

    operator fun plusAssign(element: T) = when (root) {
        is Empty -> root = Node(element)
        is Node -> recAdd(root as Node, element)
    }

    operator fun minusAssign(element: T) = when (root) {
        is Empty -> {
        }
        is Node -> recRemove(root as Node, element) {
            root = Empty()
        }
    }

    operator fun get(index: Int): T = toList()[index]

    fun toList(): List<T> = intoList(mutableListOf(), root)

    private fun intoList(list: MutableList<T>, node: BinaryTreeElement<T>): List<T> {
        if (node !is Node)
            return list

        intoList(list, node.left)
        list.add(node.element)
        intoList(list, node.right)

        return list
    }

    private fun recAdd(node: Node<T>, element: T): Unit =
        when (comparator.compare(element, node.element)) {
            0 ->
                // TODO how to handle equal elements?
                if (element != node.element)
                    node.left = Node(element)
                else {
                }
            in Int.MIN_VALUE..-1 -> when (node.left) {
                is Empty -> node.left = Node(element)
                is Node -> recAdd(node.left as Node, element)
            }
            else -> when (node.right) {
                is Empty -> node.right = Node(element)
                is Node -> recAdd(node.right as Node, element)
            }
        }

    private fun recRemove(node: Node<T>, element: T, removal: (T) -> Unit): Unit =
        when (comparator.compare(element, node.element)) {
            0 ->
                // TODO how to handle non-equal elements?
                if (element == node.element) {
                    if (node.left is Empty && node.right is Empty) // 0 children
                        removal(element)
                    else if (node.left is Empty && node.right !is Empty) { // 1 child (right)
                        node.copyFrom(node.right as Node)
                    } else if (node.left !is Empty && node.right is Empty) { // 1 child (left)
                        node.copyFrom(node.left as Node)
                    } else { // 2 children
                        if (node.right is Node) {
                            val rightNode = node.right as Node
                            val successor = minimum(rightNode)
                            node.copyFrom(successor)
                            recRemove(rightNode, successor.element) {
                                node.left = Empty()
                            }
                        } else {
                        }
                    }
                } else {
                }
            in Int.MIN_VALUE..-1 ->
                if (node.left is Node)
                    recRemove(node.left as Node, element) {
                        node.left = Empty()
                    }
                else {
                }
            else ->
                if (node.right is Node)
                    recRemove(node.right as Node, element) {
                        node.right = Empty()
                    }
                else {
                }
        }

    private fun recContains(tree: BinaryTreeElement<T>, element: T): Boolean = when (tree) {

        is Node<T> -> when (comparator.compare(element, tree.element)) {
            0 -> true
            in Int.MIN_VALUE..-1 -> recContains(tree.left, element)
            else -> recContains(tree.right, element)
        }
        is Empty<T> -> false

    }

    fun minimum(root: Node<T>): Node<T> {
        var x: BinaryTreeElement<T> = root
        while ((x as Node).left !is Empty)
            x = x.left
        return x
    }

    fun maximum(root: Node<T>): Node<T> {
        var x: BinaryTreeElement<T> = root
        while ((x as Node).right !is Empty)
            x = x.right
        return x
    }

}

sealed class BinaryTreeElement<T>

class Node<T>(
    var element: T,
    var left: BinaryTreeElement<T>,
    var right: BinaryTreeElement<T>
) : BinaryTreeElement<T>() {

    constructor(element: T) : this(element, Empty(), Empty())

    fun copyFrom(another: Node<T>) {
        element = another.element
        left = another.left
        right = another.right
    }

}

class Empty<T> : BinaryTreeElement<T>()