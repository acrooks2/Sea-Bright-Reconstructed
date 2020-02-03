package floodresponse;

//Class ABM contains the main method to run this simulation
//
/**
  @author Kim McEligot
  
  Current implementation models the response to a precursor hurricane and a single hurricane for evaluation
  
  This simulation establishes a topography and housing structures, then populates the houses with occupants
  Event based hurricane flooding is sequenced on an annual timestep. 
  Flood damage is determined and occupants respond to flooding by moving or repairing
  Storm surge height information is imported and each lot/house utilizes the nearest flood reporting point to determine
     the flood height.
  FEMA Depth-Damage Functions for split level and 1, 2 & 3 story houses are used to convert flood height to damage percentages.
  Based upon homeowner gender, ethnicity, income and flood experience, individual flood probabilities are determined and 
      the flood level is assumed as the future flood levels.
  Homes are destroyed if they have more than 50% damage
  For damaged houses:    
  If the homeowner's risk perception is greater than the risk threshold they will do cost benefit analysis to determine if they
    should add floodproofing elevation or just repair to the previous level. Homeowner funding is also assess to determine whether
    they can afford to repair and/or mitigate their house. If not they will sell and depart.
  For destroyed houses:
  The assessment is on whether to rebuild or move based upon cost-benefit analysis and an affordability analysis.
  Output data includes individual lot/house/owner parameters by flood  and summary statistics of whether homeowners have no 
    damage, repair, mitigate, rebuild or move.

 The ABM class initializes the simulation and sets up the houses, individuals and flood schedule/heights
  
  Elevations in feet (MSL)
   **/

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
//import java.time.Instant;
//import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import sim.engine.SimState;
//import sim.field.SparseField;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.distribution.Uniform;
//import sim.util.distribution.Exponential;
//import ec.util.MersenneTwisterFast;
//import java.util.HashMap;
import java.io.File;
import java.util.Scanner;
import ec.util.MersenneTwisterFast;
import java.util.Date;

public class ABM extends SimState
{
	ABM world;
	
	//Parameters
		//Initialization
	
	public static int repno = 25;				//the individual rep number is used as part of the input file
	public static double floodproofBias = -3;		//STANDARD=-3.0 bias factor for floodproofing adjustment to Sandy survey
	public static double damageBias = 0.4;		//STANDARD=0.4 decimal % bias factor for adjustment to Sandy historical damage
	public static double riskmitigate = -0.175;   //STANDARD=-0.2 perceived risks impact on mitigation
	public static double percenthousingShift = 0.2;		//weighting of shift toward ACS normal of percent of income for housing
	public static double minhousingincrease = 0000;		//minimum increase in homeowner outlay for housing going forward
	public static double maxhousingincrease = 2000;		//maximumm increase in homeowner outlay for housing going forward
	public static double minhousingpercentincrease = 0.05;	//minimum percentage increase in homeowner outlay for housing going forward
	public static double maxhousingpercentincreast = 0.1;	//maximum percentage increase in homeowner outlay for housing going forward
	public static double projectedSLR = 0;	//expected Sea Level Rise 
	public static double nonstructuralmitigationpercent = 0.0;  //percentage of intrinsic value of floodproofing not related to physical damage prevention
	public static double futurehurricanes = 1.25;		//expected number of future flood events
	public static double masonelevationoverhead = 57.0;  //initial cost per sq ft elevate masonry on crawl (from Aerts 2017 pg 23 from FEMA 2009b
	public static double elevationperfoot = 1.5;// per foot per sq ft elevate on crawl
	public static double frameelevationoverhead = 26.0; //initial cost per sq ft elevate frame on crawl
	public static double demolitionCost = 0.15;  //% of value to demolish a house
	public static double newhouseMitCost = 0.0115; //new house floodproofing incremental cost rate per foot elevation
	public static double interestrate = 537.0;	//monthly cost per $100,000. Based upon 5% interest rate (2008-2012 4.774%)
	public static double pricepersqft = 192.8;	//construction cost per sqft (average of 2014-2017 construction value/sqft)
	public static double riskinsurance = 0.22;  //GROWTH perceived risk's impact on obtaining flood insurance

	public static double femalerisk = 0.14; 	//STANDARD=0.14 correlation of sex with perceived risk
	public static double experiencerisk = 0.21;  //STANDARD=0.21 correlation of flood experience with perceived risk
	public static double incomerisk = -0.21;			//STANDARD=-0.21 correlation of income with perceived risk
	public static double racerisk = -0.14;		//STANDARD=-0.14 correlation of ethnicity with perceived risk
	public static double femaleincome = -0.0;			//correlation of sex with income	
	public static double femaleexperience = 0.0;		//correlation of sex with flood experience
	public static double incomeexperience = -0.0;		//correlation of income with flood experience
	public static double femaleincomeexperience = 0.0;	//correlation of sex with income and flood experience

	//test	public static double riskinsurance = 0.22;  //perceived risk's impact on obtaining flood insurance
	//test	public static double femalerisk = 0.21; 	//correlation of sex with perceived risk
	//test	public static double experiencerisk = 0.27;  //correlation of flood experience with perceived risk
	//test  public static double racerisk = -0.14;		//correlation of ethnicity with perceived risk
	//test	public static double incomerisk = -0.18;			//correlation of income with perceived risk
	//test	public static double femaleincome = -0.18;			//correlation of sex with income	
	//test	public static double femaleexperience = 0.14;		//correlation of sex with flood experience
	//test	public static double incomeexperience = -0.10;		//correlation of income with flood experience
	//test	public static double femaleincomeexperience = 0.02;	//correlation of sex with income and flood experience
			
