package gamestates;

import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.*;

public class PinballGameStateSettings
{
	private static final float camMoveSpeedDefault = 5f, camTurnSpeedDefault = 0.25f;
	
	private static final int inclinationLevelDefault = 5;
	
	private static final String rendererDefault = "LWJGL";
	
	private static Vector3f camStartPosDefault = new Vector3f(/*0.0f, 15.0f, 9.0f*/ 0f, 18.64f, 7.41f), camStartLookAtDefault = new Vector3f(0.0f, /*8.5f*/ 10f, 0.0f); 
	
	private Vector3f camStartPos, camStartLookAt;
	
	private float camMoveSpeed, camTurnSpeed;
	
	private int width, height, depth, freq;
	
	private int inclinationLevel;
	
	private boolean fullscreen;
	
	private Quaternion inclinationQuaternion;

	public PinballGameStateSettings()
	{
		camMoveSpeed = camMoveSpeedDefault;
		camTurnSpeed = camTurnSpeedDefault;
		inclinationLevel = inclinationLevelDefault;
		camStartLookAt = camStartLookAtDefault;
		camStartPos = camStartPosDefault;
		
		inclinationQuaternion = new Quaternion();
		inclinationQuaternion.fromAngles(FastMath.DEG_TO_RAD * inclinationLevel, 0f, 0f);
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

	public Vector3f getCamStartPos()
    {
        return new Vector3f( camStartPos );
    }

    public void setCamStartPos( Vector3f camStartPos )
    {
        this.camStartPos = camStartPos;
    }

    public Vector3f getCamStartLookAt()
    {
        return new Vector3f( camStartLookAt );
    }

    public void setCamStartLookAt( Vector3f camStartLookAt )
    {
        this.camStartLookAt = camStartLookAt;
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
		return rendererDefault;
	}

	public int getInclinationLevel()
	{
		return inclinationLevel;
	}

	public void setInclinationLevel(int inclinationLevel)
	{
		this.inclinationLevel = inclinationLevel;
		
		/* Recalculo el quaternion */
		inclinationQuaternion = new Quaternion();
		inclinationQuaternion.fromAngles(FastMath.DEG_TO_RAD * inclinationLevel, 0f, 0f);
	}

	public Quaternion getInclinationQuaternion()
	{
		return inclinationQuaternion;
	}
	
}
