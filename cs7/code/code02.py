# Addition/Multiplication
2 + 3 * 4 + 5
(2 + 3) * (4 + 5)

# Division
618 / 10
618 // 10
618 % 10
from operator import truediv, floordiv, mod
floordiv(618, 10)
truediv(618, 10)
mod(618, 10)

# Approximation
5 / 3
5 // 3
5 % 3

# Multiple return values
def divide_exact(n, d):
    return n // d, n % d
quotient, remainder = divide_exact(618, 10)

# Dostrings, doctests, & default arguments
def divide_exact(n, d=10):
    """Return the quotient and remainder of dividing N by D.

    >>> quotient, remainder = divide_exact(618, 10)
    >>> quotient
    201
    >>> remainder
    4
    """
    return floordiv(n, d), mod(n, d)

# Conditional expressions
def absolute_value(x):
    """Return the absolute value of X.

    >>> absolute_value(-3)
    3
    >>> absolute_value(0)
    0
    >>> absolute_value(3)
    3
    """
    if x < 0:
        return -x
    elif x == 0:
        return 0
    else:
        return x

# Summation via while
i, total = 0, 0
while i < 3:
    i = i + 1
    total = total + i
total

def fib(n):
    """Compute the nth Fibonacci number, for N >= 2.

    >>> fib(8)
    21
    """
    pred, curr = 0, 1   # 0th and 1st Fibonacci numbers
    k = 1               # Tracks which Fib number is curr
    while k < n:
        pred, curr = curr, pred + curr
        k = k + 1
    return curr

def pyramid(n):
    """Sum increasing and decreasing sequences.
    
    >>> pyramid(10)
    100
    """
    a, b, total = 0, n, 0
    while b:
        a, b = a+1, b-1
        total = total + a + b
    return total
    