	public static double riskperception [] = new double [100];	//array for inputing risk parameters to the Owner agent.

//	boolean internalFloodGen = true; // true uses internally generated, exponentially distributed floods
			// false uses externally provided flood schedule for comparing policy alternatives

	int id = -1;
	
	//identify input/output file names/locations
	String propertyFile = new String ("inputs/SBResidentialUpdateIncomeBenefit" + repno + ".csv");  //TODO Update with property file name  
	String outputPropertyFile = new String ("outputs/lotdata" + repno);  //TODO Update with property file name
	String GaugeFile = new String ("inputs/gaugelocationsFEMA.csv");  
//	String outputgaugeFile = new String ("outputs/gaugedata");  //TODO Update stringwriter name with:  + world.id + ".txt"
	String ddfFile = new String ("inputs/DDFs.csv");  
	String outputddfFile = new String ("outputs/DDFdata");  
	String floodFile = new String ("inputs/floodUSGSFEMA.csv");  
	String sdamageBias = Double.toString(damageBias);
	String sfloodproofingBias = Double.toString(floodproofBias);
	String outputfloodFile = new String ("outputs/flooddata_" + sdamageBias + sfloodproofingBias); 
	String outputStats = new String ("outputs/outputStats_" + sdamageBias + sfloodproofingBias);  
	String outputSummary = new String ("outputs/outputSummary");  

	//zeroize floods
	double floodlevel = 0;
	public int totalFloods = 0;
	
		//set up
	public static int numberOfYearsInSimulation = 2100;
	
//	public Bag housesOnLot = new Bag();
	
	//set up random number generator
	static long seed = 0000000000000000000000 + repno;			//seed for random number generator
	static MersenneTwisterFast randowner = new MersenneTwisterFast(seed);
	public static Uniform rdistro = new Uniform(randowner);   //0 to 1
	public static Uniform udistro = new Uniform(randowner);  //.3 to 0.4
	public static Uniform vdistro = new Uniform(randowner);  //0.15,0.35,
	public static Uniform tdistro = new Uniform(randowner);  //0.1,0.4,
	public static Uniform wdistro = new Uniform(randowner); //0.15,0.30,
	public static Uniform sdistro = new Uniform(randowner);  //0.3,0.40,
	public static Uniform xdistro = new Uniform(2000.0,10000.00,randowner);
	public static Uniform ydistro = new Uniform(randowner);  //minhousingincrease,maxhousingincrease
	
	static MersenneTwisterFast randlot = new MersenneTwisterFast(seed);
	public static Uniform qdistro = new Uniform(-0.5,0.5,randlot);

	//flood inputs
//	public double floodInterval = 16; //average interval between floods
//	public double avgfloodHeight = 72; //average flood height in inches
//	public double centurySLR = 72; //sea level rise over 100 years in inches
//	public double[] floodTimeInternal = new double[2];
	public double floodtimeExternal = 0;


	//Objects
	   //Terrain
	public static Continuous2D SeaBright = new Continuous2D (1,100,100);   //modified from Virus demo (setObjectLocation moved to ticker)
	public static Continuous2D testarea = new Continuous2D (1,50,50);   //modified from Virus demo (setObjectLocation moved to ticker)

		//initialize lots
	public static int totalProperties = 0;
	public static int totalLots = 0;
	public static int totalEmptyLots = 0;
	
	public int lotID [] = new int [3000]; //TODO Update lot information array sizes to cover total number of inputs
	public int lotnumber[]= new int [3000];
	public double lat [] = new double [3000];
	public double longitude [] = new double [3000];
	public double elev [] = new double [3000];
	public double landassess [] = new double [3000];
	public int saleyear [] = new int [3000];	
	public double saleprice [] = new double [3000];	
	public boolean owneroccupied [] = new boolean [3000];	
	public double acreage[] = new double [3000];
	public String builtflag [] = new String [3000];
	public int builtyear [] = new int [3000];	
	public double stories [] = new double [3000];	
	public String exterior [] = new String [3000];
	public int style [] = new int [3000];	
	public double floodproof [] = new double [3000];
	public double builtassess [] = new double [3000];
	public double sqft [] = new double [3000];
	public int householdcount [] = new int [3000];	
	public boolean multiFlag [] = new boolean [3000];
	public double landBenefit [] = new double [3000];
	public double locBenefit[]= new double [3000];
	
	public static ArrayList<Lot> emptyLots = new ArrayList<Lot>(); //
	public static ArrayList<Lot> builtLots = new ArrayList<Lot>(); //	
	public static ArrayList<Lot> allLots = new ArrayList<Lot>(); //

		//Owners
	public int ownerID[] = new int[3000];
	public String race[] = new String[3000];
	public String sex []= new String[3000];
	public int OwnerlotId[] = new int[3000];
	public int OwnerHouseId[] = new int [3000];
	public int age[] = new int[3000];
	public String mortgageStatus [] = new String[3000];
	public double income [] = new double [3000];
	public static ArrayList<Owner> allOwners = new ArrayList<Owner>(); //
	
