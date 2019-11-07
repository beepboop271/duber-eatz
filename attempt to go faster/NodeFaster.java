public class NodeFaster {
  char cell;
  int idx;
  Edge[] edges;
  int numEdges;

  public NodeFaster(int idx, char cell) {
    this.idx = idx;
    this.cell = cell;
    this.edges = new Edge[4];
    this.numEdges = 0;
  }

  // public String toString() {
  //   return "("+this.col+","+this.row+")";
  // }

  private void appendEdge(Edge newEdge) {
    if(this.numEdges >= 4) {
      System.out.println("something has gone very wrong");
    }
    this.edges[this.numEdges++] = newEdge;
  }

  public void attach(NodeFaster neighbour, int weight) {
    Edge newEdge = new Edge(this, neighbour, weight);
    this.appendEdge(newEdge);

    newEdge = new Edge(neighbour, this, weight);
    neighbour.appendEdge(newEdge);

    System.out.printf("attach %s to %s%n", this.toString(), neighbour.toString());
  }
}