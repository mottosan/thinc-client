/*********************************************
 * Thinc Client
 * Amortya Ray & Divya Arora
 * {ar2566, da2254} @columbia.edu
 * Fall 2006-Fall 2007
 *********************************************/

package thinc;

// ORMitchell - 08/06/2003
//
// Simple little hack to allow the SSL/TLS based VNC java client to essentially
// ignore the remote SSL/TLS server's certificate CA !!!!!
// This will allow you to use "self signed" certificates on the Server (or
// Stunnel Setup)...
// No need to pay bucks to a CA (Certificate Authority) if you really don't have
// to...
//
// The security exposure in doing this is you could be talking to an invalid
// server, however, I assume you will know who you are talking to...
//
class AllowAllX509TrustManager implements javax.net.ssl.X509TrustManager {
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	public void checkClientTrusted(java.security.cert.X509Certificate[] chain,
			java.lang.String str)
			throws java.security.cert.CertificateException {
	}

	public void checkServerTrusted(java.security.cert.X509Certificate[] chain,
			java.lang.String str)
			throws java.security.cert.CertificateException {
	}
}
