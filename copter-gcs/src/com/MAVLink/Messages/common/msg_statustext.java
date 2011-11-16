package com.MAVLink.Messages.common;

import com.MAVLink.Messages.IMAVLinkMessage;

public class msg_statustext extends IMAVLinkMessage{

	public msg_statustext(){ messageType = MAVLINK_MSG_ID_STATUSTEXT; }

	public static final int MAVLINK_MSG_ID_STATUSTEXT = 254;

	private static final long serialVersionUID = MAVLINK_MSG_ID_STATUSTEXT;

	public int severity; ///< Severity of status, 0 = info message, 255 = critical fault
	public 	int[] text = new int[50];///< Status text message, without null termination character

}