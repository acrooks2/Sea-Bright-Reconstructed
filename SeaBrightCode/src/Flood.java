
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.distribution.Uniform;
import sim.engine.SimState;
import sim.engine.Steppable;
import ec.util.MersenneTwisterFast;

//Flood provides the scheduler for flood events, determines damage and calls owners to determine their responses

public class Flood implements Steppable
{
	private static final String surge = null;
	private static final String floodproof = null;

	//parameters
	ABM world;
	
	int rep = ABM.repno;
	double floodbias = ABM.floodproofBias;
	double damagebias = ABM.damageBias;
	double riskLevel = ABM.riskmitigate;
	double hurricanes = ABM.futurehurricanes;
	double floodlevel;
	Houses maxHouse = null;
	int floodnumber = -999;
	
	Houses currentHouse = null;
	double maxHouseUtility = -123456789;
	double houseRepairCost = 123456789;
	double currentHouseUtility = -87654321;
	double floodproofingCost = 0;
	double houseCost = 0;
	public double nextHouseFloodHeight = 0;

	//  Gauge Readings
	public int gaugeID[]= new int [100];
	public double gaugelat[]= new double [100];	
	public double gaugelong[]= new double [100];	
	public double gaugereading[] = new double [100];

	//Cost-Benefit parameters
	double mitigateBenefit = -99999999;
	double mitigateCost = 999999999;
	double repairedValue = -99999999;
	double repairFunds = -99999999;
	double mitigatedValue = -99999999; 
	double mitigateFunds = -999999999;
	double repairCost = 999999999;
	
	//Initialize statistics
	int floodedHouses = 0;
	int floodedFPHouses = 0;
	int floodedNonFPHouses = 0;
	int destroyedHouses = 0;
	int destroyedFPHouses = 0;
	int destroyedNonFPHouses = 0;
	int damagedHouses = 0;
	int damagedFPHouses = 0;
	int damagedNonFPHouses = 0;
	int undamagedHouses = 0;
	int undamagedFPHouses = 0;
	int undamagedNonFPHouses = 0;
	int undamagedFloodedFPHouses = 0;
	int destroyedRebuilds = 0;
	int destroyedFPRebuilds = 0;
	int destroyedNonFPRebuilds = 0;
	int damagedRebuilds = 0;
	int damagedFPRebuilds = 0;
	int damagedNonFPRebuilds = 0;
	int destroyedRelocates = 0;
	int destroyedFPRelocates = 0;
	int destroyedNonFPRelocates = 0;
	int damagedRelocates = 0;
	int damagedFPRelocates = 0;
	int damagedNonFPRelocates = 0;
	int destroyedNewCon = 0;
	int destroyedFPNewCon = 0;
	int destroyedNonFPNewCon = 0;
	int damagedNewCon = 0;
	int damagedFPNewCon = 0;
	int damagedNonFPNewCon = 0;
	int destroyedDeparted = 0;
	int DamagedDeparted = 0;
	int dryLots = 0;
	int wetLots = 0;
	int dryHouses = 0;
	
	double costfloodedHouses = 0;
	double costfloodedFPHouses = 0;
	double costfloodedNonFPHouses = 0;
	double costdestroyedHouses = 0;
	double costdestroyedFPHouses = 0;
	double costdestroyedNonFPHouses = 0;
	double costdamagedHouses = 0;
	double costdamagedFPHouses = 0;
	double costdamagedNonFPHouses = 0;
	double costdetroyedRebuilds = 0;
	double costdetroyedFPRebuilds = 0;
	double costdetroyedNonFPRebuilds = 0;
	double costdamagedRebuilds = 0;
	double costdamagedFPRebuilds = 0;
	double costdamagedNonFPRebuilds = 0;
	double costdestroyedRelocates = 0;
	double costdestroyedFPRelocates = 0;
	double costdestroyedNonFPRelocates = 0;
	double costdamagedRelocates = 0;
	double costdamagedFPRelocates = 0;
	double costdamagedNonFPRelocates = 0;
	double costdestroyedNewCon = 0;
	double costdestroyedFPNewCon = 0;
	double costdestroyedNonFPNewCon = 0;
	double costdamagedNewCon = 0;
	double costdamagedFPNewCon = 0;
	double costdamagedNonFPNewCon = 0;
	
	double sealevel = 0;
	double rebuildCost = 0;
	
