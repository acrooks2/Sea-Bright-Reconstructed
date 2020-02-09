
/** This Class provides the UI's for visualizing the simulation, currently only building locations and damage levels are presented
/	
/	 mirrored from MASON19 Virus Demo
**/

import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.geom.Rectangle2D;
import javax.swing.JFrame;
//import javafx.scene.paint.Paint;
import sim.display.Console;
//import sim.display.Console;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
//import sim.field.continuous.Continuous2D;
//import sim.engine.Steppable;
//import sim.field.continuous.*;
import sim.portrayal.continuous.*;
//import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Double2D;

public class SandyFloodWithUI extends GUIState
{
	ABM Sandy;
	
	public Display2D display;
	public JFrame displayFrame;

	ContinuousPortrayal2D seaBrightPortrayal = new ContinuousPortrayal2D();
	ContinuousPortrayal2D owners = new ContinuousPortrayal2D();
	ContinuousPortrayal2D houses= new ContinuousPortrayal2D();  //acquiea

	public static Object getInfo()
	{
		return "<h2> Sea Bright, Hurricane Sandy </h2>"
				+ "<p> This is a simulation of coastal flooding, "
				+ "<p> damage and repair to investigate "
				+ "<p> the accuracy of the household response mental model";
	}
	
/**	protected SandyFloodWithUI (SimState state)
	{
		super(state);
		Sandy = (ABM) state;
	}
**/		
	public static void main(String[]args)
	{
		SandyFloodWithUI simple = new SandyFloodWithUI(new ABM(System.currentTimeMillis()));	//from acequiaWorld
		Console c = new Console (simple);
		c.setVisible(true);
	}
	
	public static String getName() {return "Sea Bright during Hurricane Sandy";}

	public SandyFloodWithUI ()
	{
	 super (new ABM(System.currentTimeMillis()));
	}
	
	public SandyFloodWithUI (SimState state)
	{
		super(state);
	}
	
	public void start()
	{
		super.start();
		setupPortrayals();
		
		Color p = new Color (0,0,225); //blue
		Color c = new Color (225,0,0); //red

		houses.setFrame(p);

		for (int i=0; i<ABM.totalHouses; i++)
		{
			Houses temphouse = ABM.houseList.get(i);
			Double2D lloc = temphouse.getlocation();
			ABM.SeaBright.setObjectLocation(temphouse, lloc);
		}
		houses.setField(ABM.SeaBright);
		houses.setPortrayalForAll(new HousePortrayals());
	}
	
	public void load (SimState state)
	{
		super.load(state);
		setupPortrayals();
	}
	
	public void setupPortrayals()
	{
		Color p = new Color (225,0,0);

		seaBrightPortrayal.setField(ABM.SeaBright);		//SeaBright specified in Flood
		seaBrightPortrayal.setFrame(p);
		houses.setField(ABM.SeaBright);
		owners.setField(ABM.SeaBright);
		
		display.reset();
		display.setBackdrop(Color.white);
		display.repaint();
	}

	public void init (Controller c)
	{
		super.init(c);
		display = new Display2D (750,750,this); //test
		display.setClipping(false);		//acequia
		displayFrame = display.createFrame();
		displayFrame.setTitle("Sea Bright Display");
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		display.attach(houses, "Houses", true); 
	}
	
	public void quit()
	{
		super.quit();
	}
}