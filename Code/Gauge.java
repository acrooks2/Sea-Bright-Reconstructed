package floodresponse;

/**  This class provides functionality for the tide gauges (primarily storing gauge number, location and flood heights for each storm)
**/

public class Gauge
{
	//Attributes
	public int gaugeID = -1; //index number
	int gaugenumber = 0;
	double gaugelat, gaugelong, gaugereading = 0.0; //gaugereading is reading in MSL

	public double gaugeHeight [] = new double [100];	
	
	public Gauge (int id, int number, double y, double x)
	{
		this.gaugeID = id;
		this.gaugenumber = number;
		this.gaugelong = x;
		this.gaugelat = y;
	}

	//Accessors
	public void setFloodLevel(int floodNumber, double floodHeight)
	{
		gaugeHeight[floodNumber] = floodHeight;
	}

	public double getGaugeID() {
		return gaugeID;
	}

	public double getGaugenumber() {
		return gaugenumber;
	}
	
	public double getX() {
		return gaugelong;
	}

	public double getY() {
		return gaugelat;
	}

	public double getFloodLevel(int floodNumber)
	{
		return gaugeHeight[floodNumber];
	}
}
