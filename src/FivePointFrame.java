import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

//在第236行设置服务器IP
class FivePointFrame extends JFrame
{
	public static void main(String[] args)
	{
		new FivePointFrame();
	}
	public String NEW_LINE=System.getProperty("line.separator");
	private static InetAddress serverIP;
	private static int SERVER_PORT=2048;
	private String name;
	private String peerName;
	public JTextArea notify;
	public FivePointPane fivePointPane;
	public JList user=new JList();
	public ClientThread clientThread;
	public ChatPane chatPane;
	private InetAddress thisIP;
	private int win;
	private int peerWin;
	private int lose;
	private int peerLose;
	private int escape;
	private int peerEscape;
	private boolean isHost=false;
	JTextArea info=new JTextArea(0,10);
	JTextArea peerInfo=new JTextArea(0,10);
	JButton createBtn=new JButton("创建游戏");
	JButton startBtn=new JButton("开始游戏");
	JButton addBtn=new JButton("加入游戏");
	JButton exitBtn=new JButton("退出游戏");
	JButton cancelBtn=new JButton("取消游戏");
	ExecutorService threadRunner=Executors.newFixedThreadPool(1);
	public FivePointFrame()
	{
		chatPane=new ChatPane(this);
		this.add(chatPane,BorderLayout.SOUTH);
		JPanel pane=new JPanel();
		pane.setLayout(new GridLayout(0,1));
		info.setBorder(BorderFactory.createTitledBorder("玩家信息"));
		peerInfo.setBorder(BorderFactory.createTitledBorder("对手信息"));
		info.setEditable(false);
		peerInfo.setEditable(false);
		JPanel myInfoPane=new JPanel();
		myInfoPane.setLayout(new GridLayout(0,1));
		myInfoPane.add(new JLabel(new ImageIcon("user.jpg")));
		myInfoPane.add(info);
		
		JPanel peerInfoPane=new JPanel();
		peerInfoPane.setLayout(new GridLayout(0,1));
		peerInfoPane.add(new JLabel(new ImageIcon("user.jpg")));
		peerInfoPane.add(peerInfo);
		
		pane.add(peerInfoPane);
		pane.add(myInfoPane);
		
		this.add(pane,BorderLayout.WEST);
		fivePointPane=new FivePointPane(this);
		this.add(fivePointPane);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent arg0) {
				clientThread.conveyMessage("exit");
				clientThread.setExit(true);	
				threadRunner.shutdownNow();
				System.exit(0);	
			}
		});
		notify=new JTextArea();
		notify.setLineWrap(true);
		notify.setEditable(false);
	
		final JPanel panel =new JPanel();
		panel.setLayout(new GridLayout(0,1));
		user.setBorder(BorderFactory.createTitledBorder("主机"));
		notify.setBorder(BorderFactory.createTitledBorder("游戏信息"));
		panel.add(new JScrollPane(user,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		panel.add(new JScrollPane(notify,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
		JPanel btnPane=new JPanel();
		
		btnPane.setLayout(new GridLayout(2,2));	
		btnPane.add(createBtn);
		btnPane.add(addBtn);		
		btnPane.add(cancelBtn);	
		btnPane.add(exitBtn);	
		
		JPanel btnsPane=new JPanel();
		btnsPane.setLayout(new BorderLayout());
		btnsPane.add(btnPane);
		btnsPane.add(startBtn,BorderLayout.EAST);
		panel.add(btnsPane);
		startBtn.setEnabled(false);
		//panel.add(startBtn);
		cancelBtn.setEnabled(false);
		cancelBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					clientThread.out.writeUTF("cancel");
					createBtn.setEnabled(true);
					addBtn.setEnabled(true);
					startBtn.setEnabled(false);
					cancelBtn.setEnabled(false);
					
				} catch (IOException e1) {
					e1.printStackTrace();
				}			
			}		
		});
		createBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				clientThread.conveyMessage("create");	
				notify.append("游戏创建成功"+System.getProperty("line.separator"));
				createBtn.setEnabled(false);
				startBtn.setEnabled(true);
				isHost=true;
				addBtn.setEnabled(false);
				cancelBtn.setEnabled(true);
			}
		});
		
		startBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				clientThread.conveyMessage("start");
				fivePointPane.clearChess();
				fivePointPane.setColor(1);
				startBtn.setEnabled(false);	
				addBtn.setEnabled(false);
				cancelBtn.setEnabled(false);
				createBtn.setEnabled(false);
				fivePointPane.isVict=false;
				fivePointPane.mouseAble=true;
				fivePointPane.isGaming=true;	
				isHost=false;
				SwingUtilities.invokeLater(new Runnable(){
					public void run() {
					notify.append("你的游戏已经开始"+NEW_LINE);
					notify.selectAll();
					repaint();
					}
					
				});
				
					
			}		
		});
		addBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {	
				if(!user.isSelectionEmpty() && !user.getSelectedValue().equals(name))
				{		
					notify.append("成功加入"+user.getSelectedValue()+System.getProperty("line.separator"));					
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {		
							addBtn.setEnabled(false);
							createBtn.setEnabled(false);
							cancelBtn.setEnabled(true);
						}
	
					});	
					clientThread.conveyMessage("add "+user.getSelectedIndex());	
				}
				else 
					SwingUtilities.invokeLater(new Runnable(){
						public void run() {			
							notify.append("请选择一个非己的主机!"+System.getProperty("line.separator"));
						}
					});			
			}	
		});
	
		exitBtn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				clientThread.conveyMessage("exit");
				clientThread.setExit(true);	
				threadRunner.shutdownNow();
				System.exit(0);
			}
		});
		JLabel l=new JLabel();
		this.setSize(843,650);
		BufferedImage icon=new BufferedImage(843,120,BufferedImage.TYPE_INT_RGB);
		Graphics2D gg=(Graphics2D)icon.createGraphics();
		gg.drawImage(new ImageIcon("c.jpg").getImage(), 0,0,843,120,null);
		ImageIcon image=new ImageIcon(icon);
		l.setIcon(image);
		this.add(l,BorderLayout.NORTH);
		this.add(panel,BorderLayout.EAST);
		this.setLocation(150,40);
		this.setResizable(false);
	
		
		try {
			thisIP=InetAddress.getLocalHost();
		} catch (UnknownHostException e1) {

			e1.printStackTrace();
		}	
		try {		
			serverIP=InetAddress.getByName("192.168.1.103");/////////////////服务器IP
			Socket server=new Socket(serverIP,SERVER_PORT);
			clientThread=new ClientThread(this,server);
			threadRunner.execute(clientThread);
		} catch (UnknownHostException e) {		
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	public void addPeerEscape()
	{
		this.peerEscape++;
		flushPeerInfo();
	}
	public void addWin()
	{
		this.win++;
		flushInfo();
	}
	public void addPeerWin()
	{
		this.peerWin++;
		flushPeerInfo();
	}
	public void addLose()
	{
		this.lose++;
		flushInfo();
	}
	public void addPeerLose()
	{
		this.peerLose++;
		flushPeerInfo();
	}
	public void setPeerInfo(String name,String win,String lose,String escape)
	{
		this.peerName=name;
		this.peerWin=Integer.parseInt(win);
		this.peerLose=Integer.parseInt(lose);
		this.peerEscape=Integer.parseInt(escape);
		flushPeerInfo();
		
	}
	public void setInfo(String name,String win,String lose,String escape)
	{
		this.name=name;
		this.win=Integer.parseInt(win);
		this.lose=Integer.parseInt(lose);
		this.escape=Integer.parseInt(escape);
		flushInfo();
		
	}
	public void flushPeerInfo()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
			peerInfo.setText("ID:"+peerName+NEW_LINE
					+"赢:"+peerWin+NEW_LINE
					+"输:"+peerLose+NEW_LINE
					+"逃跑:"+peerEscape+NEW_LINE
					+"等级:"+(3*peerWin-2*peerLose-5*peerEscape)/10
				);
				
			}
			
		});	
	}
	public void flushInfo()
	{
		SwingUtilities.invokeLater(new Runnable(){
			public void run() {
			info.setText("ID:"+name+NEW_LINE
					+"赢:"+win+NEW_LINE
					+"输:"+lose+NEW_LINE
					+"逃跑:"+escape+NEW_LINE
					+"等级:"+(3*win-2*lose-5*escape)/10
				);
				
			}
			
		});	
	}
	public void close()
	{
		System.exit(0);
	}
	
}
