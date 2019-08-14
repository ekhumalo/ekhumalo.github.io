#Representation
def str_repr_demos():
    from fractions import Fraction
    half = Fraction(1, 2)
    half
    print(half)
    str(half)
    repr(half)

    s = 'hello world'
    str(s)
    repr(s)
    "'hello world'"
    repr(repr(repr(s)))
    eval(eval(eval(repr(repr(repr(s))))))
    # Errors: eval('hello world')

# Implementing generic string functions

class Dog:
    """A Dog."""
    def __init__(self):
        self.__repr__ = lambda: 'drogon'
        self.__str__ = lambda: 'drogon the dog'

    def __repr__(self):
        return 'Dog()'

    def __str__(self):
        return 'a dog'

def print_bear():
    drogon = Dog()
    print(drogon)
    print(str(drogon))
    print(repr(drogon))
    print(drogon.__repr__())
    print(drogon.__str__())

def repr(x):
    return type(x).__repr__(x)

def str(x):
    t = type(x)
    if hasattr(t, '__str__'):
        return t.__str__(x)
    else:
        return repr(x)

# Ratio numbers

class Ratio:
    """A mutable ratio.

    >>> f = Ratio(9, 15)
    >>> f
    Ratio(9, 15)
    >>> print(f)
    9/15
    >>> f.gcd
    3
    >>> f.numer = 6
    >>> f.denom
    10
    >>> f.gcd
    2
    >>> f
    Ratio(6, 10)
    >>> f.denom = 5
    >>> f
    Ratio(3, 5)

    >>> Ratio(1, 3) + Ratio(1, 6)
    Ratio(1, 2)
    >>> f + 1
    Ratio(8, 5)
    >>> 1 + f
    Ratio(8, 5)
    >>> 1.4 + f
    2.0
    """
    def __init__(self, n, d):
        self.gcd = gcd(n, d)
        self._numer = n // self.gcd
        self._denom = d // self.gcd

    def __repr__(self):
        return 'Ratio({0}, {1})'.format(self.numer, self.denom)

    def __str__(self):
        return '{0}/{1}'.format(self.numer, self.denom)

    def __add__(self, other):
        if isinstance(other, Ratio):
            n = self.numer * other.denom + self.denom * other.numer
            d = self.denom * other.denom
        elif isinstance(other, int):
            n = self.numer + self.denom * other
            d = self.denom
        else:
            return float(self) + other
        r = Ratio(n, d)
        r.gcd = 1
        return r

    __radd__ = __add__

    def __float__(self):
        return self.numer / self.denom

    @property
    def numer(self):
        return self._numer * self.gcd

    @property
    def denom(self):
        return self._denom * self.gcd

    @numer.setter
    def numer(self, value):
        assert value % self._numer == 0
        self.gcd = value // self._numer       

    @denom.setter
    def denom(self, value):
        assert value % self._denom == 0
        self.gcd = value // self._denom       


def gcd(x, y):
    """Return the greatest common divisor of integers x & y.
    
    >>> gcd(12, 8)
    4
    """
    while x != y:
        x, y = abs(x-y), min(x, y)
    return x


# Time

def count(f):
    """Return a counted version of f with a call_count attribute.

    >>> def fib(n):
    ...     if n == 0 or n == 1:
    ...         return n
    ...     else:
    ...         return fib(n-2) + fib(n-1)
    >>> fib = count(fib)
    >>> fib(20)
    6765
    >>> fib.call_count
    21891
    """
    def counted(*args):
        counted.call_count += 1
        return f(*args)
    counted.call_count = 0
    return counted

from math import sqrt

def divides(k, n):
    """Return whether k evenly divides n."""
    return n % k == 0

def factors(n):
    """Count the positive integers that evenly divide n.

    >>> factors(576)
    21
    """
    total = 0
    for k in range(1, n+1):
        if divides(k, n):
            total += 1
    return total

def factors_fast(n):
    """Count the positive integers that evenly divide n.

    >>> factors_fast(576)
    21
    """
    sqrt_n = sqrt(n)
    k, total = 1, 0
    while k < sqrt_n:
        if divides(k, n):
            total += 2
        k += 1
    if k * k == n:
        total += 1
    return total
            
# Space

def count_frames(f):
    """Return a counted version of f with a max_count attribute.

    >>> def fib(n):
    ...     if n == 0 or n == 1:
    ...         return n
    ...     else:
    ...         return fib(n-2) + fib(n-1)
    >>> fib = count_frames(fib)
    >>> fib(20)
    6765
    >>> fib.open_count
    0
    >>> fib.max_count
    20
    >>> fib(25)
    75025
    >>> fib.max_count
    25
    """
    def counted(n):
        counted.open_count += 1
        counted.max_count = max(counted.max_count, counted.open_count)
        result = f(n)
        counted.open_count -= 1
        return result
    counted.open_count = 0
    counted.max_count = 0
    return counted

def fib(n):
    """The nth Fibonacci number.

    >>> fib(20)
    6765
    """
    if n == 0 or n == 1:
        return n
    else:
        return fib(n-2) + fib(n-1)

# Exponentiation

def exp(b, n):
    """Return b to the n.

    >>> exp(2, 10)
    1024
    """
    if n == 0:
        return 1
    else:
        return b * exp(b, n-1)

def square(x):
    return x*x

def exp_fast(b, n):
    """Return b to the n.

    >>> exp_fast(2, 10)
    1024
    """
    if n == 0:
        return 1
    elif n % 2 == 0:
        return square(exp_fast(b, n//2))
    else:
        return b * exp_fast(b, n-1)

# Overlap

def overlap(a, b):
    """Count the number of items that appear in both a and b.

    >>> overlap([1, 3, 2, 2, 5, 1], [5, 4, 2])
    3
    """
    count = 0
    for item in a:
        if item in b:
            count += 1
    return count