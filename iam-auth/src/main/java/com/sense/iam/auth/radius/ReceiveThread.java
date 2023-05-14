package com.sense.iam.auth.radius;

import java.net.DatagramPacket;

public class ReceiveThread implements Runnable
{
	private RadiusServer radiusServer = null;

	private DatagramPacket in = null;

	public ReceiveThread(RadiusServer radiusServer, DatagramPacket in)
	{
		this.radiusServer = radiusServer;
		this.in = in;
		try
		{
			(new Thread(this)).start();
		}
		catch (Exception e)
		{
		}
	}

	public void run()
	{
		try
		{
			radiusServer.receive(in);
		}
		catch (Exception e)
		{
		}
	}
}
