package com.plasne;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;

import javax.naming.ServiceUnavailableException;

import java.net.*;
import java.io.*;

import java.time.*;
import java.time.format.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.Charset;
import java.util.*;

public class App 
{

    private final static String AUTHORITY = "https://login.microsoftonline.com/microsoft.onmicrosoft.com/";
    private final static String CLIENT_ID = "be94389d-efb4-40b7-a4ba-6d18b4ecbeb6";
    private final static String CLIENT_SECRET = "Ykip2aLWOq55+4SStxSkASW/DdD831WY+xGyUaXkLOw=";
    private final static String RESOURCE = "https://management.azure.com/";
    private final static String SUBSCRIPTION = "83e686f6-963b-4e64-bff4-99dc369cb4ec";
    private final static String RESOURCE_GROUP = "pelasne-oms";
    private final static String WORKSPACE = "pelasne-oms";
    private final static String WORKSPACE_ID = "7fbea91c-ae1e-4521-8f72-590f6e12f6b2";
    private final static String WORKSPACE_KEY = "DXUVDTQhlT+0RKxa184eQoI0xCGK9Xw0XtN3KxJsAlLomWYMDqqS8LWfnTEFlyqLNbBqMRta0TgVcOf5ib4nDg==";

    public static void main( String[] args ) throws Exception
    {
	String mode = (args.length > 0) ? args[0] : "query";
	switch(mode) {
	    case "query":
		query();
	        break;
	    case "post":
		post();
		break;
	}
    }

    public static void query() throws Exception
    {

	// get an access token
	AuthenticationResult result = acquireTokenWithClientCredentials();

	// create the connection
	URL url = new URL("https://management.azure.com/subscriptions/" + SUBSCRIPTION + "/resourceGroups/" + RESOURCE_GROUP + "/providers/Microsoft.OperationalInsights/workspaces/" + WORKSPACE + "/query?api-version=2017-10-01");
	String body = "{ \"query\": \"search * | limit 5\" }";
	HttpURLConnection connection = (HttpURLConnection) url.openConnection();
	connection.setRequestMethod("POST");
	connection.setRequestProperty("Content-Type", "application/json");
	connection.setRequestProperty("Content-Length", Integer.toString(body.getBytes().length));
	connection.setRequestProperty("Authorization", "Bearer " + result.getAccessToken());
	connection.setUseCaches(false);
	connection.setDoOutput(true);

	// send the request
	DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
	wr.writeBytes(body);
	wr.close();

	// get the response
	InputStream is = connection.getInputStream();
	BufferedReader rd = new BufferedReader(new InputStreamReader(is));
	StringBuffer response = new StringBuffer();
	String line;
	while ((line = rd.readLine()) != null) {
	    response.append(line);
	    response.append("\r");
	}
	rd.close();
	connection.disconnect();

	// output
	System.out.println(response.toString());

    }

    public static void post() throws Exception
    {

	// variables (note time is RFC-1123 but requires the exact # of digits)
	String body = "{ \"field1\": \"stuff\", \"field2\": \"things\" }";
        String ts = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss O").format(ZonedDateTime.now(ZoneId.of("GMT")));
	String code = "POST\n" + body.length() + "\napplication/json\nx-ms-date:" + ts + "\n/api/logs";
	System.out.println(code);

	// create singature
	byte[] key = Base64.getDecoder().decode(WORKSPACE_KEY);
	Mac hasher = Mac.getInstance("HmacSHA256");
	hasher.init(new SecretKeySpec(key, "HmacSHA256"));
	byte[] hash = hasher.doFinal(code.getBytes());
	String signature = Base64.getEncoder().encodeToString(hash);

	// create the connection
	URL url = new URL("https://" + WORKSPACE_ID + ".ods.opinsights.azure.com/api/logs?api-version=2016-04-01");
	HttpURLConnection con = (HttpURLConnection) url.openConnection();
	con.setDoOutput(true);
	con.setRequestMethod("POST");
        con.setRequestProperty("Accept", "application/json");
	con.setRequestProperty("Content-Type", "application/json");
	con.setRequestProperty("Log-Type","demo");
	con.setRequestProperty("x-ms-date", ts);
	con.setRequestProperty("Authorization", "SharedKey " + WORKSPACE_ID + ":" + signature);

	// post
	DataOutputStream wr = new DataOutputStream(con.getOutputStream());
	wr.writeBytes(body);
	wr.flush();
	wr.close();

	// response
	System.out.println(con.getResponseCode() + ": " + con.getResponseMessage());

    }

    public static AuthenticationResult acquireTokenWithClientCredentials() throws Exception
    {

	// variables
	AuthenticationContext context = null;
	AuthenticationResult result = null;
	ClientCredential cred = null;
	ExecutorService service = null;

	// authenticate
	try {
	    service = Executors.newFixedThreadPool(1);
	    context = new AuthenticationContext(AUTHORITY, false, service);
	    cred = new ClientCredential(CLIENT_ID, CLIENT_SECRET);
	    Future<AuthenticationResult> future = context.acquireToken(RESOURCE, cred, null);
	    result = future.get();
	} finally {
	    service.shutdown();
	}

	// response
	if (result == null) {
	    throw new ServiceUnavailableException("authenticaton result was null");
	}
	return result;

    }

}
