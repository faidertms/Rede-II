import java.net.*;
import java.util.*;


public class ServidorUDP {
	private static final double LOSS_RATE = 0.3;
	private static final int AVERAGE_DELAY = 100;
	int ACK;


	public static void main(String[] args) throws Exception{
		int port = 8000;
		Random random = new Random();
		byte[] buf = null;
		DatagramSocket socket = new DatagramSocket(port);
		String[] protocolo = null;
		
		while (true) {
			System.out.println("Aguardando uma nova conexão....");
			DatagramPacket request = new DatagramPacket(new byte[1024], 1024);
			socket.receive(request);
			printData(request);
			
			protocolo = new String(request.getData()).split(" ");
			System.out.println(protocolo[0] + " - " +protocolo[1]+ " - " + protocolo[2]);
			
			if(protocolo[0].equals("ACK:")){
				String pacoteReposta = "R"+protocolo[0]+ " " + protocolo[1] + " ;" ;// fica +/- assim RPING: + N + tempo no momento da chegada (R = resposta)
				buf = pacoteReposta.getBytes();
			}
			
			if(protocolo[0].equals("PING:")){
				long tempoPing = System.currentTimeMillis() - Long.valueOf(protocolo[2]).longValue();
				System.out.println("Tempo: " + (tempoPing / 1000000));
				String pingContexto = "R"+protocolo[0] + " " + protocolo[1] + " " + tempoPing; // fica +/- assim RPING: + N + tempo no momento da chegada (R = resposta)
				buf = pingContexto.getBytes();
			}
			
			//gerar pacote loss
			if (random.nextDouble() < LOSS_RATE) {
				System.out.println("Perdendo dados.");
				continue; 
			}
			
			if(new String(request.getData()) == "exit"){
				break;
			}
			// Simulate network delay.
			Thread.sleep((int) (random.nextDouble() * 2 * AVERAGE_DELAY));
			confirmacao(request,socket,buf);
			}
	}

	private static void confirmacao(DatagramPacket request, DatagramSocket socket,byte[] buf) throws Exception{
		//byte[] buf = request.getData();
		InetAddress clientHost = request.getAddress();
		int clientPort = request.getPort();
		DatagramPacket reply = new DatagramPacket(buf, buf.length, clientHost, clientPort);
		socket.send(reply);
		System.out.println("Pacote Enviado.");
		
	}
	
	private static void printData(DatagramPacket request) throws Exception{
		byte[] buf = request.getData();
		System.out.println(
		"Pacote recebido de: " + 
		request.getAddress().getHostAddress() + ": " +new String(buf));
	}
}