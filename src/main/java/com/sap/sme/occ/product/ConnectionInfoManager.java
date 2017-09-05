package com.sap.sme.occ.product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log.Logger;

import com.sap.sme.vault.infra.ConfigurationProvider;
import com.sap.sme.vault.infra.impl.ConfigurationProviderSingleton;
import com.sap.sme.vault.infra.model.NameAndPwd;

/**
 * manages all connection info for mq and jdbc etc.
 * 
 * @author I075885
 *
 */
public class ConnectionInfoManager {

	static final int MQ_PORT = 5673; // 5672 for CD
	static final String MQ_HOST = "localhost";// "rabbitmq"; for CD
	static final String MQ_USER = "root";// only user that will not change
	static final String MQ_PASSWORD = "Initial0";// Random password for CD
	static final int INTERNAL_HTTP_PORT = 58080;
	static final String MQCREDSFilePath = "/etc/mqCreds";

	private Logger logger;
	private org.apache.jmeter.threads.JMeterVariables jmeterVars;

	public ConnectionInfoManager(final Logger logger, final org.apache.jmeter.threads.JMeterVariables vars) {
		this.logger = logger;
		this.jmeterVars = vars;
	}

	public String getMQUser() {
		return MQ_USER;
	}

	public int getMQPort() {
		String port = jmeterVars.get("MQTest.mqport");
		int intPort = 0;
		try {
			intPort = Integer.parseInt(port);
		} catch (NumberFormatException e) {
			logger.warn(e.getMessage(), e);
		}

		if (intPort <= 0) {
			logger.info("incorrect port from user defined variable, use 5673 as default");
			intPort = MQ_PORT;
		} else {
			logger.info("got port " + intPort + " from user defined variable...");
		}
		return intPort;
	}

	public String getMQHost() {
		String host = jmeterVars.get("MQTest.mqhost");
		if (host != null && host.length() > 0) {
			logger.info("got host " + host + " from user defined variable...");
		} else {
			logger.info("NO host " + host + " from user defined variable, user localhost as default");
			host = MQ_HOST;
		}
		return host;
	}

	public String getMQPassword() {
		String password = jmeterVars.get("MQTest.mqpassword");
		if (password != null && password.length() > 0) {

			logger.info("got password: " + password + " from user defined variable...");
			if (!password.equals(MQ_PASSWORD)) {
				logger.info("incorrect password from user defined variable, use Initial0 as default");
				password = MQ_PASSWORD;
			}

		} else {
			logger.info("no password from user defined variable, got password from file...");
			String passwordFromFile = getMQPasswordFromFile(logger);

			if (passwordFromFile != null && passwordFromFile.length() > 0) {
				password = passwordFromFile;
				logger.info("passwordFromFile is: " + passwordFromFile);
			} else {
				logger.info("no password, got password from valut...");
				password = getMQPasswordFromVault(logger);
			}
		}
		return password;
	}

	private static String getMQPasswordFromFile(Logger logger) {
		String line = null;
		// String ls = System.getProperty("line.separator");
		String content = "";
		try {
			logger.info("start to get mqpassword from file: " + MQCREDSFilePath);
			BufferedReader reader = new BufferedReader(new FileReader(MQCREDSFilePath));
			StringBuilder stringBuilder = new StringBuilder();

			while ((line = reader.readLine()) != null) {
				stringBuilder.append(line);
				// stringBuilder.append(ls);
			}
			content = stringBuilder.toString();
			logger.info("Password is: " + content);
			reader.close();

		} catch (IOException e) {
			logger.warn("Failed to get MQ Password from file.");
			logger.warn(e.getMessage(), e);
		}
		return content;
	}

	private static String getMQPasswordFromVault(Logger logger) {
		String mqPassword = "";
		try {
			ConfigurationProvider provider = ConfigurationProviderSingleton.getInstance();
			NameAndPwd mqCreds = provider.getRabbitMQCreds();
			logger.info(
					"Get user name and password from valut: " + mqCreds.getUsername() + ", " + mqCreds.getPassword());
			mqPassword = mqCreds.getPassword();
			saveMQPasswordToFile(logger, mqPassword);
		} catch (Exception e) {
			logger.warn("Failed to get MQ credential from Vault.");
			logger.warn(e.getMessage(), e);
		}
		return mqPassword;
	}

	private static void saveMQPasswordToFile(Logger logger, String password) {
		try {
			logger.info("start to save mqpassword to file: " + MQCREDSFilePath + ", mqpassword is " + password);
			BufferedWriter out = new BufferedWriter(new FileWriter(MQCREDSFilePath));
			out.write(password);
			out.close();
			logger.info("mqPassword is saved.......");
		} catch (IOException e) {
			logger.warn("failed to save mqPassword to file.");
			logger.warn(e.getMessage(), e);
		}
	}
}
