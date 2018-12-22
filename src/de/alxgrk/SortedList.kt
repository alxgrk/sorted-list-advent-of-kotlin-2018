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
        node.elements.forEach { list.add(it) }
        intoList(list, node.right)

        return list
    }

    private fun recAdd(node: Node<T>, element: T): Unit =
        when (comparator.compare(element, node.comparableElement)) {
            0 ->
                if (element !in node.elements)
                    node.elements += element
                else {
                    // do nothing
                }
            in Int.MIN_VALUE..-1 -> when (node.left) {
                is Empty -> node.createLeft(Node(element))
                is Node -> recAdd(node.left as Node, element)
            }
            else -> when (node.right) {
                is Empty -> node.createRight(Node(element))
                is Node -> recAdd(node.right as Node, element)
            }
        }

    private fun recRemove(node: Node<T>, element: T, removal: (BinaryTreeElement<T>) -> Unit): Unit =
        when (comparator.compare(element, node.comparableElement)) {
            0 ->
                if (element in node.elements) {
                    // remove element
                    node.elements -= element

                    // if there are no comparable elements left, reorder tree
                    if (node.elements.isEmpty())

                        when ((node.left is Empty) to (node.right is Empty)) {

                            true to true -> // 0 children
                                removal(node.parent)
                            true to false -> // 1 child (right)
                                node.elements = (node.right as Node).elements
                            false to true ->// 1 child (left)
                                node.elements = (node.left as Node).elements
                            else -> { // 2 children
                                val rightNode = node.right as Node
                                val successor = minimum(rightNode)
                                node.elements = successor.elements
                                node.recRemove(successor)
                            }

                        }
                    else {
                        // do nothing
                    }
                } else {
                    // do nothing, the element does not exist anyway
                }
            in Int.MIN_VALUE..-1 ->
                if (node.left is Node)
                    recRemove(node.left as Node, element) { parent ->
                        if (parent is Node)
                            parent.left = Empty()
                    }
                else {
                }
            else ->
                if (node.right is Node)
                    recRemove(node.right as Node, element) { parent ->
                        if (parent is Node)
                            parent.right = Empty()
                    }
                else {
                }
        }

    private fun recContains(tree: BinaryTreeElement<T>, element: T): Boolean = when (tree) {

        is Node<T> -> when (comparator.compare(element, tree.comparableElement)) {
            0 -> element in tree.elements
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
    var elements: List<T>,
    var parent: BinaryTreeElement<T>,
    var left: BinaryTreeElement<T>,
    var right: BinaryTreeElement<T>
) : BinaryTreeElement<T>() {

    constructor(element: T, vararg elements: T) : this(mutableListOf(element, *elements), Empty(), Empty(), Empty())

    fun createLeft(left: Node<T>) {
        this.left = left
        left.parent = this
    }

    fun createRight(right: Node<T>) {
        this.right = right
        right.parent = this
    }

    var comparableElement = elements[0]

    fun recRemove(toRemove: Node<T>): Unit = when (toRemove) {
        right -> right = Empty()
        left -> left = Empty()
        else ->
            when {
                right is Node -> (right as Node).recRemove(toRemove)
                left is Node -> (left as Node).recRemove(toRemove)
                else -> {
                    // do nothing
                }
            }
    }

}

class Empty<T> : BinaryTreeElement<T>()