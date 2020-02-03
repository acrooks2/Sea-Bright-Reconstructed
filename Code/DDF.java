package floodresponse;

/**	The class DDF includes the water elevation vs damage level matrix for each building type
**/

public class DDF 
{
	//initiation
	public int ddfStories=0;
	public int heightCount = 0;
	public int damageCount = 0;
	public double ddfHeight [] = new double [40];
	public double ddfDamage [] = new double [40];

	public DDF (int stories)
	{
		this.ddfStories = stories;
	}

	public int getDamageCount() 
	{
		return damageCount;
	}

	public int getStories() 
	{
		return ddfStories;
	}

	public double getHeight(int i) 
	{
		return ddfHeight[i];
	}

	public double getDamage(int i) 
	{
		return ddfDamage[i];
	}
	
	public void addDDFheight(double height)
	{
		ddfHeight[heightCount]=height;
		heightCount = heightCount + 1;
	}
	
	public void addDDFdamage(double damage)
	{
		ddfDamage[damageCount]=damage;
		damageCount = damageCount + 1;
		
	}
}
