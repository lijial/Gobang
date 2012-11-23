import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


class ClientThread implements Runnable
{
	private FivePointFrame frame;
	private Socket server;
	public DataInputStream in;
	public DataOutputStream out;
	private LogFrame logFrame;
	private boolean exit=false;
	
	public static String NEW_LINE=System.getProperty("line.separator");
	public ClientThread(FivePointFrame fivePointFrame,Socket server)
	{
		this.frame=fivePointFrame;
		this.server=server;
		try {
			in=new DataInputStream(server.getInputStream());
			out=new DataOutputStream(server.getOutputStream());

		} catch (IOException e) {
		
			e.printStackTrace();
		}
		
	}
	
	public void addChess(int x,int y) 
	{
		try {
			out.writeUTF("opp "+x+" "+y);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void dealMessage(String str)//chat who what 
										//opp x y
										//update host1 client1 host2 client2 ....
	{
		String[] message=str.split(" ");
		if(str.startsWith("chat"))
		{
			frame.notify.append(str.substring(5)+NEW_LINE);
			frame.notify.selectAll();		
		}
		else if(str.startsWith("opp"))
		{
			if(!frame.fivePointPane.isVict)
				frame.fivePointPane.addNetChess(Integer.parseInt(message[1]),Integer.parseInt(message[2]));	
		}
		else if(str.startsWith("update"))
		{
			
			str=str.substring(8,str.length()-1);
			message=str.split(",");
			frame.user.setListData(message);
		}
		else if(str.startsWith("notify"))
		{
			frame.notify.append(str.substring(8)+NEW_LINE);
			frame.notify.selectAll();
		}
		else if(str.startsWith("start"))
		{
			frame.fivePointPane.clearChess();
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					frame.fivePointPane.repaint();		
				}
				
			});
			frame.notify.append("你的游戏已经开始"+NEW_LINE);
			frame.notify.selectAll();		
			frame.addBtn.setEnabled(false);
			frame.cancelBtn.setEnabled(false);
			frame.createBtn.setEnabled(false);
			frame.fivePointPane.setColor(-1);
			frame.fivePointPane.mouseAble=false;
			frame.fivePointPane.isGaming=true;
			frame.fivePointPane.isVict=false;
		}
		else if(str.startsWith("exit"))//the peer is exited
		{
			if(!frame.fivePointPane.isVict && frame.fivePointPane.isGaming)
			{
				frame.notify.append("对手已退出游戏"+NEW_LINE);
				if(frame.fivePointPane.isGaming)
				{
					frame.addPeerEscape();
					frame.addWin();
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {
							frame.notify.append("你赢得了比赛"+NEW_LINE);
								frame.notify.selectAll();
						}
						
					});
					
				}	
				
				frame.fivePointPane.setEnd();
			}
			else
			{
				 frame.notify.append(str.substring(5)+"离开了房间"+NEW_LINE);
				 frame.notify.selectAll();
				 frame.startBtn.setEnabled(false);
				 frame.addBtn.setEnabled(true);
				 frame.createBtn.setEnabled(true);
			}
			frame.setPeerInfo("未知", "0", "0", "0");
			
		}
		/*else if(str.startsWith("win"))
		{
			
			frame.fivePointPane.isVict=true;
			frame.fivePointPane.isGaming=false;
			frame.fivePointPane.mouseAble=false;
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {
					frame.notify.append("真遗憾，你输了"+NEW_LINE);
					frame.notify.selectAll();
				}
				
			});
			frame.addLose();
			frame.addBtn.setEnabled(true);
			
		}*/
		else if(str.startsWith("add"))
		{
			str=str.substring(4);
			message=str.split(" ");
			frame.notify.append(message[0]+" 进入了房间"+NEW_LINE);
			frame.setPeerInfo(message[0],message[1],message[2],message[3]);
			frame.notify.selectAll();
			frame.startBtn.setEnabled(true);
			frame.cancelBtn.setEnabled(true);
		}
		else if(str.startsWith("peer"))//peer info
		{
			str=str.substring(5);
			message=str.split(" ");
			frame.setPeerInfo(message[0],message[1],message[2],message[3]);
		}
		else if(str.startsWith("client"))
		{
			str=str.substring(8,str.length()-1);
			String[] clients=str.split(",");	
			frame.chatPane.clients.removeAllItems();
			for(int i=0;i<clients.length;i++)
				frame.chatPane.clients.addItem(clients[i].trim());
			frame.chatPane.clients.addItem("all");
		
		}
		else if(str.startsWith("remove"))
		{
			frame.chatPane.clients.removeItem(str.substring(7));
		}
		else if(str.startsWith("retry"))
		{
			message=str.split(" ");
			if(message[1].equals("repeat"))
				JOptionPane.showMessageDialog(null, "请勿重复登录!");
			else 
				JOptionPane.showMessageDialog(null,"用户名或密码错误!");
			
		}
		else if(str.startsWith("log"))
		{
			frame.setVisible(true);	
			message=str.substring(4).split(" ");
			frame.setTitle("欢迎 "+message[0]+" 加入游戏"+"————五子棋  by 洪福兴");
			frame.setInfo(message[0],message[1],message[2],message[3]);
			logFrame.close();
		}
		else if(str.startsWith("repeat registe"))
		{
			JOptionPane.showMessageDialog(null, "此ID已被注册","换个ID试下吧",JOptionPane.OK_OPTION);
		}
		else if(str.startsWith("refuse"))
		{
			JOptionPane.showMessageDialog(null, "此主机已被加入，请选择其他主机加入");
		}
		else if(str.startsWith("refuse"))
		{
			JOptionPane.showMessageDialog(null, "此主机已被加入");
		}
		else if(str.startsWith("notrefuse"))
		{
			//JOptionPane.showMessageDialog(null, "成功加入主机");
			frame.addBtn.setEnabled(false);
			frame.createBtn.setEnabled(false);
			
		}
		else if(str.startsWith("cancel"))
		{
			final boolean flag;
			message=str.split(" " );	
			final String name=message[1];
			SwingUtilities.invokeLater(new Runnable(){
				public void run() {				
						frame.notify.append(name+" 取消了即将开始的游戏"+NEW_LINE);
						frame.addBtn.setEnabled(true);
						frame.cancelBtn.setEnabled(false);
						frame.createBtn.setEnabled(true);
						frame.startBtn.setEnabled(false);
		
					frame.notify.selectAll();
				}
				
			});
			
		}
	}
	
	public void conveyMessage(String str)
	{
		try {
			out.writeUTF(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void setExit(boolean b)
	{
		exit=b;	
		frame.close();
	}
	
	public void run() {
		logFrame=new LogFrame(this);
		while(!exit)
		{	
			try {
				//frame.fivePointPane.requestFocus();
				String message=in.readUTF();
				dealMessage(message);	
			} catch (IOException e) {	
				
			}
		}
		try {
			in.close();
			out.close();
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		
	}
	
	
}
