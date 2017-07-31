package pl.debuguj.tcp.client;

import pl.debuguj.tcp.client.ClientTcp.Connection;

public interface ClientTcpListener {
        //status
        public void onConnected(ClientTcp client);
	
        public void onDisconnected(ClientTcp client);
        
        public void onConnecting(ClientTcp client);

	public void onMessageReceived(String msg); 
	        
        //sending message       
	public void onMessageSent(ClientTcp client, String msg);
        
        //others
        public void onInternalError(ClientTcp client, Connection toServer, String error);

}
