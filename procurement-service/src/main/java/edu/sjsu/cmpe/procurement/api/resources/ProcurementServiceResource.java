package edu.sjsu.cmpe.procurement.api.resources;

import javax.ws.rs.client.ClientBuilder;

import com.sun.jersey.api.client.Client;

public class ProcurementServiceResource {

	private final Client client;
	public ProcurementServiceResource(Client client)
	{
		this.client=client;
	}
	ClientBuilder
}
