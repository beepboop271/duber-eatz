public class Node {
  public final char cell;
  public final int row;
  public final int col;

  private Edge[] edges;
  int numEdges;

  public Node(int row, int col, char cell) {
    this.row = row;
    this.col = col;
    this.cell = cell;
    this.edges = new Edge[4];
    this.numEdges = 0;
  }

  @Override
  public String toString() {
    return "("+this.col+","+this.row+")";
  }

  private void addEdge(Edge newEdge) {
    if(this.numEdges >= 4) {
      System.out.println("something has gone very wrong");
    }
    for(int i = 0; i < numEdges; ++i) {
      if(newEdge.isBetterThan(this.edges[i]) || newEdge.equals(this.edges[i])) {
        this.edges[i] = newEdge;
        return;
      }
    }
    this.edges[this.numEdges++] = newEdge;
  }

  public void attach(Node neighbour, int weight, String pathStr) {
    Edge newEdge = new Edge(this, neighbour, weight, pathStr);
    this.addEdge(newEdge);

    String reversedPathStr = "";
    char currentChar;

    for(int i = pathStr.length()-1; i > -1; --i) {
      currentChar = pathStr.charAt(i);
      if(currentChar == 'l') {
        reversedPathStr += 'r';
      } else if(currentChar == 'r') {
        reversedPathStr += 'l';
      } else if(currentChar == 'u') {
        reversedPathStr += 'd';
      } else if(currentChar == 'd') {
        reversedPathStr += 'u';
      }
    }

    newEdge = new Edge(neighbour, this, weight, reversedPathStr);
    neighbour.addEdge(newEdge);

    // System.out.printf("attach %s to %s w%d%n", this.toString(), neighbour.toString(), weight);
  }

  public void merge() {
    if(this.numEdges != 2) {
      return;
    }
    Node neighbour1 = this.getNeighbourNode(0);
    Node neighbour2 = this.getNeighbourNode(1);
    int newWeight = this.getNeighbourWeight(0)+this.getNeighbourWeight(1);

    String newPathStr = neighbour1.getNeighbourPath(this)+this.getNeighbourPath(neighbour2);
    neighbour1.replace(this, neighbour2, newWeight, newPathStr);
    newPathStr = neighbour2.getNeighbourPath(this)+this.getNeighbourPath(neighbour1);
    neighbour2.replace(this, neighbour1, newWeight, newPathStr);
  }

  public void cut() {
    if(this.numEdges != 1) {
      return;
    }
    this.getNeighbourNode(0).remove(this);
  }

  public void remove(int idx) {
    this.edges[idx] = null;
    for(int i = idx; i < numEdges-1; ++i) {
      this.edges[idx] = this.edges[idx+1];
    }
    // this.edges[numEdges] = null;
    --numEdges;
  }
  public void remove(Node nodeToRemove) {
    for(int i = 0; i < numEdges; ++i) {
      if(this.getNeighbourNode(i) == nodeToRemove) {
        this.remove(i);
        return;
      }
    }
  }

  public void replace(Node originalConn, Node newConn, int newWeight, String newPathStr) {
    for(int i = 0; i < this.numEdges; ++i) {
      if(this.edges[i].neighbour == originalConn) {
        this.edges[i].neighbour = newConn;
        this.edges[i].weight = newWeight;
        this.edges[i].pathStr = newPathStr;
        for(int j = 0; j < this.numEdges; ++j) {
          if(j != i) { 
            if(this.edges[i].isBetterThan(this.edges[j])) {
              if(i < j) {
                this.remove(j);
              } else {
                this.edges[j] = this.edges[i];
                this.remove(i);
              }
              return;
            } else if(this.edges[j].isBetterThan(this.edges[i])) {
              if(j < i) {
                this.remove(i);
              } else {
                this.edges[i] = this.edges[j];
                this.remove(j);
              }
              return;
            }
          }
        }
        return;
      }
    }
  }

  public Node getNeighbourNode(int index) {
    if(index > this.numEdges-1) {
      System.out.println("uh oh");
    }
    return this.edges[index].neighbour;
  }

  public int getNeighbourWeight(int index) {
    if(index > this.numEdges-1) {
      System.out.println("stop");
    }
    return this.edges[index].weight;
  }

  public String getNeighbourPath(int index) {
    if(index > this.numEdges-1) {
      System.out.println("aslkdjdslk");
    }
    return this.edges[index].pathStr;
  }
  public String getNeighbourPath(Node n) {
    for(int i = 0; i < this.numEdges; ++i) {
      if(n == this.getNeighbourNode(i)) {
        return this.edges[i].pathStr;
      }
    }
    System.out.println("no");
    return "";
  }
}