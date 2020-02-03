package floodresponse;

/** 
Class Lot is used for objects containing the landscape of the simulation
Lots contain attributes of latitude, longitude, elevation, flood height, <acreage>, land value and status
 **/

import java.util.ArrayList;

import ec.util.MersenneTwisterFast;
import sim.util.Bag;
import sim.util.distribution.Uniform;

public class Lot
{
	
	//Attributes
	int lotnumber, ownerID, lotID, saledate, houseCount;
	double latitude, longitude, elevation, saleprice, benefitPremium;
	int landuse = -1;  //-1 vacant lot, +1 built
	boolean flooded = false; // true if flooded
	String builtFlag = "empty"; //true is built out
	
	double floodheight = 0; //total flood height including initial elevation
	double landvalue = 0;

	//establish random number seed
	int rep = ABM.repno;
	long seed = 0000000000000000000000 + rep;			//seed for random number generator
	
	public ArrayList<Houses> housesOnLot = new ArrayList<Houses>();
	public ArrayList<Owner> ownerOfLot = new ArrayList<Owner>();
	
	int lotGauge = -999;
	 
	public Lot(int id, int owner, int lotnumber, double y, double x, double elev, int use, double value, double premium,
			String built, int count)
	{
		double elevdelta = ABM.qdistro.nextDouble();
		this.lotID = id; 
		this.ownerID=owner;
		this.lotnumber = lotnumber;
		this.longitude = x;
		this.latitude = y;
		this.elevation = elev-elevdelta;
		this.landuse = use;
		this.landvalue = value;
		this.builtFlag = built;
		this.houseCount = count;
		this.benefitPremium = premium; 
	}

	// Accessors
	public void setLanduse(int landusenumber) {
		landuse = landusenumber;
	}

	public void setElevation(double elev) {
		elevation = elev;
	}

	public void setGauge(int low) {
		lotGauge = low;
	}

	public void setFloodheight(double height) {
		floodheight = height;
	}
	
	public void setLandvalue(double worth) {
		landvalue = worth;
	}
	
	public void setFlooded(boolean wet) {
		flooded = wet;
	}
	
	public void addBuilding(Houses i) {
		housesOnLot.add(i);
		houseCount=houseCount+1;
	}

	public int getBuildingindex(Houses i){
		return housesOnLot.indexOf(i);
	}
	public void removeBuilding(int i){
		housesOnLot.remove(i);
		houseCount = houseCount - 1;
	}
	public int getBuildingcount(){
		return housesOnLot.size();
	}
	public Houses getBuilding(int i){
		return housesOnLot.get(i);
	}
	
	public double getX() {
		return longitude;
	}

	public double getY() {
		return latitude;
	}

	public int getLanduse() {
		return landuse;
	}

	public double getElevation() {
		return elevation;
	}

	public int getGauge() {
		return lotGauge;
	}

	public int getLotNumber() {
		return lotnumber;
	}
	
	public int getLotID() {
		return lotID;
	}

	public double getFloodheight() {
		return floodheight;
	}

	public double getLandvalue() {
		return landvalue;
	}


	public boolean getFlooded() {
		return flooded;
	}

	public String getBuildFlag() {
		return builtFlag;
	}

	public void setOwnerId(int lotcount) {
		this.ownerID = lotcount;	
	}

	public int getOwnerId() {
		return ownerID;
	}

	public int getOwnerindex(Owner i){
		return ownerOfLot.indexOf(i);
	}

	public void setOwner(Owner tempowner) {
		ownerOfLot.add(tempowner);
		
	}

	public void removeOwner(int ownercount) {
		ownerOfLot.remove(ownercount);
		
	}
	public Owner getOwner() {
		return ownerOfLot.get(0);
	}

	public double getLandBenefit() {
		double landBenefit = landvalue * benefitPremium;
		return landBenefit;
	}

	public int increaseBuildingIndex()
	{
		houseCount = houseCount + 1;
		return houseCount;
	}

	public void decreaseOwnerID() {
		ownerID = ownerID - 1;
	}
}
