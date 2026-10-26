package com.demo.upi_offline_mesh.mesh;
import lombok.Getter;
import java.util.HashSet;
import java.util.Set;

@Getter
public class MeshNode {
   private final String nodeId;
   private final Set<String> peerIds = new HashSet<>();
   private final boolean gateway;

   public MeshNode(String nodeId, boolean gateway){
       this.nodeId = nodeId;
       this.gateway = gateway;
   }
   public void connectTo(MeshNode other){
       this.peerIds.add(other.getNodeId());
       other.peerIds.add(this.nodeId);
   }
}