    	//Houses
	public static ArrayList<Houses> houseList = new ArrayList<Houses>();
//	public static ArrayList<Houses> vacantHouses = new ArrayList<Houses>();	
	public static int totalHouses = 0;
	
//	public static Bag houseBag = new Bag();
	
	//Tide Gauges
	public static ArrayList<Gauge> TideList = new ArrayList<Gauge>();	
	
	public int totalGauges = 0;	 
	
	public int gaugeID [] = new int [600]; //TODO Update gauge information array sizes to cover total number of inputs
	public double gaugelat [] = new double [600];
	public double gaugelong [] = new double [600];
	public double gaugereading [] = new double [600];
	
	//Depth Damage Functions
	public static ArrayList<DDF> damageFunction = new ArrayList<DDF>();

	public int ddfCases = 4;
	public int totalDDFs = 0;
	public int DDFnumber = 0; 
	public int DDFstories[] = new int[160];	
	public double DDFdamage[] = new double[160];
	public double DDFheight[] = new double[160];
	
	  //Occupants
//	public static ArrayList<Occupants> occupantList = new ArrayList<Occupants>();	
//	public static ArrayList<Occupants> homelessOccupants = new ArrayList<Occupants>();	
	
	
	//Statistics
	double cost = 0;
	public static double totalCost = 0;
	
	
//Body of Simulation
	
	//Main standalone version
	public static void main(String[] args) 
	{
		doLoop(ABM.class, args);
		System.exit(0);
	}
	
	//constructor default parameters
	public ABM(long seed)
	{
		super(seed);
	}

	
	//constructor with user defined parameters
//	public ABM (long seed, double floodInterval, double averageFloodHeight,
//			double seaRiseRate, int floodProofingRequirement,
//			int floodInsuranceRequirement, double mortgageScaler, double happinessScaler, double riskScaler, 
//			double neighborhoodScaler, double growthRate)
//		{
//			super (seed);
//			this.floodInterval = floodInterval;
//			this.avgfloodHeight = averageFloodHeight;
//			this.seaRiseRate = seaRiseRate;
//			this.floodProofRqmt = floodProofingRequirement;
//			this.floodInsureRqmt = floodInsuranceRequirement;
//			this.k1 = mortgageScaler;
//			this.k2 = happinessScaler;
//			this.k3 = riskScaler;
//			this.k4 = neighborhoodScaler;
//			this.growthRate = growthRate;
//		}	
			public void start()
			{
				super.start();
				
				id = (int) System.currentTimeMillis(); //gets an ID

				System.out.println ("Start Time:" + id);
				
//				//set up monthly mortgage payment per $1000
				
				
//				double monthlyIntPoints = interestRate / 12;
//				monthlyPerK = 1000*monthlyIntPoints*Math.pow(1+monthlyIntPoints,360)/(Math.pow(1+monthlyIntPoints,360)-1);
				
		//debugging			System.out.println ("monthly mortgage:"+ monthlyPerK);	//debugging

				//fill array for transfer to Owner
				riskperception[0]=riskmitigate;
				riskperception[1]=riskinsurance;
				riskperception[2]=femalerisk;
				riskperception[3]=experiencerisk;
				riskperception[4]=incomerisk;
				riskperception[5]=femaleincome;
				riskperception[6]=femaleexperience;
				riskperception[7]=incomeexperience;
				riskperception[8]=femaleincomeexperience;
				riskperception[9]=racerisk;
				
				//read in data and output for reference
				System.out.println("read in property data");
				setupTownFromFile(propertyFile);
				
				printTownToFile(outputPropertyFile);

				//read in flood height data
				System.out.println("read in tide gauge locations");
				setupTideGaugesFromFile(GaugeFile);//file tide gauge locations are entered as 1 to x, converted to java 0 to x-1
				
//debug				System.out.println(totalGauges + " " + gaugeID [totalGauges-1] + " " + gaugelat [totalGauges-1]  + " " + gaugelong [totalGauges-1]);
				
//				System.out.println("read in occupant data");
//				setupOccupantsFromFile("occupants.txt");

				//associate the nearest tide gauge with each lot
				System.out.println("determining nearest tide guage");
				associateTideGaugestoLots();

				//read in damage tables for 0 to 10 ft of flooding
				System.out.println("read in DDF data");
				setupDDFFromFile(ddfFile);

				//finish reading data
				System.out.println("Finished reading in data.\nBeginning data setup");
				
				//set up data

//				int waterw = this.lot.getWidth();
//				int waterh = this.lot.getHeight();	
//				citycenterY = Math.round(waterh/2);

		//debugging		System.out.println(waterw + " width " + waterh + " height");
//				for (int waterx=0; waterx<waterw; waterx++)
//				{
//					for (int watery=0; watery<waterh; watery++)
//					{
//						Lot tempwater = ((Lot) lot.get(waterx, watery));
//						if (tempwater.getElevation() <= 0) {tempwater.setBelowSeaLevel(true);}
		//debugging					System.out.println (tempwater.getX() + " " + tempwater.getY() + " " + tempwater.getElevation() +" "+ tempwater.getBelowSeaLevel());
//
//					}
//				}
//debug			System.out.println(" floodproofBias: " + floodproofBias + "  damageBias: " + damageBias + "  percenthousingShift: " + percenthousingShift + "  minhousingincreaseLower: " + minhousingincrease + "  minhousingincreaseUpper: " + maxhousingincrease);
				
				System.out.println("Initialize Storm and Ticker");

				//input flood data for each tide gauge for every flood
				System.out.println("read in flood data");
				setupFloodsFromFile(floodFile,totalGauges);	 //TODO update to read in ADCIRC outputs
				
//debug				System.out.println(totalGauges + " " + gaugeID [totalGauges-1] + " " + gaugelat [totalGauges-1]  + " " + gaugelong [totalGauges-1]+ " " + gaugereading [totalGauges-1]);

				//Establish flood event schedule
				int lastFlood = ((int) floodtimeExternal) + 1;
				Ticker ticker = new Ticker(this, lastFlood, outputStats, outputfloodFile, outputSummary);

				System.out.println("Ticker initialized starting ticker schedule");
				schedule.scheduleRepeating(ticker, 1, numberOfYearsInSimulation/10);
				schedule.scheduleRepeating(ticker, 1, numberOfYearsInSimulation);
				}
				
