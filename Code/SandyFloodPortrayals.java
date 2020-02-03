package floodresponse;


import java.awt.geom.Rectangle2D;


import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.*;
import sim.util.gui.ColorMap;
import java.awt.*;

class HousePortrayals extends RectanglePortrayal2D
{

	 static ColorMap damageColor = new sim.util.gui.SimpleColorMap (0, 50,
			 new Color (0, 225, 0), new Color (225, 0, 0));	


	Color houseColor = new Color (255,135,0);
	

	
	public void draw (Object object, Graphics2D graphics, DrawInfo2D info)
	{
		if (object == null)
		{
			return;
		}
		 
		Rectangle2D.Double draw = info.draw;

		 final double width = 4;
		 final double height = 4;		

		 final int x = (int) (draw.x - width / 2.0);
		 final int y = (int) (draw.y - height / 2.0);	 
		 final int w = (int) (width);
		 final int h = (int) (height);
		 
		 Houses hs = (Houses) object;
		 graphics.setColor(damageColor.getColor(hs.getDamage()));
		 graphics.fillRect(x, y, w, h);
	}
}	

	class OwnerPortrayals extends RectanglePortrayal2D
	{
		 static ColorMap riskColor = new sim.util.gui.SimpleColorMap (0, 100,
				 new Color (0, 225, 0), new Color (225, 0, 0));	      

		 public void draw (Object object, Graphics2D graphics, DrawInfo2D info)
		{
			if (object == null)
			{
				return;
			}
			 
			Rectangle2D.Double draw = info.draw;
			 final double width = 4;
			 final double height = 4;		
			 final int x = (int) (draw.x - width / 2.0);
			 final int y = (int) (draw.y - height / 2.0);	 
			 final int w = (int) (width);
			 final int h = (int) (height);
 
			 Owner os = (Owner) object;
			 
			 graphics.setColor(riskColor.getColor(os.getOwnerRiskVis()));
			 graphics.fillRect(x, y, w, h);
		}
	
	}


