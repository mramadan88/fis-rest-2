package com.vodafone.demo.ftp;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.annotation.PostConstruct;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class DispatchREST extends RouteBuilder{

	
	@Value("${service.ftp.name}")
	private String ftpServiceName;

	@Value("${service.ftp.username}")
	private String ftpServiceUsername;
	
	@Value("${service.ftp.password}")
	private String ftpServicePassword;
	
	private String orderFTP = "ftp://"+ftpServiceUsername+":"+ftpServicePassword+"@"+ftpServiceName+"?passiveMode=true&autoCreate=true&fileName=dummy-${header.id}.txt&ftpClient=#ftpClient"; 


	@Bean(name = "ftpClient")
	FTPClient ftpClient() {
		FTPClient ftpClient = new org.apache.commons.net.ftp.FTPClient();
		// required in case of forward proxy
		ftpClient.setRemoteVerificationEnabled(false);

        return ftpClient;
    }
	
	@PostConstruct
	public void print() {
		orderFTP = "ftp://" + ftpServiceUsername + ":" + ftpServicePassword + "@" + ftpServiceName
				+ "?passiveMode=true&autoCreate=true&fileName=dummy-${header.id}.txt&ftpClient=#ftpClient";
		System.out.println("PostConstruct  ======  " + orderFTP);
	}
	
    @Override
    public void configure() {       
    	onException(Exception.class)
	    	.handled(true)
	    	.setBody(simple("Order file cannot be created"));
    	
    	
    	
		rest("/orders").description("Orders service")
		
			.post("/").type(Dispatch.class).description("Create a new Order")
			.route().routeId("insert-order").tracing()
			.log("Order Id is ${body.id}")
			.setHeader("id",simple("${body.id}"))
			.log("Order Id is ${header.id} whole body is ${body}")
			.idempotentConsumer(header("id"),
			        MemoryIdempotentRepository.memoryIdempotentRepository(200)).skipDuplicate(true)
			//		.to("bean:orderService?method=createOrder")
			// .setHeader("CamelSqlRetrieveGeneratedKeys", constant(true)) // For some reason it doesn't work
			// if it works the sql to retrieve the count will be not necessary
			.log("creating new file in FTP server")
			.log("FTP component : "+orderFTP)
			.process(new Processor() {
			    public void process(Exchange exchange) throws Exception {
			    	Dispatch payload = exchange.getIn().getBody(Dispatch.class);
			        // do something with the payload and/or exchange here
			    	InputStream is = new ByteArrayInputStream(StandardCharsets.UTF_8.encode(payload.toString()).array());
			       exchange.getIn().setBody(is);
			   }
			})
			.to(orderFTP)
			.transform(constant("OK"))
			.endRest();

    			
	}
}
