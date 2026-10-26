package com.demo.upi_offline_mesh.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MeshPacket {
    private String packetId;
    private int ttl;
    private  long createdAt;
    private String cipherText;

}
