package edu.sjsu.cmpe.procurement.api.resources;
import javax.ws.rs.client.WebTarget;

import com.sun.jersey.api.client.*;

public class ProcurementServiceResource {

	private final Client client;
	public ProcurementServiceResource(Client client)
	{
		this.client=client;
	}
}