			//This method reads in data for each lot including owner and housing information, and then parses it.
			void setupTownFromFile(String filename)
			{
				Scanner townFile = null;
				try
				{
					 townFile = new Scanner(new File(filename));
			
					System.out.println("Town file found");
				}
				catch (Exception e)
				{
					System.err.println("Town file input error");
				}
				while (townFile.hasNextLine())
				{
					String property = "";
					property = townFile.nextLine();
	
					String slotnumber="";
					String slat="";
					String slong="";
					String selev="";
					String slandassess="";
					String ssaledate="";
					String ssaleprice="";
					String sowneroccupied="";
					String srace="";
					String ssex="";
					String sage="";
					String smortgage="";
					String sincome="";
					String sacreage="";
					String sbuiltyear="";
					String sstories="";
					String sstyle="";
					String sfloodproof="";
					String sbuiltassess="";
					String shouseholdcount="";
					String smultiflag = "";
					String ssqft ="";
					String locBen ="";
					Scanner segment = null;
	
					//Parse line of property information
					try
					{
						segment = new Scanner(property).useDelimiter(",");
//debug				System.out.println(totalLots + " parsing");
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "new line error");
					}
					
					//read Lot ID number
					try
					{
						slotnumber = segment.next();
						lotnumber [totalProperties] = Integer.parseInt(slotnumber);					
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot ID error");
					}
	
					//read Lot Latitude
					try
					{
						slat = segment.next();
						lat [totalProperties] = Double.parseDouble(slat);		
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot Latitude error");
					}					
					
					//read Lot Longitude
					try
					{
						slong = segment.next();
						longitude [totalProperties] = Double.parseDouble(slong);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot Longitude error");
					}					
					
					//read lot elevation
					try
					{
						selev = segment.next();
						elev [totalProperties] = Double.parseDouble(selev);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot elevation error");
					}					
					
					//read land value
					try
					{
						slandassess = segment.next();
						landassess [totalProperties] = Double.parseDouble(slandassess);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot Land value error");
					}					
					
					//read sale date
					try
					{
						String fulldate = segment.next();
						Scanner parsedate = new Scanner(fulldate).useDelimiter("/");
						String pmonth = parsedate.next();
						String pday = parsedate.next();
						ssaledate = parsedate.next();
						saleyear [totalProperties] = Integer.parseInt(ssaledate);	
						
						parsedate.close();
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot sale date error");
					}					
	
					//read sale price
					try
					{
						ssaleprice = segment.next();
						saleprice [totalProperties] = Double.parseDouble(ssaleprice);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot Land value error");
					}
					
					//read owner occupied flag	
					try
					{
						sowneroccupied = segment.next();
						owneroccupied [totalProperties] = Boolean.parseBoolean(sowneroccupied);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot owner occupied flag error");
					}				

					//read race	
					try
					{
						srace = segment.next();
						race [totalProperties] = srace;	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "race flag error");
					}				

