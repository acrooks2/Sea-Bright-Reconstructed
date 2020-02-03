package floodresponse;

//Class Owner is implemented to develop the population and behaviors

//
import ec.util.MersenneTwisterFast;
import sim.util.Double2D;
import sim.util.distribution.Uniform;

public class Owner {

	ABM world;
	
	// Attributes
	
	int rep = ABM.repno;
	long seed = 0000000000000000000000 + rep;			//seed for random number generator
	
	int ownerid = -1;
	int lotowned = -1;
	int lotid = -1;
	int houseowned = -1;
	Houses home = null;
	Lot plot = null;
	double income = 0;
	String race = "";
	String sex = "";
	Integer age = 0;
	String mortgageStatus = "";
	Double2D loc = null;
	
	
	Integer historyYear = 1999;
	double historyHeight = 0.0;
	double riskHeight = 0.0;
	double oldriskHeight = 0.0;
	double riskLevel = 0.0;
	double sexFlag = -9999999.00;	
	double experienceFlag = 0.00;	
	
	double riskmit = 0;
	double riskinsur = 0;
	double riskrace = 0;
	double femrisk=0;
	double experrisk=0;
	double incrisk = 0;
	double femincome= 0;
	double femexperience=0;
	double incexperience=0;
	double femincexperience=0;
	int raceflag = 0;

	
	Lot moveLot = null;
	double moveCost = 99999999.00;
	double mitCost = 99999998.00;
	double repairCost = 99999997.00;
	double mitBenefit = 99999996.00;	
	double moveBenefit = 99999995.00;
	double mortgagepayment = 9999999.00;
	double currenthousingpercent = 999.0;
	
	MersenneTwisterFast randowner = new MersenneTwisterFast(seed);
	
	public Owner(int id, int lotnumber, String race, String sex, int age, String mortgagestatus, double risk[], double income, double uncoor)
	{
		this.ownerid = id;
		this.lotowned = lotnumber;
		this.lotid = id;
		this.race = race;
		this.sex = sex;
		this.age = age;
		this.mortgageStatus = mortgagestatus;
		this.income = income;
		if(race.equals("White")) {raceflag = 1;}
		
		

		
		riskmit=risk[0];
		riskinsur=risk[1];
		femrisk=risk[2];
		experrisk=risk[3];
		incrisk=risk[4];
		femincome=risk[5];
		femexperience=risk[6];
		incexperience=risk[7];
		femincexperience=risk[8];
		riskrace = risk[9];


		if (this.sex.equals ("Female")) {sexFlag = 1.0;}
		else {sexFlag = 0.0;}
		
		riskLevel = (sexFlag*femrisk) + (income*incrisk/200000) + (experienceFlag * experrisk) + (riskrace * raceflag)- (femincome*sexFlag*income/200000)
				- (femexperience * experienceFlag * sexFlag) - (incexperience*experienceFlag*income/200000) + (femincexperience*experienceFlag*sexFlag*income/100000) + uncoor * (1-(femrisk + incrisk + experrisk + riskrace));

		Lot templot = ABM.allLots.get(lotid);
		Double tlat = (templot.getY()-40.343)*1800;		//translated to 0,0 and scaled to 1 mile
		final Double tlong = (74.0055+templot.getX())*1800;
		loc = new Double2D (tlat, tlong);
	}
	
	// Accessors
	
	public int getOwnerId() {return ownerid;}
	public int getOwnerLotNumber() {return lotowned;}
	public int getOwnerLotID() {return lotid;}
	public int getOwnerHouseNumber() {return houseowned;}
	public Houses getOwnerHouse() {return home;}
	public Lot getOwnerLot() {return plot;}
	public double getOwnerIncome() {return income;}
	public String getOwnerRace() {return race;}
	public String getOwnerSex() {return sex;}
	public int getOwnerAge() {return age;}
	public String getMortgageStatus() {return mortgageStatus;}
	public int getOwnerFloodYear() {return historyYear;}
	public double getOwnerFloodLevel() {return historyHeight;}
	public double getOwnerRiskHeight() {return riskHeight;}
	public double getSexFlag() {return sexFlag;}
	public Double2D getlocation() {return loc;}
	
	public void setOwnerHomeId (int newhomeId)
	{
		this.houseowned = newhomeId;
	}

	public void setOwnerLotNumber (int lotowned)
	{
		this.lotowned = lotowned;
	}

