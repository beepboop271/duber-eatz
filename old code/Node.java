public class Node {
  public final char cell;
  public final int row;
  public final int col;
  public int id = -1;
  public int numEdges;

  // there was an attempt to encapsulate
  private Edge[] edges;

  public Node(int row, int col, char cell, int maxEdges) {
    this.row = row;
    this.col = col;
    this.cell = cell;
    this.edges = new Edge[maxEdges];
    this.numEdges = 0;
  }

  @Override
  public String toString() {
    return "N"+this.id+" '"+this.cell+"' ("+this.col+","+this.row+")";
  }

  private void addEdge(Edge newEdge) {
    for(int i = 0; i < this.numEdges; ++i) {
      if(newEdge.isBetterThan(this.edges[i]) || newEdge.equals(this.edges[i])) {
        this.edges[i] = newEdge;
        return;
      } else if(newEdge.isWorseThan(this.edges[i])) {
        return;
      }
    }
    this.edges[this.numEdges++] = newEdge;
  }

  public void attach(Node neighbour, int weight, String pathStr) {
    Edge newEdge = new Edge(this, neighbour, weight, pathStr);
    this.addEdge(newEdge);

    StringBuilder reversedPathStrBuilder = new StringBuilder(pathStr.length());
    char currentChar;

    for(int i = pathStr.length()-1; i > -1; --i) {
      currentChar = pathStr.charAt(i);
      if(currentChar == 'l') {
        reversedPathStrBuilder.append('r');
      } else if(currentChar == 'r') {
        reversedPathStrBuilder.append('l');
      } else if(currentChar == 'u') {
        reversedPathStrBuilder.append('d');
      } else if(currentChar == 'd') {
        reversedPathStrBuilder.append('u');
      }
    }

    newEdge = new Edge(neighbour, this, weight, reversedPathStrBuilder.toString());
    neighbour.addEdge(newEdge);
  }

  public void merge() {
    if(this.numEdges != 2) {
      return;
    }
    Node neighbour1 = this.getNeighbourNode(0);
    Node neighbour2 = this.getNeighbourNode(1);
    int newWeight = this.getNeighbourWeight(0)+this.getNeighbourWeight(1);

    String newPathStr = neighbour1.getNeighbourPath(this)+this.getNeighbourPath(neighbour2);
    neighbour1.remove(this);
    neighbour1.attach(neighbour2, newWeight, newPathStr);
    newPathStr = neighbour2.getNeighbourPath(this)+this.getNeighbourPath(neighbour1);
    neighbour2.remove(this);
    neighbour2.attach(neighbour1, newWeight, newPathStr);
  }

  public void mergeN(boolean remove) {
    Node[] neighbours = new Node[this.numEdges];
    int[] weights = new int[this.numEdges];
    String[] pathToThis = new String[this.numEdges];
    String[] pathFromThis = new String[this.numEdges];

    int originalNumEdges = this.numEdges;

    for(int i = 0; i < this.numEdges; ++i) {
      neighbours[i] = this.getNeighbourNode(i);
      weights[i] = this.getNeighbourWeight(i);
      pathToThis[i] = neighbours[i].getNeighbourPath(this);
      pathFromThis[i] = this.getNeighbourPath(i);

      if(remove) {
        neighbours[i].remove(this);
      }
    }

    for(int i = 0; i < originalNumEdges; ++i) {
      for(int j = 0; j < originalNumEdges; ++j) {
        if(i != j) {
          neighbours[i].addEdge(new Edge(neighbours[i],
                                         neighbours[j],
                                         weights[i]+weights[j],
                                         pathToThis[i]+pathFromThis[j]));
        }
      }
    }
  }

  public void cut() {
    if(this.numEdges != 1) {
      return;
    }
    this.getNeighbourNode(0).remove(this);
  }

  public void remove(int idx) {
    this.edges[idx] = null;
    for(int i = idx; i < this.numEdges-1; ++i) {
      this.edges[i] = this.edges[i+1];
    }
    --this.numEdges;
  }
  public void remove(Node nodeToRemove) {
    for(int i = 0; i < this.numEdges; ++i) {
      if(this.getNeighbourNode(i) == nodeToRemove) {
        this.remove(i);
        return;
      }
    }
  }

  public Node getNeighbourNode(int index) {
    if(index > this.numEdges-1) {
      return null;
    }
    return this.edges[index].neighbour;
  }

  public int getNeighbourWeight(int index) {
    if(index > this.numEdges-1) {
      return -1;
    }
    return this.edges[index].weight;
  }

  public String getNeighbourPath(int index) {
    if(index > this.numEdges-1) {
      return "";
    }
    return this.edges[index].pathStr;
  }
  public String getNeighbourPath(Node n) {
    for(int i = 0; i < this.numEdges; ++i) {
      if(n == this.getNeighbourNode(i)) {
        return this.edges[i].pathStr;
      }
    }
    return "";
  }

  public void setId(int id) {
    this.id = id;
  }
}