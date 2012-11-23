import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextArea;


class ServerThread extends JFrame
{
	
	public static void main(String[]args) throws IOException
	{
		new ServerThread();	
	}
	private ServerSocket server;
	private ServerSocket verifySocket;
	private Map<String,Values> IDPass=new HashMap<String,Values>();
	private Map<ServerToClientThread,ServerToClientThread> map=new HashMap<ServerToClientThread,ServerToClientThread>();
	//private HashMap<ServerToClientThread,String> threadToName=new HashMap<ServerToClientThread,String>();
	public ArrayList<ServerToClientThread> host=new ArrayList<ServerToClientThread>();
	private ArrayList<ServerToClientThread> clients=new ArrayList<ServerToClientThread>();
	//private HashMap<String,ServerToClientThread> nameToThread=new HashMap<String,ServerToClientThread>();
	private static int LIMT=10;
	private ExecutorService app=Executors.newCachedThreadPool();
	private int count=0;
	
	public ServerThread() throws IOException
	{	
		super("服务器");
		ObjectInputStream objectIn=new ObjectInputStream(new FileInputStream("data.dat"));	
		try {
			IDPass=(HashMap<String,Values>)objectIn.readObject();
			objectIn.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		setVisible(true);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0) {
				try {
					ObjectOutputStream objectOut=new ObjectOutputStream(new FileOutputStream(new File("data.dat")));
					objectOut.writeObject(IDPass);
					objectOut.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				app.shutdownNow();	
				System.exit(0);
			}
		});	
		JTextArea text=new JTextArea();
		text.setText(""+InetAddress.getLocalHost());
		add(text);
		pack();
		ServerSocket server=new ServerSocket(2048);
	
