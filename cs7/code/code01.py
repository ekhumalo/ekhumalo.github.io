# Numeric expressions
2019
2000 + 19
0 + 1 // 2 + 3 + 4 * ((5 // 6) + 7 * 8 * 9)

# Call expressions
max(2.0, 1.5)
pow(100, 2)
pow(2, 100)
max(1, -2, 3, -4)
max(min(1, -2), min(pow(3, 5), -4))

# Importing and arithmetic with call expressions
from operator import add, mul
add(1, 2)
mul(3, 3)
mul(add(2, mul(4, 6)), add(3, 5))
mul(10, add(mul(add(2, mul(4, 6)), add(3, 5)), -6.5))

from math import sqrt
sqrt(169)

# Objects
# Note: Download from http://composingprograms.com/shakespeare.txt
shakes = open('shakespeare.txt')
text = shakes.read().split()
len(text)
text[:25]
text.count('the')
text.count('thou')
text.count('you')
text.count('forsooth')
text.count(',')

# Sets
words = set(text)
len(words)
max(words)
max(words, key=len)

# Reversals
'draw'[::-1]
{w for w in words if w == w[::-1] and len(w)>4}
{w for w in words if w[::-1] in words and len(w) == 4}
{w for w in words if w[::-1] in words and len(w) > 6}

# Imports
from math import pi
pi * 71 / 223
from math import sin
sin(pi/2)

# Assignment
radius = 10
2 * radius
area, circ = pi * radius * radius, 2 * pi * radius
radius = 20

# Function values
max
max(3, 4)
f = max
f
f(3, 4)
max = 7
f(3, 4)
f(3, max)
f = 2
# f(3, 4)

# User-defined functions
from operator import add, mul
add(2, 3)
mul(2, 3)

def square(x):
    return mul(x, x)

square(21)
square(add(2, 5))
square(square(3))

def sum_squares(x, y):
    return add(square(x), square(y))
sum_squares(3, 4)
sum_squares(5, 12)

def circ():
    return 2 * pi * radius

# Name conflicts
def square(square):
    return mul(square, square)
square(4)

# Print
-2
print(-2)
'CS7'
print('CS7')
print(1, 2, 3)
None
print(None)
x = -2
x
x = print(-2)
x
print(print(1), print(2))