	public void setOwnerLotId (int lotid)
	{
		this.lotid = lotid;
	}

	public void setOwnerHouseNumber (int house)
	{
		this.houseowned = house;
	}

	public void setOwnerHouse (Houses home)
	{
		this.home = home;
	}

	public void setOwnerLot (Lot plot)
	{
		this.plot = plot;
	}
	
	public void setOwnerIncome (double  money)
	{
		this.income = money;
	}

	public void setOwnerRace (String  race)
	{
		this.race = race;
	}

	public void setOwnerSex (String  sex)
	{
		this.sex = sex;
	}

	public void setOwnerAge (int years)
	{
		this.age = years;
	}

	public void setMortgageStatus (String  mortgagestatus)
	{
		this.mortgageStatus = mortgagestatus;
	}


	public void setOwnerFloodYear (int fyear)
	{
		this.historyYear = fyear;
	}

	public void setOwnerFloodHeight (double  height)
	{
		this.historyHeight = height;
	}

	public void setOwnerExperienceFlag (double  flag)
	{
		this.experienceFlag = flag;
	}
	
	public void setOwnerOldRiskHeight ()
	{
		oldriskHeight = riskHeight;
	}
	
	public void resetOwnerRiskHeight ()
	{
		riskHeight = oldriskHeight;
	}

	public void setOwnerRiskHeight (int cyear, double fheight)
	{
		if (cyear - 7 <= historyYear)
		{

//test			this.riskHeight = Math.max(fheight, historyHeight) + ABM.projectedSLR * 2 * ABM.udistro.nextDouble();
			this.riskHeight = fheight; //test no history
		}
		this.historyYear = cyear;
		this.historyHeight = fheight;

	}
	
	public double calcOwnerRiskLevel (int cyear, double uncoor)
	{


		riskLevel = (sexFlag*femrisk) + (income*incrisk/200000) + (experienceFlag * experrisk)  + (riskrace * raceflag) - (femincome*sexFlag*income/200000)
				- (femexperience * experienceFlag * sexFlag) - (incexperience*experienceFlag*income/200000) + (femincexperience*experienceFlag*sexFlag*income/100000) + uncoor * (1-(femrisk + incrisk + experrisk + riskrace));
		return riskLevel;
	}
	
	public double calcmitigationcost(Houses temphouse)
	{
		double mitOverhead = 999999;
		double size = temphouse.getSquarefootage();
		String type = temphouse.getHomeType();
		double freeboard = temphouse.getFloodproofheight();
		if (type.equals ("F")) {mitOverhead = ABM.frameelevationoverhead;}
		else {mitOverhead = ABM.masonelevationoverhead;}
		if (riskHeight == 0.0) {mitCost = 9999999.0;}
		else 
		{
			if (freeboard > 0)
			{
			mitCost = size * (mitOverhead + (ABM.elevationperfoot * (riskHeight - freeboard)));
			}
			else
			{
			mitCost = size * (mitOverhead + (ABM.elevationperfoot * (riskHeight)));	
			}
		}
		return mitCost;
	}

	public double calcrepaircost(Houses temphouse, double tempDDF)
	{
		double housevalue =temphouse.getHomevalue();
		double repairCost = tempDDF * housevalue / 100.0;
		return repairCost;
	}

