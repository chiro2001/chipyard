import tempfile
import sys

from pygraphml import GraphMLParser
from pygraphml import Graph

if __name__ == '__main__':
    fname = sys.argv[1]
    parser = GraphMLParser()
    g = parser.parse(fname)
    g.show()