					//read sex	
					try
					{
						ssex = segment.next();
						sex [totalProperties] = ssex;	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "sex flag error");
					}	
					
					//read age	
					try
					{
						sage = segment.next();
						age [totalProperties] = Integer.parseInt(sage);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "age flag error");
					}				

					//read mortgage status	
					try
					{
						smortgage = segment.next();
						mortgageStatus [totalProperties] = smortgage;	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "sex flag error");
					}

					//read owner income
					try
					{
						sincome = segment.next();
						income [totalProperties] = Double.parseDouble(sincome);		
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "onwer income error");
					}					
					
					//read acreage	
					try
					{
						sacreage = segment.next();
						acreage [totalProperties] = Double.parseDouble(sacreage);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "acreage flag error");
					}				
	
					//read built flag	
					try
					{
						builtflag [totalProperties]   = segment.next();
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot built out flag error");
					}				
	
					//read year built number
					try
					{
						sbuiltyear = segment.next();
						builtyear [totalProperties] = Integer.parseInt(sbuiltyear);					
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot year built error");
					}
					
					//read number of stories
					try
					{
						sstories = segment.next();
						stories [totalProperties] = Double.parseDouble(sstories);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot number of stories error");
					}					
					
					//read exterior	description
					try
					{
						exterior [totalProperties]   = segment.next();
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot exterior error");
					}				
					
					//read building style number
					try
					{
						sstyle = segment.next();
						style [totalProperties] = Integer.parseInt(sstyle);					
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot style number error");
					}
	
					//read flood proof height
					try
					{
						sfloodproof = segment.next();
						floodproof [totalProperties] = Double.parseDouble(sfloodproof) + floodproofBias;	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot floodproof error");
					}					
					
					//read building value
					try
					{
						sbuiltassess = segment.next();
						builtassess [totalProperties] = Double.parseDouble(sbuiltassess);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot building value error");
					}					

					//read square footage
					try
					{
						ssqft = segment.next();
						sqft [totalProperties] = Double.parseDouble(ssqft);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "house size value error");
					}					

					//read households per building
					try
					{
						shouseholdcount = segment.next();
						householdcount [totalProperties] = Integer.parseInt(shouseholdcount);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + " households per building count value error");
					}					

					//read in flag information on whether there are multiple housing units on the lot. GROWTH for condos.
					try
					{
						smultiflag = segment.next();
						multiFlag [totalProperties] = Boolean.parseBoolean(smultiflag);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "lot building value error");
					}	
					
					//read in the enhanced worth for the lot (waterfront, etc.) as a percentage of the lot value.
					try
					{
						locBen = segment.next();
						locBenefit [totalProperties] = Double.parseDouble(locBen);	
					}
					catch (Exception e)
					{
						System.err.println(totalProperties + "house size value error");
					}
					
					//print out inputs
					String lotReport = "outputs/lotdata.txt";
					totalProperties = totalProperties + 1;  //increment lot count
					segment.close();
				}
				
				//sequence through each lot and establish individual Lot, Owner and Houses instances for each lot 
				int lotcount = 0;
				for (int j=0; j<totalProperties; j++)  //
				{
//debug					System.out.println(j + "  " + multiFlag[j]);

					//if only one house on the lot, populate
					if (!multiFlag[j])
					{
						Lot templot = new Lot (lotcount, lotcount, lotnumber[j],lat[j],longitude[j],elev[j],1,landassess[j], locBenefit[j], builtflag[j], 1);
						allLots.add(templot); 
						
						//establish homeowner GROWTH randomize the percentage of risk which is uncorrelated
						double uncoor = rdistro.nextDouble();
						Owner tempowner = new Owner (lotcount, lotnumber[j], race[j], sex[j], age[j], mortgageStatus[j], riskperception,income[j], uncoor);

						allOwners.add(tempowner);						

						templot.setOwnerId(lotcount);
						templot.setOwner(tempowner);
						tempowner.setOwnerLot(templot);
						tempowner.setOwnerLotNumber(lotnumber[j]);	
//debug						System.out.println(lotcount + "  " + templot + " " + templot.getLotNumber() + " " + templot.getX()  + " " + templot.getY() + " " + templot.getBuildFlag());
						
						//if there is a house on the lot, populate it
						String test = "Built";
						if (builtflag[j].equals(test))
						{
							Houses temphouse = new Houses (totalHouses, lotcount,  lotnumber[j], saleyear[j], saleprice[j], builtyear[j], stories[j],
									exterior[j], style[j], floodproof[j], builtassess[j], sqft[j], this);
							
//debug							System.out.println(temphouse + " LotID:" + temphouse.getLotID() + " " + temphouse.getHomevalue()  + " houseID:" + temphouse.getId());
							
							houseList.add(temphouse);
							builtLots.add(templot);
							templot.addBuilding(temphouse);
							tempowner.setOwnerHouse(temphouse);
							tempowner.setOwnerHomeId(totalHouses);
							temphouse.setOwnerId(lotcount);
							temphouse.setOwner(tempowner);

							totalHouses = totalHouses +1;
						}
						else	//if the lot is empty, initialize the lot as such
						{
							templot = new Lot (j, j, lotnumber[j],lat[j],longitude[j],elev[j],1,landassess[j], landBenefit[j], builtflag[j],0);
//debug							System.out.println("emptylot " + templot + " " + templot.getLotNumber() + " " + templot.getX()  + " " + templot.getY());
							emptyLots.add(templot); 
							totalEmptyLots = totalEmptyLots +1;
							templot.setOwnerId(lotcount);
						}
						lotcount = lotcount + 1;
						totalLots = totalLots + 1;
					}
					else  //multi-lots only exist if there are multiple dwellings on a single lot
					{
						int templotID = -999;
						for (int i=0; i<(totalLots); i++) 
						{
							Lot l = allLots.get(i);
							Integer lID = l.getLotNumber();
//debug						System.out.println (i + " " + l + " " + totalLots + " " + lID + " " +lotnumber[j]+ " " +l.getLotNumber());
							Integer lotnoj = lotnumber[j];
							
							if(lID.equals(lotnoj))
							{
							
								templotID = l.getLotID();
								Houses temphouse = new Houses (j, templotID, lotnumber[j], saleyear[j], saleprice[j],builtyear[j], stories[j],
									exterior[j], style[j], floodproof[j], builtassess[j], sqft[j], this);
//debug							System.out.println("multihouse " + temphouse + " LotID:" + temphouse.getLotNumber() + " " 
//debug								+ temphouse.getHomevalue()  + " Lot ID:" + temphouse.getLotID()+ " house ID:" + temphouse.getId());
								houseList.add(temphouse);
								l.addBuilding(temphouse);
								double uncoor = rdistro.nextDouble();
								Owner tempowner = new Owner (lotcount, lotnumber[j], race[j], sex[j], age[j], mortgageStatus[j], riskperception, income[j], uncoor);
								allOwners.add(tempowner);
								l.setOwnerId(lotcount);
								temphouse.setOwner(tempowner);
								tempowner.setOwnerLotNumber(lotnoj);
								tempowner.setOwnerLot(l);
								tempowner.setOwnerHouse(temphouse);
							}
						}
						totalHouses = totalHouses +1;
					}		
				}
				
