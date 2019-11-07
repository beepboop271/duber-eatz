public class Edge {
  public int weight;
  public String pathStr;
  public Node self;
  public Node neighbour;

  public Edge(Node self, Node neighbour, int weight, String pathStr) {
    this.weight = weight;
    this.self = self;
    this.neighbour = neighbour;
    this.pathStr = pathStr;
  }

  public boolean isComparable(Edge otherEdge) {
    return (this.self == otherEdge.self)
           && (this.neighbour == otherEdge.neighbour);
  }

  public boolean isBetterThan(Edge otherEdge) {
    return this.isComparable(otherEdge)
           && (this.weight < otherEdge.weight);
  }

  public boolean isWorseThan(Edge otherEdge) {
    return this.isComparable(otherEdge)
           && (this.weight > otherEdge.weight);
  }

  public boolean equals(Edge otherEdge) {
    return this.isComparable(otherEdge)
           && (this.weight == otherEdge.weight);
  }
}