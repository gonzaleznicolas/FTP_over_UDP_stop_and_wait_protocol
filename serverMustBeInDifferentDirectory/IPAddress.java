import java.util.Scanner;
import java.net.*;

public class IPAddress {

	public static void main(String[] args) {
		try{
				InetAddress iAddress = InetAddress.getLocalHost();
				String ClientIP= iAddress.getHostAddress();
				System.out.println(ClientIP);
			}
			catch(Exception e){
				System.out.println("Error " + e);

			}
		
	}

}