/**debug				for (int i=0; i<totalLots; i++)
				{
					Lot l = allLots.get(i);
					int number = l.getLotNumber();
					int no = l.getBuildingcount();
					if (no > 0)
					{
						for (int j=0; j<no; j++)
						{
							Houses ho = l.getBuilding(j);
							int hono = ho.getId();
							
							System.out.println(i + " " + number + " " + j + " " + no + " " + hono);
						}
					}
					else
					{
						System.out.println(i + " " + number + " " + no );			
					}
				}
				
**/
			}

			//This method prints out input data to confirm it was correctly input
			void printTownToFile(String outputfilename)
			{
				 for (int i = 0; i<totalHouses; i++)
				 {
					 Houses h = houseList.get(i);
//					 int index = h.getId();
//					 int lindex = h.getLotID();
//					 int lno = h.getLotNumber();
//					 int lowner = h.getownerID();
					 int hlot = h.getLotID();
					 Lot lott = ABM.allLots.get(hlot);
//					 double hlat = lott.getY();
////					 double hlong = lott.getX();
					int hno = lott.getBuildingindex(h);  //remove old lot links
//debug				 System.out.println(h + " " + index + " " + lindex + " " + lno + " " + hlat + " " + hlong + " " + lowner + " " + hno);
				 }
				
				 for (int i = 0; i<totalLots; i++)
				 {
					Owner o = allOwners.get(i);
//					int oindex = o.getOwnerId();
//					int olotnumber = o.getOwnerLotNumber();
//					int ohousenumber = o.getOwnerHouseNumber();
//					Houses ohouse = o.getOwnerHouse();
//					Lot olot = o.getOwnerLot();
//					int oowner = olot.getOwnerId();
//					int oowner = 0;
//					double oincome = o.getOwnerIncome();
//					String orace = o.getOwnerRace();
//					String osex = o.getOwnerSex();
//					int oage = o.getOwnerAge();
//					int oyear = o.getOwnerFloodYear();
//					double oflood = o.getOwnerFloodLevel();
//					double orisk = o.getOwnerRiskLevel();
				
//debug				 System.out.println(o + " " + oindex + " " + olotnumber + " " + ohousenumber + " " + ohouse + " " + olot
//debug						 + " " + oowner + " " + oincome + " " + orace + " " + osex + " " + oage + " " + oyear + " " + oflood + " " + orisk);
				 }
				 
				String lotReport = outputfilename;
//debug				System.out.println(lotReport);
				String sline="";
				String slotnumber="";
				String slat="";
				String slong="";
				String selev="";
				String slandassess="";
				String ssaledate="";
				String ssaleprice="";
				String sowneroccupied="";
				String sownerID="";
				String srace="";
				String ssex="";
				String sage="";
				String smortgage="";
				String sincome="";
				String sacreage="";
				String sbuiltflag="";
				String sbuiltyear="";
				String sstories="";
				String sexterior = "";
				String sstyle="";
				String sfloodproof="";
				String ssqft="";
				String shouseholdcount="";
				String sbuiltassess="";
//				String sgauge = "";
				
				System.out.println(lotReport + ".txt ");								
			try
				{
//				 Date now = new Date();
				 DateTimeFormatter timestampPattern = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
				 String timetext = timestampPattern.format(java.time.LocalDateTime.now());
//debug				 System.out.println(timetext);
				 
					BufferedWriter w = new BufferedWriter(new FileWriter (lotReport + timetext + ".txt", true));
					
					for (int i=0; i < totalProperties; i++)
					{
						sline = Integer.toString(i);
						slotnumber= Integer.toString(lotnumber [i]);
						slat= Double.toString(lat [i]);
						slong= Double.toString(longitude [i]);
						selev= Double.toString(elev [i]);
						slandassess= Double.toString(landassess [i]);
						ssaledate= Integer.toString(saleyear [i]);
						ssaleprice= Double.toString(saleprice [i]);
						sowneroccupied= Boolean.toString(owneroccupied [i]);
						sownerID = Integer.toString(ownerID[i]);
						srace = race[i];
						ssex = sex [i];
						sage = Integer.toString(age[i]);
						smortgage = mortgageStatus[i];
						sincome = Double.toString(income[i]);
						sacreage = Double.toString(acreage[i]);
						sbuiltflag = builtflag [i];
						sbuiltyear= Integer.toString(builtyear [i]);
						sstories=  Double.toString(stories [i]);
						sexterior = exterior[i];
						sstyle= Integer.toString(style [i]);
						sfloodproof=  Double.toString(floodproof [i]);
						sbuiltassess=  Double.toString(builtassess [i]);
						shouseholdcount = Integer.toString(householdcount[i]);
						ssqft = Double.toString(sqft[i]);
						
		//				System.out.println(acreage[i] + " " + sacreage  + " " + sqft[i]  + " " + ssqft  + " " +  householdcount[i] + " " +  shouseholdcount);		
	//					System.out.println(slotid);
						
						w.newLine(); 
						w.write(sline + "\t" +slotnumber + "\t" + slat + "\t" + slong + "\t" + selev + "\t" + slandassess + "\t" + ssaledate
								+ "\t" + ssaleprice + "\t" + sowneroccupied + "\t" + srace + "\t" + ssex + "\t" + sage + "\t" + smortgage + "\t" + sincome + "\t" + sacreage + "\t" +sbuiltflag + "\t" + sbuiltyear + "\t" + sstories 
								+ "\t" + sexterior + "\t" + sstyle + "\t" + sfloodproof + "\t" + sbuiltassess + "\t" + ssqft + "\t" + shouseholdcount);
						w.flush();
					}
					w.close();
				}
				catch (Exception e)
				{
					System.err.println("File output error");
				}
			}
	
			//This method reads in tide gauge (number, latitude, longitude) from file
			void setupDDFFromFile(String filename)
			 {
				 Scanner DDFFile = null;
					try
					{
						DDFFile = new Scanner(new File(filename));
						System.out.println("DDF file found");
					}
					catch (Exception e)
					{
						System.err.println("DDF input error");
					}
					while (DDFFile.hasNextLine())
					{
						String ddfline = "";
						ddfline = DDFFile.nextLine();
						String sstories="";
						String sdepth="";
						String sdamage="";
						Scanner segment = null;
		
						//Parse line of property information
						try
						{
							segment = new Scanner(ddfline).useDelimiter(",");
//debug							System.out.println(totalGauges + " parsing");
						}
						catch (Exception e)
						{
							System.err.println(totalDDFs + "new line error");
						}
						
						//read Gauge ID number
						try
						{
							sstories = segment.next();
// debug							System.out.println(sstories);
							DDFstories [totalDDFs] = Integer.parseInt(sstories);
//debug							System.out.println(totalGauges + " " + gaugeID [totalGauges]);
						}
						catch (Exception e)
						{
							System.err.println(totalDDFs + "  DDF stories error");
						}
		
						//read Lot Latitude
						try
						{
							sdepth = segment.next();
							DDFheight [totalDDFs] = Double.parseDouble(sdepth);		
						}
						catch (Exception e)
						{
							System.err.println(totalDDFs + " DDF height error");
						}					
						
						//read Lot Longitude
						try
						{
							sdamage = segment.next();
							DDFdamage [totalDDFs] = Double.parseDouble(sdamage);	
						}
						catch (Exception e)
						{
							System.err.println(totalDDFs + " DDF damage error");
						}					
//debug						System.out.println(totalDDFs + " " + DDFstories [totalDDFs] + " " + DDFheight [totalDDFs]  + " " + DDFdamage [totalDDFs]);
						totalDDFs = totalDDFs + 1;  //increment DDF lone count
						
						segment.close();
					}
						int DDFstorycheck = 0;
//debug						System.out.println(totalDDFs);
						
						for (int i=0; i<totalDDFs; i++)
						{
							Integer tempstories = DDFstories[i];
							if(tempstories.equals(DDFstorycheck))
							{
//debug								System.out.println(i + " " + tempstories + " " + DDFstorycheck);
							 DDF tempDDF = damageFunction.get(DDFstorycheck-1);
							 tempDDF.addDDFheight(DDFheight[i]);
							 tempDDF.addDDFdamage(DDFdamage[i]);
							}
							else
							{
//debug								System.out.println(i + " " + tempstories + " " + DDFstorycheck);
								DDF tempDDF = new DDF (DDFstorycheck);
								DDFstorycheck = DDFstorycheck+1;
								damageFunction.add(tempDDF);
							}	
						}
						
						for (int i=0; i<DDFstorycheck; i++)	
						{
							DDF tempDDF = damageFunction.get(i);
							
							int damagecount = tempDDF.getDamageCount();
							
							for (int j=0; j<damagecount; j++)
							{
								double heightddf = tempDDF.getHeight(j);
								double damageddf = tempDDF.getDamage(j);
								int ddfid = tempDDF.getStories();
//debug								System.out.println(i + "  " + ddfid + "  " + heightddf + "  " + damageddf);
							}
						}
				}

			//This method establishes Gauges for each tide gauge location
			 void setupTideGaugesFromFile(String filename)
			 {
				 Scanner gaugeFile = null;
					try
					{
						 gaugeFile = new Scanner(new File(filename));
						System.out.println("Tide Gauge file found");
					}
					catch (Exception e)
					{
						System.err.println("Tide Gauge input error");
					}
					while (gaugeFile.hasNextLine())
					{
						String property = "";
						property = gaugeFile.nextLine();
						String sid="";
						String slat="";
						String slong="";
						Scanner segment = null;
		
						//Parse line of property information
						try
						{
							segment = new Scanner(property).useDelimiter(",");
//debug							System.out.println(totalGauges + " parsing");    
						}
						catch (Exception e)
						{
							System.err.println(totalGauges + "new line error");
						}
						
						//read Gauge ID number
						try
						{
							sid = segment.next();
//debug							System.out.println(sid);     //debug
							gaugeID [totalGauges] = Integer.parseInt(sid);
//debug							System.out.println(totalGauges + " " + gaugeID [totalGauges]);    //debug
						}
						catch (Exception e)
						{
							System.err.println(totalGauges + "gauge ID error");
						}
		
						//read Lot Latitude
						try
						{
							slat = segment.next();
							gaugelat [totalGauges] = Double.parseDouble(slat);		
						}
						catch (Exception e)
						{
							System.err.println(totalGauges + "lot Latitude error");
						}					
						
						//read Lot Longitude
						try
						{
							slong = segment.next();
							gaugelong [totalGauges] = Double.parseDouble(slong);	
						}
						catch (Exception e)
						{
							System.err.println(totalGauges + "lot Longitude error");
						}					
//debug						System.out.println(totalGauges + " " + gaugeID [totalGauges] + " " + gaugelat [totalGauges]  + " " + gaugelong [totalGauges]);	//debug
						
						Gauge tempGauge = new Gauge (totalGauges, gaugeID[totalGauges], gaugelat[totalGauges], gaugelong[totalGauges]);
						TideList.add(tempGauge);
						totalGauges = totalGauges + 1;  //increment gauge count

						segment.close();
					}
			}

			 //this method links lots to their nearest tide gauge
			void associateTideGaugestoLots() 
			{
				for (int i=0; i<totalLots; i++)
				{
					int low = -99;
					double minDistance = 99999;
					Lot l = ((Lot)allLots.get(i));
					double llat= l.getY();
					double llong = l.getX();
					
					for (int j=0; j<totalGauges; j++)
					{
						Gauge g = ((Gauge)TideList.get(j));
						double glat = g.getY();
						double glong = g.getX();
						double avglat = (glat + llat)/2;
						double xscale = Math.cos(Math.toRadians(avglat));
						double currentDistance = Math.sqrt(Math.pow((glat-llat),2)+Math.pow(((glong-llong)*xscale),2));
						if (currentDistance < minDistance)
						{
							low = j;
							minDistance = currentDistance;
						}
					}
					l.setGauge(low);
				}
				for (int i=0; i<totalLots; i++)
				{
					Lot j = (Lot)allLots.get(i);
//debugging					System.out.println(i + " " + j.lotGauge);      //debug
				}

			}
			
			//this method establishes the floods from file data and assigns flood heights to each tide gauge
			void setupFloodsFromFile(String filename, int count)
			{
				ArrayList<Gauge> FloodList = new ArrayList<Gauge>();
				try
				{	//open file
					FileInputStream fstream = new FileInputStream(filename);
					
					//convert input stream to a BufferedReader
					BufferedReader d = new BufferedReader (new InputStreamReader(fstream));
					
					//get the parameters from the file
					String s;
					
					int parts = (count * 2) + 1;
					int quantity = 0;
					double floodTime = 0;
					double floodparts[] = new double[100]; 
						while ((s=d.readLine()) != null)
							{
								String[] floodpts = s.split(",");
								floodparts = new double[parts];
								for (int j = 0; j <parts; j++)
								{
//debugging								System.out.println(j + " " + floodpts[j]);      //debug
									floodparts[j]= Double.parseDouble(floodpts[j]);
//debugging								System.out.println(j + " " + floodparts[j]);     //debug
								}
								floodTime = floodTime + floodparts[0];

							System.out.println(floodTime);

							for (int j = 1; j < parts-1; j+=2)
								{
//debugging									System.out.println(j + " parts: " + (parts-1)+ " parts: " + totalGauges);	//debugging		
									for (int i = 0; i < totalGauges; i++)
									{
										if (floodparts[j]==gaugeID[i])
										{
//for baseline											gaugereading[i]=floodparts[j+1]+0.24;  //TODO inputs in NAVD88, convert to MSL for Sandy (+0.24)
											gaugereading[i]=floodparts[j+1];  //for FEMA data no increase
											Gauge g = ((Gauge)TideList.get(i));
											g.setFloodLevel(totalFloods, gaugereading[i]);
											double testheight = g.getFloodLevel(totalFloods);
//debugging											System.out.println(i + " " + testheight);	//debug
										}
//debugging										System.out.println(j + " " + i + " " + totalGauges);	//debug
									}
								}
								
//debugging						System.out.println("interval: " + floodpts[0] + " time: " + floodTime);	
//debugging								for (int i = 0; i < totalGauges; i++)
//debugging								{
//debugging							System.out.println(" Gauge: " + gaugeID[i] + " Height: " + gaugereading[i]);	//debug
//debugging								}
								
							//implement an object flood for each flood event
							Flood flood = new Flood(this, quantity, gaugeID, gaugelat, gaugelong, gaugereading);
								schedule.scheduleOnceIn(floodTime,flood,2);
								this.floodtimeExternal = floodTime;
								quantity++;
								totalFloods = quantity;
							}
						d.close();
					}
				catch (Exception e) {System.out.println(e);}
			}
}	

