/**
 * [Edge.java]
 * @version 2
 * @author Kevin Qiao
 * Class for a directed edge in a graph
 */

 public class Edge {
  public Node self;
  public Node neighbour;
  public int weight;
  public String pathStr;
  
  /** 
   * [Edge]
   * Constructor for a directed Edge.
   * @param self      The source Node of the Edge.
   * @param neighbour The destination Node of the Edge.
   * @param weight    The distance between the two nodes (edge weight)
   * @param pathStr   A String which contains the steps to get
   *                  from the source to the destination.
   */
  public Edge(Node self, Node neighbour, int weight, String pathStr) {
    this.self = self;
    this.neighbour = neighbour;
    this.weight = weight;
    this.pathStr = pathStr;
  }


  /** 
   * [toString]
   * Returns a string that indicates the id,
   * cell, and position of the node.
   * @return String, text representation of the edge.
   */
  @Override
  public String toString() {
    return "Edge w"+this.weight
           +" ["+this.self.toString()+"]"
           +" to"
           +" ["+this.neighbour.toString()+"]";
  }
  

  /** 
   * [isComparable]
   * Checks to see if two Edges have the same
   * source and destination.
   * @param otherEdge The other Edge to check against.
   * @return boolean, whether or not the two Edges
   *         are comparable.
   */
  public boolean isComparable(Edge otherEdge) {
    return (this.self == otherEdge.self)
           && (this.neighbour == otherEdge.neighbour);
  }

  
  /** 
   * [isBetterThan]
   * Checks to see if this Edge connects the
   * same places as another Edge but with a
   * shorter weight.
   * @param otherEdge The other Edge to check against.
   * @return boolean, whether or not this Edge
   *         connects the same locations and has a
   *         shorter weight.
   */
  public boolean isBetterThan(Edge otherEdge) {
    return this.isComparable(otherEdge)
           && (this.weight < otherEdge.weight);
  }

  
  /** 
   * [isWorseThan]
   * Checks to see if this Edge connects the
   * same places as another Edge but with a
   * longer weight.
   * @param otherEdge The other Edge to check against.
   * @return boolean, whether or not this Edge
   *         connects the same locations and has a
   *         longer weight.
   */
  public boolean isWorseThan(Edge otherEdge) {
    return this.isComparable(otherEdge)
           && (this.weight > otherEdge.weight);
  }

  
  /** 
   * [isBetterThan]
   * Checks to see if this Edge has the
   * same start, end, and weight as another
   * Edge.
   * @param otherEdge The other Edge to check against.
   * @return boolean, whether or not this Edge
   *         connects the same locations with
   *         the same weight.
   */
  public boolean equals(Edge otherEdge) {
    return this.isComparable(otherEdge)
           && (this.weight == otherEdge.weight);
  }
}