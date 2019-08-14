#########
# Trees #
#########

def tree(root, branches=[]):
    for branch in branches:
        assert is_tree(branch), 'branches must be trees'
    return [root] + list(branches)

def root(tree):
    return tree[0]

def branches(tree):
    return tree[1:]

def is_tree(tree):
    if type(tree) != list or len(tree) < 1:
        return False
    for branch in branches(tree):
        if not is_tree(branch):
            return False
    return True

def is_leaf(tree):
    return not branches(tree)

### +++ === ABSTRACTION BARRIER === +++ ###

def fib_tree(n):
    """Construct a Fibonacci tree.

    >>> fib_tree(1)
    [1]
    >>> fib_tree(3)
    [2, [1], [1, [0], [1]]]
    >>> fib_tree(5)
    [5, [2, [1], [1, [0], [1]]], [3, [1, [0], [1]], [2, [1], [1, [0], [1]]]]]
    """
    if n == 0 or n == 1:
        return tree(n)
    else:
        left, right = fib_tree(n-2), fib_tree(n-1)
        fib_n = root(left) + root(right)
        return tree(fib_n, [left, right])

def count_leaves(tree):
    """The number of leaves in tree.

    >>> count_leaves(fib_tree(5))
    8
    """
    if is_leaf(tree):
        return 1
    else:
        branch_counts = [count_leaves(b) for b in branches(tree)]
        return sum(branch_counts)

def leaves(tree):
    """The leaf values in tree.

    >>> leaves(fib_tree(5))
    [1, 0, 1, 0, 1, 1, 0, 1]
    """
    if is_leaf(tree):
        return [root(tree)]
    else:
        return sum([leaves(b) for b in branches(tree)], [])

def partition_tree(n, m):
    """Return a partition tree of n using parts of up to m.

    >>> partition_tree(2, 2)
    [2, [True], [1, [1, [True], [False]], [False]]]
    """
    if n == 0:
        return tree(True)
    elif n < 0:
        return tree(False)
    elif m == 0:
        return tree(False)
    else:
        left = partition_tree(n-m, m)
        right = partition_tree(n, m-1)
        return tree(m, [left, right])

def print_parts(tree, partition=[]):
    """Print the partitions encoded in a partition tree.

    >>> print_parts(partition_tree(6, 4))
    4 + 2
    4 + 1 + 1
    3 + 3
    3 + 2 + 1
    3 + 1 + 1 + 1
    2 + 2 + 2
    2 + 2 + 1 + 1
    2 + 1 + 1 + 1 + 1
    1 + 1 + 1 + 1 + 1 + 1
    """
    if is_leaf(tree):
        if root(tree):
            print(' + '.join(partition))
    else:
        left, right = branches(tree)
        m = str(root(tree))
        print_parts(left, partition + [m])
        print_parts(right, partition)
        