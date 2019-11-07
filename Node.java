/**
 * [Node.java]
 * @version 3
 * @author Kevin Qiao
 * Class for a node in a graph
 */

public class Node {
  public final int row;
  public final int col;
  public final char cell;
  public int numEdges;
  public int id;

  // there was an attempt to encapsulate
  private Edge[] edges;

  
  /** 
   * [Node]
   * Constructor for a Node.
   * @param row      Y position of this node.
   * @param col      X position of this node.
   * @param cell     The type of position this node represents.
   * @param maxEdges The size of the internal array for edges.
   */
  public Node(int row, int col, char cell, int maxEdges) {
    this.row = row;
    this.col = col;
    this.cell = cell;
    this.edges = new Edge[maxEdges];
    this.numEdges = 0;
    this.id = -1;
  }

  
  /** 
   * [toString]
   * Returns a string that indicates the id,
   * cell, and position of the node.
   * @return String, text representation of this node.
   */
  @Override
  public String toString() {
    return "Node "+this.id
           +" '"+this.cell+"'"
           +" ("+this.col+","+this.row+")";
  }

  
  /** 
   * [addEdge]
   * Considers adding an edge to the node.
   * If there is already a shorter edge to the
   * same destination node, ignore the addition.
   * If there is already a longer edge to the
   * same destination node, replace that edge.
   * Otherwise, append the edge at the end.
   * @param newEdge The Edge object to add.
   */
  private void addEdge(Edge newEdge) {
    for (int i = 0; i < this.numEdges; ++i) {
      if (newEdge.isBetterThan(this.edges[i])
            || newEdge.equals(this.edges[i])) {
        // replace a worse edge with the new better one
        this.edges[i] = newEdge;
        return;
      } else if (newEdge.isWorseThan(this.edges[i])) {
        // don't replace a better edge with the worse one
        return;
      }
    }
    // append
    this.edges[this.numEdges++] = newEdge;
  }

  
  /** 
   * [attach]
   * Connects two nodes together by creating one
   * directed Edge from this Node to the other and
   * one from the other Node to this Node.
   * @param neighbour The Node to connect.
   * @param weight    The distance between the two nodes (edge weight).
   * @param pathStr   A String which contains the steps to get
   *                  from this Node to the other Node.
   */
  public void attach(Node neighbour, int weight, String pathStr) {
    Edge newEdge = new Edge(this, neighbour, weight, pathStr);
    this.addEdge(newEdge);

    // directed edge must be formed in both ways to get
    // undirected graph, but the path string only works
    // in one way, so flip it and invert each direction
    // to get the right path string
    StringBuilder reversedPathStrBuilder = new StringBuilder(pathStr.length());
    char currentChar;

    for (int i = pathStr.length()-1; i > -1; --i) {
      currentChar = pathStr.charAt(i);
      if (currentChar == 'l') {
        reversedPathStrBuilder.append('r');
      } else if (currentChar == 'r') {
        reversedPathStrBuilder.append('l');
      } else if (currentChar == 'u') {
        reversedPathStrBuilder.append('d');
      } else if (currentChar == 'd') {
        reversedPathStrBuilder.append('u');
      }
    }

    newEdge = new Edge(neighbour, this,
                       weight,
                       reversedPathStrBuilder.toString());
    neighbour.addEdge(newEdge);
  }

  
  /** 
   * [merge]
   * Simplifies three nodes and two edges
   * into two nodes with one edge.
   * If three nodes are like: a--x-o then
   * a structure like a---o is equivalent.
   */
  public void merge() {
    if (this.numEdges != 2) {
      return;
    }
    Node neighbour1 = this.getNeighbourNode(0);
    Node neighbour2 = this.getNeighbourNode(1);
    int newWeight = this.getNeighbourWeight(0)+this.getNeighbourWeight(1);
    String newPathStr;

    // path from 1-2 in 1-x-2 is the path from 1-x + x-2
    newPathStr = neighbour1.getNeighbourPath(this)
                 + this.getNeighbourPath(neighbour2);
    neighbour1.remove(this);
    neighbour1.attach(neighbour2, newWeight, newPathStr);

    newPathStr = neighbour2.getNeighbourPath(this)
                 + this.getNeighbourPath(neighbour1);
    neighbour2.remove(this);
    neighbour2.attach(neighbour1, newWeight, newPathStr);
  }

  
  /** 
   * [mergeN]
   * Performs the same idea as the merge method
   * but for a node with any number of connections
   * by removing the node and creating every
   * possible path through the node. For example,
   * a node with 3 edges to 3 nodes (1, 2, 3) around
   * it can be simplified by connecting 1, 2; 1, 3; 2, 3.
   * @param remove Whether or not to remove the node after
   *               creating all the connections.
   */
  public void mergeN(boolean remove) {
    Node[] neighbours = new Node[this.numEdges];
    int[] weights = new int[this.numEdges];
    String[] pathToThis = new String[this.numEdges];
    String[] pathFromThis = new String[this.numEdges];

    int originalNumEdges = this.numEdges;

    // record all values needed then remove the Node
    for (int i = 0; i < this.numEdges; ++i) {
      neighbours[i] = this.getNeighbourNode(i);
      weights[i] = this.getNeighbourWeight(i);
      pathToThis[i] = neighbours[i].getNeighbourPath(this);
      pathFromThis[i] = this.getNeighbourPath(i);

      if (remove) {
        neighbours[i].remove(this);
      }
    }

    for (int i = 0; i < originalNumEdges; ++i) {
      for (int j = 0; j < originalNumEdges; ++j) {
        if (i != j) {
          // connect every combination of neighbours
          // other than connecting a node to itself
          neighbours[i].addEdge(new Edge(neighbours[i],
                                         neighbours[j],
                                         weights[i]+weights[j],
                                         pathToThis[i]+pathFromThis[j]));
        }
      }
    }
  }

  
  /** 
   * [cut]
   * Removes a dead end node.
   */
  public void cut() {
    if (this.numEdges != 1) {
      return;
    }
    this.getNeighbourNode(0).remove(this);
  }

  
  /** 
   * [remove]
   * Removes the connection at the given index and
   * shift all the other connections so there isn't
   * a null in the middle of the array.
   * @param index The index to remove.
   */
  public void remove(int index) {
    this.edges[index] = null;
    for (int i = index; i < this.numEdges-1; ++i) {
      this.edges[i] = this.edges[i+1];
    }
    --this.numEdges;
  }
  