	public double calcmitigationbenefit(Houses temphouse)
	{
		double mitBenefit = 0.0;
		double heightguide = -1.0;
		int damageguide = 0;
		double ddfdamage = 0.0;
		Integer housestyle = temphouse.getStyle();
		if (housestyle.equals(3))
		{
			DDF tempDDF = ABM.damageFunction.get(3);
			while (riskHeight > heightguide)
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
				while (riskHeight > heightguide)
				{
					ddfdamage = tempDDF.getDamage(damageguide);
					heightguide = tempDDF.getHeight(damageguide);
					damageguide = damageguide +1;
				}
			}
			else if (housestories < 3.0)
			{
				DDF tempDDF = ABM.damageFunction.get(1);
				while (riskHeight > heightguide)
				{
					ddfdamage = tempDDF.getDamage(damageguide);
					heightguide = tempDDF.getHeight(damageguide);
					damageguide = damageguide +1;
				}
			}
			else 
			{
				DDF tempDDF = ABM.damageFunction.get(2);
				while (riskHeight > heightguide)
				{
					ddfdamage = tempDDF.getDamage(damageguide);
					heightguide = tempDDF.getHeight(damageguide);
					damageguide = damageguide +1;
				}
			}
		}
		double adjDDFdamage = ddfdamage * (1 + ABM.damageBias);
		double housevalue =temphouse.getHomevalue();
		if (riskHeight > 0.0 && riskLevel > riskmit)   //risk benefit is zero unless there is sufficient perceived risk and risk height
		{
			double referencehurricanes = ABM.futurehurricanes;
			double hurricanes = referencehurricanes;		//straight number 
			mitBenefit = hurricanes * housevalue * (adjDDFdamage/100 + ABM.nonstructuralmitigationpercent);
		}
		else {mitBenefit = 0.0;}	//set to limit mitigation to damaged buildings with high risk perceptions
		return mitBenefit;
	}
	
	public double calcmovebenefit()
	{
		return moveBenefit;
	}
	public Lot calcrelocationtarget()
	{
		return moveLot;
	}

	public double getMortgageValue(Houses temphouse, int cyear) 
	{
		double homevalue = temphouse.getHomevalue();
		Lot templot = temphouse.getLot();
		double landvalue = templot.getLandvalue();
		int syear = temphouse.getSaleYear();
		int duration = Math.max(0, 30-(cyear-syear));
		double mortgageValue = duration * (landvalue + homevalue) / 30.0;
		return  mortgageValue;
	}

	public double getMortgagePayment(Houses temphouse, int cyear)    //annual mortgage payments
	{
		double homevalue = temphouse.getHomevalue();
		Lot templot = temphouse.getLot();
		double landvalue = templot.getLandvalue();
		int syear = temphouse.getSaleYear();
		if (cyear-syear>30) 
		{
			mortgagepayment = 0.0;
		}
		else
		{
			mortgagepayment = 12 * ABM.interestrate * (landvalue + homevalue) / 100000.0;  //annual payment (assumes zero down)
		}
		return  mortgagepayment;
	}

	public double gethousingMoney()     //annual value of money for housing
	{
		double percent = 0;
		currenthousingpercent = mortgagepayment/income;
		if(income < 50000)
		{
			percent = 0.3 + 0.1*ABM.udistro.nextDouble();
		}
		else if (income > 75000)
		{
		percent = 0.15 + 0.2 * ABM.udistro.nextDouble();  //vdistro 0.15,0.35,
		}
		else
		{
			double check = 0.1 + 0.3 * ABM.udistro.nextDouble(); //t distro 0.1,0.4,
			if (check >0.2)
			{
				percent = 0.15 + 0.15 * ABM.udistro.nextDouble();  //wdistro 0.15,0.30,
			}
			else
			{

				percent = 0.3 + 0.1 * ABM.udistro.nextDouble();		//sdistro		0.3,0.40,
			}
		}
		double minpercent = ABM.minhousingpercentincrease + (ABM.maxhousingpercentincreast - ABM.minhousingpercentincrease) * ABM.udistro.nextDouble(); //xdistro 2000.0,10000.00
		double minincrease = ABM.minhousingincrease + (ABM.maxhousingincrease - ABM.minhousingincrease) * ABM.udistro.nextDouble();  //ydistro  minhousingincrease,maxhousingincrease
		double percentnew = ABM.percenthousingShift;

		if(percent < currenthousingpercent) {percent = currenthousingpercent + minpercent;}  //limit changes in housing percentages to increases only
		double housemoney = income * (percent * percentnew + currenthousingpercent * (1-percentnew)); 
		//dampen percent of income to housing based upon current rate

		if (housemoney < minincrease) {housemoney = minincrease;}
		currenthousingpercent = housemoney/income;
		return housemoney;
	}

	public double getInsurancePayment(Houses temphouse, double damage)  //currently limit insurance to homes with mortgages  //lump sum payment
	{
		double payment = 0;
		if (mortgageStatus.equals ("Mortgage"))
		{
			double damagevalue = damage * temphouse.getHomevalue();
			payment = Math.min(250000, damagevalue);
		}
		return payment;
	}

	public double getOwnerRiskLevel() {
		return riskLevel;
	}

	//for visualization of risk, scale from decimal to percentage 
	public double getOwnerRiskVis() {
		double riskvis = riskLevel * 100;
		return riskvis;
	}
	public double getOwnerExperience() {
		return experienceFlag;
	}

	public void decreaseBuildingID() 
	{
		houseowned = houseowned - 1;
	}
}