	double floodEventCost = 0;
	double floodEventFPCost = 0;
	double floodEventnonFPCost = 0;
	double floodEventDestroyedCost = 0;
	double floodEventDestroyedFPCost = 0;
	double floodEventDestroyednonFPCost = 0;
	double floodEventDamagedCost = 0;
	double floodEventDamagedFPCost = 0;
	double floodEventDamagednonFPCost = 0;
	double eventFloodCount = 0;

	public static double floodCost = 0;
	public static double floodFPCost = 0;
	public static double floodnonFPCost = 0;
	public static double floodDestroyedCost = 0;
	public static double floodDestroyedFPCost = 0;
	public static double floodDestroyednonFPCost = 0;
	public static double floodDamagedCost = 0;
	public static double floodDamagedFPCost = 0;
	public static double floodDamagednonFPCost = 0;
	public static double totalFloodCount = 0;

	//initialize random number generator
	long seed = 1234567890;
	MersenneTwisterFast randex = new MersenneTwisterFast(seed);
	
	public Flood(SimState state, int floodno, int [] num, double[] lat, double[] glong, double[] gheight)
	{
		for (int i=0; i<100; i++)
		{
			gaugeID[i]=-999;
			gaugelat[i]=0.0;
			gaugelong[i]=0.0;
			gaugereading[i] = 0.0;
		}
		
		this.world = (ABM) state;
		this.gaugeID=num;
		this.gaugelat=lat;
		this.gaugelong=glong;
		this.gaugereading=gheight;
		this.floodnumber=floodno;
	}
//	@Override
	public void step(SimState state)
	{
		this.world = (ABM) state;

		double gt = 0;
		
		//write flood outputs
		double t = state.schedule.getTime();

		if(Ticker.reportflood !=null)
		{
			try
			{
				gt = state.schedule.getTime();
				Gauge g = ((Gauge)ABM.TideList.get(0));
				double gid = g.getGaugeID();
				double gf = g.getFloodLevel(floodnumber);
				
				Ticker.reportflood.write("Pre-Flood******************** " + gt + "\t" + floodlevel +  " *********************************");
				Ticker.reportflood.newLine();
				Ticker.reportflood.write("HouseID" + "\t" + "lotno" +  "\t" + "FloodHeight" +  "\t" + "DDF" +  "\t" + "style"+  "\t" + "floors" 
						+  "\t" + "floodyear" + "\t" + "sex" + "\t" + "sexflag" + "\t" + "income" + "\t" + "exp"  + "\t" + "risk height"   + "\t" + "risk level");	
				Ticker.reportflood.newLine();					
				
				System.out.println("calculating flood heights for: " + floodnumber);

				//identify flooded lots 
				for (int i = 0; i < ABM.totalLots; i++)
				{
					Lot l = ((Lot)ABM.allLots.get(i));
					int lotG = l.getGauge();
					double lotE = l.getElevation();
					Gauge xGauge = ((Gauge)ABM.TideList.get(lotG));
					double gaugeE = xGauge.getFloodLevel(floodnumber);
					double xflood = gaugeE - lotE;
					if (xflood < 0)
					{
						l.setFlooded(false);
						l.setFloodheight(0.0);
						dryLots = dryLots + 1;
					}
					else 
					{
						l.setFlooded(true);
						l.setFloodheight(xflood);
						wetLots = wetLots +1;
					}
					
					boolean problem = l.getFlooded();
					double waterheight = l.getFloodheight();
				}

				//determine house flood levels above floodproofing
				System.out.println ("calculate building water levels");
				for (int i = 0; i < ABM.totalLots; i++) //for efficiency assumes town is mostly built out
				{	
					Lot l = ((Lot)ABM.allLots.get(i));
					
					int n = l.getBuildingcount();
					
					for (int j = 0; j < n; j++)
					{
						Houses h = l.getBuilding(j);
						double fp = h.getFloodproofheight();
						double fh = l.getFloodheight();
						
						if (fp < fh)
						{
							double flooding = fh - fp;
							h.setFloodHeight(flooding);
							Owner tempowner = h.getOwner();
							tempowner.setOwnerExperienceFlag(1.0);
							int home = l.getBuildingindex(h);
							int homeno = h.getId();
						}
					}
				}
				
				//determine building damage levels, iterate through the flood table until the correct height is found
				System.out.println("calculate building damage levels");  //TODO improve look up
				for (int i=0; i < ABM.totalHouses; i++)
				{
					double ddfdamage = 0.0;
					Houses temphouse = ABM.houseList.get(i);
					Integer housestyle = temphouse.getStyle();
					Double floodheight = temphouse.getFloodHeight();    
					Double heightguide = -1.0;
					int damageguide = 0;
					if (housestyle.equals(3))  //splitlevel
					{
						DDF tempDDF = ABM.damageFunction.get(3);
						while (floodheight > heightguide)
						{
							ddfdamage = tempDDF.getDamage(damageguide);
							heightguide = tempDDF.getHeight(damageguide);
							damageguide = damageguide +1;
						}
					}
					else
					{
						Double housestories = temphouse.getStories();
						if (housestories < 2.0)
						{
							DDF tempDDF = ABM.damageFunction.get(0);
							while (floodheight > heightguide)
							{
								ddfdamage = tempDDF.getDamage(damageguide);
								heightguide = tempDDF.getHeight(damageguide);
								damageguide = damageguide +1;
							}
						}
						else if (housestories < 3.0)
						{
							DDF tempDDF = ABM.damageFunction.get(1);
							while (floodheight > heightguide)
							{
								ddfdamage = tempDDF.getDamage(damageguide);
								heightguide = tempDDF.getHeight(damageguide);
								damageguide = damageguide +1;
							}
						}
						else 
						{
							DDF tempDDF = ABM.damageFunction.get(2);
							while (floodheight > heightguide)
							{
								ddfdamage = tempDDF.getDamage(damageguide);
								heightguide = tempDDF.getHeight(damageguide);
								damageguide = damageguide +1;
							}
						}
					}
					int iyear = (int) gt;					
					Owner tempowner = temphouse.getOwner();

					//for flooded houses set flood heights
					if(floodheight > 0.0)
					{
						double adjDDFdamage = ddfdamage * (1 + ABM.damageBias);
						temphouse.setDamage(adjDDFdamage);
						tempowner.setOwnerRiskHeight(iyear, floodheight);
						tempowner.setOwnerFloodYear(iyear);
						tempowner.setOwnerFloodHeight(floodheight);
					}
				}

				//record flood height info
				for (int i=0; i < ABM.totalHouses; i++)
				{
					Houses temphouse = ABM.houseList.get(i);
					double floodheight = temphouse.getFloodHeight();
					double damage = temphouse.getDamage();	
					String type = temphouse.getHomeType();
					double stories = temphouse.getStories();
					Owner tempowner = temphouse.getOwner();
					int lotno = tempowner.getOwnerLotID();
					String sex = tempowner.getOwnerSex();
					double sexflag = tempowner.getSexFlag();
					double income = tempowner.getOwnerIncome();
					double experience = tempowner.getOwnerExperience();
					int fyear = tempowner.getOwnerFloodYear();
					double fheight = tempowner.getOwnerFloodLevel();
					double oriskheight = tempowner.getOwnerRiskHeight();
					double orisklevel = tempowner.getOwnerRiskLevel();
				
					Ticker.reportflood.write(i + "\t" + lotno + "\t" + floodheight + "\t" + damage + "\t" + type + "\t" + stories  + "\t" + fyear + "\t" + sex + "\t" + sexflag + "\t" + income + "\t" + experience + "\t" + oriskheight + "\t" + orisklevel);	
					Ticker.reportflood.newLine();	
				}
			}
			catch (IOException e)
			{
				System.err.println("error at Flood69 report null");
				e.printStackTrace();
			}

			//determine homeowner responses to destruction and damage
			int housecheck = ABM.totalHouses;
			System.out.println("caluculate owner responses " + housecheck);
			try  //write flood data titles
			{
			Ticker.reportflood.write("owner id" + "\t" + "lotno" + "\t" + "lotnumber" + "\t" + "lotelev" + "\t" + "freeboard" + "\t" + "lotlat" + "\t" + "lotlong"
					+ "\t" + "floodheight" + "\t" + "damage" + "\t" + "homevalue" + "\t" + "repairCost" + "\t" + "type" + "\t" + "stories" + "\t" + "flood year" + "\t"  
					+ "flood height" + "\t" + "sex" + "\t" + "sexflag" + "\t" + "race" + "\t" + "whiteflag" + "\t" + "age" + "\t" + "income" + "\t" + "scaledincome" + "\t" + "experience" + "\t" + "risk height" + "\t" + "risk level" + "\t" + "hstyle"  + "\t" + "tempDDF"
					+ "\t" + "repairFunds" + "\t" + "mitigateBenefit" + " \t" + "mitigateCost" + "\t" + "mitigateFunds" + "\t" + "repairCost" + "\t" + "repairedValue"
					+ "\t" + "mitigatedValue" + "\t" + "decision");	
			Ticker.reportflood.newLine();					
			}
			catch (IOException e)
			{
				System.err.println("error at flood report null");
				e.printStackTrace();
			}

			//iterate through the houses to determine damage and responses
			int istart = ABM.totalHouses - 1;
			for (int i=istart; i >= 0; i--)
			{
				boolean flood = false;
				boolean repair = false;
				boolean mitigate = false;
				boolean insure = false;
				String decision = "";
				Houses temphouse = ABM.houseList.get(i);
				double tempDDF = temphouse.getDamage();
				Lot templot = temphouse.getLot();
				flood = templot.getFlooded();
				if(!flood) 
				{
					dryHouses = dryHouses + 1;
				}

				//if undamaged, note such
				if (tempDDF == 0) 
				{
					decision = "Undamaged";
					undamagedHouses = undamagedHouses + 1;
					repairCost = 0;
				}

				//if damaged determine repair and mitigation costs, benefits and affordability
				if (tempDDF > 0)
				{
					Owner tempowner = temphouse.getOwner();
					int hs = temphouse.getStyle();
					repairCost = tempowner.calcrepaircost(temphouse, tempDDF);
					
					if (hs == 10 || hs == 13) //if townhouse or duplex prevent mitigation
					{
						mitigateCost = 99999999.77;
						mitigateBenefit = 0;
					}
					else
					{
						mitigateCost = tempowner.calcmitigationcost(temphouse);
						mitigateBenefit = tempowner.calcmitigationbenefit(temphouse);
					}

					//calculate costs and value
					int cyear = (int) t;
					double mortgage = tempowner.getMortgageValue(temphouse, cyear);
					double mortgagePayment = tempowner.getMortgagePayment(temphouse, cyear);
					boolean move = false;
					double buildingValue = temphouse.getHomevalue();
					double siteBenefit = templot.getLandBenefit();
					double siteValue = templot.getLandvalue();
					double ownerhouseMoney = tempowner.gethousingMoney();
					double insurancePayment = tempowner.getInsurancePayment(temphouse, tempDDF);

					repairedValue = siteBenefit + buildingValue + insurancePayment - repairCost - mortgage;
					repairFunds = ownerhouseMoney - mortgagePayment + (insurancePayment - repairCost*12*ABM.interestrate/100000)/30;					//must be >0 to repair  //assumes a 30 year repayment plan
					mitigatedValue = siteBenefit + buildingValue + insurancePayment - repairCost - mortgage + mitigateBenefit - mitigateCost; 
					mitigateFunds = ownerhouseMoney - mortgagePayment + (insurancePayment - (repairCost + mitigateCost)*12*ABM.interestrate/100000)/30; //must be >0 to repair
					int tempid = templot.getLotNumber();

					//if the home is destroyed, determine cost-benefit of rebuilding or move 
					if (tempDDF >= 50.0)
					{
						destroyedHouses = destroyedHouses + 1;
						repairedValue = -99999999.00;
						
						double demoCost = ABM.demolitionCost * temphouse.getHomevalue();
						double lotValue = templot.getLandvalue();
						gt = state.schedule.getTime();
						int iyear = (int) gt;
						double oldMortgage = tempowner.getMortgageValue(temphouse, iyear);
						double netSales = lotValue - oldMortgage - demoCost;
						
						double owneravailinvest = (tempowner.gethousingMoney() * 30 * 100000/(ABM.interestrate * 12)) + netSales + insurancePayment;
	
						double expectedFloodHeight = tempowner.getOwnerRiskHeight() + templot.getElevation();
						tempowner.setOwnerOldRiskHeight();
						Lot bestLot = null;
						int bestLotId = -1;
						double bestValue = -9999999.00;	
						double bestHomeValue = -999999999.00;
						double bestsqft = 0;
						double bestFloodProof = 0;
						double bestMortgage = 99999999.00;
						int testLots = ABM.totalEmptyLots;
						int checklot = templot.getLotID();
						int checklotno = templot.getLotNumber();

						//if empty lots are detected, cycle through them to determine best option to move to 
						if (testLots > 0)
						{
							for (int j = 0; j < testLots; j++)
							{
								Lot tempemptylot = ABM.emptyLots.get(j);
								double newlotPrice = tempemptylot.getLandvalue();
								double newlotBenefit = tempemptylot.getLandBenefit();
								double newlotElevation = tempemptylot.getElevation();
								double newhouseFloodProofHeight = expectedFloodHeight - newlotElevation;
								double availableHousinginvestment = owneravailinvest - newlotPrice;
								double newhousesize = availableHousinginvestment/(ABM.pricepersqft * (1.0 + ABM.newhouseMitCost *newhouseFloodProofHeight));
								double newHouseValue = availableHousinginvestment; 								//assume fully invested
								double newMortgage = (newHouseValue + newlotPrice) * ABM.interestrate * 360 / 100000;  //total cost including interest
								double newMortgagepayment =(newHouseValue + newlotPrice) * ABM.interestrate * 12 / 100000;
								
								double newlotValue = newlotBenefit + newHouseValue - newMortgage + insurancePayment + netSales + mitigateBenefit;   //drive best lot 
								double newlotFunds = ownerhouseMoney - newMortgagepayment + (netSales + insurancePayment)/30; 

								if (newlotValue > bestValue && newlotFunds >= 0)
								{
									bestLotId = j;
									bestLot = ABM.emptyLots.get(j);
									bestValue = newlotValue;
									bestHomeValue = newHouseValue;
									bestsqft = newhousesize;
									bestMortgage = newMortgage;
									move = true;
									bestFloodProof = newhouseFloodProofHeight;
								}
							}
						}
						double moveValue = bestValue;
						double oldriskHeight = tempowner.getOwnerRiskHeight();
						double rebuiltValue = siteBenefit + buildingValue + insurancePayment - buildingValue* (1.0 + ABM.newhouseMitCost *oldriskHeight) - mortgage - demoCost + mitigateBenefit;					//full replacement cost + demolition
						double rebuiltFunds = ownerhouseMoney - mortgagePayment + (insurancePayment - (buildingValue* (1.0 + ABM.newhouseMitCost *oldriskHeight)+demoCost)*12*ABM.interestrate/100000)/30;					//must be >0 to repair  //assumes a 30 year repayment plan
						double rebuiltMortgage = mortgage + buildingValue * (1.0 + ABM.newhouseMitCost *oldriskHeight);

						//if moving is better than rebuilding
						if (moveValue > rebuiltValue) 
						{
							destroyedRelocates = destroyedRelocates + 1;
							decision = "Destroyed Move";
							int h = templot.getBuildingindex(temphouse);  //remove old lot links
							int lll = templot.getLotID();
							Houses hl = templot.getBuilding(h);
							templot.removeBuilding(h);
							ABM.houseList.remove(hl);
							int o = templot.getOwnerindex(tempowner);
							templot.removeOwner(o);
							ABM.emptyLots.add(templot);
							int housecount = ABM.totalHouses - 1;
							ABM.totalHouses = housecount;
							
							int k = bestLot.getLotID();					//set up new house
							int lotnumber = bestLot.getLotNumber();
							int lotid = bestLot.getLotID();
							double levels = bestsqft/1500;
							int story = (int) levels;
							String ext = "F";
							int style = 6;
							
							Houses newhouse = new Houses (k, lotid, lotnumber, cyear, bestHomeValue, cyear, story,
									ext, style, bestFloodProof, bestHomeValue, bestsqft, world);
							newhouse.setOwner(tempowner);
							int ownerid = tempowner.getOwnerId();
							newhouse.setOwnerId(ownerid);
							tempowner.setOwnerHouse(newhouse);
							housecount = ABM.totalHouses + 1;
							tempowner.setOwnerHomeId(housecount);
							ABM.houseList.add(newhouse);
							ABM.totalHouses = housecount;
							ABM.emptyLots.remove(bestLotId);			//improve new lot
							int developed = ABM.builtLots.size();
							ABM.builtLots.add(developed,bestLot);
							bestLot.setOwner(tempowner);
							bestLot.addBuilding(newhouse);
						}
						else if (rebuiltValue > 0.0) //execute rebuilding with elevation
						{
							destroyedNewCon = destroyedNewCon + 1;
							destroyedFPNewCon = destroyedFPNewCon + 1;
							decision = "Destroyed MitigateHouse";
							tempowner.resetOwnerRiskHeight();
							double newheight = tempowner.getOwnerRiskHeight();
							temphouse.setFloodproofHeight(newheight);
						}
						else //execute departure for destroyed house, demolish and then move
						{
							decision = "Destroyed Depart";
							destroyedDeparted = destroyedDeparted + 1;
							int h = templot.getBuildingindex(temphouse);  //remove old lot links
							int o = templot.getOwnerindex(tempowner);
							int l = templot.getLotID();
							Houses hl = templot.getBuilding(h);
							Owner ol = templot.getOwner();
							int on = ABM.allOwners.indexOf(ol);
							int hn = ABM.houseList.indexOf(hl);
							int h1 = hn + 1;
							int o1 = on + 1;
							int l1 = l + 1;
							int housecount = ABM.totalHouses;
							int ownercount = ABM.allOwners.size();
							int lotcount = ABM.allLots.size();

							for (int j = h1; j<housecount; j++ )
							{
								Houses nexth = ABM.houseList.get(j);
								nexth.decreaseOwnerID();
							}
							for (int j = o1; j<ownercount; j++ )
							{
								Owner nexto = ABM.allOwners.get(j);
								nexto.decreaseBuildingID();
							}
							for (int j = l1; j<lotcount; j++ )
							{
								Lot nextl = ABM.allLots.get(j);
								nextl.decreaseOwnerID();
							}
							int hindex = ABM.houseList.indexOf(hl);
							ABM.houseList.remove(hl);								
							templot.removeBuilding(h);
							templot.removeOwner(o);
							housecount = housecount - 1;
							ABM.totalHouses = housecount;
						}
					}
					else //if house is damaged determine decision
					{
						if (repairFunds > 0.0) {repair = true;}
						if (mitigateFunds > 0.0) {mitigate = true;}
						damagedHouses = damagedHouses + 1;
						repair = true;    //debug	
						mitigate = true;  //debug	
						
						if (mitigate) 
						{
							if(repair)
							{
								if(mitigatedValue >= repairedValue)
								{
									damagedFPHouses = damagedFPHouses + 1;
									decision = "MitigateHouse";
									double newheight = tempowner.getOwnerRiskHeight();
									temphouse.setFloodproofHeight(newheight);
								}
								else 
								{
									damagedNonFPHouses = damagedNonFPHouses + 1;
									decision = "RepairHouse";
								}
							}
						}
						else   //if no funds to mitigate
						{
							if(repair) 
							{
								damagedNonFPHouses = damagedNonFPHouses + 1;
								decision = "RepairHouse";
							}
							else //if not funds to repair
							{
								DamagedDeparted = DamagedDeparted + 1;
								decision = "Depart";
								int h = templot.getBuildingindex(temphouse);  //remove old lot links
								Houses hl = templot.getBuilding(h);
								int hindex = ABM.houseList.indexOf(hl);
								ABM.houseList.remove(hindex);	
								templot.removeBuilding(h);
								int o = templot.getOwnerindex(tempowner);
								templot.removeOwner(o);
								ABM.totalHouses = ABM.totalHouses-1;
							}
						}
					}
				}
				try  //output owner decision and details
				{
					double floodheight = temphouse.getFloodHeight();
					double damage = temphouse.getDamage();	
					double hvalue = temphouse.getHomevalue();
					String type = temphouse.getHomeType();
					double stories = temphouse.getStories();
					Owner tempowner = temphouse.getOwner();
					int iyear = (int) gt;					//test for owner community flood consciousness
					tempowner.setOwnerFloodYear(iyear);     //test
					tempowner.setOwnerExperienceFlag(1);	//test
					int lotno = tempowner.getOwnerLotID();
					int lotnumber = templot.getLotNumber();
					double lotelev = templot.getElevation();
					double freeboard = temphouse.getFloodproofheight();
					double lotlat = templot.getY();
					double lotlong = templot.getX();
					String sex = tempowner.getOwnerSex();
					String race = tempowner.getOwnerRace();
					int age = tempowner.getOwnerAge();
					double sexflag = tempowner.getSexFlag();
					double income = tempowner.getOwnerIncome();
					double scaleincome = income/200000;
					double experience = tempowner.getOwnerExperience();
					int fyear = tempowner.getOwnerFloodYear();
					double fheight = tempowner.getOwnerFloodLevel();
					double oriskheight = tempowner.getOwnerRiskHeight();
					double orisklevel = tempowner.getOwnerRiskLevel();
					int hstyle = temphouse.getStyle();
					int whiteflag = 0;
					if(race.contentEquals("White")) {whiteflag = 1;}

					Ticker.reportflood.write(i + "\t" + lotno + "\t" + lotnumber + "\t" + lotelev + "\t" + freeboard + "\t" + lotlat + "\t" + lotlong
							+ "\t" + floodheight + "\t" + damage + "\t" + hvalue + "\t" + repairCost + "\t" + type + "\t" + stories  + "\t" + fyear + "\t" + fheight + "\t" 
							+ sex + "\t" + sexflag + "\t" + race + "\t" + whiteflag + "\t" + age + "\t" + income + "\t" + scaleincome  + "\t" + experience + "\t" + oriskheight + "\t" + orisklevel + "\t" + hstyle  + "\t" + tempDDF + "\t" + repairFunds + "\t" + mitigateBenefit
							+ " \t" + mitigateCost + "\t" + mitigateFunds + "\t" + repairCost + "\t" + repairedValue + "\t" + mitigatedValue + "\t" + decision);	
					Ticker.reportflood.newLine();	
				}
				catch (IOException e)
				{
					System.err.println("error at Flood69 report null");
					e.printStackTrace();
				}
			}
			try //output flood statistics 
			{
				System.out.println("Writing Flood Report Summary Statistics");
				System.out.println("Dry: " + dryHouses + " NoDamage: " + undamagedHouses + " Repaired: " + damagedNonFPHouses + " Mitigated: " + damagedFPHouses + " Destroyed: " + destroyedHouses 
						+ " DestroyedDepartee: " + destroyedDeparted + " DestroyedRelocates: " + destroyedRelocates + " Damaged Departed: " + DamagedDeparted+ " DryLots: " + dryLots + " WetLots: " + wetLots);
				Ticker.reportflood.newLine();
				Ticker.reportflood.write("**********Summary Statistics*************");
				Ticker.reportflood.newLine();
				Ticker.reportflood.write("undamagedHouses" + "\t" + "damagedHouses" + "\t" + "destroyedHouses" + "\t" + "wetLots" + "\t" +  "dryLots" + "\t" +  "dryHouses" + "\t" + "damagedFPHouses"  + "\t" + "damagedNonFPHouses"
						+ "\t" + "DamagedDeparted" + "\t" + "destroyedRebuilds" + "\t" + "destroyedRelocates" + "\t" + "destroyedDeparted" + "\t" + "DamagedDeparted"); 
				Ticker.reportflood.newLine();
				Ticker.reportflood.write(undamagedHouses + "\t" + damagedHouses + "\t" + destroyedHouses + "\t" + wetLots + "\t" + dryLots +  "\t" +  dryHouses +  "\t" + damagedFPHouses  + "\t" + damagedNonFPHouses
						+ "\t" + DamagedDeparted + "\t" + destroyedRebuilds + "\t" + destroyedRelocates + "\t" + destroyedDeparted + "\t" + DamagedDeparted);	
				Ticker.reportflood.newLine();	
			}
			catch (Exception e)
			{
				System.err.println("flood report output error");
			}
			try  //output summary statistics for each flood
			{
				Ticker.reportsum.newLine();
				Ticker.reportsum.write(rep + "\t" + floodnumber + "\t" + floodbias + "\t" + damagebias + "\t" + riskLevel + "\t" + hurricanes + "\t" + dryHouses + "\t" + undamagedHouses + "\t" +  damagedNonFPHouses + "\t" 
					+ damagedFPHouses + "\t" +  destroyedHouses + "\t" + destroyedDeparted + "\t" + destroyedRelocates + "\t" + DamagedDeparted	+ "\t" + dryLots + "\t" + wetLots);
				Ticker.reportsum.flush();
			}
			catch (IOException e)
			{
				System.err.println("error at flood summary null");
				e.printStackTrace();
			}			
		}
	}
}
