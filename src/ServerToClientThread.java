import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


class ServerToClientThread implements Runnable
{
	Socket client;
	DataInputStream in;
	DataOutputStream out;
	ServerThread agent;
	private String name;
	boolean exit=false;
	private boolean isGaming=false;
	public ServerToClientThread(Socket client,ServerThread agent) throws IOException
	{
		this.client=client;
		this.agent=agent;	
	}
	public void setName(String name)
	{
		this.name=name;
	}
	public String toString()
	{
		return name;
	}
	public boolean equals(Object o)
	{
		if(this==o)return true;
		if(this==null)return false;
		if(o.getClass()!=getClass())return false;
		ServerToClientThread t=(ServerToClientThread)o;
		return t.in.equals(in) && t.out.equals(out) && agent==t.agent;
	}
	
	public void close()
	{
		try {
			in.close();
			out.close();
			client.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		exit=true;
		
		
	}
	public void setGame(boolean b)
	{
		isGaming=b;
	}
	public boolean isGaming()
	{
		return isGaming;
	}

	public void run() {
		try {	
			in=new DataInputStream(client.getInputStream());
			out=new DataOutputStream(client.getOutputStream());
			try {
				String mess=in.readUTF();	
				agent.dealWithMessage(mess, this);//just for log
			} catch (IOException e) {
				
			}	
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(!exit)
				out.writeUTF("update "+agent.host);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while(!exit)
		{		
			try {
				String mess=in.readUTF();	
				agent.dealWithMessage(mess, this);
			} catch (IOException e) {
				
			}
		}
		
		
		
	}
	
}
