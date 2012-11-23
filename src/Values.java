import java.io.Serializable;

class Values implements Serializable
{
	private String password;
	private int win;
	private int lose;
	private int escape;
	public String getPassword()
	{
		return password;
		
	}
	public void addWin()
	{
		win++;
	}
	public void addLose()
	{
		lose++;
	}
	public void addEscape()
	{
		escape++;
	}
	public Values(String password,int win,int lose,int escape)
	{
		this.password=password;
		this.win=win;
		this.lose=lose;
	}
	public String toString()
	{
		return win+" "+lose+" "+escape;
	}
}