		while(count<LIMT)
		{
			Socket client=server.accept();
			ServerToClientThread clientThread =new ServerToClientThread(client,this);
			app.execute(clientThread);
		}
	}
	
	public ServerToClientThread nameToThread(String name)
	{
		for(int i=0;i<clients.size();i++)
			if(clients.get(i).toString().equals(name))
				return clients.get(i);
		return null;
	}
	
	public void dealWithMessage(String str,ServerToClientThread thread) throws IOException
	{

		String[] message=str.split(" ");
		if(str.startsWith("chat"))
		{			
			String s="chat "+thread+" "+str.substring(str.indexOf(":"));
			if(message[1].equals("all"))
			{	
				for(int i=0;i<clients.size();i++)
					clients.get(i).out.writeUTF(s);
			
			}	
			else 
			{
				nameToThread(message[1]).out.writeUTF(s);
			}
		}

		else if(str.startsWith("create"))
		{
			if(host.contains(thread))return;
				host.add(thread);	
			
			/*for(ServerToClientThread clientThread:map.values())
			{
				clientThread.out.writeUTF("update "+host.toString());
			}*/
			for(int i=0;i<clients.size();i++)
				clients.get(i).out.writeUTF("update "+host.toString());
		}
		else if(str.startsWith("add"))
		{	
			int addedIndex=Integer.parseInt(message[1]);
			if(addedIndex<0 || addedIndex>=host.size())
			{
				thread.out.writeUTF("refuse");
				return ;
			}
			ServerToClientThread addedHost=host.get(addedIndex);
			if(map.containsKey(addedHost))
			{
				thread.out.writeUTF("refuse");
				return ;
			}
			
			thread.out.writeUTF("notrefuse");
			host.remove(addedHost);
			
			synchronized(map)
			{
			/*if(map.containsKey(thread))
			{
				ServerToClientThread t=map.get(thread);
				t.out.writeUTF("exit "+thread);
				host.add(t);
				map.remove(t);
				map.remove(thread);
			}*/			
			map.put(addedHost, thread);
			map.put(thread, addedHost);
			for(int i=0;i<clients.size();i++)
				clients.get(i).out.writeUTF("update "+host.toString());
			addedHost.out.writeUTF("add "+thread+" "+IDPass.get(thread.toString()));//
			thread.out.writeUTF("peer "+addedHost+" "+IDPass.get(addedHost.toString()));
			}
		}
		else if(str.startsWith("exit"))
		{
			if(thread.isGaming())
			{
				synchronized(map){
				map.get(thread).setGame(false);
				IDPass.get(thread.toString()).addEscape();		
				IDPass.get(map.get(thread).toString()).addWin();
				map.get(thread).setGame(false);
				}
			}
			clients.remove(thread);
			if(host.contains(thread))
				host.remove(thread);
			
			for(int i=0;i<clients.size();i++)
				{
					clients.get(i).out.writeUTF("update "+host.toString());
					clients.get(i).out.writeUTF("remove "+thread);
				}	

			synchronized(map){
				if(map.containsKey(thread))map.get(thread).out.writeUTF("exit "+thread);
			/////////////
				map.remove(map.get(thread));
			////////////
				map.remove(thread);
			}
		
			/*java.util.Iterator<Entry<ServerToClientThread, ServerToClientThread>> it=map.entrySet().iterator();
			while(it.hasNext())
			{
				Entry<ServerToClientThread, ServerToClientThread> t=it.next();
				if(t.getValue().equals(thread))
				{	
					t.getValue().out.writeUTF("exit "+thread);
					map.remove(t.getKey());
				}
				
			}*/
			thread.close();	
		}
		else if(str.startsWith("start"))
		{
			synchronized(map)
			{
			map.get(thread).out.writeUTF(str);
			if(host.contains(thread))host.remove(thread);
			thread.setGame(true);
			map.get(thread).setGame(true);
			}
			for(int i=0;i<clients.size();i++)
				clients.get(i).out.writeUTF("update "+host.toString());
		}
		else if(str.startsWith("opp"))
		{
			synchronized(map)
			{
				map.get(thread).out.writeUTF(str);
			}
		}
		else if(str.startsWith("win"))
		{
			//map.get(thread).out.writeUTF(str);
			IDPass.get(thread.toString()).addWin();
			synchronized(map)
			{
				map.get(thread).setGame(false);
				thread.setGame(false);
				IDPass.get(map.get(thread).toString()).addLose();	
				map.remove(map.get(thread));
				map.remove(thread);
			}
		
		}
		else if(str.startsWith("log"))
		{
			
			message=str.substring(4).split(" ");		
			if(IDPass.containsKey(message[0])&& IDPass.get(message[0]).getPassword().equals(message[1]) && !isUserIn(message[0]))//successful to log
			{	
				thread.setName(message[0]);
				clients.add(thread);
				for(int i=0;i<clients.size();i++)
				{
					clients.get(i).out.writeUTF("client "+clients);
				}
				count++;	
				
				thread.out.writeUTF("log "+message[0]+" "+IDPass.get(message[0]));
			}
			else
			{
				if(IDPass.containsKey(message[0])&& IDPass.get(message[0]).getPassword().equals(message[1]))
				{
					thread.out.writeUTF("retry repeat");
					return ;
				}
				thread.out.writeUTF("retry error");		
			}
			
		}
		else if(str.startsWith("error to log"))//登陆界面的退出
		{
			thread.close();
		}
		else if(str.startsWith("registe"))
		{
			message=str.substring(8).split(" ");
			if(!IDPass.containsKey(message[0]))
				IDPass.put(message[0],new Values(message[1],0,0,0));
			else
				thread.out.writeUTF("repeat registe");
		}
		else if(str.startsWith("cancel"))
		{
			if(map.containsKey(thread))
			{			
				map.get(thread).out.writeUTF("cancel "+thread);
				map.remove(map.get(thread));
				map.remove(thread);
			}
			if(host.contains(thread))	
				host.remove(thread);
			
			for(int i=0;i<clients.size();i++)
				clients.get(i).out.writeUTF("update "+host.toString());
			
		}
			
		
	}
	public boolean isUserIn(String str)
	{
		for(int i=0;i<clients.size();i++)
			if(clients.get(i).toString().equals(str))
				return true;
		return false;
	}
	
}
