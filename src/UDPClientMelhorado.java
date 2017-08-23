

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Random;

public class UDPClientMelhorado {

    private static final int MAXIMUM_BUFFER_SIZE = 1024;
    private DatagramSocket socket;
    private DatagramPacket request;
    private DatagramPacket response;
    
    // Essa classe tem metodos de Ping e UDP reliable com 5 tentativas, cada uma de 1seg caso não consiga a conexão/reposta a conexao será dada como perdida.

    public UDPClientMelhorado() throws SocketException {
        socket = new DatagramSocket();
    }

    public void enviarMensagem(String address, int port, byte[] msg) throws IOException{
        InetAddress addr = InetAddress.getByName(address);
        request = new DatagramPacket(msg, msg.length);
        request.setAddress(addr);
        request.setPort(port);
        socket.send(request);
    }
    

    // protocolo como ex ACK: valorDoAck Data ;
    // Servidor ira ler o ACK e tratar como conexao segura , pegando o valor do ACK recebido posteriormente enviando para o client a seguinte msg RACK: valorDoAck
    // o cliente vai receber a mensagem e verificar se o valor do Ack recebido é igual ao que ele enviou , se for ele considera como mensagem entregue senão ele tenta novamente ate atingir
    // o limite de tentativas.
    public void sendMessageUDPSeguro(String address, int port, String data) throws IOException {
    	int tentativas = 0;
    	Random rand = new Random();
    	while(true){
    		int valorACK = rand.nextInt(1024);
    		String msg = "ACK: " + valorACK + " " + data + " ;";
    		enviarMensagem(address, port,  msg.getBytes());
    		tentativas++;
	    	if(receiveMessageSeguro(valorACK) == false){
	    		System.out.println("Pacote Perdido, Fazendo uma nova tentativa");
	    	}else{
	    		System.out.println("Mensagem entregue");
	    		break;
	    	}
	    	if(tentativas == 5){
	    		System.out.println("Timeout,Sem conexão com servidor");
	    		break;
	    	}

    	}
    }
    
    public void estatisticasPing(double tempo[],String address,int nMax){
    	double minimo = 99999.0;
    	double maximo = 0.0;
    	double media = 0.0;
    	int nPerdidos = 0;
    	for(int i = 1; i <= nMax; i++){
    		if(tempo[i] == 999999){
    			nPerdidos++;
    			continue;
    		}
    		if(tempo[i]<minimo){
    			minimo = tempo[i];
    		}	
    		if(tempo[i]>maximo){
    			maximo = tempo[i];
    		}
    		media += tempo[i];
    	}
    	media = media /(nMax - nPerdidos);
    	System.out.println("Estatísticas do Ping para "+ address + ": ");
    	System.out.println("Pacotes: Enviados = " +nMax + ", Recebidos = " + (nMax - nPerdidos)+ ", Perdidos = " + nPerdidos+ " (" + (((double)nPerdidos / nMax )*100) +"% de perda)");
    	System.out.println("tempo minimo = " + minimo + ",tempo maximo = "+ maximo + ",tempo medio = " + media + " em ms");
    }
    //
    public void ping(String address, int port,int nMax) throws IOException{
        long comeco = System.currentTimeMillis();
    	double tTempos [] = new double[nMax+1];
    	for(int i = 1; i <= nMax; i++){
	    		while(true){
		    		String pingContexto = "PING: " + i + " " + comeco +" ;";
		    		enviarMensagem(address, port,  pingContexto.getBytes());
			    	if(receiveMessage() == null){
			    		System.out.println("PING: "+ i +" Perdido");
			    		tTempos[i] = 999999;
			    		break;
			    	}else{
			    		tTempos[i] = ((System.currentTimeMillis() - comeco) / 1000000);
			    		System.out.println("PING: " + i + " entregue em " + tTempos[i]);
			    		break;
			    	}
			    	
	    		}
    	}
    	estatisticasPing(tTempos,address,nMax);
    }
    
    
    public byte[] receiveMessage() throws IOException {
    	try{
	    	socket.setSoTimeout(1000); 
	        byte[] buffer = new byte[MAXIMUM_BUFFER_SIZE];
	        response = new DatagramPacket(buffer, buffer.length);
	        socket.receive(response);
	        return response.getData();
    	}catch (SocketTimeoutException e) {   		
    		return null;
    	}

    }
    
    public boolean receiveMessageSeguro(int valorACK) throws IOException {
    	try{
	    	socket.setSoTimeout(1000); 
	        byte[] buffer = new byte[MAXIMUM_BUFFER_SIZE];
	        response = new DatagramPacket(buffer, buffer.length);
	        socket.receive(response);
	        String[] protocolo = new String(response.getData()).split(" ");
	        if(protocolo[0].equals("RACK:")){
	        	if(Integer.parseInt(protocolo[1]) == valorACK){
	        		return true;
	        	}
	        }
	        	return false;
    	}catch (SocketTimeoutException e) {   		
    		return false;
    	}

    }

}