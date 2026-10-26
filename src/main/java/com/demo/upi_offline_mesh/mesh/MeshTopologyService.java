package com.demo.upi_offline_mesh.mesh;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class MeshTopologyService {
    private final String GATEWAY_NODE_ID = "gateway";
    private final Map<String, MeshNode> nodes = new LinkedHashMap<>();

    public synchronized void buildRandomTopology(int deviceCount, int avgConnectionsPerNode){
        nodes.clear();

        MeshNode gateway = new MeshNode(GATEWAY_NODE_ID, true);
        nodes.put(gateway.getNodeId(), gateway);

        for(int i = 1; i<=deviceCount; i++){
            String id = "device-" + i;
            nodes.put(id, new MeshNode(id, false));
        }
        List<MeshNode> allNodes = new ArrayList<>(nodes.values());
        Random random = new Random();

        for(MeshNode node: allNodes){
            int connections = 1 + random.nextInt(avgConnectionsPerNode);
            for(int i = 0; i<connections; i++){
                MeshNode peer = allNodes.get(random.nextInt(allNodes.size()));
                if(!peer.getNodeId().equals(node.getNodeId())){
                    node.connectTo(peer);
                }
            }
        }
    }
    public MeshNode getNode(String nodeId){
        MeshNode node = nodes.get(nodeId);
        if(node==null){
            throw new  NoSuchElementException("No mesh Node" + nodeId);
        }
        return node;
    }
    public MeshNode getGateway(){
        return getNode(GATEWAY_NODE_ID);
    }

    public Collection<MeshNode> getAllNodes(){
        return nodes.values();
    }


    public boolean isTopologyBuilt(){
        return !nodes.isEmpty();
    }


}
