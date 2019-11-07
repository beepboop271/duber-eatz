public class Edge {
  int weight;
  NodeFaster self;
  NodeFaster neighbour;

  public Edge(NodeFaster self, NodeFaster neighbour, int weight) {
    this.weight = weight;
    this.self = self;
    this.neighbour = neighbour;
  }
}