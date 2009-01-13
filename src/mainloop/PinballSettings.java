package mainloop;

public class PinballSettings
{
	private static float camMoveSpeedDefault = 50, camTurnSpeedDefault = 0.4f, inclinationAngleDefault = 10.0f;
	
	private float camMoveSpeed, camTurnSpeed;
	
	private int width, height, depth, freq;
	
	private float inclinationAngle;
	
	private boolean fullscreen;
	
	private String renderer;

	public PinballSettings()
	{
		camMoveSpeed = camMoveSpeedDefault;
		camTurnSpeed = camTurnSpeedDefault;
		inclinationAngle = inclinationAngleDefault;
	}
	
	public void setCamMoveSpeed(float camMoveSpeed)
	{
		this.camMoveSpeed = camMoveSpeed;
	}

	public float getCamMoveSpeed()
	{
		return camMoveSpeed;
	}

	public void setCamTurnSpeed(float camTurnSpeed)
	{
		this.camTurnSpeed = camTurnSpeed;
	}

	public float getCamTurnSpeed()
	{
		return camTurnSpeed;
	}

	public int getWidth()
	{
		return width;
	}

	public void setWidth(int width)
	{
		this.width = width;
	}

	public int getHeight()
	{
		return height;
	}

	public void setHeight(int height)
	{
		this.height = height;
	}

	public int getDepth()
	{
		return depth;
	}

	public void setDepth(int depth)
	{
		this.depth = depth;
	}

	public int getFreq()
	{
		return freq;
	}

	public void setFreq(int freq)
	{
		this.freq = freq;
	}

	public boolean isFullscreen()
	{
		return fullscreen;
	}

	public void setFullscreen(boolean fullscreen)
	{
		this.fullscreen = fullscreen;
	}

	public String getRenderer()
	{
		return renderer;
	}

	public void setRenderer(String renderer)
	{
		this.renderer = renderer;
	}

	public float getInclinationAngle()
	{
		return inclinationAngle;
	}

	public void setInclinationAngle(float inclinationAngle)
	{
		this.inclinationAngle = inclinationAngle;
	}
	
}
