package com.MAVLink.Messages.common;

import com.MAVLink.Messages.IMAVLinkMessage;

public class msg_global_position_int extends IMAVLinkMessage{

	public msg_global_position_int(){ messageType = MAVLINK_MSG_ID_GLOBAL_POSITION_INT; }

	public static final int MAVLINK_MSG_ID_GLOBAL_POSITION_INT = 73;

	private static final long serialVersionUID = MAVLINK_MSG_ID_GLOBAL_POSITION_INT;

	public 	long lat; ///< Latitude, expressed as * 1E7
	public 	long lon; ///< Longitude, expressed as * 1E7
	public 	long alt; ///< Altitude in meters, expressed as * 1000 (millimeters)
	public 	int vx; ///< Ground X Speed (Latitude), expressed as m/s * 100
	public 	int vy; ///< Ground Y Speed (Longitude), expressed as m/s * 100
	public 	int vz; ///< Ground Z Speed (Altitude), expressed as m/s * 100

}