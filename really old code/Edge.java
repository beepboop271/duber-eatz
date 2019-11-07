public class Edge {
  int weight;
  String pathStr;
  Node self;
  Node neighbour;

  public Edge(Node self, Node neighbour, int weight, String pathStr) {
    this.weight = weight;
    this.self = self;
    this.neighbour = neighbour;
    this.pathStr = pathStr;
  }

  public boolean isBetterThan(Edge otherEdge) {
    return (this.self == otherEdge.self)
           && (this.neighbour == otherEdge.neighbour)
           && (this.weight < otherEdge.weight);
  }

  public boolean equals(Edge otherEdge) {
    return (this.self == otherEdge.self)
           && (this.neighbour == otherEdge.neighbour)
           && (this.weight == otherEdge.weight);
  }
}