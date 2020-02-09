
/**
	Class Houses is implemented to develop domiciles for occupant agents
 	Houses contain attributes of lot number, housing type, stories, floodproofing height, damage level, value, 
 **/

import sim.field.continuous.*;

import java.util.ArrayList;

import sim.engine.*;
import sim.util.*;
import sim.util.Double2D;

public class Houses 
{
	
	//Attributes
		//establish and initialize variables
	int id = -1; //index number
	int lotid = -999; //lot number
	int lotID = 0;
	int lotnumber = 0;
	int ownerID = -1;
	String flag = "";
	static int totalid = 0; //house count
	String hometype = "";
	double stories = 0;
	int style = 0;
	int totalhouse=0;
	int saleyear = 0;
	double saleprice = 0;
	int builtyear = 0;
	boolean floodproof = false; //true if floodproofing
	boolean occupied = false; //true if occupied
	double damagelevel = 0.0; //percentage flood depth
	double houseStatus = 0.0; //percentage of damage
	double floodproofHeight = 0.0;//elevation above ground level of floodproofing
	double floodHeight = 0.0;//resulting flooding
	double squarefootage = 0.0;//size of house for calculating home value
	double homeValue = 0.0;//price of house 
	double residualDamage = 0.0;
	public double damage = 0.0;
	Double2D loc = null;
	
	//Associations
	public ArrayList<Owner> ownerOfHouse = new ArrayList<Owner>();
	public ArrayList<Lot> lotOfHouse = new ArrayList<Lot>();

	public Houses(int id, int lotid, int lotnumber, int year, double price, int byear, double stories, String exterior, int style, double proof, double buildvalue, double sqft, ABM world)
	{
		this.id=id;  //lot index number
		this.lotID = lotid;
		this.lotnumber = lotnumber;
		this.saleyear = year;
		this.saleprice = price;
		this.builtyear = byear;
		this.stories = stories;
		this.hometype = exterior;
		this.style = style;
		this.floodproofHeight=proof;
		this.homeValue=buildvalue;
		this.squarefootage=sqft;
		if(proof > 0.0) {this.floodproof = true;}
		totalhouse = totalhouse + 1;
		
		Lot templot = ABM.allLots.get(lotid);
		lotOfHouse.add(templot);
		Double tlat = (templot.getY()-40.343)*1800;		//translated to 0,0 and scaled to 1 mile
		final Double tlong = (74.0055+templot.getX())*1800;
		loc = new Double2D (tlat, tlong);

		//storage of elevation data for floodplane determination
	}
	// Accessors
	public int getId() {return id;}
	public int getLotID(){return lotID;}
	public int getownerID() {return ownerID;}
	public int getLotNumber(){return lotnumber;}	
	public Double2D getlocation() {return loc;}
	public static int getTotalID(){return totalid;}
	public boolean checkFloodproof(){return floodproof;}
	public double getFloodproofheight(){return floodproofHeight;}
	public double getSquarefootage(){return squarefootage;}
	public double getHomevalue(){return homeValue;}
	public String getHomeType() {return hometype;}
	public double getStories(){return stories;}
	public int getStyle(){return style;}
	public int getSaleYear() {return saleyear;}
	public double getResidualDamage() {return residualDamage;}
	public double getDamage() {return damage;}
	public void setFloodproof(boolean floodProofed) {this.floodproof=true;}
	public void setResidualDamage (double damage) {this.residualDamage=damage;}
	public void setDamage (double damage) {this.damage=damage;}
	public void setId (int id) {this.id=id;}
	public void setLotId (int lotid) {this.lotID=lotid;}
	public void setLotNumber (int lotnumber) {this.lotnumber=lotnumber;}
	
	public double checkDestruction(double houseFloodlevel) 
	{
		double houseHeight = floodproofHeight + stories * 10; //assume single story (10ft) above floodproofing
		if (houseFloodlevel > houseHeight ) {houseStatus = 1.0;}
		else if(houseFloodlevel > floodproofHeight)
		{
			damagelevel = (houseFloodlevel-floodproofHeight);
			houseStatus = damagelevel;  //TODO Update with DDS
		} 
		else {houseStatus=0.0;}//
		return houseStatus;
	}

	public static void setTotalId(int newTotal)
	{
		totalid = newTotal;
	}
	
	public void setFloodHeight(double flooding) 
	{
		floodHeight = flooding;
	}
	
	public double getFloodHeight() 
	{
		return floodHeight;
	}

	public void setOwnerId(int owner) {
		this.ownerID=owner;
	}

	public void setOwner(Owner tempowner) {
		ownerOfHouse.add(tempowner);
	}

	public Owner getOwner() 
	{
		return ownerOfHouse.get(0);
	}

	public Lot getLot() 
	{
		return lotOfHouse.get(0);
	}

	public void setFloodproofHeight(double newheight)
	{
		this.floodproofHeight=newheight;
	}

	public void decreaseOwnerID() 
	{
		ownerID = ownerID - 1;
	}
}