  /** 
   * [remove]
   * Finds and removes the connection which leads
   * to the given Node.
   * @param nodeToRemove The node to find and remove.
   */
  public void remove(Node nodeToRemove) {
    for (int i = 0; i < this.numEdges; ++i) {
      if (this.getNeighbourNode(i) == nodeToRemove) {
        this.remove(i);
        return;
      }
    }
  }

  
  /** 
   * [getNeighbourNode]
   * Retrieves a neighbouring Node from the Edge array.
   * @param index The index in the Edge array to get.
   * @return Node, the Node requested.
   */
  public Node getNeighbourNode(int index) {
    if (index > this.numEdges-1) {
      return null;
    }
    return this.edges[index].neighbour;
  }

  
  /** 
   * [getNeighbourWeight]
   * Retrieves the weight to a neighbouring Node from
   * the Edge array.
   * @param index The index in the Edge array to get.
   * @return int, the weight requested.
   */
  public int getNeighbourWeight(int index) {
    if (index > this.numEdges-1) {
      return -1;
    }
    return this.edges[index].weight;
  }

  
  /** 
   * [getNeighbourPath]
   * Retrieves the path String to a neighbouring Node
   * from the Edge array.
   * @param index The index in the Edge array to get.
   * @return String, the path String requested.
   */
  public String getNeighbourPath(int index) {
    if (index > this.numEdges-1) {
      return "";
    }
    return this.edges[index].pathStr;
  }
  
  /** 
   * [getNeighbourPath]
   * Retrieves the path String to the Node given by
   * searching for it in the Edge array.
   * @param nodeToFind The Node to get the path String to.
   * @return String, the path String requested.
   */
  public String getNeighbourPath(Node nodeToFind) {
    for (int i = 0; i < this.numEdges; ++i) {
      if (nodeToFind == this.getNeighbourNode(i)) {
        return this.edges[i].pathStr;
      }
    }
    return "";
  }

  
  /** 
   * [setId]
   * Set the id of the node.
   * @param id The id to set.
   */
  public void setId(int id) {
    this.id = id;
  }
}