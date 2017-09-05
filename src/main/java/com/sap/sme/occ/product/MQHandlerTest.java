package com.sap.sme.occ.product;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.log.Logger;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sap.sme.vault.infra.ConfigurationProvider;
import com.sap.sme.vault.infra.impl.ConfigurationProviderSingleton;
import com.sap.sme.vault.infra.model.NameAndPwd;

/**
 * 
 * @author I075885(changjun.hou@sap.com)
 * 
 *         <p>
 *         <h3>For quick asynchronous event test and assertion in micro service
 *         architecture.</h3>
 *         <p>
 *         <h3>Please refer to java doc for help.</h3>
 */
public class MQHandlerTest {

	private static Logger firstLogger;

	private static Connection mqConnection;
	private static Channel channel;

	private MQHandlerTest() {
	}

	/**
	 * @jmeterUsed
	 * 
	 * 
	 */
	public static boolean pubMessage(final Logger logger, final org.apache.jmeter.threads.JMeterVariables vars,
			String mesasge) {

		return false;
	}

	/**
	 * @jmeterUsed
	 * 
	 * verify if all messages generated in routingKeys are all consumed by checking asyncevent table
	 * 
	 * @param logger
	 * @param vars
	 * @param routingKeys
	 */
	public static void verifyMessageConsumed(final Logger logger, final org.apache.jmeter.threads.JMeterVariables vars,
			String... routingKeys) {

	}

	private void sendTenantProvisionDoneEvent() {
		firstLogger.info("Sending tenant provision done event start");
		try {
			ConnectionFactory rabbitFactory = new ConnectionFactory();
			rabbitFactory.setHost(MQ_HOST);
			rabbitFactory.setUsername("root");
			rabbitFactory.setPassword(MQ_PASSWORD);
			Long port = Long.valueOf(MQ_PORT);
			rabbitFactory.setPort(port.intValue());
			rabbitFactory.setRequestedHeartbeat(10);

			RabbitTemplate mqSender = new RabbitTemplate(new CachingConnectionFactory(rabbitFactory));
			mqSender.setChannelTransacted(true);

			String messageBody = "Tenant provision is done.";

			MessageProperties properties = new MessageProperties();
			Date date = new Date();
			String tenantId = System.getenv("TENANT_ID");
			properties.setHeader("X-Message-ID", "product.tenant.provision.done" + date);
			properties.setHeader("X-Trace-ID", "product.tenant.provision.done" + date);
			properties.setHeader("X-Session-ID", "product.tenant.provision.done" + date);
			properties.setHeader("X-Tenant-ID", tenantId);
			properties.setHeader("X-Message-Source", "product");
			properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);

			Message msg = new Message(messageBody.getBytes(), properties);
			// firstLogger.info("Message: {}, tenant ID: {}.", msg.toString(),
			// tenantId);
			mqSender.send("SharedExchange", "PRODUCT.TenantProvision.Done." + tenantId, msg);
		} catch (Exception ex) {
			firstLogger.warn("Failed to send tenant provision done event.", ex);
		}
		firstLogger.info("Sending tenant provision done event end");
	}

}
