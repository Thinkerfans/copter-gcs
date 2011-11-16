package com.MAVLink.Messages.common;

import com.MAVLink.Messages.IMAVLinkMessage;

public class msg_local_position_setpoint extends IMAVLinkMessage{

	public msg_local_position_setpoint(){ messageType = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT; }

	public static final int MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT = 51;

	private static final long serialVersionUID = MAVLINK_MSG_ID_LOCAL_POSITION_SETPOINT;

	public float x; ///< x position
	public float y; ///< y position
	public float z; ///< z position
	public float yaw; ///< Desired yaw angle

}