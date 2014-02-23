package fr.ribesg.alix.network;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLHandler {

	/**
	 * Creates a Trusting SSL Socket: accepts any certificate.
	 * Use with caution.
	 *
	 * @param url  the url to connect to
	 * @param port the port to connect to
	 *
	 * @return a SSL Socket
	 *
	 * @throws SSLException if something went wrong
	 */
	public static Socket getTrustingSSLSocket(final String url, final int port) throws SSLException {
		try {
			final SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(null, new TrustManager[] {
					new X509TrustManager() {

						@Override
						public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
							// No Exception = Accept all
						}

						@Override
						public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s) throws CertificateException {
							// No Exception = Accept all
						}

						@Override
						public X509Certificate[] getAcceptedIssuers() {
							return new X509Certificate[0];
						}
					}
			}, new SecureRandom());
			final SSLSocketFactory factory = sslContext.getSocketFactory();
			final SSLSocket resultSocket = (SSLSocket) factory.createSocket(url, port);
			resultSocket.startHandshake();
			return resultSocket;
		} catch (Exception e) {
			throw new SSLException("Failed to create SSL socket", e);
		}
	}

}
