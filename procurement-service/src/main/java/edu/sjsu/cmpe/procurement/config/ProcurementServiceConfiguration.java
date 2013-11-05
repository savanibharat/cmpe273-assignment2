package edu.sjsu.cmpe.procurement.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.jersey.server.impl.container.servlet.JerseyServletContainerInitializer;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;

public class ProcurementServiceConfiguration extends Configuration {
	 	
	 @Valid
	    @NotNull
	    @JsonProperty
	    private JerseyClientConfiguration httpClient = new JerseyClientConfiguration();
	 public JerseyClientConfiguration getJerseyClientConfiguration() {
	        return httpClient;
	    }
		
		@NotEmpty
	    @JsonProperty
	    private String stompQueueName;

	 	@NotEmpty
	    @JsonProperty
	    private String stompTopicName;

		@NotEmpty
	    @JsonProperty
	    private String apolloPassword;
	    
	    @NotEmpty
	    @JsonProperty
	    private String apolloHost;
	    
	    @NotEmpty
	    @JsonProperty
	    private String apolloPort;
	    
	    @NotEmpty
	    @JsonProperty
	    private String apolloUser;
	    
	    /**
		 * @return the apolloUser
		 */
		public String getApolloUser() {
			return apolloUser;
		}

		/**
		 * @param apolloUser the apolloUser to set
		 */
		public void setApolloUser(String apolloUser) {
			this.apolloUser = apolloUser;
		}

		/**
		 * @return the apolloPassword
		 */
		public String getApolloPassword() {
			return apolloPassword;
		}

		/**
		 * @param apolloPassword the apolloPassword to set
		 */
		public void setApolloPassword(String apolloPassword) {
			this.apolloPassword = apolloPassword;
		}

		/**
		 * @return the apolloHost
		 */
		public String getApolloHost() {
			return apolloHost;
		}

		/**
		 * @param apolloHost the apolloHost to set
		 */
		public void setApolloHost(String apolloHost) {
			this.apolloHost = apolloHost;
		}

		/**
		 * @return the apolloPort
		 */
		public String getApolloPort() {
			return apolloPort;
		}

		/**
		 * @param apolloPort the apolloPort to set
		 */
		public void setApolloPort(String apolloPort) {
			this.apolloPort = apolloPort;
		}
	    
	   
	    /**
	     * @return the stompQueueName
	     */
	    public String getStompQueueName() {
		return stompQueueName;
	    }

	    /**
	     * @param stompQueueName
	     *            the stompQueueName to set
	     */
	    public void setStompQueueName(String stompQueueName) {
		this.stompQueueName = stompQueueName;
	    }

	    /**
	     * @return the stompTopicName
	     */
	    public String getStompTopicName() {
		return stompTopicName;
	    }

	    /**
	     * @param stompTopicName
	     *            the stompTopicName to set
	     */
	    public void setStompTopicName(String stompTopicName) {
		this.stompTopicName = stompTopicName;
	    }
}
