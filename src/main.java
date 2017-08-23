

import java.io.IOException;
import java.net.SocketException;
import java.util.Scanner;

public class main {

    public static void main(String[] args) {

        try {
        	
            UDPClientMelhorado client = new UDPClientMelhorado();
            client.ping("localhost", 8000,10);
            while (true) {
            	System.out.println();
                // Lê do console uma mensagem a ser enviada para o servidor.
                Scanner scanner = new Scanner(System.in);
                System.out.println("Digite uma mensagem a ser enviada ao servidor: ");
                String requestMessage = scanner.nextLine();
 
                if(requestMessage.equalsIgnoreCase("exit")){
                    client.sendMessageUDPSeguro("localhost", 8000, requestMessage);
                    System.out.println("Encerrando o cliente...");
                    break;
                } else {
                    client.sendMessageUDPSeguro("localhost", 8000, requestMessage);


                }

            }
            
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}