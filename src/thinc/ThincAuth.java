/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

import java.nio.ByteBuffer;

public class ThincAuth implements ThincMessageTypes {

	/** Creates a new instance of ThincAuth */
	public ThincAuth() {
	}

	ThincAuthMessage_to_Server auth_info = new ThincAuthMessage_to_Server();

	class ThincAuthMessage_to_Server {
		ThincMessageHeader header;

		String user;
		String passwd;
		int user_len;
		int pass_len;
	}

	void getAuthInfo() {
		// IF AUTH
		// OR GET INFO FROM FORM
		System.out.print("UserName: ");
		// System.in.read (auth_info.user.getBytes());
		auth_info.user_len = auth_info.user.length();

		System.out.print("Password: ");
		// System.in.read (auth_info.passwd.getBytes());
		auth_info.pass_len = auth_info.passwd.length();
	}

	void createAuthPacket() {
		auth_info.header.type = T_CLIENT_AUTH;
		auth_info.header.length = (short) (auth_info.user_len
				+ auth_info.pass_len + (2 * (Integer.SIZE / 8)));
	}

	public void sendClientAuth() {
	}

	// public ByteBuffer getAuthByteBuffer() {
	// }
}
