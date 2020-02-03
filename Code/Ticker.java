package floodresponse;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.distribution.Normal;
import sim.util.distribution.Uniform;
import ec.util.MersenneTwisterFast;

//Ticker provides the scheduler for annual updates 
//GROWTH Annual updates include  population increase based upon growth rate and previous year's population
//Storm events are scheduled in class:Flood

public class Ticker implements Steppable
{
	ABM world;
	int limitYears = 100;
	public int simulationAge;
	
	//Statistics
	public int totalHouses = 0;
	public int emptyHouses = 0;
	public int floodHouses = 0;
	public int floodproofHouses = 0;
	public double floodproofing = 0;
	public double averagefloodproofHeight = 0;
	public int totalOccupants = 0;
	public double sumSize = 0;
	public String fout= " ";
	public String floodfile = " ";
	public String sumfile = " ";
	BufferedWriter report = null;
	static BufferedWriter reportflood = null;
	static BufferedWriter reportsum = null;
	
	//establishes object Ticker 
	public Ticker(SimState state, int limitYears, String fout, String floodfile, String floodsum)
	{
		world = (ABM) state;
		simulationAge = 1990;
		this.limitYears=limitYears;
		this.fout = fout;
		this.floodfile = floodfile;
		this.sumfile= floodsum;

		//print out running parameters
		try
		{
			report = new BufferedWriter(new FileWriter(fout + "_" + world.id + ".txt"));
			reportflood = new BufferedWriter(new FileWriter(floodfile + "_" + world.id + ".txt"));
			reportsum = new BufferedWriter(new FileWriter(sumfile +".txt",true));
		}
		catch (Exception e)
		{
			System.err.println("Error establishing output file");
		}
	}

	//method step includes the annual sequence of events
	@Override
	public void step(SimState state)
	{
		simulationAge = simulationAge + 1;

		//check to see if simulation should end
		if (simulationAge == limitYears + 1)
		{
			System.out.println("Simulation Duration Reached");
			if(report != null)
				try 
				{
					report.close();
				}
				catch (IOException e)
				{
					System.err.println("error at Ticker81 report null");
					e.printStackTrace();
				}
			if(reportflood != null)
				try 
				{
					reportflood.close();
				}
				catch (IOException e)
				{
					System.err.println("error at Ticker98 flood report null");
					e.printStackTrace();
				}

			if(reportsum != null)
				try 
				{
					reportsum.close();
				}
				catch (IOException e)
				{
					System.err.println("error at Ticker98 flood report null");
					e.printStackTrace();
				}
		world.finish();
		}

		//calculate annual data
		totalHouses = ABM.houseList.size();

		//write annual outputs
		if(report !=null && simulationAge < limitYears + 1)
		{
			try
			{
				report.write("************************ " + simulationAge + " ************************");
				report.newLine();
				report.write(simulationAge + "\t" +totalHouses + "\t" + floodHouses + "\t" +  floodproofHouses + "\t" 
				+ averagefloodproofHeight + "\t" + totalOccupants);
				report.newLine();
				report.write("************************************************************");
				report.newLine();
				
				for (Houses xHouses: ABM.houseList)
				{
					int housetemp = xHouses.getId();
					double fphtemp = xHouses.getFloodproofheight();
					int fptemp = (int) fphtemp;
					report.write(housetemp + "\t" + fptemp);
					report.newLine();
				}
			}
			catch (IOException e)
			{
				System.err.println("error at Ticker400 report null");
				e.printStackTrace();
			}
		}
		
		//reset annual statistics
		sumSize = 0;
		floodHouses = 0;
		floodproofing = 0;
		averagefloodproofHeight = 0;
		floodproofHouses = 0;
	}
}
