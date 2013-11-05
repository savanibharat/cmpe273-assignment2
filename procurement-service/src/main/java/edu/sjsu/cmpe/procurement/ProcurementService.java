package edu.sjsu.cmpe.procurement;
import de.spinscale.dropwizard.jobs.JobsBundle;
import edu.sjsu.cmpe.procurement.api.resources.RootResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

import edu.sjsu.cmpe.procurement.api.resources.ProcurementServiceResource;
import edu.sjsu.cmpe.procurement.config.ProcurementServiceConfiguration;
import com.yammer.dropwizard.client.JerseyClientBuilder;


public class ProcurementService extends Service<ProcurementServiceConfiguration> {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) throws Exception {
	new ProcurementService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProcurementServiceConfiguration> bootstrap) {
	bootstrap.setName("procurement-service");
	  bootstrap.addBundle(new JobsBundle("edu.sjsu.cmpe.procurement"));

    }

    @Override
    public void run(ProcurementServiceConfiguration configuration,
	    Environment environment) throws Exception {
    	
	String queueName = configuration.getStompQueueName();
	String topicName = configuration.getStompTopicName();
	String apollouser=configuration.getApolloUser();
	String apolloPassword=configuration.getApolloPassword();
	String apollohost=configuration.getApolloHost();
	String apolloPort=configuration.getApolloPort();
	
	log.debug("Queue name is {}. Topic name is {}. User is {}, password is {}. host is {}. port is {}", queueName,topicName,apollouser,apolloPassword,apollohost,apolloPort);
	// TODO: Apollo STOMP Broker URL and login
//	final Client client=new JerseyClientBuilder().using(configuration.getJerseyClientConfiguration())
//													.using(environment).build();
	environment.addResource(RootResource.class);
	environment.addResource(new ProcurementServiceResource());

    }
}
