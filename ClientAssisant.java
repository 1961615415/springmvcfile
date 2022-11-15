package cn.knet.businesstask.util;

import cn.knet.businesstask.domain.service.ZDNSResolveUpdateService;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


public class ClientAssisant {
	public static void clientAssisant(ZDNSResolveUpdateService client) {
		Client clientProxy = ClientProxy.getClient(client);

		HTTPConduit conduit = (HTTPConduit) clientProxy.getConduit();
		String targetAddr = conduit.getTarget().getAddress().getValue();
		if (targetAddr.toLowerCase().startsWith("https:")) {
			TrustManager[] simpleTrustManager = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}

				public void checkServerTrusted(
						java.security.cert.X509Certificate[] certs,
						String authType) {
				}
			} };
			TLSClientParameters tlsParams = new TLSClientParameters();
			tlsParams.setTrustManagers(simpleTrustManager);
			tlsParams.setDisableCNCheck(true);
			conduit.setTlsClientParameters(tlsParams);
		}
	}
	
	public static void clientRelease(ZDNSResolveUpdateService client){
		Client clientProxy = ClientProxy.getClient(client);
		HTTPConduit conduit = (HTTPConduit) clientProxy.getConduit();
		TLSClientParameters tlsParams = conduit.getTlsClientParameters();
		TrustManager[] simpleTrustManage = tlsParams.getTrustManagers();
		simpleTrustManage = null;
		tlsParams = null;
		conduit = null;
		clientProxy = null;
		client = null;
	}
}
